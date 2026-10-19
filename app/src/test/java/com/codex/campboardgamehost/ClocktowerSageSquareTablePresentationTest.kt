package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSageSquareTablePresentationTest {
    @Test
    fun `Sage reveal uses typed seats not display text`() {
        val cards = (1..5).map { sageTestCard("P$it") }
        val step = sageStep(cards[3])
        val reveal = clocktowerPairPlayerRevealPresentation(step, cards)
        assertEquals(listOf(2, 5), reveal?.seats?.map { it.seatId.number })
        assertEquals(listOf("P2", "P5"), reveal?.seats?.map { it.playerName })
    }

    @Test
    fun `manual Sage seat taps select exactly two actual seats and permit corrections`() {
        assertEquals(listOf(2), clocktowerSageToggledPair(emptyList(), 2, 5))
        assertEquals(listOf(2, 5), clocktowerSageToggledPair(listOf(2), 5, 5))
        assertEquals(listOf(5), clocktowerSageToggledPair(listOf(2, 5), 2, 5))
        assertEquals(listOf(3), clocktowerSageToggledPair(listOf(2, 5), 3, 5))
        assertEquals(listOf(2, 5), clocktowerSageToggledPair(listOf(2, 5), 8, 5))
        assertTrue(clocktowerSageValidPair(listOf(2, 5), 5))
        assertFalse(clocktowerSageValidPair(listOf(2, 2), 5))
        assertFalse(clocktowerSageValidPair(listOf(0, 5), 5))
        assertFalse(clocktowerSageValidPair(listOf(2, 6), 5))
        assertFalse(clocktowerSageValidPair(listOf(2), 5))
    }

    @Test
    fun `Sage actor highlight stays separate from manually chosen pair`() {
        val actor = clocktowerSageSeatPresentation(4, 4, listOf(2, 4))
        val first = clocktowerSageSeatPresentation(2, 4, listOf(2, 4))
        val other = clocktowerSageSeatPresentation(1, 4, listOf(2, 4))
        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedSecond, actor.targetState)
        assertFalse(first.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedFirst, first.targetState)
        assertFalse(other.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Selectable, other.targetState)
    }

    private fun sageStep(actor: PlayerCard) = ClocktowerNightStepUi(
        title = "Sage", actor = actor, isRealAction = true, reason = "",
        storytellerAction = "Wake Sage", tellPlayer = "Demon",
        explanation = "One of two players is the Demon",
        displayKind = ClocktowerDisplayKind.EitherOne, displayTitle = "Sage information",
        displayPrimary = "Demon", displaySecondary = "do-not-parse",
        displayFooter = "One of these two players",
        presentationSubjectSeats = listOf(2, 5), roleEnName = "Sage",
    )

    private fun sageTestCard(name: String) = PlayerCard(
        name = name, clocktowerTeam = ClocktowerTeam.Townsfolk,
        clocktowerRole = ClocktowerRole(ClocktowerTeam.Townsfolk, "贤者", "Sage", "", ""),
    )
}

