package dev.plain.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

data class App(val packageName: String, val label: String, val component: ComponentName)

class AppArt(val banner: ImageBitmap?, val icon: ImageBitmap?)

class Apps(private val context: Context) {
    private val pm = context.packageManager
    private val cache = ConcurrentHashMap<String, AppArt>()
    private val packs = IconPacks(context)

    fun load(): List<App> {
        val homes = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
            .map { it.activityInfo }
        val ownLabels = buildSet {
            add(context.applicationInfo.loadLabel(pm).toString())
            homes.filter { it.packageName == context.packageName }.forEach { add(it.loadLabel(pm).toString()) }
            add("Plain") // Previous public display name, before Butter Launcher.
        }
        // Other installations of this launcher can have a different application ID.
        // Match their display name only when the package also offers a HOME activity.
        val hiddenPackages = homes.filter {
            it.loadLabel(pm).toString() in ownLabels || it.applicationInfo.loadLabel(pm).toString() in ownLabels
        }.mapTo(hashSetOf(context.packageName)) { it.packageName }

        return listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
            .flatMap { pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(it), 0) }
            .map { it.activityInfo }
            .filter { it.packageName !in hiddenPackages }
            .distinctBy { it.packageName }
            .map { App(it.packageName, it.loadLabel(pm).toString(), ComponentName(it.packageName, it.name)) }
            .sortedBy { it.label.lowercase() }
    }

    fun open(app: App) {
        val intent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
            ?: pm.getLaunchIntentForPackage(app.packageName) ?: return
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun openInfo(app: App) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", app.packageName, null))
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun iconPacks() = packs.installed()

    // Slow (decodes drawables), so call it off the main thread.
    fun art(app: App, pack: String): AppArt = cache.getOrPut("${app.packageName}|$pack") {
        val packIcon = if (pack.isNotEmpty()) packs.icon(pack, app.component) else null
        if (packIcon != null) return@getOrPut AppArt(null, packIcon.toImage(192, 192))
        val banner = runCatching { pm.getActivityBanner(app.component) ?: pm.getApplicationBanner(app.packageName) }.getOrNull()
        val icon = runCatching { pm.getActivityIcon(app.component) }.getOrNull()
        AppArt(banner?.toImage(320, 180), icon?.toImage(192, 192))
    }

    private fun Drawable.toImage(maxWidth: Int, maxHeight: Int): ImageBitmap {
        val w = intrinsicWidth.takeIf { it > 0 } ?: maxWidth
        val h = intrinsicHeight.takeIf { it > 0 } ?: maxHeight
        val scale = minOf(1f, maxWidth / w.toFloat(), maxHeight / h.toFloat())
        return toBitmap((w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1)).asImageBitmap()
    }
}

// Icon packs in the usual ADW/Nova format: appfilter.xml maps "ComponentInfo{pkg/activity}" to a drawable name.
class IconPacks(private val context: Context) {
    private val pm = context.packageManager
    private val filters = ConcurrentHashMap<String, Map<String, String>>()

    fun installed(): List<Pair<String, String>> =
        listOf("org.adw.launcher.THEMES", "com.novalauncher.THEME", "com.gau.go.launcherex.theme")
            .flatMap { pm.queryIntentActivities(Intent(it), 0) }
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

    fun icon(pack: String, component: ComponentName): Drawable? {
        val filter = filters.getOrPut(pack) { runCatching { readFilter(pack) }.getOrDefault(emptyMap()) }
        val name = filter[component.flattenToString()] ?: filter[component.packageName] ?: return null
        return runCatching {
            val res = pm.getResourcesForApplication(pack)
            res.getIdentifier(name, "drawable", pack).takeIf { it != 0 }?.let { res.getDrawable(it, null) }
        }.getOrNull()
    }

    private fun readFilter(pack: String): Map<String, String> {
        val res = pm.getResourcesForApplication(pack)
        val xml = res.getIdentifier("appfilter", "xml", pack)
        val parser = if (xml != 0) res.getXml(xml) else XmlPullParserFactory.newInstance().newPullParser().apply {
            setInput(context.createPackageContext(pack, 0).assets.open("appfilter.xml"), "UTF-8")
        }
        val component = Regex("""ComponentInfo\{([^/]+)/([^}]+)\}""")
        val map = HashMap<String, String>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG || parser.name != "item") continue
            val match = component.find(parser.getAttributeValue(null, "component") ?: continue) ?: continue
            val drawable = parser.getAttributeValue(null, "drawable") ?: continue
            val (pkg, cls) = match.destructured
            map["$pkg/${if (cls.startsWith(".")) pkg + cls else cls}"] = drawable
            map.putIfAbsent(pkg, drawable)
        }
        return map
    }
}
