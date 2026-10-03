name("CrystalWings")
description("Кристальные крылья за спиной: острый веерный контур с длинным изогнутым клинком и светящимся ободком")

// ── Настройки ────────────────────────────────────────────────────

val scale by slider("Размер", 1.0f, 0.5f, 2.5f, 0.05f)
val flapSpeed by slider("Скорость взмаха", 0.10f, 0f, 0.5f, 0.01f)
val flapAmplitude by slider("Амплитуда взмаха", 8f, 0f, 40f).postfix(Postfixes.DEGREES)
val fillColor = colorPicker("Цвет заливки", Colors.rgba(160, 150, 205, 90))
val edgeColor = colorPicker("Цвет ободка", Colors.rgba(235, 225, 255, 255))
val shards = checkBox("Кристаллы у плеч", true)
val throughWalls = checkBox("Сквозь стены", false)

// ── Контур крыла (a = размах, b = высота) ───────────────────────
// Веер из острых зубцов вдоль верхнего/внешнего края, переходящий в
// длинный изогнутый клинок-шип впереди-снизу и маленький хвостовой
// зубец под ним — как на референсе. Контур star-shaped относительно
// точки крепления (0,0), чтобы polygon() из docs.nursultan.fun/ui/render-3d
// (фан-триангуляция от первой точки) рисовал его без искажений.

val wingOutline = listOf(
    0.00 to 0.00,
    0.08 to 0.42,
    0.05 to 0.28,
    0.20 to 0.68,
    0.15 to 0.48,
    0.34 to 0.92,
    0.27 to 0.64,
    0.50 to 1.10,
    0.42 to 0.80,
    0.68 to 1.28,
    0.60 to 0.92,
    0.88 to 1.05,
    0.82 to 0.72,
    1.05 to 0.68,
    0.95 to 0.38,
    1.20 to 0.08,
    0.88 to -0.12,
    1.00 to -0.62,
    0.55 to -0.40,
    0.38 to -0.60,
    0.15 to -0.32,
    0.05 to -0.10
)

// Маленький кристалл-шип у плеча.
val shardOutline = listOf(
    0.00 to 0.00,
    0.05 to 0.35,
    0.10 to 0.55,
    0.15 to 0.35,
    0.10 to 0.00
)

var ticks = 0f

on<ClientTickEvent> {
    whenInGame { ticks += 1f }
}

on<Render3DEvent> { e ->
    whenInGame {
        // От первого лица камера находится практически в точке глаз игрока,
        // от третьего — отнесена от неё на несколько блоков назад/вверх.
        // Ничего не рисуем, если камера ближе 1 блока к глазам — это FP-вид.
        if (e.camera().distanceTo(player.eyePosition()) < 1.0) return@on

        val yaw = Math.toRadians(player.yaw().toDouble())
        val fx = -Math.sin(yaw)
        val fz = Math.cos(yaw)
        val rx = Math.cos(yaw)
        val rz = Math.sin(yaw)

        val attach = player.renderPosition().add(
            -fx * 0.15,
            player.height() * 0.55,
            -fz * 0.15
        )

        val phase = (ticks + e.tickDelta()) * flapSpeed
        val flapAngle = Math.toRadians(flapAmplitude.toDouble()) * Math.sin(phase.toDouble())
        val cosF = Math.cos(flapAngle)
        val sinF = Math.sin(flapAngle)
        val sz = scale.toDouble()

        fun localToWorld(a: Double, b: Double, mirror: Double, depthFwd: Double = 0.0) = run {
            val ca = a * cosF - b * sinF
            val cb = a * sinF + b * cosF
            val backSweep = a * 0.35

            val dx = (rx * mirror * ca - fx * backSweep) * sz - fx * depthFwd * sz
            val dy = cb * sz
            val dz = (rz * mirror * ca - fz * backSweep) * sz - fz * depthFwd * sz

            attach.add(dx, dy, dz)
        }

        val r = e.render()

        for (mirror in doubleArrayOf(1.0, -1.0)) {
            val pts = wingOutline.map { (a, b) -> localToWorld(a, b, mirror) }

            r.polygon(pts, fillColor.value(), throughWalls.value())

            for (i in pts.indices) {
                val from = pts[i]
                val to = pts[(i + 1) % pts.size]
                r.line(from, to, edgeColor.value(), throughWalls.value())
            }

            if (shards.value()) {
                val shardPts = shardOutline.map { (a, b) -> localToWorld(a, b, mirror * 0.3, -0.05) }
                r.polygon(shardPts, Colors.withAlpha(fillColor.value(), 90), throughWalls.value())
                for (i in shardPts.indices) {
                    val from = shardPts[i]
                    val to = shardPts[(i + 1) % shardPts.size]
                    r.line(from, to, edgeColor.value(), throughWalls.value())
                }
            }
        }
    }
}
