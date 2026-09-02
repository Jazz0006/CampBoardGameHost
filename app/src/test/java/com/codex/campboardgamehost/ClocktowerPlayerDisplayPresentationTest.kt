package com.codex.campboardgamehost

// Durable UI-R3 contract: player-facing seat emphasis comes only from typed display semantics.
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
    fun `number display remains neutral even when proposition contains seat-like data`() {
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
