package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.MainActivity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Universal Rainbow Arc Floating Overlay Service.
 * - Stays visible across home screen and other apps when enabled in settings.
 * - Sleek glowing neural orb launcher.
 * - Magnetic 4-side clamping: forcefully and automatically snaps to Left, Right, Top, or Bottom.
 * - Full-screen non-clipped rainbow arc expansion on click.
 * - Fully functional shortcut routing (Camera scanner, App Media Gallery, Audio recording, Archives, Recall).
 * - 100% crash-safe across Android versions.
 */
class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var rootContainer: FrameLayout? = null

    companion object {
        var isRunning: Boolean = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Log.w("FloatingOverlayService", "Overlay permission not granted. Stopping service.")
                stopSelf()
                return
            }
            isRunning = true
            setupFloatingWindow()
        } catch (e: Throwable) {
            Log.e("FloatingOverlayService", "Error during service onCreate", e)
            stopSelf()
        }
    }

    private fun setupFloatingWindow() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val dockSizeDp = 56
            val dockSizePx = (dockSizeDp * displayMetrics.density).toInt()
            val edgePadding = (12 * displayMetrics.density).toInt()
            val topBound = (70 * displayMetrics.density).toInt()
            val bottomBound = screenHeight - dockSizePx - (90 * displayMetrics.density).toInt()

            var currentDockX = screenWidth - dockSizePx - edgePadding
            var currentDockY = (screenHeight * 0.45f).toInt()
            var isExpanded = false

            val params = WindowManager.LayoutParams(
                dockSizePx,
                dockSizePx,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = currentDockX
                y = currentDockY
            }

            // Root FrameLayout hosting scrim, arc menu, and trigger orb
            val root = FrameLayout(this).apply {
                clipChildren = false
                clipToPadding = false
            }

            // Fullscreen backdrop scrim to dismiss when expanded
            val scrimView = View(this).apply {
                setBackgroundColor(Color.argb(85, 8, 12, 24))
                visibility = View.GONE
            }

            // Container for arc buttons
            val arcContainer = FrameLayout(this).apply {
                visibility = View.GONE
                clipChildren = false
                clipToPadding = false
            }

            // Trigger Orb container matching in-app FloatingActionSideDock exactly
            val orbContainer = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(dockSizePx, dockSizePx)
                alpha = 0.50f
            }

            // Outer rainbow ring
            val outerOrbRing = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(dockSizePx, dockSizePx).apply {
                    gravity = Gravity.CENTER
                }
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.parseColor("#6366F1"),
                        Color.parseColor("#8B5CF6"),
                        Color.parseColor("#EC4899"),
                        Color.parseColor("#F59E0B"),
                        Color.parseColor("#10B981"),
                        Color.parseColor("#06B6D4"),
                        Color.parseColor("#6366F1")
                    )
                ).apply {
                    shape = GradientDrawable.OVAL
                    setStroke((2 * displayMetrics.density).toInt(), Color.argb(220, 255, 255, 255))
                }
            }

            // Inner dark circular core
            val innerCoreSizePx = (46 * displayMetrics.density).toInt()
            val innerCore = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(innerCoreSizePx, innerCoreSizePx).apply {
                    gravity = Gravity.CENTER
                }
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.parseColor("#1E1B4B"),
                        Color.parseColor("#312E81")
                    )
                ).apply {
                    shape = GradientDrawable.OVAL
                }
            }

            val triggerOrb = ImageView(this).apply {
                val iconSizePx = (24 * displayMetrics.density).toInt()
                layoutParams = FrameLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                    gravity = Gravity.CENTER
                }
                setImageResource(com.example.R.drawable.ic_dock_auto_awesome)
                setColorFilter(Color.WHITE)
            }

            orbContainer.addView(outerOrbRing)
            orbContainer.addView(innerCore)
            orbContainer.addView(triggerOrb)

            data class ArcItem(
                val iconRes: Int,
                val route: String?,
                val label: String,
                val color1: String,
                val color2: String,
                val isClose: Boolean = false
            )

            // 7 Arc Actions exactly matching in-app FloatingActionSideDock
            val arcItems = listOf(
                ArcItem(com.example.R.drawable.ic_dock_mic, "capture_voice", "Record", "#EF4444", "#DC2626"),
                ArcItem(com.example.R.drawable.ic_dock_archive, "archive_lookup", "Archives", "#F59E0B", "#D97706"),
                ArcItem(com.example.R.drawable.ic_dock_media, "media_library", "Media", "#06B6D4", "#0891B2"),
                ArcItem(com.example.R.drawable.ic_dock_psychology, "retention_recall", "Recall", "#10B981", "#059669"),
                ArcItem(com.example.R.drawable.ic_dock_translate, "language_select", "Language", "#6366F1", "#4F46E5"),
                ArcItem(com.example.R.drawable.ic_dock_camera, "camera_scan", "Camera", "#8B5CF6", "#7C3AED"),
                ArcItem(com.example.R.drawable.ic_dock_close, null, "Close", "#64748B", "#475569", isClose = true)
            )

            fun collapseArc() {
                isExpanded = false
                scrimView.visibility = View.GONE
                arcContainer.visibility = View.GONE
                orbContainer.alpha = 0.50f
                triggerOrb.setImageResource(com.example.R.drawable.ic_dock_auto_awesome)

                // Restore dock parameters to collapsed state
                params.width = dockSizePx
                params.height = dockSizePx
                params.x = currentDockX
                params.y = currentDockY
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

                (orbContainer.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = 0
                    topMargin = 0
                }
                orbContainer.requestLayout()

                windowManager?.updateViewLayout(root, params)
            }

            fun expandArc() {
                isExpanded = true
                orbContainer.alpha = 1.0f
                triggerOrb.setImageResource(com.example.R.drawable.ic_dock_close)

                // Expand window to fullscreen to guarantee zero clipping and smooth touch capture
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.MATCH_PARENT
                params.x = 0
                params.y = 0
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

                (orbContainer.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = currentDockX
                    topMargin = currentDockY
                }
                orbContainer.requestLayout()

                // Render Arc Items positioned in a rainbow curve around the orb
                arcContainer.removeAllViews()

                // Determine arc direction based on which edge the dock is snapped to
                val dLeft = currentDockX
                val dRight = screenWidth - dockSizePx - currentDockX
                val dTop = currentDockY
                val dBottom = screenHeight - dockSizePx - currentDockY
                val minD = minOf(dLeft, dRight, dTop, dBottom)

                val baseAngleRad = when (minD) {
                    dLeft -> 0.0          // Points towards the right
                    dRight -> PI          // Points towards the left
                    dTop -> PI / 2.0      // Points downward
                    else -> 3.0 * PI / 2.0 // Points upward
                }

                val radius = 108 * displayMetrics.density
                val arcSpanRad = 150.0 * (PI / 180.0)
                val count = arcItems.size
                val startAngle = baseAngleRad - (arcSpanRad / 2.0)
                val stepAngle = arcSpanRad / (count - 1).coerceAtLeast(1)

                val btnSizePx = (42 * displayMetrics.density).toInt()
                val iconSizePx = (20 * displayMetrics.density).toInt()
                val centerX = currentDockX + dockSizePx / 2
                val centerY = currentDockY + dockSizePx / 2

                arcItems.forEachIndexed { i, item ->
                    val angle = startAngle + i * stepAngle
                    val itemTargetX = (centerX + radius * cos(angle) - btnSizePx / 2).toInt()
                    val itemTargetY = (centerY + radius * sin(angle) - btnSizePx / 2).toInt()

                    // Clamped within screen bounds
                    val clampedX = itemTargetX.coerceIn(16, screenWidth - btnSizePx - 16)
                    val clampedY = itemTargetY.coerceIn(40, screenHeight - btnSizePx - 40)

                    val actionBtn = FrameLayout(this@FloatingOverlayService).apply {
                        val lp = FrameLayout.LayoutParams(btnSizePx, btnSizePx).apply {
                            leftMargin = clampedX
                            topMargin = clampedY
                        }
                        layoutParams = lp

                        elevation = 8 * displayMetrics.density
                        background = GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            intArrayOf(
                                Color.parseColor(item.color1),
                                Color.parseColor(item.color2)
                            )
                        ).apply {
                            shape = GradientDrawable.OVAL
                            setStroke((1.5f * displayMetrics.density).toInt(), Color.argb(230, 255, 255, 255))
                        }

                        val icon = ImageView(this@FloatingOverlayService).apply {
                            layoutParams = FrameLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                                gravity = Gravity.CENTER
                            }
                            setImageResource(item.iconRes)
                            setColorFilter(Color.WHITE)
                        }
                        addView(icon)

                        setOnClickListener {
                            collapseArc()
                            if (!item.isClose && item.route != null) {
                                val launchIntent = Intent(this@FloatingOverlayService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("action_route", item.route)
                                }
                                startActivity(launchIntent)
                            }
                        }
                    }
                    arcContainer.addView(actionBtn)
                }

                scrimView.visibility = View.VISIBLE
                arcContainer.visibility = View.VISIBLE

                windowManager?.updateViewLayout(root, params)
            }

            scrimView.setOnClickListener {
                collapseArc()
            }

            // Snaps strictly to Left, Right, Top, or Bottom
            fun snapToNearestEdge(x: Int, y: Int) {
                val dLeft = x
                val dRight = screenWidth - dockSizePx - x
                val dTop = y
                val dBottom = screenHeight - dockSizePx - y
                val minD = minOf(dLeft, dRight, dTop, dBottom)

                when (minD) {
                    dLeft -> {
                        currentDockX = edgePadding
                        currentDockY = y.coerceIn(topBound, bottomBound)
                    }
                    dRight -> {
                        currentDockX = screenWidth - dockSizePx - edgePadding
                        currentDockY = y.coerceIn(topBound, bottomBound)
                    }
                    dTop -> {
                        currentDockY = topBound
                        currentDockX = x.coerceIn(edgePadding, screenWidth - dockSizePx - edgePadding)
                    }
                    else -> {
                        currentDockY = bottomBound
                        currentDockX = x.coerceIn(edgePadding, screenWidth - dockSizePx - edgePadding)
                    }
                }

                params.x = currentDockX
                params.y = currentDockY
                windowManager?.updateViewLayout(root, params)
            }

            // Touch and Drag handling on the Trigger Orb
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            var hasMoved = false

            orbContainer.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        hasMoved = false
                        orbContainer.alpha = 1.0f
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(deltaX) > 14 || Math.abs(deltaY) > 14) {
                            hasMoved = true
                        }
                        if (hasMoved) {
                            if (isExpanded) {
                                collapseArc()
                            }
                            params.x = initialX + deltaX
                            params.y = initialY + deltaY
                            currentDockX = params.x
                            currentDockY = params.y
                            windowManager?.updateViewLayout(root, params)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (hasMoved) {
                            // Automatically and forcefully snap to nearest edge
                            snapToNearestEdge(params.x, params.y)
                            orbContainer.alpha = 0.50f
                        } else {
                            // Tapped! Toggle curved rainbow arc
                            if (isExpanded) {
                                collapseArc()
                            } else {
                                expandArc()
                            }
                        }
                        true
                    }
                    else -> false
                }
            }

            root.addView(scrimView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            root.addView(arcContainer, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            root.addView(orbContainer)

            rootContainer = root
            windowManager?.addView(root, params)
        } catch (e: Throwable) {
            Log.e("FloatingOverlayService", "Failed to add floating window overlay", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        rootContainer?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
