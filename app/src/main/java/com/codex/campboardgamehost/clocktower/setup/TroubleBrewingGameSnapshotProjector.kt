package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
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
