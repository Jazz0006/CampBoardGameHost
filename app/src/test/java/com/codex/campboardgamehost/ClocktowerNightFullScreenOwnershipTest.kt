package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
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
                isRealAction = true,
                action = ClocktowerNightAction.NewDemonIdentity,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
    }
}
