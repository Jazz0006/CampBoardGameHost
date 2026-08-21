package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature

/** Transient result of one session-owned Global observation transition; never persisted as a second state model. */
internal data class GlobalEpistemicObservationCommit(
    val record: RecordedEpistemicObservation,
    val observationLog: EpistemicObservationLog,
    val nextTimelineGlobalSequence: Long,
    val playerInputRevision: Long,
)

internal class ClocktowerGameSession private constructor(
    initialSnapshot: GameSnapshot,
) {
    var snapshot: GameSnapshot = initialSnapshot
        private set

    fun updateGameState(nextState: GameState): GameSnapshot {
        require(nextState.seed == snapshot.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        if (nextState == snapshot.gameState) return snapshot
        snapshot = snapshot.copy(
            gameStateRevision = snapshot.gameStateRevision + 1,
            gameState = nextState,
        )
        return snapshot
    }

    fun recordPlayerInput(): GameSnapshot {
        snapshot = snapshot.copy(playerInputRevision = snapshot.playerInputRevision + 1)
        return snapshot
    }

    /**
     * Allocates the next game-wide timeline identity without changing semantic game/input revisions.
     * Local [sequence] remains phase-specific replay/display context and never resets global ordering.
     */
    fun allocateTimelinePoint(
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): TimelinePoint {
        val point = nextTimelinePoint(
            phase = phase,
            round = round,
            sequence = sequence,
            nextTimelineGlobalSequence = snapshot.nextTimelineGlobalSequence,
        )
        snapshot = snapshot.copy(nextTimelineGlobalSequence = point.globalSequence + 1)
        return point
    }

    /** Atomic instance authority for newly produced Global observations. */
    fun commitGlobalEpistemicObservation(draft: EpistemicObservationDraft): RecordedEpistemicObservation {
        val committed = commitGlobalEpistemicObservation(
            semanticHistoryMode = snapshot.semanticHistoryMode,
            observationLog = snapshot.epistemicObservationLog,
            nextTimelineGlobalSequence = snapshot.nextTimelineGlobalSequence,
            playerInputRevision = snapshot.playerInputRevision,
            draft = draft,
        )
        if (
            committed.observationLog === snapshot.epistemicObservationLog &&
            committed.nextTimelineGlobalSequence == snapshot.nextTimelineGlobalSequence &&
            committed.playerInputRevision == snapshot.playerInputRevision
        ) {
            return committed.record
        }
        snapshot = snapshot.copy(
            playerInputRevision = committed.playerInputRevision,
            epistemicObservationLog = committed.observationLog,
            nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
        )
        return committed.record
    }

    /** Records pre-cutover LegacyLocal information only; Global producers must use the draft API. */
    fun recordEpistemicObservation(record: RecordedEpistemicObservation): GameSnapshot {
        require(snapshot.semanticHistoryMode == ClocktowerSemanticHistoryMode.LEGACY_LOCAL) {
            "Direct durable observation recording is reserved for LEGACY_LOCAL history."
        }
        require(record.timelineBinding === ObservationTimelineBinding.LegacyLocal) {
            "LEGACY_LOCAL session cannot accept a pre-bound Global observation."
        }
        snapshot = snapshot.copy(
            playerInputRevision = snapshot.playerInputRevision + 1,
            epistemicObservationLog = snapshot.epistemicObservationLog.append(record),
        )
        return snapshot
    }

    fun recordCompletedGameSignature(signature: HistoricalClueSignature): GameSnapshot {
        snapshot = snapshot.copy(crossGameHistory = snapshot.crossGameHistory.append(signature))
        return snapshot
    }

    companion object {
        /**
         * Stateless session transition used by the production Compose adapter until the full game
         * state is session-owned. It owns the same cursor/log/revision semantics as the instance API
         * without requiring a synthetic RulesetRef for scripts whose advanced ruleset is not loaded.
         */
        fun commitGlobalEpistemicObservation(
            semanticHistoryMode: ClocktowerSemanticHistoryMode,
            observationLog: EpistemicObservationLog,
            nextTimelineGlobalSequence: Long,
            playerInputRevision: Long,
            draft: EpistemicObservationDraft,
        ): GlobalEpistemicObservationCommit {
            require(semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
                "Global observation commit requires GLOBAL_V1 semantic history."
            }
            require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
            semanticHistoryMode.requireCompatible(observationLog, nextTimelineGlobalSequence)

            observationLog.records.firstOrNull { it.recordId == draft.recordId }?.let { existing ->
                require(draft.matches(existing)) {
                    "Observation record ID '${draft.recordId}' is already committed with different content."
                }
                require(existing.timelineBinding is ObservationTimelineBinding.Global) {
                    "GLOBAL_V1 history cannot reuse a LegacyLocal observation record ID."
                }
                return GlobalEpistemicObservationCommit(
                    record = existing,
                    observationLog = observationLog,
                    nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                    playerInputRevision = playerInputRevision,
                )
            }

            check(playerInputRevision != Long.MAX_VALUE) { "Player input revision exhausted." }
            val point = nextTimelinePoint(
                phase = draft.phase,
                round = draft.round,
                sequence = draft.sequence,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )
            val record = draft.bindGlobal(point)
            val nextLog = observationLog.append(record)
            return GlobalEpistemicObservationCommit(
                record = record,
                observationLog = nextLog,
                nextTimelineGlobalSequence = point.globalSequence + 1,
                playerInputRevision = playerInputRevision + 1,
            )
        }

        fun create(
            gameId: String,
            gameSeed: Long,
            rulesetRef: RulesetRef,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        ): ClocktowerGameSession = ClocktowerGameSession(
            GameSnapshot(
                gameId = gameId,
                gameStateRevision = 0,
                playerInputRevision = 0,
                gameSeed = gameSeed,
                rulesetRef = rulesetRef,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
            ),
        )

        fun restore(snapshot: GameSnapshot): ClocktowerGameSession = ClocktowerGameSession(snapshot)

        private fun nextTimelinePoint(
            phase: StorytellerPhase,
            round: Int,
            sequence: Int,
            nextTimelineGlobalSequence: Long,
        ): TimelinePoint {
            require(nextTimelineGlobalSequence >= 0L) { "Timeline global sequence cannot be negative." }
            check(nextTimelineGlobalSequence != Long.MAX_VALUE) { "Timeline global sequence exhausted." }
            return TimelinePoint(
                phase = phase,
                round = round,
                sequence = sequence,
                globalSequence = nextTimelineGlobalSequence,
            )
        }
    }
}
