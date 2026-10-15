package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI-EVENT-JIT-1E: wire SHAPE for one expert-generated direct information result.
 *
 * Unlike the older candidateId schema this does NOT send a Host-enumerated
 * candidate list to the model. Host MUST independently validate actual rules,
 * current player/ability state, and (where relevant) registration witnesses
 * AFTER decode and BEFORE recommending/adopting/publishing any result.
 *
 * This is a pure experimental contract: NOT wired to paid Responses or UI.
 */
internal object StorytellerJitDirectResultV2 {
    const val REQUEST_SCHEMA = "botc.current-expert-direct-result-v1"

    internal enum class Kind { NUMBER, BOOLEAN, PAIR, PAIR_NONE, ROLE, SEAT }

    internal data class Source(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val phase: StorytellerPhase,
        val round: Int,
        val decisionId: String,
        val memoryVersion: Int,
    ) {
        init {
            require(gameId.isNotBlank() && decisionId.isNotBlank())
            require(gameStateRevision >= 0 && playerInputRevision >= 0 &&
                exclusiveGlobalSequence >= 0 && round > 0 && memoryVersion >= 0)
        }
    }

    internal data class Result(
        val kind: Kind,
        val number: Int?,
        val boolean: Boolean?,
        val roleId: String?,
        val seats: List<Int>,
        val targetSeat: Int?,
    )

    /** Unvalidated means exactly that: structural JSON is NOT Host game legality. */
    internal data class Unvalidated(
        val result: Result,
        val memoryUpdate: HostOpeningStrategyMemoryV2?,
    )

    fun sourceFrom(
        state: ClocktowerSessionState,
        phase: StorytellerPhase,
        round: Int,
        decisionId: String,
        memoryVersion: Int,
    ) = Source(
        state.gameId, state.gameStateRevision, state.playerInputRevision,
        state.nextTimelineGlobalSequence, phase, round, decisionId, memoryVersion)

    private fun typed(type: String) = JSONObject().put("type", type)
    private fun nullable(type: String) =
        JSONObject().put("type", JSONArray(listOf(type, "null")))
    private fun obj(vararg members: Pair<String, JSONObject>) = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject().apply { members.forEach { (key, schema) ->
            put(key, schema)
        } })
        .put("required", JSONArray(members.map { it.first }))
        .put("additionalProperties", false)

    /** Strict structured output: exactly ONE direct result, no candidate IDs. */
    val responseSchema: JSONObject by lazy {
        val memory = obj(
            "tensions" to JSONObject().put("type", "array")
                .put("items", typed("string")).put("maxItems", 3),
            "intentions" to JSONObject().put("type", "array")
                .put("items", obj(
                    "trigger" to typed("string"), "approach" to typed("string"),
                    "risk" to typed("string"))).put("maxItems", 3),
            "uncertainties" to JSONObject().put("type", "array")
                .put("items", typed("string")).put("maxItems", 3),
        ).put("type", JSONArray(listOf("object", "null")))
        obj(
            "schemaVersion" to typed("integer"),
            "source" to obj(
                "gameId" to typed("string"),
                "gameStateRevision" to typed("integer"),
                "playerInputRevision" to typed("integer"),
                "exclusiveGlobalSequence" to typed("integer"),
                "phase" to typed("string"),
                "round" to typed("integer"),
                "decisionId" to typed("string"),
                "memoryVersion" to typed("integer")),
            "result" to obj(
                "kind" to typed("string").put("enum", JSONArray(Kind.entries.map { it.name })),
                "number" to nullable("integer"),
                "boolean" to nullable("boolean"),
                "roleId" to nullable("string"),
                "seats" to JSONObject().put("type", "array").put("items", typed("integer")),
                "targetSeat" to nullable("integer")),
            "strategyMemoryUpdate" to memory,
        )
    }

    fun sourceJson(s: Source): JSONObject = JSONObject()
        .put("gameId", s.gameId)
        .put("gameStateRevision", s.gameStateRevision)
        .put("playerInputRevision", s.playerInputRevision)
        .put("exclusiveGlobalSequence", s.exclusiveGlobalSequence)
        .put("phase", s.phase.name)
        .put("round", s.round)
        .put("decisionId", s.decisionId)
        .put("memoryVersion", s.memoryVersion)

    private fun JSONObject.exact(vararg expected: String) {
        require(keys().asSequence().toSet() == expected.toSet()) {
            "Missing, extra, or legacy candidateId/remainingChoices field."
        }
    }

    private fun readMemory(value: JSONObject): HostOpeningStrategyMemoryV2 {
        value.exact("tensions", "intentions", "uncertainties")
        fun strings(key: String): List<String> {
            val array = value.getJSONArray(key)
            require(array.length() <= 3)
            return (0 until array.length()).map { index ->
                array.getString(index).also { require(it.length <= 400) }
            }
        }
        val intentions = value.getJSONArray("intentions")
        require(intentions.length() <= 3)
        val parsed = (0 until intentions.length()).map { index ->
            intentions.getJSONObject(index).let { item ->
                item.exact("trigger", "approach", "risk")
                HostOpeningStrategicIntentionV2(
                    item.getString("trigger").also { require(it.length <= 400) },
                    item.getString("approach").also { require(it.length <= 400) },
                    item.getString("risk").also { require(it.length <= 400) })
            }
        }
        return HostOpeningStrategyMemoryV2(strings("tensions"), parsed, strings("uncertainties"))
    }

    /**
     * Decode binding/shape only. NEVER promote provider anchor or commit Host
     * observations based solely on this method.
     */
    fun decode(requested: Source, fresh: Source, text: String): Unvalidated {
        require(requested == fresh) { "Host changed while model response was in flight." }
        val root = JSONObject(text)
        root.exact("schemaVersion", "source", "result", "strategyMemoryUpdate")
        require(root.getInt("schemaVersion") == 1)
        val source = root.getJSONObject("source")
        source.exact("gameId", "gameStateRevision", "playerInputRevision",
            "exclusiveGlobalSequence", "phase", "round", "decisionId", "memoryVersion")
        require(source.getString("gameId") == requested.gameId &&
            source.getLong("gameStateRevision") == requested.gameStateRevision &&
            source.getLong("playerInputRevision") == requested.playerInputRevision &&
            source.getLong("exclusiveGlobalSequence") == requested.exclusiveGlobalSequence &&
            source.getString("phase") == requested.phase.name &&
            source.getInt("round") == requested.round &&
            source.getString("decisionId") == requested.decisionId &&
            source.getInt("memoryVersion") == requested.memoryVersion) {
            "Response identity differs from Host request."
        }
        val raw = root.getJSONObject("result")
        raw.exact("kind", "number", "boolean", "roleId", "seats", "targetSeat")
        val kind = Kind.valueOf(raw.getString("kind"))
        val number = if (raw.isNull("number")) null else raw.getInt("number")
        val boolean = if (raw.isNull("boolean")) null else raw.getBoolean("boolean")
        val role = if (raw.isNull("roleId")) null else raw.getString("roleId")
        val target = if (raw.isNull("targetSeat")) null else raw.getInt("targetSeat")
        val seatsArray = raw.getJSONArray("seats")
        val seats = (0 until seatsArray.length()).map { seatsArray.getInt(it) }
        require(seats.all { it > 0 } && seats.distinct().size == seats.size)
        require(role == null || (role.isNotBlank() && role.length <= 100))
        require(target == null || target > 0)
        when (kind) {
            Kind.NUMBER -> require(number != null && number >= 0 &&
                boolean == null && role == null && seats.isEmpty() && target == null)
            Kind.BOOLEAN -> require(number == null && boolean != null &&
                role == null && seats.isEmpty() && target == null)
            Kind.PAIR -> require(number == null && boolean == null &&
                role != null && seats.size == 2 && target == null)
            Kind.PAIR_NONE -> require(number == null && boolean == null &&
                role == null && seats.isEmpty() && target == null)
            Kind.ROLE -> require(number == null && boolean == null &&
                role != null && seats.isEmpty() && target == null)
            Kind.SEAT -> require(number == null && boolean == null &&
                role == null && seats.isEmpty() && target != null)
        }
        val memory = if (root.isNull("strategyMemoryUpdate")) null else
            readMemory(root.getJSONObject("strategyMemoryUpdate"))
        return Unvalidated(Result(kind, number, boolean, role, seats, target), memory)
    }
}
