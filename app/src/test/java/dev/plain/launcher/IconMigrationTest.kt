package dev.plain.launcher

import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class IconMigrationTest {
    @Test fun `retired Line preferences select Lawnicons without resetting anything else`() {
        val legacy = LauncherSettings(iconStyle = IconStyle.Line, iconPack = "tv.example.icons",
            theme = Theme.Dracula, favourites = listOf("tv.example.player"), hiddenApps = listOf("tv.example.hidden"))
        assertEquals(legacy.copy(iconStyle = IconStyle.Lawnicons), decodeSettings(settingsJson.encodeToString(legacy)))
    }

    @Test fun `TV packages keep exact upstream aliases without matching unrelated prefixes`() {
        val required = listOf("com.netflix.ninja", "com.google.android.youtube.tv", "com.disney.disneyplus",
            "com.amazon.amazonvideo.livingroom", "com.hulu.livingroomplus", "com.wbd.stream", "com.apple.atve.androidtv",
            "com.peacocktv.peacockandroid", "com.cbs.ott", "com.tubitv", "tv.pluto.android", "com.crunchyroll.crunchyroid",
            "com.plexapp.android", "org.jellyfin.androidtv", "org.xbmc.kodi", "org.videolan.vlc", "com.spotify.tv.android",
            "tv.twitch.android.app", "com.stremio.one", "bbc.iplayer.android", "air.ITVMobilePlayer", "com.dazn",
            "com.espn.score_center", "com.valvesoftware.steamlink")
        required.forEach { assertNotNull(it, appIconAliases[it]) }
        assertNull(appIconAliases["com.netflix.ninja.unrelated"])
    }
}
