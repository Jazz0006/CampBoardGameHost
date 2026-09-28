package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan

internal sealed interface RecommendationUiState {
    data object Loading : RecommendationUiState

    data class Ready(val plans: List<RecommendationPlan>) : RecommendationUiState

    data object Empty : RecommendationUiState

    data class InvalidLocks(val failureCodes: List<String>) : RecommendationUiState

    data class Error(val message: String) : RecommendationUiState
}
