package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Continuous Enterprise Speech Recognition Manager.
 * Designed to capture voice continuously across long durations without abruptly
 * stopping or failing on pauses, silence gaps, or on-device recognizer timeouts.
 */
class SpeechRecognitionManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeLanguage: DetectedLanguage = DetectedLanguage.AUTO

    @Volatile
    private var isContinuousSessionActive = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _finalTranscript = MutableStateFlow("")
    val finalTranscript: StateFlow<String> = _finalTranscript.asStateFlow()

    private val _speechRms = MutableStateFlow(0f)
    val speechRms: StateFlow<Float> = _speechRms.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(selectedLang: DetectedLanguage = DetectedLanguage.AUTO) {
        isContinuousSessionActive = true
        activeLanguage = selectedLang
        _errorMessage.value = null

        mainHandler.post {
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        if (!isContinuousSessionActive) return

        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("SpeechRecognizer", "Reset error", e)
        }

        if (!isAvailable()) {
            _errorMessage.value = "Speech recognition service not available on this device"
            _isListening.value = false
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                // High-tolerance silence lengths to prevent 1-2 second cutoffs
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000L)

                val localeTag = if (activeLanguage == DetectedLanguage.AUTO || activeLanguage == DetectedLanguage.MULTILINGUAL) {
                    Locale.getDefault().toLanguageTag()
                } else {
                    activeLanguage.code
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ur-PK", "en-US", "ar-SA", "es-ES", "fr-FR", "de-DE", "zh-CN", "hi-IN", "ur-Latn"))
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "Failed to start listening internal", e)
            if (isContinuousSessionActive) {
                scheduleRestart(500)
            } else {
                _isListening.value = false
            }
        }
    }

    private fun scheduleRestart(delayMs: Long = 150L) {
        if (!isContinuousSessionActive) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (isContinuousSessionActive) {
                startListeningInternal()
            }
        }, delayMs)
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (isContinuousSessionActive) {
                    _isListening.value = true
                }
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _speechRms.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // If in continuous mode, we wait for onResults or restart
            }

            override fun onError(error: Int) {
                // Transfer any pending partial transcript before handling error
                if (_partialTranscript.value.isNotBlank()) {
                    val current = _finalTranscript.value
                    _finalTranscript.value = if (current.isBlank()) _partialTranscript.value else "$current ${_partialTranscript.value}"
                    _partialTranscript.value = ""
                }

                // If continuous recording is on, recover smoothly from timeouts or pauses
                if (isContinuousSessionActive) {
                    when (error) {
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_CLIENT,
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                            // Non-fatal pauses/silence: auto-restart immediately
                            scheduleRestart(120)
                            return
                        }
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                            // Network hiccups: retry in 800ms while hardware recording proceeds
                            scheduleRestart(800)
                            return
                        }
                        else -> {
                            scheduleRestart(300)
                            return
                        }
                    }
                }

                _isListening.value = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0].trim()
                    if (text.isNotBlank()) {
                        val current = _finalTranscript.value
                        _finalTranscript.value = if (current.isBlank()) text else "$current $text"
                    }
                    _partialTranscript.value = ""
                } else if (_partialTranscript.value.isNotBlank()) {
                    val current = _finalTranscript.value
                    _finalTranscript.value = if (current.isBlank()) _partialTranscript.value else "$current ${_partialTranscript.value}"
                    _partialTranscript.value = ""
                }

                if (isContinuousSessionActive) {
                    scheduleRestart(100)
                } else {
                    _isListening.value = false
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _partialTranscript.value = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun appendTextDirectly(text: String) {
        val current = _finalTranscript.value
        _finalTranscript.value = if (current.isBlank()) text else "$current $text"
        _partialTranscript.value = ""
    }

    fun setTranscript(text: String) {
        _finalTranscript.value = text
        _partialTranscript.value = ""
    }

    fun clearTranscript() {
        _finalTranscript.value = ""
        _partialTranscript.value = ""
        _errorMessage.value = null
    }

    fun stopListening() {
        isContinuousSessionActive = false
        mainHandler.removeCallbacksAndMessages(null)

        // Ensure any pending partial transcript is preserved
        if (_partialTranscript.value.isNotBlank()) {
            val current = _finalTranscript.value
            _finalTranscript.value = if (current.isBlank()) _partialTranscript.value else "$current ${_partialTranscript.value}"
            _partialTranscript.value = ""
        }

        _isListening.value = false
        _speechRms.value = 0f

        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w("SpeechRecognizer", "Error destroying speech recognizer", e)
            } finally {
                speechRecognizer = null
            }
        }
    }

    fun cleanup() {
        stopListening()
    }
}
