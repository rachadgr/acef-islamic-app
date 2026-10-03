package com.example.data.model

enum class PrayerType(
    val arabicName: String,
    val englishName: String,
    val frenchName: String,
    val defaultIqamaDelayMinutes: Int
) {
    FAJR("الفجر", "Fajr", "Fajr", 20),
    SUNRISE("الشروق", "Sunrise", "Chourouq", 0),
    DHUHR("الظهر", "Dhuhr", "Dhuhr", 15),
    ASR("العصر", "Asr", "Asr", 15),
    MAGHRIB("المغرب", "Maghrib", "Maghrib", 10),
    ISHA("العشاء", "Isha", "Icha", 15);

    fun getDisplayName(isArabic: Boolean = true): String {
        return if (isArabic) arabicName else "$arabicName ($englishName)"
    }

    fun getDisplayNameByLang(lang: String): String {
        return when (lang) {
            "en" -> englishName
            "fr" -> frenchName
            else -> arabicName
        }
    }
}

data class PrayerTime(
    val type: PrayerType,
    val timeFormatted: String,
    val timestampMillis: Long,
    val iqamaTimeFormatted: String = "",
    val iqamaTimestampMillis: Long = 0L,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val isIqamaNext: Boolean = false,
    val isPassed: Boolean = false
)

data class City(
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

data class GpsLocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val cityNameAr: String = "",
    val countryNameAr: String = "",
    val timezone: Double = 1.0,
    val timestamp: Long = System.currentTimeMillis()
)
