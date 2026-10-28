package app.aloft

import app.aloft.domain.Building
import app.aloft.domain.Decor
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Issue
import app.aloft.domain.Journey
import app.aloft.domain.Mode
import app.aloft.domain.Wx
import app.aloft.ui.art.Buf
import app.aloft.ui.art.CampfireScene
import app.aloft.ui.art.ChartScene
import app.aloft.ui.art.Decors
import app.aloft.ui.art.Guests
import app.aloft.ui.art.IslandScene
import app.aloft.ui.art.IsleLook
import app.aloft.ui.art.Spirits
import app.aloft.ui.art.TimeOfDay
import app.aloft.ui.art.blit
import app.aloft.ui.art.rgb
import app.aloft.ui.art.toBitmap
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Sheets of every sprite and scene, drawn to PNGs for a look (run with `-Prender`). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderArt {
    private val out = File(System.getProperty("render.out") ?: "build/art").apply { mkdirs() }

    private fun save(b: Buf, name: String, scale: Int = 4) {
        val big = android.graphics.Bitmap.createScaledBitmap(b.toBitmap(), b.w * scale, b.h * scale, false)
        File(out, "$name.png").outputStream().use { big.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun spirits() {
        val levels = listOf(.3, .6, .9)
        val b = Buf(30 * 8, 30 * 5)
        b.r(0, 0, b.w, b.h, rgb(0xF4ECD8))
        Element.entries.forEachIndexed { i, e ->
            levels.forEachIndexed { j, v -> b.blit(Spirits.draw(e, v, j), j * 30 + 1, i * 30 + 1) }
            b.blit(Spirits.draw(e, .6, 0, sleep = true), 3 * 30 + 1, i * 30 + 1)
            for (f in 0..3) b.blit(Spirits.draw(e, .6, f, small = true), 4 * 30 + f * 30 + 1, i * 30 + 1)
        }
        save(b, "spirits")
    }

    @Test fun guests() {
        val b = Buf(30 * Guest.entries.size, 30 * 2)
        b.r(0, 0, b.w, b.h, rgb(0x78A8F0))
        Guest.entries.forEachIndexed { i, g -> for (f in 0..1) b.blit(Guests.draw(g, f * 2), i * 30 + 1, f * 30 + 1) }
        save(b, "guests")
    }

    @Test fun decors() {
        val b = Buf(20 * Decor.entries.size, 44)
        b.r(0, 0, b.w, b.h, rgb(0x9AD85A))
        b.r(0, 0, b.w, 36, rgb(0x78A8F0))
        Decor.entries.forEachIndexed { i, d -> Decors.draw(b, d, i * 20 + 10, 36, 5) }
        save(b, "decors")
    }

    private fun lv(a: Double, e: Double, w: Double, wo: Double, f: Double) =
        mapOf(Element.AIR to a, Element.EARTH to e, Element.WATER to w, Element.WOOD to wo, Element.FIRE to f)

    @Test fun islands() {
        fun isle(name: String, look: IsleLook) = save(IslandScene(look).frame(9), name, 3)
        val all = Building.entries.toSet()
        isle("isle-even", IsleLook(lv(.62, .5, .46, .48, .4), Mode.DAY, emptySet(), setOf(Building.MILL, Building.FORGE, Building.GROVE, Building.GARDEN), .62, TimeOfDay.DAY,
            decor = setOf(Decor.FEATHER, Decor.TELESCOPE, Decor.OAK), bunting = listOf(rgb(0xA8D8FF), rgb(0xF07030)), horizon = .6))
        isle("isle-great", IsleLook(lv(.9, .85, .8, .78, .82), Mode.DAY, emptySet(), all, .98, TimeOfDay.EVENING, decor = Decor.entries.toSet()))
        isle("isle-drought", IsleLook(lv(.4, .55, .1, .4, .8), Mode.DAY, setOf(Issue.DROUGHT), setOf(Building.FORGE), .5, TimeOfDay.DAY))
        isle("isle-low", IsleLook(lv(.1, .08, .05, .1, .06), Mode.DAY, setOf(Issue.LOW), emptySet(), .1, TimeOfDay.NIGHT))
        isle("isle-storm", IsleLook(lv(.62, .5, .46, .48, .4), Mode.STORM, emptySet(), setOf(Building.MILL, Building.FORGE), .62, TimeOfDay.DAY, guest = Guest.THUNDERBIRD))
        isle("isle-calm", IsleLook(lv(.62, .5, .46, .48, .4), Mode.CALM, emptySet(), setOf(Building.MILL), .6, TimeOfDay.MORNING, wx = Wx.SNOW, guest = Guest.SNOWOWL))
        isle("isle-anchor", IsleLook(lv(.5, .4, .3, .3, .3), Mode.ANCHOR, setOf(Issue.SLIDE), emptySet(), .4, TimeOfDay.DAY, wx = Wx.RAIN))
    }

    @Test fun chart() {
        save(ChartScene(Journey.route(0), 410, 0).frame(), "chart", 3)
        save(CampfireScene(lv(.62, .5, .2, .48, .08)).frame(), "campfire", 3)
    }
}
