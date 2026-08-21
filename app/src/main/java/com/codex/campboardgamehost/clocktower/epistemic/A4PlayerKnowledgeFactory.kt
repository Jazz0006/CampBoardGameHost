package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Builds replayable, recipient-scoped knowledge without exposing formal-state secrets. */
object A4PlayerKnowledgeFactory {
    fun createAll(
        formal: FormalGameState,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observations: Collection<EpistemicObservation>,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> {
        val seats = formal.players.map { it.seat }.toSet()
        require(perceivedRolesBySeat.keys == seats) { "Every formal player needs one supplied perceived role." }
        require(observations.all { it.snapshotId == formal.snapshotId }) {
            "Every replayed observation must be bound to the current formal snapshot."
        }
        require(observations.all { observation ->
            observation.sourceSeat == null || observation.sourceSeat in seats
        }) { "Observation source must exist in the formal snapshot." }
        require(observations.map { it.observationId }.distinct().size == observations.size) {
            "Observation replay cannot contain duplicate IDs."
        }
        val canonicalObservations = observations.canonicalTimelineOrder()
        // Player count is public, mechanically required setup knowledge. Including it here keeps
        // the knowledge identity used by cache keys identical to the world-builder input.
        val canonicalSetup = (
            formal.publicPropositions + setupKnowledge + InformationProposition.PlayerCount(formal.players.size)
        ).distinct()
        return seats.sorted().map { seat ->
            val public = canonicalObservations.filter { it.visibility == ObservationVisibility.PUBLIC }
            val private = canonicalObservations.filter {
                it.visibility == ObservationVisibility.PRIVATE && seat in it.recipientSeats
            }
            PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = SemanticStableId.create(
                    "knowledge",
                    listOf(
                        formal.snapshotId,
                        seat.toString(),
                        perceivedRolesBySeat.getValue(seat).value,
                        canonicalSetup.joinToString(";") { EpistemicSemanticJson.encode(it) },
                        (public + private).joinToString(";") { EpistemicSemanticJson.encode(it) },
                    ).joinToString("|"),
                ),
                formalSnapshotId = formal.snapshotId,
                recipientSeat = seat,
                perceivedRole = perceivedRolesBySeat.getValue(seat),
                publicObservations = public,
                privateObservations = private,
                setupKnowledge = canonicalSetup,
            )
        }
    }

    /** Replays durable game-session records into observations bound to [formal]'s exact version. */
    fun createAll(
        formal: FormalGameState,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observationLog: EpistemicObservationLog,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> = createAll(
        formal = formal,
        perceivedRolesBySeat = perceivedRolesBySeat,
        observations = observationLog.bindTo(formal),
        setupKnowledge = setupKnowledge,
    )
}
