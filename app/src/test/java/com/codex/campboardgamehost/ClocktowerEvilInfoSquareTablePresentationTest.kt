package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEvilInfoSquareTablePresentationTest {
    @Test
    fun `EvilInfo routes to square table and preserves Demon bluff presentation content`() {
        val step = evilInfoStep(
            tellPlayer = "Show the Demon their Minions and bluffs.",
            secondary = "Bluffs\nMayor · Monk · Undertaker",
        )

        val presentation = clocktowerEvilInfoSquareTablePresentation(step, actorSeat = 7)!!

        assertEquals(7, presentation.actorSeat)
        assertEquals("Wake P7", presentation.wakeInstruction)
        assertEquals("Demon information", presentation.title)
        assertEquals("Minions\nP2 · P5", presentation.primary)
        assertEquals("Bluffs\nMayor · Monk · Undertaker", presentation.secondary)
        assertEquals("Keep this private", presentation.footer)
        assertTrue(presentation.showPlayerDisplayAction)
    }

    @Test
    fun `EvilInfo keeps square table while reveal remains disabled until player text is ready`() {
        val presentation = clocktowerEvilInfoSquareTablePresentation(
            evilInfoStep(tellPlayer = null, secondary = null),
            actorSeat = 7,
        )!!

        assertFalse(presentation.showPlayerDisplayAction)
    }

    @Test
    fun `non-real or non-EvilInfo steps do not claim the EvilInfo square-table owner`() {
        assertNull(
            clocktowerEvilInfoSquareTablePresentation(
                evilInfoStep(tellPlayer = "ready", secondary = null).copy(isRealAction = false),
                actorSeat = 7,
            ),
        )
        assertNull(
            clocktowerEvilInfoSquareTablePresentation(
                evilInfoStep(tellPlayer = "ready", secondary = null).copy(
                    displayKind = ClocktowerDisplayKind.Plain,
                ),
                actorSeat = 7,
            ),
        )
    }

    @Test
    fun `only the acting evil seat is marked as current while the table stays read only`() {
        val actor = clocktowerEvilInfoSeatPresentation(seatNumber = 4, actorSeat = 4)
        val other = clocktowerEvilInfoSeatPresentation(seatNumber = 5, actorSeat = 4)

        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertFalse(other.isCurrentActor)
    }

    private fun evilInfoStep(
        tellPlayer: String?,
        secondary: String?,
    ) = ClocktowerNightStepUi(
        title = "Demon information",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "Wake the Demon",
        tellPlayer = tellPlayer,
        explanation = "",
        displayKind = ClocktowerDisplayKind.EvilInfo,
        displayTitle = "Demon information",
        displayPrimary = "Minions\nP2 · P5",
        displaySecondary = secondary,
        displayFooter = "Keep this private",
        wakeText = "Wake P7",
    )
}
