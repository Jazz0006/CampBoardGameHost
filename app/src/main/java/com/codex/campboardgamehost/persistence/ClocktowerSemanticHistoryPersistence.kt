package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.KlutzDeathTriggerEvidenceV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.json.JSONArray
import org.json.JSONObject

/** Active-game JSON contract for explicit Clocktower semantic-history metadata. */
internal object ClocktowerSemanticHistoryPersistence {
    const val ACTION_TIMELINE_KEY = "clocktowerActionTimeline"

    fun deriveNextTimelineGlobalSequence(
        actionTimeline: ActionFactTimeline,
        observations: List<RecordedEpistemicObservation>,
    ): Long {
        val actionMax = actionTimeline.entries.maxOfOrNull { it.point.globalSequence }
        val observationMax = observations
            .mapNotNull { observation ->
                (observation.timelineBinding as? ObservationTimelineBinding.Global)?.point?.globalSequence
            }
            .maxOrNull()
        val maxCommitted = listOfNotNull(actionMax, observationMax).maxOrNull() ?: return 0L
        require(maxCommitted != Long.MAX_VALUE) {
            "Committed global timeline cannot exhaust the recovery cursor."
        }
        return maxCommitted + 1L
    }

    /** Current-format durable semantic-action history. */
    fun encodeActionTimeline(timeline: ActionFactTimeline): JSONArray = JSONArray().apply {
        timeline.entries.forEach { entry ->
            put(JSONObject().apply {
                put("fact", encodeActionFact(entry.fact))
                put("point", encodeTimelinePoint(entry.point))
            })
        }
    }

    fun decodeActionTimeline(json: JSONObject): ActionFactTimeline {
        require(json.has(ACTION_TIMELINE_KEY)) { "$ACTION_TIMELINE_KEY is required." }
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
                fact.klutzDeathTrigger?.let { put("klutzDeathTrigger", encodeKlutzDeathTrigger(it)) }
            }
            is ActionFact.Death -> {
                put("kind", "death")
                put("targetSeat", fact.targetSeat)
                fact.klutzDeathTrigger?.let { put("klutzDeathTrigger", encodeKlutzDeathTrigger(it)) }
            }
            is ActionFact.SlayerShot -> {
                put("kind", "slayer-shot")
                put("claimantSeat", fact.claimantSeat)
                put("targetSeat", fact.targetSeat)
                put("abilityConsumed", fact.abilityConsumed)
                put("hit", fact.hit)
            }
            is ActionFact.Nomination -> {
                put("kind", "nomination")
                put("nominatorSeat", fact.nominatorSeat)
                put("nomineeSeat", fact.nomineeSeat)
                put("firstVirginNomination", fact.firstVirginNomination)
            }
            is ActionFact.Vote -> {
                put("kind", "vote")
                put("nominatorSeat", fact.nominatorSeat)
                put("nomineeSeat", fact.nomineeSeat)
                put("voterSeats", JSONArray(fact.voterSeats))
                put("ghostVoterSeats", JSONArray(fact.ghostVoterSeats))
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
            "execution" -> ActionFact.Execution(
                actionId, sequence, positiveSeat(json, "targetSeat"), decodeKlutzDeathTrigger(json),
            )
            "death" -> ActionFact.Death(
                actionId, sequence, positiveSeat(json, "targetSeat"), decodeKlutzDeathTrigger(json),
            )
            "slayer-shot" -> ActionFact.SlayerShot(
                actionId, sequence, positiveSeat(json, "claimantSeat"), positiveSeat(json, "targetSeat"),
                booleanValue(json, "abilityConsumed"), booleanValue(json, "hit"),
            ).also {
                require(!it.hit || it.abilityConsumed)
            }
            "nomination" -> ActionFact.Nomination(
                actionId, sequence, positiveSeat(json, "nominatorSeat"), positiveSeat(json, "nomineeSeat"),
                booleanValue(json, "firstVirginNomination"),
            ).also { require(it.nominatorSeat != it.nomineeSeat) }
            "vote" -> ActionFact.Vote(
                actionId, sequence, positiveSeat(json, "nominatorSeat"), positiveSeat(json, "nomineeSeat"),
                positiveSeatArray(json, "voterSeats"), positiveSeatArray(json, "ghostVoterSeats"),
            ).also {
                require(it.nominatorSeat != it.nomineeSeat)
                require(it.ghostVoterSeats.all { seat -> seat in it.voterSeats })
            }
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

    private fun booleanValue(json: JSONObject, key: String): Boolean {
        require(json.has(key) && !json.isNull(key)) { "$key is required." }
        val value = json.opt(key)
        require(value is Boolean) { "$key must be a Boolean." }
        return value
    }

    private fun positiveSeatArray(json: JSONObject, key: String): List<Int> {
        val array = json.opt(key) as? JSONArray
            ?: throw IllegalArgumentException("$key must be an array.")
        val values = (0 until array.length()).map { index ->
            val value = array.opt(index)
            require(value is Int && value > 0) { "$key[$index] must be a positive seat." }
            value
        }
        require(values.distinct().size == values.size) { "$key must not contain duplicates." }
        return values
    }

    private fun encodeKlutzDeathTrigger(value: KlutzDeathTriggerEvidenceV1): JSONObject = JSONObject().apply {
        put("role", value.actualRole.value)
        put("aliveBeforeDeath", value.wasAlive)
        put("poisonedBeforeDeath", value.wasPoisoned)
        put("sourceGameStateRevision", value.sourceGameStateRevision)
    }

    private fun decodeKlutzDeathTrigger(json: JSONObject): KlutzDeathTriggerEvidenceV1? {
        if (!json.has("klutzDeathTrigger")) return null // old format is UNKNOWN, not functioning
        val raw = json.opt("klutzDeathTrigger")
        require(raw is JSONObject) { "klutzDeathTrigger must be an object when present." }
        require(raw.has("aliveBeforeDeath") && raw.opt("aliveBeforeDeath") is Boolean)
        require(raw.has("poisonedBeforeDeath") && raw.opt("poisonedBeforeDeath") is Boolean)
        return KlutzDeathTriggerEvidenceV1(
            actualRole = RoleId(stringValue(raw, "role")),
            wasAlive = raw.getBoolean("aliveBeforeDeath"),
            wasPoisoned = raw.getBoolean("poisonedBeforeDeath"),
            sourceGameStateRevision = longValue(raw, "sourceGameStateRevision"),
        )
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
