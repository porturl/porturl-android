package org.friesoft.porturl.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.friesoft.porturl.data.model.ColorSource
import org.friesoft.porturl.data.model.ThemeMode
import org.friesoft.porturl.data.model.UserPreferences

/**
 * The main theme for the PortURL application, updated to be fully dynamic.
 *
 * This Composable function wraps the entire UI, providing a consistent
 * color scheme, typography, and shape system based on Material Design 3.
 *
 * It now dynamically adjusts the theme based on the user's saved preferences.
 *
 * @param userPreferences The user's saved settings for theme and colors.
 * @param content The content to be themed.
 */
@Composable
fun PortUrlTheme(
    userPreferences: UserPreferences,
    content: @Composable () -> Unit
) {
    val darkTheme = when (userPreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (userPreferences.colorSource) {
        ColorSource.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                // Fallback for older Android versions without Material You
                if (darkTheme) defaultDarkColors else defaultLightColors
            }
        }
        ColorSource.PREDEFINED -> {
            val (light, dark) = predefinedThemes[userPreferences.predefinedColorName]
                ?: (defaultLightColors to defaultDarkColors)
            if (darkTheme) dark else light
        }
        ColorSource.CUSTOM -> {
            userPreferences.customColors?.let { custom ->
                val primary = Color(custom.primary)
                val secondary = Color(custom.secondary)
                val tertiary = Color(custom.tertiary)
                if (darkTheme) {
                    darkColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
                } else {
                    lightColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
                }
            } ?: (if (darkTheme) defaultDarkColors else defaultLightColors) // Fallback if custom colors are null
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Typography and Shapes can be customized here as well.
        content = content
    )
}