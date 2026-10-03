name("Trails")
description("Шлейф за игроками: полупрозрачная стена с яркими кромками сверху и снизу")

val showPlayers = entry("players")
val showFriends = entry("friends", selected = true)
val showInvisibles = entry("invisibles")
val showSelf = entry("self", selected = true)

val show = combo("Show", showPlayers, showFriends, showInvisibles, showSelf)
val trailLength by slider("Length", 2f, 2f, 4f, 0.5f)
val trailColor by colorPicker("Color", 0xFF8BACFF)

val trailShader = shader(
    "trails-ribbon",
    fragmentSource = """
        #version 330

        in vec4 v_color;
        out vec4 out_color;

        void main() {
            out_color = v_color;
        }
    """,
    vertexSource = """
        #version 330

        layout(location = 0) in vec3 a_pos;
        layout(location = 1) in vec4 a_color;

        out vec4 v_color;

        uniform mat4 u_view;
        uniform mat4 u_projection;

        void main() {
            gl_Position = u_projection * u_view * vec4(a_pos, 1.0);
            v_color = a_color.bgra;
        }
    """
)

val trailFormat = gpu.format(VertexAttribute.floats(3), VertexAttribute.color())
val trailMesh = gpu.indexedMesh(trailFormat)
val trailPass = gpu.renderType(
    gpu.pipeline(trailShader, DrawMode.TRIANGLES, BlendMode.ALPHA, DepthMode.TEST),
    trailMesh
)

class Trail(private val capacity: Int = 256) {

    private val xs = DoubleArray(capacity)
    private val ys = DoubleArray(capacity)
    private val zs = DoubleArray(capacity)
    private val spawn = LongArray(capacity)
    private val ages = FloatArray(capacity)

    private var head = 0

    var size = 0
        private set

    var stamp = 0L

    private fun slot(index: Int) = (head + index) % capacity

    fun push(x: Double, y: Double, z: Double, time: Long) {
        if (size == capacity) {
            head = (head + 1) % capacity
            size--
        }
        val at = slot(size)
        xs[at] = x
        ys[at] = y
        zs[at] = z
        spawn[at] = time
        ages[at] = 0f
        size++
    }

    fun dropExpired(now: Long, lifetime: Float) {
        while (size > 0 && (now - spawn[head]).toFloat() > lifetime) {
            head = (head + 1) % capacity
            size--
        }
    }

    fun x(index: Int) = xs[slot(index)]

    fun y(index: Int) = ys[slot(index)]

    fun z(index: Int) = zs[slot(index)]

    fun spawnedAt(index: Int) = spawn[slot(index)]

    fun age(index: Int) = ages[slot(index)]

    fun age(index: Int, value: Float) {
        ages[slot(index)] = value
    }
}

val trails = HashMap<Int, Trail>()

var frame = 0L
var shaderReported = false

fun channel(value: Int) = value.coerceIn(0, 255)

fun argb(red: Int, green: Int, blue: Int, alpha: Int): Int =
    (channel(alpha) shl 24) or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue)

fun darker(color: Int, factor: Float): Int = argb(
    (Colors.red(color) * factor).toInt(),
    (Colors.green(color) * factor).toInt(),
    (Colors.blue(color) * factor).toInt(),
    Colors.alpha(color)
)

fun brighter(color: Int, factor: Float): Int {
    var red = Colors.red(color)
    var green = Colors.green(color)
    var blue = Colors.blue(color)

    val floor = (1f / (1f - factor)).toInt()

    if (red == 0 && green == 0 && blue == 0) {
        return argb(floor, floor, floor, Colors.alpha(color))
    }
    if (red in 1 until floor) {
        red = floor
    }
    if (green in 1 until floor) {
        green = floor
    }
    if (blue in 1 until floor) {
        blue = floor
    }

    return argb(
        (red / factor).toInt(),
        (green / factor).toInt(),
        (blue / factor).toInt(),
        Colors.alpha(color)
    )
}

fun lerpChannel(from: Int, to: Int, ratio: Float) = (from + ratio * (to - from)).toInt()

fun lerpColors(first: Int, second: Int, amount: Float): Int {
    val ratio = amount.coerceIn(0f, 1f)
    return argb(
        lerpChannel(Colors.red(first), Colors.red(second), ratio),
        lerpChannel(Colors.green(first), Colors.green(second), ratio),
        lerpChannel(Colors.blue(first), Colors.blue(second), ratio),
        lerpChannel(Colors.alpha(first), Colors.alpha(second), ratio)
    )
}

fun blendColors(speed: Int, index: Int, start: Int, end: Int, now: Long): Int {
    var angle = ((now / speed + index) % 360).toInt()
    angle = (if (angle >= 180) 360 - angle else angle) * 2
    return lerpColors(start, end, angle / 360f)
}

fun wall(
    verts: VertexWriter,
    indices: IndexWriter,
    fromX: Float,
    fromZ: Float,
    fromBottom: Float,
    fromTop: Float,
    toX: Float,
    toZ: Float,
    toBottom: Float,
    toTop: Float,
    fromColor: Int,
    toColor: Int
) {
    val base = verts.vertexCount()
    verts.putVec3(fromX, fromBottom, fromZ).putColor(fromColor).next()
    verts.putVec3(fromX, fromTop, fromZ).putColor(fromColor).next()
    verts.putVec3(toX, toTop, toZ).putColor(toColor).next()
    verts.putVec3(toX, toBottom, toZ).putColor(toColor).next()
    indices.putQuad(base, base + 1, base + 2, base + 3)
}

fun valid(target: PlayerEntity): Boolean {
    if (target.isSelf()) {
        return show.has(showSelf)
    }
    if (!show.has(showInvisibles) && target.invisible()) {
        return false
    }
    if (show.has(showFriends) && target.isFriend()) {
        return true
    }
    return show.has(showPlayers) && !target.isBot() && !target.isFriend()
}

fun sweep(current: Long) {
    val iterator = trails.values.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().stamp != current) {
            iterator.remove()
        }
    }
}

onDisable {
    trails.clear()
}

on<Render3DEvent> { event ->
    if (!inGame) {
        return@on
    }

    val failure = trailShader.error()
    if (failure != null && !shaderReported) {
        shaderReported = true
        log.error("шейдер следа не собрался: $failure")
    }

    val indices = trailMesh.idx() ?: return@on
    val verts = trailMesh.verts()

    val camera = event.camera()
    val cameraX = camera.x()
    val cameraY = camera.y()
    val cameraZ = camera.z()

    val now = client.millis()
    val span = trailLength * 100f
    val lifetime = span + 200f
    val firstPerson = gameSettings.perspective().firstPerson()

    val bright = brighter(trailColor, 0.8f)
    val dark = darker(trailColor, 0.5f)

    frame++
    var touched = 0

    var lastColor = 0

    for (target in world.players()) {
        if (!valid(target)) {
            continue
        }

        val trail = trails.getOrPut(target.id()) { Trail() }
        trail.stamp = frame
        touched++

        val position = target.renderPosition()
        val headX = position.x()
        val headY = position.y()
        val headZ = position.z()

        if (!(target.isSelf() && firstPerson) && !target.isBot()) {
            trail.push(headX, headY, headZ, now)
        }

        trail.dropExpired(now, lifetime)

        val height = if (target.sneaking()) target.height() - 0.15f else target.height()
        val edge = height * 0.01f

        var lastX = headX
        var lastY = headY
        var lastZ = headZ

        for (index in 0 until trail.size) {
            val age = trail.age(index).toInt()
            val color = Colors.withAlpha(
                blendColors(4, age, bright, dark, now),
                (180 * (180 - age) / 180f).toInt()
            )

            val fromX = (lastX - cameraX).toFloat()
            val fromY = (lastY - cameraY).toFloat()
            val fromZ = (lastZ - cameraZ).toFloat()

            val toX = (trail.x(index) - cameraX).toFloat()
            val toY = (trail.y(index) - cameraY).toFloat()
            val toZ = (trail.z(index) - cameraZ).toFloat()

            wall(
                verts, indices,
                fromX, fromZ, fromY, fromY + height,
                toX, toZ, toY, toY + height,
                lastColor, color
            )

            wall(
                verts, indices,
                fromX, fromZ, fromY, fromY + edge,
                toX, toZ, toY, toY + edge,
                lastColor, color
            )

            wall(
                verts, indices,
                fromX, fromZ, fromY + height, fromY + height - edge,
                toX, toZ, toY + height, toY + height - edge,
                lastColor, color
            )

            lastX = trail.x(index)
            lastY = trail.y(index)
            lastZ = trail.z(index)
            lastColor = color

            trail.age(index, ((now - trail.spawnedAt(index)).toFloat() / span).coerceIn(0f, 1f) * 180f)
        }
    }

    if (touched != trails.size) {
        sweep(frame)
    }

    trailShader.setMat4("u_view", event.viewMatrix())
    trailShader.setMat4("u_projection", event.projectionMatrix())

    trailPass.draw()
}
