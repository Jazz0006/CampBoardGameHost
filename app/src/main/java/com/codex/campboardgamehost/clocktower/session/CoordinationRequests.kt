package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext

internal data class SetupCoordinationRequest(
    val game: GameState,
    val roles: List<RoleDefinition>,
    val lockedDecisions: List<StorytellerDecision> = emptyList(),
    val history: CrossGameHistory = CrossGameHistory(),
)

internal data class RegistrationResolutionRequest(
    val request: DynamicDecisionRequest,
    val context: SpecialRegistrationContext,
    val style: RecommendationStyle,
)
