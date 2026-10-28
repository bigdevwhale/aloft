# Aloft

A training log where five elemental spirits keep a floating island in the sky. Android, Kotlin + Compose, pixel art drawn in code.

- **Five elements.** Every kind of training feeds one spirit: long and steady work is air (sylph), heavy work is earth (golem), anything in water is water (undine), stretching and balance are wood (dryad), bursts and fights are fire (salamander). Your own kinds of training can be added and given to any spirit.
- **Load, honestly.** A training is minutes × how hard it felt (sRPE). It feeds its element and fades over about two weeks. The island's height is the average of all five. An imbalance shows as weather on the island: drought, landslides, gusts, cracks. A sudden jump against the last four weeks brings a storm and a nudge to rest. Rest days bring calm, and illness drops anchor so nothing fades.
- **The voyage.** Every training moves the island along a season's route past legendary islands — Laputa, Buyan, Avalon — each with a letter and a keepsake.
- **Islanders' requests.** Three small requests every week, the first from whoever's element is weakest.
- **Guests from the weather.** Training in rain, snow, fog, frost or heat, at dawn or at midnight, or trying something new brings rare guests.
- No accounts, no ads; everything stays on the phone. Weather (optional) from Open-Meteo or MET Norway; only a town's coordinates are sent.

Build: `./gradlew assembleDebug`. `-Pdemo` builds `app.aloft.demo` with eight weeks of training. `./gradlew testDebugUnitTest -Prender --tests '*RenderArt*'` draws every sprite, scene and screen to `app/build/art`. Strings live in `tools/res.py`, the icon in `tools/icon.py`.

Font m6x11 by Daniel Linssen, Cyrillic by Natto Games.
