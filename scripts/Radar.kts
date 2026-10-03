name("Radar")
description("Миникарта по чанкам с игроками, навигационной проекцией и перемещаемым HUD")
requireApi(1)

val radarDistance = slider("Дальность (чанки)", 4, 1, 12)
val radarFormat = selectable("Формат", "1:1", "16:9", selected = "1:1")
val radarScale = slider("Размер", 2.0f, 0.5f, 2.0f, 0.05f)
val radarMarkerSize = slider("Размер элементов", 32, 8, 32)
val radarProjectionEnabled = checkBox("Наклон проекции", true)
val radarGpsEnabled = checkBox("GPS-метка", true)
val radarGpsCircleColor = colorPicker("Цвет метки GPS", Colors.rgba(255, 0, 0, 255))
    .visibleWhen { radarGpsEnabled.value() }
val radarArrowColor = colorPicker("Цвет стрелки", Colors.rgba(247, 0, 38, 255))
val radarChunkColor = colorPicker("Цвет границ чанков", Colors.rgba(255, 255, 255, 42))
val radarBackgroundColor = colorPicker("Цвет заднего фона", Colors.rgba(10, 12, 15, 205))

// Позиция хранится в долях экрана, поэтому HUD остаётся на месте
// после смены разрешения или GUI Scale.
var radarHudX = storage.getDouble("hud.x", 0.03).toFloat()
var radarHudY = storage.getDouble("hud.y", 0.18).toFloat()
var radarDragging = false
var radarDragOffsetX = 0f
var radarDragOffsetY = 0f
var radarMouseWasDown = false
var radarGpsRenderedHeight = 0f
var radarGpsPreviousVisibility = 0f
var radarGpsVisibility = 0f

class RadarPlayerMarker(
    var entity: PlayerEntity,
    var lastPosition: Vec,
    var friend: Boolean,
    var previousVisibility: Float = 0f,
    var visibility: Float = 0f,
    var present: Boolean = true,
    var onRadar: Boolean = false,
    var insideRadar: Boolean = true,
    var previousSizeScale: Float = 1f,
    var sizeScale: Float = 1f,
    var hasScreenPosition: Boolean = false,
    var lastScreenX: Float = 0f,
    var lastScreenY: Float = 0f
)

val radarPlayerMarkers = mutableMapOf<Int, RadarPlayerMarker>()
var radarGpsPosition: Vec? = null
// Быстрый fade занимает около четверти секунды при 20 TPS.
val radarMarkerVisibilityStep = 0.20f
// Вынесенная на край голова чуть компактнее, а переход между размерами
// интерполируется между тиками, чтобы при пересечении границы не было скачка.
val radarOutsideHeadScale = 0.78f
val radarHeadScaleStep = 0.06f
val radarFriendGlowStrength = 5.0f

// Сетка вычисляется в мировых координатах для каждого пикселя. Поэтому она
// точно следует границам x/z = 16*n и вращается вместе с направлением взгляда.
val radarGridShader = shader("radar-navigation-grid-v3-rounded", """
    #version 330

    in vec2 in_uv;
    out vec4 out_color;

    uniform vec2 u_mapSize;
    uniform vec2 u_playerPos;
    uniform float u_yaw;
    uniform float u_pixelsPerBlock;
    uniform float u_perspective;
    uniform float u_anchorY;
    uniform float u_cornerRadius;
    uniform vec4 u_gridColor;

    void main() {
        vec2 screen = (in_uv - vec2(0.5)) * u_mapSize;

        // Обрезаем саму проекцию по скруглённому прямоугольнику, а не только
        // фон HUD. fwidth оставляет край гладким при любом GUI Scale.
        vec2 roundedPoint = abs(screen) - (u_mapSize * 0.5 - vec2(u_cornerRadius));
        float roundedDistance = length(max(roundedPoint, vec2(0.0)))
            + min(max(roundedPoint.x, roundedPoint.y), 0.0) - u_cornerRadius;
        float roundedAA = fwidth(roundedDistance) + 0.001;
        float roundedMask = 1.0 - smoothstep(-roundedAA, roundedAA, roundedDistance);
        if (roundedMask <= 0.0) discard;

        // Мягкая навигационная перспектива заполняет весь прямоугольник HUD.
        // Дальний край слегка сужается, а позиция игрока смещается вниз.
        float screenY = in_uv.y * 2.0 - 1.0;
        float anchorScale = 1.0 + u_perspective * u_anchorY;
        float rowScale = (1.0 + u_perspective * screenY) / anchorScale;
        vec2 plane;
        plane.x = screen.x / max(rowScale, 0.001);
        plane.y = (screenY - u_anchorY) * (u_mapSize.y * 0.5)
            * anchorScale / max(1.0 + u_perspective * screenY, 0.001);

        float s = sin(u_yaw);
        float c = cos(u_yaw);
        vec2 right = vec2(-c, -s);
        vec2 forward = vec2(-s, c);
        vec2 world = u_playerPos
            + right * (plane.x / u_pixelsPerBlock)
            + forward * (-plane.y / u_pixelsPerBlock);

        vec2 chunkDistanceBlocks = abs(fract(world / 16.0 + vec2(0.5)) - vec2(0.5)) * 16.0;
        vec2 blocksPerPixel = max(fwidth(world), vec2(0.0001));
        float distancePixels = min(
            chunkDistanceBlocks.x / blocksPerPixel.x,
            chunkDistanceBlocks.y / blocksPerPixel.y
        );
        float line = 1.0 - smoothstep(0.45, 1.25, distancePixels);
        if (line <= 0.0) discard;
        out_color = vec4(u_gridColor.rgb, u_gridColor.a * line * roundedMask);
    }
""")

val radarHeadShader = shader("radar-rounded-player-head", """
    #version 330

    in vec2 in_uv;
    out vec4 out_color;

    uniform sampler2D u_skin;
    uniform vec4 u_face;
    uniform vec4 u_hat;
    uniform vec2 u_atlasSize;
    uniform float u_damageTint;
    uniform float u_opacity;

    // Скин Minecraft — атлас. Берём строго центр каждого текселя области
    // 8x8, чтобы линейная фильтрация не смешивала лицо с соседними тайлами.
    vec4 sampleAtlasTile(vec4 tile, vec2 uv) {
        vec2 tilePixels = tile.zw * u_atlasSize;
        vec2 localPixel = clamp(floor(uv * tilePixels), vec2(0.0), tilePixels - vec2(1.0));
        vec2 atlasPixel = tile.xy * u_atlasSize + localPixel + vec2(0.5);
        return texture(u_skin, atlasPixel / u_atlasSize);
    }

    void main() {
        vec2 p = abs(in_uv - vec2(0.5)) - vec2(0.31);
        float distanceToEdge = length(max(p, vec2(0.0)))
            + min(max(p.x, p.y), 0.0) - 0.19;
        float aa = fwidth(distanceToEdge) + 0.001;
        float mask = 1.0 - smoothstep(-aa, aa, distanceToEdge);
        if (mask <= 0.0) discard;

        vec4 face = sampleAtlasTile(u_face, in_uv);
        vec4 hat = sampleAtlasTile(u_hat, in_uv);
        vec4 skin = mix(face, hat, hat.a);
        skin.rgb = mix(skin.rgb, vec3(1.0, 0.06, 0.06), u_damageTint);
        out_color = vec4(skin.rgb, skin.a * mask * u_opacity);
    }
""")

// Мягкий зелёный ореол рисуется отдельным проходом под головой. Список
// друзей уже доступен на самой сущности через isFriend(): настройка
// module.nofrienddamage.setting.teams выбирает категории союзников для
// NoFriendDamage, но не содержит сам список игроков.
val radarFriendGlowShader = shader("radar-friend-head-green-glow", """
    #version 330

    in vec2 in_uv;
    out vec4 out_color;
    uniform float u_opacity;
    uniform float u_strength;

    void main() {
        vec2 q = abs(in_uv - vec2(0.5));
        vec2 roundedPoint = q - vec2(0.22);
        float distanceToEdge = length(max(roundedPoint, vec2(0.0)))
            + min(max(roundedPoint.x, roundedPoint.y), 0.0) - 0.12;
        float strength = clamp(u_strength, 0.0, 5.0);
        float outerEdge = 0.12 + 0.025 * strength;
        float glow = 1.0 - smoothstep(-0.08, outerEdge, distanceToEdge);
        glow *= glow;
        if (glow <= 0.002) discard;
        float glowAlpha = min(0.72 * strength, 0.95);
        out_color = vec4(0.18, 1.0, 0.34, glowAlpha * glow * u_opacity);
    }
""")

val radarArrowShader = shader("radar-fixed-arrow", """
    #version 330

    in vec2 in_uv;
    out vec4 out_color;
    uniform vec4 u_color;
    uniform float u_depth;
    uniform vec2 u_drawSize;
    uniform vec2 u_shapeSize;
    uniform float u_mapHeight;
    uniform float u_perspective;
    uniform float u_anchorY;

    vec2 cubicBezier(vec2 a, vec2 b, vec2 c, vec2 d, float t) {
        float oneMinusT = 1.0 - t;
        return oneMinusT * oneMinusT * oneMinusT * a
            + 3.0 * oneMinusT * oneMinusT * t * b
            + 3.0 * oneMinusT * t * t * c
            + t * t * t * d;
    }

    void includeEdge(vec2 a, vec2 b, vec2 p, inout float minDistance, inout bool inside) {
        vec2 edge = b - a;
        float edgeLengthSquared = max(dot(edge, edge), 0.000001);
        float progress = clamp(dot(p - a, edge) / edgeLengthSquared, 0.0, 1.0);
        minDistance = min(minDistance, length(p - (a + edge * progress)));
        inside = inside && (edge.x * (p.y - a.y) - edge.y * (p.x - a.x) >= 0.0);
    }

    void main() {
        // Возвращаем экранный пиксель на плоскость карты тем же обратным
        // преобразованием, которым grid shader строит чанки. Поэтому при
        // включённом наклоне стрелка действительно лежит на этой плоскости.
        vec2 screenLocal = (in_uv - vec2(0.5)) * u_drawSize;
        float mapHalfHeight = max(u_mapHeight * 0.5, 0.001);
        float screenY = u_anchorY + screenLocal.y / mapHalfHeight;
        float anchorScale = 1.0 + u_perspective * u_anchorY;
        float rowScale = (1.0 + u_perspective * screenY) / anchorScale;
        vec2 planeLocal = vec2(
            screenLocal.x / max(rowScale, 0.001),
            (screenY - u_anchorY) * mapHalfHeight * anchorScale
                / max(1.0 + u_perspective * screenY, 0.001)
        );

        // Геометрия 71x84 повторяет присланный SVG. Кривые разбиваются на
        // короткие отрезки; при размере HUD погрешность меньше доли пикселя.
        vec2 p = (planeLocal / u_shapeSize + vec2(0.5)) * vec2(71.0, 84.0);
        float minDistance = 1000.0;
        bool inside = true;
        vec2 previous = vec2(23.969, 7.6356);

        for (int i = 1; i <= 20; i++) {
            float t = float(i) / 20.0;
            vec2 current = cubicBezier(
                vec2(23.969, 7.6356), vec2(27.9439, -2.54521),
                vec2(42.3504, -2.54521), vec2(46.3254, 7.6356), t
            );
            includeEdge(previous, current, p, minDistance, inside);
            previous = current;
        }

        includeEdge(previous, vec2(69.4611, 66.8913), p, minDistance, inside);
        previous = vec2(69.4611, 66.8913);
        for (int i = 1; i <= 12; i++) {
            float t = float(i) / 12.0;
            vec2 current = cubicBezier(
                vec2(69.4611, 66.8913), vec2(72.5333, 74.7601),
                vec2(66.7301, 83.2557), vec2(58.2829, 83.2557), t
            );
            includeEdge(previous, current, p, minDistance, inside);
            previous = current;
        }

        includeEdge(previous, vec2(12.0114, 83.2557), p, minDistance, inside);
        previous = vec2(12.0114, 83.2557);
        for (int i = 1; i <= 12; i++) {
            float t = float(i) / 12.0;
            vec2 current = cubicBezier(
                vec2(12.0114, 83.2557), vec2(3.56419, 83.2557),
                vec2(-2.23901, 74.7601), vec2(0.833249, 66.8913), t
            );
            includeEdge(previous, current, p, minDistance, inside);
            previous = current;
        }
        includeEdge(previous, vec2(23.969, 7.6356), p, minDistance, inside);

        float signedDistance = inside ? -minDistance : minDistance;
        float aa = fwidth(signedDistance) + 0.001;
        float mask = 1.0 - smoothstep(-aa, aa, signedDistance);
        if (mask <= 0.0) discard;
        float depthShade = mix(1.08, 0.78, in_uv.y);
        vec3 shadedColor = u_color.rgb * mix(1.0, depthShade, u_depth);
        out_color = vec4(shadedColor, u_color.a * mask);
    }
""")

// GPS-маркер повторяет присланный SVG 30x50. Нижняя точка ножки привязана
// к мировой координате GPS. Ножка всегда #D9D9D9, круг имеет свой цвет.
val radarGpsShader = shader("radar-gps-pin-30x50-highlight", """
    #version 330

    in vec2 in_uv;
    out vec4 out_color;
    uniform vec4 u_circleColor;
    uniform float u_opacity;

    float roundedBox(vec2 p, vec2 center, vec2 halfSize, float radius) {
        vec2 q = abs(p - center) - (halfSize - vec2(radius));
        return length(max(q, vec2(0.0)))
            + min(max(q.x, q.y), 0.0) - radius;
    }

    void main() {
        vec2 p = in_uv * vec2(30.0, 50.0);
        float stemDistance = roundedBox(p, vec2(15.5, 36.0), vec2(2.5, 14.0), 2.5);
        float circleDistance = length(p - vec2(15.0, 15.0)) - 15.0;
        float highlightDistance = length(p - vec2(20.0, 11.0)) - 5.0;
        float aa = max(fwidth(min(min(stemDistance, circleDistance), highlightDistance)), 0.001);

        float stemMask = 1.0 - smoothstep(-aa, aa, stemDistance);
        float circleMask = 1.0 - smoothstep(-aa, aa, circleDistance);
        float highlightMask = 1.0 - smoothstep(-aa, aa, highlightDistance);
        float alpha = max(max(stemMask, circleMask), highlightMask);
        if (alpha <= 0.0) discard;

        vec3 stemColor = vec3(217.0 / 255.0);
        // 75/255 белого: для #FF0000 получается ровно #FF4B4B.
        vec3 highlightColor = mix(u_circleColor.rgb, vec3(1.0), 75.0 / 255.0);
        vec3 color = mix(stemColor, u_circleColor.rgb, circleMask);
        color = mix(color, highlightColor, highlightMask);
        float colorAlpha = mix(1.0, u_circleColor.a, max(circleMask, highlightMask));
        out_color = vec4(color, alpha * colorAlpha * u_opacity);
    }
""")

fun saveRadarPosition() {
    storage.put("hud.x", radarHudX.toDouble())
    storage.put("hud.y", radarHudY.toDouble())
    storage.save()
}

fun radarEaseVisibility(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

fun radarInterpolatedVisibility(previous: Float, current: Float, tickDelta: Float): Float {
    return radarEaseVisibility(previous + (current - previous) * tickDelta.coerceIn(0f, 1f))
}

fun drawRadarHead(
    render: Render,
    target: PlayerEntity,
    friend: Boolean,
    x: Float,
    y: Float,
    size: Float,
    tickDelta: Float,
    opacity: Float
) {
    val glowStrength = radarFriendGlowStrength
    val friendHighlight = friend && glowStrength > 0.001f
    if (friendHighlight) {
        val glowPadding = size * 0.28f
        radarFriendGlowShader.set("u_opacity", opacity)
        radarFriendGlowShader.set("u_strength", glowStrength)
        render.shader(
            radarFriendGlowShader,
            x - glowPadding,
            y - glowPadding,
            size + glowPadding * 2f,
            size + glowPadding * 2f
        )
    }

    val hurtProgress = ((target.hurtTicks().toFloat() - tickDelta) / 10f).coerceIn(0f, 1f)
    val smoothHurt = hurtProgress * hurtProgress * (3f - 2f * hurtProgress)
    val damageTint = smoothHurt * 0.72f
    val skin = target.skinTexture()
    if (skin == null || !skin.ready() || skin.width() <= 0 || skin.height() <= 0) {
        render.head(target, x, y, size)
        if (damageTint > 0.001f) {
            render.roundedRect(
                x,
                y,
                size,
                size,
                size * 0.18f,
                Colors.rgba(255, 20, 20, (damageTint * opacity * 175f).toInt())
            )
        }
        return
    }

    val textureWidth = skin.width().toFloat()
    val textureHeight = skin.height().toFloat()
    render.roundedRect(
        x - 2f,
        y - 2f,
        size + 4f,
        size + 4f,
        size * 0.22f + 2f,
        Colors.rgba(0, 0, 0, (190f * opacity).toInt())
    )
    radarHeadShader.set("u_skin", skin)
    radarHeadShader.set("u_face", 8f / textureWidth, 8f / textureHeight, 8f / textureWidth, 8f / textureHeight)
    radarHeadShader.set("u_hat", 40f / textureWidth, 8f / textureHeight, 8f / textureWidth, 8f / textureHeight)
    radarHeadShader.set("u_atlasSize", textureWidth, textureHeight)
    radarHeadShader.set("u_damageTint", damageTint)
    radarHeadShader.set("u_opacity", opacity)
    render.shader(radarHeadShader, x, y, size, size)
    render.roundedOutline(
        x,
        y,
        size,
        size,
        size * 0.18f,
        1f,
        if (friend) {
            val strength = glowStrength.coerceIn(0f, 1f)
            Colors.rgba(
                (255f + (70f - 255f) * strength).toInt(),
                255,
                (255f + (105f - 255f) * strength).toInt(),
                ((150f + (230f - 150f) * strength) * opacity).toInt()
            )
        } else {
            Colors.rgba(255, 255, 255, (150f * opacity).toInt())
        }
    )
}

fun drawRadarArrow(
    render: Render,
    centerX: Float,
    centerY: Float,
    size: Float,
    color: Int
) {
    val arrowWidth = size * 71f / 84f
    val arrowHeight = size
    // Запас по краям нужен после перспективного изгиба крайних строк фигуры.
    val drawWidth = arrowWidth * 1.25f
    val drawHeight = arrowHeight * 1.20f
    val arrowX = centerX - drawWidth / 2f
    val arrowY = centerY - drawHeight / 2f

    // Отдельный смещённый проход даёт стрелке глубину без круглого фона.
    radarArrowShader.set("u_color", 0f, 0f, 0f, 0.58f)
    radarArrowShader.set("u_depth", 0f)
    render.shader(
        radarArrowShader,
        arrowX + size * 0.075f,
        arrowY + arrowHeight * 0.095f,
        drawWidth,
        drawHeight
    )

    radarArrowShader.set(
        "u_color",
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f,
        ((color ushr 24) and 255) / 255f
    )
    radarArrowShader.set("u_depth", 1f)
    render.shader(radarArrowShader, arrowX, arrowY, drawWidth, drawHeight)
}

fun drawRadarGpsMarker(
    render: Render,
    anchorX: Float,
    anchorY: Float,
    height: Float,
    color: Int,
    opacity: Float
) {
    val width = height * 30f / 50f
    radarGpsShader.set(
        "u_circleColor",
        ((color ushr 16) and 255) / 255f,
        ((color ushr 8) and 255) / 255f,
        (color and 255) / 255f,
        ((color ushr 24) and 255) / 255f
    )
    radarGpsShader.set("u_opacity", opacity)
    render.shader(radarGpsShader, anchorX - width / 2f, anchorY - height, width, height)
}

// Цель GPS — это две настройки встроенного модуля: текстовые поля
// module.gps.setting.target-x и module.gps.setting.target-z. Ссылки на модуль и
// на его настройки не меняются за время работы клиента, поэтому ищем их один
// раз при загрузке: find перебирает весь реестр модулей, и на каждом тике это
// лишняя работа.
val radarGpsModule = client.modules().find("GPS")
val radarGpsTargetX = radarGpsModule?.setting("module.gps.setting.target-x") as? Input
val radarGpsTargetZ = radarGpsModule?.setting("module.gps.setting.target-z") as? Input

// Модуль хранит цель текстом, и пока её вводят, там лежит "", "-" или ".".
// toDoubleOrNull отсекает такие промежуточные значения, поэтому метка
// появляется ровно тогда же, когда цель считает заданной сам клиент.
fun readRadarGpsPosition(): Vec? {
    val gps = radarGpsModule ?: return null
    if (!gps.enabled()) return null
    val x = radarGpsTargetX?.value()?.toDoubleOrNull() ?: return null
    val z = radarGpsTargetZ?.value()?.toDoubleOrNull() ?: return null
    return Vec.of(x, player.position().y(), z)
}

onEnable {
    if (radarGpsTargetX == null || radarGpsTargetZ == null) {
        log.warn("Не нашёл настройки target-x/target-z у модуля GPS — метка цели рисоваться не будет")
    }
    radarPlayerMarkers.clear()
    radarGpsPosition = null
    radarGpsRenderedHeight = 0f
    radarGpsPreviousVisibility = 0f
    radarGpsVisibility = 0f
}

onDisable {
    if (radarDragging) saveRadarPosition()
    radarDragging = false
    radarMouseWasDown = false
    radarPlayerMarkers.clear()
    radarGpsPosition = null
    radarGpsRenderedHeight = 0f
    radarGpsPreviousVisibility = 0f
    radarGpsVisibility = 0f
}

// Игрок не удаляется из состояния мгновенно: после ухода из мира его последняя
// позиция и сущность живут ещё несколько тиков, пока голова плавно исчезает.
on<ClientTickEvent> {
    if (!inGame) {
        radarPlayerMarkers.clear()
        radarGpsPosition = null
        radarGpsRenderedHeight = 0f
        radarGpsPreviousVisibility = 0f
        radarGpsVisibility = 0f
        return@on
    }

    // Сервер помечает часть настоящих игроков как bot, поэтому фильтруем
    // только себя и мёртвых сущностей.
    for (marker in radarPlayerMarkers.values) marker.present = false
    val currentPlayers = world.players().filter {
        !it.isSelf() && it.alive()
    }

    for (target in currentPlayers) {
        val id = target.id()
        val position = target.renderPosition()
        val marker = radarPlayerMarkers[id]
        if (marker == null) {
            radarPlayerMarkers[id] = RadarPlayerMarker(
                entity = target,
                lastPosition = position,
                friend = target.isFriend()
            )
        } else {
            marker.entity = target
            marker.lastPosition = position
            marker.friend = target.isFriend()
            marker.present = true
        }
    }

    val markerIterator = radarPlayerMarkers.entries.iterator()
    while (markerIterator.hasNext()) {
        val marker = markerIterator.next().value
        marker.previousVisibility = marker.visibility
        marker.visibility = if (marker.present && marker.onRadar) {
            (marker.visibility + radarMarkerVisibilityStep).coerceAtMost(1f)
        } else {
            (marker.visibility - radarMarkerVisibilityStep).coerceAtLeast(0f)
        }
        marker.previousSizeScale = marker.sizeScale
        val targetSizeScale = if (marker.insideRadar) 1f else radarOutsideHeadScale
        marker.sizeScale = if (marker.sizeScale < targetSizeScale) {
            (marker.sizeScale + radarHeadScaleStep).coerceAtMost(targetSizeScale)
        } else {
            (marker.sizeScale - radarHeadScaleStep).coerceAtLeast(targetSizeScale)
        }
        if (!marker.present && marker.visibility <= 0.001f) markerIterator.remove()
    }

    val currentGpsPosition = if (radarGpsEnabled.value()) readRadarGpsPosition() else null
    radarGpsPreviousVisibility = radarGpsVisibility
    if (currentGpsPosition != null) {
        radarGpsPosition = currentGpsPosition
        radarGpsVisibility = (radarGpsVisibility + radarMarkerVisibilityStep).coerceAtMost(1f)
    } else {
        radarGpsVisibility = (radarGpsVisibility - radarMarkerVisibilityStep).coerceAtLeast(0f)
        if (radarGpsVisibility <= 0.001f) radarGpsPosition = null
    }

}

on<Render2DEvent> { event ->
    if (!inGame) return@on

    val render = event.render()
    val scale = radarScale.value()
    val panelHeight = 180f * scale
    val panelWidth = panelHeight * if (radarFormat.value().name() == "16:9") 16f / 9f else 1f
    val markerSize = radarMarkerSize.intValue().toFloat()
    val screenWidth = event.width().toFloat()
    val screenHeight = event.height().toFloat()

    var panelX = (screenWidth * radarHudX)
        .coerceIn(4f, (screenWidth - panelWidth - 4f).coerceAtLeast(4f))
    var panelY = (screenHeight * radarHudY)
        .coerceIn(4f, (screenHeight - panelHeight - 4f).coerceAtLeast(4f))

    // Как в Hoplite: открыть чат, зажать ЛКМ на HUD и перетащить.
    val mouseX = keys.mouseX()
    val mouseY = keys.mouseY()
    val radarEditMode = !keys.cursorLocked()
    val mouseDown = radarEditMode && keys.mouseDown(0)
    val hovered = mouseX >= panelX && mouseX <= panelX + panelWidth &&
        mouseY >= panelY && mouseY <= panelY + panelHeight

    if (mouseDown && !radarMouseWasDown && hovered) {
        radarDragging = true
        radarDragOffsetX = mouseX - panelX
        radarDragOffsetY = mouseY - panelY
    }

    if (radarDragging && mouseDown) {
        panelX = (mouseX - radarDragOffsetX)
            .coerceIn(4f, (screenWidth - panelWidth - 4f).coerceAtLeast(4f))
        panelY = (mouseY - radarDragOffsetY)
            .coerceIn(4f, (screenHeight - panelHeight - 4f).coerceAtLeast(4f))
        radarHudX = panelX / screenWidth
        radarHudY = panelY / screenHeight
    } else if (radarDragging && !mouseDown) {
        radarDragging = false
        saveRadarPosition()
    }
    radarMouseWasDown = mouseDown

    val shortestSide = minOf(panelWidth, panelHeight)
    val panelRadius = (shortestSide * 0.075f).coerceIn(8f, 22f)
    val innerPadding = (shortestSide * 0.055f).coerceIn(6f, 16f)
    val mapX = panelX + innerPadding
    val mapY = panelY + innerPadding
    val mapWidth = panelWidth - innerPadding * 2f
    val mapHeight = panelHeight - innerPadding * 2f
    val centerX = panelX + panelWidth / 2f
    val centerY = panelY + panelHeight / 2f

    val backgroundColor = radarBackgroundColor.value()
    render.blur(
        panelX,
        panelY,
        panelWidth,
        panelHeight,
        16f,
        -1,
        panelRadius
    )
    render.roundedRect(
        panelX,
        panelY,
        panelWidth,
        panelHeight,
        panelRadius,
        backgroundColor
    )

    val playerPosition = player.renderPosition()
    val configuredRangeBlocks = radarDistance.intValue().toDouble() * 16.0
    // Один масштаб по обеим осям: чанк остаётся квадратным даже у прямоугольного HUD.
    val pixelsPerBlock = minOf(mapWidth, mapHeight) / (configuredRangeBlocks * 2.0).toFloat()
    val gridColor = radarChunkColor.value()
    // rotations.camera() хранит именно визуальное направление камеры.
    // player.yaw() и CameraEvent могут быть подменены системой ротаций
    // Attack Aura, из-за чего радар раньше поворачивался к цели ауры.
    val visualYaw = rotations.camera().yawDegrees()
    val yawRadians = Math.toRadians(visualYaw.toDouble())
    val yawSin = kotlin.math.sin(yawRadians)
    val yawCos = kotlin.math.cos(yawRadians)
    // Чекбокс включает прежнее максимальное положение слайдера — 180 градусов.
    val projectionEnabled = radarProjectionEnabled.value()
    val projectionPerspective = if (projectionEnabled) 0.88f else 0f
    val projectionAnchorY = if (projectionEnabled) 0.72f else 0f
    val projectionCornerRadius = if (projectionEnabled) {
        (8f * scale).coerceAtMost(minOf(mapWidth, mapHeight) * 0.5f)
    } else 0f

    radarGridShader.set("u_mapSize", mapWidth, mapHeight)
    radarGridShader.set("u_playerPos", playerPosition.x().toFloat(), playerPosition.z().toFloat())
    radarGridShader.set("u_yaw", yawRadians.toFloat())
    radarGridShader.set("u_pixelsPerBlock", pixelsPerBlock)
    radarGridShader.set("u_perspective", projectionPerspective)
    radarGridShader.set("u_anchorY", projectionAnchorY)
    radarGridShader.set("u_cornerRadius", projectionCornerRadius)
    radarGridShader.set(
        "u_gridColor",
        ((gridColor ushr 16) and 255) / 255f,
        ((gridColor ushr 8) and 255) / 255f,
        (gridColor and 255) / 255f,
        ((gridColor ushr 24) and 255) / 255f
    )
    render.shader(radarGridShader, mapX, mapY, mapWidth, mapHeight)

    radarArrowShader.set("u_drawSize", markerSize * 71f / 84f * 1.25f, markerSize * 1.20f)
    radarArrowShader.set("u_shapeSize", markerSize * 71f / 84f, markerSize)
    radarArrowShader.set("u_mapHeight", mapHeight)
    radarArrowShader.set("u_perspective", projectionPerspective)
    radarArrowShader.set("u_anchorY", projectionAnchorY)

    for (marker in radarPlayerMarkers.values) {
        val visibility = radarInterpolatedVisibility(
            marker.previousVisibility,
            marker.visibility,
            event.tickDelta()
        )
        val target = marker.entity
        val targetPosition = if (marker.present && target.alive()) {
            target.renderPosition().also { marker.lastPosition = it }
        } else {
            marker.lastPosition
        }
        val deltaX = targetPosition.x() - playerPosition.x()
        val deltaZ = targetPosition.z() - playerPosition.z()
        // Координаты игрока проходят через ту же вращаемую проекцию, что и чанки:
        // направление взгляда всегда вверх, правая сторона игрока — справа.
        val projectedX = -deltaX * yawCos - deltaZ * yawSin
        val projectedY = deltaX * yawSin - deltaZ * yawCos

        val planeX = (projectedX * pixelsPerBlock).toFloat()
        val planeY = (projectedY * pixelsPerBlock).toFloat()
        val normalizedPlaneY = planeY / (mapHeight * 0.5f)
        val anchorScale = 1f + projectionPerspective * projectionAnchorY
        val projectionDenominator = anchorScale - normalizedPlaneY * projectionPerspective
        val playerScreenX = centerX
        val playerScreenY = centerY + projectionAnchorY * (mapHeight * 0.5f)

        var exactHeadX = playerScreenX
        var exactHeadY = playerScreenY
        if (projectionDenominator > 0.001f) {
            val projectedScreenY = (anchorScale * projectionAnchorY + normalizedPlaneY) /
                projectionDenominator
            val perspectiveWidthScale = (1f + projectionPerspective * projectedScreenY) /
                anchorScale
            exactHeadX = centerX + planeX * perspectiveWidthScale
            exactHeadY = centerY + projectedScreenY * (mapHeight * 0.5f)
        }

        val exactPositionVisible = projectionDenominator > 0.001f &&
            exactHeadX >= mapX && exactHeadX <= mapX + mapWidth &&
            exactHeadY >= mapY && exactHeadY <= mapY + mapHeight

        val headCenterX: Float
        val headCenterY: Float
        if (exactPositionVisible) {
            headCenterX = exactHeadX
            headCenterY = exactHeadY
        } else {
            // Как у GPS: пересекаем край лучом от своей стрелки к точному
            // положению игрока. Центр остаётся прямо на границе, поэтому ровно
            // половина головы может выступать наружу, но она не исчезает.
            val rawRayX: Float
            val rawRayY: Float
            if (projectionDenominator > 0.001f) {
                rawRayX = exactHeadX - playerScreenX
                rawRayY = exactHeadY - playerScreenY
            } else {
                rawRayX = projectedX.toFloat()
                rawRayY = projectedY.toFloat()
            }
            val directionLength = kotlin.math.sqrt(
                rawRayX * rawRayX + rawRayY * rawRayY
            )
            if (directionLength <= 0.001f) {
                headCenterX = playerScreenX
                headCenterY = playerScreenY
            } else {
                val rayX = rawRayX / directionLength
                val rayY = rawRayY / directionLength
                val edgeScaleX = if (rayX > 0.001f) {
                    (mapX + mapWidth - playerScreenX) / rayX
                } else if (rayX < -0.001f) {
                    (mapX - playerScreenX) / rayX
                } else Float.POSITIVE_INFINITY
                val edgeScaleY = if (rayY > 0.001f) {
                    (mapY + mapHeight - playerScreenY) / rayY
                } else if (rayY < -0.001f) {
                    (mapY - playerScreenY) / rayY
                } else Float.POSITIVE_INFINITY
                val edgeScale = minOf(edgeScaleX, edgeScaleY)
                headCenterX = (playerScreenX + rayX * edgeScale).coerceIn(mapX, mapX + mapWidth)
                headCenterY = (playerScreenY + rayY * edgeScale).coerceIn(mapY, mapY + mapHeight)
            }
        }

        marker.onRadar = true
        marker.insideRadar = exactPositionVisible
        marker.lastScreenX = headCenterX
        marker.lastScreenY = headCenterY
        marker.hasScreenPosition = true

        // За край больше не исчезаем; fade нужен только при появлении или
        // фактической выгрузке игрока из мира.
        if (visibility <= 0.001f) continue
        val drawCenterX = headCenterX
        val drawCenterY = headCenterY
        val headScale = marker.previousSizeScale +
            (marker.sizeScale - marker.previousSizeScale) * event.tickDelta().coerceIn(0f, 1f)
        val renderedHeadSize = markerSize * headScale
        val headHalf = renderedHeadSize / 2f

        drawRadarHead(
            render,
            target,
            marker.friend,
            drawCenterX - headHalf,
            drawCenterY - headHalf,
            renderedHeadSize,
            event.tickDelta(),
            visibility
        )
    }

    // Размер GPS полностью привязан к общему размеру элементов: 25 при 32.
    val gpsBaseHeight = 25f * (markerSize / 32f)
    val gpsPosition = radarGpsPosition
    val gpsVisibility = radarInterpolatedVisibility(
        radarGpsPreviousVisibility,
        radarGpsVisibility,
        event.tickDelta()
    )
    if (gpsPosition != null && gpsVisibility > 0.001f) {
        val deltaX = gpsPosition.x() - playerPosition.x()
        val deltaZ = gpsPosition.z() - playerPosition.z()
        val projectedX = -deltaX * yawCos - deltaZ * yawSin
        val projectedY = deltaX * yawSin - deltaZ * yawCos

        // Мировая GPS-точка всегда проходит через ту же обратную перспективу,
        // что сетка. Поэтому она остаётся внутри своего реального чанка даже
        // далеко у горизонта — обычная дальность радара здесь не используется.
        val planeX = (projectedX * pixelsPerBlock).toFloat()
        val planeY = (projectedY * pixelsPerBlock).toFloat()
        val normalizedPlaneY = planeY / (mapHeight * 0.5f)
        val anchorScale = 1f + projectionPerspective * projectionAnchorY
        val denominator = anchorScale - normalizedPlaneY * projectionPerspective
        val playerScreenX = centerX
        val playerScreenY = centerY + projectionAnchorY * (mapHeight * 0.5f)

        var exactGpsX = playerScreenX
        var exactGpsY = playerScreenY
        if (denominator > 0.001f) {
            val screenY = (anchorScale * projectionAnchorY + normalizedPlaneY) / denominator
            val widthScale = (1f + projectionPerspective * screenY) / anchorScale
            exactGpsX = centerX + planeX * widthScale
            exactGpsY = centerY + screenY * (mapHeight * 0.5f)
        }

        val baseHalfWidth = gpsBaseHeight * 30f / 50f / 2f
        val baseSafeLeft = mapX + baseHalfWidth
        val baseSafeRight = mapX + mapWidth - baseHalfWidth
        val baseSafeTop = mapY + gpsBaseHeight
        val baseSafeBottom = mapY + mapHeight
        val gpsInsideMap = denominator > 0.001f &&
            exactGpsX >= baseSafeLeft && exactGpsX <= baseSafeRight &&
            exactGpsY >= baseSafeTop && exactGpsY <= baseSafeBottom

        // За границей маркер на 3 px меньше. Плавное приближение размера даёт
        // короткий scale-эффект при входе в основную область радара и выходе.
        val targetGpsHeight = if (gpsInsideMap) gpsBaseHeight else (gpsBaseHeight - 3f).coerceAtLeast(5f)
        if (radarGpsRenderedHeight <= 0.001f) radarGpsRenderedHeight = targetGpsHeight
        radarGpsRenderedHeight += (targetGpsHeight - radarGpsRenderedHeight) * 0.22f

        val gpsHeight = radarGpsRenderedHeight
        val gpsHalfWidth = gpsHeight * 30f / 50f / 2f
        val safeLeft = mapX + gpsHalfWidth
        val safeRight = mapX + mapWidth - gpsHalfWidth
        val safeTop = mapY + gpsHeight
        val safeBottom = mapY + mapHeight

        val gpsX: Float
        val gpsY: Float
        if (gpsInsideMap) {
            gpsX = exactGpsX
            gpsY = exactGpsY
        } else {
            // Если спроецированный чанк уже за HUD, пересекаем край лучом к
            // его точной экранной позиции. При переходе через границу обе
            // формулы дают одну точку, поэтому скачка больше нет.
            val rawRayX: Float
            val rawRayY: Float
            if (denominator > 0.001f) {
                rawRayX = exactGpsX - playerScreenX
                rawRayY = exactGpsY - playerScreenY
            } else {
                // Обратная сторона перспективного горизонта не имеет конечной
                // экранной координаты; там достаточно непрерывного направления.
                rawRayX = projectedX.toFloat()
                rawRayY = projectedY.toFloat()
            }
            val directionLength = kotlin.math.sqrt(
                rawRayX * rawRayX + rawRayY * rawRayY
            ).coerceAtLeast(0.001f)
            val rayX = rawRayX / directionLength
            val rayY = rawRayY / directionLength
            val edgeScaleX = if (rayX > 0.001f) {
                (safeRight - playerScreenX) / rayX
            } else if (rayX < -0.001f) {
                (safeLeft - playerScreenX) / rayX
            } else Float.POSITIVE_INFINITY
            val edgeScaleY = if (rayY > 0.001f) {
                (safeBottom - playerScreenY) / rayY
            } else if (rayY < -0.001f) {
                (safeTop - playerScreenY) / rayY
            } else Float.POSITIVE_INFINITY
            val edgeScale = minOf(edgeScaleX, edgeScaleY)
            gpsX = (playerScreenX + rayX * edgeScale).coerceIn(safeLeft, safeRight)
            gpsY = (playerScreenY + rayY * edgeScale).coerceIn(safeTop, safeBottom)
        }
        drawRadarGpsMarker(
            render,
            gpsX,
            gpsY,
            gpsHeight,
            radarGpsCircleColor.value(),
            gpsVisibility
        )
    } else {
        radarGpsRenderedHeight = 0f
    }

    // Своя стрелка всегда последняя: она не теряется под головой другого игрока.
    val arrowCenterY = centerY + projectionAnchorY * (mapHeight * 0.5f)
    drawRadarArrow(render, centerX, arrowCenterY, markerSize, radarArrowColor.value())

    render.roundedOutline(
        panelX,
        panelY,
        panelWidth,
        panelHeight,
        panelRadius,
        1f,
        Colors.rgba(255, 255, 255, if (radarEditMode && (hovered || radarDragging)) 95 else 35)
    )
}
