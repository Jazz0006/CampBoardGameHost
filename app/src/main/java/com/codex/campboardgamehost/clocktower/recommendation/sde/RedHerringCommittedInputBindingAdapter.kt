package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState

/**
 * Correlates the externally owned committed Red-Herring player name back to the exact setup-owned
 * candidate identity that produced the precommit SDE reference.
 *
 * This adapter owns no setup state. It never parses candidate IDs, never regenerates Red-Herring
 * legality, and never introduces Fortune Teller target choices. The canonical game resolves the
 * external player name to one seat; the existing precommit projection resolves that seat to the
 * setup-owned [CommittedDecisionInputRef].
 */
internal object RedHerringCommittedInputBindingAdapter {
    fun bind(
        game: GameState,
        projection: RedHerringSetupPrecommitProjection,
        committedTargetName: String,
    ): SdeDecisionInputBindings.Captured {
        require(committedTargetName.isNotBlank()) {
            "Committed Red-Herring target name cannot be blank."
        }

        val matchingPlayers = game.players.filter { player ->
            player.name == committedTargetName
        }
        require(matchingPlayers.size == 1) {
            "Committed Red-Herring target name must resolve to exactly one canonical player."
        }
        val targetSeat = matchingPlayers.single().seat

        val matchingCandidates = projection.candidates.filter { candidate ->
            candidate.targetSeat == targetSeat
        }
        require(matchingCandidates.size == 1) {
            "Committed Red-Herring target must belong to the setup-owned legal candidate domain."
        }
        val committedRef = matchingCandidates.single().proposedCommitRef
        require(committedRef.kind == SdeCommittedDecisionInputKind.RED_HERRING) {
            "Committed Red-Herring binding requires the setup-owned RED_HERRING reference."
        }

        return SdeDecisionInputBindings.Captured(
            committedInputRefs = setOf(committedRef),
            playerControlledInputRefs = emptySet(),
        )
    }
}