package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(
            null,
            clocktowerUndertakerTypedResult(
                proposition = InformationProposition.RoleInPlay(RoleId("Chef"), true),
                seatCount = 7,
            ),
        )
        assertEquals(
            null,
            clocktowerUndertakerTypedResult(
                proposition = InformationProposition.RoleAt(8, RoleId("Chef")),
                seatCount = 7,
            ),
        )
    }

    @Test
    fun `manual final results keep one fixed executed seat while role may vary`() {
        val chef = option(seat = 4, role = "Chef", recommended = true)
        val Spy = option(seat = 4, role = "Spy")

        val choices = clocktowerUndertakerResultChoices(
            step = undertakerStep(),
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = listOf(chef, Spy),
        )

        assertEquals(listOf(4, 4), choices.map { it.executedSeat })
        assertEquals(listOf("Chef", "Spy"), choices.map { it.roleId.value })
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
    fun `unreliable role choices remain typed and preserve legacy publish source`() {
        val step = undertakerStep(
            displayOptions = listOf(
                option(seat = 4, role = "Chef"),
                option(seat = 4, role = "Imp"),
            ),
        )

        val choices = clocktowerUndertakerResultChoices(
            step = step,
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
        )

        assertEquals(setOf(4), choices.map { it.executedSeat }.toSet())
        assertEquals(
            listOf(
                ClocktowerUndertakerResultSourceKind.LegacyUnreliable,
                ClocktowerUndertakerResultSourceKind.LegacyUnreliable,
            ),
            choices.map { it.sourceKind },
        )
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
        tellPlayer = "",
        explanation = "",
        roleEnName = "Undertaker",
        displayProposition = InformationProposition.RoleAt(4, RoleId("Chef")),
        displayOptions = displayOptions,
    )

    private fun option(
        seat: Int,
        role: String,
        recommended: Boolean = false,
    ) = ClocktowerDisplayOption(
        label = role,
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayTitle = "Undertaker information",
        displayPrimary = role,
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.RoleAt(seat, RoleId(role)),
        isDefaultRecommendation = recommended,
    )
}
