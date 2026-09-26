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
        val actionTimelineJson = root.requiredArray("actionTimeline")
        validateActionTimeline(actionTimelineJson)
        val actionTimeline = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
            JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
                actionTimelineJson,
            ),
        )
        val observations = root.requiredArray("observations").mapObjects("observations") { value ->
            validateRecordedObservation(value)
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

    /**
     * Replay-specific strict validation. The shared persistence/epistemic decoders intentionally
     * retain compatibility behavior for older save surfaces, so SDE durable replay validates its
     * complete nested wire shape and primitive types before delegating semantic decoding to them.
     */
    private fun validateActionTimeline(timeline: JSONArray) {
        timeline.mapObjects("actionTimeline") { entry ->
            entry.requireExactKeys(setOf("fact", "point"), "actionTimeline entry")
            validateActionFact(entry.requiredObject("fact"))
            validateTimelinePoint(entry.requiredObject("point"), "actionTimeline point")
        }
    }

    private fun validateActionFact(fact: JSONObject) {
        val kind = fact.requiredString("kind")
        val expectedKeys = when (kind) {
            "poison", "protect", "attack", "execution", "death" ->
                setOf("actionId", "sequence", "kind", "targetSeat")
            "role-change" ->
                setOf("actionId", "sequence", "kind", "targetSeat", "role", "alignment", "type")
            "phase-advance" ->
                setOf("actionId", "sequence", "kind", "phase", "round")
            else -> throw IllegalArgumentException("Unknown action fact kind '$kind'.")
        }
        fact.requireExactKeys(expectedKeys, "action fact '$kind'")
        fact.requiredString("actionId")
        fact.requiredLong("sequence")
        when (kind) {
            "poison" -> fact.requiredNullableInt("targetSeat")
            "protect", "attack", "execution", "death" -> fact.requiredInt("targetSeat")
            "role-change" -> {
                fact.requiredInt("targetSeat")
                fact.requiredString("role")
                fact.requiredString("alignment")
                fact.requiredString("type")
            }
            "phase-advance" -> {
                fact.requiredString("phase")
                fact.requiredInt("round")
            }
        }
    }

    private fun validateRecordedObservation(observation: JSONObject) {
        observation.requireExactKeys(
            setOf(
                "recordId", "phase", "round", "sequence", "sourceSeat", "sourceAbility",
                "visibility", "recipientSeats", "reliability", "proposition", "schemaVersion",
                "timelineBinding",
            ),
            "recorded observation",
        )
        observation.requiredString("recordId")
        observation.requiredString("phase")
        observation.requiredInt("round")
        observation.requiredInt("sequence")
        observation.requiredNullableInt("sourceSeat")
        observation.requiredNullableString("sourceAbility")
        observation.requiredString("visibility")
        observation.requiredArray("recipientSeats").requireIntegers("recipientSeats")
        observation.requiredString("reliability")
        observation.requiredInt("schemaVersion")
        validateProposition(observation.requiredObject("proposition"), "proposition")

        val binding = observation.requiredObject("timelineBinding")
        binding.requireExactKeys(setOf("kind", "point"), "observation timelineBinding")
        require(binding.requiredString("kind") == "global") {
            "Replay observations require a global timeline binding."
        }
        validateTimelinePoint(binding.requiredObject("point"), "observation timeline point")
    }

    private fun validateTimelinePoint(point: JSONObject, owner: String) {
        point.requireExactKeys(setOf("phase", "round", "sequence", "globalSequence"), owner)
        point.requiredString("phase")
        point.requiredInt("round")
        point.requiredInt("sequence")
        point.requiredLong("globalSequence")
    }

    private fun validateProposition(proposition: JSONObject, owner: String) {
        val kind = proposition.requiredString("kind")
        val expectedKeys = when (kind) {
            "role-at", "shown-role-at" -> setOf("kind", "role", "seat")
            "alignment-at" -> setOf("alignment", "kind", "seat")
            "character-type-at" -> setOf("characterType", "kind", "seat")
            "alive-at" -> setOf("alive", "kind", "seat")
            "ability-state-at" -> setOf("abilityRole", "abilityState", "kind", "seat")
            "role-in-play" -> setOf("inPlay", "kind", "role")
            "player-count" -> setOf("kind", "value")
            "setup-profile" -> setOf("demons", "kind", "minions", "outsiders", "townsfolk")
            "any-of" -> setOf("alternatives", "kind")
            "all-of" -> setOf("kind", "propositions")
            "not" -> setOf("kind", "proposition")
            "numeric-result" -> setOf("kind", "metric", "sourceSeat", "subjectSeats", "value")
            "boolean-result" -> setOf("kind", "metric", "sourceSeat", "subjectSeats", "value")
            "grimoire-state" -> if (proposition.has("truthBinding")) {
                setOf("kind", "seats", "truthBinding")
            } else {
                setOf("kind", "seats")
            }
            else -> throw IllegalArgumentException("Unknown InformationProposition kind '$kind'.")
        }
        proposition.requireExactKeys(expectedKeys, "$owner '$kind'")

        when (kind) {
            "role-at", "shown-role-at" -> {
                proposition.requiredString("role")
                proposition.requiredInt("seat")
            }
            "alignment-at" -> {
                proposition.requiredString("alignment")
                proposition.requiredInt("seat")
            }
            "character-type-at" -> {
                proposition.requiredString("characterType")
                proposition.requiredInt("seat")
            }
            "alive-at" -> {
                proposition.requiredBoolean("alive")
                proposition.requiredInt("seat")
            }
            "ability-state-at" -> {
                proposition.requiredString("abilityRole")
                proposition.requiredString("abilityState")
                proposition.requiredInt("seat")
            }
            "role-in-play" -> {
                proposition.requiredBoolean("inPlay")
                proposition.requiredString("role")
            }
            "player-count" -> proposition.requiredInt("value")
            "setup-profile" -> {
                proposition.requiredInt("townsfolk")
                proposition.requiredInt("outsiders")
                proposition.requiredInt("minions")
                proposition.requiredInt("demons")
            }
            "any-of" -> proposition.requiredArray("alternatives").mapObjects("$owner.alternatives") {
                validateProposition(it, "$owner.alternatives")
            }
            "all-of" -> proposition.requiredArray("propositions").mapObjects("$owner.propositions") {
                validateProposition(it, "$owner.propositions")
            }
            "not" -> validateProposition(proposition.requiredObject("proposition"), "$owner.not")
            "numeric-result" -> {
                proposition.requiredString("metric")
                proposition.requiredInt("sourceSeat")
                proposition.requiredArray("subjectSeats").requireIntegers("$owner.subjectSeats")
                proposition.requiredInt("value")
            }
            "boolean-result" -> {
                proposition.requiredString("metric")
                proposition.requiredInt("sourceSeat")
                proposition.requiredArray("subjectSeats").requireIntegers("$owner.subjectSeats")
                proposition.requiredBoolean("value")
            }
            "grimoire-state" -> {
                if (proposition.has("truthBinding")) proposition.requiredString("truthBinding")
                proposition.requiredArray("seats").mapObjects("$owner.seats") { seat ->
                    validateGrimoireSeat(seat)
                }
            }
        }
    }

    private fun validateGrimoireSeat(seat: JSONObject) {
        seat.requireExactKeys(
            setOf("alive", "displayedRole", "ruleReminderTokens", "seat"),
            "grimoire seat",
        )
        seat.requiredBoolean("alive")
        seat.requiredString("displayedRole")
        seat.requiredInt("seat")
        seat.requiredArray("ruleReminderTokens").mapObjects("grimoire ruleReminderTokens") { token ->
            token.requireExactKeys(setOf("label", "occurrence", "scope", "sourceRole"), "grimoire token")
            token.requiredString("label")
            token.requiredInt("occurrence")
            token.requiredString("scope")
            token.requiredString("sourceRole")
        }
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

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        require(has(key) && !isNull(key)) { "$key is required." }
        return opt(key) as? Boolean ?: throw IllegalArgumentException("$key must be a boolean.")
    }

    private fun JSONObject.requiredNullableInt(key: String): Int? {
        require(has(key)) { "$key is required." }
        if (isNull(key)) return null
        return requiredInt(key)
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

    private fun JSONArray.requireIntegers(owner: String) {
        for (index in 0 until length()) {
            val value = opt(index)
            require(value is Byte || value is Short || value is Int || value is Long) {
                "$owner[$index] must be an integer."
            }
        }
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
