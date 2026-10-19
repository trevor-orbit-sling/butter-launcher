package dev.plain.launcher

import dev.plain.launcher.ui.shouldMoveBackground
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class BackgroundSettingsTest {
    @Test fun `existing settings preserve the current theme background`() {
        assertEquals(BackgroundStyle.Theme, LauncherSettings().background)
        val legacy = decodeSettings("""{"theme":"Paper","favourites":["tv.example.player"]}""")
        assertEquals(Theme.Paper, legacy.theme)
        assertEquals(BackgroundStyle.Theme, legacy.background)
        assertEquals(listOf("tv.example.player"), legacy.favourites)
        assertEquals(BackgroundStyle.Theme, decodeSettings("""{"background":"FutureStyle"}""").background)
    }

    @Test fun `each background preference survives saving without changing the theme`() {
        for (style in BackgroundStyle.entries) {
            val settings = LauncherSettings(theme = Theme.Ink, background = style, accent = Accent.Amber)
            assertEquals(settings, decodeSettings(settingsJson.encodeToString(settings)))
        }
    }

    @Test fun `only moving backgrounds in the foreground with enabled animation can move`() {
        for (style in BackgroundStyle.entries) {
            assertFalse(shouldMoveBackground(style, resumed = false, durationScale = 1f))
            assertFalse(shouldMoveBackground(style, resumed = true, durationScale = 0f))
        }
        assertFalse(shouldMoveBackground(BackgroundStyle.Theme, true, 1f))
        assertFalse(shouldMoveBackground(BackgroundStyle.Blobs, true, 1f))
        assertFalse(shouldMoveBackground(BackgroundStyle.Moving, true, Float.NaN))
        assertFalse(shouldMoveBackground(BackgroundStyle.Moving, true, -1f))
        assertTrue(shouldMoveBackground(BackgroundStyle.Moving, true, 1f))
        assertTrue(shouldMoveBackground(BackgroundStyle.Moving, true, 2f))
    }
}
