package dev.plain.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Text
import dev.plain.launcher.App
import dev.plain.launcher.Apps
import dev.plain.launcher.Choice
import dev.plain.launcher.IconStyle
import dev.plain.launcher.LauncherSettings
import dev.plain.launcher.Place
import dev.plain.launcher.Weather
import dev.plain.launcher.cycle
import dev.plain.launcher.changeTheme
import dev.plain.launcher.restoreApp
import dev.plain.launcher.addFavourite
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

typealias Change = (LauncherSettings) -> LauncherSettings
internal val SettingsPanelWidth = 440.dp

internal class Option(val label: String, val value: String, val open: (() -> Unit)? = null, val step: (Int) -> Unit = {}) {
    fun adjust(direction: Int) { open?.invoke() ?: step(direction) }
}

internal inline fun <reified E> option(
    label: String,
    settings: LauncherSettings,
    crossinline read: (LauncherSettings) -> E,
    noinline onChange: (Change) -> Unit,
    crossinline set: LauncherSettings.(E) -> LauncherSettings,
): Option where E : Enum<E>, E : Choice = Option(label, read(settings).label) { step ->
    onChange { current -> current.set(read(current).cycle(step)) }
}

internal fun weatherOption(settings: LauncherSettings, onChange: (Change) -> Unit, onLocation: () -> Unit): Option =
    if (!settings.hasWeatherLocation) Option("Weather", "Choose a city", open = onLocation)
    else option("Weather", settings, LauncherSettings::showWeather, onChange) { copy(showWeather = it) }

@Composable
fun SettingsPanel(apps: Apps, installed: List<App>, onChange: (Change) -> Unit, onTutorial: () -> Unit) {
    val s = LocalSettings.current
    val look = LocalLook.current
    val type = LocalType.current
    val context = LocalContext.current
    val first = remember { FocusRequester() }
    val hiddenButton = remember { FocusRequester() }
    var choosingHidden by remember { mutableStateOf(false) }
    if (choosingHidden) HiddenAppsDialog(installed, onChange) {
        choosingHidden = false
        runCatching { hiddenButton.requestFocus() }
    }
    var choosingRemote by remember { mutableStateOf(false) }
    if (choosingRemote) RemoteDialog(onChange) { choosingRemote = false }
    var choosingPlace by remember { mutableStateOf(false) }
    var choosingIcons by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    if (choosingPlace) PlaceDialog(onChange) { choosingPlace = false }
    if (choosingIcons) IconChooser(apps, onChange) { choosingIcons = false }

    val sections = listOf(
        "Theme" to listOf(
            option("Theme", s, LauncherSettings::theme, onChange) { changeTheme(it) },
            option("Accent", s, LauncherSettings::accent, onChange) { copy(accent = it) },
            option("Background", s, LauncherSettings::background, onChange) { copy(background = it) },
        ),
        "Text" to listOf(
            option("Font", s, LauncherSettings::font, onChange) { copy(font = it) },
            option("Text size", s, LauncherSettings::textSize, onChange) { copy(textSize = it) },
        ),
        "Layout" to listOf(
            option("Tile size", s, LauncherSettings::tileSize, onChange) { copy(tileSize = it) },
            option("Tile shape", s, LauncherSettings::tileShape, onChange) { copy(tileShape = it) },
            option("Spacing", s, LauncherSettings::spacing, onChange) { copy(spacing = it) },
            option("App names", s, LauncherSettings::showNames, onChange) { copy(showNames = it) },
            option("Clock", s, LauncherSettings::clock, onChange) { copy(clock = it) },
            option("Row order", s, LauncherSettings::rowOrder, onChange) { copy(rowOrder = it) },
            option("Continue watching", s, LauncherSettings::showWatching, onChange) { copy(showWatching = it) },
            option("All apps row", s, LauncherSettings::showAllApps, onChange) { copy(showAllApps = it) },
        ),
        "Weather" to listOf(
            weatherOption(s, onChange) { choosingPlace = true },
            option("Units", s, LauncherSettings::tempUnit, onChange) { copy(tempUnit = it) },
            Option("Location", s.place.ifBlank { "Choose a city" }, open = { choosingPlace = true }),
        ),
        "Icons" to listOf(
            Option("Choose icons", if (s.iconStyle == IconStyle.Pack) "Installed pack" else s.iconStyle.label,
                open = { choosingIcons = true }) { choosingIcons = true },
        ),
    )

    Column(Modifier.fillMaxHeight().width(SettingsPanelWidth).background(look.panel).padding(vertical = 28.dp)) {
        Text("Settings", style = type.title, color = look.text, modifier = Modifier.padding(horizontal = 28.dp))
        Text("◀ ▶ to change · OK to open · Back to close", style = type.caption, color = look.textDim, modifier = Modifier.padding(start = 28.dp, top = 4.dp, bottom = 12.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { MenuButton("Quick tour", onClick = onTutorial) }
            item { MenuButton("Remote & controller") { choosingRemote = true } }
            item { MenuButton("Screen saver") { openScreenSaverSettings(context) } }
            item { MenuButton("Hidden apps", Modifier.focusRequester(hiddenButton)) { choosingHidden = true } }
            sections.forEachIndexed { i, (title, options) ->
                item { Text(title, style = type.caption, color = look.accent, modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 4.dp)) }
                options.forEachIndexed { j, o ->
                    item(key = o.label) { OptionRow(o, if (i == 0 && j == 0) Modifier.focusRequester(first) else Modifier) }
                }
                if (title == "Weather") {
                    item {
                        FocusSurface(onClick = {
                            runCatching {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://open-meteo.com/")).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }, zoom = 1.02f) {
                            Text("Weather by Open-Meteo ↗", style = type.caption, color = look.textDim, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(o: Option, modifier: Modifier) {
    val look = LocalLook.current
    val type = LocalType.current
    FocusSurface(
        onClick = { o.adjust(1) },
        zoom = 1.02f,
        modifier = modifier.fillMaxWidth().onPreviewKeyEvent { e ->
            when {
                e.type != KeyEventType.KeyDown -> false
                e.key == Key.DirectionLeft -> { o.adjust(-1); true }
                e.key == Key.DirectionRight -> { o.adjust(1); true }
                else -> false
            }
        },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(o.label, style = type.body, color = look.text)
            Text(if (o.open != null) "${o.value}  ›" else "‹  ${o.value}  ›", style = type.label, color = look.accent, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PopupCard(width: Int, content: @Composable ColumnScope.() -> Unit) {
    val look = LocalLook.current
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    Column(
        Modifier.width(width.dp).heightIn(max = maxHeight).background(look.panel, RoundedCornerShape(look.corner)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun MenuButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FocusSurface(onClick = onClick, zoom = 1.02f, modifier = modifier.fillMaxWidth()) {
        Text(text, style = LocalType.current.body, color = LocalLook.current.text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    }
}

@Composable
fun AppMenu(app: App, favourite: Boolean, canMove: Boolean, onMove: () -> Unit, onInfo: () -> Unit, onHide: (() -> Unit)?, onDismiss: () -> Unit, onChange: (Change) -> Unit) {
    val first = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
        PopupCard(360) {
            Text(app.label, style = LocalType.current.title, color = LocalLook.current.text, modifier = Modifier.padding(start = 6.dp, bottom = 6.dp))
            if (favourite) {
                if (canMove) MenuButton("Move", Modifier.focusRequester(first), onMove)
                MenuButton("Remove from favourites", if (!canMove) Modifier.focusRequester(first) else Modifier) { onChange { it.copy(favourites = it.favourites.filterNot { pkg -> pkg == app.packageName }) }; onDismiss() }
            } else {
                MenuButton("Add to favourites", Modifier.focusRequester(first)) { onChange { it.addFavourite(app.packageName) }; onDismiss() }
            }
            if (onHide != null) MenuButton("Hide from All apps", onClick = onHide)
            MenuButton("App info", onClick = onInfo)
        }
    }
}

@Composable
private fun HiddenAppsDialog(installed: List<App>, onChange: (Change) -> Unit, onDismiss: () -> Unit) {
    val hidden = LocalSettings.current.hiddenApps.distinct()
    val labels = installed.associate { it.packageName to it.label }
    val duplicateLabels = installed.groupingBy { it.label }.eachCount().filterValues { it > 1 }.keys
    val look = LocalLook.current
    val type = LocalType.current
    val first = remember { FocusRequester() }
    val done = remember { FocusRequester() }
    val list = androidx.compose.foundation.lazy.rememberLazyListState()
    Dialog(onDismissRequest = onDismiss) {
        LaunchedEffect(hidden) {
            list.scrollToItem(0)
            androidx.compose.runtime.withFrameNanos { }
            runCatching { (if (hidden.isEmpty()) done else first).requestFocus() }
        }
        PopupCard(520) {
            Text("Hidden apps", style = type.title, color = look.text)
            Text("Hidden apps stay in your favourites. Restore an app to show it in All apps again.", style = type.caption, color = look.textDim)
            if (hidden.isEmpty()) Text("No hidden apps.", style = type.body, color = look.textDim, modifier = Modifier.padding(vertical = 12.dp))
            LazyColumn(state = list, modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(hidden, key = { _, pkg -> pkg }) { index, pkg ->
                    Column {
                        MenuButton("Restore ${labels[pkg] ?: pkg}", if (index == 0) Modifier.focusRequester(first) else Modifier) {
                            onChange { it.restoreApp(pkg) }
                        }
                        if (labels[pkg] in duplicateLabels) Text(pkg, style = type.caption, color = look.textDim, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                    }
                }
            }
            MenuButton("Done", Modifier.focusRequester(done), onDismiss)
        }
    }
}

@Composable
private fun PlaceDialog(onChange: (Change) -> Unit, onDismiss: () -> Unit) {
    val look = LocalLook.current
    val type = LocalType.current
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var query by remember { mutableStateOf("") }
    var places by remember { mutableStateOf(emptyList<Place>()) }
    var status by remember { mutableStateOf("Type a city, then press Search.") }
    val field = remember { FocusRequester() }
    val searchButton = remember { FocusRequester() }
    val firstResult = remember { FocusRequester() }
    val resultList = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    fun pick(place: Place) {
        keyboard?.hide()
        onChange { it.copy(place = place.name, lat = place.lat, lon = place.lon, showWeather = dev.plain.launcher.Toggle.On) }
        onDismiss()
    }
    fun search() {
        val name = query.trim()
        if (name.isBlank()) return
        searchJob?.cancel()
        keyboard?.hide()
        searchButton.requestFocus()
        places = emptyList()
        status = "Searching…"
        searchJob = scope.launch {
            try {
                places = Weather.search(name)
                status = if (places.isEmpty()) "Nothing found for \"$name\"." else "Pick a place:"
                resultList.scrollToItem(0)
                androidx.compose.runtime.withFrameNanos { }
                runCatching { if (places.isNotEmpty()) firstResult.requestFocus() else searchButton.requestFocus() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                status = "Couldn't search right now. Check your connection and try again."
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        LaunchedEffect(Unit) { runCatching { field.requestFocus() } }
        PopupCard(520) {
            Text("Weather location", style = type.title, color = look.text)
            Text("City searches and selected coordinates are sent to Open-Meteo. Choosing a city enables weather.", style = type.caption, color = look.textDim)
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = type.body.copy(color = look.text),
                cursorBrush = SolidColor(look.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }, onDone = { search() }),
                modifier = Modifier.fillMaxWidth().focusRequester(field).onPreviewKeyEvent { event ->
                    if (event.key != Key.DirectionDown) return@onPreviewKeyEvent false
                    if (event.type == KeyEventType.KeyDown) {
                        keyboard?.hide()
                        scope.launch {
                            resultList.scrollToItem(0)
                            androidx.compose.runtime.withFrameNanos { }
                            runCatching { if (places.isNotEmpty()) firstResult.requestFocus() else searchButton.requestFocus() }
                        }
                    }
                    true
                },
                decorationBox = { input ->
                    Box(Modifier.fillMaxWidth().background(look.surface, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
                        if (query.isEmpty()) Text("City name", style = type.body, color = look.textDim)
                        input()
                    }
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuButton("Search", Modifier.weight(1f).focusRequester(searchButton)) { search() }
            }
            Text(status, style = type.caption, color = look.textDim)
            LazyColumn(Modifier.weight(1f, fill = false), state = resultList, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(places) { index, place ->
                    MenuButton(
                        listOf(place.name, place.detail).filter { it.isNotEmpty() }.joinToString(" · "),
                        if (index == 0) Modifier.focusRequester(firstResult) else Modifier,
                    ) { pick(place) }
                }
            }
        }
    }
}

private fun openScreenSaverSettings(context: android.content.Context) {
    val dream = android.content.Intent(android.provider.Settings.ACTION_DREAM_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    if (runCatching { context.startActivity(dream) }.isSuccess) return
    val settings = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    val message = if (runCatching { context.startActivity(settings) }.isSuccess) {
        "Look for Screen saver in your TV settings. Availability depends on the device."
    } else "Screen saver settings aren't available on this TV."
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
}
