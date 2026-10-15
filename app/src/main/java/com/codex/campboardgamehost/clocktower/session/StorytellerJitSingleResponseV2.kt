package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject

/**
 * AI-EVENT-JIT-1C: ONE current Host-legal choice on the wire; whole-game
 * reasoning and memory remain in the strategic context, never in the result ID.
 *
 * Not connected to a live Responses request yet. Decode is pure and must be
 * followed by the original Host adoption/publication action; it writes none.
 */
internal object StorytellerJitSingleResponseV2 {
    const val REQUEST_SCHEMA = "botc.whole-game-current-choice-v2"

    internal data class Accepted(
        val candidateId: String,
        val updatedStrategyMemory: HostOpeningStrategyMemoryV2,
    )

    private fun str() = JSONObject().put("type", "string")
    private fun integer() = JSONObject().put("type", "integer")
    private fun arr(item: JSONObject) = JSONObject().put("type", "array").put("items", item)
    private fun obj(vararg members: Pair<String, JSONObject>) = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject().apply { members.forEach { (k, v) -> put(k, v) } })
        .put("required", JSONArray(members.map { it.first }))
        .put("additionalProperties", false)

    /** Strict Responses JSON Schema: no remainingChoices array or extra decisions. */
    val responseSchema: JSONObject by lazy {
        obj(
            "schemaVersion" to integer(),
            "source" to obj(
                "gameId" to str(),
                "gameStateRevision" to integer(),
                "playerInputRevision" to integer(),
                "exclusiveGlobalSequence" to integer(),
                "phase" to str(),
                "round" to integer(),
                "decisionId" to str(),
                "memoryVersion" to integer(),
            ),
            "candidateId" to str(),
            "updatedStrategyMemory" to obj(
                "tensions" to arr(str()),
                "intentions" to arr(obj(
                    "trigger" to str(), "approach" to str(), "risk" to str())),
                "uncertainties" to arr(str()),
            ),
        )
    }

    private fun JSONObject.exact(vararg keys: String) {
        require(this.keys().asSequence().toSet() == keys.toSet()) {
            "Missing, extra or legacy bulk-response field."
        }
    }

    /** Small Host-owned source binding, not a replacement for full context input. */
    fun sourceJson(source: HostCurrentSingleDecisionJitV2.Source) = JSONObject()
        .put("gameId", source.gameId)
        .put("gameStateRevision", source.gameStateRevision)
        .put("playerInputRevision", source.playerInputRevision)
        .put("exclusiveGlobalSequence", source.exclusiveGlobalSequence)
        .put("phase", source.phase.name)
        .put("round", source.round)
        .put("decisionId", source.decisionId)
        .put("memoryVersion", source.memoryVersion)

    private fun parseMemory(raw: JSONObject): HostOpeningStrategyMemoryV2 {
        raw.exact("tensions", "intentions", "uncertainties")
        fun strings(key: String): List<String> {
            val array = raw.getJSONArray(key)
            require(array.length() <= 3)
            return (0 until array.length()).map { array.getString(it) }
        }
        val intentions = raw.getJSONArray("intentions")
        require(intentions.length() <= 3)
        val parsed = (0 until intentions.length()).map { i ->
            val value = intentions.getJSONObject(i)
            value.exact("trigger", "approach", "risk")
            HostOpeningStrategicIntentionV2(
                value.getString("trigger"), value.getString("approach"),
                value.getString("risk"))
        }
        return HostOpeningStrategyMemoryV2(strings("tensions"), parsed, strings("uncertainties"))
    }

    fun decode(
        requested: HostCurrentSingleDecisionJitV2.Scope,
        fresh: HostCurrentSingleDecisionJitV2.Scope,
        responseJson: String,
    ): Accepted {
        val raw = JSONObject(responseJson)
        raw.exact("schemaVersion", "source", "candidateId", "updatedStrategyMemory")
        require(raw.getInt("schemaVersion") == 2)
        val observed = raw.getJSONObject("source")
        observed.exact("gameId", "gameStateRevision", "playerInputRevision",
            "exclusiveGlobalSequence", "phase", "round", "decisionId", "memoryVersion")
        val expected = requested.source
        require(observed.getString("gameId") == expected.gameId &&
            observed.getLong("gameStateRevision") == expected.gameStateRevision &&
            observed.getLong("playerInputRevision") == expected.playerInputRevision &&
            observed.getLong("exclusiveGlobalSequence") == expected.exclusiveGlobalSequence &&
            observed.getString("phase") == expected.phase.name &&
            observed.getInt("round") == expected.round &&
            observed.getString("decisionId") == expected.decisionId &&
            observed.getInt("memoryVersion") == expected.memoryVersion) {
            "Response source does not match frozen Host request."
        }
        val candidate = raw.getString("candidateId")
        HostCurrentSingleDecisionJitV2.verifyProposal(
            requested, fresh, HostCurrentSingleDecisionJitV2.Proposal(requested.source, candidate))
        val memory = parseMemory(raw.getJSONObject("updatedStrategyMemory"))
        return Accepted(candidate, memory)
    }
}
