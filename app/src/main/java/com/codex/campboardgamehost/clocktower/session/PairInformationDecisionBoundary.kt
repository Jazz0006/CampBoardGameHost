package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RegistrationDecision
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain

/** RES-1 engine/session boundary for one pending pair-information Storyteller decision. */
internal data class PendingPairInformationDecision(
    val pending: PendingStorytellerDecision<PairInformationLegalCandidate>,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
) {
    val requestIdentity: StorytellerDecisionRequestIdentity get() = pending.requestIdentity
    val revision: StorytellerDecisionRevision get() = pending.revision
    val legalCandidates: List<PairInformationLegalCandidate>
        get() = pending.legalCandidates.map { candidate -> candidate.payload }

    init {
        require(sourceSeat > 0) { "Pair-information source seat must be positive." }
    }

    fun candidateIdFor(
        shownRole: RoleId?,
        candidateSeats: List<Int>,
    ): String? {
        val canonicalSeats = candidateSeats.sorted()
        return legalCandidates.singleOrNull { candidate ->
            candidate.outcome.shownRole == shownRole &&
                candidate.outcome.candidateSeats == canonicalSeats
        }?.candidateId
    }

    fun confirm(
        candidateId: String,
        currentRevision: StorytellerDecisionRevision,
    ): PairInformationDecisionConfirmation = when (
        val confirmation = pending.confirm(candidateId, currentRevision)
    ) {
        is StorytellerDecisionConfirmation.Confirmed -> PairInformationDecisionConfirmation.Confirmed(
            candidateId = confirmation.candidate.candidateId,
            observation = confirmation.candidate.payload.toAbilityObservation(
                sourceSeat = sourceSeat,
                abilityRole = abilityRole,
                reliability = reliability,
            ),
        )

        is StorytellerDecisionConfirmation.Blocked -> PairInformationDecisionConfirmation.Blocked(
            when (confirmation.reason) {
                StorytellerDecisionBlockReason.STALE_CONTEXT -> PairInformationDecisionBlockReason.STALE_CONTEXT
                StorytellerDecisionBlockReason.ILLEGAL_CANDIDATE -> PairInformationDecisionBlockReason.ILLEGAL_CANDIDATE
            },
        )
    }
}

internal object PairInformationDecisionBoundary {
    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): PendingPairInformationDecision {
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
        return PendingPairInformationDecision(
            pending = PendingStorytellerDecision(
                requestIdentity = requestIdentity,
                revision = revision,
                legalCandidates = legalCandidates.map { candidate ->
                    StorytellerDecisionCandidate(
                        candidateId = candidate.candidateId,
                        payload = candidate,
                    )
                },
            ),
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
    }
}

internal enum class PairInformationDecisionBlockReason {
    STALE_CONTEXT,
    ILLEGAL_CANDIDATE,
}

internal sealed interface PairInformationDecisionConfirmation {
    data class Confirmed(
        val candidateId: String,
        val observation: AbilityObservation,
    ) : PairInformationDecisionConfirmation

    data class Blocked(
        val reason: PairInformationDecisionBlockReason,
    ) : PairInformationDecisionConfirmation
}

private fun PairInformationLegalCandidate.toAbilityObservation(
    sourceSeat: Int,
    abilityRole: RoleId,
    reliability: ReliabilityState,
): AbilityObservation = AbilityObservation(
    sourceSeat = sourceSeat,
    perceivedRole = abilityRole,
    shownRole = outcome.shownRole,
    candidateSeats = outcome.candidateSeats,
    reliability = reliability,
    semanticTruth = semanticTruth,
    registrations = registrations.map { registration ->
        RegistrationDecision(
            playerSeat = registration.subjectSeat,
            affectedAbility = abilityRole,
            registeredAlignment = registration.registeredAlignment,
            registeredType = registration.registeredType,
            registeredRole = registration.registeredRole,
            reason = registration.reason,
        )
    },
)
