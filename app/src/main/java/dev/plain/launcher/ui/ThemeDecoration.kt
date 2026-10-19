package dev.plain.launcher.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.plain.launcher.Theme
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

internal data class ThemeMark(val path: Path, val color: Color, val style: DrawStyle = Fill)

/** Original decorative paths cached with the backdrop. All marks remain static, including CRT lines. */
internal fun themeMarks(theme: Theme, look: Look, size: Size, unit: Float): List<ThemeMark> {
    val marks = mutableListOf<ThemeMark>()
    val w = size.width
    val h = size.height
    fun fill(color: Color, path: Path.() -> Unit) { marks += ThemeMark(Path().apply(path), color) }
    fun line(color: Color, width: Float = unit, path: Path.() -> Unit) {
        marks += ThemeMark(Path().apply(path), color, Stroke(width))
    }
    when (theme) {
        Theme.Phosphor -> {
            line(Color(0xFF83C99B).copy(alpha = 0.045f)) {
                var y = 0f
                while (y < h) { moveTo(0f, y); lineTo(w, y); y += 4f * unit }
            }
            line(look.accent.copy(alpha = 0.17f)) {
                addRect(Rect(18f * unit, 14f * unit, w - 18f * unit, h - 14f * unit))
                for (index in 0..22) {
                    val x = w * 0.62f + index * w * 0.014f
                    moveTo(x, h - 24f * unit); lineTo(x, h - (if (index % 5 == 0) 32f else 28f) * unit)
                }
                moveTo(w * 0.64f, h * 0.85f)
                for (index in 0..90) {
                    val t = index / 90f
                    lineTo(w * (0.64f + t * 0.31f), h * (0.85f + sin(t * PI * 8).toFloat() * 0.035f))
                }
            }
        }
        Theme.Orbital -> {
            val center = Offset(w * 0.91f, h * 0.83f)
            val radius = h * 0.30f
            fill(Color(0xFFC85A28).copy(alpha = 0.12f)) { addOval(Rect(center - Offset(radius, radius), Size(radius * 2, radius * 2))) }
            line(Color(0xFF142C3F).copy(alpha = 0.16f), 2f * unit) {
                for (factor in listOf(0.72f, 1.12f, 1.45f)) {
                    val r = radius * factor
                    addOval(Rect(center - Offset(r, r * 0.42f), Size(r * 2, r * 0.84f)))
                }
                moveTo(20f * unit, 18f * unit); lineTo(w * 0.57f, 18f * unit)
                moveTo(20f * unit, 25f * unit); lineTo(w * 0.30f, 25f * unit)
            }
            fill(Color(0xFFC85A28).copy(alpha = 0.38f)) {
                addRect(Rect(20f * unit, h - 26f * unit, w * 0.16f, h - 18f * unit))
                addRect(Rect(w * 0.17f, h - 26f * unit, w * 0.23f, h - 18f * unit))
            }
        }
        Theme.AfterHours -> {
            val center = Offset(w * 0.84f, h * 0.26f)
            val radius = h * 0.19f
            fill(Color(0xFFFF589E).copy(alpha = 0.16f)) { addOval(Rect(center - Offset(radius, radius), Size(radius * 2, radius * 2))) }
            line(look.top, 5f * unit) {
                for (index in 1..5) {
                    val y = radius * index / 6f
                    val half = sqrt(radius * radius - y * y)
                    moveTo(center.x - half, center.y + y); lineTo(center.x + half, center.y + y)
                }
            }
            line(Color(0xFF9065DF).copy(alpha = 0.14f)) {
                val horizon = h * 0.72f
                for (index in 0..12) {
                    moveTo(w * 0.70f, horizon); lineTo(w * (index / 8f - 0.25f), h)
                }
                for (fraction in listOf(0f, 0.12f, 0.29f, 0.54f, 0.88f)) {
                    val y = horizon + (h - horizon) * fraction
                    moveTo(0f, y); lineTo(w, y)
                }
            }
        }
        Theme.DeepSea -> {
            line(look.accent.copy(alpha = 0.13f)) {
                for (index in 0..4) {
                    val y = h * (0.72f + index * 0.055f)
                    moveTo(w * 0.48f, y)
                    cubicTo(w * 0.67f, y - h * 0.18f, w * 0.84f, y + h * 0.15f, w * 1.05f, y - h * 0.07f)
                }
            }
            fill(Color(0xFF71F8C9).copy(alpha = 0.18f)) {
                for (index in 0..13) {
                    val x = w * (0.74f + (index % 4) * 0.065f)
                    val y = h * (0.24f + index * 0.05f)
                    val r = (1.5f + index % 3) * unit
                    addOval(Rect(x - r, y - r, x + r, y + r))
                }
            }
        }
        Theme.Butter -> {
            fill(Color(0xFFD59C2B).copy(alpha = 0.13f)) {
                moveTo(w * 0.84f, -h * 0.10f)
                cubicTo(w * 0.65f, h * 0.17f, w * 1.04f, h * 0.16f, w * 0.85f, h * 0.46f)
                cubicTo(w * 0.70f, h * 0.66f, w * 0.92f, h * 0.88f, w * 1.08f, h * 0.65f)
                lineTo(w * 1.1f, -h * 0.10f); close()
                moveTo(-w * 0.10f, h * 0.86f)
                cubicTo(w * 0.05f, h * 0.66f, w * 0.20f, h * 1.08f, w * 0.39f, h * 0.94f)
                lineTo(w * 0.45f, h * 1.10f); lineTo(-w * 0.1f, h * 1.1f); close()
            }
            fill(Color(0xFF956519).copy(alpha = 0.18f)) {
                for (index in 0..8) {
                    val x = w * (0.80f + index % 3 * 0.055f)
                    val y = h * (0.70f + index / 3 * 0.065f)
                    val r = (2f + index % 2) * unit
                    addOval(Rect(x - r, y - r, x + r, y + r))
                }
            }
        }
        Theme.Canopy -> {
            // A curved fern at the right edge: closed leaves with separate visible veins.
            val stem = Path().apply {
                moveTo(w * 1.02f, h * 1.08f)
                cubicTo(w * 0.80f, h * 0.76f, w * 0.96f, h * 0.37f, w * 0.83f, h * 0.06f)
            }
            marks += ThemeMark(stem, Color(0xFF90C27E).copy(alpha = 0.23f), Stroke(2f * unit))
            for (index in 0..8) {
                val t = index / 8f
                val base = Offset(w * (0.95f - 0.09f * t), h * (0.94f - 0.78f * t))
                val side = if (index % 2 == 0) -1f else 1f
                val tip = base + Offset(side * w * (0.085f - t * 0.025f), -h * 0.13f)
                val middle = (base + tip) / 2f
                fill(Color(0xFF8FBF78).copy(alpha = if (index % 2 == 0) 0.12f else 0.08f)) {
                    moveTo(base.x, base.y)
                    cubicTo(middle.x - side * w * 0.035f, middle.y - h * 0.055f, tip.x - side * w * 0.02f, tip.y, tip.x, tip.y)
                    cubicTo(tip.x + side * w * 0.025f, tip.y + h * 0.08f, middle.x + side * w * 0.035f, middle.y + h * 0.04f, base.x, base.y)
                    close()
                }
                line(look.accent.copy(alpha = 0.15f)) {
                    moveTo(base.x, base.y); quadraticBezierTo(middle.x, middle.y, tip.x, tip.y)
                }
            }
        }
        else -> Unit
    }
    return marks
}
