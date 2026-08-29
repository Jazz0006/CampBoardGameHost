package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingSetupPresetRotationScorerTest {
    @Test
    fun `five history ages use the explicit dataset decay weights`() {
        val candidate = candidatePreset()
        val weights = listOf(1.0, 0.65, 0.40, 0.20, 0.10)
        val totalWeight = weights.sum()

        weights.indices.forEach { matchingAge ->
            val history = TroubleBrewingSetupRotationHistory(
                recentGames = weights.indices.map { age ->
                    rotationRecord(
                        presetId = "history-$age",
                        roleIds = if (age == matchingAge) {
                            candidate.nonDemonRoleIds()
                        } else {
                            disjointRoleIds(candidate.playerCount, age)
                        },
                    )
                },
            )

            val score = TroubleBrewingSetupPresetRotationScorer.scoreBaseNovelty(
                candidate = candidate,
                recentSetupRotationHistory = history,
                historyWeights = weights,
            )
            val expectedOverlap = weights[matchingAge] / totalWeight

            assertEquals(expectedOverlap, score.weightedOverlap, 1e-9)
            assertEquals(maxOf(0.20, 1.0 - expectedOverlap), score.baseNoveltyWeight, 1e-9)
        }
    }

    @Test
    fun `history denominator contains only available recent games`() {
        val candidate = candidatePreset()
        val weights = listOf(1.0, 0.65, 0.40, 0.20, 0.10)
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = listOf(
                rotationRecord(
                    presetId = "most-recent",
                    roleIds = candidate.nonDemonRoleIds(),
                ),
                rotationRecord(
                    presetId = "age-one",
                    roleIds = disjointRoleIds(candidate.playerCount, 1),
                ),
            ),
        )

        val score = TroubleBrewingSetupPresetRotationScorer.scoreBaseNovelty(
            candidate = candidate,
            recentSetupRotationHistory = history,
            historyWeights = weights,
        )

        val expectedOverlap = 1.0 / (1.0 + 0.65)
        assertEquals(expectedOverlap, score.weightedOverlap, 1e-9)
        assertEquals(1.0 - expectedOverlap, score.baseNoveltyWeight, 1e-9)
    }

    private fun candidatePreset() = TroubleBrewingSetupPreset(
        id = "candidate",
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

    private fun rotationRecord(
        presetId: String,
        roleIds: Set<String>,
    ) = TroubleBrewingSetupRotationRecord(
        datasetId = "history-dataset",
        schemaVersion = 2,
        presetId = presetId,
        playerCount = 8,
        realNonDemonRoleIds = roleIds,
        minionRoleIds = emptySet(),
        primaryStyleTag = null,
        selectedDrunkShownRole = null,
    )

    private fun disjointRoleIds(playerCount: Int, age: Int): Set<String> =
        List(playerCount - 1) { index -> "disjoint-$age-$index" }.toSet()

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()
}
