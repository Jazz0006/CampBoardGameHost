package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import java.util.Collections

internal enum class SdeHistoricalReplayInputOrigin {
    FRESH_AUTHORITIES,
    DURABLE_EXPORT,
}

internal data class SdeHistoricalReplayMaterialization(
    val input: SdeHistoricalReplayInput,
    val origin: SdeHistoricalReplayInputOrigin,
)

/**
 * Versioned read-only inputs needed to reconstruct an SDE historical evaluation baseline.
 *
 * This is a transport projection of existing owners, never a writable game-state authority.
 * Decision-specific legal candidates and policy outputs deliberately do not belong here.
 */
internal class SdeHistoricalReplayInput(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rulesetRef: RulesetRef,
    val committedSetup: CommittedClocktowerSetup,
    playerNamesBySeat: List<String>,
    val actionTimeline: ActionFactTimeline,
    val observationLog: EpistemicObservationLog,
    val nextTimelineGlobalSequence: Long,
) {
    val playerNamesBySeat: List<String> = Collections.unmodifiableList(playerNamesBySeat.toList())

    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION) {
            "Unsupported SDE historical replay input version $schemaVersion."
        }
        require(gameId.isNotBlank()) { "SDE historical replay game ID cannot be blank." }
        require(gameStateRevision >= 0 && playerInputRevision >= 0) {
            "SDE historical replay revisions cannot be negative."
        }
        require(rulesetRef.scriptId == committedSetup.script) {
            "Replay ruleset and committed setup must use the same script."
        }
        require(playerNamesBySeat.size == committedSetup.playerCount) {
            "Replay player names must cover every committed setup seat."
        }
        require(playerNamesBySeat.all(String::isNotBlank) && playerNamesBySeat.distinct().size == playerNamesBySeat.size) {
            "Replay player names must be non-blank and unique."
        }
        ClocktowerSemanticHistoryMode.GLOBAL_V1.requireCompatible(
            actionTimeline = actionTimeline,
            observationLog = observationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }

    fun toCommittedSetup(): CommittedClocktowerSetup = committedSetup

    fun toGameSnapshot(roleDefinitions: Collection<RoleDefinition>): GameSnapshot {
        val rolesById = roleDefinitions.associateBy(RoleDefinition::id)
        require(rolesById.isNotEmpty()) { "Replay reconstruction requires role definitions." }
        val players = committedSetup.assignments.mapIndexed { index, assignment ->
            val definition = requireNotNull(rolesById[assignment.actualRole]) {
                "Missing role definition for committed role ${assignment.actualRole.value}."
            }
            PlayerState(
                seat = assignment.seat,
                name = playerNamesBySeat[index],
                actualRole = assignment.actualRole,
                actualAlignment = definition.alignment,
                actualType = definition.type,
                shownRole = assignment.shownRole,
                alive = true,
                poisoned = false,
            )
        }
        return GameSnapshot(
            gameId = gameId,
            gameStateRevision = gameStateRevision,
            playerInputRevision = playerInputRevision,
            gameSeed = committedSetup.setupSeed,
            rulesetRef = rulesetRef,
            gameState = GameState(committedSetup.script, players, committedSetup.setupSeed),
            actionTimeline = actionTimeline,
            epistemicObservationLog = observationLog,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is SdeHistoricalReplayInput &&
            schemaVersion == other.schemaVersion &&
            gameId == other.gameId &&
            gameStateRevision == other.gameStateRevision &&
            playerInputRevision == other.playerInputRevision &&
            rulesetRef == other.rulesetRef &&
            committedSetup == other.committedSetup &&
            playerNamesBySeat == other.playerNamesBySeat &&
            actionTimeline == other.actionTimeline &&
            observationLog == other.observationLog &&
            nextTimelineGlobalSequence == other.nextTimelineGlobalSequence

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + gameId.hashCode()
        result = 31 * result + gameStateRevision.hashCode()
        result = 31 * result + playerInputRevision.hashCode()
        result = 31 * result + rulesetRef.hashCode()
        result = 31 * result + committedSetup.hashCode()
        result = 31 * result + playerNamesBySeat.hashCode()
        result = 31 * result + actionTimeline.hashCode()
        result = 31 * result + observationLog.hashCode()
        result = 31 * result + nextTimelineGlobalSequence.hashCode()
        return result
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

internal object SdeHistoricalReplayInputFactory {
    fun captureFresh(
        committedSetup: CommittedClocktowerSetup,
        currentSnapshot: GameSnapshot,
    ): SdeHistoricalReplayMaterialization {
        require(currentSnapshot.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "SDE historical replay export requires GLOBAL_V1 canonical history."
        }
        require(currentSnapshot.gameState.script == committedSetup.script) {
            "Current snapshot and committed setup must use the same script."
        }
        require(currentSnapshot.rulesetRef.scriptId == committedSetup.script) {
            "Current ruleset and committed setup must use the same script."
        }
        require(currentSnapshot.gameSeed == committedSetup.setupSeed) {
            "Current snapshot and committed setup must use the same seed."
        }
        val playersBySeat = currentSnapshot.gameState.players.sortedBy(PlayerState::seat)
        require(playersBySeat.map(PlayerState::seat) == committedSetup.assignments.map { it.seat }) {
            "Current snapshot seats must match the committed setup."
        }
        return SdeHistoricalReplayMaterialization(
            input = SdeHistoricalReplayInput(
                gameId = currentSnapshot.gameId,
                gameStateRevision = currentSnapshot.gameStateRevision,
                playerInputRevision = currentSnapshot.playerInputRevision,
                rulesetRef = currentSnapshot.rulesetRef,
                committedSetup = committedSetup,
                playerNamesBySeat = playersBySeat.map(PlayerState::name),
                actionTimeline = currentSnapshot.actionTimeline,
                observationLog = currentSnapshot.epistemicObservationLog,
                nextTimelineGlobalSequence = currentSnapshot.nextTimelineGlobalSequence,
            ),
            origin = SdeHistoricalReplayInputOrigin.FRESH_AUTHORITIES,
        )
    }
}
