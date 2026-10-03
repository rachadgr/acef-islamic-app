package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ===========================================================================
//  1. GEMINI PALETTE (افتراضي) — Blue → Violet → Rose
// ===========================================================================
val LightGeminiColorScheme = lightColorScheme(
    primary = GeminiIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7ECFF),
    onPrimaryContainer = Color(0xFF1B2A6B),
    secondary = GeminiViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF341F73),
    tertiary = GeminiRose,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE6EC),
    onTertiaryContainer = Color(0xFF6E0F2E),
    background = Color(0xFFF8F9FE),
    onBackground = TextDarkPrimary,
    surface = IvorySurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = Color(0xFFEEF1FA),
    onSurfaceVariant = TextDarkSecondary,
    outline = Color(0xFFD8DEF0),
    outlineVariant = Color(0xFFE5E9F7)
)

val DarkGeminiColorScheme = darkColorScheme(
    primary = GeminiBlueLight,
    onPrimary = Color(0xFF0A1430),
    primaryContainer = Color(0xFF1E2A55),
    onPrimaryContainer = Color(0xFFD7E1FF),
    secondary = Color(0xFFC9B8FF),
    onSecondary = Color(0xFF241454),
    secondaryContainer = Color(0xFF2E2359),
    onSecondaryContainer = Color(0xFFEDE5FF),
    tertiary = Color(0xFFFFB2C2),
    onTertiary = Color(0xFF521026),
    tertiaryContainer = Color(0xFF3D1024),
    onTertiaryContainer = Color(0xFFFFD9E1),
    background = NightBackground,
    onBackground = TextLightPrimary,
    surface = NightSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = NightCard,
    onSurfaceVariant = TextLightSecondary,
    outline = NightBorder,
    outlineVariant = Color(0xFF222A4C)
)

// Backwards-compatible alias (previous default palette name).
val LightEmeraldColorScheme = LightGeminiColorScheme
val DarkEmeraldColorScheme = DarkGeminiColorScheme

// ===========================================================================
//  2. NAVY PALETTE (الكحلي الملكي)
// ===========================================================================
val LightNavyColorScheme = lightColorScheme(
    primary = RoyalNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = RoyalNavyContainer,
    onPrimaryContainer = RoyalNavyDark,
    secondary = GeminiViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF341F73),
    tertiary = GeminiBlue,
    onTertiary = Color.White,
    background = Color(0xFFF6F9FF),
    onBackground = Color(0xFF0A1730),
    surface = Color.White,
    onSurface = Color(0xFF0A1730),
    surfaceVariant = Color(0xFFECF2FF),
    onSurfaceVariant = Color(0xFF3A5178),
    outline = Color(0xFFD4E1F5),
    outlineVariant = Color(0xFFD4E1F5)
)

val DarkNavyColorScheme = darkColorScheme(
    primary = GeminiBlueLight,
    onPrimary = Color(0xFF04122C),
    primaryContainer = RoyalNavyContainerDark,
    onPrimaryContainer = Color(0xFFD7E1FF),
    secondary = Color(0xFFC9B8FF),
    onSecondary = Color(0xFF1C1046),
    secondaryContainer = Color(0xFF232A52),
    onSecondaryContainer = Color(0xFFEDF4FC),
    tertiary = GeminiBlue,
    onTertiary = Color(0xFF04122C),
    background = Color(0xFF061124),
    onBackground = Color(0xFFEDF4FC),
    surface = Color(0xFF0B1B36),
    onSurface = Color(0xFFEDF4FC),
    surfaceVariant = Color(0xFF132A4C),
    onSurfaceVariant = Color(0xFFADC4DE),
    outline = Color(0xFF1C3C68),
    outlineVariant = Color(0xFF1C3C68)
)

// ===========================================================================
//  3. VIOLET / MAROON PALETTE (البنفسجي الأندلسي)
// ===========================================================================
val LightMaroonColorScheme = lightColorScheme(
    primary = AndalusianMaroonPrimary,
    onPrimary = Color.White,
    primaryContainer = AndalusianMaroonContainer,
    onPrimaryContainer = AndalusianMaroonDark,
    secondary = GeminiViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF341F73),
    tertiary = GeminiRose,
    onTertiary = Color.White,
    background = Color(0xFFFDF7FA),
    onBackground = Color(0xFF2A0A1F),
    surface = Color.White,
    onSurface = Color(0xFF2A0A1F),
    surfaceVariant = Color(0xFFFBEAF4),
    onSurfaceVariant = Color(0xFF6B3A58),
    outline = Color(0xFFEED2E3),
    outlineVariant = Color(0xFFEED2E3)
)

val DarkMaroonColorScheme = darkColorScheme(
    primary = AndalusianMaroonLight,
    onPrimary = Color(0xFF2A0718),
    primaryContainer = AndalusianMaroonContainerDark,
    onPrimaryContainer = Color(0xFFFFD9EC),
    secondary = Color(0xFFC9B8FF),
    onSecondary = Color(0xFF1C1046),
    secondaryContainer = Color(0xFF3A0F2C),
    onSecondaryContainer = Color(0xFFFDECF4),
    tertiary = Color(0xFFFFB2C2),
    onTertiary = Color(0xFF521026),
    background = Color(0xFF170512),
    onBackground = Color(0xFFFDECF4),
    surface = Color(0xFF25091D),
    onSurface = Color(0xFFFDECF4),
    surfaceVariant = Color(0xFF3A1030),
    onSurfaceVariant = Color(0xFFDFABC8),
    outline = Color(0xFF52203F),
    outlineVariant = Color(0xFF52203F)
)

// ===========================================================================
//  4. MIDNIGHT PALETTE (الأسود والرمادي الفاخر)
// ===========================================================================
val LightMidnightColorScheme = lightColorScheme(
    primary = Color(0xFF20222E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E8F0),
    onPrimaryContainer = Color(0xFF101219),
    secondary = GeminiViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF341F73),
    tertiary = GeminiBlue,
    onTertiary = Color.White,
    background = Color(0xFFF9F9FC),
    onBackground = Color(0xFF14151B),
    surface = Color.White,
    onSurface = Color(0xFF14151B),
    surfaceVariant = Color(0xFFF0F1F6),
    onSurfaceVariant = Color(0xFF4E5060),
    outline = Color(0xFFDDDFE8),
    outlineVariant = Color(0xFFDDDFE8)
)

val DarkMidnightColorScheme = darkColorScheme(
    primary = Color(0xFFB9C0E0),
    onPrimary = Color(0xFF101219),
    primaryContainer = Color(0xFF262834),
    onPrimaryContainer = Color(0xFFF2F2F7),
    secondary = Color(0xFFC9B8FF),
    onSecondary = Color(0xFF1C1046),
    secondaryContainer = Color(0xFF2E2E3E),
    onSecondaryContainer = Color(0xFFF2F2F7),
    tertiary = GeminiBlueLight,
    onTertiary = Color(0xFF0A0A12),
    background = Color(0xFF0C0D13),
    onBackground = Color(0xFFEDEDF2),
    surface = Color(0xFF14151D),
    onSurface = Color(0xFFEDEDF2),
    surfaceVariant = Color(0xFF212230),
    onSurfaceVariant = Color(0xFFB5B6C6),
    outline = Color(0xFF343646),
    outlineVariant = Color(0xFF343646)
)

// ===========================================================================
//  5. TURQUOISE PALETTE (الفيروزي العصري)
// ===========================================================================
val LightTurquoiseColorScheme = lightColorScheme(
    primary = TurquoisePrimary,
    onPrimary = Color.White,
    primaryContainer = TurquoiseContainer,
    onPrimaryContainer = TurquoiseDark,
    secondary = GeminiViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E9FF),
    onSecondaryContainer = Color(0xFF341F73),
    tertiary = GeminiBlue,
    onTertiary = Color.White,
    background = Color(0xFFF3FBFC),
    onBackground = Color(0xFF04272D),
    surface = Color.White,
    onSurface = Color(0xFF04272D),
    surfaceVariant = Color(0xFFE2F5F8),
    onSurfaceVariant = Color(0xFF2B565C),
    outline = Color(0xFFC8E6EA),
    outlineVariant = Color(0xFFC8E6EA)
)

val DarkTurquoiseColorScheme = darkColorScheme(
    primary = TurquoiseLight,
    onPrimary = Color(0xFF032025),
    primaryContainer = TurquoiseContainerDark,
    onPrimaryContainer = Color(0xFFD6F4F8),
    secondary = Color(0xFFC9B8FF),
    onSecondary = Color(0xFF1C1046),
    secondaryContainer = Color(0xFF093E45),
    onSecondaryContainer = Color(0xFFE2F9FC),
    tertiary = GeminiBlueLight,
    onTertiary = Color(0xFF032025),
    background = Color(0xFF02181C),
    onBackground = Color(0xFFE2F9FC),
    surface = Color(0xFF062429),
    onSurface = Color(0xFFE2F9FC),
    surfaceVariant = Color(0xFF0B373E),
    onSurfaceVariant = Color(0xFFA1D0D6),
    outline = Color(0xFF15535C),
    outlineVariant = Color(0xFF15535C)
)

fun getAppColorScheme(themeMode: String, colorTheme: String, systemInDark: Boolean): ColorScheme {
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    return when (colorTheme.uppercase()) {
        "NAVY" -> if (isDark) DarkNavyColorScheme else LightNavyColorScheme
        "MAROON" -> if (isDark) DarkMaroonColorScheme else LightMaroonColorScheme
        "MIDNIGHT" -> if (isDark) DarkMidnightColorScheme else LightMidnightColorScheme
        "TURQUOISE" -> if (isDark) DarkTurquoiseColorScheme else LightTurquoiseColorScheme
        else -> if (isDark) DarkGeminiColorScheme else LightGeminiColorScheme
    }
}

/** Returns the Gemini signature gradient adapted to the current theme. */
@Composable
fun geminiGradient(): Brush = GeminiGradient

/** Returns an app background wash adapted to the current theme. */
@Composable
fun geminiBackground(themeMode: String = "SYSTEM"): Brush {
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    return if (isDark) GeminiBackgroundDark else GeminiBackgroundLight
}

@Composable
fun AcefTheme(
    themeMode: String = "SYSTEM",
    colorTheme: String = "EMERALD",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = getAppColorScheme(themeMode, colorTheme, darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
