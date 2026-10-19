package dev.plain.launcher.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.plain.launcher.TileShape

/** A quiet leaf skeleton behind app artwork; cached per tile size and clipped by its surface. */
@Composable
internal fun CanopyTileDecoration(color: Color, shape: TileShape) {
    Box(Modifier.fillMaxSize().drawWithCache {
        val w = size.width
        val h = size.height
        // Bring the base inward for pill cards so the short stem stays inside their curved edge.
        val base = if (shape == TileShape.Pill) Offset(w * 0.21f, h * 0.78f) else Offset(w * 0.13f, h * 0.84f)
        val stemStart = if (shape == TileShape.Pill) Offset(w * 0.17f, h * 0.85f) else Offset(w * 0.09f, h * 0.91f)
        val midrib = Path().apply {
            moveTo(base.x, base.y)
            cubicTo(w * 0.34f, h * 0.69f, w * 0.65f, h * 0.35f, w * 0.90f, h * 0.17f)
        }
        val veins = Path().apply {
            moveTo(w * 0.29f, h * 0.70f)
            quadraticBezierTo(w * 0.23f, h * 0.60f, w * 0.20f, h * 0.43f)
            moveTo(w * 0.29f, h * 0.70f)
            quadraticBezierTo(w * 0.39f, h * 0.82f, w * 0.49f, h * 0.87f)
            moveTo(w * 0.47f, h * 0.53f)
            quadraticBezierTo(w * 0.36f, h * 0.38f, w * 0.33f, h * 0.23f)
            moveTo(w * 0.47f, h * 0.53f)
            quadraticBezierTo(w * 0.62f, h * 0.72f, w * 0.74f, h * 0.75f)
            moveTo(w * 0.70f, h * 0.34f)
            quadraticBezierTo(w * 0.59f, h * 0.21f, w * 0.53f, h * 0.13f)
            moveTo(w * 0.70f, h * 0.34f)
            quadraticBezierTo(w * 0.81f, h * 0.47f, w * 0.88f, h * 0.51f)
        }
        // Fade under the central brand mark, leaving its negative space visually quiet.
        val veinInk = Brush.radialGradient(
            0f to color.copy(alpha = 0.025f),
            0.35f to color.copy(alpha = 0.045f),
            1f to color.copy(alpha = 0.24f),
            center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.52f,
        )
        val ribStroke = Stroke(width = 1.15.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val veinStroke = Stroke(width = 0.95.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val stemStroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val stem = Path().apply { moveTo(stemStart.x, stemStart.y); lineTo(base.x, base.y) }
        onDrawBehind {
            drawPath(veins, veinInk, style = veinStroke)
            drawPath(midrib, veinInk, style = ribStroke)
            drawPath(stem, color.copy(alpha = 0.34f), style = stemStroke)
        }
    })
}
