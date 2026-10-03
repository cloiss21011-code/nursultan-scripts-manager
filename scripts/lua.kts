name("manager")
description("менеджер аккаунтов")

font("vasnarrow", "vasnarrow.ttf")

val eye = shader("vas_eye9", """
    #version 330
    in vec2 in_uv;
    out vec4 out_color;
    uniform vec4 u_rect;
    void main() {
        float aspect = u_rect.z / max(u_rect.w, 1.0);
        vec2 pc = in_uv - vec2(0.5);
        vec2 q = vec2(pc.x * aspect, pc.y);
        float halfW = 1.0;
        float halfH = 0.365;
        float ax = abs(q.x) / halfW;
        if (ax > 1.0) discard;
        float base = max(0.0, 1.0 - ax * ax);
        float top = halfH * pow(base, 0.95);
        float d = abs(q.y) - top;
        float px = max(u_rect.w, 1.0);
        float aa = 1.2 / px;
        float outlineQ = 3.0 / px;
        if (d > outlineQ + aa) discard;
        float ringQ = 2.8 / px;
        float gapQ = 0.8 / px;
        float irisR = halfH - ringQ - gapQ;
        float dotR = irisR * 0.09;
        float irisD = length(q);
        float ringOuter = irisR + ringQ;
        float insideRing = 1.0 - smoothstep(ringOuter - aa, ringOuter + aa, irisD);
        float insideIris = 1.0 - smoothstep(irisR - aa, irisR + aa, irisD);
        float dotMask = 1.0 - smoothstep(dotR - aa, dotR + aa, irisD);
        vec3 white = vec3(0.85);
        vec3 gray = vec3(0.42);
        vec3 black = vec3(0.0);
        vec3 col = gray;
        col = mix(col, white, insideRing);
        col = mix(col, black, insideIris);
        col = mix(col, white, dotMask);
        float outlineMask = smoothstep(-aa, aa, d);
        col = mix(col, white, outlineMask);
        float alpha = 1.0 - smoothstep(outlineQ - aa, outlineQ + aa, d);
        if (alpha < 0.02) discard;
        out_color = vec4(col, alpha);
    }
""")

val bootTimer = timer()
val fadeTimer = timer()

val hudModules = listOf(
    "Hotkeys",
    "Staff Online",
    "Cooldowns",
    "Item Binds",
    "Logo",
    "GPS",
    "Target Info",
    "Inventory",
    "Potions",
    "Notifications"
)
var savedHud = mutableListOf<String>()

val bootLines = listOf(
    "Booting Windows .....",
    "Boot error: 0x03527737",
    "Boot error: 0x0266712",
    "Boot error: 0x02897593",
    "Boot error: 0x01447812",
    "Boot error: 0x0150974",
    "Boot error: 0x03873700",
    "Boot error: 0x0700882",
    "Boot error: 0x03803618",
    "Memory section at address 0x0424* is locked!",
    "Service VXCryptor started.",
    "",
    "* Windows blocked!"
)

val scytheArt = listOf(
    "               ...",
    "             ;::::;",
    "           ;::::; :;",
    "         ;:::::'   :;",
    "        ;:::::;     ;.",
    "       ,:::::'       ;           OOO\\",
    "       ::::::;       ;          OOOOO\\",
    "       ;:::::;       ;         OOOOOOOO",
    "      ,;::::::;     ;'         / OOOOOOO",
    "    ;:::::::::`. ,,,;.        /  / DOOOOOO",
    "  .';:::::::::::::::::;,     /  /     DOOOO",
    " ,::::::;::::::;;;;::::;,   /  /        DOOO",
    ";`::::::`'::::::;;;::::: ,#/  /          DOOO",
    ":`:::::::`;::::::;;::: ;::#  /            DOOO",
    "::`:::::::`;:::::::: ;::::# /              DOO",
    "`:`:::::::`;:::::: ;::::::#/               DOO",
    " :::`:::::::`;; ;:::::::::##                OO",
    " ::::`:::::::`;::::::::;:::#                OO",
    " `:::::`::::::::::::;'`:;::#                O",
    "  `:::::`::::::::;' /  / `:#",
    "   ::::::`:::::;'  /  /   `#"
)

val bodyText = "Внимание! Доступ к данным временно ограничен защитным модулем. Для продолжения работы введите персональный код разблокировки в поле ниже и нажмите Enter. Не пытайтесь обойти проверку."

var exitCode = "00000000000000"
var typed = ""
var showError = false
var errAt = 0L
var unlocked = false
var pcId = "PC - 00000000000000"
var started = false
var fsAt = 0L

val lineStepMs = 100L
val whiteHoldMs = 500L
val redAtMs = bootLines.size * lineStepMs + whiteHoldMs
val redBgAtMs = redAtMs + 380L
val blueAtMs = redBgAtMs + 1000L
val blackHoldMs = 1000L
val errHoldMs = 1000L
val armDelayMs = 5000L

fun inBlue(): Boolean = started && !unlocked && bootTimer.elapsedMillis() >= blueAtMs

fun looksFullscreen(w: Float, h: Float): Boolean {
    val iw = w.toInt()
    val ih = h.toInt()
    if (iw == 1920 && ih == 1080) return true
    if (iw == 2560 && ih == 1440) return true
    if (iw == 1366 && ih == 768) return true
    if (iw == 1600 && ih == 900) return true
    if (iw == 1280 && ih == 720) return true
    if (iw == 2560 && ih == 1080) return true
    if (iw == 3440 && ih == 1440) return true
    if (iw == 3840 && ih == 2160) return true
    if (iw == 1536 && ih == 864) return true
    if (iw == 1440 && ih == 900) return true
    if (iw == 1680 && ih == 1050) return true
    if (iw == 1920 && ih == 1200) return true
    return false
}

fun genCode(): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    val rnd = kotlin.random.Random.Default
    val sb = StringBuilder()
    repeat(14) { sb.append(chars[rnd.nextInt(chars.length)]) }
    return sb.toString()
}

fun nowTime(): String {
    return try {
        java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
    } catch (e: Exception) {
        "--:--:--"
    }
}

fun ruToEn(c: Char): Char {
    val ru = "йцукенгшщзхъфывапролджэячсмитьбю"
    val en = "qwertyuiop[]asdfghjkl;'zxcvbnm,."
    val i = ru.indexOf(c)
    if (i >= 0) return en[i]
    return c
}

fun hideHudOnce() {
    savedHud.clear()
    for (name in hudModules) {
        try {
            val mod = client.modules().find(name)
            if (mod != null && mod.enabled()) {
                savedHud.add(name)
                mod.setEnabled(false)
            }
        } catch (e: Exception) {
        }
    }
}

onEnable {
    bootTimer.reset()
    typed = ""
    showError = false
    unlocked = false
    started = false
    fsAt = 0L
    exitCode = genCode()
    pcId = "PC - " + exitCode
}

onDisable {
    for (name in savedHud) {
        try {
            client.modules().find(name)?.setEnabled(true)
        } catch (e: Exception) {
        }
    }
    savedHud.clear()
}

on<CharEvent> { e ->
    if (!started) return@on
    if (!inBlue()) {
        e.cancel()
        return@on
    }
    val ch = e.character()
    if (ch.length == 1 && typed.length < 24) {
        val c = ruToEn(ch[0].lowercaseChar())
        if (c in '0'..'9' || c in 'a'..'z') {
            typed += c
            showError = false
        }
    }
    e.cancel()
}

on<KeyEvent> { e ->
    if (!started) return@on
    val k = e.key()
    if (inBlue()) {
        if (k == Key.BACKSPACE && (e.pressed() || e.action() == KeyAction.REPEAT)) {
            if (typed.isNotEmpty()) typed = typed.dropLast(1)
            showError = false
            e.cancel()
            return@on
        }
        if ((k == Key.ENTER || k == Key.KP_ENTER) && e.pressed()) {
            if (typed == exitCode) {
                unlocked = true
                typed = ""
                fadeTimer.reset()
            } else {
                typed = ""
                showError = true
                errAt = client.millis()
            }
            e.cancel()
            return@on
        }
    }
    if (bind.bound() && e.matches(bind.key())) return@on
    e.cancel()
}

on<Render2DEvent> { e ->
    val r = e.render()
    val w = e.width()
    val h = e.height()
    if (w <= 0f || h <= 0f) return@on

    val m = minOf(w, h)

    if (!started) {
        if (looksFullscreen(w, h)) {
            if (fsAt == 0L) fsAt = client.millis()
            if (client.millis() - fsAt >= armDelayMs) {
                started = true
                bootTimer.reset()
                hideHudOnce()
            }
        } else {
            fsAt = 0L
            r.rect(0f, 0f, w, h, Colors.rgba(0, 0, 0, 150))
            val msg = "Пожалуйста, нажмите F11 для продолжения"
            val ms = m * 0.028f
            val mw = r.textWidth(msg, ms, "jetbrains-mono", Weight.BOLD)
            r.text(msg, (w - mw) * 0.5f, h * 0.5f, ms, Colors.WHITE, "jetbrains-mono", Weight.BOLD)
        }
        return@on
    }

    if (unlocked) {
        r.rect(0f, 0f, w, h, Colors.BLACK)
        if (fadeTimer.elapsedMillis() >= blackHoldMs) {
            enabled = false
        }
        return@on
    }

    val elapsed = bootTimer.elapsedMillis()

    if (elapsed >= blueAtMs) {
        val t = elapsed - blueAtMs
        val showWindow = t >= 500L
        val showInput = t >= 800L

        val bgBlue = Colors.rgba(0, 0, 255, 255)
        val darkBlue = Colors.rgba(0, 0, 140, 255)
        val white = Colors.WHITE
        val black = Colors.BLACK
        r.rect(0f, 0f, w, h, bgBlue)

        val mono = "jetbrains-mono"
        val winW = w * 0.62f
        val winH = h * 0.32f
        val winX = (w - winW) * 0.5f
        val winY = (h - winH) * 0.5f
        val pad = m * 0.012f

        val title = "Nursultan files зашифрованы"
        val titleSize = m * 0.024f
        val titleW = r.textWidth(title, titleSize, mono, Weight.BOLD) + pad * 2f
        val titleH = r.textHeight(titleSize, mono, Weight.BOLD) + pad * 0.9f
        val titleX = (w - titleW) * 0.5f
        val titleY = winY - titleH - m * 0.010f

        r.rect(titleX, titleY, titleW, titleH, white)
        r.text(title, titleX + pad, titleY + pad * 0.35f, titleSize, black, mono, Weight.BOLD)
        if (!showWindow) return@on

        val headH = m * 0.026f
        val bodySize = m * 0.014f
        val bodyLineH = r.textHeight(bodySize, mono, Weight.REGULAR) * 1.25f
        val midH = m * 0.032f
        val inputH = m * 0.048f

        r.rect(winX, winY, winW, winH, bgBlue)
        r.outline(winX, winY, winW, winH, 1f, white)
        r.rect(winX, winY, winW, headH, darkBlue)
        r.text("create by @alochnostb", winX + pad * 0.6f, winY + headH * 0.22f, bodySize * 0.8f, Colors.rgba(200, 200, 220, 255), mono, Weight.REGULAR)

        val words = bodyText.split(" ")
        val wrapped = mutableListOf<String>()
        var cur = ""
        for (word in words) {
            val test = if (cur.isEmpty()) word else cur + " " + word
            if (r.textWidth(test, bodySize, mono, Weight.REGULAR) <= winW - pad * 2f) {
                cur = test
            } else {
                if (cur.isNotEmpty()) wrapped.add(cur)
                cur = word
            }
        }
        if (cur.isNotEmpty()) wrapped.add(cur)
        var ty = winY + headH + pad * 0.7f
        for (line in wrapped) {
            r.text(line, winX + pad, ty, bodySize, white, mono, Weight.REGULAR)
            ty += bodyLineH
        }
        if (!showInput) return@on

        val bottomPad = m * 0.035f
        val inY = winY + winH - bottomPad - inputH
        val midY = inY - pad * 0.5f - midH
        r.rect(winX, midY, winW, midH, darkBlue)
        val midLabel = "Введите код"
        val midW = r.textWidth(midLabel, bodySize, mono, Weight.REGULAR)
        r.text(midLabel, winX + (winW - midW) * 0.5f, midY + midH * 0.22f, bodySize, white, mono, Weight.REGULAR)
        val inX = winX + pad * 0.5f
        val inW = winW - pad
        r.rect(inX, inY, inW, inputH, white)
        val blink = (client.millis() / 450L) % 2L == 0L
        val dots = "•".repeat(typed.length) + if (blink) "▌" else ""
        val dotsSize = bodySize * 1.3f
        val dotsW = r.textWidth(dots, dotsSize, mono, Weight.BOLD)
        val dotsH = r.textHeight(dotsSize, mono, Weight.BOLD)
        r.text(dots, inX + (inW - dotsW) * 0.5f, inY + (inputH - dotsH) * 0.5f, dotsSize, black, mono, Weight.BOLD)
        val footSize = m * 0.016f
        val footY = winY + winH + pad * 0.6f
        r.text(pcId, winX, footY, footSize, white, mono, Weight.REGULAR)
        val clock = nowTime()
        val clockW = r.textWidth(clock, footSize, mono, Weight.REGULAR)
        r.text(clock, winX + winW - clockW, footY, footSize, white, mono, Weight.REGULAR)
        if (showError) {
            if (client.millis() - errAt >= errHoldMs) {
                showError = false
            } else {
                val errText = "Неверный код"
                val errSize = m * 0.030f
                val errPad = m * 0.014f
                val errW = r.textWidth(errText, errSize, mono, Weight.BOLD) + errPad * 2f
                val errH = r.textHeight(errSize, mono, Weight.BOLD) + errPad * 1.2f
                val errX = (w - errW) * 0.5f
                val errY = (h - errH) * 0.5f - m * 0.06f
                r.rect(errX, errY, errW, errH, Colors.rgba(255, 0, 0, 255))
                r.outline(errX, errY, errW, errH, 1f, Colors.WHITE)
                r.text(errText, errX + errPad, errY + errPad * 0.5f, errSize, Colors.WHITE, mono, Weight.BOLD)
            }
        }
        return@on
    }

    if (elapsed >= redBgAtMs) {
        r.rect(0f, 0f, w, h, Colors.rgba(200, 0, 0, 255))
        val artSize = m * 0.022f
        val artLineH = r.textHeight(artSize, "jetbrains-mono", Weight.BOLD) * 1.0f
        val ax = m * 0.045f
        var ay = m * 0.035f
        for (line in scytheArt) {
            r.text(line, ax, ay, artSize, Colors.WHITE, "jetbrains-mono", Weight.BOLD)
            ay += artLineH
        }
        return@on
    }

    r.rect(0f, 0f, w, h, Colors.BLACK)

    val eyeW = m * 0.23f
    val eyeH = eyeW * 0.365f
    val textSize = m * 0.044f
    val gap = m * 0.010f
    val label = "ВАС ЗАМЕТИЛИ"
    val tw = r.textWidth(label, textSize, "vasnarrow", Weight.REGULAR)
    val textH = r.textHeight(textSize, "vasnarrow", Weight.REGULAR)
    val totalH = eyeH + gap + textH

    val eyeX = (w - eyeW) * 0.5f
    val eyeY = (h - totalH) * 0.5f
    r.shader(eye, eyeX, eyeY, eyeW, eyeH)

    val tx = (w - tw) * 0.5f
    val ty = eyeY + eyeH + gap
    val dirtyWhite = Colors.rgba(216, 216, 216, 255)
    r.text(label, tx, ty, textSize, dirtyWhite, "vasnarrow", Weight.REGULAR)

    val isRed = elapsed >= redAtMs
    val col = if (isRed) Colors.rgba(255, 32, 32, 255) else dirtyWhite
    val monoSize = m * 0.020f
    val lineH = r.textHeight(monoSize, "jetbrains-mono", Weight.BOLD) * 1.02f
    val bx = m * 0.018f
    var by = m * 0.018f

    val count = ((elapsed / lineStepMs).toInt() + 1).coerceIn(0, bootLines.size)
    for (i in 0 until count) {
        val line = bootLines[i]
        if (line.isNotEmpty()) r.text(line, bx, by, monoSize, col, "jetbrains-mono", Weight.BOLD)
        by += lineH
    }
}
