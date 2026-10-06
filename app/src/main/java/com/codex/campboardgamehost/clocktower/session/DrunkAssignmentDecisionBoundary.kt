package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef

/**
 * Engine-owned pending-decision boundary for Trouble Brewing Drunk late binding.
 *
 * The complete legal domain comes from setup/rules ownership. Stable candidate IDs deliberately
 * preserve the historical Drunk decision identity while removing any dependency on SDE or
 * recommendation implementation.
 */
internal data class PendingDrunkAssignmentDecision(
    val pending: PendingStorytellerDecision<TroubleBrewingDrunkCandidateRef>,
) {
    val requestIdentity: StorytellerDecisionRequestIdentity
        get() = pending.requestIdentity

    val revision: StorytellerDecisionRevision
        get() = pending.revision

    val legalCandidates: List<StorytellerDecisionCandidate<TroubleBrewingDrunkCandidateRef>>
        get() = pending.legalCandidates

    fun confirm(
        candidateId: String,
        currentRevision: StorytellerDecisionRevision,
    ): StorytellerDecisionConfirmation<TroubleBrewingDrunkCandidateRef> =
        pending.confirm(candidateId, currentRevision)
}

internal object DrunkAssignmentDecisionBoundary {
    fun create(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
        revision: StorytellerDecisionRevision,
    ): PendingDrunkAssignmentDecision {
        val candidates = TroubleBrewingDrunkCandidateDomain.legalCandidateRefs(
            snapshot = snapshot,
            characterRegistry = characterRegistry,
        )
        return PendingDrunkAssignmentDecision(
            pending = PendingStorytellerDecision(
                requestIdentity = StorytellerDecisionRequestIdentity(
                    gameId = snapshot.gameId,
                    requestId = "setup:drunk-seat:${snapshot.gameId}",
                ),
                revision = revision,
                legalCandidates = candidates.map { candidate ->
                    StorytellerDecisionCandidate(
                        candidateId = "setup:drunk-seat:seat-${candidate.seat}",
                        payload = candidate,
                    )
                },
            ),
        )
    }
}
