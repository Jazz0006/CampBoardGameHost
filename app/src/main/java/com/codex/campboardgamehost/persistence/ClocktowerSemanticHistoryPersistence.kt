package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.json.JSONArray
import org.json.JSONObject

/** Active-game JSON contract for explicit Clocktower semantic-history metadata. */
internal object ClocktowerSemanticHistoryPersistence {
    const val MODE_KEY = "clocktowerSemanticHistoryMode"
    const val CURSOR_KEY = "clocktowerNextTimelineGlobalSequence"
    const val ACTION_TIMELINE_KEY = "clocktowerActionTimeline"

    fun encode(mode: ClocktowerSemanticHistoryMode): String = mode.name

    fun decodeMode(json: JSONObject): ClocktowerSemanticHistoryMode {
        require(json.has(MODE_KEY)) { "$MODE_KEY is required in active-game schema v3." }
        require(!json.isNull(MODE_KEY)) { "$MODE_KEY cannot be null." }
        val rawMode = json.opt(MODE_KEY)
        require(rawMode is String && rawMode.isNotBlank()) {
            "$MODE_KEY must be a non-blank string."
        }

        require(json.has(CURSOR_KEY)) { "$CURSOR_KEY is required in active-game schema v3." }
        require(!json.isNull(CURSOR_KEY)) { "$CURSOR_KEY cannot be null." }
        val rawCursor = json.opt(CURSOR_KEY)
        require(rawCursor is Byte || rawCursor is Short || rawCursor is Int || rawCursor is Long) {
            "$CURSOR_KEY must be an integer."
        }
        require((rawCursor as Number).toLong() >= 0L) {
            "$CURSOR_KEY cannot be negative."
        }

        return ClocktowerSemanticHistoryMode.values().firstOrNull { it.name == rawMode }
            ?: throw IllegalArgumentException("Unknown $MODE_KEY '$rawMode'.")
    }

    /**
     * Additive schema field for production semantic actions. Older active-game saves simply have no
     * durable action history; they are restored empty rather than reconstructed from UI event text.
     */
    fun encodeActionTimeline(timeline: ActionFactTimeline): JSONArray = JSONArray().apply {
        timeline.entries.forEach { entry ->
            put(JSONObject().apply {
                put("fact", encodeActionFact(entry.fact))
                put("point", encodeTimelinePoint(entry.point))
            })
        }
    }

    fun decodeActionTimeline(json: JSONObject): ActionFactTimeline {
        if (!json.has(ACTION_TIMELINE_KEY)) return ActionFactTimeline()
        require(!json.isNull(ACTION_TIMELINE_KEY)) { "$ACTION_TIMELINE_KEY cannot be null." }
        val rawTimeline = json.opt(ACTION_TIMELINE_KEY)
        require(rawTimeline is JSONArray) { "$ACTION_TIMELINE_KEY must be an array." }

        return ActionFactTimeline(
            buildList {
                for (index in 0 until rawTimeline.length()) {
                    val entry = rawTimeline.optJSONObject(index)
                        ?: throw IllegalArgumentException("$ACTION_TIMELINE_KEY[$index] must be an object.")
                    val fact = entry.optJSONObject("fact")
                        ?: throw IllegalArgumentException("$ACTION_TIMELINE_KEY[$index].fact must be an object.")
                    val point = entry.optJSONObject("point")
                        ?: throw IllegalArgumentException("$ACTION_TIMELINE_KEY[$index].point must be an object.")
                    add(
                        TimelineBoundActionFact(
                            fact = decodeActionFact(fact),
                            point = decodeTimelinePoint(point),
                        ),
                    )
                }
            },
        )
    }

    private fun encodeTimelinePoint(point: TimelinePoint): JSONObject = JSONObject().apply {
        put("phase", point.phase.name)
        put("round", point.round)
        put("sequence", point.sequence)
        put("globalSequence", point.globalSequence)
    }

    private fun decodeTimelinePoint(json: JSONObject): TimelinePoint = TimelinePoint(
        phase = enumValue<StorytellerPhase>(json, "phase"),
        round = intValue(json, "round"),
        sequence = intValue(json, "sequence"),
        globalSequence = longValue(json, "globalSequence"),
    )

    private fun encodeActionFact(fact: ActionFact): JSONObject = JSONObject().apply {
        put("actionId", fact.actionId)
        put("sequence", fact.sequence)
        when (fact) {
            is ActionFact.Poison -> {
                put("kind", "poison")
                put("targetSeat", fact.targetSeat ?: JSONObject.NULL)
            }
            is ActionFact.Protect -> {
                put("kind", "protect")
                put("targetSeat", fact.targetSeat)
            }
            is ActionFact.Attack -> {
                put("kind", "attack")
                put("targetSeat", fact.targetSeat)
            }
            is ActionFact.Execution -> {
                put("kind", "execution")
                put("targetSeat", fact.targetSeat)
            }
            is ActionFact.Death -> {
                put("kind", "death")
                put("targetSeat", fact.targetSeat)
            }
            is ActionFact.RoleChange -> {
                put("kind", "role-change")
                put("targetSeat", fact.targetSeat)
                put("role", fact.role.value)
                put("alignment", fact.alignment.name)
                put("type", fact.type.name)
            }
            is ActionFact.PhaseAdvance -> {
                put("kind", "phase-advance")
                put("phase", fact.phase.name)
                put("round", fact.round)
            }
        }
    }

    private fun decodeActionFact(json: JSONObject): ActionFact {
        val actionId = stringValue(json, "actionId")
        val sequence = longValue(json, "sequence")
        require(actionId.isNotBlank()) { "Action fact ID cannot be blank." }
        require(sequence >= 0L) { "Action fact sequence cannot be negative." }

        return when (stringValue(json, "kind")) {
            "poison" -> ActionFact.Poison(
                actionId = actionId,
                sequence = sequence,
                targetSeat = nullablePositiveSeat(json, "targetSeat"),
            )
            "protect" -> ActionFact.Protect(actionId, sequence, positiveSeat(json, "targetSeat"))
            "attack" -> ActionFact.Attack(actionId, sequence, positiveSeat(json, "targetSeat"))
            "execution" -> ActionFact.Execution(actionId, sequence, positiveSeat(json, "targetSeat"))
            "death" -> ActionFact.Death(actionId, sequence, positiveSeat(json, "targetSeat"))
            "role-change" -> ActionFact.RoleChange(
                actionId = actionId,
                sequence = sequence,
                targetSeat = positiveSeat(json, "targetSeat"),
                role = RoleId(stringValue(json, "role")),
                alignment = enumValue(json, "alignment"),
                type = enumValue(json, "type"),
            )
            "phase-advance" -> ActionFact.PhaseAdvance(
                actionId = actionId,
                sequence = sequence,
                phase = enumValue(json, "phase"),
                round = intValue(json, "round").also { require(it > 0) { "Action target round must be positive." } },
            )
            else -> throw IllegalArgumentException("Unknown action fact kind '${stringValue(json, "kind")}'.")
        }
    }

    private fun stringValue(json: JSONObject, key: String): String {
        require(json.has(key) && !json.isNull(key)) { "$key is required." }
        return json.opt(key) as? String ?: throw IllegalArgumentException("$key must be a string.")
    }

    private fun intValue(json: JSONObject, key: String): Int {
        val value = integralNumber(json, key).toLong()
        require(value in Int.MIN_VALUE..Int.MAX_VALUE) { "$key is outside Int range." }
        return value.toInt()
    }

    private fun longValue(json: JSONObject, key: String): Long = integralNumber(json, key).toLong()

    private fun integralNumber(json: JSONObject, key: String): Number {
        require(json.has(key) && !json.isNull(key)) { "$key is required." }
        val raw = json.opt(key)
        require(raw is Byte || raw is Short || raw is Int || raw is Long) { "$key must be an integer." }
        return raw as Number
    }

    private fun positiveSeat(json: JSONObject, key: String): Int = intValue(json, key).also {
        require(it > 0) { "$key must be positive." }
    }

    private fun nullablePositiveSeat(json: JSONObject, key: String): Int? {
        require(json.has(key)) { "$key is required." }
        if (json.isNull(key)) return null
        return positiveSeat(json, key)
    }

    private inline fun <reified T : Enum<T>> enumValue(json: JSONObject, key: String): T {
        val raw = stringValue(json, key)
        return enumValues<T>().firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown $key '$raw'.")
    }
}
