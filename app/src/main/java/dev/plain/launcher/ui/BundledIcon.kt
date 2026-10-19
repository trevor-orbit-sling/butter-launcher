package dev.plain.launcher.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.plain.launcher.IconStyle
import dev.plain.launcher.bundledIconAsset

/** Decode the complete upstream SVG, including its CSS, shapes and transforms. */
@Composable
internal fun BundledIcon(packageName: String, style: IconStyle, tint: Color, size: Dp) {
    val asset = bundledIconAsset(style, packageName)
    if (asset == null) {
        StandardAppIcon(tint, size)
        return
    }
    val context = LocalContext.current
    val request = remember(context, asset) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/$asset")
            .decoderFactory(SvgDecoder.Factory())
            .build()
    }
    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier.size(size),
        loading = { StandardAppIcon(tint, size) },
        error = { StandardAppIcon(tint, size) },
    )
}
