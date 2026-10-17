package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The player's displayed value, never the correct game's value, is the recorded evidence. */
class ClocktowerShownUnreliablePropositionTest {
    private val seats = (1..8).toList()

    @Test
    fun `untyped poisoned chef zero derives only the literal shown numeric result`() {
        val step = step("Chef", "0", ClocktowerDisplayKind.Number)
        assertEquals(
            InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, seats, 0,
            ),
            recordable(step),
        )
    }

    @Test
    fun `unreliable empath value captures the shown neighbors and number`() {
        val step = step("Empath", "2", ClocktowerDisplayKind.Number)
        assertEquals(
            InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS, 1, listOf(8, 2), 2,
            ),
            recordable(step),
        )
    }

    @Test
    fun `inconsistent typed number cannot silently record a true answer instead of shown lie`() {
        val misleading = step("Chef", "0", ClocktowerDisplayKind.Number).copy(
            displayProposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, seats, 1,
            ),
        )
        assertNull(recordable(misleading))
    }

    @Test
    fun `untyped pair and role claims cannot be inferred from localized text`() {
        assertNull(recordable(step("Investigator", "Baron", ClocktowerDisplayKind.EitherOne)))
        assertNull(recordable(step("Undertaker", "Imp", ClocktowerDisplayKind.RoleReveal)))
        assertNull(recordable(step("Chef", "?", ClocktowerDisplayKind.Number)))
        val typed = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(4, RoleId("Baron")),
            InformationProposition.RoleAt(7, RoleId("Baron")),
        ))
        assertEquals(
            typed,
            recordable(step("Investigator", "Baron", ClocktowerDisplayKind.EitherOne).copy(
                displayProposition = typed,
            )),
        )
    }

    @Test
    fun `poisoned fortune teller records the actual yes or no that was shown`() {
        val actualNo = step("Fortune Teller", "没有", ClocktowerDisplayKind.YesNo)
        assertEquals(
            InformationProposition.BooleanResult(
                com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                1, listOf(3, 7), false,
            ),
            recordable(actualNo),
        )
        val truthfulYesButNoWasShown = actualNo.copy(
            displayProposition = InformationProposition.BooleanResult(
                com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                1, listOf(3, 7), true,
            ),
        )
        assertNull(recordable(truthfulYesButNoWasShown))
        assertEquals(
            InformationProposition.BooleanResult(
                com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                1, listOf(3, 7), false,
            ),
            recordable(actualNo.copy(displayPrimary = "NO DEMON")),
        )
    }

    @Test
    fun `chambermaid requires the two actual queried subjects`() {
        val raw = step("Chambermaid", "1", ClocktowerDisplayKind.Number)
        assertNull(recordable(raw))
        assertEquals(
            InformationProposition.NumericResult(
                NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 1, listOf(3, 4), 1,
            ),
            recordable(raw.copy(presentationSubjectSeats = listOf(3, 4))),
        )
    }

    private fun step(role: String, primary: String, kind: ClocktowerDisplayKind) = ClocktowerNightStepUi(
        title = role,
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = primary,
        explanation = "",
        displayKind = kind,
        displayPrimary = primary,
        roleEnName = role,
    )

    private fun recordable(step: ClocktowerNightStepUi) = clocktowerShownUnreliableProposition(
        step = step,
        actorSeat = 1,
        allSeats = seats,
        empathNeighborSeats = listOf(8, 2),
        fortuneTellerSubjectSeats = listOf(3, 7),
    )
}
