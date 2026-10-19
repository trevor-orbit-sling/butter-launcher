package dev.plain.launcher

import androidx.compose.ui.graphics.vector.PathParser
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class LineGlyphsTest {
    @Test fun `common TV packages have curated line artwork`() {
        val tvPackages = mapOf(
            "com.netflix.ninja" to "netflix",
            "com.google.android.youtube.tv" to "youtube",
            "com.disney.disneyplus" to "disney-plus",
            "com.amazon.amazonvideo.livingroom" to "prime-video",
            "com.hulu.livingroomplus" to "hulu",
            "com.wbd.stream" to "max",
            "com.apple.atve.androidtv" to "apple-tv",
            "com.peacocktv.peacockandroid" to "peacock",
            "com.cbs.ott" to "paramount-plus",
            "com.tubitv" to "tubi",
            "tv.pluto.android" to "pluto-tv",
            "com.crunchyroll.crunchyroid" to "crunchyroll",
            "com.plexapp.android" to "plex",
            "org.jellyfin.androidtv" to "jellyfin",
            "org.xbmc.kodi" to "kodi",
            "org.videolan.vlc" to "vlc",
            "com.spotify.tv.android" to "spotify",
            "tv.twitch.android.app" to "twitch",
            "com.stremio.one" to "stremio",
            "bbc.iplayer.android" to "bbc-iplayer",
            "air.ITVMobilePlayer" to "itvx",
            "com.dazn" to "dazn",
            "com.espn.score_center" to "espn",
            "com.valvesoftware.steamlink" to "steam-link",
        )
        tvPackages.forEach { (packageName, expected) ->
            assertEquals(packageName, expected, lineGlyphFor(packageName).id)
        }
    }

    @Test fun `TV mobile and legacy aliases share a drawing without matching unrelated apps`() {
        for ((tv, variant) in listOf(
            "com.netflix.ninja" to "com.netflix.mediaclient",
            "com.amazon.amazonvideo.livingroom" to "com.amazon.avod.thirdpartyclient",
            "com.hulu.livingroomplus" to "com.hulu.plus",
            "com.wbd.stream" to "com.hbo.hbomax",
            "com.apple.atve.androidtv" to "com.apple.atve.sony.appletv",
            "com.stremio.one" to "com.stremio.two",
        )) assertSame(lineGlyphFor(tv), lineGlyphFor(variant))
        assertEquals("app", lineGlyphFor("tv.example.player").id)
        assertEquals("app", lineGlyphFor("com.netflix.ninja.unrelated").id)
        assertEquals("app", lineGlyphFor("").id)
    }

    @Test fun `every shipped line path parses in the renderer and aliases resolve`() {
        lineGlyphs.values.forEach { glyph ->
            assertTrue(glyph.id, glyph.paths.isNotEmpty())
            glyph.paths.forEach { path ->
                assertTrue(glyph.id, PathParser().parsePathString(path).toNodes().isNotEmpty())
            }
        }
        linePackageAliases.values.forEach { assertTrue(it, lineGlyphs.containsKey(it)) }
    }

    @Test fun `existing mono preference survives and line preference round trips`() {
        assertEquals(IconStyle.Mono, decodeSettings("""{"iconStyle":"Mono"}""").iconStyle)
        val line = LauncherSettings(iconStyle = IconStyle.Line)
        assertEquals(line, decodeSettings(settingsJson.encodeToString(line)))
    }
}
