package dev.plain.launcher

import android.view.KeyEvent
import org.junit.Assert.*
import org.junit.Test

class ReleaseGestureTest {
    private val selectKeys = listOf(
        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A,
    )

    @Test fun `held select opens only on release and does not activate a new menu`() {
        for (code in selectKeys) {
            val tile = ReleaseGesture(500)
            val menu = ReleaseGesture(500)
            tile.down(code, 100)
            // A real remote can send many repeats before the user lets go.
            for (repeat in 1..12) tile.down(code, 100L + repeat * 100, repeat, longPress = repeat == 1)
            assertEquals(ReleaseGesture.Decision.LongClick, tile.up(code, 1400))
            // The opening UP is consumed by the tile. Even duplicate/orphan events cannot choose Move/Add.
            assertEquals(ReleaseGesture.Decision.None, tile.up(code, 1400))
            menu.down(code, 1401, repeatCount = 13)
            assertEquals(ReleaseGesture.Decision.None, menu.up(code, 1402, allowLongClick = false))
            menu.down(code, 2000)
            assertEquals(ReleaseGesture.Decision.Click, menu.up(code, 2050, allowLongClick = false))
            assertEquals(ReleaseGesture.Decision.None, menu.up(code, 2051, allowLongClick = false))
        }
    }

    @Test fun `a long duration without repeat still opens options and a short press launches once`() {
        for (code in selectKeys) {
            val tile = ReleaseGesture(500)
            tile.down(code, 100)
            assertEquals(ReleaseGesture.Decision.LongClick, tile.up(code, 700))
            tile.down(code, 1000)
            assertEquals(ReleaseGesture.Decision.Click, tile.up(code, 1050))
            assertEquals(ReleaseGesture.Decision.None, tile.up(code, 1050))
        }
    }

    @Test fun `cancelled releases and focus loss trigger no action`() {
        val tile = ReleaseGesture(500)
        tile.down(KeyEvent.KEYCODE_DPAD_CENTER, 100)
        assertEquals(ReleaseGesture.Decision.None, tile.up(KeyEvent.KEYCODE_DPAD_CENTER, 1000, cancelled = true))
        tile.down(KeyEvent.KEYCODE_ENTER, 2000)
        tile.cancel() // The surface lost focus or its window closed.
        tile.down(KeyEvent.KEYCODE_ENTER, 3000, repeatCount = 5, longPress = true)
        assertEquals(ReleaseGesture.Decision.None, tile.up(KeyEvent.KEYCODE_ENTER, 3100))
    }

    @Test fun `shortcut holds do not fire before release or leak repeat events into the menu`() {
        val shortcut = ReleaseGesture(500)
        val menu = ReleaseGesture(500)
        val code = KeyEvent.KEYCODE_BUTTON_Y
        shortcut.down(code, 100)
        for (repeat in 1..8) shortcut.down(code, 100L + repeat * 150, repeat)
        assertEquals(ReleaseGesture.Decision.Click, shortcut.up(code, 1400, allowLongClick = false))
        assertEquals(ReleaseGesture.Decision.None, shortcut.up(code, 1400, allowLongClick = false))
        // A shortcut must not fabricate an OK gesture in the newly opened menu.
        assertEquals(ReleaseGesture.Decision.None, menu.up(KeyEvent.KEYCODE_DPAD_CENTER, 1401, allowLongClick = false))
    }

    @Test fun `orphan long-press flags and a different released key cannot choose an action`() {
        val tile = ReleaseGesture(500)
        tile.down(KeyEvent.KEYCODE_DPAD_CENTER, 100, longPress = true)
        assertEquals(ReleaseGesture.Decision.None, tile.up(KeyEvent.KEYCODE_DPAD_CENTER, 200))
        tile.down(KeyEvent.KEYCODE_DPAD_CENTER, 300)
        assertEquals(ReleaseGesture.Decision.None, tile.up(KeyEvent.KEYCODE_ENTER, 400))
        assertEquals(ReleaseGesture.Decision.Click, tile.up(KeyEvent.KEYCODE_DPAD_CENTER, 450))
    }
}
