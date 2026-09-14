package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerSingleTargetInteractionPresentationTest {
    private val selection = ClocktowerSingleTargetSelection(2, setOf(1, 3, 7), true)

    @Test
    fun `confirmation requires an enabled interaction and a selected candidate`() {
        assertFalse(
            clocktowerSingleTargetConfirmationEnabled(
                ClocktowerSingleTargetSelection(null, setOf(1, 3, 7), true),
            ),
        )
        assertTrue(
            clocktowerSingleTargetConfirmationEnabled(
                ClocktowerSingleTargetSelection(3, setOf(1, 3, 7), true),
            ),
        )
        assertFalse(
            clocktowerSingleTargetConfirmationEnabled(
                ClocktowerSingleTargetSelection(2, setOf(1, 3, 7), true),
            ),
        )
        assertFalse(
            clocktowerSingleTargetConfirmationEnabled(
                ClocktowerSingleTargetSelection(3, setOf(1, 3, 7), false),
            ),
        )
    }

    @Test
    fun `confirmation accepts an explicitly supplied alternate legal seat`() {
        val mayorOutcome = ClocktowerSingleTargetSelection(2, setOf(1, 3, 7), true)

        assertFalse(clocktowerSingleTargetConfirmationEnabled(mayorOutcome))
        assertTrue(
            clocktowerSingleTargetConfirmationEnabled(
                selection = mayorOutcome,
                additionalSelectableSeats = setOf(2),
            ),
        )
    }

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
            assertFalse(model.confirmationEnabled)
        }
    }

    @Test
    fun `only Ravenkeeper exposes the supplied result permission`() {
        val allowed = requireNotNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.Ravenkeeper, selection, 4, "wake", true))
        val blocked = requireNotNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.Ravenkeeper, selection, 4, "wake", false))
        assertTrue(allowed.canShowResult)
        assertFalse(allowed.confirmationEnabled)
        assertFalse(blocked.canShowResult)
        assertEquals(allowed.copy(canShowResult = false), blocked)
        assertNull(clocktowerSingleTargetAbilityPresentation(ClocktowerNightAction.FortuneTeller, selection, 4, "wake", true))
    }

    @Test
    fun `automatic rulings keep a concrete read only surface while effects settle`() {
        for (action in listOf(ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.DemonSuccessor)) {
            val presentation = requireNotNull(
                clocktowerNightRulingPresentation(action, selection, true, 2, "explanation"),
            )
            assertTrue(presentation.automatic)
            assertFalse(presentation.selection.enabled)
            assertTrue(presentation.selection.selectableSeats.isEmpty())
            assertFalse(presentation.confirmationEnabled)
        }
    }

    @Test
    fun `Mayor absence disables targets while retaining current selection`() {
        val missing = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.MayorRedirect, selection, false, null, ""))
        assertFalse(missing.selection.enabled)
        assertTrue(missing.selection.selectableSeats.isEmpty())
        assertEquals(selection.selectedSeat, missing.selection.selectedSeat)
        assertNull(missing.mayorSeat)
        assertFalse(missing.confirmationEnabled)
        val present = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.MayorRedirect, selection, false, 2, ""))
        assertEquals(selection, present.selection)
        assertEquals(2, present.mayorSeat)
        assertTrue(present.confirmationEnabled)
    }

    @Test
    fun `successor retains supplied eligibility and explanation without Mayor action`() {
        val model = requireNotNull(clocktowerNightRulingPresentation(ClocktowerNightAction.DemonSuccessor, selection, false, 2, "Choose eligible minion"))
        assertEquals(selection, model.selection)
        assertEquals("Choose eligible minion", model.explanation)
        assertNull(model.mayorSeat)
        assertFalse(model.confirmationEnabled)
        assertNull(clocktowerNightRulingPresentation(ClocktowerNightAction.Poison, selection, false, null, ""))
    }
}
