package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1

/** A current action question, not a Host-generated set of legal answers. */
internal data class PendingMayorRedirectDecision(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    val mayorSeat: Int,
)

internal object MayorRedirectDecisionBoundary {
    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        game: GameState,
        mayorSeat: Int = requireNotNull(game.players.singleOrNull {
            it.actualRole == RoleId("Mayor") && it.alive
        }).seat,
    ): PendingMayorRedirectDecision {
        require(game.playerAt(mayorSeat) != null) { "Mayor source seat is absent from the game." }
        return PendingMayorRedirectDecision(requestIdentity, revision, mayorSeat)
    }

    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): PendingMayorRedirectDecision = create(
        requestIdentity, revision, TroubleBrewingRuntimeGameProjector.project(snapshot, characterRegistry),
    )
}
