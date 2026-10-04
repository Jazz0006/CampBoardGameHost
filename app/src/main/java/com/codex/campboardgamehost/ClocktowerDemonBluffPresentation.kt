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

internal fun toggleManualDemonBluffSelection(
    selectedRoleNames: List<String>,
    roleName: String,
    legalRoleNames: Set<String>,
): List<String> {
    if (roleName !in legalRoleNames) return selectedRoleNames
    if (roleName in selectedRoleNames) return selectedRoleNames - roleName
    if (selectedRoleNames.size >= 3) return selectedRoleNames
    return selectedRoleNames + roleName
}

internal fun manualDemonBluffSelectionReady(
    selectedRoleNames: List<String>,
    legalRoles: List<ClocktowerRole>,
): Boolean = resolveDemonBluffPresentation(
    recommendedRoleNames = selectedRoleNames,
    legalRoles = legalRoles,
) is DemonBluffPresentationResolution.Ready

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
