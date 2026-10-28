package app.aloft.domain

import app.aloft.data.Workout

/** Small things the island keeps from its travels and its islanders' thanks; each is drawn on it. */
enum class Decor { FEATHER, TELESCOPE, OAK, LANTERN, CRYSTAL, KITE, APPLE, WELL, AURORA, HIVE, ORRERY, NEST, TENT, SIGNPOST, BIRDHOUSE, SWING, BELL, WINDVANE }

/** A stop on the voyage: [at] versts from the start of the season. */
class Stop(val at: Int, val name: Tx, val to: Tx, val letter: Tx, val gift: Tx, val decor: Decor?)

object Journey {
    /** Versts per unit of session load: a 45-minute training at 6/10 carries the island about fifty. */
    const val PER_LOAD = 5.0

    private val home = Stop(0, Tx("Home Shallows", "Родная отмель"), Tx("Home Shallows", "Родной отмели"),
        Tx("This is where the voyage begins: shallow clouds, a warm wind, and not a single guest yet. The spirits have checked the ropes. Off we go.",
            "Отсюда начинается путь: мелкие облака, тёплый ветер и ни одного гостя. Духи проверили канаты. Отчаливаем."),
        Tx("", ""), null)

    val routes: List<List<Stop>> = listOf(
        listOf(
            home,
            Stop(140, Tx("Goose Rookery", "Гусиное гнездовье"), Tx("the Goose Rookery", "Гусиного гнездовья"),
                Tx("Cloud geese build their nests straight out of fog here. The old gander looked our island over for a long time, then left a single feather on the flagpole. For luck, the geese say.",
                    "Облачные гуси вьют гнёзда прямо из тумана. Старый гусак долго разглядывал наш остров, а потом оставил на флагштоке одно перо. На удачу, говорят гуси."),
                Tx("a goose feather on the flagpole", "гусиное перо на флагштоке"), Decor.FEATHER),
            Stop(320, Tx("Laputa", "Лапута"), Tx("Laputa", "Лапуты"),
                Tx("A city of scholars hanging on one enormous magnet. They let us into the observatory and showed us Buyan on their charts. As we left, they gave us a spyglass so we would not lose our way.",
                    "Город учёных висит на одном огромном магните. Нас пустили в обсерваторию и показали на картах Буян. На прощание подарили подзорную трубу, чтобы мы не сбились с курса."),
                Tx("a spyglass from the observatory", "подзорная труба из обсерватории"), Decor.TELESCOPE),
            Stop(540, Tx("Buyan Island", "Остров Буян"), Tx("Buyan", "Буяна"),
                Tx("Under the old oak lies the white burning stone. The golem sat beside it and said nothing for an hour — they must be kin. The dryad begged an acorn and planted it at once.",
                    "Под старым дубом лежит бел-горюч камень. Голем сел рядом и целый час молчал — видно, родня. Дриада выпросила жёлудь и сразу его посадила."),
                Tx("an oak sapling from Buyan", "дубок с Буяна"), Decor.OAK),
            Stop(780, Tx("Lighthouse of Winds", "Маяк Ветров"), Tx("the Lighthouse of Winds", "Маяка Ветров"),
                Tx("The keeper lights his lamp from lightning. The salamander made friends with him straight away and came back covered in sparks, carrying a lantern of the lighthouse's own fire.",
                    "Смотритель маяка зажигает огонь от молний. Саламандра сразу с ним подружилась и вернулась вся в искрах — с фонарём, в котором горит огонь маяка."),
                Tx("a lantern of the lighthouse's fire", "фонарь с огнём маяка"), Decor.LANTERN),
            Stop(1040, Tx("Glass Mountain", "Стеклянная гора"), Tx("the Glass Mountain", "Стеклянной горы"),
                Tx("Nobody has ever climbed it: the slopes are smooth as ice. The sylph flew round the top three times, and the mountain, pleased, let a crystal fall to us.",
                    "На неё никто не взбирался: склоны гладкие, как лёд. Сильф трижды облетел вершину, и гора на радостях уронила нам кристалл."),
                Tx("a crystal from the Glass Mountain", "кристалл со Стеклянной горы"), Decor.CRYSTAL),
            Stop(1320, Tx("Rainbow Fair", "Ярмарка на Радуге"), Tx("the Rainbow Fair", "Ярмарки на Радуге"),
                Tx("Islands from all over the sky trade here, on the arch of a rainbow. The undine haggled for a kite that never comes down. The islanders cheered.",
                    "Сюда со всего неба слетаются острова торговать прямо на дуге радуги. Ундина сторговала воздушного змея, который никогда не падает. Жители ликовали."),
                Tx("a kite that never comes down", "воздушный змей, который не падает"), Decor.KITE),
            Stop(1600, Tx("Avalon", "Авалон"), Tx("Avalon", "Авалона"),
                Tx("Apple orchards above the clouds. Islands come here at the end of the season. The spirits are asleep under the trees, and this year's log is shut with a golden clasp. A new road starts tomorrow.",
                    "Яблоневые сады над облаками. Сюда острова приплывают к концу сезона. Духи спят под яблонями, а летопись года закрыта на золотую застёжку. Завтра — новая дорога."),
                Tx("an apple tree from Avalon", "яблоня с Авалона"), Decor.APPLE),
        ),
        listOf(
            home,
            Stop(140, Tx("Belovodye", "Беловодье"), Tx("Belovodye", "Беловодья"),
                Tx("A land of white water that everyone looks for and few find. We found it because we were not looking. The undine filled a well with that water; it never runs dry.",
                    "Страна белых вод, которую все ищут и мало кто находит. Мы нашли, потому что не искали. Ундина наполнила этой водой колодец — он никогда не пересыхает."),
                Tx("a well of white water", "колодец белой воды"), Decor.WELL),
            Stop(320, Tx("Hyperborea", "Гиперборея"), Tx("Hyperborea", "Гипербореи"),
                Tx("Far beyond the north wind, where the sun does not set for half a year. The locals keep the northern lights in jars and gave us one. On grey evenings it glows green.",
                    "Далеко за северным ветром, где солнце полгода не садится. Местные держат северное сияние в банках и подарили нам одну. В серые вечера она светится зелёным."),
                Tx("a jar of northern lights", "банка северного сияния"), Decor.AURORA),
            Stop(540, Tx("Sky Apiaries", "Небесные пасеки"), Tx("the Sky Apiaries", "Небесных пасек"),
                Tx("Bees here gather nectar from clouds, and the honey tastes of rain. The beekeeper gave us a hive. The dryad has already found it a place among the flowers.",
                    "Здешние пчёлы собирают нектар с облаков, и мёд пахнет дождём. Пасечник подарил нам улей. Дриада уже нашла ему место среди цветов."),
                Tx("a beehive", "улей"), Decor.HIVE),
            Stop(780, Tx("Stargazer's Spire", "Звездочётова башня"), Tx("the Stargazer's Spire", "Звездочётовой башни"),
                Tx("An old stargazer counts the stars every night and is always one short. We helped him count; it came out right. He gave us a little brass orrery.",
                    "Старый звездочёт каждую ночь пересчитывает звёзды, и всегда одной не хватает. Мы помогли — сошлось. Он подарил нам маленькую медную модель неба."),
                Tx("a brass orrery", "медная модель неба"), Decor.ORRERY),
            Stop(1040, Tx("Iriy", "Ирий"), Tx("Iriy", "Ирия"),
                Tx("Where the birds fly for the winter. It is warm and loud. A pair of swifts asked to come with us, and now they live in a nest under the roof.",
                    "Туда улетают на зиму птицы. Тепло и шумно. Пара стрижей попросилась с нами — теперь живут в гнезде под крышей."),
                Tx("a swifts' nest", "гнездо стрижей"), Decor.NEST),
            Stop(1320, Tx("Floating Market", "Летучий рынок"), Tx("the Floating Market", "Летучего рынка"),
                Tx("Stalls on balloons, haggling in the wind. The golem bought a striped tent and refuses to explain why. He likes to sit in it, that's all.",
                    "Лавки на воздушных шарах, торг на ветру. Голем купил полосатый шатёр и не объясняет зачем. Сидит в нём, и всё."),
                Tx("a striped tent", "полосатый шатёр"), Decor.TENT),
            Stop(1600, Tx("World's End", "Край Света"), Tx("World's End", "Края Света"),
                Tx("There is a signpost here: one arrow points back, the other says «further». The season is over; the spirits are tired and proud. Tomorrow we turn round and fly home the long way.",
                    "Здесь стоит указатель: одна стрелка смотрит назад, на другой написано «дальше». Сезон окончен, духи устали и гордятся собой. Завтра развернёмся и полетим домой длинной дорогой."),
                Tx("the signpost from World's End", "указатель с Края Света"), Decor.SIGNPOST),
        ),
    )

    fun route(season: Int) = routes[season % routes.size]

    /** Versts covered since [start]. */
    fun distance(ws: List<Workout>, start: Long): Int = (ws.filter { it.at >= start }.sumOf { Load.of(it) } / PER_LOAD).toInt()

    /** The last stop reached and the next one ahead (null at the end of the route). */
    fun position(season: Int, dist: Int): Pair<Int, Stop?> {
        val r = route(season)
        val i = r.indexOfLast { it.at <= dist }
        return i to r.getOrNull(i + 1)
    }

    /** Islanders' thanks add things too: these many thanks for each. */
    val thanksDecor = listOf(3 to Decor.BIRDHOUSE, 8 to Decor.SWING, 15 to Decor.BELL, 25 to Decor.WINDVANE)

    val thanksGift = mapOf(
        Decor.BIRDHOUSE to Tx("a birdhouse from the islanders", "скворечник от жителей"),
        Decor.SWING to Tx("a swing on the old tree", "качели на старом дереве"),
        Decor.BELL to Tx("a bell that rings in the wind", "колокол, звонящий на ветру"),
        Decor.WINDVANE to Tx("a weathervane shaped like a salamander", "флюгер-саламандра"),
    )
}
