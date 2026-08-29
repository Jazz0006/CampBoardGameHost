package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingSetupPresetRotationScorerTest {
    @Test
    fun `five history ages use the explicit dataset decay weights`() {
        val candidate = candidatePreset()
        val weights = historyWeights()
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
        val weights = historyWeights()
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

    @Test
    fun `same immediately previous minion set applies medium soft penalty`() {
        val candidate = candidatePreset()
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = listOf(
                rotationRecord(
                    presetId = "previous",
                    roleIds = disjointRoleIds(candidate.playerCount, 0),
                    minionRoleIds = candidate.minions.toSet(),
                ),
            ),
        )

        val score = TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight(
            candidate = candidate,
            selectedDrunkShownRole = null,
            recentSetupRotationHistory = history,
            historyWeights = historyWeights(),
        )

        assertEquals(1.0, score.baseNoveltyWeight, 1e-9)
        assertEquals(0.70, score.finalWeight, 1e-9)
    }

    @Test
    fun `primary style repeated at least twice in previous five applies light soft penalty`() {
        val candidate = candidatePreset()
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = listOf(
                rotationRecord("history-0", disjointRoleIds(candidate.playerCount, 0), primaryStyleTag = "balanced"),
                rotationRecord("history-1", disjointRoleIds(candidate.playerCount, 1), primaryStyleTag = "other"),
                rotationRecord("history-2", disjointRoleIds(candidate.playerCount, 2), primaryStyleTag = "balanced"),
                rotationRecord("history-3", disjointRoleIds(candidate.playerCount, 3), primaryStyleTag = "other"),
                rotationRecord("history-4", disjointRoleIds(candidate.playerCount, 4), primaryStyleTag = null),
            ),
        )

        val score = TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight(
            candidate = candidate,
            selectedDrunkShownRole = null,
            recentSetupRotationHistory = history,
            historyWeights = historyWeights(),
        )

        assertEquals(1.0, score.baseNoveltyWeight, 1e-9)
        assertEquals(0.88, score.finalWeight, 1e-9)
    }

    @Test
    fun `same consecutive Drunk shown role applies strong soft penalty`() {
        val candidate = drunkCandidatePreset()
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = listOf(
                rotationRecord(
                    presetId = "previous",
                    roleIds = disjointRoleIds(candidate.playerCount, 0),
                    selectedDrunkShownRole = "washerwoman",
                ),
            ),
        )

        val score = TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight(
            candidate = candidate,
            selectedDrunkShownRole = "washerwoman",
            recentSetupRotationHistory = history,
            historyWeights = historyWeights(),
        )

        assertEquals(1.0, score.baseNoveltyWeight, 1e-9)
        assertEquals(0.40, score.finalWeight, 1e-9)
    }

    @Test
    fun `soft penalties multiply but final candidate weight has five percent floor`() {
        val candidate = drunkCandidatePreset()
        val history = TroubleBrewingSetupRotationHistory(
            recentGames = List(5) { age ->
                rotationRecord(
                    presetId = "history-$age",
                    roleIds = candidate.nonDemonRoleIds(),
                    minionRoleIds = candidate.minions.toSet(),
                    primaryStyleTag = "balanced",
                    selectedDrunkShownRole = "washerwoman",
                )
            },
        )

        val score = TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight(
            candidate = candidate,
            selectedDrunkShownRole = "washerwoman",
            recentSetupRotationHistory = history,
            historyWeights = historyWeights(),
        )

        assertEquals(0.20, score.baseNoveltyWeight, 1e-9)
        assertEquals(0.05, score.finalWeight, 1e-9)
    }

    private fun historyWeights() = listOf(1.0, 0.65, 0.40, 0.20, 0.10)

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

    private fun drunkCandidatePreset() = TroubleBrewingSetupPreset(
        id = "drunk-candidate",
        playerCount = 8,
        townsfolk = listOf("librarian", "investigator", "chef", "empath", "undertaker"),
        outsiders = listOf("drunk"),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "test",
        styleTags = listOf("balanced"),
        drunkAsOptions = listOf("washerwoman", "monk", "fortuneteller"),
    )

    private fun rotationRecord(
        presetId: String,
        roleIds: Set<String>,
        minionRoleIds: Set<String> = emptySet(),
        primaryStyleTag: String? = null,
        selectedDrunkShownRole: String? = null,
    ) = TroubleBrewingSetupRotationRecord(
        datasetId = "history-dataset",
        schemaVersion = 2,
        presetId = presetId,
        playerCount = 8,
        realNonDemonRoleIds = roleIds,
        minionRoleIds = minionRoleIds,
        primaryStyleTag = primaryStyleTag,
        selectedDrunkShownRole = selectedDrunkShownRole,
    )

    private fun disjointRoleIds(playerCount: Int, age: Int): Set<String> =
        List(playerCount - 1) { index -> "disjoint-$age-$index" }.toSet()

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()
}
