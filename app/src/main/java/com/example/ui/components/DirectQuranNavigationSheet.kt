package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.QuranJuzInfo
import com.example.data.model.QuranMushafMetadata
import com.example.data.model.Surah
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright

enum class QuranNavTab(val title: String) {
    SURAHS("السور"),
    JUZ("الأجزاء"),
    PAGE("رقم الصفحة")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectQuranNavigationSheet(
    currentSurahNumber: Int,
    currentPageNumber: Int,
    onDismiss: () -> Unit,
    onSelectSurah: (Surah) -> Unit,
    onSelectJuz: (QuranJuzInfo) -> Unit,
    onSelectPage: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(QuranNavTab.PAGE) }
    var surahSearchQuery by remember { mutableStateOf("") }
    var pageInputText by remember { mutableStateOf(currentPageNumber.toString()) }
    var sliderPageValue by remember { mutableStateOf(currentPageNumber.toFloat().coerceIn(1f, 604f)) }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.88f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = GoldAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MenuBook, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الانتقال المباشر في المصحف",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            // Tab Selector: السور | الأجزاء | رقم الصفحة
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                QuranNavTab.values().forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = QuranNavTab.values().size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = EmeraldDark,
                            activeContentColor = GoldBright,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = tab.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                QuranNavTab.PAGE -> {
                    DirectPageJumpContent(
                        pageInputText = pageInputText,
                        onPageInputChange = { pageInputText = it },
                        sliderPageValue = sliderPageValue,
                        onSliderChange = {
                            sliderPageValue = it
                            pageInputText = it.toInt().toString()
                        },
                        onJump = {
                            val page = pageInputText.toIntOrNull()?.coerceIn(1, 604) ?: sliderPageValue.toInt().coerceIn(1, 604)
                            focusManager.clearFocus()
                            onSelectPage(page)
                            onDismiss()
                        },
                        onQuickShortcut = { targetPage ->
                            focusManager.clearFocus()
                            onSelectPage(targetPage)
                            onDismiss()
                        }
                    )
                }

                QuranNavTab.JUZ -> {
                    DirectJuzListContent(
                        currentPageNumber = currentPageNumber,
                        onSelectJuz = { juz ->
                            onSelectJuz(juz)
                            onDismiss()
                        }
                    )
                }

                QuranNavTab.SURAHS -> {
                    DirectSurahListContent(
                        searchQuery = surahSearchQuery,
                        onSearchChange = { surahSearchQuery = it },
                        currentSurahNumber = currentSurahNumber,
                        onSelectSurah = { surah ->
                            onSelectSurah(surah)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectPageJumpContent(
    pageInputText: String,
    onPageInputChange: (String) -> Unit,
    sliderPageValue: Float,
    onSliderChange: (Float) -> Unit,
    onJump: () -> Unit,
    onQuickShortcut: (Int) -> Unit
) {
    val targetPage = pageInputText.toIntOrNull() ?: sliderPageValue.toInt()
    val previewSurah = QuranMushafMetadata.getSurahForPage(targetPage)
    val previewJuz = QuranMushafMetadata.getJuzForPage(targetPage)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page Input Field with Jump Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = pageInputText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 3) {
                        onPageInputChange(input)
                        val num = input.toIntOrNull()
                        if (num != null && num in 1..604) {
                            onSliderChange(num.toFloat())
                        }
                    }
                },
                label = { Text("أدخل رقم الصفحة (1 - 604)", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onJump() }),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = onJump,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("انتقال", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Page Preview Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = EmeraldDark.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "صفحة $targetPage من 604",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سورة ${previewSurah.nameArabic} • ${previewJuz.nameArabic}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Slider from page 1 to 604
        Slider(
            value = sliderPageValue,
            onValueChange = onSliderChange,
            valueRange = 1f..604f,
            steps = 0,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = EmeraldPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("صفحة 1 (الفاتحة)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("صفحة 604 (الناس)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Surah Shortcuts
        Text(
            text = "محطات وسور مختارة:",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val quickPages = listOf(
            Pair("الفاتحة", 1),
            Pair("البقرة", 2),
            Pair("آل عمران", 50),
            Pair("الكهف", 293),
            Pair("يس", 440),
            Pair("الرحمن", 531),
            Pair("الملك", 562),
            Pair("جزء عمّ", 582)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPages.take(4).forEach { (name, page) ->
                OutlinedButton(
                    onClick = { onQuickShortcut(page) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPages.drop(4).forEach { (name, page) ->
                OutlinedButton(
                    onClick = { onQuickShortcut(page) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun DirectJuzListContent(
    currentPageNumber: Int,
    onSelectJuz: (QuranJuzInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(QuranMushafMetadata.all30Ajzaa) { juz ->
            val isCurrent = currentPageNumber >= juz.startPage &&
                    (juz.number == 30 || currentPageNumber < QuranMushafMetadata.all30Ajzaa[juz.number].startPage)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectJuz(juz) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) EmeraldDark.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isCurrent) 1.5.dp else 0.8.dp,
                    color = if (isCurrent) GoldAccent else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrent) GoldAccent else EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${juz.number}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) Color.Black else EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = juz.nameArabic,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "بداية من سورة ${juz.startSurahName} (الآية ${juz.startAyahNumber})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ص ${juz.startPage}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectSurahListContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentSurahNumber: Int,
    onSelectSurah: (Surah) -> Unit
) {
    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            IslamicDataProvider.all114Surahs
        } else {
            IslamicDataProvider.all114Surahs.filter {
                it.nameArabic.contains(searchQuery.trim()) ||
                        it.englishName.contains(searchQuery.trim(), ignoreCase = true) ||
                        it.number.toString() == searchQuery.trim()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldAccent) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredSurahs) { surah ->
                val isSelected = surah.number == currentSurahNumber
                val startPage = QuranMushafMetadata.getPageForSurah(surah.number)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSurah(surah) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) EmeraldDark.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.8.dp,
                        color = if (isSelected) GoldAccent else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${surah.number}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.Black else EmeraldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "سورة ${surah.nameArabic}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (surah.revelationType == "مكية") Color(0xFFD4AF37).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = surah.revelationType,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (surah.revelationType == "مكية") GoldAccent else EmeraldPrimary
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "آياتها ${surah.numberOfAyahs} • ${surah.nameTranslation}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "ص $startPage",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
