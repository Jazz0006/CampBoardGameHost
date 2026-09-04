package com.codex.campboardgamehost

// Durable UI-R2 contract: selection continuity is derived only from supplied legal candidates.
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPairManualSelectionModelTest {
    @Test
    fun `role and first seat choices come only from supplied legal candidates`() {
        val candidates = listOf(
            option("Chef", 1, 4),
            option("Chef", 1, 7),
            option("Empath", 2, 6),
        )
        val model = clocktowerPairManualSelectionModel(candidates)

        assertEquals(listOf("Chef", "Empath"), model.roleIds)
        assertEquals(setOf(1, 4, 7), model.firstSeats("Chef").toSet())
        assertEquals(setOf(2, 6), model.firstSeats("Empath").toSet())
    }

    @Test
    fun `first seat constrains legal second seats and changing it cannot retain stale second seat`() {
        val model = clocktowerPairManualSelectionModel(
            listOf(option("Chef", 1, 4), option("Chef", 1, 7), option("Chef", 2, 8)),
        )

        val first = model.selectRole("Chef").selectSeat(1).selectSeat(7)
        assertEquals(1, first.selectedFirstSeat)
        assertEquals(7, first.selectedSecondSeat)
        assertEquals(setOf(4, 7), model.secondSeats("Chef", 1).toSet())

        val corrected = first.selectSeat(2)
        assertEquals(2, corrected.selectedFirstSeat)
        assertNull(corrected.selectedSecondSeat)
        assertEquals(setOf(8), model.secondSeats("Chef", 2).toSet())
    }

    @Test
    fun `two selected seats resolve the exact typed candidate independent of display label`() {
        val expected = option("Chef", 1, 7, label = "same label")
        val wrong = option("Chef", 1, 4, label = "same label")
        val state = clocktowerPairManualSelectionModel(listOf(wrong, expected))
            .selectRole("Chef")
            .selectSeat(7)
            .selectSeat(1)

        assertEquals(expected, state.resolvedOption)
    }

    @Test
    fun `zero case is exposed only when supplied by legal candidates`() {
        val withZero = clocktowerPairManualSelectionModel(listOf(option("Chef", 1, 4), zeroOption()))
        val withoutZero = clocktowerPairManualSelectionModel(listOf(option("Minion", 1, 4)))

        assertTrue(withZero.hasZeroCase)
        assertEquals(zeroOption(), withZero.selectZeroCase().resolvedOption)
        assertTrue(!withoutZero.hasZeroCase)
    }

    private fun option(role: String, first: Int, second: Int, label: String = "$role $first/$second") =
        ClocktowerDisplayOption(
            label = label,
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "info",
            displayPrimary = role,
            displaySecondary = null,
            displayFooter = null,
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(first, RoleId(role)),
                    InformationProposition.RoleAt(second, RoleId(role)),
                ),
            ),
        )

    private fun zeroOption() = ClocktowerDisplayOption(
        label = "none",
        displayKind = ClocktowerDisplayKind.Plain,
        displayTitle = "info",
        displayPrimary = "0",
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.AllOf(
            listOf(InformationProposition.RoleInPlay(RoleId("Outsider"), false)),
        ),
    )
}
