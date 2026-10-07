package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

internal enum class InformationReliability {
    RELIABLE,
    DRUNK,
    POISONED,
}

internal data class DynamicGenerationContext(
    val abilityRole: RoleId,
    val recipientSeat: Int,
    val reliability: InformationReliability,
) {
    init {
        require(recipientSeat > 0) { "recipientSeat must be positive." }
    }
}

internal object DynamicCandidateGenerator {
    private const val CANDIDATE_SCHEMA_VERSION = "dynamic-v1"

    private fun informationFamilyId(
        reliability: InformationReliability,
        truthful: Boolean,
    ): String = when {
        truthful && reliability == InformationReliability.RELIABLE -> "natural-truth"
        truthful -> "malfunction-truth"
        else -> "malfunction-falsehood-role"
    }

    fun generateNumeric(
        numberContext: UnreliableNumberContext,
        context: DynamicGenerationContext,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Number>> =
        (numberContext.minimumValue..numberContext.maximumValue).map { value ->
            evaluation(
                candidate = candidate(
                    stableOptionId = value.toString(),
                    outcome = DynamicInformationOutcome.Number(value),
                    truthful = value in numberContext.truthfulValues,
                    context = context,
                    informationValue = InformationValue.Number(value),
                    decisionType = "numeric-information",
                ),
                warnings = numberContext.warningCodes(value),
            )
        }.sortedBy { it.candidate.candidateId }

    fun generateCategorical(
        candidates: List<UnreliableCategoricalCandidate>,
        context: DynamicGenerationContext,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Category>> = candidates
        .distinctBy { it.id }
        .map { input ->
            evaluation(
                candidate(
                    stableOptionId = input.id,
                    outcome = DynamicInformationOutcome.Category(input.id),
                    truthful = input.isTruthful,
                    context = context,
                    informationValue = InformationValue.Category(input.id),
                    decisionType = "categorical-information",
                ),
            )
        }.sortedBy { it.candidate.candidateId }

    private fun <T : DynamicInformationOutcome> candidate(
        stableOptionId: String,
        outcome: T,
        truthful: Boolean,
        context: DynamicGenerationContext,
        informationValue: InformationValue,
        decisionType: String,
    ): DecisionCandidate<T> = DecisionCandidate(
        candidateId = stableCandidateId(decisionType, stableOptionId, context),
        candidateFamilyId = when (outcome) {
            is DynamicInformationOutcome.Number -> when {
                truthful && context.reliability == InformationReliability.RELIABLE -> "natural-truth"
                truthful -> "malfunction-truth"
                else -> "malfunction-falsehood-numeric"
            }
            else -> informationFamilyId(context.reliability, truthful)
        },
        outcome = outcome,
        abilityState = when (context.reliability) {
            InformationReliability.RELIABLE -> AbilityState.FUNCTIONING
            InformationReliability.DRUNK -> AbilityState.MALFUNCTIONING_DRUNK
            InformationReliability.POISONED -> AbilityState.MALFUNCTIONING_POISONED
        },
        truthRelation = if (truthful) TruthRelation.TRUE_TO_ACTUAL_STATE else TruthRelation.FALSE_TO_ACTUAL_STATE,
        effects = listOf(EffectDraft.PlayerInformation(context.recipientSeat, context.abilityRole, informationValue)),
        metadata = CandidateMetadata(CANDIDATE_SCHEMA_VERSION, decisionType, setOf("dynamic-information")),
    )

    private fun <T : DynamicInformationOutcome> evaluation(
        candidate: DecisionCandidate<T>,
        warnings: List<String> = emptyList(),
    ): DecisionEvaluation<T> = DecisionEvaluation(
        candidate = candidate,
        qualityTier = QualityTier.RECOMMENDED,
        warnings = warnings,
    )

    private fun stableCandidateId(
        decisionType: String,
        stableOptionId: String,
        context: DynamicGenerationContext,
    ): String = java.lang.Long.toUnsignedString(
        MurmurHash3.low64Utf8(
            "$CANDIDATE_SCHEMA_VERSION|$decisionType|${context.abilityRole.value}|${context.recipientSeat}|${context.reliability.name}|$stableOptionId",
        ),
        16,
    ).padStart(16, '0')
}
