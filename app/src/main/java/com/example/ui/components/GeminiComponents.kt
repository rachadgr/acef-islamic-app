package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.example.ui.theme.GeminiBackgroundDark
import com.example.ui.theme.GeminiBackgroundLight
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiGradient
import com.example.ui.theme.GeminiRose
import com.example.ui.theme.GeminiViolet

/**
 * Full-screen Gemini-style background: a soft vertical wash with a couple of
 * blurred colored "blobs" that give the signature airy, modern look.
 */
@Composable
fun GeminiGradientBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val wash: Brush = if (isDark) GeminiBackgroundDark else GeminiBackgroundLight
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(wash)
    ) {
        // Decorative blurred blobs
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .blur(70.dp)
                .background(
                    GeminiBlue.copy(alpha = if (isDark) 0.28f else 0.22f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 210.dp, y = 120.dp)
                .blur(80.dp)
                .background(
                    GeminiViolet.copy(alpha = if (isDark) 0.26f else 0.20f),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 40.dp, y = 520.dp)
                .blur(85.dp)
                .background(
                    GeminiRose.copy(alpha = if (isDark) 0.22f else 0.16f),
                    CircleShape
                )
        )
        content()
    }
}

/**
 * Rounded card with a subtle translucent (glassmorphism) surface and a light
 * border — the core building block of the Gemini-inspired UI.
 */
@Composable
fun GeminiGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    isDark: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val container = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    } else {
        Color.White.copy(alpha = 0.80f)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(1.dp, borderColor.copy(alpha = 0.6f), shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

/** A card filled with the signature Gemini gradient. */
@Composable
fun GeminiGradientCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(GeminiGradient)
            .padding(contentPadding)
    ) {
        content()
    }
}

/** Section header with a small gradient dot and a title. */
@Composable
fun GeminiSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(GeminiGradient)
        )
        Spacer(modifier = Modifier.width(10.dp))
        if (icon != null) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** Small pill chip with an optional gradient fill. */
@Composable
fun GeminiChip(
    text: String,
    modifier: Modifier = Modifier,
    gradient: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    val clickModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier
    Box(
        modifier = clickModifier
            .clip(shape)
            .then(
                if (gradient) Modifier.background(GeminiGradient)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = if (gradient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Compact stat pill used on hero cards. */
@Composable
fun GeminiStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
