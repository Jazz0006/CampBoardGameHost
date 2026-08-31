package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupPresetRotationScore(
    val weightedOverlap: Double,
    val baseNoveltyWeight: Double,
    val finalWeight: Double,
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
                finalWeight = 1.0,
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
        val baseNoveltyWeight = maxOf(MIN_BASE_NOVELTY_WEIGHT, 1.0 - weightedOverlap)

        return TroubleBrewingSetupPresetRotationScore(
            weightedOverlap = weightedOverlap,
            baseNoveltyWeight = baseNoveltyWeight,
            finalWeight = baseNoveltyWeight,
        )
    }

    fun scoreFinalWeight(
        candidate: TroubleBrewingSetupPreset,
        selectedDrunkShownRole: String?,
        recentSetupRotationHistory: TroubleBrewingSetupRotationHistory,
        historyWeights: List<Double>,
    ): TroubleBrewingSetupPresetRotationScore {
        val baseScore = scoreBaseNovelty(
            candidate = candidate,
            recentSetupRotationHistory = recentSetupRotationHistory,
            historyWeights = historyWeights,
        )
        val previousGame = recentSetupRotationHistory.recentGames.firstOrNull()

        val minionMultiplier = if (
            previousGame != null && candidate.minions.toSet() == previousGame.minionRoleIds
        ) {
            SAME_MINION_SET_MULTIPLIER
        } else {
            1.0
        }

        val primaryStyle = candidate.styleTags.firstOrNull()
        val styleRepeatCount = if (primaryStyle == null) {
            0
        } else {
            recentSetupRotationHistory.recentGames
                .take(MAX_STYLE_HISTORY_GAMES)
                .count { it.primaryStyleTag == primaryStyle }
        }
        val styleMultiplier = if (styleRepeatCount >= STYLE_REPEAT_TRIGGER_COUNT) {
            REPEATED_PRIMARY_STYLE_MULTIPLIER
        } else {
            1.0
        }

        val drunkMultiplier = if (
            DRUNK_EXTERNAL_ID in candidate.outsiders &&
            selectedDrunkShownRole != null &&
            previousGame?.selectedDrunkShownRole == selectedDrunkShownRole
        ) {
            SAME_DRUNK_SHOWN_ROLE_MULTIPLIER
        } else {
            1.0
        }

        val rawWeight = baseScore.baseNoveltyWeight *
            minionMultiplier *
            styleMultiplier *
            drunkMultiplier

        return baseScore.copy(
            finalWeight = maxOf(MIN_FINAL_WEIGHT, rawWeight),
        )
    }

    private fun TroubleBrewingSetupPreset.overlapWith(previousRoleIds: Set<String>): Double =
        nonDemonRoleIds().intersect(previousRoleIds).size.toDouble() / (playerCount - 1).toDouble()

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()

    private const val MIN_BASE_NOVELTY_WEIGHT = 0.20
    private const val SAME_MINION_SET_MULTIPLIER = 0.70
    private const val REPEATED_PRIMARY_STYLE_MULTIPLIER = 0.88
    private const val SAME_DRUNK_SHOWN_ROLE_MULTIPLIER = 0.40
    private const val MIN_FINAL_WEIGHT = 0.05
    private const val MAX_STYLE_HISTORY_GAMES = 5
    private const val STYLE_REPEAT_TRIGGER_COUNT = 2
    private const val DRUNK_EXTERNAL_ID = "drunk"
}
