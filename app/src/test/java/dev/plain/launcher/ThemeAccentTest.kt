package dev.plain.launcher

import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class ThemeAccentTest {
    @Test fun `choosing another theme updates its accent without resetting personal choices`() {
        for (theme in Theme.entries) {
            val before = LauncherSettings(
                theme = if (theme == Theme.Ink) Theme.Paper else Theme.Ink,
                accent = Accent.Rose, background = BackgroundStyle.Moving,
                favourites = listOf("tv.example.player"), font = FontFace.Outfit,
                showWeather = Toggle.On, place = "London", lat = "51.5", lon = "-0.1",
                tutorialComplete = true,
            )
            assertEquals(before.copy(theme = theme, accent = theme.defaultAccent), before.changeTheme(theme))
        }
        assertEquals(Accent.Lavender, LauncherSettings().changeTheme(Theme.CatppuccinMocha).accent)
        assertEquals(Accent.Lavender, LauncherSettings().changeTheme(Theme.CatppuccinLatte).accent)
        assertEquals(Accent.Lavender, LauncherSettings().changeTheme(Theme.Dracula).accent)
        assertEquals(Accent.Teal, LauncherSettings().changeTheme(Theme.Nord).accent)
        assertEquals(Accent.Amber, LauncherSettings().changeTheme(Theme.Sand).accent)
    }

    @Test fun `custom accent remains editable and persists until another theme is chosen`() {
        val custom = LauncherSettings().changeTheme(Theme.Nord).copy(accent = Accent.Rose)
        assertSame(custom, custom.changeTheme(Theme.Nord))
        assertEquals(custom, decodeSettings(settingsJson.encodeToString(custom)))
        assertEquals(Accent.Lavender, custom.changeTheme(Theme.Dracula).accent)
    }

    @Test fun `loading saved preferences does not replace a user selected accent`() {
        val saved = decodeSettings("""{"theme":"Nord","accent":"Amber","favourites":["tv.example.player"]}""")
        assertEquals(Theme.Nord, saved.theme)
        assertEquals(Accent.Amber, saved.accent)
        assertEquals(listOf("tv.example.player"), saved.favourites)
    }
}
