package dev.plain.launcher.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import dev.plain.launcher.App
import dev.plain.launcher.AppArt
import dev.plain.launcher.Apps
import dev.plain.launcher.IconStyle
import dev.plain.launcher.TileShape
import dev.plain.launcher.WatchItem
import dev.plain.launcher.brandGlyphs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FocusSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(LocalLook.current.corner),
    selected: Boolean = false,
    zoom: Float = LocalLook.current.zoom,
    content: @Composable () -> Unit,
) {
    val look = LocalLook.current
    var gamepadDown by remember { mutableStateOf(false) }
    var gamepadLong by remember { mutableStateOf(false) }
    val border = if (selected) BorderStroke(2.dp, look.accent) else BorderStroke(look.borderWidth, look.border)
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.onPreviewKeyEvent { event ->
            if (event.key != Key.ButtonA) return@onPreviewKeyEvent false
            if (event.type == KeyEventType.KeyDown) {
                if (event.nativeKeyEvent.repeatCount == 0) {
                    gamepadDown = true
                    gamepadLong = false
                } else if (gamepadDown && !gamepadLong && onLongClick != null) {
                    gamepadLong = true
                    onLongClick()
                }
            } else if (gamepadDown) {
                gamepadDown = false
                if (!gamepadLong) onClick()
            }
            true
        },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = look.surface,
            focusedContainerColor = look.surfaceFocused,
            pressedContainerColor = look.surfaceFocused,
            contentColor = look.text,
            focusedContentColor = look.text,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border, shape = shape),
            focusedBorder = Border(BorderStroke(look.focusBorderWidth, look.focusBorder), shape = shape),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
    ) { content() }
}

@Composable
fun AppTile(
    app: App,
    apps: Apps,
    width: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    held: Boolean = false,
) {
    val settings = LocalSettings.current
    val look = LocalLook.current
    val pack = if (settings.iconStyle == IconStyle.Pack) settings.iconPack else ""
    var art by remember(app.packageName, pack) { mutableStateOf<AppArt?>(null) }
    LaunchedEffect(app.packageName, pack) {
        art = withContext(Dispatchers.IO) { apps.art(app, pack) }
    }
    Column(Modifier.width(width.dp)) {
        FocusSurface(
            onClick = onClick,
            onLongClick = onLongClick,
            shape = tileShape(settings, look),
            selected = selected,
            modifier = modifier.width(width.dp).height((width * 9 / 16).dp).semantics { contentDescription = app.label },
        ) {
            TileArt(app, art, settings.iconStyle, width)
            if (held) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 10.dp)) {
                    Text("‹", style = LocalType.current.title, color = Color.White, modifier = Modifier.align(Alignment.CenterStart))
                    Text("›", style = LocalType.current.title, color = Color.White, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }
        }
        if (settings.showNames.on) {
            Text(
                app.label,
                style = LocalType.current.caption,
                color = look.textDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            )
        }
    }
}

private val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
private fun TileArt(app: App, art: AppArt?, style: IconStyle, width: Int) {
    val glyph = brandGlyphs[app.packageName]
    val filter = if (style == IconStyle.Desaturated) grayscale else null
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            style == IconStyle.Mono || (style == IconStyle.Pack && art?.icon == null) ->
                if (glyph != null) PathIcon(glyph, 24f, 0f, LocalLook.current.text, (width * 0.22f).dp) else Monogram(app.label)
            art == null -> Unit
            style != IconStyle.Pack && art.banner != null ->
                Image(art.banner, app.label, contentScale = ContentScale.Crop, colorFilter = filter, modifier = Modifier.fillMaxSize())
            art.icon != null ->
                Image(art.icon, app.label, colorFilter = filter, modifier = Modifier.size((width * 0.30f).dp))
        }
    }
}

// Draws SVG path data (Simple Icons use a 24 viewport, Material Symbols 960 shifted up by 960).
@Composable
fun PathIcon(path: String, viewport: Float, shiftY: Float, tint: Color, size: Dp) {
    val vector = remember(path) {
        ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = viewport, viewportHeight = viewport)
            .addGroup(translationY = shiftY)
            .addPath(PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.White))
            .clearGroup()
            .build()
    }
    Image(vector, null, colorFilter = ColorFilter.tint(tint), modifier = Modifier.size(size))
}

@Composable
private fun Monogram(label: String) {
    val letters = label.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1) }.uppercase()
    Text(letters, style = LocalType.current.title, color = LocalLook.current.text)
}

@Composable
fun WatchTile(item: WatchItem, width: Int, onClick: () -> Unit) {
    val look = LocalLook.current
    val type = LocalType.current
    val settings = LocalSettings.current
    // Pill-shaped posters look odd, so they fall back to rounded corners.
    val shape = if (settings.tileShape == TileShape.Pill) RoundedCornerShape(look.corner) else tileShape(settings, look)
    Column(Modifier.width(width.dp)) {
        FocusSurface(onClick = onClick, shape = shape, modifier = Modifier.width(width.dp).height((width * 9 / 16).dp).semantics { contentDescription = item.title }) {
            Box(Modifier.fillMaxSize()) {
                if (item.image != null) {
                    AsyncImage(item.image, item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f))))
                if (item.progress > 0f) {
                    Box(
                        Modifier.align(Alignment.BottomStart).padding(10.dp).fillMaxWidth().height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.25f)),
                    ) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(item.progress).background(look.accent))
                    }
                }
            }
        }
        Text(item.title, style = type.label, color = look.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 10.dp))
        if (item.subtitle.isNotEmpty()) {
            Text(item.subtitle, style = type.caption, color = look.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
