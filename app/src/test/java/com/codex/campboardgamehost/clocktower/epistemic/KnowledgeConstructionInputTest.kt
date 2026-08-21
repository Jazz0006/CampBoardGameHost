package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class KnowledgeConstructionInputTest {
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val baseFormal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
    private val publicFact = InformationProposition.AliveAt(2, true)
    private val formal = baseFormal.copy(publicPropositions = listOf(publicFact))
    private val perceivedRoles = baseFormal.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test fun `projection keeps only safe structure plus explicitly public propositions`() {
        val alteredSecrets = formal.copy(
            players = formal.players.mapIndexed { index, player ->
                if (index == 0) player.copy(
                    actualRole = RoleId("Imp"),
                    actualAlignment = Alignment.EVIL,
                    actualType = CharacterType.DEMON,
                    poisoned = true,
                ) else player
            },
            storytellerOnlyPropositions = listOf(InformationProposition.RoleAt(1, RoleId("Imp"))),
        )

        val original = formal.toKnowledgeConstructionInput()
        val altered = alteredSecrets.toKnowledgeConstructionInput()

        assertEquals(original, altered)
        assertEquals(formal.toKnowledgeSafeWorldInput(), original.worldInput)
        assertEquals(listOf(publicFact), original.publicPropositions)

        val changedPublic = formal.copy(
            publicPropositions = listOf(InformationProposition.AliveAt(2, false)),
        ).toKnowledgeConstructionInput()
        assertNotEquals(original, changedPublic)
    }

    @Test fun `safe knowledge input rejects public proposition for unknown seat`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            KnowledgeConstructionInput(
                worldInput = formal.toKnowledgeSafeWorldInput(),
                publicPropositions = listOf(InformationProposition.AliveAt(99, true)),
            )
        }
    }

    @Test fun `safe knowledge core matches formal compatibility adapter`() {
        val observations = listOf(
            EpistemicObservation(
                observationId = "public-observation",
                snapshotId = formal.snapshotId,
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(3, true),
            ),
        )

        val viaFormal = A4PlayerKnowledgeFactory.createAll(
            formal,
            perceivedRoles,
            observations,
        )
        val viaSafeInput = A4PlayerKnowledgeFactory.createAll(
            formal.toKnowledgeConstructionInput(),
            perceivedRoles,
            observations,
        )

        assertEquals(viaFormal, viaSafeInput)
    }

    @Test fun `safe knowledge log path matches formal compatibility adapter`() {
        val log = EpistemicObservationLog().append(
            RecordedEpistemicObservation(
                recordId = "first-night-public",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(4, true),
            ),
        )

        val viaFormal = A4PlayerKnowledgeFactory.createAll(
            formal,
            perceivedRoles,
            log,
        )
        val viaSafeInput = A4PlayerKnowledgeFactory.createAll(
            formal.toKnowledgeConstructionInput(),
            perceivedRoles,
            log,
        )

        assertEquals(viaFormal, viaSafeInput)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `safe knowledge core rejects observations bound to another snapshot`() {
        val mismatched = EpistemicObservation(
            observationId = "wrong-snapshot",
            snapshotId = "snapshot-other",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, true),
        )

        A4PlayerKnowledgeFactory.createAll(
            formal.toKnowledgeConstructionInput(),
            perceivedRoles,
            listOf(mismatched),
        )
    }
}
