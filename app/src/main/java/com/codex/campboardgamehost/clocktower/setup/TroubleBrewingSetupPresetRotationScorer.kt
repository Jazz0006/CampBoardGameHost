package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupPresetRotationScore(
    val weightedOverlap: Double,
    val baseNoveltyWeight: Double,
)

internal object TroubleBrewingSetupPresetRotationScorer {
    fun scoreBaseNovelty(
        candidate: TroubleBrewingSetupPreset,
        recentSetupRotationHistory: TroubleBrewingSetupRotationHistory,
        historyWeights: List<Double>,
    ): TroubleBrewingSetupPresetRotationScore {
        val recentGames = recentSetupRotationHistory.recentGames.take(historyWeights.size)
        if (recentGames.isEmpty()) {
            return TroubleBrewingSetupPresetRotationScore(
                weightedOverlap = 0.0,
                baseNoveltyWeight = 1.0,
            )
        }

        val availableWeights = historyWeights.take(recentGames.size)
        require(availableWeights.all { it >= 0.0 }) {
            "Trouble Brewing history weights must be non-negative."
        }
        val denominator = availableWeights.sum()
        require(denominator > 0.0) {
            "Trouble Brewing available history weights must have positive total weight."
        }

        val weightedOverlap = recentGames.indices.sumOf { age ->
            candidate.overlapWith(recentGames[age].realNonDemonRoleIds) * availableWeights[age]
        } / denominator

        return TroubleBrewingSetupPresetRotationScore(
            weightedOverlap = weightedOverlap,
            baseNoveltyWeight = maxOf(MIN_BASE_NOVELTY_WEIGHT, 1.0 - weightedOverlap),
        )
    }

    private fun TroubleBrewingSetupPreset.overlapWith(previousRoleIds: Set<String>): Double =
        nonDemonRoleIds().intersect(previousRoleIds).size.toDouble() / (playerCount - 1).toDouble()

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()

    private const val MIN_BASE_NOVELTY_WEIGHT = 0.20
}
