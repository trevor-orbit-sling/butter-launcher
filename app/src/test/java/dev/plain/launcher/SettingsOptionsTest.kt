package dev.plain.launcher

import dev.plain.launcher.ui.Change
import dev.plain.launcher.ui.Option
import dev.plain.launcher.ui.option
import dev.plain.launcher.ui.weatherOption
import org.junit.Assert.*
import org.junit.Test

class SettingsOptionsTest {
    @Test fun `queued remote steps use the latest value for every enum control`() {
        checkSteps(LauncherSettings::theme) { copy(theme = it) }
        checkSteps(LauncherSettings::accent) { copy(accent = it) }
        checkSteps(LauncherSettings::background) { copy(background = it) }
        checkSteps(LauncherSettings::font) { copy(font = it) }
        checkSteps(LauncherSettings::textSize) { copy(textSize = it) }
        checkSteps(LauncherSettings::tileSize) { copy(tileSize = it) }
        checkSteps(LauncherSettings::tileShape) { copy(tileShape = it) }
        checkSteps(LauncherSettings::spacing) { copy(spacing = it) }
        checkSteps(LauncherSettings::showNames) { copy(showNames = it) }
        checkSteps(LauncherSettings::clock) { copy(clock = it) }
        checkSteps(LauncherSettings::rowOrder) { copy(rowOrder = it) }
        checkSteps(LauncherSettings::showWatching) { copy(showWatching = it) }
        checkSteps(LauncherSettings::showAllApps) { copy(showAllApps = it) }
        checkSteps(LauncherSettings::showWeather) { copy(showWeather = it) }
        checkSteps(LauncherSettings::tempUnit) { copy(tempUnit = it) }
    }

    @Test fun `queued theme steps apply the final theme defaults without resetting unrelated settings`() {
        val before = LauncherSettings(font = FontFace.DmSerif, hiddenApps = listOf("tv.example.hidden"), favourites = listOf("tv.example.player"))
        val queue = mutableListOf<Change>()
        val control = option("Theme", before, LauncherSettings::theme, queue::add) { changeTheme(it) }
        repeat(12) { control.adjust(1) }
        assertEquals(before.copy(theme = Theme.Orbital, accent = Accent.Copper, font = FontFace.SpaceGrotesk), queue.fold(before) { settings, change -> change(settings) })
    }

    @Test fun `action rows open with either direction instead of consuming a no-op`() {
        var opened = 0
        var stepped = 0
        val action = Option("Location", "Choose a city", open = { opened++ }, step = { stepped++ })
        action.adjust(-1)
        action.adjust(1)
        assertEquals(2, opened)
        assertEquals(0, stepped)
    }

    @Test fun `weather requires choosing valid coordinates before its toggle can enable`() {
        val queue = mutableListOf<Change>()
        var opened = 0
        for (settings in listOf(LauncherSettings(), LauncherSettings(showWeather = Toggle.On, lat = "NaN", lon = "0"))) {
            val control = weatherOption(settings, queue::add) { opened++ }
            assertNotNull(control.open)
            control.adjust(1)
        }
        assertEquals(2, opened)
        assertTrue(queue.isEmpty())
        val configured = LauncherSettings(lat = "51.5", lon = "-0.1")
        val control = weatherOption(configured, queue::add) { fail("Location already configured") }
        control.adjust(1)
        control.adjust(1)
        assertEquals(Toggle.Off, queue.fold(configured) { settings, change -> change(settings) }.showWeather)
        assertNotNull(queue.first()(configured).weatherQuery)
    }

    @Test fun `duplicate saved app IDs recover stable keys and repeated add stays idempotent`() {
        val recovered = decodeSettings("""{"theme":"Canopy","font":"Manrope","favourites":["tv.example.b","tv.example.a","tv.example.b",""],"hiddenApps":["tv.example.a"," ","tv.example.a"]}""")
        assertEquals(listOf("tv.example.b", "tv.example.a"), recovered.favourites)
        assertEquals(listOf("tv.example.a"), recovered.hiddenApps)
        assertEquals(Theme.Canopy, recovered.theme)
        assertEquals(FontFace.Manrope, recovered.font)
        val once = recovered.addFavourite("tv.example.c")
        assertSame(once, once.addFavourite("tv.example.c"))
        assertEquals(recovered.copy(favourites = recovered.favourites + "tv.example.c"), once)
    }

    private inline fun <reified E> checkSteps(
        crossinline read: (LauncherSettings) -> E,
        crossinline set: LauncherSettings.(E) -> LauncherSettings,
    ) where E : Enum<E>, E : Choice {
        val before = LauncherSettings()
        val queue = mutableListOf<Change>()
        val control = option("Setting", before, read, queue::add, set)
        val count = enumValues<E>().size + 3
        repeat(count) { control.adjust(1) }
        assertEquals(read(before).cycle(count), read(queue.fold(before) { settings, change -> change(settings) }))
        repeat(count) { control.adjust(-1) }
        assertEquals(before, queue.fold(before) { settings, change -> change(settings) })
    }
}
