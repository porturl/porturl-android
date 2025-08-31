package org.friesoft.porturl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The main theme for the PortURL application.
 *
 * This Composable function wraps the entire UI, providing a consistent
 * color scheme, typography, and shape system based on Material Design 3.
 *
 * It's configured with a custom dark color scheme.
 */
@Composable
fun PortUrlTheme(content: @Composable () -> Unit) {
    // Defines the custom color palette for the dark theme.
    val colorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),      // Main brand color, used for primary actions
        secondary = Color(0xFF03DAC6),    // Accent color for secondary elements
        tertiary = Color(0xFF3700B3),      // Another accent color
        background = Color(0xFF121212),   // App's background color
        surface = Color(0xFF121212),      // Color for surfaces like cards, sheets
        onPrimary = Color.Black,          // Text/icon color on top of the primary color
        onSecondary = Color.Black,        // Text/icon color on top of the secondary color
        onBackground = Color.White,       // Text/icon color on top of the background color
        onSurface = Color.White           // Text/icon color on top of surface colors
    )

    MaterialTheme(
        colorScheme = colorScheme,
        // Typography and Shapes can be customized here as well.
        content = content
    )
}

