package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.R
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Ayah
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class Reciter(
    val id: String,
    val nameArabic: String,
    val style: String,
    val serverUrlTemplate: String,
    val isOfflineAvailable: Boolean = false,
    val localRawResId: Int? = null
)

data class AdhanSound(
    val id: String,
    val nameArabic: String,
    val muadhinOrPlace: String,
    val audioUrl: String,
    val rawResId: Int? = null,
    val description: String = ""
)

data class IqamaSound(
    val id: String,
    val nameArabic: String,
    val description: String,
    val audioUrl: String = "",
    val rawResId: Int? = null
)

class AudioPlayerHelper(private val context: Context) {

    companion object {
        val availableAdhanSounds = listOf(
            AdhanSound(
                id = "qatami",
                nameArabic = "أذان الشيخ ناصر القطامي (أوفلاين)",
                muadhinOrPlace = "الرياض - المملكة العربية السعودية",
                audioUrl = "",
                rawResId = R.raw.adhan_qatami,
                description = "أذان ندي خاشع بصوت إمام وخطيب جامع الملك عبد الله بالرياض (يعمل بدون إنترنت)"
            ),
            AdhanSound(
                id = "makkah",
                nameArabic = "أذان الحرم المكي الشريف (أوفلاين)",
                muadhinOrPlace = "الشيخ علي أحمد ملا - مكة المكرمة",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Ali_Ibn_Ahmad_Mala_1_-_Al_Haram_Al_Maki_(%D8%B9%D9%84%D9%8A_%D8%A8%D9%86_%D8%A3%D8%AD%D9%85%D8%AF_%D9%85%D9%84%D8%A7_-_%D8%A7%D9%84%D8%AD%D8%B1%D9%85_%D8%A7%D9%84%D9%85%D9%83%D9%8A).mp3",
                rawResId = R.raw.adhan_makkah,
                description = "الأذان النبوي الشجي الكامل بصوت شيخ مؤذني الحرم المكي (يعمل بدون إنترنت فوراً)"
            ),
            AdhanSound(
                id = "madinah",
                nameArabic = "أذان المسجد النبوي الشريف",
                muadhinOrPlace = "الشيخ عصام بخاري - المدينة المنورة",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Adhan_Al_Haram_Al_Madani_-_Al_Madinah_1_(%D8%A3%D8%B0%D8%A7%D9%86_%D8%A7%D9%84%D8%AD%D8%B1%D9%85_%D8%A7%D9%84%D9%85%D8%AF%D9%86%D9%8A_-_%D8%A7%D9%84%D9%85%D8%AF%D9%8A%D9%86%D8%A9_%D8%A7%D9%84%D9%85%D9%86%D9%88%D8%B1%D8%A9).mp3",
                rawResId = null,
                description = "أذان الخشوع والسكينة والروحانية العالية من مسجد رسول الله ﷺ"
            ),
            AdhanSound(
                id = "aqsa",
                nameArabic = "أذان المسجد الأقصى المبارك",
                muadhinOrPlace = "القدس الشريف - فلسطين",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Adhan_Al_Aqsa_-_Jerusalem_(%D8%A3%D8%B0%D8%A7%D9%86_%D8%A7%D9%84%D9%85%D8%B3%D8%AC%D8%AF_%D8%A7%D9%84%D8%A3%D9%82%D8%B5%D9%89_-_%D8%A7%D9%84%D9%82%D8%AF%D8%B3).mp3",
                rawResId = null,
                description = "الأذان المبارك الخالد من أولى القبلتين وثالث الحرمين الشريفين"
            ),
            AdhanSound(
                id = "egypt",
                nameArabic = "أذان الشيخ عبد الباسط عبد الصمد",
                muadhinOrPlace = "جمهورية مصر العربية",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Abdulbasit_Abdusamad_1_-_Egypt_(%D8%B9%D8%A8%D8%AF_%D8%A7%D9%84%D8%A8%D8%A7%D8%B3%D8%B7_%D8%B9%D8%A8%D8%AF_%D8%A7%D9%84%D8%B5%D9%85%D8%AF_-_%D9%85%D8%B5%D8%B1).mp3",
                rawResId = null,
                description = "الأذان الرخيم الخاشع بصوت صوت مكة وقارئ العالم الإسلامي الشيخ عبد الباسط رحمه الله"
            ),
            AdhanSound(
                id = "afasy",
                nameArabic = "أذان الشيخ مشاري راشد العفاسي",
                muadhinOrPlace = "دولة الكويت",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Mishary_Rashid_Alafasy_1_-_Kuwait_(%D9%85%D8%B4%D8%A7%D8%B1%D9%8A_%D8%B1%D8%A7%D8%B4%D8%AF_%D8%A7%D9%84%D8%B9%D9%81%D8%A7%D8%B3%D9%8A_-_%D8%A7%D9%84%D9%83%D9%88%D9%8A%D8%AA).mp3",
                rawResId = null,
                description = "الأذان العذب الشجي بصوت الشيخ مشاري راشد العفاسي"
            ),
            AdhanSound(
                id = "minshawi",
                nameArabic = "أذان الشيخ محمد صديق المنشاوي",
                muadhinOrPlace = "مصر",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Mohamed_Siddiq_El-Minshawi_-_Egypt_1_(%D9%85%D8%AD%D9%85%D8%AF_%D8%B5%D8%AF%D9%8A%D9%82_%D8%A7%D9%84%D9%85%D9%86%D8%B4%D8%A7%D9%88%D9%8A_-_%D9%85%D8%B5%D8%B1).mp3",
                rawResId = null,
                description = "الأذان الباكي المؤثر الشديد الروحانية بأداء الشيخ المنشاوي رحمه الله"
            ),
            AdhanSound(
                id = "algeria",
                nameArabic = "أذان الجزائر (الشيخ وليد مهساس)",
                muadhinOrPlace = "مسجد أبي ذر الغفاري - الجزائر",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Waled_Mahsas_2_-_Algeria_(%D9%88%D9%84%D9%8A%D8%AF_%D9%85%D8%AD%D8%B3%D8%A7%D8%B3_-_%D8%A7%D9%84%D8%AC%D8%B2%D8%A7%D8%A6%D8%B1).mp3",
                rawResId = null,
                description = "الأذان الجزائري الخاشع بالمقام المغاربي الندي الأصيل"
            ),
            AdhanSound(
                id = "turkey",
                nameArabic = "أذان جامع السلطان أحمد (تركيا)",
                muadhinOrPlace = "إسطنبول - تركيا",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Adhan_Turkey_1_(%D8%A3%D8%B0%D8%A7%D9%86_%D8%AA%D8%B1%D9%83%D9%8A%D8%A7).mp3",
                rawResId = null,
                description = "أذان المقامات العثمانية الهادئة المؤثرة التي تبعث الطمأنينة في النفس"
            ),
            AdhanSound(
                id = "fajr_special",
                nameArabic = "أذان الفجر النبوي الشريف",
                muadhinOrPlace = "المسجد النبوي الشريف - المدينة المنورة",
                audioUrl = "https://raw.githubusercontent.com/Kiwifu/adhan-mp3/main/Adhan_Fajr_Al_Haram_Al_Madani_(%D8%A3%D8%B0%D8%A7%D9%86_%D8%A7%D9%84%D9%81%D8%AC%D8%B1_%D8%A7%D9%84%D8%AD%D8%B1%D9%85_%D8%A7%D9%84%D9%85%D8%AF%D9%86%D9%8A).mp3",
                rawResId = null,
                description = "أذان الفجر الشجي مع جملة التثويب: (الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ)"
            )
        )

        val availableIqamaSounds = listOf(
            IqamaSound(
                id = "standard",
                nameArabic = "تكبيرات وإقامة الصلاة الشرعية (أوفلاين)",
                description = "الله أكبر، الله أكبر، قد قامت الصلاة، قد قامت الصلاة (يعمل بدون إنترنت)",
                audioUrl = "",
                rawResId = R.raw.iqama
            ),
            IqamaSound(
                id = "haram",
                nameArabic = "إقامة صلاة الحرم المكي الشريف",
                description = "تكبيرات وإقامة الصلاة بأداء وروحانية مؤذني الحرم المكي الشريف",
                audioUrl = "",
                rawResId = R.raw.iqama
            ),
            IqamaSound(
                id = "madinah_iqama",
                nameArabic = "إقامة صلاة المسجد النبوي الشريف",
                description = "تكبيرات وإقامة الصلاة بسكينة وهدوء محراب المسجد النبوي الشريف",
                audioUrl = "",
                rawResId = R.raw.iqama
            )
        )

        val availableReciters = listOf(
            Reciter(
                id = "frs_a",
                nameArabic = "فارس عباد (أوفلاين)",
                style = "تلاوة خاشعة شجية ومؤثرة (مدعوم أوفلاين)",
                serverUrlTemplate = "https://server8.mp3quran.net/frs_a/%03d.mp3",
                isOfflineAvailable = true,
                localRawResId = R.raw.fares_abbad_fatiha
            ),
            Reciter(
                id = "afs",
                nameArabic = "مشاري راشد العفاسي",
                style = "مرتل عذب",
                serverUrlTemplate = "https://server8.mp3quran.net/afs/%03d.mp3"
            ),
            Reciter(
                id = "basit",
                nameArabic = "عبد الباسط عبد الصمد",
                style = "تلاوة مجودة خاشعة",
                serverUrlTemplate = "https://server7.mp3quran.net/basit/%03d.mp3"
            ),
            Reciter(
                id = "husr",
                nameArabic = "محمود خليل الحصري",
                style = "المصحف المعلم والمرتل",
                serverUrlTemplate = "https://server13.mp3quran.net/husr/%03d.mp3"
            ),
            Reciter(
                id = "s_gmd",
                nameArabic = "سعد الغامدي",
                style = "ترتيل هادئ",
                serverUrlTemplate = "https://server7.mp3quran.net/s_gmd/%03d.mp3"
            ),
            Reciter(
                id = "yasser",
                nameArabic = "ياسر الدوسري",
                style = "تلاوة حجازية مؤثرة",
                serverUrlTemplate = "https://server11.mp3quran.net/yasser/%03d.mp3"
            ),
            Reciter(
                id = "a_jbr",
                nameArabic = "علي جابر",
                style = "تلاوة الحرم المكي التراثية",
                serverUrlTemplate = "https://server11.mp3quran.net/a_jbr/%03d.mp3"
            ),
            Reciter(
                id = "ajm",
                nameArabic = "أحمد بن علي العجمي",
                style = "ترتيل شجي",
                serverUrlTemplate = "https://server10.mp3quran.net/ajm/%03d.mp3"
            ),
            Reciter(
                id = "maher",
                nameArabic = "ماهر المعيقلي",
                style = "تلاوة الحرم المكي المعاصرة",
                serverUrlTemplate = "https://server12.mp3quran.net/maher/%03d.mp3"
            )
        )
    }

    sealed class PlaybackState {
        object Idle : PlaybackState()
        data class Buffering(val url: String, val title: String) : PlaybackState()
        data class Playing(val url: String, val title: String) : PlaybackState()
        data class Paused(val url: String, val title: String) : PlaybackState()
        data class Error(val message: String) : PlaybackState()
    }

    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _selectedReciter = MutableStateFlow(availableReciters.first())
    val selectedReciter: StateFlow<Reciter> = _selectedReciter.asStateFlow()

    private val _selectedAdhanSound = MutableStateFlow(availableAdhanSounds.first())
    val selectedAdhanSound: StateFlow<AdhanSound> = _selectedAdhanSound.asStateFlow()

    private val _selectedIqamaSound = MutableStateFlow(availableIqamaSounds.first())
    val selectedIqamaSound: StateFlow<IqamaSound> = _selectedIqamaSound.asStateFlow()

    private val _isAdhanPlaying = MutableStateFlow(false)
    val isAdhanPlaying: StateFlow<Boolean> = _isAdhanPlaying.asStateFlow()

    private val _isIqamaPlaying = MutableStateFlow(false)
    val isIqamaPlaying: StateFlow<Boolean> = _isIqamaPlaying.asStateFlow()

    private val _currentSurahNumber = MutableStateFlow<Int?>(null)
    val currentSurahNumber: StateFlow<Int?> = _currentSurahNumber.asStateFlow()

    private val _currentSurahName = MutableStateFlow<String?>(null)
    val currentSurahName: StateFlow<String?> = _currentSurahName.asStateFlow()

    private val _isFullSurahPlaying = MutableStateFlow(false)
    val isFullSurahPlaying: StateFlow<Boolean> = _isFullSurahPlaying.asStateFlow()

    var onSurahRecitationCompleted: ((surahNumber: Int) -> Unit)? = null

    private val _isVerseByVersePlaying = MutableStateFlow(false)
    val isVerseByVersePlaying: StateFlow<Boolean> = _isVerseByVersePlaying.asStateFlow()

    private val _currentActiveAyahNumber = MutableStateFlow<Int?>(null)
    val currentActiveAyahNumber: StateFlow<Int?> = _currentActiveAyahNumber.asStateFlow()

    private var activeVerseList: List<Ayah> = emptyList()
    private var activeVerseIndex: Int = 0
    private var activeSurahNumberForTracking: Int = 1
    private var activeSurahNameForTracking: String = ""

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ==========================================
    // OFFLINE SURAH DOWNLOAD & STORAGE SYSTEM
    // ==========================================
    val bundledFaresAbbadSurahs = listOf(1, 108, 109, 110, 111, 112, 113, 114)

    fun getOfflineSurahFile(reciterId: String, surahNumber: Int): java.io.File {
        val dir = java.io.File(context.filesDir, "quran_offline/$reciterId")
        if (!dir.exists()) dir.mkdirs()
        return java.io.File(dir, "$surahNumber.mp3")
    }

    fun isSurahOfflineAvailable(reciterId: String, surahNumber: Int): Boolean {
        if (reciterId == "frs_a") {
            if (surahNumber in bundledFaresAbbadSurahs) {
                return true
            }
        }
        val file = getOfflineSurahFile(reciterId, surahNumber)
        return file.exists() && file.length() > 10_000L
    }

    private val _downloadedSurahs = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedSurahs: StateFlow<Set<Int>> = _downloadedSurahs.asStateFlow()

    private val _downloadProgressMap = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<Int, Float>> = _downloadProgressMap.asStateFlow()

    private val _isBatchDownloading = MutableStateFlow(false)
    val isBatchDownloading: StateFlow<Boolean> = _isBatchDownloading.asStateFlow()

    private val _batchDownloadSurahNumber = MutableStateFlow<Int?>(null)
    val batchDownloadSurahNumber: StateFlow<Int?> = _batchDownloadSurahNumber.asStateFlow()

    private val _batchDownloadProgress = MutableStateFlow(0f)
    val batchDownloadProgress: StateFlow<Float> = _batchDownloadProgress.asStateFlow()

    private val _batchStatusText = MutableStateFlow("")
    val batchStatusText: StateFlow<String> = _batchStatusText.asStateFlow()

    private var batchDownloadJob: Job? = null

    init {
        refreshDownloadedSurahs()
    }

    fun refreshDownloadedSurahs(reciterId: String = _selectedReciter.value.id) {
        val set = mutableSetOf<Int>()
        if (reciterId == "frs_a") {
            set.addAll(bundledFaresAbbadSurahs)
        }
        val dir = java.io.File(context.filesDir, "quran_offline/$reciterId")
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() > 10_000L && file.extension == "mp3") {
                    file.nameWithoutExtension.toIntOrNull()?.let { num ->
                        if (num in 1..114) set.add(num)
                    }
                }
            }
        }
        _downloadedSurahs.value = set
    }

    fun downloadSingleSurah(
        surahNumber: Int,
        reciter: Reciter = _selectedReciter.value,
        onFinished: ((Boolean) -> Unit)? = null
    ) {
        if (isSurahOfflineAvailable(reciter.id, surahNumber)) {
            refreshDownloadedSurahs(reciter.id)
            onFinished?.invoke(true)
            return
        }

        scope.launch(Dispatchers.IO) {
            val file = getOfflineSurahFile(reciter.id, surahNumber)
            val tempFile = java.io.File(file.parentFile, "${file.name}.tmp")
            val urlStr = String.format(Locale.US, reciter.serverUrlTemplate, surahNumber)

            try {
                withContext(Dispatchers.Main) {
                    _downloadProgressMap.value = _downloadProgressMap.value + (surahNumber to 0.05f)
                }

                val conn = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 15000
                conn.instanceFollowRedirects = true

                if (conn.responseCode == 200) {
                    val totalLength = conn.contentLength.toFloat()
                    var bytesCopied = 0L

                    conn.inputStream.use { input ->
                        java.io.FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytes = input.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                if (totalLength > 0) {
                                    val prog = (bytesCopied / totalLength).coerceIn(0f, 1f)
                                    withContext(Dispatchers.Main) {
                                        _downloadProgressMap.value = _downloadProgressMap.value + (surahNumber to prog)
                                    }
                                }
                                bytes = input.read(buffer)
                            }
                        }
                    }

                    if (tempFile.length() > 10_000L) {
                        tempFile.renameTo(file)
                        withContext(Dispatchers.Main) {
                            _downloadProgressMap.value = _downloadProgressMap.value - surahNumber
                            refreshDownloadedSurahs(reciter.id)
                            onFinished?.invoke(true)
                        }
                    } else {
                        tempFile.delete()
                        withContext(Dispatchers.Main) {
                            _downloadProgressMap.value = _downloadProgressMap.value - surahNumber
                            onFinished?.invoke(false)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _downloadProgressMap.value = _downloadProgressMap.value - surahNumber
                        onFinished?.invoke(false)
                    }
                }
            } catch (_: Exception) {
                tempFile.delete()
                withContext(Dispatchers.Main) {
                    _downloadProgressMap.value = _downloadProgressMap.value - surahNumber
                    onFinished?.invoke(false)
                }
            }
        }
    }

    fun startBatchDownloadSurahs(
        surahNumbers: List<Int>,
        reciter: Reciter = _selectedReciter.value,
        packName: String = "السور",
        onFinished: ((Int) -> Unit)? = null
    ) {
        if (_isBatchDownloading.value) return
        batchDownloadJob?.cancel()

        batchDownloadJob = scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isBatchDownloading.value = true
                _batchStatusText.value = "بدء تجهيز تحميل $packName للقارئ ${reciter.nameArabic} أوفلاين..."
                _batchDownloadProgress.value = 0f
            }

            var successCount = 0
            val total = surahNumbers.size
            val allSurahs = IslamicDataProvider.all114Surahs

            for ((index, surahNum) in surahNumbers.withIndex()) {
                if (!isActive) break
                val surah = allSurahs.find { it.number == surahNum }
                val surahName = surah?.nameArabic ?: "$surahNum"

                withContext(Dispatchers.Main) {
                    _batchDownloadSurahNumber.value = surahNum
                    _batchDownloadProgress.value = index.toFloat() / total.toFloat()
                    _batchStatusText.value = "جاري تحميل سورة $surahName (${index + 1} من $total)..."
                }

                if (isSurahOfflineAvailable(reciter.id, surahNum)) {
                    successCount++
                    continue
                }

                val file = getOfflineSurahFile(reciter.id, surahNum)
                val tempFile = java.io.File(file.parentFile, "${file.name}.tmp")
                val urlStr = String.format(Locale.US, reciter.serverUrlTemplate, surahNum)

                try {
                    val conn = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 45000
                    conn.instanceFollowRedirects = true

                    if (conn.responseCode == 200) {
                        conn.inputStream.use { input ->
                            java.io.FileOutputStream(tempFile).use { output ->
                                val buffer = ByteArray(16 * 1024)
                                var bytes = input.read(buffer)
                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    bytes = input.read(buffer)
                                }
                            }
                        }
                        if (tempFile.length() > 10_000L) {
                            tempFile.renameTo(file)
                            successCount++
                            withContext(Dispatchers.Main) {
                                refreshDownloadedSurahs(reciter.id)
                            }
                        } else {
                            tempFile.delete()
                        }
                    }
                } catch (_: Exception) {
                    tempFile.delete()
                }

                withContext(Dispatchers.Main) {
                    _batchDownloadProgress.value = (index + 1).toFloat() / total.toFloat()
                }
            }

            withContext(Dispatchers.Main) {
                _isBatchDownloading.value = false
                _batchDownloadSurahNumber.value = null
                _batchDownloadProgress.value = 1f
                _batchStatusText.value = "تم اكتمال تحميل $successCount من $total سورة أوفلاين بنجاح"
                refreshDownloadedSurahs(reciter.id)
                onFinished?.invoke(successCount)
            }
        }
    }

    fun startBatchDownloadAllSurahs(
        reciter: Reciter = _selectedReciter.value,
        onFinished: ((Int) -> Unit)? = null
    ) {
        startBatchDownloadSurahs(
            surahNumbers = (1..114).toList(),
            reciter = reciter,
            packName = "جميع سور القرآن الكريم (114 سورة)",
            onFinished = onFinished
        )
    }

    fun startBatchDownloadJuzAmma(
        reciter: Reciter = _selectedReciter.value,
        onFinished: ((Int) -> Unit)? = null
    ) {
        startBatchDownloadSurahs(
            surahNumbers = (78..114).toList(),
            reciter = reciter,
            packName = "سور جزء عم (37 سورة)",
            onFinished = onFinished
        )
    }

    fun startBatchDownloadPopularSurahs(
        reciter: Reciter = _selectedReciter.value,
        onFinished: ((Int) -> Unit)? = null
    ) {
        startBatchDownloadSurahs(
            surahNumbers = listOf(1, 2, 3, 18, 36, 55, 56, 67, 112, 113, 114),
            reciter = reciter,
            packName = "السور الأكثر استماعاً",
            onFinished = onFinished
        )
    }

    fun cancelBatchDownload() {
        batchDownloadJob?.cancel()
        batchDownloadJob = null
        _isBatchDownloading.value = false
        _batchDownloadSurahNumber.value = null
        _batchStatusText.value = "تم إيقاف التحميل"
    }

    fun deleteDownloadedSurah(reciterId: String, surahNumber: Int) {
        val file = getOfflineSurahFile(reciterId, surahNumber)
        if (file.exists()) file.delete()
        refreshDownloadedSurahs(reciterId)
    }

    fun deleteAllDownloadedSurahs(reciterId: String) {
        val dir = java.io.File(context.filesDir, "quran_offline/$reciterId")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
        refreshDownloadedSurahs(reciterId)
    }

    fun selectReciter(reciter: Reciter) {
        _selectedReciter.value = reciter
        refreshDownloadedSurahs(reciter.id)
        val surahNum = _currentSurahNumber.value
        val surahName = _currentSurahName.value
        if (_isVerseByVersePlaying.value && surahNum != null && activeVerseList.isNotEmpty()) {
            val currentAyah = _currentActiveAyahNumber.value ?: 1
            playVerseByVerse(surahNum, surahName ?: "", activeVerseList, currentAyah, reciter)
        } else if (_isFullSurahPlaying.value && surahNum != null && surahName != null) {
            playFullSurah(surahNum, surahName, reciter)
        }
    }

    fun setReciterById(reciterId: String) {
        val found = availableReciters.find { it.id == reciterId }
        if (found != null && found.id != _selectedReciter.value.id) {
            selectReciter(found)
        }
    }

    private fun getAyahCacheFile(reciterId: String, surah: Int, ayah: Int): java.io.File {
        val dir = java.io.File(context.cacheDir, "ayah_audio")
        if (!dir.exists()) dir.mkdirs()
        return java.io.File(dir, "${reciterId}_${surah}_${ayah}.mp3")
    }

    private fun prefetchVersesAhead(reciterId: String, surah: Int, startIndex: Int, count: Int = 2) {
        scope.launch(Dispatchers.IO) {
            for (i in 1..count) {
                val nextIdx = startIndex + i
                if (nextIdx < activeVerseList.size) {
                    val ayahNum = activeVerseList[nextIdx].ayahNumber
                    try {
                        val file = getAyahCacheFile(reciterId, surah, ayahNum)
                        if (!file.exists() || file.length() < 1024) {
                            val urlStr = IslamicDataProvider.getAyahAudioUrl(reciterId, surah, ayahNum)
                            val conn = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                            conn.connectTimeout = 4000
                            conn.readTimeout = 6000
                            if (conn.responseCode == 200) {
                                val tempFile = java.io.File(file.parentFile, "${file.name}.tmp")
                                conn.inputStream.use { input ->
                                    java.io.FileOutputStream(tempFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (tempFile.length() > 1024) {
                                    tempFile.renameTo(file)
                                } else {
                                    tempFile.delete()
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun playAyahAudio(reciterId: String, surah: Int, ayahNum: Int, title: String) {
        val cacheFile = getAyahCacheFile(reciterId, surah, ayahNum)
        if (cacheFile.exists() && cacheFile.length() > 1024) {
            playAudioInternal(cacheFile.absolutePath, title)
            prefetchVersesAhead(reciterId, surah, activeVerseIndex)
            return
        }

        // Dedicated offline support for Fares Abbad (Surah Al-Fatiha bundled in app)
        if (reciterId == "frs_a" && surah == 1) {
            playRawResource(R.raw.fares_abbad_fatiha, title)
            return
        }

        val url = IslamicDataProvider.getAyahAudioUrl(reciterId, surah, ayahNum)
        val fallback = if (reciterId == "frs_a") R.raw.fares_abbad_fatiha else null
        playAudioInternal(url, title, fallbackRawResId = fallback)
        scope.launch(Dispatchers.IO) {
            try {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 6000
                if (conn.responseCode == 200) {
                    val tempFile = java.io.File(cacheFile.parentFile, "${cacheFile.name}.tmp")
                    conn.inputStream.use { input ->
                        java.io.FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempFile.length() > 1024) {
                        tempFile.renameTo(cacheFile)
                    } else {
                        tempFile.delete()
                    }
                }
            } catch (_: Exception) {}
        }
        prefetchVersesAhead(reciterId, surah, activeVerseIndex)
    }

    fun playVerseByVerse(
        surahNumber: Int,
        surahName: String,
        verses: List<Ayah>,
        startAyahNumber: Int = 1,
        reciter: Reciter = _selectedReciter.value
    ) {
        if (verses.isEmpty()) return
        activeVerseList = verses
        activeSurahNumberForTracking = surahNumber
        activeSurahNameForTracking = surahName
        val foundIndex = verses.indexOfFirst { it.ayahNumber == startAyahNumber }.let { if (it >= 0) it else 0 }
        activeVerseIndex = foundIndex
        val targetAyah = verses[foundIndex]

        _currentSurahNumber.value = surahNumber
        _currentSurahName.value = surahName
        _selectedReciter.value = reciter
        _isVerseByVersePlaying.value = true
        _isFullSurahPlaying.value = false
        _isAdhanPlaying.value = false
        _isIqamaPlaying.value = false
        _currentActiveAyahNumber.value = targetAyah.ayahNumber

        val title = "سورة $surahName - آية ${targetAyah.ayahNumber} - القارئ ${reciter.nameArabic}"
        playAyahAudio(reciter.id, surahNumber, targetAyah.ayahNumber, title)
    }

    fun playNextAyah() {
        if (!_isVerseByVersePlaying.value || activeVerseList.isEmpty()) return
        val nextIndex = activeVerseIndex + 1
        if (nextIndex < activeVerseList.size) {
            activeVerseIndex = nextIndex
            val nextAyah = activeVerseList[nextIndex]
            _currentActiveAyahNumber.value = nextAyah.ayahNumber
            val reciter = _selectedReciter.value
            val title = "سورة $activeSurahNameForTracking - آية ${nextAyah.ayahNumber} - القارئ ${reciter.nameArabic}"
            playAyahAudio(reciter.id, activeSurahNumberForTracking, nextAyah.ayahNumber, title)
        }
    }

    fun playPreviousAyah() {
        if (!_isVerseByVersePlaying.value || activeVerseList.isEmpty()) return
        val prevIndex = activeVerseIndex - 1
        if (prevIndex >= 0) {
            activeVerseIndex = prevIndex
            val prevAyah = activeVerseList[prevIndex]
            _currentActiveAyahNumber.value = prevAyah.ayahNumber
            val reciter = _selectedReciter.value
            val title = "سورة $activeSurahNameForTracking - آية ${prevAyah.ayahNumber} - القارئ ${reciter.nameArabic}"
            playAyahAudio(reciter.id, activeSurahNumberForTracking, prevAyah.ayahNumber, title)
        }
    }

    fun stopVerseByVerse() {
        _isVerseByVersePlaying.value = false
        _currentActiveAyahNumber.value = null
        stopAudio()
    }

    fun selectAdhanSound(adhan: AdhanSound) {
        _selectedAdhanSound.value = adhan
    }

    fun selectIqamaSound(iqama: IqamaSound) {
        _selectedIqamaSound.value = iqama
    }

    fun playAdhan(adhan: AdhanSound = _selectedAdhanSound.value) {
        _selectedAdhanSound.value = adhan
        _isFullSurahPlaying.value = false
        _isIqamaPlaying.value = false
        _isAdhanPlaying.value = true
        val title = "${adhan.nameArabic} - ${adhan.muadhinOrPlace}"
        if (adhan.rawResId != null) {
            playRawResource(adhan.rawResId, title, isAdhan = true)
        } else {
            playAudioInternal(adhan.audioUrl, title, isAdhan = true, fallbackRawResId = null)
        }
    }

    fun playIqama(iqama: IqamaSound = _selectedIqamaSound.value) {
        _selectedIqamaSound.value = iqama
        _isFullSurahPlaying.value = false
        _isAdhanPlaying.value = false
        _isIqamaPlaying.value = true
        val title = "إقامة الصلاة - ${iqama.nameArabic}"
        if (iqama.rawResId != null) {
            playRawResource(iqama.rawResId, title, isIqama = true)
        } else {
            playAudioInternal(iqama.audioUrl, title, isIqama = true, fallbackRawResId = R.raw.iqama)
        }
    }

    fun playRawResource(
        resId: Int,
        title: String,
        isAdhan: Boolean = false,
        isIqama: Boolean = false
    ) {
        try {
            stopAudioInternal(resetSurahInfo = false)
            _isAdhanPlaying.value = isAdhan
            _isIqamaPlaying.value = isIqama
            val key = "raw://$resId"
            _playbackState.value = PlaybackState.Buffering(key, title)

            val afd = context.resources.openRawResourceFd(resId)
                ?: throw IllegalStateException("Resource not accessible")

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setVolume(1.0f, 1.0f)
                setOnPreparedListener { mp ->
                    mp.start()
                    _duration.value = mp.duration.toLong()
                    _playbackState.value = PlaybackState.Playing(key, title)
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stopProgressTracker()
                    _playbackState.value = PlaybackState.Idle
                    _currentPosition.value = 0L
                    _isFullSurahPlaying.value = false
                    _isAdhanPlaying.value = false
                    _isIqamaPlaying.value = false
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressTracker()
                    _playbackState.value = PlaybackState.Error("خطأ في تشغيل الصوت المحلي")
                    _isAdhanPlaying.value = false
                    _isIqamaPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            stopProgressTracker()
            _playbackState.value = PlaybackState.Error("تعذر تشغيل الصوت: ${e.localizedMessage}")
            _isAdhanPlaying.value = false
            _isIqamaPlaying.value = false
        }
    }

    fun playFullSurah(
        surahNumber: Int,
        surahName: String,
        reciter: Reciter = _selectedReciter.value
    ) {
        _currentSurahNumber.value = surahNumber
        _currentSurahName.value = surahName
        _isFullSurahPlaying.value = true
        _isAdhanPlaying.value = false
        _isIqamaPlaying.value = false

        // 1. Instant offline playback for Fares Abbad bundled surahs
        if (reciter.id == "frs_a") {
            when (surahNumber) {
                1 -> {
                    playRawResource(R.raw.fares_abbad_fatiha, "سورة الفاتحة - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                108 -> {
                    playRawResource(R.raw.fares_abbad_108, "سورة الكوثر - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                109 -> {
                    playRawResource(R.raw.fares_abbad_109, "سورة الكافرون - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                110 -> {
                    playRawResource(R.raw.fares_abbad_110, "سورة النصر - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                111 -> {
                    playRawResource(R.raw.fares_abbad_111, "سورة المسد - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                112 -> {
                    playRawResource(R.raw.fares_abbad_112, "سورة الإخلاص - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                113 -> {
                    playRawResource(R.raw.fares_abbad_113, "سورة الفلق - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
                114 -> {
                    playRawResource(R.raw.fares_abbad_114, "سورة الناس - القارئ ${reciter.nameArabic} (أوفلاين)")
                    return
                }
            }
        }

        // 2. Instant offline playback if surah file is downloaded
        val offlineFile = getOfflineSurahFile(reciter.id, surahNumber)
        if (offlineFile.exists() && offlineFile.length() > 10_000L) {
            val title = "سورة $surahName - القارئ ${reciter.nameArabic} (أوفلاين بدون نت)"
            playAudioInternal(offlineFile.absolutePath, title)
            return
        }

        val url = String.format(Locale.US, reciter.serverUrlTemplate, surahNumber)
        val title = "سورة $surahName - القارئ ${reciter.nameArabic}"
        val fallback = if (reciter.id == "frs_a") R.raw.fares_abbad_fatiha else null
        playAudioInternal(url, title, fallbackRawResId = fallback)

        // 3. Auto-cache this surah in background so future playback is 100% offline
        if (!isSurahOfflineAvailable(reciter.id, surahNumber)) {
            downloadSingleSurah(surahNumber, reciter)
        }
    }

    fun playAudio(url: String, title: String = "") {
        _isFullSurahPlaying.value = false
        _isAdhanPlaying.value = false
        _isIqamaPlaying.value = false
        playAudioInternal(url, title)
    }

    private fun playAudioInternal(
        url: String,
        title: String,
        isAdhan: Boolean = false,
        isIqama: Boolean = false,
        fallbackRawResId: Int? = null
    ) {
        if (url.isBlank()) {
            if (fallbackRawResId != null) {
                playRawResource(fallbackRawResId, title, isAdhan, isIqama)
            }
            return
        }
        try {
            if (_playbackState.value is PlaybackState.Playing && (mediaPlayer?.isPlaying == true)) {
                val currentUrl = (_playbackState.value as PlaybackState.Playing).url
                if (currentUrl == url) {
                    mediaPlayer?.pause()
                    _playbackState.value = PlaybackState.Paused(url, title)
                    stopProgressTracker()
                    return
                }
            } else if (_playbackState.value is PlaybackState.Paused && (_playbackState.value as PlaybackState.Paused).url == url) {
                mediaPlayer?.start()
                _playbackState.value = PlaybackState.Playing(url, title)
                startProgressTracker()
                return
            }

            stopAudioInternal(resetSurahInfo = false)
            _isAdhanPlaying.value = isAdhan
            _isIqamaPlaying.value = isIqama
            _playbackState.value = PlaybackState.Buffering(url, title)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, android.net.Uri.parse(url))
                setVolume(1.0f, 1.0f)
                setOnPreparedListener { mp ->
                    mp.start()
                    _duration.value = mp.duration.toLong()
                    _playbackState.value = PlaybackState.Playing(url, title)
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stopProgressTracker()
                    if (_isVerseByVersePlaying.value && activeVerseList.isNotEmpty()) {
                        val nextIndex = activeVerseIndex + 1
                        if (nextIndex < activeVerseList.size) {
                            activeVerseIndex = nextIndex
                            val nextAyah = activeVerseList[nextIndex]
                            _currentActiveAyahNumber.value = nextAyah.ayahNumber
                            val reciter = _selectedReciter.value
                            val title = "سورة $activeSurahNameForTracking - آية ${nextAyah.ayahNumber} - القارئ ${reciter.nameArabic}"
                            playAyahAudio(reciter.id, activeSurahNumberForTracking, nextAyah.ayahNumber, title)
                            return@setOnCompletionListener
                        } else {
                            val finishedSurahNum = activeSurahNumberForTracking
                            _playbackState.value = PlaybackState.Idle
                            _currentPosition.value = 0L
                            _isFullSurahPlaying.value = false
                            _isVerseByVersePlaying.value = false
                            _currentActiveAyahNumber.value = null
                            _isAdhanPlaying.value = false
                            _isIqamaPlaying.value = false
                            onSurahRecitationCompleted?.invoke(finishedSurahNum)
                            return@setOnCompletionListener
                        }
                    }
                    _playbackState.value = PlaybackState.Idle
                    _currentPosition.value = 0L
                    _isFullSurahPlaying.value = false
                    _isVerseByVersePlaying.value = false
                    _currentActiveAyahNumber.value = null
                    _isAdhanPlaying.value = false
                    _isIqamaPlaying.value = false
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressTracker()
                    if (fallbackRawResId != null) {
                        playRawResource(fallbackRawResId, title, isAdhan, isIqama)
                    } else {
                        _playbackState.value = PlaybackState.Error("تعذر تشغيل الصوت عبر الإنترنت، يرجى التحقق من الشبكة")
                        _isAdhanPlaying.value = false
                        _isIqamaPlaying.value = false
                        _isFullSurahPlaying.value = false
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            stopProgressTracker()
            if (fallbackRawResId != null) {
                playRawResource(fallbackRawResId, title, isAdhan, isIqama)
            } else {
                _playbackState.value = PlaybackState.Error("خطأ في الصوت: ${e.localizedMessage}")
                _isAdhanPlaying.value = false
                _isIqamaPlaying.value = false
                _isFullSurahPlaying.value = false
            }
        }
    }

    fun togglePlayPause() {
        val state = _playbackState.value
        if (state is PlaybackState.Playing) {
            mediaPlayer?.pause()
            _playbackState.value = PlaybackState.Paused(state.url, state.title)
            stopProgressTracker()
        } else if (state is PlaybackState.Paused) {
            mediaPlayer?.start()
            _playbackState.value = PlaybackState.Playing(state.url, state.title)
            startProgressTracker()
        }
    }

    fun seekTo(positionMillis: Long) {
        try {
            val clamped = positionMillis.coerceIn(0L, _duration.value.coerceAtLeast(0L))
            mediaPlayer?.seekTo(clamped.toInt())
            _currentPosition.value = clamped
        } catch (_: Exception) {}
    }

    fun forward10Seconds() {
        seekTo(_currentPosition.value + 10_000L)
    }

    fun rewind10Seconds() {
        seekTo(_currentPosition.value - 10_000L)
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition.toLong()
                        if (_duration.value <= 0L && mp.duration > 0) {
                            _duration.value = mp.duration.toLong()
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stopAudio() {
        stopAudioInternal(resetSurahInfo = true)
    }

    private fun stopAudioInternal(resetSurahInfo: Boolean) {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = PlaybackState.Idle
        _currentPosition.value = 0L
        _duration.value = 0L
        if (resetSurahInfo) {
            _isFullSurahPlaying.value = false
            _isVerseByVersePlaying.value = false
            _currentActiveAyahNumber.value = null
            activeVerseList = emptyList()
            activeVerseIndex = 0
            _isAdhanPlaying.value = false
            _isIqamaPlaying.value = false
            _currentSurahNumber.value = null
            _currentSurahName.value = null
        }
    }

    fun formatMillis(millis: Long): String {
        if (millis <= 0) return "00:00"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
