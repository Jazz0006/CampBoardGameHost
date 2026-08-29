package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupPresetSelectorTest {
    @Test
    fun `selection is isolated to the requested player count and returns provenance`() {
        val dataset = TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(7 to 1, 8 to 1),
            runtimeSelectionPolicy = testPolicy(),
            pools = mapOf(
                7 to listOf(preset(id = "seven", playerCount = 7)),
                8 to listOf(preset(id = "eight", playerCount = 8)),
            ),
        )

        val selected = TroubleBrewingSetupPresetSelector.select(
            dataset = dataset,
            playerCount = 8,
            gameSeed = 1234L,
        )

        assertEquals("test-dataset", selected.datasetId)
        assertEquals(2, selected.schemaVersion)
        assertEquals("eight", selected.presetId)
        assertEquals(8, selected.playerCount)
        assertEquals(1234L, selected.gameSeed)
        assertEquals("eight", selected.preset.id)
        assertEquals(8, selected.preset.playerCount)
        assertNull(selected.selectedDrunkShownRole)
    }

    @Test
    fun `selection rejects unsupported player counts instead of falling across pools`() {
        val dataset = TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(7 to 1),
            runtimeSelectionPolicy = testPolicy(),
            pools = mapOf(7 to listOf(preset(id = "seven", playerCount = 7))),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupPresetSelector.select(
                dataset = dataset,
                playerCount = 8,
                gameSeed = 55L,
            )
        }
    }

    @Test
    fun `same seed is reproducible and independent of input pool order`() {
        val presets = listOf(
            preset(id = "alpha", playerCount = 8),
            preset(id = "bravo", playerCount = 8),
            preset(id = "charlie", playerCount = 8),
            preset(id = "delta", playerCount = 8),
        )
        val forward = datasetOf(8, presets)
        val reversed = datasetOf(8, presets.reversed())

        val first = TroubleBrewingSetupPresetSelector.select(
            dataset = forward,
            playerCount = 8,
            gameSeed = 987654321L,
        )
        val repeated = TroubleBrewingSetupPresetSelector.select(
            dataset = forward,
            playerCount = 8,
            gameSeed = 987654321L,
        )
        val reordered = TroubleBrewingSetupPresetSelector.select(
            dataset = reversed,
            playerCount = 8,
            gameSeed = 987654321L,
        )

        assertEquals(first, repeated)
        assertEquals(first.presetId, reordered.presetId)
        assertEquals(first.gameSeed, reordered.gameSeed)
    }

    @Test
    fun `same seed selects the same Drunk shown role independent of option order`() {
        val options = listOf("washerwoman", "librarian", "investigator")
        val drunkPreset = preset(id = "drunk-eight", playerCount = 8).copy(
            townsfolk = List(5) { index -> "town-drunk-$index" },
            outsiders = listOf("drunk"),
            drunkAsOptions = options,
        )
        val reorderedPreset = drunkPreset.copy(drunkAsOptions = options.reversed())

        val first = TroubleBrewingSetupPresetSelector.select(
            dataset = datasetOf(8, listOf(drunkPreset)),
            playerCount = 8,
            gameSeed = 24680L,
        )
        val repeated = TroubleBrewingSetupPresetSelector.select(
            dataset = datasetOf(8, listOf(drunkPreset)),
            playerCount = 8,
            gameSeed = 24680L,
        )
        val reordered = TroubleBrewingSetupPresetSelector.select(
            dataset = datasetOf(8, listOf(reorderedPreset)),
            playerCount = 8,
            gameSeed = 24680L,
        )

        assertTrue(first.selectedDrunkShownRole in options)
        assertEquals(first.selectedDrunkShownRole, repeated.selectedDrunkShownRole)
        assertEquals(first.selectedDrunkShownRole, reordered.selectedDrunkShownRole)
    }

    @Test
    fun `exact previous real non Demon composition is rejected even when preset ids differ`() {
        val repeatedComposition = eightPlayerPreset(id = "candidate-new-id")
        val alternative = repeatedComposition.copy(
            id = "candidate-alternative",
            townsfolk = repeatedComposition.townsfolk.dropLast(1) + "monk",
        )
        val dataset = datasetOf(
            playerCount = 8,
            presets = listOf(repeatedComposition, alternative),
            policy = testPolicy().copy(lastGameMaxOverlap = mapOf(8 to 1.0)),
        )
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = listOf(
                TroubleBrewingSetupRotationRecord(
                    datasetId = "previous-dataset",
                    schemaVersion = 2,
                    presetId = "previous-different-id",
                    playerCount = 8,
                    realNonDemonRoleIds = repeatedComposition.nonDemonRoleIds(),
                    minionRoleIds = repeatedComposition.minions.toSet(),
                    primaryStyleTag = repeatedComposition.styleTags.firstOrNull(),
                    selectedDrunkShownRole = null,
                ),
            ),
        )

        val selected = TroubleBrewingSetupPresetSelector.select(
            dataset = dataset,
            playerCount = 8,
            gameSeed = 13579L,
            recentSetupRotationHistory = history,
        )

        assertEquals("candidate-alternative", selected.presetId)
    }

    private fun datasetOf(
        playerCount: Int,
        presets: List<TroubleBrewingSetupPreset>,
        policy: TroubleBrewingRuntimeSelectionPolicy = testPolicy(),
    ) = TroubleBrewingSetupPresetDataset(
        schemaVersion = 2,
        datasetId = "test-dataset",
        status = "test",
        declaredPoolSizes = mapOf(playerCount to presets.size),
        runtimeSelectionPolicy = policy,
        pools = mapOf(playerCount to presets),
    )

    private fun testPolicy() = TroubleBrewingRuntimeSelectionPolicy(
        exactRepeat = "reject",
        similarityScope = "test",
        roleOverlapFormula = "test",
        lastGameMaxOverlap = mapOf(7 to 0.7, 8 to 0.72),
        historyWeights = listOf(1.0, 0.65, 0.4, 0.2, 0.1),
        extraSoftPenalties = emptyList(),
        fallback = "test",
    )

    private fun eightPlayerPreset(id: String) = TroubleBrewingSetupPreset(
        id = id,
        playerCount = 8,
        townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "empath"),
        outsiders = listOf("recluse"),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "test",
        styleTags = listOf("balanced"),
        drunkAsOptions = emptyList(),
    )

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()

    private fun preset(
        id: String,
        playerCount: Int,
    ) = TroubleBrewingSetupPreset(
        id = id,
        playerCount = playerCount,
        townsfolk = List(playerCount - 2) { index -> "town-$id-$index" },
        outsiders = emptyList(),
        minions = listOf("minion-$id"),
        demons = listOf("imp"),
        source = "test",
        complexity = "test",
        styleTags = emptyList(),
        drunkAsOptions = emptyList(),
    )
}
