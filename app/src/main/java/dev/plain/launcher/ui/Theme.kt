package dev.plain.launcher.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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
    val type = remember(settings.font, settings.textSize, settings.theme) { type(family(settings.font), settings.textSize.scale, settings.theme) }
    CompositionLocalProvider(LocalLook provides look, LocalType provides type, LocalSettings provides settings, content = content)
}


fun tileShape(settings: LauncherSettings, look: Look): Shape = when (settings.tileShape) {
    TileShape.Rounded -> when (settings.theme) {
        Theme.Cyberpunk -> CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp)
        Theme.Phosphor -> RoundedCornerShape(2.dp)
        Theme.Orbital -> RoundedCornerShape(topStart = 28.dp, topEnd = 6.dp, bottomEnd = 28.dp, bottomStart = 6.dp)
        Theme.AfterHours -> RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomEnd = 8.dp, bottomStart = 8.dp)
        Theme.DeepSea -> RoundedCornerShape(topStart = 24.dp, topEnd = 12.dp, bottomEnd = 28.dp, bottomStart = 12.dp)
        Theme.Canopy -> RoundedCornerShape(topStart = 36.dp, topEnd = 10.dp, bottomEnd = 36.dp, bottomStart = 10.dp)
        else -> RoundedCornerShape(look.corner)
    }
    TileShape.Square -> RoundedCornerShape(3.dp)
    TileShape.Pill -> RoundedCornerShape(percent = 50)
}

internal fun tone(seed: Color, saturation: Float, lightness: Float, hueShift: Float = 0f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    hsl[0] = (hsl[0] + hueShift + 360f) % 360f
    hsl[1] = saturation.coerceIn(0f, 1f)
    hsl[2] = lightness.coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}

internal fun look(theme: Theme, accent: Color): Look = when (theme) {
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
    // Published MIT base/surface/text palettes; pinned sources and full notices ship with the app.
    // Native palette focus colours stay recognisable; the user's accent still colours setting values.
    Theme.CatppuccinMocha -> flat(
        Color(0xFF1E1E2E), Color(0xFF181825), Color(0xFF313244), Color(0xFF45475A),
        Color(0xFFCBA6F7), Color(0xFFCDD6F4), Color(0xFFBAC2DE), tone(accent, 0.65f, 0.82f), 16.dp,
    )
    Theme.CatppuccinLatte -> flat(
        Color(0xFFEFF1F5), Color(0xFFE6E9EF), Color(0xFFE6E9EF), Color(0xFFDCE0E8),
        Color(0xFF8839EF), Color(0xFF4C4F69), Color(0xFF5C5F77), tone(accent, 0.70f, 0.32f), 16.dp,
    )
    Theme.Dracula -> flat(
        Color(0xFF282A36), Color(0xFF282A36), Color(0xFF44475A), Color(0xFF44475A),
        Color(0xFFBD93F9), Color(0xFFF8F8F2), Color(0xFFF8F8F2).copy(alpha = 0.72f), tone(accent, 0.65f, 0.82f), 12.dp,
    )
    Theme.Nord -> flat(
        Color(0xFF2E3440), Color(0xFF2E3440), Color(0xFF3B4252), Color(0xFF434C5E),
        Color(0xFF88C0D0), Color(0xFFECEFF4), Color(0xFFD8DEE9), tone(accent, 0.65f, 0.82f), 12.dp,
    )
    // Original neon-terminal styling; no game artwork, logos or fonts are used.
    Theme.Cyberpunk -> Look(
        top = Color(0xFF080D16), bottom = Color(0xFF03060B),
        blobs = listOf(Color(0xFF00F0FF).copy(alpha = 0.13f), Color(0xFFFF2A8D).copy(alpha = 0.12f)),
        surface = Color(0xFF101A24), surfaceFocused = Color(0xFF203440),
        border = Color(0xFF00F0FF).copy(alpha = 0.45f), borderWidth = 1.dp,
        focusBorder = accent, focusBorderWidth = 2.5.dp,
        text = Color(0xFFF3F8FA), textDim = Color(0xFFADC5D1), accent = accent,
        panel = Color(0xFF0A111B), corner = 3.dp, zoom = 1.04f,
    )
    Theme.Phosphor -> flat(
        Color(0xFF0D1210), Color(0xFF040806), Color(0xFF111B15), Color(0xFF203025),
        tone(accent, 0.83f, 0.69f), Color(0xFFFFE9BC), Color(0xFFC2BE9E), tone(accent, 0.83f, 0.69f), 3.dp,
        Color(0xFF83C99B).copy(alpha = 0.45f),
    )
    Theme.Orbital -> flat(
        Color(0xFFF4ECD8), Color(0xFFE8DCC3), Color(0xFFFFF8E7), Color(0xFFEBDDCA),
        tone(accent, 0.72f, 0.30f), Color(0xFF142C3F), Color(0xFF4F5A5D), tone(accent, 0.72f, 0.30f), 20.dp,
        Color(0xFF142C3F).copy(alpha = 0.24f),
    ).copy(focusBorderWidth = 3.dp)
    Theme.AfterHours -> flat(
        Color(0xFF160D2B), Color(0xFF090816), Color(0xFF261537), Color(0xFF3D214C),
        tone(accent, 0.85f, 0.76f), Color(0xFFFFF3FC), Color(0xFFD3B7D5), tone(accent, 0.85f, 0.76f), 18.dp,
        Color(0xFFB76CCF).copy(alpha = 0.30f),
    ).copy(blobs = listOf(Color(0xFFFF3E96).copy(alpha = 0.20f), Color(0xFF4870FF).copy(alpha = 0.19f)))
    Theme.DeepSea -> flat(
        Color(0xFF02151C), Color(0xFF01090F), Color(0xFF09252D), Color(0xFF123C45),
        tone(accent, 0.78f, 0.68f), Color(0xFFE0FAF5), Color(0xFFA3C8C9), tone(accent, 0.78f, 0.68f), 24.dp,
        Color(0xFF63DECA).copy(alpha = 0.25f),
    ).copy(blobs = listOf(Color(0xFF00CEB5).copy(alpha = 0.20f), Color(0xFF157AA0).copy(alpha = 0.19f), Color(0xFF42AF84).copy(alpha = 0.13f)))
    Theme.Butter -> flat(
        Color(0xFFFFF6DA), Color(0xFFF1E0B3), Color(0xFFFFFAEC), Color(0xFFF3E4BE),
        tone(accent, 0.80f, 0.27f), Color(0xFF45301D), Color(0xFF6B5640), tone(accent, 0.80f, 0.27f), 26.dp,
        Color(0xFF986A18).copy(alpha = 0.23f),
    ).copy(focusBorderWidth = 3.dp)
    Theme.Canopy -> flat(
        Color(0xFF071D14), Color(0xFF030E0A), Color(0xFF112A1E), Color(0xFF203D2B),
        tone(accent, 0.65f, 0.72f), Color(0xFFE6F4DE), Color(0xFFADCAA7), tone(accent, 0.65f, 0.72f), 24.dp,
        Color(0xFF90B58A).copy(alpha = 0.34f),
    )
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

internal fun type(family: FontFamily, scale: Float, theme: Theme) = Type(
    hero = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = ((if (theme == Theme.Butter) 94 else 88) * scale).sp, letterSpacing = (if (theme == Theme.Phosphor) 0.5 else -2.0).sp),
    title = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = ((if (theme == Theme.Orbital) 24 else 22) * scale).sp, letterSpacing = (if (theme == Theme.Phosphor) 1.0 else 0.0).sp),
    body = TextStyle(fontFamily = family, fontSize = (17 * scale).sp),
    label = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = (14 * scale).sp, letterSpacing = (if (theme == Theme.Phosphor) 0.9 else if (theme == Theme.Orbital) 0.5 else 0.0).sp),
    caption = TextStyle(fontFamily = family, fontSize = (13 * scale).sp),
)
