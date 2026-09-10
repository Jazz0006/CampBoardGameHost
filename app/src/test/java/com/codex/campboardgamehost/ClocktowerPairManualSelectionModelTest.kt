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
        val model = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(candidates))

        assertEquals(listOf("Chef", "Empath"), model.roleIds)
        assertEquals(setOf(1, 4, 7), model.firstSeats("Chef").toSet())
        assertEquals(setOf(2, 6), model.firstSeats("Empath").toSet())
    }

    @Test
    fun `recommended pair seeds manual editing without reconstructing localized display text`() {
        val recommended = option("Chef", 1, 7, label = "localized label is presentation only")
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(option("Chef", 1, 4), recommended, option("Empath", 2, 6)),
        )

        val model = ClocktowerPairManualSelectionModel.from(
            presentation = presentation,
            initialOption = recommended,
        )

        assertEquals("Chef", model.selectedRoleId)
        assertEquals(1, model.selectedFirstSeat)
        assertEquals(7, model.selectedSecondSeat)
        assertEquals(recommended, model.resolvedOption)
    }

    @Test
    fun `manual seed fails closed when recommendation is outside supplied legal presentation`() {
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(option("Chef", 1, 4)),
        )

        val model = ClocktowerPairManualSelectionModel.from(
            presentation = presentation,
            initialOption = option("Chef", 2, 8),
        )

        assertNull(model.selectedRoleId)
        assertNull(model.selectedFirstSeat)
        assertNull(model.selectedSecondSeat)
        assertNull(model.resolvedOption)
    }

    @Test
    fun `first seat constrains legal second seats and changing it cannot retain stale second seat`() {
        val model = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(
            listOf(option("Chef", 1, 4), option("Chef", 1, 7), option("Chef", 2, 8)),
        ))

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
        val state = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(listOf(wrong, expected)))
            .selectRole("Chef")
            .selectSeat(7)
            .selectSeat(1)

        assertEquals(expected, state.resolvedOption)
    }

    @Test
    fun `zero case is exposed only when supplied by legal candidates`() {
        val withZero = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(listOf(option("Chef", 1, 4), zeroOption())))
        val withoutZero = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(listOf(option("Minion", 1, 4))))

        assertTrue(withZero.hasZeroCase)
        assertEquals(zeroOption(), withZero.selectZeroCase().resolvedOption)
        assertTrue(!withoutZero.hasZeroCase)
    }

    @Test
    fun `malformed candidates are ignored and first duplicate wins`() {
        val first = option("Chef", 7, 1, "first")
        val duplicate = option("Chef", 1, 7, "second")
        val mixed = first.copy(proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(1, RoleId("Chef")),
            InformationProposition.RoleAt(7, RoleId("Empath")),
        )))
        val repeated = option("Chef", 1, 1)
        val mixedZero = zeroOption().copy(proposition = InformationProposition.AllOf(listOf(
            InformationProposition.RoleInPlay(RoleId("Outsider"), false),
            InformationProposition.RoleAt(1, RoleId("Chef")),
        )))
        val presentRole = zeroOption().copy(proposition = InformationProposition.AllOf(listOf(
            InformationProposition.RoleInPlay(RoleId("Outsider"), true),
        )))
        val malformed = listOf(mixed, repeated, mixedZero, presentRole)
        val empty = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(malformed))
        assertTrue(empty.roleIds.isEmpty())
        assertTrue(!empty.hasZeroCase)
        val model = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(malformed + first + duplicate))
        assertEquals(first, model.selectRole("Chef").selectSeat(1).selectSeat(7).resolvedOption)
    }

    @Test
    fun `first zero option wins and switching roles clears the old pair`() {
        val zero = zeroOption()
        val model = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(listOf(
            option("Chef", 1, 4), option("Empath", 2, 6), zero, zero.copy(label = "second zero"),
        )))
        assertEquals(zero, model.selectZeroCase().resolvedOption)
        val changed = model.selectRole("Chef").selectSeat(1).selectSeat(4).selectRole("Empath")
        assertNull(changed.selectedFirstSeat)
        assertNull(changed.selectedSecondSeat)
        assertNull(changed.resolvedOption)
    }

    @Test
    fun `presentation equality preserves candidate change reset identity`() {
        val option = option("Chef", 1, 4)
        val first = ClocktowerPairManualAuthority.selectionPresentation(listOf(option))
        val equalCopy = ClocktowerPairManualAuthority.selectionPresentation(listOf(option.copy()))
        val ignored = option.copy(proposition = null)
        val changed = ClocktowerPairManualAuthority.selectionPresentation(listOf(option, ignored))
        assertEquals(first, equalCopy)
        assertTrue(first != changed)
        assertEquals(first.candidates, changed.candidates)
        val relabeled = ClocktowerPairManualAuthority.selectionPresentation(listOf(option.copy(label = "new")))
        assertTrue(first != relabeled)
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
