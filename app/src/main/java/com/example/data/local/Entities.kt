package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "QURAN", "HADITH", "AZKAR"
    val title: String,
    val arabicText: String,
    val subtitle: String = "",
    val reference: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_counters")
data class TasbihCountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dhikrText: String,
    val count: Int,
    val target: Int,
    val totalCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val appLanguage: String = "SYSTEM", // "SYSTEM", "ar", "en", "fr"
    val is24HourFormat: Boolean = false, // false = 12h with AM/PM (ص/م), true = 24h
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val colorTheme: String = "EMERALD", // "EMERALD", "NAVY", "MAROON", "MIDNIGHT", "TURQUOISE"
    val numberFormat: String = "FRENCH", // "FRENCH" (1, 2, 3...) or "ARABIC" (١، ٢، ٣...)
    val customGeminiApiKey: String = "",
    val fontSizeScale: Float = 1.0f,
    val cityName: String = "الجزائر العاصمة",
    val latitude: Double = 36.7538,
    val longitude: Double = 3.0588,
    val timezone: Double = 1.0,
    val calculationMethod: String = "ALGERIA", // "ALGERIA", "MWL", "MAKKAH", "EGYPT", "ISNA"
    val madhhab: String = "SHAFI", // "SHAFI", "HANAFI"
    val isGpsEnabled: Boolean = false,
    val gpsAccuracyMeters: Float = 0f,
    // Auto Adhan & Iqama settings
    val autoPlayAdhan: Boolean = true,
    val autoPlayIqama: Boolean = true,
    val playAdhanOnIqama: Boolean = false, // Play Adhan on phone when Iqama arrives (default false to prevent unwanted adhan at iqama)
    val iqamaDelayMinutes: Int = 15,
    val selectedAdhanId: String = "makkah",
    val selectedIqamaId: String = "standard",
    // Floating Azkar Bubble settings
    val floatingAzkarEnabled: Boolean = false,
    val floatingAzkarIntervalSeconds: Int = 30,
    val floatingAzkarAutoRotate: Boolean = true,
    // General notifications & haptics
    val vibrationEnabled: Boolean = true,
    val adhanSoundEnabled: Boolean = true,
    val prayerNotifications: Boolean = true,
    val iqamaNotifications: Boolean = true,
    val morningAzkarNotification: Boolean = true,
    val eveningAzkarNotification: Boolean = true,
    val quranReminderNotification: Boolean = true,
    val dailyHadithNotification: Boolean = true,
    val dailyVerseNotification: Boolean = true,
    // Quran Riwayah (HAFS / WARSH)
    val quranRiwayah: String = "HAFS",
    // Selected Quran Reciter for full Quran reading & recitation
    val selectedQuranReciterId: String = "frs_a"
)

@Entity(tableName = "ai_explanations")
data class AiExplanationEntity(
    @PrimaryKey
    val key: String, // e.g. "AYAH_1_1" or "HADITH_2" or "QUERY_HASH"
    val type: String, // "AYAH" or "HADITH" or "QUERY"
    val title: String,
    val arabicText: String,
    val explanation: String,
    val isAiGenerated: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
