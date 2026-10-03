package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.PrayerTime
import com.example.data.model.PrayerType
import com.example.service.AdhanPlaybackService
import com.example.ui.AcefViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.util.AudioPlayerHelper
import com.example.util.PrayerCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: AcefViewModel
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val iqamaCountdownText by viewModel.iqamaCountdownText.collectAsState()
    val selectedAdhanSound by viewModel.audioPlayer.selectedAdhanSound.collectAsState()
    val selectedIqamaSound by viewModel.audioPlayer.selectedIqamaSound.collectAsState()
    val isAdhanPlaying by viewModel.audioPlayer.isAdhanPlaying.collectAsState()
    val isIqamaPlaying by viewModel.audioPlayer.isIqamaPlaying.collectAsState()

    var showCitySelectorSheet by remember { mutableStateOf(false) }
    var showSoundSelectorSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. GPS & Location Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (appSettings.isGpsEnabled) Icons.Default.GpsFixed else Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = appSettings.cityName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (appSettings.isGpsEnabled) "تحديد دقيق عبر الأقمار الصناعية (GPS)" else "مدينة مختارة يدوياً",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { viewModel.refreshGpsLocation() }) {
                                Icon(Icons.Default.MyLocation, "تحديث GPS", tint = GoldAccent)
                            }
                            IconButton(onClick = { showCitySelectorSheet = true }) {
                                Icon(Icons.Default.EditLocationAlt, "تغيير المدينة", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الإحداثيات: ${String.format("%.4f", appSettings.latitude)}° N, ${String.format("%.4f", appSettings.longitude)}° E • النطاق الزمني: GMT+${appSettings.timezone.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // 2. Automatic Adhan & Iqama Settings Card (Focus of User Request)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        IconButton(onClick = { showSoundSelectorSheet = true }) {
                            Icon(Icons.Default.Tune, "تخصيص الأصوات", tint = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch 1: Auto-play Adhan at prayer time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تشغيل الأذان تلقائياً عند دخول الوقت",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "صوت: ${selectedAdhanSound.nameArabic}",
                                style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = appSettings.autoPlayAdhan,
                            onCheckedChange = { viewModel.toggleAutoPlayAdhan() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldDark,
                                checkedTrackColor = GoldAccent
                            )
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Switch 2: Auto-play Iqama when Iqama time arrives
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "التنبيه الصوتي عند وصول وقت الإقامة",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (appSettings.playAdhanOnIqama) {
                                    "يشتغل صوت الأذان في الهاتف عند الإقامة • بعد ${appSettings.iqamaDelayMinutes} د"
                                } else {
                                    "يشتغل صوت تكبيرات الإقامة • بعد ${appSettings.iqamaDelayMinutes} د"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = appSettings.autoPlayIqama,
                            onCheckedChange = { viewModel.toggleAutoPlayIqama() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldDark,
                                checkedTrackColor = GoldAccent
                            )
                        )
                    }

                    if (appSettings.autoPlayIqama) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Iqama Sound Control: Option to NOT play Adhan at Iqama
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.togglePlayAdhanOnIqama() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (appSettings.playAdhanOnIqama) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (appSettings.playAdhanOnIqama) GoldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = if (appSettings.playAdhanOnIqama) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (appSettings.playAdhanOnIqama) "أذان كامل عند الإقامة (مفعّل)" else "عدم تشغيل أذان الإقامة (تكبيرات فقط)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (appSettings.playAdhanOnIqama) "مفعّل: يصدح صوت الأذان كاملاً بالهاتف عند الإقامة" else "معطّل: عدم تشغيل أذان الإقامة — تشغيل تكبيرات الصلاة الهادئة فقط",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (appSettings.playAdhanOnIqama) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Switch(
                                    checked = appSettings.playAdhanOnIqama,
                                    onCheckedChange = { viewModel.togglePlayAdhanOnIqama() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EmeraldDark,
                                        checkedTrackColor = GoldAccent
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Iqama Delay Selector Chips
                    Text(
                        text = "الفارق الزمني بين الأذان والإقامة:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 20, 25).forEach { minutes ->
                            val isSelected = appSettings.iqamaDelayMinutes == minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setIqamaDelayMinutes(minutes) },
                                label = { Text("$minutes دقيقة") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Buttons: Play Adhan & Play Iqama on phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isAdhanPlaying) {
                                    viewModel.audioPlayer.stopAudio()
                                    AdhanPlaybackService.stop(context)
                                } else {
                                    viewModel.testAdhanOnPhone(context)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                        ) {
                            Icon(
                                imageVector = if (isAdhanPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdhanPlaying) "إيقاف الصوت" else "تجربة الأذان بالهاتف",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (isIqamaPlaying) {
                                    viewModel.audioPlayer.stopAudio()
                                    AdhanPlaybackService.stop(context)
                                } else {
                                    viewModel.testIqamaOnPhone(context)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                        ) {
                            Icon(
                                imageVector = if (isIqamaPlaying) Icons.Default.Stop else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isIqamaPlaying) "إيقاف الصوت" else if (appSettings.playAdhanOnIqama) "تجربة أذان الإقامة" else "تجربة الإقامة",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // 3. Prayer Times List Cards & 12H/24H Format Toggle
        item {
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // 12h / 24h Toggle Chips
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!appSettings.is24HourFormat) GoldAccent else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { viewModel.setTimeFormat24Hour(false) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "12H",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!appSettings.is24HourFormat) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (appSettings.is24HourFormat) GoldAccent else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { viewModel.setTimeFormat24Hour(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "24H",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (appSettings.is24HourFormat) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(prayerTimes) { prayer ->
            PrayerScheduleCard(
                prayer = prayer,
                appLanguage = appSettings.appLanguage,
                countdownText = viewModel.formatNumber(countdownText),
                iqamaCountdownText = viewModel.formatNumber(iqamaCountdownText),
                formattedPrayerTime = viewModel.formatNumber(prayer.timeFormatted),
                formattedIqamaTime = viewModel.formatNumber(prayer.iqamaTimeFormatted),
                onPlayAdhan = {
                    viewModel.audioPlayer.playAdhan(selectedAdhanSound)
                },
                onPlayIqama = {
                    viewModel.audioPlayer.playIqama(selectedIqamaSound)
                }
            )
        }
    }

    // Sheet: City Selector
    if (showCitySelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySelectorSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر المدينة أو فعّل GPS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        showCitySelectorSheet = false
                        viewModel.toggleUseGps(true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = EmeraldDark)
                ) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استخدام الموقع الدقيق الحالي (GPS)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "قائمة المدن المتاحة:",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))

                IslamicDataProvider.defaultCities.forEach { city ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectCity(city)
                                showCitySelectorSheet = false
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = city.nameAr, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${city.countryAr} (GMT+${city.timezone.toInt()})",
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                            )
                        }
                    }
                }
            }
        }
    }

    // Sheet: Adhan & Iqama Sounds Customization
    if (showSoundSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSoundSelectorSheet = false
                viewModel.audioPlayer.stopAudio()
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
            ) {
                item {
                    Text(
                        text = "تخصيص أصوات الأذان والإقامة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختر المؤذن الشريف، وتحكم في تشغيل الأذان بالهاتف عند الإقامة",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Adhan at Iqama toggle card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePlayAdhanOnIqama() },
                        shape = RoundedCornerShape(14.dp),
                        color = if (appSettings.playAdhanOnIqama) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (appSettings.playAdhanOnIqama) GoldAccent else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تشغيل الأذان في الهاتف عند وقت الإقامة",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (appSettings.playAdhanOnIqama) {
                                        "مفعّل: يصدح صوت الأذان كاملاً في الهاتف عند حلول وقت الإقامة بعد الأذان بـ ${appSettings.iqamaDelayMinutes} دقيقة"
                                    } else {
                                        "معطّل: يشتغل صوت تكبيرات وإقامة الصلاة الشرعية فقط"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (appSettings.playAdhanOnIqama) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = appSettings.playAdhanOnIqama,
                                onCheckedChange = { viewModel.togglePlayAdhanOnIqama() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldDark,
                                    checkedTrackColor = GoldAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "اختر صوت الأذان الشريف المفضل:",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Available Adhan Sounds List
                items(AudioPlayerHelper.availableAdhanSounds) { adhan ->
                    val isSelected = appSettings.selectedAdhanId == adhan.id || (appSettings.selectedAdhanId.isEmpty() && adhan.id == "makkah")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectAdhanSound(adhan)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 0.5.dp,
                            if (isSelected) GoldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(adhan.nameArabic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldAccent.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "المعتمد للأذان",
                                                color = GoldAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = adhan.muadhinOrPlace,
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = adhan.description,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isAdhanPlaying && selectedAdhanSound.id == adhan.id) {
                                            viewModel.audioPlayer.stopAudio()
                                        } else {
                                            viewModel.audioPlayer.playAdhan(adhan)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isAdhanPlaying && selectedAdhanSound.id == adhan.id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "تجربة الصوت",
                                        tint = GoldAccent
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "اختر صوت تكبيرات الإقامة:",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Available Iqama Sounds List
                items(AudioPlayerHelper.availableIqamaSounds) { iqama ->
                    val isSelected = appSettings.selectedIqamaId == iqama.id || (appSettings.selectedIqamaId.isEmpty() && iqama.id == "standard")
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectIqamaSound(iqama)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 0.5.dp,
                            if (isSelected) GoldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(iqama.nameArabic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldAccent.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "المعتمد للإقامة",
                                                color = GoldAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = iqama.description,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isIqamaPlaying && selectedIqamaSound.id == iqama.id) {
                                        viewModel.audioPlayer.stopAudio()
                                    } else {
                                        viewModel.audioPlayer.playIqama(iqama)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isIqamaPlaying && selectedIqamaSound.id == iqama.id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "تجربة",
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "اختبار التشغيل في الهاتف (محاكاة التنبيه الحقيقي):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.testAdhanOnPhone(context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تجربة الأذان بالهاتف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.testIqamaOnPhone(context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = EmeraldDark)
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appSettings.playAdhanOnIqama) "تجربة أذان الإقامة" else "تجربة الإقامة بالهاتف",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.audioPlayer.stopAudio()
                            AdhanPlaybackService.stop(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إيقاف كافة الأصوات والتنبيهات")
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerScheduleCard(
    prayer: PrayerTime,
    appLanguage: String = "ar",
    countdownText: String,
    iqamaCountdownText: String,
    formattedPrayerTime: String = prayer.timeFormatted,
    formattedIqamaTime: String = prayer.iqamaTimeFormatted,
    onPlayAdhan: () -> Unit,
    onPlayIqama: () -> Unit
) {
    val isHighlighted = prayer.isNext || prayer.isCurrent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isNext) EmeraldPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 3.dp else 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (prayer.isNext) 1.5.dp else 0.5.dp,
            if (prayer.isNext) GoldAccent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (prayer.isNext) GoldAccent else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (prayer.type) {
                                PrayerType.FAJR -> Icons.Default.WbTwilight
                                PrayerType.SUNRISE -> Icons.Default.WbSunny
                                PrayerType.DHUHR -> Icons.Default.LightMode
                                PrayerType.ASR -> Icons.Default.WbCloudy
                                PrayerType.MAGHRIB -> Icons.Default.NightsStay
                                PrayerType.ISHA -> Icons.Default.Bedtime
                            },
                            contentDescription = null,
                            tint = if (prayer.isNext) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = prayer.type.getDisplayNameByLang(appLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (prayer.isNext) {
                            val nextAdhanLabel = when (appLanguage) {
                                "en" -> "Next Adhan in $countdownText"
                                "fr" -> "Prochain Adhan dans $countdownText"
                                else -> "الأذان القادم خلال $countdownText"
                            }
                            Text(
                                text = nextAdhanLabel,
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                            )
                        } else if (prayer.isIqamaNext) {
                            val nextIqamaLabel = when (appLanguage) {
                                "en" -> "Iqama in $iqamaCountdownText"
                                "fr" -> "Iqama dans $iqamaCountdownText"
                                else -> "الإقامة خلال $iqamaCountdownText"
                            }
                            Text(
                                text = nextIqamaLabel,
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedPrayerTime,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (prayer.isNext) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (prayer.type != PrayerType.SUNRISE && formattedIqamaTime.isNotBlank()) {
                        val iqamaPrefix = when (appLanguage) {
                            "en" -> "Iqama: "
                            "fr" -> "Iqama: "
                            else -> "الإقامة: "
                        }
                        Text(
                            text = "$iqamaPrefix$formattedIqamaTime",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
