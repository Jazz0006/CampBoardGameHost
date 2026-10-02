package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingDrunkSelectionRouterTest {
    @Test
    fun `experienced drunk setup requires manual selection across exact legal domain`() {
        val prepared = drunkPreparedSetup().copy(
            compatibilityConfirmedDrunkCandidate = null,
        )
        val legal = TroubleBrewingDrunkCandidateDomain.legalCandidates(prepared.intermediateSetup)

        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = prepared,
            experienceMode = StorytellerExperienceMode.EXPERIENCED,
            recommendedCandidate = null,
        )

        val manual = route as TroubleBrewingDrunkSelectionRoute.ManualSelection
        assertEquals(legal, manual.request.candidates)
        assertNull(manual.request.recommendedCandidate)
    }

    @Test
    fun `experienced manual request may carry only an exact current legal recommendation`() {
        val prepared = drunkPreparedSetup()
        val legal = TroubleBrewingDrunkCandidateDomain.legalCandidates(prepared.intermediateSetup)
        val recommended = legal.first()

        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = prepared,
            experienceMode = StorytellerExperienceMode.EXPERIENCED,
            recommendedCandidate = recommended,
        )

        val manual = route as TroubleBrewingDrunkSelectionRoute.ManualSelection
        assertEquals(recommended, manual.request.recommendedCandidate)

        val stale = recommended.copy(playerName = "Stale Player")
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = prepared,
                experienceMode = StorytellerExperienceMode.EXPERIENCED,
                recommendedCandidate = stale,
            )
        }
    }

    @Test
    fun `beginner drunk setup preserves exact compatibility confirmation without recommendation authority`() {
        val prepared = drunkPreparedSetup()
        val expected = requireNotNull(prepared.compatibilityConfirmedDrunkCandidate)

        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = prepared,
            experienceMode = StorytellerExperienceMode.BEGINNER,
            recommendedCandidate = null,
        )

        val immediate = route as TroubleBrewingDrunkSelectionRoute.CompatibilityImmediate
        assertEquals(expected, immediate.candidate)
        assertTrue(
            immediate.candidate in
                TroubleBrewingDrunkCandidateDomain.legalCandidates(prepared.intermediateSetup),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = prepared,
                experienceMode = StorytellerExperienceMode.BEGINNER,
                recommendedCandidate = expected,
            )
        }
    }

    @Test
    fun `beginner may consume an already evaluated automatic candidate while compatibility remains fallback`() {
        val prepared = drunkPreparedSetup()
        val legal = TroubleBrewingDrunkCandidateDomain.legalCandidates(prepared.intermediateSetup)
        val automatic = legal.first { candidate ->
            candidate != prepared.compatibilityConfirmedDrunkCandidate
        }

        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = prepared,
            experienceMode = StorytellerExperienceMode.BEGINNER,
            recommendedCandidate = null,
            beginnerAutomaticCandidate = automatic,
        )

        val immediate = route as TroubleBrewingDrunkSelectionRoute.BeginnerAutomatic
        assertEquals(automatic, immediate.candidate)

        val stale = automatic.copy(playerName = "Stale Player")
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = prepared,
                experienceMode = StorytellerExperienceMode.BEGINNER,
                recommendedCandidate = null,
                beginnerAutomaticCandidate = stale,
            )
        }
    }

    @Test
    fun `setup without drunk needs no selection in either experience mode`() {
        val prepared = nonDrunkPreparedSetup()

        StorytellerExperienceMode.entries.forEach { mode ->
            val route = TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = prepared,
                experienceMode = mode,
                recommendedCandidate = null,
            )
            assertEquals(TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded, route)
        }
    }

    private fun drunkPreparedSetup(): TroubleBrewingPreparedSetup {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-drunk-selection-test",
            playerCount = 5,
            townsfolk = listOf("chef", "empath"),
            outsiders = listOf("drunk"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunkAsOptions = listOf("washerwoman"),
        )
        val intermediate = TroubleBrewingIntermediateSetup(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = 5,
            gameSeed = 4_101L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
                outsiderRoleIds = emptyList(),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Alice", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Bob", "empath"),
                TroubleBrewingShownSeatAssignment(3, "Cara", "washerwoman"),
                TroubleBrewingShownSeatAssignment(4, "Dan", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Eve", "imp"),
            ),
        )
        val compatibility = TroubleBrewingSetupDealPlan(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = 5,
            gameSeed = 4_101L,
            selectedDrunkShownRole = "washerwoman",
            assignments = listOf(
                TroubleBrewingSetupDealAssignment(1, "Alice", "chef", "chef"),
                TroubleBrewingSetupDealAssignment(2, "Bob", "empath", "empath"),
                TroubleBrewingSetupDealAssignment(3, "Cara", "drunk", "washerwoman"),
                TroubleBrewingSetupDealAssignment(4, "Dan", "poisoner", "poisoner"),
                TroubleBrewingSetupDealAssignment(5, "Eve", "imp", "imp"),
            ),
        )
        val compatibilityCandidate = TroubleBrewingDrunkCandidate(
            seat = 3,
            playerName = "Cara",
            shownRoleId = "washerwoman",
        )
        return TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
            compatibilityDealPlan = compatibility,
            compatibilityConfirmedDrunkCandidate = compatibilityCandidate,
        )
    }

    private fun nonDrunkPreparedSetup(): TroubleBrewingPreparedSetup {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-no-drunk-selection-test",
            playerCount = 5,
            townsfolk = listOf("chef", "empath"),
            outsiders = listOf("saint"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )
        val intermediate = TroubleBrewingIntermediateSetup(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = 5,
            gameSeed = 4_102L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = false,
                townsfolkRoleIds = listOf("chef", "empath"),
                outsiderRoleIds = listOf("saint"),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Alice", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Bob", "empath"),
                TroubleBrewingShownSeatAssignment(3, "Cara", "saint"),
                TroubleBrewingShownSeatAssignment(4, "Dan", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Eve", "imp"),
            ),
        )
        val compatibility = TroubleBrewingSetupDealPlan(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = 5,
            gameSeed = 4_102L,
            selectedDrunkShownRole = null,
            assignments = intermediate.shownSeatAssignments.map { assignment ->
                TroubleBrewingSetupDealAssignment(
                    seat = assignment.seat,
                    playerName = assignment.playerName,
                    actualRoleId = assignment.shownRoleId,
                    shownRoleId = assignment.shownRoleId,
                )
            },
        )
        return TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
            compatibilityDealPlan = compatibility,
            compatibilityConfirmedDrunkCandidate = null,
        )
    }
}
