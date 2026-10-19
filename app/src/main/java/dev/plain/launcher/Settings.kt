package dev.plain.launcher

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface Choice {
    val label: String
}

enum class Theme(override val label: String) : Choice {
    Glass("Frosted glass"), MaterialYou("Material You"), Ink("Minimal ink"),
    Paper("Minimal paper"), Slate("Minimal slate"), Sand("Minimal sand"),
    CatppuccinMocha("Catppuccin Mocha"), CatppuccinLatte("Catppuccin Latte"),
    Dracula("Dracula"), Nord("Nord"),
    Cyberpunk("Cyberpunk"),
    Phosphor("Phosphor"), Orbital("Orbital"), AfterHours("After Hours"),
    DeepSea("Deep Sea"), Butter("Butter"), Canopy("Canopy"),
}

enum class BackgroundStyle(override val label: String) : Choice {
    Theme("Theme"), Blobs("Gradient blobs"), Moving("Moving blobs"),
}

enum class Accent(override val label: String, val color: Color) : Choice {
    Blue("Blue", Color(0xFF4F8DFD)), Teal("Teal", Color(0xFF26A69A)), Green("Green", Color(0xFF66BB6A)),
    Amber("Amber", Color(0xFFFFB300)), Rose("Rose", Color(0xFFEC5F8A)), Lavender("Lavender", Color(0xFF9C7CF4)),
    NeonYellow("Neon yellow", Color(0xFFFCEE0A)),
    Copper("Copper", Color(0xFFC85A28)), HotPink("Hot pink", Color(0xFFFF4FA3)), Lime("Lime", Color(0xFFA5D64B)),
}

enum class FontFace(override val label: String) : Choice {
    Inter("Inter"), Manrope("Manrope"), SpaceGrotesk("Space Grotesk"),
    Outfit("Outfit"), DmSerif("DM Serif"), PlexMono("Plex Mono"),
}

enum class TextSize(override val label: String, val scale: Float) : Choice {
    Small("90%", 0.9f), Normal("100%", 1f), Large("115%", 1.15f), Huge("130%", 1.3f),
}

enum class TileSize(override val label: String, val width: Int) : Choice {
    Small("Small", 150), Medium("Medium", 190), Large("Large", 240),
}

enum class TileShape(override val label: String) : Choice {
    Rounded("Rounded"), Square("Square"), Pill("Pill"),
}

enum class Spacing(override val label: String, val gap: Int) : Choice {
    Compact("Compact", 12), Comfortable("Comfortable", 20), Airy("Airy", 32),
}

enum class Clock(override val label: String) : Choice {
    Hidden("Hidden"), Corner("Corner"), Large("Large"),
}

enum class RowOrder(override val label: String) : Choice {
    WatchingFirst("Watching first"), AppsFirst("Apps first"),
}

enum class IconStyle(override val label: String) : Choice {
    Original("Original"), Mono("Minimal mono"), Line("Lawnicons"), Arcticons("Arcticons"), Lawnicons("Lawnicons"),
    Desaturated("Desaturated"), Pack("Icon pack"),
}

enum class TempUnit(override val label: String) : Choice {
    Celsius("°C"), Fahrenheit("°F"),
}

enum class Toggle(override val label: String) : Choice {
    On("On"), Off("Off");

    val on get() = this == On
}

@Serializable
data class LauncherSettings(
    val remoteBindings: RemoteBindings = RemoteBindings(),
    val tutorialComplete: Boolean = false,
    val theme: Theme = Theme.Glass,
    val background: BackgroundStyle = BackgroundStyle.Theme,
    val accent: Accent = Accent.Blue,
    val font: FontFace = FontFace.Inter,
    val textSize: TextSize = TextSize.Normal,
    val tileSize: TileSize = TileSize.Medium,
    val tileShape: TileShape = TileShape.Rounded,
    val spacing: Spacing = Spacing.Comfortable,
    val showNames: Toggle = Toggle.On,
    val clock: Clock = Clock.Corner,
    val rowOrder: RowOrder = RowOrder.WatchingFirst,
    val showWatching: Toggle = Toggle.On,
    val showAllApps: Toggle = Toggle.On,
    val iconStyle: IconStyle = IconStyle.Original,
    val iconPack: String = "",
    val favourites: List<String> = emptyList(),
    val hiddenApps: List<String> = emptyList(),
    val showWeather: Toggle = Toggle.Off,
    val tempUnit: TempUnit = TempUnit.Celsius,
    val place: String = "",
    val lat: String = "",
    val lon: String = "",
) {
    val weatherQuery get() = if (showWeather.on && validCoordinates(lat, lon)) WeatherQuery(lat, lon, tempUnit) else null

}

private val Context.dataStore by preferencesDataStore("settings")
private val key = stringPreferencesKey("settings")
internal val settingsJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

class SettingsStore(private val context: Context) {
    val settings = context.dataStore.data.map { decodeSettings(it[key]) }

    suspend fun update(change: (LauncherSettings) -> LauncherSettings) {
        context.dataStore.edit { it[key] = settingsJson.encodeToString(change(decodeSettings(it[key]))) }
    }

}

internal fun decodeSettings(text: String?) =
    (text?.let { runCatching { settingsJson.decodeFromString<LauncherSettings>(it) }.getOrNull() } ?: LauncherSettings())
        .let { if (it.iconStyle == IconStyle.Line) it.copy(iconStyle = IconStyle.Lawnicons) else it }

inline fun <reified E : Enum<E>> E.cycle(step: Int): E = enumValues<E>().let { it[(ordinal + step).mod(it.size)] }

internal fun validCoordinates(lat: String, lon: String): Boolean {
    val latitude = lat.toDoubleOrNull() ?: return false
    val longitude = lon.toDoubleOrNull() ?: return false
    return latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
}

/** Apply defaults only for explicit selection; saved or manually edited choices are left alone. */
internal fun LauncherSettings.changeTheme(theme: Theme): LauncherSettings =
    if (theme == this.theme) this else copy(theme = theme, accent = theme.defaultAccent, font = theme.defaultFont ?: font)

internal fun LauncherSettings.hideApp(packageName: String): LauncherSettings =
    if (packageName in hiddenApps) this else copy(hiddenApps = hiddenApps + packageName)

internal fun LauncherSettings.restoreApp(packageName: String): LauncherSettings =
    copy(hiddenApps = hiddenApps.filterNot { it == packageName })

internal fun visibleApps(installed: List<App>, hiddenApps: List<String>): List<App> {
    val hidden = hiddenApps.toHashSet()
    return installed.filterNot { it.packageName in hidden }
}

internal val Theme.defaultAccent: Accent get() = when (this) {
    Theme.Glass, Theme.Ink, Theme.Slate -> Accent.Blue
    Theme.MaterialYou, Theme.CatppuccinMocha, Theme.CatppuccinLatte, Theme.Dracula -> Accent.Lavender
    Theme.Paper, Theme.Nord -> Accent.Teal
    Theme.Sand -> Accent.Amber
    Theme.Cyberpunk -> Accent.NeonYellow
    Theme.Phosphor, Theme.Butter -> Accent.Amber
    Theme.Orbital -> Accent.Copper
    Theme.AfterHours -> Accent.HotPink
    Theme.DeepSea -> Accent.Teal
    Theme.Canopy -> Accent.Lime
}

internal val Theme.defaultFont: FontFace? get() = when (this) {
    Theme.Phosphor -> FontFace.PlexMono
    Theme.Orbital -> FontFace.SpaceGrotesk
    Theme.AfterHours, Theme.Butter -> FontFace.Outfit
    Theme.DeepSea, Theme.Canopy -> FontFace.Manrope
    else -> null
}
