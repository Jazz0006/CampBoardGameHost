package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import java.util.Collections

/**
 * Explicit bridge between the domain reducer's existing global action sequence and the shared
 * epistemic game timeline. Domain actions remain independent of the epistemic package; callers
 * must supply the committed [TimelinePoint] rather than reconstructing one from local phase data.
 */
data class TimelineBoundActionFact(
    val fact: ActionFact,
    val point: TimelinePoint,
) {
    init {
        require(fact.actionId.isNotBlank()) { "Timeline-bound action ID cannot be blank." }
        require(fact.sequence >= 0) { "Timeline-bound action sequence cannot be negative." }
        require(fact.sequence == point.globalSequence) {
            "ActionFact sequence must equal TimelinePoint globalSequence; local phase/round/sequence cannot infer global action identity."
        }
    }
}

/**
 * Persistence migration state for FormalGameState action history.
 *
 * Legacy means the persisted schema-v2 action list contains only ActionFact.sequence and therefore
 * has no recoverable phase/round/local TimelinePoint metadata. Global is valid only when every
 * action is explicitly bound to a committed TimelinePoint.
 */
sealed interface FormalActionTimelineBinding {
    object Legacy : FormalActionTimelineBinding
    data class Global(val timeline: ActionFactTimeline) : FormalActionTimelineBinding
}

/**
 * Immutable canonical action history whose ordering authority is the shared global timeline.
 *
 * The underlying [ActionFact.sequence] remains unchanged for [com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer]
 * compatibility. This contract proves that sequence is the same identity as [TimelinePoint.globalSequence]
 * before facts cross into timeline-aware epistemic code.
 */
class ActionFactTimeline(
    entries: List<TimelineBoundActionFact> = emptyList(),
) {
    /** Defensive immutable snapshot; caller-owned mutable collections cannot rewrite validated history. */
    val entries: List<TimelineBoundActionFact> = Collections.unmodifiableList(entries.toList())

    init {
        require(this.entries.map { it.fact.actionId }.distinct().size == this.entries.size) {
            "Action timeline cannot contain duplicate action IDs."
        }
        require(this.entries.map { it.point.globalSequence }.distinct().size == this.entries.size) {
            "Action timeline cannot contain duplicate global timeline sequences."
        }
        require(this.entries == this.entries.canonical()) {
            "Action timeline entries must use canonical global timeline order."
        }
    }

    fun append(entry: TimelineBoundActionFact): ActionFactTimeline {
        require(entries.none { it.fact.actionId == entry.fact.actionId }) {
            "Action timeline cannot contain duplicate action ID ${entry.fact.actionId}."
        }
        require(entries.none { it.point.globalSequence == entry.point.globalSequence }) {
            "Action timeline cannot contain duplicate global timeline sequence ${entry.point.globalSequence}."
        }
        return ActionFactTimeline((entries + entry).canonical())
    }

    /** Exact reducer-compatible view; no action payload or sequence is rewritten. */
    fun reducerFacts(): List<ActionFact> = entries.map { it.fact }

    override fun equals(other: Any?): Boolean = other is ActionFactTimeline && entries == other.entries
    override fun hashCode(): Int = entries.hashCode()
    override fun toString(): String = "ActionFactTimeline(entries=$entries)"

    private fun List<TimelineBoundActionFact>.canonical(): List<TimelineBoundActionFact> =
        sortedWith(compareBy<TimelineBoundActionFact>({ it.point.globalSequence }, { it.fact.actionId }))
}

/**
 * Validates that independently persisted action and observation histories can coexist on one
 * game-wide timeline. Empty histories need no cross-type ordering relation. Once both histories
 * contain entries, observations must be globally bound and every global position must be unique
 * across both event types; legacy local positions are never guessed into the global sequence.
 */
internal fun ActionFactTimeline.requireCompatibleWith(observationLog: EpistemicObservationLog) {
    if (entries.isEmpty() || observationLog.records.isEmpty()) return

    require(observationLog.records.first().timelineBinding is ObservationTimelineBinding.Global) {
        "Globally bound actions cannot be combined with LegacyLocal observations; cross-type ordering cannot be inferred."
    }

    val actionSequences = entries.mapTo(linkedSetOf()) { it.point.globalSequence }
    val observationSequences = observationLog.records.mapTo(linkedSetOf()) {
        (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
    }
    val collisions = actionSequences intersect observationSequences
    require(collisions.isEmpty()) {
        "Action and observation timelines cannot share global timeline sequences: ${collisions.sorted().joinToString()}."
    }
}
