package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

internal object ClocktowerActiveSessionValidator {
    fun validateForRecoverySave(
        script: ClocktowerScriptDefinition,
        assignedRoleIds: List<RoleId>,
    ) {
        require(assignedRoleIds.isNotEmpty()) {
            "Clocktower persistence requires assigned role IDs."
        }
        val allowed = script.characterIds.toSet()
        require(assignedRoleIds.all { it in allowed }) {
            "Clocktower assigned roles do not belong to the selected script."
        }
    }
}
