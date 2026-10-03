package com.example.util

import java.util.Calendar
import kotlin.math.floor

object HijriCalendarHelper {

    private val hijriMonthsArabic = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val hijriMonthsEnglish = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val hijriMonthsFrench = listOf(
        "Mouharram", "Safar", "Rabi al-Awwal", "Rabi ath-Thani",
        "Joumada al-Oula", "Joumada al-Akhira", "Rajab", "Cha'bane",
        "Ramadan", "Chawwal", "Dhou al-Qi'da", "Dhou al-Hijja"
    )

    private val arabicDayNames = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )
    private val englishDayNames = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )
    private val frenchDayNames = listOf(
        "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
    )

    private val gregorianMonthsArabic = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    private val gregorianMonthsEnglish = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val gregorianMonthsFrench = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )

    data class HijriDate(
        val day: Int,
        val monthNumber: Int,
        val monthNameAr: String,
        val monthNameEn: String,
        val monthNameFr: String,
        val year: Int,
        val dayNameAr: String,
        val dayNameEn: String,
        val dayNameFr: String
    ) {
        fun formatted(lang: String = "ar"): String {
            return when (lang) {
                "en" -> "$dayNameEn, $day $monthNameEn $year AH"
                "fr" -> "$dayNameFr $day $monthNameFr $year H"
                else -> "$dayNameAr $day $monthNameAr $year هـ"
            }
        }
    }

    fun getHijriDate(cal: Calendar = Calendar.getInstance()): HijriDate {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) // 0-based
        val year = cal.get(Calendar.YEAR)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday

        var m = month + 1
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0)
        val l1 = l - 10631 * n + 354
        val j = (floor((10985 - l1) / 5316.0)) * (floor((50 * l1) / 17719.0)) +
                (floor(l1 / 5670.0)) * (floor((43 * l1) / 15238.0))
        val l2 = l1 - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = floor((24 * l2) / 709.0).toInt()
        val hDay = (l2 - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val monthIndex = (hMonth - 1).coerceIn(0, 11)
        val dayNameIndex = (dayOfWeek - 1).coerceIn(0, 6)

        return HijriDate(
            day = hDay,
            monthNumber = hMonth,
            monthNameAr = hijriMonthsArabic[monthIndex],
            monthNameEn = hijriMonthsEnglish[monthIndex],
            monthNameFr = hijriMonthsFrench[monthIndex],
            year = hYear,
            dayNameAr = arabicDayNames[dayNameIndex],
            dayNameEn = englishDayNames[dayNameIndex],
            dayNameFr = frenchDayNames[dayNameIndex]
        )
    }

    fun getGregorianDateFormatted(cal: Calendar = Calendar.getInstance(), lang: String = "ar"): String {
        val dayNameIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthIndex = cal.get(Calendar.MONTH).coerceIn(0, 11)
        val year = cal.get(Calendar.YEAR)
        return when (lang) {
            "en" -> "${englishDayNames[dayNameIndex]}, ${gregorianMonthsEnglish[monthIndex]} $day, $year"
            "fr" -> "${frenchDayNames[dayNameIndex]} $day ${gregorianMonthsFrench[monthIndex]} $year"
            else -> "${arabicDayNames[dayNameIndex]}، $day ${gregorianMonthsArabic[monthIndex]} $year م"
        }
    }
}
