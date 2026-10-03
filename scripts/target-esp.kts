name("Target ESP")
description("Кольцо или спирали свечения вокруг цели Attack Aura")

requireApi(2)

val RING = "Кольцо"
val SPIRALS = "Спирали"
val ONE = "Один цвет"
val GRADIENT = "Градиент"
val RAINBOW = "Радуга"

val mode by selectable("Режим", RING, SPIRALS, selected = RING)
val colourMode by selectable("Цвет", ONE, GRADIENT, RAINBOW, selected = GRADIENT)
val colour1 by colorPicker("Первый цвет", 0xFF8A5CFFL)
val colour2 by colorPicker("Второй цвет", 0xFF00E5FFL).visibleWhen { colourMode == GRADIENT }
val power by slider("Интенсивность", 1f, 0.1f, 3f, 0.05f)
val speed by slider("Скорость", 1f, 0.1f, 4f, 0.05f)
val throughWalls by checkBox("Сквозь стены", true)
val crosshairToo by checkBox("Цель под прицелом, если аура молчит", false)

val trail by slider("Кольцо: длина шлейфа", 8, 0, 30, 1).visibleWhen { mode == RING }
val thick by slider("Кольцо: толщина", 0.26f, 0.05f, 0.8f, 0.01f).visibleWhen { mode == RING }
val ringRadius by slider("Кольцо: радиус", 1.05f, 0.5f, 2f, 0.05f).visibleWhen { mode == RING }
val density by slider("Кольцо: плотность", 44, 16, 128, 1).visibleWhen { mode == RING }
val spin by slider("Кольцо: вращение", 0.6f, 0f, 3f, 0.05f).visibleWhen { mode == RING }
val period by slider("Кольцо: время хода, с", 2.2f, 0.6f, 6f, 0.1f).visibleWhen { mode == RING }
val core by checkBox("Кольцо: яркое ядро", true).visibleWhen { mode == RING }

val strands by slider("Спирали: количество", 4, 1, 8, 1).visibleWhen { mode == SPIRALS }
val length by slider("Спирали: длина", 14, 4, 40, 1).visibleWhen { mode == SPIRALS }
val spiralSize by slider("Спирали: размер", 0.4f, 0.1f, 1f, 0.02f).visibleWhen { mode == SPIRALS }
val spiralRadius by slider("Спирали: радиус", 1f, 0.5f, 2f, 0.05f).visibleWhen { mode == SPIRALS }

// A soft round glow drawn by the shader on every camera-facing quad - the same look as glow.png,
// added on top of the scene so overlapping sprites brighten into a band.
val glowShader = shader("target_esp_glow", """
    #version 330
    in vec2 v_uv;
    in vec4 v_color;
    out vec4 out_color;
    void main() {
        float d = length(v_uv * 2.0 - 1.0);
        if (d >= 1.0) discard;
        float g = pow(1.0 - d, 2.2) + 0.35 * exp(-d * d * 18.0);
        out_color = vec4(v_color.rgb, v_color.a * g);
    }
""", """
    #version 330
    layout(location = 0) in vec3 pos;
    layout(location = 1) in vec2 uv;
    layout(location = 2) in vec4 color;
    uniform mat4 u_view;
    uniform mat4 u_projection;
    out vec2 v_uv;
    out vec4 v_color;
    void main() {
        v_uv = uv;
        v_color = color.bgra;
        gl_Position = u_projection * u_view * vec4(pos, 1.0);
    }
""")

val format = gpu.format(VertexAttribute.floats(3), VertexAttribute.floats(2), VertexAttribute.color())
val mesh = gpu.indexedMesh(format)
val passWalls = gpu.renderType(gpu.pipeline(glowShader, DrawMode.TRIANGLES, BlendMode.ADDITIVE, DepthMode.OFF), mesh)
val passDepth = gpu.renderType(gpu.pipeline(glowShader, DrawMode.TRIANGLES, BlendMode.ADDITIVE, DepthMode.TEST), mesh)

var auraTarget: Entity? = null
var shown: Entity? = null
var fade = 0f
var lastTime = -1.0

var hitTarget: Entity? = null
var hitAt = 0.0

on<TargetUpdateEvent> { e -> auraTarget = e.target() }
// any hit also marks its target for a moment, so it shows even when the aura's target event is quiet
on<AttackEvent> { e -> hitTarget = e.target(); hitAt = seconds() }
onDisable { auraTarget = null; shown = null; fade = 0f }

fun seconds(): Double {
    val now = java.time.Instant.now()
    return (now.epochSecond % 100000L) + now.nano / 1.0e9
}

fun currentTarget(): Entity? {
    val a = auraTarget
    if (a != null && a.alive()) return a
    val h = hitTarget
    if (h != null && h.alive() && seconds() - hitAt < 1.5) return h
    if (crosshairToo) {
        val ent = raycast.entityAtCrosshair(6.0, filters.player().and(filters.attackable()))
        if (ent != null && !ent.isSelf()) return ent
    }
    return null
}

fun mix(a: Int, b: Int, t: Float): Int {
    val r = ((a shr 16 and 255) + ((b shr 16 and 255) - (a shr 16 and 255)) * t).toInt()
    val g = ((a shr 8 and 255) + ((b shr 8 and 255) - (a shr 8 and 255)) * t).toInt()
    val bl = ((a and 255) + ((b and 255) - (a and 255)) * t).toInt()
    return (r shl 16) or (g shl 8) or bl
}

fun hue(h: Double): Int {
    val x = ((h % 1.0) + 1.0) % 1.0 * 6.0
    fun ch(o: Double): Int {
        val v = (Math.abs(((x + o) % 6.0) - 3.0) - 1.0).coerceIn(0.0, 1.0)
        // saturation 0.75, like the Java ring
        return ((0.25 + 0.75 * v) * 255).toInt()
    }
    return (ch(0.0) shl 16) or (ch(4.0) shl 8) or ch(2.0)
}

fun colourAt(ang: Double, now: Double): Int = when (colourMode) {
    RAINBOW -> hue(ang / (Math.PI * 2.0) + now * 0.15)
    GRADIENT -> mix(colour1 and 0xFFFFFF, colour2 and 0xFFFFFF, 0.5f + 0.5f * Math.sin(ang + now * 1.5).toFloat())
    else -> colour1 and 0xFFFFFF
}

// camera axes in world space, read off the view matrix (column-major)
var rx = 1f; var ry = 0f; var rz = 0f
var ux = 0f; var uy = 1f; var uz = 0f

fun sprite(x: Double, y: Double, z: Double, size: Float, alpha: Float, rgb: Int) {
    val a = (255f * alpha.coerceIn(0f, 1f)).toInt()
    if (a <= 0) return
    val argb = (a shl 24) or rgb
    val h = size / 2f
    val px = x.toFloat(); val py = y.toFloat(); val pz = z.toFloat()
    val v = mesh.verts()
    val base = v.putVec3(px + (-rx - ux) * h, py + (-ry - uy) * h, pz + (-rz - uz) * h).putUv(0f, 0f).putColor(argb).next()
    v.putVec3(px + (rx - ux) * h, py + (ry - uy) * h, pz + (rz - uz) * h).putUv(1f, 0f).putColor(argb).next()
    v.putVec3(px + (rx + ux) * h, py + (ry + uy) * h, pz + (rz + uz) * h).putUv(1f, 1f).putColor(argb).next()
    v.putVec3(px + (-rx + ux) * h, py + (-ry + uy) * h, pz + (-rz + uz) * h).putUv(0f, 1f).putColor(argb).next()
    mesh.idx()?.putQuad(base, base + 1, base + 2, base + 3)
}

/** 0 at the feet, 1 at the head: period seconds up and back, eased at both ends. */
fun ringHeight(time: Double, period: Float): Double {
    val u = ((time / Math.max(0.2f, period)) % 1.0 + 1.0) % 1.0
    val s = if (u < 0.5) u * 2.0 else 2.0 - u * 2.0
    return 0.5 - 0.5 * Math.cos(Math.PI * s)
}

// per sprite values shared by every trail copy - worked out once a frame, not once per copy
var ringCos = DoubleArray(0)
var ringSin = DoubleArray(0)
var ringRgb = IntArray(0)
var ringShimmer = FloatArray(0)

fun drawRing(x: Double, y: Double, z: Double, height: Float, width: Float, now: Double) {
    val radius = width * ringRadius
    val sprites = Math.max(8, density.toInt())
    if (ringCos.size != sprites) {
        ringCos = DoubleArray(sprites); ringSin = DoubleArray(sprites)
        ringRgb = IntArray(sprites); ringShimmer = FloatArray(sprites)
    }
    for (i in 0 until sprites) {
        val ang = i * (Math.PI * 2.0 / sprites) + now * spin
        ringCos[i] = x + Math.cos(ang) * radius
        ringSin[i] = z + Math.sin(ang) * radius
        ringRgb[i] = colourAt(ang, now)
        ringShimmer[i] = 0.72f + 0.28f * Math.sin(ang * 3.0 + now * 4.0).toFloat()
    }
    val k0 = trail.toInt()
    for (k in k0 downTo 0) {
        val at = now - k * 0.035
        val ringY = y + 0.05 + (height - 0.10) * ringHeight(at, period)
        val tail = 1f - k.toFloat() / (k0 + 1)
        val a = fade * Math.min(1f, power) * (if (k == 0) 1f else 0.55f * tail * tail)
        if (a < 0.01f) continue
        val size = thick * (if (k == 0) 1f else 0.77f + 0.2f * tail) * (0.8f + 0.2f * Math.min(2f, power))
        for (i in 0 until sprites) {
            sprite(ringCos[i], ringY, ringSin[i], size, a * ringShimmer[i], ringRgb[i])
            if (k == 0 && core) sprite(ringCos[i], ringY, ringSin[i], size * 0.45f, fade * 0.8f * ringShimmer[i], mix(ringRgb[i], 0xFFFFFF, 0.55f))
        }
    }
}

fun drawSpirals(x: Double, y: Double, z: Double, height: Float, width: Float, now: Double) {
    val t = now
    val turn = (t * 3.0) % (Math.PI * 2.0)
    val steps = length.toInt()
    val span = Math.toRadians(60.0 * steps / 14.0)
    val count = strands.toInt()
    val layers = Math.max(1, Math.ceil(power.toDouble()).toInt())
    val alpha = fade * Math.min(1f, power / layers)
    val radius = width * spiralRadius
    val mid = height / 2f + 0.2f * (height / 1.8f)
    for (layer in 0 until layers) for (s in 0 until count) {
        val phase = t + s * 15.0
        val offset = s * (Math.PI * 2.0 / count)
        for (i in 0..steps) {
            val along = i * span / steps
            val ang = along + turn + offset
            val sx = x + radius * Math.cos(ang)
            val sy = y + Math.sin(phase + along + s) * 0.7 * (height / 1.8f) + mid
            val sz = z + radius * Math.sin(ang)
            val size = spiralSize * (0.5f + i.toFloat() / steps)
            sprite(sx, sy, sz, size, alpha, colourAt(ang, t))
        }
    }
}

on<Render3DEvent> { e ->
    if (!inGame) return@on
    // a draw that never happened (shader not built yet) leaves last frame's vertices in the mesh
    mesh.clear()
    val now = seconds()
    val dt = if (lastTime < 0) 0.0 else (now - lastTime).coerceIn(0.0, 0.1)
    lastTime = now

    val live = currentTarget()
    if (live != null) shown = live
    fade += ((if (live != null) 1f else 0f) - fade) * (1f - Math.exp(-dt / 0.12).toFloat())
    val ent = shown ?: return@on
    if (fade < 0.01f || !ent.alive()) {
        if (live == null) shown = null
        return@on
    }

    val view = e.viewMatrix()
    val identity = view[0] == 1f && view[5] == 1f && view[10] == 1f
    if (identity) {
        // no rotation in the view matrix: build the axes from the player's look
        val yaw = Math.toRadians(player.yaw().toDouble())
        val pitch = Math.toRadians(player.pitch().toDouble())
        rx = -Math.cos(yaw).toFloat(); ry = 0f; rz = -Math.sin(yaw).toFloat()
        ux = (-Math.sin(pitch) * -Math.sin(yaw)).toFloat(); uy = Math.cos(pitch).toFloat(); uz = (-Math.sin(pitch) * Math.cos(yaw)).toFloat()
    } else {
        rx = view[0]; ry = view[4]; rz = view[8]
        ux = view[1]; uy = view[5]; uz = view[9]
    }

    val cam = e.camera()
    val p = ent.renderPosition()
    val x = p.x() - cam.x()
    val y = p.y() - cam.y()
    val z = p.z() - cam.z()
    val speedNow = now * speed
    if (mode == RING) drawRing(x, y, z, ent.height(), ent.width(), speedNow)
    else drawSpirals(x, y, z, ent.height(), ent.width(), speedNow)

    glowShader.setMat4("u_view", view)
    glowShader.setMat4("u_projection", e.projectionMatrix())
    if (throughWalls) passWalls.draw() else passDepth.draw()
}
