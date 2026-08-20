package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptJsonParser
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptNormalizer
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.json.JSONObject

internal object RulesetJsonLoader {
    /**
     * Legacy built-in rules asset parser. Kept behavior-compatible while S0 establishes the new
     * script/catalog pipeline; production callers continue using this entry point until later R5.5
     * migration batches explicitly switch them.
     */
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

    /**
     * S0 strong-typed import seam for the official custom-script JSON format. Raw JSON is parsed,
     * resolved through the supplied character registry and normalized before it becomes runtime
     * content. UI/FlowPlanner code must consume the returned domain objects rather than JSON maps.
     */
    fun parseScript(
        json: String,
        requestedScriptId: ScriptId,
        registry: ClocktowerCharacterRegistry,
        source: ClocktowerScriptSource,
    ): ValidatedClocktowerRuleset = ClocktowerScriptNormalizer.normalize(
        parsed = ClocktowerScriptJsonParser.parse(json),
        requestedScriptId = requestedScriptId,
        registry = registry,
        requestedSource = source,
    )

    private fun org.json.JSONArray.toRoleIds(): List<RoleId> =
        (0 until length()).map { index -> RoleId(getString(index)) }
}
