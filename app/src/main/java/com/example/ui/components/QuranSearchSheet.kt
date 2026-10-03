package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

data class QuranSearchResult(
    val surah: Surah,
    val ayah: Ayah?,
    val matchedSnippet: String,
    val matchType: String // "سورة" or "آية"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSearchSheet(
    onDismiss: () -> Unit,
    onNavigateToResult: (Surah, Int?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val searchResults by remember(searchQuery) {
        derivedStateOf {
            val query = searchQuery.trim()
            if (query.length < 2) return@derivedStateOf emptyList<QuranSearchResult>()

            val results = mutableListOf<QuranSearchResult>()

            // 1. Search Surah names
            IslamicDataProvider.all114Surahs.forEach { surah ->
                if (surah.nameArabic.contains(query) || surah.englishName.contains(query, ignoreCase = true)) {
                    results.add(
                        QuranSearchResult(
                            surah = surah,
                            ayah = null,
                            matchedSnippet = "سورة ${surah.nameArabic} (${surah.nameTranslation}) - آياتها ${surah.numberOfAyahs}",
                            matchType = "سورة"
                        )
                    )
                }
            }

            // 2. Search in preloaded authentic verses
            IslamicDataProvider.surahVerses.forEach { (surahNum, ayahs) ->
                val surah = IslamicDataProvider.all114Surahs.find { it.number == surahNum }
                if (surah != null) {
                    ayahs.forEach { ayah ->
                        if (ayah.textArabic.contains(query)) {
                            results.add(
                                QuranSearchResult(
                                    surah = surah,
                                    ayah = ayah,
                                    matchedSnippet = ayah.textArabic,
                                    matchType = "آية ${ayah.ayahNumber}"
                                )
                            )
                        }
                    }
                }
            }

            results.take(40)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
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
                            Icon(Icons.Default.Search, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "البحث في القرآن الكريم",
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن كلمة، آية، أو اسم سورة...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldAccent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp)
            )

            if (searchQuery.trim().length >= 2) {
                Text(
                    text = "نتائج البحث (${searchResults.size}):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(searchResults) { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateToResult(result.surah, result.ayah?.ayahNumber)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "سورة ${result.surah.nameArabic}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = result.matchType,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GoldAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = result.matchedSnippet,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    ),
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اكتب حرفين أو أكثر للبحث في سور وآيات القرآن الكريم",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
