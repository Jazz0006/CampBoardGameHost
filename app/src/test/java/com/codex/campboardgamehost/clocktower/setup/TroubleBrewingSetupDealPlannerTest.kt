package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

    @Test
    fun `seat assignment is deterministic canonicalized and uses an independent seed namespace`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tbsp-3c-seat-eight",
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
        val reorderedPreset = preset.copy(
            townsfolk = preset.townsfolk.reversed(),
            outsiders = preset.outsiders.reversed(),
            minions = preset.minions.reversed(),
            demons = preset.demons.reversed(),
        )
        val orderedPlayerNames = List(preset.playerCount) { index -> "Player ${index + 1}" }

        fun assignmentsFor(candidate: TroubleBrewingSetupPreset, seed: Long) =
            TroubleBrewingSetupDealPlanner.plan(
                selection = TroubleBrewingSetupPresetSelection(
                    datasetId = "test-dataset",
                    schemaVersion = 2,
                    presetId = preset.id,
                    playerCount = preset.playerCount,
                    gameSeed = seed,
                    preset = candidate,
                    selectedDrunkShownRole = null,
                ),
                orderedPlayerNames = orderedPlayerNames,
            ).assignments.map { assignment -> assignment.seat to assignment.actualRoleId }

        val seedAFirst = assignmentsFor(preset, 3_101L)
        val seedAReplay = assignmentsFor(preset, 3_101L)
        val seedAReorderedInput = assignmentsFor(reorderedPreset, 3_101L)
        val seedB = assignmentsFor(preset, 3_102L)

        assertEquals(seedAFirst, seedAReplay)
        assertEquals(seedAFirst, seedAReorderedInput)
        assertNotEquals(seedAFirst, seedB)
    }
}
