package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright

/**
 * Authentic silk bookmark ribbon (شريط العلامة المرجعية للمصحف الشريف)
 * Hung from the top corner of the Mushaf page, with rich crimson silk and gold braided tassel.
 */
@Composable
fun BookmarkRibbon(
    isBookmarked: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isBookmarked,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        Box(modifier = Modifier.size(width = 32.dp, height = 64.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Ribbon fabric body with swallowtail bottom
                val ribbonPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h - 14f)
                    lineTo(w / 2f, h - 26f)
                    lineTo(0f, h - 14f)
                    close()
                }

                // Luxurious Crimson-Wine Silk Gradient
                val silkBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B1E1E),
                        Color(0xFFA82626),
                        Color(0xFF7A1515)
                    ),
                    startY = 0f,
                    endY = h
                )

                drawPath(ribbonPath, brush = silkBrush)

                // Gold edge pinstripes
                drawLine(
                    color = GoldAccent,
                    start = Offset(2.5f, 0f),
                    end = Offset(2.5f, h - 16f),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(w - 2.5f, 0f),
                    end = Offset(w - 2.5f, h - 16f),
                    strokeWidth = 2.5f
                )

                // Gold center ornamental stitch
                drawLine(
                    color = GoldBright.copy(alpha = 0.8f),
                    start = Offset(w / 2f, 4f),
                    end = Offset(w / 2f, h - 30f),
                    strokeWidth = 1.5f
                )

                // Gold fringe tassel dots at the swallowtail tips
                drawCircle(color = GoldBright, radius = 3.5f, center = Offset(w * 0.15f, h - 12f))
                drawCircle(color = GoldBright, radius = 3.5f, center = Offset(w * 0.85f, h - 12f))
                drawCircle(color = GoldBright, radius = 4f, center = Offset(w / 2f, h - 24f))
            }
        }
    }
}
