package dev.plain.launcher

import android.app.Application
import coil.decode.ImageSource
import coil.decode.SvgDecoder
import coil.request.Options
import coil.size.Size
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.buffer
import okio.source
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BundledIconsTest {
    private val context: Application get() = RuntimeEnvironment.getApplication()

    @Test fun `both bundled styles persist alongside original preferences`() {
        for (style in listOf(IconStyle.Original, IconStyle.Mono, IconStyle.Arcticons, IconStyle.Lawnicons)) {
            val settings = LauncherSettings(iconStyle = style)
            assertEquals(settings, decodeSettings(settingsJson.encodeToString(settings)))
        }
    }

    @Test fun `common TV aliases select upstream artwork and unknown apps use standard generic symbols`() {
        for (style in listOf(IconStyle.Arcticons, IconStyle.Lawnicons)) {
            for (packageName in listOf(
                "com.netflix.ninja", "com.amazon.amazonvideo.livingroom", "com.hulu.livingroomplus",
                "com.google.android.youtube.tv", "org.jellyfin.androidtv", "com.stremio.two",
                "com.apple.atve.androidtv", "com.valvesoftware.steamlink",
            )) assertNotNull("$style: $packageName", bundledIconAsset(style, packageName))
            assertEquals(bundledIconAsset(style, "com.netflix.ninja"), bundledIconAsset(style, "com.netflix.mediaclient"))
            assertEquals(bundledIconAssets.getValue(style)["app"], bundledIconAsset(style, "com.netflix.ninja.unrelated"))
            assertEquals(bundledIconAssets.getValue(style)["app"], bundledIconAsset(style, "tv.example.unknown"))
        }
        assertNotNull(bundledIconAsset(IconStyle.Arcticons, "air.ITVMobilePlayer"))
        assertNotNull(bundledIconAsset(IconStyle.Lawnicons, "air.ITVMobilePlayer"))
        assertEquals("itvx", appIconAliases["air.ITVMobilePlayer"])
        assertEquals(bundledIconAsset(IconStyle.Lawnicons, "org.jellyfin.androidtv"), bundledIconAsset(IconStyle.Line, "org.jellyfin.androidtv"))
        assertNull(bundledIconAsset(IconStyle.Original, "com.netflix.ninja"))
    }

    @Test fun `all packaged source files match the provenance manifest and contain only local drawings`() {
        val manifest = context.assets.open("icons/bundled-sources.json").bufferedReader().use { settingsJson.parseToJsonElement(it.readText()) }.jsonObject
        val allAssets = bundledIconAssets.values.flatMap { it.values }.toSet()
        val recordedAssets = mutableSetOf<String>()
        for (pack in manifest.getValue("packs").jsonArray) {
            val data = pack.jsonObject
            val style = IconStyle.valueOf(data.getValue("label").jsonPrimitive.content)
            val importedIds = data.getValue("icons").jsonArray.map { it.jsonObject.getValue("id").jsonPrimitive.content }.toSet()
            val fallbacks = data.getValue("fallbacks").jsonArray.map { it.jsonPrimitive.content }.toSet()
            assertEquals(appIconAliases.values.toSet() + "app", importedIds + fallbacks)
            assertTrue(importedIds.intersect(fallbacks).isEmpty())
            for (icon in data.getValue("icons").jsonArray) {
                val entry = icon.jsonObject
                val asset = entry.getValue("asset").jsonPrimitive.content
                val bytes = context.assets.open(asset).use { it.readBytes() }
                recordedAssets += asset
                assertEquals(asset, bundledIconAssets.getValue(style)[entry.getValue("id").jsonPrimitive.content])
                val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                assertEquals(asset, entry.getValue("sha256").jsonPrimitive.content, hash)
                val source = bytes.toString(Charsets.UTF_8)
                assertFalse(asset, Regex("<!DOCTYPE|<!ENTITY|<\\?xml-stylesheet|@import|javascript:", RegexOption.IGNORE_CASE).containsMatchIn(source))
                val factory = DocumentBuilderFactory.newInstance().apply {
                    isNamespaceAware = true
                    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                    setFeature("http://xml.org/sax/features/external-general-entities", false)
                    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                }
                val elements = factory.newDocumentBuilder().parse(bytes.inputStream()).getElementsByTagName("*")
                val allowedTags = setOf("svg", "g", "defs", "style", "path", "line", "polyline", "polygon", "circle", "ellipse", "rect", "clipPath", "mask", "linearGradient", "radialGradient", "stop", "title", "desc")
                for (i in 0 until elements.length) {
                    val element = elements.item(i)
                    assertTrue("$asset: ${element.localName}", element.localName in allowedTags)
                    val attributes = element.attributes
                    for (j in 0 until attributes.length) {
                        val attr = attributes.item(j)
                        val name = (attr.localName ?: attr.nodeName).lowercase()
                        assertFalse(asset, name.startsWith("on"))
                        if (name == "href" || name == "src") assertTrue(asset, attr.nodeValue.startsWith("#"))
                    }
                }
                Regex("url\\(\\s*['\"]?([^)'\"\\s]+)", RegexOption.IGNORE_CASE).findAll(source).forEach {
                    assertTrue(asset, it.groupValues[1].startsWith("#"))
                }
            }
        }
        assertEquals(allAssets, recordedAssets)
    }

    @Test fun `every bundled SVG decodes with the same Coil decoder used by tiles`() = runBlocking {
        for (asset in bundledIconAssets.values.flatMap { it.values }.toSet()) {
            // Exercise the real SVG parser/renderer without ImageLoader's Android main-loop scheduling.
            val drawable = ImageSource(context.assets.open(asset).source().buffer(), context).use { source ->
                SvgDecoder(source, Options(context, size = Size(96, 96))).decode().drawable
            }
            assertTrue(asset, drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
        }
    }
}
