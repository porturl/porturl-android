package org.friesoft.porturl.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable

/**
 * Represents the available theme modes for the application.
 */
enum class ThemeMode {
    /**
     * The app's theme (light or dark) is determined by the system's settings.
     */
    SYSTEM,

    /**
     * The app is always in light mode, regardless of the system's settings.
     */
    LIGHT,

    /**
     * The app is always in dark mode, regardless of the system's settings.
     */
    DARK
}

/**
 * Represents the available color sources for the application's theme.
 */
enum class ColorSource {
    /**
     * The app's color scheme is determined by the system's dynamic colors (e.g., Material You).
     * If dynamic colors are not available, a default color scheme is used.
     */
    SYSTEM,

    /**
     * The app uses one of the predefined color schemes.
     */
    PREDEFINED,

    /**
     * The app uses a custom color scheme defined by the user.
     */
    CUSTOM
}

/**
 * Represents a set of custom colors for the application's theme.
 *
 * @property primary The primary color.
 * @property secondary The secondary color.
 * @property tertiary The tertiary color.
 */
@Serializable
data class CustomColors(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
) {
    companion object {
    }
}

/**
 * Represents the user's preferences for the application's theme.
 *
 * @property themeMode The selected theme mode.
 * @property colorSource The selected color source.
 * @property predefinedColorName The name of the selected predefined color scheme.
 * @property customColors The user's custom color scheme.
 */
data class UserPreferences(
    val themeMode: ThemeMode,
    val colorSource: ColorSource,
    val predefinedColorName: String?,
    val customColors: CustomColors?,
    val translucentBackground: Boolean = false,
    val telemetryEnabled: Boolean = true
)
