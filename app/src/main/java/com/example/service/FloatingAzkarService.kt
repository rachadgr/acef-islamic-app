package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.IslamicDataProvider
import com.example.data.model.Dhikr
import com.example.util.NotificationHelper
import kotlin.math.abs

class FloatingAzkarService : Service() {

    private var windowManager: WindowManager? = null
    private var rootView: FrameLayout? = null
    private var bubbleView: FrameLayout? = null
    private var cardView: LinearLayout? = null
    private var tvDhikrText: TextView? = null
    private var tvCategory: TextView? = null
    private var tvFadl: TextView? = null
    private var tvCounterBtn: TextView? = null
    private var tvAutoRotateBtn: TextView? = null

    private val azkarList: List<Dhikr> by lazy { IslamicDataProvider.azkarList }
    private var currentDhikrIndex = 0
    private var currentDhikrCount = 0

    private var isExpanded = false
    private var isAutoRotateActive = true
    private val handler = Handler(Looper.getMainLooper())
    private var autoRotateRunnable: Runnable? = null

    private lateinit var layoutParams: WindowManager.LayoutParams

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START_FLOATING"
        const val ACTION_STOP = "com.example.service.ACTION_STOP_FLOATING"
        const val ACTION_TOGGLE = "com.example.service.ACTION_TOGGLE_FLOATING"

        var isRunning = false
            private set

        fun canDrawOverlay(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun start(context: Context) {
            val intent = Intent(context, FloatingAzkarService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingAzkarService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NotificationHelper.NOTIF_ID_FLOATING_SERVICE, createServiceNotification())
        createFloatingWidget()
        setupAutoRotate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createServiceNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "azkar")
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            9001,
            openIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val closeIntent = Intent(this, FloatingAzkarService::class.java).apply {
            action = ACTION_STOP
        }
        val closePendingIntent = PendingIntent.getService(
            this,
            9002,
            closeIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_FLOATING_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("فقاعة الأذكار العائمة نشطة")
            .setContentText("أذكار المسلم تظهر الآن فوق التطبيقات الأخرى")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق الفقاعة", closePendingIntent)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingWidget() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 350
        }

        rootView = FrameLayout(this)

        // 1. Collapsed Bubble View
        bubbleView = FrameLayout(this).apply {
            val sizePx = dpToPx(60)
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)

            // Circle background with gold border and dark emerald fill
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0F4C35"))
                setStroke(dpToPx(2.5f), Color.parseColor("#D4AF37"))
            }
            background = bg
            elevation = 16f

            val icon = ImageView(this@FloatingAzkarService).apply {
                val iconSize = dpToPx(38)
                val ivParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                    gravity = Gravity.CENTER
                }
                layoutParams = ivParams
                setImageResource(R.drawable.ic_acef_logo)
            }
            addView(icon)
        }

        // 2. Expanded Card View
        cardView = buildExpandedCardView().apply {
            visibility = View.GONE
        }

        rootView?.addView(bubbleView)
        rootView?.addView(cardView)

        setupDragAndClickTouchListener()

        try {
            windowManager?.addView(rootView, layoutParams)
        } catch (_: Exception) {}
    }

    private fun buildExpandedCardView(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val widthPx = dpToPx(320)
            layoutParams = LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT)

            // Card background: deep emerald surface with gold border and rounded corners
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(20).toFloat()
                setColor(Color.parseColor("#09261A"))
                setStroke(dpToPx(1.5f), Color.parseColor("#D4AF37"))
            }
            background = bg
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            elevation = 24f

            // Top Header: Category and Minimize button
            val headerRow = LinearLayout(this@FloatingAzkarService).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            // Close (X) button
            val btnClose = TextView(this@FloatingAzkarService).apply {
                text = "✕"
                setTextColor(Color.parseColor("#E0E0E0"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                setOnClickListener {
                    stopSelf()
                }
            }
            headerRow.addView(btnClose)

            // Category Badge
            tvCategory = TextView(this@FloatingAzkarService).apply {
                text = "أذكار المسلم"
                setTextColor(Color.parseColor("#D4AF37"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(null, Typeface.BOLD)
                val badgeBg = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                    setColor(Color.parseColor("#154F38"))
                }
                background = badgeBg
                setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                ).apply {
                    marginStart = dpToPx(8)
                    marginEnd = dpToPx(8)
                }
                gravity = Gravity.CENTER
            }
            headerRow.addView(tvCategory)

            // Minimize button
            val btnMinimize = TextView(this@FloatingAzkarService).apply {
                text = "⚊"
                setTextColor(Color.parseColor("#D4AF37"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                setOnClickListener {
                    collapseWidget()
                }
            }
            headerRow.addView(btnMinimize)

            addView(headerRow)

            // Dhikr Arabic Text Box
            tvDhikrText = TextView(this@FloatingAzkarService).apply {
                val current = azkarList.getOrNull(currentDhikrIndex)
                text = current?.text ?: "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setLineSpacing(0f, 1.25f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(12)
                    bottomMargin = dpToPx(8)
                }
            }
            addView(tvDhikrText)

            // Virtue / Fadl text
            tvFadl = TextView(this@FloatingAzkarService).apply {
                val current = azkarList.getOrNull(currentDhikrIndex)
                text = current?.fadl ?: ""
                setTextColor(Color.parseColor("#B0C8BA"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(12)
                }
            }
            addView(tvFadl)

            // Interactive Counter Button (+1)
            tvCounterBtn = TextView(this@FloatingAzkarService).apply {
                val target = azkarList.getOrNull(currentDhikrIndex)?.count ?: 1
                text = "تسبيح (+1)  [$currentDhikrCount/$target]"
                setTextColor(Color.parseColor("#09261A"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                val countBg = GradientDrawable().apply {
                    cornerRadius = dpToPx(12).toFloat()
                    setColor(Color.parseColor("#D4AF37"))
                }
                background = countBg
                setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    incrementCurrentDhikr()
                }
            }
            addView(tvCounterBtn)

            // Navigation Row: Previous, Next, Auto-rotate
            val navRow = LinearLayout(this@FloatingAzkarService).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(10)
                }
                gravity = Gravity.CENTER_VERTICAL
            }

            val btnPrev = createNavButton("السابق") {
                showPreviousDhikr()
            }
            navRow.addView(btnPrev)

            tvAutoRotateBtn = createNavButton("تلقائي: مفعّل") {
                toggleAutoRotate()
            }.apply {
                setTextColor(Color.parseColor("#D4AF37"))
            }
            navRow.addView(tvAutoRotateBtn)

            val btnNext = createNavButton("التالي") {
                showNextDhikr()
            }
            navRow.addView(btnNext)

            addView(navRow)
        }
    }

    private fun createNavButton(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(Color.parseColor("#E0E0E0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            val btnBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#154F38"))
            }
            background = btnBg
            setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                marginStart = dpToPx(4)
                marginEnd = dpToPx(4)
            }
            setOnClickListener { onClick() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragAndClickTouchListener() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        bubbleView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isMoving = true
                    }
                    layoutParams.x = initialX + deltaX
                    layoutParams.y = initialY + deltaY
                    windowManager?.updateViewLayout(rootView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving) {
                        expandWidget()
                    } else {
                        // Snap to nearest screen edge
                        val screenWidth = resources.displayMetrics.widthPixels
                        layoutParams.x = if (layoutParams.x < screenWidth / 2) dpToPx(16) else screenWidth - dpToPx(76)
                        windowManager?.updateViewLayout(rootView, layoutParams)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun expandWidget() {
        isExpanded = true
        bubbleView?.visibility = View.GONE
        cardView?.visibility = View.VISIBLE
        updateDhikrUi()
    }

    private fun collapseWidget() {
        isExpanded = false
        cardView?.visibility = View.GONE
        bubbleView?.visibility = View.VISIBLE
    }

    private fun updateDhikrUi() {
        val dhikr = azkarList.getOrNull(currentDhikrIndex) ?: return
        tvCategory?.text = dhikr.category
        tvDhikrText?.text = dhikr.text
        tvFadl?.text = dhikr.fadl.ifBlank { "فضل عظيم وثواب جزيل من الله تعالى" }
        tvCounterBtn?.text = "تسبيح (+1)  [$currentDhikrCount/${dhikr.count}]"
    }

    private fun incrementCurrentDhikr() {
        val dhikr = azkarList.getOrNull(currentDhikrIndex) ?: return
        currentDhikrCount++
        if (currentDhikrCount >= dhikr.count) {
            // Completed current dhikr, move to next after brief delay
            updateDhikrUi()
            handler.postDelayed({
                showNextDhikr()
            }, 500)
        } else {
            updateDhikrUi()
        }
    }

    private fun showNextDhikr() {
        currentDhikrIndex = (currentDhikrIndex + 1) % azkarList.size
        currentDhikrCount = 0
        updateDhikrUi()
    }

    private fun showPreviousDhikr() {
        currentDhikrIndex = if (currentDhikrIndex > 0) currentDhikrIndex - 1 else azkarList.size - 1
        currentDhikrCount = 0
        updateDhikrUi()
    }

    private fun toggleAutoRotate() {
        isAutoRotateActive = !isAutoRotateActive
        tvAutoRotateBtn?.text = if (isAutoRotateActive) "تلقائي: مفعّل" else "تلقائي: متوقف"
        tvAutoRotateBtn?.setTextColor(if (isAutoRotateActive) Color.parseColor("#D4AF37") else Color.LTGRAY)
    }

    private fun setupAutoRotate() {
        autoRotateRunnable = object : Runnable {
            override fun run() {
                if (isAutoRotateActive) {
                    showNextDhikr()
                }
                handler.postDelayed(this, 25000L) // 25 seconds interval
            }
        }
        handler.postDelayed(autoRotateRunnable!!, 25000L)
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun dpToPx(dp: Int): Int = dpToPx(dp.toFloat())

    override fun onDestroy() {
        isRunning = false
        autoRotateRunnable?.let { handler.removeCallbacks(it) }
        try {
            if (rootView != null) {
                windowManager?.removeView(rootView)
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
