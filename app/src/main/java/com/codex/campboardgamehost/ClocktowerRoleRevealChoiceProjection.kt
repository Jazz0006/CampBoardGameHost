package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerRoleRevealAnchor(
    val targetSeat: Int,
    val roleId: RoleId,
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
