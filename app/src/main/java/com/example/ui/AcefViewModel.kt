package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AcefDatabase
import com.example.data.local.AcefRepository
import com.example.data.local.AiExplanationEntity
import com.example.data.local.AppSettingsEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.IslamicDataProvider
import com.example.data.local.ReadingProgressEntity
import com.example.data.model.Ayah
import com.example.data.model.City
import com.example.data.model.Dhikr
import com.example.data.model.GpsLocationData
import com.example.data.model.Hadith
import com.example.data.model.HourlyAyahTafsir
import com.example.data.model.HourlyHadithExplanation
import com.example.data.model.PrayerTime
import com.example.data.model.QuranMushafMetadata
import com.example.data.model.QuranRiwayah
import com.example.data.model.Surah
import com.example.service.AdhanPlaybackService
import com.example.service.FloatingAzkarService
import com.example.util.AdhanSound
import com.example.util.AudioPlayerHelper
import com.example.util.IqamaSound
import com.example.util.GeminiAiTafsirService
import com.example.util.GpsLocationHelper
import com.example.util.NotificationHelper
import com.example.util.NotificationReceiver
import com.example.util.PrayerCalculator
import com.example.util.QiblaCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class AcefViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AcefRepository(AcefDatabase.getDatabase(application))
    val audioPlayer = AudioPlayerHelper(application)
    val gpsHelper = GpsLocationHelper(application)

    // Current Navigation Destination
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // Settings
    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettingsEntity())

    // Favorites
    val favorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reading progress
    val readingProgress: StateFlow<ReadingProgressEntity?> = repository.readingProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current City
    private val _selectedCity = MutableStateFlow(IslamicDataProvider.defaultCities.first())
    val selectedCity: StateFlow<City> = _selectedCity.asStateFlow()

    // GPS Status & Data
    val gpsState: StateFlow<GpsLocationHelper.GpsState> = gpsHelper.gpsState
    val lastGpsLocation: StateFlow<GpsLocationData?> = gpsHelper.lastGpsLocation

    // Prayer Times & Countdown
    private val _prayerTimes = MutableStateFlow<List<PrayerTime>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerTime>> = _prayerTimes.asStateFlow()

    private val _countdownText = MutableStateFlow("00:00:00")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _iqamaCountdownText = MutableStateFlow("00:00:00")
    val iqamaCountdownText: StateFlow<String> = _iqamaCountdownText.asStateFlow()

    // Hourly Explained Verse for Home Screen (تتغير تلقائياً كل ساعة)
    private val _hourlyVerse = MutableStateFlow<HourlyAyahTafsir>(IslamicDataProvider.getHourlyVerse())
    val hourlyVerse: StateFlow<HourlyAyahTafsir> = _hourlyVerse.asStateFlow()
    private var currentHourTracker = -1

    // Hourly Explained Hadith (تحديث تفسير الأحاديث النبوية تلقائياً كل ساعة)
    private val _hourlyHadith = MutableStateFlow<HourlyHadithExplanation>(IslamicDataProvider.getHourlyHadith())
    val hourlyHadith: StateFlow<HourlyHadithExplanation> = _hourlyHadith.asStateFlow()

    // Quran Riwayah State (حفص أو ورش)
    val currentRiwayah: StateFlow<QuranRiwayah> = appSettings
        .map { settings ->
            try {
                QuranRiwayah.valueOf(settings.quranRiwayah)
            } catch (e: Throwable) {
                QuranRiwayah.HAFS
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, QuranRiwayah.HAFS)

    // Quran State
    private val _quranSearchQuery = MutableStateFlow("")
    val quranSearchQuery: StateFlow<String> = _quranSearchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _currentSurahVerses = MutableStateFlow<List<Ayah>>(emptyList())
    val currentSurahVerses: StateFlow<List<Ayah>> = _currentSurahVerses.asStateFlow()

    private val _requestedQuranPage = MutableStateFlow<Int?>(null)
    val requestedQuranPage: StateFlow<Int?> = _requestedQuranPage.asStateFlow()

    // Tafsir Dialog & AI State
    private val _selectedAyahForTafsir = MutableStateFlow<Ayah?>(null)
    val selectedAyahForTafsir: StateFlow<Ayah?> = _selectedAyahForTafsir.asStateFlow()

    private val _selectedAyahSurahName = MutableStateFlow<String>("الفاتحة")
    val selectedAyahSurahName: StateFlow<String> = _selectedAyahSurahName.asStateFlow()

    private val _aiTafsirText = MutableStateFlow<String?>(null)
    val aiTafsirText: StateFlow<String?> = _aiTafsirText.asStateFlow()

    private val _isAiTafsirLoading = MutableStateFlow(false)
    val isAiTafsirLoading: StateFlow<Boolean> = _isAiTafsirLoading.asStateFlow()

    // Hadith State
    private val _selectedHadithCategory = MutableStateFlow("الكل")
    val selectedHadithCategory: StateFlow<String> = _selectedHadithCategory.asStateFlow()

    private val _selectedHadithForExplanation = MutableStateFlow<Hadith?>(null)
    val selectedHadithForExplanation: StateFlow<Hadith?> = _selectedHadithForExplanation.asStateFlow()

    private val _aiHadithExplanationText = MutableStateFlow<String?>(null)
    val aiHadithExplanationText: StateFlow<String?> = _aiHadithExplanationText.asStateFlow()

    private val _isAiHadithLoading = MutableStateFlow(false)
    val isAiHadithLoading: StateFlow<Boolean> = _isAiHadithLoading.asStateFlow()

    // Open AI Islamic Scholar State
    private val _aiScholarQuestion = MutableStateFlow("")
    val aiScholarQuestion: StateFlow<String> = _aiScholarQuestion.asStateFlow()

    private val _aiScholarAnswer = MutableStateFlow<String?>(null)
    val aiScholarAnswer: StateFlow<String?> = _aiScholarAnswer.asStateFlow()

    private val _isAiScholarLoading = MutableStateFlow(false)
    val isAiScholarLoading: StateFlow<Boolean> = _isAiScholarLoading.asStateFlow()

    // Floating Azkar Bubble state
    private val _isFloatingAzkarActive = MutableStateFlow(false)
    val isFloatingAzkarActive: StateFlow<Boolean> = _isFloatingAzkarActive.asStateFlow()

    // Azkar State
    private val _selectedAzkarCategory = MutableStateFlow("أذكار الصباح")
    val selectedAzkarCategory: StateFlow<String> = _selectedAzkarCategory.asStateFlow()

    private val _azkarCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val azkarCounts: StateFlow<Map<Int, Int>> = _azkarCounts.asStateFlow()

    // Tasbih State
    private val _currentTasbihText = MutableStateFlow("سُبْحَانَ اللَّهِ")
    val currentTasbihText: StateFlow<String> = _currentTasbihText.asStateFlow()

    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _totalTasbihCount = MutableStateFlow(0)
    val totalTasbihCount: StateFlow<Int> = _totalTasbihCount.asStateFlow()

    // Qibla State
    private val _qiblaBearing = MutableStateFlow(0.0)
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    private val _distanceToKaaba = MutableStateFlow(0)
    val distanceToKaaba: StateFlow<Int> = _distanceToKaaba.asStateFlow()

    // Unified Search
    private val _unifiedSearchQuery = MutableStateFlow("")
    val unifiedSearchQuery: StateFlow<String> = _unifiedSearchQuery.asStateFlow()

    // Toast/Feedback state
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private var prayerUpdateJob: Job? = null

    init {
        // Automatically proceed to next Surah if continuous recitation is on
        audioPlayer.onSurahRecitationCompleted = { finishedSurahNum ->
            if (finishedSurahNum < 114) {
                openNextSurah(autoPlayAudio = true)
            }
        }

        // Create notification channels on start
        NotificationHelper.createNotificationChannels(application)

        // Load initial settings and trigger calculations & scheduling
        viewModelScope.launch {
            appSettings.collect { settings ->
                GeminiAiTafsirService.userCustomApiKey = settings.customGeminiApiKey
                updateCalculationsForCurrentSettings(settings)
                _isFloatingAzkarActive.value = FloatingAzkarService.isRunning || settings.floatingAzkarEnabled
                NotificationReceiver.scheduleAllNotifications(
                    context = getApplication(),
                    enablePrayer = settings.prayerNotifications,
                    enableMorningAzkar = settings.morningAzkarNotification,
                    enableEveningAzkar = settings.eveningAzkarNotification,
                    enableQuranReminder = settings.quranReminderNotification
                )
                // Sync selected Adhan and Iqama sound from persistent settings
                val adhan = AudioPlayerHelper.availableAdhanSounds.find { it.id == settings.selectedAdhanId }
                if (adhan != null) {
                    audioPlayer.selectAdhanSound(adhan)
                }
                val iqama = AudioPlayerHelper.availableIqamaSounds.find { it.id == settings.selectedIqamaId }
                if (iqama != null) {
                    audioPlayer.selectIqamaSound(iqama)
                }
                // Sync selected Quran Reciter
                val reciter = AudioPlayerHelper.availableReciters.find { it.id == settings.selectedQuranReciterId }
                if (reciter != null && reciter.id != audioPlayer.selectedReciter.value.id) {
                    audioPlayer.setReciterById(reciter.id)
                }
            }
        }

        startPrayerTicker()
    }

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    private fun startPrayerTicker() {
        prayerUpdateJob?.cancel()
        prayerUpdateJob = viewModelScope.launch {
            while (isActive) {
                calculateTodayPrayers()
                checkMidnightRollover()
                checkHourlyVerseUpdate()
                delay(1000)
            }
        }
    }

    private fun checkMidnightRollover() {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (todayStr != lastScheduledPrayerDate) {
            rescheduleAlarmsIfNeeded(force = true)
        }
    }

    private fun checkHourlyVerseUpdate() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour != currentHourTracker) {
            currentHourTracker = currentHour
            _hourlyVerse.value = IslamicDataProvider.getHourlyVerse()
            _hourlyHadith.value = IslamicDataProvider.getHourlyHadith()
        }
    }

    fun refreshHourlyVerse(hourOffset: Int = 0) {
        _hourlyVerse.value = IslamicDataProvider.getHourlyVerse(hourOffset)
    }

    fun nextHourlyVerse() {
        val current = _hourlyVerse.value
        val currentIndex = IslamicDataProvider.hourlyExplainedVerses.indexOfFirst {
            it.surahNumber == current.surahNumber && it.ayahNumber == current.ayahNumber
        }.let { if (it >= 0) it else 0 }
        val nextIndex = (currentIndex + 1) % IslamicDataProvider.hourlyExplainedVerses.size
        _hourlyVerse.value = IslamicDataProvider.hourlyExplainedVerses[nextIndex]
    }

    fun prevHourlyVerse() {
        val current = _hourlyVerse.value
        val currentIndex = IslamicDataProvider.hourlyExplainedVerses.indexOfFirst {
            it.surahNumber == current.surahNumber && it.ayahNumber == current.ayahNumber
        }.let { if (it >= 0) it else 0 }
        val prevIndex = Math.floorMod(currentIndex - 1, IslamicDataProvider.hourlyExplainedVerses.size)
        _hourlyVerse.value = IslamicDataProvider.hourlyExplainedVerses[prevIndex]
    }

    // Hourly Hadith Navigation (تحديث تفسير وشرح الأحاديث كل ساعة)
    fun refreshHourlyHadith(hourOffset: Int = 0) {
        _hourlyHadith.value = IslamicDataProvider.getHourlyHadith(hourOffset)
    }

    fun nextHourlyHadith() {
        val current = _hourlyHadith.value
        val currentIndex = IslamicDataProvider.hourlyExplainedHadiths.indexOfFirst { it.id == current.id }
            .let { if (it >= 0) it else 0 }
        val nextIndex = (currentIndex + 1) % IslamicDataProvider.hourlyExplainedHadiths.size
        _hourlyHadith.value = IslamicDataProvider.hourlyExplainedHadiths[nextIndex]
    }

    fun prevHourlyHadith() {
        val current = _hourlyHadith.value
        val currentIndex = IslamicDataProvider.hourlyExplainedHadiths.indexOfFirst { it.id == current.id }
            .let { if (it >= 0) it else 0 }
        val prevIndex = Math.floorMod(currentIndex - 1, IslamicDataProvider.hourlyExplainedHadiths.size)
        _hourlyHadith.value = IslamicDataProvider.hourlyExplainedHadiths[prevIndex]
    }

    // Quran Riwayah Switcher (حفص وورش)
    fun setQuranRiwayah(riwayah: QuranRiwayah) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(quranRiwayah = riwayah.name)
            repository.saveSettings(updated)
            _feedbackMessage.value = "تم اعتماد ${riwayah.titleArabic}"
        }
    }

    fun toggleAutoPlayAdhan(enabled: Boolean) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(autoPlayAdhan = enabled)
            repository.saveSettings(updated)
            _feedbackMessage.value = if (enabled) "تم تفعيل تشغيل الأذان تلقائياً" else "تم إيقاف تشغيل الأذان تلقائياً"
        }
    }

    fun toggleAutoPlayIqama(enabled: Boolean) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(autoPlayIqama = enabled)
            repository.saveSettings(updated)
            _feedbackMessage.value = if (enabled) "تم تفعيل تنبيه الإقامة تلقائياً" else "تم إيقاف تنبيه الإقامة تلقائياً"
        }
    }

    fun togglePlayAdhanOnIqama(enabled: Boolean) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(playAdhanOnIqama = enabled)
            repository.saveSettings(updated)
            _feedbackMessage.value = if (enabled) "سيتم تشغيل الأذان عند الإقامة" else "تم إلغاء تشغيل أذان الإقامة (اعتماد تكبيرات الإقامة فقط)"
        }
    }

    /**
     * Request GPS Location and automatically update coordinates, city, and prayers.
     */
    fun refreshGpsLocation() {
        gpsHelper.requestGpsLocation { locData ->
            if (locData != null) {
                viewModelScope.launch {
                    val current = appSettings.value
                    val updated = current.copy(
                        isGpsEnabled = true,
                        cityName = locData.cityNameAr.ifBlank { "موقعي الحالي عبر GPS" },
                        latitude = locData.latitude,
                        longitude = locData.longitude,
                        timezone = locData.timezone,
                        gpsAccuracyMeters = locData.accuracyMeters
                    )
                    repository.saveSettings(updated)
                    updateCalculationsForCurrentSettings(updated)
                    _feedbackMessage.value = "تم تحديث الموقع بدقة: ${updated.cityName}"
                    vibrateDevice(50)
                }
            } else {
                when (gpsState.value) {
                    is GpsLocationHelper.GpsState.PermissionRequired -> {
                        _feedbackMessage.value = "يلزم منح إذن الوصول إلى الموقع GPS"
                    }
                    is GpsLocationHelper.GpsState.GpsDisabled -> {
                        _feedbackMessage.value = "يرجى تفعيل خدمة GPS في إعدادات الهاتف"
                    }
                    is GpsLocationHelper.GpsState.Error -> {
                        val errMsg = (gpsState.value as GpsLocationHelper.GpsState.Error).message
                        _feedbackMessage.value = errMsg
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleUseGps(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value
            if (enabled) {
                refreshGpsLocation()
            } else {
                val city = _selectedCity.value
                val updated = current.copy(
                    isGpsEnabled = false,
                    cityName = city.nameAr,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezone = city.timezone,
                    gpsAccuracyMeters = 0f
                )
                repository.saveSettings(updated)
                updateCalculationsForCurrentSettings(updated)
                _feedbackMessage.value = "تم اعتماد مدينة: ${city.nameAr}"
            }
        }
    }

    private fun updateCalculationsForCurrentSettings(settings: AppSettingsEntity) {
        val lat = settings.latitude
        val lng = settings.longitude
        _qiblaBearing.value = QiblaCalculator.calculateQiblaBearing(lat, lng)
        _distanceToKaaba.value = QiblaCalculator.calculateDistanceToKaabaKm(lat, lng)
        calculateTodayPrayers()
        rescheduleAlarmsIfNeeded(force = true)
    }

    private var lastScheduledPrayerDate: String = ""
    private var lastScheduledLocationKey: String = ""

    fun rescheduleAlarmsIfNeeded(force: Boolean = false) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val settings = appSettings.value
        val locationKey = "${settings.latitude}_${settings.longitude}_${settings.calculationMethod}_${settings.madhhab}_${settings.iqamaDelayMinutes}_${settings.prayerNotifications}_${settings.iqamaNotifications}_${settings.autoPlayAdhan}_${settings.autoPlayIqama}"

        if (!force && todayStr == lastScheduledPrayerDate && locationKey == lastScheduledLocationKey) {
            return
        }

        lastScheduledPrayerDate = todayStr
        lastScheduledLocationKey = locationKey

        val times = _prayerTimes.value
        if (times.isNotEmpty()) {
            NotificationReceiver.scheduleAllDailyPrayerAlarms(
                context = getApplication(),
                prayers = times,
                prayerNotificationsEnabled = settings.prayerNotifications,
                iqamaNotificationsEnabled = settings.iqamaNotifications,
                autoPlayAdhan = settings.autoPlayAdhan,
                autoPlayIqama = settings.autoPlayIqama
            )
        }
    }

    fun calculateTodayPrayers() {
        val settings = appSettings.value
        val lat = settings.latitude
        val lng = settings.longitude
        val tz = settings.timezone
        val method = try {
            PrayerCalculator.Method.valueOf(settings.calculationMethod)
        } catch (_: Exception) {
            PrayerCalculator.Method.ALGERIA
        }
        val juristic = if (settings.madhhab == "HANAFI") {
            PrayerCalculator.Juristic.HANAFI
        } else {
            PrayerCalculator.Juristic.SHAFI
        }

        val isArabicLocale = settings.appLanguage == "ar" || (settings.appLanguage == "SYSTEM" && java.util.Locale.getDefault().language == "ar")
        val times = PrayerCalculator.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            timezone = tz,
            date = Calendar.getInstance(),
            method = method,
            juristic = juristic,
            defaultIqamaOffsetMinutes = settings.iqamaDelayMinutes,
            is24Hour = settings.is24HourFormat,
            isArabicLocale = isArabicLocale
        )
        _prayerTimes.value = times

        val next = times.find { it.isNext }
        if (next != null) {
            _countdownText.value = PrayerCalculator.getCountdownTo(next.timestampMillis)
        } else {
            _countdownText.value = "00:00:00"
        }

        // Check if there is an upcoming Iqama
        val iqamaPrayer = times.find { it.isIqamaNext }
        if (iqamaPrayer != null && iqamaPrayer.iqamaTimestampMillis > System.currentTimeMillis()) {
            _iqamaCountdownText.value = PrayerCalculator.getCountdownTo(iqamaPrayer.iqamaTimestampMillis)
        } else {
            _iqamaCountdownText.value = "--:--:--"
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(appLanguage = lang)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            _feedbackMessage.value = when (lang) {
                "en" -> "Language changed to English"
                "fr" -> "Langue modifiée en Français"
                "ar" -> "تم تغيير لغة التطبيق إلى العربية"
                else -> "تم ضبط لغة التطبيق تلقائياً"
            }
        }
    }

    fun setTimeFormat24Hour(is24Hour: Boolean) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(is24HourFormat = is24Hour)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            val lang = updated.appLanguage
            _feedbackMessage.value = if (is24Hour) {
                when (lang) {
                    "en" -> "24-Hour time format activated (00:00 - 23:59)"
                    "fr" -> "Format 24 heures activé (00:00 - 23:59)"
                    else -> "تم تفعيل نظام 24 ساعة (00:00 - 23:59)"
                }
            } else {
                when (lang) {
                    "en" -> "12-Hour time format activated (AM / PM)"
                    "fr" -> "Format 12 heures activé (AM / PM)"
                    else -> "تم تفعيل نظام 12 ساعة (ص / م)"
                }
            }
        }
    }

    fun selectCity(city: City) {
        _selectedCity.value = city
        viewModelScope.launch {
            repository.saveSettings(
                appSettings.value.copy(
                    isGpsEnabled = false,
                    cityName = city.nameAr,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezone = city.timezone,
                    gpsAccuracyMeters = 0f
                )
            )
            updateCalculationsForCurrentSettings(
                appSettings.value.copy(
                    isGpsEnabled = false,
                    cityName = city.nameAr,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezone = city.timezone
                )
            )
            _feedbackMessage.value = "تم ضبط الموقع: ${city.nameAr}"
        }
    }

    // Auto Adhan & Iqama settings
    fun toggleAutoPlayAdhan() {
        viewModelScope.launch {
            val current = appSettings.value.autoPlayAdhan
            val updated = appSettings.value.copy(autoPlayAdhan = !current)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            NotificationReceiver.rescheduleUpcomingPrayers(getApplication())
            _feedbackMessage.value = if (!current) "تم تفعيل تشغيل الأذان تلقائياً عند دخول الوقت" else "تم إيقاف تشغيل الأذان التلقائي"
        }
    }

    fun toggleAutoPlayIqama() {
        viewModelScope.launch {
            val current = appSettings.value.autoPlayIqama
            val updated = appSettings.value.copy(autoPlayIqama = !current)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            NotificationReceiver.rescheduleUpcomingPrayers(getApplication())
            _feedbackMessage.value = if (!current) "تم تفعيل تشغيل تنبيه/تكبيرات الإقامة تلقائياً" else "تم إيقاف تشغيل الإقامة التلقائي"
        }
    }

    fun togglePlayAdhanOnIqama() {
        viewModelScope.launch {
            val current = appSettings.value.playAdhanOnIqama
            val updated = appSettings.value.copy(playAdhanOnIqama = !current)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            NotificationReceiver.rescheduleUpcomingPrayers(getApplication())
            _feedbackMessage.value = if (!current) {
                "تم تفعيل: تشغيل الأذان في الهاتف عند وصول وقت الإقامة"
            } else {
                "تم تفعيل خيار عدم تشغيل أذان الإقامة (تشغيل تكبيرات الإقامة فقط)"
            }
        }
    }

    fun setIqamaSoundMode(mode: String) {
        viewModelScope.launch {
            val updated = when (mode) {
                "NONE" -> appSettings.value.copy(autoPlayIqama = false, playAdhanOnIqama = false)
                "TAKBEER" -> appSettings.value.copy(autoPlayIqama = true, playAdhanOnIqama = false)
                "ADHAN" -> appSettings.value.copy(autoPlayIqama = true, playAdhanOnIqama = true)
                else -> appSettings.value.copy(autoPlayIqama = true, playAdhanOnIqama = false)
            }
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            NotificationReceiver.rescheduleUpcomingPrayers(getApplication())
            _feedbackMessage.value = when (mode) {
                "NONE" -> "تم تفعيل: عدم تشغيل أي صوت عند الإقامة (صامت/إشعار فقط)"
                "TAKBEER" -> "تم تفعيل: تشغيل تكبيرات الإقامة فقط (بدون أذان)"
                "ADHAN" -> "تم تفعيل: تشغيل صوت الأذان كاملاً عند وصول وقت الإقامة"
                else -> "تم تحديث إعدادات صوت الإقامة"
            }
        }
    }

    fun rescheduleAllAlarmsManually() {
        viewModelScope.launch {
            val settings = appSettings.value
            updateCalculationsForCurrentSettings(settings)
            NotificationReceiver.rescheduleUpcomingPrayers(getApplication())
            _feedbackMessage.value = "تمت إعادة جدولة وتثبيت منبهات الأذان والصلوات بنجاح"
        }
    }

    fun testInstantAdhanPlayback() {
        val sound = audioPlayer.selectedAdhanSound.value
        val soundUrl = if (sound.rawResId != null) "raw://${sound.rawResId}" else sound.audioUrl
        com.example.service.AdhanPlaybackService.startPlayAdhan(getApplication(), "التجريبي", soundUrl)
        _feedbackMessage.value = "جاري تشغيل صوت الأذان التجريبي الآن"
    }

    fun selectQuranReciter(reciter: com.example.util.Reciter) {
        audioPlayer.selectReciter(reciter)
        viewModelScope.launch {
            val updated = appSettings.value.copy(selectedQuranReciterId = reciter.id)
            repository.saveSettings(updated)
            _feedbackMessage.value = "تم اختيار القارئ: ${reciter.nameArabic}"
        }
    }

    // Offline Surah download manager functions
    val downloadedSurahs = audioPlayer.downloadedSurahs
    val downloadProgressMap = audioPlayer.downloadProgressMap
    val isBatchDownloading = audioPlayer.isBatchDownloading
    val batchDownloadProgress = audioPlayer.batchDownloadProgress
    val batchStatusText = audioPlayer.batchStatusText
    val batchDownloadSurahNumber = audioPlayer.batchDownloadSurahNumber

    fun downloadSurahOffline(surahNumber: Int, reciter: com.example.util.Reciter = audioPlayer.selectedReciter.value) {
        audioPlayer.downloadSingleSurah(surahNumber, reciter) { success ->
            if (success) {
                _feedbackMessage.value = "تم اكتمال تحميل السورة أوفلاين بنجاح"
            } else {
                _feedbackMessage.value = "تعذر تحميل السورة، يرجى التحقق من الاتصال بالإنترنت"
            }
        }
    }

    fun downloadAllSurahsOffline(reciter: com.example.util.Reciter = audioPlayer.selectedReciter.value) {
        audioPlayer.startBatchDownloadAllSurahs(reciter) { count ->
            _feedbackMessage.value = "تم اكتمال تحميل جميع السور ($count سورة) أوفلاين بنجاح"
        }
    }

    fun downloadJuzAmmaOffline(reciter: com.example.util.Reciter = audioPlayer.selectedReciter.value) {
        audioPlayer.startBatchDownloadJuzAmma(reciter) { count ->
            _feedbackMessage.value = "تم اكتمال تحميل سور جزء عم ($count سورة) أوفلاين بنجاح"
        }
    }

    fun downloadPopularSurahsOffline(reciter: com.example.util.Reciter = audioPlayer.selectedReciter.value) {
        audioPlayer.startBatchDownloadPopularSurahs(reciter) { count ->
            _feedbackMessage.value = "تم اكتمال تحميل السور الأكثر استماعاً ($count سورة) أوفلاين بنجاح"
        }
    }

    fun cancelAllSurahsDownload() {
        audioPlayer.cancelBatchDownload()
        _feedbackMessage.value = "تم إيقاف التحميل"
    }

    fun deleteSurahOffline(surahNumber: Int, reciterId: String = audioPlayer.selectedReciter.value.id) {
        audioPlayer.deleteDownloadedSurah(reciterId, surahNumber)
        _feedbackMessage.value = "تم حذف السورة المحملة"
    }

    fun deleteAllSurahsOffline(reciterId: String = audioPlayer.selectedReciter.value.id) {
        audioPlayer.deleteAllDownloadedSurahs(reciterId)
        _feedbackMessage.value = "تم حذف كافة السور المحملة لتوفير المساحة"
    }

    fun selectAdhanSound(adhan: AdhanSound) {
        viewModelScope.launch {
            audioPlayer.selectAdhanSound(adhan)
            val updated = appSettings.value.copy(selectedAdhanId = adhan.id)
            repository.saveSettings(updated)
            _feedbackMessage.value = "تم اعتماد ${adhan.nameArabic} كصوت للأذان"
        }
    }

    fun selectIqamaSound(iqama: IqamaSound) {
        viewModelScope.launch {
            audioPlayer.selectIqamaSound(iqama)
            val updated = appSettings.value.copy(selectedIqamaId = iqama.id)
            repository.saveSettings(updated)
            _feedbackMessage.value = "تم اعتماد ${iqama.nameArabic} لصوت الإقامة"
        }
    }

    fun testAdhanOnPhone(context: Context) {
        val selectedAdhan = AudioPlayerHelper.availableAdhanSounds.find { it.id == appSettings.value.selectedAdhanId }
            ?: AudioPlayerHelper.availableAdhanSounds.first()
        val currentOrNext = prayerTimes.value.firstOrNull { it.isNext || it.isCurrent } ?: prayerTimes.value.firstOrNull()
        val name = currentOrNext?.type?.arabicName ?: "الظهر"
        val timeStr = currentOrNext?.timeFormatted ?: "12:45"
        val soundParam = if (selectedAdhan.rawResId != null) "raw://${selectedAdhan.rawResId}" else selectedAdhan.audioUrl
        NotificationHelper.showPrayerNotification(context, name, timeStr, autoPlayingAdhan = true)
        AdhanPlaybackService.startPlayAdhan(context, name, soundParam)
        _feedbackMessage.value = "جاري تشغيل تجربة الأذان (${selectedAdhan.nameArabic}) في الهاتف مع الإشعار..."
    }

    fun testIqamaOnPhone(context: Context) {
        val currentOrNext = prayerTimes.value.firstOrNull { it.isNext || it.isCurrent } ?: prayerTimes.value.firstOrNull()
        val name = currentOrNext?.type?.arabicName ?: "الظهر"
        val timeStr = currentOrNext?.iqamaTimeFormatted ?: "01:00"
        if (appSettings.value.playAdhanOnIqama) {
            val selectedAdhan = AudioPlayerHelper.availableAdhanSounds.find { it.id == appSettings.value.selectedAdhanId }
                ?: AudioPlayerHelper.availableAdhanSounds.first()
            val soundParam = if (selectedAdhan.rawResId != null) "raw://${selectedAdhan.rawResId}" else selectedAdhan.audioUrl
            NotificationHelper.showIqamaNotification(context, name, timeStr, autoPlayingIqama = true)
            AdhanPlaybackService.startPlayAdhanForIqama(context, name, soundParam)
            _feedbackMessage.value = "جاري تشغيل الأذان عند الإقامة (${selectedAdhan.nameArabic}) في الهاتف..."
        } else {
            val selectedIqama = AudioPlayerHelper.availableIqamaSounds.find { it.id == appSettings.value.selectedIqamaId }
                ?: AudioPlayerHelper.availableIqamaSounds.first()
            val soundParam = if (selectedIqama.rawResId != null) "raw://${selectedIqama.rawResId}" else selectedIqama.audioUrl
            NotificationHelper.showIqamaNotification(context, name, timeStr, autoPlayingIqama = true)
            AdhanPlaybackService.startPlayIqama(context, name, soundParam)
            _feedbackMessage.value = "جاري تشغيل تكبيرات الإقامة (${selectedIqama.nameArabic}) في الهاتف..."
        }
    }

    fun setIqamaDelayMinutes(minutes: Int) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(iqamaDelayMinutes = minutes)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
            _feedbackMessage.value = "تم ضبط وقت الإقامة: بعد $minutes دقيقة من الأذان"
        }
    }

    // Floating Azkar Bubble controls
    fun toggleFloatingAzkarBubble(enabled: Boolean, context: Context, onRequestPermission: () -> Unit) {
        if (enabled) {
            if (FloatingAzkarService.canDrawOverlay(context)) {
                FloatingAzkarService.start(context)
                _isFloatingAzkarActive.value = true
                viewModelScope.launch {
                    repository.saveSettings(appSettings.value.copy(floatingAzkarEnabled = true))
                }
                _feedbackMessage.value = "تم تشغيل فقاعة الأذكار العائمة فوق التطبيقات"
            } else {
                onRequestPermission()
            }
        } else {
            FloatingAzkarService.stop(context)
            _isFloatingAzkarActive.value = false
            viewModelScope.launch {
                repository.saveSettings(appSettings.value.copy(floatingAzkarEnabled = false))
            }
            _feedbackMessage.value = "تم إيقاف فقاعة الأذكار العائمة"
        }
    }

    // Quran actions
    fun setQuranSearchQuery(query: String) {
        _quranSearchQuery.value = query
    }

    fun clearRequestedQuranPage() {
        _requestedQuranPage.value = null
    }

    fun openSurah(surah: Surah) {
        openSurahAtPage(surah, 0)
    }

    fun openSurahAtPage(surah: Surah, pageIndex: Int = 0) {
        _selectedSurah.value = surah
        _requestedQuranPage.value = pageIndex
        _currentSurahVerses.value = IslamicDataProvider.getVersesForSurah(surah.number)
        viewModelScope.launch {
            repository.saveReadingProgress(surah.number, 1, surah.nameArabic)
            // Load full online Quran text if connected
            val onlineVerses = IslamicDataProvider.fetchOnlineSurahVerses(surah.number, audioPlayer.selectedReciter.value.id)
            if (onlineVerses.isNotEmpty()) {
                _currentSurahVerses.value = onlineVerses
            }
        }
    }

    fun jumpToPageNumber(pageNumber: Int) {
        val targetPage = pageNumber.coerceIn(1, 604)
        val surah = QuranMushafMetadata.getSurahForPage(targetPage)
        val surahStart = QuranMushafMetadata.getPageForSurah(surah.number)
        val pageOffset = (targetPage - surahStart).coerceAtLeast(0)
        openSurahAtPage(surah, pageOffset)
        _feedbackMessage.value = "تم الانتقال إلى صفحة $targetPage • سورة ${surah.nameArabic}"
    }

    fun jumpToJuz(juzNumber: Int) {
        val juz = QuranMushafMetadata.all30Ajzaa.find { it.number == juzNumber } ?: return
        val surah = IslamicDataProvider.all114Surahs.find { it.number == juz.startSurahNumber } ?: return
        val surahStart = QuranMushafMetadata.getPageForSurah(surah.number)
        val pageOffset = (juz.startPage - surahStart).coerceAtLeast(0)
        openSurahAtPage(surah, pageOffset)
        _feedbackMessage.value = "تم الانتقال إلى ${juz.nameArabic} • صفحة ${juz.startPage}"
    }

    fun saveCurrentReadingProgress(surahNumber: Int, ayahNumber: Int, surahName: String) {
        viewModelScope.launch {
            repository.saveReadingProgress(surahNumber, ayahNumber, surahName)
        }
    }

    fun toggleQuranPageBookmark(surah: Surah, pageNumber: Int, firstAyahSnippet: String) {
        viewModelScope.launch {
            val title = "سورة ${surah.nameArabic} - صفحة $pageNumber"
            val existing = favorites.value.find { it.type == "BOOKMARK" && it.title == title }
            if (existing != null) {
                repository.removeFavoriteById(existing.id)
                _feedbackMessage.value = "تمت إزالة العلامة المرجعية لصفحة $pageNumber"
            } else {
                repository.addFavorite(
                    FavoriteEntity(
                        type = "BOOKMARK",
                        title = title,
                        arabicText = firstAyahSnippet.ifBlank { "سورة ${surah.nameArabic} صفحة $pageNumber" },
                        subtitle = "سورة ${surah.nameArabic}",
                        reference = "${surah.number}:$pageNumber"
                    )
                )
                _feedbackMessage.value = "تم حفظ علامة مرجعية لصفحة $pageNumber بنجاح 🔖"
            }
        }
    }

    fun openNextSurah(autoPlayAudio: Boolean = false) {
        val current = _selectedSurah.value ?: return
        if (current.number < 114) {
            val nextSurah = IslamicDataProvider.allSurahs.find { it.number == current.number + 1 }
            if (nextSurah != null) {
                openSurah(nextSurah)
                _feedbackMessage.value = "تم الانتقال إلى سورة ${nextSurah.nameArabic}"
                if (autoPlayAudio) {
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500)
                        playSurahWithFollowAlong(1)
                    }
                }
            }
        }
    }

    fun openPreviousSurah() {
        val current = _selectedSurah.value ?: return
        if (current.number > 1) {
            val prevSurah = IslamicDataProvider.allSurahs.find { it.number == current.number - 1 }
            if (prevSurah != null) {
                openSurah(prevSurah)
                _feedbackMessage.value = "تم الانتقال إلى سورة ${prevSurah.nameArabic}"
            }
        }
    }

    fun openSurahByNumber(number: Int) {
        val surah = IslamicDataProvider.allSurahs.find { it.number == number }
        if (surah != null) {
            openSurah(surah)
            _feedbackMessage.value = "تم الانتقال إلى سورة ${surah.nameArabic}"
        }
    }

    fun playSurahWithFollowAlong(startAyahNumber: Int = 1) {
        val surah = _selectedSurah.value ?: return
        val verses = _currentSurahVerses.value
        if (verses.isNotEmpty()) {
            audioPlayer.playVerseByVerse(
                surahNumber = surah.number,
                surahName = surah.nameArabic,
                verses = verses,
                startAyahNumber = startAyahNumber
            )
            _feedbackMessage.value = "جاري تلاوة سورة ${surah.nameArabic} مع التتبع الصوتي والتظليل..."
        }
    }

    fun playAyahAudio(ayah: Ayah) {
        val surah = _selectedSurah.value
        val surahName = surah?.nameArabic ?: "القرآن"
        val verses = _currentSurahVerses.value.ifEmpty { listOf(ayah) }
        audioPlayer.playVerseByVerse(
            surahNumber = ayah.surahNumber,
            surahName = surahName,
            verses = verses,
            startAyahNumber = ayah.ayahNumber
        )
    }

    fun playNextAyah() {
        audioPlayer.playNextAyah()
    }

    fun playPreviousAyah() {
        audioPlayer.playPreviousAyah()
    }

    fun stopQuranAudio() {
        audioPlayer.stopVerseByVerse()
    }

    fun closeSurahDetail() {
        _selectedSurah.value = null
        audioPlayer.stopVerseByVerse()
        audioPlayer.stopAudio()
    }

    fun openTafsir(ayah: Ayah, surahName: String? = null) {
        _selectedAyahForTafsir.value = ayah
        if (surahName != null) {
            _selectedAyahSurahName.value = surahName
        } else if (_selectedSurah.value != null) {
            _selectedAyahSurahName.value = _selectedSurah.value!!.nameArabic
        }
        _aiTafsirText.value = null
        _isAiTafsirLoading.value = false

        // Check local cache
        viewModelScope.launch {
            val key = "AYAH_${ayah.surahNumber}_${ayah.ayahNumber}"
            val cached = repository.getAiExplanation(key)
            if (cached != null && cached.explanation.isNotBlank()) {
                _aiTafsirText.value = cached.explanation
            }
        }
    }

    fun requestAiTafsir(forceRefresh: Boolean = false) {
        val ayah = _selectedAyahForTafsir.value ?: return
        val surahName = _selectedAyahSurahName.value
        val key = "AYAH_${ayah.surahNumber}_${ayah.ayahNumber}"

        viewModelScope.launch {
            _isAiTafsirLoading.value = true
            try {
                if (!forceRefresh) {
                    val cached = repository.getAiExplanation(key)
                    if (cached != null && cached.explanation.isNotBlank()) {
                        _aiTafsirText.value = cached.explanation
                        _isAiTafsirLoading.value = false
                        return@launch
                    }
                }

                val result = GeminiAiTafsirService.explainAyah(
                    surahName = surahName,
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.ayahNumber,
                    ayahText = ayah.textArabic,
                    classicalTafsir = ayah.tafsir,
                    language = appSettings.value.appLanguage
                )

                result.onSuccess { text ->
                    _aiTafsirText.value = text
                    repository.saveAiExplanation(
                        AiExplanationEntity(
                            key = key,
                            type = "AYAH",
                            title = "تفسير سورة $surahName: ${ayah.ayahNumber}",
                            arabicText = ayah.textArabic,
                            explanation = text,
                            isAiGenerated = true
                        )
                    )
                }.onFailure {
                    _feedbackMessage.value = "تعذر استلام التفسير، يرجى المحاولة لاحقاً"
                }
            } finally {
                _isAiTafsirLoading.value = false
            }
        }
    }

    fun closeTafsir() {
        _selectedAyahForTafsir.value = null
        _aiTafsirText.value = null
        _isAiTafsirLoading.value = false
    }

    // Hadith actions
    fun setHadithCategory(category: String) {
        _selectedHadithCategory.value = category
    }

    fun openHadithExplanation(hadith: Hadith) {
        _selectedHadithForExplanation.value = hadith
        _aiHadithExplanationText.value = null
        _isAiHadithLoading.value = false

        viewModelScope.launch {
            val key = "HADITH_${hadith.id}"
            val cached = repository.getAiExplanation(key)
            if (cached != null && cached.explanation.isNotBlank()) {
                _aiHadithExplanationText.value = cached.explanation
            } else {
                requestAiHadithExplanation(false)
            }
        }
    }

    fun requestAiHadithExplanation(forceRefresh: Boolean = false) {
        val hadith = _selectedHadithForExplanation.value ?: return
        val key = "HADITH_${hadith.id}"

        viewModelScope.launch {
            _isAiHadithLoading.value = true
            try {
                if (!forceRefresh) {
                    val cached = repository.getAiExplanation(key)
                    if (cached != null && cached.explanation.isNotBlank()) {
                        _aiHadithExplanationText.value = cached.explanation
                        _isAiHadithLoading.value = false
                        return@launch
                    }
                }

                val result = GeminiAiTafsirService.explainHadith(
                    hadithText = hadith.text,
                    narrator = hadith.narrator,
                    source = hadith.source,
                    category = hadith.category,
                    grading = hadith.grading,
                    language = appSettings.value.appLanguage
                )

                result.onSuccess { text ->
                    _aiHadithExplanationText.value = text
                    repository.saveAiExplanation(
                        AiExplanationEntity(
                            key = key,
                            type = "HADITH",
                            title = "شرح حديث: ${hadith.category}",
                            arabicText = hadith.text,
                            explanation = text,
                            isAiGenerated = true
                        )
                    )
                }.onFailure {
                    _feedbackMessage.value = "تعذر استلام شرح الحديث، يرجى المحاولة ثانية"
                }
            } finally {
                _isAiHadithLoading.value = false
            }
        }
    }

    fun closeHadithExplanation() {
        _selectedHadithForExplanation.value = null
        _aiHadithExplanationText.value = null
        _isAiHadithLoading.value = false
    }

    // Open AI Scholar Questions
    fun setAiScholarQuestion(q: String) {
        _aiScholarQuestion.value = q
    }

    fun askAiScholar(customQuery: String? = null) {
        val query = (customQuery ?: _aiScholarQuestion.value).trim()
        if (query.isBlank()) return
        if (customQuery != null) {
            _aiScholarQuestion.value = query
        }
        viewModelScope.launch {
            _isAiScholarLoading.value = true
            val key = "QUERY_${query.hashCode()}"

            // Fast DB Cache check for instant responsiveness
            val cached = repository.getAiExplanation(key)
            if (cached != null && cached.explanation.isNotBlank()) {
                _aiScholarAnswer.value = cached.explanation
                _isAiScholarLoading.value = false
                return@launch
            }

            try {
                val res = GeminiAiTafsirService.askIslamicScholar(
                    question = query,
                    language = appSettings.value.appLanguage
                )
                res.onSuccess { answer ->
                    _aiScholarAnswer.value = answer
                    repository.saveAiExplanation(
                        AiExplanationEntity(
                            key = key,
                            type = "QUERY",
                            title = query.take(40),
                            arabicText = query,
                            explanation = answer,
                            isAiGenerated = true
                        )
                    )
                }.onFailure {
                    _feedbackMessage.value = "تعذر الحصول على إجابة، يرجى المحاولة ثانية"
                }
            } finally {
                _isAiScholarLoading.value = false
            }
        }
    }

    // Azkar actions
    fun setAzkarCategory(category: String) {
        _selectedAzkarCategory.value = category
    }

    fun incrementDhikrCount(dhikr: Dhikr) {
        val current = _azkarCounts.value[dhikr.id] ?: 0
        if (current < dhikr.count) {
            val newCount = current + 1
            _azkarCounts.value = _azkarCounts.value.toMutableMap().apply {
                put(dhikr.id, newCount)
            }
            vibrateDevice(40)
            if (newCount == dhikr.count) {
                vibrateDevice(100)
            }
        }
    }

    fun resetDhikrCount(dhikrId: Int) {
        _azkarCounts.value = _azkarCounts.value.toMutableMap().apply {
            remove(dhikrId)
        }
    }

    // Tasbih actions
    fun setTasbihDhikr(text: String, target: Int = 33) {
        _currentTasbihText.value = text
        _tasbihTarget.value = target
        _tasbihCount.value = 0
    }

    fun incrementTasbih() {
        val current = _tasbihCount.value
        val target = _tasbihTarget.value
        val next = current + 1
        _tasbihCount.value = next
        _totalTasbihCount.value += 1
        vibrateDevice(35)
        if (target > 0 && next % target == 0) {
            vibrateDevice(120)
        }
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
        vibrateDevice(50)
    }

    fun setTasbihTarget(target: Int) {
        _tasbihTarget.value = target
    }

    // Favorites
    fun toggleFavorite(
        type: String,
        title: String,
        arabicText: String,
        subtitle: String,
        reference: String
    ) {
        viewModelScope.launch {
            val existing = favorites.value.find { it.arabicText == arabicText }
            if (existing != null) {
                repository.removeFavoriteById(existing.id)
                _feedbackMessage.value = "تمت الإزالة من المفضلة"
            } else {
                repository.addFavorite(
                    FavoriteEntity(
                        type = type,
                        title = title,
                        arabicText = arabicText,
                        subtitle = subtitle,
                        reference = reference
                    )
                )
                _feedbackMessage.value = "تمت الإضافة إلى المفضلة بنجاح"
            }
        }
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch {
            repository.removeFavoriteById(id)
            _feedbackMessage.value = "تم حذف العنصر من المفضلة"
        }
    }

    fun removeFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.removeFavoriteById(favorite.id)
            _feedbackMessage.value = "تم حذف العنصر من المفضلة"
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Unified Search
    fun setUnifiedSearchQuery(query: String) {
        _unifiedSearchQuery.value = query
    }

    // Settings Updates
    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            repository.saveSettings(appSettings.value.copy(themeMode = mode))
            _feedbackMessage.value = when (mode) {
                "LIGHT" -> "تم تفعيل الوضع النهاري"
                "DARK" -> "تم تفعيل الوضع الليلي"
                else -> "تم تفعيل وضع النظام التلقائي"
            }
        }
    }

    fun updateColorTheme(theme: String) {
        viewModelScope.launch {
            repository.saveSettings(appSettings.value.copy(colorTheme = theme))
            _feedbackMessage.value = "تم تغيير مظهر وتدرج الألوان للتطبيق"
        }
    }

    fun updateNumberFormat(format: String) {
        viewModelScope.launch {
            repository.saveSettings(appSettings.value.copy(numberFormat = format))
            val msg = if (format == "ARABIC") "تم اعتماد الأرقام المشرقية (١، ٢، ٣...)" else "تم اعتماد الأرقام الغربية / الفرنسية (1, 2, 3...)"
            _feedbackMessage.value = msg
        }
    }

    fun updateCustomGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            val trimmed = apiKey.trim()
            repository.saveSettings(appSettings.value.copy(customGeminiApiKey = trimmed))
            GeminiAiTafsirService.userCustomApiKey = trimmed
            _feedbackMessage.value = if (trimmed.isNotBlank()) "تم حفظ مفتاح Google Gemini وتفعيله" else "تمت استعادة المفتاح الافتراضي"
        }
    }

    fun testGeminiConnectivity(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = GeminiAiTafsirService.testLiveConnection()
            onResult(res.first, res.second)
        }
    }

    fun formatNumber(number: Number): String {
        val useArabic = appSettings.value.numberFormat == "ARABIC"
        return com.example.util.NumberFormatter.format(number, useArabic)
    }

    fun formatNumber(text: String): String {
        val useArabic = appSettings.value.numberFormat == "ARABIC"
        return com.example.util.NumberFormatter.format(text, useArabic)
    }

    fun clearAiScholarAnswer() {
        _aiScholarAnswer.value = null
    }

    fun updateCalculationMethod(method: String) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(calculationMethod = method)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
        }
    }

    fun updateMadhhab(madhhab: String) {
        viewModelScope.launch {
            val updated = appSettings.value.copy(madhhab = madhhab)
            repository.saveSettings(updated)
            updateCalculationsForCurrentSettings(updated)
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            val current = appSettings.value.vibrationEnabled
            repository.saveSettings(appSettings.value.copy(vibrationEnabled = !current))
        }
    }

    fun toggleNotificationSetting(type: String) {
        viewModelScope.launch {
            val current = appSettings.value
            val updated = when (type) {
                "prayer" -> current.copy(prayerNotifications = !current.prayerNotifications)
                "iqama" -> current.copy(iqamaNotifications = !current.iqamaNotifications)
                "morning" -> current.copy(morningAzkarNotification = !current.morningAzkarNotification)
                "evening" -> current.copy(eveningAzkarNotification = !current.eveningAzkarNotification)
                "quran" -> current.copy(quranReminderNotification = !current.quranReminderNotification)
                "hadith" -> current.copy(dailyHadithNotification = !current.dailyHadithNotification)
                "verse" -> current.copy(dailyVerseNotification = !current.dailyVerseNotification)
                else -> current
            }
            repository.saveSettings(updated)
        }
    }

    /**
     * Send instant test notification for testing on device
     */
    fun triggerTestNotification(type: String) {
        val context = getApplication<Application>()
        when (type) {
            "prayer" -> {
                NotificationHelper.showPrayerNotification(context, "الظهر", "12:45", autoPlayingAdhan = false)
                _feedbackMessage.value = "تم إرسال إشعار تجريبي لموعد الأذان"
            }
            "iqama" -> {
                NotificationHelper.showIqamaNotification(context, "الظهر", "01:00", autoPlayingIqama = false)
                _feedbackMessage.value = "تم إرسال إشعار تجريبي لموعد الإقامة"
            }
            "morning" -> {
                NotificationHelper.showMorningAzkarNotification(context)
                _feedbackMessage.value = "تم إرسال إشعار تجريبي لأذكار الصباح"
            }
            "evening" -> {
                NotificationHelper.showEveningAzkarNotification(context)
                _feedbackMessage.value = "تم إرسال إشعار تجريبي لأذكار المساء"
            }
            "quran" -> {
                val dailyVerse = IslamicDataProvider.dailyVerse
                val hint = "﴿${dailyVerse.textArabic}﴾ [سورة البقرة: ${dailyVerse.ayahNumber}]"
                NotificationHelper.showQuranReminderNotification(context, hint)
                _feedbackMessage.value = "تم إرسال إشعار تجريبي للورد القرآني"
            }
        }
    }

    // Clipboard & Sharing helpers
    fun copyTextToClipboard(label: String, text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        _feedbackMessage.value = "تم النسخ إلى الحافظة"
    }

    fun shareText(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, "مشاركة المحتوى").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(chooser)
    }

    private fun vibrateDevice(durationMillis: Long) {
        if (!appSettings.value.vibrationEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {}
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun setFeedbackMessage(message: String) {
        _feedbackMessage.value = message
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stopAudio()
        prayerUpdateJob?.cancel()
    }
}
