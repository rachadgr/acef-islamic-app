package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.QuranRiwayah
import com.example.data.model.Surah
import com.example.ui.AcefViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.util.AudioPlayerHelper
import com.example.util.Reciter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: AcefViewModel,
    onOpenSurah: (Surah) -> Unit
) {
    val searchQuery by viewModel.quranSearchQuery.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val selectedReciter by viewModel.audioPlayer.selectedReciter.collectAsState()
    val isFullSurahPlaying by viewModel.audioPlayer.isFullSurahPlaying.collectAsState()
    val currentPlayingSurahNum by viewModel.audioPlayer.currentSurahNumber.collectAsState()
    val currentRiwayah by viewModel.currentRiwayah.collectAsState()

    // Offline download states
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsState()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsState()
    val isBatchDownloading by viewModel.isBatchDownloading.collectAsState()
    val batchDownloadProgress by viewModel.batchDownloadProgress.collectAsState()
    val batchStatusText by viewModel.batchStatusText.collectAsState()
    val batchDownloadSurahNumber by viewModel.batchDownloadSurahNumber.collectAsState()

    var showReciterSheet by remember { mutableStateOf(false) }
    var showOfflineManagerSheet by remember { mutableStateOf(false) }

    val filteredSurahs = remember(searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            IslamicDataProvider.allSurahs
        } else {
            IslamicDataProvider.allSurahs.filter { surah ->
                surah.nameArabic.contains(query) ||
                        surah.englishName.contains(query, ignoreCase = true) ||
                        surah.number.toString() == query
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Reciter selector bar
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setQuranSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setQuranSearchQuery("") }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Riwayah Switcher Row (حفص / ورش)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رواية المصحف الشريف:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuranRiwayah.values().forEach { riwayah ->
                            val isSelected = currentRiwayah == riwayah
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setQuranRiwayah(riwayah) },
                                label = {
                                    Text(
                                        text = riwayah.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent,
                                    selectedLabelColor = EmeraldDark
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reciter Selector Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReciterSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.RecordVoiceOver, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "القارئ: ${selectedReciter.nameArabic} (${selectedReciter.style})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (selectedReciter.id == "frs_a") "✓ صوت الشيخ فارس عباد متاح بدون إنترنت لكافة السور" else "تلاوات بجودة صوتية عالية وتتبع مباشر",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (selectedReciter.id == "frs_a") GoldAccent else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "تغيير",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // FARES ABBAD & OFFLINE SURAH DOWNLOAD BANNER
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOfflineManagerSheet = true },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isBatchDownloading) EmeraldDark else EmeraldPrimary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBatchDownloading) GoldAccent else GoldAccent.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isBatchDownloading) Icons.Default.CloudSync else Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تلاوات الشيخ ${selectedReciter.nameArabic} (أوفلاين)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBatchDownloading) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = if (isBatchDownloading) batchStatusText else "${downloadedSurahs.size} من 114 سورة جاهزة للاستماع بدون نت",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isBatchDownloading) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isBatchDownloading) {
                                TextButton(
                                    onClick = { viewModel.cancelAllSurahsDownload() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("إلغاء", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = { showOfflineManagerSheet = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = GoldAccent.copy(alpha = 0.22f),
                                        contentColor = GoldAccent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحميل أوفلاين", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isBatchDownloading) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { batchDownloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GoldAccent,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(batchDownloadProgress * 100).toInt()}% • جاري الحفظ في ذاكرة الهاتف للاستماع بدون نت",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp
                                )
                            )
                        } else if (downloadedSurahs.size < 114) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.downloadAllSurahsOffline(selectedReciter) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldDark,
                                        contentColor = GoldAccent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.DownloadForOffline, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحميل جميع السور (114)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.downloadJuzAmmaOffline(selectedReciter) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("جزء عم (37)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quran Book & Reciter Follow-Along Banner
        if (searchQuery.isBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val surahToOpen = if (readingProgress != null) {
                                IslamicDataProvider.allSurahs.find { it.number == readingProgress!!.surahNumber }
                            } else {
                                IslamicDataProvider.allSurahs.first()
                            }
                            if (surahToOpen != null) onOpenSurah(surahToOpen)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "كتاب المصحف الشريف والتتبع",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                )
                                Text(
                                    text = "تتبع صوتي مباشر آية بآية مع القارئ ${selectedReciter.nameArabic}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = GoldAccent)
                    }
                }
            }
        }

        // Resume Reading Bookmark Banner (if available)
        if (readingProgress != null && searchQuery.isBlank()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val surah = IslamicDataProvider.allSurahs.find { it.number == readingProgress!!.surahNumber }
                            if (surah != null) onOpenSurah(surah)
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("متابعة القراءة", style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent))
                                Text("سورة ${readingProgress!!.surahName} • الآية ${readingProgress!!.ayahNumber}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Icon(Icons.Default.ArrowBack, null, tint = GoldAccent)
                    }
                }
            }
        }

        items(filteredSurahs, key = { it.number }) { surah ->
            val isPlayingThis = isFullSurahPlaying && currentPlayingSurahNum == surah.number
            val isOffline = surah.number in downloadedSurahs
            val isDownloading = downloadProgressMap.containsKey(surah.number) || (isBatchDownloading && batchDownloadSurahNumber == surah.number)
            val downloadProgress = downloadProgressMap[surah.number]

            SurahItemCard(
                surah = surah,
                isPlaying = isPlayingThis,
                isOffline = isOffline,
                isDownloading = isDownloading,
                downloadProgress = downloadProgress,
                reciterName = selectedReciter.nameArabic,
                onCardClick = { onOpenSurah(surah) },
                onPlayAudio = {
                    if (isPlayingThis) {
                        viewModel.audioPlayer.stopAudio()
                    } else {
                        viewModel.audioPlayer.playFullSurah(surah.number, surah.nameArabic, selectedReciter)
                    }
                },
                onDownloadClick = {
                    viewModel.downloadSurahOffline(surah.number, selectedReciter)
                }
            )
        }
    }

    // ==========================================
    // OFFLINE RECITER SURAH MANAGER SHEET
    // ==========================================
    if (showOfflineManagerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOfflineManagerSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تحميل سور القرآن الكريم أوفلاين",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "القارئ: ${selectedReciter.nameArabic} • ${downloadedSurahs.size} من 114 سورة محملة",
                            style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                        )
                    }
                    IconButton(onClick = { showOfflineManagerSheet = false }) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Batch Download Options
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "حزم التحميل المباشر بدون إنترنت",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 1: Download All 114 Surahs
                        Button(
                            onClick = {
                                viewModel.downloadAllSurahsOffline(selectedReciter)
                                showOfflineManagerSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark, contentColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isBatchDownloading && downloadedSurahs.size < 114
                        ) {
                            Icon(Icons.Default.DownloadForOffline, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (downloadedSurahs.size >= 114) "جميع سور القرآن محملة بالكامل (114)" else "تحميل المصحف كاملاً (114 سورة) أوفلاين",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 2: Download Juz Amma
                        OutlinedButton(
                            onClick = {
                                viewModel.downloadJuzAmmaOffline(selectedReciter)
                                showOfflineManagerSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                            enabled = !isBatchDownloading
                        ) {
                            Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحميل سور جزء عم (النبأ إلى الناس - 37 سورة)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 3: Download Most Listened Surahs
                        OutlinedButton(
                            onClick = {
                                viewModel.downloadPopularSurahsOffline(selectedReciter)
                                showOfflineManagerSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            enabled = !isBatchDownloading
                        ) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحميل السور الأكثر استماعاً (الفاتحة، البقرة، الكهف، يس، الملك...)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Delete All Downloads Option
                if (downloadedSurahs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.deleteAllSurahsOffline(selectedReciter.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف جميع التنزيلات لتفريغ المساحة", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ==========================================
    // RECITER SELECTOR SHEET
    // ==========================================
    if (showReciterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReciterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر قارئ القرآن الكريم المفضل",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "صوت الشيخ فارس عباد مدعوم ومخصص للتشغيل والتحميل أوفلاين بدون نت",
                    style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                )
                Spacer(modifier = Modifier.height(14.dp))

                AudioPlayerHelper.availableReciters.forEach { reciter ->
                    val isSelected = selectedReciter.id == reciter.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectQuranReciter(reciter)
                                showReciterSheet = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GoldAccent else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(reciter.nameArabic, fontWeight = FontWeight.Bold)
                                Text(reciter.style, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = GoldAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahItemCard(
    surah: Surah,
    isPlaying: Boolean,
    isOffline: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float?,
    reciterName: String,
    onCardClick: () -> Unit,
    onPlayAudio: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isPlaying) 1.5.dp else 0.5.dp,
            if (isPlaying) GoldAccent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Surah Number Star / Medallion
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) GoldAccent else MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) EmeraldDark else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سورة ${surah.nameArabic}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isOffline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldDark.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = "أوفلاين",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "أوفلاين",
                                        fontSize = 9.5.sp,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    val isNonArabic = java.util.Locale.getDefault().language != "ar"
                    val subtitleText = if (isNonArabic && surah.nameEnglish.isNotBlank()) {
                        "${surah.nameEnglish} (${surah.nameTranslation}) • ${surah.numberOfAyahs} verses"
                    } else {
                        "${surah.revelationTypeAr} • ${surah.numberOfAyahs} آية"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Download Status / Action Button
                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress ?: 0.1f },
                            modifier = Modifier.size(20.dp),
                            color = GoldAccent,
                            strokeWidth = 2.5.dp
                        )
                    }
                } else if (!isOffline) {
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "تحميل للاستماع أوفلاين",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Play / Pause Button
                IconButton(
                    onClick = onPlayAudio,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = "استماع للسورة كاملة",
                        tint = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
