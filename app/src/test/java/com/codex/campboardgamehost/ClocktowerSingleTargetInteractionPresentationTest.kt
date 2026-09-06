package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerSingleTargetInteractionPresentationTest {
    private val selection = ClocktowerSingleTargetSelection(2, setOf(1, 3, 7), true)

    @Test
    fun `red herring omits non-real steps and never adds an actor cue`() {
        assertNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.RedHerring, selection.copy(enabled = false), 2, "wake", true))
        val model = requireNotNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.RedHerring, selection, 2, "wake", true))
        assertNull(model.actorSeat)
        assertNull(model.wakeInstruction)
        assertFalse(model.canShowResult)
        assertEquals(selection, model.selection)
    }

    @Test
    fun `ordinary actions preserve supplied selection and actor independently`() {
        for (action in listOf(ClocktowerNightAction.Poison, ClocktowerNightAction.ButlerMaster,
            ClocktowerNightAction.MonkProtect, ClocktowerNightAction.DemonKill)) {
            val input = selection.copy(enabled = false)
            val model = requireNotNull(clocktowerSingleTargetAbilityPresentation(action, input, 2, "wake", true))
            assertEquals(input, model.selection)
            assertEquals(2, model.actorSeat)
            assertEquals("wake", model.wakeInstruction)
            assertFalse(model.canShowResult)
        }
    }

    @Test
    fun `only Ravenkeeper exposes the supplied result permission`() {
        val allowed = requireNotNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.Ravenkeeper, selection, 4, "wake", true))
        val blocked = requireNotNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.Ravenkeeper, selection, 4, "wake", false))
        assertTrue(allowed.canShowResult)
        assertFalse(blocked.canShowResult)
        assertEquals(allowed.copy(canShowResult = false), blocked)
        assertNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.FortuneTeller, selection, 4, "wake", true))
    }

    @Test
    fun `automatic rulings do not show a manual target dialog`() {
        for (action in listOf(ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.DemonSuccessor)) {
            assertNull(clocktowerNightRulingPresentation(action, selection, true, 2, "explanation"))
        }
    }

    @Test
    fun `Mayor absence disables targets while retaining current selection`() {
        val missing = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.MayorRedirect, selection, false, null, ""))
        assertFalse(missing.selection.enabled)
        assertTrue(missing.selection.selectableSeats.isEmpty())
        assertEquals(selection.selectedSeat, missing.selection.selectedSeat)
        assertNull(missing.mayorSeat)
        val present = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.MayorRedirect, selection, false, 2, ""))
        assertEquals(selection, present.selection)
        assertEquals(2, present.mayorSeat)
    }

    @Test
    fun `successor retains supplied eligibility and explanation without Mayor action`() {
        val model = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.DemonSuccessor, selection, false, 2, "Choose eligible minion"))
        assertEquals(selection, model.selection)
        assertEquals("Choose eligible minion", model.explanation)
        assertNull(model.mayorSeat)
        assertNull(clocktowerNightRulingPresentation(ClocktowerNightAction.Poison, selection, false, null, ""))
    }
}
