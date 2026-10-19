package dev.plain.launcher

import android.view.KeyEvent
import org.junit.Assert.*
import org.junit.Test

class RemoteBindingsRecoveryTest {
    @Test fun `protected and invalid keys in otherwise valid saved JSON never dispatch shortcuts`() {
        val protected = listOf(
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_POWER, KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_TV_INPUT, KeyEvent.KEYCODE_UNKNOWN, -1, Int.MAX_VALUE,
        )
        for (key in protected) {
            val settings = decodeSettings("""{"remoteBindings":{"customise":$key},"favourites":["tv.example.player"]}""")
            assertNull("Saved key $key must remain available to the system/navigation", settings.remoteBindings.action(key))
            assertEquals(listOf("tv.example.player"), settings.favourites)
            assertEquals(RemoteAction.AppOptions, settings.remoteBindings.action(KeyEvent.KEYCODE_BUTTON_Y))
        }
    }

    @Test fun `valid saved shortcuts still dispatch and fresh conflicts are rejected accurately`() {
        val settings = decodeSettings("""{"remoteBindings":{"customise":${KeyEvent.KEYCODE_INFO}}}""")
        assertEquals(RemoteAction.Customise, settings.remoteBindings.action(KeyEvent.KEYCODE_INFO))
        val stale = RemoteBindings()
        assertNull(stale.problem(RemoteAction.Inputs, KeyEvent.KEYCODE_INFO))
        val fresh = stale.assign(RemoteAction.Customise, KeyEvent.KEYCODE_INFO)
        val rejected = fresh.assignment(RemoteAction.Inputs, KeyEvent.KEYCODE_INFO)
        assertEquals(fresh, rejected.bindings)
        assertNotNull(rejected.problem)
        val accepted = fresh.assignment(RemoteAction.Inputs, KeyEvent.KEYCODE_PROG_BLUE)
        assertNull(accepted.problem)
        assertEquals(RemoteAction.Inputs, accepted.bindings.action(KeyEvent.KEYCODE_PROG_BLUE))
    }
}
