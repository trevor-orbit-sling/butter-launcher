package dev.plain.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import dev.plain.launcher.FontFace
import dev.plain.launcher.LauncherSettings
import dev.plain.launcher.R
import dev.plain.launcher.Theme
import dev.plain.launcher.TileShape

data class Look(
    val top: Color,
    val bottom: Color,
    val blobs: List<Color>,
    val surface: Color,
    val surfaceFocused: Color,
    val border: Color,
    val borderWidth: Dp,
    val focusBorder: Color,
    val focusBorderWidth: Dp,
    val text: Color,
    val textDim: Color,
    val accent: Color,
    val panel: Color,
    val corner: Dp,
    val zoom: Float,
)

data class Type(val hero: TextStyle, val title: TextStyle, val body: TextStyle, val label: TextStyle, val caption: TextStyle)

val LocalLook = staticCompositionLocalOf<Look> { error("no look") }
val LocalType = staticCompositionLocalOf<Type> { error("no type") }
val LocalSettings = staticCompositionLocalOf { LauncherSettings() }

@Composable
fun PlainTheme(settings: LauncherSettings, content: @Composable () -> Unit) {
    val look = remember(settings.theme, settings.accent) { look(settings.theme, settings.accent.color) }
    val type = remember(settings.font, settings.textSize) { type(family(settings.font), settings.textSize.scale) }
    CompositionLocalProvider(LocalLook provides look, LocalType provides type, LocalSettings provides settings, content = content)
}

// Android 11 can't blur live, so "glass" is soft colour blobs behind translucent surfaces.
fun Modifier.backdrop(look: Look) = background(Brush.verticalGradient(listOf(look.top, look.bottom))).drawBehind {
    val spots = listOf(Offset(0.15f, 0.2f), Offset(0.85f, 0.35f), Offset(0.45f, 0.95f))
    look.blobs.zip(spots).forEach { (color, spot) ->
        val center = Offset(size.width * spot.x, size.height * spot.y)
        val radius = size.maxDimension * 0.55f
        drawCircle(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center, radius), radius, center)
    }
}

fun tileShape(settings: LauncherSettings, look: Look): Shape = when (settings.tileShape) {
    TileShape.Rounded -> RoundedCornerShape(look.corner)
    TileShape.Square -> RoundedCornerShape(3.dp)
    TileShape.Pill -> RoundedCornerShape(percent = 50)
}

private fun tone(seed: Color, saturation: Float, lightness: Float, hueShift: Float = 0f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    hsl[0] = (hsl[0] + hueShift + 360f) % 360f
    hsl[1] = saturation.coerceIn(0f, 1f)
    hsl[2] = lightness.coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun look(theme: Theme, accent: Color): Look = when (theme) {
    Theme.Glass -> Look(
        top = tone(accent, 0.45f, 0.10f),
        bottom = tone(accent, 0.35f, 0.05f, 30f),
        blobs = listOf(
            tone(accent, 0.70f, 0.45f).copy(alpha = 0.55f),
            tone(accent, 0.65f, 0.45f, 50f).copy(alpha = 0.45f),
            tone(accent, 0.60f, 0.40f, -60f).copy(alpha = 0.45f),
        ),
        surface = Color.White.copy(alpha = 0.10f),
        surfaceFocused = Color.White.copy(alpha = 0.24f),
        border = Color.White.copy(alpha = 0.18f),
        borderWidth = 1.dp,
        focusBorder = Color.White.copy(alpha = 0.75f),
        focusBorderWidth = 1.5.dp,
        text = Color.White,
        textDim = Color.White.copy(alpha = 0.72f),
        accent = tone(accent, 0.85f, 0.75f),
        panel = tone(accent, 0.40f, 0.10f),
        corner = 20.dp,
        zoom = 1.06f,
    )
    Theme.MaterialYou -> {
        val hsl = FloatArray(3).also { ColorUtils.colorToHSL(accent.toArgb(), it) }
        val s = hsl[1].coerceIn(0.45f, 0.85f)
        Look(
            top = tone(accent, s * 0.55f, 0.17f),
            bottom = tone(accent, s * 0.40f, 0.09f),
            blobs = emptyList(),
            surface = tone(accent, s * 0.45f, 0.24f),
            surfaceFocused = tone(accent, s * 0.75f, 0.40f),
            border = Color.Transparent,
            borderWidth = 0.dp,
            focusBorder = tone(accent, s, 0.82f),
            focusBorderWidth = 2.dp,
            text = tone(accent, 0.35f, 0.94f),
            textDim = tone(accent, 0.30f, 0.78f),
            accent = tone(accent, s, 0.80f),
            panel = tone(accent, s * 0.50f, 0.15f),
            corner = 28.dp,
            zoom = 1.05f,
        )
    }
    Theme.Ink -> flat(Color.Black, Color.Black, Color(0xFF121212), Color(0xFF1F1F1F), Color.White, Color(0xFFF2F2F2), Color(0xFF9A9A9A), accent, 10.dp)
    Theme.Paper -> flat(Color(0xFFF3F1EC), Color(0xFFE2DED6), Color.White, Color.White, tone(accent, 0.7f, 0.35f), Color(0xFF1C1C1C), Color(0xFF5F5F5F), accent, 12.dp, Color(0x1A000000))
    Theme.Slate -> flat(Color(0xFF1C2230), Color(0xFF0D1017), Color(0xFF252D3D), Color(0xFF33405A), accent, Color(0xFFE8ECF4), Color(0xFFA7B0C2), accent, 12.dp)
    Theme.Sand -> flat(Color(0xFF2B2520), Color(0xFF161210), Color(0xFF3A322B), Color(0xFF4A4037), accent, Color(0xFFF3EAE0), Color(0xFFC2B4A4), accent, 14.dp)
}

private fun flat(
    top: Color, bottom: Color, surface: Color, focused: Color, focusBorder: Color,
    text: Color, textDim: Color, accent: Color, corner: Dp, border: Color = Color.Transparent,
) = Look(
    top = top, bottom = bottom, blobs = emptyList(), surface = surface, surfaceFocused = focused,
    border = border, borderWidth = if (border == Color.Transparent) 0.dp else 1.dp,
    focusBorder = focusBorder, focusBorderWidth = 2.dp, text = text, textDim = textDim,
    accent = accent, panel = surface, corner = corner, zoom = 1.04f,
)

private fun family(font: FontFace) = when (font) {
    FontFace.Inter -> FontFamily(Font(R.font.inter_400), Font(R.font.inter_600, FontWeight.SemiBold))
    FontFace.Manrope -> FontFamily(Font(R.font.manrope_400), Font(R.font.manrope_600, FontWeight.SemiBold))
    FontFace.SpaceGrotesk -> FontFamily(Font(R.font.space_grotesk_400), Font(R.font.space_grotesk_600, FontWeight.SemiBold))
    FontFace.Outfit -> FontFamily(Font(R.font.outfit_400), Font(R.font.outfit_600, FontWeight.SemiBold))
    FontFace.DmSerif -> FontFamily(Font(R.font.dm_serif_display_400))
    FontFace.PlexMono -> FontFamily(Font(R.font.ibm_plex_mono_400), Font(R.font.ibm_plex_mono_600, FontWeight.SemiBold))
}

private fun type(family: FontFamily, scale: Float) = Type(
    hero = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = (88 * scale).sp, letterSpacing = (-2).sp),
    title = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = (22 * scale).sp),
    body = TextStyle(fontFamily = family, fontSize = (17 * scale).sp),
    label = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = (14 * scale).sp),
    caption = TextStyle(fontFamily = family, fontSize = (13 * scale).sp),
)
