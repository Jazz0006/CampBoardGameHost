package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CommittedClocktowerSetupTest {
    @Test
    fun `committed setup snapshots caller assignment list`() {
        val mutableAssignments = mutableListOf(
            seat(1, actual = "Chef", shown = "Chef"),
            seat(2, actual = "Imp", shown = "Imp"),
        )
        val setup = setup(assignments = mutableAssignments)

        mutableAssignments.clear()

        assertEquals(2, setup.playerCount)
        assertEquals(listOf(1, 2), setup.assignments.map(CommittedSetupSeat::seat))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty assignments are rejected`() {
        setup(assignments = emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non canonical seat order is rejected`() {
        setup(
            assignments = listOf(
                seat(2, actual = "Imp", shown = "Imp"),
                seat(1, actual = "Chef", shown = "Chef"),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate seats are rejected by canonical seat invariant`() {
        setup(
            assignments = listOf(
                seat(1, actual = "Chef", shown = "Chef"),
                seat(1, actual = "Imp", shown = "Imp"),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank provider identity is rejected`() {
        SetupProvenance(
            sourceKind = SetupSourceKind.GENERATED,
            providerId = " ",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank candidate identity is rejected when present`() {
        SetupProvenance(
            sourceKind = SetupSourceKind.GENERATED,
            providerId = "ruleset-generator",
            candidateId = "",
        )
    }

    private fun setup(assignments: List<CommittedSetupSeat>): CommittedClocktowerSetup =
        CommittedClocktowerSetup(
            script = ScriptId("trouble_brewing"),
            setupSeed = 7L,
            assignments = assignments,
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "ruleset-generator",
            ),
        )

    private fun seat(seat: Int, actual: String, shown: String): CommittedSetupSeat =
        CommittedSetupSeat(
            seat = seat,
            actualRole = RoleId(actual),
            shownRole = RoleId(shown),
        )
}
