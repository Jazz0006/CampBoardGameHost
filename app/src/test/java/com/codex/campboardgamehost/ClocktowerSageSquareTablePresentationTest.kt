package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSageSquareTablePresentationTest {
    @Test
    fun `healthy Sage reveal uses typed presentation seats without parsing display text`() {
        val cards = (1..5).map { seat -> sageTestCard("P$seat") }
        val step = sageStep(
            actor = cards[3],
            displaySecondary = "999 / localized nonsense",
            presentationSubjectSeats = listOf(2, 5),
        )

        val presentation = clocktowerPairPlayerRevealPresentation(step, cards)

        assertEquals(listOf(2, 5), presentation?.seats?.map { it.seatId.number })
        assertEquals(listOf("P2", "P5"), presentation?.seats?.map { it.playerName })
    }

    @Test
    fun `manual unreliable Sage keeps opaque propositions while preserving typed pair seats`() {
        val actor = sageTestCard("Sage")
        val option = sageOption("balanced", listOf(1, 3), recommended = true)
        val step = sageStep(
            actor = actor,
            displayKind = ClocktowerDisplayKind.None,
            displayPrimary = null,
            presentationSubjectSeats = emptyList(),
            displayOptions = listOf(option),
        )

        val choices = clocktowerSageResultChoices(
            step = step,
            seatCount = 5,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
        )

        assertEquals(1, choices.size)
        assertEquals(listOf(1, 3), choices.single().subjectSeats)
        assertEquals(ClocktowerSageResultSourceKind.LegacyUnreliable, choices.single().sourceKind)
        assertNull(choices.single().displayOption?.proposition)

        val resolved = resolveClocktowerLegacyUnreliablePlayerDisplay(step, option)
        assertNull(resolved.displayProposition)
        assertEquals(listOf(1, 3), resolved.presentationSubjectSeats)
    }

    @Test
    fun `automatic Sage uses only the already selected candidate`() {
        val selected = sageOption("selected", listOf(2, 4), recommended = true)
        val ignored = sageOption("ignored", listOf(1, 5), recommended = false)
        val step = sageStep(
            displayKind = ClocktowerDisplayKind.None,
            displayPrimary = null,
            presentationSubjectSeats = emptyList(),
            displayOptions = listOf(ignored),
        )

        val choices = clocktowerSageResultChoices(
            step = step,
            seatCount = 5,
            automaticStorytellerInfo = true,
            automaticDisplayOption = selected,
        )

        assertEquals(1, choices.size)
        assertEquals(listOf(2, 4), choices.single().subjectSeats)
        assertEquals(selected, choices.single().displayOption)
        assertEquals(ClocktowerSageResultSourceKind.DisplayOption, choices.single().sourceKind)
    }

    @Test
    fun `reliable manual Sage preserves existing recommended candidates`() {
        val first = sageOption("first", listOf(1, 4), recommended = false, truthful = true)
        val balanced = sageOption("balanced", listOf(3, 5), recommended = true, truthful = true)
        val step = sageStep(
            recommendedDisplayOptions = listOf(first, balanced),
        )

        val choices = clocktowerSageResultChoices(
            step = step,
            seatCount = 5,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
        )

        assertEquals(listOf(listOf(1, 4), listOf(3, 5)), choices.map { it.subjectSeats })
        assertTrue(choices.last().recommended)
        assertTrue(choices.all { it.sourceKind == ClocktowerSageResultSourceKind.DisplayOption })
    }

    @Test
    fun `Sage pair identity fails closed when missing duplicated or out of range`() {
        val invalidOptions = listOf(
            sageOption("missing", emptyList()),
            sageOption("duplicate", listOf(2, 2)),
            sageOption("low", listOf(0, 2)),
            sageOption("high", listOf(2, 6)),
            sageOption("too many", listOf(1, 2, 3)),
        )

        invalidOptions.forEach { option ->
            val step = sageStep(
                displayKind = ClocktowerDisplayKind.None,
                displayPrimary = null,
                presentationSubjectSeats = emptyList(),
                displayOptions = listOf(option),
            )
            assertTrue(
                option.label,
                clocktowerSageResultChoices(
                    step = step,
                    seatCount = 5,
                    automaticStorytellerInfo = false,
                    automaticDisplayOption = null,
                ).isEmpty(),
            )
        }
    }

    @Test
    fun `dead Sage actor highlight is independent from information pair highlight`() {
        val actor = clocktowerSageSeatPresentation(
            seatNumber = 4,
            actorSeat = 4,
            subjectSeats = listOf(2, 4),
        )
        val first = clocktowerSageSeatPresentation(
            seatNumber = 2,
            actorSeat = 4,
            subjectSeats = listOf(2, 4),
        )
        val other = clocktowerSageSeatPresentation(
            seatNumber = 1,
            actorSeat = 4,
            subjectSeats = listOf(2, 4),
        )

        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedSecond, actor.targetState)
        assertFalse(first.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedFirst, first.targetState)
        assertFalse(other.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
    }

    private fun sageStep(
        actor: PlayerCard? = sageTestCard("Sage"),
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.EitherOne,
        displayPrimary: String? = "Demon",
        displaySecondary: String? = "2   5",
        presentationSubjectSeats: List<Int> = listOf(2, 5),
        displayOptions: List<ClocktowerDisplayOption> = emptyList(),
        recommendedDisplayOptions: List<ClocktowerDisplayOption> = emptyList(),
    ) = ClocktowerNightStepUi(
        title = "Sage",
        actor = actor,
        isRealAction = actor != null,
        reason = "",
        storytellerAction = "Wake Sage",
        tellPlayer = displayPrimary,
        explanation = "One of two players is the Demon",
        displayKind = displayKind,
        displayTitle = "Sage information",
        displayPrimary = displayPrimary,
        displaySecondary = displaySecondary,
        displayFooter = "One of these two players",
        displayProposition = null,
        presentationSubjectSeats = presentationSubjectSeats,
        displayOptions = displayOptions,
        recommendedDisplayOptions = recommendedDisplayOptions,
        roleEnName = "Sage",
    )

    private fun sageOption(
        label: String,
        seats: List<Int>,
        recommended: Boolean = false,
        truthful: Boolean = false,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = "Sage information",
        displayPrimary = "Demon",
        displaySecondary = "do-not-parse",
        displayFooter = "One of these two players",
        proposition = null,
        presentationSubjectSeats = seats,
        isTruthful = truthful,
        isDefaultRecommendation = recommended,
    )

    private fun sageTestCard(name: String) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = ClocktowerTeam.Townsfolk,
        clocktowerRole = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "贤者",
            enName = "Sage",
            zhDescription = "",
            enDescription = "",
        ),
    )
}
