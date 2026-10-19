package dev.plain.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.plain.launcher.LineGlyph
import dev.plain.launcher.lineStrokeWidth
import dev.plain.launcher.lineViewport

/** Original outline artwork stays transparent and takes the current theme's foreground. */
@Composable
internal fun LineIcon(glyph: LineGlyph, tint: Color, size: Dp) {
    val vector = remember(glyph) {
        ImageVector.Builder(
            name = glyph.id,
            defaultWidth = lineViewport.dp,
            defaultHeight = lineViewport.dp,
            viewportWidth = lineViewport,
            viewportHeight = lineViewport,
        ).apply {
            glyph.paths.forEach { path ->
                addPath(
                    pathData = PathParser().parsePathString(path).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = lineStrokeWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
    }
    Image(vector, null, colorFilter = ColorFilter.tint(tint), modifier = Modifier.size(size))
}
