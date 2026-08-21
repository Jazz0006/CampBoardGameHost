package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog

data class GameSnapshot(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val rulesetRef: RulesetRef,
    val gameState: GameState,
    val decisionHistory: DecisionHistoryArchive = DecisionHistoryArchive(),
    val crossGameHistory: CrossGameHistory = CrossGameHistory(),
    /** Recipient-scoped facts already delivered during this game; actual roles are never inferred from it. */
    val epistemicObservationLog: EpistemicObservationLog = EpistemicObservationLog(),
    /** Explicit chronology mode; new/default snapshots stay LegacyLocal until the later producer cutover. */
    val semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
    /** Next game-wide monotonic identity reserved for a committed epistemic timeline point. */
    val nextTimelineGlobalSequence: Long = 0L,
) {
    init {
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
        require(nextTimelineGlobalSequence >= 0) { "nextTimelineGlobalSequence cannot be negative." }
        require(gameSeed == gameState.seed) {
            "The persisted gameSeed must match the recommendation GameState seed."
        }
        require(rulesetRef.scriptId == gameState.script) {
            "RulesetRef and GameState must identify the same script."
        }
        semanticHistoryMode.requireCompatible(
            observationLog = epistemicObservationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }
}
