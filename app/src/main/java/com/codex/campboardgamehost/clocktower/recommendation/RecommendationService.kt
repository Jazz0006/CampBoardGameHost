package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision

/** Single entry point used by the Android UI. */
internal object RecommendationService {
    fun recommend(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<RecommendationPlan> = RecommendationSearch.recommend(game, roleDefinitions)

    fun recommendConstrained(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision>,
    ): RecommendationSearch.ConstrainedResult = RecommendationSearch.recommendConstrained(
        game = game,
        roleDefinitions = roleDefinitions,
        lockedDecisions = lockedDecisions,
    )
}

internal sealed interface RecommendationUiState {
    data object Loading : RecommendationUiState

    data class Ready(val plans: List<RecommendationPlan>) : RecommendationUiState

    data object Empty : RecommendationUiState

    data class InvalidLocks(val failureCodes: List<String>) : RecommendationUiState

    data class Error(val message: String) : RecommendationUiState
}
