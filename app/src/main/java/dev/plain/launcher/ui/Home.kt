package dev.plain.launcher.ui

import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import dev.plain.launcher.App
import dev.plain.launcher.Apps
import dev.plain.launcher.Clock
import dev.plain.launcher.RowOrder
import dev.plain.launcher.RemoteAction
import dev.plain.launcher.WatchItem
import dev.plain.launcher.WatchState
import dev.plain.launcher.WeatherNow
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun HomeScreen(
    apps: Apps,
    installed: List<App>,
    watching: WatchState,
    weather: WeatherNow?,
    homePresses: Int,
    menuPresses: Int,
    onWatch: (WatchItem) -> Unit,
    onChange: (Change) -> Unit,
) {
    val settings = LocalSettings.current
    val look = LocalLook.current
    var settingsOpen by remember { mutableStateOf(false) }
    var tutorialOpen by remember { mutableStateOf(false) }
    var menuApp by remember { mutableStateOf<App?>(null) }
    var focusedRow by remember { mutableStateOf("all") }
    var focusedApp by remember { mutableStateOf<App?>(null) }
    var homeShortcut by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val tileFocus = remember { mutableMapOf<String, FocusRequester>() }
    fun focusKey(row: String, app: App) = "$row:${app.packageName}"
    var originApp by remember { mutableStateOf<App?>(null) }
    var originRow by remember { mutableStateOf("all") }
    var originFocus by remember { mutableStateOf<FocusRequester?>(null) }
    var menuCanMove by remember { mutableStateOf(false) }
    fun showMenu(app: App, row: String) {
        originApp = app
        originRow = row
        originFocus = tileFocus[focusKey(row, app)]
        menuCanMove = row == "favourite"
        menuApp = app
    }
    var held by remember { mutableStateOf<String?>(null) } // favourite being moved
    val page = rememberLazyListState()
    val favouriteRow = rememberLazyListState()
    val customiseFocus = remember { FocusRequester() }
    val allAppsRow = rememberLazyListState()

    val byPackage = installed.associateBy { it.packageName }
    val favourites = settings.favourites.mapNotNull { byPackage[it] }
    val others = installed
    val gap = settings.spacing.gap.dp
    val width = settings.tileSize.width
    val edge = 56.dp

    // Only reorders installed favourites; ones for uninstalled apps stay at the end.
    fun move(pkg: String, by: Int) = onChange { s ->
        val order = s.favourites.filter { it in byPackage }.toMutableList()
        val from = order.indexOf(pkg)
        val to = from + by
        if (from < 0 || to !in order.indices) s
        else s.copy(favourites = order.apply { add(to, removeAt(from)) } + s.favourites.filter { it !in byPackage })
    }

    LaunchedEffect(held, settings.favourites) {
        val index = favourites.indexOfFirst { it.packageName == held }
        if (index >= 0) favouriteRow.animateScrollToItem((index - 1).coerceAtLeast(0))
    }
    LaunchedEffect(homePresses, homeShortcut, installed) {
        settingsOpen = false
        menuApp = null
        held = null
        page.scrollToItem(0)
        favouriteRow.scrollToItem(0)
        allAppsRow.scrollToItem(0)
        androidx.compose.runtime.withFrameNanos { }
        runCatching {
            (favourites.firstOrNull()?.let { tileFocus[focusKey("favourite", it)] } ?: installed.firstOrNull()?.let { tileFocus[focusKey("all", it)] } ?: customiseFocus).requestFocus()
        }
    }
    LaunchedEffect(menuApp, settings.favourites) {
        if (menuApp == null) {
            kotlinx.coroutines.delay(80)
            val target = if (originRow == "favourite" && originApp?.packageName !in settings.favourites) {
                originApp?.let { tileFocus[focusKey("all", it)] }
            } else originFocus
            if (originApp != null) {
                runCatching { (target ?: favourites.firstOrNull()?.let { tileFocus[focusKey("favourite", it)] } ?: customiseFocus).requestFocus() }
                    .onFailure { customiseFocus.requestFocus() }
            }
        }
    }
    LaunchedEffect(menuPresses) { if (menuPresses > 0) settingsOpen = true }
    BackHandler {
        when {
            held != null -> held = null
            menuApp != null -> menuApp = null
            else -> settingsOpen = false
        }
    }

    Box(Modifier.fillMaxSize().backdrop(look).onPreviewKeyEvent { event ->
        if (settingsOpen || tutorialOpen || !settings.tutorialComplete || menuApp != null) return@onPreviewKeyEvent false
        val action = settings.remoteBindings.action(event.nativeKeyEvent.keyCode) ?: return@onPreviewKeyEvent false
        if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.repeatCount == 0) {
            when (action) {
                RemoteAction.Customise -> settingsOpen = true
                RemoteAction.AppOptions -> focusedApp?.let { showMenu(it, if (it.packageName == held) "favourite" else focusedRow) }
                RemoteAction.FirstRow -> homeShortcut++
            }
        }
        true
    }) {
        LazyColumn(
            state = page,
            contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(gap * 1.6f),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { Header(edge, weather, customiseFocus) { settingsOpen = true } }

            fun watchingRow() {
                if (!settings.showWatching.on) return
                if (watching.items.isEmpty()) {
                    item {
                        Column(Modifier.padding(horizontal = edge), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Continue watching", style = LocalType.current.label, color = look.textDim)
                            Text(watching.message, style = LocalType.current.body, color = look.textDim)
                        }
                    }
                    return
                }
                item {
                    Shelf("Continue watching", edge, gap) {
                        items(watching.items, key = { it.id }) { WatchTile(it, (width * 1.35f).toInt()) { onWatch(it) } }
                    }
                }
            }

            fun appRows() {
                if (favourites.isEmpty()) item {
                    Text("Make this home yours. Hold OK on an app below to add a favourite.", style = LocalType.current.body, color = look.textDim, modifier = Modifier.padding(horizontal = edge))
                }
                if (favourites.isNotEmpty()) item {
                    Shelf("Favourites", edge, gap, favouriteRow) {
                        items(favourites, key = { app -> app.packageName }) { app ->
                            val isHeld = held == app.packageName
                            val requester = tileFocus.getOrPut(focusKey("favourite", app)) { FocusRequester() }
                            AppTile(
                                app, apps, width,
                                onClick = { apps.open(app) },
                                onLongClick = { showMenu(app, "favourite") },
                                selected = isHeld || menuApp == app,
                                held = isHeld,
                                modifier = Modifier.focusRequester(requester).onFocusChanged { if (it.isFocused) { focusedApp = app; focusedRow = "favourite" } else if (focusedApp == app && focusedRow == "favourite") focusedApp = null }.onPreviewKeyEvent { e ->
                                    if (!isHeld) { return@onPreviewKeyEvent false }
                                    val down = e.type == KeyEventType.KeyDown
                                    when (e.key) {
                                        Key.DirectionLeft -> { if (down) move(app.packageName, -1); true }
                                        Key.DirectionRight -> { if (down) move(app.packageName, 1); true }
                                        Key.DirectionUp, Key.DirectionDown -> true
                                        Key.Menu -> { if (down) { held = null; showMenu(app, "favourite") }; true }
                                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.ButtonA, Key.Back -> {
                                            if (!down) held = null
                                            true
                                        }
                                        else -> false
                                    }
                                },
                            )
                        }
                    }
                }
                if ((settings.showAllApps.on || favourites.isEmpty()) && others.isNotEmpty()) {
                    item {
                        Shelf("All apps", edge, gap, allAppsRow) {
                            items(others, key = { it.packageName }) { app ->
                                val requester = tileFocus.getOrPut(focusKey("all", app)) { FocusRequester() }
                                AppTile(app, apps, (width * 0.8f).toInt(), onClick = { apps.open(app) }, onLongClick = { showMenu(app, "all") },
                                    modifier = Modifier.focusRequester(requester).onFocusChanged { if (it.isFocused) { focusedApp = app; focusedRow = "all" } else if (focusedApp == app && focusedRow == "all") focusedApp = null })
                            }
                        }
                    }
                }
            }

            if (settings.rowOrder == RowOrder.WatchingFirst) {
                watchingRow()
                appRows()
            } else {
                appRows()
                watchingRow()
            }
        }

        AnimatedVisibility(held != null, Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp), fadeIn(), fadeOut()) {
            Text(
                "◀ ▶  move    OK  done    Menu  more options",
                style = LocalType.current.label,
                color = look.text,
                modifier = Modifier.background(look.panel, RoundedCornerShape(50)).padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }

        AnimatedVisibility(
            settingsOpen,
            Modifier.align(Alignment.CenterEnd),
            slideInHorizontally { it } + fadeIn(),
            slideOutHorizontally { it } + fadeOut(),
        ) {
            SettingsPanel(apps, onChange) { settingsOpen = false; tutorialOpen = true }
        }
    }

    if (!settings.tutorialComplete || tutorialOpen) {
        Onboarding(onFinish = { tutorialOpen = false; onChange { it.copy(tutorialComplete = true) } })
    }

    menuApp?.let { app ->
        AppMenu(
            app = app,
            favourite = app.packageName in settings.favourites,
            canMove = menuCanMove,
            onMove = { menuApp = null; held = app.packageName },
            onInfo = { menuApp = null; apps.openInfo(app) },
            onDismiss = { menuApp = null },
            onChange = onChange,
        )
    }
}

@Composable
private fun Shelf(title: String, edge: Dp, gap: Dp, state: LazyListState = rememberLazyListState(), content: LazyListScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, style = LocalType.current.label, color = LocalLook.current.textDim, modifier = Modifier.padding(start = edge))
        LazyRow(state = state, contentPadding = PaddingValues(horizontal = edge), horizontalArrangement = Arrangement.spacedBy(gap), content = content)
    }
}

@Composable
private fun Header(edge: Dp, weather: WeatherNow?, customiseFocus: FocusRequester, onCustomise: () -> Unit) {
    val settings = LocalSettings.current
    val look = LocalLook.current
    val type = LocalType.current
    val context = LocalContext.current
    val now by produceState(Date()) {
        while (true) {
            value = Date()
            delay(10_000)
        }
    }
    val time = DateFormat.getTimeFormat(context).format(now)
    val date = DateFormat.format("EEEE d MMMM", now).toString()

    Column(Modifier.fillMaxWidth().padding(horizontal = edge), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            if (settings.clock == Clock.Large) {
                Column {
                    Text(time, style = type.hero, color = look.text)
                    Text(date, style = type.body, color = look.textDim)
                    weather?.let { w ->
                        Row(Modifier.padding(top = 14.dp), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
                            PathIcon(w.glyph.path, 960f, 960f, look.accent, 30.dp)
                            Text("${w.temp}°", style = type.title, color = look.text)
                            Text("${w.condition} · H ${w.high}°  L ${w.low}° · ${settings.place}", style = type.body, color = look.textDim)
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (settings.clock == Clock.Corner) {
                        Text(time, style = type.title, color = look.text)
                        Text(date, style = type.caption, color = look.textDim)
                    }
                    weather?.let { w ->
                        if (settings.clock == Clock.Corner) Text("·", style = type.caption, color = look.textDim)
                        PathIcon(w.glyph.path, 960f, 960f, look.accent, 22.dp)
                        Text("${w.temp}°", style = type.label, color = look.text)
                        Text("${w.condition} · ${w.high}° / ${w.low}°", style = type.caption, color = look.textDim)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundButton(Icons.Filled.Edit, "Customise", Modifier.focusRequester(customiseFocus), onCustomise)
                RoundButton(Icons.Filled.Settings, "System settings") {
                    runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
            }
        }
        if (weather != null) {
            FocusSurface(onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://open-meteo.com/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }, zoom = 1.02f) {
                Text("Weather by Open-Meteo ↗", style = type.caption, color = look.textDim, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, description: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FocusSurface(onClick = onClick, shape = CircleShape, modifier = modifier.size(44.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(icon, description, colorFilter = ColorFilter.tint(LocalLook.current.text), modifier = Modifier.size(22.dp))
        }
    }
}
