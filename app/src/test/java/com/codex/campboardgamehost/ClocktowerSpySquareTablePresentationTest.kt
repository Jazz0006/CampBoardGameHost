package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSpySquareTablePresentationTest {
    @Test
    fun `healthy Spy keeps the existing Grimoire reveal action`() {
        val presentation = clocktowerSpySquareTablePresentation(
            step = spyStep(
                tellPlayer = "P1: Spy\nP2: Chef",
                displayProposition = InformationProposition.GrimoireState(
                    listOf(
                        GrimoireSeatView(1, RoleId("Spy"), true),
                        GrimoireSeatView(2, RoleId("Chef"), true),
                    ),
                ),
            ),
        )

        assertEquals(ClocktowerDisplayKind.Grimoire, presentation?.displayKind)
        assertTrue(presentation?.showLegacyRevealAction == true)
    }

    @Test
    fun `poisoned Spy keeps the step but never exposes a true Grimoire action`() {
        val presentation = clocktowerSpySquareTablePresentation(
            step = spyStep(
                tellPlayer = null,
                displayProposition = null,
            ),
        )

        assertEquals(ClocktowerDisplayKind.Grimoire, presentation?.displayKind)
        assertFalse(presentation?.showLegacyRevealAction ?: true)
    }

    @Test
    fun `Spy shell fails closed for missing actor wrong role or wrong display kind`() {
        assertNull(clocktowerSpySquareTablePresentation(spyStep(actor = null)))
        assertNull(clocktowerSpySquareTablePresentation(spyStep(roleEnName = "Empath")))
        assertNull(clocktowerSpySquareTablePresentation(spyStep(displayKind = ClocktowerDisplayKind.RoleReveal)))
    }

    @Test
    fun `Spy actor highlight is independent from read-only table seat state`() {
        val actor = clocktowerSpySeatPresentation(seatNumber = 3, actorSeat = 3)
        val other = clocktowerSpySeatPresentation(seatNumber = 2, actorSeat = 3)

        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertFalse(other.isCurrentActor)
    }

    private fun spyStep(
        actor: PlayerCard? = PlayerCard(name = "S", clocktowerRole = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).first { it.enName == "Spy" }),
        roleEnName: String = "Spy",
        tellPlayer: String? = "P1: Spy",
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.Grimoire,
        displayProposition: InformationProposition? = InformationProposition.GrimoireState(
            listOf(GrimoireSeatView(1, RoleId("Spy"), true)),
        ),
    ) = ClocktowerNightStepUi(
        title = "Spy",
        actor = actor,
        isRealAction = true,
        reason = "",
        storytellerAction = "Wake Spy",
        tellPlayer = tellPlayer,
        explanation = "",
        roleEnName = roleEnName,
        displayKind = displayKind,
        displayTitle = "Grimoire",
        displayProposition = displayProposition,
    )
}
