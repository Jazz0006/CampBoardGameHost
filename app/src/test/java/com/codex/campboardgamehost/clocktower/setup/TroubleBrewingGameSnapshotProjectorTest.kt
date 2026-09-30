package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TroubleBrewingGameSnapshotProjectorTest {
    @Test
    fun `G10 precommit projection preserves known shown roles and keeps Townsfolk actual roles uncommitted`() {
        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            gameId = "evidence:c1d:g10-game2",
            intermediateSetup = g10Game2IntermediateSetup(),
        )

        assertEquals("botc.tb.game-snapshot", snapshot.schemaId)
        assertEquals(1, snapshot.schemaVersion)
        assertEquals("evidence:c1d:g10-game2", snapshot.gameId)
        assertEquals(ScriptId("trouble_brewing"), snapshot.script)
        assertEquals(20_260_929L, snapshot.gameSeed)

        assertEquals(TroubleBrewingSnapshotStage.SETUP_PRECOMMIT, snapshot.position.stage)
        assertSame(SnapshotField.NotApplicable, snapshot.position.phase)
        assertSame(SnapshotField.NotApplicable, snapshot.position.round)

        assertEquals(SnapshotField.Known(true), snapshot.setupState.hasDrunk)
        assertSame(SnapshotField.Uncommitted, snapshot.setupState.drunkAssignmentSeat)

        assertEquals(
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            snapshot.grimoireSeats.map { it.seat },
        )
        assertEquals(
            listOf(
                "empath",
                "imp",
                "undertaker",
                "librarian",
                "spy",
                "monk",
                "mayor",
                "virgin",
                "butler",
            ).map { SnapshotField.Known(it) },
            snapshot.grimoireSeats.map { it.shownRoleId },
        )

        val actualBySeat = snapshot.grimoireSeats.associate { it.seat to it.actualRoleId }
        listOf(1, 3, 4, 6, 7, 8).forEach { seat ->
            assertSame(SnapshotField.Uncommitted, actualBySeat.getValue(seat))
        }
        assertEquals(SnapshotField.Known("imp"), actualBySeat.getValue(2))
        assertEquals(SnapshotField.Known("spy"), actualBySeat.getValue(5))
        assertEquals(SnapshotField.Known("butler"), actualBySeat.getValue(9))

        snapshot.grimoireSeats.forEach { seat ->
            assertEquals(SnapshotField.Known(true), seat.alive)
            assertEquals(SnapshotField.Known(false), seat.poisoned)
        }
    }

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "evidence-c1d-g10",
            schemaVersion = 1,
            presetId = "historical-replay-only:g10-game2",
            playerCount = 9,
            gameSeed = 20_260_929L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf(
                    "empath",
                    "undertaker",
                    "librarian",
                    "monk",
                    "mayor",
                    "virgin",
                ),
                outsiderRoleIds = listOf("butler"),
                minionRoleIds = listOf("spy"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "imp"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "undertaker"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "librarian"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "spy"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "monk"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", "mayor"),
                TroubleBrewingShownSeatAssignment(8, "Seat 8", "virgin"),
                TroubleBrewingShownSeatAssignment(9, "Seat 9", "butler"),
            ),
        )
}
