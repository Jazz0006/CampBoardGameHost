package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision

/**
 * Owns the mutable-lock boundary for setup recommendations.
 *
 * Committed setup facts, including the Drunk's shown identity, are consumed through GameState and
 * must never be reconstructed as mutable recommendation locks.
 */
internal object SetupRecommendationLockPolicy {
    fun initialLocks(): List<StorytellerDecision> = emptyList()

    fun replaceWith(decisions: List<StorytellerDecision>): List<StorytellerDecision> =
        decisions.filterNot { it is StorytellerDecision.DrunkShownRole }

    fun clear(): List<StorytellerDecision> = emptyList()
}
