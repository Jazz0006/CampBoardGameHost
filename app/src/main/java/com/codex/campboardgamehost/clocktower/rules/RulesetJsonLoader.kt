package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.json.JSONObject

internal object RulesetJsonLoader {
    fun parse(json: String): RulesetKnowledge {
        val root = JSONObject(json)
        val charactersJson = root.getJSONArray("characters")
        val characters = (0 until charactersJson.length()).map { index ->
            charactersJson.getJSONObject(index).let { character ->
                RuleCharacterText(
                    roleId = RoleId(character.getString("id")),
                    abilityText = character.getString("abilityText"),
                )
            }
        }
        val nightOrder = root.getJSONObject("nightOrder")
        val jinxesJson = root.getJSONArray("jinxes")
        return RulesetKnowledge(
            scriptId = ScriptId(root.getString("scriptId")),
            characters = characters,
            firstNightOrder = nightOrder.getJSONArray("firstNight").toRoleIds(),
            otherNightOrder = nightOrder.getJSONArray("otherNights").toRoleIds(),
            jinxes = (0 until jinxesJson.length()).map { index ->
                jinxesJson.getJSONObject(index).let { jinx ->
                    RuleJinx(
                        firstRoleId = RoleId(jinx.getString("firstRoleId")),
                        secondRoleId = RoleId(jinx.getString("secondRoleId")),
                        text = jinx.getString("text"),
                    )
                }
            },
        )
    }

    private fun org.json.JSONArray.toRoleIds(): List<RoleId> =
        (0 until length()).map { index -> RoleId(getString(index)) }
}
