package dev.plain.launcher

import android.content.Intent
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.database.MatrixCursor
import android.media.tv.TvContract.WatchNextPrograms as Programs
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class WatchNextTest {
    @Test fun `missing TV listings permission offers an explicit access action`() = runBlocking {
        val state = WatchNext(RuntimeEnvironment.getApplication()).load()
        assertTrue(state.needsPermission)
        assertTrue(state.items.isEmpty())
        assertTrue(state.message.contains("Allow TV listings"))
    }

    @Test fun `granting TV listings exposes programs and revoking it removes them`() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val provider = object : ContentProvider() {
            override fun onCreate() = true
            override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?) = row()
            override fun getType(uri: Uri): String? = null
            override fun insert(uri: Uri, values: ContentValues?): Uri? = null
            override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        }
        provider.attachInfo(context, ProviderInfo().apply { authority = "android.media.tv" })
        ShadowContentResolver.registerProviderInternal("android.media.tv", provider)
        val link = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.test/watch/42")).setPackage("tv.example.player")
        shadowOf(context.packageManager).addResolveInfoForIntent(link, ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "tv.example.player"
                name = "tv.example.player.Playback"
                applicationInfo = ApplicationInfo().apply { packageName = "tv.example.player" }
            }
        })
        shadowOf(context).grantPermissions(READ_TV_LISTINGS)
        val watching = WatchNext(context).load()
        assertFalse(watching.needsPermission)
        assertEquals(listOf("tv.example.player:7"), watching.items.map { it.id })

        shadowOf(context).denyPermissions(READ_TV_LISTINGS)
        val revoked = WatchNext(context).load()
        assertTrue(revoked.needsPermission)
        assertTrue(revoked.items.isEmpty())
    }

    @Test fun `publisher owned content artwork is accepted while foreign providers are rejected`() {
        val art = "content://tv.example.player.images/poster/42"
        assertEquals(art, safeArtwork(art, "tv.example.player") { "tv.example.player" })
        assertNull(safeArtwork(art, "tv.example.player") { "other.player" })
        assertNull(safeArtwork(art, "tv.example.player") { null })
        assertNull(safeArtwork(art))
        assertNull(safeArtwork("content://user@tv.example.player.images/poster/42", "tv.example.player") { "tv.example.player" })
        assertNull(safeArtwork("file:///private/poster.png", "tv.example.player") { "tv.example.player" })
    }

    @Test fun `provider artwork validation uses the row publisher`() {
        row(art = "content://tv.example.player.images/poster/42").use {
            it.moveToFirst()
            assertEquals("content://tv.example.player.images/poster/42", readWatchItem(it) { "tv.example.player" }!!.image)
            assertNull(readWatchItem(it) { "foreign.player" }!!.image)
        }
    }
    @Test fun `plain deep link stays in its publishing app`() {
        val intent = safeWatchIntent("https://example.test/watch/42", "tv.example.player")!!
        assertEquals("tv.example.player", intent.`package`)
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
    }

    @Test fun `publisher component and scalar item ID survive but unsafe flags do not`() {
        val intent = safeWatchIntent("intent:#Intent;action=android.intent.action.VIEW;component=tv.example.player/.Playback;launchFlags=0x3;S.itemId=42;end", "tv.example.player")!!
        assertEquals("tv.example.player.Playback", intent.component!!.className)
        assertEquals("42", intent.getStringExtra("itemId"))
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
        assertNull(intent.selector)
        assertNull(intent.clipData)
    }

    @Test fun `foreign packages actions and local URI schemes are rejected`() {
        val invalid = listOf(
            "intent://watch/42#Intent;scheme=https;package=other.player;end",
            "intent:#Intent;component=other.player/.Playback;end",
            "intent://watch/42#Intent;scheme=https;action=android.intent.action.SEND;end",
            "file:///data/private", "content://other.provider/private", "javascript:alert(1)", "data:text/plain,secret", "", "not a URI",
        )
        invalid.forEach { assertNull(it, safeWatchIntent(it, "tv.example.player")) }
        assertNull(safeWatchIntent("https://example.test", ""))
    }

    @Test fun `provider row yields bounded progress and safe art`() {
        row(position = 200, duration = 100).use {
            it.moveToFirst()
            val item = readWatchItem(it)!!
            assertEquals("tv.example.player:7", item.id)
            assertEquals(1f, item.progress)
            assertEquals("https://example.test/poster.jpg", item.image)
            assertEquals("Episode 2", item.subtitle)
        }
        row(position = -1, duration = 0).use { it.moveToFirst(); assertEquals(0f, readWatchItem(it)!!.progress) }
    }

    @Test fun `hidden promotional and incomplete rows are ignored`() {
        row(browsable = 0).use { it.moveToFirst(); assertNull(readWatchItem(it)) }
        row(type = Programs.WATCH_NEXT_TYPE_NEW).use { it.moveToFirst(); assertNull(readWatchItem(it)) }
        row(title = " ").use { it.moveToFirst(); assertNull(readWatchItem(it)) }
        assertNull(safeArtwork("http://example.test/poster.jpg"))
        assertNull(safeArtwork("file:///private/photo"))
        assertNull(safeArtwork("https://user:secret@example.test/poster"))
    }

    private fun row(position: Long = 20, duration: Long = 100, browsable: Int = 1, type: Int = Programs.WATCH_NEXT_TYPE_CONTINUE, title: String = "Example show", art: String = "https://example.test/poster.jpg") =
        MatrixCursor(WatchNext.projection).apply {
            addRow(arrayOf(7, "tv.example.player", title, "Episode 2", art, "https://example.test/watch/42", position, duration, 123L, type, browsable))
        }
}
