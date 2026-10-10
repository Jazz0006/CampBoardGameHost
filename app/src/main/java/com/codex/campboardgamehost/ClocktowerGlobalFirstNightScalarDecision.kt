package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/**
 * A current, real first-night scalar information boundary for the GLOBAL strategist.
 *
 * This is an adaptation of the existing Host legal domains, not a second selector.
 * Result-first registration options remain possible legal explanations, never an
 * asserted unique registration. Foundation supplies the legal unreliable values.
 */
internal data class PendingGlobalScalarInformationDecision(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val kind: StorytellerProviderScalarKindV1,
    val metric: String,
    val subjectSeats: List<Int>,
    val legalCandidates: List<StorytellerProviderCandidateV1>,
)

internal fun pendingGlobalFirstNightScalarDecision(
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    cards: List<PlayerCard>,
    gameId: String,
    revision: StorytellerDecisionRevision,
    fortuneTellerSelectedNames: List<String> = emptyList(),
    coordinator: ClocktowerRecommendationCoordinator = ClocktowerRecommendationCoordinator(),
): PendingGlobalScalarInformationDecision? {
    if (phase != ClocktowerPhase.FirstNight || !step.isRealAction) return null
    val actor = step.actor ?: return null
    val actorSeat = cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
        ?: return null
    val proposition = step.displayProposition
    val kind: StorytellerProviderScalarKindV1
    val metric: String
    val subjects: List<Int>
    val legalValues: List<Pair<String, String>>
    val selectedSeats = fortuneTellerSelectedNames.mapNotNull { name ->
        cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    }
    when (proposition) {
        is InformationProposition.NumericResult -> {
            if (proposition.sourceSeat != actorSeat) return null
            kind = StorytellerProviderScalarKindV1.NUMBER
            metric = proposition.metric.name
            subjects = proposition.subjectSeats
            // The Host's result-first domain expresses legal observable results without
            // committing a particular Spy/Recluse registration witness.
            val manual = step.manualInformationCandidates
                .mapNotNull { option ->
                    val value = option.proposition as? InformationProposition.NumericResult
                    value?.takeIf {
                        it.sourceSeat == actorSeat && it.metric == proposition.metric &&
                            it.subjectSeats == subjects
                    }?.let { it.value.toString() to clocktowerInformationCandidateId(option) }
                }
                .distinctBy { it.first }
            legalValues = if (manual.isNotEmpty()) {
                manual
            } else {
                val preparation = clocktowerNumericInformationPreparation(step, actorSeat, null)
                    ?: ClocktowerNumericInformationPreparation(
                        actorSeat, RoleId(requireNotNull(step.roleEnName)),
                        proposition.metric, subjects, proposition.value,
                        step.numericMinimumValue ?: proposition.value,
                        step.numericMaximumValue ?: proposition.value,
                        step.informationReliability, null, step.previousShownNumber,
                    )
                preparation.prepareUiModel(
                    coordinator, ClocktowerInformationDecisionIdentity(
                        gameId, phase, round, sequence, revision,
                    ),
                ).choices.map { it.value.toString() to it.candidateId }
            }
        }
        is InformationProposition.BooleanResult -> {
            if (proposition.sourceSeat != actorSeat || selectedSeats.size != 2 ||
                selectedSeats.distinct().size != 2 || selectedSeats != proposition.subjectSeats
            ) return null
            kind = StorytellerProviderScalarKindV1.BOOLEAN
            metric = proposition.metric.name
            subjects = proposition.subjectSeats
            val manual = step.manualInformationCandidates
                .mapNotNull { option ->
                    val value = option.proposition as? InformationProposition.BooleanResult
                    value?.takeIf {
                        it.sourceSeat == actorSeat && it.metric == proposition.metric &&
                            it.subjectSeats == subjects
                    }?.let { it.value.toString() to clocktowerInformationCandidateId(option) }
                }
                .distinctBy { it.first }
            legalValues = if (manual.isNotEmpty()) {
                manual
            } else {
                val preparation = clocktowerBooleanInformationPreparation(
                    step, actorSeat, subjects, null,
                ) ?: return null
                preparation.prepareUiModel(
                    coordinator, ClocktowerInformationDecisionIdentity(
                        gameId, phase, round, sequence, revision,
                    ),
                ).choices.map { it.value.toString() to it.candidateId }
            }
        }
        else -> return null
    }
    if (legalValues.isEmpty() || subjects.any { it !in 1..cards.size }) return null
    val reliability = when (step.informationReliability) {
        InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
        InformationReliability.DRUNK -> ReliabilityState.DRUNK
        InformationReliability.POISONED -> ReliabilityState.POISONED
    }
    return PendingGlobalScalarInformationDecision(
        requestIdentity = StorytellerDecisionRequestIdentity(
            gameId, "first-night-scalar:${phase.name}:$round:$sequence:$actorSeat:" +
                "$metric:${subjects.joinToString(",")}",
        ),
        revision = revision,
        sourceSeat = actorSeat,
        abilityRole = RoleId(requireNotNull(step.roleEnName)),
        reliability = reliability,
        kind = kind,
        metric = metric,
        subjectSeats = subjects,
        legalCandidates = legalValues.map { (value, candidateId) ->
            StorytellerProviderCandidateV1(
                candidateId, StorytellerProviderCandidatePayloadV1.ScalarResult(value),
            )
        },
    )
}
