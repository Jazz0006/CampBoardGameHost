package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Localizes a canonical Clocktower role identity without parsing presentation labels. */
internal fun clocktowerRoleLabel(
    roleId: RoleId,
    language: String,
): String {
    val role = ClocktowerScript.values()
        .asSequence()
        .flatMap { script -> clocktowerRolesForScript(script).asSequence() }
        .firstOrNull { candidate -> candidate.enName == roleId.value }
        ?: return roleId.value
    return role.nameFor(language)
}

/**
 * Resolves a Host-table role label from the current game's PlayerCard role objects first.
 * This keeps Day presentation on the same localization authority as Night presentation and
 * only falls back to the catalog for defensive compatibility.
 */
internal fun clocktowerRoleLabel(
    roleId: RoleId,
    language: String,
    cards: List<PlayerCard>,
): String {
    val role = cards.asSequence()
        .flatMap { card -> sequenceOf(card.clocktowerRole, card.clocktowerShownRole) }
        .filterNotNull()
        .firstOrNull { candidate -> candidate.enName == roleId.value }
        ?: return clocktowerRoleLabel(roleId, language)
    return role.nameFor(language)
}
