package com.codex.campboardgamehost
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.*
import org.junit.Test

class ClocktowerUndertakerSquareTablePresentationTest {
    private fun option(seat: Int, role: String) = ClocktowerDisplayOption(role, ClocktowerDisplayKind.RoleReveal,
        "Undertaker", role, null, null, proposition = InformationProposition.RoleAt(seat, RoleId(role)))
    private fun step() = ClocktowerNightStepUi("Undertaker", PlayerCard("Alice"), true, "", "", null, "",
        roleEnName = "Undertaker", informationQuestion = ClocktowerInformationQuestion.Role(4),
        manualInformationCandidates = listOf(option(4, "Chef"), option(4, "Imp")))
    @Test fun `executed subject stays fixed without a Host generated role answer`() {
        val step = step()
        assertNull(step.displayProposition)
        val choices = clocktowerUndertakerResultChoices(step, 7, false, null)
        assertEquals(setOf("Chef", "Imp"), choices.map { it.roleId!!.value }.toSet())
        assertTrue(choices.all { it.executedSeat == 4 })
        val advised = option(4, "Spy")
        assertEquals("Spy", clocktowerUndertakerResultChoices(step, 7, true, advised).single().roleId!!.value)
        assertTrue(clocktowerUndertakerResultChoices(step, 7, true, option(5, "Spy")).isEmpty())
        assertTrue(clocktowerUndertakerResultChoices(step.copy(informationQuestion = null), 7, false, null).isEmpty())
    }
    @Test fun `executed context and current actor are independent presentation dimensions`() {
        val visual = clocktowerUndertakerSeatVisual(4, 1, 4, "en")
        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, visual.state)
        assertFalse(visual.isCurrentActor)
        assertTrue(clocktowerUndertakerSeatVisual(1, 1, 4, "en").isCurrentActor)
    }
    @Test fun `typed role result rejects non-role and unknown target`() {
        assertEquals(4, clocktowerUndertakerTypedResult(InformationProposition.RoleAt(4, RoleId("Chef")), 7)!!.executedSeat)
        assertNull(clocktowerUndertakerTypedResult(InformationProposition.RoleInPlay(RoleId("Chef"), true), 7))
        assertNull(clocktowerUndertakerTypedResult(InformationProposition.RoleAt(9, RoleId("Chef")), 7))
    }
}
