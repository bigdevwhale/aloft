package app.aloft.domain

/** Rare guests who fly in when a training happens in unusual weather or at an unusual hour. */
enum class Guest(val title: Tx, val whenTx: Tx, val about: Tx) {
    WANDERER(Tx("The Wanderer", "Странник"), Tx("the first training of a new kind", "первая тренировка нового вида"),
        Tx("A traveller in a cloak with a lantern. He turns up whenever you try something for the first time, and tells what he has seen on other islands.",
            "Путник в плаще с фонарём. Приходит, когда ты пробуешь что-то впервые, и рассказывает, что видел на других островах.")),
    THUNDERBIRD(Tx("Thunderbird", "Громовая птица"), Tx("a training at 9–10 out of 10, or in a thunderstorm", "тренировка на 9–10 из 10 или в грозу"),
        Tx("Comes with the storm. Respects those who gave it everything — and reminds them that after a storm comes the calm.",
            "Прилетает вместе с грозой. Уважает тех, кто выложился до конца, — и напоминает, что после грозы нужен штиль.")),
    FROST(Tx("Jack Frost", "Морозко"), Tx("a training at −10° or colder", "тренировка в мороз от −10°"),
        Tx("A small old man with a frosty beard. Paints ferns on the windows of the mill and taps the spirits' noses to make them pink.",
            "Маленький старичок с инеем в бороде. Рисует папоротники на окнах мельницы и щёлкает духов по носу, чтобы румянились.")),
    SUNHARE(Tx("Sunbeam hare", "Солнечный заяц"), Tx("a training in heat over +27°", "тренировка в жару выше +27°"),
        Tx("Jumps from wall to wall and never sits still. The salamander tries to catch him every time and never can.",
            "Прыгает со стены на стену и минуты не сидит на месте. Саламандра каждый раз пытается его поймать — и никак.")),
    SNOWOWL(Tx("Snow owl", "Снежная сова"), Tx("a training while it snows", "тренировка в снегопад"),
        Tx("Sits on the mill and stares without blinking. They say she brings silence to the island.",
            "Садится на мельницу и смотрит не мигая. Говорят, приносит на остров тишину.")),
    MISTLING(Tx("Mistling", "Туманник"), Tx("a training in fog", "тренировка в туман"),
        Tx("Shy. You only see him out of the corner of your eye — and only if you went out into the fog.",
            "Застенчивый. Его видно только краем глаза — и только если ты сам вышел в туман.")),
    DRIZZLE(Tx("Drizzle", "Дождевик"), Tx("a training in the rain", "тренировка под дождём"),
        Tx("A little cloud with a temper. Comes to those who were not afraid of wet weather, and waters the garden on the way.",
            "Маленькая тучка с характером. Прилетает к тем, кто не испугался мокрой погоды, и заодно поливает сад.")),
    ROBIN(Tx("Dawn robin", "Зорянка"), Tx("a training that starts before 8 in the morning", "тренировка, начатая до 8 утра"),
        Tx("A bird that gets up before the sun. Sings so that the sylph starts flying in circles.",
            "Птичка, которая встаёт раньше солнца. Поёт так, что сильф начинает носиться кругами.")),
    FIREFLY(Tx("Midnight firefly", "Полуночник"), Tx("a training that starts after 9 at night", "тренировка, начатая после 9 вечера"),
        Tx("A firefly the size of an apple. Lights the paths while the islanders sleep.",
            "Светлячок размером с яблоко. Освещает тропинки, пока жители спят."));

    companion object {
        fun of(s: String?) = entries.firstOrNull { it.name == s }

        /** Who flies in for a training, if anyone; the rarest condition wins. */
        fun pick(firstOfKind: Boolean, rpe: Int, wx: Wx?, temp: Double?, startHour: Int): Guest? = when {
            firstOfKind -> WANDERER
            rpe >= 9 || wx == Wx.STORM -> THUNDERBIRD
            temp != null && temp <= -10 -> FROST
            temp != null && temp >= 27 -> SUNHARE
            wx == Wx.SNOW -> SNOWOWL
            wx == Wx.FOG -> MISTLING
            wx == Wx.RAIN -> DRIZZLE
            startHour in 4..7 -> ROBIN
            startHour >= 21 || startHour < 4 -> FIREFLY
            else -> null
        }
    }
}
