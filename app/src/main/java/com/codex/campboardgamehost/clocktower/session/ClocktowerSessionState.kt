package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

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
 * Ruleset-independent canonical state owned by one production Clocktower session.
 *
 * Scripts without an advanced ruleset still need one identity/revision/history owner.
 * [GameSnapshot] remains the stricter projection for consumers that have a resolved [RulesetRef].
 */
internal data class ClocktowerSessionState(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val gameState: GameState,
    val actionTimeline: ActionFactTimeline = ActionFactTimeline(),
    val epistemicObservationLog: EpistemicObservationLog = EpistemicObservationLog(),
    val semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
    val nextTimelineGlobalSequence: Long = 0L,
    val storytellerPlayerContextBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
    /** Accepted current choices are stored once here, not in Compose draft state. */
    val nightTargets: ClocktowerNightTargets = ClocktowerNightTargets(),
    val voteStanding: ClocktowerVoteStanding = ClocktowerVoteStanding(),
    val redHerringSeat: Int? = null,
    val demonBluffRoleIds: List<RoleId> = emptyList(),
) {
    private val lastPhaseAdvance: ActionFact.PhaseAdvance?
        get() = actionTimeline.entries.asReversed().firstNotNullOfOrNull { it.fact as? ActionFact.PhaseAdvance }
    val phase: StorytellerPhase get() = lastPhaseAdvance?.phase ?: StorytellerPhase.FIRST_NIGHT
    val round: Int get() = lastPhaseAdvance?.round ?: 1

    init {
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0L) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
        require(nextTimelineGlobalSequence >= 0L) { "nextTimelineGlobalSequence cannot be negative." }
        require(gameSeed == gameState.seed) {
            "The session gameSeed must match the GameState seed."
        }
        val currentSeats = gameState.players.map { it.seat }.toSet()
        require(storytellerPlayerContextBySeat.keys.all { it in currentSeats }) {
            "Storyteller player-context overrides must reference current GameState seats."
        }
        redHerringSeat?.let { seat ->
            require(gameState.playerAt(seat) != null) {
                "Committed Red Herring must reference a current Session seat."
            }
        }
        require(demonBluffRoleIds.distinct() == demonBluffRoleIds) {
            "Committed Demon bluff roles must be distinct."
        }
        semanticHistoryMode.requireCompatible(
            actionTimeline = actionTimeline,
            observationLog = epistemicObservationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }
}

/** Action categories for reading accepted night facts; no pending state is retained. */
internal enum class NightOperationKind { ATTACK, POISON, PROTECT, BUTLER }
