package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.B4DynamicPlayerWorldSetShadow
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowCandidate
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowOutcome
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowReport
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowRequest
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis

/**
 * Production-isolated session seam for the A3/B4 historical exact shadow.
 *
 * The coordinator consumes the current session snapshot only as GLOBAL semantic-history authority;
 * the exact replay still starts from an explicitly supplied setup snapshot. Its result is a B4
 * cardinality report and is not connected to recommendation selection or Host authority.
 */
internal class ClocktowerB4HistoricalShadowCoordinator(
    private val validatedRuleset: ValidatedClocktowerRuleset,
) {
    fun evaluate(
        setupSnapshot: GameSnapshot,
        currentSnapshot: GameSnapshot,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        perceivedRolesBySeat: Map<Int, RoleId>,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        candidates: List<B4ShadowCandidate>,
    ): B4ShadowReport {
        if (!sameHistoricalSession(setupSnapshot, currentSnapshot)) {
            return deferred()
        }
        if (currentSnapshot.semanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            return deferred()
        }

        return try {
            B4DynamicPlayerWorldSetShadow(validatedRuleset).evaluate(
                B4ShadowRequest(
                    initialSnapshot = setupSnapshot,
                    initialPhase = initialPhase,
                    initialRound = initialRound,
                    actionTimeline = currentSnapshot.actionTimeline,
                    perceivedRolesBySeat = perceivedRolesBySeat,
                    observationLog = currentSnapshot.epistemicObservationLog,
                    hypothesis = hypothesis,
                    roleDefinitions = roleDefinitions,
                    candidates = candidates,
                ),
            )
        } catch (_: IllegalArgumentException) {
            deferred()
        }
    }

    private fun sameHistoricalSession(
        setupSnapshot: GameSnapshot,
        currentSnapshot: GameSnapshot,
    ): Boolean =
        setupSnapshot.gameId == currentSnapshot.gameId &&
            setupSnapshot.gameSeed == currentSnapshot.gameSeed &&
            setupSnapshot.rulesetRef == currentSnapshot.rulesetRef &&
            setupSnapshot.gameState.scriptId == currentSnapshot.gameState.scriptId &&
            setupSnapshot.gameState.players.map { it.seat }.toSet() ==
                currentSnapshot.gameState.players.map { it.seat }.toSet() &&
            currentSnapshot.gameStateRevision >= setupSnapshot.gameStateRevision

    private fun deferred(): B4ShadowReport =
        B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
}
