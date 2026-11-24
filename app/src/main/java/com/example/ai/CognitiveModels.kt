package com.example.ai

/**
 * Dynamic Knowledge Format Archetypes allowing user to dictate the exact structural
 * depth and presentation format of synthesized documents.
 */
enum class CognitiveFormatArchetype(
    val title: String,
    val description: String
) {
    RESEARCH_DISSERTATION(
        title = "Deep Research Dissertation",
        description = "Exhaustive academic investigation with historical axioms, underlying mechanisms, and theoretical models."
    ),
    IMPLEMENTATION_BLUEPRINT(
        title = "Technical Architecture Blueprint",
        description = "Step-by-step technical implementation with concrete code, ASCII diagrams, protocols, and security rules."
    ),
    EXECUTIVE_DECISION_MATRIX(
        title = "Executive Strategic Brief",
        description = "High-yield decision matrix, quantitative benchmarks, pros/cons analysis, and strategic recommendations."
    ),
    STEP_BY_STEP_PLAYBOOK(
        title = "Standard Operating Playbook",
        description = "Sequential procedure, operational milestones, execution checklists, and failure safeguards."
    ),
    SOCRATIC_TUTORIAL_QA(
        title = "Socratic Pedagogical Tutorial",
        description = "Guided conceptual discovery through dialectical questions, intuitive proofs, and common misconceptions."
    ),
    VISUAL_MINDMAP_OUTLINE(
        title = "Concept Tree & Mind Map",
        description = "Multi-level hierarchical concept tree, taxonomic branches, structural mental diagrams, and core dependencies."
    ),
    CLINICAL_SCIENTIFIC_DOSSIER(
        title = "Scientific & Clinical Dossier",
        description = "Rigorous empirical analysis covering biological/chemical mechanisms, experimental proof, and clinical pathways."
    ),
    LEGAL_STATUTORY_ANALYSIS(
        title = "Legal & Statutory Precedent",
        description = "Formal statutory interpretation, precedent jurisprudence, multi-jurisdiction comparative law, and counsel."
    ),
    PHILOSOPHICAL_FIRST_PRINCIPLES(
        title = "First-Principles Deconstruction",
        description = "Feynman-style axiomatic breakdown, stripping assumptions, and rebuilding fundamental truths from bedrock."
    ),
    CREATIVE_NARRATIVE_ESSAY(
        title = "Expository & Narrative Essay",
        description = "Compelling literary exposition, intellectual history, rhetoric, and engaging contextual synthesis."
    ),
    CUSTOM_USER_SPECIFICATION(
        title = "Custom User Specification",
        description = "User-defined format, custom emphasis, or bespoke structure instructions."
    );

    val label: String get() = title
}

data class CognitiveSynthesisResult(
    val title: String,
    val summary: String,
    val structuredContent: String,
    val keyTerms: List<String>,
    val actionItems: List<String>,
    val flashcards: List<CognitiveFlashcardPrompt>,
    val detectedLanguage: String,
    val extractedTranscript: String = ""
)

data class CognitiveFlashcardPrompt(
    val question: String,
    val answer: String,
    val conceptKey: String
)

enum class AskNeuroScribeMode(val label: String, val description: String) {
    MY_KNOWLEDGE("My Knowledge", "Answers grounded strictly in your personal notes & recordings with source citations."),
    ADVANCED_INTELLIGENCE("Advanced Intelligence", "Combines personal knowledge with global cognitive synthesis.")
}

data class SourceCitation(
    val noteId: Long,
    val title: String,
    val excerpt: String
)

data class AskNeuroScribeResult(
    val answer: String,
    val citedSources: List<SourceCitation>,
    val mode: AskNeuroScribeMode
)

data class GeneratedQuestion(
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class GeneratedQuizResult(
    val title: String,
    val topic: String,
    val questions: List<GeneratedQuestion>
)

data class StudyScheduleItem(
    val phase: String,
    val durationText: String,
    val topicsToCover: List<String>,
    val keyActivities: List<String>
)

data class GeneratedStudyPlanResult(
    val title: String,
    val subject: String,
    val goals: String,
    val scheduleItems: List<StudyScheduleItem>
)

data class DiscoveredConnection(
    val sourceNoteId: Long,
    val targetNoteId: Long,
    val relationship: String,
    val sharedConcept: String
)
