package app.aloft.ui

import androidx.compose.ui.graphics.Color
import app.aloft.domain.Building
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Issue
import app.aloft.domain.Load
import app.aloft.domain.Mode
import app.aloft.domain.Tx
import app.aloft.ui.theme.C

/** One thing said in the text box, and who says it. */
class Line(val who: String, val color: Color, val text: String)

/** What the spirits, guests and islanders say, worked out from the island's state. */
object Talk {
    private fun e(k: Element, ru: Boolean, en: String, r: String) = Line(k.spirit.of(ru), C.elementInk(k), if (ru) r else en)

    /** "5 вёрст", "2 версты", "1 верста"; "5 versts". */
    fun versts(n: Int, ru: Boolean): String {
        if (!ru) return if (n == 1) "1 verst" else "$n versts"
        val m10 = n % 10
        val m100 = n % 100
        val w = when {
            m10 == 1 && m100 != 11 -> "верста"
            m10 in 2..4 && m100 !in 12..14 -> "версты"
            else -> "вёрст"
        }
        return "$n $w"
    }

    fun metres(n: Int): String = String.format(java.util.Locale.US, "%,d", n).replace(',', ' ')

    fun lines(h: Home): List<Line> {
        val ru = h.ru
        val i = h.isle
        when (i.mode) {
            Mode.ANCHOR -> return listOf(e(Element.EARTH, ru, "The island is at anchor. Get well — we're not going anywhere, and nothing will fade.",
                "Остров на якоре. Выздоравливай — мы никуда не денемся и форму не растеряем."))
            Mode.CALM -> return listOf(
                e(Element.WOOD, ru, "Calm. The spirits are asleep, a rainbow over the island. Rest is training too.", "Штиль. Духи спят, над островом радуга. Отдых — тоже тренировка."),
                e(Element.AIR, ru, "Mm… we'll run tomorrow… today we sleep…", "Мм… завтра побежим… а сегодня спим…"),
            )
            Mode.STORM -> {
                val k = h.g.workouts.firstOrNull()?.let { Element.of(it.element) } ?: Element.FIRE
                return listOf(e(k, ru, "A storm over the island: the load jumped too fast. Better rest tomorrow, or one of us will start limping.",
                    "Над островом гроза: нагрузка резко выросла. Завтра лучше отдохнуть, иначе кто-нибудь из нас начнёт хромать."))
            }
            Mode.DAY -> Unit
        }
        val out = mutableListOf<Line>()
        h.guestToday?.let { out += guest(it, ru) }
        if (Issue.LOW in i.issues) out += if (h.g.workouts.isEmpty()) e(Element.AIR, ru,
            "We're down in the clouds, waiting. Log your first training and we'll start to rise.",
            "Мы пока в облаках и ждём. Запиши первую тренировку — и остров начнёт подниматься.")
        else e(Element.AIR, ru, "We're down in the clouds. Quiet, damp, gulls crying. We can rise any day — one walk is enough.",
            "Мы в облаках. Тихо, сыро, кричат чайки. Подняться можно в любой день — хватит одной прогулки.")
        out += issues(h)
        if (out.isEmpty() || out.size == 1 && h.guestToday != null) out += e(Element.AIR, ru, "The island is ${metres(i.metres)} m up. The elements are in harmony, the wind is steady.",
            "Остров на высоте ${metres(i.metres)} м. Стихии в ладу, ветер ровный.")
        h.next?.let { st -> out += e(Element.AIR, ru, "${versts(st.at - h.dist, false)} to ${st.to.en}.", "До ${st.to.ru} ещё ${versts(st.at - h.dist, true)}.") }
        h.requests.firstOrNull { !it.done }?.let { a ->
            val who = a.req.who
            out += Line(who.title.of(ru), who.element?.let { C.elementInk(it) } ?: C.muted, a.req.text.of(ru) + "  (${a.have}/${a.req.goal})")
        }
        val b = i.buildings
        if (Building.GARDEN in b) out += e(Element.WOOD, ru, "Water lilies have opened in the garden. The herbalist says it's the swimming and the stretching.",
            "В саду раскрылись кувшинки. Травница говорит — от воды и растяжки.")
        if (Building.SPRING in b) out += e(Element.WATER, ru, "Two people off an airship are sitting in the hot springs and won't leave.",
            "У горячих источников сидят двое с дирижабля и не хотят улетать.")
        if (Building.FORGE in b) out += e(Element.FIRE, ru, "The smith asked me to tell you: it's me working the bellows. Just so you know.",
            "Кузнец просил передать: мехи раздуваю я. Пусть знают.")
        if (Building.MILL in b) out += e(Element.EARTH, ru, "The mill is grinding. The miller says there hasn't been wind like this since spring.",
            "Мельница мелет. Мельник говорит, такого ветра не было с весны.")
        if (Building.TOWER in b) out += e(Element.AIR, ru, "From the watchtower you can see far ahead. They say there's an island of apple trees out there.",
            "Со смотровой башни видно далеко вперёд. Говорят, там остров яблонь.")
        return out
    }

    /** What the spirits say about the island's imbalances, the most pressing first. */
    fun issues(h: Home): List<Line> {
        val ru = h.ru
        val i = h.isle
        val out = mutableListOf<Line>()
        if (Issue.DROUGHT in i.issues) out += e(Element.WATER, ru, "The springs have dried up and the grass is going yellow. Lots of fire, no water. A swim, maybe?",
            "Родники пересохли, трава желтеет. Огня много, а воды нет. Может, в бассейн?")
        if (Issue.SLIDE in i.issues) out += e(Element.EARTH, ru, "The water is washing out the rock; the edge is crumbling. I could use some heavy weights.",
            "Вода подмывает скалы, край осыпается. Мне бы веса потяжелее.")
        if (Issue.GUSTS in i.issues) out += e(Element.AIR, ru, "I pull the island up and nobody holds it down — we're tossed about. Lift something heavy.",
            "Я тяну остров вверх, а держать его некому — нас болтает. Потаскай железо.")
        if (Issue.HEAVY in i.issues) out += e(Element.AIR, ru, "The island is getting heavy and sinking. The golem is strong, but I'm short of wind. A run?",
            "Остров тяжелеет и проседает. Голем силён, а мне не хватает ветра. Пробежимся?")
        if (Issue.CRACK in i.issues) out += e(Element.WOOD, ru, "The stone is cracking: everything stiff, nothing bends. Stretch, even for ten minutes.",
            "Камень трескается: всё жёсткое, ничего не гнётся. Растяжка, хоть десять минут.")
        return out
    }

    fun guest(g: Guest, ru: Boolean): Line {
        val t = when (g) {
            Guest.WANDERER -> Tx("A new road? I love new roads. I'll tell you later where this one leads.", "Новая дорога? Люблю новые дороги. Потом расскажу, куда ведёт эта.")
            Guest.THUNDERBIRD -> Tx("Giving it everything — I respect that. Now a calm, agreed?", "Выложиться до конца — уважаю. А теперь штиль, договорились?")
            Guest.FROST -> Tx("Red cheeks? That's the way. Frost is the best coach.", "Щёки красные? То-то же. Мороз — лучший тренер.")
            Guest.SUNHARE -> Tx("Hot! Drink water. Lots of it. I'm off!", "Жарко! Пей воду. Много воды. А я побежал!")
            Guest.SNOWOWL -> Tx("Hoo. Snow falling and you don't care a bit. Hoo.", "Уху. Снег идёт, а тебе хоть бы что. Уху.")
            Guest.MISTLING -> Tx("…you can see me? Oh.", "…ты меня видишь? Ой.")
            Guest.DRIZZLE -> Tx("Not afraid of rain? Then I'll water the garden too.", "Дождя не боишься? Тогда и я полью сад.")
            Guest.ROBIN -> Tx("Up before the sun! The sylph is still yawning.", "Раньше солнца! Сильф ещё зевает.")
            Guest.FIREFLY -> Tx("Shh, the islanders are asleep. I'll light the path.", "Тише, жители спят. Я посвечу на тропинку.")
        }
        return Line(g.title.of(ru), C.muted, t.of(ru))
    }

    /** What the spirit says when it catches the light of a training. */
    fun caught(k: Element, ru: Boolean, n: Int): Line {
        val a = if (k.female) "а" else ""
        return when (n % 3) {
            0 -> e(k, ru, "Caught it! Feel the island rise a little?", "Поймал$a! Чувствуешь, остров чуть приподнялся?")
            1 -> e(k, ru, "Warm… Thank you. I'm holding on tighter.", "Тепло… Спасибо. Держу крепче.")
            else -> e(k, ru, "Another one like that and I'll grow.", "Ещё одну такую — и я подрасту.")
        }
    }

    private val acts = mapOf(
        Element.AIR to Tx("The sylph raced about all week.", "Сильф носился всю неделю."),
        Element.EARTH to Tx("The golem hauled boulders all week.", "Голем всю неделю таскал валуны."),
        Element.WATER to Tx("The undine splashed about all week.", "Ундина плескалась всю неделю."),
        Element.WOOD to Tx("The dryad stretched towards the sun all week.", "Дриада всю неделю тянулась к солнцу."),
        Element.FIRE to Tx("The salamander blazed all week.", "Саламандра полыхала всю неделю."),
    )
    private val bored = mapOf(
        Element.AIR to Tx("The sylph got bored and hardly came out of the clouds.", "Сильф заскучал и почти не показывался из облаков."),
        Element.EARTH to Tx("The golem got bored, and the edges began to crumble.", "Голем заскучал, и края острова начали крошиться."),
        Element.WATER to Tx("The undine got bored, and the springs barely run.", "Ундина заскучала, и родники еле текут."),
        Element.WOOD to Tx("The dryad got bored, and the stone has started to crack.", "Дриада заскучала, и камень пошёл трещинами."),
        Element.FIRE to Tx("The salamander got bored; the forge is cooling.", "Саламандра заскучала, кузня остывает."),
    )

    /** The council by the fire: the week told in a few plain sentences. */
    fun council(h: Home): List<String> {
        val ru = h.ru
        val out = mutableListOf<String>()
        val byEl = Element.entries.associateWith { k -> h.week.filter { Element.of(it.element) == k }.sumOf { Load.of(it) } }
        if (h.week.isEmpty()) out += if (ru) "На этой неделе тренировок не было. Духи грелись у костра и ждали." else "No training this week. The spirits warmed themselves by the fire and waited."
        else {
            out += acts.getValue(byEl.maxBy { it.value }.key).of(ru)
            Element.entries.filter { byEl.getValue(it) == 0.0 && h.isle.of(it) < .35 }.minByOrNull { h.isle.of(it) }?.let { out += bored.getValue(it).of(ru) }
        }
        issues(h).firstOrNull()?.let {
            out += if (ru) "${it.who} просит слова: «${it.text}»" else "${it.who} asks to speak: “${it.text}”"
        }
        val n = h.week.size
        val walked = (h.week.sumOf { Load.of(it) } / app.aloft.domain.Journey.PER_LOAD).toInt()
        out += if (ru) "Тренировок — $n, дней отдыха — ${h.weekCalms}. За неделю пройдено ${versts(walked, true)}, остров на высоте ${metres(h.isle.metres)} м."
        else "Trainings: $n, rest days: ${h.weekCalms}. ${versts(walked, false).replaceFirstChar { it.uppercase() }} covered this week; the island is ${metres(h.isle.metres)} m up."
        val guests = h.week.mapNotNull { Guest.of(it.guest) }.distinct()
        if (guests.isNotEmpty()) out += (if (ru) "Заглядывали: " else "Guests this week: ") + guests.joinToString(", ") { it.title.of(ru) } + "."
        val done = h.requests.count { it.done }
        out += if (ru) "Жители благодарят: выполнено просьб — $done из ${h.requests.size}." else "The islanders say thank you: $done of ${h.requests.size} requests done."
        return out
    }
}
