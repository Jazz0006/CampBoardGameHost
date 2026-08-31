package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingCommittedSetupAdapterTest {
    @Test
    fun `deal plan becomes exact generic committed setup without reselection`() {
        val plan = TroubleBrewingSetupDealPlan(
            datasetId = "trouble_brewing_setup_presets_v2_final",
            schemaVersion = 2,
            presetId = "tb-5-001",
            playerCount = 2,
            gameSeed = 91L,
            selectedDrunkShownRole = "chef",
            assignments = listOf(
                TroubleBrewingSetupDealAssignment(
                    seat = 1,
                    playerName = "A",
                    actualRoleId = "drunk",
                    shownRoleId = "chef",
                ),
                TroubleBrewingSetupDealAssignment(
                    seat = 2,
                    playerName = "B",
                    actualRoleId = "imp",
                    shownRoleId = "imp",
                ),
            ),
        )

        val committed = TroubleBrewingCommittedSetupAdapter.fromDealPlan(plan)

        assertEquals(ScriptId("trouble_brewing"), committed.script)
        assertEquals(91L, committed.setupSeed)
        assertEquals(listOf(1, 2), committed.assignments.map { it.seat })
        assertEquals(RoleId("drunk"), committed.assignments[0].actualRole)
        assertEquals(RoleId("chef"), committed.assignments[0].shownRole)
        assertEquals(SetupSourceKind.TEMPLATE, committed.provenance.sourceKind)
        assertEquals("trouble_brewing_setup_presets_v2_final", committed.provenance.providerId)
        assertEquals("tb-5-001", committed.provenance.candidateId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `deal plan player count mismatch is rejected before commitment`() {
        TroubleBrewingCommittedSetupAdapter.fromDealPlan(
            TroubleBrewingSetupDealPlan(
                datasetId = "dataset",
                schemaVersion = 2,
                presetId = "preset",
                playerCount = 3,
                gameSeed = 1L,
                selectedDrunkShownRole = null,
                assignments = listOf(
                    TroubleBrewingSetupDealAssignment(1, "A", "chef", "chef"),
                    TroubleBrewingSetupDealAssignment(2, "B", "imp", "imp"),
                ),
            ),
        )
    }
}
