import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

name("Manul")
description("Манул ходит за тобой")
requireApi(2)

val coat by selectable("Окрас", "Серый", "Рыжий", selected = "Серый")
val followDist by slider("Дистанция", 0.95f, 0.25f, 2.5f, 0.05f)
val sideOffset by slider("Сбоку", 0.95f, 0.35f, 2.5f, 0.05f)
val sideHand by selectable("Сторона", "Справа", "Слева", selected = "Справа")
val modelScale by slider("Размер", 1.0f, 0.5f, 2.0f, 0.05f)
val sitSneak by checkBox("Сидит при sneak", true)

val TEX_W = 128f
val TEX_H = 64f
val BODY_Y = 14f
val HEAD_Y = 16.5f
val HEAD_Z = -5.5f
val LEG_Y = 21f
val WHITE = -1
val PX = 1f / 16f
val Y_LIFT = 1.501f

class Bone(val parent: Bone?) {
    var x = 0f
    var y = 0f
    var z = 0f
    var xRot = 0f
    var yRot = 0f
    var zRot = 0f
    var rx = 0f
    var ry = 0f
    var rz = 0f
    var rXRot = 0f
    var rYRot = 0f
    var rZRot = 0f

    fun rest(px: Float, py: Float, pz: Float, xr: Float = 0f, yr: Float = 0f, zr: Float = 0f): Bone {
        rx = px
        ry = py
        rz = pz
        rXRot = xr
        rYRot = yr
        rZRot = zr
        reset()
        return this
    }

    fun reset() {
        x = rx
        y = ry
        z = rz
        xRot = rXRot
        yRot = rYRot
        zRot = rZRot
    }

    fun toModel(px: Float, py: Float, pz: Float, out: FloatArray) {
        var cx = px
        var cy = py
        var cz = pz
        var b: Bone? = this
        while (b != null) {
            val xr = b.xRot
            val yr = b.yRot
            val zr = b.zRot
            if (xr != 0f) {
                val c = cos(xr)
                val s = sin(xr)
                val ny = cy * c - cz * s
                val nz = cy * s + cz * c
                cy = ny
                cz = nz
            }
            if (yr != 0f) {
                val c = cos(yr)
                val s = sin(yr)
                val nx = cx * c + cz * s
                val nz = -cx * s + cz * c
                cx = nx
                cz = nz
            }
            if (zr != 0f) {
                val c = cos(zr)
                val s = sin(zr)
                val nx = cx * c - cy * s
                val ny = cx * s + cy * c
                cx = nx
                cy = ny
            }
            cx += b.x
            cy += b.y
            cz += b.z
            b = b.parent
        }
        out[0] = cx
        out[1] = cy
        out[2] = cz
    }
}

val body = Bone(null).rest(0f, BODY_Y, 0f)
val skirt = Bone(body).rest(0f, 4f, 0f)
val tail = Bone(body).rest(0f, 2.5f, 6.5f, 0.55f)
val tailTip = Bone(tail).rest(0f, 0f, 8f, 0.35f)
val head = Bone(null).rest(0f, HEAD_Y, HEAD_Z)
val muzzle = Bone(head).rest(0f, 0f, -6f)
val leftCheek = Bone(head).rest(4.5f, 0f, -5.5f, 0f, 0f, -0.14f)
val rightCheek = Bone(head).rest(-4.5f, 0f, -5.5f, 0f, 0f, 0.14f)
val leftEar = Bone(head).rest(4.5f, -2f, -2.5f, 0f, 0f, 0.45f)
val rightEar = Bone(head).rest(-4.5f, -2f, -2.5f, 0f, 0f, -0.45f)
val fl = Bone(null).rest(2.5f, LEG_Y, -4.5f)
val fr = Bone(null).rest(-2.5f, LEG_Y, -4.5f)
val hl = Bone(null).rest(2.5f, LEG_Y, 4.5f)
val hr = Bone(null).rest(-2.5f, LEG_Y, 4.5f)

val allBones = arrayOf(
    body, skirt, tail, tailTip, head, muzzle,
    leftCheek, rightCheek, leftEar, rightEar, fl, fr, hl, hr
)

val format = gpu.format(
    VertexAttribute.floats(3),
    VertexAttribute.floats(2),
    VertexAttribute.color()
)
val mesh = gpu.indexedMesh(format)
val manulShader = shader(
    "manul_mesh",
    """
    #version 330
    in vec2 in_uv;
    in vec4 in_color;
    out vec4 out_color;
    uniform sampler2D u_tex;
    void main() {
        vec4 t = texture(u_tex, in_uv);
        if (t.a < 0.01) discard;
        out_color = t * in_color;
    }
    """.trimIndent(),
    """
    #version 330
    layout(location=0) in vec3 pos;
    layout(location=1) in vec2 uv;
    layout(location=2) in vec4 color;
    out vec2 in_uv;
    out vec4 in_color;
    uniform mat4 u_view;
    uniform mat4 u_projection;
    void main() {
        in_uv = uv;
        in_color = color.bgra;
        gl_Position = u_projection * u_view * vec4(pos, 1.0);
    }
    """.trimIndent()
)
val pass = gpu.renderType(
    gpu.pipeline(manulShader, DrawMode.TRIANGLES, BlendMode.ALPHA, DepthMode.TEST_AND_WRITE),
    mesh
)

val texGray = image(
    "manul",
    base64("iVBORw0KGgoAAAANSUhEUgAAAIAAAABACAYAAADS1n9/AAACUElEQVR42u2bMU7DMBiFfQokJCQkBg7B0IGJqWMnxMiEGBEjCwwcAHGEigN0YOiCxAJzmZAYOnGJIiOlMsZ2bCeOneT7pCeH0Dhu/udnA0Us5zebUD0v5rVaf77VKubeQuPr42VjUltjtPW/el/8k96v6TWx19nGYxu38KVNAzzcXWCAsRhAFtum0g1wOp301gBSs+OjX7nGnSUBSlsCqkIPKQGkqomW1QB9SQCXCfpqAB+RABn2AD5Fjr1OpGLICaDOftNxE/PIPnRVRTN9r+l1+jPXx9O5AUpJANNDq/S6vNrs7e78Ka78Wp53Xefz/rpOAKnH28utdEOM1gC2WSwLWbXqPdXzMQlQzb4cCVCUAUpfApoawBSzOROguD1A3QzJvQT43L9uCXCZQHRMcXuA0hOgyQ7fZ4nLYQD2AAYDhI41dAyHB/uN9HR/tpXa7+xkspX6ms4N4FPMkjRWAySNFn3NU9cY0zlbdNpi1LffFAlwfT51avQGUAcli6G2tnM+BojpV30QNvU5AWLeX3LUm8tiqK3tnM/6HtNvCgO0nQD69XUG+F6vgjRqA7h+srBtdrpOgMEZwOeh6/IxQKp++5YAonRC/8yYW31LgOINELpJMe1oXfItbNNxVLNZb13JYGtViaGDATAABsAAGAAD9NQAAjAAYADAAIABAAMABgAMABgAMAAEGED+2lM/1lv9OESp+qeCop0PhMgHrx/rrX4colT9U0Exrg+ERP8fPGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIBh8AMmE/rgNHM8OQAAAABJRU5ErkJggg==")
)
val texGinger = image(
    "manul_ginger",
    base64("iVBORw0KGgoAAAANSUhEUgAAAIAAAABACAYAAADS1n9/AAACTklEQVR42u2bMU7DMBiFfQiGDoiBiRExoE6IkZGBAyCEOjBzAmZGhp6h4gBMiKli6hEYOjMgLlBkpFTmx3Zsp06c5PukJ4fQOG7+52cDRb0/Xm1itZzPavX18VqrlHsrwedqsbFpV2N09b9+m/+T7Nf2mtTrXONxjVuFsksDPF1PMcBYDKCL7VLpBriZHvbWAFp3Zye/8o27kwQobQmoCj2kBNCqJlqnBuhLAvhM0FcDhIgE6GAPEFLk1OtULoacAObstx03MY/uQ6oqmu17Ta+Tz1yOp3UDlJIAtodWafUy2+xP9v4UV3+tz/uuC3l/bSeA1uL+citpiNEawDWLdSGr1ryneT4lAarZ10UCFGWA0peApgawxWyXCVDcHqBuhnS9BITcv24J8JlAtUxxe4DSE6DJDj9kievCAOwBLAaIHWvsGI4OJo30PDvdyuz39vx4K/M1rRsgpJglaawGyBotcs0z1xjbOVd0umI0tN8cCfBwceTV6A1gDkoXw2xd50IMkNKv+SBc6nMCpLy/7Jg318UwW9e5kPU9pd8cBth1Asjr6wzwvV5GadQG8P1k4drstJ0AgzNAyEOXCjFArn77lgCqdGL/zNi1+pYAxRsgdpNi29H6FFrYpuOoZrNsfcngak2poYMBMAAGwAAYAAP01AAKMABgAMAAgAEAAwAGAAwAGAAwAEQYQP/aUx7LVh7HKFf/VFDt5gMh+sHLY9nK4xjl6p8KqnF9ICT5/+ABAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADIMfo3DI+nc4ekAAAAAASUVORK5CYII=")
)

var spawned = false
var mx = 0.0
var my = 0.0
var mz = 0.0
var px = 0.0
var py = 0.0
var pz = 0.0
var yaw = 0f
var prevYaw = 0f
var moveYaw = 0f
var groundY = 0.0
var haveGround = false
var walkPos = 0f
var walkSpeed = 0f
var ticks = 0
var missingLogged = false
var settled = false
val tmp = FloatArray(3)
val worldTmp = FloatArray(3)
val Y_STEP = 0.06

fun hypot2(ax: Double, az: Double): Double {
    return sqrt(ax * ax + az * az)
}

fun wrapDeg(v: Float): Float {
    var a = v % 360f
    if (a >= 180f) a -= 360f
    if (a < -180f) a += 360f
    return a
}

fun lerpAngle(from: Float, to: Float, t: Float): Float {
    return from + wrapDeg(to - from) * t
}

fun toDegrees(rad: Double): Float {
    return (rad * 180.0 / PI).toFloat()
}

fun resetFollow() {
    spawned = false
    settled = false
    haveGround = false
    walkPos = 0f
    walkSpeed = 0f
}

fun stepFloor(from: Double, to: Double): Double {
    val gap = to - from
    return when {
        gap > Y_STEP -> from + Y_STEP
        gap < -Y_STEP -> from - Y_STEP
        else -> to
    }
}

fun floorUnder(x: Double, startY: Double, z: Double): Double {
    val bx = floor(x).toInt()
    val bz = floor(z).toInt()
    var y = floor(startY).toInt()
    var n = 0
    while (n < 16) {
        val b = world.block(bx, y, bz)
        if (b.hasCollision() && !b.liquid()) {
            return (y + 1).toDouble()
        }
        y--
        n++
    }
    return startY
}

fun petSlotX(originX: Double, yawDeg: Float): Double {
    val rad = yawDeg * PI / 180.0
    val fx = -sin(rad)
    val rx = -cos(rad)
    val side = if (sideHand == "Слева") -sideOffset.toDouble() else sideOffset.toDouble()
    return originX + rx * side + fx * followDist.toDouble()
}

fun petSlotZ(originZ: Double, yawDeg: Float): Double {
    val rad = yawDeg * PI / 180.0
    val fz = cos(rad)
    val rz = -sin(rad)
    val side = if (sideHand == "Слева") -sideOffset.toDouble() else sideOffset.toDouble()
    return originZ + rz * side + fz * followDist.toDouble()
}

fun setupPose(sitting: Boolean, limb: Float, speed: Float, idle: Float) {
    for (b in allBones) b.reset()

    val phase = limb * 1.3f
    val amount = speed.coerceAtMost(1f) * 0.95f
    val pi = PI.toFloat()
    if (!sitting) {
        body.zRot = sin(phase) * amount * 0.04f
    }

    fl.xRot = cos(phase) * amount
    fr.xRot = cos(phase + pi) * amount
    hl.xRot = cos(phase + pi) * amount
    hr.xRot = cos(phase) * amount

    tail.yRot = sin(idle * 0.07f) * (0.18f + amount * 0.12f)
    tail.xRot = 0.55f + sin(idle * 0.05f) * 0.08f
    tailTip.yRot = sin(idle * 0.07f - 0.9f) * (0.22f + amount * 0.1f)
    tailTip.xRot = 0.35f + sin(idle * 0.05f - 0.9f) * 0.1f

    head.yRot = sin(idle * 0.035f) * 0.06f
    head.xRot = sin(idle * 0.04f) * 0.04f

    if (!sitting) return

    body.y = BODY_Y + 2f
    body.xRot = -0.35f
    body.zRot = 0f
    head.y = HEAD_Y + 0.5f
    head.z = HEAD_Z - 0.5f
    fl.xRot = 0f
    fr.xRot = 0f
    hl.xRot = -1.4f
    hr.xRot = -1.4f
    hl.y = LEG_Y + 2f
    hr.y = LEG_Y + 2f
    hl.z = 5.5f
    hr.z = 5.5f
    tail.xRot = 0.15f
    tail.yRot = 0.7f + sin(idle * 0.05f) * 0.04f
    tailTip.xRot = 0.1f
    tailTip.yRot = 0.5f
}

fun modelToCamera(
    mxp: Float,
    myp: Float,
    mzp: Float,
    originX: Float,
    originY: Float,
    originZ: Float,
    yawDeg: Float,
    scl: Float,
    camX: Float,
    camY: Float,
    camZ: Float,
    out: FloatArray
) {
    val bx = mxp * PX
    val by = myp * PX
    val bz = mzp * PX
    val lx = -bx * scl
    val ly = (-by + Y_LIFT) * scl
    val lz = bz * scl
    val rad = ((180.0 - yawDeg.toDouble()) * PI / 180.0)
    val c = cos(rad).toFloat()
    val s = sin(rad).toFloat()
    val rx = lx * c - lz * s
    val rz = lx * s + lz * c
    out[0] = originX + rx - camX
    out[1] = originY + ly - camY
    out[2] = originZ + rz - camZ
}

fun emitVert(
    verts: VertexWriter,
    bone: Bone,
    lx: Float,
    ly: Float,
    lz: Float,
    u: Float,
    v: Float,
    originX: Float,
    originY: Float,
    originZ: Float,
    yawDeg: Float,
    scl: Float,
    camX: Float,
    camY: Float,
    camZ: Float
): Int {
    bone.toModel(lx, ly, lz, tmp)
    modelToCamera(tmp[0], tmp[1], tmp[2], originX, originY, originZ, yawDeg, scl, camX, camY, camZ, worldTmp)
    return verts.putVec3(worldTmp[0], worldTmp[1], worldTmp[2])
        .putUv(u / TEX_W, v / TEX_H)
        .putColor(WHITE)
        .next()
}

fun emitQuad(
    verts: VertexWriter,
    idx: IndexWriter,
    bone: Bone,
    x0: Float, y0: Float, z0: Float, u0: Float, v0: Float,
    x1: Float, y1: Float, z1: Float, u1: Float, v1: Float,
    x2: Float, y2: Float, z2: Float, u2: Float, v2: Float,
    x3: Float, y3: Float, z3: Float, u3: Float, v3: Float,
    originX: Float, originY: Float, originZ: Float,
    yawDeg: Float, scl: Float,
    camX: Float, camY: Float, camZ: Float
) {
    val a = emitVert(verts, bone, x0, y0, z0, u0, v0, originX, originY, originZ, yawDeg, scl, camX, camY, camZ)
    val b = emitVert(verts, bone, x1, y1, z1, u1, v1, originX, originY, originZ, yawDeg, scl, camX, camY, camZ)
    val c = emitVert(verts, bone, x2, y2, z2, u2, v2, originX, originY, originZ, yawDeg, scl, camX, camY, camZ)
    val d = emitVert(verts, bone, x3, y3, z3, u3, v3, originX, originY, originZ, yawDeg, scl, camX, camY, camZ)
    idx.putQuad(a, b, c, d)
}

fun emitCube(
    verts: VertexWriter,
    idx: IndexWriter,
    bone: Bone,
    ox: Float,
    oy: Float,
    oz: Float,
    w: Float,
    h: Float,
    d: Float,
    u: Float,
    v: Float,
    originX: Float,
    originY: Float,
    originZ: Float,
    yawDeg: Float,
    scl: Float,
    camX: Float,
    camY: Float,
    camZ: Float
) {
    val x0 = ox
    val y0 = oy
    val z0 = oz
    val x1 = ox + w
    val y1 = oy + h
    val z1 = oz + d
    val uu = u
    val vv = v
    val westU0 = uu
    val westU1 = uu + d
    val eastU0 = uu + d + w
    val eastU1 = uu + d + w + d
    val northU0 = uu + d
    val northU1 = uu + d + w
    val southU0 = uu + d + w + d
    val southU1 = uu + d + w + d + w
    val upU0 = uu + d
    val upU1 = uu + d + w
    val downU0 = uu + d + w
    val downU1 = uu + d + w + w
    val sideV0 = vv + d
    val sideV1 = vv + d + h
    val capV0 = vv
    val capV1 = vv + d

    emitQuad(
        verts, idx, bone,
        x0, y0, z1, westU0, sideV0,
        x0, y0, z0, westU1, sideV0,
        x0, y1, z0, westU1, sideV1,
        x0, y1, z1, westU0, sideV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
    emitQuad(
        verts, idx, bone,
        x1, y0, z0, eastU0, sideV0,
        x1, y0, z1, eastU1, sideV0,
        x1, y1, z1, eastU1, sideV1,
        x1, y1, z0, eastU0, sideV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
    emitQuad(
        verts, idx, bone,
        x0, y0, z0, northU0, sideV0,
        x1, y0, z0, northU1, sideV0,
        x1, y1, z0, northU1, sideV1,
        x0, y1, z0, northU0, sideV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
    emitQuad(
        verts, idx, bone,
        x1, y0, z1, southU0, sideV0,
        x0, y0, z1, southU1, sideV0,
        x0, y1, z1, southU1, sideV1,
        x1, y1, z1, southU0, sideV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
    emitQuad(
        verts, idx, bone,
        x0, y0, z0, upU0, capV0,
        x1, y0, z0, upU1, capV0,
        x1, y0, z1, upU1, capV1,
        x0, y0, z1, upU0, capV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
    emitQuad(
        verts, idx, bone,
        x1, y1, z0, downU0, capV0,
        x0, y1, z0, downU1, capV0,
        x0, y1, z1, downU1, capV1,
        x1, y1, z1, downU0, capV1,
        originX, originY, originZ, yawDeg, scl, camX, camY, camZ
    )
}

fun currentTex(): Texture? {
    return if (coat == "Рыжий") texGinger else texGray
}

onEnable { resetFollow() }
on<WorldLoadEvent> { resetFollow() }

on<Render2DEvent> { e ->
    val r = e.render()
    val gray = texGray
    val ginger = texGinger
    if (gray != null && !gray.ready()) r.texture(gray, 0f, 0f, 2f, 2f)
    if (ginger != null && !ginger.ready()) r.texture(ginger, 2f, 0f, 2f, 2f)
}

on<ClientTickEvent> {
    if (!inGame) {
        spawned = false
        return@on
    }
    ticks++

    val prev = player.previousPosition()
    val pdx = player.x() - prev.x()
    val pdz = player.z() - prev.z()
    val playerMoved = hypot2(pdx, pdz)
    val playerWalking = playerMoved > 0.03
    if (playerWalking) {
        moveYaw = toDegrees(atan2(-pdx, pdz))
    }
    if (player.onGround()) {
        groundY = player.y()
        haveGround = true
    } else if (!haveGround) {
        groundY = floorUnder(player.x(), player.y(), player.z())
        haveGround = true
    }
    val slotX = petSlotX(player.x(), moveYaw)
    val slotZ = petSlotZ(player.z(), moveYaw)
    val ty = groundY

    px = mx
    py = my
    pz = mz
    prevYaw = yaw

    if (!spawned) {
        mx = slotX
        my = ty
        mz = slotZ
        yaw = moveYaw
        prevYaw = yaw
        px = mx
        py = my
        pz = mz
        spawned = true
        settled = true
        return@on
    }

    val dx = slotX - mx
    val dz = slotZ - mz
    val gap = hypot2(dx, dz)

    if (gap > 24.0) {
        mx = slotX
        my = ty
        mz = slotZ
        px = mx
        py = my
        pz = mz
        yaw = moveYaw
        prevYaw = yaw
        walkSpeed = 0f
        settled = true
        return@on
    }

    my = stepFloor(my, ty)

    val wantWalk = playerWalking || gap > 0.85
    if (!wantWalk && (settled || gap < 0.28)) {
        settled = true
        walkSpeed += (0f - walkSpeed) * 0.28f
        if (walkSpeed < 0.03f) walkSpeed = 0f
    } else {
        settled = false
        val catchUp = if (gap > 3.0) 0.24 else 0.16
        val step = if (gap < catchUp) gap else catchUp
        if (gap > 0.001) {
            mx += dx / gap * step
            mz += dz / gap * step
        }
        val moved = hypot2(mx - px, mz - pz)
        if (moved > 0.008) {
            val catYaw = toDegrees(atan2(-(mx - px), mz - pz))
            yaw = lerpAngle(yaw, catYaw, 0.28f)
        }
        val targetSpeed = (moved / 0.16).toFloat().coerceIn(0f, 1f)
        walkSpeed += (targetSpeed - walkSpeed) * 0.4f
        walkPos += 0.85f * walkSpeed
        if (hypot2(slotX - mx, slotZ - mz) < 0.22 && !playerWalking) {
            settled = true
        }
    }
}

on<Render3DEvent> { e ->
    if (!inGame || !spawned) return@on
    val tex = currentTex()
    if (tex == null) {
        if (!missingLogged) {
            log.warn("manul: texture decode failed")
            missingLogged = true
        }
        return@on
    }
    if (!tex.ready()) return@on

    val td = e.tickDelta()
    val ox = (px + (mx - px) * td).toFloat()
    val oy = (py + (my - py) * td).toFloat()
    val oz = (pz + (mz - pz) * td).toFloat()
    val yawNow = prevYaw + wrapDeg(yaw - prevYaw) * td
    val idle = ticks + td
    val sitting = sitSneak && player.sneaking() && walkSpeed < 0.2f && settled
    setupPose(sitting, walkPos + walkSpeed * td, walkSpeed, idle)

    val cam = e.camera()
    val camX = cam.x().toFloat()
    val camY = cam.y().toFloat()
    val camZ = cam.z().toFloat()
    val scl = modelScale
    val verts = mesh.verts()
    val idx = mesh.idx() ?: return@on

    fun cube(bone: Bone, boxX: Float, boxY: Float, boxZ: Float, w: Float, h: Float, d: Float, u: Float, v: Float) {
        emitCube(verts, idx, bone, boxX, boxY, boxZ, w, h, d, u, v, ox, oy, oz, yawNow, scl, camX, camY, camZ)
    }

    cube(body, -4.5f, 0f, -6.5f, 9f, 7f, 13f, 0f, 0f)
    cube(skirt, -5f, 0f, -7f, 10f, 4f, 14f, 0f, 20f)
    cube(tail, -2f, -2f, 0f, 4f, 4f, 8f, 48f, 12f)
    cube(tailTip, -2f, -2f, 0f, 4f, 4f, 4f, 72f, 12f)
    cube(head, -4.5f, -3f, -6f, 9f, 6f, 6f, 48f, 0f)
    cube(muzzle, -2f, 0f, -2f, 4f, 3f, 2f, 106f, 0f)
    cube(leftCheek, 0f, -2.5f, 0f, 2f, 5f, 5f, 78f, 0f)
    cube(rightCheek, -2f, -2.5f, 0f, 2f, 5f, 5f, 92f, 0f)
    cube(leftEar, -1.5f, -2f, -0.5f, 3f, 2f, 1f, 106f, 6f)
    cube(rightEar, -1.5f, -2f, -0.5f, 3f, 2f, 1f, 106f, 10f)
    cube(fl, -1.5f, 0f, -1.5f, 3f, 3f, 3f, 88f, 12f)
    cube(fr, -1.5f, 0f, -1.5f, 3f, 3f, 3f, 88f, 12f)
    cube(hl, -1.5f, 0f, -1.5f, 3f, 3f, 3f, 88f, 12f)
    cube(hr, -1.5f, 0f, -1.5f, 3f, 3f, 3f, 88f, 12f)

    manulShader.setMat4("u_view", e.viewMatrix())
    manulShader.setMat4("u_projection", e.projectionMatrix())
    manulShader.set("u_tex", tex, TextureFilter.NEAREST, TextureWrap.CLAMP)
    pass.draw()
}
