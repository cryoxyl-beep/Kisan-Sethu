package com.kisansethu.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0C3B18),
    primaryContainer = Color(0xFF234F2E),
    onPrimaryContainer = Color(0xFFA8E0B5),
    secondary = Color(0xFFB8CCBC),
    onSecondary = Color(0xFF233428),
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = Color(0xFF2A312C),
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    tertiary = SuccessColor,
    onTertiary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryLightGreen,
    onPrimaryContainer = PrimaryTextColor,
    secondary = PrimaryLightGreen,
    onSecondary = PrimaryTextColor,
    background = BackgroundColor,
    onBackground = PrimaryTextColor,
    surface = SurfaceColor,
    onSurface = PrimaryTextColor,
    surfaceVariant = BackgroundColor,
    onSurfaceVariant = SecondaryTextColor,
    outline = BorderColor,
    error = ErrorColor,
    onError = Color.White,
    tertiary = SuccessColor,
    onTertiary = Color.White
)

@Composable
fun KisanSethuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
