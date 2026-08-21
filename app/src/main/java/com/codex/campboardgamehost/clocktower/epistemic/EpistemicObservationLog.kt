package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/** Explicit migration state for durable observations during the P1.2 timeline cutover. */
sealed interface ObservationTimelineBinding {
    /** Pre-global-timeline observation. Local phase/round/sequence are compatibility context only. */
    object LegacyLocal : ObservationTimelineBinding

    /** Observation whose ordering identity is the supplied game-wide timeline point. */
    data class Global(val point: TimelinePoint) : ObservationTimelineBinding
}

internal fun ObservationTimelineBinding.requireReplayFieldsMatch(
    phase: StorytellerPhase,
    round: Int,
    sequence: Int,
) {
    if (this is ObservationTimelineBinding.Global) {
        require(point.phase == phase && point.round == round && point.sequence == sequence) {
            "Global observation TimelinePoint must match flat phase/round/sequence replay fields."
        }
    }
}

internal enum class ObservationTimelineMode { LEGACY_LOCAL, GLOBAL }

internal val ObservationTimelineBinding.timelineMode: ObservationTimelineMode
    get() = when (this) {
        ObservationTimelineBinding.LegacyLocal -> ObservationTimelineMode.LEGACY_LOCAL
        is ObservationTimelineBinding.Global -> ObservationTimelineMode.GLOBAL
    }

/**
 * Canonicalizes already-bound observations using the same whole-history timeline contract as the
 * durable log. Direct knowledge/world-set builders use this so they cannot silently reintroduce
 * legacy round/local-sequence ordering after a Global observation has been persisted.
 */
internal fun Collection<EpistemicObservation>.canonicalTimelineOrder(): List<EpistemicObservation> {
    val values = toList()
    if (values.isEmpty()) return values
    require(values.map { it.timelineBinding.timelineMode }.distinct().size <= 1) {
        "Observation replay cannot mix LegacyLocal and Global timeline records."
    }
    return when (values.first().timelineBinding) {
        ObservationTimelineBinding.LegacyLocal ->
            values.sortedWith(compareBy<EpistemicObservation>({ it.round }, { it.sequence }, { it.observationId }))
        is ObservationTimelineBinding.Global -> {
            val globalSequences = values.map {
                (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
            }
            require(globalSequences.distinct().size == globalSequences.size) {
                "Observation replay cannot contain duplicate global timeline sequences."
            }
            values.sortedWith(compareBy<EpistemicObservation>(
                { (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence },
                { it.observationId },
            ))
        }
    }
}

/**
 * A durable statement of information that was actually shown or publicly established in a game.
 *
 * Unlike [EpistemicObservation], this record deliberately has no formal snapshot ID. A game state
 * revision creates a new formal snapshot, but it must not make already received information vanish
 * from a player's history. [bindTo] creates the version-bound observation used by a world-set build.
 */
data class RecordedEpistemicObservation(
    val recordId: String,
    val phase: StorytellerPhase,
    val round: Int,
    val sequence: Int,
    val sourceSeat: Int?,
    val sourceAbility: RoleId?,
    val visibility: ObservationVisibility,
    val recipientSeats: Set<Int>,
    val reliability: ObservationReliability,
    val proposition: InformationProposition,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
    val timelineBinding: ObservationTimelineBinding = ObservationTimelineBinding.LegacyLocal,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(recordId.isNotBlank()) { "recordId cannot be blank." }
        require(round > 0 && sequence >= 0)
        require(sourceSeat == null || sourceSeat > 0)
        require(recipientSeats.all { it > 0 })
        require(visibility != ObservationVisibility.PRIVATE || recipientSeats.isNotEmpty())
        require(visibility != ObservationVisibility.PUBLIC || recipientSeats.isEmpty())
        timelineBinding.requireReplayFieldsMatch(phase, round, sequence)
    }

    fun bindTo(formal: FormalGameState): EpistemicObservation = EpistemicObservation(
        observationId = SemanticStableId.create("observation", "$recordId|${formal.snapshotId}"),
        snapshotId = formal.snapshotId,
        phase = phase,
        round = round,
        sequence = sequence,
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        visibility = visibility,
        recipientSeats = recipientSeats,
        reliability = reliability,
        proposition = proposition,
        schemaVersion = schemaVersion,
        timelineBinding = timelineBinding,
    )
}

/** Immutable, ordered history of facts that players have actually received. */
data class EpistemicObservationLog(
    val records: List<RecordedEpistemicObservation> = emptyList(),
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(records.map(RecordedEpistemicObservation::recordId).distinct().size == records.size) {
            "An epistemic observation log cannot contain duplicate record IDs."
        }
        require(records.map { it.timelineBinding.timelineMode }.distinct().size <= 1) {
            "An epistemic observation log cannot mix LegacyLocal and Global timeline records."
        }
        if (records.firstOrNull()?.timelineBinding is ObservationTimelineBinding.Global) {
            val globalSequences = records.map {
                (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
            }
            require(globalSequences.distinct().size == globalSequences.size) {
                "An epistemic observation log cannot contain duplicate global timeline sequences."
            }
        }
        require(records == records.canonical()) { "Epistemic observation records must use canonical order." }
    }

    fun append(record: RecordedEpistemicObservation): EpistemicObservationLog {
        require(records.none { it.recordId == record.recordId }) {
            "An epistemic observation log cannot contain duplicate record ID ${record.recordId}."
        }
        if (records.isNotEmpty()) {
            require(records.first().timelineBinding.timelineMode == record.timelineBinding.timelineMode) {
                "An epistemic observation log cannot mix LegacyLocal and Global timeline records."
            }
        }
        if (record.timelineBinding is ObservationTimelineBinding.Global) {
            val globalSequence = record.timelineBinding.point.globalSequence
            require(records.none {
                (it.timelineBinding as? ObservationTimelineBinding.Global)?.point?.globalSequence == globalSequence
            }) {
                "An epistemic observation log cannot contain duplicate global timeline sequence $globalSequence."
            }
        }
        return copy(records = (records + record).canonical())
    }

    fun bindTo(formal: FormalGameState): List<EpistemicObservation> = records.map { it.bindTo(formal) }

    private fun List<RecordedEpistemicObservation>.canonical(): List<RecordedEpistemicObservation> {
        if (isEmpty()) return this
        return when (first().timelineBinding) {
            ObservationTimelineBinding.LegacyLocal ->
                sortedWith(compareBy<RecordedEpistemicObservation>({ it.round }, { it.sequence }, { it.recordId }))
            is ObservationTimelineBinding.Global ->
                sortedWith(compareBy<RecordedEpistemicObservation>(
                    { (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence },
                    { it.recordId },
                ))
        }
    }
}
