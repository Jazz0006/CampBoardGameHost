package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.setup.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** User-visible 6-seat square-table selection; no AI network calls or extra Session. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerDrunkTableUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun manualDrunkUsesOneTableWithOnlyTownsfolkTappableAndSelectButton() {
        val prepared = sample()
        val request = (TroubleBrewingDrunkSelectionRouter.route(prepared, null)
            as TroubleBrewingDrunkSelectionRoute.ManualSelection).request
        assertEquals(setOf(1, 3, 5), request.candidates.map { it.seat }.toSet())
        var confirmed: TroubleBrewingDrunkCandidate? = null

        ui.setContent {
            ClocktowerDrunkSelectionScreen(
                preparedSetup = prepared,
                request = request,
                operationMode = StorytellerOperationMode.MANUAL,
                language = "en",
                roleNameForExternalId = { it },
                onBack = {},
                onConfirm = { confirmed = it },
            )
        }

        ui.onNodeWithText("Select").assertIsNotEnabled()
        for (notTown in listOf("Bob", "Dan", "Frank")) {
            ui.onNodeWithText(notTown).assert(hasClickAction().not())
        }
        ui.onNodeWithText("Alice").performClick()
        ui.onNodeWithText("Select").assertIsEnabled()
        ui.onNodeWithText("Selected: 1", substring = true).assertExists()
        ui.onNodeWithText("Carol").performClick()
        ui.onNodeWithText("Selected: 3", substring = true).assertExists()
        assertNull(confirmed) // Selecting/overriding is still UI-only.
        ui.onNodeWithText("Select").performClick()
        assertEquals(3, confirmed?.seat)
    }

    @Test fun assistedRecommendationPreselectsButCanBeReplacedWithoutAnotherRequest() {
        val prepared = sample()
        val original = (TroubleBrewingDrunkSelectionRouter.route(prepared, null)
            as TroubleBrewingDrunkSelectionRoute.ManualSelection).request
        val request = original.copy(recommendedCandidate = original.candidates.single { it.seat == 5 })
        var confirmed: TroubleBrewingDrunkCandidate? = null
        ui.setContent {
            ClocktowerDrunkSelectionScreen(
                preparedSetup = prepared, request = request,
                operationMode = StorytellerOperationMode.AI_ASSISTED,
                language = "en", roleNameForExternalId = { it },
                onBack = {}, onConfirm = { confirmed = it },
            )
        }
        ui.onNodeWithText("Select").assertIsEnabled()
        ui.onNodeWithText("Selected: 5", substring = true).assertExists()
        ui.onNodeWithText("Alice").performClick()
        ui.onNodeWithText("Select").performClick()
        assertEquals(1, confirmed?.seat)
    }

    @Test fun pendingPrecommitOnlyProjectsShownRolesNotInventedDrunkIdentity() {
        val seats = troubleBrewingPrecommitHostSeats(sample()) { it }
        assertEquals((1..6).toList(), seats.map { it.seatId.number })
        assertEquals(listOf(Alignment.GOOD, Alignment.GOOD, Alignment.GOOD, Alignment.EVIL, Alignment.GOOD, Alignment.EVIL),
            seats.map { it.actualAlignment })
        seats.forEach { assertNull(it.actualRole) }
    }

    private fun sample(): TroubleBrewingPreparedSetup {
        val roster = TroubleBrewingVisibleRoster(
            hasDrunk = true, townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
            outsiderRoleIds = listOf("saint"), minionRoleIds = listOf("poisoner"),
            demonRoleIds = listOf("imp"),
        )
        val shown = listOf(
            "Alice" to "chef", "Bob" to "saint", "Carol" to "empath",
            "Dan" to "poisoner", "Emma" to "washerwoman", "Frank" to "imp",
        ).mapIndexed { index, pair ->
            TroubleBrewingShownSeatAssignment(index + 1, pair.first, pair.second)
        }
        val preset = TroubleBrewingSetupPreset(
            id = "drunk-table-ui", playerCount = 6,
            townsfolk = roster.townsfolkRoleIds, outsiders = roster.outsiderRoleIds,
            minions = roster.minionRoleIds, demons = roster.demonRoleIds,
            source = "test", complexity = "test", styleTags = emptyList(), drunk = true,
        )
        val intermediate = TroubleBrewingIntermediateSetup(
            datasetId = "ui-test", schemaVersion = 2, presetId = preset.id,
            playerCount = 6, gameSeed = 9L,
            visibleRoster = roster, shownSeatAssignments = shown,
        )
        return TroubleBrewingPreparedSetup(preset, intermediate)
    }
}
