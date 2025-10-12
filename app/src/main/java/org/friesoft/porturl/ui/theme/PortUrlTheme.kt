package org.friesoft.porturl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
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
                val background = generateBackgroundColor(primary, darkTheme)
                val surface = generateSurfaceColor(background, darkTheme)

                val onPrimary = if (primary.isLight()) Color.Black else Color.White
                val onSecondary = if (secondary.isLight()) Color.Black else Color.White
                val onTertiary = if (tertiary.isLight()) Color.Black else Color.White
                val onBackground = if (background.isLight()) Color.Black else Color.White
                val onSurface = if (surface.isLight()) Color.Black else Color.White

                // Generate container colors by blending with the surface
                val primaryContainer = primary.copy(alpha = 0.12f).compositeOver(surface)
                val secondaryContainer = secondary.copy(alpha = 0.12f).compositeOver(surface)
                val tertiaryContainer = tertiary.copy(alpha = 0.12f).compositeOver(surface)

                val onPrimaryContainer = if (primaryContainer.isLight()) Color.Black else Color.White
                val onSecondaryContainer = if (secondaryContainer.isLight()) Color.Black else Color.White
                val onTertiaryContainer = if (tertiaryContainer.isLight()) Color.Black else Color.White

                if (darkTheme) {
                    darkColorScheme(
                        primary = primary,
                        secondary = secondary,
                        tertiary = tertiary,
                        background = background,
                        surface = surface,
                        onPrimary = onPrimary,
                        onSecondary = onSecondary,
                        onTertiary = onTertiary,
                        onBackground = onBackground,
                        onSurface = onSurface,
                        primaryContainer = primaryContainer,
                        onPrimaryContainer = onPrimaryContainer,
                        secondaryContainer = secondaryContainer,
                        onSecondaryContainer = onSecondaryContainer,
                        tertiaryContainer = tertiaryContainer,
                        onTertiaryContainer = onTertiaryContainer
                    )
                } else {
                    lightColorScheme(
                        primary = primary,
                        secondary = secondary,
                        tertiary = tertiary,
                        background = background,
                        surface = surface,
                        onPrimary = onPrimary,
                        onSecondary = onSecondary,
                        onTertiary = onTertiary,
                        onBackground = onBackground,
                        onSurface = onSurface,
                        primaryContainer = primaryContainer,
                        onPrimaryContainer = onPrimaryContainer,
                        secondaryContainer = secondaryContainer,
                        onSecondaryContainer = onSecondaryContainer,
                        tertiaryContainer = tertiaryContainer,
                        onTertiaryContainer = onTertiaryContainer
                    )
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
