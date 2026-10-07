package com.codex.campboardgamehost

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

/**
 * Resolves one exact role triple against the current legal script roles.
 *
 * Missing input is pending. Partial, duplicate, illegal or unresolvable identities are invalid.
 * Neither state is silently replaced with an arbitrary legal triple.
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
