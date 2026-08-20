package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class A4PlayerKnowledgeFactoryTest {
    @Test fun `each recipient receives public observations plus only their private replay`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val public = observation(formal, "public-alive", ObservationVisibility.PUBLIC, emptySet(),
            InformationProposition.AliveAt(2, true))
        val privateOne = observation(formal, "private-one", ObservationVisibility.PRIVATE, setOf(1),
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 1))
        val privateTwo = observation(formal, "private-two", ObservationVisibility.PRIVATE, setOf(2),
            InformationProposition.RoleAt(3, RoleId("Empath")))

        val knowledge = A4PlayerKnowledgeFactory.createAll(
            formal,
            mapOf(1 to RoleId("Chef"), 2 to RoleId("Empath"), 3 to RoleId("Washerwoman"), 4 to RoleId("Poisoner"), 5 to RoleId("Imp")),
            listOf(privateTwo, public, privateOne),
        ).associateBy { it.recipientSeat }

        assertEquals(listOf("public-alive"), knowledge.getValue(3).publicObservations.map { it.observationId })
        assertEquals(listOf("public-alive", "private-one"),
            (knowledge.getValue(1).publicObservations + knowledge.getValue(1).privateObservations).map { it.observationId })
        assertEquals(listOf("public-alive", "private-two"),
            (knowledge.getValue(2).publicObservations + knowledge.getValue(2).privateObservations).map { it.observationId })
        assertTrue(knowledge.values.map { it.knowledgeSnapshotId }.distinct().size == 5)
        assertTrue(knowledge.values.all { InformationProposition.PlayerCount(5) in it.setupKnowledge })
    }

    @Test fun `durable records rebind to a newer formal snapshot without widening private visibility`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val firstFormal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val laterFormal = FormalGameState.from(
            snapshot.copy(gameStateRevision = snapshot.gameStateRevision + 1),
            StorytellerPhase.DAY,
            1,
        )
        val log = EpistemicObservationLog().append(
            RecordedEpistemicObservation(
                recordId = "first-night-chef",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 4,
                sourceSeat = 1,
                sourceAbility = RoleId("Chef"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 1,
                ),
            ),
        ).append(
            RecordedEpistemicObservation(
                recordId = "dawn-seat-two-dead",
                phase = StorytellerPhase.DAWN,
                round = 1,
                sequence = 0,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(2, false),
            ),
        )
        val roles = mapOf(1 to RoleId("Chef"), 2 to RoleId("Empath"), 3 to RoleId("Washerwoman"), 4 to RoleId("Poisoner"), 5 to RoleId("Imp"))

        val first = A4PlayerKnowledgeFactory.createAll(firstFormal, roles, log).associateBy { it.recipientSeat }
        val later = A4PlayerKnowledgeFactory.createAll(laterFormal, roles, log).associateBy { it.recipientSeat }

        assertEquals(1, later.getValue(2).publicObservations.size)
        assertEquals(InformationProposition.AliveAt(2, false), later.getValue(2).publicObservations.single().proposition)
        assertEquals(1, later.getValue(1).privateObservations.size)
        assertEquals(0, later.getValue(2).privateObservations.size)
        assertTrue(later.values.flatMap { it.publicObservations + it.privateObservations }
            .all { it.snapshotId == laterFormal.snapshotId })
        assertTrue(first.getValue(1).knowledgeSnapshotId != later.getValue(1).knowledgeSnapshotId)
    }

    private fun observation(
        formal: FormalGameState,
        id: String,
        visibility: ObservationVisibility,
        recipients: Set<Int>,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = if (id == "public-alive") 0 else 1,
        sourceSeat = if (visibility == ObservationVisibility.PRIVATE) recipients.single() else null,
        sourceAbility = if (visibility == ObservationVisibility.PRIVATE) RoleId("Chef") else null,
        visibility = visibility,
        recipientSeats = recipients,
        reliability = if (visibility == ObservationVisibility.PRIVATE) ObservationReliability.RECEIVED_AS_FUNCTIONING else ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )
}
