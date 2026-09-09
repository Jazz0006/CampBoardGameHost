package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerRoleRevealAnchor(
    val targetSeat: Int,
    val roleId: RoleId,
)

internal data class ClocktowerRoleRevealChoiceProjection(
    val targetSeat: Int,
    val roleId: RoleId?,
    val displayLabel: String,
)

/**
 * The step-level proposition is the authoritative target-seat anchor. Its role may represent the
 * truthful/registered result and must not be substituted for an unreliable option's shown role.
 */
internal fun clocktowerRoleRevealAnchor(
    proposition: InformationProposition?,
    expectedSeat: Int?,
    seatCount: Int,
): ClocktowerRoleRevealAnchor? {
    if (expectedSeat == null || expectedSeat !in 1..seatCount) return null
    val roleAt = proposition as? InformationProposition.RoleAt ?: return null
    if (roleAt.seat != expectedSeat || roleAt.seat !in 1..seatCount) return null
    return ClocktowerRoleRevealAnchor(roleAt.seat, roleAt.role)
}

/**
 * Preserve an existing display option as an opaque choice. A typed RoleAt, when present, must agree
 * with the anchored target; a null proposition is valid for unreliable information and is never
 * reconstructed from localized display text.
 */
internal fun clocktowerRoleRevealChoiceProjection(
    option: ClocktowerDisplayOption,
    targetSeat: Int,
    seatCount: Int,
): ClocktowerRoleRevealChoiceProjection? {
    if (targetSeat !in 1..seatCount) return null
    val roleAt = when (val proposition = option.proposition) {
        null -> null
        is InformationProposition.RoleAt -> proposition
        else -> return null
    }
    if (roleAt != null && (roleAt.seat != targetSeat || roleAt.seat !in 1..seatCount)) return null
    val displayLabel = option.displayPrimary?.takeIf { it.isNotBlank() } ?: return null
    return ClocktowerRoleRevealChoiceProjection(
        targetSeat = targetSeat,
        roleId = roleAt?.role,
        displayLabel = displayLabel,
    )
}
