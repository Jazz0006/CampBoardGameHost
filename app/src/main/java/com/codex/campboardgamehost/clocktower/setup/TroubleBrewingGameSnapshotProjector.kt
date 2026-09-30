package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage

/**
 * Pure TBGS-0 projection from the post-seating / pre-Drunk setup authority.
 *
 * When a Drunk is present, every shown Townsfolk remains a legal possible Drunk at this boundary,
 * so its actual role is UNCOMMITTED. Non-Townsfolk actual identities are already fixed and remain
 * KNOWN. No later setup or first-night decisions are reconstructed here.
 */
internal object TroubleBrewingGameSnapshotProjector {
    fun fromCommitted(
        gameId: String,
        committedSetup: CommittedClocktowerSetup,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingGameSnapshotV1 {
        require(committedSetup.script == ScriptId("trouble_brewing")) {
            "Trouble Brewing snapshot projector only accepts Trouble Brewing committed setup."
        }

        val drunkSeats = committedSetup.assignments.filter { assignment ->
            externalRoleId(assignment.actualRole, characterRegistry) == "drunk"
        }
        require(drunkSeats.size <= 1) {
            "Trouble Brewing committed setup cannot contain more than one Drunk."
        }

        return TroubleBrewingGameSnapshotV1(
            gameId = gameId,
            gameSeed = committedSetup.setupSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.SETUP_COMMITTED,
                phase = SnapshotField.NotApplicable,
                round = SnapshotField.NotApplicable,
            ),
            grimoireSeats = committedSetup.assignments.map { assignment ->
                TroubleBrewingSnapshotSeat(
                    seat = assignment.seat,
                    shownRoleId = SnapshotField.Known(
                        externalRoleId(assignment.shownRole, characterRegistry),
                    ),
                    actualRoleId = SnapshotField.Known(
                        externalRoleId(assignment.actualRole, characterRegistry),
                    ),
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(drunkSeats.isNotEmpty()),
                drunkAssignmentSeat = drunkSeats.singleOrNull()?.let { assignment ->
                    SnapshotField.Known(assignment.seat)
                } ?: SnapshotField.NotApplicable,
            ),
        )
    }

    fun fromRuntime(
        gameSnapshot: GameSnapshot,
        phase: StorytellerPhase,
        round: Int,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingGameSnapshotV1 {
        require(gameSnapshot.gameState.script == ScriptId("trouble_brewing")) {
            "Trouble Brewing snapshot projector only accepts Trouble Brewing runtime state."
        }
        require(round > 0) { "Trouble Brewing runtime round must be positive." }

        val players = gameSnapshot.gameState.players.sortedBy { player -> player.seat }
        val drunkPlayers = players.filter { player ->
            externalRoleId(player.actualRole, characterRegistry) == "drunk"
        }
        require(drunkPlayers.size <= 1) {
            "Trouble Brewing runtime state cannot contain more than one Drunk."
        }

        return TroubleBrewingGameSnapshotV1(
            gameId = gameSnapshot.gameId,
            gameSeed = gameSnapshot.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(phase),
                round = SnapshotField.Known(round),
                gameStateRevision = SnapshotField.Known(gameSnapshot.gameStateRevision),
                playerInputRevision = SnapshotField.Known(gameSnapshot.playerInputRevision),
            ),
            grimoireSeats = players.map { player ->
                val shownRole = player.shownRole ?: player.actualRole
                TroubleBrewingSnapshotSeat(
                    seat = player.seat,
                    shownRoleId = SnapshotField.Known(
                        externalRoleId(shownRole, characterRegistry),
                    ),
                    actualRoleId = SnapshotField.Known(
                        externalRoleId(player.actualRole, characterRegistry),
                    ),
                    alive = SnapshotField.Known(player.alive),
                    poisoned = SnapshotField.Known(player.poisoned),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(drunkPlayers.isNotEmpty()),
                drunkAssignmentSeat = drunkPlayers.singleOrNull()?.let { player ->
                    SnapshotField.Known(player.seat)
                } ?: SnapshotField.NotApplicable,
            ),
        )
    }

    private fun externalRoleId(
        roleId: RoleId,
        characterRegistry: ClocktowerCharacterRegistry,
    ): String = requireNotNull(characterRegistry.findByRoleId(roleId)) {
        "Trouble Brewing snapshot requires every committed RoleId to exist in the character registry."
    }.externalId

    fun fromIntermediate(
        gameId: String,
        intermediateSetup: TroubleBrewingIntermediateSetup,
    ): TroubleBrewingGameSnapshotV1 {
        val hasDrunk = intermediateSetup.visibleRoster.hasDrunk
        val townsfolkRoleIds = intermediateSetup.visibleRoster.townsfolkRoleIds.toSet()

        return TroubleBrewingGameSnapshotV1(
            gameId = gameId,
            gameSeed = intermediateSetup.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                phase = SnapshotField.NotApplicable,
                round = SnapshotField.NotApplicable,
            ),
            grimoireSeats = intermediateSetup.shownSeatAssignments.map { assignment ->
                val actualRole =
                    if (hasDrunk && assignment.shownRoleId in townsfolkRoleIds) {
                        SnapshotField.Uncommitted
                    } else {
                        SnapshotField.Known(assignment.shownRoleId)
                    }

                TroubleBrewingSnapshotSeat(
                    seat = assignment.seat,
                    shownRoleId = SnapshotField.Known(assignment.shownRoleId),
                    actualRoleId = actualRole,
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(hasDrunk),
                drunkAssignmentSeat =
                    if (hasDrunk) SnapshotField.Uncommitted else SnapshotField.NotApplicable,
            ),
        )
    }
}
