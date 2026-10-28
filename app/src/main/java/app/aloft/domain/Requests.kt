package app.aloft.domain

import app.aloft.data.Workout
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Who asks: an islander tied to an element's work, or the lighthouse keeper who watches it all. */
enum class Islander(val title: Tx, val element: Element?) {
    MILLER(Tx("The miller", "Мельник"), Element.AIR),
    MASON(Tx("The stonemason", "Каменотёс"), Element.EARTH),
    HERBALIST(Tx("The herbalist", "Травница"), Element.WATER),
    FORESTER(Tx("The forester", "Лесничий"), Element.WOOD),
    SMITH(Tx("The smith", "Кузнец"), Element.FIRE),
    KEEPER(Tx("The lighthouse keeper", "Смотритель маяка"), null),
}

/** What a request wants done within its week. */
sealed interface Need {
    /** [n] trainings of an element. */
    data class Count(val e: Element, val n: Int) : Need
    /** [min] minutes of an element in all. */
    data class Minutes(val e: Element, val min: Int) : Need
    /** [n] trainings at 7/10 or harder. */
    data class Hard(val n: Int) : Need
    /** [n] different elements. */
    data class Mix(val n: Int) : Need
    /** A day of rest. */
    data object Rest : Need
    /** A training that started before nine in the morning. */
    data object Early : Need
}

class Request(val id: String, val who: Islander, val need: Need, val text: Tx, val thanks: Tx) {
    val goal: Int get() = when (need) {
        is Need.Count -> need.n
        is Need.Minutes -> need.min
        is Need.Hard -> need.n
        is Need.Mix -> need.n
        Need.Rest, Need.Early -> 1
    }
}

/** A request and how far along it is this week. */
class Asked(val req: Request, val have: Int) {
    val done get() = have >= req.goal
}

object Requests {
    private fun r(id: String, who: Islander, need: Need, en: String, ru: String, ten: String, tru: String) = Request(id, who, need, Tx(en, ru), Tx(ten, tru))

    val all = listOf(
        r("air_two", Islander.MILLER, Need.Count(Element.AIR, 2),
            "The mill sails have stopped. Some wind, please: two runs or walks this week.", "Крылья мельницы встали. Ветра бы — две пробежки или прогулки за неделю.",
            "The mill is grinding! Here's a warm loaf.", "Мельница мелет! Держи горячий каравай."),
        r("air_90", Islander.MILLER, Need.Minutes(Element.AIR, 90),
            "An hour and a half on your feet this week, and I'll grind all the flour before winter.", "Полтора часа на ногах за неделю — и я смелю всю муку до зимы.",
            "The flour is ground for the winter. Thank you!", "Мука смолота до зимы. Спасибо!"),
        r("earth_two", Islander.MASON, Need.Count(Element.EARTH, 2),
            "The north edge is crumbling. Two strength sessions and I'll shore it up.", "Северный край крошится. Две силовые — и я его подопру.",
            "Shored up! The stone sat like it grew there.", "Подпёр! Камень лёг как влитой."),
        r("earth_one", Islander.MASON, Need.Count(Element.EARTH, 1),
            "I need one heavy stone for the steps. Just one strength session.", "Нужен один тяжёлый камень для ступеней. Хотя бы одна силовая.",
            "The steps are done: you can walk up to the mill without puffing now.", "Ступени готовы — теперь к мельнице можно подняться не запыхавшись."),
        r("water_one", Islander.HERBALIST, Need.Count(Element.WATER, 1),
            "The spring barely trickles. Swim just once and it will come back to life.", "Родник еле течёт. Поплавай хоть разок — оживёт.",
            "The spring is babbling! The garden is drinking.", "Родник журчит! Сад пьёт."),
        r("water_60", Islander.HERBALIST, Need.Minutes(Element.WATER, 60),
            "An hour in the water this week and I'll fill the pond to the brim.", "Час в воде за неделю — и я наполню пруд до краёв.",
            "The pond is full and the lilies are open.", "Пруд полон, лилии раскрылись."),
        r("wood_two", Islander.FORESTER, Need.Count(Element.WOOD, 2),
            "The trees creak like old doors. Two stretching sessions and the grove will thaw.", "Деревья скрипят, как старые двери. Две растяжки — и роща оттает.",
            "The grove rustles softly now. Hear it?", "Роща шумит мягко. Слышишь?"),
        r("wood_one", Islander.FORESTER, Need.Count(Element.WOOD, 1),
            "Just one yoga or stretching session: I need to replant the young oaks.", "Хоть одна йога или растяжка — мне нужно пересадить молодые дубки.",
            "The oaks are replanted; there was room for all.", "Дубки пересажены, всем хватило места."),
        r("fire_hard", Islander.SMITH, Need.Hard(1),
            "The forge is cooling. One hard training, seven out of ten or more, and I'll fire it up again.", "Горн остывает. Одна тяжёлая тренировка — от семи из десяти — и снова раздую.",
            "The forge roars! I'll make you a horseshoe for luck.", "Горн гудит! Выкую тебе подкову на счастье."),
        r("fire_two", Islander.SMITH, Need.Count(Element.FIRE, 2),
            "Two hot trainings this week — intervals, a fight, a ball game — and I'll finish the weathervane.", "Две жаркие тренировки за неделю — интервалы, бой, мяч — и я закончу флюгер.",
            "The weathervane is on the roof! Look, it spins.", "Флюгер на крыше! Смотри, крутится."),
        r("mix3", Islander.KEEPER, Need.Mix(3),
            "Three different elements this week and the lighthouse will burn in three colours.", "Три разные стихии за неделю — и маяк засветит трёхцветным огнём.",
            "Tonight the lighthouse burns in three colours.", "Сегодня ночью маяк горит тремя цветами."),
        r("mix4", Islander.KEEPER, Need.Mix(4),
            "Four elements in a week. Hard, I know. But then they'll see our light from Buyan.", "Четыре стихии за неделю. Трудно, знаю. Зато тогда наш огонь увидят с Буяна.",
            "Buyan sent a light back!", "С Буяна прислали ответный огонёк!"),
        r("rest", Islander.KEEPER, Need.Rest,
            "At least one day of rest this week. Spirits need their sleep too.", "Хоть один день отдыха на этой неделе. Духам тоже надо выспаться.",
            "The calm went well. The spirits slept and are full of beans.", "Штиль удался. Духи выспались и бодрые."),
        r("early", Islander.KEEPER, Need.Early,
            "One training before nine in the morning — I'll meet you with a lantern.", "Одна тренировка до девяти утра — встречу тебя с фонарём.",
            "That was a fine dawn, wasn't it?", "Хороший был рассвет, правда?"),
    )

    fun byId(id: String) = all.firstOrNull { it.id == id }

    fun monday(d: LocalDate): LocalDate = d.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

    /**
     * The week's three requests: one from the islander of the weakest element, one from another
     * islander, and one from the lighthouse keeper. The same all week.
     */
    fun of(week: LocalDate, level: Map<Element, Double>): List<Request> {
        val seed = week.toEpochDay().toInt()
        fun pick(list: List<Request>, salt: Int) = list[Math.floorMod(seed * 31 + salt, list.size)]
        val weak = Element.entries.minBy { (level[it] ?: 0.0) + it.ordinal * 1e-6 }
        val first = pick(all.filter { it.who.element == weak }, 1)
        val others = Element.entries.filter { it != weak }
        val second = pick(all.filter { it.who.element == others[Math.floorMod(seed, others.size)] }, 2)
        val third = pick(all.filter { it.who == Islander.KEEPER }, 3)
        return listOf(first, second, third)
    }

    /** How far each request has got, from the week's trainings and rest days. */
    fun progress(reqs: List<Request>, week: List<Workout>, calmDays: Int, zone: ZoneId): List<Asked> = reqs.map { r ->
        val have = when (val n = r.need) {
            is Need.Count -> week.count { Element.of(it.element) == n.e }
            is Need.Minutes -> week.filter { Element.of(it.element) == n.e }.sumOf { it.minutes }
            is Need.Hard -> week.count { it.rpe >= 7 }
            is Need.Mix -> week.map { it.element }.distinct().size
            Need.Rest -> calmDays
            Need.Early -> week.count { Instant.ofEpochMilli(it.at - it.minutes * 60_000L).atZone(zone).hour < 9 }
        }
        Asked(r, have)
    }
}
