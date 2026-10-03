package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ===========================================================================
//  ACEF — Gemini-inspired design system
//  Signature gradient: Blue → Violet → Rose, with modern, airy surfaces.
// ===========================================================================

// --- Gemini signature brand colors -----------------------------------------
val GeminiBlue = Color(0xFF4285F4)
val GeminiBlueLight = Color(0xFF8AB4F8)
val GeminiIndigo = Color(0xFF4C6FFF)
val GeminiViolet = Color(0xFF9B72CB)
val GeminiPurple = Color(0xFF7B61FF)
val GeminiRose = Color(0xFFD96570)
val GeminiPink = Color(0xFFE94A8A)
val GeminiTeal = Color(0xFF34A853)
val GeminiAmber = Color(0xFFFBBC05)

// --- Legacy palette aliases (kept for full source compatibility) -----------
// "Emerald*" identifiers are now mapped to the Gemini blue/violet scale.
val IslamicGreenDark = Color(0xFF0B0E1A)
val IslamicGreenPrimary = Color(0xFF4C6FFF)
val IslamicGreenMedium = Color(0xFF7B61FF)
val IslamicGreenLight = Color(0xFF8AB4F8)
val IslamicGreenContainer = Color(0xFFE8EDFF)
val IslamicGreenContainerDark = Color(0xFF1E2A55)

val EmeraldDark = Color(0xFF0B0E1A)
val EmeraldPrimary = IslamicGreenPrimary
val EmeraldMedium = IslamicGreenMedium
val EmeraldLight = IslamicGreenLight

// "Gold*" identifiers now map to the bright Gemini accent.
val GoldAccent = Color(0xFF7C9CFF)
val GoldBright = Color(0xFFA5BEFF)
val IslamicGold = GoldAccent
val IslamicGoldLight = GoldBright
val IslamicGoldDark = Color(0xFF4C6FFF)
val IslamicGoldContainer = Color(0xFFE8EDFF)
val IslamicGoldContainerDark = Color(0xFF1E2A55)

// --- Alternative palettes (accessible from Settings) ------------------------
val RoyalNavyDark = Color(0xFF0A1430)
val RoyalNavyPrimary = Color(0xFF2E5BFF)
val RoyalNavyMedium = Color(0xFF4C7DFF)
val RoyalNavyLight = Color(0xFF8AB4F8)
val RoyalNavyContainer = Color(0xFFE4ECFF)
val RoyalNavyContainerDark = Color(0xFF152350)

val AndalusianMaroonDark = Color(0xFF1A0716)
val AndalusianMaroonPrimary = Color(0xFFB03A78)
val AndalusianMaroonMedium = Color(0xFFD24E93)
val AndalusianMaroonLight = Color(0xFFE98CC0)
val AndalusianMaroonContainer = Color(0xFFFCE6F3)
val AndalusianMaroonContainerDark = Color(0xFF3A0F2C)

val MidnightBlackDark = Color(0xFF0A0A0F)
val MidnightBlackPrimary = Color(0xFF1B1B24)
val MidnightBlackMedium = Color(0xFF2A2A38)
val MidnightBlackLight = Color(0xFF4A4A5E)
val MidnightBlackContainer = Color(0xFFECECF2)
val MidnightBlackContainerDark = Color(0xFF24242E)

val TurquoiseDark = Color(0xFF04212A)
val TurquoisePrimary = Color(0xFF00A3B5)
val TurquoiseMedium = Color(0xFF14BFD1)
val TurquoiseLight = Color(0xFF5FD6E4)
val TurquoiseContainer = Color(0xFFD6F4F8)
val TurquoiseContainerDark = Color(0xFF083A44)

// --- Neutral surfaces ------------------------------------------------------
val IvoryLight = Color(0xFFF8F9FE)
val IvorySurface = Color(0xFFFFFFFF)
val IvoryCard = Color(0xFFEEF1FA)
val IvoryBorder = Color(0xFFD8DEF0)

val NightBackground = Color(0xFF0A0E1A)
val NightSurface = Color(0xFF11162A)
val NightCard = Color(0xFF1B2140)
val NightBorder = Color(0xFF2A3358)

// --- Text colors -----------------------------------------------------------
val TextDarkPrimary = Color(0xFF131629)
val TextDarkSecondary = Color(0xFF444A63)
val TextDarkTertiary = Color(0xFF7A8098)

val TextLightPrimary = Color(0xFFF3F5FF)
val TextLightSecondary = Color(0xFFC3CAE8)
val TextLightTertiary = Color(0xFF8A92B8)

// ===========================================================================
//  Gradients
// ===========================================================================

/** Full-strength Gemini signature gradient (Blue → Violet → Rose). */
val GeminiGradient: Brush = Brush.linearGradient(
    colors = listOf(GeminiBlue, GeminiViolet, GeminiRose)
)

/** Horizontal variant, ideal for top bars and banners. */
val GeminiGradientHorizontal: Brush = Brush.horizontalGradient(
    colors = listOf(GeminiBlue, GeminiViolet, GeminiRose)
)

/** Softer diagonal gradient for hero cards. */
val GeminiGradientSoft: Brush = Brush.linearGradient(
    colors = listOf(
        GeminiIndigo.copy(alpha = 0.95f),
        GeminiPurple.copy(alpha = 0.92f),
        GeminiRose.copy(alpha = 0.88f)
    )
)

/** Blue → Violet only, for subtle accent surfaces. */
val GeminiGradientCool: Brush = Brush.linearGradient(
    colors = listOf(GeminiBlue, GeminiViolet)
)

/** Very light wash used as an app background in light mode. */
val GeminiBackgroundLight: Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF6F8FF),
        Color(0xFFF3F0FF),
        Color(0xFFFDF5F8)
    )
)

/** Deep night wash used as an app background in dark mode. */
val GeminiBackgroundDark: Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF080B16),
        Color(0xFF0C1024),
        Color(0xFF120E22)
    )
)
