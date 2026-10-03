name("Formatify")
description("Добавляет цвет и оформление к каждому исходящему сообщению")
requireApi(2)

val specifiedFormat = input(
    "Коды форматирования",
    "&f&l",
    "Пример - &f&l / §f§l / fl"
).id("formatify_codes")

val randomFormatting = checkBox("Рандомное форматирование", false)
    .id("formatify_random")

val validCodes = "0123456789abcdefklmnor"
val randomColors = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
val randomStyles = arrayOf(null, 'l', 'm', 'n', 'o')

var resentMessage: String? = null
var lastInvalidValue: String? = null

fun normalizeFormat(value: String): String? {
    val compact = value.trim().replace(" ", "").lowercase()
    if (compact.isEmpty()) return ""

    val withAmpersands = compact.replace('§', '&')
    if ('&' !in withAmpersands) {
        if (!withAmpersands.all { it in validCodes }) return null
        return withAmpersands.map { "&$it" }.joinToString("")
    }

    if (withAmpersands.length % 2 != 0) return null
    val result = StringBuilder(withAmpersands.length)
    var index = 0
    while (index < withAmpersands.length) {
        if (withAmpersands[index] != '&') return null
        val code = withAmpersands[index + 1]
        if (code !in validCodes) return null
        result.append('&').append(code)
        index += 2
    }
    return result.toString()
}

fun randomFormat(): String {
    val color = randomColors.random()
    val style = randomStyles.random()
    return if (style == null) "&$color" else "&$color&$style"
}

fun selectedFormat(): String? = if (randomFormatting.value()) {
    randomFormat()
} else {
    normalizeFormat(specifiedFormat.value())
}

fun formattedMessage(message: String, format: String): String {
    return if (message.startsWith("!")) {
        "!" + format + message.substring(1)
    } else {
        format + message
    }
}

val previewButton = button("Показать пример") {
    val format = selectedFormat()
    if (format == null) {
        chat.print(text.legacy("§cНекорректные коды. Используй &f&l, §f§l или fl."))
        return@button
    }

    val preview = format.replace('&', '§') + "Пример текста"
    chat.printPrefixed("Formatify", text.legacy("§7Пример: §r$preview"))
}

on<PacketSendEvent> { event ->
    val packet = event.packet()
    if (packet !is C2SChatMessagePacket) return@on

    val message = packet.chatMessage()
    if (resentMessage == message) {
        resentMessage = null
        return@on
    }

    if (message.startsWith("/") || message.startsWith(".")) return@on

    val format = selectedFormat()
    if (format == null) {
        val badValue = specifiedFormat.value()
        if (lastInvalidValue != badValue) {
            lastInvalidValue = badValue
            onClientThread {
                chat.print(text.legacy("§cFormatify: некорректные коды; сообщение отправлено без оформления."))
            }
        }
        return@on
    }

    lastInvalidValue = null
    if (format.isEmpty()) return@on

    val formatted = formattedMessage(message, format)
    event.cancel()

    onClientThread {
        resentMessage = formatted
        chat.sendToServer(formatted)
        if (resentMessage == formatted) resentMessage = null
    }
}

onDisable {
    resentMessage = null
    lastInvalidValue = null
}
