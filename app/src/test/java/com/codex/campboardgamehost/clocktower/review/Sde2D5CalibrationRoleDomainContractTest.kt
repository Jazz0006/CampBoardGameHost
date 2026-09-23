package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Sde2D5CalibrationRoleDomainContractTest {
    @Test
    fun `full Trouble Brewing role domain is accepted for D5 policy calibration`() {
        assertEquals(
            Sde2D5CalibrationRoleDomainCompleteness.FULL_SCRIPT_DOMAIN,
            Sde2D5CalibrationRoleDomainContract.requireFullTroubleBrewing(
                TroubleBrewingFixtures.fullRoleDefinitions(),
            ),
        )
    }

    @Test
    fun `bounded exact role domain is rejected for D5 policy calibration`() {
        val bounded = TroubleBrewingFixtures.fullRoleDefinitions()
            .filterNot { definition ->
                definition.id.value in setOf("Baron", "Drunk")
            }

        assertEquals(
            Sde2D5CalibrationRoleDomainCompleteness.BOUNDED_FIXTURE,
            Sde2D5CalibrationRoleDomainContract.classifyTroubleBrewing(bounded),
        )
        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5CalibrationRoleDomainContract.requireFullTroubleBrewing(bounded)
        }
    }
}
