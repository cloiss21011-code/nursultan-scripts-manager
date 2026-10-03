name("Custom ESP V1")
description("Custom player ESP with boxes, nameplates, health, ping, equipment and profiles")
requireApi(9)


// =====================================================
// BASE HOTKEY
// =====================================================

val managerKey = hotkey(
    "Open Custom ESP",
    Key.F9
) {
}


// =====================================================
// TABS
// =====================================================

val TAB_ESP = 0
val TAB_NAME = 1
val TAB_FILTERS = 2
val TAB_PROFILES = 3
val TAB_STYLE = 4


// =====================================================
// TEXT FIELDS
// =====================================================

val FIELD_NONE = 0
val FIELD_PROFILE = 1
val FIELD_THEME = 2


// =====================================================
// COLOR TARGETS
// =====================================================

val COLOR_NONE = 0

val COLOR_ENEMY = 1
val COLOR_RELATION = 2

val COLOR_HEALTH_GOOD = 3
val COLOR_HEALTH_BAD = 4

val COLOR_NAME_BG = 5
val COLOR_NAME_TEXT = 6

val COLOR_WINDOW = 7
val COLOR_PANEL = 8
val COLOR_ACCENT = 9


// =====================================================
// BOX MODES
// =====================================================

// 0 = Full 2D
// 1 = Corners
// 2 = 3D
// 3 = None

var boxMode = 1


// =====================================================
// ESP
// =====================================================

var espEnabled = true

var boxFill = true
var boxGlow = true

var tracerEnabled = false
var throughWalls = true

var hurtFlash = true
var distanceFade = true

var boxThickness = 1.8f
var cornerLength = 0.27f

var boxPadding = 2f
var boxRadius = 4f

var boxFillAlpha = 22f
var glowStrength = 0.55f

var tracerAlpha = 175f


// =====================================================
// NAMEPLATE
// =====================================================

var showNameplate = true

var showHead = true

var showHealthText = true
var showDistance = true
var showPing = true

var showArmor = true
var showGameMode = false
var showAbsorption = true

var showHealthBar = true

var nameSize = 9.5f
var nameOffset = 9f

var namePadding = 6f
var nameRadius = 7f

var nameBlur = 10f
var nameOpacity = 195f

var healthBarWidth = 4f
var armorIconSize = 14f


// =====================================================
// TARGET FILTERS
// =====================================================

var maxDistance = 128f

var showFriends = false
var showParty = false
var showAllies = false

var showBots = false
var showInvisible = true
var showSpectators = false

var ignoreSameTeam = true


// =====================================================
// ESP COLORS
// =====================================================

var enemyColor =
    0xFF65C8FFL.toInt()

var relationColor =
    0xFF63E6A6L.toInt()

var healthGoodColor =
    0xFF64E572L.toInt()

var healthBadColor =
    0xFFFF5364L.toInt()

var nameBackgroundColor =
    0xE80B1018L.toInt()

var nameTextColor =
    0xFFF5F8FFL.toInt()


// =====================================================
// WINDOW STYLE
// =====================================================

var uiBlur = 16f
var uiPanelBlur = 10f

var uiOpacity = 220f
var uiBackdrop = 110f

var uiScale = 1.0f
var uiTextScale = 1.15f

var uiRadius = 14f

// 0 Fade
// 1 Slide Up
// 2 Slide Down
var uiAnimation = 1

var uiAnimationSpeed = 150f

var uiWindowColor =
    0xFF0D121BL.toInt()

var uiPanelColor =
    0xFF171F2CL.toInt()

var uiAccentColor =
    0xFF72C7FFL.toInt()

var uiUseClientAccent = false


// =====================================================
// WINDOW STRIP
// =====================================================

var stripEnabled = true

// 0 left
// 1 top
// 2 right
// 3 bottom

var stripPosition = 1

var stripThickness = 4f
var stripInset = 7f
var stripLength = 0.80f


// =====================================================
// MENU STATE
// =====================================================

var menuOpen = false

var menuTab =
    TAB_ESP

var menuAlpha = 0f
var lastRenderMs = 0L

var menuX = 0f
var menuY = 0f

var menuW = 0f
var menuH = 0f

var menuCenterX = 0.5
var menuCenterY = 0.5

var windowDragging = false

var dragOffsetX = 0f
var dragOffsetY = 0f

var statusText = "READY"


// =====================================================
// SLIDER DRAG
// =====================================================

var draggingSlider = -1

// Important:
//
// These coordinates are frozen at mouse-down.
// Therefore Menu Scale cannot move its own slider while dragging.

var dragTrackX = 0f
var dragTrackW = 1f


// =====================================================
// NUMERIC EDIT
// =====================================================

var numericEditId = -1
var numericEditText = ""


// =====================================================
// PROFILE INPUT
// =====================================================

var focusedField =
    FIELD_NONE

var profileNameText = ""
var themeNameText = ""

var selectedEspProfile = ""
var selectedThemeProfile = ""

var espProfileScroll = 0
var themeProfileScroll = 0


// =====================================================
// COLOR PICKER
// =====================================================

var activeColor =
    COLOR_NONE

var pickerHue = 0f
var pickerSat = 0f
var pickerValue = 1f

var draggingColorSV = false
var draggingColorHue = false


// =====================================================
// HELPERS
// =====================================================

fun inside(
    x: Float,
    y: Float,
    rx: Float,
    ry: Float,
    rw: Float,
    rh: Float
): Boolean {

    return (
        x >= rx &&
        x <= rx + rw &&
        y >= ry &&
        y <= ry + rh
    )
}


fun accent(): Int {

    return if (
        uiUseClientAccent
    ) {
        theme.accent()
    } else {
        uiAccentColor
    }
}


fun cleanNumber(
    value: Float
): String {

    val rounded =
        kotlin.math.round(
            value *
            100f
        ) /
        100f


    val integer =
        rounded.toInt()


    return if (
        kotlin.math.abs(
            rounded -
            integer.toFloat()
        ) <
        0.001f
    ) {

        integer.toString()

    } else {

        rounded.toString()
    }
}


fun boxModeName(): String {

    return when (
        boxMode
    ) {

        0 ->
            "FULL 2D"

        1 ->
            "CORNERS"

        2 ->
            "3D BOX"

        else ->
            "OFF"
    }
}


fun stripName(): String {

    return when (
        stripPosition
    ) {

        0 -> "LEFT"
        1 -> "TOP"
        2 -> "RIGHT"

        else ->
            "BOTTOM"
    }
}


fun animationName(): String {

    return when (
        uiAnimation
    ) {

        0 -> "FADE"
        1 -> "SLIDE UP"

        else ->
            "SLIDE DOWN"
    }
}


fun colorHex(
    color: Int
): String {

    return "#" +
        (
            color and
            0xFFFFFF
        ).toString(
            16
        )
            .uppercase()
            .padStart(
                6,
                '0'
            )
}


// =====================================================
// HSV
// =====================================================

fun hsvToColor(
    hueRaw: Float,
    satRaw: Float,
    valueRaw: Float
): Int {

    var hue =
        hueRaw


    while (
        hue <
        0f
    ) {
        hue +=
            1f
    }


    while (
        hue >=
        1f
    ) {
        hue -=
            1f
    }


    val saturation =
        satRaw.coerceIn(
            0f,
            1f
        )


    val value =
        valueRaw.coerceIn(
            0f,
            1f
        )


    if (
        saturation <=
        0f
    ) {

        val channel =
            (
                value *
                255f +
                0.5f
            ).toInt()


        return Colors.rgb(
            channel,
            channel,
            channel
        )
    }


    val scaled =
        hue *
        6f


    val sector =
        kotlin.math.floor(
            scaled.toDouble()
        ).toInt()


    val fraction =
        scaled -
        sector.toFloat()


    val p =
        value *
        (
            1f -
            saturation
        )


    val q =
        value *
        (
            1f -
            saturation *
            fraction
        )


    val t =
        value *
        (
            1f -
            saturation *
            (
                1f -
                fraction
            )
        )


    val rgb =
        when (
            sector %
            6
        ) {

            0 ->
                Triple(
                    value,
                    t,
                    p
                )

            1 ->
                Triple(
                    q,
                    value,
                    p
                )

            2 ->
                Triple(
                    p,
                    value,
                    t
                )

            3 ->
                Triple(
                    p,
                    q,
                    value
                )

            4 ->
                Triple(
                    t,
                    p,
                    value
                )

            else ->
                Triple(
                    value,
                    p,
                    q
                )
        }


    return Colors.rgb(
        (
            rgb.first *
            255f +
            0.5f
        ).toInt(),
        (
            rgb.second *
            255f +
            0.5f
        ).toInt(),
        (
            rgb.third *
            255f +
            0.5f
        ).toInt()
    )
}


fun colorToHsv(
    color: Int
): Triple<Float, Float, Float> {

    val red =
        Colors.red(
            color
        ).toFloat() /
        255f


    val green =
        Colors.green(
            color
        ).toFloat() /
        255f


    val blue =
        Colors.blue(
            color
        ).toFloat() /
        255f


    val maximum =
        kotlin.math.max(
            red,
            kotlin.math.max(
                green,
                blue
            )
        )


    val minimum =
        kotlin.math.min(
            red,
            kotlin.math.min(
                green,
                blue
            )
        )


    val delta =
        maximum -
        minimum


    var hue =
        0f


    if (
        delta >
        0.00001f
    ) {

        hue =
            when (
                maximum
            ) {

                red ->
                    (
                        (
                            green -
                            blue
                        ) /
                        delta
                    ) %
                    6f

                green ->
                    (
                        (
                            blue -
                            red
                        ) /
                        delta
                    ) +
                    2f

                else ->
                    (
                        (
                            red -
                            green
                        ) /
                        delta
                    ) +
                    4f
            }


        hue /=
            6f


        if (
            hue <
            0f
        ) {

            hue +=
                1f
        }
    }


    val saturation =
        if (
            maximum <=
            0f
        ) {

            0f

        } else {

            delta /
            maximum
        }


    return Triple(
        hue,
        saturation,
        maximum
    )
}


fun pickerColor(): Int {

    return when (
        activeColor
    ) {

        COLOR_ENEMY ->
            enemyColor

        COLOR_RELATION ->
            relationColor

        COLOR_HEALTH_GOOD ->
            healthGoodColor

        COLOR_HEALTH_BAD ->
            healthBadColor

        COLOR_NAME_BG ->
            nameBackgroundColor

        COLOR_NAME_TEXT ->
            nameTextColor

        COLOR_WINDOW ->
            uiWindowColor

        COLOR_PANEL ->
            uiPanelColor

        COLOR_ACCENT ->
            uiAccentColor

        else ->
            Colors.WHITE
    }
}


fun setPickerColor(
    color: Int
) {

    when (
        activeColor
    ) {

        COLOR_ENEMY ->
            enemyColor =
                color

        COLOR_RELATION ->
            relationColor =
                color

        COLOR_HEALTH_GOOD ->
            healthGoodColor =
                color

        COLOR_HEALTH_BAD ->
            healthBadColor =
                color

        COLOR_NAME_BG ->
            nameBackgroundColor =
                color

        COLOR_NAME_TEXT ->
            nameTextColor =
                color

        COLOR_WINDOW ->
            uiWindowColor =
                color

        COLOR_PANEL ->
            uiPanelColor =
                color

        COLOR_ACCENT ->
            uiAccentColor =
                color
    }
}


fun openPicker(
    target: Int
) {

    activeColor =
        target


    val hsv =
        colorToHsv(
            pickerColor()
        )


    pickerHue =
        hsv.first

    pickerSat =
        hsv.second

    pickerValue =
        hsv.third
}


// =====================================================
// PICKER GEOMETRY
// =====================================================

fun pickerX(): Float {

    return menuX +
        565f *
        uiScale
}


fun pickerY(): Float {

    return menuY +
        200f *
        uiScale
}


fun pickerW(): Float {

    return 280f *
        uiScale
}


fun pickerH(): Float {

    return 180f *
        uiScale
}


fun pickerHueY(): Float {

    return pickerY() +
        pickerH() +
        14f *
        uiScale
}


fun updatePickerSV() {

    pickerSat =
        (
            (
                keys.mouseX() -
                pickerX()
            ) /
            pickerW()
        ).coerceIn(
            0f,
            1f
        )


    pickerValue =
        (
            1f -
            (
                (
                    keys.mouseY() -
                    pickerY()
                ) /
                pickerH()
            )
        ).coerceIn(
            0f,
            1f
        )


    setPickerColor(
        hsvToColor(
            pickerHue,
            pickerSat,
            pickerValue
        )
    )
}


fun updatePickerHue() {

    pickerHue =
        (
            (
                keys.mouseX() -
                pickerX()
            ) /
            pickerW()
        ).coerceIn(
            0f,
            1f
        )


    setPickerColor(
        hsvToColor(
            pickerHue,
            pickerSat,
            pickerValue
        )
    )
}


// =====================================================
// SLIDERS
// =====================================================

// Style: 0..10
//
// ESP:
// 20 thickness
// 21 corner
// 22 padding
// 23 radius
// 24 fill alpha
// 25 glow
// 26 tracer alpha
//
// Name:
// 40 name size
// 41 offset
// 42 padding
// 43 radius
// 44 blur
// 45 opacity
// 46 health bar width
// 47 armor size
//
// Filters:
// 60 max distance


fun sliderValue(
    id: Int
): Float {

    return when (
        id
    ) {

        // Style
        0 -> uiBlur
        1 -> uiPanelBlur
        2 -> uiOpacity
        3 -> uiBackdrop
        4 -> uiScale
        5 -> uiTextScale
        6 -> uiRadius
        7 -> uiAnimationSpeed
        8 -> stripThickness
        9 -> stripLength
        10 -> stripInset

        // ESP
        20 -> boxThickness
        21 -> cornerLength
        22 -> boxPadding
        23 -> boxRadius
        24 -> boxFillAlpha
        25 -> glowStrength
        26 -> tracerAlpha

        // Name
        40 -> nameSize
        41 -> nameOffset
        42 -> namePadding
        43 -> nameRadius
        44 -> nameBlur
        45 -> nameOpacity
        46 -> healthBarWidth
        47 -> armorIconSize

        // Filter
        else ->
            maxDistance
    }
}


fun sliderMin(
    id: Int
): Float {

    return when (
        id
    ) {

        0 -> 0f
        1 -> 0f
        2 -> 70f
        3 -> 0f
        4 -> 0.70f
        5 -> 0.80f
        6 -> 0f
        7 -> 60f
        8 -> 1f
        9 -> 0.20f
        10 -> 0f

        20 -> 0.5f
        21 -> 0.10f
        22 -> 0f
        23 -> 0f
        24 -> 0f
        25 -> 0f
        26 -> 20f

        40 -> 6f
        41 -> 0f
        42 -> 2f
        43 -> 0f
        44 -> 0f
        45 -> 50f
        46 -> 2f
        47 -> 8f

        else ->
            8f
    }
}


fun sliderMax(
    id: Int
): Float {

    return when (
        id
    ) {

        0 -> 30f
        1 -> 30f
        2 -> 255f
        3 -> 220f
        4 -> 1.30f
        5 -> 1.60f
        6 -> 28f
        7 -> 400f
        8 -> 12f
        9 -> 1f
        10 -> 24f

        20 -> 6f
        21 -> 0.50f
        22 -> 10f
        23 -> 14f
        24 -> 120f
        25 -> 1f
        26 -> 255f

        40 -> 16f
        41 -> 30f
        42 -> 14f
        43 -> 18f
        44 -> 24f
        45 -> 255f
        46 -> 9f
        47 -> 24f

        else ->
            256f
    }
}


fun sliderLabel(
    id: Int
): String {

    return when (
        id
    ) {

        0 -> "Background blur"
        1 -> "Panel blur"
        2 -> "Window opacity"
        3 -> "Backdrop darkness"
        4 -> "Menu scale"
        5 -> "Text scale"
        6 -> "Corner radius"
        7 -> "Animation speed"
        8 -> "Strip thickness"
        9 -> "Strip length"
        10 -> "Strip inset"

        20 -> "Box thickness"
        21 -> "Corner length"
        22 -> "Box padding"
        23 -> "Box radius"
        24 -> "Fill opacity"
        25 -> "Glow strength"
        26 -> "Tracer opacity"

        40 -> "Name size"
        41 -> "Name offset"
        42 -> "Plate padding"
        43 -> "Plate radius"
        44 -> "Plate blur"
        45 -> "Plate opacity"
        46 -> "Health bar width"
        47 -> "Armor icon size"

        else ->
            "Max distance"
    }
}


fun setSliderValue(
    id: Int,
    raw: Float
) {

    val value =
        raw.coerceIn(
            sliderMin(
                id
            ),
            sliderMax(
                id
            )
        )


    when (
        id
    ) {

        0 ->
            uiBlur =
                value

        1 ->
            uiPanelBlur =
                value

        2 ->
            uiOpacity =
                value

        3 ->
            uiBackdrop =
                value

        4 ->
            uiScale =
                value

        5 ->
            uiTextScale =
                value

        6 ->
            uiRadius =
                value

        7 ->
            uiAnimationSpeed =
                value

        8 ->
            stripThickness =
                value

        9 ->
            stripLength =
                value

        10 ->
            stripInset =
                value


        20 ->
            boxThickness =
                value

        21 ->
            cornerLength =
                value

        22 ->
            boxPadding =
                value

        23 ->
            boxRadius =
                value

        24 ->
            boxFillAlpha =
                value

        25 ->
            glowStrength =
                value

        26 ->
            tracerAlpha =
                value


        40 ->
            nameSize =
                value

        41 ->
            nameOffset =
                value

        42 ->
            namePadding =
                value

        43 ->
            nameRadius =
                value

        44 ->
            nameBlur =
                value

        45 ->
            nameOpacity =
                value

        46 ->
            healthBarWidth =
                value

        47 ->
            armorIconSize =
                value


        else ->
            maxDistance =
                value
    }
}


// =====================================================
// SLIDER GEOMETRY
// =====================================================

fun sliderTrackX(
    id: Int
): Float {

    return when {

        id in
        0..10 ->

            menuX +
            225f *
            uiScale


        id in
        20..26 ->

            menuX +
            225f *
            uiScale


        id in
        40..47 ->

            menuX +
            225f *
            uiScale


        else ->

            menuX +
            280f *
            uiScale
    }
}


fun sliderTrackY(
    id: Int
): Float {

    return when {

        id in
        0..10 ->

            menuY +
            (
                128f +
                id *
                33f
            ) *
            uiScale


        id in
        20..26 ->

            menuY +
            (
                395f +
                (
                    id -
                    20
                ) *
                29f
            ) *
            uiScale


        id in
        40..47 ->

            menuY +
            (
                372f +
                (
                    id -
                    40
                ) *
                28f
            ) *
            uiScale


        else ->

            menuY +
            465f *
            uiScale
    }
}


fun sliderTrackW(
    id: Int
): Float {

    return when {

        id in
        0..10 ->
            195f *
            uiScale

        id in
        20..26 ->
            180f *
            uiScale

        id in
        40..47 ->
            180f *
            uiScale

        else ->
            260f *
            uiScale
    }
}


fun numericBoxX(
    id: Int
): Float {

    return sliderTrackX(
        id
    ) +
        sliderTrackW(
            id
        ) +
        10f *
        uiScale
}


fun numericBoxY(
    id: Int
): Float {

    return sliderTrackY(
        id
    ) -
        12f *
        uiScale
}


fun numericBoxW(): Float {

    return 68f *
        uiScale
}


fun numericBoxH(): Float {

    return 26f *
        uiScale
}


// =====================================================
// FIXED SLIDER DRAG
// =====================================================

fun beginSliderDrag(
    id: Int
) {

    draggingSlider =
        id


    dragTrackX =
        sliderTrackX(
            id
        )


    dragTrackW =
        kotlin.math.max(
            1f,
            sliderTrackW(
                id
            )
        )


    updateDraggedSlider()
}


fun updateDraggedSlider() {

    if (
        draggingSlider <
        0
    ) {
        return
    }


    val progress =
        (
            (
                keys.mouseX() -
                dragTrackX
            ) /
            dragTrackW
        ).coerceIn(
            0f,
            1f
        )


    setSliderValue(
        draggingSlider,
        sliderMin(
            draggingSlider
        ) +
        (
            sliderMax(
                draggingSlider
            ) -
            sliderMin(
                draggingSlider
            )
        ) *
        progress
    )
}


// =====================================================
// NUMERIC INPUT
// =====================================================

fun startNumericEdit(
    id: Int
) {

    numericEditId =
        id


    numericEditText =
        cleanNumber(
            sliderValue(
                id
            )
        )


    focusedField =
        FIELD_NONE
}


fun cancelNumericEdit() {

    numericEditId =
        -1

    numericEditText =
        ""
}


fun commitNumericEdit() {

    val id =
        numericEditId


    if (
        id <
        0
    ) {
        return
    }


    val parsed =
        numericEditText
            .replace(
                ',',
                '.'
            )
            .toFloatOrNull()


    if (
        parsed !=
        null
    ) {

        setSliderValue(
            id,
            parsed
        )


        statusText =
            "${sliderLabel(id)} = ${cleanNumber(sliderValue(id))}"


        saveStorage()
    }


    cancelNumericEdit()
}


// =====================================================
// CONFIG NAME
// =====================================================

fun cleanName(
    raw: String
): String {

    val result =
        StringBuilder()


    for (
        c in
        raw.trim()
            .lowercase()
    ) {

        when {

            c in 'a'..'z' ->
                result.append(
                    c
                )

            c in '0'..'9' ->
                result.append(
                    c
                )

            c == '_' ||
            c == '-' ->
                result.append(
                    c
                )

            c == ' ' ->
                result.append(
                    '_'
                )
        }


        if (
            result.length >=
            24
        ) {
            break
        }
    }


    return result.toString()
}


fun espConfigName(
    title: String
): String {

    val clean =
        cleanName(
            title
        )


    return if (
        clean.isBlank()
    ) {
        ""
    } else {
        "esp_$clean"
    }
}


fun themeConfigName(
    title: String
): String {

    val clean =
        cleanName(
            title
        )


    return if (
        clean.isBlank()
    ) {
        ""
    } else {
        "esptheme_$clean"
    }
}


// =====================================================
// PROFILE LISTS
// =====================================================

fun espProfiles(): List<String> {

    return configs.names()
        .filter {
            it.startsWith(
                "esp_"
            ) &&
            !it.startsWith(
                "esptheme_"
            )
        }
}


fun themeProfiles(): List<String> {

    return configs.names()
        .filter {
            it.startsWith(
                "esptheme_"
            )
        }
}


fun espProfileTitle(
    name: String
): String {

    if (
        name.isBlank()
    ) {
        return ""
    }


    return config(
        name
    ).get(
        "title",
        name.removePrefix(
            "esp_"
        )
    )
}


fun themeProfileTitle(
    name: String
): String {

    if (
        name.isBlank()
    ) {
        return ""
    }


    return config(
        name
    ).get(
        "title",
        name.removePrefix(
            "esptheme_"
        )
    )
}


// =====================================================
// SAVE MAIN STORAGE
// =====================================================

fun saveStorage() {

    storage.put(
        "esp_enabled",
        espEnabled
    )

    storage.put(
        "box_mode",
        boxMode
    )

    storage.put(
        "box_fill",
        boxFill
    )

    storage.put(
        "box_glow",
        boxGlow
    )

    storage.put(
        "tracer",
        tracerEnabled
    )

    storage.put(
        "through_walls",
        throughWalls
    )

    storage.put(
        "hurt_flash",
        hurtFlash
    )

    storage.put(
        "distance_fade",
        distanceFade
    )


    storage.put(
        "box_thickness",
        boxThickness.toDouble()
    )

    storage.put(
        "corner_length",
        cornerLength.toDouble()
    )

    storage.put(
        "box_padding",
        boxPadding.toDouble()
    )

    storage.put(
        "box_radius",
        boxRadius.toDouble()
    )

    storage.put(
        "box_fill_alpha",
        boxFillAlpha.toDouble()
    )

    storage.put(
        "glow_strength",
        glowStrength.toDouble()
    )

    storage.put(
        "tracer_alpha",
        tracerAlpha.toDouble()
    )


    storage.put(
        "show_name",
        showNameplate
    )

    storage.put(
        "show_head",
        showHead
    )

    storage.put(
        "health_text",
        showHealthText
    )

    storage.put(
        "distance",
        showDistance
    )

    storage.put(
        "ping",
        showPing
    )

    storage.put(
        "armor",
        showArmor
    )

    storage.put(
        "gamemode",
        showGameMode
    )

    storage.put(
        "absorption",
        showAbsorption
    )

    storage.put(
        "health_bar",
        showHealthBar
    )


    storage.put(
        "name_size",
        nameSize.toDouble()
    )

    storage.put(
        "name_offset",
        nameOffset.toDouble()
    )

    storage.put(
        "name_padding",
        namePadding.toDouble()
    )

    storage.put(
        "name_radius",
        nameRadius.toDouble()
    )

    storage.put(
        "name_blur",
        nameBlur.toDouble()
    )

    storage.put(
        "name_opacity",
        nameOpacity.toDouble()
    )

    storage.put(
        "health_width",
        healthBarWidth.toDouble()
    )

    storage.put(
        "armor_size",
        armorIconSize.toDouble()
    )


    storage.put(
        "max_distance",
        maxDistance.toDouble()
    )

    storage.put(
        "friends",
        showFriends
    )

    storage.put(
        "party",
        showParty
    )

    storage.put(
        "allies",
        showAllies
    )

    storage.put(
        "bots",
        showBots
    )

    storage.put(
        "invisible",
        showInvisible
    )

    storage.put(
        "spectators",
        showSpectators
    )

    storage.put(
        "same_team",
        ignoreSameTeam
    )


    storage.put(
        "enemy_color",
        enemyColor
    )

    storage.put(
        "relation_color",
        relationColor
    )

    storage.put(
        "health_good",
        healthGoodColor
    )

    storage.put(
        "health_bad",
        healthBadColor
    )

    storage.put(
        "name_bg",
        nameBackgroundColor
    )

    storage.put(
        "name_text",
        nameTextColor
    )


    storage.put(
        "menu_x",
        menuCenterX
    )

    storage.put(
        "menu_y",
        menuCenterY
    )


    storage.put(
        "ui_blur",
        uiBlur.toDouble()
    )

    storage.put(
        "ui_panel_blur",
        uiPanelBlur.toDouble()
    )

    storage.put(
        "ui_opacity",
        uiOpacity.toDouble()
    )

    storage.put(
        "ui_backdrop",
        uiBackdrop.toDouble()
    )

    storage.put(
        "ui_scale",
        uiScale.toDouble()
    )

    storage.put(
        "ui_text_scale",
        uiTextScale.toDouble()
    )

    storage.put(
        "ui_radius",
        uiRadius.toDouble()
    )

    storage.put(
        "ui_animation",
        uiAnimation
    )

    storage.put(
        "ui_animation_speed",
        uiAnimationSpeed.toDouble()
    )


    storage.put(
        "window_color",
        uiWindowColor
    )

    storage.put(
        "panel_color",
        uiPanelColor
    )

    storage.put(
        "accent_color",
        uiAccentColor
    )

    storage.put(
        "client_accent",
        uiUseClientAccent
    )


    storage.put(
        "strip",
        stripEnabled
    )

    storage.put(
        "strip_position",
        stripPosition
    )

    storage.put(
        "strip_thickness",
        stripThickness.toDouble()
    )

    storage.put(
        "strip_inset",
        stripInset.toDouble()
    )

    storage.put(
        "strip_length",
        stripLength.toDouble()
    )


    storage.save()
}


// =====================================================
// LOAD STORAGE
// =====================================================

fun loadStorage() {

    espEnabled =
        storage.getBoolean(
            "esp_enabled",
            true
        )


    boxMode =
        storage.getInt(
            "box_mode",
            1
        ).coerceIn(
            0,
            3
        )


    boxFill =
        storage.getBoolean(
            "box_fill",
            true
        )


    boxGlow =
        storage.getBoolean(
            "box_glow",
            true
        )


    tracerEnabled =
        storage.getBoolean(
            "tracer",
            false
        )


    throughWalls =
        storage.getBoolean(
            "through_walls",
            true
        )


    hurtFlash =
        storage.getBoolean(
            "hurt_flash",
            true
        )


    distanceFade =
        storage.getBoolean(
            "distance_fade",
            true
        )


    boxThickness =
        storage.getDouble(
            "box_thickness",
            1.8
        ).toFloat()
            .coerceIn(
                0.5f,
                6f
            )


    cornerLength =
        storage.getDouble(
            "corner_length",
            0.27
        ).toFloat()
            .coerceIn(
                0.10f,
                0.50f
            )


    boxPadding =
        storage.getDouble(
            "box_padding",
            2.0
        ).toFloat()
            .coerceIn(
                0f,
                10f
            )


    boxRadius =
        storage.getDouble(
            "box_radius",
            4.0
        ).toFloat()
            .coerceIn(
                0f,
                14f
            )


    boxFillAlpha =
        storage.getDouble(
            "box_fill_alpha",
            22.0
        ).toFloat()
            .coerceIn(
                0f,
                120f
            )


    glowStrength =
        storage.getDouble(
            "glow_strength",
            0.55
        ).toFloat()
            .coerceIn(
                0f,
                1f
            )


    tracerAlpha =
        storage.getDouble(
            "tracer_alpha",
            175.0
        ).toFloat()
            .coerceIn(
                20f,
                255f
            )


    showNameplate =
        storage.getBoolean(
            "show_name",
            true
        )


    showHead =
        storage.getBoolean(
            "show_head",
            true
        )


    showHealthText =
        storage.getBoolean(
            "health_text",
            true
        )


    showDistance =
        storage.getBoolean(
            "distance",
            true
        )


    showPing =
        storage.getBoolean(
            "ping",
            true
        )


    showArmor =
        storage.getBoolean(
            "armor",
            true
        )


    showGameMode =
        storage.getBoolean(
            "gamemode",
            false
        )


    showAbsorption =
        storage.getBoolean(
            "absorption",
            true
        )


    showHealthBar =
        storage.getBoolean(
            "health_bar",
            true
        )


    nameSize =
        storage.getDouble(
            "name_size",
            9.5
        ).toFloat()
            .coerceIn(
                6f,
                16f
            )


    nameOffset =
        storage.getDouble(
            "name_offset",
            9.0
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    namePadding =
        storage.getDouble(
            "name_padding",
            6.0
        ).toFloat()
            .coerceIn(
                2f,
                14f
            )


    nameRadius =
        storage.getDouble(
            "name_radius",
            7.0
        ).toFloat()
            .coerceIn(
                0f,
                18f
            )


    nameBlur =
        storage.getDouble(
            "name_blur",
            10.0
        ).toFloat()
            .coerceIn(
                0f,
                24f
            )


    nameOpacity =
        storage.getDouble(
            "name_opacity",
            195.0
        ).toFloat()
            .coerceIn(
                50f,
                255f
            )


    healthBarWidth =
        storage.getDouble(
            "health_width",
            4.0
        ).toFloat()
            .coerceIn(
                2f,
                9f
            )


    armorIconSize =
        storage.getDouble(
            "armor_size",
            14.0
        ).toFloat()
            .coerceIn(
                8f,
                24f
            )


    maxDistance =
        storage.getDouble(
            "max_distance",
            128.0
        ).toFloat()
            .coerceIn(
                8f,
                256f
            )


    showFriends =
        storage.getBoolean(
            "friends",
            false
        )


    showParty =
        storage.getBoolean(
            "party",
            false
        )


    showAllies =
        storage.getBoolean(
            "allies",
            false
        )


    showBots =
        storage.getBoolean(
            "bots",
            false
        )


    showInvisible =
        storage.getBoolean(
            "invisible",
            true
        )


    showSpectators =
        storage.getBoolean(
            "spectators",
            false
        )


    ignoreSameTeam =
        storage.getBoolean(
            "same_team",
            true
        )


    enemyColor =
        storage.getInt(
            "enemy_color",
            0xFF65C8FFL.toInt()
        )


    relationColor =
        storage.getInt(
            "relation_color",
            0xFF63E6A6L.toInt()
        )


    healthGoodColor =
        storage.getInt(
            "health_good",
            0xFF64E572L.toInt()
        )


    healthBadColor =
        storage.getInt(
            "health_bad",
            0xFFFF5364L.toInt()
        )


    nameBackgroundColor =
        storage.getInt(
            "name_bg",
            0xE80B1018L.toInt()
        )


    nameTextColor =
        storage.getInt(
            "name_text",
            0xFFF5F8FFL.toInt()
        )


    menuCenterX =
        storage.getDouble(
            "menu_x",
            0.5
        ).coerceIn(
            0.0,
            1.0
        )


    menuCenterY =
        storage.getDouble(
            "menu_y",
            0.5
        ).coerceIn(
            0.0,
            1.0
        )


    uiBlur =
        storage.getDouble(
            "ui_blur",
            16.0
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    uiPanelBlur =
        storage.getDouble(
            "ui_panel_blur",
            10.0
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    uiOpacity =
        storage.getDouble(
            "ui_opacity",
            220.0
        ).toFloat()
            .coerceIn(
                70f,
                255f
            )


    uiBackdrop =
        storage.getDouble(
            "ui_backdrop",
            110.0
        ).toFloat()
            .coerceIn(
                0f,
                220f
            )


    uiScale =
        storage.getDouble(
            "ui_scale",
            1.0
        ).toFloat()
            .coerceIn(
                0.70f,
                1.30f
            )


    uiTextScale =
        storage.getDouble(
            "ui_text_scale",
            1.15
        ).toFloat()
            .coerceIn(
                0.80f,
                1.60f
            )


    uiRadius =
        storage.getDouble(
            "ui_radius",
            14.0
        ).toFloat()
            .coerceIn(
                0f,
                28f
            )


    uiAnimation =
        storage.getInt(
            "ui_animation",
            1
        ).coerceIn(
            0,
            2
        )


    uiAnimationSpeed =
        storage.getDouble(
            "ui_animation_speed",
            150.0
        ).toFloat()
            .coerceIn(
                60f,
                400f
            )


    uiWindowColor =
        storage.getInt(
            "window_color",
            0xFF0D121BL.toInt()
        )


    uiPanelColor =
        storage.getInt(
            "panel_color",
            0xFF171F2CL.toInt()
        )


    uiAccentColor =
        storage.getInt(
            "accent_color",
            0xFF72C7FFL.toInt()
        )


    uiUseClientAccent =
        storage.getBoolean(
            "client_accent",
            false
        )


    stripEnabled =
        storage.getBoolean(
            "strip",
            true
        )


    stripPosition =
        storage.getInt(
            "strip_position",
            1
        ).coerceIn(
            0,
            3
        )


    stripThickness =
        storage.getDouble(
            "strip_thickness",
            4.0
        ).toFloat()
            .coerceIn(
                1f,
                12f
            )


    stripInset =
        storage.getDouble(
            "strip_inset",
            7.0
        ).toFloat()
            .coerceIn(
                0f,
                24f
            )


    stripLength =
        storage.getDouble(
            "strip_length",
            0.80
        ).toFloat()
            .coerceIn(
                0.20f,
                1f
            )
}


// =====================================================
// ESP PROFILE SAVE
// =====================================================

fun saveEspProfile(
    title: String
) {

    val name =
        espConfigName(
            title
        )


    if (
        name.isBlank()
    ) {

        statusText =
            "ENTER PROFILE NAME"

        return
    }


    val cfg =
        config(
            name
        )


    cfg.put(
        "title",
        title.trim()
    )


    cfg.put(
        "box_mode",
        boxMode
    )

    cfg.put(
        "box_fill",
        boxFill
    )

    cfg.put(
        "box_glow",
        boxGlow
    )

    cfg.put(
        "tracer",
        tracerEnabled
    )

    cfg.put(
        "through",
        throughWalls
    )

    cfg.put(
        "hurt",
        hurtFlash
    )

    cfg.put(
        "fade",
        distanceFade
    )


    cfg.put(
        "box_thickness",
        boxThickness.toDouble()
    )

    cfg.put(
        "corner",
        cornerLength.toDouble()
    )

    cfg.put(
        "padding",
        boxPadding.toDouble()
    )

    cfg.put(
        "radius",
        boxRadius.toDouble()
    )

    cfg.put(
        "fill_alpha",
        boxFillAlpha.toDouble()
    )

    cfg.put(
        "glow",
        glowStrength.toDouble()
    )

    cfg.put(
        "tracer_alpha",
        tracerAlpha.toDouble()
    )


    cfg.put(
        "name",
        showNameplate
    )

    cfg.put(
        "head",
        showHead
    )

    cfg.put(
        "health_text",
        showHealthText
    )

    cfg.put(
        "distance",
        showDistance
    )

    cfg.put(
        "ping",
        showPing
    )

    cfg.put(
        "armor",
        showArmor
    )

    cfg.put(
        "gamemode",
        showGameMode
    )

    cfg.put(
        "absorption",
        showAbsorption
    )

    cfg.put(
        "health_bar",
        showHealthBar
    )


    cfg.put(
        "name_size",
        nameSize.toDouble()
    )

    cfg.put(
        "name_offset",
        nameOffset.toDouble()
    )

    cfg.put(
        "name_padding",
        namePadding.toDouble()
    )

    cfg.put(
        "name_radius",
        nameRadius.toDouble()
    )

    cfg.put(
        "name_blur",
        nameBlur.toDouble()
    )

    cfg.put(
        "name_opacity",
        nameOpacity.toDouble()
    )

    cfg.put(
        "health_width",
        healthBarWidth.toDouble()
    )

    cfg.put(
        "armor_size",
        armorIconSize.toDouble()
    )


    cfg.put(
        "max_distance",
        maxDistance.toDouble()
    )

    cfg.put(
        "friends",
        showFriends
    )

    cfg.put(
        "party",
        showParty
    )

    cfg.put(
        "allies",
        showAllies
    )

    cfg.put(
        "bots",
        showBots
    )

    cfg.put(
        "invisible",
        showInvisible
    )

    cfg.put(
        "spectators",
        showSpectators
    )

    cfg.put(
        "same_team",
        ignoreSameTeam
    )


    cfg.put(
        "enemy_color",
        enemyColor
    )

    cfg.put(
        "relation_color",
        relationColor
    )

    cfg.put(
        "health_good",
        healthGoodColor
    )

    cfg.put(
        "health_bad",
        healthBadColor
    )

    cfg.put(
        "name_bg",
        nameBackgroundColor
    )

    cfg.put(
        "name_text",
        nameTextColor
    )


    cfg.save()


    selectedEspProfile =
        name


    statusText =
        "ESP PROFILE SAVED"
}


// =====================================================
// ESP PROFILE LOAD
// =====================================================

fun loadEspProfile(
    name: String
) {

    if (
        name.isBlank() ||
        !configs.exists(
            name
        )
    ) {
        return
    }


    val cfg =
        config(
            name
        )


    boxMode =
        cfg.getInt(
            "box_mode",
            boxMode
        ).coerceIn(
            0,
            3
        )


    boxFill =
        cfg.getBoolean(
            "box_fill",
            boxFill
        )


    boxGlow =
        cfg.getBoolean(
            "box_glow",
            boxGlow
        )


    tracerEnabled =
        cfg.getBoolean(
            "tracer",
            tracerEnabled
        )


    throughWalls =
        cfg.getBoolean(
            "through",
            throughWalls
        )


    hurtFlash =
        cfg.getBoolean(
            "hurt",
            hurtFlash
        )


    distanceFade =
        cfg.getBoolean(
            "fade",
            distanceFade
        )


    boxThickness =
        cfg.getDouble(
            "box_thickness",
            boxThickness.toDouble()
        ).toFloat()
            .coerceIn(
                0.5f,
                6f
            )


    cornerLength =
        cfg.getDouble(
            "corner",
            cornerLength.toDouble()
        ).toFloat()
            .coerceIn(
                0.10f,
                0.50f
            )


    boxPadding =
        cfg.getDouble(
            "padding",
            boxPadding.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                10f
            )


    boxRadius =
        cfg.getDouble(
            "radius",
            boxRadius.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                14f
            )


    boxFillAlpha =
        cfg.getDouble(
            "fill_alpha",
            boxFillAlpha.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                120f
            )


    glowStrength =
        cfg.getDouble(
            "glow",
            glowStrength.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                1f
            )


    tracerAlpha =
        cfg.getDouble(
            "tracer_alpha",
            tracerAlpha.toDouble()
        ).toFloat()
            .coerceIn(
                20f,
                255f
            )


    showNameplate =
        cfg.getBoolean(
            "name",
            showNameplate
        )


    showHead =
        cfg.getBoolean(
            "head",
            showHead
        )


    showHealthText =
        cfg.getBoolean(
            "health_text",
            showHealthText
        )


    showDistance =
        cfg.getBoolean(
            "distance",
            showDistance
        )


    showPing =
        cfg.getBoolean(
            "ping",
            showPing
        )


    showArmor =
        cfg.getBoolean(
            "armor",
            showArmor
        )


    showGameMode =
        cfg.getBoolean(
            "gamemode",
            showGameMode
        )


    showAbsorption =
        cfg.getBoolean(
            "absorption",
            showAbsorption
        )


    showHealthBar =
        cfg.getBoolean(
            "health_bar",
            showHealthBar
        )


    nameSize =
        cfg.getDouble(
            "name_size",
            nameSize.toDouble()
        ).toFloat()
            .coerceIn(
                6f,
                16f
            )


    nameOffset =
        cfg.getDouble(
            "name_offset",
            nameOffset.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    namePadding =
        cfg.getDouble(
            "name_padding",
            namePadding.toDouble()
        ).toFloat()
            .coerceIn(
                2f,
                14f
            )


    nameRadius =
        cfg.getDouble(
            "name_radius",
            nameRadius.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                18f
            )


    nameBlur =
        cfg.getDouble(
            "name_blur",
            nameBlur.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                24f
            )


    nameOpacity =
        cfg.getDouble(
            "name_opacity",
            nameOpacity.toDouble()
        ).toFloat()
            .coerceIn(
                50f,
                255f
            )


    healthBarWidth =
        cfg.getDouble(
            "health_width",
            healthBarWidth.toDouble()
        ).toFloat()
            .coerceIn(
                2f,
                9f
            )


    armorIconSize =
        cfg.getDouble(
            "armor_size",
            armorIconSize.toDouble()
        ).toFloat()
            .coerceIn(
                8f,
                24f
            )


    maxDistance =
        cfg.getDouble(
            "max_distance",
            maxDistance.toDouble()
        ).toFloat()
            .coerceIn(
                8f,
                256f
            )


    showFriends =
        cfg.getBoolean(
            "friends",
            showFriends
        )


    showParty =
        cfg.getBoolean(
            "party",
            showParty
        )


    showAllies =
        cfg.getBoolean(
            "allies",
            showAllies
        )


    showBots =
        cfg.getBoolean(
            "bots",
            showBots
        )


    showInvisible =
        cfg.getBoolean(
            "invisible",
            showInvisible
        )


    showSpectators =
        cfg.getBoolean(
            "spectators",
            showSpectators
        )


    ignoreSameTeam =
        cfg.getBoolean(
            "same_team",
            ignoreSameTeam
        )


    enemyColor =
        cfg.getInt(
            "enemy_color",
            enemyColor
        )


    relationColor =
        cfg.getInt(
            "relation_color",
            relationColor
        )


    healthGoodColor =
        cfg.getInt(
            "health_good",
            healthGoodColor
        )


    healthBadColor =
        cfg.getInt(
            "health_bad",
            healthBadColor
        )


    nameBackgroundColor =
        cfg.getInt(
            "name_bg",
            nameBackgroundColor
        )


    nameTextColor =
        cfg.getInt(
            "name_text",
            nameTextColor
        )


    selectedEspProfile =
        name


    saveStorage()


    statusText =
        "ESP PROFILE LOADED"
}


// =====================================================
// THEME SAVE
// =====================================================

fun saveThemeProfile(
    title: String
) {

    val name =
        themeConfigName(
            title
        )


    if (
        name.isBlank()
    ) {

        statusText =
            "ENTER THEME NAME"

        return
    }


    val cfg =
        config(
            name
        )


    cfg.put(
        "title",
        title.trim()
    )


    cfg.put(
        "blur",
        uiBlur.toDouble()
    )

    cfg.put(
        "panel_blur",
        uiPanelBlur.toDouble()
    )

    cfg.put(
        "opacity",
        uiOpacity.toDouble()
    )

    cfg.put(
        "backdrop",
        uiBackdrop.toDouble()
    )

    cfg.put(
        "scale",
        uiScale.toDouble()
    )

    cfg.put(
        "text_scale",
        uiTextScale.toDouble()
    )

    cfg.put(
        "radius",
        uiRadius.toDouble()
    )

    cfg.put(
        "animation",
        uiAnimation
    )

    cfg.put(
        "animation_speed",
        uiAnimationSpeed.toDouble()
    )


    cfg.put(
        "window",
        uiWindowColor
    )

    cfg.put(
        "panel",
        uiPanelColor
    )

    cfg.put(
        "accent",
        uiAccentColor
    )

    cfg.put(
        "client_accent",
        uiUseClientAccent
    )


    cfg.put(
        "strip",
        stripEnabled
    )

    cfg.put(
        "strip_position",
        stripPosition
    )

    cfg.put(
        "strip_thickness",
        stripThickness.toDouble()
    )

    cfg.put(
        "strip_inset",
        stripInset.toDouble()
    )

    cfg.put(
        "strip_length",
        stripLength.toDouble()
    )


    cfg.save()


    selectedThemeProfile =
        name


    statusText =
        "THEME SAVED"
}


// =====================================================
// THEME LOAD
// =====================================================

fun loadThemeProfile(
    name: String
) {

    if (
        name.isBlank() ||
        !configs.exists(
            name
        )
    ) {
        return
    }


    val cfg =
        config(
            name
        )


    uiBlur =
        cfg.getDouble(
            "blur",
            uiBlur.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    uiPanelBlur =
        cfg.getDouble(
            "panel_blur",
            uiPanelBlur.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                30f
            )


    uiOpacity =
        cfg.getDouble(
            "opacity",
            uiOpacity.toDouble()
        ).toFloat()
            .coerceIn(
                70f,
                255f
            )


    uiBackdrop =
        cfg.getDouble(
            "backdrop",
            uiBackdrop.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                220f
            )


    uiScale =
        cfg.getDouble(
            "scale",
            uiScale.toDouble()
        ).toFloat()
            .coerceIn(
                0.70f,
                1.30f
            )


    uiTextScale =
        cfg.getDouble(
            "text_scale",
            uiTextScale.toDouble()
        ).toFloat()
            .coerceIn(
                0.80f,
                1.60f
            )


    uiRadius =
        cfg.getDouble(
            "radius",
            uiRadius.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                28f
            )


    uiAnimation =
        cfg.getInt(
            "animation",
            uiAnimation
        ).coerceIn(
            0,
            2
        )


    uiAnimationSpeed =
        cfg.getDouble(
            "animation_speed",
            uiAnimationSpeed.toDouble()
        ).toFloat()
            .coerceIn(
                60f,
                400f
            )


    uiWindowColor =
        cfg.getInt(
            "window",
            uiWindowColor
        )


    uiPanelColor =
        cfg.getInt(
            "panel",
            uiPanelColor
        )


    uiAccentColor =
        cfg.getInt(
            "accent",
            uiAccentColor
        )


    uiUseClientAccent =
        cfg.getBoolean(
            "client_accent",
            uiUseClientAccent
        )


    stripEnabled =
        cfg.getBoolean(
            "strip",
            stripEnabled
        )


    stripPosition =
        cfg.getInt(
            "strip_position",
            stripPosition
        ).coerceIn(
            0,
            3
        )


    stripThickness =
        cfg.getDouble(
            "strip_thickness",
            stripThickness.toDouble()
        ).toFloat()
            .coerceIn(
                1f,
                12f
            )


    stripInset =
        cfg.getDouble(
            "strip_inset",
            stripInset.toDouble()
        ).toFloat()
            .coerceIn(
                0f,
                24f
            )


    stripLength =
        cfg.getDouble(
            "strip_length",
            stripLength.toDouble()
        ).toFloat()
            .coerceIn(
                0.20f,
                1f
            )


    selectedThemeProfile =
        name


    saveStorage()


    statusText =
        "THEME LOADED"
}


// =====================================================
// DELETE PROFILES
// =====================================================

fun deleteEspProfile(
    name: String
) {

    if (
        name.isBlank() ||
        !configs.exists(
            name
        )
    ) {
        return
    }


    configs.delete(
        name
    )


    if (
        selectedEspProfile ==
        name
    ) {

        selectedEspProfile =
            ""
    }


    statusText =
        "ESP PROFILE DELETED"
}


fun deleteThemeProfile(
    name: String
) {

    if (
        name.isBlank() ||
        !configs.exists(
            name
        )
    ) {
        return
    }


    configs.delete(
        name
    )


    if (
        selectedThemeProfile ==
        name
    ) {

        selectedThemeProfile =
            ""
    }


    statusText =
        "THEME DELETED"
}


// =====================================================
// ESP PRESETS
// =====================================================

fun applyPreset(
    index: Int
) {

    when (
        index
    ) {

        // CLEAN
        0 -> {

            boxMode = 0

            boxFill = false
            boxGlow = false

            tracerEnabled = false

            boxThickness = 1.4f
            boxPadding = 2f
            boxRadius = 2f

            showNameplate = true
            showHead = false

            showHealthText = true
            showDistance = true
            showPing = true

            nameBlur = 6f
            nameOpacity = 190f

            enemyColor =
                0xFFE6EAF2L.toInt()
        }


        // GLASS
        1 -> {

            boxMode = 1

            boxFill = true
            boxGlow = true

            tracerEnabled = false

            boxThickness = 1.7f
            cornerLength = 0.25f

            boxFillAlpha = 18f
            glowStrength = 0.42f

            showNameplate = true
            showHead = true

            nameBlur = 14f
            nameRadius = 8f
            nameOpacity = 175f

            enemyColor =
                0xFF72C7FFL.toInt()
        }


        // NEON
        else -> {

            boxMode = 1

            boxFill = true
            boxGlow = true

            boxThickness = 2.2f
            cornerLength = 0.30f

            boxFillAlpha = 12f
            glowStrength = 0.90f

            showNameplate = true
            showHead = true

            nameBlur = 16f
            nameOpacity = 205f

            enemyColor =
                0xFF53F5FFL.toInt()

            healthGoodColor =
                0xFF65FF9AL.toInt()

            healthBadColor =
                0xFFFF4D83L.toInt()
        }
    }


    saveStorage()


    statusText =
        "PRESET APPLIED"
}


// =====================================================
// MENU OPEN / CLOSE
// =====================================================

fun openMenu() {

    if (
        !inGame ||
        game.screenOpen()
    ) {
        return
    }


    menuOpen =
        true


    focusedField =
        FIELD_NONE

    numericEditId =
        -1

    activeColor =
        COLOR_NONE


    draggingSlider =
        -1

    windowDragging =
        false


    keys.unlockCursor()
}


fun closeMenu() {

    if (
        numericEditId >=
        0
    ) {

        commitNumericEdit()
    }


    menuOpen =
        false


    focusedField =
        FIELD_NONE

    activeColor =
        COLOR_NONE

    draggingSlider =
        -1

    windowDragging =
        false


    saveStorage()


    if (
        inGame &&
        !game.screenOpen()
    ) {

        keys.lockCursor()
    }
}


// =====================================================
// CLICK HANDLER
// =====================================================

fun handleClick(
    x: Float,
    y: Float,
    button: Int
) {

    val s =
        uiScale


    // =================================================
    // COMMIT OLD NUMBER
    // =================================================

    if (
        numericEditId >=
        0
    ) {

        val oldId =
            numericEditId


        if (
            !inside(
                x,
                y,
                numericBoxX(
                    oldId
                ),
                numericBoxY(
                    oldId
                ),
                numericBoxW(),
                numericBoxH()
            )
        ) {

            commitNumericEdit()
        }
    }


    // =================================================
    // COLOR PICKER
    // =================================================

    if (
        activeColor !=
        COLOR_NONE
    ) {

        if (
            inside(
                x,
                y,
                pickerX(),
                pickerY(),
                pickerW(),
                pickerH()
            )
        ) {

            draggingColorSV =
                true


            updatePickerSV()


            return
        }


        if (
            inside(
                x,
                y,
                pickerX(),
                pickerHueY(),
                pickerW(),
                14f * s
            )
        ) {

            draggingColorHue =
                true


            updatePickerHue()


            return
        }


        if (
            inside(
                x,
                y,
                pickerX() +
                pickerW() -
                70f * s,
                pickerHueY() +
                30f * s,
                70f * s,
                28f * s
            )
        ) {

            activeColor =
                COLOR_NONE


            saveStorage()


            return
        }
    }


    // =================================================
    // HEADER DRAG
    // =================================================

    if (
        inside(
            x,
            y,
            menuX,
            menuY,
            menuW,
            50f * s
        )
    ) {

        windowDragging =
            true


        dragOffsetX =
            x -
            menuX


        dragOffsetY =
            y -
            menuY


        return
    }


    // =================================================
    // TABS
    // =================================================

    val tabX =
        menuX +
        30f * s


    val tabY =
        menuY +
        68f * s


    val tabW =
        160f * s


    for (
        i in
        0 until 5
    ) {

        val xx =
            tabX +
            i *
            (
                tabW +
                8f * s
            )


        if (
            inside(
                x,
                y,
                xx,
                tabY,
                tabW,
                32f * s
            )
        ) {

            menuTab =
                i


            focusedField =
                FIELD_NONE


            cancelNumericEdit()


            activeColor =
                COLOR_NONE


            return
        }
    }


    // =================================================
    // ESP TAB
    // =================================================

    if (
        menuTab ==
        TAB_ESP
    ) {

        val left =
            menuX +
            40f * s


        val right =
            menuX +
            285f * s


        val y0 =
            menuY +
            125f * s


        val cardW =
            220f * s


        val cardH =
            52f * s


        for (
            i in
            0 until 8
        ) {

            val col =
                i %
                2


            val row =
                i /
                2


            val xx =
                if (
                    col ==
                    0
                ) {
                    left
                } else {
                    right
                }


            val yy =
                y0 +
                row *
                62f * s


            if (
                inside(
                    x,
                    y,
                    xx,
                    yy,
                    cardW,
                    cardH
                )
            ) {

                when (
                    i
                ) {

                    0 ->
                        espEnabled =
                            !espEnabled

                    1 ->
                        boxMode =
                            (
                                boxMode +
                                1
                            ) %
                            4

                    2 ->
                        boxFill =
                            !boxFill

                    3 ->
                        boxGlow =
                            !boxGlow

                    4 ->
                        tracerEnabled =
                            !tracerEnabled

                    5 ->
                        throughWalls =
                            !throughWalls

                    6 ->
                        hurtFlash =
                            !hurtFlash

                    7 ->
                        distanceFade =
                            !distanceFade
                }


                saveStorage()


                return
            }
        }


        // Sliders + number fields.
        for (
            id in
            20..26
        ) {

            if (
                inside(
                    x,
                    y,
                    numericBoxX(id),
                    numericBoxY(id),
                    numericBoxW(),
                    numericBoxH()
                )
            ) {

                startNumericEdit(
                    id
                )


                return
            }
        }


        for (
            id in
            20..26
        ) {

            val yy =
                sliderTrackY(
                    id
                )


            if (
                inside(
                    x,
                    y,
                    sliderTrackX(id) -
                    5f * s,
                    yy -
                    10f * s,
                    sliderTrackW(id) +
                    10f * s,
                    22f * s
                )
            ) {

                beginSliderDrag(
                    id
                )


                return
            }
        }


        // Colors.
        val colorTargets =
            listOf(
                COLOR_ENEMY,
                COLOR_RELATION,
                COLOR_HEALTH_GOOD,
                COLOR_HEALTH_BAD
            )


        val colorY =
            menuY +
            410f * s


        for (
            i in
            colorTargets.indices
        ) {

            val xx =
                menuX +
                (
                    555f +
                    i *
                    78f
                ) *
                s


            if (
                inside(
                    x,
                    y,
                    xx,
                    colorY,
                    70f * s,
                    36f * s
                )
            ) {

                openPicker(
                    colorTargets[i]
                )


                return
            }
        }


        // Presets.
        val presetY =
            menuY +
            555f * s


        for (
            i in
            0 until 3
        ) {

            val xx =
                menuX +
                (
                    560f +
                    i *
                    100f
                ) *
                s


            if (
                inside(
                    x,
                    y,
                    xx,
                    presetY,
                    90f * s,
                    32f * s
                )
            ) {

                applyPreset(
                    i
                )


                return
            }
        }
    }


    // =================================================
    // NAME TAB
    // =================================================

    if (
        menuTab ==
        TAB_NAME
    ) {

        val left =
            menuX +
            40f * s


        val right =
            menuX +
            285f * s


        val y0 =
            menuY +
            125f * s


        val cardW =
            220f * s


        val cardH =
            48f * s


        for (
            i in
            0 until 9
        ) {

            val col =
                i %
                2


            val row =
                i /
                2


            val xx =
                if (
                    col ==
                    0
                ) {
                    left
                } else {
                    right
                }


            val yy =
                y0 +
                row *
                56f * s


            if (
                inside(
                    x,
                    y,
                    xx,
                    yy,
                    cardW,
                    cardH
                )
            ) {

                when (
                    i
                ) {

                    0 ->
                        showNameplate =
                            !showNameplate

                    1 ->
                        showHead =
                            !showHead

                    2 ->
                        showHealthText =
                            !showHealthText

                    3 ->
                        showDistance =
                            !showDistance

                    4 ->
                        showPing =
                            !showPing

                    5 ->
                        showArmor =
                            !showArmor

                    6 ->
                        showGameMode =
                            !showGameMode

                    7 ->
                        showAbsorption =
                            !showAbsorption

                    8 ->
                        showHealthBar =
                            !showHealthBar
                }


                saveStorage()


                return
            }
        }


        for (
            id in
            40..47
        ) {

            if (
                inside(
                    x,
                    y,
                    numericBoxX(id),
                    numericBoxY(id),
                    numericBoxW(),
                    numericBoxH()
                )
            ) {

                startNumericEdit(
                    id
                )


                return
            }
        }


        for (
            id in
            40..47
        ) {

            val yy =
                sliderTrackY(
                    id
                )


            if (
                inside(
                    x,
                    y,
                    sliderTrackX(id) -
                    5f * s,
                    yy -
                    10f * s,
                    sliderTrackW(id) +
                    10f * s,
                    22f * s
                )
            ) {

                beginSliderDrag(
                    id
                )


                return
            }
        }


        val colorY =
            menuY +
            415f * s


        if (
            inside(
                x,
                y,
                menuX +
                600f * s,
                colorY,
                105f * s,
                38f * s
            )
        ) {

            openPicker(
                COLOR_NAME_BG
            )


            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                715f * s,
                colorY,
                105f * s,
                38f * s
            )
        ) {

            openPicker(
                COLOR_NAME_TEXT
            )


            return
        }
    }


    // =================================================
    // FILTERS TAB
    // =================================================

    if (
        menuTab ==
        TAB_FILTERS
    ) {

        val left =
            menuX +
            55f * s


        val right =
            menuX +
            485f * s


        val y0 =
            menuY +
            135f * s


        val cardW =
            390f * s


        val cardH =
            62f * s


        for (
            i in
            0 until 7
        ) {

            val col =
                if (
                    i <
                    4
                ) {
                    0
                } else {
                    1
                }


            val row =
                if (
                    i <
                    4
                ) {
                    i
                } else {
                    i -
                    4
                }


            val xx =
                if (
                    col ==
                    0
                ) {
                    left
                } else {
                    right
                }


            val yy =
                y0 +
                row *
                75f * s


            if (
                inside(
                    x,
                    y,
                    xx,
                    yy,
                    cardW,
                    cardH
                )
            ) {

                when (
                    i
                ) {

                    0 ->
                        showFriends =
                            !showFriends

                    1 ->
                        showParty =
                            !showParty

                    2 ->
                        showAllies =
                            !showAllies

                    3 ->
                        showBots =
                            !showBots

                    4 ->
                        showInvisible =
                            !showInvisible

                    5 ->
                        showSpectators =
                            !showSpectators

                    6 ->
                        ignoreSameTeam =
                            !ignoreSameTeam
                }


                saveStorage()


                return
            }
        }


        if (
            inside(
                x,
                y,
                numericBoxX(60),
                numericBoxY(60),
                numericBoxW(),
                numericBoxH()
            )
        ) {

            startNumericEdit(
                60
            )


            return
        }


        val yy =
            sliderTrackY(
                60
            )


        if (
            inside(
                x,
                y,
                sliderTrackX(60) -
                5f * s,
                yy -
                10f * s,
                sliderTrackW(60) +
                10f * s,
                22f * s
            )
        ) {

            beginSliderDrag(
                60
            )


            return
        }
    }


    // =================================================
    // PROFILES TAB
    // =================================================

    if (
        menuTab ==
        TAB_PROFILES
    ) {

        val left =
            menuX +
            45f * s


        val right =
            menuX +
            490f * s


        val inputY =
            menuY +
            135f * s


        if (
            inside(
                x,
                y,
                left,
                inputY,
                390f * s,
                36f * s
            )
        ) {

            focusedField =
                FIELD_PROFILE


            return
        }


        if (
            inside(
                x,
                y,
                right,
                inputY,
                390f * s,
                36f * s
            )
        ) {

            focusedField =
                FIELD_THEME


            return
        }


        val saveY =
            menuY +
            183f * s


        if (
            inside(
                x,
                y,
                left,
                saveY,
                160f * s,
                32f * s
            )
        ) {

            saveEspProfile(
                profileNameText
            )


            return
        }


        if (
            inside(
                x,
                y,
                right,
                saveY,
                160f * s,
                32f * s
            )
        ) {

            saveThemeProfile(
                themeNameText
            )


            return
        }


        val espList =
            espProfiles()


        val themeList =
            themeProfiles()


        val listY =
            menuY +
            235f * s


        val rowH =
            42f * s


        for (
            row in
            0 until 7
        ) {

            val yy =
                listY +
                row *
                48f * s


            val espIndex =
                espProfileScroll +
                row


            if (
                espIndex <
                espList.size
            ) {

                val cfg =
                    espList[
                        espIndex
                    ]


                if (
                    inside(
                        x,
                        y,
                        left,
                        yy,
                        275f * s,
                        rowH
                    )
                ) {

                    selectedEspProfile =
                        cfg


                    return
                }


                if (
                    inside(
                        x,
                        y,
                        left +
                        285f * s,
                        yy +
                        5f * s,
                        95f * s,
                        32f * s
                    )
                ) {

                    selectedEspProfile =
                        cfg


                    loadEspProfile(
                        cfg
                    )


                    return
                }
            }


            val themeIndex =
                themeProfileScroll +
                row


            if (
                themeIndex <
                themeList.size
            ) {

                val cfg =
                    themeList[
                        themeIndex
                    ]


                if (
                    inside(
                        x,
                        y,
                        right,
                        yy,
                        275f * s,
                        rowH
                    )
                ) {

                    selectedThemeProfile =
                        cfg


                    return
                }


                if (
                    inside(
                        x,
                        y,
                        right +
                        285f * s,
                        yy +
                        5f * s,
                        95f * s,
                        32f * s
                    )
                ) {

                    selectedThemeProfile =
                        cfg


                    loadThemeProfile(
                        cfg
                    )


                    return
                }
            }
        }


        val bottomY =
            menuY +
            575f * s


        if (
            inside(
                x,
                y,
                left,
                bottomY,
                150f * s,
                32f * s
            )
        ) {

            deleteEspProfile(
                selectedEspProfile
            )


            return
        }


        if (
            inside(
                x,
                y,
                right,
                bottomY,
                150f * s,
                32f * s
            )
        ) {

            deleteThemeProfile(
                selectedThemeProfile
            )


            return
        }
    }


    // =================================================
    // STYLE TAB
    // =================================================

    if (
        menuTab ==
        TAB_STYLE
    ) {

        for (
            id in
            0..10
        ) {

            if (
                inside(
                    x,
                    y,
                    numericBoxX(id),
                    numericBoxY(id),
                    numericBoxW(),
                    numericBoxH()
                )
            ) {

                startNumericEdit(
                    id
                )


                return
            }
        }


        for (
            id in
            0..10
        ) {

            val yy =
                sliderTrackY(
                    id
                )


            if (
                inside(
                    x,
                    y,
                    sliderTrackX(id) -
                    5f * s,
                    yy -
                    10f * s,
                    sliderTrackW(id) +
                    10f * s,
                    22f * s
                )
            ) {

                beginSliderDrag(
                    id
                )


                return
            }
        }


        val colorY =
            menuY +
            125f * s


        val colors =
            listOf(
                COLOR_WINDOW,
                COLOR_PANEL,
                COLOR_ACCENT
            )


        for (
            i in
            colors.indices
        ) {

            val xx =
                menuX +
                (
                    545f +
                    i *
                    105f
                ) *
                s


            if (
                inside(
                    x,
                    y,
                    xx,
                    colorY,
                    95f * s,
                    38f * s
                )
            ) {

                openPicker(
                    colors[i]
                )


                return
            }
        }


        val bottomY =
            menuY +
            570f * s


        if (
            inside(
                x,
                y,
                menuX +
                45f * s,
                bottomY,
                112f * s,
                32f * s
            )
        ) {

            stripEnabled =
                !stripEnabled


            saveStorage()


            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                165f * s,
                bottomY,
                125f * s,
                32f * s
            )
        ) {

            stripPosition =
                (
                    stripPosition +
                    1
                ) %
                4


            saveStorage()


            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                298f * s,
                bottomY,
                150f * s,
                32f * s
            )
        ) {

            uiUseClientAccent =
                !uiUseClientAccent


            saveStorage()


            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                456f * s,
                bottomY,
                150f * s,
                32f * s
            )
        ) {

            uiAnimation =
                (
                    uiAnimation +
                    1
                ) %
                3


            saveStorage()


            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                614f * s,
                bottomY,
                145f * s,
                32f * s
            )
        ) {

            uiBlur = 16f
            uiPanelBlur = 10f

            uiOpacity = 220f
            uiBackdrop = 110f

            uiScale = 1f
            uiTextScale = 1.15f

            uiRadius = 14f

            uiAnimation = 1
            uiAnimationSpeed = 150f

            uiWindowColor =
                0xFF0D121BL.toInt()

            uiPanelColor =
                0xFF171F2CL.toInt()

            uiAccentColor =
                0xFF72C7FFL.toInt()

            uiUseClientAccent =
                false

            stripEnabled =
                true

            stripPosition =
                1

            stripThickness =
                4f

            stripInset =
                7f

            stripLength =
                0.80f


            cancelNumericEdit()


            activeColor =
                COLOR_NONE


            saveStorage()


            return
        }
    }
}


// =====================================================
// KEY INPUT
// =====================================================

on<KeyEvent> { e ->

    // =================================================
    // NUMERIC EDIT
    // =================================================

    if (
        menuOpen &&
        numericEditId >=
        0
    ) {

        if (
            (
                e.pressed() ||
                e.action() ==
                KeyAction.REPEAT
            ) &&
            e.key() ==
            Key.BACKSPACE
        ) {

            if (
                numericEditText.isNotEmpty()
            ) {

                numericEditText =
                    numericEditText.dropLast(
                        1
                    )
            }


            e.cancel()


            return@on
        }


        if (
            e.pressed() &&
            (
                e.key() ==
                Key.ENTER ||
                e.key() ==
                Key.KP_ENTER
            )
        ) {

            commitNumericEdit()


            e.cancel()


            return@on
        }


        if (
            e.pressed() &&
            e.key() ==
            Key.ESCAPE
        ) {

            cancelNumericEdit()


            e.cancel()


            return@on
        }
    }


    // =================================================
    // F9
    // =================================================

    if (
        e.pressed() &&
        e.key() ==
        managerKey.key()
    ) {

        if (
            !menuOpen &&
            keys.inputBlocked()
        ) {

            return@on
        }


        if (
            menuOpen
        ) {

            closeMenu()

        } else {

            openMenu()
        }


        e.cancel()


        return@on
    }


    if (
        !menuOpen
    ) {

        return@on
    }


    // =================================================
    // TEXT FIELD
    // =================================================

    if (
        focusedField !=
        FIELD_NONE
    ) {

        if (
            (
                e.pressed() ||
                e.action() ==
                KeyAction.REPEAT
            ) &&
            e.key() ==
            Key.BACKSPACE
        ) {

            if (
                focusedField ==
                FIELD_PROFILE &&
                profileNameText.isNotEmpty()
            ) {

                profileNameText =
                    profileNameText.dropLast(
                        1
                    )
            }


            if (
                focusedField ==
                FIELD_THEME &&
                themeNameText.isNotEmpty()
            ) {

                themeNameText =
                    themeNameText.dropLast(
                        1
                    )
            }


            e.cancel()


            return@on
        }


        if (
            e.pressed() &&
            (
                e.key() ==
                Key.ENTER ||
                e.key() ==
                Key.KP_ENTER
            )
        ) {

            focusedField =
                FIELD_NONE


            e.cancel()


            return@on
        }


        if (
            e.pressed() &&
            e.key() ==
            Key.ESCAPE
        ) {

            focusedField =
                FIELD_NONE


            e.cancel()


            return@on
        }
    }


    if (
        activeColor !=
        COLOR_NONE &&
        e.pressed() &&
        e.key() ==
        Key.ESCAPE
    ) {

        activeColor =
            COLOR_NONE


        e.cancel()


        return@on
    }


    if (
        e.pressed() &&
        e.key() ==
        Key.ESCAPE
    ) {

        closeMenu()


        e.cancel()


        return@on
    }


    if (
        e.pressed() &&
        e.key() ==
        Key.MOUSE_1
    ) {

        handleClick(
            keys.mouseX(),
            keys.mouseY(),
            0
        )


        e.cancel()


        return@on
    }


    if (
        e.pressed() &&
        e.key() ==
        Key.MOUSE_2
    ) {

        handleClick(
            keys.mouseX(),
            keys.mouseY(),
            1
        )


        e.cancel()


        return@on
    }


    e.cancel()
}


// =====================================================
// CHAR INPUT
// =====================================================

on<CharEvent> { e ->

    if (
        !menuOpen
    ) {

        return@on
    }


    // =================================================
    // NUMERIC
    // =================================================

    if (
        numericEditId >=
        0
    ) {

        var value =
            e.character()


        if (
            value ==
            ","
        ) {

            value =
                "."
        }


        if (
            value.length ==
            1
        ) {

            val c =
                value[0]


            val digit =
                c in
                '0'..'9'


            val dot =
                c ==
                '.' &&
                !numericEditText.contains(
                    "."
                )


            val minus =
                c ==
                '-' &&
                numericEditText.isEmpty() &&
                sliderMin(
                    numericEditId
                ) <
                0f


            if (
                digit ||
                dot ||
                minus
            ) {

                if (
                    numericEditText.length <
                    10
                ) {

                    numericEditText +=
                        c
                }
            }
        }


        e.cancel()


        return@on
    }


    // =================================================
    // PROFILE
    // =================================================

    if (
        focusedField ==
        FIELD_PROFILE &&
        profileNameText.length <
        24
    ) {

        profileNameText +=
            e.character()


        e.cancel()


        return@on
    }


    // =================================================
    // THEME
    // =================================================

    if (
        focusedField ==
        FIELD_THEME &&
        themeNameText.length <
        24
    ) {

        themeNameText +=
            e.character()


        e.cancel()
    }
}


// =====================================================
// BLOCK CAMERA / MOVEMENT IN MENU
// =====================================================

on<LookInputEvent> { e ->

    if (
        menuOpen
    ) {

        e.cancel()
    }
}


on<MoveInputEvent> { e ->

    if (
        !menuOpen
    ) {

        return@on
    }


    e.forward(false)
    e.backward(false)

    e.left(false)
    e.right(false)

    e.jump(false)
    e.sneak(false)
    e.sprint(false)
}


// =====================================================
// PROFILE SCROLL
// =====================================================

on<MouseScrollEvent> { e ->

    if (
        !menuOpen
    ) {

        return@on
    }


    if (
        menuTab ==
        TAB_PROFILES
    ) {

        if (
            keys.mouseX() <
            menuX +
            menuW /
            2f
        ) {

            espProfileScroll +=
                if (
                    e.vertical() <
                    0.0
                ) {
                    1
                } else {
                    -1
                }


            espProfileScroll =
                espProfileScroll.coerceIn(
                    0,
                    kotlin.math.max(
                        0,
                        espProfiles().size -
                        7
                    )
                )

        } else {

            themeProfileScroll +=
                if (
                    e.vertical() <
                    0.0
                ) {
                    1
                } else {
                    -1
                }


            themeProfileScroll =
                themeProfileScroll.coerceIn(
                    0,
                    kotlin.math.max(
                        0,
                        themeProfiles().size -
                        7
                    )
                )
        }
    }


    e.cancel()
}


// =====================================================
// 3D ESP
// =====================================================

on<Render3DEvent> { e ->

    if (
        !espEnabled ||
        !inGame
    ) {

        return@on
    }


    if (
        boxMode !=
        2 &&
        !tracerEnabled
    ) {

        return@on
    }


    val render =
        e.render()


    val ownTeam =
        player.team()


    for (
        target in
        world.players()
    ) {

        if (
            target.isSelf() ||
            !target.alive() ||
            target.dead()
        ) {

            continue
        }


        val distance =
            target.distanceTo(
                player
            )


        if (
            distance >
            maxDistance.toDouble()
        ) {

            continue
        }


        if (
            !showInvisible &&
            target.invisible()
        ) {

            continue
        }


        if (
            !showBots &&
            target.isBot()
        ) {

            continue
        }


        if (
            !showFriends &&
            target.isFriend()
        ) {

            continue
        }


        if (
            !showParty &&
            target.isParty()
        ) {

            continue
        }


        if (
            !showAllies &&
            target.isAlly()
        ) {

            continue
        }


        if (
            !showSpectators &&
            target.gameMode() ==
            GameMode.SPECTATOR
        ) {

            continue
        }


        if (
            ignoreSameTeam
        ) {

            val targetTeam =
                target.team()


            if (
                ownTeam !=
                null &&
                targetTeam !=
                null &&
                ownTeam ==
                targetTeam
            ) {

                continue
            }
        }


        val relation =
            target.isFriend() ||
            target.isParty() ||
            target.isAlly()


        var baseColor =
            if (
                relation
            ) {

                relationColor

            } else {

                enemyColor
            }


        if (
            hurtFlash &&
            target.hurtTicks() >
            0
        ) {

            val amount =
                (
                    target.hurtTicks()
                        .toFloat() /
                    10f *
                    0.65f
                ).coerceIn(
                    0f,
                    0.65f
                )


            baseColor =
                Colors.mix(
                    baseColor,
                    Colors.WHITE,
                    amount
                )
        }


        val distanceAlpha =
            if (
                distanceFade
            ) {

                (
                    1f -
                    (
                        distance.toFloat() /
                        maxDistance
                    ) *
                    0.65f
                ).coerceIn(
                    0.30f,
                    1f
                )

            } else {

                1f
            }


        if (
            boxMode ==
            2
        ) {

            val position =
                target.renderPosition()


            val half =
                target.width()
                    .toDouble() /
                2.0


            val targetBox =
                Box(
                    position.x() -
                    half,
                    position.y(),
                    position.z() -
                    half,
                    position.x() +
                    half,
                    position.y() +
                    target.height()
                        .toDouble(),
                    position.z() +
                    half
                )


            if (
                boxFill &&
                boxFillAlpha >
                0f
            ) {

                render.filledBox(
                    targetBox,
                    Colors.fade(
                        Colors.withAlpha(
                            baseColor,
                            boxFillAlpha.toInt()
                        ),
                        distanceAlpha
                    ),
                    throughWalls
                )
            }


            render.entityBox(
                target,
                Colors.fade(
                    Colors.withAlpha(
                        baseColor,
                        240
                    ),
                    distanceAlpha
                ),
                throughWalls
            )
        }


        if (
            tracerEnabled
        ) {

            render.tracer(
                target.renderPosition()
                    .add(
                        0.0,
                        target.height()
                            .toDouble() /
                        2.0,
                        0.0
                    ),
                Colors.fade(
                    Colors.withAlpha(
                        baseColor,
                        tracerAlpha.toInt()
                    ),
                    distanceAlpha
                ),
                throughWalls
            )
        }
    }
}


// =====================================================
// 2D ESP + MENU
// =====================================================

on<Render2DEvent> { e ->

    val r =
        e.render()


    val screenW =
        r.width()


    val screenH =
        r.height()


    // =================================================
    // PLAYER ESP
    // =================================================

    if (
        espEnabled &&
        inGame
    ) {

        val ownTeam =
            player.team()


        val targets =
            world.players()
                .filter { target ->

                    if (
                        target.isSelf() ||
                        !target.alive() ||
                        target.dead()
                    ) {

                        false

                    } else {

                        val distance =
                            target.distanceTo(
                                player
                            )


                        if (
                            distance >
                            maxDistance.toDouble()
                        ) {

                            false

                        } else if (
                            !showInvisible &&
                            target.invisible()
                        ) {

                            false

                        } else if (
                            !showBots &&
                            target.isBot()
                        ) {

                            false

                        } else if (
                            !showFriends &&
                            target.isFriend()
                        ) {

                            false

                        } else if (
                            !showParty &&
                            target.isParty()
                        ) {

                            false

                        } else if (
                            !showAllies &&
                            target.isAlly()
                        ) {

                            false

                        } else if (
                            !showSpectators &&
                            target.gameMode() ==
                            GameMode.SPECTATOR
                        ) {

                            false

                        } else if (
                            ignoreSameTeam &&
                            ownTeam !=
                            null &&
                            target.team() !=
                            null &&
                            ownTeam ==
                            target.team()
                        ) {

                            false

                        } else {

                            true
                        }
                    }
                }
                .sortedByDescending {
                    it.distanceTo(
                        player
                    )
                }


        fun drawCornerBox(
            x: Float,
            y: Float,
            w: Float,
            h: Float,
            thickness: Float,
            fraction: Float,
            color: Int
        ) {

            val horizontal =
                kotlin.math.max(
                    2f,
                    w *
                    fraction
                )


            val vertical =
                kotlin.math.max(
                    2f,
                    h *
                    fraction
                )


            // top left
            r.rect(
                x,
                y,
                horizontal,
                thickness,
                color
            )

            r.rect(
                x,
                y,
                thickness,
                vertical,
                color
            )


            // top right
            r.rect(
                x +
                w -
                horizontal,
                y,
                horizontal,
                thickness,
                color
            )

            r.rect(
                x +
                w -
                thickness,
                y,
                thickness,
                vertical,
                color
            )


            // bottom left
            r.rect(
                x,
                y +
                h -
                thickness,
                horizontal,
                thickness,
                color
            )

            r.rect(
                x,
                y +
                h -
                vertical,
                thickness,
                vertical,
                color
            )


            // bottom right
            r.rect(
                x +
                w -
                horizontal,
                y +
                h -
                thickness,
                horizontal,
                thickness,
                color
            )

            r.rect(
                x +
                w -
                thickness,
                y +
                h -
                vertical,
                thickness,
                vertical,
                color
            )
        }


        for (
            target in
            targets
        ) {

            val distance =
                target.distanceTo(
                    player
                )


            val relation =
                target.isFriend() ||
                target.isParty() ||
                target.isAlly()


            var targetColor =
                if (
                    relation
                ) {

                    relationColor

                } else {

                    enemyColor
                }


            if (
                hurtFlash &&
                target.hurtTicks() >
                0
            ) {

                val amount =
                    (
                        target.hurtTicks()
                            .toFloat() /
                        10f *
                        0.65f
                    ).coerceIn(
                        0f,
                        0.65f
                    )


                targetColor =
                    Colors.mix(
                        targetColor,
                        Colors.WHITE,
                        amount
                    )
            }


            val alphaFactor =
                if (
                    distanceFade
                ) {

                    (
                        1f -
                        (
                            distance.toFloat() /
                            maxDistance
                        ) *
                        0.65f
                    ).coerceIn(
                        0.30f,
                        1f
                    )

                } else {

                    1f
                }


            // =================================================
            // PROJECT PLAYER BOX
            // =================================================

            val position =
                target.renderPosition()


            val halfWidth =
                target.width()
                    .toDouble() /
                2.0


            val minX =
                position.x() -
                halfWidth


            val maxX =
                position.x() +
                halfWidth


            val minY =
                position.y()


            val maxY =
                position.y() +
                target.height()
                    .toDouble()


            val minZ =
                position.z() -
                halfWidth


            val maxZ =
                position.z() +
                halfWidth


            val corners =
                listOf(
                    Vec.of(
                        minX,
                        minY,
                        minZ
                    ),
                    Vec.of(
                        maxX,
                        minY,
                        minZ
                    ),
                    Vec.of(
                        minX,
                        minY,
                        maxZ
                    ),
                    Vec.of(
                        maxX,
                        minY,
                        maxZ
                    ),
                    Vec.of(
                        minX,
                        maxY,
                        minZ
                    ),
                    Vec.of(
                        maxX,
                        maxY,
                        minZ
                    ),
                    Vec.of(
                        minX,
                        maxY,
                        maxZ
                    ),
                    Vec.of(
                        maxX,
                        maxY,
                        maxZ
                    )
                )


            var projectedMinX =
                Float.POSITIVE_INFINITY


            var projectedMinY =
                Float.POSITIVE_INFINITY


            var projectedMaxX =
                Float.NEGATIVE_INFINITY


            var projectedMaxY =
                Float.NEGATIVE_INFINITY


            var projectionValid =
                true


            for (
                corner in
                corners
            ) {

                val projection =
                    r.project(
                        corner
                    )


                if (
                    !projection.visible()
                ) {

                    projectionValid =
                        false


                    break
                }


                projectedMinX =
                    kotlin.math.min(
                        projectedMinX,
                        projection.x()
                    )


                projectedMinY =
                    kotlin.math.min(
                        projectedMinY,
                        projection.y()
                    )


                projectedMaxX =
                    kotlin.math.max(
                        projectedMaxX,
                        projection.x()
                    )


                projectedMaxY =
                    kotlin.math.max(
                        projectedMaxY,
                        projection.y()
                    )
            }


            if (
                !projectionValid
            ) {

                continue
            }


            var bx =
                projectedMinX -
                boxPadding


            var by =
                projectedMinY -
                boxPadding


            var bw =
                (
                    projectedMaxX -
                    projectedMinX
                ) +
                boxPadding *
                2f


            var bh =
                (
                    projectedMaxY -
                    projectedMinY
                ) +
                boxPadding *
                2f


            if (
                bw <
                3f ||
                bh <
                5f
            ) {

                continue
            }


            val drawColor =
                Colors.fade(
                    Colors.withAlpha(
                        targetColor,
                        245
                    ),
                    alphaFactor
                )


            // =================================================
            // 2D BOX
            // =================================================

            if (
                boxMode ==
                0 ||
                boxMode ==
                1
            ) {

                if (
                    boxFill &&
                    boxFillAlpha >
                    0f
                ) {

                    r.roundedRect(
                        bx,
                        by,
                        bw,
                        bh,
                        boxRadius,
                        Colors.fade(
                            Colors.withAlpha(
                                targetColor,
                                boxFillAlpha.toInt()
                            ),
                            alphaFactor
                        )
                    )
                }


                if (
                    boxGlow &&
                    glowStrength >
                    0f
                ) {

                    val glowAlpha =
                        (
                            70f *
                            glowStrength
                        ).toInt()


                    if (
                        boxMode ==
                        0
                    ) {

                        r.roundedOutline(
                            bx -
                            2f,
                            by -
                            2f,
                            bw +
                            4f,
                            bh +
                            4f,
                            boxRadius +
                            2f,
                            boxThickness +
                            4f *
                            glowStrength,
                            Colors.fade(
                                Colors.withAlpha(
                                    targetColor,
                                    glowAlpha
                                ),
                                alphaFactor
                            )
                        )


                        r.roundedOutline(
                            bx -
                            1f,
                            by -
                            1f,
                            bw +
                            2f,
                            bh +
                            2f,
                            boxRadius +
                            1f,
                            boxThickness +
                            2f *
                            glowStrength,
                            Colors.fade(
                                Colors.withAlpha(
                                    targetColor,
                                    kotlin.math.min(
                                        120,
                                        glowAlpha +
                                        30
                                    )
                                ),
                                alphaFactor
                            )
                        )

                    } else {

                        drawCornerBox(
                            bx -
                            1f,
                            by -
                            1f,
                            bw +
                            2f,
                            bh +
                            2f,
                            boxThickness +
                            2f *
                            glowStrength,
                            cornerLength,
                            Colors.fade(
                                Colors.withAlpha(
                                    targetColor,
                                    glowAlpha
                                ),
                                alphaFactor
                            )
                        )
                    }
                }


                if (
                    boxMode ==
                    0
                ) {

                    r.roundedOutline(
                        bx,
                        by,
                        bw,
                        bh,
                        boxRadius,
                        boxThickness,
                        drawColor
                    )

                } else {

                    drawCornerBox(
                        bx,
                        by,
                        bw,
                        bh,
                        boxThickness,
                        cornerLength,
                        drawColor
                    )
                }
            }


            // =================================================
            // HEALTH BAR
            // =================================================

            val rawHealth =
                target.bypassedHealth()


            val maxHealth =
                kotlin.math.max(
                    1f,
                    target.maxHealth()
                )


            val healthProgress =
                (
                    rawHealth /
                    maxHealth
                ).coerceIn(
                    0f,
                    1f
                )


            val healthColor =
                Colors.mix(
                    healthBadColor,
                    healthGoodColor,
                    healthProgress
                )


            if (
                showHealthBar
            ) {

                val barGap =
                    5f


                val barX =
                    bx -
                    healthBarWidth -
                    barGap


                val barY =
                    by


                val barH =
                    bh


                r.roundedRect(
                    barX,
                    barY,
                    healthBarWidth,
                    barH,
                    healthBarWidth /
                    2f,
                    Colors.rgba(
                        0,
                        0,
                        0,
                        140
                    )
                )


                val fillH =
                    barH *
                    healthProgress


                if (
                    fillH >
                    0.5f
                ) {

                    r.roundedRect(
                        barX,
                        barY +
                        barH -
                        fillH,
                        healthBarWidth,
                        fillH,
                        healthBarWidth /
                        2f,
                        Colors.fade(
                            healthColor,
                            alphaFactor
                        )
                    )
                }
            }


            // =================================================
            // NAMEPLATE
            // =================================================

            if (
                showNameplate
            ) {

                val primaryText =
                    target.name()


                val secondary =
                    mutableListOf<String>()


                if (
                    showHealthText
                ) {

                    secondary.add(
                        "${cleanNumber(rawHealth)} HP"
                    )
                }


                if (
                    showAbsorption &&
                    target.absorption() >
                    0f
                ) {

                    secondary.add(
                        "+${cleanNumber(target.absorption())}"
                    )
                }


                if (
                    showDistance
                ) {

                    secondary.add(
                        "${cleanNumber(distance.toFloat())}m"
                    )
                }


                if (
                    showPing
                ) {

                    val ping =
                        target.pingMs()


                    secondary.add(
                        if (
                            ping >
                            0
                        ) {
                            "${ping}ms"
                        } else {
                            "—ms"
                        }
                    )
                }


                if (
                    showGameMode
                ) {

                    val mode =
                        when (
                            target.gameMode()
                        ) {

                            GameMode.CREATIVE ->
                                "C"

                            GameMode.ADVENTURE ->
                                "A"

                            GameMode.SPECTATOR ->
                                "SP"

                            else ->
                                "S"
                        }


                    secondary.add(
                        mode
                    )
                }


                secondary.add(
                    "ARM ${target.armorPoints()}"
                )


                val secondaryText =
                    secondary.joinToString(
                        "  •  "
                    )


                val primarySize =
                    nameSize


                val secondarySize =
                    kotlin.math.max(
                        5.5f,
                        nameSize *
                        0.67f
                    )


                val primaryW =
                    r.textWidth(
                        primaryText,
                        primarySize,
                        Weight.SEMI_BOLD
                    )


                val primaryH =
                    r.textHeight(
                        primarySize,
                        Weight.SEMI_BOLD
                    )


                val secondaryW =
                    if (
                        secondaryText.isBlank()
                    ) {
                        0f
                    } else {
                        r.textWidth(
                            secondaryText,
                            secondarySize
                        )
                    }


                val secondaryH =
                    if (
                        secondaryText.isBlank()
                    ) {
                        0f
                    } else {
                        r.textHeight(
                            secondarySize
                        )
                    }


                val gap =
                    if (
                        secondaryText.isBlank()
                    ) {
                        0f
                    } else {
                        2f
                    }


                val textBlockH =
                    primaryH +
                    gap +
                    secondaryH


                val headSize =
                    if (
                        showHead
                    ) {

                        kotlin.math.max(
                            20f,
                            textBlockH
                        )

                    } else {

                        0f
                    }


                val headGap =
                    if (
                        showHead
                    ) {
                        5f
                    } else {
                        0f
                    }


                val textWidth =
                    kotlin.math.max(
                        primaryW,
                        secondaryW
                    )


                val plateW =
                    namePadding *
                    2f +
                    headSize +
                    headGap +
                    textWidth


                val plateH =
                    namePadding *
                    2f +
                    kotlin.math.max(
                        textBlockH,
                        headSize
                    )


                val plateX =
                    bx +
                    bw /
                    2f -
                    plateW /
                    2f


                val plateY =
                    by -
                    nameOffset -
                    plateH


                if (
                    nameBlur >
                    0f
                ) {

                    r.blur(
                        plateX,
                        plateY,
                        plateW,
                        plateH,
                        nameBlur,
                        Colors.withAlpha(
                            Colors.BLACK,
                            nameOpacity.toInt()
                        ),
                        nameRadius
                    )
                }


                r.roundedRect(
                    plateX,
                    plateY,
                    plateW,
                    plateH,
                    nameRadius,
                    Colors.fade(
                        Colors.withAlpha(
                            nameBackgroundColor,
                            nameOpacity.toInt()
                        ),
                        alphaFactor
                    )
                )


                r.roundedOutline(
                    plateX,
                    plateY,
                    plateW,
                    plateH,
                    nameRadius,
                    1f,
                    Colors.fade(
                        Colors.withAlpha(
                            targetColor,
                            80
                        ),
                        alphaFactor
                    )
                )


                var contentX =
                    plateX +
                    namePadding


                if (
                    showHead
                ) {

                    r.head(
                        target,
                        contentX,
                        plateY +
                        (
                            plateH -
                            headSize
                        ) /
                        2f,
                        headSize
                    )


                    contentX +=
                        headSize +
                        headGap
                }


                val textY =
                    plateY +
                    (
                        plateH -
                        textBlockH
                    ) /
                    2f


                r.text(
                    primaryText,
                    contentX,
                    textY,
                    primarySize,
                    Colors.fade(
                        nameTextColor,
                        alphaFactor
                    ),
                    Weight.SEMI_BOLD
                )


                if (
                    secondaryText.isNotBlank()
                ) {

                    r.text(
                        secondaryText,
                        contentX,
                        textY +
                        primaryH +
                        gap,
                        secondarySize,
                        Colors.fade(
                            Colors.withAlpha(
                                nameTextColor,
                                170
                            ),
                            alphaFactor
                        )
                    )
                }
            }


            // =================================================
            // ARMOR / ITEMS
            // =================================================

            if (
                showArmor
            ) {

                val equipment =
                    listOf(
                        target.armorItem(
                            ArmorSlot.HELMET
                        ),
                        target.armorItem(
                            ArmorSlot.CHESTPLATE
                        ),
                        target.armorItem(
                            ArmorSlot.LEGGINGS
                        ),
                        target.armorItem(
                            ArmorSlot.BOOTS
                        ),
                        target.mainHandItem(),
                        target.offHandItem()
                    )
                        .filter {
                            !it.empty()
                        }


                if (
                    equipment.isNotEmpty()
                ) {

                    val gap =
                        2f


                    val count =
                        equipment.size


                    val rowW =
                        count *
                        armorIconSize +
                        (
                            count -
                            1
                        ) *
                        gap


                    val backgroundPad =
                        4f


                    val rowX =
                        bx +
                        bw /
                        2f -
                        rowW /
                        2f


                    val rowY =
                        by +
                        bh +
                        7f


                    r.roundedRect(
                        rowX -
                        backgroundPad,
                        rowY -
                        backgroundPad,
                        rowW +
                        backgroundPad *
                        2f,
                        armorIconSize +
                        backgroundPad *
                        2f,
                        6f,
                        Colors.rgba(
                            7,
                            10,
                            15,
                            150
                        )
                    )


                    for (
                        i in
                        equipment.indices
                    ) {

                        r.item(
                            equipment[i],
                            rowX +
                            i *
                            (
                                armorIconSize +
                                gap
                            ),
                            rowY,
                            armorIconSize
                        )
                    }
                }
            }
        }
    }


    // =================================================
    // MENU ANIMATION
    // =================================================

    val now =
        client.millis()


    val delta =
        if (
            lastRenderMs ==
            0L
        ) {

            16f

        } else {

            (
                now -
                lastRenderMs
            ).coerceIn(
                0L,
                100L
            ).toFloat()
        }


    lastRenderMs =
        now


    if (
        menuOpen
    ) {

        menuAlpha +=
            delta /
            uiAnimationSpeed

    } else {

        menuAlpha -=
            delta /
            uiAnimationSpeed
    }


    menuAlpha =
        menuAlpha.coerceIn(
            0f,
            1f
        )


    if (
        menuAlpha <=
        0.001f
    ) {

        return@on
    }


    val s =
        uiScale


    val ts =
        uiTextScale


    menuW =
        940f *
        s


    menuH =
        650f *
        s


    var targetX =
        (
            menuCenterX *
            screenW.toDouble()
        ).toFloat() -
        menuW /
        2f


    var targetY =
        (
            menuCenterY *
            screenH.toDouble()
        ).toFloat() -
        menuH /
        2f


    targetX =
        targetX.coerceIn(
            4f,
            kotlin.math.max(
                4f,
                screenW -
                menuW -
                4f
            )
        )


    targetY =
        targetY.coerceIn(
            4f,
            kotlin.math.max(
                4f,
                screenH -
                menuH -
                4f
            )
        )


    // =================================================
    // DRAG WINDOW
    // =================================================

    if (
        windowDragging
    ) {

        if (
            keys.mouseDown(
                0
            )
        ) {

            targetX =
                (
                    keys.mouseX() -
                    dragOffsetX
                ).coerceIn(
                    4f,
                    kotlin.math.max(
                        4f,
                        screenW -
                        menuW -
                        4f
                    )
                )


            targetY =
                (
                    keys.mouseY() -
                    dragOffsetY
                ).coerceIn(
                    4f,
                    kotlin.math.max(
                        4f,
                        screenH -
                        menuH -
                        4f
                    )
                )


            menuCenterX =
                (
                    (
                        targetX +
                        menuW /
                        2f
                    ) /
                    screenW
                ).toDouble()
                    .coerceIn(
                        0.0,
                        1.0
                    )


            menuCenterY =
                (
                    (
                        targetY +
                        menuH /
                        2f
                    ) /
                    screenH
                ).toDouble()
                    .coerceIn(
                        0.0,
                        1.0
                    )

        } else {

            windowDragging =
                false


            saveStorage()
        }
    }


    // =================================================
    // SLIDER DRAG
    // =================================================

    if (
        draggingSlider >=
        0
    ) {

        if (
            keys.mouseDown(
                0
            )
        ) {

            updateDraggedSlider()

        } else {

            draggingSlider =
                -1


            saveStorage()
        }
    }


    // =================================================
    // PICKER DRAG
    // =================================================

    if (
        draggingColorSV
    ) {

        if (
            keys.mouseDown(
                0
            )
        ) {

            updatePickerSV()

        } else {

            draggingColorSV =
                false


            saveStorage()
        }
    }


    if (
        draggingColorHue
    ) {

        if (
            keys.mouseDown(
                0
            )
        ) {

            updatePickerHue()

        } else {

            draggingColorHue =
                false


            saveStorage()
        }
    }


    // =================================================
    // WINDOW POSITION
    // =================================================

    val slideOffset =
        (
            1f -
            menuAlpha
        ) *
        20f *
        s


    menuX =
        targetX


    menuY =
        when (
            uiAnimation
        ) {

            1 ->
                targetY -
                slideOffset

            2 ->
                targetY +
                slideOffset

            else ->
                targetY
        }


    val alpha =
        menuAlpha


    val accentColor =
        Colors.fade(
            accent(),
            alpha
        )


    // =================================================
    // BACKDROP
    // =================================================

    if (
        uiBlur >
        0f
    ) {

        r.blur(
            0f,
            0f,
            screenW,
            screenH,
            uiBlur
        )
    }


    r.rect(
        0f,
        0f,
        screenW,
        screenH,
        Colors.fade(
            Colors.rgba(
                0,
                0,
                0,
                uiBackdrop.toInt()
            ),
            alpha
        )
    )


    // =================================================
    // WINDOW
    // =================================================

    if (
        uiPanelBlur >
        0f
    ) {

        r.blur(
            menuX,
            menuY,
            menuW,
            menuH,
            uiPanelBlur,
            Colors.withAlpha(
                Colors.BLACK,
                130
            ),
            uiRadius *
            s
        )
    }


    r.roundedRect(
        menuX,
        menuY,
        menuW,
        menuH,
        uiRadius *
        s,
        Colors.fade(
            Colors.withAlpha(
                uiWindowColor,
                uiOpacity.toInt()
            ),
            alpha
        )
    )


    r.roundedOutline(
        menuX,
        menuY,
        menuW,
        menuH,
        uiRadius *
        s,
        1f *
        s,
        Colors.fade(
            Colors.withAlpha(
                accent(),
                48
            ),
            alpha
        )
    )


    // =================================================
    // INTERNAL ACCENT STRIP
    // =================================================

    if (
        stripEnabled
    ) {

        val thickness =
            stripThickness *
            s


        val inset =
            kotlin.math.max(
                stripInset *
                s,
                uiRadius *
                s *
                0.55f
            )


        if (
            stripPosition ==
            0 ||
            stripPosition ==
            2
        ) {

            val available =
                kotlin.math.max(
                    1f,
                    menuH -
                    inset *
                    2f
                )


            val length =
                available *
                stripLength


            val yy =
                menuY +
                (
                    menuH -
                    length
                ) /
                2f


            val xx =
                if (
                    stripPosition ==
                    0
                ) {

                    menuX +
                    stripInset *
                    s

                } else {

                    menuX +
                    menuW -
                    stripInset *
                    s -
                    thickness
                }


            r.roundedRect(
                xx,
                yy,
                thickness,
                length,
                thickness /
                2f,
                accentColor
            )

        } else {

            val available =
                kotlin.math.max(
                    1f,
                    menuW -
                    inset *
                    2f
                )


            val length =
                available *
                stripLength


            val xx =
                menuX +
                (
                    menuW -
                    length
                ) /
                2f


            val yy =
                if (
                    stripPosition ==
                    1
                ) {

                    menuY +
                    stripInset *
                    s

                } else {

                    menuY +
                    menuH -
                    stripInset *
                    s -
                    thickness
                }


            r.roundedRect(
                xx,
                yy,
                length,
                thickness,
                thickness /
                2f,
                accentColor
            )
        }
    }


    // =================================================
    // HEADER
    // =================================================

    r.text(
        "Custom ESP",
        menuX +
        30f * s,
        menuY +
        17f * s,
        17f *
        s *
        ts,
        Colors.fade(
            theme.textPrimary(),
            alpha
        ),
        Weight.BOLD
    )


    r.text(
        "Player ESP  •  2D / Corners / 3D  •  Nameplates  •  Ping  •  Equipment",
        menuX +
        30f * s,
        menuY +
        43f * s,
        7.2f *
        s *
        ts,
        Colors.fade(
            theme.textTertiary(),
            alpha
        )
    )


    // =================================================
    // TABS
    // =================================================

    val tabs =
        listOf(
            "ESP",
            "NAMEPLATE",
            "FILTERS",
            "PROFILES",
            "STYLE"
        )


    val tabX =
        menuX +
        30f * s


    val tabY =
        menuY +
        68f * s


    val tabW =
        160f * s


    for (
        i in
        tabs.indices
    ) {

        val xx =
            tabX +
            i *
            (
                tabW +
                8f * s
            )


        val selected =
            menuTab ==
            i


        r.roundedRect(
            xx,
            tabY,
            tabW,
            32f * s,
            8f * s,
            Colors.fade(
                if (
                    selected
                ) {

                    Colors.withAlpha(
                        accent(),
                        40
                    )

                } else {

                    uiPanelColor
                },
                alpha
            )
        )


        if (
            selected
        ) {

            r.roundedOutline(
                xx,
                tabY,
                tabW,
                32f * s,
                8f * s,
                1f * s,
                accentColor
            )
        }


        val fs =
            7f *
            s *
            ts


        val tw =
            r.textWidth(
                tabs[i],
                fs,
                Weight.SEMI_BOLD
            )


        val th =
            r.textHeight(
                fs,
                Weight.SEMI_BOLD
            )


        r.text(
            tabs[i],
            xx +
            (
                tabW -
                tw
            ) /
            2f,
            tabY +
            (
                32f *
                s -
                th
            ) /
            2f,
            fs,
            if (
                selected
            ) {
                accentColor
            } else {
                Colors.fade(
                    theme.textSecondary(),
                    alpha
                )
            },
            Weight.SEMI_BOLD
        )
    }


    // =================================================
    // COMMON DRAW HELPERS
    // =================================================

    fun drawToggleCard(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        description: String,
        enabled: Boolean
    ) {

        r.roundedRect(
            x,
            y,
            w,
            h,
            9f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.text(
            title,
            x +
            13f * s,
            y +
            9f * s,
            7.7f *
            s *
            ts,
            Colors.fade(
                theme.textPrimary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        if (
            description.isNotBlank()
        ) {

            r.text(
                description,
                x +
                13f * s,
                y +
                29f * s,
                5.5f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )
        }


        val toggleW =
            34f *
            s


        val toggleH =
            18f *
            s


        val tx =
            x +
            w -
            46f *
            s


        val ty =
            y +
            (
                h -
                toggleH
            ) /
            2f


        r.roundedRect(
            tx,
            ty,
            toggleW,
            toggleH,
            toggleH /
            2f,
            Colors.fade(
                if (
                    enabled
                ) {
                    accent()
                } else {
                    uiWindowColor
                },
                alpha
            )
        )


        r.circle(
            if (
                enabled
            ) {
                tx +
                25f * s
            } else {
                tx +
                9f * s
            },
            ty +
            toggleH /
            2f,
            6f * s,
            Colors.fade(
                theme.textPrimary(),
                alpha
            )
        )
    }


    fun drawSlider(
        id: Int
    ) {

        val y =
            sliderTrackY(
                id
            )


        val progress =
            (
                (
                    sliderValue(id) -
                    sliderMin(id)
                ) /
                (
                    sliderMax(id) -
                    sliderMin(id)
                )
            ).coerceIn(
                0f,
                1f
            )


        val labelX =
            when {

                id in
                20..26 ->

                    menuX +
                    40f * s


                id in
                40..47 ->

                    menuX +
                    40f * s


                id ==
                60 ->

                    menuX +
                    55f * s


                else ->

                    menuX +
                    45f * s
            }


        r.text(
            sliderLabel(
                id
            ),
            labelX,
            y -
            12f * s,
            6.3f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            )
        )


        r.roundedRect(
            sliderTrackX(id),
            y,
            sliderTrackW(id),
            4f * s,
            2f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.roundedRect(
            sliderTrackX(id),
            y,
            sliderTrackW(id) *
            progress,
            4f * s,
            2f * s,
            accentColor
        )


        r.circle(
            sliderTrackX(id) +
            sliderTrackW(id) *
            progress,
            y +
            2f * s,
            5f * s,
            accentColor
        )


        val nx =
            numericBoxX(
                id
            )


        val ny =
            numericBoxY(
                id
            )


        val nw =
            numericBoxW()


        val nh =
            numericBoxH()


        val editing =
            numericEditId ==
            id


        r.roundedRect(
            nx,
            ny,
            nw,
            nh,
            6f * s,
            Colors.fade(
                if (
                    editing
                ) {

                    Colors.mix(
                        uiPanelColor,
                        accent(),
                        0.16f
                    )

                } else {

                    uiPanelColor
                },
                alpha
            )
        )


        if (
            editing
        ) {

            r.roundedOutline(
                nx,
                ny,
                nw,
                nh,
                6f * s,
                1f * s,
                accentColor
            )
        }


        val text =
            if (
                editing
            ) {
                numericEditText
            } else {
                cleanNumber(
                    sliderValue(
                        id
                    )
                )
            }


        val fs =
            6.4f *
            s *
            ts


        val tw =
            r.textWidth(
                text,
                fs,
                Weight.SEMI_BOLD
            )


        val th =
            r.textHeight(
                fs,
                Weight.SEMI_BOLD
            )


        r.text(
            text,
            nx +
            (
                nw -
                tw
            ) /
            2f,
            ny +
            (
                nh -
                th
            ) /
            2f,
            fs,
            if (
                editing
            ) {
                accentColor
            } else {
                Colors.fade(
                    theme.textPrimary(),
                    alpha
                )
            },
            Weight.SEMI_BOLD
        )
    }


    fun drawColorCard(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        target: Int,
        label: String,
        color: Int
    ) {

        r.roundedRect(
            x,
            y,
            w,
            h,
            7f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.circle(
            x +
            14f * s,
            y +
            h /
            2f,
            6f * s,
            color
        )


        val fs =
            5.7f *
            s *
            ts


        val th =
            r.textHeight(
                fs,
                Weight.SEMI_BOLD
            )


        r.text(
            label,
            x +
            26f * s,
            y +
            (
                h -
                th
            ) /
            2f,
            fs,
            if (
                activeColor ==
                target
            ) {
                accentColor
            } else {
                Colors.fade(
                    theme.textSecondary(),
                    alpha
                )
            },
            Weight.SEMI_BOLD
        )
    }


    // =================================================
    // ESP TAB
    // =================================================

    if (
        menuTab ==
        TAB_ESP
    ) {

        val left =
            menuX +
            40f * s


        val right =
            menuX +
            285f * s


        val y0 =
            menuY +
            125f * s


        val cw =
            220f * s


        val ch =
            52f * s


        drawToggleCard(
            left,
            y0,
            cw,
            ch,
            "ESP enabled",
            "Master switch",
            espEnabled
        )


        drawToggleCard(
            right,
            y0,
            cw,
            ch,
            "Box: ${boxModeName()}",
            "Click to cycle",
            boxMode != 3
        )


        drawToggleCard(
            left,
            y0 + 62f * s,
            cw,
            ch,
            "Box fill",
            "Soft background",
            boxFill
        )


        drawToggleCard(
            right,
            y0 + 62f * s,
            cw,
            ch,
            "Glow",
            "Soft outer highlight",
            boxGlow
        )


        drawToggleCard(
            left,
            y0 + 124f * s,
            cw,
            ch,
            "Tracer",
            "Camera to target",
            tracerEnabled
        )


        drawToggleCard(
            right,
            y0 + 124f * s,
            cw,
            ch,
            "Through walls",
            "3D box / tracer",
            throughWalls
        )


        drawToggleCard(
            left,
            y0 + 186f * s,
            cw,
            ch,
            "Hurt flash",
            "Brightens damaged player",
            hurtFlash
        )


        drawToggleCard(
            right,
            y0 + 186f * s,
            cw,
            ch,
            "Distance fade",
            "Far targets become softer",
            distanceFade
        )


        r.text(
            "BOX DETAILS",
            menuX +
            40f * s,
            menuY +
            365f * s,
            7.2f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        for (
            id in
            20..26
        ) {

            drawSlider(
                id
            )
        }


        r.text(
            "ESP COLORS",
            menuX +
            555f * s,
            menuY +
            382f * s,
            7.2f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val cy =
            menuY +
            410f * s


        drawColorCard(
            menuX +
            555f * s,
            cy,
            70f * s,
            36f * s,
            COLOR_ENEMY,
            "ENEMY",
            enemyColor
        )


        drawColorCard(
            menuX +
            633f * s,
            cy,
            70f * s,
            36f * s,
            COLOR_RELATION,
            "ALLY",
            relationColor
        )


        drawColorCard(
            menuX +
            711f * s,
            cy,
            70f * s,
            36f * s,
            COLOR_HEALTH_GOOD,
            "HP +",
            healthGoodColor
        )


        drawColorCard(
            menuX +
            789f * s,
            cy,
            70f * s,
            36f * s,
            COLOR_HEALTH_BAD,
            "HP -",
            healthBadColor
        )


        r.text(
            "PRESETS",
            menuX +
            560f * s,
            menuY +
            527f * s,
            7f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val presetNames =
            listOf(
                "CLEAN",
                "GLASS",
                "NEON"
            )


        for (
            i in
            presetNames.indices
        ) {

            val xx =
                menuX +
                (
                    560f +
                    i *
                    100f
                ) *
                s


            val yy =
                menuY +
                555f * s


            r.roundedRect(
                xx,
                yy,
                90f * s,
                32f * s,
                7f * s,
                Colors.fade(
                    uiPanelColor,
                    alpha
                )
            )


            val fs =
                6.5f *
                s *
                ts


            val tw =
                r.textWidth(
                    presetNames[i],
                    fs,
                    Weight.SEMI_BOLD
                )


            val th =
                r.textHeight(
                    fs,
                    Weight.SEMI_BOLD
                )


            r.text(
                presetNames[i],
                xx +
                (
                    90f *
                    s -
                    tw
                ) /
                2f,
                yy +
                (
                    32f *
                    s -
                    th
                ) /
                2f,
                fs,
                Colors.fade(
                    theme.textSecondary(),
                    alpha
                ),
                Weight.SEMI_BOLD
            )
        }
    }


    // =================================================
    // NAME TAB
    // =================================================

    if (
        menuTab ==
        TAB_NAME
    ) {

        val left =
            menuX +
            40f * s


        val right =
            menuX +
            285f * s


        val y0 =
            menuY +
            125f * s


        val cw =
            220f * s


        val ch =
            48f * s


        val values =
            listOf(
                showNameplate,
                showHead,
                showHealthText,
                showDistance,
                showPing,
                showArmor,
                showGameMode,
                showAbsorption,
                showHealthBar
            )


        val titles =
            listOf(
                "Nameplate",
                "Player head",
                "Health text",
                "Distance",
                "Ping",
                "Armor + hands",
                "Game mode",
                "Absorption",
                "Health bar"
            )


        val descriptions =
            listOf(
                "Custom rounded label",
                "Skin head in label",
                "Current HP",
                "Distance in blocks",
                "Player-list latency",
                "Equipment under box",
                "S / C / A / SP",
                "Yellow health",
                "Vertical HP bar"
            )


        for (
            i in
            values.indices
        ) {

            val col =
                i %
                2


            val row =
                i /
                2


            val xx =
                if (
                    col ==
                    0
                ) {
                    left
                } else {
                    right
                }


            val yy =
                y0 +
                row *
                56f * s


            drawToggleCard(
                xx,
                yy,
                cw,
                ch,
                titles[i],
                descriptions[i],
                values[i]
            )
        }


        r.text(
            "NAMEPLATE DETAILS",
            menuX +
            40f * s,
            menuY +
            344f * s,
            7.2f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        for (
            id in
            40..47
        ) {

            drawSlider(
                id
            )
        }


        r.text(
            "NAMEPLATE COLORS",
            menuX +
            600f * s,
            menuY +
            385f * s,
            7.2f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        drawColorCard(
            menuX +
            600f * s,
            menuY +
            415f * s,
            105f * s,
            38f * s,
            COLOR_NAME_BG,
            "BACKGROUND",
            nameBackgroundColor
        )


        drawColorCard(
            menuX +
            715f * s,
            menuY +
            415f * s,
            105f * s,
            38f * s,
            COLOR_NAME_TEXT,
            "TEXT",
            nameTextColor
        )
    }


    // =================================================
    // FILTERS
    // =================================================

    if (
        menuTab ==
        TAB_FILTERS
    ) {

        val left =
            menuX +
            55f * s


        val right =
            menuX +
            485f * s


        val y0 =
            menuY +
            135f * s


        val cw =
            390f * s


        val ch =
            62f * s


        drawToggleCard(
            left,
            y0,
            cw,
            ch,
            "Show friends",
            "Display players from the client friend list.",
            showFriends
        )


        drawToggleCard(
            left,
            y0 + 75f * s,
            cw,
            ch,
            "Show party",
            "Display your Nursultan party members.",
            showParty
        )


        drawToggleCard(
            left,
            y0 + 150f * s,
            cw,
            ch,
            "Show allies",
            "Display entities detected as allies.",
            showAllies
        )


        drawToggleCard(
            left,
            y0 + 225f * s,
            cw,
            ch,
            "Show bots",
            "Include players marked by client bot heuristics.",
            showBots
        )


        drawToggleCard(
            right,
            y0,
            cw,
            ch,
            "Invisible players",
            "Include players with vanilla invisibility.",
            showInvisible
        )


        drawToggleCard(
            right,
            y0 + 75f * s,
            cw,
            ch,
            "Spectators",
            "Include spectator-mode players.",
            showSpectators
        )


        drawToggleCard(
            right,
            y0 + 150f * s,
            cw,
            ch,
            "Ignore same team",
            "Hide players with the same scoreboard team.",
            ignoreSameTeam
        )


        r.text(
            "RANGE",
            menuX +
            55f * s,
            menuY +
            435f * s,
            7.2f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        drawSlider(
            60
        )


        r.text(
            "Default filter is enemy-oriented: friends, party, allies, bots and same-team players are hidden.",
            menuX +
            55f * s,
            menuY +
            535f * s,
            6.4f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            )
        )
    }


    // =================================================
    // PROFILES
    // =================================================

    if (
        menuTab ==
        TAB_PROFILES
    ) {

        val left =
            menuX +
            45f * s


        val right =
            menuX +
            490f * s


        val inputY =
            menuY +
            135f * s


        r.text(
            "ESP PROFILES",
            left,
            inputY -
            24f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        r.text(
            "MENU THEMES",
            right,
            inputY -
            24f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        r.roundedRect(
            left,
            inputY,
            390f * s,
            36f * s,
            8f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.roundedRect(
            right,
            inputY,
            390f * s,
            36f * s,
            8f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        if (
            focusedField ==
            FIELD_PROFILE
        ) {

            r.roundedOutline(
                left,
                inputY,
                390f * s,
                36f * s,
                8f * s,
                1f * s,
                accentColor
            )
        }


        if (
            focusedField ==
            FIELD_THEME
        ) {

            r.roundedOutline(
                right,
                inputY,
                390f * s,
                36f * s,
                8f * s,
                1f * s,
                accentColor
            )
        }


        val inputFs =
            7f *
            s *
            ts


        val inputH =
            r.textHeight(
                inputFs
            )


        r.text(
            if (
                profileNameText.isBlank()
            ) {
                "ESP profile name..."
            } else {
                profileNameText
            },
            left +
            12f * s,
            inputY +
            (
                36f *
                s -
                inputH
            ) /
            2f,
            inputFs,
            Colors.fade(
                if (
                    profileNameText.isBlank()
                ) {
                    theme.textTertiary()
                } else {
                    theme.textPrimary()
                },
                alpha
            )
        )


        r.text(
            if (
                themeNameText.isBlank()
            ) {
                "Theme name..."
            } else {
                themeNameText
            },
            right +
            12f * s,
            inputY +
            (
                36f *
                s -
                inputH
            ) /
            2f,
            inputFs,
            Colors.fade(
                if (
                    themeNameText.isBlank()
                ) {
                    theme.textTertiary()
                } else {
                    theme.textPrimary()
                },
                alpha
            )
        )


        val saveY =
            menuY +
            183f * s


        r.roundedRect(
            left,
            saveY,
            160f * s,
            32f * s,
            7f * s,
            Colors.fade(
                Colors.withAlpha(
                    accent(),
                    40
                ),
                alpha
            )
        )


        r.roundedRect(
            right,
            saveY,
            160f * s,
            32f * s,
            7f * s,
            Colors.fade(
                Colors.withAlpha(
                    accent(),
                    40
                ),
                alpha
            )
        )


        r.text(
            "SAVE ESP",
            left +
            42f * s,
            saveY +
            10f * s,
            7f *
            s *
            ts,
            accentColor,
            Weight.SEMI_BOLD
        )


        r.text(
            "SAVE THEME",
            right +
            35f * s,
            saveY +
            10f * s,
            7f *
            s *
            ts,
            accentColor,
            Weight.SEMI_BOLD
        )


        val espList =
            espProfiles()


        val themeList =
            themeProfiles()


        val listY =
            menuY +
            235f * s


        val rowH =
            42f * s


        for (
            row in
            0 until 7
        ) {

            val yy =
                listY +
                row *
                48f * s


            val espIndex =
                espProfileScroll +
                row


            if (
                espIndex <
                espList.size
            ) {

                val cfg =
                    espList[
                        espIndex
                    ]


                val selected =
                    selectedEspProfile ==
                    cfg


                r.roundedRect(
                    left,
                    yy,
                    380f * s,
                    rowH,
                    8f * s,
                    Colors.fade(
                        if (
                            selected
                        ) {

                            Colors.withAlpha(
                                accent(),
                                34
                            )

                        } else {

                            uiPanelColor
                        },
                        alpha
                    )
                )


                val fs =
                    7.2f *
                    s *
                    ts


                val th =
                    r.textHeight(
                        fs,
                        Weight.SEMI_BOLD
                    )


                r.text(
                    espProfileTitle(
                        cfg
                    ),
                    left +
                    14f * s,
                    yy +
                    (
                        rowH -
                        th
                    ) /
                    2f,
                    fs,
                    Colors.fade(
                        theme.textPrimary(),
                        alpha
                    ),
                    Weight.SEMI_BOLD
                )


                val loadX =
                    left +
                    285f * s


                val loadY =
                    yy +
                    5f * s


                r.roundedRect(
                    loadX,
                    loadY,
                    95f * s,
                    32f * s,
                    6f * s,
                    Colors.fade(
                        Colors.withAlpha(
                            accent(),
                            36
                        ),
                        alpha
                    )
                )


                r.text(
                    "LOAD",
                    loadX +
                    30f * s,
                    loadY +
                    10f * s,
                    6.5f *
                    s *
                    ts,
                    accentColor,
                    Weight.BOLD
                )
            }


            val themeIndex =
                themeProfileScroll +
                row


            if (
                themeIndex <
                themeList.size
            ) {

                val cfg =
                    themeList[
                        themeIndex
                    ]


                val selected =
                    selectedThemeProfile ==
                    cfg


                r.roundedRect(
                    right,
                    yy,
                    380f * s,
                    rowH,
                    8f * s,
                    Colors.fade(
                        if (
                            selected
                        ) {

                            Colors.withAlpha(
                                accent(),
                                34
                            )

                        } else {

                            uiPanelColor
                        },
                        alpha
                    )
                )


                val fs =
                    7.2f *
                    s *
                    ts


                val th =
                    r.textHeight(
                        fs,
                        Weight.SEMI_BOLD
                    )


                r.text(
                    themeProfileTitle(
                        cfg
                    ),
                    right +
                    14f * s,
                    yy +
                    (
                        rowH -
                        th
                    ) /
                    2f,
                    fs,
                    Colors.fade(
                        theme.textPrimary(),
                        alpha
                    ),
                    Weight.SEMI_BOLD
                )


                val loadX =
                    right +
                    285f * s


                val loadY =
                    yy +
                    5f * s


                r.roundedRect(
                    loadX,
                    loadY,
                    95f * s,
                    32f * s,
                    6f * s,
                    Colors.fade(
                        Colors.withAlpha(
                            accent(),
                            36
                        ),
                        alpha
                    )
                )


                r.text(
                    "LOAD",
                    loadX +
                    30f * s,
                    loadY +
                    10f * s,
                    6.5f *
                    s *
                    ts,
                    accentColor,
                    Weight.BOLD
                )
            }
        }


        val bottomY =
            menuY +
            575f * s


        r.roundedRect(
            left,
            bottomY,
            150f * s,
            32f * s,
            7f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.roundedRect(
            right,
            bottomY,
            150f * s,
            32f * s,
            7f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        r.text(
            "DELETE ESP",
            left +
            30f * s,
            bottomY +
            10f * s,
            6.5f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        r.text(
            "DELETE THEME",
            right +
            24f * s,
            bottomY +
            10f * s,
            6.5f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )
    }


    // =================================================
    // STYLE
    // =================================================

    if (
        menuTab ==
        TAB_STYLE
    ) {

        r.text(
            "WINDOW",
            menuX +
            45f * s,
            menuY +
            104f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        for (
            id in
            0..10
        ) {

            drawSlider(
                id
            )
        }


        r.text(
            "COLORS",
            menuX +
            545f * s,
            menuY +
            102f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val cy =
            menuY +
            125f * s


        drawColorCard(
            menuX +
            545f * s,
            cy,
            95f * s,
            38f * s,
            COLOR_WINDOW,
            "WINDOW",
            uiWindowColor
        )


        drawColorCard(
            menuX +
            650f * s,
            cy,
            95f * s,
            38f * s,
            COLOR_PANEL,
            "PANELS",
            uiPanelColor
        )


        drawColorCard(
            menuX +
            755f * s,
            cy,
            95f * s,
            38f * s,
            COLOR_ACCENT,
            "ACCENT",
            uiAccentColor
        )


        if (
            activeColor ==
            COLOR_NONE
        ) {

            r.text(
                "Click a number to type an exact value.",
                menuX +
                565f * s,
                menuY +
                210f * s,
                6.5f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )


            r.text(
                "Menu scale uses a frozen drag track, so it no longer jumps while resizing.",
                menuX +
                565f * s,
                menuY +
                235f * s,
                6.2f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )
        }


        val bottomY =
            menuY +
            570f * s


        val labels =
            listOf(
                if (
                    stripEnabled
                ) {
                    "STRIP ON"
                } else {
                    "STRIP OFF"
                },
                "SIDE ${stripName()}",
                if (
                    uiUseClientAccent
                ) {
                    "CLIENT ACCENT"
                } else {
                    "CUSTOM ACCENT"
                },
                animationName(),
                "RESET STYLE"
            )


        val widths =
            listOf(
                112f,
                125f,
                150f,
                150f,
                145f
            )


        var ox =
            menuX +
            45f * s


        for (
            i in
            labels.indices
        ) {

            val width =
                widths[i] *
                s


            r.roundedRect(
                ox,
                bottomY,
                width,
                32f * s,
                7f * s,
                Colors.fade(
                    uiPanelColor,
                    alpha
                )
            )


            val fs =
                6.1f *
                s *
                ts


            val tw =
                r.textWidth(
                    labels[i],
                    fs,
                    Weight.SEMI_BOLD
                )


            val th =
                r.textHeight(
                    fs,
                    Weight.SEMI_BOLD
                )


            r.text(
                labels[i],
                ox +
                (
                    width -
                    tw
                ) /
                2f,
                bottomY +
                (
                    32f *
                    s -
                    th
                ) /
                2f,
                fs,
                if (
                    i ==
                    0 &&
                    stripEnabled
                ) {
                    accentColor
                } else {
                    Colors.fade(
                        theme.textSecondary(),
                        alpha
                    )
                },
                Weight.SEMI_BOLD
            )


            ox +=
                width +
                8f * s
        }
    }


    // =================================================
    // COLOR PICKER OVERLAY
    // =================================================

    if (
        activeColor !=
        COLOR_NONE
    ) {

        val px =
            pickerX()


        val py =
            pickerY()


        val pw =
            pickerW()


        val ph =
            pickerH()


        r.roundedRect(
            px -
            14f * s,
            py -
            40f * s,
            pw +
            28f * s,
            ph +
            115f * s,
            12f * s,
            Colors.fade(
                Colors.withAlpha(
                    uiWindowColor,
                    245
                ),
                alpha
            )
        )


        r.roundedOutline(
            px -
            14f * s,
            py -
            40f * s,
            pw +
            28f * s,
            ph +
            115f * s,
            12f * s,
            1f * s,
            Colors.fade(
                Colors.withAlpha(
                    accent(),
                    65
                ),
                alpha
            )
        )


        r.text(
            "COLOR PICKER",
            px,
            py -
            27f * s,
            7.5f *
            s *
            ts,
            Colors.fade(
                theme.textPrimary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        r.gradient(
            px,
            py,
            pw,
            ph,
            Colors.WHITE,
            hsvToColor(
                pickerHue,
                1f,
                1f
            ),
            true
        )


        r.gradient(
            px,
            py,
            pw,
            ph,
            Colors.TRANSPARENT,
            Colors.BLACK,
            false
        )


        val cursorX =
            px +
            pickerSat *
            pw


        val cursorY =
            py +
            (
                1f -
                pickerValue
            ) *
            ph


        r.circle(
            cursorX,
            cursorY,
            6f * s,
            Colors.WHITE
        )


        r.ring(
            cursorX,
            cursorY,
            7f * s,
            2f * s,
            Colors.BLACK
        )


        val hues =
            listOf(
                0f,
                1f / 6f,
                2f / 6f,
                3f / 6f,
                4f / 6f,
                5f / 6f,
                1f
            )


        for (
            i in
            0 until 6
        ) {

            val part =
                pw /
                6f


            r.gradient(
                px +
                part *
                i,
                pickerHueY(),
                part +
                1f,
                14f * s,
                hsvToColor(
                    hues[i],
                    1f,
                    1f
                ),
                hsvToColor(
                    hues[i + 1],
                    1f,
                    1f
                ),
                true
            )
        }


        r.circle(
            px +
            pickerHue *
            pw,
            pickerHueY() +
            7f * s,
            5f * s,
            Colors.WHITE
        )


        r.text(
            colorHex(
                pickerColor()
            ),
            px,
            pickerHueY() +
            34f * s,
            7f *
            s *
            ts,
            Colors.fade(
                theme.textPrimary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val doneX =
            px +
            pw -
            70f * s


        val doneY =
            pickerHueY() +
            30f * s


        val doneW =
            70f * s


        val doneH =
            28f * s


        r.roundedRect(
            doneX,
            doneY,
            doneW,
            doneH,
            6f * s,
            Colors.fade(
                uiPanelColor,
                alpha
            )
        )


        val doneFs =
            6.5f *
            s *
            ts


        val doneWText =
            r.textWidth(
                "DONE",
                doneFs,
                Weight.BOLD
            )


        val doneHText =
            r.textHeight(
                doneFs,
                Weight.BOLD
            )


        r.text(
            "DONE",
            doneX +
            (
                doneW -
                doneWText
            ) /
            2f,
            doneY +
            (
                doneH -
                doneHText
            ) /
            2f,
            doneFs,
            accentColor,
            Weight.BOLD
        )
    }


    // =================================================
    // FOOTER
    // =================================================

    r.text(
        "$statusText  •  F9 closes  •  drag header to move  •  exact values: click number + Enter",
        menuX +
        30f * s,
        menuY +
        menuH -
        20f * s,
        6.1f *
        s *
        ts,
        Colors.fade(
            theme.textQuaternary(),
            alpha
        )
    )
}


// =====================================================
// ENABLE
// =====================================================

onEnable {

    loadStorage()


    menuOpen =
        false


    menuAlpha =
        0f


    lastRenderMs =
        0L


    numericEditId =
        -1


    activeColor =
        COLOR_NONE


    draggingSlider =
        -1


    statusText =
        "READY"
}


// =====================================================
// DISABLE
// =====================================================

onDisable {

    if (
        numericEditId >=
        0
    ) {

        commitNumericEdit()
    }


    menuOpen =
        false


    windowDragging =
        false


    draggingSlider =
        -1


    draggingColorSV =
        false


    draggingColorHue =
        false


    activeColor =
        COLOR_NONE


    saveStorage()


    if (
        inGame &&
        !game.screenOpen()
    ) {

        keys.lockCursor()
    }
}