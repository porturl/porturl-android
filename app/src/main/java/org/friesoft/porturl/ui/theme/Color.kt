package org.friesoft.porturl.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb

// Helper function to generate container colors for a given theme
private fun generateContainerColors(
    primary: Color,
    secondary: Color,
    tertiary: Color,
    surface: Color
): Map<String, Color> {
    val primaryContainer = primary.copy(alpha = 0.12f).compositeOver(surface)
    val secondaryContainer = secondary.copy(alpha = 0.12f).compositeOver(surface)
    val tertiaryContainer = tertiary.copy(alpha = 0.12f).compositeOver(surface)

    return mapOf(
        "primaryContainer" to primaryContainer,
        "onPrimaryContainer" to if (primaryContainer.isLight()) Color.Black else Color.White,
        "secondaryContainer" to secondaryContainer,
        "onSecondaryContainer" to if (secondaryContainer.isLight()) Color.Black else Color.White,
        "tertiaryContainer" to tertiaryContainer,
        "onTertiaryContainer" to if (tertiaryContainer.isLight()) Color.Black else Color.White
    )
}


// Default (Original) Colors
private val defaultLightPrimary = Color(0xFF6200EE)
private val defaultLightSecondary = Color(0xFF03DAC6)
private val defaultLightTertiary = Color(0xFF3700B3)
private val defaultLightSurface = Color(0xFFFFFFFF)
private val defaultLightContainers = generateContainerColors(defaultLightPrimary, defaultLightSecondary, defaultLightTertiary, defaultLightSurface)

val defaultLightColors = lightColorScheme(
    primary = defaultLightPrimary,
    secondary = defaultLightSecondary,
    tertiary = defaultLightTertiary,
    background = Color(0xFFFFFFFF),
    surface = defaultLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    primaryContainer = defaultLightContainers.getValue("primaryContainer"),
    onPrimaryContainer = defaultLightContainers.getValue("onPrimaryContainer"),
    secondaryContainer = defaultLightContainers.getValue("secondaryContainer"),
    onSecondaryContainer = defaultLightContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = defaultLightContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = defaultLightContainers.getValue("onTertiaryContainer")
)

private val defaultDarkPrimary = Color(0xFFBB86FC)
private val defaultDarkSecondary = Color(0xFF03DAC6)
private val defaultDarkTertiary = Color(0xFF3700B3)
private val defaultDarkSurface = Color(0xFF121212)
private val defaultDarkContainers = generateContainerColors(defaultDarkPrimary, defaultDarkSecondary, defaultDarkTertiary, defaultDarkSurface)

val defaultDarkColors = darkColorScheme(
    primary = defaultDarkPrimary,
    secondary = defaultDarkSecondary,
    tertiary = defaultDarkTertiary,
    background = Color(0xFF121212),
    surface = defaultDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = defaultDarkContainers.getValue("primaryContainer"),
    onPrimaryContainer = defaultDarkContainers.getValue("onPrimaryContainer"),
    secondaryContainer = defaultDarkContainers.getValue("secondaryContainer"),
    onSecondaryContainer = defaultDarkContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = defaultDarkContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = defaultDarkContainers.getValue("onTertiaryContainer")
)

// Forest Colors
private val forestLightPrimary = Color(0xFF2E7D32)
private val forestLightSecondary = Color(0xFF689F38)
private val forestLightTertiary = Color(0xFF388E3C)
private val forestLightSurface = Color(0xFFF1F8E9)
private val forestLightContainers = generateContainerColors(forestLightPrimary, forestLightSecondary, forestLightTertiary, forestLightSurface)

val forestLightColors = lightColorScheme(
    primary = forestLightPrimary,
    secondary = forestLightSecondary,
    tertiary = forestLightTertiary,
    background = Color(0xFFF1F8E9),
    surface = forestLightSurface,
    primaryContainer = forestLightContainers.getValue("primaryContainer"),
    onPrimaryContainer = forestLightContainers.getValue("onPrimaryContainer"),
    secondaryContainer = forestLightContainers.getValue("secondaryContainer"),
    onSecondaryContainer = forestLightContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = forestLightContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = forestLightContainers.getValue("onTertiaryContainer")
)

private val forestDarkPrimary = Color(0xFF66BB6A)
private val forestDarkSecondary = Color(0xFF9CCC65)
private val forestDarkTertiary = Color(0xFF81C784)
private val forestDarkSurface = Color(0xFF1B261B)
private val forestDarkContainers = generateContainerColors(forestDarkPrimary, forestDarkSecondary, forestDarkTertiary, forestDarkSurface)

val forestDarkColors = darkColorScheme(
    primary = forestDarkPrimary,
    secondary = forestDarkSecondary,
    tertiary = forestDarkTertiary,
    background = Color(0xFF1B261B),
    surface = forestDarkSurface,
    primaryContainer = forestDarkContainers.getValue("primaryContainer"),
    onPrimaryContainer = forestDarkContainers.getValue("onPrimaryContainer"),
    secondaryContainer = forestDarkContainers.getValue("secondaryContainer"),
    onSecondaryContainer = forestDarkContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = forestDarkContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = forestDarkContainers.getValue("onTertiaryContainer")
)

// Ocean Colors
private val oceanLightPrimary = Color(0xFF0277BD)
private val oceanLightSecondary = Color(0xFF0091EA)
private val oceanLightTertiary = Color(0xFF03A9F4)
private val oceanLightSurface = Color(0xFFE1F5FE)
private val oceanLightContainers = generateContainerColors(oceanLightPrimary, oceanLightSecondary, oceanLightTertiary, oceanLightSurface)

val oceanLightColors = lightColorScheme(
    primary = oceanLightPrimary,
    secondary = oceanLightSecondary,
    tertiary = oceanLightTertiary,
    background = Color(0xFFE1F5FE),
    surface = oceanLightSurface,
    primaryContainer = oceanLightContainers.getValue("primaryContainer"),
    onPrimaryContainer = oceanLightContainers.getValue("onPrimaryContainer"),
    secondaryContainer = oceanLightContainers.getValue("secondaryContainer"),
    onSecondaryContainer = oceanLightContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = oceanLightContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = oceanLightContainers.getValue("onTertiaryContainer")
)

private val oceanDarkPrimary = Color(0xFF4FC3F7)
private val oceanDarkSecondary = Color(0xFF29B6F6)
private val oceanDarkTertiary = Color(0xFF81D4FA)
private val oceanDarkSurface = Color(0xFF1A252A)
private val oceanDarkContainers = generateContainerColors(oceanDarkPrimary, oceanDarkSecondary, oceanDarkTertiary, oceanDarkSurface)

val oceanDarkColors = darkColorScheme(
    primary = oceanDarkPrimary,
    secondary = oceanDarkSecondary,
    tertiary = oceanDarkTertiary,
    background = Color(0xFF1A252A),
    surface = oceanDarkSurface,
    primaryContainer = oceanDarkContainers.getValue("primaryContainer"),
    onPrimaryContainer = oceanDarkContainers.getValue("onPrimaryContainer"),
    secondaryContainer = oceanDarkContainers.getValue("secondaryContainer"),
    onSecondaryContainer = oceanDarkContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = oceanDarkContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = oceanDarkContainers.getValue("onTertiaryContainer")
)

// Sunset Colors
private val sunsetLightPrimary = Color(0xFFF57C00)
private val sunsetLightSecondary = Color(0xFFFF9800)
private val sunsetLightTertiary = Color(0xFFFFA726)
private val sunsetLightSurface = Color(0xFFFFF3E0)
private val sunsetLightContainers = generateContainerColors(sunsetLightPrimary, sunsetLightSecondary, sunsetLightTertiary, sunsetLightSurface)

val sunsetLightColors = lightColorScheme(
    primary = sunsetLightPrimary,
    secondary = sunsetLightSecondary,
    tertiary = sunsetLightTertiary,
    background = Color(0xFFFFF3E0),
    surface = sunsetLightSurface,
    primaryContainer = sunsetLightContainers.getValue("primaryContainer"),
    onPrimaryContainer = sunsetLightContainers.getValue("onPrimaryContainer"),
    secondaryContainer = sunsetLightContainers.getValue("secondaryContainer"),
    onSecondaryContainer = sunsetLightContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = sunsetLightContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = sunsetLightContainers.getValue("onTertiaryContainer")
)

private val sunsetDarkPrimary = Color(0xFFFFB74D)
private val sunsetDarkSecondary = Color(0xFFFB8C00)
private val sunsetDarkTertiary = Color(0xFFFF9800)
private val sunsetDarkSurface = Color(0xFF2A211A)
private val sunsetDarkContainers = generateContainerColors(sunsetDarkPrimary, sunsetDarkSecondary, sunsetDarkTertiary, sunsetDarkSurface)

val sunsetDarkColors = darkColorScheme(
    primary = sunsetDarkPrimary,
    secondary = sunsetDarkSecondary,
    tertiary = sunsetDarkTertiary,
    background = Color(0xFF2A211A),
    surface = sunsetDarkSurface,
    primaryContainer = sunsetDarkContainers.getValue("primaryContainer"),
    onPrimaryContainer = sunsetDarkContainers.getValue("onPrimaryContainer"),
    secondaryContainer = sunsetDarkContainers.getValue("secondaryContainer"),
    onSecondaryContainer = sunsetDarkContainers.getValue("onSecondaryContainer"),
    tertiaryContainer = sunsetDarkContainers.getValue("tertiaryContainer"),
    onTertiaryContainer = sunsetDarkContainers.getValue("onTertiaryContainer")
)

val predefinedThemes = mapOf(
    "Default" to (defaultLightColors to defaultDarkColors),
    "Forest" to (forestLightColors to forestDarkColors),
    "Ocean" to (oceanLightColors to oceanDarkColors),
    "Sunset" to (sunsetLightColors to sunsetDarkColors)
)

/**
 * Generates a background color by reducing the saturation and increasing the brightness of the primary color.
 *
 * @param primary The primary color.
 * @param isDark Whether the theme is dark or not.
 * @return The generated background color.
 */
fun generateBackgroundColor(primary: Color, isDark: Boolean): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(primary.toArgb(), hsv)
    hsv[1] = if (isDark) hsv[1] * 0.4f else hsv[1] * 0.1f // Reduce saturation
    hsv[2] = if (isDark) 0.1f else 0.98f // Adjust brightness for dark/light theme
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * Determines whether a color is light or dark.
 *
 * @return The perceived luminance of the color.
 */
fun Color.isLight(): Boolean {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return luminance > 0.5
}
