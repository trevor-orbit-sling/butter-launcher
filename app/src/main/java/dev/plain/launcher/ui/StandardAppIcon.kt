package dev.plain.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.plain.launcher.standardAppPath

/** Exact Lawnicons generic_grid_2x2.svg geometry and styling; Apache 2.0. */
@Composable
internal fun StandardAppIcon(tint: Color, size: Dp) {
    val vector = remember {
        ImageVector.Builder(defaultWidth = 192.dp, defaultHeight = 192.dp, viewportWidth = 192f, viewportHeight = 192f)
            .addPath(
                PathParser().parsePathString(standardAppPath).toNodes(), fill = null,
                stroke = SolidColor(Color.White), strokeLineWidth = 12f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            ).build()
    }
    Image(vector, null, colorFilter = ColorFilter.tint(tint), modifier = Modifier.size(size))
}
