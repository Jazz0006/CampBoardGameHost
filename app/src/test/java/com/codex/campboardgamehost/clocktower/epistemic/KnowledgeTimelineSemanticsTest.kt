package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
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

    @Test fun `world replay preserves global chronology after public private split`() {
        val earlierPrivate = globalObservation(
            id = "day-private-earlier",
            point = TimelinePoint(StorytellerPhase.DAY, round = 2, sequence = 99, globalSequence = 40),
            visibility = ObservationVisibility.PRIVATE,
        )
        val laterPublic = globalObservation(
            id = "night-public-later",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 41),
        )
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "world-replay-global",
            formalSnapshotId = formal.snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            publicObservations = listOf(laterPublic),
            privateObservations = listOf(earlierPrivate),
        )

        assertEquals(
            listOf("day-private-earlier", "night-public-later"),
            knowledge.worldReplayObservationsInTimelineOrder().map { it.observationId },
        )
    }

    @Test fun `world replay keeps legacy compatibility order`() {
        val later = legacyObservation("legacy-later", StorytellerPhase.NIGHT, round = 2, sequence = 0)
        val earlier = legacyObservation("legacy-earlier", StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 99)
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "world-replay-legacy",
            formalSnapshotId = formal.snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            publicObservations = listOf(later, earlier),
        )

        assertEquals(
            listOf("legacy-earlier", "legacy-later"),
            knowledge.worldReplayObservationsInTimelineOrder().map { it.observationId },
        )
    }

    @Test fun `A3 replay consumer rejects mixed legacy and global chronology`() {
        val knowledge = mixedModeFirstNightKnowledge()

        expectIllegalArgument("A3 mixed timeline modes") {
            TroubleBrewingWorldEnumerator.enumerate(
                snapshot.rulesetRef,
                knowledge,
                EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                TroubleBrewingFixtures.fullRoleDefinitions(),
            )
        }
    }

    @Test fun `ZDD replay consumer rejects mixed legacy and global chronology`() {
        val knowledge = mixedModeFirstNightKnowledge()

        expectIllegalArgument("ZDD mixed timeline modes") {
            ZddPlayerWorldSet.enumerateDirect(
                snapshot.rulesetRef,
                knowledge,
                EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                TroubleBrewingFixtures.fullRoleDefinitions(),
            )
        }
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

    private fun mixedModeFirstNightKnowledge(): PlayerKnowledgeSnapshot {
        val global = globalObservation(
            id = "mixed-global",
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 0, globalSequence = 10),
        )
        val legacy = legacyObservation(
            id = "mixed-legacy",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
        )
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "mixed-world-replay",
            formalSnapshotId = formal.snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            publicObservations = listOf(global, legacy),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
    }

    private fun globalObservation(
        id: String,
        point: TimelinePoint,
        visibility: ObservationVisibility = ObservationVisibility.PUBLIC,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = point.phase,
        round = point.round,
        sequence = point.sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = visibility,
        recipientSeats = if (visibility == ObservationVisibility.PRIVATE) setOf(1) else emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, true),
        timelineBinding = ObservationTimelineBinding.Global(point),
    )

    private fun legacyObservation(
        id: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = phase,
        round = round,
        sequence = sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, true),
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
