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
        val history = historyOf(
            playerCount = 8,
            realNonDemonRoleIds = repeatedComposition.nonDemonRoleIds(),
            presetId = "previous-different-id",
        )

        val selected = TroubleBrewingSetupPresetSelector.select(
            dataset = dataset,
            playerCount = 8,
            gameSeed = 13579L,
            recentSetupRotationHistory = history,
        )

        assertEquals("candidate-alternative", selected.presetId)
    }

    @Test
    fun `last game overlap threshold admits the highest discrete value below the limit and rejects the next value for every player count`() {
        val thresholds = linkedMapOf(
            5 to 0.60,
            6 to 0.60,
            7 to 0.70,
            8 to 0.72,
            9 to 0.75,
            10 to 0.78,
            11 to 0.80,
            12 to 0.82,
            13 to 0.83,
            14 to 0.85,
            15 to 0.86,
        )

        thresholds.forEach { (playerCount, threshold) ->
            val nonDemonCount = playerCount - 1
            val previousRoleIds = List(nonDemonCount) { index -> "previous-$playerCount-$index" }
            val allowedOverlapCount = (threshold * nonDemonCount).toInt()
            val rejectedOverlapCount = allowedOverlapCount + 1
            val allowed = overlapPreset(
                id = "allowed-$playerCount",
                playerCount = playerCount,
                previousRoleIds = previousRoleIds,
                overlapCount = allowedOverlapCount,
            )
            val rejected = overlapPreset(
                id = "rejected-$playerCount",
                playerCount = playerCount,
                previousRoleIds = previousRoleIds,
                overlapCount = rejectedOverlapCount,
            )
            val previousSet = previousRoleIds.toSet()
            assertTrue(allowed.overlapWith(previousSet) <= threshold)
            assertTrue(rejected.overlapWith(previousSet) > threshold)

            val dataset = datasetOf(
                playerCount = playerCount,
                presets = listOf(rejected, allowed),
                policy = testPolicy().copy(lastGameMaxOverlap = mapOf(playerCount to threshold)),
            )
            val history = historyOf(
                playerCount = playerCount,
                realNonDemonRoleIds = previousSet,
            )

            val selectedIds = (0L until 64L).map { gameSeed ->
                TroubleBrewingSetupPresetSelector.select(
                    dataset = dataset,
                    playerCount = playerCount,
                    gameSeed = gameSeed,
                    recentSetupRotationHistory = history,
                ).presetId
            }.toSet()

            assertEquals("playerCount=$playerCount", setOf(allowed.id), selectedIds)
        }
    }

    @Test
    fun `fallback relaxes overlap by five points and selects from the first non empty level`() {
        val playerCount = 8
        val previousRoleIds = List(playerCount - 1) { index -> "fallback-previous-$index" }
        val previousSet = previousRoleIds.toSet()
        val exactRepeat = overlapPreset(
            id = "exact-repeat-must-stay-rejected",
            playerCount = playerCount,
            previousRoleIds = previousRoleIds,
            overlapCount = 7,
        )
        val firstEligibleAtPlusTen = overlapPreset(
            id = "first-eligible-at-plus-ten",
            playerCount = playerCount,
            previousRoleIds = previousRoleIds,
            overlapCount = 4,
        )
        val laterEligible = overlapPreset(
            id = "later-eligible",
            playerCount = playerCount,
            previousRoleIds = previousRoleIds,
            overlapCount = 5,
        )
        assertTrue(firstEligibleAtPlusTen.overlapWith(previousSet) > 0.55)
        assertTrue(firstEligibleAtPlusTen.overlapWith(previousSet) <= 0.60)
        assertTrue(laterEligible.overlapWith(previousSet) > 0.60)
        assertTrue(laterEligible.overlapWith(previousSet) <= 0.75)

        val dataset = datasetOf(
            playerCount = playerCount,
            presets = listOf(laterEligible, exactRepeat, firstEligibleAtPlusTen),
            policy = testPolicy().copy(
                lastGameMaxOverlap = mapOf(playerCount to 0.50),
                historyWeights = emptyList(),
            ),
        )
        val history = historyOf(
            playerCount = playerCount,
            realNonDemonRoleIds = previousSet,
            presetId = "previous-different-id",
        )

        val selectedIds = (0L until 128L).map { gameSeed ->
            TroubleBrewingSetupPresetSelector.select(
                dataset = dataset,
                playerCount = playerCount,
                gameSeed = gameSeed,
                recentSetupRotationHistory = history,
            ).presetId
        }.toSet()

        assertEquals(setOf(firstEligibleAtPlusTen.id), selectedIds)
    }

    @Test
    fun `fallback never re-enables an exact repeat when it is the only preset`() {
        val playerCount = 8
        val previousRoleIds = List(playerCount - 1) { index -> "exact-only-$index" }
        val exactRepeat = overlapPreset(
            id = "exact-only-candidate",
            playerCount = playerCount,
            previousRoleIds = previousRoleIds,
            overlapCount = playerCount - 1,
        )
        val dataset = datasetOf(
            playerCount = playerCount,
            presets = listOf(exactRepeat),
            policy = testPolicy().copy(lastGameMaxOverlap = mapOf(playerCount to 0.50)),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupPresetSelector.select(
                dataset = dataset,
                playerCount = playerCount,
                gameSeed = 1L,
                recentSetupRotationHistory = historyOf(
                    playerCount = playerCount,
                    realNonDemonRoleIds = previousRoleIds.toSet(),
                    presetId = "previous-different-id",
                ),
            )
        }
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

    private fun historyOf(
        playerCount: Int,
        realNonDemonRoleIds: Set<String>,
        presetId: String = "previous-preset",
    ) = TroubleBrewingSetupRotationHistory(
        recentGames = listOf(
            TroubleBrewingSetupRotationRecord(
                datasetId = "previous-dataset",
                schemaVersion = 2,
                presetId = presetId,
                playerCount = playerCount,
                realNonDemonRoleIds = realNonDemonRoleIds,
                minionRoleIds = emptySet(),
                primaryStyleTag = null,
                selectedDrunkShownRole = null,
            ),
        ),
    )

    private fun overlapPreset(
        id: String,
        playerCount: Int,
        previousRoleIds: List<String>,
        overlapCount: Int,
    ): TroubleBrewingSetupPreset {
        val nonDemonCount = playerCount - 1
        require(previousRoleIds.size == nonDemonCount)
        require(overlapCount in 0..nonDemonCount)
        val roleIds = previousRoleIds.take(overlapCount) +
            List(nonDemonCount - overlapCount) { index -> "new-$id-$index" }
        return TroubleBrewingSetupPreset(
            id = id,
            playerCount = playerCount,
            townsfolk = roleIds.dropLast(1),
            outsiders = emptyList(),
            minions = listOf(roleIds.last()),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )
    }

    private fun TroubleBrewingSetupPreset.overlapWith(previousRoleIds: Set<String>): Double =
        nonDemonRoleIds().intersect(previousRoleIds).size.toDouble() / (playerCount - 1).toDouble()

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
