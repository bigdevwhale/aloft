package app.aloft.domain

/** Text in both languages; the app picks by the current locale. */
data class Tx(val en: String, val ru: String) {
    fun of(ru: Boolean) = if (ru) this.ru else en
}

/**
 * The five elements whose spirits hold the island up. Every kind of training feeds one of them:
 * long and steady work is air, heavy work is earth, anything in water is water, stretching and
 * balance are wood, bursts and fights are fire.
 */
enum class Element(val color: Long, val spirit: Tx, val word: Tx, val female: Boolean) {
    AIR(0xFFA8D8FF, Tx("Sylph", "Сильф"), Tx("Air", "Воздух"), false),
    EARTH(0xFFC8A070, Tx("Golem", "Голем"), Tx("Earth", "Земля"), false),
    WATER(0xFF4AA8E8, Tx("Undine", "Ундина"), Tx("Water", "Вода"), true),
    WOOD(0xFF7AC850, Tx("Dryad", "Дриада"), Tx("Wood", "Дерево"), true),
    FIRE(0xFFF07030, Tx("Salamander", "Саламандра"), Tx("Fire", "Огонь"), true);

    companion object {
        fun of(s: String): Element = entries.firstOrNull { it.name == s } ?: AIR
    }
}

/** A kind of training: the built-in ones by key, the user's own as "own_<id>". */
data class Sport(val key: String, val name: Tx, val element: Element, val own: Boolean = false)

object Sports {
    private fun s(key: String, en: String, ru: String, e: Element) = Sport(key, Tx(en, ru), e)

    val builtIn: List<Sport> = listOf(
        s("run", "Running", "Бег", Element.AIR),
        s("walk", "Walking", "Ходьба", Element.AIR),
        s("hike", "Hiking", "Поход", Element.AIR),
        s("ski", "Skiing", "Лыжи", Element.AIR),
        s("rope", "Skipping rope", "Скакалка", Element.AIR),
        s("bike", "Cycling", "Велосипед", Element.AIR),
        s("gym", "Weights", "Силовая", Element.EARTH),
        s("bar", "Pull-up bar", "Турник", Element.EARTH),
        s("kettle", "Kettlebells", "Гири", Element.EARTH),
        s("climb", "Climbing", "Скалолазание", Element.EARTH),
        s("body", "Bodyweight", "Свой вес", Element.EARTH),
        s("swim", "Swimming", "Плавание", Element.WATER),
        s("row", "Rowing", "Гребля", Element.WATER),
        s("sup", "Paddleboard", "Сап", Element.WATER),
        s("aqua", "Aqua aerobics", "Аквааэробика", Element.WATER),
        s("yoga", "Yoga", "Йога", Element.WOOD),
        s("stretch", "Stretching", "Растяжка", Element.WOOD),
        s("pilates", "Pilates", "Пилатес", Element.WOOD),
        s("taichi", "Tai chi", "Тайцзи", Element.WOOD),
        s("hiit", "Intervals", "Интервалы", Element.FIRE),
        s("box", "Boxing", "Бокс", Element.FIRE),
        s("fight", "Wrestling", "Борьба", Element.FIRE),
        s("ball", "Ball games", "Игры с мячом", Element.FIRE),
        s("dance", "Dancing", "Танцы", Element.FIRE),
    )

    fun byKey(key: String, own: List<Sport>): Sport? = builtIn.firstOrNull { it.key == key } ?: own.firstOrNull { it.key == key }
}
