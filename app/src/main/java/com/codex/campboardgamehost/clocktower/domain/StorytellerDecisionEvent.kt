package com.codex.campboardgamehost.clocktower.domain

data class DecisionOutcomeSnapshot(
    val decisionType: String,
    val canonicalFields: Map<String, String>,
) {
    init {
        require(decisionType.isNotBlank()) { "decisionType cannot be blank." }
        require(canonicalFields.keys.all { it.isNotBlank() }) { "Outcome field names cannot be blank." }
        require(canonicalFields.keys.toList() == canonicalFields.keys.sorted()) {
            "Outcome fields must use canonical key order."
        }
    }
}

data class CandidateAuditSummary(
    val candidateId: String,
    val candidateFamilyId: String,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val finalProbabilityFixedPoint: Long,
    val explanationCodes: List<String>,
) {
    init {
        require(candidateId.isNotBlank()) { "candidateId cannot be blank." }
        require(candidateFamilyId.isNotBlank()) { "candidateFamilyId cannot be blank." }
        require(finalProbabilityFixedPoint >= 0) { "final probability cannot be negative." }
        require(explanationCodes.all { it.isNotBlank() }) { "Explanation codes cannot be blank." }
    }
}

data class StorytellerDecisionEvent(
    val eventId: String,
    val requestId: String,
    val idempotencyKey: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rulesetRef: RulesetRef,
    val algorithmConfigVersion: String,
    val selectorVersion: String,
    val decisionSeed: Long,
    val stateDigest: String,
    val historyDigest: String,
    val selectedCandidateId: String,
    val selectedOutcomeSnapshot: DecisionOutcomeSnapshot,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact>,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val finalProbabilityFixedPoint: Long,
    val pressureDelta: Map<Int, Int>,
    val candidatePoolFingerprint: String,
    val candidateAudit: List<CandidateAuditSummary>,
    val explanationCodes: List<String>,
    val status: DecisionEventStatus,
) {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank." }
        require(requestId.isNotBlank()) { "requestId cannot be blank." }
        require(idempotencyKey.isNotBlank()) { "idempotencyKey cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
        require(algorithmConfigVersion.isNotBlank()) { "algorithmConfigVersion cannot be blank." }
        require(selectorVersion.isNotBlank()) { "selectorVersion cannot be blank." }
        require(stateDigest.isNotBlank()) { "stateDigest cannot be blank." }
        require(historyDigest.isNotBlank()) { "historyDigest cannot be blank." }
        require(selectedCandidateId.isNotBlank()) { "selectedCandidateId cannot be blank." }
        require(finalProbabilityFixedPoint >= 0) { "final probability cannot be negative." }
        require(pressureDelta.keys.all { it > 0 }) { "Pressure seats must be positive." }
        require(candidatePoolFingerprint.isNotBlank()) { "candidatePoolFingerprint cannot be blank." }
        require(candidateAudit.map { it.candidateId }.distinct().size == candidateAudit.size) {
            "Candidate audit IDs must be unique."
        }
        require(candidateAudit.any { it.candidateId == selectedCandidateId }) {
            "Candidate audit must contain the selected candidate."
        }
        require(explanationCodes.all { it.isNotBlank() }) { "Explanation codes cannot be blank." }
    }
}

enum class DecisionEventStatus {
    PROPOSED,
    CONFIRMED,
    APPLIED,
    FAILED,
}
