package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.NotificationHelper
import com.example.util.NotificationReceiver

class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Incoming call or high priority sound: immediately stop Adhan
                stopPlayback()
                stopSelf()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }

    companion object {
        const val ACTION_PLAY_ADHAN = "com.example.service.ACTION_PLAY_ADHAN"
        const val ACTION_PLAY_IQAMA = "com.example.service.ACTION_PLAY_IQAMA"
        const val ACTION_PLAY_ADHAN_FOR_IQAMA = "com.example.service.ACTION_PLAY_ADHAN_FOR_IQAMA"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_AUDIO_URL = "extra_audio_url"
        const val EXTRA_IS_IQAMA = "extra_is_iqama"
        const val EXTRA_IS_ADHAN_FOR_IQAMA = "extra_is_adhan_for_iqama"

        // Concurrency lock to prevent multiple overlapping playback
        @Volatile
        var isAdhanPlaybackActive = false

        fun startPlayAdhan(context: Context, prayerName: String, audioUrl: String? = null) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_IS_IQAMA, false)
                putExtra(EXTRA_IS_ADHAN_FOR_IQAMA, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startPlayIqama(context: Context, prayerName: String, audioUrl: String? = null) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY_IQAMA
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_IS_IQAMA, true)
                putExtra(EXTRA_IS_ADHAN_FOR_IQAMA, false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startPlayAdhanForIqama(context: Context, prayerName: String, audioUrl: String? = null) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY_ADHAN_FOR_IQAMA
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_IS_IQAMA, true)
                putExtra(EXTRA_IS_ADHAN_FOR_IQAMA, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        // Stop any running media before starting new one (interlocking)
        stopPlaybackOnly()

        acquireWakeLock()
        requestAudioFocus()

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val isIqama = intent?.getBooleanExtra(EXTRA_IS_IQAMA, false) ?: false
        val isAdhanForIqama = intent?.getBooleanExtra(EXTRA_IS_ADHAN_FOR_IQAMA, false) ?: (action == ACTION_PLAY_ADHAN_FOR_IQAMA)
        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)

        val title = when {
            isAdhanForIqama -> "حان الآن وقت إقامة صلاة $prayerName"
            isIqama -> "حان الآن وقت إقامة صلاة $prayerName"
            else -> "حان الآن أذان صلاة $prayerName"
        }
        val subtitle = when {
            isAdhanForIqama -> "الله أكبر.. جاري تشغيل صوت الأذان في الهاتف عند الإقامة"
            isIqama -> "جاري تشغيل تكبيرات وإقامة الصلاة تلقائياً"
            else -> "الله أكبر.. جاري تشغيل الأذان تلقائياً"
        }

        val notification = createPlaybackNotification(title, subtitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIF_ID_ADHAN_PLAYBACK,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NotificationHelper.NOTIF_ID_ADHAN_PLAYBACK, notification)
        }

        // Default audio is strictly offline from res/raw
        val targetResId = if (isIqama && !isAdhanForIqama) R.raw.iqama else R.raw.adhan_makkah

        if (audioUrl.isNullOrBlank() || audioUrl.startsWith("raw://")) {
            val customResId = if (audioUrl != null && audioUrl.startsWith("raw://")) {
                audioUrl.removePrefix("raw://").toIntOrNull() ?: targetResId
            } else {
                targetResId
            }
            playLocalRawResource(customResId)
        } else {
            playAudio(audioUrl, fallbackResId = targetResId)
        }

        // Resilient: redeliver intent if killed by system
        return START_REDELIVER_INTENT
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Acef:AdhanPlaybackWakeLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes maximum timeout
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun requestAudioFocus() {
        try {
            if (audioManager == null) {
                audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                audioManager?.requestAudioFocus(focusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Exception) {}
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (_: Exception) {}
    }

    private fun playLocalRawResource(resId: Int) {
        stopPlaybackOnly()
        try {
            val afd = resources.openRawResourceFd(resId) ?: run {
                stopSelf()
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnPreparedListener { mp ->
                    isAdhanPlaybackActive = true
                    mp.start()
                }
                setOnCompletionListener {
                    stopPlayback()
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    stopSelf()
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            stopPlayback()
            stopSelf()
        }
    }

    private var prepareTimeoutHandler: android.os.Handler? = null
    private var prepareTimeoutRunnable: Runnable? = null

    private fun playAudio(url: String, fallbackResId: Int) {
        stopPlaybackOnly()
        try {
            var isPrepared = false
            prepareTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
            prepareTimeoutRunnable = Runnable {
                if (!isPrepared) {
                    // Network too slow or offline; immediately switch to local offline audio
                    playLocalRawResource(fallbackResId)
                }
            }
            prepareTimeoutHandler?.postDelayed(prepareTimeoutRunnable!!, 3500L) // 3.5s timeout

            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(applicationContext, android.net.Uri.parse(url))
                setOnPreparedListener { mp ->
                    isPrepared = true
                    isAdhanPlaybackActive = true
                    prepareTimeoutRunnable?.let { prepareTimeoutHandler?.removeCallbacks(it) }
                    mp.start()
                }
                setOnCompletionListener {
                    stopPlayback()
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to local raw resource if network stream fails
                    prepareTimeoutRunnable?.let { prepareTimeoutHandler?.removeCallbacks(it) }
                    playLocalRawResource(fallbackResId)
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            prepareTimeoutRunnable?.let { prepareTimeoutHandler?.removeCallbacks(it) }
            playLocalRawResource(fallbackResId)
        }
    }

    private fun stopPlaybackOnly() {
        try {
            prepareTimeoutRunnable?.let { prepareTimeoutHandler?.removeCallbacks(it) }
            prepareTimeoutRunnable = null
            prepareTimeoutHandler = null
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isAdhanPlaybackActive = false
    }

    private fun stopPlayback() {
        stopPlaybackOnly()
        abandonAudioFocus()
        releaseWakeLock()
    }

    private fun createPlaybackNotification(title: String, subtitle: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "prayer_times")
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            8001,
            openIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Use BroadcastReceiver to stop playback instead of PendingIntent.getService
        val stopBroadcastIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            8002,
            stopBroadcastIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopLabel = try {
            getString(R.string.stop_audio)
        } catch (_: Exception) {
            "إيقاف الصوت"
        }

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ADHAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, stopLabel, stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}
