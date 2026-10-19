package dev.plain.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import dev.plain.launcher.App
import dev.plain.launcher.AppArt
import dev.plain.launcher.Apps
import dev.plain.launcher.IconStyle
import dev.plain.launcher.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class IconChoice(val style: IconStyle, val label: String, val pack: String = "") {
    val key get() = "${style.name}:$pack"
    fun matches(settings: LauncherSettings) = settings.iconStyle == style &&
        (style != IconStyle.Pack || settings.iconPack == pack)
    fun applyTo(settings: LauncherSettings) = settings.copy(
        iconStyle = style,
        iconPack = if (style == IconStyle.Pack) pack else settings.iconPack,
    )
}

internal fun iconChoices(installedPacks: List<Pair<String, String>>): List<IconChoice> =
    listOf(IconStyle.Lawnicons, IconStyle.Original, IconStyle.Arcticons, IconStyle.Mono, IconStyle.Desaturated)
        .map { IconChoice(it, it.label) } +
        installedPacks.filter { it.first.isNotBlank() }.distinctBy { it.first }
            .map { (packageName, label) -> IconChoice(IconStyle.Pack, label, packageName) }

@Composable
internal fun IconChooser(apps: Apps, onChange: (Change) -> Unit, onDismiss: () -> Unit) {
    val settings = LocalSettings.current
    val look = LocalLook.current
    val type = LocalType.current
    var installedPacks by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    var installedApps by remember { mutableStateOf<List<App>?>(null) }
    LaunchedEffect(apps) {
        installedPacks = withContext(Dispatchers.IO) { runCatching { apps.iconPacks() }.getOrDefault(emptyList()) }
        installedApps = withContext(Dispatchers.IO) { runCatching { apps.load() }.getOrDefault(emptyList()) }
    }
    val choices = remember(installedPacks) { iconChoices(installedPacks.orEmpty()) }
    val selectedIndex = choices.indexOfFirst { it.matches(settings) }.coerceAtLeast(0)
    var preview by remember { mutableStateOf(choices[selectedIndex]) }
    val selectedFocus = remember { FocusRequester() }
    val list = rememberLazyListState()
    val preferred = listOf(
        "com.google.android.youtube.tv", "org.jellyfin.androidtv", "com.spotify.tv.android",
        "com.netflix.ninja", "com.stremio.one", "com.valvesoftware.steamlink",
    )
    val samples = remember(installedApps, settings.hiddenApps) {
        installedApps.orEmpty().filterNot { it.packageName in settings.hiddenApps }
            .sortedBy { preferred.indexOf(it.packageName).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
            .take(3)
    }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        LaunchedEffect(installedPacks != null) {
            if (installedPacks == null) return@LaunchedEffect
            preview = choices[selectedIndex]
            list.scrollToItem(selectedIndex)
            withFrameNanos { }
            runCatching { selectedFocus.requestFocus() }
        }
        Column(
            Modifier.widthIn(max = 800.dp).fillMaxWidth(0.94f).heightIn(max = maxHeight)
                .background(look.panel, RoundedCornerShape(look.corner)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Choose icons", style = type.title, color = look.text)
            Text("Move to preview · OK applies · Back cancels", style = type.caption, color = look.textDim)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LazyColumn(
                    state = list,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(choices, key = { _, choice -> choice.key }) { index, choice ->
                        val applied = choice.matches(settings)
                        FocusSurface(
                            onClick = { onChange { choice.applyTo(it) }; onDismiss() },
                            selected = applied,
                            zoom = 1.01f,
                            modifier = (if (index == selectedIndex) Modifier.focusRequester(selectedFocus) else Modifier)
                                .fillMaxWidth().onFocusChanged { if (it.isFocused) preview = choice }
                                .semantics { selected = applied },
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(choice.label, style = type.label, color = look.text)
                                    Text(choice.description(), style = type.caption, color = look.textDim)
                                }
                                if (applied) Text("✓", style = type.label, color = look.accent, modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                }
                Column(Modifier.width(156.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview", style = type.caption, color = look.textDim)
                    samples.forEach { app -> IconSample(app, apps, preview) }
                    if (samples.isEmpty()) {
                        Text(if (installedApps == null) "Loading apps…" else "No visible apps to preview.", style = type.caption, color = look.textDim)
                    }
                }
            }
            val unavailable = settings.iconStyle == IconStyle.Pack && choices.none { it.matches(settings) }
            val message = when {
                installedPacks == null -> "Looking for installed icon packs… Built-in styles are ready to use."
                unavailable -> "The selected installed pack is unavailable. Choose any built-in style above."
                installedPacks.isNullOrEmpty() -> "No compatible icon packs installed. The five built-in styles above are included."
                else -> "Built-in styles are included. Installed icon packs are listed with them."
            }
            Text(message, style = type.caption, color = look.textDim)
        }
    }
}

private fun IconChoice.description() = when (style) {
    IconStyle.Original -> "App banners and icons"
    IconStyle.Mono -> "Solid monochrome brands · included"
    IconStyle.Line -> "Lawnicons · included"
    IconStyle.Arcticons -> "Fine line icons · included"
    IconStyle.Lawnicons -> "Recommended for TV · bold icons · included"
    IconStyle.Desaturated -> "Original artwork in grayscale"
    IconStyle.Pack -> "Installed icon pack"
}

@Composable
private fun IconSample(app: App, apps: Apps, choice: IconChoice) {
    val look = LocalLook.current
    val pack = if (choice.style == IconStyle.Pack) choice.pack else ""
    var art by remember(app.packageName, pack) { mutableStateOf<AppArt?>(null) }
    LaunchedEffect(app.packageName, pack) {
        art = withContext(Dispatchers.IO) { runCatching { apps.art(app, pack) }.getOrNull() }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            Modifier.width(128.dp).height(72.dp).background(look.surface, RoundedCornerShape(8.dp))
                .semantics { contentDescription = "Preview: ${app.label}" },
        ) { TileArt(app, art, choice.style, 128) }
        Text(app.label, style = LocalType.current.caption, color = look.textDim, maxLines = 1,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.width(128.dp))
    }
}
