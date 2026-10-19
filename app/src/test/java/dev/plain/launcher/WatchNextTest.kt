package dev.plain.launcher

import android.content.Intent
import android.database.MatrixCursor
import android.media.tv.TvContract.WatchNextPrograms as Programs
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class WatchNextTest {
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

    private fun row(position: Long = 20, duration: Long = 100, browsable: Int = 1, type: Int = Programs.WATCH_NEXT_TYPE_CONTINUE, title: String = "Example show") =
        MatrixCursor(WatchNext.projection).apply {
            addRow(arrayOf(7, "tv.example.player", title, "Episode 2", "https://example.test/poster.jpg", "https://example.test/watch/42", position, duration, 123L, type, browsable))
        }
}
