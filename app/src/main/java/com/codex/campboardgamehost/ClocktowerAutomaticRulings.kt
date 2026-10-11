package com.codex.campboardgamehost

internal data class ClocktowerAutomaticRegistrationRuling(
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
)

internal fun clocktowerAutomaticMayorRulingShouldAdvance(
    allowRuleDeterminedAutoUi: Boolean,
    action: ClocktowerNightAction,
    selectedName: String?,
    automaticTargetName: String?,
): Boolean =
    allowRuleDeterminedAutoUi &&
        action in setOf(ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.DemonSuccessor) &&
        automaticTargetName != null &&
        selectedName == automaticTargetName
