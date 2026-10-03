package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.example.MainActivity
import com.example.data.local.AcefDatabase
import com.example.data.local.IslamicDataProvider
import com.example.service.AdhanPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        // Instant synchronous handling for user stop action
        if (action == ACTION_STOP_ADHAN) {
            AdhanPlaybackService.stop(context)
            return
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val prayerTimeStr = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Acef:NotificationReceiverWakeLock")?.apply {
            setReferenceCounted(false)
            acquire(30_000L) // Keep CPU awake for 30s until foreground service is running
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_PRAYER_NOTIFICATION -> {
                        val db = AcefDatabase.getDatabase(context)
                        val settings = db.settingsDao().getSettingsSync()
                        val autoPlay = settings?.autoPlayAdhan ?: true

                        // Trigger Adhan notification
                        NotificationHelper.showPrayerNotification(context, prayerName, prayerTimeStr, autoPlayingAdhan = autoPlay)

                        // Automatically play Adhan audio if enabled
                        if (autoPlay) {
                            val adhanSound = AudioPlayerHelper.availableAdhanSounds.find { it.id == settings?.selectedAdhanId }
                                ?: AudioPlayerHelper.availableAdhanSounds.first()
                            val soundUrl = if (adhanSound.rawResId != null) "raw://${adhanSound.rawResId}" else adhanSound.audioUrl
                            AdhanPlaybackService.startPlayAdhan(context, prayerName, soundUrl)
                        }

                        // Auto-reschedule upcoming prayers so alarms never miss tomorrow even if app is closed
                        rescheduleUpcomingPrayers(context)
                    }

                    ACTION_IQAMA_NOTIFICATION -> {
                        val db = AcefDatabase.getDatabase(context)
                        val settings = db.settingsDao().getSettingsSync()
                        val autoPlay = settings?.autoPlayIqama ?: false
                        val playAdhanAtIqama = settings?.playAdhanOnIqama ?: false

                        // Trigger Iqama notification
                        NotificationHelper.showIqamaNotification(context, prayerName, prayerTimeStr, autoPlayingIqama = autoPlay)

                        // Automatically play audio on phone when Iqama arrives ONLY if enabled
                        if (autoPlay) {
                            if (playAdhanAtIqama) {
                                val adhanSound = AudioPlayerHelper.availableAdhanSounds.find { it.id == settings?.selectedAdhanId }
                                    ?: AudioPlayerHelper.availableAdhanSounds.first()
                                val soundUrl = if (adhanSound.rawResId != null) "raw://${adhanSound.rawResId}" else adhanSound.audioUrl
                                AdhanPlaybackService.startPlayAdhanForIqama(context, prayerName, soundUrl)
                            } else {
                                val iqamaSound = AudioPlayerHelper.availableIqamaSounds.find { it.id == settings?.selectedIqamaId }
                                    ?: AudioPlayerHelper.availableIqamaSounds.first()
                                val soundUrl = if (iqamaSound.rawResId != null) "raw://${iqamaSound.rawResId}" else iqamaSound.audioUrl
                                AdhanPlaybackService.startPlayIqama(context, prayerName, soundUrl)
                            }
                        }

                        // Auto-reschedule upcoming prayers
                        rescheduleUpcomingPrayers(context)
                    }

                    ACTION_MORNING_AZKAR -> {
                        NotificationHelper.showMorningAzkarNotification(context)
                    }

                    ACTION_EVENING_AZKAR -> {
                        NotificationHelper.showEveningAzkarNotification(context)
                    }

                    ACTION_QURAN_REMINDER -> {
                        val dailyVerse = IslamicDataProvider.dailyVerse
                        val hint = "﴿${dailyVerse.textArabic}﴾ [سورة البقرة: ${dailyVerse.ayahNumber}]"
                        NotificationHelper.showQuranReminderNotification(context, hint)
                    }

                    ACTION_TEST_NOTIFICATION -> {
                        val type = intent.getStringExtra("test_type") ?: "prayer"
                        when (type) {
                            "prayer" -> {
                                NotificationHelper.showPrayerNotification(context, "الظهر", "12:45", autoPlayingAdhan = false)
                            }
                            "iqama" -> {
                                NotificationHelper.showIqamaNotification(context, "الظهر", "01:00", autoPlayingIqama = false)
                            }
                            "morning" -> NotificationHelper.showMorningAzkarNotification(context)
                            "evening" -> NotificationHelper.showEveningAzkarNotification(context)
                            "quran" -> NotificationHelper.showQuranReminderNotification(context)
                        }
                    }

                    Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" -> {
                        // Reschedule all alarms after device reboot
                        val db = AcefDatabase.getDatabase(context)
                        val settings = db.settingsDao().getSettingsSync()
                        if (settings != null) {
                            scheduleAllNotifications(
                                context = context,
                                enablePrayer = settings.prayerNotifications,
                                enableMorningAzkar = settings.morningAzkarNotification,
                                enableEveningAzkar = settings.eveningAzkarNotification,
                                enableQuranReminder = settings.quranReminderNotification
                            )
                            rescheduleUpcomingPrayers(context)
                        }
                    }
                }
            } finally {
                try {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_NOTIFICATION = "com.example.ACTION_PRAYER_NOTIFICATION"
        const val ACTION_IQAMA_NOTIFICATION = "com.example.ACTION_IQAMA_NOTIFICATION"
        const val ACTION_MORNING_AZKAR = "com.example.ACTION_MORNING_AZKAR"
        const val ACTION_EVENING_AZKAR = "com.example.ACTION_EVENING_AZKAR"
        const val ACTION_QURAN_REMINDER = "com.example.ACTION_QURAN_REMINDER"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        const val ACTION_TEST_NOTIFICATION = "com.example.ACTION_TEST_NOTIFICATION"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"

        fun canScheduleExact(alarmManager: AlarmManager): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    alarmManager.canScheduleExactAlarms()
                } catch (_: Exception) {
                    false
                }
            } else {
                true
            }
        }

        private fun setAlarmSafely(
            alarmManager: AlarmManager,
            timeInMillis: Long,
            pendingIntent: PendingIntent,
            showIntent: PendingIntent? = null
        ) {
            try {
                val canExact = canScheduleExact(alarmManager)
                if (canExact) {
                    if (showIntent != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val clockInfo = AlarmManager.AlarmClockInfo(timeInMillis, showIntent)
                        alarmManager.setAlarmClock(clockInfo, pendingIntent)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    }
                } else {
                    // Fall back gracefully to inexact while idle without throwing exception
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    }
                }
            } catch (_: SecurityException) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                    }
                } catch (_: Exception) {}
            } catch (_: Exception) {
                try {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } catch (_: Exception) {}
            }
        }

        fun scheduleAllNotifications(
            context: Context,
            enablePrayer: Boolean,
            enableMorningAzkar: Boolean,
            enableEveningAzkar: Boolean,
            enableQuranReminder: Boolean
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // 1. Morning Azkar (06:30 AM daily)
            scheduleDailyAlarm(
                context,
                alarmManager,
                ACTION_MORNING_AZKAR,
                requestCode = 200,
                hour = 6,
                minute = 30,
                enabled = enableMorningAzkar
            )

            // 2. Evening Azkar (17:30 / 5:30 PM daily)
            scheduleDailyAlarm(
                context,
                alarmManager,
                ACTION_EVENING_AZKAR,
                requestCode = 201,
                hour = 17,
                minute = 30,
                enabled = enableEveningAzkar
            )

            // 3. Quran Reminder (20:30 / 8:30 PM daily)
            scheduleDailyAlarm(
                context,
                alarmManager,
                ACTION_QURAN_REMINDER,
                requestCode = 202,
                hour = 20,
                minute = 30,
                enabled = enableQuranReminder
            )
        }

        private fun scheduleDailyAlarm(
            context: Context,
            alarmManager: AlarmManager,
            action: String,
            requestCode: Int,
            hour: Int,
            minute: Int,
            enabled: Boolean
        ) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                this.action = action
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            if (!enabled) {
                alarmManager.cancel(pendingIntent)
                return
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val showIntent = PendingIntent.getActivity(
                context,
                requestCode + 1000,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                flags
            )

            setAlarmSafely(alarmManager, calendar.timeInMillis, pendingIntent, showIntent)
        }

        /**
         * Schedule exact AlarmClock or AllowWhileIdle for Adhan prayer time.
         */
        fun schedulePrayerAlarm(
            context: Context,
            prayerName: String,
            prayerTimeStr: String,
            timeInMillis: Long,
            requestCode: Int = 100
        ) {
            if (timeInMillis <= System.currentTimeMillis()) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_PRAYER_NOTIFICATION
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_TIME, prayerTimeStr)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            val showIntent = PendingIntent.getActivity(
                context,
                requestCode + 2000,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("route", "prayer_times")
                },
                flags
            )

            setAlarmSafely(alarmManager, timeInMillis, pendingIntent, showIntent)
        }

        /**
         * Schedule exact AlarmClock or AllowWhileIdle for Iqama time.
         */
        fun scheduleIqamaAlarm(
            context: Context,
            prayerName: String,
            iqamaTimeStr: String,
            timeInMillis: Long,
            requestCode: Int = 101
        ) {
            if (timeInMillis <= System.currentTimeMillis()) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_IQAMA_NOTIFICATION
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_TIME, iqamaTimeStr)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            val showIntent = PendingIntent.getActivity(
                context,
                requestCode + 3000,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("route", "prayer_times")
                },
                flags
            )

            setAlarmSafely(alarmManager, timeInMillis, pendingIntent, showIntent)
        }

        /**
         * Schedule all 5 daily prayers and their Iqamas with unique request codes
         */
        fun scheduleAllDailyPrayerAlarms(
            context: Context,
            prayers: List<com.example.data.model.PrayerTime>,
            prayerNotificationsEnabled: Boolean,
            iqamaNotificationsEnabled: Boolean,
            autoPlayAdhan: Boolean = true,
            autoPlayIqama: Boolean = true
        ) {
            val now = System.currentTimeMillis()
            val shouldSchedulePrayer = prayerNotificationsEnabled || autoPlayAdhan
            val shouldScheduleIqama = iqamaNotificationsEnabled || autoPlayIqama

            prayers.forEachIndexed { index, prayer ->
                if (prayer.type != com.example.data.model.PrayerType.SUNRISE) {
                    val prayerCode = 110 + index
                    val iqamaCode = 120 + index

                    if (shouldSchedulePrayer) {
                        val targetTime = if (prayer.timestampMillis > now) {
                            prayer.timestampMillis
                        } else {
                            prayer.timestampMillis + 24 * 60 * 60 * 1000L
                        }
                        schedulePrayerAlarm(
                            context = context,
                            prayerName = prayer.type.arabicName,
                            prayerTimeStr = prayer.timeFormatted,
                            timeInMillis = targetTime,
                            requestCode = prayerCode
                        )
                    }

                    if (shouldScheduleIqama) {
                        val targetIqamaTime = if (prayer.iqamaTimestampMillis > now) {
                            prayer.iqamaTimestampMillis
                        } else {
                            prayer.iqamaTimestampMillis + 24 * 60 * 60 * 1000L
                        }
                        scheduleIqamaAlarm(
                            context = context,
                            prayerName = prayer.type.arabicName,
                            iqamaTimeStr = prayer.iqamaTimeFormatted,
                            timeInMillis = targetIqamaTime,
                            requestCode = iqamaCode
                        )
                    }
                }
            }
        }

        /**
         * Test Adhan execution pipeline: launches foreground playback service and shows notification
         */
        fun triggerTestAdhan(context: Context) {
            val db = AcefDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val settings = db.settingsDao().getSettingsSync()
                val adhanSound = AudioPlayerHelper.availableAdhanSounds.find { it.id == settings?.selectedAdhanId }
                    ?: AudioPlayerHelper.availableAdhanSounds.first()
                val soundUrl = if (adhanSound.rawResId != null) "raw://${adhanSound.rawResId}" else adhanSound.audioUrl
                NotificationHelper.showPrayerNotification(context, "الظهر", "12:45", autoPlayingAdhan = true)
                AdhanPlaybackService.startPlayAdhan(context, "الظهر", soundUrl)
            }
        }

        /**
         * Automatically recalculate and reschedule prayers for today and tomorrow.
         * Ensures continuous reliability even when the app is never opened.
         */
        suspend fun rescheduleUpcomingPrayers(context: Context) {
            try {
                val db = AcefDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsSync() ?: return

                val method = try {
                    PrayerCalculator.Method.valueOf(settings.calculationMethod)
                } catch (_: Exception) {
                    PrayerCalculator.Method.ALGERIA
                }
                val juristic = try {
                    PrayerCalculator.Juristic.valueOf(settings.madhhab)
                } catch (_: Exception) {
                    PrayerCalculator.Juristic.SHAFI
                }

                // 1. Calculate today's prayers
                val todayCal = Calendar.getInstance()
                val todayPrayers = PrayerCalculator.calculatePrayerTimes(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    timezone = settings.timezone,
                    date = todayCal,
                    method = method,
                    juristic = juristic,
                    defaultIqamaOffsetMinutes = settings.iqamaDelayMinutes,
                    is24Hour = settings.is24HourFormat
                )

                // 2. Calculate tomorrow's prayers
                val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomorrowPrayers = PrayerCalculator.calculatePrayerTimes(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    timezone = settings.timezone,
                    date = tomorrowCal,
                    method = method,
                    juristic = juristic,
                    defaultIqamaOffsetMinutes = settings.iqamaDelayMinutes,
                    is24Hour = settings.is24HourFormat
                )

                val now = System.currentTimeMillis()
                val shouldPrayer = settings.prayerNotifications || settings.autoPlayAdhan
                val shouldIqama = settings.iqamaNotifications || settings.autoPlayIqama

                // Schedule any remaining prayers today
                todayPrayers.forEachIndexed { index, prayer ->
                    if (prayer.type != com.example.data.model.PrayerType.SUNRISE) {
                        if (shouldPrayer && prayer.timestampMillis > now) {
                            schedulePrayerAlarm(context, prayer.type.arabicName, prayer.timeFormatted, prayer.timestampMillis, 110 + index)
                        }
                        if (shouldIqama && prayer.iqamaTimestampMillis > now) {
                            scheduleIqamaAlarm(context, prayer.type.arabicName, prayer.iqamaTimeFormatted, prayer.iqamaTimestampMillis, 120 + index)
                        }
                    }
                }

                // Schedule tomorrow's prayers as backup
                tomorrowPrayers.forEachIndexed { index, prayer ->
                    if (prayer.type != com.example.data.model.PrayerType.SUNRISE) {
                        if (shouldPrayer && prayer.timestampMillis > now) {
                            schedulePrayerAlarm(context, prayer.type.arabicName, prayer.timeFormatted, prayer.timestampMillis, 130 + index)
                        }
                        if (shouldIqama && prayer.iqamaTimestampMillis > now) {
                            scheduleIqamaAlarm(context, prayer.type.arabicName, prayer.iqamaTimeFormatted, prayer.iqamaTimestampMillis, 140 + index)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
