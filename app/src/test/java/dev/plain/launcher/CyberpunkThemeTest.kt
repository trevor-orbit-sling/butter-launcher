package dev.plain.launcher

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import dev.plain.launcher.ui.look
import dev.plain.launcher.ui.tileShape
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class CyberpunkThemeTest {
    @Test fun `Cyberpunk applies neon yellow without changing other personal choices`() {
        val before = LauncherSettings(
            theme = Theme.Nord, accent = Accent.Rose, font = FontFace.PlexMono,
            background = BackgroundStyle.Theme, tileShape = TileShape.Pill,
            favourites = listOf("tv.example.player"), hiddenApps = listOf("tv.example.hidden"),
            remoteBindings = RemoteBindings(inputs = android.view.KeyEvent.KEYCODE_PROG_BLUE),
            tutorialComplete = true, showWeather = Toggle.On, place = "London", lat = "51.5", lon = "-0.1",
        )
        assertEquals(before.copy(theme = Theme.Cyberpunk, accent = Accent.NeonYellow), before.changeTheme(Theme.Cyberpunk))
        assertEquals(0xFFFCEE0A.toInt(), Accent.NeonYellow.color.toArgb())
    }

    @Test fun `Cyberpunk and custom accent survive saving with every background choice`() {
        for (background in BackgroundStyle.entries) {
            val chosen = LauncherSettings(background = background).changeTheme(Theme.Cyberpunk)
            assertEquals(chosen, decodeSettings(settingsJson.encodeToString(chosen)))
            val custom = chosen.copy(accent = Accent.Teal)
            assertEquals(custom, decodeSettings(settingsJson.encodeToString(custom)))
            assertSame(custom, custom.changeTheme(Theme.Cyberpunk))
        }
    }

    @Test fun `Cyberpunk focus and text stay clear and custom tile shapes remain available`() {
        val settings = LauncherSettings().changeTheme(Theme.Cyberpunk)
        val palette = look(settings.theme, settings.accent.color)
        assertTrue(ColorUtils.calculateContrast(palette.text.toArgb(), palette.surface.toArgb()) >= 7.0)
        assertTrue(ColorUtils.calculateContrast(palette.textDim.toArgb(), palette.surface.toArgb()) >= 4.5)
        assertTrue(ColorUtils.calculateContrast(palette.focusBorder.toArgb(), palette.surfaceFocused.toArgb()) >= 3.0)
        assertTrue(tileShape(settings, palette) is CutCornerShape)
        assertTrue(tileShape(settings.copy(tileShape = TileShape.Pill), palette) is RoundedCornerShape)
        assertTrue(tileShape(settings.copy(tileShape = TileShape.Square), palette) is RoundedCornerShape)
    }
}
