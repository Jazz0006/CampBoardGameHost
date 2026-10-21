package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingDrunkSelectionRouterTest {
    @Test
    fun `multiple legal Drunk assignments remain manual without a recommendation`() {
        val prepared = drunkPreparedSetup()
        val legal = prepared.intermediateSetup.shownSeatAssignments
            .filter { it.shownRoleId in prepared.intermediateSetup.visibleRoster.townsfolkRoleIds }
            .map { TroubleBrewingDrunkCandidate(it.seat, it.playerName, it.shownRoleId) }
        val route = TroubleBrewingDrunkSelectionRouter.route(prepared, null)
        val manual = route as TroubleBrewingDrunkSelectionRoute.ManualSelection
        assertEquals(legal, manual.request.candidates)
        assertNull(manual.request.recommendedCandidate)
    }

    @Test
    fun `recommended Drunk must exactly match the live Host legal domain`() {
        val prepared = drunkPreparedSetup()
        val selected = prepared.intermediateSetup.shownSeatAssignments.map { TroubleBrewingDrunkCandidate(it.seat, it.playerName, it.shownRoleId) }.first()
        val route = TroubleBrewingDrunkSelectionRouter.route(prepared, selected)
        assertEquals(selected,
            (route as TroubleBrewingDrunkSelectionRoute.ManualSelection).request.recommendedCandidate)
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingDrunkSelectionRouter.route(prepared,
                selected.copy(playerName = "Stale Player"))
        }
        for (seat in prepared.intermediateSetup.shownSeatAssignments.filter {
            it.shownRoleId !in prepared.intermediateSetup.visibleRoster.townsfolkRoleIds
        }) {
            assertThrows(IllegalArgumentException::class.java) {
                TroubleBrewingDrunkSelectionRouter.route(
                    prepared, TroubleBrewingDrunkCandidate(seat.seat, seat.playerName, seat.shownRoleId),
                )
            }
        }
    }

    @Test
    fun `setup without Drunk never requires a Drunk selection`() {
        assertEquals(TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded,
            TroubleBrewingDrunkSelectionRouter.route(nonDrunkPreparedSetup(), null))
    }

    private fun drunkPreparedSetup(): TroubleBrewingPreparedSetup {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-drunk-selection-test",
            playerCount = 5,
            townsfolk = listOf("chef", "empath", "washerwoman"),
            outsiders = emptyList(),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunk = true,
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
        return TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
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
            drunk = false,
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
        return TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
        )
    }
}
