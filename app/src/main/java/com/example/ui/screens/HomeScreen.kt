package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Ayah
import com.example.data.model.HourlyHadithExplanation
import com.example.data.model.PrayerTime
import com.example.data.model.PrayerType
import com.example.ui.AcefViewModel
import com.example.ui.components.AboutAppDialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.util.AudioPlayerHelper
import com.example.util.HijriCalendarHelper

@Composable
fun HomeScreen(
    viewModel: AcefViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val iqamaCountdownText by viewModel.iqamaCountdownText.collectAsState()
    val isFloatingActive by viewModel.isFloatingAzkarActive.collectAsState()
    val hourlyVerse by viewModel.hourlyVerse.collectAsState()
    val hourlyHadith by viewModel.hourlyHadith.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()

    val hijriDate = remember(appSettings.appLanguage) { HijriCalendarHelper.getHijriDate() }
    val gregorianDate = remember(appSettings.appLanguage) { HijriCalendarHelper.getGregorianDateFormatted(lang = appSettings.appLanguage) }

    val nextPrayer = prayerTimes.find { it.isNext }
    val currentPrayer = prayerTimes.find { it.isCurrent }
    val iqamaPrayer = prayerTimes.find { it.isIqamaNext }
    val sunrisePrayer = prayerTimes.find { it.type == PrayerType.SUNRISE }
    val maghribPrayer = prayerTimes.find { it.type == PrayerType.MAGHRIB }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text("إذن الظهور فوق التطبيقات", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("لعرض فقاعة الأذكار العائمة أثناء استخدامك للهاتف والتطبيقات الأخرى، يلزم تفعيل إذن (الظهور فوق التطبيقات الأخرى) من إعدادات النظام.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = EmeraldDark)
                ) {
                    Text("فتح الإعدادات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Islamic Date & Location Glassmorphic Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.example.ui.theme.GeminiGradient)
                        .padding(20.dp)
                ) {
                    Column {
                        // Date & GPS Location Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = hijriDate.formatted(appSettings.appLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = gregorianDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // City badge with GPS indicator
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.40f)),
                                modifier = Modifier.clickable { onNavigate("prayer_times") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (appSettings.isGpsEnabled) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appSettings.cityName.take(18),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Next Prayer Hero Countdown Section
                        if (nextPrayer != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GoldAccent.copy(alpha = 0.18f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = when (appSettings.appLanguage) {
                                                "en" -> "Next Prayer"
                                                "fr" -> "Prochaine Prière"
                                                else -> "الصلاة القادمة"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GoldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    val adhanTitle = when (appSettings.appLanguage) {
                                        "en" -> "Adhan ${nextPrayer.type.englishName}"
                                        "fr" -> "Adhan ${nextPrayer.type.frenchName}"
                                        else -> "أذان ${nextPrayer.type.arabicName}"
                                    }
                                    Text(
                                        text = adhanTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = viewModel.formatNumber(nextPrayer.timeFormatted),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }

                                // Elegant Countdown Box
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.30f),
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color.White.copy(alpha = 0.45f)),
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = when (appSettings.appLanguage) {
                                                "en" -> "Time Remaining"
                                                "fr" -> "Temps Restant"
                                                else -> "متبقي على الأذان"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = viewModel.formatNumber(countdownText),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Iqama countdown status if pending
                        if (iqamaPrayer != null && iqamaCountdownText != "--:--:--") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTimeFilled,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val iqamaLabel = when (appSettings.appLanguage) {
                                            "en" -> "Iqama ${iqamaPrayer.type.englishName} (${viewModel.formatNumber(iqamaPrayer.iqamaTimeFormatted)})"
                                            "fr" -> "Iqama ${iqamaPrayer.type.frenchName} (${viewModel.formatNumber(iqamaPrayer.iqamaTimeFormatted)})"
                                            else -> "إقامة صلاة ${iqamaPrayer.type.arabicName} (${viewModel.formatNumber(iqamaPrayer.iqamaTimeFormatted)})"
                                        }
                                        Text(
                                            text = iqamaLabel,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Text(
                                        text = viewModel.formatNumber(iqamaCountdownText),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Sunrise & Sunset info line
                        if (sunrisePrayer != null && maghribPrayer != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.28f), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Sunrise",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val sunriseText = when (appSettings.appLanguage) {
                                        "en" -> "Sunrise: "
                                        "fr" -> "Chourouq: "
                                        else -> "الشروق: "
                                    }
                                    Text(
                                        text = "$sunriseText${viewModel.formatNumber(sunrisePrayer.timeFormatted)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.92f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = "Maghrib",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val maghribText = when (appSettings.appLanguage) {
                                        "en" -> "Sunset (Maghrib): "
                                        "fr" -> "Coucher (Maghreb): "
                                        else -> "الغروب (المغرب): "
                                    }
                                    Text(
                                        text = "$maghribText${viewModel.formatNumber(maghribPrayer.timeFormatted)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.92f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Five Daily Prayers Schedule Strip with Golden Glow on Current/Next
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (appSettings.appLanguage) {
                            "en" -> "Today's Prayer Times"
                            "fr" -> "Horaires des Prières"
                            else -> "مواقيت الصلوات الخمس اليوم"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    TextButton(onClick = { onNavigate("prayer_times") }) {
                        Text(
                            text = when (appSettings.appLanguage) {
                                "en" -> "View Details →"
                                "fr" -> "Voir Détails →"
                                else -> "عرض التفاصيل ←"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(prayerTimes) { prayer ->
                        val isHighlighted = prayer.isNext || prayer.isCurrent
                        Card(
                            modifier = Modifier
                                .width(105.dp)
                                .clickable { onNavigate("prayer_times") },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighlighted) {
                                    EmeraldDark
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isHighlighted) 6.dp else 2.dp
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isHighlighted) 2.dp else 0.8.dp,
                                color = if (isHighlighted) GoldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = getPrayerIcon(prayer.type),
                                    contentDescription = prayer.type.arabicName,
                                    tint = if (isHighlighted) GoldBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prayer.type.getDisplayNameByLang(appSettings.appLanguage),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHighlighted) GoldAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.formatNumber(prayer.timeFormatted),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (prayer.isNext) {
                                        GoldAccent.copy(alpha = 0.25f)
                                    } else if (prayer.isCurrent) {
                                        EmeraldPrimary.copy(alpha = 0.4f)
                                    } else if (prayer.isPassed) {
                                        Color.Gray.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    val statusText = when {
                                        prayer.isNext -> when (appSettings.appLanguage) {
                                            "en" -> "Next"
                                            "fr" -> "Prochaine"
                                            else -> "القادمة"
                                        }
                                        prayer.isCurrent -> when (appSettings.appLanguage) {
                                            "en" -> "Current"
                                            "fr" -> "Actuelle"
                                            else -> "الحالية"
                                        }
                                        prayer.isPassed -> when (appSettings.appLanguage) {
                                            "en" -> "Passed"
                                            "fr" -> "Passée"
                                            else -> "مضت"
                                        }
                                        else -> when (appSettings.appLanguage) {
                                            "en" -> "Upcoming"
                                            "fr" -> "À venir"
                                            else -> "لاحقاً"
                                        }
                                    }
                                    Text(
                                        text = statusText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHighlighted) GoldBright else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Actions Grid: القرآن، الأذكار، القبلة، مواقيت الصلاة، الإعدادات
        item {
            Text(
                text = when (appSettings.appLanguage) {
                    "en" -> "Quick Actions & Worship"
                    "fr" -> "Accès Rapide & Dévotion"
                    else -> "الوصول السريع والعبادات"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "القرآن الكريم",
                        subtitle = "حفص وورش وتلاوة",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("quran") }
                    )
                    QuickActionCard(
                        title = "الأذكار وحصن المسلم",
                        subtitle = "الصباح والمساء واليوم",
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("azkar") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "القبلة الشريفة",
                        subtitle = "بوصلة مكة المكرمة",
                        icon = Icons.Default.Explore,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("qibla") }
                    )
                    QuickActionCard(
                        title = "مواقيت الصلاة",
                        subtitle = "الأذان والإقامة والتقويم",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("prayer_times") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "المستشار الذكي",
                        subtitle = "استفسارات إسلامية",
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("ai_scholar") }
                    )
                    QuickActionCard(
                        title = "السبحة الإلكترونية",
                        subtitle = "عداد التسبيح الذكي",
                        icon = Icons.Default.TouchApp,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("tasbih") }
                    )
                    QuickActionCard(
                        title = "الإعدادات",
                        subtitle = "تخصيص الأصوات والموقع",
                        icon = Icons.Default.Settings,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("settings") }
                    )
                }
            }
        }

        // 4. Auto Adhan & Iqama Quick Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تشغيل الأذان والإقامة تلقائياً",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(onClick = { onNavigate("prayer_times") }) {
                            Icon(Icons.Default.Tune, "تخصيص", tint = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Auto Adhan Toggle
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (appSettings.autoPlayAdhan) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (appSettings.autoPlayAdhan) GoldAccent.copy(alpha = 0.6f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleAutoPlayAdhan(!appSettings.autoPlayAdhan) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = if (appSettings.autoPlayAdhan) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Switch(
                                        checked = appSettings.autoPlayAdhan,
                                        onCheckedChange = { viewModel.toggleAutoPlayAdhan(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldDark,
                                            checkedTrackColor = GoldAccent
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الأذان التلقائي",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (appSettings.autoPlayAdhan) "مفعّل عند دخول الوقت" else "متوقف",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (appSettings.autoPlayAdhan) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Auto Iqama Toggle
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (appSettings.autoPlayIqama) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (appSettings.autoPlayIqama) GoldAccent.copy(alpha = 0.6f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleAutoPlayIqama(!appSettings.autoPlayIqama) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (appSettings.autoPlayIqama) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Switch(
                                        checked = appSettings.autoPlayIqama,
                                        onCheckedChange = { viewModel.toggleAutoPlayIqama(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldDark,
                                            checkedTrackColor = GoldAccent
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تنبيه الإقامة",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (appSettings.autoPlayIqama) "بعد ${appSettings.iqamaDelayMinutes} دقيقة" else "متوقف",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (appSettings.autoPlayIqama) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Hourly Verse & Tafsir (تتغير تلقائياً كل ساعة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GoldAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "آية وتفسير كل ساعة",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = hourlyVerse.hourLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                                )
                            }
                        }

                        // Cycle hour buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.prevHourlyVerse() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "الساعة السابقة",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.nextHourlyVerse() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "الساعة القادمة",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سورة ${hourlyVerse.surahName} • الآية (${hourlyVerse.ayahNumber})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                            Text(
                                text = "تتجدد تلقائياً كل ساعة ⏰",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "﴿${hourlyVerse.textArabic}﴾",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = hourlyVerse.tafsir,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isPlayingThisAyah = playbackState is AudioPlayerHelper.PlaybackState.Playing &&
                                (playbackState as AudioPlayerHelper.PlaybackState.Playing).title.contains(hourlyVerse.surahName)

                        OutlinedButton(
                            onClick = {
                                if (isPlayingThisAyah) {
                                    viewModel.audioPlayer.togglePlayPause()
                                } else {
                                    val ayahToPlay = Ayah(
                                        surahNumber = hourlyVerse.surahNumber,
                                        ayahNumber = hourlyVerse.ayahNumber,
                                        textArabic = hourlyVerse.textArabic,
                                        tafsir = hourlyVerse.tafsir,
                                        audioUrl = hourlyVerse.audioUrl
                                    )
                                    viewModel.playAyahAudio(ayahToPlay)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlayingThisAyah) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPlayingThisAyah) "إيقاف" else "استماع للآية",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = {
                                val ayahModel = Ayah(
                                    surahNumber = hourlyVerse.surahNumber,
                                    ayahNumber = hourlyVerse.ayahNumber,
                                    textArabic = hourlyVerse.textArabic,
                                    tafsir = hourlyVerse.tafsir,
                                    audioUrl = hourlyVerse.audioUrl
                                )
                                viewModel.openTafsir(ayahModel, hourlyVerse.surahName)
                                viewModel.requestAiTafsir()
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تدبر مفصل (Gemini AI)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // 6. Hourly Explained Hadith (تحديث تفسير الأحاديث النبوية كل ساعة)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "حديث وشرح نبوي كل ساعة",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = hourlyHadith.hourLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                                )
                            }
                        }

                        // Cycle hadith buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.prevHourlyHadith() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "السابق",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.nextHourlyHadith() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "التالي",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "«${hourlyHadith.title}»",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            )
                            Text(
                                text = hourlyHadith.source,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "«${hourlyHadith.textArabic}»",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanation Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الشرح والتوجيه النبوي:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hourlyHadith.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (hourlyHadith.keyLessons.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💡 الفوائد: ${hourlyHadith.keyLessons.joinToString(" • ")}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. About App Banner - محمد آصف ڨرارة
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark)
                            .border(1.5.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "حول تطبيق آصف",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "v1.0",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "إعداد وتطوير: محمد آصف ڨرارة",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = "مواقيت الصلاة والأذان، القرآن ورش وحفص، ومستشار إسلامي ذكي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "حول التطبيق",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false },
            onShare = {
                viewModel.shareText(
                    text = "تطبيق آصف الإسلامي\nإعداد وتطوير: محمد آصف ڨرارة\nأذان وإقامة تلقائية، القرآن الكريم بروايتي حفص وورش وتلاوة الشيخ فارس عباد والقطامي، أذكار، ومستشار إسلامي ذكي.",
                    title = "مشاركة تطبيق آصف"
                )
            }
        )
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector = when (type) {
    PrayerType.FAJR -> Icons.Default.WbTwilight
    PrayerType.SUNRISE -> Icons.Default.WbSunny
    PrayerType.DHUHR -> Icons.Default.Brightness5
    PrayerType.ASR -> Icons.Default.Brightness6
    PrayerType.MAGHRIB -> Icons.Default.NightsStay
    PrayerType.ISHA -> Icons.Default.Bedtime
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
