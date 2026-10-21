package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/** A single actually chosen role for an already identified target. No Host-generated answer grid. */
internal data class ClocktowerDirectRoleRevealChoice(
    val targetSeat: Int,
    val roleId: RoleId,
    val displayLabel: String,
    val displayOption: ClocktowerDisplayOption,
)

internal fun clocktowerDirectRoleRevealChoice(
    step: ClocktowerNightStepUi,
    targetSeat: Int?,
    seatCount: Int,
    selectedRoleId: String,
    scriptRoles: List<ClocktowerRole>,
    roleLabel: (String) -> String,
): ClocktowerDirectRoleRevealChoice? {
    val question = step.informationQuestion as? ClocktowerInformationQuestion.Role ?: return null
    if (step.roleEnName !in setOf("Undertaker", "Ravenkeeper") ||
        targetSeat == null || targetSeat !in 1..seatCount || question.subjectSeat != targetSeat ||
        scriptRoles.none { it.enName == selectedRoleId }
    ) return null
    val shown = roleLabel(selectedRoleId)
    return ClocktowerDirectRoleRevealChoice(
        targetSeat = targetSeat,
        roleId = RoleId(selectedRoleId),
        displayLabel = shown,
        displayOption = ClocktowerDisplayOption(
            label = shown,
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = step.displayTitle,
            displayPrimary = shown,
            displaySecondary = null,
            displayFooter = "",
            proposition = InformationProposition.RoleAt(targetSeat, RoleId(selectedRoleId)),
            presentationSubjectSeats = listOf(targetSeat),
            presentationRoleId = selectedRoleId,
            isTruthful = false,
        ),
    )
}

/** A fresh AI typed role may be shown without matching any manual candidate list. */
internal fun clocktowerDirectRecommendedRoleReveal(
    step: ClocktowerNightStepUi,
    targetSeat: Int?,
    seatCount: Int,
    scriptRoles: List<ClocktowerRole>,
    recommended: ClocktowerDisplayOption?,
): ClocktowerDirectRoleRevealChoice? {
    val roleAt = recommended?.proposition as? InformationProposition.RoleAt ?: return null
    if (recommended.displayKind != ClocktowerDisplayKind.RoleReveal ||
        targetSeat != roleAt.seat || targetSeat !in 1..seatCount ||
        (step.informationQuestion as? ClocktowerInformationQuestion.Role)?.subjectSeat != targetSeat ||
        scriptRoles.none { it.enName == roleAt.role.value }
    ) return null
    return ClocktowerDirectRoleRevealChoice(
        targetSeat = roleAt.seat,
        roleId = roleAt.role,
        displayLabel = recommended.displayPrimary ?: roleAt.role.value,
        displayOption = recommended,
    )
}
