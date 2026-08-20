package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

internal data class RuleCharacterText(
    val roleId: RoleId,
    val abilityText: String,
)

internal data class RuleJinx(
    val firstRoleId: RoleId,
    val secondRoleId: RoleId,
    val text: String,
)

internal data class RulesetKnowledge(
    val scriptId: ScriptId,
    val characters: List<RuleCharacterText>,
    val firstNightOrder: List<RoleId>,
    val otherNightOrder: List<RoleId>,
    val jinxes: List<RuleJinx>,
) {
    init {
        require(characters.isNotEmpty()) { "Ruleset knowledge must contain characters." }
        require(characters.map { it.roleId }.distinct().size == characters.size) {
            "Ruleset character IDs must be unique."
        }
        require(characters.all { it.abilityText.isNotBlank() }) {
            "Ruleset character text cannot be blank."
        }
    }
}
