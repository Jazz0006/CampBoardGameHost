package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationResolutionRequest
import com.codex.campboardgamehost.clocktower.session.StructuredBooleanInformationUiModel

/**
 * Role-neutral Foundation adapter for genuine Yes/No Storyteller information.
 *
 * The two semantic candidates are generated explicitly rather than reconstructed from localized
 * labels or a recommendation shortlist. Foundation reliability rules decide which values are legal;
 * recommendation identity only marks one already-legal result as primary.
 */
internal fun prepareBooleanInformationUiModel(
    coordinator: ClocktowerRecommendationCoordinator,
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    actorSeat: Int,
    abilityRole: RoleId,
    metric: BooleanMetric,
    subjectSeats: List<Int>,
    trueValue: Boolean,
    reliability: InformationReliability,
    recommendationStyle: RecommendationStyle,
    revision: InformationDecisionRevision,
    recommendedValue: Boolean?,
    falseMisinformationPressure: Int = 3,
): StructuredBooleanInformationUiModel {
    require(actorSeat > 0) { "Boolean information actor seat must be positive." }
    require(subjectSeats.all { it > 0 } && subjectSeats.distinct().size == subjectSeats.size) {
        "Boolean information subject seats must be positive and unique."
    }
    require(falseMisinformationPressure >= 0) { "Boolean misinformation pressure cannot be negative." }

    val yes = UnreliableCategoricalCandidate(
        id = "yes",
        isTruthful = trueValue,
        misinformationPressure = if (trueValue) 0 else falseMisinformationPressure,
    )
    val no = UnreliableCategoricalCandidate(
        id = "no",
        isTruthful = !trueValue,
        misinformationPressure = if (trueValue) falseMisinformationPressure else 0,
    )
    val evaluations = coordinator.resolveInformation(
        InformationResolutionRequest.Category(
            candidates = listOf(yes, no),
            generation = DynamicGenerationContext(
                abilityRole = abilityRole,
                recipientSeat = actorSeat,
                reliability = reliability,
                style = recommendationStyle,
                targetSeats = subjectSeats.toSet(),
                playerSelectedTarget = subjectSeats.isNotEmpty(),
            ),
        ),
    ).map { evaluation ->
        @Suppress("UNCHECKED_CAST")
        evaluation as com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation<
            com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome.Category
        >
    }

    val effectiveRecommendedValue = recommendedValue
        ?: trueValue.takeIf { reliability == InformationReliability.RELIABLE }
    val recommendedOutcomeId = effectiveRecommendedValue?.let { if (it) "yes" else "no" }
    val recommendedIds = recommendedOutcomeId?.let { outcomeId ->
        evaluations
            .filter { it.candidate.outcome.id == outcomeId }
            .mapTo(linkedSetOf()) { it.candidate.candidateId }
    }.orEmpty()
    val storytellerPhase = when (phase) {
        ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
        ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
        ClocktowerPhase.Day -> StorytellerPhase.DAY
        ClocktowerPhase.Night -> StorytellerPhase.NIGHT
    }

    val context = coordinator.informationDecisionContext(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedIds,
        revision = revision,
        semanticIdentity = "boolean|${abilityRole.value}|$gameId|${phase.name}|$round|$sequence|$actorSeat|${metric.name}|${subjectSeats.joinToString(",")}",
        draftOf = { evaluation ->
            val value = when (evaluation.candidate.outcome.id) {
                "yes" -> true
                "no" -> false
                else -> error("Unexpected Boolean outcome '${evaluation.candidate.outcome.id}'.")
            }
            val proposition = InformationProposition.BooleanResult(
                metric = metric,
                sourceSeat = actorSeat,
                subjectSeats = subjectSeats,
                value = value,
            )
            EpistemicObservationDraft(
                recordId = clocktowerPrivateObservationRecordId(
                    gameId = gameId,
                    phase = phase,
                    round = round,
                    roleEnName = abilityRole.value,
                    actorSeat = actorSeat,
                    proposition = proposition,
                ),
                phase = storytellerPhase,
                round = round,
                sequence = sequence,
                sourceSeat = actorSeat,
                sourceAbility = abilityRole,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(actorSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = proposition,
            )
        },
    )

    return StructuredBooleanInformationUiModel.from(
        context = context,
        trueOutcomeId = "yes",
        falseOutcomeId = "no",
    )
}
