package com.codex.campboardgamehost

// Durable UI-R3/R4B contract: player-facing seat emphasis comes only from typed display semantics.
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPlayerDisplayPresentationTest {
    @Test
    fun `pair display highlights exact seats from typed proposition not display text`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.EitherOne,
            secondary = "8   9",
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Chef")),
                    InformationProposition.RoleAt(5, RoleId("Chef")),
                ),
            ),
        )

        assertEquals(setOf(2, 5), clocktowerPlayerDisplayHighlightedSeats(step))
    }

    @Test
    fun `role reveal highlights the exact typed subject seat`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.RoleReveal,
            secondary = "seat-like text 9",
            proposition = InformationProposition.RoleAt(4, RoleId("Empath")),
            roleEnName = "Ravenkeeper",
        )

        assertEquals(setOf(4), clocktowerPlayerDisplayHighlightedSeats(step))
    }

    @Test
    fun `number result highlights typed subject seats`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.Number,
            secondary = "untrusted text 7 8",
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = listOf(3, 6),
                value = 1,
            ),
            roleEnName = "Chambermaid",
        )

        assertEquals(setOf(3, 6), clocktowerPlayerDisplayHighlightedSeats(step))
    }

    @Test
    fun `yes no result highlights typed subject seats`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.YesNo,
            secondary = "untrusted text 1 9",
            proposition = InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 2,
                subjectSeats = listOf(4, 7),
                value = true,
            ),
            roleEnName = "Fortune Teller",
        )

        assertEquals(setOf(4, 7), clocktowerPlayerDisplayHighlightedSeats(step))
    }

    @Test
    fun `zero pair result keeps the square table neutral`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.EitherOne,
            secondary = "2   5",
            proposition = InformationProposition.AllOf(
                listOf(InformationProposition.RoleInPlay(RoleId("Butler"), false)),
            ),
        )

        assertTrue(clocktowerPlayerDisplayHighlightedSeats(step).isEmpty())
    }

    @Test
    fun `unsupported proposition does not create number highlights from seat-like data`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.Number,
            secondary = "2   5",
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Chef")),
                    InformationProposition.RoleAt(5, RoleId("Chef")),
                ),
            ),
        )

        assertTrue(clocktowerPlayerDisplayHighlightedSeats(step).isEmpty())
    }

    @Test
    fun `evil info without role ability identity is presentation safe and table neutral`() {
        val step = displayStep(
            kind = ClocktowerDisplayKind.EvilInfo,
            secondary = "Minions and bluffs",
            proposition = null,
            roleEnName = null,
        )

        assertTrue(clocktowerPlayerDisplayHighlightedSeats(step).isEmpty())
    }

    private fun displayStep(
        kind: ClocktowerDisplayKind,
        secondary: String?,
        proposition: InformationProposition?,
        roleEnName: String? = "Washerwoman",
    ) = ClocktowerNightStepUi(
        title = "information",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = "shown",
        explanation = "",
        displayKind = kind,
        displayTitle = "information",
        displayPrimary = "shown",
        displaySecondary = secondary,
        displayFooter = "",
        displayProposition = proposition,
        roleEnName = roleEnName,
    )
}
