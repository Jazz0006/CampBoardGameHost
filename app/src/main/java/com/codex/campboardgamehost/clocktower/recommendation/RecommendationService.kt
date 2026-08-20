package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

/** Single entry point used by the Android UI. */
internal object RecommendationService {
    fun recommend(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        history: CrossGameHistory = CrossGameHistory(),
    ): List<RecommendationPlan> = SetupRecommendationService.recommend(game, roleDefinitions, history)

    fun recommendConstrained(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision>,
        history: CrossGameHistory = CrossGameHistory(),
    ): SetupRecommendationService.ConstrainedResult = SetupRecommendationService.recommendConstrained(
        game = game,
        roleDefinitions = roleDefinitions,
        lockedDecisions = lockedDecisions,
        history = history,
    )
}

internal sealed interface RecommendationUiState {
    data object Loading : RecommendationUiState

    data class Ready(val plans: List<RecommendationPlan>) : RecommendationUiState

    data object Empty : RecommendationUiState

    data class InvalidLocks(val failureCodes: List<String>) : RecommendationUiState

    data class Error(val message: String) : RecommendationUiState
}
