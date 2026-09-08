package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.bindGlobal
import com.codex.campboardgamehost.clocktower.epistemic.matches
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature

/** Transient result of one session-owned Global observation transition; never persisted as a second state model. */
internal data class GlobalEpistemicObservationCommit(
    val record: RecordedEpistemicObservation,
    val observationLog: EpistemicObservationLog,
    val nextTimelineGlobalSequence: Long,
    val playerInputRevision: Long,
)

/** Transient result of one session-owned Global action transition; never persisted as a second state model. */
internal data class GlobalActionFactCommit(
    val entry: TimelineBoundActionFact,
    val actionTimeline: ActionFactTimeline,
    val nextTimelineGlobalSequence: Long,
)

/**
 * Immutable read model for the D6.1c production cutover-safe subset.
 *
 * It intentionally excludes [GameState]: App-root mechanics are still the live mechanical source until
 * D6.1d proves and completes canonical GameState cutover. Compose may observe this value, but cannot
 * mutate session-owned identity, revision, or semantic chronology through it.
 */
internal data class ClocktowerSessionView(
    val gameId: String,
    val scriptId: ScriptId,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val actionTimeline: ActionFactTimeline,
    val epistemicObservationLog: EpistemicObservationLog,
    val semanticHistoryMode: ClocktowerSemanticHistoryMode,
    val nextTimelineGlobalSequence: Long,
)

/**
 * Ruleset-independent canonical state owned by one production Clocktower session.
 *
 * Recovery and scripts without an advanced ruleset still need one identity/revision/history owner.
 * [GameSnapshot] remains the stricter projection for consumers that have a resolved [RulesetRef].
 */
internal data class ClocktowerSessionState(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val gameState: GameState,
    val decisionHistory: DecisionHistoryArchive = DecisionHistoryArchive(),
    val crossGameHistory: CrossGameHistory = CrossGameHistory(),
    val actionTimeline: ActionFactTimeline = ActionFactTimeline(),
    val epistemicObservationLog: EpistemicObservationLog = EpistemicObservationLog(),
    val semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
    val nextTimelineGlobalSequence: Long = 0L,
) {
    init {
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0L) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
        require(nextTimelineGlobalSequence >= 0L) { "nextTimelineGlobalSequence cannot be negative." }
        require(gameSeed == gameState.seed) {
            "The session gameSeed must match the GameState seed."
        }
        semanticHistoryMode.requireCompatible(
            actionTimeline = actionTimeline,
            observationLog = epistemicObservationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }
}

internal class ClocktowerGameSession private constructor(
    initialState: ClocktowerSessionState,
    private val defaultRulesetRef: RulesetRef?,
    initialSnapshotCache: GameSnapshot? = null,
) {
    var state: ClocktowerSessionState = initialState
        private set

    private var snapshotCache: GameSnapshot? = initialSnapshotCache

    /** Read-only cutover projection suitable for publishing through an observable UI boundary. */
    val view: ClocktowerSessionView
        get() = ClocktowerSessionView(
            gameId = state.gameId,
            scriptId = state.gameState.script,
            gameStateRevision = state.gameStateRevision,
            playerInputRevision = state.playerInputRevision,
            gameSeed = state.gameSeed,
            actionTimeline = state.actionTimeline,
            epistemicObservationLog = state.epistemicObservationLog,
            semanticHistoryMode = state.semanticHistoryMode,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
        )

    /**
     * Backward-compatible strict snapshot view for ruleset-backed sessions.
     * Production sessions without an advanced ruleset must use [state] until a real ref is available.
     */
    val snapshot: GameSnapshot
        get() {
            val rulesetRef = requireNotNull(defaultRulesetRef) {
                "This production session has no default advanced RulesetRef; use state or toGameSnapshot(ref)."
            }
            snapshotCache?.let { return it }
            return toGameSnapshot(rulesetRef).also { snapshotCache = it }
        }

    fun toGameSnapshot(rulesetRef: RulesetRef): GameSnapshot = GameSnapshot(
        gameId = state.gameId,
        gameStateRevision = state.gameStateRevision,
        playerInputRevision = state.playerInputRevision,
        gameSeed = state.gameSeed,
        rulesetRef = rulesetRef,
        gameState = state.gameState,
        decisionHistory = state.decisionHistory,
        crossGameHistory = state.crossGameHistory,
        actionTimeline = state.actionTimeline,
        epistemicObservationLog = state.epistemicObservationLog,
        semanticHistoryMode = state.semanticHistoryMode,
        nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
    )

    fun updateGameState(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        if (nextState == state.gameState) return state
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /**
     * Commits one accepted production mechanical boundary atomically.
     *
     * Unlike [updateGameState], accepted production boundaries preserve the established revision
     * cadence even when their projected [GameState] is value-equal to the previous state.
     */
    fun commitGameStateBoundary(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /** Production event/decision boundary whose accepted revision cadence is independent of state equality. */
    fun advanceGameStateRevision(): ClocktowerSessionState = updateState(
        state.copy(gameStateRevision = state.gameStateRevision + 1),
    )

    fun recordPlayerInput(): ClocktowerSessionState = updateState(
        state.copy(playerInputRevision = state.playerInputRevision + 1),
    )

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
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
        )
        updateState(state.copy(nextTimelineGlobalSequence = point.globalSequence + 1))
        return point
    }

    /** Atomic instance authority for newly produced Global actions. */
    fun commitGlobalActionFact(draft: ActionFactDraft): TimelineBoundActionFact {
        val committed = commitGlobalActionFact(
            semanticHistoryMode = state.semanticHistoryMode,
            actionTimeline = state.actionTimeline,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            draft = draft,
        )
        if (
            committed.actionTimeline === state.actionTimeline &&
            committed.nextTimelineGlobalSequence == state.nextTimelineGlobalSequence
        ) {
            return committed.entry
        }
        updateState(
            state.copy(
                actionTimeline = committed.actionTimeline,
                nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
            ),
        )
        return committed.entry
    }

    /** Non-mutating validation/projection for production flows that must persist before commit. */
    fun preflightGlobalEpistemicObservation(draft: EpistemicObservationDraft): GlobalEpistemicObservationCommit =
        commitGlobalEpistemicObservation(
            semanticHistoryMode = state.semanticHistoryMode,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            playerInputRevision = state.playerInputRevision,
            draft = draft,
            actionTimeline = state.actionTimeline,
        )

    /** Atomic instance authority for newly produced Global observations. */
    fun commitGlobalEpistemicObservation(draft: EpistemicObservationDraft): RecordedEpistemicObservation {
        val committed = preflightGlobalEpistemicObservation(draft)
        if (
            committed.observationLog === state.epistemicObservationLog &&
            committed.nextTimelineGlobalSequence == state.nextTimelineGlobalSequence &&
            committed.playerInputRevision == state.playerInputRevision
        ) {
            return committed.record
        }
        updateState(
            state.copy(
                playerInputRevision = committed.playerInputRevision,
                epistemicObservationLog = committed.observationLog,
                nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
            ),
        )
        return committed.record
    }

    /** Records pre-cutover LegacyLocal information only; Global producers must use the draft API. */
    fun recordEpistemicObservation(record: RecordedEpistemicObservation): ClocktowerSessionState {
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.LEGACY_LOCAL) {
            "Direct durable observation recording is reserved for LEGACY_LOCAL history."
        }
        require(record.timelineBinding === ObservationTimelineBinding.LegacyLocal) {
            "LEGACY_LOCAL session cannot accept a pre-bound Global observation."
        }
        return updateState(
            state.copy(
                playerInputRevision = state.playerInputRevision + 1,
                epistemicObservationLog = state.epistemicObservationLog.append(record),
            ),
        )
    }

    fun recordCompletedGameSignature(signature: HistoricalClueSignature): ClocktowerSessionState = updateState(
        state.copy(crossGameHistory = state.crossGameHistory.append(signature)),
    )

    private fun updateState(nextState: ClocktowerSessionState): ClocktowerSessionState {
        if (nextState == state) return state
        state = nextState
        snapshotCache = null
        return state
    }

    companion object {
        /**
         * Stateless session transition used by the production Compose adapter until the full game
         * state is session-owned. It owns the same cursor/log semantics as the instance API without
         * requiring a synthetic RulesetRef for scripts whose advanced ruleset is not loaded.
         * Mechanical action capture does not increment game/input revisions: the production state
         * mutation that the fact records remains the owner of those revisions.
         */
        fun commitGlobalActionFact(
            semanticHistoryMode: ClocktowerSemanticHistoryMode,
            actionTimeline: ActionFactTimeline,
            observationLog: EpistemicObservationLog,
            nextTimelineGlobalSequence: Long,
            draft: ActionFactDraft,
        ): GlobalActionFactCommit {
            require(semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
                "Global action commit requires GLOBAL_V1 semantic history."
            }
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )

            actionTimeline.entries.firstOrNull { it.fact.actionId == draft.actionId }?.let { existing ->
                require(draft.matches(existing)) {
                    "Action ID '${draft.actionId}' is already committed with different content."
                }
                return GlobalActionFactCommit(
                    entry = existing,
                    actionTimeline = actionTimeline,
                    nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                )
            }

            val point = nextTimelinePoint(
                phase = draft.phase,
                round = draft.round,
                sequence = draft.sequence,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )
            val entry = draft.bindGlobal(point)
            val nextTimeline = actionTimeline.append(entry)
            val nextCursor = point.globalSequence + 1
            semanticHistoryMode.requireCompatible(
                actionTimeline = nextTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextCursor,
            )
            return GlobalActionFactCommit(
                entry = entry,
                actionTimeline = nextTimeline,
                nextTimelineGlobalSequence = nextCursor,
            )
        }

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
            actionTimeline: ActionFactTimeline = ActionFactTimeline(),
        ): GlobalEpistemicObservationCommit {
            require(semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
                "Global observation commit requires GLOBAL_V1 semantic history."
            }
            require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )

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
            val nextCursor = point.globalSequence + 1
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = nextLog,
                nextTimelineGlobalSequence = nextCursor,
            )
            return GlobalEpistemicObservationCommit(
                record = record,
                observationLog = nextLog,
                nextTimelineGlobalSequence = nextCursor,
                playerInputRevision = playerInputRevision + 1,
            )
        }

        fun create(
            gameId: String,
            gameSeed: Long,
            rulesetRef: RulesetRef,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        ): ClocktowerGameSession {
            val snapshot = GameSnapshot(
                gameId = gameId,
                gameStateRevision = 0,
                playerInputRevision = 0,
                gameSeed = gameSeed,
                rulesetRef = rulesetRef,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
            )
            return ClocktowerGameSession(
                initialState = snapshot.toSessionState(),
                defaultRulesetRef = rulesetRef,
                initialSnapshotCache = snapshot,
            )
        }

        fun createProduction(
            gameId: String,
            gameSeed: Long,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        ): ClocktowerGameSession = ClocktowerGameSession(
            initialState = ClocktowerSessionState(
                gameId = gameId,
                gameStateRevision = 0L,
                playerInputRevision = 0L,
                gameSeed = gameSeed,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
            ),
            defaultRulesetRef = null,
        )

        fun restore(snapshot: GameSnapshot): ClocktowerGameSession = ClocktowerGameSession(
            initialState = snapshot.toSessionState(),
            defaultRulesetRef = snapshot.rulesetRef,
            initialSnapshotCache = snapshot,
        )

        fun restoreProduction(state: ClocktowerSessionState): ClocktowerGameSession = ClocktowerGameSession(
            initialState = state,
            defaultRulesetRef = null,
        )

        private fun GameSnapshot.toSessionState(): ClocktowerSessionState = ClocktowerSessionState(
            gameId = gameId,
            gameStateRevision = gameStateRevision,
            playerInputRevision = playerInputRevision,
            gameSeed = gameSeed,
            gameState = gameState,
            decisionHistory = decisionHistory,
            crossGameHistory = crossGameHistory,
            actionTimeline = actionTimeline,
            epistemicObservationLog = epistemicObservationLog,
            semanticHistoryMode = semanticHistoryMode,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )

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
