package com.example.audio

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentPlayingPath = MutableStateFlow<String?>(null)
    val currentPlayingPath: StateFlow<String?> = _currentPlayingPath.asStateFlow()

    fun playOrPause(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayerManager", "File does not exist: $filePath")
            return
        }

        if (_currentPlayingPath.value == filePath && mediaPlayer != null) {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
            return
        }

        // Initialize new file playback
        stop()
        _currentPlayingPath.value = filePath
        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setPlaybackSpeedInternal(_playbackSpeed.value)
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    progressJob?.cancel()
                }
                start()
            }
            mediaPlayer = player
            _totalDurationMs.value = player.duration.toLong()
            _isPlaying.value = true
            startProgressTracker()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error playing audio file", e)
            stop()
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error seeking", e)
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        setPlaybackSpeedInternal(speed)
    }

    private fun setPlaybackSpeedInternal(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    val params = player.playbackParams ?: PlaybackParams()
                    params.speed = speed
                    player.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w("AudioPlayerManager", "Could not set playback speed", e)
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        _currentPositionMs.value = player.currentPosition.toLong()
                    } catch (e: Exception) {
                        // ignore during release
                    }
                }
                delay(100)
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioPlayerManager", "Error releasing player", e)
        } finally {
            mediaPlayer = null
            _currentPlayingPath.value = null
        }
    }

    fun cleanup() {
        stop()
    }
}
