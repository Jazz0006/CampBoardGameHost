package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * A newly produced player-visible fact before durable timeline identity is assigned.
 *
 * This type deliberately has no [ObservationTimelineBinding]. Global identity belongs to the
 * game/session authority; callers must not reinterpret a persisted LegacyLocal record as a draft.
 */
data class EpistemicObservationDraft(
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

    /** Compatibility binding for observations produced while resuming a persisted LegacyLocal game. */
    internal fun bindLegacyLocal(): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = recordId,
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
        timelineBinding = ObservationTimelineBinding.LegacyLocal,
    )

    internal fun bindGlobal(point: TimelinePoint): RecordedEpistemicObservation {
        require(point.phase == phase && point.round == round && point.sequence == sequence) {
            "Global TimelinePoint must match observation draft replay fields."
        }
        return RecordedEpistemicObservation(
            recordId = recordId,
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
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }

    internal fun matches(record: RecordedEpistemicObservation): Boolean =
        record.recordId == recordId &&
            record.phase == phase &&
            record.round == round &&
            record.sequence == sequence &&
            record.sourceSeat == sourceSeat &&
            record.sourceAbility == sourceAbility &&
            record.visibility == visibility &&
            record.recipientSeats == recipientSeats &&
            record.reliability == reliability &&
            record.proposition == proposition &&
            record.schemaVersion == schemaVersion
}
