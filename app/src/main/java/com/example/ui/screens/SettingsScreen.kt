package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AdhanPlaybackService
import com.example.service.FloatingAzkarService
import com.example.ui.AcefViewModel
import com.example.ui.components.AboutAppDialog
import com.example.ui.theme.*
import com.example.util.AudioPlayerHelper
import com.example.util.GeminiAiTafsirService
import com.example.util.NotificationReceiver
import com.example.util.PrayerCalculator

data class ColorThemeOption(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val accentColor: Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AcefViewModel
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val isFloatingActive by viewModel.isFloatingAzkarActive.collectAsState()
    val isAdhanPlaying by viewModel.audioPlayer.isAdhanPlaying.collectAsState()
    val playingAdhanSound by viewModel.audioPlayer.selectedAdhanSound.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Custom API Key input state
    var customApiKeyInput by remember(appSettings.customGeminiApiKey) {
        mutableStateOf(appSettings.customGeminiApiKey)
    }

    // Ping test state
    var isPinging by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val colorThemes = remember {
        listOf(
            ColorThemeOption("EMERALD", "الزمردي النبوي", Color(0xFF0F4C35), Color(0xFFD4AF37), "أخضر إسلامي هادئ مع لمسات ذهبية"),
            ColorThemeOption("NAVY", "الكحلي الملكي", Color(0xFF133E68), Color(0xFFD4AF37), "أزرق كحلي ملكي راقٍ ومهيب"),
            ColorThemeOption("MAROON", "العنابي الأندلسي", Color(0xFF6B1B2A), Color(0xFFE5A93C), "عنابي أندلسي فاخر مع تدرج دافئ"),
            ColorThemeOption("MIDNIGHT", "الأسود والذهبي", Color(0xFF1E1E1E), Color(0xFFD4AF37), "مظهر ليلي ملوكي فاحم مع ذهب خالص"),
            ColorThemeOption("TURQUOISE", "الفيروزي السكني", Color(0xFF006D77), Color(0xFFB56549), "فيروزي مريح للبصر مستوحى من قباب المساجد")
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("إذن الظهور فوق التطبيقات", fontWeight = FontWeight.Bold) },
            text = { Text("لتشغيل فقاعة الأذكار العائمة فوق الشاشات والتطبيقات الأخرى، يلزم تفعيل إذن (الظهور فوق التطبيقات) في إعدادات النظام.") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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

    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager }
    val canScheduleExact = remember(appSettings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.let { NotificationReceiver.canScheduleExact(it) } ?: true
        } else {
            true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // Exact Alarm Permission Warning (Android 12+)
        // ==========================================
        if (!canScheduleExact) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "إذن المنبهات الدقيقة غير مفعل",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لضمان تشغيل الأذان في وقته الدقيق بدون تأخير في وضع السكون (Doze Mode)، يتطلب نظام أندرويد 12+ تفعيل إذن المنبهات الدقيقة.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.AlarmOn, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("منح إذن المنبه الدقيق الآن")
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 0: لغة التطبيق والواجهة (Language)
        // ==========================================
        item {
            SettingsSectionHeader(
                title = androidx.compose.ui.res.stringResource(com.example.R.string.settings_section_language),
                icon = Icons.Default.Language
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_language_label) + ":",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val languages = listOf(
                        Triple("SYSTEM", androidx.compose.ui.res.stringResource(com.example.R.string.settings_lang_system), "🌐 System"),
                        Triple("ar", "العربية (Arabic)", "🇸🇦 العربية"),
                        Triple("en", "English", "🇬🇧 English"),
                        Triple("fr", "Français", "🇫🇷 Français")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.take(2).forEach { (code, title, _) ->
                            val isSelected = appSettings.appLanguage.equals(code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setAppLanguage(code) },
                                label = { Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.drop(2).forEach { (code, title, _) ->
                            val isSelected = appSettings.appLanguage.equals(code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setAppLanguage(code) },
                                label = { Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section: نظام عرض التوقيت (12h / 24h Time Format)
        // ==========================================
        item {
            SettingsSectionHeader(
                title = androidx.compose.ui.res.stringResource(com.example.R.string.settings_time_format),
                icon = Icons.Default.Schedule
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_time_format_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !appSettings.is24HourFormat,
                            onClick = { viewModel.setTimeFormat24Hour(false) },
                            label = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_time_format_12),
                                    fontSize = 12.sp,
                                    fontWeight = if (!appSettings.is24HourFormat) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                if (!appSettings.is24HourFormat) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = appSettings.is24HourFormat,
                            onClick = { viewModel.setTimeFormat24Hour(true) },
                            label = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_time_format_24),
                                    fontSize = 12.sp,
                                    fontWeight = if (appSettings.is24HourFormat) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                if (appSettings.is24HourFormat) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!appSettings.is24HourFormat) {
                                    androidx.compose.ui.res.stringResource(com.example.R.string.format_preview_sample_12)
                                } else {
                                    androidx.compose.ui.res.stringResource(com.example.R.string.format_preview_sample_24)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
        // ==========================================
        // Section 1: Thème للتطبيق (Theme & Appearance)
        // ==========================================
        item {
            SettingsSectionHeader(
                title = androidx.compose.ui.res.stringResource(com.example.R.string.settings_section_appearance),
                icon = Icons.Default.Palette
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_theme_mode) + ":",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = appSettings.themeMode == "SYSTEM",
                            onClick = { viewModel.updateThemeMode("SYSTEM") },
                            label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.settings_theme_system), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.BrightnessAuto, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = appSettings.themeMode == "LIGHT",
                            onClick = { viewModel.updateThemeMode("LIGHT") },
                            label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.settings_theme_light), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.LightMode, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = appSettings.themeMode == "DARK",
                            onClick = { viewModel.updateThemeMode("DARK") },
                            label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.settings_theme_dark), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.DarkMode, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_color_theme) + ":",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    colorThemes.forEach { theme ->
                        val isSelected = appSettings.colorTheme.equals(theme.id, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.updateColorTheme(theme.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color swatches
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                        .border(2.dp, theme.accentColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = theme.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 2: نظام الأرقام (French vs Arabic Numerals)
        // ==========================================
        item {
            SettingsSectionHeader(
                title = androidx.compose.ui.res.stringResource(com.example.R.string.settings_number_format),
                icon = Icons.Default.Pin
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر صيغة ظهور الأرقام عبر سائر شاشات التطبيق:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // French / Western Numerals (1, 2, 3...)
                        val isFrench = appSettings.numberFormat != "ARABIC"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateNumberFormat("FRENCH") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFrench) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isFrench) 1.5.dp else 0.5.dp,
                                if (isFrench) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الأرقام الفرنسية / الغربية", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("1 2 3 4 5 6 7 8 9", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (isFrench) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Eastern Arabic Numerals (١، ٢، ٣...)
                        val isArabic = appSettings.numberFormat == "ARABIC"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateNumberFormat("ARABIC") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isArabic) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isArabic) 1.5.dp else 0.5.dp,
                                if (isArabic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الأرقام العربية المشرقية", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("١ ٢ ٣ ٤ ٥ ٦ ٧ ٨ ٩", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (isArabic) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live preview box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("معاينة حية لشكل الأرقام:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            val isArabicActive = appSettings.numberFormat == "ARABIC"
                            val timeSample = if (isArabicActive) "٠٤:٣٠ م" else "04:30 PM"
                            val tasbihSample = if (isArabicActive) "٣٣ / ٣٣ تسبيحة" else "33 / 33 تسبيحة"
                            val ayahSample = if (isArabicActive) "الآية رقم: ٢٥٥ (سورة البقرة: ٢)" else "الآية رقم: 255 (سورة البقرة: 2)"
                            Text("• وقت الصلاة: $timeSample", fontSize = 12.sp)
                            Text("• السبحة: $tasbihSample", fontSize = 12.sp)
                            Text("• القرآن: $ayahSample", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 3: الذكاء الاصطناعي والربط بالإنترنت (Google Gemini AI)
        // ==========================================
        item {
            SettingsSectionHeader(title = "الذكاء الاصطناعي والربط بالإنترنت", icon = Icons.Default.AutoAwesome)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isKeyConfigured = GeminiAiTafsirService.isGeminiConfigured()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isKeyConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isKeyConfigured) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                null,
                                tint = if (isKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محرك Google Gemini السحابي المباشر",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isKeyConfigured) "المحرك نشط ومتصل بالإنترنت (Google Gemini 3.8 / 3.6 Flash)" else "المفتاح غير مضبوط",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ping Test Button
                    Button(
                        onClick = {
                            isPinging = true
                            pingResult = null
                            viewModel.testGeminiConnectivity { success, msg ->
                                isPinging = false
                                pingResult = Pair(success, msg)
                            }
                        },
                        enabled = !isPinging,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ فحص الاتصال بالإنترنت...")
                        } else {
                            Icon(Icons.Default.Speed, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اختبار اتصال الذكاء الاصطناعي المباشر (Ping)")
                        }
                    }

                    pingResult?.let { (success, msg) ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (success) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    null,
                                    tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = if (success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Custom API Key Entry
                    Text(
                        text = "مفتاح Google Gemini مخصص (اختياري):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customApiKeyInput,
                        onValueChange = { customApiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ألصق مفتاحك AIzaSy... (اختياري)", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updateCustomGeminiApiKey(customApiKeyInput) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ وتفعيل المفتاح", fontSize = 12.sp)
                        }

                        if (customApiKeyInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    customApiKeyInput = ""
                                    viewModel.updateCustomGeminiApiKey("")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("مسح", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 4: الأذان والإقامة التلقائية
        // ==========================================
        item {
            SettingsSectionHeader(title = "الأذان والإقامة وتخصيص الأصوات", icon = Icons.Default.VolumeUp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auto Adhan
                    SettingsToggleRow(
                        title = "تشغيل الأذان تلقائياً عند دخول الوقت",
                        subtitle = "يبدأ صوت الأذان الشريف فور دخول وقت الصلاة",
                        checked = appSettings.autoPlayAdhan,
                        onCheckedChange = { viewModel.toggleAutoPlayAdhan() }
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Auto Iqama & Iqama Sound Mode Controls
                    Text(
                        text = "خيار صوت وتنبيه الإقامة في الهاتف:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تحكم كامل في كيفية تنبيه الهاتف عند انتهاء وقت الإقامة وحلول الصلاة:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 3-Mode selector:
                    // 1. None (Muted)
                    // 2. Takbeerat only (No Adhan) - Default recommended
                    // 3. Full Adhan
                    val currentIqamaMode = when {
                        !appSettings.autoPlayIqama -> "NONE"
                        appSettings.playAdhanOnIqama -> "ADHAN"
                        else -> "TAKBEER"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Option 1: Takbeerat only (Default / Recommended - No Adhan)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setIqamaSoundMode("TAKBEER") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentIqamaMode == "TAKBEER") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (currentIqamaMode == "TAKBEER") 1.5.dp else 0.5.dp,
                                if (currentIqamaMode == "TAKBEER") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentIqamaMode == "TAKBEER",
                                    onClick = { viewModel.setIqamaSoundMode("TAKBEER") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "تكبيرات الإقامة فقط (عدم تشغيل أذان الإقامة)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "صوت إقامة الصلاة المعتاد (الله أكبر، قد قامت الصلاة) بدون أذان",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // Option 2: Completely Muted / Notification Only
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setIqamaSoundMode("NONE") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentIqamaMode == "NONE") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (currentIqamaMode == "NONE") 1.5.dp else 0.5.dp,
                                if (currentIqamaMode == "NONE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentIqamaMode == "NONE",
                                    onClick = { viewModel.setIqamaSoundMode("NONE") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "صامت عند الإقامة (إشعار فقط بدون أي صوت)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "عدم تشغيل أي صوت عند الإقامة (مريح للمسجد والمكتب)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // Option 3: Full Adhan at Iqama
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setIqamaSoundMode("ADHAN") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentIqamaMode == "ADHAN") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (currentIqamaMode == "ADHAN") 1.5.dp else 0.5.dp,
                                if (currentIqamaMode == "ADHAN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentIqamaMode == "ADHAN",
                                    onClick = { viewModel.setIqamaSoundMode("ADHAN") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "تشغيل صوت الأذان كاملاً عند وقت الإقامة",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "يصدح الأذان الشريف كاملاً بالهاتف عند بلوغ وقت الإقامة",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Iqama Offset Selection
                    Text(
                        text = "فارق وقت الإقامة عن الأذان:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val offsets = listOf(10, 15, 20, 25, 30)
                        offsets.forEach { minutes ->
                            val isSelected = appSettings.iqamaDelayMinutes == minutes
                            val label = viewModel.formatNumber(minutes) + " د"
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setIqamaDelayMinutes(minutes) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Adhan Sound Customization Section
                    val selectedAdhan = AudioPlayerHelper.availableAdhanSounds.find { it.id == appSettings.selectedAdhanId }
                        ?: AudioPlayerHelper.availableAdhanSounds.first()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تعديل صوت الأذان المعتمد:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "المعتمد حالياً: ${selectedAdhan.nameArabic}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AudioPlayerHelper.availableAdhanSounds.forEach { adhan ->
                            val isSelected = adhan.id == selectedAdhan.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectAdhanSound(adhan) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(adhan.nameArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        Text(adhan.muadhinOrPlace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    val isCurrentPlaying = isAdhanPlaying && playingAdhanSound.id == adhan.id
                                    IconButton(
                                        onClick = {
                                            if (isCurrentPlaying) {
                                                viewModel.audioPlayer.stopAudio()
                                            } else {
                                                viewModel.audioPlayer.playAdhan(adhan)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                            contentDescription = if (isCurrentPlaying) "إيقاف" else "استماع",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Iqama Sound Customization Section
                    val selectedIqama = AudioPlayerHelper.availableIqamaSounds.find { it.id == appSettings.selectedIqamaId }
                        ?: AudioPlayerHelper.availableIqamaSounds.first()

                    Text(
                        text = "تعديل صوت تكبيرات الإقامة المعتمد:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AudioPlayerHelper.availableIqamaSounds.forEach { iqama ->
                            val isSelected = iqama.id == selectedIqama.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectIqamaSound(iqama) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(iqama.nameArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        Text(iqama.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { viewModel.audioPlayer.playIqama(iqama) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayCircle, "استماع", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Direct Test Buttons
                    Text(
                        text = "تجربة تشغيل الصوت في الهاتف فوراً:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testAdhanOnPhone(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تجربة الأذان", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.testIqamaOnPhone(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appSettings.playAdhanOnIqama) "تجربة أذان الإقامة" else "تجربة الإقامة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.audioPlayer.stopAudio()
                                AdhanPlaybackService.stop(context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إيقاف الصوت", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.rescheduleAllAlarmsManually()
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AlarmOn, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تثبيت منبهات الأذان", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 4.5: قارئ القرآن الكريم المعتمد
        // ==========================================
        item {
            SettingsSectionHeader(title = "قارئ القرآن الكريم المعتمد", icon = Icons.Default.RecordVoiceOver)

            val currentReciter by viewModel.audioPlayer.selectedReciter.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر قارئ القرآن الكريم للتلاوة والتتبع الآلي:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "القارئ الحالي: ${currentReciter.nameArabic} • ${currentReciter.style}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AudioPlayerHelper.availableReciters.forEach { reciter ->
                            val isSelected = reciter.id == currentReciter.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectQuranReciter(reciter) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 0.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(reciter.nameArabic, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (reciter.isOfflineAvailable) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = EmeraldPrimary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "أوفلاين بدون نت",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        Text(reciter.style, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.selectQuranReciter(reciter)
                                            viewModel.audioPlayer.playFullSurah(1, "الفاتحة", reciter)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayCircle, "استماع للفاتحة", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 4.5: تحميل سور القرآن الكريم أوفلاين (بدون إنترنت)
        // ==========================================
        item {
            SettingsSectionHeader(title = "تحميل سور القرآن الكريم أوفلاين", icon = Icons.Default.CloudDownload)

            val downloadedSurahs by viewModel.downloadedSurahs.collectAsState()
            val isBatchDownloading by viewModel.isBatchDownloading.collectAsState()
            val batchProgress by viewModel.batchDownloadProgress.collectAsState()
            val batchStatusText by viewModel.batchStatusText.collectAsState()
            val currentReciter by viewModel.audioPlayer.selectedReciter.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حالة التحميل للقارئ: ${currentReciter.nameArabic}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${downloadedSurahs.size} من 114 سورة محملة ومتاحة أوفلاين",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (downloadedSurahs.size >= 114) {
                            Surface(shape = RoundedCornerShape(8.dp), color = EmeraldDark.copy(alpha = 0.2f)) {
                                Text(
                                    "المصحف كامل ✓",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }
                    }

                    if (isBatchDownloading) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(batchStatusText, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { batchProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelAllSurahsDownload() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("إلغاء التحميل")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.downloadAllSurahsOffline(currentReciter) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = downloadedSurahs.size < 114
                        ) {
                            Icon(Icons.Default.DownloadForOffline, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (downloadedSurahs.size >= 114) "جميع سور القرآن محملة (114 سورة)" else "تحميل المصحف كاملاً (114 سورة) أوفلاين",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.downloadJuzAmmaOffline(currentReciter) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("تحميل جزء عم (37)", fontSize = 11.sp)
                            }
                            if (downloadedSurahs.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.deleteAllSurahsOffline(currentReciter.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("حذف التنزيلات", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 5: فقاعة الأذكار العائمة فوق التطبيقات
        // ==========================================
        item {
            SettingsSectionHeader(title = "فقاعة الأذكار العائمة", icon = Icons.Default.ChatBubbleOutline)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggleRow(
                        title = "تشغيل فقاعة الأذكار العائمة فوق الشاشة",
                        subtitle = "تعرض أذكاراً واستغفاراً دورياً فوق أي تطبيق تستخدمه",
                        checked = isFloatingActive,
                        onCheckedChange = { enabled ->
                            viewModel.toggleFloatingAzkarBubble(
                                enabled = enabled,
                                context = context,
                                onRequestPermission = { showPermissionDialog = true }
                            )
                        }
                    )

                    if (isFloatingActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الفقاعة تعمل بنجاح. يمكنك تحريكها أو الضغط عليها لتغيير الذكر.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 6: مواقيت الصلاة والحساب
        // ==========================================
        item {
            SettingsSectionHeader(title = "طريقة الحساب والمذهب الفقهي", icon = Icons.Default.Calculate)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طريقة حساب مواقيت الصلاة المعتمدة:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val methods = listOf(
                        PrayerCalculator.Method.ALGERIA to "وزارة الشؤون الدينية (الجزائر)",
                        PrayerCalculator.Method.MWL to "رابطة العالم الإسلامي",
                        PrayerCalculator.Method.MAKKAH to "جامعة أم القرى (مكة المكرمة)",
                        PrayerCalculator.Method.EGYPT to "الهيئة المصرية العامة للمساحة"
                    )

                    methods.forEach { (method, label) ->
                        val isSelected = appSettings.calculationMethod == method.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateCalculationMethod(method.name) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateCalculationMethod(method.name) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "مذهب حساب صلاة العصر:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = appSettings.madhhab == "SHAFI",
                            onClick = { viewModel.updateMadhhab("SHAFI") },
                            label = { Text("الجمهور (مالكي / شافعي / حنبلي)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = appSettings.madhhab == "HANAFI",
                            onClick = { viewModel.updateMadhhab("HANAFI") },
                            label = { Text("الحنفي (مثلين)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // Section 7: مركز اختبار التنبيهات المباشر
        // ==========================================
        item {
            SettingsSectionHeader(title = "مركز اختبار التنبيهات المباشر", icon = Icons.Default.NotificationsActive)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اضغط لاختبار إشعارات التطبيق فوراً على جهازك:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.triggerTestNotification("prayer") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إشعار الأذان", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerTestNotification("iqama") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إشعار الإقامة", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.triggerTestNotification("morning") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("أذكار الصباح", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerTestNotification("evening") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("أذكار المساء", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerTestNotification("quran") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("الورد القرآني", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // Section 8: حول التطبيق والمعلومات (محمد آصف ڨرارة)
        // ==========================================
        item {
            SettingsSectionHeader(title = "حول التطبيق والمعلومات", icon = Icons.Default.Info)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mosque, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تطبيق آصف الإسلامي",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "إعداد وتطوير: محمد آصف ڨرارة",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "الإصدار 1.0.0 • اضغط لعرض التفاصيل الكاملة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حول التطبيق", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.shareText(
                                    text = "تطبيق آصف الإسلامي\nإعداد وتطوير: محمد آصف ڨرارة\nأذان تلقائي عند وقت الإقامة، أذكار عائمة، وتفسير بالذكاء الاصطناعي.",
                                    title = "مشاركة تطبيق آصف"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة")
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false },
            onShare = {
                viewModel.shareText(
                    text = "تطبيق آصف الإسلامي\nإعداد وتطوير: محمد آصف ڨرارة\nأذان تلقائي عند وقت الإقامة، أذكار عائمة فوق التطبيقات، وتفسير القرآن وشرح الأحاديث بالذكاء الاصطناعي.",
                    title = "مشاركة تطبيق آصف"
                )
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
