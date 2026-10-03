package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.service.AdhanPlaybackService

object NotificationHelper {
    const val CHANNEL_ADHAN = "channel_adhan_prayer"
    const val CHANNEL_IQAMA = "channel_iqama_prayer"
    const val CHANNEL_AZKAR = "channel_azkar_reminders"
    const val CHANNEL_QURAN = "channel_quran_reminder"
    const val CHANNEL_FLOATING_SERVICE = "channel_floating_azkar_service"

    const val NOTIF_ID_PRAYER = 1001
    const val NOTIF_ID_IQAMA = 1002
    const val NOTIF_ID_MORNING_AZKAR = 2001
    const val NOTIF_ID_EVENING_AZKAR = 2002
    const val NOTIF_ID_QURAN = 3001
    const val NOTIF_ID_HADITH = 4001
    const val NOTIF_ID_FLOATING_SERVICE = 5001
    const val NOTIF_ID_ADHAN_PLAYBACK = 6001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Channel for Adhan
            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN,
                "تنبيهات الأذان ومواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات دخول أوقات الصلوات المفروضة مع تشغيل الأذان تلقائياً"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            // 2. Channel for Iqama
            val iqamaChannel = NotificationChannel(
                CHANNEL_IQAMA,
                "تنبيهات وقت الإقامة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات وصول وقت إقامة الصلاة"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
            }

            // 3. Channel for Morning & Evening Azkar
            val azkarChannel = NotificationChannel(
                CHANNEL_AZKAR,
                "أذكار الصباح والمساء واليوم",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير يومي مبارك بأذكار الصباح والمساء"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
            }

            // 4. Channel for Quran Reading Reminder
            val quranChannel = NotificationChannel(
                CHANNEL_QURAN,
                "تذكير الورد القرآني والتدبر",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير يومي لتلاوة القرآن وتدبر آياته"
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
            }

            // 5. Channel for Floating Azkar Service
            val floatingChannel = NotificationChannel(
                CHANNEL_FLOATING_SERVICE,
                "فقاعة الأذكار العائمة فوق التطبيقات",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "خدمة عرض نافذة الأذكار العائمة أثناء استخدام التطبيقات الأخرى"
            }

            notificationManager.createNotificationChannels(
                listOf(adhanChannel, iqamaChannel, azkarChannel, quranChannel, floatingChannel)
            )
        }
    }

    private fun getPendingIntent(context: Context, route: String = "home", notifId: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", route)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, notifId, intent, flags)
    }

    /**
     * Show notification for Adhan / Prayer Time
     */
    fun showPrayerNotification(context: Context, prayerName: String, prayerTimeStr: String, autoPlayingAdhan: Boolean = false) {
        val pendingIntent = getPendingIntent(context, "prayer_times", NOTIF_ID_PRAYER)

        val stopBroadcastIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_STOP_ADHAN
        }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, 7001, stopBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 7001, stopBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ADHAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان الآن وقت أذان $prayerName")
            .setContentText("موعد صلاة $prayerName ($prayerTimeStr) ${if (autoPlayingAdhan) "• جاري تشغيل الأذان تلقائياً" else ""}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("الله أكبر، حان الآن موعد أذان $prayerName ($prayerTimeStr).\nحي على الصلاة.. حي على الفلاح.\n${if (autoPlayingAdhan) "🔊 يتم تشغيل الأذان تلقائياً الآن." else ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (autoPlayingAdhan) {
            builder.addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopPendingIntent)
        }

        notifySafely(context, NOTIF_ID_PRAYER, builder)
    }

    /**
     * Show notification for Iqama Time
     */
    fun showIqamaNotification(context: Context, prayerName: String, iqamaTimeStr: String, autoPlayingIqama: Boolean = false) {
        val pendingIntent = getPendingIntent(context, "prayer_times", NOTIF_ID_IQAMA)

        val stopBroadcastIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_STOP_ADHAN
        }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, 7002, stopBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 7002, stopBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_IQAMA)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان الآن موعد إقامة صلاة $prayerName")
            .setContentText("أُقيمت صلاة $prayerName ($iqamaTimeStr) ${if (autoPlayingIqama) "• جاري تشغيل التكبيرات" else ""}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("قد قامت الصلاة.. قد قامت الصلاة. حان الآن موعد إقامة صلاة $prayerName ($iqamaTimeStr). تقبل الله طاعتكم.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (autoPlayingIqama) {
            builder.addAction(android.R.drawable.ic_media_pause, "إيقاف الصوت", stopPendingIntent)
        }

        notifySafely(context, NOTIF_ID_IQAMA, builder)
    }

    /**
     * Show Morning Azkar Reminder
     */
    fun showMorningAzkarNotification(context: Context) {
        val pendingIntent = getPendingIntent(context, "azkar", NOTIF_ID_MORNING_AZKAR)
        val dhikrSample = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ"
        val builder = NotificationCompat.Builder(context, CHANNEL_AZKAR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("أذكار الصباح المباركة")
            .setContentText("ابدأ صباحك بذكر الله: $dhikrSample")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.\n\nاضغط لقراءة أذكار الصباح وحصن نفسك.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notifySafely(context, NOTIF_ID_MORNING_AZKAR, builder)
    }

    /**
     * Show Evening Azkar Reminder
     */
    fun showEveningAzkarNotification(context: Context) {
        val pendingIntent = getPendingIntent(context, "azkar", NOTIF_ID_EVENING_AZKAR)
        val dhikrSample = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ"
        val builder = NotificationCompat.Builder(context, CHANNEL_AZKAR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("أذكار المساء وحفظ النفس")
            .setContentText("حصن نفسك بذكر الله: $dhikrSample")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.\n\nاضغط لقراءة أذكار المساء المباركة.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notifySafely(context, NOTIF_ID_EVENING_AZKAR, builder)
    }

    /**
     * Show Quran Reading Reminder
     */
    fun showQuranReminderNotification(context: Context, verseHint: String? = null) {
        val pendingIntent = getPendingIntent(context, "quran", NOTIF_ID_QURAN)
        val defaultHint = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾ [الرعد: 28]"
        val textToDisplay = verseHint ?: defaultHint
        val builder = NotificationCompat.Builder(context, CHANNEL_QURAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("تذكير الورد اليومي من القرآن الكريم")
            .setContentText("وردك اليومي: $textToDisplay")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$textToDisplay\n\nلا تهجر القرآن الكريم اليوم، واقرأ ما تيسر مع التدبر والتفسير.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notifySafely(context, NOTIF_ID_QURAN, builder)
    }

    private fun notifySafely(context: Context, id: Int, builder: NotificationCompat.Builder) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(id, builder.build())
            }
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }
}
