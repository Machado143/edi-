package com.exemplo.oficina.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Brand300,
    onPrimary = Brand900,
    primaryContainer = Brand800,
    onPrimaryContainer = Brand100,
    secondary = Amber400,
    onSecondary = Color(0xFF3A2400),
    secondaryContainer = Color(0xFF4A3000),
    onSecondaryContainer = Amber100,
    tertiary = Teal300,
    onTertiary = Color(0xFF00332E),
    tertiaryContainer = Color(0xFF0B4A44),
    onTertiaryContainer = Teal100,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkVariant,
    onSurfaceVariant = DarkOutline,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkVariant,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF4A0000)
)

private val LightColorScheme = lightColorScheme(
    primary = Brand600,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand900,
    secondary = Amber700,
    onSecondary = Color.White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Color(0xFF3A2400),
    tertiary = Teal700,
    onTertiary = Color.White,
    tertiaryContainer = Teal100,
    onTertiaryContainer = Color(0xFF00332E),
    background = NeutralBg,
    onBackground = NeutralText,
    surface = NeutralSurface,
    onSurface = NeutralText,
    surfaceVariant = NeutralVariant,
    onSurfaceVariant = NeutralOutline,
    surfaceContainer = NeutralSurface,
    surfaceContainerHigh = Brand50,
    outline = NeutralOutline,
    outlineVariant = NeutralVariant,
    error = Color(0xFFC62828),
    onError = Color.White
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TrabalhoEdivaldoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desligado por padrão para manter a identidade visual da marca.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Barras superiores são sempre escuras em ambos os temas: ícones da status bar claros.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

/** True quando o esquema de cores ativo é escuro (inclusive quando forçado pelas configurações). */
@Composable
fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
