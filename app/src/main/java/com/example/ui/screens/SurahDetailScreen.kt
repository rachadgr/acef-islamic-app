package com.example.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Ayah
import com.example.data.model.QuranMushafMetadata
import com.example.data.model.QuranMushafMetadata.getJuzArabicName
import com.example.data.model.QuranMushafMetadata.getJuzNumber
import com.example.data.model.QuranRiwayah
import com.example.data.model.Surah
import com.example.ui.AcefViewModel
import com.example.ui.components.BookmarkRibbon
import com.example.ui.components.DirectQuranNavigationSheet
import com.example.ui.components.QuranBookmarksSheet
import com.example.ui.components.QuranSearchSheet
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.util.AudioPlayerHelper
import kotlinx.coroutines.launch

enum class QuranDisplayMode {
    MUSHAF_PAGES,       // صفحات المصحف الشريف (صفحة بصفحة مع إطار المصحف المذهب)
    MUSHAF_CONTINUOUS,  // المصحف المسترسل (نص متصل بإطار المصحف وقراءة متواصلة)
    VERSES_LIST         // قائمة الآيات والبطاقات المفصلة
}

enum class MushafPaperTheme(
    val title: String,
    val iconName: String,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val accentColor: Color
) {
    CREAM_PAPER(
        title = "ورق المصحف الأصيل",
        iconName = "📜",
        backgroundColor = Color(0xFFFAF5E8), // Authentic warm ivory cream of Madinah Mushaf
        surfaceColor = Color(0xFFF3ECE0),
        textColor = Color(0xFF141414),       // High-contrast deep black calligraphy ink
        borderColor = Color(0xFFC5A059),     // Antique warm gold
        accentColor = Color(0xFF9A7B38)
    ),
    NIGHT_MODE(
        title = "الوضع الليلي الفاخر",
        iconName = "🌙",
        backgroundColor = Color(0xFF101214), // Luxurious dark AMOLED night
        surfaceColor = Color(0xFF181C20),
        textColor = Color(0xFFEEEEEE),       // Soft ivory white
        borderColor = Color(0xFFD4AF37),     // Luminous gold
        accentColor = Color(0xFFE5A93C)
    ),
    NIGHT_EMERALD(
        title = "المصحف الزمردي",
        iconName = "🌿",
        backgroundColor = Color(0xFF071912), // Luxurious dark emerald night
        surfaceColor = Color(0xFF0E271D),
        textColor = Color(0xFFF7F4EC),       // Soft cream white
        borderColor = Color(0xFFD4AF37),     // Luminous gold
        accentColor = Color(0xFFE5A93C)
    ),
    PURE_WHITE(
        title = "المصحف الأبيض النقي",
        iconName = "☀️",
        backgroundColor = Color(0xFFFFFFFF),
        surfaceColor = Color(0xFFF8F9FA),
        textColor = Color(0xFF111111),
        borderColor = Color(0xFFC5A059),
        accentColor = Color(0xFFB8860B)
    ),
    SEPIA(
        title = "المصحف الكلاسيكي المعتّق",
        iconName = "🍂",
        backgroundColor = Color(0xFFF4ECD8),
        surfaceColor = Color(0xFFEADFCA),
        textColor = Color(0xFF2C2416),
        borderColor = Color(0xFFB38947),
        accentColor = Color(0xFF8C6627)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surah: Surah,
    verses: List<Ayah>,
    viewModel: AcefViewModel,
    onBack: () -> Unit,
    onGoHome: () -> Unit = {}
) {
    val selectedReciter by viewModel.audioPlayer.selectedReciter.collectAsState()
    val isFullSurahPlaying by viewModel.audioPlayer.isFullSurahPlaying.collectAsState()
    val isVerseByVersePlaying by viewModel.audioPlayer.isVerseByVersePlaying.collectAsState()
    val currentActiveAyah by viewModel.audioPlayer.currentActiveAyahNumber.collectAsState()
    val currentSurahNum by viewModel.audioPlayer.currentSurahNumber.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val currentRiwayah by viewModel.currentRiwayah.collectAsState()
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsState()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsState()

    var displayMode by remember { mutableStateOf(QuranDisplayMode.MUSHAF_PAGES) }
    var paperTheme by remember { mutableStateOf(MushafPaperTheme.CREAM_PAPER) }
    var showReciterSheet by remember { mutableStateOf(false) }
    var showFontSettingsSheet by remember { mutableStateOf(false) }
    var showSurahPickerSheet by remember { mutableStateOf(false) }
    var showDirectNavSheet by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }
    var quranFontSizeSp by remember { mutableFloatStateOf(25f) }

    // System and Gesture Back Handler: cleanly dismisses overlays first, or returns to Surah index / Home
    BackHandler(enabled = true) {
        when {
            showReciterSheet -> showReciterSheet = false
            showFontSettingsSheet -> showFontSettingsSheet = false
            showSurahPickerSheet -> showSurahPickerSheet = false
            showDirectNavSheet -> showDirectNavSheet = false
            showSearchSheet -> showSearchSheet = false
            showBookmarksSheet -> showBookmarksSheet = false
            showControls -> showControls = false
            else -> onBack()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val previousSurah = remember(surah.number) {
        if (surah.number > 1) IslamicDataProvider.allSurahs.find { it.number == surah.number - 1 } else null
    }
    val nextSurah = remember(surah.number) {
        if (surah.number < 114) IslamicDataProvider.allSurahs.find { it.number == surah.number + 1 } else null
    }

    // Selected Ayah for quick action bottom sheet
    var selectedAyahForActions by remember { mutableStateOf<Ayah?>(null) }
    val isCurrentSurahVersePlaying = isVerseByVersePlaying && currentSurahNum == surah.number

    // Pagination for Mushaf Pages mode
    val versesPerPage = remember(verses.size) {
        when {
            verses.size <= 8 -> verses.size.coerceAtLeast(1)
            verses.size <= 20 -> 7
            verses.size <= 45 -> 8
            else -> 10
        }
    }
    val totalPages = ((verses.size + versesPerPage - 1) / versesPerPage).coerceAtLeast(1)
    var currentPageIndex by remember(surah.number) { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { totalPages }
    )

    // Reset page index and pager scroll when surah changes
    LaunchedEffect(surah.number) {
        pagerState.scrollToPage(0)
        currentPageIndex = 0
    }

    // Synchronize currentPageIndex with pagerState.currentPage and auto-save reading progress
    LaunchedEffect(pagerState.currentPage, surah.number) {
        currentPageIndex = pagerState.currentPage
        val firstAyah = (currentPageIndex * versesPerPage + 1).coerceAtMost(surah.numberOfAyahs)
        viewModel.saveCurrentReadingProgress(surah.number, firstAyah, surah.nameArabic)
    }

    // Handle requested Quran page jump (from direct navigation or bookmarks)
    val requestedPage by viewModel.requestedQuranPage.collectAsState()
    LaunchedEffect(requestedPage) {
        if (requestedPage != null) {
            val target = requestedPage!!.coerceIn(0, totalPages - 1)
            pagerState.scrollToPage(target)
            viewModel.clearRequestedQuranPage()
        }
    }

    // Auto-navigate to page containing the currently reciting ayah
    LaunchedEffect(currentActiveAyah, isCurrentSurahVersePlaying) {
        val active = currentActiveAyah
        if (isCurrentSurahVersePlaying && active != null && active > 0) {
            val targetPage = ((active - 1) / versesPerPage).coerceIn(0, totalPages - 1)
            if (targetPage != pagerState.currentPage) {
                pagerState.animateScrollToPage(targetPage)
            }
        }
    }

    val isCurrentPageBookmarked = remember(favorites, surah.number, currentPageIndex) {
        val pageNum = currentPageIndex + 1
        val title = "سورة ${surah.nameArabic} - صفحة $pageNum"
        favorites.any { it.type == "BOOKMARK" && it.title == title }
    }

    val pageVerses = remember(currentPageIndex, verses, versesPerPage, displayMode) {
        if (displayMode == QuranDisplayMode.MUSHAF_CONTINUOUS) {
            verses
        } else {
            val start = currentPageIndex * versesPerPage
            val end = (start + versesPerPage).coerceAtMost(verses.size)
            if (start in verses.indices) verses.subList(start, end) else verses
        }
    }

    val juzNumber = remember(surah.number, pageVerses) {
        getJuzNumber(surah.number, pageVerses.firstOrNull()?.ayahNumber ?: 1)
    }
    val juzName = remember(juzNumber) { getJuzArabicName(juzNumber) }
    val hizbNumber = remember(juzNumber, pageVerses) {
        val firstAyah = pageVerses.firstOrNull()?.ayahNumber ?: 1
        val hizbBase = (juzNumber - 1) * 2 + 1
        if (firstAyah > (surah.numberOfAyahs / 2)) hizbBase + 1 else hizbBase
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(paperTheme.backgroundColor)
    ) {
        // ==========================================
        // 1. MAIN READER CANVAS (FULL SCREEN IMMERSION)
        // ==========================================
        when (displayMode) {
            QuranDisplayMode.MUSHAF_PAGES -> {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 3,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(totalPages, surah.number) {
                                detectTapGestures { offset ->
                                    val width = size.width
                                    val x = offset.x
                                    when {
                                        // Right edge: Next page (or next Surah if on last page)
                                        x > width * 0.72f -> {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage < totalPages - 1) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                } else if (surah.number < 114) {
                                                    viewModel.openNextSurah()
                                                }
                                            }
                                        }
                                        // Left edge: Previous page (or previous Surah if on first page)
                                        x < width * 0.28f -> {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage > 0) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                } else if (surah.number > 1) {
                                                    viewModel.openPreviousSurah()
                                                }
                                            }
                                        }
                                        // Center tap: toggle controls
                                        else -> {
                                            showControls = !showControls
                                        }
                                    }
                                }
                            }
                    ) { pageIdx ->
                        val pageOffset = ((pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction)
                        val absOffset = kotlin.math.abs(pageOffset)

                        val thisPageVerses = remember(pageIdx, verses, versesPerPage) {
                            val start = pageIdx * versesPerPage
                            val end = (start + versesPerPage).coerceAtMost(verses.size)
                            if (start in verses.indices) verses.subList(start, end) else verses
                        }
                        val thisJuzNumber = getJuzNumber(surah.number, thisPageVerses.firstOrNull()?.ayahNumber ?: 1)
                        val thisJuzName = getJuzArabicName(thisJuzNumber)
                        val thisHizbNumber = remember(thisJuzNumber, thisPageVerses) {
                            val firstAyah = thisPageVerses.firstOrNull()?.ayahNumber ?: 1
                            val hizbBase = (thisJuzNumber - 1) * 2 + 1
                            if (firstAyah > (surah.numberOfAyahs / 2)) hizbBase + 1 else hizbBase
                        }

                        val thisPageBookmarked = remember(favorites, surah.number, pageIdx) {
                            val pageNum = pageIdx + 1
                            val title = "سورة ${surah.nameArabic} - صفحة $pageNum"
                            favorites.any { it.type == "BOOKMARK" && it.title == title }
                        }

                        // 3D Physical Page Turn Layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    cameraDistance = 16f * density
                                    rotationY = -14f * pageOffset.coerceIn(-1f, 1f)
                                    val scale = 1f - 0.035f * absOffset.coerceIn(0f, 1f)
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = (1f - 0.2f * absOffset).coerceIn(0f, 1f)
                                }
                                .padding(
                                    top = if (showControls) 82.dp else 4.dp,
                                    bottom = if (showControls || isCurrentSurahVersePlaying) 125.dp else 16.dp,
                                    start = if (isLandscape) 12.dp else 4.dp,
                                    end = if (isLandscape) 12.dp else 4.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLandscape) {
                                // Dual-page spread in landscape (صفحتان متقابلتان كالمصحف المفتوح)
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right page
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MushafPageContainer(
                                            paperTheme = paperTheme,
                                            juzName = thisJuzName,
                                            hizbNumber = thisHizbNumber,
                                            pageNumber = pageIdx + 1,
                                            totalPages = totalPages,
                                            surah = surah,
                                            showSurahHeader = (pageIdx == 0),
                                            pageVerses = thisPageVerses,
                                            currentRiwayah = currentRiwayah,
                                            fontSizeSp = quranFontSizeSp * 0.9f,
                                            isCurrentSurahVersePlaying = isCurrentSurahVersePlaying,
                                            currentActiveAyah = currentActiveAyah,
                                            selectedAyahNumber = selectedAyahForActions?.ayahNumber,
                                            isBookmarked = thisPageBookmarked,
                                            onAyahClicked = { tappedAyahNum ->
                                                val tapped = verses.find { it.ayahNumber == tappedAyahNum }
                                                if (tapped != null) selectedAyahForActions = tapped
                                            }
                                        )
                                    }

                                    // Center Spine Gutter Divider
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .fillMaxHeight(0.92f)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.25f),
                                                        GoldAccent.copy(alpha = 0.4f),
                                                        Color.Black.copy(alpha = 0.25f)
                                                    )
                                                )
                                            )
                                    )

                                    // Left page (next page if available, else decorative closure)
                                    val nextPageIdx = pageIdx + 1
                                    val nextHasVerses = nextPageIdx < totalPages
                                    val nextVerses = remember(nextPageIdx, verses, versesPerPage) {
                                        if (nextHasVerses) {
                                            val start = nextPageIdx * versesPerPage
                                            val end = (start + versesPerPage).coerceAtMost(verses.size)
                                            if (start in verses.indices) verses.subList(start, end) else emptyList()
                                        } else emptyList()
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (nextHasVerses && nextVerses.isNotEmpty()) {
                                            val nextJuz = getJuzNumber(surah.number, nextVerses.firstOrNull()?.ayahNumber ?: 1)
                                            val nextBookmarked = favorites.any {
                                                it.type == "BOOKMARK" && it.title == "سورة ${surah.nameArabic} - صفحة ${nextPageIdx + 1}"
                                            }
                                            MushafPageContainer(
                                                paperTheme = paperTheme,
                                                juzName = getJuzArabicName(nextJuz),
                                                hizbNumber = thisHizbNumber,
                                                pageNumber = nextPageIdx + 1,
                                                totalPages = totalPages,
                                                surah = surah,
                                                showSurahHeader = false,
                                                pageVerses = nextVerses,
                                                currentRiwayah = currentRiwayah,
                                                fontSizeSp = quranFontSizeSp * 0.9f,
                                                isCurrentSurahVersePlaying = isCurrentSurahVersePlaying,
                                                currentActiveAyah = currentActiveAyah,
                                                selectedAyahNumber = selectedAyahForActions?.ayahNumber,
                                                isBookmarked = nextBookmarked,
                                                onAyahClicked = { tappedAyahNum ->
                                                    val tapped = verses.find { it.ayahNumber == tappedAyahNum }
                                                    if (tapped != null) selectedAyahForActions = tapped
                                                }
                                            )
                                        } else {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                color = paperTheme.surfaceColor.copy(alpha = 0.5f),
                                                border = androidx.compose.foundation.BorderStroke(1.2.dp, paperTheme.borderColor.copy(alpha = 0.4f))
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("۞ تَمَّتْ بِحَمْدِ اللَّهِ ۞", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                        if (nextSurah != null) {
                                                            Spacer(modifier = Modifier.height(10.dp))
                                                            Button(
                                                                onClick = { viewModel.openNextSurah() },
                                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                                            ) {
                                                                Text("الانتقال إلى سورة ${nextSurah.nameArabic}")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Portrait: Single full-screen Mushaf Page
                                MushafPageContainer(
                                    paperTheme = paperTheme,
                                    juzName = thisJuzName,
                                    hizbNumber = thisHizbNumber,
                                    pageNumber = pageIdx + 1,
                                    totalPages = totalPages,
                                    surah = surah,
                                    showSurahHeader = (pageIdx == 0),
                                    pageVerses = thisPageVerses,
                                    currentRiwayah = currentRiwayah,
                                    fontSizeSp = quranFontSizeSp,
                                    isCurrentSurahVersePlaying = isCurrentSurahVersePlaying,
                                    currentActiveAyah = currentActiveAyah,
                                    selectedAyahNumber = selectedAyahForActions?.ayahNumber,
                                    isBookmarked = thisPageBookmarked,
                                    onAyahClicked = { tappedAyahNum ->
                                        val tapped = verses.find { it.ayahNumber == tappedAyahNum }
                                        if (tapped != null) {
                                            selectedAyahForActions = tapped
                                        }
                                    }
                                )
                            }

                            // Quick transition pill on the last page of the surah
                            if (pageIdx == totalPages - 1 && nextSurah != null && !isLandscape) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = if (showControls || isCurrentSurahVersePlaying) 125.dp else 18.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.openNextSurah() },
                                    shape = RoundedCornerShape(16.dp),
                                    color = EmeraldDark.copy(alpha = 0.95f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                    shadowElevation = 6.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "نهاية السورة • الانتقال إلى سورة ${nextSurah.nameArabic}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = GoldBright,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            QuranDisplayMode.MUSHAF_CONTINUOUS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            top = if (showControls) 90.dp else 12.dp,
                            bottom = if (showControls || isCurrentSurahVersePlaying) 140.dp else 40.dp,
                            start = 8.dp,
                            end = 8.dp
                        )
                        .pointerInput(Unit) {
                            detectTapGestures {
                                showControls = !showControls
                            }
                        }
                ) {
                    MushafPageContainer(
                        paperTheme = paperTheme,
                        juzName = juzName,
                        hizbNumber = hizbNumber,
                        pageNumber = 1,
                        totalPages = 1,
                        surah = surah,
                        showSurahHeader = true,
                        pageVerses = verses,
                        currentRiwayah = currentRiwayah,
                        fontSizeSp = quranFontSizeSp,
                        isCurrentSurahVersePlaying = isCurrentSurahVersePlaying,
                        currentActiveAyah = currentActiveAyah,
                        selectedAyahNumber = selectedAyahForActions?.ayahNumber,
                        onAyahClicked = { tappedAyahNum ->
                            val tapped = verses.find { it.ayahNumber == tappedAyahNum }
                            if (tapped != null) {
                                selectedAyahForActions = tapped
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SurahEndNavigationSection(
                        surah = surah,
                        onPrevSurah = { viewModel.openPreviousSurah() },
                        onNextSurah = { viewModel.openNextSurah() },
                        onOpenIndex = { showSurahPickerSheet = true },
                        paperTheme = paperTheme
                    )
                }
            }

            QuranDisplayMode.VERSES_LIST -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (showControls) 90.dp else 12.dp,
                            bottom = if (showControls || isCurrentSurahVersePlaying) 140.dp else 40.dp,
                            start = 14.dp,
                            end = 14.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(verses, key = { _, it -> it.ayahNumber }) { _, ayah ->
                        val isActive = isCurrentSurahVersePlaying && currentActiveAyah == ayah.ayahNumber
                        val isFavorite = favorites.any { it.arabicText == ayah.textArabic }

                        AyahCardItem(
                            ayah = ayah,
                            riwayah = currentRiwayah,
                            fontSizeSp = quranFontSizeSp,
                            surahName = surah.nameArabic,
                            isActive = isActive,
                            isFavorite = isFavorite,
                            onTafsirClick = {
                                viewModel.openTafsir(ayah, surah.nameArabic)
                                viewModel.requestAiTafsir()
                            },
                            onPlayAudio = {
                                viewModel.playAyahAudio(ayah)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(
                                    type = "AYAH",
                                    title = "سورة ${surah.nameArabic}: ${ayah.ayahNumber}",
                                    arabicText = ayah.getText(currentRiwayah),
                                    subtitle = "سورة ${surah.nameArabic}",
                                    reference = "آية رقم ${ayah.ayahNumber}"
                                )
                            },
                            onCopy = {
                                viewModel.copyTextToClipboard("آية قرآنية", "﴿${ayah.getText(currentRiwayah)}﴾ [سورة ${surah.nameArabic}: ${ayah.ayahNumber}]")
                            },
                            onShare = {
                                viewModel.shareText("﴿${ayah.getText(currentRiwayah)}﴾ [سورة ${surah.nameArabic}: ${ayah.ayahNumber}]", "آية كريمة من تطبيق آصف")
                            }
                        )
                    }

                    item {
                        SurahEndNavigationSection(
                            surah = surah,
                            onPrevSurah = { viewModel.openPreviousSurah() },
                            onNextSurah = { viewModel.openNextSurah() },
                            onOpenIndex = { showSurahPickerSheet = true },
                            paperTheme = paperTheme
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. SUBTLE BOTTOM PAGE PILL (WHEN CONTROLS HIDDEN)
        // ==========================================
        if (!showControls && displayMode == QuranDisplayMode.MUSHAF_PAGES && !isCurrentSurahVersePlaying) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clickable { showControls = true },
                shape = RoundedCornerShape(16.dp),
                color = paperTheme.surfaceColor.copy(alpha = 0.90f),
                border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.4f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MenuBook, null, tint = paperTheme.borderColor, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سورة ${surah.nameArabic} • ص ${currentPageIndex + 1}/$totalPages • انقر للتحكم والفهرس",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.textColor
                        )
                    )
                }
            }
        }

        // ==========================================
        // 3. TOP CONTROL BAR (ANIMATED)
        // ==========================================
        AnimatedVisibility(
            visible = showControls || displayMode != QuranDisplayMode.MUSHAF_PAGES,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = paperTheme.surfaceColor.copy(alpha = 0.97f),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Back to Surahs Index
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "فهرس السور",
                                    tint = paperTheme.borderColor
                                )
                            }

                            // Go directly Home
                            IconButton(onClick = onGoHome) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "الرئيسية",
                                    tint = GoldAccent
                                )
                            }

                            // Direct Navigation button (سورة / جزء / رقم الصفحة)
                            IconButton(onClick = { showDirectNavSheet = true }) {
                                Icon(
                                    Icons.Default.Explore,
                                    contentDescription = "الانتقال المباشر",
                                    tint = GoldAccent
                                )
                            }

                            // Search button (بحث في السور والآيات)
                            IconButton(onClick = { showSearchSheet = true }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = paperTheme.borderColor
                                )
                            }
                        }

                        // Center: Interactive Surah Name with Dropdown & Previous/Next Quick Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (surah.number > 1) {
                                IconButton(
                                    onClick = { viewModel.openPreviousSurah() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "السورة السابقة",
                                        tint = paperTheme.borderColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { showDirectNavSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                color = paperTheme.borderColor.copy(alpha = 0.10f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "سُورَةُ ${surah.nameArabic}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = paperTheme.textColor
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "الانتقال",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = "${surah.revelationTypeAr} • ${surah.numberOfAyahs} آية • $juzName",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                color = paperTheme.textColor.copy(alpha = 0.75f)
                                            )
                                        )
                                    }
                                }
                            }

                            if (surah.number < 114) {
                                IconButton(
                                    onClick = { viewModel.openNextSurah() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "السورة التالية",
                                        tint = paperTheme.borderColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Night Mode quick toggle
                            IconButton(onClick = {
                                paperTheme = if (paperTheme == MushafPaperTheme.NIGHT_MODE) MushafPaperTheme.CREAM_PAPER else MushafPaperTheme.NIGHT_MODE
                            }) {
                                Icon(
                                    if (paperTheme == MushafPaperTheme.NIGHT_MODE) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                                    contentDescription = "الوضع الليلي",
                                    tint = if (paperTheme == MushafPaperTheme.NIGHT_MODE) GoldBright else paperTheme.borderColor
                                )
                            }

                            // Bookmark toggle for current page
                            IconButton(onClick = {
                                val firstText = pageVerses.firstOrNull()?.textArabic ?: ""
                                viewModel.toggleQuranPageBookmark(surah, currentPageIndex + 1, firstText)
                            }) {
                                Icon(
                                    if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "حفظ علامة مرجعية",
                                    tint = if (isCurrentPageBookmarked) GoldBright else paperTheme.borderColor
                                )
                            }

                            // Bookmarks List
                            IconButton(onClick = { showBookmarksSheet = true }) {
                                Icon(
                                    Icons.Default.Bookmarks,
                                    contentDescription = "العلامات المرجعية",
                                    tint = paperTheme.borderColor
                                )
                            }

                            // Font & Display Settings Button (Modern Font Sizing Sheet)
                            IconButton(onClick = { showFontSettingsSheet = true }) {
                                Icon(
                                    Icons.Default.FormatSize,
                                    contentDescription = "إعدادات الخط والقراءة",
                                    tint = paperTheme.borderColor
                                )
                            }

                            // Reciter Selector Button
                            IconButton(onClick = { showReciterSheet = true }) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = "تغيير القارئ",
                                    tint = paperTheme.borderColor
                                )
                            }

                            // Offline Surah Download / Status Button
                            val isThisSurahOffline = surah.number in downloadedSurahs
                            val isDownloadingThis = downloadProgressMap.containsKey(surah.number)
                            if (isDownloadingThis) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { downloadProgressMap[surah.number] ?: 0.1f },
                                        modifier = Modifier.size(18.dp),
                                        color = GoldAccent,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else if (isThisSurahOffline) {
                                IconButton(onClick = {
                                    viewModel.showFeedback("سورة ${surah.nameArabic} محملة وجاهزة للاستماع أوفلاين بدون إنترنت")
                                }) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = "السورة محملة أوفلاين",
                                        tint = GoldAccent
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    viewModel.downloadSurahOffline(surah.number, selectedReciter)
                                }) {
                                    Icon(
                                        Icons.Default.CloudDownload,
                                        contentDescription = "تحميل السورة للاستماع أوفلاين",
                                        tint = paperTheme.borderColor
                                    )
                                }
                            }
                        }
                    }

                    // Display Mode Chips & Live Page Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { showDirectNavSheet = true }
                        ) {
                            Text(
                                text = "صفحة ${currentPageIndex + 1} / $totalPages",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = displayMode == QuranDisplayMode.MUSHAF_PAGES,
                                onClick = { displayMode = QuranDisplayMode.MUSHAF_PAGES },
                                label = { Text("صفحات المصحف", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(13.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = paperTheme.borderColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = displayMode == QuranDisplayMode.MUSHAF_CONTINUOUS,
                                onClick = { displayMode = QuranDisplayMode.MUSHAF_CONTINUOUS },
                                label = { Text("مسترسل", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = paperTheme.borderColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = displayMode == QuranDisplayMode.VERSES_LIST,
                                onClick = { displayMode = QuranDisplayMode.VERSES_LIST },
                                label = { Text("بطاقات", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = paperTheme.borderColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Riwayah Switcher Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = paperTheme.borderColor.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                val nextRiwayah = if (currentRiwayah == QuranRiwayah.HAFS) QuranRiwayah.WARSH else QuranRiwayah.HAFS
                                viewModel.setQuranRiwayah(nextRiwayah)
                            }
                        ) {
                            Text(
                                text = currentRiwayah.shortName,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = paperTheme.borderColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3.5. PERSISTENT FLOATING EXIT BUTTON
        // Always visible in immersive mode so user can never get stuck
        // ==========================================
        AnimatedVisibility(
            visible = !showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = paperTheme.surfaceColor.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.5f)),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { onBack() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "خروج إلى قائمة السور",
                            tint = paperTheme.borderColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "خروج",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = paperTheme.textColor,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(paperTheme.borderColor.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.clickable { onGoHome() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "الصفحة الرئيسية",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "الرئيسية",
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

        // ==========================================
        // 4. BOTTOM CONTROL BAR (ANIMATED)
        // ==========================================
        AnimatedVisibility(
            visible = showControls || isCurrentSurahVersePlaying,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Audio Player Card
                if (isCurrentSurahVersePlaying) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GoldAccent, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "القارئ: ${selectedReciter.nameArabic}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldAccent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "الآية ($currentActiveAyah / ${verses.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GoldBright
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.playPreviousAyah() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.SkipPrevious, "الآية السابقة", tint = Color.White)
                                }
                                val isPlaying = playbackState is AudioPlayerHelper.PlaybackState.Playing
                                IconButton(
                                    onClick = { viewModel.audioPlayer.togglePlayPause() },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(GoldAccent, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل/إيقاف",
                                        tint = EmeraldDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.playNextAyah() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.SkipNext, "الآية التالية", tint = Color.White)
                                }
                                IconButton(
                                    onClick = { viewModel.stopQuranAudio() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Stop, "إيقاف", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                // Page slider and turn controls (for Mushaf Pages mode)
                if (displayMode == QuranDisplayMode.MUSHAF_PAGES) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = paperTheme.surfaceColor.copy(alpha = 0.96f),
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            // Seamless Surah Navigation Header Bar inside Bottom Controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (surah.number > 1 && previousSurah != null) {
                                    TextButton(
                                        onClick = { viewModel.openPreviousSurah() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(12.dp), tint = paperTheme.borderColor)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "سورة ${previousSurah.nameArabic}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = paperTheme.borderColor,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                TextButton(
                                    onClick = { showSurahPickerSheet = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(13.dp), tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "فهرس السور",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = paperTheme.textColor,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }

                                if (surah.number < 114 && nextSurah != null) {
                                    TextButton(
                                        onClick = { viewModel.openNextSurah() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "سورة ${nextSurah.nameArabic}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = paperTheme.borderColor,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(12.dp), tint = paperTheme.borderColor)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }

                            if (totalPages > 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val canGoPrev = pagerState.currentPage > 0 || surah.number > 1
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage > 0) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                } else if (surah.number > 1) {
                                                    viewModel.openPreviousSurah()
                                                }
                                            }
                                        },
                                        enabled = canGoPrev
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = if (pagerState.currentPage > 0) "الصفحة السابقة" else "السورة السابقة",
                                            tint = if (canGoPrev) paperTheme.borderColor else Color.Gray.copy(alpha = 0.4f)
                                        )
                                    }

                                    // Interactive Slider for rapid page jumping
                                    Slider(
                                        value = pagerState.currentPage.toFloat(),
                                        onValueChange = { newIdx ->
                                            coroutineScope.launch {
                                                pagerState.scrollToPage(newIdx.toInt())
                                            }
                                        },
                                        valueRange = 0f..(totalPages - 1).toFloat(),
                                        steps = if (totalPages > 2) totalPages - 2 else 0,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = GoldAccent,
                                            activeTrackColor = paperTheme.borderColor
                                        )
                                    )

                                    val canGoNext = pagerState.currentPage < totalPages - 1 || surah.number < 114
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (pagerState.currentPage < totalPages - 1) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                } else if (surah.number < 114) {
                                                    viewModel.openNextSurah()
                                                }
                                            }
                                        },
                                        enabled = canGoNext
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = if (pagerState.currentPage < totalPages - 1) "الصفحة التالية" else "السورة التالية",
                                            tint = if (canGoNext) paperTheme.borderColor else Color.Gray.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }

                            // Recitation quick start button if audio is not already playing
                            if (!isCurrentSurahVersePlaying) {
                                Button(
                                    onClick = {
                                        val firstAyahOfPage = (currentPageIndex * versesPerPage + 1).coerceAtMost(surah.numberOfAyahs)
                                        viewModel.playSurahWithFollowAlong(firstAyahOfPage)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تلاوة الصفحة بصوت ${selectedReciter.nameArabic}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Direct Quran Navigation Sheet (سورة / جزء / رقم الصفحة)
    if (showDirectNavSheet) {
        val currentMushafPage = QuranMushafMetadata.getPageForSurah(surah.number) + currentPageIndex
        DirectQuranNavigationSheet(
            currentSurahNumber = surah.number,
            currentPageNumber = currentMushafPage,
            onDismiss = { showDirectNavSheet = false },
            onSelectSurah = { chosenSurah ->
                viewModel.openSurah(chosenSurah)
            },
            onSelectJuz = { juz ->
                viewModel.jumpToJuz(juz.number)
            },
            onSelectPage = { pageNum ->
                viewModel.jumpToPageNumber(pageNum)
            }
        )
    }

    // Modal Quran Search Sheet (بحث في السور والآيات)
    if (showSearchSheet) {
        QuranSearchSheet(
            onDismiss = { showSearchSheet = false },
            onNavigateToResult = { chosenSurah, ayahNum ->
                viewModel.openSurah(chosenSurah)
                if (ayahNum != null) {
                    val targetPage = ((ayahNum - 1) / versesPerPage).coerceIn(0, totalPages - 1)
                    coroutineScope.launch {
                        pagerState.scrollToPage(targetPage)
                    }
                }
            }
        )
    }

    // Modal Saved Bookmarks Sheet (العلامات المرجعية المحفوظة)
    if (showBookmarksSheet) {
        val quranBookmarks = remember(favorites) {
            favorites.filter { it.type == "BOOKMARK" }
        }
        QuranBookmarksSheet(
            bookmarks = quranBookmarks,
            onDismiss = { showBookmarksSheet = false },
            onSelectBookmark = { bookmark ->
                val parts = bookmark.reference.split(":")
                if (parts.size >= 2) {
                    val surahNum = parts[0].toIntOrNull()
                    val pageNum = parts[1].toIntOrNull()
                    if (surahNum != null) {
                        val targetSurah = IslamicDataProvider.allSurahs.find { it.number == surahNum }
                        if (targetSurah != null) {
                            if (pageNum != null) {
                                viewModel.openSurahAtPage(targetSurah, (pageNum - 1).coerceAtLeast(0))
                            } else {
                                viewModel.openSurah(targetSurah)
                            }
                        }
                    }
                }
            },
            onDeleteBookmark = { bookmark ->
                viewModel.removeFavorite(bookmark)
            }
        )
    }

    // Modal Quick Surah Picker Sheet
    if (showSurahPickerSheet) {
        QuickSurahPickerSheet(
            currentSurahNumber = surah.number,
            onSelectSurah = { chosenSurah ->
                viewModel.openSurah(chosenSurah)
            },
            onDismiss = { showSurahPickerSheet = false },
            paperTheme = paperTheme
        )
    }

    // Modal Font & Display Settings Sheet
    if (showFontSettingsSheet) {
        QuranFontSettingsSheet(
            fontSizeSp = quranFontSizeSp,
            onFontSizeChange = { quranFontSizeSp = it },
            paperTheme = paperTheme,
            onPaperThemeChange = { paperTheme = it },
            currentRiwayah = currentRiwayah,
            onRiwayahChange = { viewModel.setQuranRiwayah(it) },
            onDismiss = { showFontSettingsSheet = false }
        )
    }

    // ==========================================
    // Interactive Ayah Quick Action Bottom Sheet
    // ==========================================
    selectedAyahForActions?.let { ayah ->
        val rawText = ayah.getText(currentRiwayah)
        val isFavorite = favorites.any { it.arabicText == ayah.textArabic }

        ModalBottomSheet(
            onDismissRequest = { selectedAyahForActions = null },
            containerColor = paperTheme.surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ayah Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سُورَةُ ${surah.nameArabic} - الآيَةُ (${ayah.ayahNumber})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.accentColor
                        )
                    )
                    IconButton(onClick = { selectedAyahForActions = null }) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Verses Text Card inside Sheet
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = paperTheme.backgroundColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "﴿$rawText﴾ ﴿${ayah.ayahNumber}﴾",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.textColor,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedAyahForActions = null
                            viewModel.playAyahAudio(ayah)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = paperTheme.borderColor,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تلاوة الآية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedAyahForActions = null
                            viewModel.playSurahWithFollowAlong(ayah.ayahNumber)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تتبع من هنا", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Action Row: Tafsir, Favorite, Copy, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedAyahForActions = null
                            viewModel.openTafsir(ayah, surah.nameArabic)
                            viewModel.requestAiTafsir()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تفسير وتدبر", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.toggleFavorite(
                                type = "AYAH",
                                title = "سورة ${surah.nameArabic}: ${ayah.ayahNumber}",
                                arabicText = rawText,
                                subtitle = "سورة ${surah.nameArabic}",
                                reference = "آية رقم ${ayah.ayahNumber}"
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFavorite) "محفوظة" else "حفظ", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.copyTextToClipboard("آية قرآنية", "﴿$rawText﴾ [سورة ${surah.nameArabic}: ${ayah.ayahNumber}]")
                            viewModel.showFeedback("تم نسخ الآية الكريمة")
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.shareText("﴿$rawText﴾ [سورة ${surah.nameArabic}: ${ayah.ayahNumber}]", "آية كريمة من تطبيق آصف")
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheet for changing reciter on the fly
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
                    text = "اختر قارئ القرآن الكريم للتتبع والتلاوة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                                if (isCurrentSurahVersePlaying) {
                                    val startAyah = currentActiveAyah ?: 1
                                    viewModel.playSurahWithFollowAlong(startAyah.coerceAtLeast(1))
                                }
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
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = reciter.nameArabic,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = reciter.style,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Authentic Mushaf Page Container with Double Ornamental Borders and Continuous Flowing Text.
 */
@Composable
fun MushafPageContainer(
    paperTheme: MushafPaperTheme,
    juzName: String,
    hizbNumber: Int,
    pageNumber: Int = 1,
    totalPages: Int = 1,
    surah: Surah,
    showSurahHeader: Boolean,
    pageVerses: List<Ayah>,
    currentRiwayah: QuranRiwayah,
    fontSizeSp: Float,
    isCurrentSurahVersePlaying: Boolean,
    currentActiveAyah: Int?,
    selectedAyahNumber: Int?,
    isBookmarked: Boolean = false,
    modifier: Modifier = Modifier,
    onAyahClicked: (Int) -> Unit
) {
    // Outer Decorative Frame
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = paperTheme.backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.2.dp, paperTheme.borderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Inner Fine Margin Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .border(
                        width = 0.8.dp,
                        color = paperTheme.borderColor.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // 1. Mushaf Page Header Bar: Juz (Right) - Surah (Center) - Hizb (Left)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "۞ $juzName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.accentColor
                        )
                    )
                    Text(
                        text = "سُورَةُ ${surah.nameArabic}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = paperTheme.borderColor
                        )
                    )
                    Text(
                        text = "الحزب $hizbNumber ۞",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.accentColor
                        )
                    )
                }

                HorizontalDivider(color = paperTheme.borderColor.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // 2. Surah Cartouche / Heading Plaque (Only on first page)
                if (showSurahHeader) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = paperTheme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, paperTheme.borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "۞  سُورَةُ ${surah.nameArabic}  ۞",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = paperTheme.accentColor,
                                    fontSize = 20.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "آيَاتُهَا ${surah.numberOfAyahs}  •  ${surah.revelationTypeAr}  •  ${currentRiwayah.titleArabic}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    color = paperTheme.textColor.copy(alpha = 0.75f)
                                )
                            )
                        }
                    }

                    // Basmalah (Except Surah At-Tawbah)
                    if (surah.number != 9 && surah.number != 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = paperTheme.borderColor,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // 3. Sacred Continuous Mushaf Text Block
                val annotatedMushafText = remember(
                    pageVerses,
                    currentRiwayah,
                    isCurrentSurahVersePlaying,
                    currentActiveAyah,
                    selectedAyahNumber,
                    fontSizeSp,
                    paperTheme
                ) {
                    buildAnnotatedString {
                        pageVerses.forEach { ayah ->
                            val isAyahActive = isCurrentSurahVersePlaying && currentActiveAyah == ayah.ayahNumber
                            val isAyahSelected = selectedAyahNumber == ayah.ayahNumber
                            val rawText = ayah.getText(currentRiwayah)

                            pushStringAnnotation(tag = "AYAH", annotation = ayah.ayahNumber.toString())

                            // Active or Selected highlight background
                            if (isAyahActive) {
                                pushStyle(
                                    SpanStyle(
                                        background = paperTheme.borderColor.copy(alpha = 0.35f),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            } else if (isAyahSelected) {
                                pushStyle(
                                    SpanStyle(
                                        background = paperTheme.borderColor.copy(alpha = 0.20f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            append(rawText)
                            append(" ")

                            // Gilded Ayah End Symbol ﴿١﴾
                            pushStyle(
                                SpanStyle(
                                    color = paperTheme.borderColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = (fontSizeSp * 0.92f).sp
                                )
                            )
                            append("﴿${ayah.ayahNumber}﴾")
                            pop() // pop style for ayah end

                            if (isAyahActive || isAyahSelected) {
                                pop() // pop style for active/selected
                            }

                            pop() // pop string annotation
                            append("   ")
                        }
                    }
                }

                ClickableText(
                    text = annotatedMushafText,
                    style = TextStyle(
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 2.15f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Justify,
                        color = paperTheme.textColor,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    onClick = { offset ->
                        annotatedMushafText.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val ayahNum = annotation.item.toIntOrNull()
                                if (ayahNum != null) {
                                    onAyahClicked(ayahNum)
                                }
                            }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = paperTheme.borderColor.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Page Number Marker: Juz (Right) - Page Medallion (Center) - Hizb (Left)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "﴿ $juzName ﴾",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = paperTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = paperTheme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor)
                    ) {
                        Text(
                            text = "صفحة $pageNumber من $totalPages",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = paperTheme.borderColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = "﴿ الحزب $hizbNumber ﴾",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = paperTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Real silk bookmark ribbon hanging from the top-end corner
            if (isBookmarked) {
                BookmarkRibbon(
                    isBookmarked = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 18.dp)
                )
            }
        }
    }
}
}

/**
 * Standard Card Item with in-depth Tafsir button for List mode.
 */
@Composable
fun AyahCardItem(
    ayah: Ayah,
    riwayah: QuranRiwayah = QuranRiwayah.HAFS,
    fontSizeSp: Float,
    surahName: String,
    isActive: Boolean,
    isFavorite: Boolean,
    onTafsirClick: () -> Unit,
    onPlayAudio: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val rawText = ayah.getText(riwayah)
    val annotatedAyahText = remember(rawText, ayah.ayahNumber, isActive) {
        buildAnnotatedString {
            append(rawText)
            append("  ")
            pushStyle(SpanStyle(color = GoldBright, fontWeight = FontWeight.ExtraBold))
            append("﴿${ayah.ayahNumber}﴾")
            pop()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) EmeraldDark else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 6.dp else 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 0.8.dp,
            color = if (isActive) GoldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) GoldAccent else MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.ayahNumber.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) EmeraldDark else GoldAccent
                        )
                    )
                }

                Row {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.PauseCircle else Icons.Default.VolumeUp,
                            contentDescription = "استماع للآية",
                            tint = if (isActive) GoldBright else EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ في المفضلة",
                            tint = if (isFavorite) GoldAccent else if (isActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, "نسخ", tint = if (isActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, "مشاركة", tint = if (isActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Uthmani Quran Script
            Text(
                text = annotatedAyahText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 2.0f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onTafsirClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تفسير وتدبر بالذكاء الاصطناعي (Gemini)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

fun getJuzNumber(surahNumber: Int, ayahNumber: Int): Int {
    return when (surahNumber) {
        1 -> 1
        2 -> if (ayahNumber <= 141) 1 else if (ayahNumber <= 252) 2 else 3
        3 -> if (ayahNumber <= 92) 3 else 4
        4 -> if (ayahNumber <= 23) 4 else if (ayahNumber <= 147) 5 else 6
        5 -> if (ayahNumber <= 81) 6 else 7
        6 -> if (ayahNumber <= 110) 7 else 8
        7 -> if (ayahNumber <= 87) 8 else 9
        8 -> if (ayahNumber <= 40) 9 else 10
        9 -> if (ayahNumber <= 92) 10 else 11
        10 -> 11
        11 -> if (ayahNumber <= 5) 11 else 12
        12 -> if (ayahNumber <= 52) 12 else 13
        13 -> 13
        14 -> 13
        15 -> 14
        16 -> 14
        17 -> 15
        18 -> if (ayahNumber <= 74) 15 else 16
        19 -> 16
        20 -> 16
        21 -> 17
        22 -> 17
        23 -> 18
        24 -> 18
        25 -> if (ayahNumber <= 20) 18 else 19
        26 -> 19
        27 -> if (ayahNumber <= 55) 19 else 20
        28 -> 20
        29 -> if (ayahNumber <= 45) 20 else 21
        30 -> 21
        31 -> 21
        32 -> 21
        33 -> if (ayahNumber <= 30) 21 else 22
        34 -> 22
        35 -> 22
        36 -> if (ayahNumber <= 27) 22 else 23
        37 -> 23
        38 -> 23
        39 -> if (ayahNumber <= 31) 23 else 24
        40 -> 24
        41 -> if (ayahNumber <= 46) 24 else 25
        42 -> 25
        43 -> 25
        44 -> 25
        45 -> 25
        in 46..51 -> 26
        in 52..57 -> 27
        in 58..66 -> 28
        in 67..77 -> 29
        in 78..114 -> 30
        else -> 1
    }
}

fun getJuzArabicName(juz: Int): String = when (juz) {
    1 -> "الجزء الأول"
    2 -> "الجزء الثاني"
    3 -> "الجزء الثالث"
    4 -> "الجزء الرابع"
    5 -> "الجزء الخامس"
    6 -> "الجزء السادس"
    7 -> "الجزء السابع"
    8 -> "الجزء الثامن"
    9 -> "الجزء التاسع"
    10 -> "الجزء العاشر"
    11 -> "الجزء الحادي عشر"
    12 -> "الجزء الثاني عشر"
    13 -> "الجزء الثالث عشر"
    14 -> "الجزء الرابع عشر"
    15 -> "الجزء الخامس عشر"
    16 -> "الجزء السادس عشر"
    17 -> "الجزء السابع عشر"
    18 -> "الجزء الثامن عشر"
    19 -> "الجزء التاسع عشر"
    20 -> "الجزء العشرون"
    21 -> "الجزء الحادي والعشرون"
    22 -> "الجزء الثاني والعشرون"
    23 -> "الجزء الثالث والعشرون"
    24 -> "الجزء الرابع والعشرون"
    25 -> "الجزء الخامس والعشرون"
    26 -> "الجزء السادس والعشرون"
    27 -> "الجزء السابع والعشرون"
    28 -> "الجزء الثامن والعشرون"
    29 -> "الجزء التاسع والعشرون"
    30 -> "الجزء الثلاثون"
    else -> "الجزء $juz"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranFontSettingsSheet(
    fontSizeSp: Float,
    onFontSizeChange: (Float) -> Unit,
    paperTheme: MushafPaperTheme,
    onPaperThemeChange: (MushafPaperTheme) -> Unit,
    currentRiwayah: QuranRiwayah,
    onRiwayahChange: (QuranRiwayah) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = paperTheme.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إعدادات خط وقراءة المصحف الشريف",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = paperTheme.textColor
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = paperTheme.borderColor)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Interactive Verse Preview Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = paperTheme.backgroundColor,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, paperTheme.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معاينة الخط المباشرة الحية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = paperTheme.borderColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾\nاقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ ﴿١﴾",
                        style = TextStyle(
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.9f).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = paperTheme.textColor,
                            textDirection = TextDirection.Rtl
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stepper and Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حجم الخط: ${fontSizeSp.toInt()} نقطة",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = paperTheme.textColor
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { if (fontSizeSp > 18f) onFontSizeChange(fontSizeSp - 2f) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = paperTheme.borderColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(Icons.Default.Remove, "تصغير الخط", tint = paperTheme.borderColor)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    FilledTonalIconButton(
                        onClick = { if (fontSizeSp < 42f) onFontSizeChange(fontSizeSp + 2f) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = paperTheme.borderColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(Icons.Default.Add, "تكبير الخط", tint = paperTheme.borderColor)
                    }
                }
            }

            // Slider
            Slider(
                value = fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 18f..42f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = GoldAccent,
                    activeTrackColor = paperTheme.borderColor
                )
            )

            // Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val presets = listOf(
                    "صغير (20)" to 20f,
                    "معتدل (25)" to 25f,
                    "كبير (30)" to 30f,
                    "جلي (36)" to 36f
                )
                presets.forEach { (label, size) ->
                    val isSelected = kotlin.math.abs(fontSizeSp - size) < 1f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFontSizeChange(size) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = paperTheme.borderColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Paper Theme Choices
            Text(
                text = "مظهر ورق وخلفية المصحف:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = paperTheme.textColor
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MushafPaperTheme.values().forEach { theme ->
                    val isSelected = paperTheme == theme
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPaperThemeChange(theme) },
                        shape = RoundedCornerShape(12.dp),
                        color = theme.backgroundColor,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) GoldAccent else theme.borderColor.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(theme.iconName, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.title.replace("المصحف ", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = theme.textColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Quick bottom sheet index for jumping directly to any Surah in the Holy Quran
 * without having to exit the reader screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSurahPickerSheet(
    currentSurahNumber: Int,
    onSelectSurah: (Surah) -> Unit,
    onDismiss: () -> Unit,
    paperTheme: MushafPaperTheme
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }

    val filteredSurahs = remember(searchQuery, selectedFilter) {
        val query = searchQuery.trim()
        IslamicDataProvider.allSurahs.filter { surah ->
            val matchesQuery = if (query.isBlank()) true else {
                surah.nameArabic.contains(query) ||
                        surah.englishName.contains(query, ignoreCase = true) ||
                        surah.number.toString() == query
            }
            val matchesFilter = when (selectedFilter) {
                "مكية" -> surah.revelationType == "Meccan" || surah.revelationTypeAr == "مكية"
                "مدنية" -> surah.revelationType == "Medinan" || surah.revelationTypeAr == "مدنية"
                "جزء عم" -> surah.number in 78..114
                "جزء تبارك" -> surah.number in 67..77
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = paperTheme.surfaceColor,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = paperTheme.borderColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فهرس سور القرآن الكريم",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.textColor
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = paperTheme.borderColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${filteredSurahs.size} سورة",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.borderColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "ابحث باسم السورة أو رقمها (مثال: الكهف أو 18)...",
                        fontSize = 13.sp,
                        color = paperTheme.textColor.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = paperTheme.borderColor)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "مسح", tint = paperTheme.borderColor)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = paperTheme.borderColor.copy(alpha = 0.4f),
                    focusedTextColor = paperTheme.textColor,
                    unfocusedTextColor = paperTheme.textColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("الكل", "مكية", "مدنية", "جزء عم", "جزء تبارك")
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = paperTheme.borderColor,
                            selectedLabelColor = Color.White,
                            containerColor = paperTheme.surfaceColor,
                            labelColor = paperTheme.textColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) paperTheme.borderColor else paperTheme.borderColor.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Surahs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { s ->
                    val isCurrent = s.number == currentSurahNumber
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectSurah(s)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) paperTheme.borderColor.copy(alpha = 0.15f) else paperTheme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) GoldAccent else paperTheme.borderColor.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Surah Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) GoldAccent else paperTheme.borderColor.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${s.number}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) EmeraldDark else paperTheme.borderColor,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "سُورَةُ ${s.nameArabic}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) GoldAccent else paperTheme.textColor
                                            )
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GoldAccent.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "تقرأ الآن",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = paperTheme.textColor
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${s.revelationTypeAr} • ${s.numberOfAyahs} آية • ${s.englishName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = paperTheme.textColor.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isCurrent) GoldAccent else paperTheme.borderColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * End of Surah navigation section: congratulations, next surah direct button,
 * previous surah button, and surah index jump.
 */
@Composable
fun SurahEndNavigationSection(
    surah: Surah,
    onPrevSurah: () -> Unit,
    onNextSurah: () -> Unit,
    onOpenIndex: () -> Unit,
    paperTheme: MushafPaperTheme,
    modifier: Modifier = Modifier
) {
    val prevSurah = remember(surah.number) {
        if (surah.number > 1) IslamicDataProvider.allSurahs.find { it.number == surah.number - 1 } else null
    }
    val nextSurah = remember(surah.number) {
        if (surah.number < 114) IslamicDataProvider.allSurahs.find { it.number == surah.number + 1 } else null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = paperTheme.surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, paperTheme.borderColor.copy(alpha = 0.45f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Noble Islamic Calligraphy Header
            Text(
                text = "﴿ صَدَقَ اللَّهُ الْعَظِيمُ ﴾",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    fontSize = 17.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "تمت بحمد الله وتوفيقه سورة ${surah.nameArabic}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = paperTheme.textColor.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Next Surah Button (if available)
            if (nextSurah != null) {
                Button(
                    onClick = onNextSurah,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الانتقال إلى سورة ${nextSurah.nameArabic}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${nextSurah.numberOfAyahs} آية)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row with Previous Surah and Index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (prevSurah != null) {
                    OutlinedButton(
                        onClick = onPrevSurah,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, paperTheme.borderColor.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = paperTheme.borderColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سورة ${prevSurah.nameArabic}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = paperTheme.textColor,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenIndex,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "فهرس السور",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = paperTheme.textColor,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
