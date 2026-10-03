package com.example.util

import com.example.data.model.PrayerTime
import com.example.data.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

object PrayerCalculator {

    enum class Method(val arabicName: String, val fajrAngle: Double, val ishaAngle: Double, val ishaIntervalMin: Int = 0) {
        ALGERIA("وزارة الشؤون الدينية (الجزائر)", 18.0, 17.0),
        MWL("رابطة العالم الإسلامي", 18.0, 17.0),
        MAKKAH("جامعة أم القرى (مكة المكرمة)", 18.5, 0.0, ishaIntervalMin = 90),
        EGYPT("الهيئة المصرية العامة للمساحة", 19.5, 17.5),
        ISNA("الجمعية الإسلامية لأمريكا الشمالية (ISNA)", 15.0, 15.0)
    }

    enum class Juristic(val arabicName: String, val shadowFactor: Int) {
        SHAFI("الشافعي والمالكي والحنبلي", 1),
        HANAFI("الحنفي", 2)
    }

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        timezone: Double,
        date: Calendar = Calendar.getInstance(),
        method: Method = Method.ALGERIA,
        juristic: Juristic = Juristic.SHAFI,
        defaultIqamaOffsetMinutes: Int = 15,
        is24Hour: Boolean = false,
        isArabicLocale: Boolean = true
    ): List<PrayerTime> {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)

        val jd = julianDay(year, month, day) - (longitude / (15.0 * 24.0))
        val t = (jd - 2451545.0) / 36525.0

        val l0 = fixAngle(280.46646 + 36000.76983 * t)
        val m = fixAngle(357.52911 + 35999.05029 * t)
        val c = (1.914602 - 0.004817 * t) * sin(Math.toRadians(m)) + (0.019993 - 0.000101 * t) * sin(Math.toRadians(2 * m))
        val sunTrueLong = fixAngle(l0 + c)

        val obliq = 23.439291 - 0.0130042 * t
        val declination = Math.toDegrees(asin(sin(Math.toRadians(obliq)) * sin(Math.toRadians(sunTrueLong))))

        val y = tan(Math.toRadians(obliq / 2.0)).pow(2)
        val eot = 4.0 * Math.toDegrees(
            y * sin(2 * Math.toRadians(l0))
                    - 2 * 0.016708634 * sin(Math.toRadians(m))
                    + 4 * 0.016708634 * y * sin(Math.toRadians(m)) * cos(2 * Math.toRadians(l0))
                    - 0.5 * y * y * sin(4 * Math.toRadians(l0))
                    - 1.25 * 0.016708634 * 0.016708634 * sin(2 * Math.toRadians(m))
        )

        val noon = 12.0 + timezone - (longitude / 15.0) - (eot / 60.0)

        val sunAngle = 90.833
        val sunriseHourAngle = hourAngle(latitude, declination, sunAngle)
        val sunrise = noon - (sunriseHourAngle / 15.0)
        val sunset = noon + (sunriseHourAngle / 15.0)

        val fajrHourAngle = hourAngle(latitude, declination, 90.0 + method.fajrAngle)
        val fajr = noon - (fajrHourAngle / 15.0)

        val asrAltitude = Math.toDegrees(atan(1.0 / (juristic.shadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = hourAngle(latitude, declination, 90.0 - asrAltitude)
        val asr = noon + (asrHourAngle / 15.0)

        val maghrib = sunset + (2.0 / 60.0)

        val isha = if (method.ishaIntervalMin > 0) {
            maghrib + (method.ishaIntervalMin / 60.0)
        } else {
            val ishaHourAngle = hourAngle(latitude, declination, 90.0 + method.ishaAngle)
            noon + (ishaHourAngle / 15.0)
        }

        val rawTimes = listOf(
            PrayerType.FAJR to fajr,
            PrayerType.SUNRISE to sunrise,
            PrayerType.DHUHR to noon,
            PrayerType.ASR to asr,
            PrayerType.MAGHRIB to maghrib,
            PrayerType.ISHA to isha
        )

        val now = System.currentTimeMillis()
        val calendarBase = (date.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var lastCurrentIndex = -1
        var nextPrayerIndex = -1

        val listWithMillis = rawTimes.mapIndexed { index, (type, decimalHour) ->
            val hour = decimalHour.toInt()
            val minute = ((decimalHour - hour) * 60).roundToInt().coerceIn(0, 59)

            val prayerCal = (calendarBase.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val prayerMillis = prayerCal.timeInMillis

            // Iqama offset: Fajr 20min, Maghrib 10min, others default (e.g. 15min). Sunrise has no Iqama.
            val iqamaOffsetMinutes = if (type == PrayerType.SUNRISE) 0 else {
                when (type) {
                    PrayerType.FAJR -> maxOf(defaultIqamaOffsetMinutes, 20)
                    PrayerType.MAGHRIB -> minOf(defaultIqamaOffsetMinutes, 10)
                    else -> defaultIqamaOffsetMinutes
                }
            }

            val iqamaMillis = if (type == PrayerType.SUNRISE) 0L else prayerMillis + (iqamaOffsetMinutes * 60 * 1000L)
            val iqamaFormatted = if (type == PrayerType.SUNRISE) "" else {
                val iqamaCal = Calendar.getInstance().apply { timeInMillis = iqamaMillis }
                formatTime(iqamaCal.get(Calendar.HOUR_OF_DAY), iqamaCal.get(Calendar.MINUTE), is24Hour, isArabicLocale)
            }

            data class TempItem(
                val type: PrayerType,
                val timeStr: String,
                val millis: Long,
                val iqamaTimeStr: String,
                val iqamaMillis: Long
            )
            TempItem(type, formatTime(hour, minute, is24Hour, isArabicLocale), prayerMillis, iqamaFormatted, iqamaMillis)
        }

        // Determine current prayer and next prayer
        for (i in listWithMillis.indices) {
            if (now >= listWithMillis[i].millis) {
                lastCurrentIndex = i
            } else {
                if (nextPrayerIndex == -1) {
                    nextPrayerIndex = i
                }
            }
        }
        if (nextPrayerIndex == -1) {
            nextPrayerIndex = 0 // loop to Fajr tomorrow
        }

        return listWithMillis.mapIndexed { index, item ->
            val isNext = index == nextPrayerIndex
            val isCurrent = index == lastCurrentIndex
            val isIqamaNext = isCurrent && item.iqamaMillis > now

            PrayerTime(
                type = item.type,
                timeFormatted = item.timeStr,
                timestampMillis = item.millis,
                iqamaTimeFormatted = item.iqamaTimeStr,
                iqamaTimestampMillis = item.iqamaMillis,
                isNext = isNext,
                isCurrent = isCurrent,
                isIqamaNext = isIqamaNext
            )
        }
    }

    private fun hourAngle(lat: Double, dec: Double, zenith: Double): Double {
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val zenRad = Math.toRadians(zenith)
        val cosH = (cos(zenRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        return Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0)))
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        val b = a - 360.0 * floor(a / 360.0)
        return if (b < 0) b + 360.0 else b
    }

    fun formatTime(
        hour: Int,
        minute: Int,
        is24Hour: Boolean = false,
        isArabicLocale: Boolean = true
    ): String {
        return if (is24Hour) {
            String.format(Locale.US, "%02d:%02d", hour, minute)
        } else {
            val h = if (hour % 12 == 0) 12 else hour % 12
            val amPm = if (hour < 12) (if (isArabicLocale) "ص" else "AM") else (if (isArabicLocale) "م" else "PM")
            String.format(Locale.US, "%02d:%02d %s", h, minute, amPm)
        }
    }

    fun getCountdownTo(targetMillis: Long): String {
        val diff = targetMillis - System.currentTimeMillis()
        if (diff <= 0) return "00:00:00"
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
