package com.codex.campboardgamehost.clocktower.domain

data class DecisionExplanation(
    val decisionId: String,
    val status: DecisionEventStatus? = null,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val finalProbabilityFixedPoint: Long = 0,
    val explanationCodes: List<String>,
    val warningCodes: List<String>,
    val affectedSeats: Set<Int>,
    val alternativeCandidateIds: List<String> = emptyList(),
)

data class ReviewedDecision(
    val eventId: String,
    val requestId: String,
    val selectedCandidateId: String,
    val status: DecisionEventStatus,
    val corrected: Boolean,
    val explanation: DecisionExplanation,
)

data class PostGameDecisionReview(
    val decisions: List<ReviewedDecision>,
    val effectiveDecisionCount: Int,
    val failedDecisionCount: Int,
    val correctionCount: Int,
    val misinformationLedger: MisinformationLedger,
    val registrationLedgerBySeat: Map<Int, RegistrationLedger>,
    val pressureBySeat: Map<Int, PlayerInformationPressure>,
)
