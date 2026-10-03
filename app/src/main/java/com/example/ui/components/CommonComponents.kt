package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GeminiGradient
import com.example.ui.theme.GeminiGradientHorizontal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.util.AudioPlayerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcefTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GeminiGradientHorizontal)
    ) {
        // subtle dark scrim to keep the gradient text readable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.06f))
        )
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                if (navigationIcon != null && onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = navigationIcon,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )
    }
}

@Composable
fun AcefBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        Triple("home", androidx.compose.ui.res.stringResource(com.example.R.string.nav_home), Icons.Default.Home),
        Triple("prayer_times", androidx.compose.ui.res.stringResource(com.example.R.string.nav_prayer_times), Icons.Default.Schedule),
        Triple("quran", androidx.compose.ui.res.stringResource(com.example.R.string.nav_quran), Icons.Default.MenuBook),
        Triple("azkar", androidx.compose.ui.res.stringResource(com.example.R.string.nav_azkar), Icons.Default.Favorite),
        Triple("ai_scholar", androidx.compose.ui.res.stringResource(com.example.R.string.nav_ai_scholar), Icons.Default.AutoAwesome)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, icon) ->
                val isSelected = currentRoute == route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigate(route) }
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSelected) Modifier.background(GeminiGradient)
                                else Modifier
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlayerMiniBar(
    audioPlayer: AudioPlayerHelper,
    modifier: Modifier = Modifier
) {
    val playbackState by audioPlayer.playbackState.collectAsState()
    val isAdhanPlaying by audioPlayer.isAdhanPlaying.collectAsState()
    val isIqamaPlaying by audioPlayer.isIqamaPlaying.collectAsState()
    val position by audioPlayer.currentPosition.collectAsState()
    val duration by audioPlayer.duration.collectAsState()

    val isVisible = playbackState !is AudioPlayerHelper.PlaybackState.Idle

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val titleText = when (val s = playbackState) {
                        is AudioPlayerHelper.PlaybackState.Playing -> s.title
                        is AudioPlayerHelper.PlaybackState.Paused -> s.title
                        is AudioPlayerHelper.PlaybackState.Buffering -> "جاري التحميل: ${s.title}"
                        is AudioPlayerHelper.PlaybackState.Error -> s.message
                        else -> ""
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAdhanPlaying) "🔊 الأذان الشريف" else if (isIqamaPlaying) "🕌 إقامة الصلاة" else "تلاوة القرآن الكريم",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                        )
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { audioPlayer.rewind10Seconds() }) {
                            Icon(Icons.Default.Replay10, "تقديم 10 ثواني", tint = GoldAccent)
                        }

                        IconButton(
                            onClick = { audioPlayer.togglePlayPause() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        ) {
                            val isPlaying = playbackState is AudioPlayerHelper.PlaybackState.Playing
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = EmeraldDark
                            )
                        }

                        IconButton(onClick = { audioPlayer.forward10Seconds() }) {
                            Icon(Icons.Default.Forward10, "تأخير 10 ثواني", tint = GoldAccent)
                        }

                        IconButton(onClick = { audioPlayer.stopAudio() }) {
                            Icon(Icons.Default.Close, "إغلاق المشغل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (duration > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = GoldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = audioPlayer.formatMillis(position),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                        Text(
                            text = audioPlayer.formatMillis(duration),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }
                }
            }
        }
    }
}
