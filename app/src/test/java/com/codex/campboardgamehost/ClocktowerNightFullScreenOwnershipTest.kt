package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightFullScreenOwnershipTest {
    @Test
    fun `real target actions own the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.FortuneTeller,
                displayKind = ClocktowerDisplayKind.YesNo,
            ),
        )
    }

    @Test
    fun `real information display owns the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.NewDemonIdentity,
                displayKind = ClocktowerDisplayKind.Number,
            ),
        )
    }

    @Test
    fun `real unreliable information step still owns the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.None,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
    }

    @Test
    fun `new demon identity owns full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.NewDemonIdentity,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
    }

    @Test
    fun `new demon identity uses private read only square table in beginner mode`() {
        val presentation = clocktowerEvilInfoSquareTablePresentation(
            step = newDemonIdentityStep(),
            actorSeat = 3,
            wakeInstruction = "Wake player 3",
            beginnerMode = true,
        )

        assertNotNull(presentation)
        assertEquals(3, presentation?.actorSeat)
        assertTrue(presentation?.showPlayerDisplayAction == true)
        assertFalse(presentation?.showHostDetails ?: true)
    }

    @Test
    fun `new demon identity player handoff projects role reveal`() {
        val displayStep = clocktowerPlayerDisplayStep(newDemonIdentityStep())

        assertEquals(ClocktowerDisplayKind.RoleReveal, displayStep.displayKind)
        assertEquals("You are now the Imp", displayStep.displayPrimary)
    }

    @Test
    fun `non real or empty legacy step stays in the regular night shell`() {
        assertFalse(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = false,
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
        assertFalse(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = false,
                action = ClocktowerNightAction.None,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
    }

    private fun newDemonIdentityStep(): ClocktowerNightStepUi = ClocktowerNightStepUi(
        title = "New Demon",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "Wake the new Demon",
        tellPlayer = "You are now the Imp",
        explanation = "",
        action = ClocktowerNightAction.NewDemonIdentity,
        displayKind = ClocktowerDisplayKind.None,
        displayTitle = "New Demon Identity",
    )
}
