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

internal class WerewolfActiveGameSaveValidator(
    private val roleRegistry: WerewolfRoleRegistry,
) {
    fun validate(
        assignedRoles: List<Role>,
        werewolfCount: Int,
        includeSeer: Boolean,
        includeWitch: Boolean,
        includeHunter: Boolean,
    ) {
        require(assignedRoles.isNotEmpty()) {
            "Werewolf persistence requires assigned roles."
        }
        require(werewolfCount > 0) {
            "Werewolf persistence requires at least one werewolf."
        }

        val roleIds = assignedRoles.map { role ->
            roleRegistry.roleIdFor(role)
                ?: throw IllegalArgumentException(
                    "Werewolf save contains an unsupported assigned role '$role'.",
                )
        }
        val actual = roleIds.groupingBy { it }.eachCount()
        val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
        val villagerCount = assignedRoles.size - werewolfCount - specialCount
        require(villagerCount >= 0) {
            "Werewolf mechanical setup exceeds assigned player count."
        }
        val expected = buildMap<WerewolfRoleId, Int> {
            put(WerewolfRoleIds.WEREWOLF, werewolfCount)
            if (includeSeer) put(WerewolfRoleIds.SEER, 1)
            if (includeWitch) put(WerewolfRoleIds.WITCH, 1)
            if (includeHunter) put(WerewolfRoleIds.HUNTER, 1)
            if (villagerCount > 0) put(WerewolfRoleIds.VILLAGER, villagerCount)
        }
        require(actual == expected) {
            "Werewolf assigned role deck does not match persisted mechanical setup."
        }
    }
}
