package com.codex.campboardgamehost.clocktower.domain

internal const val TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_ID = "botc.tb.game-snapshot"
internal const val TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_VERSION = 1

/**
 * Four-state semantic value used by the canonical Trouble Brewing read snapshot.
 *
 * UNCOMMITTED is a live-game lifecycle fact: the decision that creates the value has not happened.
 * UNKNOWN is an evidence/reconstruction fact: the value may already exist, but cannot be established
 * from the available prefix. Keeping them distinct prevents historical uncertainty from being
 * mistaken for an unfinished live-game decision.
 */
internal sealed interface SnapshotField<out T> {
    data class Known<T>(val value: T) : SnapshotField<T>

    object Uncommitted : SnapshotField<Nothing>

    object Unknown : SnapshotField<Nothing>

    object NotApplicable : SnapshotField<Nothing>
}

internal enum class TroubleBrewingSnapshotStage {
    SETUP_PRECOMMIT,
    SETUP_COMMITTED,
    RUNTIME,
}

internal data class TroubleBrewingSnapshotPosition(
    val stage: TroubleBrewingSnapshotStage,
    val phase: SnapshotField<StorytellerPhase>,
    val round: SnapshotField<Int>,
    val gameStateRevision: SnapshotField<Long> = SnapshotField.NotApplicable,
    val playerInputRevision: SnapshotField<Long> = SnapshotField.NotApplicable,
) {
    init {
        val knownRound = (round as? SnapshotField.Known<Int>)?.value
        require(knownRound == null || knownRound > 0) {
            "Known Trouble Brewing snapshot round must be positive."
        }
        val knownGameStateRevision = (gameStateRevision as? SnapshotField.Known<Long>)?.value
        require(knownGameStateRevision == null || knownGameStateRevision >= 0L) {
            "Known game-state revision cannot be negative."
        }
        val knownPlayerInputRevision = (playerInputRevision as? SnapshotField.Known<Long>)?.value
        require(knownPlayerInputRevision == null || knownPlayerInputRevision >= 0L) {
            "Known player-input revision cannot be negative."
        }
    }
}

internal data class TroubleBrewingSnapshotSeat(
    val seat: Int,
    val shownRoleId: SnapshotField<String>,
    val actualRoleId: SnapshotField<String>,
    val alive: SnapshotField<Boolean>,
    val poisoned: SnapshotField<Boolean>,
) {
    init {
        require(seat > 0) { "Trouble Brewing snapshot seat numbers start at 1." }
        (shownRoleId as? SnapshotField.Known<String>)?.value?.let { roleId ->
            require(roleId.isNotBlank()) { "Known shown role ID cannot be blank." }
        }
        (actualRoleId as? SnapshotField.Known<String>)?.value?.let { roleId ->
            require(roleId.isNotBlank()) { "Known actual role ID cannot be blank." }
        }
    }
}

internal data class TroubleBrewingSnapshotSetupState(
    val hasDrunk: SnapshotField<Boolean>,
    val drunkAssignmentSeat: SnapshotField<Int>,
) {
    init {
        val knownSeat = (drunkAssignmentSeat as? SnapshotField.Known<Int>)?.value
        require(knownSeat == null || knownSeat > 0) {
            "Known Drunk assignment seat must be positive."
        }

        when ((hasDrunk as? SnapshotField.Known<Boolean>)?.value) {
            false -> require(drunkAssignmentSeat === SnapshotField.NotApplicable) {
                "A known non-Drunk setup must mark Drunk assignment NOT_APPLICABLE."
            }
            true -> require(drunkAssignmentSeat !== SnapshotField.NotApplicable) {
                "A known Drunk setup cannot mark Drunk assignment NOT_APPLICABLE."
            }
            null -> Unit
        }
    }
}

/**
 * Versioned, immutable Trouble Brewing semantic read snapshot.
 *
 * This is a projection over canonical setup/session/history owners, never a mutable state owner.
 * V1 begins deliberately narrow and grows only when a bounded TB consumer requires another field.
 */
internal class TroubleBrewingGameSnapshotV1(
    val gameId: String,
    val gameSeed: Long,
    val position: TroubleBrewingSnapshotPosition,
    grimoireSeats: List<TroubleBrewingSnapshotSeat>,
    val setupState: TroubleBrewingSnapshotSetupState,
) {
    val schemaId: String = TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_ID
    val schemaVersion: Int = TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_VERSION
    val script: ScriptId = ScriptId("trouble_brewing")
    val grimoireSeats: List<TroubleBrewingSnapshotSeat> = grimoireSeats.toList()

    init {
        require(gameId.isNotBlank()) { "Trouble Brewing snapshot gameId cannot be blank." }
        require(this.grimoireSeats.isNotEmpty()) {
            "Trouble Brewing snapshot must contain at least one seat."
        }
        require(this.grimoireSeats.map(TroubleBrewingSnapshotSeat::seat) ==
            (1..this.grimoireSeats.size).toList()) {
            "Trouble Brewing snapshot seats must be ordered canonically from 1 through player count."
        }

        val knownDrunkSeat = (setupState.drunkAssignmentSeat as? SnapshotField.Known<Int>)?.value
        require(knownDrunkSeat == null || knownDrunkSeat in 1..this.grimoireSeats.size) {
            "Known Drunk assignment must reference a snapshot seat."
        }
    }
}
