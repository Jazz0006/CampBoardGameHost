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
 * Returns the exact committed Demon bluff state that player presentation is allowed to consume.
 *
 * Setup recommendations may be evaluated before the Demon-info barrier, but they are not
 * player-visible presentation state until the selected legal triple has been committed.
 *
 * The unused recommendation arguments are retained only as a narrow transitional call contract
 * until the DLB-5 cleanup slice removes the old presentation shape.
 */
@Suppress("UNUSED_PARAMETER")
internal fun demonBluffRoleNamesForPresentation(
    automaticStorytellerInfo: Boolean,
    appliedRoleNames: List<String>,
    setupPlans: List<RecommendationPlan>,
    storytellerStyle: RecommendationStyle,
): List<String>? = appliedRoleNames.takeIf { it.isNotEmpty() }

internal fun demonBluffRoleNamesToCommitAtBarrier(
    isDemonInfoStep: Boolean,
    isRealAction: Boolean,
    committedRoleNames: List<String>,
    setupPlans: List<RecommendationPlan>,
    storytellerStyle: RecommendationStyle,
    legalRoles: List<ClocktowerRole>,
): List<String>? {
    if (!isDemonInfoStep || !isRealAction || committedRoleNames.isNotEmpty()) return null

    val selectedPlan = WeightedStableSelector.selectStyle(
        options = setupPlans,
        style = storytellerStyle,
        styleOf = RecommendationPlan::style,
    ) ?: return null
    val recommendedRoleNames = selectedPlan.decisions
        .filterIsInstance<StorytellerDecision.DemonBluffs>()
        .singleOrNull()
        ?.roles
        ?.map { it.value }

    return when (
        val resolution = resolveDemonBluffPresentation(
            recommendedRoleNames = recommendedRoleNames,
            legalRoles = legalRoles,
        )
    ) {
        is DemonBluffPresentationResolution.Ready -> resolution.roles.map { it.enName }
        DemonBluffPresentationResolution.Pending -> null
        is DemonBluffPresentationResolution.Invalid -> null
    }
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
