package com.codex.campboardgamehost.clocktower.epistemic

/** Shadow-only policy decision for applying a newly delivered observation to cached knowledge. */
object A4ObservationCacheUpdateCoordinator {
    fun affectedSeats(record: RecordedEpistemicObservation, allSeats: Set<Int>): Set<Int> = when (record.visibility) {
        ObservationVisibility.PUBLIC -> allSeats
        ObservationVisibility.PRIVATE -> record.recipientSeats
    }

    /** Native ZDD restrictions may be incrementally filtered; every other proposition must replay. */
    fun strategyFor(record: RecordedEpistemicObservation): A4ObservationCacheUpdateStrategy = when (record.proposition) {
        is InformationProposition.AliveAt,
        is InformationProposition.RoleInPlay -> A4ObservationCacheUpdateStrategy.NATIVE_FILTER
        else -> A4ObservationCacheUpdateStrategy.REPLAY_REQUIRED
    }

    fun classify(
        record: RecordedEpistemicObservation,
        allSeats: Set<Int>,
        cachedSeats: Set<Int>,
        currentSeats: Set<Int>,
    ): List<A4ObservationCacheUpdateEntry> = affectedSeats(record, allSeats).sorted().map { seat ->
        A4ObservationCacheUpdateEntry(
            recipientSeat = seat,
            outcome = when {
                seat !in currentSeats -> A4ObservationCacheUpdateOutcome.STALE
                seat !in cachedSeats -> A4ObservationCacheUpdateOutcome.MISSING
                strategyFor(record) == A4ObservationCacheUpdateStrategy.NATIVE_FILTER -> A4ObservationCacheUpdateOutcome.UPDATED
                else -> A4ObservationCacheUpdateOutcome.REBUILT
            },
        )
    }
}

enum class A4ObservationCacheUpdateStrategy { NATIVE_FILTER, REPLAY_REQUIRED }
enum class A4ObservationCacheUpdateOutcome { UPDATED, REBUILT, MISSING, STALE }
data class A4ObservationCacheUpdateEntry(val recipientSeat: Int, val outcome: A4ObservationCacheUpdateOutcome)

data class A4ObservationCacheUpdateReport(
    val entries: List<A4ObservationCacheUpdateEntry>,
) {
    fun toLogLine(): String = "A4_OBSERVATION_CACHE_UPDATE " + entries.joinToString(" ") {
        "seat=${it.recipientSeat}:${it.outcome}"
    }
}
