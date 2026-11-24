package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

/**
 * Foreground Service for Background Voice Recording Indication in Notification Chamber.
 * Provides system-level persistent notification indicating ongoing audio capture,
 * complete with Pause/Resume and Stop control affordances.
 */
class AudioRecordingService : Service {

    constructor() : super()

    companion object {
        const val CHANNEL_ID = "voice_recording_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.START_RECORDING"
        const val ACTION_PAUSE = "com.example.service.PAUSE_RECORDING"
        const val ACTION_RESUME = "com.example.service.RESUME_RECORDING"
        const val ACTION_STOP = "com.example.service.STOP_RECORDING"

        var isServiceRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }

        fun notifyStateChange(context: Context, isPaused: Boolean, durationText: String) {
            if (!isServiceRunning) return
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notification = createNotification(context, isPaused, durationText)
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }

        private fun createNotification(context: Context, isPaused: Boolean, durationText: String): Notification {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("action_route", "capture_voice")
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val pauseResumeActionIntent = Intent(context, AudioRecordingService::class.java).apply {
                action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            }
            val pauseResumePendingIntent = PendingIntent.getService(
                context,
                1,
                pauseResumeActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopActionIntent = Intent(context, AudioRecordingService::class.java).apply {
                action = ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                context,
                2,
                stopActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val titleText = if (isPaused) "NeuroScribe Pro • Recording Paused" else "NeuroScribe Pro • Live Recording Active"
            val statusText = if (isPaused) "Paused • $durationText" else "Capturing Voice • $durationText"

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(titleText)
                .setContentText(statusText)
                .setSubText("Voice Intelligence")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .setContentIntent(openAppPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(titleText)
                        .bigText("Session Duration: $durationText\nSpeech recognition, acoustic noise filtering, and high-fidelity audio synthesis active in background.")
                )
                .addAction(
                    if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                    if (isPaused) "Resume" else "Pause",
                    pauseResumePendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop & Synthesize",
                    stopPendingIntent
                )
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                isServiceRunning = true
                val notification = createNotification(this, isPaused = false, durationText = "00:00")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
            ACTION_PAUSE -> {
                // Send broadcast or update state
                val broadcast = Intent("com.example.RECORDING_CONTROL").apply {
                    putExtra("command", "pause")
                }
                sendBroadcast(broadcast)
                val notification = createNotification(this, isPaused = true, durationText = "Paused")
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.notify(NOTIFICATION_ID, notification)
            }
            ACTION_RESUME -> {
                val broadcast = Intent("com.example.RECORDING_CONTROL").apply {
                    putExtra("command", "resume")
                }
                sendBroadcast(broadcast)
                val notification = createNotification(this, isPaused = false, durationText = "Recording Live")
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                val broadcast = Intent("com.example.RECORDING_CONTROL").apply {
                    putExtra("command", "stop")
                }
                sendBroadcast(broadcast)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Intelligence Recording",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows live audio recording status and controls in the notification chamber."
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
