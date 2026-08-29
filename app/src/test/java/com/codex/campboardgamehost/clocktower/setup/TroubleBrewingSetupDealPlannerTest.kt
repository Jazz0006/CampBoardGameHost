package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingSetupDealPlannerTest {
    @Test
    fun `Baron preset materializes the exact selected role multiset without applying Baron again`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tbsp-3a-baron-seven",
            playerCount = 7,
            townsfolk = listOf("washerwoman", "librarian", "investigator"),
            outsiders = listOf("butler", "recluse"),
            minions = listOf("baron"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )
        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = 3_001L,
            preset = preset,
            selectedDrunkShownRole = null,
        )
        val orderedPlayerNames = List(preset.playerCount) { index -> "Player ${index + 1}" }

        val plan = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
            orderedPlayerNames = orderedPlayerNames,
        )

        val expectedActualRoleIds =
            preset.townsfolk + preset.outsiders + preset.minions + preset.demons
        val actualRoleIds = plan.assignments.map { it.actualRoleId }

        assertEquals(preset.playerCount, plan.assignments.size)
        assertEquals(expectedActualRoleIds.sorted(), actualRoleIds.sorted())
    }

    @Test
    fun `ordinary preset materializes the exact selected role multiset`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tbsp-3b-ordinary-eight",
            playerCount = 8,
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "empath"),
            outsiders = listOf("butler"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )
        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = 3_002L,
            preset = preset,
            selectedDrunkShownRole = null,
        )
        val orderedPlayerNames = List(preset.playerCount) { index -> "Player ${index + 1}" }

        val plan = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
            orderedPlayerNames = orderedPlayerNames,
        )

        val expectedActualRoleIds =
            preset.townsfolk + preset.outsiders + preset.minions + preset.demons
        val actualRoleIds = plan.assignments.map { it.actualRoleId }

        assertEquals(preset.playerCount, plan.assignments.size)
        assertEquals(expectedActualRoleIds.sorted(), actualRoleIds.sorted())
    }
}
