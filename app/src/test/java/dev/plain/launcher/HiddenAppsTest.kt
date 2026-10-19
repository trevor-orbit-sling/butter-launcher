package dev.plain.launcher

import android.content.ComponentName
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class HiddenAppsTest {
    @Test fun `old settings show all apps and hidden packages persist`() {
        assertTrue(decodeSettings("""{"theme":"Nord"}""").hiddenApps.isEmpty())
        val settings = LauncherSettings().hideApp("tv.example.player")
        assertEquals(settings, decodeSettings(settingsJson.encodeToString(settings)))
    }

    @Test fun `hiding is idempotent and leaves favourites and other settings intact`() {
        val before = LauncherSettings(favourites = listOf("tv.example.player"), theme = Theme.Nord, accent = Accent.Rose)
        val hidden = before.hideApp("tv.example.player")
        assertEquals(before.copy(hiddenApps = listOf("tv.example.player")), hidden)
        assertSame(hidden, hidden.hideApp("tv.example.player"))
    }

    @Test fun `all apps filtering preserves order and hides only matching package IDs`() {
        val installed = listOf(app("tv.example.first"), app("tv.example.player"), app("tv.example.last"))
        assertEquals(listOf(installed[0], installed[2]), visibleApps(installed, listOf("tv.example.player", "tv.example.uninstalled")))
        assertEquals(installed, visibleApps(installed, emptyList()))
        assertTrue(visibleApps(installed, installed.map { it.packageName }).isEmpty())
        val settings = LauncherSettings(favourites = listOf("tv.example.player")).hideApp("tv.example.player")
        assertEquals(listOf(installed[1]), settings.favourites.mapNotNull { pkg -> installed.find { it.packageName == pkg } })
    }

    @Test fun `restoring clears stale IDs without changing favourites or another hidden package`() {
        val settings = LauncherSettings(favourites = listOf("tv.example.player"), hiddenApps = listOf("tv.example.player", "tv.example.uninstalled"))
        assertEquals(settings.copy(hiddenApps = listOf("tv.example.player")), settings.restoreApp("tv.example.uninstalled"))
        assertEquals(settings.copy(hiddenApps = listOf("tv.example.uninstalled")), settings.restoreApp("tv.example.player"))
        assertEquals(settings, settings.restoreApp("tv.example.absent"))
        assertTrue(settings.copy(hiddenApps = listOf("tv.example.player", "tv.example.player")).restoreApp("tv.example.player").hiddenApps.isEmpty())
    }

    private fun app(pkg: String) = App(pkg, "Player", ComponentName(pkg, "$pkg.Main"))
}
