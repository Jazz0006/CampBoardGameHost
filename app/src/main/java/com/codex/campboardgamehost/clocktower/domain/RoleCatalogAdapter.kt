package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.ClocktowerRole
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.clocktowerRolesForScript

internal fun clocktowerRoleDefinitionsForScript(script: ClocktowerScript): List<RoleDefinition> {
    val scriptId = script.toRecommendationScriptId()
    return clocktowerRolesForScript(script).map { role -> role.toRoleDefinition(scriptId) }
}

internal fun ClocktowerScript.toRecommendationScriptId(): ScriptId = ScriptId(
    when (this) {
        ClocktowerScript.TroubleBrewing -> "trouble_brewing"
        ClocktowerScript.NoGreaterJoy -> "no_greater_joy"
    },
)

private fun ClocktowerRole.toRoleDefinition(scriptId: ScriptId): RoleDefinition = RoleDefinition(
    id = RoleId(enName),
    alignment = when (team) {
        ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> Alignment.GOOD
        ClocktowerTeam.Minion, ClocktowerTeam.Demon -> Alignment.EVIL
    },
    type = when (team) {
        ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
        ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
        ClocktowerTeam.Minion -> CharacterType.MINION
        ClocktowerTeam.Demon -> CharacterType.DEMON
    },
    scriptIds = setOf(scriptId),
)
