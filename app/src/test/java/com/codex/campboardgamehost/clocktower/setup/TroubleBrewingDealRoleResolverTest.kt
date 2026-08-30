package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktowerRolesForScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingDealRoleResolverTest {
    @Test
    fun `external preset role ids resolve exactly to existing app roles without changing shown identity`() {
        val dealPlan = TroubleBrewingSetupDealPlan(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "tb-8-role-resolution",
            playerCount = 3,
            gameSeed = 6_003L,
            selectedDrunkShownRole = "investigator",
            assignments = listOf(
                TroubleBrewingSetupDealAssignment(1, "A", "drunk", "investigator"),
                TroubleBrewingSetupDealAssignment(2, "B", "fortune_teller", "fortune_teller"),
                TroubleBrewingSetupDealAssignment(3, "C", "scarlet_woman", "scarlet_woman"),
            ),
        )

        val resolved = TroubleBrewingDealRoleResolver.resolve(
            dealPlan = dealPlan,
            availableRoles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing),
        )

        assertEquals(listOf(1, 2, 3), resolved.map { it.seat })
        assertEquals(listOf("A", "B", "C"), resolved.map { it.playerName })
        assertEquals(listOf("Drunk", "Fortune Teller", "Scarlet Woman"), resolved.map { it.actualRole.enName })
        assertEquals(listOf("Investigator", "Fortune Teller", "Scarlet Woman"), resolved.map { it.shownRole.enName })
    }

    @Test
    fun `unknown or ambiguous app role identity is rejected rather than substituted`() {
        val dealPlan = TroubleBrewingSetupDealPlan(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "tb-role-resolution-invalid",
            playerCount = 1,
            gameSeed = 6_004L,
            selectedDrunkShownRole = null,
            assignments = listOf(
                TroubleBrewingSetupDealAssignment(1, "A", "not_a_role", "not_a_role"),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingDealRoleResolver.resolve(
                dealPlan = dealPlan,
                availableRoles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing),
            )
        }
    }
}
