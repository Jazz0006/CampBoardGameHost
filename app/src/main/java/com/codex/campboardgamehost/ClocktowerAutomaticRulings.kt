package com.codex.campboardgamehost

internal data class ClocktowerAutomaticRegistrationRuling(
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
)

internal fun clocktowerAutomaticMayorRulingShouldAdvance(
    automaticStorytellerInfo: Boolean,
    action: ClocktowerNightAction,
    selectedName: String?,
    automaticTargetName: String?,
): Boolean =
    automaticStorytellerInfo &&
        action in setOf(ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.DemonSuccessor) &&
        automaticTargetName != null &&
        selectedName == automaticTargetName
