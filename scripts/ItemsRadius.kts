name("ItemsRadius")
description("Заранее показывает радиус действия предмета в руке")

val defaultFill = Colors.rgba(0, 220, 60, 70)
val defaultBorder = Colors.rgba(0, 90, 25, 255)

val methodSetting = selectable("Метод предмета", "Через id", "FunTime", "Оба", selected = "Оба")
val method by methodSetting

val throughWalls = checkBox("Сквозь блоки", false)

val useDisorient = checkBox("Дезориентация", true)
val disorientShapeSetting = selectable(useDisorient, "Форма", "Круг", "Квадрат", "Ромб", selected = "Круг")
val disorientShape by disorientShapeSetting
val disorientFilled = checkBox(useDisorient, "Заливать форму", true)
val disorientFill = colorPicker(useDisorient, "Заполнение", defaultFill)
val disorientBorder = colorPicker(useDisorient, "Бордер", defaultBorder)

val useDust = checkBox("Явная пыль", true)
val dustShapeSetting = selectable(useDust, "Форма", "Круг", "Квадрат", "Ромб", selected = "Круг")
val dustShape by dustShapeSetting
val dustFilled = checkBox(useDust, "Заливать форму", true)
val dustFill = colorPicker(useDust, "Заполнение", defaultFill)
val dustBorder = colorPicker(useDust, "Бордер", defaultBorder)

val useTornado = checkBox("Огненный смерч", true)
val tornadoShapeSetting = selectable(useTornado, "Форма", "Круг", "Квадрат", "Ромб", selected = "Круг")
val tornadoShape by tornadoShapeSetting
val tornadoFilled = checkBox(useTornado, "Заливать форму", true)
val tornadoFill = colorPicker(useTornado, "Заполнение", defaultFill)
val tornadoBorder = colorPicker(useTornado, "Бордер", defaultBorder)

val useAura = checkBox("Божья аура", true)
val auraShapeSetting = selectable(useAura, "Форма", "Круг", "Квадрат", "Ромб", selected = "Круг")
val auraShape by auraShapeSetting
val auraFilled = checkBox(useAura, "Заливать форму", true)
val auraFill = colorPicker(useAura, "Заполнение", defaultFill)
val auraBorder = colorPicker(useAura, "Бордер", defaultBorder)

val useScrap = checkBox("Трапка", true)
val scrapFilled = checkBox(useScrap, "Заливать форму", true)
val scrapFill = colorPicker(useScrap, "Заполнение", defaultFill)
val scrapBorder = colorPicker(useScrap, "Бордер", defaultBorder)

val useKelp = checkBox("Пласт", true)
val kelpFilled = checkBox(useKelp, "Заливать форму", true)
val kelpFill = colorPicker(useKelp, "Заполнение", defaultFill)
val kelpBorder = colorPicker(useKelp, "Бордер", defaultBorder)

disorientShapeSetting.visibleWhen { useDisorient.value() }
disorientFilled.visibleWhen { useDisorient.value() }
disorientFill.visibleWhen { useDisorient.value() && disorientFilled.value() }
disorientBorder.visibleWhen { useDisorient.value() }

dustShapeSetting.visibleWhen { useDust.value() }
dustFilled.visibleWhen { useDust.value() }
dustFill.visibleWhen { useDust.value() && dustFilled.value() }
dustBorder.visibleWhen { useDust.value() }

tornadoShapeSetting.visibleWhen { useTornado.value() }
tornadoFilled.visibleWhen { useTornado.value() }
tornadoFill.visibleWhen { useTornado.value() && tornadoFilled.value() }
tornadoBorder.visibleWhen { useTornado.value() }

auraShapeSetting.visibleWhen { useAura.value() }
auraFilled.visibleWhen { useAura.value() }
auraFill.visibleWhen { useAura.value() && auraFilled.value() }
auraBorder.visibleWhen { useAura.value() }

scrapFilled.visibleWhen { useScrap.value() }
scrapFill.visibleWhen { useScrap.value() && scrapFilled.value() }
scrapBorder.visibleWhen { useScrap.value() }

kelpFilled.visibleWhen { useKelp.value() }
kelpFill.visibleWhen { useKelp.value() && kelpFilled.value() }
kelpBorder.visibleWhen { useKelp.value() }

val ids = listOf(
    "minecraft:ender_eye",
    "minecraft:sugar",
    "minecraft:fire_charge",
    "minecraft:phantom_membrane",
    "minecraft:netherite_scrap",
    "minecraft:dried_kelp"
)

val names = listOf(
    "Дезориентация",
    "Явная пыль",
    "Огненный смерч",
    "Божья аура",
    "Трапка",
    "Пласт"
)

val radii = listOf(10.0, 10.0, 10.0, 2.0, 0.0, 0.0)
val toggles = listOf(useDisorient, useDust, useTornado, useAura, useScrap, useKelp)
val filledToggles = listOf(disorientFilled, dustFilled, tornadoFilled, auraFilled, scrapFilled, kelpFilled)
val fills = listOf(disorientFill, dustFill, tornadoFill, auraFill, scrapFill, kelpFill)
val borders = listOf(disorientBorder, dustBorder, tornadoBorder, auraBorder, scrapBorder, kelpBorder)

val segments = 64
val cubeSize = 3.0
val plateSize = 3.0
val plateDistance = 2.0
val plateThickness = 1.0
val plateLookThresholdDeg = 60.0
val plateCeilingHeight = 2.5

var active = -1

fun shapeOf(index: Int): String = when (index) {
    0 -> disorientShape
    1 -> dustShape
    2 -> tornadoShape
    3 -> auraShape
    else -> "Круг"
}

fun matches(index: Int, id: String, display: String): Boolean = when (method) {
    "Через id" -> id == ids[index]
    "FunTime" -> display.contains(names[index], true)
    else -> id == ids[index] || display.contains(names[index], true)
}

onEnable {
    active = -1
}

on<ClientTickEvent> {
    whenInGame {
        active = -1
        val held = inventory.held()
        if (held.empty()) return@whenInGame

        val id = held.id()
        val display = held.displayName().string()

        for (i in ids.indices) {
            if (!toggles[i].value()) continue
            if (matches(i, id, display)) {
                active = i
                break
            }
        }
    }
}

on<Render3DEvent> { e ->
    val index = active
    if (index < 0) return@on

    val r = e.render()
    val through = throughWalls.value()
    val filled = filledToggles[index].value()
    val fillColor = fills[index].value()
    val borderColor = borders[index].value()

    if (index == 4) {
        val centre = player.renderPosition().add(0.0, cubeSize / 2.0, 0.0)
        val cube = Box.around(centre, cubeSize / 2.0)
        if (filled) {
            r.filledBox(cube, fillColor, through)
        }
        r.box(cube, borderColor, through)
        return@on
    }

    if (index == 5) {
        val half = plateSize / 2.0
        val halfThickness = plateThickness / 2.0
        val rawYaw = Math.toRadians(player.yaw().toDouble())
        val yaw = Math.round(rawYaw / (Math.PI / 2.0)) * (Math.PI / 2.0)

        val fx = -Math.sin(yaw)
        val fz = Math.cos(yaw)
        val rx = -Math.cos(yaw)
        val rz = -Math.sin(yaw)

        val pitchDeg = player.pitch().toDouble()
        val feet = player.renderPosition()

        var nx = fx; var ny = 0.0; var nz = fz
        var ux = rx; var uy = 0.0; var uz = rz
        var vx = 0.0; var vy = 1.0; var vz = 0.0
        var centre = feet.add(fx * plateDistance, half, fz * plateDistance)

        if (pitchDeg <= -plateLookThresholdDeg) {
            // вверх
            nx = 0.0; ny = 0.0; nz = 0.0
            vx = fx; vy = 0.0; vz = fz
            centre = feet.add(0.0, plateCeilingHeight, 0.0)
        } else if (pitchDeg >= plateLookThresholdDeg) {
            // низ
            nx = 0.0; ny = 1.1; nz = 0.0
            vx = fx; vy = 0.0; vz = fz
            centre = feet.add(0.0, -halfThickness, 0.0)
        }

        val frontCentre = centre.add(-nx * halfThickness, -ny * halfThickness, -nz * halfThickness)
        val backCentre = centre.add(nx * halfThickness, ny * halfThickness, nz * halfThickness)

        val fa = frontCentre.add(ux * half + vx * half, uy * half + vy * half, uz * half + vz * half)
        val fb = frontCentre.add(ux * half - vx * half, uy * half - vy * half, uz * half - vz * half)
        val fc = frontCentre.add(-ux * half - vx * half, -uy * half - vy * half, -uz * half - vz * half)
        val fd = frontCentre.add(-ux * half + vx * half, -uy * half + vy * half, -uz * half + vz * half)

        val ba = backCentre.add(ux * half + vx * half, uy * half + vy * half, uz * half + vz * half)
        val bb = backCentre.add(ux * half - vx * half, uy * half - vy * half, uz * half - vz * half)
        val bc = backCentre.add(-ux * half - vx * half, -uy * half - vy * half, -uz * half - vz * half)
        val bd = backCentre.add(-ux * half + vx * half, -uy * half + vy * half, -uz * half + vz * half)

        if (filled) {
            r.quad(fa, fb, fc, fd, fillColor, through)
            r.quad(ba, bb, bc, bd, fillColor, through)
            r.quad(fa, fb, bb, ba, fillColor, through)
            r.quad(fb, fc, bc, bb, fillColor, through)
            r.quad(fc, fd, bd, bc, fillColor, through)
            r.quad(fd, fa, ba, bd, fillColor, through)
        }

        val front = listOf(fa, fb, fc, fd)
        val back = listOf(ba, bb, bc, bd)
        for (i in front.indices) {
            val ni = (i + 1) % front.size
            r.line(front[i], front[ni], borderColor, through)
            r.line(back[i], back[ni], borderColor, through)
            r.line(front[i], back[i], borderColor, through)
        }
        return@on
    }

    val radius = radii[index]
    if (radius <= 0.0) return@on

    val centre = player.renderPosition().add(0.0, 0.03, 0.0)
    val offsets = when (shapeOf(index)) {
        "Квадрат" -> listOf(
            radius to radius,
            radius to -radius,
            -radius to -radius,
            -radius to radius
        )
        "Ромб" -> listOf(
            radius to 0.0,
            0.0 to radius,
            -radius to 0.0,
            0.0 to -radius
        )
        else -> (0 until segments).map {
            val angle = Math.toRadians(it * 360.0 / segments)
            Math.cos(angle) * radius to Math.sin(angle) * radius
        }
    }

    val ring = offsets.map { centre.add(it.first, 0.0, it.second) }

    if (filled) {
        r.polygon(ring, fillColor, through)
    }

    for (i in ring.indices) {
        r.line(ring[i], ring[(i + 1) % ring.size], borderColor, through)
    }
}