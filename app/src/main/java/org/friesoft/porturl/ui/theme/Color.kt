package org.friesoft.porturl.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Default (Original) Colors
val defaultLightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

val defaultDarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// Forest Colors
val forestLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF689F38),
    tertiary = Color(0xFF388E3C),
    background = Color(0xFFF1F8E9),
)

val forestDarkColors = darkColorScheme(
    primary = Color(0xFF66BB6A),
    secondary = Color(0xFF9CCC65),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF1B261B),
)

// Ocean Colors
val oceanLightColors = lightColorScheme(
    primary = Color(0xFF0277BD),
    secondary = Color(0xFF0091EA),
    tertiary = Color(0xFF03A9F4),
    background = Color(0xFFE1F5FE),
)

val oceanDarkColors = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    secondary = Color(0xFF29B6F6),
    tertiary = Color(0xFF81D4FA),
    background = Color(0xFF1A252A),
)

// Sunset Colors
val sunsetLightColors = lightColorScheme(
    primary = Color(0xFFF57C00),
    secondary = Color(0xFFFF9800),
    tertiary = Color(0xFFFFA726),
    background = Color(0xFFFFF3E0),
)

val sunsetDarkColors = darkColorScheme(
    primary = Color(0xFFFFB74D),
    secondary = Color(0xFFFB8C00),
    tertiary = Color(0xFFFF9800),
    background = Color(0xFF2A211A),
)

val predefinedThemes = mapOf(
    "Default" to (defaultLightColors to defaultDarkColors),
    "Forest" to (forestLightColors to forestDarkColors),
    "Ocean" to (oceanLightColors to oceanDarkColors),
    "Sunset" to (sunsetLightColors to sunsetDarkColors)
)