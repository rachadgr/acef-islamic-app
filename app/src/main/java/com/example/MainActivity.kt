package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.example.ui.AcefViewModel
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.AcefBottomNavigation
import com.example.ui.components.AcefTopBar
import com.example.ui.components.AudioPlayerMiniBar
import com.example.ui.components.AyahAiTafsirSheet
import com.example.ui.components.HadithAiExplanationSheet
import com.example.ui.screens.*
import com.example.ui.theme.AcefTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.geminiBackground
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            // KSP / Robolectric standalone workaround:
            // prevents "Cannot invoke Application.getService(...) because
            // ApplicationManager.getApplication() is null" during class loading.
            try {
                System.setProperty("idea.use.native.fs.for.win", "false")
                System.setProperty("java.awt.headless", "true")
            } catch (_: Throwable) {}
        }
    }

    private val viewModel: AcefViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global safety net: always log unexpected exceptions with a clear tag so
        // crash reports are actionable, then delegate to the platform handler.
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                android.util.Log.e("AcefCrash", "Uncaught exception on ${thread.name}", throwable)
            } catch (_: Throwable) {}
            previousHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()

        val routeFromIntent = intent?.getStringExtra("route")
        if (!routeFromIntent.isNullOrBlank()) {
            viewModel.navigateTo(routeFromIntent)
        }

        setContent {
            val settings by viewModel.appSettings.collectAsState()

            LaunchedEffect(settings.appLanguage) {
                val targetTag = when (settings.appLanguage) {
                    "ar" -> "ar"
                    "en" -> "en"
                    "fr" -> "fr"
                    else -> ""
                }
                val targetLocales = if (targetTag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(targetTag)
                }
                if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
                    AppCompatDelegate.setApplicationLocales(targetLocales)
                }
            }

            val currentLocale = remember(settings.appLanguage) {
                when (settings.appLanguage) {
                    "ar" -> Locale("ar")
                    "en" -> Locale("en")
                    "fr" -> Locale("fr")
                    else -> Locale.getDefault()
                }
            }

            val localizedContext = remember(currentLocale) {
                val config = Configuration(resources.configuration)
                config.setLocale(currentLocale)
                config.setLayoutDirection(currentLocale)
                createConfigurationContext(config)
            }

            val isRtl = when (settings.appLanguage) {
                "ar" -> true
                "en", "fr" -> false
                else -> {
                    TextUtils.getLayoutDirectionFromLocale(currentLocale) == View.LAYOUT_DIRECTION_RTL
                }
            }
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            AcefTheme(
                themeMode = settings.themeMode,
                colorTheme = settings.colorTheme
            ) {
                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedContext.resources.configuration,
                    LocalLayoutDirection provides layoutDirection
                ) {
                    AcefMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcefMainApp(viewModel: AcefViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val currentSurahVerses by viewModel.currentSurahVerses.collectAsState()
    val selectedAyahForTafsir by viewModel.selectedAyahForTafsir.collectAsState()
    val selectedAyahSurahName by viewModel.selectedAyahSurahName.collectAsState()
    val aiTafsirText by viewModel.aiTafsirText.collectAsState()
    val isAiTafsirLoading by viewModel.isAiTafsirLoading.collectAsState()
    val selectedHadithForExplanation by viewModel.selectedHadithForExplanation.collectAsState()
    val aiHadithExplanationText by viewModel.aiHadithExplanationText.collectAsState()
    val isAiHadithLoading by viewModel.isAiHadithLoading.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Global BackHandler
    BackHandler(enabled = selectedSurah != null || currentRoute != "home") {
        if (selectedSurah != null) {
            viewModel.closeSurahDetail()
        } else if (currentRoute != "home") {
            viewModel.navigateTo("home")
        }
    }

    // Permission launchers
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            viewModel.refreshGpsLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.refreshGpsLocation()
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Dynamic Top Bar Titles according to active locale
    val topBarTitle: String
    val topBarSubtitle: String?
    val showBackButton: Boolean

    if (selectedSurah != null) {
        val s = selectedSurah!!
        val surahTitlePrefix = if (appSettings.appLanguage == "en") "Surah " else if (appSettings.appLanguage == "fr") "Sourate " else "سورة "
        val ayahUnit = if (appSettings.appLanguage == "en") "verses" else if (appSettings.appLanguage == "fr") "versets" else "آية"
        topBarTitle = "$surahTitlePrefix${s.nameArabic}"
        topBarSubtitle = "${s.revelationTypeAr} • ${s.numberOfAyahs} $ayahUnit"
        showBackButton = true
    } else {
        when (currentRoute) {
            "home" -> {
                topBarTitle = stringResource(R.string.app_brand_arabic)
                topBarSubtitle = stringResource(R.string.app_tagline)
                showBackButton = false
            }
            "prayer_times" -> {
                topBarTitle = stringResource(R.string.prayer_screen_title)
                topBarSubtitle = stringResource(R.string.prayer_screen_subtitle)
                showBackButton = false
            }
            "quran" -> {
                topBarTitle = stringResource(R.string.quran_screen_title)
                topBarSubtitle = stringResource(R.string.quran_screen_subtitle)
                showBackButton = false
            }
            "azkar" -> {
                topBarTitle = stringResource(R.string.azkar_screen_title)
                topBarSubtitle = stringResource(R.string.azkar_screen_subtitle)
                showBackButton = false
            }
            "hadith" -> {
                topBarTitle = stringResource(R.string.hadith_screen_title)
                topBarSubtitle = stringResource(R.string.hadith_screen_subtitle)
                showBackButton = true
            }
            "ai_scholar" -> {
                topBarTitle = stringResource(R.string.scholar_screen_title)
                topBarSubtitle = stringResource(R.string.scholar_screen_subtitle)
                showBackButton = false
            }
            "qibla" -> {
                topBarTitle = stringResource(R.string.qibla_screen_title)
                topBarSubtitle = stringResource(R.string.qibla_screen_subtitle)
                showBackButton = true
            }
            "tasbih" -> {
                topBarTitle = stringResource(R.string.tasbih_screen_title)
                topBarSubtitle = stringResource(R.string.tasbih_screen_subtitle)
                showBackButton = true
            }
            "settings" -> {
                topBarTitle = stringResource(R.string.settings_title)
                topBarSubtitle = stringResource(R.string.settings_subtitle)
                showBackButton = true
            }
            else -> {
                topBarTitle = stringResource(R.string.app_name)
                topBarSubtitle = null
                showBackButton = false
            }
        }
    }

    // Language Selection Modal Dialog
    if (showLanguageDialog) {
        val langOptions = listOf(
            Triple("SYSTEM", stringResource(R.string.settings_lang_system), "🌐 System"),
            Triple("ar", "العربية (Arabic)", "🇸🇦 العربية"),
            Triple("en", "English", "🇬🇧 English"),
            Triple("fr", "Français", "🇫🇷 Français")
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.language_switch_dialog_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    langOptions.forEach { (code, title, flag) ->
                        val isSelected = appSettings.appLanguage.equals(code, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setAppLanguage(code)
                                    showLanguageDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GoldAccent) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, "Selected", tint = GoldAccent, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (appSettings.themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            if (selectedSurah == null) {
                AcefTopBar(
                    title = topBarTitle,
                    subtitle = topBarSubtitle,
                    navigationIcon = if (showBackButton) Icons.Default.ArrowBack else null,
                    onNavigationClick = {
                        if (currentRoute != "home") {
                            viewModel.navigateTo("home")
                        }
                    },
                    actions = {
                        // Quick Language Switcher Icon
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = stringResource(R.string.settings_section_language),
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = stringResource(R.string.app_about_title),
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        if (currentRoute != "settings") {
                            IconButton(onClick = { viewModel.navigateTo("settings") }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.settings_title),
                                    tint = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selectedSurah == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    AudioPlayerMiniBar(audioPlayer = viewModel.audioPlayer)
                    AcefBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route -> viewModel.navigateTo(route) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(geminiBackground(appSettings.themeMode))
                .padding(innerPadding)
        ) {
            when {
                selectedSurah != null -> {
                    SurahDetailScreen(
                        surah = selectedSurah!!,
                        verses = currentSurahVerses,
                        viewModel = viewModel,
                        onBack = { viewModel.closeSurahDetail() },
                        onGoHome = {
                            viewModel.closeSurahDetail()
                            viewModel.navigateTo("home")
                        }
                    )
                }
                currentRoute == "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> viewModel.navigateTo(route) }
                )
                currentRoute == "prayer_times" -> PrayerTimesScreen(viewModel = viewModel)
                currentRoute == "quran" -> QuranScreen(
                    viewModel = viewModel,
                    onOpenSurah = { surah -> viewModel.openSurah(surah) }
                )
                currentRoute == "azkar" -> AzkarScreen(viewModel = viewModel)
                currentRoute == "hadith" -> HadithScreen(viewModel = viewModel)
                currentRoute == "ai_scholar" -> AiScholarScreen(viewModel = viewModel)
                currentRoute == "qibla" -> QiblaScreen(viewModel = viewModel)
                currentRoute == "tasbih" -> TasbihScreen(viewModel = viewModel)
                currentRoute == "settings" -> SettingsScreen(viewModel = viewModel)
                else -> HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> viewModel.navigateTo(route) }
                )
            }

            if (selectedAyahForTafsir != null) {
                AyahAiTafsirSheet(
                    ayah = selectedAyahForTafsir!!,
                    surahName = selectedAyahSurahName ?: "",
                    aiText = aiTafsirText,
                    isLoading = isAiTafsirLoading,
                    onRefresh = { viewModel.requestAiTafsir(forceRefresh = true) },
                    onDismiss = { viewModel.closeTafsir() },
                    onCopy = { text -> viewModel.copyTextToClipboard("Tafsir", text) },
                    onShare = { text -> viewModel.shareText(text, "تفسير الآية") }
                )
            }

            if (selectedHadithForExplanation != null) {
                HadithAiExplanationSheet(
                    hadith = selectedHadithForExplanation!!,
                    aiText = aiHadithExplanationText,
                    isLoading = isAiHadithLoading,
                    onRefresh = { viewModel.requestAiHadithExplanation(forceRefresh = true) },
                    onDismiss = { viewModel.closeHadithExplanation() },
                    onCopy = { text -> viewModel.copyTextToClipboard("Hadith", text) },
                    onShare = { text -> viewModel.shareText(text, "شرح الحديث") }
                )
            }

            if (showAboutDialog) {
                val shareTextStr = stringResource(R.string.share_app_text)
                val shareTitleStr = stringResource(R.string.share_app_title)
                AboutAppDialog(
                    onDismiss = { showAboutDialog = false },
                    onShare = { viewModel.shareText(shareTextStr, shareTitleStr) }
                )
            }
        }
    }
}
