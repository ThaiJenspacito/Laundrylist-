package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = VibrantBlueLight,
    onPrimary = Slate900,
    secondary = VibrantOrange,
    tertiary = VibrantEmerald,
    background = Slate900,
    surface = Slate800,
    error = VibrantRedLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VibrantBlue,
    onPrimary = SurfaceColor,
    primaryContainer = VibrantBlueLight,
    onPrimaryContainer = Slate900,
    secondary = VibrantOrange,
    onSecondary = SurfaceColor,
    tertiary = VibrantEmerald,
    onTertiary = SurfaceColor,
    background = BackgroundColor,
    onBackground = Slate800,
    surface = SurfaceColor,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = VibrantRed,
    onError = SurfaceColor,
    errorContainer = VibrantRedLight,
    onErrorContainer = VibrantRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable for vibrant theme
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

