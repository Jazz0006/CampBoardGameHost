package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Builds replayable, recipient-scoped knowledge without exposing formal-state secrets. */
object A4PlayerKnowledgeFactory {
    /** Compatibility adapter. The knowledge core consumes only [KnowledgeConstructionInput]. */
    fun createAll(
        formal: FormalGameState,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observations: Collection<EpistemicObservation>,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> = createAll(
        input = formal.toKnowledgeConstructionInput(),
        perceivedRolesBySeat = perceivedRolesBySeat,
        observations = observations,
        setupKnowledge = setupKnowledge,
    )

    fun createAll(
        input: KnowledgeConstructionInput,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observations: Collection<EpistemicObservation>,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> {
        val seats = input.playerSeats.toSet()
        require(perceivedRolesBySeat.keys == seats) { "Every formal player needs one supplied perceived role." }
        require(observations.all { it.snapshotId == input.formalSnapshotId }) {
            "Every replayed observation must be bound to the current formal snapshot."
        }
        require(observations.all { observation ->
            observation.sourceSeat == null || observation.sourceSeat in seats
        }) { "Observation source must exist in the formal snapshot."
        }
        require(observations.map { it.observationId }.distinct().size == observations.size) {
            "Observation replay cannot contain duplicate IDs."
        }
        val canonicalObservations = observations.canonicalTimelineOrder()
        // Player count is public, mechanically required setup knowledge. Including it here keeps
        // the knowledge identity used by cache keys identical to the world-builder input.
        val canonicalSetup = (
            input.publicPropositions + setupKnowledge + InformationProposition.PlayerCount(input.playerCount)
        ).distinct()
        return input.playerSeats.map { seat ->
            val public = canonicalObservations.filter { it.visibility == ObservationVisibility.PUBLIC }
            val private = canonicalObservations.filter {
                it.visibility == ObservationVisibility.PRIVATE && seat in it.recipientSeats
            }
            PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = SemanticStableId.create(
                    "knowledge",
                    listOf(
                        input.formalSnapshotId,
                        seat.toString(),
                        perceivedRolesBySeat.getValue(seat).value,
                        canonicalSetup.joinToString(";") { EpistemicSemanticJson.encode(it) },
                        (public + private).joinToString(";") { EpistemicSemanticJson.encode(it) },
                    ).joinToString("|"),
                ),
                formalSnapshotId = input.formalSnapshotId,
                recipientSeat = seat,
                perceivedRole = perceivedRolesBySeat.getValue(seat),
                publicObservations = public,
                privateObservations = private,
                setupKnowledge = canonicalSetup,
            )
        }
    }

    /** Compatibility adapter for durable game-session records. */
    fun createAll(
        formal: FormalGameState,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observationLog: EpistemicObservationLog,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> = createAll(
        input = formal.toKnowledgeConstructionInput(),
        perceivedRolesBySeat = perceivedRolesBySeat,
        observationLog = observationLog,
        setupKnowledge = setupKnowledge,
    )

    fun createAll(
        input: KnowledgeConstructionInput,
        perceivedRolesBySeat: Map<Int, RoleId>,
        observationLog: EpistemicObservationLog,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): List<PlayerKnowledgeSnapshot> = createAll(
        input = input,
        perceivedRolesBySeat = perceivedRolesBySeat,
        observations = observationLog.bindTo(input.formalSnapshotId),
        setupKnowledge = setupKnowledge,
    )
}
