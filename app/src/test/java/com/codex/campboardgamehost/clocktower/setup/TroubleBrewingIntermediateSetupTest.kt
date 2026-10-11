package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingIntermediateSetupTest {
    @Test
    fun `drunk preset realizes extra visible townsfolk without binding a drunk seat`() {
        val preset = drunkPreset()

        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = preset,
        )
        val setup = TroubleBrewingIntermediateSetup(
            datasetId = "tb-test",
            schemaVersion = 1,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = 42L,
            visibleRoster = visibleRoster,
            shownSeatAssignments = visibleRoster.visibleRoleIds.mapIndexed { index, roleId ->
                TroubleBrewingShownSeatAssignment(
                    seat = index + 1,
                    playerName = "Player ${index + 1}",
                    shownRoleId = roleId,
                )
            },
        )

        assertTrue(setup.visibleRoster.hasDrunk)
        assertEquals(
            setOf("washerwoman", "librarian", "investigator", "chef"),
            setup.visibleRoster.townsfolkRoleIds.toSet(),
        )
        assertFalse("drunk" in setup.visibleRoster.visibleRoleIds)
        assertEquals(preset.playerCount, setup.shownSeatAssignments.size)
        assertEquals(
            setup.visibleRoster.visibleRoleIds.sorted(),
            setup.shownSeatAssignments.map { it.shownRoleId }.sorted(),
        )
    }

    @Test
    fun `non drunk preset preserves its visible roster`() {
        val preset = nonDrunkPreset()

        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = preset,
        )

        assertFalse(visibleRoster.hasDrunk)
        assertEquals(
            (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).sorted(),
            visibleRoster.visibleRoleIds.sorted(),
        )

    }

    @Test
    fun `intermediate setup requires shown seats to exactly cover the visible roster`() {
        val preset = drunkPreset()
        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = preset,
        )
        val assignments = visibleRoster.visibleRoleIds.mapIndexed { index, roleId ->
            TroubleBrewingShownSeatAssignment(
                seat = index + 1,
                playerName = "Player ${index + 1}",
                shownRoleId = if (index == 0) visibleRoster.visibleRoleIds[1] else roleId,
            )
        }

        expectIllegalArgument {
            TroubleBrewingIntermediateSetup(
                datasetId = "tb-test",
                schemaVersion = 1,
                presetId = preset.id,
                playerCount = preset.playerCount,
                gameSeed = 42L,
                visibleRoster = visibleRoster,
                shownSeatAssignments = assignments,
            )
        }
    }

    private fun drunkPreset() = TroubleBrewingSetupPreset(
        id = "drunk-preset",
        playerCount = 6,
        townsfolk = listOf("washerwoman", "librarian", "investigator", "chef"),
        outsiders = emptyList(),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "beginner",
        styleTags = emptyList(),
        drunk = true,
    )

    private fun nonDrunkPreset() = TroubleBrewingSetupPreset(
        id = "non-drunk-preset",
        playerCount = 5,
        townsfolk = listOf("washerwoman", "librarian", "investigator"),
        outsiders = emptyList(),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "beginner",
        styleTags = emptyList(),
        drunk = false,
    )

    private fun expectIllegalArgument(block: () -> Unit) {
        var thrown = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            thrown = true
        }
        assertTrue("Expected IllegalArgumentException", thrown)
    }
}
