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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Dhikr
import com.example.service.FloatingAzkarService
import com.example.ui.AcefViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen(
    viewModel: AcefViewModel
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedAzkarCategory.collectAsState()
    val azkarCounts by viewModel.azkarCounts.collectAsState()
    val isFloatingActive by viewModel.isFloatingAzkarActive.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text("إذن الظهور فوق التطبيقات", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("لتشغيل فقاعة الأذكار العائمة فوق الشاشات والتطبيقات الأخرى، يلزم تفعيل إذن (الظهور فوق التطبيقات) في إعدادات النظام.")
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

    val categories = listOf(
        "أذكار الصباح",
        "أذكار المساء",
        "أذكار النوم",
        "أذكار بعد الصلاة",
        "أدعية قرآنية"
    )

    val currentList = remember(selectedCategory) {
        IslamicDataProvider.getAzkarByCategory(selectedCategory)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Floating Bubble Control Banner (User Request 2)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFloatingActive) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "فقاعة الأذكار فوق التطبيقات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isFloatingActive) "الفقاعة تعمل الآن فوق التطبيقات الأخرى" else "أذكار تطفو فوق شاشة هاتفك أثناء التصفح",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isFloatingActive) GoldAccent else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingActive,
                        onCheckedChange = { checked ->
                            viewModel.toggleFloatingAzkarBubble(checked, context) {
                                showPermissionDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldDark,
                            checkedTrackColor = GoldAccent
                        )
                    )
                }
            }
        }

        // Category Horizontal Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAzkarCategory(cat) },
                        label = {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = EmeraldDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = GoldAccent,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        // List of Dhikrs in current category
        items(currentList, key = { it.id }) { dhikr ->
            val count = azkarCounts[dhikr.id] ?: 0
            val isCompleted = count >= dhikr.count

            DhikrItemCard(
                dhikr = dhikr,
                currentCount = count,
                isCompleted = isCompleted,
                onIncrement = { viewModel.incrementDhikrCount(dhikr) },
                onReset = { viewModel.resetDhikrCount(dhikr.id) },
                onCopy = { viewModel.copyTextToClipboard("ذكر", "${dhikr.text}\n${dhikr.fadl}") },
                onShare = { viewModel.shareText("${dhikr.text}\n${dhikr.fadl}", "ذكر من تطبيق عاصف") }
            )
        }
    }
}

@Composable
fun DhikrItemCard(
    dhikr: Dhikr,
    currentCount: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isCompleted) 1.5.dp else 0.5.dp,
            if (isCompleted) GoldAccent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = dhikr.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, "نسخ", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, "مشاركة", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    if (currentCount > 0) {
                        IconButton(onClick = onReset) {
                            Icon(Icons.Default.Refresh, "تصفير", tint = GoldAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Dhikr Text
            Text(
                text = dhikr.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (dhikr.fadl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• الفضل: ${dhikr.fadl}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GoldAccent,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )
            }

            if (dhikr.reference.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "[${dhikr.reference}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counter Button Row
            Button(
                onClick = onIncrement,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) GoldAccent else EmeraldPrimary,
                    contentColor = if (isCompleted) EmeraldDark else Color.White
                )
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompleted) "تم الانتهاء ($currentCount/${dhikr.count})" else "تسبيح (+1)  [$currentCount/${dhikr.count}]",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
