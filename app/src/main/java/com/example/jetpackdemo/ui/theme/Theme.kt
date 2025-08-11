package com.example.jetpackdemo.ui.theme

import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun JetPackDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        content = content
    )
}

// Centralized AppColors object for the "Sunset & Slate" theme.
// All other composables should import and use this object.
//object AppColors {
//    // 60% Dominant (Neutral)
//    val background = Color(0xFFFDF5E6) // Soft Beige
//
//    // 30% Secondary (Primary UI)
//    val primary = Color(0xFF4A6572) // Cool Slate
//    val textPrimary = Color(0xFF4A6572) // Cool Slate for text
//
//    // 10% Accent (Highlights)
//    val accent = Color(0xFFF9A825) // Sunset Orange
//
//    // Other colors
//    val surface = Color.White
//    val textSecondary = Color(0xFF758A93) // Lighter version of Cool Slate
//    val onPrimary = Color.White // Text color on primary background
//}

// Bluish Theme
//object AppColors {
//    val primary = Color(0xFF4A90E2)
//    val primaryVariant = Color(0xFF3A77C8)
//    val accent = Color(0xFF50E3C2) // A soft teal for accents
//    val background = Color(0xFFF7F9FC)
//    val surface = Color.White
//    val textPrimary = Color(0xFF4A4A4A)
//    val textSecondary = Color(0xFF7F8C8D)
//    val onPrimary = Color.White
//    val chipBackground = Color(0xFFE8F0FE)
////}

// original
object AppColors {
    // --- Primary & Accent Colors ---
    // A cooler, modern blue for primary actions.
    val primary = Color(0xFF3B82F6) // Cool Blue

    // The accent color is the same as the primary for a cohesive look.
    val accent = Color(0xFF3B82F6)

    // A dedicated color for progress bars and success states.
    val progressGreen = Color(0xFF10B981) // Balanced Green

    // --- Background & Surface Colors ---
    // A very light, off-white background that is easy on the eyes.
    val background = Color(0xFFF8FAFC) // Whitish Background

    // Cards and other elevated surfaces are pure white to create a clean, layered effect.
    val surface = Color.White

    // --- Text & Icon Colors ---
    // A dark, readable charcoal for primary text.
    val textPrimary = Color(0xFF1E293B) // Dark Slate

    // A lighter gray for secondary text, subtitles, and hints.
    val textSecondary = Color(0xFF64748B) // Medium Slate

    // --- Consistent Naming for Readability ---
    val onPrimary = Color.White // Text/icons on top of a primary-colored background.
    val onSurface = Color(0xFF1E293B) // Text/icons on top of a surface-colored background.
    val onBackground = Color(0xFF1E293B) // Text/icons on top of the main background.
}

// Dark Color Theme
//object AppColors {
//    // --- Primary & Accent Colors (10%) ---
//    // A vibrant, pure blue that stands out brilliantly. Represents the "AI spark."
//    // Used for all primary actions, buttons, and important highlights.
//    val primary = Color(0xFF007BFF) // Electric Cobalt
//
//    // In this dark theme, the primary color is also the main accent.
//    // We define it separately for semantic clarity in the code.
//    val accent = Color(0xFF007BFF) // Electric Cobalt
//
//    // --- Background & Surface Colors (60%) ---
//    // A very dark, desaturated blue-gray. Less harsh than pure black.
//    // Feels deep, focused, and premium.
//    val background = Color(0xFF1D232A) // Celestial Blue
//
//    // A slightly lighter Celestial for elevated surfaces like cards.
//    val surface = Color(0xFF2C3A47)
//
//    // --- Text & Icon Colors (30%) ---
//    // High-contrast colors for maximum readability on dark surfaces.
//    val onPrimary = Color.White // Text/icons on top of a primary-colored background (e.g., inside a button).
//    val onSurface = Color.White // Text/icons on top of a surface-colored background (e.g., on a card).
//    val onBackground = Color.White // Text/icons on top of the main background.
//
//    // For semantic clarity, textPrimary is the main color for text.
//    val textPrimary = Color.White
//
//    // A lighter, less prominent gray for subtitles, hints, and secondary information.
//    val textSecondary = Color(0xFFA4B0BE)
//}

// turned out bad one
//object AppColors {
//    // --- Background & Surface Colors (60%) ---
//    // A very light, almost white, with a hint of mint. Clean and easy on the eyes.
//    val background = Color(0xFFF1FAEE) // Honeydew
//    // Cards and other elevated surfaces will be pure white for a clean layered look.
//    val surface = Color.White
//
//    // --- Primary UI Color (30%) ---
//    // A strong, classic blue. Excellent for primary buttons and important text.
//    val primary = Color(0xFF457B9D) // Celadon Blue
//    // A darker shade for text to ensure high readability.
//    val textPrimary = Color(0xFF1D3557) // Prussian Blue
//
//    // --- Accent & Highlight Color (10%) ---
//    // A vibrant, attention-grabbing red. Used for critical actions and highlights.
//    val accent = Color(0xFFE63946) // Imperial Red
//
//    // --- Text & Icon Colors ---
//    val onPrimary = Color.White // Text/icons on top of a primary-colored background.
//    val onSurface = Color(0xFF1D3557) // Text/icons on top of a surface-colored background.
//    val onBackground = Color(0xFF1D3557) // Text/icons on top of the main background.
//
//    // A lighter, secondary blue for less important text or hints.
//    val textSecondary = Color(0xFFA8DADC) // Powder Blue
//}

// Feels like grey with no other colors
//object AppColors {
//    // --- Background & Surface Colors (60%) ---
//    // A very light, clean gray. Easy on the eyes and makes surfaces pop.
//    val background = Color(0xFFF5F5F5)
//    // Cards and other elevated surfaces are pure white for a clean layered look.
//    val surface = Color.White
//
//    // --- Primary UI Color (30%) ---
//    // A strong, dark, desaturated blue. Authoritative and professional.
//    val primary = Color(0xFF284B63) // Indigo Dye
//    // The same color is used for primary text to maintain cohesion.
//    val textPrimary = Color(0xFF284B63)
//
//    // --- Accent & Highlight Color (10%) ---
//    // A unique, muted teal. Used for highlights, links, and selected states.
//    val accent = Color(0xFF3C6E71) // Deep Space Sparkle
//
//    // --- Text & Icon Colors ---
//    val onPrimary = Color.White // Text/icons on top of a primary-colored background.
//    val onSurface = Color(0xFF284B63) // Text/icons on top of a surface-colored background.
//    val onBackground = Color(0xFF284B63) // Text/icons on top of the main background.
//
//    // A softer, dark gray for secondary text, less prominent than the primary.
//    val textSecondary = Color(0xFF353535) // Jet
//}

// ai choose this

//object AppColors {
//    // --- Primary UI Gradient (Used for all major buttons and actions) ---
//    val primaryGradient = Brush.horizontalGradient(
//        colors = listOf(
//            Color(0xFF7B2CBF), // Deep Orchid (Intelligent, Creative)
//            Color(0xFF10B981)  // Vibrant Teal (Modern, Energetic)
//        )
//    )
//
//    // --- Solid Colors for Consistency ---
//    // A solid color for smaller elements where a gradient would be too busy.
//    // We use the teal from the gradient to maintain consistency.
//    val primary = Color(0xFF10B981) // Vibrant Teal
//
//    // The accent color is also the teal, used for links and highlights.
//    val accent = Color(0xFF10B981)
//
//    // --- Background & Surface Colors (Clean & Minimalist) ---
//    // A very light, cool white that provides a clean, modern canvas.
//    val background = Color(0xFFF7F9FC)
//    // Pure white for cards to create a subtle, layered effect.
//    val surface = Color.White
//
//    // --- Text & Icon Colors (High Contrast & Readability) ---
//    // A strong, dark charcoal for primary text. More sophisticated than pure black.
//    val textPrimary = Color(0xFF263238)
//    // A softer gray for secondary text, subtitles, and hints.
//    val textSecondary = Color(0xFF546E7A)
//
//    // Text color that appears on top of the primary gradient.
//    val onPrimary = Color.White
//    // Text color on top of surfaces.
//    val onSurface = Color(0xFF263238)
//    // Text color on top of the main background.
//    val onBackground = Color(0xFF263238)
//}

