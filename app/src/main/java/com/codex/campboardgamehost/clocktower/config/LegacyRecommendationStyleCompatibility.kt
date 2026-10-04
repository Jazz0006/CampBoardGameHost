package com.codex.campboardgamehost.clocktower.config

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle

/**
 * Temporary compatibility seam for recommendation families that have not yet migrated away from
 * the legacy global GENTLE / BALANCED / AGGRESSIVE dimension.
 *
 * This value preserves current pre-RSR behavior only. It is not Storyteller experience-mode
 * semantics, it is not PlayerExperience NORMAL semantics, and new recommendation policies must not
 * depend on it.
 */
internal object LegacyRecommendationStyleCompatibility {
    val automatic: RecommendationStyle = RecommendationStyle.AGGRESSIVE
}
