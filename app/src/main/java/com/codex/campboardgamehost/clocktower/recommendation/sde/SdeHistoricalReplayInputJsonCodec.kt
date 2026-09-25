package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerSemanticHistoryPersistence
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import org.json.JSONArray
import org.json.JSONObject

internal object SdeHistoricalReplayInputJsonCodec {
    private val rootKeys = setOf(
        "schemaVersion", "gameId", "gameStateRevision", "playerInputRevision", "rulesetRef",
        "committedSetup", "playerNamesBySeat", "actionTimeline", "observations",
        "nextTimelineGlobalSequence",
    )

    fun encode(input: SdeHistoricalReplayInput): String = JSONObject().apply {
        put("schemaVersion", input.schemaVersion)
        put("gameId", input.gameId)
        put("gameStateRevision", input.gameStateRevision)
        put("playerInputRevision", input.playerInputRevision)
        put("rulesetRef", encodeRuleset(input.rulesetRef))
        put("committedSetup", encodeSetup(input.committedSetup))
        put("playerNamesBySeat", JSONArray(input.playerNamesBySeat))
        put("actionTimeline", ClocktowerSemanticHistoryPersistence.encodeActionTimeline(input.actionTimeline))
        put("observations", JSONArray().apply {
            input.observationLog.records.forEach { put(JSONObject(EpistemicSemanticJson.encode(it))) }
        })
        put("nextTimelineGlobalSequence", input.nextTimelineGlobalSequence)
    }.toString()

    fun decodeStrict(raw: String): SdeHistoricalReplayMaterialization {
        val root = try {
            JSONObject(raw)
        } catch (error: Exception) {
            throw IllegalArgumentException("Malformed SDE historical replay input.", error)
        }
        root.requireExactKeys(rootKeys, "replay input")
        val schemaVersion = root.requiredInt("schemaVersion")
        require(schemaVersion == SdeHistoricalReplayInput.CURRENT_SCHEMA_VERSION) {
            "Unsupported SDE historical replay input version $schemaVersion."
        }
        val actionTimeline = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
            JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
                root.requiredArray("actionTimeline"),
            ),
        )
        val observations = root.requiredArray("observations").mapObjects("observations") { value ->
            EpistemicSemanticJson.decodeRecordedEpistemicObservation(value.toString())
        }
        return SdeHistoricalReplayMaterialization(
            input = SdeHistoricalReplayInput(
                schemaVersion = schemaVersion,
                gameId = root.requiredString("gameId"),
                gameStateRevision = root.requiredLong("gameStateRevision"),
                playerInputRevision = root.requiredLong("playerInputRevision"),
                rulesetRef = decodeRuleset(root.requiredObject("rulesetRef")),
                committedSetup = decodeSetup(root.requiredObject("committedSetup")),
                playerNamesBySeat = root.requiredArray("playerNamesBySeat").mapStrings("playerNamesBySeat"),
                actionTimeline = actionTimeline,
                observationLog = EpistemicObservationLog(observations),
                nextTimelineGlobalSequence = root.requiredLong("nextTimelineGlobalSequence"),
            ),
            origin = SdeHistoricalReplayInputOrigin.DURABLE_EXPORT,
        )
    }

    private fun encodeRuleset(value: RulesetRef) = JSONObject().apply {
        put("scriptId", value.scriptId.value)
        put("scriptContentHash", value.scriptContentHash)
        put("rulesetVersion", value.rulesetVersion)
        put("sourceRevision", value.sourceRevision)
        put("coverage", value.coverage.name)
    }

    private fun decodeRuleset(json: JSONObject): RulesetRef {
        json.requireExactKeys(
            setOf("scriptId", "scriptContentHash", "rulesetVersion", "sourceRevision", "coverage"),
            "rulesetRef",
        )
        return RulesetRef(
            scriptId = ScriptId(json.requiredString("scriptId")),
            scriptContentHash = json.requiredString("scriptContentHash"),
            rulesetVersion = json.requiredString("rulesetVersion"),
            sourceRevision = json.requiredString("sourceRevision"),
            coverage = json.requiredEnum("coverage"),
        )
    }

    private fun encodeSetup(value: CommittedClocktowerSetup) = JSONObject().apply {
        put("scriptId", value.script.value)
        put("setupSeed", value.setupSeed)
        put("assignments", JSONArray().apply {
            value.assignments.forEach { assignment ->
                put(JSONObject().apply {
                    put("seat", assignment.seat)
                    put("actualRole", assignment.actualRole.value)
                    put("shownRole", assignment.shownRole.value)
                })
            }
        })
        put("provenance", JSONObject().apply {
            put("sourceKind", value.provenance.sourceKind.name)
            put("providerId", value.provenance.providerId)
            put("candidateId", value.provenance.candidateId ?: JSONObject.NULL)
        })
    }

    private fun decodeSetup(json: JSONObject): CommittedClocktowerSetup {
        json.requireExactKeys(setOf("scriptId", "setupSeed", "assignments", "provenance"), "committedSetup")
        val assignments = json.requiredArray("assignments").mapObjects("assignments") { assignment ->
            assignment.requireExactKeys(setOf("seat", "actualRole", "shownRole"), "assignment")
            CommittedSetupSeat(
                seat = assignment.requiredInt("seat"),
                actualRole = RoleId(assignment.requiredString("actualRole")),
                shownRole = RoleId(assignment.requiredString("shownRole")),
            )
        }
        val provenance = json.requiredObject("provenance").also {
            it.requireExactKeys(setOf("sourceKind", "providerId", "candidateId"), "provenance")
        }
        return CommittedClocktowerSetup(
            script = ScriptId(json.requiredString("scriptId")),
            setupSeed = json.requiredLong("setupSeed"),
            assignments = assignments,
            provenance = SetupProvenance(
                sourceKind = provenance.requiredEnum("sourceKind"),
                providerId = provenance.requiredString("providerId"),
                candidateId = provenance.requiredNullableString("candidateId"),
            ),
        )
    }

    private fun JSONObject.requireExactKeys(expected: Set<String>, owner: String) {
        val actual = keys().asSequence().toSet()
        require(actual == expected) {
            "$owner keys must exactly match the supported schema; missing=${expected - actual}, unknown=${actual - expected}."
        }
    }

    private fun JSONObject.requiredObject(key: String): JSONObject = optJSONObject(key)
        ?: throw IllegalArgumentException("$key must be an object.")

    private fun JSONObject.requiredArray(key: String): JSONArray = optJSONArray(key)
        ?: throw IllegalArgumentException("$key must be an array.")

    private fun JSONObject.requiredString(key: String): String {
        require(has(key) && !isNull(key)) { "$key is required." }
        val value = opt(key) as? String ?: throw IllegalArgumentException("$key must be a string.")
        require(value.isNotBlank()) { "$key cannot be blank." }
        return value
    }

    private fun JSONObject.requiredNullableString(key: String): String? {
        require(has(key)) { "$key is required." }
        if (isNull(key)) return null
        return requiredString(key)
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredLong(key)
        require(value in Int.MIN_VALUE..Int.MAX_VALUE) { "$key is outside Int range." }
        return value.toInt()
    }

    private fun JSONObject.requiredLong(key: String): Long {
        require(has(key) && !isNull(key)) { "$key is required." }
        val value = opt(key)
        require(value is Byte || value is Short || value is Int || value is Long) { "$key must be an integer." }
        return (value as Number).toLong()
    }

    private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(key: String): T {
        val value = requiredString(key)
        return enumValues<T>().firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown $key '$value'.")
    }

    private fun JSONArray.mapStrings(owner: String): List<String> = buildList {
        for (index in 0 until length()) {
            val value = opt(index) as? String
                ?: throw IllegalArgumentException("$owner[$index] must be a string.")
            require(value.isNotBlank()) { "$owner[$index] cannot be blank." }
            add(value)
        }
    }

    private fun <T> JSONArray.mapObjects(owner: String, transform: (JSONObject) -> T): List<T> = buildList {
        for (index in 0 until length()) {
            val value = optJSONObject(index)
                ?: throw IllegalArgumentException("$owner[$index] must be an object.")
            add(transform(value))
        }
    }
}
