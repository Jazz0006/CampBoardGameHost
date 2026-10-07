package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory

internal data class SetupCoordinationRequest(
    val game: GameState,
    val roles: List<RoleDefinition>,
    val lockedDecisions: List<StorytellerDecision> = emptyList(),
    val history: CrossGameHistory = CrossGameHistory(),
)
