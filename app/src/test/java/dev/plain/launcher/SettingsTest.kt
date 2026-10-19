package dev.plain.launcher

import android.view.KeyEvent
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class SettingsTest {
    @Test fun `fresh install has no personal services location or favourites`() {
        val settings = LauncherSettings()
        assertTrue(settings.favourites.isEmpty())
        assertFalse(settings.showWeather.on)
        assertNull(settings.weatherQuery)
        assertEquals("", settings.place)
        assertFalse(settings.tutorialComplete)
    }

    @Test fun `corrupt unknown and newer settings recover safely`() {
        assertEquals(LauncherSettings(), decodeSettings("bad json"))
        assertEquals(Theme.Glass, decodeSettings("""{"theme":"Unknown","future":true}""").theme)
        val configured = LauncherSettings(tutorialComplete = true, favourites = listOf("tv.example.player"), remoteBindings = RemoteBindings(customise = KeyEvent.KEYCODE_INFO))
        assertEquals(configured, decodeSettings(settingsJson.encodeToString(configured)))
    }

    @Test fun `weather needs an explicit valid location`() {
        assertNull(LauncherSettings(showWeather = Toggle.On).weatherQuery)
        assertFalse(validCoordinates("NaN", "1"))
        assertFalse(validCoordinates("90.1", "1"))
        assertFalse(validCoordinates("1", "181"))
        assertTrue(validCoordinates("-90", "180"))
        assertNotNull(LauncherSettings(showWeather = Toggle.On, lat = "51.5", lon = "-0.1").weatherQuery)
    }

    @Test fun `remote bindings reject conflicts and protected keys`() {
        val defaults = RemoteBindings()
        assertEquals(RemoteAction.Customise, defaults.action(KeyEvent.KEYCODE_MENU))
        assertNotNull(defaults.problem(RemoteAction.AppOptions, KeyEvent.KEYCODE_MENU))
        for (code in listOf(KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_POWER, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_VOLUME_UP)) {
            assertNotNull(defaults.problem(RemoteAction.Customise, code))
            assertEquals(defaults, defaults.assign(RemoteAction.Customise, code))
        }
        val changed = defaults.assign(RemoteAction.AppOptions, KeyEvent.KEYCODE_PROG_RED)
        assertEquals(RemoteAction.AppOptions, changed.action(KeyEvent.KEYCODE_PROG_RED))
        assertNull(changed.action(KeyEvent.KEYCODE_BUTTON_Y))
        assertEquals(KeyEvent.KEYCODE_BUTTON_Y, RemoteBindings().appOptions)
    }
}
