package dev.plain.launcher

import dev.plain.launcher.ui.IconChoice
import dev.plain.launcher.ui.iconChoices
import org.junit.Assert.*
import org.junit.Test

class IconChoicesTest {
    @Test fun `included icon styles stay visible with no installed packs`() {
        assertEquals(
            listOf(IconStyle.Lawnicons, IconStyle.Original, IconStyle.Arcticons, IconStyle.Mono, IconStyle.Desaturated),
            iconChoices(emptyList()).map { it.style },
        )
    }

    @Test fun `installed packs join the same list without an empty None choice`() {
        val choices = iconChoices(listOf("" to "None", "tv.example.icons" to "Example", "tv.example.icons" to "Duplicate"))
        assertEquals(5, choices.count { it.style != IconStyle.Pack })
        assertEquals(listOf(IconChoice(IconStyle.Pack, "Example", "tv.example.icons")), choices.filter { it.style == IconStyle.Pack })
    }

    @Test fun `choosing included art preserves the installed pack and unrelated preferences`() {
        val settings = LauncherSettings(iconStyle = IconStyle.Pack, iconPack = "tv.example.icons", favourites = listOf("tv.example.player"))
        val line = IconChoice(IconStyle.Lawnicons, "Lawnicons")
        assertEquals(settings.copy(iconStyle = IconStyle.Lawnicons), line.applyTo(settings))
        assertTrue(line.matches(line.applyTo(settings)))
        val pack = IconChoice(IconStyle.Pack, "Other", "tv.example.other")
        assertEquals(settings.copy(iconPack = "tv.example.other"), pack.applyTo(settings))
        assertFalse(pack.matches(settings))
    }
}
