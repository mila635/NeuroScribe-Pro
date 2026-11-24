package com.example.audio

enum class AuditoryFilterState {
    IDLE,
    FILTERING_NOISE, // Low amplitude or non-speech background noise filtered out
    SPEECH_NLP_DETECTED // Genuine linguistic NLP speech detected and approved for intake
}

enum class DetectedLanguage(
    val label: String,
    val nativeLabel: String,
    val code: String,
    val flagEmoji: String
) {
    AUTO("Auto-Detect", "Auto / خودکار", "auto", "AUTO"),
    ENGLISH("English", "English", "en-US", "EN"),
    URDU("Urdu", "اردو", "ur-PK", "UR"),
    ARABIC("Arabic", "العربية", "ar-SA", "AR"),
    SPANISH("Spanish", "Español", "es-ES", "ES"),
    FRENCH("French", "Français", "fr-FR", "FR"),
    GERMAN("German", "Deutsch", "de-DE", "DE"),
    CHINESE("Chinese", "中文", "zh-CN", "ZH"),
    JAPANESE("Japanese", "日本語", "ja-JP", "JA"),
    HINDI("Hindi", "हिन्दी", "hi-IN", "HI"),
    RUSSIAN("Russian", "Русский", "ru-RU", "RU"),
    PORTUGUESE("Portuguese", "Português", "pt-PT", "PT"),
    TURKISH("Turkish", "Türkçe", "tr-TR", "TR"),
    PERSIAN("Persian", "فارسی", "fa-IR", "FA"),
    ITALIAN("Italian", "Italiano", "it-IT", "IT"),
    KOREAN("Korean", "한국어", "ko-KR", "KO"),
    ROMAN_URDU("Roman Urdu", "Roman Urdu", "ur-Latn", "UR-L"),
    MULTILINGUAL("Multilingual", "Code-Switched", "multi", "MULTI")
}

object VoiceActivityFilter {
    // Amplitude threshold below which sounds are treated as filtered ambient noise
    const val NOISE_AMPLITUDE_THRESHOLD = 0.06f

    // Common Roman Urdu lexemes to identify spoken Roman Urdu vs English
    private val romanUrduKeywords = setOf(
        "hai", "hain", "karna", "yeh", "woh", "hum", "aap", "tum", "mera", "meri", "mere",
        "kya", "kyun", "kaise", "hota", "hoti", "hote", "bhi", "aur", "mein", "pe", "par",
        "nahi", "raha", "rahi", "rahe", "thha", "thhi", "baat", "cheez", "soch", "samajh",
        "kaam", "dost", "waqt", "sab", "bohot", "acha", "achi", "lekin", "shuru", "karo"
    )

    fun evaluateAudioState(amplitude: Float, hasTranscribedWords: Boolean): AuditoryFilterState {
        return when {
            hasTranscribedWords -> AuditoryFilterState.SPEECH_NLP_DETECTED
            amplitude > NOISE_AMPLITUDE_THRESHOLD -> AuditoryFilterState.SPEECH_NLP_DETECTED
            amplitude > 0.015f -> AuditoryFilterState.FILTERING_NOISE
            else -> AuditoryFilterState.IDLE
        }
    }

    fun detectLanguage(text: String): DetectedLanguage {
        if (text.isBlank()) return DetectedLanguage.ENGLISH

        // Check for Urdu Arabic script unicode range
        val urduCharCount = text.count { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
        if (urduCharCount > text.length * 0.25) {
            return DetectedLanguage.URDU
        }

        // Check for Roman Urdu tokens
        val words = text.lowercase().split(Regex("\\s+")).map { it.trim('.', ',', '!', '?') }
        val romanMatches = words.count { it in romanUrduKeywords }
        if (romanMatches >= 2 || (words.size in 1..4 && romanMatches >= 1)) {
            return DetectedLanguage.ROMAN_URDU
        }

        return DetectedLanguage.ENGLISH
    }

    fun containsNlpContent(text: String): Boolean {
        // Checks if text contains meaningful linguistic tokens rather than garbage
        val clean = text.trim()
        return clean.length >= 2 && clean.any { it.isLetter() }
    }
}
