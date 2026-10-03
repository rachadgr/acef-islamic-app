package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AcefViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import kotlinx.coroutines.launch

data class ScholarCategory(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val questions: List<String>
)

data class ScholarChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timeFormatted: String = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar")).format(java.util.Date())
)

@Composable
fun AiScholarScreen(
    viewModel: AcefViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val question by viewModel.aiScholarQuestion.collectAsState()
    val answer by viewModel.aiScholarAnswer.collectAsState()
    val isLoading by viewModel.isAiScholarLoading.collectAsState()

    // Conversational Chat History in memory
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ScholarChatMessage(
                    isUser = false,
                    text = "السلام عليكم ورحمة الله وبركاته، مرحباً بك في «المستشار الذكي» لتطبيق آصف.\nأنا هنا لمساعدتك في فهم وتدبر القرآن الكريم، وشروح الأحاديث النبوية، وفقه العبادات، والأذكار.\n\nتفضل بطرح تساؤلك الشرعي أو اختر من النماذج المقترحة أدناه."
                )
            )
        )
    }

    // When answer updates, append to chat history
    LaunchedEffect(answer) {
        val ans = answer
        if (!ans.isNullOrBlank()) {
            val lastMsg = chatMessages.lastOrNull()
            if (lastMsg == null || lastMsg.isUser || lastMsg.text != ans) {
                chatMessages = chatMessages + ScholarChatMessage(isUser = false, text = ans)
                coroutineScope.launch {
                    listState.animateScrollToItem((chatMessages.size - 1).coerceAtLeast(0))
                }
            }
        }
    }

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.setAiScholarQuestion(spokenText)
                // Automatically send question
                chatMessages = chatMessages + ScholarChatMessage(isUser = true, text = spokenText)
                viewModel.askAiScholar(spokenText)
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تفضل بطرح سؤالك الإسلامي صوتياً...")
            }
            try {
                speechLauncher.launch(intent)
            } catch (_: Exception) {
                viewModel.showFeedback("ميزة الإدخال الصوتي غير متوفرة على الجهاز")
            }
        } else {
            viewModel.showFeedback("يرجى منح إذن الميكروفون لاستخدام الإدخال الصوتي")
        }
    }

    val categories = remember {
        listOf(
            ScholarCategory(
                title = "تفسير القرآن",
                icon = Icons.Default.MenuBook,
                questions = listOf(
                    "تفسير قوله تعالى: ﴿وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ﴾",
                    "تفسير آية الكرسي والدلالات العقدية والأسماء الحسنى فيها",
                    "ما هي مقاصد سورة الفاتحة وأسرارها الإيمانية؟",
                    "تفسير سورة الإخلاص ولماذا تعدل ثلث القرآن؟"
                )
            ),
            ScholarCategory(
                title = "شروح الحديث",
                icon = Icons.Default.HistoryEdu,
                questions = listOf(
                    "ما هو شرح حديث: «إنما الأعمال بالنيات»؟",
                    "ما معنى حديث: «كلمتان خفيفتان على اللسان ثقيلتان في الميزان»؟",
                    "شرح حديث: «بني الإسلام على خمس» ودلالاته",
                    "شرح حديث: «المسلم من سلم المسلمون من لسانه ويده»"
                )
            ),
            ScholarCategory(
                title = "فقه العبادات",
                icon = Icons.Default.Mosque,
                questions = listOf(
                    "ما هو فضل صلاة الوتر وكيفية أدائها الصحيحة؟",
                    "كيف نحقق الخشوع والسكينة في الصلاة؟",
                    "ما هي كيفية صلاة الاستخارة ودعاؤها المأثور؟",
                    "ما هي شروط التوبة الصادقة وكيفية صلاة التوبة؟"
                )
            ),
            ScholarCategory(
                title = "أذكار وأدعية",
                icon = Icons.Default.VolunteerActivism,
                questions = listOf(
                    "ما هي أفضل الأدعية والأذكار المستجابة عند الكرب؟",
                    "ما هو فضل سيد الاستغفار وشرح معانيه؟",
                    "ما هي أذكار النوم الصحيحة الواردة عن النبي ﷺ؟",
                    "كيف نجمع بين الأخذ بالأسباب والتوكل على الله في طلب الرزق؟"
                )
            )
        )
    }

    var selectedCategoryIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_scholar_screen")
    ) {
        // Sticky Header: Disclaimer Notice
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GoldAccent.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(0.6.dp, GoldAccent.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تنبيه شرعي: الإجابات للاسترشاد والتثقيف العام في علوم الدين، ويجب استشارة أهل الفتوى المعتمدين في المسائل الخاصة.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Suggested Questions / Categories Chips Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.indices.toList()) { index ->
                    val cat = categories[index]
                    val isSelected = selectedCategoryIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryIndex = index },
                        label = { Text(cat.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(cat.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = EmeraldDark,
                            selectedLeadingIconColor = EmeraldDark
                        )
                    )
                }
            }

            // Quick Pill Prompts for chosen category
            val currentQuestions = categories[selectedCategoryIndex].questions
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items(currentQuestions) { sample ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable {
                            viewModel.setAiScholarQuestion(sample)
                            chatMessages = chatMessages + ScholarChatMessage(isUser = true, text = sample)
                            viewModel.askAiScholar(sample)
                        }
                    ) {
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.6.dp)

        // Conversational Messages Scrollable Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    // User Message Bubble (RTL Right-aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp).copy(bottomEnd = androidx.compose.foundation.shape.CornerSize(2.dp)),
                            color = EmeraldPrimary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timeFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }
                        }
                    }
                } else {
                    // AI Scholar Message Bubble (RTL Left-aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp).copy(bottomStart = androidx.compose.foundation.shape.CornerSize(2.dp)),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.6f)),
                            shadowElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Scholar Header with Avatar and Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = GoldBright,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "المستشار الذكي (آصف)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = GoldAccent
                                            )
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { viewModel.copyTextToClipboard("إجابة المستشار الذكي", msg.text) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "نسخ",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.shareText(msg.text, "إجابة من المستشار الإسلامي") },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "مشاركة",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.5.sp,
                                        lineHeight = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.timeFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Loading state indicator
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = GoldAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "المستشار يتأمل ويبحث في المراجع والتفاسير المعتمدة...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Field and Bottom Controls Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Input Button
                    IconButton(
                        onClick = {
                            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "تفضل بطرح سؤالك الإسلامي صوتياً...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (_: Exception) {
                                    viewModel.showFeedback("ميزة الإدخال الصوتي غير متوفرة على الجهاز")
                                }
                            } else {
                                recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "تسجيل صوتي",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Text Field
                    OutlinedTextField(
                        value = question,
                        onValueChange = { viewModel.setAiScholarQuestion(it) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .testTag("ai_question_input"),
                        placeholder = { Text("اكتب سؤالك الشرعي هنا...", fontSize = 13.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            val q = question.trim()
                            if (q.isNotBlank() && !isLoading) {
                                chatMessages = chatMessages + ScholarChatMessage(isUser = true, text = q)
                                viewModel.askAiScholar(q)
                                viewModel.setAiScholarQuestion("")
                            }
                        },
                        enabled = question.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (question.isNotBlank() && !isLoading) GoldAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("ai_ask_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "إرسال",
                            tint = if (question.isNotBlank() && !isLoading) EmeraldDark else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
