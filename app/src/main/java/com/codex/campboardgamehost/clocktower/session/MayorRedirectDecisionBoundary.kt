package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegalDomain

internal data class PendingMayorRedirectDecision(
    val pending: PendingStorytellerDecision<Int>,
    val mayorSeat: Int,
) {
    val requestIdentity: StorytellerDecisionRequestIdentity get() = pending.requestIdentity
    val revision: StorytellerDecisionRevision get() = pending.revision
    val legalTargetSeats: Set<Int>
        get() = pending.legalCandidates.mapTo(linkedSetOf()) { candidate -> candidate.payload }

    fun candidateIdForSeat(targetSeat: Int): String? = pending.legalCandidates
        .singleOrNull { candidate -> candidate.payload == targetSeat }
        ?.candidateId

    fun confirm(
        candidateId: String,
        currentRevision: StorytellerDecisionRevision,
    ): MayorRedirectDecisionConfirmation = when (val confirmation = pending.confirm(candidateId, currentRevision)) {
        is StorytellerDecisionConfirmation.Confirmed -> MayorRedirectDecisionConfirmation.Confirmed(
            candidateId = confirmation.candidate.candidateId,
            targetSeat = confirmation.candidate.payload,
        )

        is StorytellerDecisionConfirmation.Blocked -> MayorRedirectDecisionConfirmation.Blocked(
            when (confirmation.reason) {
                StorytellerDecisionBlockReason.STALE_CONTEXT -> MayorRedirectDecisionBlockReason.STALE_CONTEXT
                StorytellerDecisionBlockReason.ILLEGAL_CANDIDATE -> MayorRedirectDecisionBlockReason.ILLEGAL_CANDIDATE
            },
        )
    }
}

internal object MayorRedirectDecisionBoundary {
    private val mayorRole = RoleId("Mayor")

    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        game: GameState,
    ): PendingMayorRedirectDecision {
        val mayorSeat = requireNotNull(
            game.players.singleOrNull { player -> player.actualRole == mayorRole && player.alive },
        ) { "Mayor redirect decision requires exactly one living actual Mayor." }.seat
        return create(
            requestIdentity = requestIdentity,
            revision = revision,
            game = game,
            mayorSeat = mayorSeat,
        )
    }

    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        game: GameState,
        mayorSeat: Int,
    ): PendingMayorRedirectDecision {
        val domain = MayorRedirectLegalDomain.resolve(game, mayorSeat)
        return PendingMayorRedirectDecision(
            pending = PendingStorytellerDecision(
                requestIdentity = requestIdentity,
                revision = revision,
                legalCandidates = domain.legalTargetSeats
                    .sorted()
                    .map { targetSeat ->
                        StorytellerDecisionCandidate(
                            candidateId = candidateIdForSeat(targetSeat),
                            payload = targetSeat,
                        )
                    },
            ),
            mayorSeat = mayorSeat,
        )
    }

    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): PendingMayorRedirectDecision = create(
        requestIdentity = requestIdentity,
        revision = revision,
        game = TroubleBrewingRuntimeGameProjector.project(snapshot, characterRegistry),
    )

    fun candidateIdForSeat(targetSeat: Int): String {
        require(targetSeat > 0) { "Mayor redirect target seat must be positive." }
        return "mayor-redirect-v1|seat:$targetSeat"
    }
}

internal enum class MayorRedirectDecisionBlockReason {
    STALE_CONTEXT,
    ILLEGAL_CANDIDATE,
}

internal sealed interface MayorRedirectDecisionConfirmation {
    data class Confirmed(
        val candidateId: String,
        val targetSeat: Int,
    ) : MayorRedirectDecisionConfirmation

    data class Blocked(
        val reason: MayorRedirectDecisionBlockReason,
    ) : MayorRedirectDecisionConfirmation
}
