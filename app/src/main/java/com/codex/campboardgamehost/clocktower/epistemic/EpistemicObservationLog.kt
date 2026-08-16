package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

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
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(recordId.isNotBlank()) { "recordId cannot be blank." }
        require(round > 0 && sequence >= 0)
        require(sourceSeat == null || sourceSeat > 0)
        require(recipientSeats.all { it > 0 })
        require(visibility != ObservationVisibility.PRIVATE || recipientSeats.isNotEmpty())
        require(visibility != ObservationVisibility.PUBLIC || recipientSeats.isEmpty())
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
        require(records == records.canonical()) { "Epistemic observation records must use canonical order." }
    }

    fun append(record: RecordedEpistemicObservation): EpistemicObservationLog {
        require(records.none { it.recordId == record.recordId }) {
            "An epistemic observation log cannot contain duplicate record ID ${record.recordId}."
        }
        return copy(records = (records + record).canonical())
    }

    fun bindTo(formal: FormalGameState): List<EpistemicObservation> = records.map { it.bindTo(formal) }

    private fun List<RecordedEpistemicObservation>.canonical(): List<RecordedEpistemicObservation> =
        sortedWith(compareBy<RecordedEpistemicObservation>({ it.round }, { it.sequence }, { it.recordId }))
}
