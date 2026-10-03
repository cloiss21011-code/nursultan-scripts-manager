name("Inventory Manager V5")
description("Inventory layouts, profiles, automation and fully customizable GUI")
requireApi(9)


// =====================================================
// BASE SETTING
// =====================================================

val managerKey = hotkey(
    "Open Inventory Manager",
    Key.RIGHT_SHIFT
) {
}


// =====================================================
// CONSTANTS
// =====================================================

val TAB_LAYOUT = 0
val TAB_PROFILES = 1
val TAB_AUTOMATION = 2
val TAB_STYLE = 3

val FIELD_NONE = 0
val FIELD_PROFILE = 1
val FIELD_THEME = 2

val COLOR_NONE = 0
val COLOR_WINDOW = 1
val COLOR_ELEMENT = 2
val COLOR_ACCENT = 3
val COLOR_MISSING = 4

val ARMOR_SLOTS = listOf(
    ArmorSlot.HELMET,
    ArmorSlot.CHESTPLATE,
    ArmorSlot.LEGGINGS,
    ArmorSlot.BOOTS
)


// =====================================================
// AUTOMATION
// =====================================================

var autoSort = true
var inventoryLock = false

var restoreSavedArmor = true
var autoBestArmor = false
var restoreOffhand = true

var pickupDelayMs = 600f
var moveDelayTicks = 1f

var missingOverlay = true
var missingBlink = true

var missingBlinkSpeed = 1.8f
var missingFlashIntensity = 0.80f
var missingGhostDarkness = 115f

var missingOffsetX = 0f
var missingOffsetY = 0f


// =====================================================
// COMMON WINDOW STYLE
// =====================================================

var uiBlur = 16f
var uiPanelBlur = 10f

var uiOpacity = 220f
var uiBackdrop = 110f

var uiScale = 1.00f
var uiTextScale = 1.15f

var uiRadius = 14f

// 0 Fade
// 1 Slide Up
// 2 Slide Down
var uiAnimation = 1
var uiAnimationSpeed = 150f

var uiWindowColor = 0xFF0D121BL.toInt()
var uiElementColor = 0xFF171F2CL.toInt()
var uiAccentColor = 0xFF72C7FFL.toInt()
var missingColor = 0xFFFF4F5EL.toInt()

var uiUseClientAccent = false

var stripEnabled = true

// 0 left
// 1 top
// 2 right
// 3 bottom
var stripPosition = 0

var stripThickness = 4f
var stripInset = 7f
var stripLength = 0.78f


// =====================================================
// INVENTORY STATE
// =====================================================

var savedLayout = MutableList(36) { "" }
var savedArmor = MutableList(4) { "" }

var savedOffhand = ""

var sortingRequested = false
var sortingActive = false

var pendingAutoSortAt = 0L
var lastTotalItems = 0

var completedSorts = 0
var lastStatus = "READY"

var activeProfile = ""

var selectedInventoryProfile = ""
var selectedThemeProfile = ""

var inventoryProfileScroll = 0
var themeProfileScroll = 0


// =====================================================
// GUI STATE
// =====================================================

var menuOpen = false
var menuTab = TAB_LAYOUT

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


// =====================================================
// SLIDER DRAG
// =====================================================

var draggingSlider = -1

// Critical fix:
// track geometry is frozen at mouse-down time.
// uiScale changing can no longer move its own input coordinate system.
var dragTrackX = 0f
var dragTrackW = 1f


// =====================================================
// TEXT INPUT
// =====================================================

var focusedField = FIELD_NONE

var profileNameText = ""
var themeNameText = ""

var numericEditId = -1
var numericEditText = ""


// =====================================================
// COLOR PICKER
// =====================================================

var activeColor = COLOR_NONE

var pickerHue = 0f
var pickerSat = 0f
var pickerValue = 1f

var draggingColorSV = false
var draggingColorHue = false


// =====================================================
// BASICS
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

    return if (uiUseClientAccent) {
        theme.accent()
    } else {
        uiAccentColor
    }
}


fun layoutExists(): Boolean {

    return (
        savedLayout.any { it.isNotBlank() } ||
        savedArmor.any { it.isNotBlank() } ||
        savedOffhand.isNotBlank()
    )
}


fun canModifyInventory(): Boolean {

    if (!inGame) {
        return false
    }


    val kind =
        game.screenKind()


    return (
        kind == ScreenKind.NONE ||
        kind == ScreenKind.INVENTORY
    )
}


fun stripName(): String {

    return when (stripPosition) {

        0 -> "LEFT"
        1 -> "TOP"
        2 -> "RIGHT"
        else -> "BOTTOM"
    }
}


fun animationName(): String {

    return when (uiAnimation) {

        0 -> "FADE"
        1 -> "SLIDE UP"
        else -> "SLIDE DOWN"
    }
}


fun colorHex(
    color: Int
): String {

    return "#" +
        (
            color and
            0xFFFFFF
        ).toString(16)
            .uppercase()
            .padStart(
                6,
                '0'
            )
}


// =====================================================
// NUMBER FORMATTING
// =====================================================

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


    while (hue < 0f) {
        hue += 1f
    }


    while (hue >= 1f) {
        hue -= 1f
    }


    val sat =
        satRaw.coerceIn(
            0f,
            1f
        )


    val value =
        valueRaw.coerceIn(
            0f,
            1f
        )


    if (sat <= 0f) {

        val c =
            (
                value *
                255f +
                0.5f
            ).toInt()


        return Colors.rgb(
            c,
            c,
            c
        )
    }


    val scaled =
        hue *
        6f


    val sector =
        kotlin.math.floor(
            scaled.toDouble()
        ).toInt()


    val f =
        scaled -
        sector.toFloat()


    val p =
        value *
        (
            1f -
            sat
        )


    val q =
        value *
        (
            1f -
            sat *
            f
        )


    val t =
        value *
        (
            1f -
            sat *
            (
                1f -
                f
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

    val r =
        Colors.red(color)
            .toFloat() /
        255f


    val g =
        Colors.green(color)
            .toFloat() /
        255f


    val b =
        Colors.blue(color)
            .toFloat() /
        255f


    val max =
        kotlin.math.max(
            r,
            kotlin.math.max(
                g,
                b
            )
        )


    val min =
        kotlin.math.min(
            r,
            kotlin.math.min(
                g,
                b
            )
        )


    val delta =
        max -
        min


    var hue =
        0f


    if (
        delta >
        0.00001f
    ) {

        hue =
            when (max) {

                r ->
                    (
                        (
                            g -
                            b
                        ) /
                        delta
                    ) %
                    6f

                g ->
                    (
                        (
                            b -
                            r
                        ) /
                        delta
                    ) +
                    2f

                else ->
                    (
                        (
                            r -
                            g
                        ) /
                        delta
                    ) +
                    4f
            }


        hue /=
            6f


        if (hue < 0f) {
            hue += 1f
        }
    }


    val saturation =
        if (
            max <=
            0f
        ) {

            0f

        } else {

            delta /
            max
        }


    return Triple(
        hue,
        saturation,
        max
    )
}


fun pickerColor(): Int {

    return when (activeColor) {

        COLOR_WINDOW ->
            uiWindowColor

        COLOR_ELEMENT ->
            uiElementColor

        COLOR_ACCENT ->
            uiAccentColor

        COLOR_MISSING ->
            missingColor

        else ->
            Colors.WHITE
    }
}


fun setPickerColor(
    color: Int
) {

    when (activeColor) {

        COLOR_WINDOW ->
            uiWindowColor =
                color

        COLOR_ELEMENT ->
            uiElementColor =
                color

        COLOR_ACCENT ->
            uiAccentColor =
                color

        COLOR_MISSING ->
            missingColor =
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
// PICKER POSITIONS
// =====================================================

fun pickerX(): Float {

    return menuX +
        470f *
        uiScale
}


fun pickerY(): Float {

    return menuY +
        170f *
        uiScale
}


fun pickerW(): Float {

    return 230f *
        uiScale
}


fun pickerH(): Float {

    return 155f *
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
// STORAGE
// =====================================================

fun saveStorage() {

    storage.putList(
        "layout_main",
        savedLayout
    )

    storage.putList(
        "layout_armor",
        savedArmor
    )

    storage.put(
        "layout_offhand",
        savedOffhand
    )


    storage.put(
        "sorts",
        completedSorts
    )

    storage.put(
        "active_profile",
        activeProfile
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
        "auto_sort",
        autoSort
    )

    storage.put(
        "inventory_lock",
        inventoryLock
    )

    storage.put(
        "restore_armor",
        restoreSavedArmor
    )

    storage.put(
        "best_armor",
        autoBestArmor
    )

    storage.put(
        "restore_offhand",
        restoreOffhand
    )

    storage.put(
        "pickup_delay",
        pickupDelayMs.toDouble()
    )

    storage.put(
        "move_delay",
        moveDelayTicks.toDouble()
    )


    storage.put(
        "missing_overlay",
        missingOverlay
    )

    storage.put(
        "missing_blink",
        missingBlink
    )

    storage.put(
        "missing_speed",
        missingBlinkSpeed.toDouble()
    )

    storage.put(
        "missing_intensity",
        missingFlashIntensity.toDouble()
    )

    storage.put(
        "missing_darkness",
        missingGhostDarkness.toDouble()
    )

    storage.put(
        "missing_offset_x",
        missingOffsetX.toDouble()
    )

    storage.put(
        "missing_offset_y",
        missingOffsetY.toDouble()
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
        "element_color",
        uiElementColor
    )

    storage.put(
        "accent_color",
        uiAccentColor
    )

    storage.put(
        "missing_color",
        missingColor
    )

    storage.put(
        "client_accent",
        uiUseClientAccent
    )


    storage.put(
        "strip_enabled",
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


fun loadStorage() {

    val main =
        storage.getList(
            "layout_main"
        )


    savedLayout =
        MutableList(36) { i ->
            main.getOrNull(i) ?: ""
        }


    val armorData =
        storage.getList(
            "layout_armor"
        )


    savedArmor =
        MutableList(4) { i ->
            armorData.getOrNull(i) ?: ""
        }


    savedOffhand =
        storage.get(
            "layout_offhand",
            ""
        )


    completedSorts =
        storage.getInt(
            "sorts",
            0
        )


    activeProfile =
        storage.get(
            "active_profile",
            ""
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


    autoSort =
        storage.getBoolean(
            "auto_sort",
            true
        )


    inventoryLock =
        storage.getBoolean(
            "inventory_lock",
            false
        )


    restoreSavedArmor =
        storage.getBoolean(
            "restore_armor",
            true
        )


    autoBestArmor =
        storage.getBoolean(
            "best_armor",
            false
        )


    restoreOffhand =
        storage.getBoolean(
            "restore_offhand",
            true
        )


    pickupDelayMs =
        storage.getDouble(
            "pickup_delay",
            600.0
        ).toFloat()
            .coerceIn(
                100f,
                3000f
            )


    moveDelayTicks =
        storage.getDouble(
            "move_delay",
            1.0
        ).toFloat()
            .coerceIn(
                0f,
                10f
            )


    missingOverlay =
        storage.getBoolean(
            "missing_overlay",
            true
        )


    missingBlink =
        storage.getBoolean(
            "missing_blink",
            true
        )


    missingBlinkSpeed =
        storage.getDouble(
            "missing_speed",
            1.8
        ).toFloat()
            .coerceIn(
                0.2f,
                6f
            )


    missingFlashIntensity =
        storage.getDouble(
            "missing_intensity",
            0.8
        ).toFloat()
            .coerceIn(
                0.1f,
                1f
            )


    missingGhostDarkness =
        storage.getDouble(
            "missing_darkness",
            115.0
        ).toFloat()
            .coerceIn(
                0f,
                220f
            )


    missingOffsetX =
        storage.getDouble(
            "missing_offset_x",
            0.0
        ).toFloat()
            .coerceIn(
                -60f,
                60f
            )


    missingOffsetY =
        storage.getDouble(
            "missing_offset_y",
            0.0
        ).toFloat()
            .coerceIn(
                -60f,
                60f
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
                26f
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


    uiElementColor =
        storage.getInt(
            "element_color",
            0xFF171F2CL.toInt()
        )


    uiAccentColor =
        storage.getInt(
            "accent_color",
            0xFF72C7FFL.toInt()
        )


    missingColor =
        storage.getInt(
            "missing_color",
            0xFFFF4F5EL.toInt()
        )


    uiUseClientAccent =
        storage.getBoolean(
            "client_accent",
            false
        )


    stripEnabled =
        storage.getBoolean(
            "strip_enabled",
            true
        )


    stripPosition =
        storage.getInt(
            "strip_position",
            0
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
            0.78
        ).toFloat()
            .coerceIn(
                0.2f,
                1f
            )
}


// =====================================================
// INVENTORY DATA
// =====================================================

fun totalItemCount(): Int {

    if (!inGame) {
        return 0
    }


    var total =
        0


    for (
        i in
        0 until 36
    ) {

        val item =
            inventory.item(
                Slot.inventory(i)
            )


        if (!item.empty()) {
            total +=
                item.count()
        }
    }


    for (
        slot in
        ARMOR_SLOTS
    ) {

        val item =
            inventory.armor(
                slot
            )


        if (!item.empty()) {
            total +=
                item.count()
        }
    }


    val off =
        inventory.offhand()


    if (!off.empty()) {
        total +=
            off.count()
    }


    return total
}


fun availableStackCounts(): MutableMap<String, Int> {

    val result =
        mutableMapOf<String, Int>()


    if (!inGame) {
        return result
    }


    for (
        i in
        0 until 36
    ) {

        val item =
            inventory.item(
                Slot.inventory(i)
            )


        if (!item.empty()) {

            val id =
                item.id()


            result[id] =
                (
                    result[id] ?:
                    0
                ) +
                1
        }
    }


    for (
        armorSlot in
        ARMOR_SLOTS
    ) {

        val item =
            inventory.armor(
                armorSlot
            )


        if (!item.empty()) {

            val id =
                item.id()


            result[id] =
                (
                    result[id] ?:
                    0
                ) +
                1
        }
    }


    val off =
        inventory.offhand()


    if (!off.empty()) {

        val id =
            off.id()


        result[id] =
            (
                result[id] ?:
                0
            ) +
                1
    }


    return result
}


fun missingMainFlags(): BooleanArray {

    val available =
        availableStackCounts()


    val flags =
        BooleanArray(
            36
        )


    for (
        i in
        0 until 36
    ) {

        val id =
            savedLayout[i]


        if (id.isBlank()) {
            continue
        }


        val amount =
            available[id] ?:
            0


        if (
            amount >
            0
        ) {

            available[id] =
                amount -
                1

        } else {

            flags[i] =
                true
        }
    }


    return flags
}


// =====================================================
// LAYOUT
// =====================================================

fun captureCurrentInventory() {

    if (!inGame) {
        return
    }


    for (
        i in
        0 until 36
    ) {

        val item =
            inventory.item(
                Slot.inventory(i)
            )


        savedLayout[i] =
            if (item.empty()) {
                ""
            } else {
                item.id()
            }
    }


    for (
        i in
        ARMOR_SLOTS.indices
    ) {

        val item =
            inventory.armor(
                ARMOR_SLOTS[i]
            )


        savedArmor[i] =
            if (item.empty()) {
                ""
            } else {
                item.id()
            }
    }


    val off =
        inventory.offhand()


    savedOffhand =
        if (off.empty()) {
            ""
        } else {
            off.id()
        }


    activeProfile =
        ""


    saveStorage()


    lastStatus =
        "LAYOUT SAVED"
}


fun clearLayout() {

    for (
        i in
        0 until 36
    ) {
        savedLayout[i] =
            ""
    }


    for (
        i in
        0 until 4
    ) {
        savedArmor[i] =
            ""
    }


    savedOffhand =
        ""


    activeProfile =
        ""


    sortingRequested =
        false

    sortingActive =
        false


    saveStorage()


    lastStatus =
        "LAYOUT CLEARED"
}


// =====================================================
// PROFILE HELPERS
// =====================================================

fun cleanName(
    raw: String
): String {

    val out =
        StringBuilder()


    for (
        c in
        raw.trim()
            .lowercase()
    ) {

        when {

            c in 'a'..'z' ->
                out.append(
                    c
                )

            c in '0'..'9' ->
                out.append(
                    c
                )

            c == '_' ||
            c == '-' ->
                out.append(
                    c
                )

            c == ' ' ->
                out.append(
                    '_'
                )
        }


        if (
            out.length >=
            24
        ) {
            break
        }
    }


    return out.toString()
}


fun inventoryConfigName(
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
        "inv_$clean"
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
        "theme_$clean"
    }
}


fun inventoryProfiles(): List<String> {

    return configs.names()
        .filter {
            it.startsWith(
                "inv_"
            )
        }
}


fun inventoryProfileTitle(
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
            "inv_"
        )
    )
}


fun themeProfiles(): List<String> {

    return configs.names()
        .filter {
            it.startsWith(
                "theme_"
            )
        }
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
            "theme_"
        )
    )
}


// =====================================================
// INVENTORY PROFILES
// =====================================================

fun saveInventoryProfile(
    title: String,
    captureFirst: Boolean
) {

    if (
        captureFirst
    ) {

        if (!inGame) {
            return
        }


        captureCurrentInventory()
    }


    val name =
        inventoryConfigName(
            title
        )


    if (
        name.isBlank()
    ) {

        lastStatus =
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


    cfg.putList(
        "layout_main",
        savedLayout
    )


    cfg.putList(
        "layout_armor",
        savedArmor
    )


    cfg.put(
        "layout_offhand",
        savedOffhand
    )


    cfg.save()


    activeProfile =
        name

    selectedInventoryProfile =
        name


    saveStorage()


    lastStatus =
        "PROFILE SAVED"
}


fun loadInventoryProfile(
    name: String,
    sortAfter: Boolean
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


    val main =
        cfg.getList(
            "layout_main"
        )


    savedLayout =
        MutableList(36) { i ->
            main.getOrNull(i) ?: ""
        }


    val armorData =
        cfg.getList(
            "layout_armor"
        )


    savedArmor =
        MutableList(4) { i ->
            armorData.getOrNull(i) ?: ""
        }


    savedOffhand =
        cfg.get(
            "layout_offhand",
            ""
        )


    activeProfile =
        name


    saveStorage()


    if (
        sortAfter
    ) {

        requestSort()


        lastStatus =
            "PROFILE LOADED + SORT"

    } else {

        lastStatus =
            "PROFILE LOADED"
    }
}


fun deleteInventoryProfile(
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
        activeProfile ==
        name
    ) {
        activeProfile =
            ""
    }


    if (
        selectedInventoryProfile ==
        name
    ) {
        selectedInventoryProfile =
            ""
    }


    saveStorage()


    lastStatus =
        "PROFILE DELETED"
}


// =====================================================
// THEME PROFILE
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

        lastStatus =
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
        "window_color",
        uiWindowColor
    )

    cfg.put(
        "element_color",
        uiElementColor
    )

    cfg.put(
        "accent_color",
        uiAccentColor
    )

    cfg.put(
        "missing_color",
        missingColor
    )

    cfg.put(
        "client_accent",
        uiUseClientAccent
    )


    cfg.put(
        "strip_enabled",
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


    lastStatus =
        "THEME SAVED"
}


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
                26f
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
            "window_color",
            uiWindowColor
        )


    uiElementColor =
        cfg.getInt(
            "element_color",
            uiElementColor
        )


    uiAccentColor =
        cfg.getInt(
            "accent_color",
            uiAccentColor
        )


    missingColor =
        cfg.getInt(
            "missing_color",
            missingColor
        )


    uiUseClientAccent =
        cfg.getBoolean(
            "client_accent",
            uiUseClientAccent
        )


    stripEnabled =
        cfg.getBoolean(
            "strip_enabled",
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
                0.2f,
                1f
            )


    saveStorage()


    lastStatus =
        "THEME LOADED"
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


    lastStatus =
        "THEME DELETED"
}


// =====================================================
// SORT ENGINE
// =====================================================

fun findMovableItem(
    wantedId: String,
    exclude: Int = -1
): Slot {

    if (
        wantedId.isBlank()
    ) {
        return Slot.NONE
    }


    for (
        i in
        0 until 36
    ) {

        if (
            i ==
            exclude
        ) {
            continue
        }


        val slot =
            Slot.inventory(
                i
            )


        val item =
            inventory.item(
                slot
            )


        if (
            item.empty() ||
            item.id() !=
            wantedId
        ) {
            continue
        }


        if (
            savedLayout[i] ==
            wantedId
        ) {
            continue
        }


        return slot
    }


    return Slot.NONE
}


fun queueSwap(
    source: Slot,
    target: Slot
) {

    val delay =
        moveDelayTicks
            .toInt()
            .coerceIn(
                0,
                10
            )


    inventory.batch { batch ->

        batch.click(
            source,
            false
        )


        if (
            delay >
            0
        ) {
            batch.delay(
                delay
            )
        }


        batch.click(
            target,
            false
        )


        if (
            delay >
            0
        ) {
            batch.delay(
                delay
            )
        }


        batch.click(
            source,
            false
        )
    }
}


fun restoreArmorStep(): Boolean {

    if (
        !restoreSavedArmor
    ) {
        return false
    }


    for (
        i in
        ARMOR_SLOTS.indices
    ) {

        val wanted =
            savedArmor[i]


        if (
            wanted.isBlank()
        ) {
            continue
        }


        val armorSlot =
            ARMOR_SLOTS[i]


        val equipped =
            inventory.armor(
                armorSlot
            )


        if (
            !equipped.empty() &&
            equipped.id() ==
            wanted
        ) {
            continue
        }


        val source =
            findMovableItem(
                wanted
            )


        if (
            !source.found()
        ) {
            continue
        }


        queueSwap(
            source,
            Slot.armor(
                armorSlot
            )
        )


        lastStatus =
            "RESTORING ARMOR"


        return true
    }


    return false
}


fun bestArmorStep(): Boolean {

    if (
        !autoBestArmor
    ) {
        return false
    }


    if (
        restoreSavedArmor &&
        savedArmor.any {
            it.isNotBlank()
        }
    ) {
        return false
    }


    for (
        armorSlot in
        ARMOR_SLOTS
    ) {

        val best =
            armor.bestSlotFor(
                armorSlot
            )


        if (
            !best.found()
        ) {
            continue
        }


        inventory.shiftClick(
            best
        )


        lastStatus =
            "EQUIPPING ARMOR"


        return true
    }


    return false
}


fun restoreOffhandStep(): Boolean {

    if (
        !restoreOffhand ||
        savedOffhand.isBlank()
    ) {
        return false
    }


    val current =
        inventory.offhand()


    if (
        !current.empty() &&
        current.id() ==
        savedOffhand
    ) {
        return false
    }


    val source =
        findMovableItem(
            savedOffhand
        )


    if (
        !source.found()
    ) {
        return false
    }


    queueSwap(
        source,
        Slot.offhand()
    )


    lastStatus =
        "RESTORING OFFHAND"


    return true
}


fun restoreMainStep(): Boolean {

    for (
        targetIndex in
        0 until 36
    ) {

        val wanted =
            savedLayout[
                targetIndex
            ]


        if (
            wanted.isBlank()
        ) {
            continue
        }


        val target =
            Slot.inventory(
                targetIndex
            )


        val current =
            inventory.item(
                target
            )


        if (
            !current.empty() &&
            current.id() ==
            wanted
        ) {
            continue
        }


        val source =
            findMovableItem(
                wanted,
                targetIndex
            )


        if (
            !source.found()
        ) {
            continue
        }


        if (
            targetIndex <
            9
        ) {

            inventory.swap(
                source,
                Slot.hotbar(
                    targetIndex
                )
            )

        } else {

            queueSwap(
                source,
                target
            )
        }


        lastStatus =
            "SORTING SLOT ${targetIndex + 1}"


        return true
    }


    return false
}


fun executeSortStep(): Boolean {

    if (
        !layoutExists()
    ) {
        return false
    }


    if (
        !canModifyInventory() ||
        inventory.busy()
    ) {
        return true
    }


    if (
        restoreArmorStep()
    ) {
        return true
    }


    if (
        bestArmorStep()
    ) {
        return true
    }


    if (
        restoreOffhandStep()
    ) {
        return true
    }


    if (
        restoreMainStep()
    ) {
        return true
    }


    return false
}


fun requestSort() {

    if (
        !layoutExists()
    ) {

        lastStatus =
            "NO SAVED LAYOUT"

        return
    }


    sortingRequested =
        true


    lastStatus =
        "SORT QUEUED"
}


// =====================================================
// STYLE SLIDERS
// =====================================================

fun sliderValue(
    id: Int
): Float {

    return when (id) {

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

        20 -> pickupDelayMs
        21 -> moveDelayTicks
        22 -> missingBlinkSpeed
        23 -> missingFlashIntensity
        24 -> missingGhostDarkness
        25 -> missingOffsetX

        else ->
            missingOffsetY
    }
}


fun sliderMin(
    id: Int
): Float {

    return when (id) {

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

        20 -> 100f
        21 -> 0f
        22 -> 0.2f
        23 -> 0.1f
        24 -> 0f
        25 -> -60f

        else ->
            -60f
    }
}


fun sliderMax(
    id: Int
): Float {

    return when (id) {

        0 -> 30f
        1 -> 30f
        2 -> 255f
        3 -> 220f
        4 -> 1.30f
        5 -> 1.60f
        6 -> 26f
        7 -> 400f
        8 -> 12f
        9 -> 1f
        10 -> 24f

        20 -> 3000f
        21 -> 10f
        22 -> 6f
        23 -> 1f
        24 -> 220f
        25 -> 60f

        else ->
            60f
    }
}


fun sliderLabel(
    id: Int
): String {

    return when (id) {

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

        20 -> "Pickup settle delay"
        21 -> "Move delay"
        22 -> "Missing blink speed"
        23 -> "Missing flash intensity"
        24 -> "Ghost darkness"
        25 -> "Overlay X offset"

        else ->
            "Overlay Y offset"
    }
}


fun setSliderValue(
    id: Int,
    raw: Float
) {

    val value =
        raw.coerceIn(
            sliderMin(id),
            sliderMax(id)
        )


    when (id) {

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
            pickupDelayMs =
                value

        21 ->
            moveDelayTicks =
                value

        22 ->
            missingBlinkSpeed =
                value

        23 ->
            missingFlashIntensity =
                value

        24 ->
            missingGhostDarkness =
                value

        25 ->
            missingOffsetX =
                value

        else ->
            missingOffsetY =
                value
    }
}


// =====================================================
// STYLE TRACK GEOMETRY
// =====================================================

fun sliderTrackX(
    id: Int
): Float {

    return if (
        id in
        0..10
    ) {

        menuX +
        200f *
        uiScale

    } else {

        menuX +
        470f *
        uiScale
    }
}


fun sliderTrackY(
    id: Int
): Float {

    return if (
        id in
        0..10
    ) {

        menuY +
        (
            130f +
            id *
            34f
        ) *
        uiScale

    } else {

        menuY +
        (
            410f +
            (
                id -
                20
            ) *
            27f
        ) *
        uiScale
    }
}


fun sliderTrackW(
    id: Int
): Float {

    return if (
        id in
        0..10
    ) {

        145f *
        uiScale

    } else {

        210f *
        uiScale
    }
}


fun numericBoxX(
    id: Int
): Float {

    return sliderTrackX(id) +
        sliderTrackW(id) +
        10f *
        uiScale
}


fun numericBoxY(
    id: Int
): Float {

    return sliderTrackY(id) -
        12f *
        uiScale
}


fun numericBoxW(): Float {

    return 66f *
        uiScale
}


fun numericBoxH(): Float {

    return 26f *
        uiScale
}


// =====================================================
// FIXED-GEOMETRY SLIDER DRAG
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
        id,
        sliderMin(id) +
        (
            sliderMax(id) -
            sliderMin(id)
        ) *
        progress
    )
}


fun updateDraggedSlider() {

    val id =
        draggingSlider


    if (
        id <
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
        id,
        sliderMin(id) +
        (
            sliderMax(id) -
            sliderMin(id)
        ) *
        progress
    )
}


// =====================================================
// NUMERIC EDITOR
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


    val normalized =
        numericEditText
            .replace(
                ',',
                '.'
            )


    val parsed =
        normalized.toFloatOrNull()


    if (
        parsed !=
        null
    ) {

        setSliderValue(
            id,
            parsed
        )


        lastStatus =
            "${sliderLabel(id)} = ${cleanNumber(sliderValue(id))}"


        saveStorage()
    }


    cancelNumericEdit()
}


// =====================================================
// OPEN / CLOSE
// =====================================================

fun openManager() {

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

    windowDragging =
        false

    draggingSlider =
        -1


    keys.unlockCursor()
}


fun closeManager() {

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

    windowDragging =
        false

    draggingSlider =
        -1


    saveStorage()


    if (
        inGame &&
        !game.screenOpen()
    ) {
        keys.lockCursor()
    }
}


// =====================================================
// GRID
// =====================================================

fun gridSlotAt(
    mouseX: Float,
    mouseY: Float
): Int {

    val s =
        uiScale


    val startX =
        menuX +
        30f *
        s


    val startY =
        menuY +
        158f *
        s


    val size =
        36f *
        s


    val step =
        40f *
        s


    for (
        row in
        0 until 4
    ) {

        for (
            col in
            0 until 9
        ) {

            val x =
                startX +
                col *
                step


            val y =
                startY +
                row *
                step


            if (
                inside(
                    mouseX,
                    mouseY,
                    x,
                    y,
                    size,
                    size
                )
            ) {

                return if (
                    row ==
                    3
                ) {

                    col

                } else {

                    9 +
                    row *
                    9 +
                    col
                }
            }
        }
    }


    return -1
}


// =====================================================
// CLICK HANDLER
// =====================================================

fun handleMenuClick(
    x: Float,
    y: Float,
    button: Int
) {

    val s =
        uiScale


    // Commit old numeric edit when clicking elsewhere.
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
                numericBoxX(oldId),
                numericBoxY(oldId),
                numericBoxW(),
                numericBoxH()
            )
        ) {

            commitNumericEdit()
        }
    }


    // Color picker.
    if (
        menuTab ==
        TAB_STYLE &&
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
                62f * s,
                pickerHueY() +
                28f * s,
                62f * s,
                27f * s
            )
        ) {

            activeColor =
                COLOR_NONE

            return
        }
    }


    // Header drag.
    if (
        inside(
            x,
            y,
            menuX,
            menuY,
            menuW,
            48f * s
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


    // Tabs.
    val tabX =
        menuX +
        30f * s


    val tabY =
        menuY +
        64f * s


    val tabW =
        135f * s


    for (
        i in
        0 until 4
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
    // LAYOUT
    // =================================================

    if (
        menuTab ==
        TAB_LAYOUT
    ) {

        val bx =
            menuX +
            30f * s


        val by =
            menuY +
            112f * s


        val bw =
            130f * s


        if (
            inside(
                x,
                y,
                bx,
                by,
                bw,
                33f * s
            )
        ) {

            captureCurrentInventory()

            return
        }


        if (
            inside(
                x,
                y,
                bx +
                bw +
                8f * s,
                by,
                bw,
                33f * s
            )
        ) {

            requestSort()

            return
        }


        if (
            inside(
                x,
                y,
                bx +
                (
                    bw +
                    8f * s
                ) *
                2f,
                by,
                bw,
                33f * s
            )
        ) {

            clearLayout()

            return
        }


        val target =
            gridSlotAt(
                x,
                y
            )


        if (
            target in
            0..35
        ) {

            if (
                button ==
                1
            ) {

                savedLayout[target] =
                    ""

            } else {

                val item =
                    inventory.item(
                        Slot.inventory(
                            target
                        )
                    )


                savedLayout[target] =
                    if (
                        item.empty()
                    ) {
                        ""
                    } else {
                        item.id()
                    }
            }


            activeProfile =
                ""


            saveStorage()


            lastStatus =
                "SLOT UPDATED"


            return
        }
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
            35f * s


        val right =
            menuX +
            410f * s


        val inputY =
            menuY +
            120f * s


        if (
            inside(
                x,
                y,
                left,
                inputY,
                315f * s,
                34f * s
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
                315f * s,
                34f * s
            )
        ) {

            focusedField =
                FIELD_THEME

            return
        }


        val saveY =
            menuY +
            164f * s


        if (
            inside(
                x,
                y,
                left,
                saveY,
                150f * s,
                31f * s
            )
        ) {

            saveInventoryProfile(
                profileNameText,
                true
            )

            return
        }


        if (
            inside(
                x,
                y,
                left +
                158f * s,
                saveY,
                157f * s,
                31f * s
            )
        ) {

            saveInventoryProfile(
                profileNameText,
                false
            )

            return
        }


        if (
            inside(
                x,
                y,
                right,
                saveY,
                150f * s,
                31f * s
            )
        ) {

            saveThemeProfile(
                themeNameText
            )

            return
        }


        val invList =
            inventoryProfiles()


        val themeList =
            themeProfiles()


        val listY =
            menuY +
            210f * s


        val rowH =
            38f * s


        for (
            row in
            0 until 7
        ) {

            val invIndex =
                inventoryProfileScroll +
                row


            if (
                invIndex <
                invList.size
            ) {

                val cfg =
                    invList[
                        invIndex
                    ]


                val yy =
                    listY +
                    row *
                    (
                        rowH +
                        5f * s
                    )


                if (
                    inside(
                        x,
                        y,
                        left,
                        yy,
                        230f * s,
                        rowH
                    )
                ) {

                    selectedInventoryProfile =
                        cfg

                    return
                }


                if (
                    inside(
                        x,
                        y,
                        left +
                        238f * s,
                        yy +
                        5f * s,
                        77f * s,
                        rowH -
                        10f * s
                    )
                ) {

                    selectedInventoryProfile =
                        cfg


                    loadInventoryProfile(
                        cfg,
                        false
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


                val yy =
                    listY +
                    row *
                    (
                        rowH +
                        5f * s
                    )


                if (
                    inside(
                        x,
                        y,
                        right,
                        yy,
                        230f * s,
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
                        238f * s,
                        yy +
                        5f * s,
                        77f * s,
                        rowH -
                        10f * s
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
            522f * s


        if (
            inside(
                x,
                y,
                left,
                bottomY,
                105f * s,
                32f * s
            )
        ) {

            requestSort()

            return
        }


        if (
            inside(
                x,
                y,
                left +
                113f * s,
                bottomY,
                125f * s,
                32f * s
            )
        ) {

            loadInventoryProfile(
                selectedInventoryProfile,
                true
            )

            return
        }


        if (
            inside(
                x,
                y,
                left +
                246f * s,
                bottomY,
                90f * s,
                32f * s
            )
        ) {

            deleteInventoryProfile(
                selectedInventoryProfile
            )

            return
        }


        if (
            inside(
                x,
                y,
                right,
                bottomY,
                120f * s,
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
    // AUTOMATION
    // =================================================

    if (
        menuTab ==
        TAB_AUTOMATION
    ) {

        val leftX =
            menuX +
            40f * s


        val rightX =
            menuX +
            390f * s


        val startY =
            menuY +
            125f * s


        val cardW =
            320f * s


        val cardH =
            58f * s


        for (
            i in
            0 until 7
        ) {

            val column =
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
                    column ==
                    0
                ) {
                    leftX
                } else {
                    rightX
                }


            val yy =
                startY +
                row *
                70f * s


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

                when (i) {

                    0 ->
                        autoSort =
                            !autoSort

                    1 ->
                        inventoryLock =
                            !inventoryLock

                    2 ->
                        restoreSavedArmor =
                            !restoreSavedArmor

                    3 ->
                        autoBestArmor =
                            !autoBestArmor

                    4 ->
                        restoreOffhand =
                            !restoreOffhand

                    5 ->
                        missingOverlay =
                            !missingOverlay

                    6 ->
                        missingBlink =
                            !missingBlink
                }


                saveStorage()


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
                    9f * s,
                    sliderTrackW(id) +
                    10f * s,
                    20f * s
                )
            ) {

                beginSliderDrag(
                    id
                )

                return
            }
        }
    }


    // =================================================
    // STYLE
    // =================================================

    if (
        menuTab ==
        TAB_STYLE
    ) {

        // Numeric value boxes.
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


        // Sliders.
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


        // Colors.
        val colorY =
            menuY +
            112f * s


        val targets =
            listOf(
                COLOR_WINDOW,
                COLOR_ELEMENT,
                COLOR_ACCENT,
                COLOR_MISSING
            )


        for (
            i in
            targets.indices
        ) {

            val xx =
                menuX +
                (
                    455f +
                    i *
                    68f
                ) *
                s


            if (
                inside(
                    x,
                    y,
                    xx,
                    colorY,
                    61f * s,
                    34f * s
                )
            ) {

                openPicker(
                    targets[i]
                )

                return
            }
        }


        val optionsY =
            menuY +
            522f * s


        if (
            inside(
                x,
                y,
                menuX +
                40f * s,
                optionsY,
                112f * s,
                31f * s
            )
        ) {

            stripEnabled =
                !stripEnabled

            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                160f * s,
                optionsY,
                125f * s,
                31f * s
            )
        ) {

            stripPosition =
                (
                    stripPosition +
                    1
                ) %
                4

            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                293f * s,
                optionsY,
                140f * s,
                31f * s
            )
        ) {

            uiUseClientAccent =
                !uiUseClientAccent

            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                441f * s,
                optionsY,
                150f * s,
                31f * s
            )
        ) {

            uiAnimation =
                (
                    uiAnimation +
                    1
                ) %
                3

            return
        }


        if (
            inside(
                x,
                y,
                menuX +
                599f * s,
                optionsY,
                125f * s,
                31f * s
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

            uiElementColor =
                0xFF171F2CL.toInt()

            uiAccentColor =
                0xFF72C7FFL.toInt()

            missingColor =
                0xFFFF4F5EL.toInt()

            stripEnabled = true
            stripPosition = 0

            stripThickness = 4f
            stripInset = 7f
            stripLength = 0.78f

            cancelNumericEdit()

            activeColor =
                COLOR_NONE


            saveStorage()


            return
        }
    }
}


// =====================================================
// INPUT
// =====================================================

on<KeyEvent> { e ->

    // -------------------------------------------------
    // NUMERIC EDITOR
    // -------------------------------------------------

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


    // -------------------------------------------------
    // OPEN / CLOSE
    // -------------------------------------------------

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
            closeManager()
        } else {
            openManager()
        }


        e.cancel()

        return@on
    }


    if (
        !menuOpen
    ) {
        return@on
    }


    // -------------------------------------------------
    // PROFILE TEXT
    // -------------------------------------------------

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

        closeManager()

        e.cancel()

        return@on
    }


    if (
        e.pressed() &&
        e.key() ==
        Key.MOUSE_1
    ) {

        handleMenuClick(
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

        handleMenuClick(
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


    // Numeric field.
    if (
        numericEditId >=
        0
    ) {

        var c =
            e.character()


        if (
            c ==
            ","
        ) {
            c =
                "."
        }


        if (
            c.length ==
            1
        ) {

            val ch =
                c[0]


            val digit =
                ch in
                '0'..'9'


            val dot =
                ch ==
                '.' &&
                !numericEditText.contains(
                    "."
                )


            val minus =
                ch ==
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
                        ch
                }
            }
        }


        e.cancel()

        return@on
    }


    // Profile/theme fields.
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
// BLOCK GAME INPUT
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

            inventoryProfileScroll +=
                if (
                    e.vertical() <
                    0.0
                ) {
                    1
                } else {
                    -1
                }


            inventoryProfileScroll =
                inventoryProfileScroll
                    .coerceIn(
                        0,
                        kotlin.math.max(
                            0,
                            inventoryProfiles()
                                .size -
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
                themeProfileScroll
                    .coerceIn(
                        0,
                        kotlin.math.max(
                            0,
                            themeProfiles()
                                .size -
                            7
                        )
                    )
        }
    }


    e.cancel()
}


// =====================================================
// SLOT LOCK
// =====================================================

on<SlotClickEvent> { e ->

    if (
        !inventoryLock ||
        sortingActive ||
        !layoutExists() ||
        !inGame
    ) {
        return@on
    }


    if (
        game.screenKind() !=
        ScreenKind.INVENTORY
    ) {
        return@on
    }


    if (
        e.action() ==
        SlotAction.SWAP
    ) {

        val hotbar =
            e.button()


        if (
            hotbar in
            0..8 &&
            savedLayout[
                hotbar
            ].isNotBlank()
        ) {

            e.cancel()

            return@on
        }
    }


    val raw =
        e.slotId()


    if (
        raw <
        0
    ) {
        return@on
    }


    val playerSlot =
        container.playerSlot(
            ContainerSlot.of(
                raw
            )
        )


    if (
        !playerSlot.found()
    ) {
        return@on
    }


    val index =
        playerSlot.index()


    if (
        index in
        0..35 &&
        savedLayout[
            index
        ].isNotBlank()
    ) {

        e.cancel()
    }
}


// =====================================================
// AUTO SORT
// =====================================================

on<ClientTickEvent> {

    if (
        !inGame
    ) {

        sortingRequested =
            false

        sortingActive =
            false

        pendingAutoSortAt =
            0L

        lastTotalItems =
            0


        return@on
    }


    val total =
        totalItemCount()


    if (
        total >
        lastTotalItems &&
        autoSort &&
        layoutExists()
    ) {

        pendingAutoSortAt =
            client.millis() +
            pickupDelayMs
                .toLong()


        lastStatus =
            "ITEMS DETECTED"
    }


    lastTotalItems =
        total


    if (
        pendingAutoSortAt >
        0L &&
        client.millis() >=
        pendingAutoSortAt
    ) {

        pendingAutoSortAt =
            0L


        requestSort()
    }


    if (
        !sortingRequested
    ) {

        sortingActive =
            false


        return@on
    }


    if (
        !canModifyInventory() ||
        inventory.busy()
    ) {
        return@on
    }


    sortingActive =
        true


    if (
        !executeSortStep()
    ) {

        sortingRequested =
            false

        sortingActive =
            false

        completedSorts++


        lastStatus =
            "SORT COMPLETE"


        lastTotalItems =
            totalItemCount()


        saveStorage()
    }
}


// =====================================================
// RENDER
// =====================================================

on<Render2DEvent> { e ->

    val r =
        e.render()


    val screenW =
        r.width()


    val screenH =
        r.height()


    // =================================================
    // MISSING ITEMS IN VANILLA INVENTORY
    // =================================================

    if (
        missingOverlay &&
        inGame &&
        layoutExists() &&
        game.screenKind() ==
        ScreenKind.INVENTORY
    ) {

        val factor =
            gameSettings.scaleFactor()
                .toFloat()


        if (
            factor >
            0f
        ) {

            val guiW =
                screenW /
                factor


            val guiH =
                screenH /
                factor


            val guiLeft =
                (
                    guiW -
                    176f
                ) /
                2f +
                missingOffsetX


            val guiTop =
                (
                    guiH -
                    166f
                ) /
                2f +
                missingOffsetY


            val flags =
                missingMainFlags()


            val pulse =
                if (
                    missingBlink
                ) {

                    (
                        0.35 +
                        0.65 *
                        (
                            0.5 +
                            0.5 *
                            kotlin.math.sin(
                                client.millis()
                                    .toDouble() /
                                1000.0 *
                                missingBlinkSpeed
                                    .toDouble() *
                                6.283185307
                            )
                        )
                    ).toFloat()

                } else {

                    1f
                }


            val flash =
                (
                    pulse *
                    missingFlashIntensity
                ).coerceIn(
                    0f,
                    1f
                )


            fun ghost(
                id: String,
                gx: Float,
                gy: Float
            ) {

                val px =
                    gx *
                    factor


                val py =
                    gy *
                    factor


                val size =
                    16f *
                    factor


                r.item(
                    id,
                    px,
                    py,
                    size
                )


                r.rect(
                    px,
                    py,
                    size,
                    size,
                    Colors.rgba(
                        0,
                        0,
                        0,
                        missingGhostDarkness
                            .toInt()
                    )
                )


                r.roundedOutline(
                    px -
                    factor,
                    py -
                    factor,
                    size +
                    factor *
                    2f,
                    size +
                    factor *
                    2f,
                    2f *
                    factor,
                    kotlin.math.max(
                        1f,
                        factor
                    ),
                    Colors.fade(
                        missingColor,
                        flash
                    )
                )
            }


            for (
                index in
                0 until 36
            ) {

                if (
                    !flags[index]
                ) {
                    continue
                }


                val wanted =
                    savedLayout[index]


                if (
                    wanted.isBlank()
                ) {
                    continue
                }


                if (
                    index <
                    9
                ) {

                    ghost(
                        wanted,
                        guiLeft +
                        8f +
                        index *
                        18f,
                        guiTop +
                        142f
                    )

                } else {

                    val local =
                        index -
                        9


                    val row =
                        local /
                        9


                    val col =
                        local %
                        9


                    ghost(
                        wanted,
                        guiLeft +
                        8f +
                        col *
                        18f,
                        guiTop +
                        84f +
                        row *
                        18f
                    )
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
        760f *
        s


    menuH =
        580f *
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


    val offset =
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
                offset

            2 ->
                targetY +
                offset

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
    // BACKGROUND
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
    // STRIP
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
        "Inventory Manager",
        menuX +
        30f * s,
        menuY +
        17f * s,
        16f *
        s *
        ts,
        Colors.fade(
            theme.textPrimary(),
            alpha
        ),
        Weight.BOLD
    )


    r.text(
        if (
            activeProfile.isBlank()
        ) {
            "Custom layout"
        } else {
            "Profile: ${inventoryProfileTitle(activeProfile)}"
        },
        menuX +
        30f * s,
        menuY +
        40f * s,
        7.5f *
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
            "LAYOUT",
            "PROFILES",
            "AUTOMATION",
            "STYLE"
        )


    val tabX =
        menuX +
        30f * s


    val tabY =
        menuY +
        64f * s


    val tabW =
        135f * s


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

                    uiElementColor
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


        val fontSize =
            7.5f *
            s *
            ts


        val tw =
            r.textWidth(
                tabs[i],
                fontSize,
                Weight.SEMI_BOLD
            )


        val th =
            r.textHeight(
                fontSize,
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
            fontSize,
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
    // LAYOUT
    // =================================================

    if (
        menuTab ==
        TAB_LAYOUT
    ) {

        val buttonNames =
            listOf(
                "SAVE CURRENT",
                "SORT NOW",
                "CLEAR"
            )


        val bx =
            menuX +
            30f * s


        val by =
            menuY +
            112f * s


        val bw =
            130f * s


        for (
            i in
            buttonNames.indices
        ) {

            val xx =
                bx +
                i *
                (
                    bw +
                    8f * s
                )


            r.roundedRect(
                xx,
                by,
                bw,
                33f * s,
                8f * s,
                Colors.fade(
                    if (
                        i ==
                        1
                    ) {

                        Colors.withAlpha(
                            accent(),
                            42
                        )

                    } else {

                        uiElementColor
                    },
                    alpha
                )
            )


            val fs =
                7f *
                s *
                ts


            val tw =
                r.textWidth(
                    buttonNames[i],
                    fs,
                    Weight.SEMI_BOLD
                )


            val th =
                r.textHeight(
                    fs,
                    Weight.SEMI_BOLD
                )


            r.text(
                buttonNames[i],
                xx +
                (
                    bw -
                    tw
                ) /
                2f,
                by +
                (
                    33f *
                    s -
                    th
                ) /
                2f,
                fs,
                if (
                    i ==
                    1
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


        val missingFlags =
            missingMainFlags()


        val gridX =
            menuX +
            30f * s


        val gridY =
            menuY +
            158f * s


        val slotSize =
            36f * s


        val step =
            40f * s


        for (
            row in
            0 until 4
        ) {

            for (
                col in
                0 until 9
            ) {

                val index =
                    if (
                        row ==
                        3
                    ) {

                        col

                    } else {

                        9 +
                        row *
                        9 +
                        col
                    }


                val xx =
                    gridX +
                    col *
                    step


                val yy =
                    gridY +
                    row *
                    step


                val wanted =
                    savedLayout[
                        index
                    ]


                val current =
                    inventory.item(
                        Slot.inventory(
                            index
                        )
                    )


                val correct =
                    (
                        wanted.isNotBlank() &&
                        !current.empty() &&
                        current.id() ==
                        wanted
                    )


                r.roundedRect(
                    xx,
                    yy,
                    slotSize,
                    slotSize,
                    7f * s,
                    Colors.fade(
                        uiElementColor,
                        alpha
                    )
                )


                if (
                    wanted.isNotBlank()
                ) {

                    r.item(
                        wanted,
                        xx +
                        6f * s,
                        yy +
                        6f * s,
                        24f * s
                    )


                    if (
                        missingFlags[index]
                    ) {

                        r.rect(
                            xx +
                            5f * s,
                            yy +
                            5f * s,
                            26f * s,
                            26f * s,
                            Colors.rgba(
                                0,
                                0,
                                0,
                                105
                            )
                        )
                    }


                    r.roundedOutline(
                        xx,
                        yy,
                        slotSize,
                        slotSize,
                        7f * s,
                        1f * s,
                        Colors.fade(
                            if (
                                missingFlags[index]
                            ) {

                                missingColor

                            } else if (
                                correct
                            ) {

                                Colors.GREEN

                            } else {

                                Colors.ORANGE
                            },
                            alpha
                        )
                    )

                } else if (
                    !current.empty()
                ) {

                    r.item(
                        current,
                        xx +
                        8f * s,
                        yy +
                        8f * s,
                        20f * s
                    )
                }
            }
        }


        val sideX =
            menuX +
            430f * s


        r.text(
            "EQUIPMENT",
            sideX,
            menuY +
            118f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val names =
            listOf(
                "Helmet",
                "Chestplate",
                "Leggings",
                "Boots"
            )


        for (
            i in
            0 until 4
        ) {

            val yy =
                menuY +
                (
                    148f +
                    i *
                    50f
                ) *
                s


            r.roundedRect(
                sideX,
                yy,
                42f * s,
                42f * s,
                8f * s,
                Colors.fade(
                    uiElementColor,
                    alpha
                )
            )


            if (
                savedArmor[i]
                    .isNotBlank()
            ) {

                r.item(
                    savedArmor[i],
                    sideX +
                    7f * s,
                    yy +
                    7f * s,
                    28f * s
                )
            }


            val fs =
                8f *
                s *
                ts


            val th =
                r.textHeight(
                    fs
                )


            r.text(
                names[i],
                sideX +
                54f * s,
                yy +
                (
                    42f *
                    s -
                    th
                ) /
                2f,
                fs,
                Colors.fade(
                    theme.textSecondary(),
                    alpha
                )
            )
        }


        r.text(
            "STATUS",
            sideX,
            menuY +
            385f * s,
            7f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            )
        )


        r.text(
            lastStatus,
            sideX,
            menuY +
            408f * s,
            9f *
            s *
            ts,
            if (
                sortingActive
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


        r.text(
            "LMB slot  Save item",
            sideX,
            menuY +
            450f * s,
            7f *
            s *
            ts,
            Colors.fade(
                theme.textTertiary(),
                alpha
            )
        )


        r.text(
            "RMB slot  Clear item",
            sideX,
            menuY +
            469f * s,
            7f *
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
            35f * s


        val right =
            menuX +
            410f * s


        val inputY =
            menuY +
            120f * s


        r.text(
            "INVENTORY PROFILES",
            left,
            inputY -
            21f * s,
            8.5f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        r.text(
            "THEME PROFILES",
            right,
            inputY -
            21f * s,
            8.5f *
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
            315f * s,
            34f * s,
            8f * s,
            Colors.fade(
                uiElementColor,
                alpha
            )
        )


        r.roundedRect(
            right,
            inputY,
            315f * s,
            34f * s,
            8f * s,
            Colors.fade(
                uiElementColor,
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
                315f * s,
                34f * s,
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
                315f * s,
                34f * s,
                8f * s,
                1f * s,
                accentColor
            )
        }


        val inputFont =
            7.5f *
            s *
            ts


        val inputTextH =
            r.textHeight(
                inputFont
            )


        r.text(
            if (
                profileNameText.isBlank()
            ) {
                "Profile name..."
            } else {
                profileNameText
            },
            left +
            10f * s,
            inputY +
            (
                34f *
                s -
                inputTextH
            ) /
            2f,
            inputFont,
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
            10f * s,
            inputY +
            (
                34f *
                s -
                inputTextH
            ) /
            2f,
            inputFont,
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
            164f * s


        fun action(
            x: Float,
            width: Float,
            text: String,
            strong: Boolean
        ) {

            r.roundedRect(
                x,
                saveY,
                width,
                31f * s,
                7f * s,
                Colors.fade(
                    if (
                        strong
                    ) {
                        Colors.withAlpha(
                            accent(),
                            40
                        )
                    } else {
                        uiElementColor
                    },
                    alpha
                )
            )


            val fs =
                6.8f *
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
                x +
                (
                    width -
                    tw
                ) /
                2f,
                saveY +
                (
                    31f *
                    s -
                    th
                ) /
                2f,
                fs,
                if (
                    strong
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


        action(
            left,
            150f * s,
            "SAVE CURRENT",
            true
        )


        action(
            left +
            158f * s,
            157f * s,
            "SAVE TEMPLATE",
            false
        )


        action(
            right,
            150f * s,
            "SAVE THEME",
            true
        )


        val invList =
            inventoryProfiles()


        val themeList =
            themeProfiles()


        val listY =
            menuY +
            210f * s


        val rowH =
            38f * s


        for (
            row in
            0 until 7
        ) {

            val invIndex =
                inventoryProfileScroll +
                row


            if (
                invIndex <
                invList.size
            ) {

                val cfg =
                    invList[
                        invIndex
                    ]


                val yy =
                    listY +
                    row *
                    (
                        rowH +
                        5f * s
                    )


                val selected =
                    selectedInventoryProfile ==
                    cfg


                r.roundedRect(
                    left,
                    yy,
                    315f * s,
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

                            uiElementColor
                        },
                        alpha
                    )
                )


                if (
                    activeProfile ==
                    cfg
                ) {

                    r.roundedRect(
                        left +
                        6f * s,
                        yy +
                        8f * s,
                        3f * s,
                        rowH -
                        16f * s,
                        2f * s,
                        accentColor
                    )
                }


                val fs =
                    7.8f *
                    s *
                    ts


                val th =
                    r.textHeight(
                        fs,
                        Weight.SEMI_BOLD
                    )


                r.text(
                    inventoryProfileTitle(
                        cfg
                    ),
                    left +
                    16f * s,
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


                val loadY =
                    yy +
                    5f * s


                val loadH =
                    rowH -
                    10f * s


                r.roundedRect(
                    left +
                    238f * s,
                    loadY,
                    77f * s,
                    loadH,
                    6f * s,
                    Colors.fade(
                        Colors.withAlpha(
                            accent(),
                            36
                        ),
                        alpha
                    )
                )


                val loadFs =
                    6.8f *
                    s *
                    ts


                val loadTh =
                    r.textHeight(
                        loadFs,
                        Weight.BOLD
                    )


                r.text(
                    "LOAD",
                    left +
                    254f * s,
                    loadY +
                    (
                        loadH -
                        loadTh
                    ) /
                    2f,
                    loadFs,
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


                val yy =
                    listY +
                    row *
                    (
                        rowH +
                        5f * s
                    )


                val selected =
                    selectedThemeProfile ==
                    cfg


                r.roundedRect(
                    right,
                    yy,
                    315f * s,
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

                            uiElementColor
                        },
                        alpha
                    )
                )


                val fs =
                    7.8f *
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
                    16f * s,
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


                val loadY =
                    yy +
                    5f * s


                val loadH =
                    rowH -
                    10f * s


                r.roundedRect(
                    right +
                    238f * s,
                    loadY,
                    77f * s,
                    loadH,
                    6f * s,
                    Colors.fade(
                        Colors.withAlpha(
                            accent(),
                            36
                        ),
                        alpha
                    )
                )


                val loadFs =
                    6.8f *
                    s *
                    ts


                val loadTh =
                    r.textHeight(
                        loadFs,
                        Weight.BOLD
                    )


                r.text(
                    "LOAD",
                    right +
                    254f * s,
                    loadY +
                    (
                        loadH -
                        loadTh
                    ) /
                    2f,
                    loadFs,
                    accentColor,
                    Weight.BOLD
                )
            }
        }
    }


    // =================================================
    // AUTOMATION
    // =================================================

    if (
        menuTab ==
        TAB_AUTOMATION
    ) {

        val leftX =
            menuX +
            40f * s


        val rightX =
            menuX +
            390f * s


        val startY =
            menuY +
            125f * s


        val cardW =
            320f * s


        val cardH =
            58f * s


        val titles =
            listOf(
                "Auto sort",
                "Lock saved slots",
                "Restore saved armor",
                "Use best armor",
                "Restore offhand",
                "Missing-item overlay",
                "Blink missing items"
            )


        val descriptions =
            listOf(
                "Restores the saved layout after new items are collected.",
                "Stops manual clicks from moving protected saved slots.",
                "Returns the exact saved armor pieces.",
                "Uses stronger armor when no exact saved set controls it.",
                "Returns the saved offhand item.",
                "Shows ghost items in missing Minecraft inventory slots.",
                "Makes the missing-item highlight pulse."
            )


        val values =
            listOf(
                autoSort,
                inventoryLock,
                restoreSavedArmor,
                autoBestArmor,
                restoreOffhand,
                missingOverlay,
                missingBlink
            )


        for (
            i in
            titles.indices
        ) {

            val column =
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
                    column ==
                    0
                ) {
                    leftX
                } else {
                    rightX
                }


            val yy =
                startY +
                row *
                70f * s


            r.roundedRect(
                xx,
                yy,
                cardW,
                cardH,
                9f * s,
                Colors.fade(
                    uiElementColor,
                    alpha
                )
            )


            r.text(
                titles[i],
                xx +
                13f * s,
                yy +
                9f * s,
                8.2f *
                s *
                ts,
                Colors.fade(
                    theme.textPrimary(),
                    alpha
                ),
                Weight.SEMI_BOLD
            )


            r.text(
                descriptions[i],
                xx +
                13f * s,
                yy +
                30f * s,
                5.9f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )


            val toggleX =
                xx +
                cardW -
                45f * s


            val toggleY =
                yy +
                (
                    cardH -
                    18f *
                    s
                ) /
                2f


            r.roundedRect(
                toggleX,
                toggleY,
                34f * s,
                18f * s,
                9f * s,
                Colors.fade(
                    if (
                        values[i]
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
                    values[i]
                ) {
                    toggleX +
                    25f * s
                } else {
                    toggleX +
                    9f * s
                },
                toggleY +
                9f * s,
                6f * s,
                Colors.fade(
                    theme.textPrimary(),
                    alpha
                )
            )
        }


        r.text(
            "FINE TUNING",
            rightX,
            menuY +
            350f * s,
            7f *
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

            val y =
                sliderTrackY(id)


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


            r.text(
                sliderLabel(id),
                rightX,
                y -
                13f * s,
                6.4f *
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
                    uiElementColor,
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
        }
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
            40f * s,
            menuY +
            106f * s,
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


            r.text(
                sliderLabel(id),
                menuX +
                40f * s,
                y -
                12f * s,
                6.5f *
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
                    uiElementColor,
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


            // Numeric text field.
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
                            uiElementColor,
                            accent(),
                            0.16f
                        )

                    } else {

                        uiElementColor
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
                6.6f *
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


        r.text(
            "COLORS",
            menuX +
            455f * s,
            menuY +
            91f * s,
            8f *
            s *
            ts,
            Colors.fade(
                theme.textSecondary(),
                alpha
            ),
            Weight.SEMI_BOLD
        )


        val targets =
            listOf(
                COLOR_WINDOW,
                COLOR_ELEMENT,
                COLOR_ACCENT,
                COLOR_MISSING
            )


        val labels =
            listOf(
                "WIN",
                "PANEL",
                "ACCENT",
                "MISS"
            )


        val colors =
            listOf(
                uiWindowColor,
                uiElementColor,
                uiAccentColor,
                missingColor
            )


        val colorY =
            menuY +
            112f * s


        for (
            i in
            targets.indices
        ) {

            val xx =
                menuX +
                (
                    455f +
                    i *
                    68f
                ) *
                s


            r.roundedRect(
                xx,
                colorY,
                61f * s,
                34f * s,
                7f * s,
                Colors.fade(
                    uiElementColor,
                    alpha
                )
            )


            r.circle(
                xx +
                12f * s,
                colorY +
                17f * s,
                6f * s,
                colors[i]
            )


            val fs =
                5f *
                s *
                ts


            val th =
                r.textHeight(
                    fs,
                    Weight.SEMI_BOLD
                )


            r.text(
                labels[i],
                xx +
                22f * s,
                colorY +
                (
                    34f *
                    s -
                    th
                ) /
                2f,
                fs,
                if (
                    activeColor ==
                    targets[i]
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
                31f * s,
                7f *
                s *
                ts,
                Colors.fade(
                    theme.textPrimary(),
                    alpha
                ),
                Weight.SEMI_BOLD
            )


            r.roundedRect(
                px +
                pw -
                62f * s,
                pickerHueY() +
                28f * s,
                62f * s,
                27f * s,
                6f * s,
                Colors.fade(
                    uiElementColor,
                    alpha
                )
            )


            val fs =
                6.5f *
                s *
                ts


            val th =
                r.textHeight(
                    fs,
                    Weight.BOLD
                )


            r.text(
                "DONE",
                px +
                pw -
                48f * s,
                pickerHueY() +
                28f * s +
                (
                    27f *
                    s -
                    th
                ) /
                2f,
                fs,
                accentColor,
                Weight.BOLD
            )

        } else {

            r.text(
                "Click a color swatch",
                menuX +
                470f * s,
                menuY +
                180f * s,
                7f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )


            r.text(
                "Click any number to type an exact value.",
                menuX +
                470f * s,
                menuY +
                206f * s,
                6.3f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )


            r.text(
                "Example: Menu scale = 0.93 or 1.17",
                menuX +
                470f * s,
                menuY +
                228f * s,
                6.3f *
                s *
                ts,
                Colors.fade(
                    theme.textTertiary(),
                    alpha
                )
            )
        }


        val optionsY =
            menuY +
            522f * s


        val options =
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
                140f,
                150f,
                125f
            )


        var xx =
            menuX +
            40f * s


        for (
            i in
            options.indices
        ) {

            val width =
                widths[i] *
                s


            r.roundedRect(
                xx,
                optionsY,
                width,
                31f * s,
                7f * s,
                Colors.fade(
                    uiElementColor,
                    alpha
                )
            )


            val fs =
                6.1f *
                s *
                ts


            val tw =
                r.textWidth(
                    options[i],
                    fs,
                    Weight.SEMI_BOLD
                )


            val th =
                r.textHeight(
                    fs,
                    Weight.SEMI_BOLD
                )


            r.text(
                options[i],
                xx +
                (
                    width -
                    tw
                ) /
                2f,
                optionsY +
                (
                    31f *
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


            xx +=
                width +
                8f * s
        }
    }


    r.text(
        "Drag header to move  •  ESC closes  •  settings save automatically",
        menuX +
        30f * s,
        menuY +
        menuH -
        18f * s,
        6.2f *
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


    sortingRequested =
        false

    sortingActive =
        false

    pendingAutoSortAt =
        0L


    menuOpen =
        false

    menuAlpha =
        0f

    lastRenderMs =
        0L


    activeColor =
        COLOR_NONE

    numericEditId =
        -1


    lastTotalItems =
        if (
            inGame
        ) {
            totalItemCount()
        } else {
            0
        }


    lastStatus =
        if (
            layoutExists()
        ) {
            "READY"
        } else {
            "SAVE A LAYOUT"
        }
}


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


    sortingRequested =
        false

    sortingActive =
        false

    pendingAutoSortAt =
        0L


    saveStorage()


    if (
        inGame &&
        !game.screenOpen()
    ) {
        keys.lockCursor()
    }
}