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
    return if (language == "en") role.enName else role.zhName
}
