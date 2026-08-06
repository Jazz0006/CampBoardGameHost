package com.codex.campboardgamehost.clocktower.domain

data class CandidateMetadata(
    val candidateSchemaVersion: String,
    val decisionType: String,
    val tags: Set<String> = emptySet(),
) {
    init {
        require(candidateSchemaVersion.isNotBlank()) { "candidateSchemaVersion cannot be blank." }
        require(decisionType.isNotBlank()) { "decisionType cannot be blank." }
        require(tags.all { it.isNotBlank() }) { "Candidate tags cannot be blank." }
    }
}

data class DecisionCandidate<T>(
    val candidateId: String,
    val candidateFamilyId: String,
    val outcome: T,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact> = emptyList(),
    val effects: List<EffectDraft> = emptyList(),
    val metadata: CandidateMetadata,
) {
    init {
        require(candidateId.isNotBlank()) { "candidateId cannot be blank." }
        require(candidateFamilyId.isNotBlank()) { "candidateFamilyId cannot be blank." }
        require(registrations.map { it.interactionId }.distinct().size == registrations.size) {
            "Registration interaction IDs must be unique within a candidate."
        }
        require(
            truthRelation != TruthRelation.TRUE_TO_REGISTERED_STATE || registrations.isNotEmpty(),
        ) { "Truth relative to registered state requires at least one registration fact." }
    }
}

data class DecisionEvaluation<T>(
    val candidate: DecisionCandidate<T>,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val withinFamilyWeightFixedPoint: Long,
    val finalProbabilityFixedPoint: Long,
    val pressureDelta: Map<Int, Int>,
    val warnings: List<String>,
    val explanationCodes: List<String>,
) {
    init {
        require(withinFamilyWeightFixedPoint >= 0) { "within-family weight cannot be negative." }
        require(finalProbabilityFixedPoint >= 0) { "final probability cannot be negative." }
        require(pressureDelta.keys.all { it > 0 }) { "Pressure seats must be positive." }
        require(warnings.all { it.isNotBlank() }) { "Warning codes cannot be blank." }
        require(explanationCodes.all { it.isNotBlank() }) { "Explanation codes cannot be blank." }
    }
}
