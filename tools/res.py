"""Writes the string resources (English and Russian) from one table, so the two never drift apart.

python tools/res.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"

S = [
    ("app_name", "Aloft", "Летучий остров"),
    ("app_title", "ALOFT", "ОСТРОВ"),
    ("channel", "Council of the elements", "Совет стихий"),
    ("tab_isle", "Island", "Остров"),
    ("tab_map", "Voyage", "Путь"),
    ("tab_log", "Log", "Журнал"),
    ("tab_spirits", "Spirits", "Духи"),
    ("height_m", "%1$s m", "%1$s м"),
    # island
    ("log_training", "TRAINING", "ТРЕНИРОВКА"),
    ("calm_on", "REST DAY", "ОТДЫХ"),
    ("calm_off", "NOT RESTING", "НЕ ОТДЫХ"),
    ("anchor_down", "Ill or injured? Drop anchor", "Болею — бросить якорь"),
    ("anchor_up", "Weigh anchor", "Поднять якорь"),
    ("requests_title", "Islanders' requests", "Просьбы жителей"),
    ("requests_hint", "New requests every Monday. Done ones hang a pennant under the island.", "Новые просьбы — каждый понедельник. За выполненные под островом вешают флажки."),
    ("req_done", "DONE", "ГОТОВО"),
    ("st_spark", "spark", "искра"),
    ("st_ember", "ember", "огонёк"),
    ("st_spirit", "spirit", "дух"),
    ("st_great", "great spirit", "великий дух"),
    ("arrived", "The island has arrived", "Остров прибыл"),
    ("gift", "Gift: %1$s", "Подарок: %1$s"),
    ("onward", "ONWARD", "ДАЛЬШЕ"),
    ("new_guest", "A new guest", "Новый гость"),
    ("guest_when", "Comes for: %1$s", "Прилетает, когда: %1$s"),
    ("welcome", "WELCOME", "ДОБРО ПОЖАЛОВАТЬ"),
    ("ok", "OK", "ХОРОШО"),
    # log
    ("log_title", "Training", "Тренировка"),
    ("log_what", "What it was", "Что было"),
    ("own_add", "+ my own", "+ своя"),
    ("own_title", "My own kind of training", "Своя тренировка"),
    ("own_hint", "e.g. climbing gym", "например, скалодром"),
    ("own_whose", "Whose spirit does it feed?", "Чьего духа она кормит?"),
    ("own_pick", "Pick a spirit: the light will fly to it.", "Выбери духа — к нему полетит свет."),
    ("own_err_name", "What is it called?", "Как она называется?"),
    ("own_err_dup", "There's one like that already.", "Такая уже есть."),
    ("hint_air", "Air: long and steady — running, walking, skiing, hiking, cycling.", "Воздух — долгое и ровное: бег, ходьба, лыжи, походы, велосипед."),
    ("hint_earth", "Earth: heavy — weights, climbing, anything where you push and pull.", "Земля — тяжёлое: веса, лазанье, всё, где тянешь и толкаешь."),
    ("hint_water", "Water: anything in or on the water.", "Вода — всё, что в воде и на воде."),
    ("hint_wood", "Wood: flexibility, balance and breathing.", "Дерево — гибкость, равновесие и дыхание."),
    ("hint_fire", "Fire: bursts — sprints, fights, ball games, dancing.", "Огонь — взрывное: рывки, бои, игры с мячом, танцы."),
    ("add", "ADD", "ДОБАВИТЬ"),
    ("cancel", "CANCEL", "ОТМЕНА"),
    ("log_long", "How long", "Сколько"),
    ("log_hard", "How hard it felt, out of 10", "Как далось, из 10"),
    ("rpe_easy", "easy — barely a breath of steam", "легко — пар едва заметен"),
    ("rpe_mid", "steady — breathing evenly", "в меру — дышится ровно"),
    ("rpe_hard", "hard — clouds of steam", "тяжело — пар клубами"),
    ("rpe_max", "all out — flames from the nostrils", "на пределе — из ноздрей пламя"),
    ("log_wx", "Weather outside", "Погода за окном"),
    ("log_wx_now", "Now: %1$s, %2$d°", "Сейчас: %1$s, %2$d°"),
    ("log_when", "When it ended", "Когда закончилась"),
    ("when_now", "just now", "только что"),
    ("when_hour", "an hour ago", "час назад"),
    ("when_morning", "this morning", "утром"),
    ("when_evening", "this evening", "вечером"),
    ("when_yesterday", "yesterday", "вчера"),
    ("log_flies", "The light will fly to: %1$s.", "Свет полетит к духу: %1$s."),
    ("log_pick", "Pick what it was: the light flies to that spirit.", "Выбери, что было, — свет полетит к его духу."),
    ("log_go", "RELEASE THE LIGHT", "ОТПУСТИТЬ СВЕТ"),
    ("min_n", "%1$d min", "%1$d мин"),
    ("hours_only", "%1$d h", "%1$d ч"),
    ("hours_n", "%1$d h %2$d min", "%1$d ч %2$d мин"),
    ("wx_clear", "clear", "ясно"),
    ("wx_cloud", "cloudy", "облачно"),
    ("wx_rain", "rain", "дождь"),
    ("wx_snow", "snow", "снег"),
    ("wx_fog", "fog", "туман"),
    ("wx_storm", "thunder", "гроза"),
    # voyage
    ("season_n", "Season %1$d", "Сезон %1$d"),
    ("route_to", "Route to %1$s", "Путь до %1$s"),
    ("route_done", "%1$s of %2$s", "%1$s из %2$s"),
    ("route_next", "Next: %1$s, %2$s to go.", "Дальше — до %1$s, ещё %2$s."),
    ("route_hint", "Every training moves the island: minutes × effort. Any kind counts, yoga too.", "Остров движет любая тренировка: минуты × усилие. Йога тоже считается."),
    ("stop_reached", "reached %1$s", "прибыли %1$s"),
    ("stop_start", "the start", "начало пути"),
    ("seasons_past", "Seasons behind us: %1$d.", "Пройдено сезонов: %1$d."),
    ("close", "CLOSE", "ЗАКРЫТЬ"),
    # journal
    ("council", "Council of the elements", "Совет стихий"),
    ("council_sub", "this week, by the fire", "эта неделя, у костра"),
    ("this_week", "This week", "Эта неделя"),
    ("all_trainings", "All trainings", "Все тренировки"),
    ("no_trainings", "Nothing here yet. Log a training from the island.", "Пока пусто. Запиши тренировку на острове."),
    ("today", "today", "сегодня"),
    ("yesterday", "yesterday", "вчера"),
    ("more", "SHOW MORE", "ЕЩЁ"),
    ("guest_came", "%1$s flew in for it", "К ней прилетел гость: %1$s"),
    ("save", "SAVE", "СОХРАНИТЬ"),
    ("delete", "DELETE", "УДАЛИТЬ"),
    ("delete_sure", "SURE? DELETE", "ТОЧНО? УДАЛИТЬ"),
    # spirits
    ("guests_title", "Guests from the weather · %1$d of %2$d", "Гости из погоды · %1$d из %2$d"),
    ("guests_hint", "They fly in for a training in unusual weather or at an unusual hour.", "Прилетают на тренировку в необычную погоду или в необычный час."),
    ("feeds_him", "Feeds him", "Кормят его"),
    ("feeds_her", "Feeds her", "Кормят её"),
    ("own_remove_hint", "Tap one of your own (✕) to remove it from the list. Its trainings stay in the log.", "Нажми на свою (✕), чтобы убрать её из списка. Тренировки в журнале останутся."),
    ("four_weeks", "Last four weeks", "Последние четыре недели"),
    ("four_weeks_n", "Trainings: %1$d · hours: %2$s", "Тренировок: %1$d · часов: %2$s"),
    ("four_weeks_none", "None yet — the spirit glows as a spark.", "Пока ни одной — дух светится искрой."),
    ("stage_hint", "A spirit grows from what you did in the last weeks and slowly fades without it. Days at anchor don't count.", "Дух растёт от того, что было в последние недели, и без тренировок медленно слабеет. Дни на якоре не считаются."),
    ("visits_n", "visits: %1$d", "визитов: %1$d"),
    # settings
    ("settings", "Settings", "Настройки"),
    ("weather", "Weather", "Погода"),
    ("place_now", "Weather from: %1$s", "Погода: %1$s"),
    ("place_none", "No town: pick the weather by hand when logging.", "Город не выбран: погоду можно отметить вручную при записи."),
    ("place_hint", "With a town, the island sees the real weather: rain, snow and fog over it, and the guests they bring. Only the town's coordinates are sent (Open-Meteo, or MET Norway).", "С городом остров видит настоящую погоду: дождь, снег и туман над ним и гостей, которых они приносят. Уходят только координаты города (Open-Meteo или MET Norway)."),
    ("place_search", "Town", "Город"),
    ("find", "FIND", "НАЙТИ"),
    ("place_fail", "Couldn't search. Check the connection.", "Не удалось найти. Проверь интернет."),
    ("place_off", "NO TOWN", "БЕЗ ГОРОДА"),
    ("notify_title", "Notifications", "Уведомления"),
    ("notify_text", "Sunday evening: the council has met", "В воскресенье вечером: совет стихий собрался"),
    ("on", "ON", "ВКЛ"),
    ("off", "OFF", "ВЫКЛ"),
    ("language", "Language", "Язык"),
    ("lang_system", "System", "Системный"),
    ("how_title", "How it works", "Как это устроено"),
    ("how_text", "Each training is minutes × how hard it felt (1–10). It feeds its element's spirit and fades over about two weeks. The island's height is the average of all five; an imbalance shows as weather: drought, landslides, gusts, cracks.", "Каждая тренировка — это минуты × усилие (1–10). Она кормит духа своей стихии и выветривается примерно за две недели. Высота острова — среднее по всем пяти, а перекос виден по погоде: засуха, оползни, шквалы, трещины."),
    ("how_text2", "A storm means the load jumped too fast compared with the last four weeks (the acute-to-chronic ratio). It's a nudge to rest, not a punishment. While at anchor, nothing fades.", "Гроза значит, что нагрузка выросла слишком резко по сравнению с последними четырьмя неделями (соотношение острой и хронической нагрузки). Это подсказка отдохнуть, а не наказание. На якоре ничего не выветривается."),
    ("community", "Community", "Сообщество"),
    ("community_text", "Aloft is made by one person. Bugs, ideas and news go through the StillCraft bot in Telegram.", "«Летучий остров» делает один человек. Ошибки, идеи и новости — через бота StillCraft в Telegram."),
    ("community_bug", "Report a bug", "Сообщить об ошибке"),
    ("community_bug_sub", "Something broke or looks wrong", "Что-то сломалось или выглядит не так"),
    ("community_idea", "Suggest an idea", "Предложить идею"),
    ("community_idea_sub", "What would make it better", "Что сделало бы приложение лучше"),
    ("community_follow", "Follow updates", "Следить за обновлениями"),
    ("community_follow_sub", "A message when a new version is out", "Сообщение, когда выйдет новая версия"),
    ("community_chat", "StillCraft chat", "Чат StillCraft"),
    ("community_chat_sub", "Talk to other people who use the apps", "Поговорить с теми, кто пользуется приложениями"),
    ("about", "About", "О приложении"),
    ("about_text", "A training log where five elemental spirits keep your island in the sky. No accounts, no ads; everything stays on the phone.", "Журнал тренировок, где пять духов стихий держат твой остров в небе. Без аккаунтов и рекламы, всё хранится на телефоне."),
    ("credits", "Font m6x11 by Daniel Linssen, Cyrillic by Natto Games. Weather: Open-Meteo.com and MET Norway (CC BY 4.0). The load model is general advice, not medical advice.", "Шрифт m6x11 — Daniel Linssen, кириллица — Natto Games. Погода: Open-Meteo.com и MET Norway (CC BY 4.0). Модель нагрузки — общий ориентир, а не медицинский совет."),
    # onboarding
    ("ob_1", "This island floats above the clouds. Five spirits hold it up, and your training gives them strength.", "Этот остров парит над облаками. Его держат пять духов стихий, а силы им даёт твоя тренировка."),
    ("ob_1b", "Log a training and a light flies to its spirit. The more evenly you train, the higher the island rises and the more grows on it.", "Запиши тренировку — и свет полетит к духу её стихии. Чем ровнее тренируешься, тем выше поднимается остров и тем больше на нём растёт."),
    ("ob_2", "Every kind of training feeds one element.", "Каждый вид тренировки кормит свою стихию."),
    ("ob_2b", "Your own kinds of training can be added and given to any spirit.", "Свои виды тренировок можно добавить и отдать любому духу."),
    ("ob_3", "Where are you? With a town, the island sees the real weather, and rare guests fly in for training in rain, snow or frost.", "Где ты? С городом остров видит настоящую погоду, а на тренировки в дождь, снег или мороз прилетают редкие гости."),
    ("ob_3b", "Optional: without a town, mark the weather by hand.", "Можно и без города — тогда погоду отмечаешь сам."),
    ("ob_start", "CAST OFF", "ОТЧАЛИВАЕМ"),
    ("skip_start", "CAST OFF WITHOUT", "БЕЗ ГОРОДА"),
    ("next", "NEXT", "ДАЛЕЕ"),
    ("back", "BACK", "НАЗАД"),
    ("n_council_title", "The council of the elements has met", "Совет стихий собрался"),
]

A = {}
P = {}


def esc(s):
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "\\'").replace('"', '\\"').replace("\n", "\\n")
    return "\\" + s if s[:1] in "@?" else s


def write(lang):
    i = 1 if lang == "en" else 2
    out = ['<?xml version="1.0" encoding="utf-8"?>', "<!-- Generated by tools/res.py; edit the table there. -->", "<resources>"]
    for row in S:
        out.append(f'    <string name="{row[0]}">{esc(row[i])}</string>')
    out.append("</resources>")
    d = RES / ("values" if lang == "en" else "values-ru")
    d.mkdir(parents=True, exist_ok=True)
    (d / "strings.xml").write_text("\n".join(out) + "\n", encoding="utf-8")


if __name__ == "__main__":
    keys = [r[0] for r in S]
    assert len(keys) == len(set(keys)), [k for k in keys if keys.count(k) > 1]
    write("en")
    write("ru")
    print(len(S), "strings")
