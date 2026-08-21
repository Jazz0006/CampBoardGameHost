package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.fail
import org.junit.Test

class KnowledgeTimelineSemanticsTest {
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.NIGHT, 2)
    private val perceivedRoles = mapOf(
        1 to RoleId("Chef"),
        2 to RoleId("Empath"),
        3 to RoleId("Washerwoman"),
        4 to RoleId("Poisoner"),
        5 to RoleId("Imp"),
    )

    @Test fun `knowledge factory preserves global chronology instead of legacy round order`() {
        val globallyFirst = globalObservation(
            id = "night-two-first",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 40),
        )
        val globallySecond = globalObservation(
            id = "first-night-later-global",
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 99, globalSequence = 41),
        )

        val knowledge = A4PlayerKnowledgeFactory.createAll(
            formal,
            perceivedRoles,
            listOf(globallySecond, globallyFirst),
        ).first { it.recipientSeat == 1 }

        assertEquals(
            listOf("night-two-first", "first-night-later-global"),
            knowledge.publicObservations.map { it.observationId },
        )
    }

    @Test fun `knowledge factory rejects mixed legacy and global observation modes`() {
        val global = globalObservation(
            id = "global-one",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 10),
        )
        val legacy = EpistemicObservation(
            observationId = "legacy-one",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(1, true),
        )

        expectIllegalArgument("mixed timeline modes") {
            A4PlayerKnowledgeFactory.createAll(formal, perceivedRoles, listOf(global, legacy))
        }
    }

    @Test fun `knowledge factory rejects duplicate global observation sequence`() {
        val first = globalObservation(
            id = "global-one",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 10),
        )
        val duplicate = globalObservation(
            id = "global-two",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 1, globalSequence = 10),
        )

        expectIllegalArgument("duplicate global sequence") {
            A4PlayerKnowledgeFactory.createAll(formal, perceivedRoles, listOf(first, duplicate))
        }
    }

    @Test fun `current world set identity remains insensitive to global sequence until evaluator is time aware`() {
        val firstObservation = globalObservation(
            id = "same-visible-fact",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 10),
        )
        val movedObservation = firstObservation.copy(
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 99),
            ),
        )

        val firstKnowledge = A4PlayerKnowledgeFactory.createAll(
            formal,
            perceivedRoles,
            listOf(firstObservation),
        ).first { it.recipientSeat == 1 }
        val movedKnowledge = A4PlayerKnowledgeFactory.createAll(
            formal,
            perceivedRoles,
            listOf(movedObservation),
        ).first { it.recipientSeat == 1 }

        assertNotEquals(firstKnowledge.knowledgeSnapshotId, movedKnowledge.knowledgeSnapshotId)
        assertEquals(
            PlayerWorldSetIdentity.create(snapshot.rulesetRef, firstKnowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE).value,
            PlayerWorldSetIdentity.create(snapshot.rulesetRef, movedKnowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE).value,
        )
    }

    private fun globalObservation(
        id: String,
        point: TimelinePoint,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = point.phase,
        round = point.round,
        sequence = point.sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, true),
        timelineBinding = ObservationTimelineBinding.Global(point),
    )

    private fun expectIllegalArgument(label: String, block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException for $label")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed contract.
        }
    }
}
