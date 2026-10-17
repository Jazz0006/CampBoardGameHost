package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.GameState
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
 * A current, real-night scalar information boundary for the GLOBAL strategist.
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

internal fun pendingGlobalNightScalarDecision(
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    cards: List<PlayerCard>,
    gameId: String,
    revision: StorytellerDecisionRevision,
    fortuneTellerSelectedNames: List<String> = emptyList(),
    coordinator: ClocktowerRecommendationCoordinator = ClocktowerRecommendationCoordinator(),
    canonicalGameState: GameState? = null,
): PendingGlobalScalarInformationDecision? {
    if (phase !in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) ||
        !step.isRealAction) return null
    val actor = step.actor ?: return null
    // Once a game is committed, the Session owns seat identities. Cards are
    // only the UI's current question and display options, not a second roster.
    if (canonicalGameState != null) {
        require(canonicalGameState.players.sortedBy { it.seat }.map { it.name } ==
            cards.map { it.name }) { "UI card order diverged from canonical Host seats." }
    }
    fun canonicalSeat(name: String): Int? =
        canonicalGameState?.players?.singleOrNull { it.name == name }?.seat
            ?: cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    val actorSeat = canonicalSeat(actor.name) ?: return null
    fun typedRoleResult(option: ClocktowerDisplayOption): Pair<Int, String>? {
        if (option.displayKind != ClocktowerDisplayKind.RoleReveal) return null
        (option.proposition as? InformationProposition.RoleAt)?.let {
            return it.seat to it.role.value
        }
        val seat = option.presentationSubjectSeats.singleOrNull() ?: return null
        val role = option.presentationRoleId?.takeIf { it.isNotBlank() } ?: return null
        return seat to role
    }
    val proposition = step.displayProposition ?: run {
        // A registration-sensitive Host step may not yet have a single true
        // role proposition, although its legal *possible* results already
        // identify one confirmed queried target. Preserve that ambiguity.
        val typed = (step.manualInformationCandidates + step.automaticInformationCandidates)
            .mapNotNull(::typedRoleResult)
        if (typed.isNotEmpty() && typed.map { it.first }.distinct().size == 1)
            InformationProposition.RoleAt(typed.first().first, RoleId(typed.first().second))
        else null
    }
    val kind: StorytellerProviderScalarKindV1
    val metric: String
    val subjects: List<Int>
    val legalValues: List<Pair<String, String>>
    val selectedSeats = fortuneTellerSelectedNames.mapNotNull(::canonicalSeat)
    when (proposition) {
        is InformationProposition.NumericResult -> {
            if (proposition.sourceSeat != actorSeat) return null
            kind = StorytellerProviderScalarKindV1.NUMBER
            metric = proposition.metric.name
            subjects = proposition.subjectSeats
            // The Host's result-first domain expresses legal observable results without
            // committing a particular Spy/Recluse registration witness.
            // Reuse the Host's complete typed domain before constructing a
            // synthetic scalar foundation pool. This matters for unreliable
            // target-dependent numbers (for example Chambermaid): the true
            // value alone is not the full legal result set.
            val hostOptions = step.manualInformationCandidates.ifEmpty {
                step.automaticInformationCandidates
            }
            val manual = hostOptions
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
        is InformationProposition.RoleAt -> {
            // A real target is a prerequisite. Never produce role-result choices
            // for a future player query whose target has not been observed.
            if (proposition.seat !in 1..cards.size ||
                step.displayKind != ClocktowerDisplayKind.RoleReveal &&
                    step.automaticInformationCandidates.none {
                        it.displayKind == ClocktowerDisplayKind.RoleReveal
                    }) return null
            kind = StorytellerProviderScalarKindV1.ROLE
            metric = "REVEALED_CHARACTER"
            subjects = listOf(proposition.seat)
            // Prefer the Host's exact legal result-first domain. When there
            // is no special registration, use the complete Host-rendered role
            // choice domain; only typed RoleAt can enter the model request.
            val roleOptions = step.manualInformationCandidates.ifEmpty {
                step.automaticInformationCandidates
            }
            val typed = roleOptions.mapNotNull(::typedRoleResult)
                .filter { it.first == proposition.seat }
                .map { it.second }.distinct()
            val legalRoles = typed.ifEmpty {
                // Reliable, ordinary target has exactly one confirmed true
                // role, which the Host itself has materialized as RoleAt.
                if (step.informationReliability == InformationReliability.RELIABLE &&
                    step.manualInformationCandidates.isEmpty()
                ) listOf(proposition.role.value) else emptyList()
            }
            legalValues = legalRoles.map { role ->
                role to "role-result:${proposition.seat}:$role"
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
            gameId, "night-scalar:${phase.name}:$round:$sequence:$actorSeat:" +
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
