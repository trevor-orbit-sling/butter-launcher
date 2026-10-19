package dev.plain.launcher.ui

import android.animation.ValueAnimator
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.plain.launcher.BackgroundStyle
import dev.plain.launcher.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

private val blobSpots = listOf(Offset(0.15f, 0.2f), Offset(0.85f, 0.35f), Offset(0.45f, 0.95f))

internal fun shouldMoveBackground(style: BackgroundStyle, resumed: Boolean, durationScale: Float) =
    style == BackgroundStyle.Moving && resumed && durationScale.isFinite() && durationScale > 0f

/** Three cached radial shaders; animation state is read only in the draw phase, never by home content. */
@Composable
fun Modifier.backdrop(look: Look, style: BackgroundStyle, accent: Color): Modifier {
    val cyberpunk = LocalSettings.current.theme == Theme.Cyberpunk
    val phase = if (style == BackgroundStyle.Moving) rememberBlobPhase() else null
    val colors = remember(look, style, accent) {
        if (style == BackgroundStyle.Theme) look.blobs
        else {
            val light = look.top.luminance() > 0.5f
            listOf(0f, 65f, -55f).mapIndexed { index, shift ->
                tone(accent, 0.78f, if (light) 0.62f else 0.40f, shift)
                    .copy(alpha = if (light) 0.25f - index * 0.025f else 0.38f - index * 0.035f)
            }
        }
    }
    return drawWithCache {
        val background = Brush.verticalGradient(listOf(look.top, look.bottom))
        val radius = size.maxDimension * 0.55f
        val shaders = colors.map { color ->
            Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), Offset.Zero, radius)
        }
        // Static paths are built once per size/theme, not once per animation frame.
        val grid = Path()
        val scanlines = Path()
        val edgeMarks = Path()
        if (cyberpunk) {
            val gridStep = 48.dp.toPx()
            var x = 0f
            while (x <= size.width) { grid.moveTo(x, 0f); grid.lineTo(x, size.height); x += gridStep }
            var y = 0f
            while (y <= size.height) { grid.moveTo(0f, y); grid.lineTo(size.width, y); y += gridStep }
            y = 0f
            val scanStep = 4.dp.toPx()
            while (y <= size.height) { scanlines.moveTo(0f, y); scanlines.lineTo(size.width, y); y += scanStep }
            val inset = 14.dp.toPx()
            val length = 70.dp.toPx()
            edgeMarks.moveTo(inset, inset + length)
            edgeMarks.lineTo(inset, inset + 12.dp.toPx())
            edgeMarks.lineTo(inset + 12.dp.toPx(), inset)
            edgeMarks.lineTo(inset + length, inset)
            edgeMarks.moveTo(size.width - inset - length, size.height - inset)
            edgeMarks.lineTo(size.width - inset - 12.dp.toPx(), size.height - inset)
            edgeMarks.lineTo(size.width - inset, size.height - inset - 12.dp.toPx())
            edgeMarks.lineTo(size.width - inset, size.height - inset - length)
        }
        val hairline = Stroke(width = 1.dp.toPx())
        val edgeStroke = Stroke(width = 2.dp.toPx())
        onDrawBehind {
            drawRect(background)
            val cycle = phase?.floatValue ?: 0f
            shaders.forEachIndexed { index, shader ->
                val spot = blobSpots[index]
                val angle = index * 2.0 * PI / 3.0
                val dx = if (phase == null) 0f else ((sin(cycle * 2.0 * PI + angle) - sin(angle)) * 0.06).toFloat()
                val dy = if (phase == null) 0f else ((sin(cycle * 2.0 * PI + angle + 0.9) - sin(angle + 0.9)) * 0.05).toFloat()
                withTransform({ translate(size.width * (spot.x + dx), size.height * (spot.y + dy)) }) {
                    drawCircle(shader, radius, Offset.Zero)
                }
            }
            if (cyberpunk) {
                drawPath(grid, Color(0xFF00F0FF).copy(alpha = 0.045f), style = hairline)
                drawPath(scanlines, Color.White.copy(alpha = 0.018f), style = hairline)
                drawPath(edgeMarks, accent.copy(alpha = 0.6f), style = edgeStroke)
                drawLine(Color(0xFFFF2A8D).copy(alpha = 0.5f), Offset(size.width * 0.66f, size.height - 14.dp.toPx()), Offset(size.width * 0.83f, size.height - 14.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
        }
    }
}

@Composable
private fun rememberBlobPhase(): androidx.compose.runtime.MutableFloatState {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val phase = remember { mutableFloatStateOf(0f) }
    var resumed by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    fun readScale() = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(0f)
    var durationScale by remember(context) { mutableFloatStateOf(readScale()) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, _ -> resumed = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { durationScale = readScale() }
        }
        val registered = runCatching {
            context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        }.isSuccess
        onDispose { if (registered) context.contentResolver.unregisterContentObserver(observer) }
    }
    val animate = shouldMoveBackground(BackgroundStyle.Moving, resumed, durationScale)
    LaunchedEffect(animate, durationScale) {
        if (!animate) return@LaunchedEffect
        var previous = SystemClock.uptimeMillis()
        while (isActive) {
            // A tiny shift at 15 fps is smooth at this speed, without scheduling 60/120 home redraws a second.
            val enabled = ValueAnimator.areAnimatorsEnabled()
            delay(if (enabled) 66L else 1_000L)
            val now = SystemClock.uptimeMillis()
            if (enabled && ValueAnimator.areAnimatorsEnabled()) {
                phase.floatValue = (phase.floatValue + (now - previous) / (180_000f * durationScale.coerceAtLeast(1f))).mod(1f)
            }
            previous = now
        }
    }
    return phase
}
