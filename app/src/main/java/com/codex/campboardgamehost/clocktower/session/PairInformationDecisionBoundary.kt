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
    val requestIdentity: InformationDecisionRequestIdentity,
    val revision: InformationDecisionRevision,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val legalCandidates: List<PairInformationLegalCandidate>,
) {
    private val candidatesById = legalCandidates.associateBy(PairInformationLegalCandidate::candidateId)

    init {
        require(sourceSeat > 0) { "Pair-information source seat must be positive." }
        require(legalCandidates.isNotEmpty()) { "Pair-information decision requires legal candidates." }
        require(candidatesById.size == legalCandidates.size) { "Pair-information candidate IDs must be unique." }
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
        currentRevision: InformationDecisionRevision,
    ): PairInformationDecisionConfirmation {
        if (currentRevision != revision) {
            return PairInformationDecisionConfirmation.Blocked(PairInformationDecisionBlockReason.STALE_CONTEXT)
        }
        val candidate = candidatesById[candidateId]
            ?: return PairInformationDecisionConfirmation.Blocked(PairInformationDecisionBlockReason.ILLEGAL_CANDIDATE)

        return PairInformationDecisionConfirmation.Confirmed(
            candidateId = candidateId,
            observation = candidate.toAbilityObservation(
                sourceSeat = sourceSeat,
                abilityRole = abilityRole,
                reliability = reliability,
            ),
        )
    }
}

internal object PairInformationDecisionBoundary {
    fun create(
        requestIdentity: InformationDecisionRequestIdentity,
        revision: InformationDecisionRevision,
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): PendingPairInformationDecision = PendingPairInformationDecision(
        requestIdentity = requestIdentity,
        revision = revision,
        sourceSeat = sourceSeat,
        abilityRole = abilityRole,
        reliability = reliability,
        legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        ),
    )
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
