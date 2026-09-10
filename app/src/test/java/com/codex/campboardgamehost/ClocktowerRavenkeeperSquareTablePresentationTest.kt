package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRavenkeeperSquareTablePresentationTest {
    @Test
    fun `typed result must belong to the selected Ravenkeeper target`() {
        val result = clocktowerRavenkeeperTypedResult(
            proposition = InformationProposition.RoleAt(5, RoleId("Chef")),
            selectedSeat = 5,
            seatCount = 7,
        )

        assertEquals(ClocktowerRavenkeeperTypedResult(5, RoleId("Chef")), result)
    }

    @Test
    fun `missing selected target mismatched seat or invalid proposition fails closed`() {
        assertNull(
            clocktowerRavenkeeperTypedResult(
                proposition = InformationProposition.RoleAt(5, RoleId("Chef")),
                selectedSeat = null,
                seatCount = 7,
            ),
        )
        assertNull(
            clocktowerRavenkeeperTypedResult(
                proposition = InformationProposition.RoleAt(4, RoleId("Chef")),
                selectedSeat = 5,
                seatCount = 7,
            ),
        )
        assertNull(
            clocktowerRavenkeeperTypedResult(
                proposition = InformationProposition.RoleInPlay(RoleId("Chef"), true),
                selectedSeat = 5,
                seatCount = 7,
            ),
        )
    }

    @Test
    fun `result first registration options preserve typed role choices for one selected target`() {
        val chef = option(seat = 5, role = "Chef", recommended = true)
        val washerwoman = option(seat = 5, role = "Washerwoman")

        val choices = clocktowerRavenkeeperResultChoices(
            step = ravenkeeperStep(),
            selectedSeat = 5,
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = listOf(chef, washerwoman),
        )

        assertEquals(listOf(5, 5), choices.map { it.targetSeat })
        assertEquals(listOf("Chef", "Washerwoman"), choices.map { it.roleId?.value })
        assertEquals(listOf("Chef", "Washerwoman"), choices.map { it.displayLabel })
        assertEquals(
            listOf(
                ClocktowerRavenkeeperResultSourceKind.DisplayOption,
                ClocktowerRavenkeeperResultSourceKind.DisplayOption,
            ),
            choices.map { it.sourceKind },
        )
        assertTrue(choices.first().recommended)
    }

    @Test
    fun `candidate for a different target fails closed instead of changing target ownership`() {
        val choices = clocktowerRavenkeeperResultChoices(
            step = ravenkeeperStep(),
            selectedSeat = 5,
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = listOf(
                option(seat = 5, role = "Chef"),
                option(seat = 4, role = "Washerwoman"),
            ),
        )

        assertTrue(choices.isEmpty())
    }

    @Test
    fun `unreliable role choices stay opaque while selected target remains typed`() {
        val choices = clocktowerRavenkeeperResultChoices(
            step = ravenkeeperStep(
                displayOptions = listOf(
                    option(seat = 5, role = "Chef", typed = false),
                    option(seat = 5, role = "Imp", typed = false),
                ),
            ),
            selectedSeat = 5,
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
        )

        assertEquals(setOf(5), choices.map { it.targetSeat }.toSet())
        assertTrue(choices.all { it.roleId == null })
        assertEquals(listOf("Chef", "Imp"), choices.map { it.displayLabel })
        assertEquals(
            listOf(
                ClocktowerRavenkeeperResultSourceKind.LegacyUnreliable,
                ClocktowerRavenkeeperResultSourceKind.LegacyUnreliable,
            ),
            choices.map { it.sourceKind },
        )
    }

    @Test
    fun `opaque unreliable option never becomes a player proposition`() {
        val selected = option(seat = 5, role = "Imp", typed = false)
        val resolved = resolveClocktowerLegacyUnreliablePlayerDisplay(
            ravenkeeperStep(displayOptions = listOf(selected)),
            selected,
        )

        assertEquals("Imp", resolved.displayPrimary)
        assertNull(resolved.displayProposition)
    }

    @Test
    fun `ordinary reliable result uses the typed direct proposition`() {
        val choices = clocktowerRavenkeeperResultChoices(
            step = ravenkeeperStep(),
            selectedSeat = 5,
            seatCount = 7,
            automaticStorytellerInfo = false,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
        )

        assertEquals(1, choices.size)
        assertEquals("Chef", choices.single().roleId?.value)
        assertEquals("Chef", choices.single().displayLabel)
        assertEquals(ClocktowerRavenkeeperResultSourceKind.Direct, choices.single().sourceKind)
    }

    @Test
    fun `automatic unreliable result uses only the already selected opaque option`() {
        val selected = option(seat = 5, role = "Imp", typed = false)
        val choices = clocktowerRavenkeeperResultChoices(
            step = ravenkeeperStep(
                displayOptions = listOf(
                    option(seat = 5, role = "Chef", typed = false),
                    selected,
                ),
            ),
            selectedSeat = 5,
            seatCount = 7,
            automaticStorytellerInfo = true,
            automaticDisplayOption = selected,
            resultFirstRegistrationCandidates = emptyList(),
        )

        assertEquals(1, choices.size)
        assertNull(choices.single().roleId)
        assertEquals("Imp", choices.single().displayLabel)
        assertEquals(ClocktowerRavenkeeperResultSourceKind.DisplayOption, choices.single().sourceKind)
    }

    @Test
    fun `non role-at typed option fails closed while null proposition remains valid opaque choice`() {
        val invalid = ClocktowerDisplayOption(
            label = "Chef",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = "Ravenkeeper information",
            displayPrimary = "Chef",
            displaySecondary = null,
            displayFooter = null,
            proposition = InformationProposition.RoleInPlay(RoleId("Chef"), true),
        )
        assertNull(clocktowerRoleRevealChoiceProjection(invalid, targetSeat = 5, seatCount = 7))

        val opaque = option(seat = 5, role = "Chef", typed = false)
        val projection = clocktowerRoleRevealChoiceProjection(opaque, targetSeat = 5, seatCount = 7)
        assertEquals(5, projection?.targetSeat)
        assertNull(projection?.roleId)
        assertEquals("Chef", projection?.displayLabel)
    }

    private fun ravenkeeperStep(
        displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    ) = ClocktowerNightStepUi(
        title = "Ravenkeeper",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = "P5 is Chef",
        explanation = "",
        action = ClocktowerNightAction.Ravenkeeper,
        roleEnName = "Ravenkeeper",
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayPrimary = "Chef",
        displayProposition = InformationProposition.RoleAt(5, RoleId("Chef")),
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
        displayTitle = "Ravenkeeper information",
        displayPrimary = role,
        displaySecondary = null,
        displayFooter = "Checked player: P$seat",
        proposition = if (typed) InformationProposition.RoleAt(seat, RoleId(role)) else null,
        isDefaultRecommendation = recommended,
    )
}
