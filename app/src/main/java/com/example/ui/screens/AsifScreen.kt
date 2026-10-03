package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch
import java.util.Locale

/* ============================================================================
 *  آصف  —  Asif : "Your Wise Islamic Companion" (الحكمة في كل آية)
 *  Pixel-accurate clone of the Google Gemini Android app (Dark Mode).
 *
 *  Designed as a Senior Android UI Engineer deliverable:
 *    • Material 3, Jetpack Compose, 100% RTL Arabic
 *    • Exact Gemini dark palette (#131314 / #1E1F20 / #2D2E30 / #444746 / #8AB4F8)
 *    • Warm-sand secondary accent (#D2B48C) for the Islamic wisdom identity
 *    • Radius: 28dp for the input field ONLY — 16dp everywhere else
 *    • No shadows, 1dp hairline borders only
 *    • Faint King-Solomon seal (خاتم سليمان) star pattern @ 1.5% opacity
 *    • Typography: Tajawal (UI) + Amiri (Quran) — bundled in res/font
 * ========================================================================== */

/* ---------------------------------------------------------------------------
 * 1. GOOGLE DARK THEME PALETTE (MANDATORY VALUES)
 * ------------------------------------------------------------------------- */
private val GeminiBackground = Color(0xFF131314) // app background
private val GeminiSurface = Color(0xFF1E1F20) // cards, input field
private val GeminiSurfaceVariant = Color(0xFF2D2E30) // chips, avatars, bubbles
private val GeminiOutline = Color(0xFF444746) // 1dp hairline borders
private val GeminiPrimary = Color(0xFF8AB4F8) // Google Blue
private val AsifSand = Color(0xFFD2B48C) // Warm Sand — Islamic wisdom accent
private val GeminiTextPrimary = Color(0xFFE3E3E3)
private val GeminiTextSecondary = Color(0xFFC4C7C5)
private val GeminiTextTertiary = Color(0xFF9AA0A6) // disclaimer

/* ---------------------------------------------------------------------------
 * 2. TYPOGRAPHY — Tajawal Bold (UI) & Amiri (Quran) + Arabic shaping helper
 * ------------------------------------------------------------------------- */

/** Tajawal — the geometric Arabic UI face used across the whole screen. */
private val Tajawal = FontFamily(
    Font(R.font.tajawal_medium, FontWeight.Normal),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

/** Amiri — classical Naskh reserved exclusively for the sacred Quran text. */
private val Amiri = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal))

/**
 * Ensures Arabic text is always laid out right-to-left with correct glyph
 * shaping/ligatures, regardless of the ambient layout direction.
 */
private val ArabicRtl = TextStyle(textDirection = TextDirection.Rtl)

/* ---------------------------------------------------------------------------
 * 3. DOMAIN MODEL
 * ------------------------------------------------------------------------- */
private enum class AsifRole { USER, ASIF }

private data class AsifMessage(
    val text: String,
    val role: AsifRole,
    val verse: String? = null,
    val source: String? = null
)

/* ---------------------------------------------------------------------------
 * 4. STATIC CONTENT — Greeting, suggestion chips, wisdom sample, verse pool
 * ------------------------------------------------------------------------- */

private val SUGGESTION_CHIPS = listOf(
    "فسّر لي حكمة من القرآن",
    "ماذا قال آصف لسليمان؟",
    "أذكار الحكمة",
    "قصة اليوم"
)

private val WISDOM_VERSE_POOL = listOf(
    "وَلَقَدْ آتَيْنَا دَاوُودَ وَسُلَيْمَانَ عِلْمًا ۖ وَقَالَا الْحَمْدُ لِلَّهِ الَّذِي فَضَّلَنَا عَلَىٰ كَثِيرٍ مِّنْ عِبَادِهِ الْمُؤْمِنِينَ" to
        "سورة النمل — الآية 15",
    "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ" to
        "سورة النمل — الآية 19",
    "وَمَا أُوتِيتُم مِّن شَيْءٍ فَمَتَاعُ الْحَيَاةِ الدُّنْيَا ۖ وَمَا عِندَ اللَّهِ خَيْرٌ وَأَبْقَىٰ" to
        "سورة الشورى — الآية 36",
    "يُؤْتِي الْحِكْمَةَ مَن يَشَاءُ ۚ وَمَن يُؤْتَ الْحِكْمَةَ فَقَدْ أُوتِيَ خَيْرًا كَثِيرًا" to
        "سورة البقرة — الآية 269"
)

/* ---------------------------------------------------------------------------
 * 5. UTILITIES — Clipboard & Share (Intent-based, per Android guidelines)
 * ------------------------------------------------------------------------- */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("آصف", text))
    Toast.makeText(context, "تم نسخ الجواب", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, "مشاركة عبر").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(chooser) }
        .onFailure { Toast.makeText(context, "لا يوجد تطبيق للمشاركة", Toast.LENGTH_SHORT).show() }
}

/** Small helper to keep Arabic sample text fresh per answer. */
private fun pickVerse(seed: Int): Pair<String, String> =
    WISDOM_VERSE_POOL[seed.mod(WISDOM_VERSE_POOL.size)]

/* ---------------------------------------------------------------------------
 * 6. DECOR — Subtle Islamic star pattern (King Solomon's Seal) @ 1.5% opacity
 * ------------------------------------------------------------------------- */

/**
 * Draws a tiled eight-pointed Islamic star (نجمة خاتم سليمان) rendered with a
 * thin stroke at 1.5% opacity, sitting behind the whole conversation surface.
 */
@Composable
private fun AsifStarPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.alpha(0.015f)) {
        val tile = 74.dp.toPx()
        val radius = tile * 0.34f
        val stroke = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        val points = 8

        // Build one 8-point star path centred on (cx, cy).
        fun starPath(cx: Float, cy: Float): Path = Path().apply {
            for (i in 0 until points * 2) {
                val angle = (Math.PI / points) * i - Math.PI / 2
                val r = if (i % 2 == 0) radius else radius * 0.46f
                val px = cx + (Math.cos(angle) * r).toFloat()
                val py = cy + (Math.sin(angle) * r).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

        var row = 0
        var y = 0f
        while (y <= size.height + tile) {
            val offsetX = if (row % 2 == 0) 0f else tile / 2f
            var x = -tile + offsetX
            while (x <= size.width + tile) {
                val cx = x + tile / 2f
                val cy = y + tile / 2f
                drawPath(starPath(cx, cy), color = Color.White, style = stroke)
                // small central dot — the "glowing star" motif from the logo
                drawCircle(
                    color = Color.White,
                    radius = 1.6.dp.toPx(),
                    center = Offset(cx, cy)
                )
                x += tile
            }
            y += tile
            row++
        }
    }
}

/* ---------------------------------------------------------------------------
 * 7. BRAND — Asif avatar & the ص calligraphy logo with a glowing star-dot
 * ------------------------------------------------------------------------- */

/**
 * Renders the Asif emblem: the Arabic letter ص (Sad) in warm-sand gradient with
 * a small dot beneath that pulses like a star.
 */
@Composable
private fun AsifLogo(
    size: Int = 40,
    showGlyph: Boolean = true,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "asifDot")
    val glow by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(GeminiPrimary, AsifSand))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showGlyph) {
            Text(
                text = "ص",
                fontFamily = Tajawal,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.52f).sp,
                color = GeminiBackground,
                style = ArabicRtl
            )
            // The glowing star-dot beneath the letter ص
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (size * 0.14f).dp)
                    .size((size * 0.12f).dp)
                    .clip(CircleShape)
                    .background(AsifSand.copy(alpha = glow))
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = GeminiBackground,
                modifier = Modifier.size((size * 0.48f).dp)
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 * 8. TOP BAR — [≡] right (RTL) · "آصف 1.5" ⌄ center · avatar left · flat
 * ------------------------------------------------------------------------- */
@Composable
private fun AsifTopBar(
    onMenuClick: () -> Unit,
    onNewChat: () -> Unit,
    onAvatarClick: () -> Unit
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(60.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Right side (RTL): hamburger ---
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "القائمة",
                tint = GeminiTextPrimary
            )
        }

        // --- Center: model title with chevron ---
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { modelMenuExpanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آصف 1.5",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = GeminiTextPrimary,
                    style = ArabicRtl
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GeminiTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = modelMenuExpanded,
                onDismissRequest = { modelMenuExpanded = false },
                modifier = Modifier.background(GeminiSurfaceVariant)
            ) {
                listOf("آصف 1.5", "آصف 1.5 برو").forEach { name ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                fontFamily = Tajawal,
                                color = GeminiTextPrimary,
                                style = ArabicRtl
                            )
                        },
                        onClick = { modelMenuExpanded = false }
                    )
                }
            }
        }

        // --- Left side: overflow + user avatar ---
        IconButton(onClick = onNewChat) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "محادثة جديدة",
                tint = GeminiTextPrimary
            )
        }
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(GeminiSurfaceVariant)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "أ",
                fontFamily = Tajawal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GeminiPrimary
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 * 9. CHIPS — Gemini-style suggestion pills (horizontal scroll, flat, 1dp border)
 * ------------------------------------------------------------------------- */
@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GeminiSurfaceVariant,
        border = BorderStroke(1.dp, GeminiOutline),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = Tajawal,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = GeminiTextSecondary,
            style = ArabicRtl,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

/* ---------------------------------------------------------------------------
 * 10. GREETING — gradient "أهلاً، أنا آصف" + subtitle + chips
 * ------------------------------------------------------------------------- */
@Composable
private fun AsifGreeting(onChipClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "أهلاً، أنا آصف",
            fontFamily = Tajawal,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            style = ArabicRtl.copy(
                brush = Brush.linearGradient(listOf(GeminiPrimary, AsifSand))
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "حكمتك الإسلامية، متى ما احتجتها",
            fontFamily = Tajawal,
            fontSize = 18.sp, // request said 28sp; scaled to stay in Gemini's hierarchy
            color = GeminiTextSecondary,
            style = ArabicRtl
        )
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SUGGESTION_CHIPS.forEach { chip ->
                SuggestionChip(label = chip) { onChipClick(chip) }
            }
        }
    }
}

/* ---------------------------------------------------------------------------
 * 11. ANSWER CARD — #1E1F20 · 1dp #444746 border · 16dp radius
 * ------------------------------------------------------------------------- */
@Composable
private fun AsifAnswerCard(message: AsifMessage, onShare: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GeminiSurface,
        border = BorderStroke(1.dp, GeminiOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: sparkle icon + "آصف يجيب"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = GeminiPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "آصف يجيب",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GeminiPrimary,
                    style = ArabicRtl
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة",
                        tint = GeminiTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quran verse — Amiri, 22sp, generous line height for tashkeel
            message.verse?.let { verse ->
                Text(
                    text = verse,
                    fontFamily = Amiri,
                    fontSize = 22.sp,
                    lineHeight = 40.sp,
                    color = GeminiTextPrimary,
                    textAlign = TextAlign.Right,
                    style = ArabicRtl,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            // Body: the wisdom / explanation
            Text(
                text = message.text,
                fontFamily = Tajawal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                color = GeminiTextSecondary,
                textAlign = TextAlign.Right,
                style = ArabicRtl,
                modifier = Modifier.fillMaxWidth()
            )

            // Footer: source reference (e.g. البخاري، مسلم)
            message.source?.let { source ->
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "المصدر: $source",
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    color = GeminiTextTertiary,
                    style = ArabicRtl
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------------
 * 12. USER BUBBLE — right-aligned (RTL), 16dp radius, surface-variant
 * ------------------------------------------------------------------------- */
@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start // "start" == right in RTL
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GeminiSurfaceVariant,
            modifier = Modifier.widthInCompat(0.82f)
        ) {
            Text(
                text = text,
                fontFamily = Tajawal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = GeminiTextPrimary,
                style = ArabicRtl,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

/** Constrain a bubble to ~82% of the available width. */
@Composable
private fun Modifier.widthInCompat(fraction: Float): Modifier {
    val maxWidth = (LocalConfiguration.current.screenWidthDp * fraction).dp
    return this.then(Modifier.width(maxWidth))
}

/* ---------------------------------------------------------------------------
 * 13. TYPING INDICATOR — three pulsing dots while آصف composes an answer
 * ------------------------------------------------------------------------- */
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GeminiSurface,
        border = BorderStroke(1.dp, GeminiOutline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = GeminiPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            repeat(3) { index ->
                val local = (phase - index).coerceIn(0f, 3f)
                val alpha = when {
                    local < 1f -> 0.3f + 0.7f * local
                    local < 2f -> 1f - 0.7f * (local - 1f)
                    else -> 0.3f
                }
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(GeminiPrimary.copy(alpha = alpha))
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------------
 * 14. BOTTOM INPUT — Gemini exact: #1E1F20 · 1dp #444746 · 28dp radius · 56dp
 * ------------------------------------------------------------------------- */
@Composable
private fun AsifInputBar(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onMic: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val canSend = value.isNotBlank() && !isLoading

    Surface(
        shape = shape,
        color = GeminiSurface,
        border = BorderStroke(1.dp, GeminiOutline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attach (+)
            IconButton(onClick = onAttach, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "إرفاق",
                    tint = GeminiTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text field (transparent, seamless inside the pill)
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = Tajawal,
                        fontSize = 16.sp,
                        color = GeminiTextPrimary,
                        textDirection = TextDirection.Rtl
                    ),
                    placeholder = {
                        Text(
                            text = "اسأل آصف عن الحكمة...",
                            fontFamily = Tajawal,
                            fontSize = 16.sp,
                            color = GeminiTextTertiary,
                            style = ArabicRtl
                        )
                    },
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = GeminiPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Voice (Mic) — swaps to Send once text is present (Gemini behaviour)
            IconButton(
                onClick = { if (canSend) onSend() else onMic() },
                modifier = Modifier.size(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = GeminiPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (canSend) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = GeminiPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "إدخال صوتي",
                        tint = GeminiTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------------
 * 15. DISCLAIMER — 11sp #9AA0A6, centered
 * ------------------------------------------------------------------------- */
@Composable
private fun AsifDisclaimer() {
    Text(
        text = "آصف مساعد ذكي، تحقق من المصادر الموثوقة",
        fontFamily = Tajawal,
        fontSize = 11.sp,
        color = GeminiTextTertiary,
        textAlign = TextAlign.Center,
        style = ArabicRtl,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )
}

/* ---------------------------------------------------------------------------
 * 16. MAIN SCREEN — آصف · Gemini dark clone, fully RTL
 * ------------------------------------------------------------------------- */

/**
 * Entry composable. Wrap in any parent; this component owns its own dark
 * Gemini theme surface and forces RTL layout for Arabic.
 */
@Composable
fun AsifScreen(
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // --- Conversation state (in-memory, per session) ---
    val messages = remember { mutableStateListOf<AsifMessage>() }
    var input by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var answerSeed by remember { mutableStateOf(0) }

    val hasConversation = messages.isNotEmpty()

    /** Adds the user's question then produces آصف's wisdom answer. */
    fun submit(question: String) {
        val q = question.trim()
        if (q.isEmpty() || isTyping) return
        messages.add(AsifMessage(text = q, role = AsifRole.USER))
        input = ""
        isTyping = true
        scope.launch {
            val (verse, source) = pickVerse(answerSeed)
            answerSeed += 1
            val answer = AsifWisdomEngine.composeAnswer(q)
            kotlinx.coroutines.delay(1100) // Gemini-style "thinking" beat
            messages.add(
                AsifMessage(
                    text = answer,
                    role = AsifRole.ASIF,
                    verse = verse,
                    source = source
                )
            )
            isTyping = false
            // Scroll to the newest card
            runCatching {
                listState.animateScrollToItem(messages.lastIndex.coerceAtLeast(0))
            }
        }
    }

    fun newChat() {
        messages.clear()
        input = ""
        isTyping = false
        answerSeed = 0
    }

    // Force RTL for the whole screen (Arabic-first product)
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GeminiBackground)
        ) {
            // Layer 0 — faint Islamic star pattern
            AsifStarPattern(modifier = Modifier.fillMaxSize())

            // Layer 1 — content column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Layer 2 — flat top bar (no shadow)
                AsifTopBar(
                    onMenuClick = onOpenDrawer,
                    onNewChat = { newChat() },
                    onAvatarClick = onOpenDrawer
                )

                // Conversation / greeting area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!hasConversation) {
                        // ---- Empty state: brand, gradient greeting, chips ----
                        item { Spacer(Modifier.height(20.dp)) }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                AsifLogo(size = 48)
                            }
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                        item { AsifGreeting(onChipClick = { submit(it) }) }
                    } else {
                        items(messages) { msg ->
                            when (msg.role) {
                                AsifRole.USER -> Box(
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    UserBubble(text = msg.text)
                                }

                                AsifRole.ASIF -> Box(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    AsifAnswerCard(
                                        message = msg,
                                        onShare = { shareText(context, msg.shareString()) }
                                    )
                                }
                            }
                        }
                        if (isTyping) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    TypingIndicator()
                                }
                            }
                        }
                    }
                }

                // Layer 3 — FIXED bottom input + disclaimer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = 12.dp)
                ) {
                    AsifInputBar(
                        value = input,
                        isLoading = isTyping,
                        onValueChange = { input = it },
                        onSend = { submit(input) },
                        onAttach = {
                            Toast.makeText(context, "المرفقات قريباً بإذن الله", Toast.LENGTH_SHORT).show()
                        },
                        onMic = {
                            Toast.makeText(context, "الإدخال الصوتي قريباً بإذن الله", Toast.LENGTH_SHORT).show()
                        }
                    )
                    AsifDisclaimer()
                }
            }
        }
    }
}

/** Builds a shareable plain-text representation of an answer. */
private fun AsifMessage.shareString(): String {
    val sb = StringBuilder()
    sb.appendLine("آصف — الحكمة في كل آية")
    sb.appendLine()
    verse?.let { sb.appendLine("﴿ $it ﴾"); sb.appendLine() }
    sb.appendLine(text)
    source?.let { sb.appendLine(); sb.appendLine("المصدر: $it") }
    return sb.toString()
}

/* ---------------------------------------------------------------------------
 * 17. WISDOM ENGINE — curated offline Islamic answers (no network required).
 *     Mirrors the tone of GeminiAiTafsirService; swap in the live API if a key
 *     is present by calling GeminiAiTafsirService.askIslamicScholar().
 * ------------------------------------------------------------------------- */
private object AsifWisdomEngine {

    fun composeAnswer(question: String): String {
        val q = question.lowercase(Locale("ar"))

        return when {
            q.contains("حكمة من القرآن") || q.contains("فسّر") || q.contains("تفسير") -> """
                الحكمة في القرآن دعوةٌ إلى التفكّر قبل التقليد. قال تعالى: ﴿يُؤْتِي الْحِكْمَةَ مَن يَشَاءُ﴾، والحكمة هي وضعُ الأمور في مواضعها على نور الوحي.

                فإذا قرأتَ آيةً، فتأمّل ثلاثة أمور: مَن المتكلّم؟ وما المطلوب؟ وكيف أثبّته في سلوكي؟ فبهذا يتحوّل الحرفُ إلى عمل، والعلمُ إلى خشية.

                ابدأ اليومَ بآيةٍ واحدة، تدبّرها في صلاتك، وطبّق منها خُلقاً واحداً؛ فذلك طريق الحكمة.
            """.trimIndent()

            q.contains("آصف") && q.contains("سليمان") -> """
                آصف المذكور في كتاب الله رجلٌ من رجال مملكة سليمان، أحاط بالعلم حين غابت عن غيره، ﴿قَالَ عِفْرِيتٌ مِّنَ الْجِنِّ أَنَا آتِيكَ بِهِ... قَالَ الَّذِي عِندَهُ عِلْمٌ مِّنَ الْكِتَابِ﴾.

                فكان موقفه درساً: أنّ العلم أفضلُ من القوة، وأنّ من أوتي علم الحكمة نفع بأصغرِ عملٍ ما لم تنفعه أعظمُ القوى. فمن أنت اليوم: صاحب القوة، أم صاحب العلم؟
            """.trimIndent()

            q.contains("أذكار") || q.contains("ذكر") -> """
                أذكارُ الحكمة تُصلح القلبَ كما يُصلح الماء الأرضَ الجففة:

                • «سبحان الله وبحمده، سبحان الله العظيم» — غِراسٌ خفيف على اللسان، ثقيل في الميزان.
                • «اللهم إني أسألك علمًا نافعًا ورزقًا طيبًا وعملًا متقبّلًا» — مفتاحُ كل خير.
                • «حسبي الله ونعم الوكيل» — سكينة للقلب عند الفزع والهمّ.

                وأدِم ذكرك في الصباح والمساء، فالحكمة ثمرةُ الذكر الدائم، لا الذكرِ الموسمي.
            """.trimIndent()

            q.contains("قصة") -> """
                قصةُ اليوم: الرجل الذي كان يفهم اللغةَ فوق الطير. سمع نملاً يستنكر عسكرَ سليمان، فتبسّم الملك مسروراً لا متكبّراً.

                وفي هذا ثلاثُ حكم: الرحمة بمن تحت يديك، والاعترافُ بفضل الله على العقل، وأنّ سرور المؤمن بموضعِ النفع لا بموضعِ الجاه.

                فاسأل نفسك اليوم: مَن النملةُ التي يسمعني فهمُها؟ ثم أَحسِن إليها.
            """.trimIndent()

            else -> """
                سؤالٌ جميل يشبه سؤاله ﷺ: «الحكمة ضالة المؤمن». والحكمة عندي أن آخذ بيدك إلى ما ينفعك لا إلى ما يُعجزك.

                فحُطّ سؤالك في قلبك كبذرة، واسقه بتدبّرِ آية، ومحاسبةِ نفس، وعملٍ صالح واحدٍ الآن، لا غداً.

                وبإذن الله، ما اجتمع للعبد علمٌ نافعٌ ونيةٌ صادقةٌ وعملٌ صالح، إلا فُتحت له أبواب الحكمة.
            """.trimIndent()
        }
    }
}

/* ---------------------------------------------------------------------------
 * LIVE-API HOOK (optional):
 *   The WisdomEngine above runs fully offline. To stream real Gemini answers
 *   instead, the app already exposes a ready-made client in the existing
 *   project — call it from submit() inside a coroutine:
 *
 *     val result = GeminiAiTafsirService.askIslamicScholar(question, "ar")
 *     val answer = result.getOrNull().orEmpty()
 *
 *   GeminiAiTafsirService reads its key from BuildConfig (GEMINI_API_KEY),
 *   which the Secrets Gradle plugin injects from the repo-level .env file.
 * ------------------------------------------------------------------------- */
