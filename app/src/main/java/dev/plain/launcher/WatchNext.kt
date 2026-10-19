package dev.plain.launcher

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.Cursor
import android.media.tv.TvContract.WatchNextPrograms as Programs
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

/** Read-only view of programs shared with this launcher by the device's TV provider. */
data class WatchItem(
    val id: String,
    val packageName: String,
    val title: String,
    val subtitle: String,
    val image: String?,
    val progress: Float,
    val intentUri: String,
    val lastEngagement: Long,
)

data class WatchState(val items: List<WatchItem> = emptyList(), val message: String = "Looking for shared programs…")

class WatchNext(private val context: Context) {
    private val resolver = context.contentResolver

    fun changes() = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { trySend(Unit) }
        }
        // Some TV images omit the provider or deny registration. A query still supplies a useful state.
        val registered = runCatching { resolver.registerContentObserver(Programs.CONTENT_URI, true, observer) }.isSuccess
        trySend(Unit)
        awaitClose { if (registered) resolver.unregisterContentObserver(observer) }
    }.conflate()

    suspend fun load(): WatchState = withContext(Dispatchers.IO) {
        try {
            val cursor = resolver.query(Programs.CONTENT_URI, projection, null, null, null)
                ?: return@withContext WatchState(message = "This device doesn't share Continue watching with Plain. You can hide this row in Settings.")
            val items = cursor.use { rows ->
                buildList {
                    while (rows.moveToNext()) {
                        val item = readWatchItem(rows) ?: continue
                        val intent = safeWatchIntent(item.intentUri, item.packageName) ?: continue
                        if (intent.resolveActivity(context.packageManager) != null) add(item)
                    }
                }
            }.sortedByDescending { it.lastEngagement }.take(20)
            WatchState(items, "No shared programs yet. Play something in a participating TV app. Some devices only share this row with their built-in home screen.")
        } catch (_: SecurityException) {
            WatchState(message = "This device keeps Continue watching private to its built-in home screen. Open your apps directly, or hide this row in Settings.")
        } catch (_: Exception) {
            WatchState(message = "Continue watching is unavailable on this device. Open your apps directly, or hide this row in Settings.")
        }
    }

    fun open(item: WatchItem): Boolean {
        val intent = safeWatchIntent(item.intentUri, item.packageName) ?: return false
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    companion object {
        internal val projection = arrayOf(
            Programs._ID, Programs.COLUMN_PACKAGE_NAME, Programs.COLUMN_TITLE, Programs.COLUMN_EPISODE_TITLE,
            Programs.COLUMN_POSTER_ART_URI, Programs.COLUMN_INTENT_URI, Programs.COLUMN_LAST_PLAYBACK_POSITION_MILLIS,
            Programs.COLUMN_DURATION_MILLIS, Programs.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS,
            Programs.COLUMN_WATCH_NEXT_TYPE, Programs.COLUMN_BROWSABLE,
        )
    }
}

internal fun readWatchItem(cursor: Cursor): WatchItem? {
    fun text(column: String) = cursor.getColumnIndex(column).takeIf { it >= 0 }?.let { cursor.getString(it) }.orEmpty()
    fun number(column: String) = cursor.getColumnIndex(column).takeIf { it >= 0 }?.let { cursor.getLong(it) } ?: 0L
    if (number(Programs.COLUMN_BROWSABLE) != 1L) return null
    if (number(Programs.COLUMN_WATCH_NEXT_TYPE).toInt() !in setOf(Programs.WATCH_NEXT_TYPE_CONTINUE, Programs.WATCH_NEXT_TYPE_NEXT)) return null
    val pkg = text(Programs.COLUMN_PACKAGE_NAME)
    val title = text(Programs.COLUMN_TITLE).trim()
    val uri = text(Programs.COLUMN_INTENT_URI)
    if (title.isEmpty() || safeWatchIntent(uri, pkg) == null) return null
    val duration = number(Programs.COLUMN_DURATION_MILLIS)
    val position = number(Programs.COLUMN_LAST_PLAYBACK_POSITION_MILLIS)
    return WatchItem(
        id = "$pkg:${number(Programs._ID)}", packageName = pkg, title = title,
        subtitle = text(Programs.COLUMN_EPISODE_TITLE), image = safeArtwork(text(Programs.COLUMN_POSTER_ART_URI)),
        progress = if (duration > 0) (position.toDouble() / duration).toFloat().coerceIn(0f, 1f) else 0f,
        intentUri = uri, lastEngagement = number(Programs.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS),
    )
}

/** Rebuild a publisher-bound VIEW intent, dropping grants, selectors and arbitrary actions. */
internal fun safeWatchIntent(value: String, publisher: String): Intent? = runCatching {
    if (!Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+").matches(publisher)) return null
    val parsed = Intent.parseUri(value, Intent.URI_INTENT_SCHEME)
    if (parsed.action != null && parsed.action != Intent.ACTION_VIEW) return null
    if (parsed.`package` != null && parsed.`package` != publisher) return null
    if (parsed.component != null && parsed.component!!.packageName != publisher) return null
    val data = parsed.data
    if (data == null && parsed.component == null) return null
    if (data != null) {
        val scheme = data.scheme?.lowercase() ?: return null
        if (scheme in setOf("file", "content", "javascript", "data", "intent", "android-app")) return null
    }
    Intent(Intent.ACTION_VIEW, data).setPackage(publisher).apply {
        component = parsed.component
        // parseUri supports scalar extras; preserve the publisher's item IDs, never its routing flags.
        parsed.extras?.let { putExtras(it) }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
}.getOrNull()

internal fun safeArtwork(value: String): String? = runCatching {
    val uri = Uri.parse(value)
    // No local files or arbitrary content providers. HTTPS artwork can be loaded without URI grants.
    value.takeIf { uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null }
}.getOrNull()
