package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector

internal sealed interface DemonBluffPresentationResolution {
    data class Ready(val roles: List<ClocktowerRole>) : DemonBluffPresentationResolution

    data object Pending : DemonBluffPresentationResolution

    data class Invalid(
        val requestedRoleNames: List<String>,
        val unresolvedRoleNames: List<String>,
    ) : DemonBluffPresentationResolution
}

/**
 * Returns the exact Demon bluff recommendation that presentation is allowed to consume.
 *
 * AUTO uses the already-applied setup decision so presentation never outruns the state commit.
 * MANUAL has no setup-plan apply step, so it consumes the default BALANCED setup recommendation
 * directly once that recommendation is ready. Other setup decisions remain manual Storyteller
 * authority and are not applied by this projection.
 */
internal fun demonBluffRoleNamesForPresentation(
    automaticStorytellerInfo: Boolean,
    appliedRoleNames: List<String>,
    setupPlans: List<RecommendationPlan>,
    preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED,
): List<String>? {
    appliedRoleNames.takeIf { it.isNotEmpty() }?.let { return it }
    if (automaticStorytellerInfo) return null

    val selectedPlan = WeightedStableSelector.selectStyle(
        options = setupPlans,
        style = preferredManualStyle,
        styleOf = RecommendationPlan::style,
    ) ?: return null

    return selectedPlan.decisions
        .filterIsInstance<StorytellerDecision.DemonBluffs>()
        .singleOrNull()
        ?.roles
        ?.map { it.value }
}

/**
 * Resolves one exact recommended triple against current legal script roles.
 *
 * Missing recommendation is pending. Partial, duplicate, illegal or unresolvable identities are
 * invalid. Neither state is silently replaced with an arbitrary legal triple.
 */
internal fun resolveDemonBluffPresentation(
    recommendedRoleNames: List<String>?,
    legalRoles: List<ClocktowerRole>,
): DemonBluffPresentationResolution {
    val requested = recommendedRoleNames ?: return DemonBluffPresentationResolution.Pending
    if (requested.size != 3 || requested.distinct().size != 3) {
        return DemonBluffPresentationResolution.Invalid(
            requestedRoleNames = requested,
            unresolvedRoleNames = emptyList(),
        )
    }

    val legalByName = legalRoles.associateBy(ClocktowerRole::enName)
    val unresolved = requested.filterNot(legalByName::containsKey)
    if (unresolved.isNotEmpty()) {
        return DemonBluffPresentationResolution.Invalid(
            requestedRoleNames = requested,
            unresolvedRoleNames = unresolved,
        )
    }

    return DemonBluffPresentationResolution.Ready(requested.map(legalByName::getValue))
}
