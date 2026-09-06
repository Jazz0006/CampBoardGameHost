package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.StructuredBooleanInformationUiModel
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

/** Identity/freshness of one information interaction, not a state holder or service container. */
internal data class ClocktowerInformationDecisionIdentity(
    val gameId: String,
    val phase: ClocktowerPhase,
    val round: Int,
    val sequence: Int,
    val revision: InformationDecisionRevision,
)

/** Preserve the existing presentation default order; this does not rank or generate candidates. */
internal fun clocktowerStructuredRecommendedOption(
    automatic: Boolean,
    automaticOption: ClocktowerDisplayOption?,
    displayedOptions: List<ClocktowerDisplayOption>,
    unreliableOptions: List<ClocktowerDisplayOption>,
): ClocktowerDisplayOption? = if (automatic) automaticOption else {
    displayedOptions.firstOrNull { it.isDefaultRecommendation }
        ?: unreliableOptions.firstOrNull { it.isDefaultRecommendation }
        ?: automaticOption
}

/** A numeric adapter request. It owns no UI state, callbacks, roster or mutable session. */
internal data class ClocktowerNumericInformationPreparation(
    val actorSeat: Int,
    val abilityRole: RoleId,
    val metric: NumericMetric,
    val subjectSeats: List<Int>,
    val trueValue: Int,
    val minimumValue: Int,
    val maximumValue: Int,
    val reliability: InformationReliability,
    val recommendedValue: Int?,
    val previousShownValue: Int?,
) {
    fun isTruthful(value: Int, projectedOptions: List<ClocktowerDisplayOption>): Boolean =
        projectedOptions.firstOrNull { numericOptionValue(it) == value }?.isTruthful ?: (value == trueValue)

    fun prepareUiModel(
        coordinator: ClocktowerRecommendationCoordinator,
        identity: ClocktowerInformationDecisionIdentity,
        style: RecommendationStyle,
    ): StructuredNumberInformationUiModel = prepareNumericInformationUiModel(
        coordinator = coordinator,
        gameId = identity.gameId,
        phase = identity.phase,
        round = identity.round,
        sequence = identity.sequence,
        actorSeat = actorSeat,
        abilityRole = abilityRole,
        metric = metric,
        subjectSeats = subjectSeats,
        trueValue = trueValue,
        minimumValue = minimumValue,
        maximumValue = maximumValue,
        reliability = reliability,
        recommendationStyle = style,
        revision = identity.revision,
        recommendedValue = recommendedValue,
        previousShownValue = previousShownValue,
        pressureCostPerPoint = 1,
    )
}

/** Project existing Host semantics; Foundation adapters remain the legal-value authority. */
internal fun clocktowerNumericInformationPreparation(
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    recommendedOption: ClocktowerDisplayOption?,
): ClocktowerNumericInformationPreparation? {
    if (actorSeat == null || step.spyRegistrationKey != null || step.recluseRegistrationKey != null) return null
    val metric: NumericMetric
    val subjects: List<Int>
    val truth: Int
    val minimum: Int
    val maximum: Int
    when (step.roleEnName) {
        "Empath" -> {
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS
            subjects = step.legacyInformationCandidates.asSequence()
                .mapNotNull { it.proposition as? InformationProposition.NumericResult }
                .firstOrNull { it.metric == metric }?.subjectSeats
                ?: (step.displayProposition as? InformationProposition.NumericResult)
                    ?.takeIf { it.metric == metric }?.subjectSeats
                ?: emptyList()
            if (subjects.isEmpty()) return null
            truth = step.legacyInformationCandidates.firstOrNull { it.isTruthful }?.let(::numericOptionValue)
                ?: (step.displayProposition as? InformationProposition.NumericResult)?.value
                ?: step.tellPlayer?.toIntOrNull()
                ?: return null
            minimum = 0
            maximum = 2
        }
        "Chef" -> {
            if (step.informationReliability == InformationReliability.RELIABLE) return null
            metric = NumericMetric.ADJACENT_EVIL_PAIRS
            val proposition = (step.displayProposition as? InformationProposition.NumericResult)
                ?.takeIf { it.metric == metric } ?: return null
            subjects = proposition.subjectSeats
            truth = proposition.value
            minimum = step.numericMinimumValue ?: return null
            maximum = step.numericMaximumValue ?: return null
        }
        else -> return null
    }
    return ClocktowerNumericInformationPreparation(
        actorSeat, RoleId(requireNotNull(step.roleEnName)), metric, subjects, truth, minimum, maximum,
        step.informationReliability, numericOptionValue(recommendedOption), step.previousShownNumber,
    )
}

internal data class ClocktowerBooleanInformationPreparation(
    val actorSeat: Int,
    val subjectSeats: List<Int>,
    val trueValue: Boolean,
    val reliability: InformationReliability,
    val recommendedValue: Boolean?,
) {
    fun prepareUiModel(
        coordinator: ClocktowerRecommendationCoordinator,
        identity: ClocktowerInformationDecisionIdentity,
        style: RecommendationStyle,
    ): StructuredBooleanInformationUiModel = prepareBooleanInformationUiModel(
        coordinator = coordinator,
        gameId = identity.gameId,
        phase = identity.phase,
        round = identity.round,
        sequence = identity.sequence,
        actorSeat = actorSeat,
        abilityRole = RoleId("Fortune Teller"),
        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
        subjectSeats = subjectSeats,
        trueValue = trueValue,
        reliability = reliability,
        recommendationStyle = style,
        revision = identity.revision,
        recommendedValue = recommendedValue,
    )
}

internal fun clocktowerBooleanInformationPreparation(
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    selectedSeats: List<Int>,
    recommendedOption: ClocktowerDisplayOption?,
): ClocktowerBooleanInformationPreparation? {
    if (step.action != ClocktowerNightAction.FortuneTeller || step.roleEnName != "Fortune Teller" ||
        actorSeat == null || selectedSeats.size != 2 || selectedSeats.distinct().size != 2
    ) return null
    fun matching(proposition: InformationProposition?): InformationProposition.BooleanResult? =
        (proposition as? InformationProposition.BooleanResult)?.takeIf {
            it.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT &&
                it.sourceSeat == actorSeat && it.subjectSeats == selectedSeats
        }
    val proposition = matching(step.displayProposition) ?: return null
    return ClocktowerBooleanInformationPreparation(
        actorSeat, selectedSeats, proposition.value, step.informationReliability,
        matching(recommendedOption?.proposition)?.value,
    )
}

private fun numericOptionValue(option: ClocktowerDisplayOption?): Int? =
    (option?.proposition as? InformationProposition.NumericResult)?.value ?: option?.displayPrimary?.toIntOrNull()
