package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protect the only real-player handoff after removing the old Spy-specific screen. */
class ClocktowerGrimoireRevealGateTest {
    private val spy = ClocktowerNightStepUi(
        title = "Spy",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "Wake the Spy",
        tellPlayer = null,
        explanation = "",
        roleEnName = "Spy",
        displayKind = ClocktowerDisplayKind.Grimoire,
    )

    @Test fun poisonedSpyCannotOpenRealGrimoireFromOrdinaryInformationHandoff() {
        val presented = requireNotNull(clocktowerPlainInformationSquareTablePresentation(spy, 1, "Wake"))
        assertFalse(presented.showPlayerDisplayAction)
        assertFalse(requireNotNull(clocktowerPlainInformationSquareTablePresentation(
            spy.copy(tellPlayer = "View the Grimoire"), 1, "Wake",
        )).showPlayerDisplayAction)
    }

    @Test fun confirmedHealthySpyCanOpenSharedTable() {
        val actualGrimoire = spy.copy(
            tellPlayer = "View the Grimoire",
            displayProposition = InformationProposition.GrimoireState(
                listOf(GrimoireSeatView(1, RoleId("Spy"), true)),
            ),
        )
        assertTrue(requireNotNull(clocktowerPlainInformationSquareTablePresentation(
            actualGrimoire, 1, "Wake",
        )).showPlayerDisplayAction)
    }
}
