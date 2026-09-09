package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerClockmakerSquareTablePresentationTest {
    @Test
    fun `healthy Clockmaker keeps one direct number reveal`() {
        val choices = clocktowerClockmakerResultChoices(
            step = clockmakerStep(displayKind = ClocktowerDisplayKind.Number, displayPrimary = "2"),
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
        )

        assertEquals(1, choices.size)
        assertEquals("2", choices.single().displayValue)
        assertEquals(ClocktowerClockmakerResultSourceKind.Direct, choices.single().sourceKind)
    }

    @Test
    fun `manual unreliable Clockmaker preserves opaque existing display options`() {
        val choices = clocktowerClockmakerResultChoices(
            step = clockmakerStep(
                displayKind = ClocktowerDisplayKind.None,
                displayPrimary = null,
                displayOptions = listOf(
                    option(label = "Recommended · gentle: 1", value = "1", recommended = false),
                    option(label = "Recommended · balanced: 3", value = "3", recommended = true),
                ),
            ),
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
        )

        assertEquals(listOf("1", "3"), choices.map { it.displayValue })
        assertEquals(
            listOf("Recommended · gentle: 1", "Recommended · balanced: 3"),
            choices.map { it.selectionLabel },
        )
        assertTrue(choices.all { it.displayOption?.proposition == null })
        assertTrue(choices.all { it.sourceKind == ClocktowerClockmakerResultSourceKind.LegacyUnreliable })
        assertTrue(choices.last().recommended)
    }

    @Test
    fun `automatic unreliable Clockmaker uses only already selected option`() {
        val selected = option(label = "Recommended · balanced: 3", value = "3", recommended = true)
        val choices = clocktowerClockmakerResultChoices(
            step = clockmakerStep(
                displayKind = ClocktowerDisplayKind.None,
                displayPrimary = null,
            ),
            automaticStorytellerInfo = true,
            automaticDisplayOption = selected,
        )

        assertEquals(1, choices.size)
        assertEquals("3", choices.single().displayValue)
        assertEquals(ClocktowerClockmakerResultSourceKind.DisplayOption, choices.single().sourceKind)
        assertEquals(selected, choices.single().displayOption)
    }

    @Test
    fun `Clockmaker presentation fails closed for wrong role missing actor or blank result`() {
        assertTrue(
            clocktowerClockmakerResultChoices(
                step = clockmakerStep(roleEnName = "Empath"),
                automaticStorytellerInfo = false,
                automaticDisplayOption = null,
            ).isEmpty(),
        )
        assertTrue(
            clocktowerClockmakerResultChoices(
                step = clockmakerStep(actor = null),
                automaticStorytellerInfo = false,
                automaticDisplayOption = null,
            ).isEmpty(),
        )
        assertTrue(
            clocktowerClockmakerResultChoices(
                step = clockmakerStep(displayPrimary = ""),
                automaticStorytellerInfo = false,
                automaticDisplayOption = null,
            ).isEmpty(),
        )
    }

    @Test
    fun `Clockmaker actor highlight is independent from read-only seat state`() {
        val actor = clocktowerClockmakerSeatPresentation(seatNumber = 4, actorSeat = 4)
        val other = clocktowerClockmakerSeatPresentation(seatNumber = 2, actorSeat = 4)

        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertFalse(other.isCurrentActor)
    }

    private fun clockmakerStep(
        actor: PlayerCard? = clockmakerCard(),
        roleEnName: String = "Clockmaker",
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.Number,
        displayPrimary: String? = "2",
        displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    ) = ClocktowerNightStepUi(
        title = "Clockmaker",
        actor = actor,
        isRealAction = actor != null,
        reason = "",
        storytellerAction = "Wake Clockmaker",
        tellPlayer = displayPrimary,
        explanation = "Distance from Demon to nearest Minion",
        roleEnName = roleEnName,
        displayKind = displayKind,
        displayTitle = "Clockmaker information",
        displayPrimary = displayPrimary,
        displayOptions = displayOptions,
    )

    private fun option(
        label: String,
        value: String,
        recommended: Boolean,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Clockmaker information",
        displayPrimary = value,
        displaySecondary = null,
        displayFooter = "Distance from Demon to nearest Minion",
        proposition = null,
        isDefaultRecommendation = recommended,
    )

    private fun clockmakerCard() = PlayerCard(
        name = "C",
        role = Role.Civilian,
        word = "",
        clocktowerTeam = ClocktowerTeam.Townsfolk,
        clocktowerRole = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "钟表匠",
            enName = "Clockmaker",
            zhDescription = "",
            enDescription = "",
        ),
    )
}
