package com.codex.campboardgamehost
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.*
import org.junit.Test

class ClocktowerRavenkeeperSquareTablePresentationTest {
    private fun option(seat: Int, role: String) = ClocktowerDisplayOption(role, ClocktowerDisplayKind.RoleReveal,
        "Ravenkeeper", role, null, null, proposition = InformationProposition.RoleAt(seat, RoleId(role)))
    private fun step() = ClocktowerNightStepUi("Ravenkeeper", PlayerCard("Alice"), true, "", "", null, "",
        action = ClocktowerNightAction.Ravenkeeper, roleEnName = "Ravenkeeper",
        informationQuestion = ClocktowerInformationQuestion.Role(5),
        manualInformationCandidates = listOf(option(5, "Chef"), option(5, "Imp")))
    @Test fun `actual queried seat permits manual and AI roles without true-role anchor`() {
        val step = step()
        assertNull(step.displayProposition)
        val choices = clocktowerRavenkeeperResultChoices(step, 5, 7, false, null)
        assertEquals(setOf("Chef", "Imp"), choices.map { it.roleId!!.value }.toSet())
        assertTrue(choices.all { it.targetSeat == 5 })
        val advised = option(5, "Spy")
        assertEquals("Spy", clocktowerRavenkeeperResultChoices(step, 5, 7, true, advised).single().roleId!!.value)
        assertEquals(advised.proposition, resolveClocktowerPlayerDisplay(step, advised).displayProposition)
    }
    @Test fun `missing different or unknown actual query cannot be replaced by an answer`() {
        val step = step()
        assertTrue(clocktowerRavenkeeperResultChoices(step, null, 7, false, null).isEmpty())
        assertTrue(clocktowerRavenkeeperResultChoices(step, 4, 7, true, option(4, "Spy")).isEmpty())
        assertTrue(clocktowerRavenkeeperResultChoices(step.copy(informationQuestion = null), 5, 7, false, null).isEmpty())
        assertTrue(clocktowerRavenkeeperResultChoices(step, 5, 7, true, option(4, "Spy")).isEmpty())
    }
    @Test fun `typed result rejects missing target and invalid seat`() {
        assertEquals(RoleId("Chef"), clocktowerRavenkeeperTypedResult(InformationProposition.RoleAt(5, RoleId("Chef")), 5, 7)!!.roleId)
        assertNull(clocktowerRavenkeeperTypedResult(InformationProposition.RoleAt(5, RoleId("Chef")), 4, 7))
        assertNull(clocktowerRavenkeeperTypedResult(InformationProposition.RoleAt(5, RoleId("Chef")), null, 7))
        assertNull(clocktowerRavenkeeperTypedResult(InformationProposition.RoleAt(9, RoleId("Chef")), 9, 7))
    }
}
