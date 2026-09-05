package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle

internal sealed interface DemonBluffPresentationResolution {
    data class Ready(val roles: List<ClocktowerRole>) : DemonBluffPresentationResolution

    data object Pending : DemonBluffPresentationResolution

    data class Invalid(
        val requestedRoleNames: List<String>,
        val unresolvedRoleNames: List<String>,
    ) : DemonBluffPresentationResolution
}

/**
 * Chooses the role-name source that the Demon presentation should consume.
 *
 * This initial characterization preserves the current production behavior: only a previously
 * applied recommendation can supply role names. The hotfix regression test intentionally proves
 * that MANUAL mode needs a stronger contract.
 */
internal fun demonBluffRoleNamesForPresentation(
    automaticStorytellerInfo: Boolean,
    appliedRoleNames: List<String>,
    setupPlans: List<RecommendationPlan>,
    preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED,
): List<String>? = appliedRoleNames.takeIf { it.isNotEmpty() }

/**
 * Resolves the requested recommendation against the current legal script roles.
 *
 * This initial characterization intentionally mirrors the legacy silent fallback so the RED test
 * can prove that malformed or unavailable recommendation state must not become the first three
 * legal roles.
 */
internal fun resolveDemonBluffPresentation(
    recommendedRoleNames: List<String>?,
    legalRoles: List<ClocktowerRole>,
): DemonBluffPresentationResolution {
    val applied = recommendedRoleNames.orEmpty()
        .mapNotNull { roleName -> legalRoles.firstOrNull { it.enName == roleName } }
        .distinctBy(ClocktowerRole::enName)
    val roles = if (applied.size == 3) applied else legalRoles.take(3)
    return DemonBluffPresentationResolution.Ready(roles)
}
