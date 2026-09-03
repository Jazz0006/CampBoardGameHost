package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerPlayerDisplayPresentationTest {
    @Test
    fun `pair reveal resolves typed seats and player names without parsing display text`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.EitherOne,
            primary = "Chef",
            secondary = "8 / 9",
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Chef")),
                    InformationProposition.RoleAt(5, RoleId("Chef")),
                ),
            ),
        )

        val presentation = requireNotNull(clocktowerPairPlayerRevealPresentation(step, roster()))

        assertEquals(
            listOf(
                ClocktowerPairPlayerRevealSeat(ClocktowerSeatId(2), "Bob"),
                ClocktowerPairPlayerRevealSeat(ClocktowerSeatId(5), "Eve"),
            ),
            presentation.seats,
        )
        assertEquals(ClocktowerDisplayKind.EitherOne, presentation.displayKind)
        assertEquals("information", presentation.title)
        assertEquals("Chef", presentation.primary)
        assertEquals("player-visible footer", presentation.footer)
    }

    @Test
    fun `number and yes no pair reveals use typed subject seats`() {
        val number = requireNotNull(
            clocktowerPairPlayerRevealPresentation(
                displayStep(
                    kind = ClocktowerDisplayKind.Number,
                    primary = "1",
                    secondary = "untrusted 8 / 9",
                    proposition = InformationProposition.NumericResult(
                        metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                        sourceSeat = 1,
                        subjectSeats = listOf(3, 6),
                        value = 1,
                    ),
                    roleEnName = "Chambermaid",
                ),
                roster(),
            ),
        )
        val yesNo = requireNotNull(
            clocktowerPairPlayerRevealPresentation(
                displayStep(
                    kind = ClocktowerDisplayKind.YesNo,
                    primary = "YES",
                    secondary = "untrusted 1 / 9",
                    proposition = InformationProposition.BooleanResult(
                        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat = 2,
                        subjectSeats = listOf(4, 7),
                        value = true,
                    ),
                    roleEnName = "Fortune Teller",
                ),
                roster(),
            ),
        )

        assertEquals(listOf(ClocktowerSeatId(3), ClocktowerSeatId(6)), number.seats.map { it.seatId })
        assertEquals(listOf("Cathy", "Frank"), number.seats.map { it.playerName })
        assertEquals(listOf(ClocktowerSeatId(4), ClocktowerSeatId(7)), yesNo.seats.map { it.seatId })
        assertEquals(listOf("David", "Grace"), yesNo.seats.map { it.playerName })
    }

    @Test
    fun `non pair or malformed typed propositions do not create pair reveal presentation`() {
        val zeroCase = displayStep(
            kind = ClocktowerDisplayKind.EitherOne,
            primary = "No Outsiders",
            secondary = "2 / 5",
            proposition = InformationProposition.AllOf(
                listOf(InformationProposition.RoleInPlay(RoleId("Butler"), false)),
            ),
        )
        val wrongKind = displayStep(
            kind = ClocktowerDisplayKind.Number,
            primary = "1",
            secondary = "2 / 5",
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Chef")),
                    InformationProposition.RoleAt(5, RoleId("Chef")),
                ),
            ),
        )
        val unknownSeat = displayStep(
            kind = ClocktowerDisplayKind.YesNo,
            primary = "YES",
            secondary = null,
            proposition = InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 2,
                subjectSeats = listOf(4, 9),
                value = true,
            ),
            roleEnName = "Fortune Teller",
        )

        assertNull(clocktowerPairPlayerRevealPresentation(zeroCase, roster()))
        assertNull(clocktowerPairPlayerRevealPresentation(wrongKind, roster()))
        assertNull(clocktowerPairPlayerRevealPresentation(unknownSeat, roster()))
    }

    private fun displayStep(
        kind: ClocktowerDisplayKind,
        primary: String,
        secondary: String?,
        proposition: InformationProposition?,
        roleEnName: String? = "Washerwoman",
    ) = ClocktowerNightStepUi(
        title = "information",
        actor = null,
        isRealAction = true,
        reason = "storyteller-only reason",
        storytellerAction = "storyteller-only action",
        tellPlayer = primary,
        explanation = "storyteller-only explanation",
        displayKind = kind,
        displayTitle = "information",
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = "player-visible footer",
        displayProposition = proposition,
        roleEnName = roleEnName,
    )

    private fun roster(): List<PlayerCard> = listOf(
        "Alice",
        "Bob",
        "Cathy",
        "David",
        "Eve",
        "Frank",
        "Grace",
    ).map { name ->
        PlayerCard(
            name = name,
            role = Role.Civilian,
            word = "",
        )
    }
}
