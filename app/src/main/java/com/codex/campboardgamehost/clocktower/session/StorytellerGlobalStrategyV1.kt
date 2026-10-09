package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject

/**
 * Model-authored STRATEGY, never Host game truth. The Host owns the seat domain,
 * real facts and every actionable legal candidate.
 *
 * This object can be retained for an active game and passed as bounded intent to
 * the next provider request; it is deliberately not persisted as Recovery truth.
 */
internal data class StorytellerGlobalIssueV1(
    val issueId: String,
    val priority: Int,
    val seats: List<Int>,
    val diagnosis: String,
    val futureEffect: String,
)

internal data class StorytellerGlobalRelationV1(
    val fromSeat: Int,
    val toSeat: Int,
    val issueId: String,
    val label: String,
)

internal data class StorytellerGlobalIntentionV1(
    val trigger: String,
    val approach: String,
    val tradeoff: String,
)

internal data class StorytellerGlobalStrategyV1(
    val situationSummary: String,
    val issues: List<StorytellerGlobalIssueV1>,
    val relations: List<StorytellerGlobalRelationV1>,
    val intentions: List<StorytellerGlobalIntentionV1>,
    val planRevisionNote: String,
) {
    /** Only advisory intent is serialised. No magic proof of player registration or fact. */
    fun toJson(): JSONObject = JSONObject()
        .put("situationSummary", situationSummary)
        .put("issues", JSONArray().also { out ->
            issues.forEach { issue ->
                out.put(JSONObject()
                    .put("issueId", issue.issueId)
                    .put("priority", issue.priority)
                    .put("seats", JSONArray(issue.seats))
                    .put("diagnosis", issue.diagnosis)
                    .put("futureEffect", issue.futureEffect))
            }
        })
        .put("relations", JSONArray().also { out ->
            relations.forEach { relation ->
                out.put(JSONObject()
                    .put("fromSeat", relation.fromSeat)
                    .put("toSeat", relation.toSeat)
                    .put("issueId", relation.issueId)
                    .put("label", relation.label))
            }
        })
        .put("intentions", JSONArray().also { out ->
            intentions.forEach { intention ->
                out.put(JSONObject()
                    .put("trigger", intention.trigger)
                    .put("approach", intention.approach)
                    .put("tradeoff", intention.tradeoff))
            }
        })
        .put("planRevisionNote", planRevisionNote)

    /** Model-proposed relationships are hypotheses. Never label them confirmed registrations. */
    companion object {
        fun decode(raw: JSONObject, legalSeatNumbers: Set<Int>): StorytellerGlobalStrategyV1 {
            require(legalSeatNumbers.isNotEmpty())
            fun text(value: JSONObject, key: String, max: Int): String {
                require(value.opt(key) is String) { "Invalid global strategy field: $key" }
                return value.getString(key).also { require(it.isNotBlank() && it.length <= max) }
            }
            fun <T> mapped(
                array: JSONArray, maximum: Int, minimum: Int = 0,
                builder: (JSONObject) -> T,
            ): List<T> {
                require(array.length() in minimum..maximum) { "Invalid global strategy item count" }
                return (0 until array.length()).map { builder(array.getJSONObject(it)) }
            }
            val issues = mapped(raw.getJSONArray("issues"), maximum = 4, minimum = 1) { item ->
                val seats = item.getJSONArray("seats").let { array ->
                    (0 until array.length()).map { index ->
                        val seat = array.getInt(index)
                        require(array.get(index) is Int && seat in legalSeatNumbers) {
                            "Issue seat is not in canonical Host roster"
                        }
                        seat
                    }
                }
                require(seats.isNotEmpty() && seats.distinct().size == seats.size)
                StorytellerGlobalIssueV1(
                    issueId = text(item, "issueId", 80),
                    priority = item.getInt("priority").also { require(it in 1..4) },
                    seats = seats,
                    diagnosis = text(item, "diagnosis", 1600),
                    futureEffect = text(item, "futureEffect", 1600),
                )
            }
            val knownIssues = issues.map { it.issueId }.toSet()
            require(knownIssues.size == issues.size) { "Duplicate strategy issue IDs" }
            val relations = mapped(raw.getJSONArray("relations"), maximum = 4) { item ->
                val from = item.getInt("fromSeat")
                val to = item.getInt("toSeat")
                require(item.get("fromSeat") is Int && item.get("toSeat") is Int)
                require(from != to && from in legalSeatNumbers && to in legalSeatNumbers)
                val issueId = text(item, "issueId", 80)
                require(issueId in knownIssues) { "Unknown related issue" }
                StorytellerGlobalRelationV1(
                    fromSeat = from, toSeat = to, issueId = issueId,
                    label = text(item, "label", 500),
                )
            }
            val intentions = mapped(raw.getJSONArray("intentions"), maximum = 4, minimum = 1) { item ->
                StorytellerGlobalIntentionV1(
                    trigger = text(item, "trigger", 900),
                    approach = text(item, "approach", 900),
                    tradeoff = text(item, "tradeoff", 900),
                )
            }
            val note = raw.opt("planRevisionNote")
            require(note is String && note.length <= 900)
            return StorytellerGlobalStrategyV1(
                situationSummary = text(raw, "situationSummary", 2400),
                issues = issues,
                relations = relations,
                intentions = intentions,
                planRevisionNote = note,
            )
        }
    }
}
