package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerUndertakerSquareTablePresentationTest {
    @Test
    fun `executed seat and shown role come only from typed role-at proposition`() {
        val result = clocktowerUndertakerTypedResult(
            proposition = InformationProposition.RoleAt(4, RoleId("Chef")),
            seatCount = 7,
        )

        assertEquals(ClocktowerUndertakerTypedResult(4, RoleId("Chef")), result)
    }

    @Test
    fun `non role-at or invalid seat fails closed`() {
        assertNull(
            clocktowerUndertakerTypedResult(
                proposition = InformationProposition.RoleInPlay(RoleId("Chef"), true),
                seatCount = 7,
            ),
        )
        assertNull(
            clocktowerUndertakerTypedResult(
                proposition = InformationProposition.RoleAt(8, RoleId("Chef")),
                seatCount = 7,
            ),
        )
    }

    @Test
    fun `manual final results keep one fixed executed seat while role may vary`() {
        val chef = option(seat = 4, role = "Chef", recommended = true)
        val spy = option(seat = 4, role = "Spy")

        val choices = clocktowerUndertakerResultChoices(
            step = undertakerStep(),
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = listOf(chef, spy),
        )

        assertEquals(listOf(4, 4), choices.map { it.executedSeat })
        assertEquals(listOf("Chef", "Spy"), choices.map { it.roleId?.value })
        assertEquals(listOf("Chef", "Spy"), choices.map { it.displayLabel })
        assertEquals(
            listOf(
                ClocktowerUndertakerResultSourceKind.DisplayOption,
                ClocktowerUndertakerResultSourceKind.DisplayOption,
            ),
            choices.map { it.sourceKind },
        )
        assertTrue(choices.first().recommended)
    }

    @Test
    fun `conflicting candidate seats fail closed instead of inventing execution context`() {
        val choices = clocktowerUndertakerResultChoices(
            step = undertakerStep(),
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = listOf(
                option(seat = 4, role = "Chef"),
                option(seat = 5, role = "Spy"),
            ),
        )

        assertTrue(choices.isEmpty())
    }

    @Test
    fun `unreliable role choices stay opaque while execution context remains typed`() {
        val choices = clocktowerUndertakerResultChoices(
            step = undertakerStep(
                displayOptions = listOf(
                    option(seat = 4, role = "Chef", typed = false),
                    option(seat = 4, role = "Imp", typed = false),
                ),
            ),
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
        )

        assertEquals(setOf(4), choices.map { it.executedSeat }.toSet())
        assertTrue(choices.all { it.roleId == null })
        assertEquals(listOf("Chef", "Imp"), choices.map { it.displayLabel })
        assertEquals(
            listOf(
                ClocktowerUndertakerResultSourceKind.LegacyUnreliable,
                ClocktowerUndertakerResultSourceKind.LegacyUnreliable,
            ),
            choices.map { it.sourceKind },
        )
    }

    @Test
    fun `opaque unreliable option never becomes a player proposition`() {
        val selected = option(seat = 4, role = "Imp", typed = false)
        val resolved = resolveClocktowerLegacyUnreliablePlayerDisplay(
            undertakerStep(displayOptions = listOf(selected)),
            selected,
        )

        assertEquals("Imp", resolved.displayPrimary)
        assertNull(resolved.displayProposition)
    }

    @Test
    fun `executed context and current actor are independent visual dimensions`() {
        val executed = clocktowerUndertakerSeatVisual(
            seatNumber = 4,
            actorSeat = 2,
            executedSeat = 4,
            language = "zh",
        )
        val actor = clocktowerUndertakerSeatVisual(
            seatNumber = 2,
            actorSeat = 2,
            executedSeat = 4,
            language = "zh",
        )

        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, executed.state)
        assertEquals("处", executed.badge)
        assertFalse(executed.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.state)
        assertTrue(actor.isCurrentActor)
    }

    private fun undertakerStep(
        displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    ) = ClocktowerNightStepUi(
        title = "Undertaker",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = "P4 was Chef",
        explanation = "",
        roleEnName = "Undertaker",
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayPrimary = "Chef",
        displayProposition = InformationProposition.RoleAt(4, RoleId("Chef")),
        displayOptions = displayOptions,
    )

    private fun option(
        seat: Int,
        role: String,
        recommended: Boolean = false,
        typed: Boolean = true,
    ) = ClocktowerDisplayOption(
        label = role,
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayTitle = "Undertaker information",
        displayPrimary = role,
        displaySecondary = null,
        displayFooter = "Executed today: P$seat",
        proposition = if (typed) InformationProposition.RoleAt(seat, RoleId(role)) else null,
        isDefaultRecommendation = recommended,
    )
}
