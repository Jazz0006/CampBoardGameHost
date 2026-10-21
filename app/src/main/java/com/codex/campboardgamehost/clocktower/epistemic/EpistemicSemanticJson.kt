package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONArray
import org.json.JSONObject

/** Canonical, locale-independent JSON for actual observations and information propositions. */
object EpistemicSemanticJson {
    fun encode(value: InformationProposition): String = canonical(proposition(value))
    fun encode(value: RecordedEpistemicObservation): String = canonical(recordedObservation(value))

    fun decodeInformationProposition(json: String): InformationProposition = proposition(JSONObject(json))
    fun decodeRecordedEpistemicObservation(json: String): RecordedEpistemicObservation = recordedObservation(checkedRoot(json))

    private fun proposition(value: InformationProposition): Map<String, Any?> = when (value) {
        is InformationProposition.RoleAt -> mapOf("kind" to "role-at", "role" to value.role.value, "seat" to value.seat)
        is InformationProposition.ShownRoleAt -> mapOf("kind" to "shown-role-at", "role" to value.role.value, "seat" to value.seat)
        is InformationProposition.AlignmentAt -> mapOf("alignment" to value.alignment.name, "kind" to "alignment-at", "seat" to value.seat)
        is InformationProposition.CharacterTypeAt -> mapOf("characterType" to value.characterType.name, "kind" to "character-type-at", "seat" to value.seat)
        is InformationProposition.AliveAt -> mapOf("alive" to value.alive, "kind" to "alive-at", "seat" to value.seat)
        is InformationProposition.AbilityStateAt -> mapOf("abilityRole" to value.abilityRole.value, "abilityState" to value.abilityState.name, "kind" to "ability-state-at", "seat" to value.seat)
        is InformationProposition.RoleInPlay -> mapOf("inPlay" to value.inPlay, "kind" to "role-in-play", "role" to value.role.value)
        is InformationProposition.PlayerCount -> mapOf("kind" to "player-count", "value" to value.value)
        is InformationProposition.SetupProfile -> mapOf("demons" to value.demons, "kind" to "setup-profile", "minions" to value.minions, "outsiders" to value.outsiders, "townsfolk" to value.townsfolk)
        is InformationProposition.AnyOf -> mapOf("alternatives" to value.alternatives.map(::proposition), "kind" to "any-of")
        is InformationProposition.AllOf -> mapOf("kind" to "all-of", "propositions" to value.propositions.map(::proposition))
        is InformationProposition.Not -> mapOf("kind" to "not", "proposition" to proposition(value.proposition))
        is InformationProposition.NumericResult -> mapOf("kind" to "numeric-result", "metric" to value.metric.name, "sourceSeat" to value.sourceSeat, "subjectSeats" to value.subjectSeats, "value" to value.value)
        is InformationProposition.BooleanResult -> mapOf("kind" to "boolean-result", "metric" to value.metric.name, "sourceSeat" to value.sourceSeat, "subjectSeats" to value.subjectSeats, "value" to value.value)
    }

    private fun recordedObservation(value: RecordedEpistemicObservation): Map<String, Any?> = mapOf(
        "phase" to value.phase.name, "proposition" to proposition(value.proposition),
        "recipientSeats" to value.recipientSeats.sorted(), "recordId" to value.recordId,
        "reliability" to value.reliability.name, "round" to value.round, "schemaVersion" to value.schemaVersion,
        "sequence" to value.sequence, "sourceAbility" to value.sourceAbility?.value,
        "sourceSeat" to value.sourceSeat, "visibility" to value.visibility.name,
    ) + timelineBindingFields(value.timelineBinding)

    private fun timelineBindingFields(value: ObservationTimelineBinding): Map<String, Any?> = when (value) {
        ObservationTimelineBinding.LegacyLocal -> emptyMap()
        is ObservationTimelineBinding.Global -> mapOf(
            "timelineBinding" to mapOf(
                "kind" to "global",
                "point" to timelinePoint(value.point),
            ),
        )
    }

    private fun timelinePoint(value: TimelinePoint): Map<String, Any?> = mapOf(
        "globalSequence" to value.globalSequence, "phase" to value.phase.name,
        "round" to value.round, "sequence" to value.sequence,
    )

    private fun proposition(json: JSONObject): InformationProposition = when (json.getString("kind")) {
        "role-at" -> InformationProposition.RoleAt(json.getInt("seat"), RoleId(json.getString("role")))
        "shown-role-at" -> InformationProposition.ShownRoleAt(json.getInt("seat"), RoleId(json.getString("role")))
        "alignment-at" -> InformationProposition.AlignmentAt(json.getInt("seat"), Alignment.valueOf(json.getString("alignment")))
        "character-type-at" -> InformationProposition.CharacterTypeAt(json.getInt("seat"), CharacterType.valueOf(json.getString("characterType")))
        "alive-at" -> InformationProposition.AliveAt(json.getInt("seat"), json.getBoolean("alive"))
        "ability-state-at" -> InformationProposition.AbilityStateAt(json.getInt("seat"), RoleId(json.getString("abilityRole")), AbilityState.valueOf(json.getString("abilityState")))
        "role-in-play" -> InformationProposition.RoleInPlay(RoleId(json.getString("role")), json.getBoolean("inPlay"))
        "player-count" -> InformationProposition.PlayerCount(json.getInt("value"))
        "setup-profile" -> InformationProposition.SetupProfile(json.getInt("townsfolk"), json.getInt("outsiders"), json.getInt("minions"), json.getInt("demons"))
        "any-of" -> InformationProposition.AnyOf(json.getJSONArray("alternatives").objects().map(::proposition))
        "all-of" -> InformationProposition.AllOf(json.getJSONArray("propositions").objects().map(::proposition))
        "not" -> InformationProposition.Not(proposition(json.getJSONObject("proposition")))
        "numeric-result" -> InformationProposition.NumericResult(NumericMetric.valueOf(json.getString("metric")), json.getInt("sourceSeat"), json.getJSONArray("subjectSeats").ints(), json.getInt("value"))
        "boolean-result" -> InformationProposition.BooleanResult(BooleanMetric.valueOf(json.getString("metric")), json.getInt("sourceSeat"), json.getJSONArray("subjectSeats").ints(), json.getBoolean("value"))
        else -> error("Unknown InformationProposition kind: ${json.getString("kind")}")
    }

    private fun recordedObservation(json: JSONObject): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = json.getString("recordId"), phase = StorytellerPhase.valueOf(json.getString("phase")),
        round = json.getInt("round"), sequence = json.getInt("sequence"),
        sourceSeat = json.nullableInt("sourceSeat"), sourceAbility = json.nullableString("sourceAbility")?.let(::RoleId),
        visibility = ObservationVisibility.valueOf(json.getString("visibility")),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        reliability = ObservationReliability.valueOf(json.getString("reliability")),
        proposition = proposition(json.getJSONObject("proposition")), schemaVersion = json.getInt("schemaVersion"),
        timelineBinding = observationTimelineBinding(json),
    )

    private fun observationTimelineBinding(json: JSONObject): ObservationTimelineBinding {
        if (!json.has("timelineBinding")) return ObservationTimelineBinding.LegacyLocal
        require(!json.isNull("timelineBinding")) {
            "timelineBinding cannot be null when present."
        }
        val binding = json.getJSONObject("timelineBinding")
        return when (binding.getString("kind")) {
            "global" -> ObservationTimelineBinding.Global(timelinePoint(binding.getJSONObject("point")))
            else -> throw IllegalArgumentException("Unknown observation timeline binding kind: ${binding.getString("kind")}")
        }
    }

    private fun timelinePoint(json: JSONObject): TimelinePoint {
        require(json.has("globalSequence") && !json.isNull("globalSequence")) {
            "Legacy schema-v2 TimelinePoint without globalSequence requires explicit migration; " +
                "globalSequence cannot be inferred from local sequence."
        }
        return TimelinePoint(
            phase = StorytellerPhase.valueOf(json.getString("phase")),
            round = json.getInt("round"),
            sequence = json.getInt("sequence"),
            globalSequence = json.getLong("globalSequence"),
        )
    }
    private fun checkedRoot(json: String): JSONObject = JSONObject(json).also {
        requireSchemaVersion(it.getInt("schemaVersion"))
    }

    private fun canonical(value: Any?): String = when (value) {
        null -> "null"
        is String -> JSONObject.quote(value)
        is Boolean, is Int, is Long -> value.toString()
        is Map<*, *> -> value.entries.map { (key, item) -> requireNotNull(key) as String to item }.sortedBy { it.first }
            .joinToString(prefix = "{", postfix = "}") { (key, item) -> "${JSONObject.quote(key)}:${canonical(item)}" }
        is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { canonical(it) }
        else -> error("Unsupported canonical JSON value: ${value::class.java.name}")
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length(), ::getJSONObject)
    private fun JSONArray.ints(): List<Int> = List(length(), ::getInt)
    private fun JSONArray.strings(): List<String> = List(length(), ::getString)
    private fun JSONObject.nullableString(key: String): String? = if (!has(key) || isNull(key)) null else getString(key)
    private fun JSONObject.nullableInt(key: String): Int? = if (!has(key) || isNull(key)) null else getInt(key)
}
