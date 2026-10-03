import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

requireApi(2)

name("ArrayList")
description("Список включенных функций")

// ───────────── настройки ─────────────
// «Color» всегда включён: выключить нельзя, любая попытка тут же возвращается в true
val colorCb = checkBox("Color", true)
val customA = colorPicker(colorCb, "Custom Color 1", 0xFF817BFFL)
val customB = colorPicker(colorCb, "Custom Color 2", 0xFFF5EBFFL)
colorCb.onChange(java.util.function.Consumer<Boolean> { v -> if (!v) colorCb.value(true) })
colorCb.value(true)

val scaleMode by selectable("Scale", "100%", "125%", "150%", "200%", selected = "100%")
val glow by checkBox("Glow", true)
val waveSpeed by slider("Wave speed", 3.5f, 0f, 5f, 0.1f).postfix(Postfixes.MULTIPLIER)
val stiffness by slider("Stiffness", 400f, 60f, 400f, 5f)
val categories by combo("Categories", "Combat", "Movement", "Visuals", "Player", "Misc", selected = listOf("Combat", "Movement", "Visuals", "Player", "Misc"))

// ───────────── состояние ─────────────
class Palette(val a: Int, val b: Int, val bg: Int)

class Row(val key: String) {
    var label = key
    var mLabel = ""
    var mSize = -1f
    var textW = 0f

    var target = 0f      // 1 = включён, 0 = выключен
    var appear = 0f      // пружина появления (перелетает за 1)
    var appearV = 0f
    var targetY = 0f
    var y = 0f           // пружина позиции по вертикали
    var yV = 0f
    var fresh = true
}

// всё, что нужно для отрисовки строки за один кадр
class DrawItem(
    val row: Row, val x0: Float, val y0: Float, val w: Float,
    val a: Float, val col: Int, val colNext: Int
)

val rows = mutableMapOf<String, Row>()
var lastNanos = 0L

// позиция: расстояние от края экрана в GUI-единицах, край = левый или правый
var ax = storage.getDouble("x", 4.0).toFloat()
var ay = storage.getDouble("y", 4.0).toFloat()
var alignRight = storage.getBoolean("right", false)

var dragging = false
var wasDown = false
var grabDX = 0f
var grabDY = 0f
var boxW = 0f
var boxH = 0f

// ───────────── Categories (перенесено из ArrayList__8_.kts) ─────────────
private fun normalizeName(s: String): String {
    val sb = StringBuilder(s.length)
    for (ch in s.lowercase()) {
        if ((ch in 'a'..'z') || (ch in 'а'..'я') || ch == 'ё') {
            sb.append(if (ch == 'ё') 'е' else ch)
        } else if (ch in '0'..'9') {
            sb.append(ch)
        }
    }
    return sb.toString()
}

private val COMBAT_EXACT = setOf(
    "attackaura", "killaura", "aura", "aimbot", "bowaim", "bowaimbot", "aimassist", "antibot",
    "autoarmor", "autoexplosion", "autocrystal", "crystalaura", "autopotion", "autobuff",
    "autoswap", "autototem", "smarttotem", "autoeatgapple", "autogapple", "autoapple",
    "autoinvisible", "hitboxes", "hitbox", "silenthitbox", "itemrelease", "nofrienddamage",
    "novelocity", "velocity", "antiknockback", "antikb", "packetcriticals", "criticals",
    "crits", "rwgodmode", "godmode", "superprojectile", "throwsync", "triggerbot", "trigger",
    "webtrap", "wtap", "sprinttap", "reach", "shieldbreaker", "shielddisabler",
    "superknockback", "superkb", "tpaura", "potionspoof", "fastbow", "backtrack",
    "autoclicker", "clicker", "anchoraura", "autoanchor", "bedaura", "autobed",
    "maceaura", "automace", "pistonaura", "autopiston", "autoweapon", "webaura",
    "autoweb", "creeperaura", "tntaura", "antiattack", "hitdelay", "surround",
    "selftrap", "holesnap", "autooffhand", "offhand", "antifire", "antiaim", "tapemouse",
    "combathud", "smartarmor", "smoothaim",
    "киллаура", "килаура", "аура", "триггер", "тригер", "триггербот", "велосити", "антикб",
    "отдача", "антиотдача", "суперкб", "суперотдача", "антибот", "боты", "криты", "критикалс",
    "критическиеудары", "дальность", "дистанция", "рич", "хитбоксы", "хитбокс", "автоброня",
    "броня", "автототем", "тотем", "оффхенд", "втораярука", "леваярука", "щитолом",
    "ломательщита", "щит", "автозелья", "автозелье", "зелья", "автобафф", "бафф", "баффы",
    "гэпл", "гэплы", "автогэпл", "автояблоко", "яблоко", "быстрыйлук", "лук", "арбалет",
    "бэктрек", "бектрек", "бэктрэк", "аим", "аимбот", "доводка", "наводка", "ассист",
    "кликер", "автокликер", "кристаллы", "кристаллаура", "анкор", "якорь", "анкораура",
    "кровать", "кровати", "кроватьаура", "булава", "мейс", "мейсаура", "поршни", "поршень",
    "паутина", "автопаутина", "бой", "свап", "автосвап", "смарттотем", "сайлентхитбокс",
    "антиаим", "втап", "спринттап", "годмод", "суперснаряд", "синхронизацияброска",
    "вебтрап", "ловушкавпаутину", "безуронадрузьям", "автовыстрел", "автоневидимость",
    "автовзрыв", "взрывкристаллов", "автокликмыши"
)

private val MOVEMENT_EXACT = setOf(
    "airjump", "airstuck", "autojump", "autopilot", "blink", "elytraitemflight",
    "elytratarget", "flight", "fly", "guimove", "invmove", "inventorymove",
    "highjump", "movementhelper", "nojumpboost", "nojumpdelay", "noslow",
    "noslowdown", "phase", "speed", "speeds", "sprint", "autosprint", "strafe",
    "targetstrafe", "superfirework", "timer", "wallclimb", "waterspeed", "jesus",
    "waterwalk", "spider", "step", "autostep", "longjump", "doublejump", "parkour",
    "safewalk", "noweb", "glide", "boatfly", "elytrafly", "elytra", "fastladder",
    "noclip", "teleport", "clicktp", "coordtp", "bhop", "bunnyhop", "airwalk",
    "hover", "fastfall", "speedinwater", "flightboat", "icefly", "waterbounce", "elytrabounce",
    "флай", "полет", "полёт", "спид", "спиды", "скорость", "быстрота", "спринт", "автоспринт",
    "бег", "стрейф", "стрейфы", "таргетстрейф", "нослоу", "ноуслоу", "замедление", "антизамедление",
    "иисус", "ходьбаповоде", "вода", "плавание", "паук", "климб", "лазание", "степ", "ступень",
    "ступени", "автошаг", "прыжки", "прыжок", "высокийпрыжок", "длинныйпрыжок", "двойнойпрыжок",
    "паркур", "сейфволк", "безопасныйшаг", "глайд", "планирование", "лодкадельтаплан", "лодкафлай",
    "элитры", "элитрафлай", "блинк", "мерцание", "таймер", "быстраялестница", "инвмув", "движение",
    "движениевинвентаре", "нолкип", "ноуклип", "телепорт", "кликтп", "бхоп", "быстроепадение",
    "парение", "эирволк", "прыжокввоздухе", "зависаниеввоздухе", "автопрыжок", "автопилот",
    "полетзапредметами", "элитракцели", "безпрыгучести", "беззадержкипрыжка", "суперфейерверк",
    "взбираниепостенам", "скоростьвводе", "безпаутины", "ноувеб", "хелперпередвижения", "фейз"
)

private val VISUALS_EXACT = setOf(
    "killeffect", "killeffects", "entityesp", "esp", "blockesp", "fireworkesp",
    "chestesp", "storageesp", "itemesp", "tracers", "tracer", "trails", "trail",
    "breadcrumbs", "tags", "nametags", "viewmodel", "customhand", "hands",
    "freecamera", "freecam", "thirdperson", "shulkerpreview", "seeinvisibles",
    "removals", "jumpcircles", "jumpcircle", "itemphysics", "fullbright", "brightness",
    "extendedtab", "crosshair", "customcrosshair", "clickgui", "chinahat",
    "aspectratio", "arrows", "targetarrows", "armordurabilityview", "durabilityview",
    "animations", "swordanimations", "ambience", "ambient", "worldtime", "time",
    "weather", "customfog", "fog", "sky", "prediction", "trajectory", "particles",
    "hitparticles", "damageparticles", "customparticles", "bloodparticles", "blood",
    "interface", "hud", "watermark", "arraylist", "keystrokes", "armorhud", "hotbar",
    "scoreboard", "radar", "notifications", "scoreboardhealth", "totemcounter",
    "totempopcounter", "totempop", "partypoint", "usetracker", "itemscooldown",
    "cooldowns", "potiontracker", "chams", "glow", "glowesp", "targethud",
    "targetesp", "targetinfo", "blockoutline", "freelook", "cameraclip", "zoom",
    "fov", "customfov", "norender", "antiblind", "nohurtcam", "skeleton", "shaders",
    "blur", "xray", "wetworld", "cape", "wings", "visuals", "render",
    "киллэффект", "киллэффекты", "эффектубийства", "эффектыубийств", "есп", "вх",
    "подсветкасущностей", "подсветкаблоков", "подсветкасундуков", "трейсеры", "трейсер",
    "линии", "трейлы", "трейл", "шлейф", "следы", "хвосты", "теги", "неймтеги",
    "ники", "имена", "вьюмодель", "руки", "зеркальныеруки", "положениерук", "моделька",
    "свободнаякамера", "фрикам", "камера", "третьелицо", "свободныйобзор",
    "просмотршалкера", "шалкерпревью", "шалкер", "видетьневидимок", "невидимыеигроки",
    "ремувалс", "удалениеэффектов", "удалениевизуалов", "джампсеркл", "кругиприпрыжке",
    "кругипрыжка", "физикапредметов", "физикавещей", "физика", "яркость", "фуллбрайт",
    "освещение", "пнв", "ночноезрение", "расширенныйтаб", "таб", "прицел",
    "кастомныйприцел", "кликгуи", "меню", "китайскаяшапка", "шапка", "китайскаяшляпа",
    "шляпа", "соотношениесторон", "аспектратио", "аспект", "стрелки", "указатели",
    "прочностьброни", "отображениеброни", "анимации", "анимацияудара", "анимациямеча",
    "атмосфера", "времясуток", "время", "погода", "туман", "кастомныйтуман", "небо",
    "предикшн", "предугадывание", "траектория", "траекторияполета", "частицы",
    "партиклы", "хитпартиклы", "дамагпартиклы", "частицыудара", "кровь", "интерфейс",
    "худ", "ватермарка", "ватермарк", "аррейлист", "список", "кейстроки", "броняхуд",
    "хотбар", "табло", "радар", "уведомления", "здоровьевтабло", "хпвтабло",
    "счетчиктотемов", "тотемкаунтер", "меткивгруппе", "патипоинт", "точкипати",
    "оповещениеоеде", "трекериспользования", "перезарядкапредметов", "кулдаунпредметов",
    "кулдауны", "трекерзелий", "оповещениеозельях", "чамсы", "подсветка", "свечение",
    "таргетхуд", "таргетесп", "таргетинфо", "обводкаблока", "круговойобзор", "обзор",
    "камераклип", "зум", "норендер", "антислепота", "слепота", "ноухерт", "безтряски",
    "скелеты", "шейдеры", "блюр", "иксрей", "рентген", "мокрыймир", "плащ", "крылья",
    "визуалы", "рендер", "фейерверкесп"
)

private val PLAYER_EXACT = setOf(
    "airplace", "autocreeperfarm", "autoeat", "autofarm", "autofish", "autorespawn",
    "autotool", "clickpearl", "pearltarget", "pearlhelper", "pearlblock", "elytrahelper",
    "expbottlefilling", "farmbuilder", "fastbreak", "fastexp", "fastplace",
    "inventoryplus", "invcleaner", "inventorycleaner", "itemscroller", "itemhelper",
    "multiactions", "noentitytrace", "nofall", "nointeract", "nopush", "noslotchange",
    "nuker", "openwalls", "scaffold", "blockfly", "fastbridge", "safeslot",
    "cheststealer", "stealer", "autoloot", "chestcounter", "portalgod", "ghost",
    "fastdrop", "autodrop", "bedbreaker", "autocraft",
    "установкаввоздухе", "фармилкакриперов", "автоеда", "быстраяеда", "еда", "автоферма",
    "авторыбалка", "рыбалка", "автовозрождение", "респавн", "возрождение", "автоинструмент",
    "автотул", "инструмент", "кликперл", "бросокжемчуга", "перл", "эндерперл",
    "мидлкликперл", "хелперэлитры", "заполнениепузырьков", "постройкафермы",
    "быстроеломание", "копание", "ломание", "быстрыйопыт", "быстраяустановка",
    "фастплейс", "допслоты", "инвклинер", "клинер", "очистка", "сортировка",
    "скроллер", "использованиепредмета", "мультидействия", "кликсквозьсущности",
    "ноуфолл", "нофолл", "безпадения", "антипадение", "безвзаимодействия",
    "антитолкание", "толкание", "ноупуш", "безсменыслота", "нукер",
    "открытиечерезстены", "скаффолд", "мост", "стройка", "строитель", "фастбридж",
    "сейфслот", "стилер", "стиллер", "честстилер", "автолут", "лут", "собиратель",
    "порталгод", "призрак", "автодроп", "бедбрейкер", "ломателькроватей", "автокрафт"
)

private val MISC_EXACT = setOf(
    "antiafk", "auctionhelper", "autoaccept", "autotpa", "autoauth", "autologin",
    "autoregister", "autobuy", "autobuyer", "auction", "autoclanupgrade", "autojoin",
    "autoleave", "ktleave", "automyst", "autotrade", "chathelper", "chat",
    "chatcalculator", "spammer", "chatspammer", "antispam", "clickfriend", "mcf",
    "middleclickfriend", "discordactivity", "discordrpc", "rpc", "funtimehelper",
    "holyworldhelper", "reallyworldhelper", "irc", "itemsaver", "noserverrotation",
    "srpspoofer", "streamermode", "togglesounds", "sound", "voice", "voicechat",
    "disabler", "macro", "macros", "panic", "selfdestruct", "nameprotect",
    "fakelag", "serverspoof", "proxy", "clientsettings", "clearplayercache", "autoreset",
    "антиафк", "хелпераукциона", "аукцион", "аук", "скупка", "автопринятие",
    "автопароль", "автоавторизация", "автологин", "авторег", "автобай", "автопокупка",
    "прокачкаклана", "автовход", "автолив", "лив", "ктлив", "выход", "автомистик",
    "автотрейд", "хелперчата", "чат", "спамер", "калькулятор", "добавлениедруга",
    "дискордактивность", "дискорд", "фантаймхелпер", "холиворлдхелпер",
    "рилливорлдхелпер", "ирчат", "иркс", "сохранениересурсов", "запретротациисервера",
    "спуферресурспака", "режимстримера", "звукипереключения", "звук", "звуки",
    "голосовойчат", "дисейблер", "обход", "античит", "макрос", "макросы", "паника",
    "самоуничтожение", "защитаника", "фейклаг", "прокси"
)

private fun classifyByKeywords(raw: String): String? {
    val s = normalizeName(raw)
    if (s.isEmpty()) return null

    if (VISUALS_EXACT.contains(s)) return "Visuals"
    if (COMBAT_EXACT.contains(s)) return "Combat"
    if (MOVEMENT_EXACT.contains(s)) return "Movement"
    if (PLAYER_EXACT.contains(s)) return "Player"
    if (MISC_EXACT.contains(s)) return "Misc"

    if (s.contains("killeffect") || s.contains("киллэффект") || s.contains("эффектубийств") || s.contains("effect") || s.contains("эффект")) return "Visuals"
    if (s.contains("totemcounter") || s.contains("счетчиктотем") || s.contains("тотемкаунтер") || s.contains("totempop")) return "Visuals"
    if (s.contains("armordurability") || s.contains("durability") || s.contains("прочностьброн") || s.contains("armorhud") || s.contains("броняхуд")) return "Visuals"
    if (s.contains("itemscooldown") || s.contains("cooldown") || s.contains("кулдаун") || s.contains("перезарядк")) return "Visuals"
    if (s.contains("potiontracker") || s.contains("трекерзель") || s.contains("usetracker") || s.contains("трекериспольз") || s.contains("partypoint") || s.contains("патипоинт")) return "Visuals"
    if (s.contains("targethud") || s.contains("таргетхуд") || s.contains("targetinfo") || s.contains("таргетинфо") || s.contains("targetesp") || s.contains("таргетесп")) return "Visuals"
    if (s.contains("jumpcircle") || s.contains("джампсеркл") || s.contains("кругпрыж")) return "Visuals"
    if (s.contains("particle") || s.contains("партикл") || s.contains("частиц")) return "Visuals"
    if (s.contains("itemphysics") || s.contains("физикавещ") || s.contains("физикапредмет")) return "Visuals"
    if (s.contains("chestesp") || s.contains("blockesp") || s.contains("storageesp") || s.contains("itemesp") || s.contains("еспсундук")) return "Visuals"
    if (s.contains("xray") || s.contains("иксрей") || s.contains("рентген")) return "Visuals"
    if (s.contains("trajectory") || s.contains("траектори") || s.contains("prediction") || s.contains("предикшн")) return "Visuals"
    if (s.contains("shulker") || s.contains("шалкер")) return "Visuals"
    if (s.contains("aspect") || s.contains("аспект")) return "Visuals"
    if (s.contains("wetworld") || s.contains("мокрыймир")) return "Visuals"
    if (s.contains("fireworkesp") || s.contains("фейерверкесп")) return "Visuals"
    if (s.contains("seeinvisible") || s.contains("видетьневидим")) return "Visuals"
    if (s.contains("swordanimation") || s.contains("анимациямеч") || s.contains("анимацияудар")) return "Visuals"
    if (s.contains("scoreboard") || s.contains("скорборд") || s.contains("табло")) return "Visuals"

    if (s.contains("targetstrafe") || s.contains("таргетстрейф")) return "Movement"
    if (s.contains("invmove") || s.contains("инвмув") || s.contains("guimove") || s.contains("инвентармув")) return "Movement"
    if (s.contains("superfirework") || s.contains("суперфейерверк")) return "Movement"
    if (s.contains("airstuck") || s.contains("зависаниеввоздух")) return "Movement"
    if (s.contains("nojump") || s.contains("безпрыгуч") || s.contains("беззадержкипрыж")) return "Movement"

    if (s.contains("nofall") || s.contains("нофолл") || s.contains("ноуфолл") || s.contains("антипаден") || s.contains("безпаден")) return "Player"
    if (s.contains("scaffold") || s.contains("скаффолд") || s.contains("мост") || s.contains("стройк") || s.contains("бридж") || s.contains("bridge")) return "Player"
    if (s.contains("bedbreaker") || s.contains("бедбрейкер") || s.contains("ломателькроват")) return "Player"
    if (s.contains("autocreeper") || s.contains("фармилкакрипер")) return "Player"
    if (s.contains("farmbuilder") || s.contains("постройкаферм")) return "Player"
    if (s.contains("elytrahelper") || s.contains("хелперэлитр")) return "Player"
    if (s.contains("expbottle") || s.contains("пузырек") || s.contains("пузырьк")) return "Player"
    if (s.contains("openwalls") || s.contains("открытиечерезстен")) return "Player"

    if (s.contains("rwgodmode") || s.contains("годмод")) return "Combat"
    if (s.contains("autoinvisible") || s.contains("автоневидим")) return "Combat"
    if (s.contains("autoexplosion") || s.contains("автовзрыв")) return "Combat"
    if (s.contains("tapemouse") || s.contains("автокликмыш")) return "Combat"
    if (s.contains("throwsync") || s.contains("синхронизацияброск")) return "Combat"
    if (s.contains("superprojectile") || s.contains("суперснаряд")) return "Combat"

    if (s.contains("staff") || s.contains("стафф") || s.contains("модератор") || s.contains("админ") ||
        s.contains("autobuy") || s.contains("автобай") || s.contains("скупк") || s.contains("аукцион") ||
        s.contains("disabler") || s.contains("дисейблер") || s.contains("обход") ||
        s.contains("spammer") || s.contains("спамер") || s.contains("авторег") || s.contains("автологин") || s.contains("автопарол") ||
        s.contains("macro") || s.contains("макрос") || s.contains("panic") || s.contains("паник") ||
        s.contains("discord") || s.contains("дискорд") || s.contains("selfdestruct") || s.contains("самоуничтож") ||
        s.contains("nameprotect") || s.contains("защитаник") || s.contains("proxy") || s.contains("прокси") ||
        s.contains("chat") || s.contains("чат") || s.contains("voice") || s.contains("голосов") ||
        s.contains("togglesound") || s.contains("звукипереключен") ||
        s.contains("streamermode") || s.contains("режимстример") ||
        s.contains("srpspoofer") || s.contains("спуфер") ||
        s.contains("helper") || s.contains("хелпер") ||
        s.contains("itemsaver") || s.contains("сохранениересурс") ||
        s.contains("noserverrotation") || s.contains("запретротац") ||
        s.contains("autoclan") || s.contains("прокачкаклан") ||
        s.contains("irc") || s.contains("иркс") ||
        s.contains("automyst") || s.contains("автомистик") ||
        s.contains("autotrade") || s.contains("автотрейд") ||
        s.contains("autojoin") || s.contains("автовход") ||
        s.contains("autoleave") || s.contains("автолив") || s.contains("ktleave") || s.contains("ктлив") ||
        s.contains("autoaccept") || s.contains("автопринят") ||
        s.contains("clickfriend") || s.contains("добавлениедруг")) return "Misc"

    if (s.contains("esp") || s.contains("есп") || s.contains("wh") || s.contains("вх") || s.contains("wallhack") || s.contains("валхак") || s.contains("валлхак") ||
        s.contains("tag") || s.contains("тег") || s.contains("ник") || s.contains("имен") ||
        s.contains("tracer") || s.contains("трейсер") || s.contains("линии") ||
        s.contains("cham") || s.contains("чам") || s.contains("подсветк") ||
        s.contains("bright") || s.contains("яркост") || s.contains("брайт") || s.contains("пнв") || s.contains("ночноезрен") || s.contains("light") ||
        s.contains("freecam") || s.contains("фрикам") || s.contains("камер") || s.contains("cam") ||
        s.contains("crosshair") || s.contains("прицел") ||
        s.contains("hat") || s.contains("шляп") || s.contains("шапк") || s.contains("cap") ||
        s.contains("trail") || s.contains("трейл") || s.contains("хвост") || s.contains("след") || s.contains("шлейф") ||
        s.contains("glow") || s.contains("свечен") || s.contains("bloom") || s.contains("блюм") ||
        s.contains("hud") || s.contains("худ") || s.contains("watermark") || s.contains("ватермарк") || s.contains("array") || s.contains("аррей") || s.contains("список") || s.contains("keystroke") || s.contains("кейстрок") ||
        s.contains("radar") || s.contains("радар") || s.contains("notif") || s.contains("уведомлен") ||
        s.contains("norender") || s.contains("норендер") || s.contains("blind") || s.contains("слепот") || s.contains("nohurt") || s.contains("ноухерт") || s.contains("тряск") ||
        s.contains("skeleton") || s.contains("скелет") || s.contains("кост") ||
        s.contains("shader") || s.contains("шейдер") || s.contains("blur") || s.contains("блюр") ||
        s.contains("visual") || s.contains("визуал") || s.contains("рендер") || s.contains("render") || s.contains("view") || s.contains("вид") ||
        s.contains("model") || s.contains("hand") || s.contains("рука") || s.contains("руки") || s.contains("viewmodel") ||
        s.contains("time") || s.contains("врем") || s.contains("weather") || s.contains("погод") || s.contains("fog") || s.contains("туман") || s.contains("sky") || s.contains("небо") ||
        s.contains("world") || s.contains("мир") || s.contains("arrow") || s.contains("стрелк") || s.contains("указател") ||
        s.contains("look") || s.contains("обзор") || s.contains("zoom") || s.contains("зум") || s.contains("fov") || s.contains("фов") ||
        s.contains("animation") || s.contains("анимац") || s.contains("blood") || s.contains("кров") ||
        s.contains("cape") || s.contains("плащ") || s.contains("wings") || s.contains("крыл")) {
        return "Visuals"
    }

    if (s.contains("aura") || s.contains("аур") ||
        s.contains("aim") || s.contains("аим") ||
        s.contains("trigger") || s.contains("триггер") || s.contains("тригер") ||
        s.contains("velocity") || s.contains("велосит") || s.contains("отдач") || s.contains("antikb") || s.contains("антикб") || s.contains("superkb") || s.contains("knockback") || s.contains("нокаур") ||
        (s.contains("bot") && !s.contains("boat") && !s.contains("bottom")) || s.contains("антибот") ||
        s.contains("crit") || s.contains("крит") ||
        s.contains("reach") || s.contains("рич") || s.contains("дальност") ||
        s.contains("hitbox") || s.contains("хитбокс") ||
        s.contains("shield") || s.contains("щит") ||
        s.contains("potion") || s.contains("зель") || s.contains("buff") || s.contains("бафф") ||
        s.contains("gapple") || s.contains("гэп") || s.contains("яблок") ||
        s.contains("totem") || s.contains("тотем") ||
        s.contains("offhand") || s.contains("оффхенд") || s.contains("втораярук") || s.contains("леваярук") ||
        s.contains("armor") || s.contains("брон") ||
        s.contains("backtrack") || s.contains("бектрек") || s.contains("бэктрек") || s.contains("бэктрэк") ||
        s.contains("bow") || s.contains("лук") || s.contains("crossbow") || s.contains("арбалет") ||
        s.contains("crystal") || s.contains("кристалл") || s.contains("кристал") ||
        s.contains("anchor") || s.contains("анкор") || s.contains("якор") ||
        s.contains("mace") || s.contains("мейс") || s.contains("булав") ||
        s.contains("piston") || s.contains("порш") ||
        s.contains("clicker") || s.contains("кликер") ||
        s.contains("sword") || s.contains("меч") ||
        s.contains("weapon") || s.contains("оружи") ||
        s.contains("combat") || s.contains("бой") || s.contains("fight") || s.contains("драка") ||
        s.contains("attack") || s.contains("атак") || s.contains("удар") ||
        s.contains("tnt") || s.contains("тнт") || s.contains("динамит") ||
        s.contains("surround") || s.contains("сурраунд") || s.contains("яма") ||
        s.contains("antifire") || s.contains("антиогонь") || s.contains("antiaim") || s.contains("антиаим")) {
        return "Combat"
    }

    if (s.contains("fly") || s.contains("флай") || s.contains("полет") || s.contains("полёт") || s.contains("flight") || s.contains("boat") || s.contains("лодк") || s.contains("elytra") || s.contains("элитр") || s.contains("hover") || s.contains("парен") ||
        s.contains("speed") || s.contains("спид") || s.contains("скорост") || s.contains("быстрот") ||
        s.contains("sprint") || s.contains("спринт") || s.contains("бег") ||
        s.contains("strafe") || s.contains("стрейф") ||
        s.contains("noslow") || s.contains("нослоу") || s.contains("ноуслоу") || s.contains("slow") || s.contains("замедлен") ||
        s.contains("jesus") || s.contains("иисус") || s.contains("water") || s.contains("вод") || s.contains("плаван") || s.contains("dolphin") || s.contains("дельфин") ||
        s.contains("spider") || s.contains("паук") || s.contains("wallclimb") || s.contains("fastclimb") || s.contains("climb") || s.contains("лестниц") || s.contains("ladder") || s.contains("лазан") || s.contains("климб") ||
        s.contains("step") || s.contains("степ") || s.contains("ступен") || s.contains("шаг") ||
        s.contains("jump") || s.contains("прыж") || s.contains("прыг") || s.contains("bhop") || s.contains("бхоп") ||
        s.contains("parkour") || s.contains("паркур") || s.contains("safewalk") || s.contains("безопасн") ||
        s.contains("noweb") || s.contains("ноувеб") ||
        s.contains("glide") || s.contains("глайд") || s.contains("планирован") || s.contains("fastfall") ||
        s.contains("blink") || s.contains("блинк") || s.contains("мерцан") ||
        s.contains("timer") || s.contains("таймер") ||
        s.contains("teleport") || s.contains("кликтп") || s.contains("тп") ||
        s.contains("noclip") || s.contains("ноуклип") || s.contains("сквозьстен") ||
        s.contains("motion") || s.contains("движен") || s.contains("move") || s.contains("перемещен") ||
        s.contains("ice") || s.contains("лед") || s.contains("лёд")) {
        return "Movement"
    }

    if (s.contains("eat") || s.contains("ед") || s.contains("кушат") || s.contains("food") ||
        s.contains("break") || s.contains("лома") || s.contains("копа") || s.contains("шахт") || s.contains("nuker") || s.contains("нукер") || s.contains("mine") ||
        s.contains("tool") || s.contains("инструмент") ||
        s.contains("respawn") || s.contains("респавн") || s.contains("возрожден") ||
        s.contains("middle") || s.contains("мидл") || s.contains("колесик") || s.contains("mcf") || s.contains("pearl") || s.contains("перл") ||
        s.contains("steal") || s.contains("стил") ||
        s.contains("loot") || s.contains("лут") || s.contains("собирател") ||
        s.contains("clean") || s.contains("клинер") || s.contains("очистк") || s.contains("сортировк") ||
        s.contains("place") || s.contains("плейс") || s.contains("ставител") || s.contains("установк") ||
        s.contains("phase") || s.contains("фаз") || s.contains("portal") || s.contains("портал") ||
        s.contains("fish") || s.contains("рыб") || s.contains("удочк") ||
        s.contains("push") || s.contains("толка") ||
        s.contains("ghost") || s.contains("призрак") ||
        s.contains("slot") || s.contains("слот") || s.contains("drop") || s.contains("дроп") || s.contains("выбрасыван") || s.contains("scroller") || s.contains("скроллер") ||
        s.contains("afk") || s.contains("афк") ||
        s.contains("player") || s.contains("игрок") ||
        s.contains("inv") || s.contains("инвентар") ||
        s.contains("refill") || s.contains("пополнен") ||
        s.contains("interact") || s.contains("fastuse") || s.contains("использован") ||
        s.contains("craft") || s.contains("крафт") ||
        s.contains("item") || s.contains("предмет")) {
        return "Player"
    }

    return null
}

private fun getModuleCategory(name: String, displayName: String = ""): String {
    if (displayName.isNotBlank()) {
        val fromDisplay = classifyByKeywords(displayName)
        if (fromDisplay != null && fromDisplay != "Misc") return fromDisplay
    }

    if (name.isNotBlank()) {
        val fromName = classifyByKeywords(name)
        if (fromName != null && fromName != "Misc") return fromName
    }

    if (displayName.isNotBlank() && classifyByKeywords(displayName) == "Misc") return "Misc"
    if (name.isNotBlank() && classifyByKeywords(name) == "Misc") return "Misc"

    return "Visuals"
}

private fun categoryMatches(cat: String): Boolean {
    if (categories.contains(cat)) return true
    if ((cat == "Visuals" || cat == "Render") && (categories.contains("Visuals") || categories.contains("Render"))) return true
    return false
}

// категория считается один раз на модуль, а не каждый кадр
val catCache = mutableMapOf<String, String>()

fun savePos() {
    storage.put("x", ax.toDouble())
    storage.put("y", ay.toDouble())
    storage.put("right", alignRight)
    storage.save()
}

// плавный поворот: 0 = выравнивание влево, 1 = вправо
var alAnim = if (alignRight) 1f else 0f
var alV = 0f

onDisable {
    rows.clear()
    lastNanos = 0L
    dragging = false
    alAnim = if (alignRight) 1f else 0f
    alV = 0f
}

// ───────────── отрисовка ─────────────
on<Render2DEvent> { e ->
    val r = e.render()
    val s = gameSettings.scaleFactor().toFloat()
    val sw = r.width()
    val sh = r.height()

    val now = client.nanos()
    val dt = if (lastNanos == 0L) 0.016f
             else ((now - lastNanos) / 1.0e9).toFloat().coerceIn(0.0005f, 0.05f)
    lastNanos = now

    // сохранённое состояние могло подгрузиться уже после верхнего уровня файла
    if (!colorCb.value()) colorCb.value(true)

    val pal = Palette(
        customA.value(),
        customB.value(),
        Colors.withAlpha(Colors.mix(Colors.BLACK, customA.value(), 0.12f), 255)
    )

    // размеры
    val scaleK = when (scaleMode) {
        "125%" -> 1.25f
        "150%" -> 1.5f
        "200%" -> 2f
        else -> 1f
    }
    val k = s * scaleK          // GUI-масштаб клиента * выбранный процент
    val size = 6.5f * k
    val padX = 3f * k
    val barW = max(1f, 1.4f * k)
    val rowH = size + 1.5f * k
    val weight = Weight.SEMI_BOLD

    // какие модули сейчас должны быть в списке
    val active = mutableSetOf<String>()
    for (m in client.modules().all()) {
        val k = m.name()
        if (!m.enabled()) continue
        val disp = m.displayName()
        val cat = catCache.getOrPut(k) { getModuleCategory(k, disp) }
        if (!categoryMatches(cat)) continue
        active.add(k)
        var row = rows[k]
        if (row == null) {
            row = Row(k)
            rows[k] = row
        }
        row.label = disp
    }

    for (row in rows.values) {
        row.target = if (active.contains(row.key)) 1f else 0f
        if (row.mLabel != row.label || row.mSize != size) {
            row.textW = r.textWidth(row.label, size, weight)
            row.mLabel = row.label
            row.mSize = size
        }
    }

    // длинные названия сверху
    val ordered = rows.values.sortedWith(compareByDescending<Row> { it.textW }.thenBy { it.key })

    // целевая Y: строки складываются по высоте, схлопывающиеся сжимаются
    var cursor = 0f
    for (row in ordered) {
        row.targetY = cursor
        cursor += rowH * row.appear.coerceIn(0f, 1f)
    }

    // физика: две пружины на строку (появление и позиция), подшаги для устойчивости
    val bounce = 1f   // упругость зафиксирована на максимуме, настройки нет
    val zeta = 1f - 0.75f * bounce
    val cA = 2f * zeta * sqrt(stiffness)
    val kY = stiffness * 1.4f
    val cY = 2f * (1f - 0.5f * bounce) * sqrt(kY)
    val steps = ceil(dt / 0.004f).toInt().coerceIn(1, 16)
    val h = dt / steps

    for (row in ordered) {
        if (row.fresh) {
            row.y = row.targetY
            row.yV = 0f
            row.fresh = false
        }
        for (i in 0 until steps) {
            row.appearV += (stiffness * (row.target - row.appear) - cA * row.appearV) * h
            row.appear += row.appearV * h
            row.yV += (kY * (row.targetY - row.y) - cY * row.yV) * h
            row.y += row.yV * h
        }
    }

    // рамка списка для перетаскивания
    var bw = 0f
    for (row in ordered) {
        if (row.appear > 0.01f || row.target > 0f) bw = max(bw, row.textW + padX * 2f + barW)
    }
    boxW = max(bw, 40f * s)
    boxH = max(cursor, rowH)

    // перетаскивание: только пока открыт чат, левая кнопка мыши
    val inChat = game.screenKind() == ScreenKind.CHAT
    val mx = keys.mouseX()
    val my = keys.mouseY()
    val down = keys.mouseDown(0)

    val left0 = if (alignRight) sw - ax * s - boxW else ax * s
    val top0 = ay * s
    val hover = inChat && mx >= left0 && mx <= left0 + boxW && my >= top0 && my <= top0 + boxH

    if (inChat) {
        if (down && !wasDown && hover) {
            dragging = true
            grabDX = mx - left0
            grabDY = my - top0
        }
        if (dragging) {
            if (down) {
                // левый край рамки следует за мышью, сторона выбирается на лету по половине экрана
                val nl = (mx - grabDX).coerceIn(0f, max(0f, sw - boxW))
                val nt = (my - grabDY).coerceIn(0f, max(0f, sh - boxH))
                alignRight = nl + boxW / 2f > sw / 2f
                ax = if (alignRight) (sw - (nl + boxW)) / s else nl / s
                ay = nt / s
            } else {
                dragging = false
                savePos()
            }
        }
    } else if (dragging) {
        dragging = false
        savePos()
    }
    wasDown = down

    val boxLeft = if (alignRight) sw - ax * s - boxW else ax * s
    val boxTop = ay * s

    // пружина поворота выравнивания
    val alTarget = if (alignRight) 1f else 0f
    val kAl = 300f
    val cAl = 2f * 0.85f * sqrt(kAl)
    for (i in 0 until steps) {
        alV += (kAl * (alTarget - alAnim) - cAl * alV) * h
        alAnim += alV * h
    }
    val al = alAnim
    val alc = al.coerceIn(0f, 1f)

    // рисуем: сначала собираем геометрию и цвета строк
    val t = now / 1.0e9
    val items = mutableListOf<DrawItem>()

    for (row in ordered) {
        val a = row.appear.coerceIn(0f, 1f)
        if (a < 0.01f) continue

        val w = row.textW + padX * 2f + barW
        val slide = (1f - row.appear) * (w + ax * s + 6f * s)
        val x0 = boxLeft + (boxW - w) * al + (if (alignRight) slide else -slide)
        val y0 = boxTop + row.y

        // волна цвета вдоль списка
        val phase = t * waveSpeed * 2.0 + (row.y / rowH) * 0.5
        val col = Colors.mix(pal.a, pal.b, (0.5 + 0.5 * sin(phase)).toFloat())
        val colNext = Colors.mix(pal.a, pal.b, (0.5 + 0.5 * sin(phase + 0.5)).toFloat())

        items.add(DrawItem(row, x0, y0, w, a, col, colNext))
    }

    // цвет волны в произвольной точке по высоте, та же формула, что у текста строк
    fun colAt(y: Float): Int {
        val ph = t * waveSpeed * 2.0 + ((y - boxTop) / rowH) * 0.5
        return Colors.mix(pal.a, pal.b, (0.5 + 0.5 * sin(ph)).toFloat())
    }

    // свечение: единый силуэт всего списка, без наложения строк друг на друга.
    // каждый слой = силуэт, раздутый на d; он режется по высоте на непересекающиеся
    // полосы, поэтому прозрачность нигде не суммируется дважды (никаких «лампочек»).
    // слои добавляют по чуть-чуть, а сумма даёт плавное затухание от края наружу.
    // свечение компактное: яркая кромка у самого края и быстрый спад, радиус около 3 GUI-единиц
    if (glow && items.isNotEmpty()) {
        val layers = 6
        val step = max(0.75f, 0.5f * k)

        for (i in 1..layers) {
            val d = i * step
            val f0 = 1f - (i - 1f) / layers
            val f1 = 1f - i.toFloat() / layers
            val layerAlpha = 100f * (f0 * f0 - f1 * f1)

            val ys = mutableListOf<Float>()
            for (item in items) {
                ys.add(item.y0 - d)
                ys.add(item.y0 + rowH + d)
            }
            ys.sort()

            for (j in 0 until ys.size - 1) {
                val ua = ys[j]
                val ub = ys[j + 1]
                val ya = round(ua)
                val yb = round(ub)
                if (yb - ya < 1f) continue

                // строки, чей раздутый прямоугольник закрывает всю эту полосу
                var minL = Float.MAX_VALUE
                var maxR = -Float.MAX_VALUE
                var amax = 0f
                for (item in items) {
                    if (item.y0 - d <= ua + 0.001f && item.y0 + rowH + d >= ub - 0.001f) {
                        minL = min(minL, item.x0 - d)
                        maxR = max(maxR, item.x0 + item.w + d)
                        amax = max(amax, item.a)
                    }
                }
                if (amax <= 0f) continue

                val ga = (layerAlpha * amax + 0.5f).toInt()
                if (ga <= 0) continue

                val lx = round(minL)
                val rx = round(maxR)
                r.gradient(
                    lx, ya, rx - lx, yb - ya,
                    Colors.withAlpha(colAt(ya), ga),
                    Colors.withAlpha(colAt(yb), ga),
                    false
                )
            }
        }
    }

    for (item in items) {
        val row = item.row
        val x0 = item.x0
        val y0 = item.y0
        val w = item.w
        val a = item.a
        val col = item.col
        val colNext = item.colNext

        // фон строки по ширине текста
        r.rect(x0, y0, w, rowH, Colors.fade(Colors.withAlpha(pal.bg, 255), a))

        // лёгкий оттенок от стороны с акцентом, переезжает вместе с поворотом
        r.gradient(
            x0, y0, w, rowH,
            Colors.withAlpha(col, (46f * a * (1f - alc)).toInt()),
            Colors.withAlpha(col, (46f * a * alc).toInt()),
            true
        )

        // акцентная полоска, цвет стекает от строки к строке
        val bx = x0 + (w - barW) * alc
        r.gradient(bx, y0, barW, rowH, Colors.fade(col, a), Colors.fade(colNext, a), false)

        // текст с тенью
        val tx = x0 + padX + barW * (1f - alc)
        val ty = y0 + (rowH - r.textHeight(size, weight)) / 2f
        r.textShadow(
            row.label, tx, ty, size,
            Colors.fade(col, a),
            Colors.rgba(0, 0, 0, (120f * a).toInt()),
            weight
        )
    }

    // убираем полностью исчезнувшие
    val dead = rows.values.filter { it.target == 0f && it.appear < 0.005f && abs(it.appearV) < 0.05f }
    for (d in dead) rows.remove(d.key)
}
