package dev.plain.launcher

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.ColorUtils
import dev.plain.launcher.ui.look
import dev.plain.launcher.ui.themeMarks
import dev.plain.launcher.ui.tileShape
import dev.plain.launcher.ui.type
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class OriginalThemesTest {
    private val defaults = listOf(
        Triple(Theme.Phosphor, Accent.Amber, FontFace.PlexMono),
        Triple(Theme.Orbital, Accent.Copper, FontFace.SpaceGrotesk),
        Triple(Theme.AfterHours, Accent.HotPink, FontFace.Outfit),
        Triple(Theme.DeepSea, Accent.Teal, FontFace.Manrope),
        Triple(Theme.Butter, Accent.Amber, FontFace.Outfit),
        Triple(Theme.Canopy, Accent.Lime, FontFace.Manrope),
    )

    @Test fun `original themes apply suggested font and accent and preserve the rest of home`() {
        val before = LauncherSettings(
            font = FontFace.DmSerif, accent = Accent.Rose, background = BackgroundStyle.Moving,
            tileShape = TileShape.Pill, favourites = listOf("tv.example.player"), hiddenApps = listOf("tv.example.hidden"),
            iconStyle = IconStyle.Lawnicons, showWeather = Toggle.On, place = "London", lat = "51.5", lon = "-0.1",
            remoteBindings = RemoteBindings(inputs = android.view.KeyEvent.KEYCODE_PROG_BLUE), tutorialComplete = true,
        )
        for ((theme, accent, font) in defaults) {
            assertEquals(before.copy(theme = theme, accent = accent, font = font), before.changeTheme(theme))
        }
        assertEquals(17, Theme.entries.size)
        for (theme in Theme.entries - defaults.map { it.first }.toSet()) {
            assertEquals(before.font, before.changeTheme(theme).font)
        }
    }

    @Test fun `saved and manually edited theme choices do not reset font accent or motion preference`() {
        for ((theme, _, _) in defaults) for (background in BackgroundStyle.entries) {
            val custom = LauncherSettings().changeTheme(theme).copy(font = FontFace.Inter, accent = Accent.Rose, background = background)
            assertSame(custom, custom.changeTheme(theme))
            assertEquals(custom, decodeSettings(settingsJson.encodeToString(custom)))
        }
    }

    @Test fun `all six defaults have readable text and focus on their opaque surfaces and backgrounds`() {
        for ((theme, accent, _) in defaults) {
            val palette = look(theme, accent.color)
            for (background in listOf(palette.surface, palette.surfaceFocused, palette.panel, palette.top, palette.bottom)) {
                assertEquals("$theme opaque", 1f, background.alpha, 0f)
                assertTrue("$theme primary text", ColorUtils.calculateContrast(palette.text.toArgb(), background.toArgb()) >= 7.0)
                assertTrue("$theme secondary text", ColorUtils.calculateContrast(palette.textDim.toArgb(), background.toArgb()) >= 4.5)
            }
            assertTrue("$theme focus", ColorUtils.calculateContrast(palette.focusBorder.toArgb(), palette.surfaceFocused.toArgb()) >= 3.0)
            assertTrue("$theme settings values", ColorUtils.calculateContrast(palette.accent.toArgb(), palette.panel.toArgb()) >= 4.5)
        }
    }

    @Test fun `motifs and default tile outlines scale cleanly without overriding custom shapes`() {
        val reference = look(Theme.Ink, Accent.Blue.color)
        val square = tileShape(LauncherSettings(tileShape = TileShape.Square), reference)
        val pill = tileShape(LauncherSettings(tileShape = TileShape.Pill), reference)
        for ((theme, accent, _) in defaults) {
            val settings = LauncherSettings().changeTheme(theme)
            val palette = look(theme, accent.color)
            assertEquals(square, tileShape(settings.copy(tileShape = TileShape.Square), palette))
            assertEquals(pill, tileShape(settings.copy(tileShape = TileShape.Pill), palette))
            assertTrue(tileShape(settings, palette).createOutline(Size(190f, 106f), LayoutDirection.Ltr, Density(1f)) is Outline.Rounded)
            for (size in listOf(Size(960f, 540f), Size(1920f, 1080f))) {
                val marks = themeMarks(theme, palette, size, size.width / 960f)
                assertTrue("$theme visible motif", marks.isNotEmpty())
                for (mark in marks) {
                    assertFalse(mark.path.isEmpty)
                    val bounds = mark.path.getBounds()
                    assertTrue(listOf(bounds.left, bounds.top, bounds.right, bounds.bottom).all { it.isFinite() })
                }
            }
        }
        val terminal = type(FontFamily.Monospace, 1f, Theme.Phosphor)
        assertTrue(terminal.label.letterSpacing.value > type(FontFamily.Default, 1f, Theme.Butter).label.letterSpacing.value)
    }
}
