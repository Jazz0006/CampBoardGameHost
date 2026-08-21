package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KnowledgeSafeWorldInputTest {
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)

    @Test fun `projection exposes structural identity without copying storyteller truth`() {
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

        val originalInput = formal.toKnowledgeSafeWorldInput()
        val alteredInput = alteredSecrets.toKnowledgeSafeWorldInput()

        assertEquals(originalInput, alteredInput)
        assertEquals(formal.snapshotId, originalInput.formalSnapshotId)
        assertEquals(formal.rulesetRef, originalInput.rulesetRef)
        assertEquals(formal.players.map { it.seat }.sorted(), originalInput.playerSeats)
    }

    @Test fun `safe input core build matches formal adapter build`() {
        val knowledge = knowledge(formal)
        val definitions = TroubleBrewingFixtures.fullRoleDefinitions()

        val viaFormal = A4PlayerWorldSetRuntime().build(
            formal,
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            definitions,
        )
        val viaSafeInput = A4PlayerWorldSetRuntime().build(
            formal.toKnowledgeSafeWorldInput(),
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            definitions,
        )

        assertEquals(viaFormal.selected.cardinality(), viaSafeInput.selected.cardinality())
        assertEquals(viaFormal.selected.identity, viaSafeInput.selected.identity)
        assertEquals(viaFormal.selected.possibleRoles(1), viaSafeInput.selected.possibleRoles(1))
    }

    @Test fun `safe input core rejects knowledge bound to another formal snapshot`() {
        val mismatched = knowledge(formal).copy(formalSnapshotId = "snapshot-other")

        expectIllegalArgument("mismatched formal snapshot") {
            A4PlayerWorldSetRuntime().build(
                formal.toKnowledgeSafeWorldInput(),
                mismatched,
                EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                TroubleBrewingFixtures.fullRoleDefinitions(),
            )
        }
    }

    @Test fun `safe input rejects noncanonical player seats`() {
        expectIllegalArgument("duplicate player seats") {
            KnowledgeSafeWorldInput(formal.snapshotId, formal.rulesetRef, listOf(1, 1, 2))
        }
        expectIllegalArgument("unsorted player seats") {
            KnowledgeSafeWorldInput(formal.snapshotId, formal.rulesetRef, listOf(2, 1, 3))
        }
        expectIllegalArgument("noncontiguous player seats") {
            KnowledgeSafeWorldInput(formal.snapshotId, formal.rulesetRef, listOf(2, 3, 4, 5, 6))
        }
    }

    @Test fun `safe input core validates recipient only against structural seats`() {
        val outsideRecipient = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "outside-recipient",
            formalSnapshotId = formal.snapshotId,
            recipientSeat = 99,
            perceivedRole = RoleId("Chef"),
        )

        expectIllegalArgument("recipient outside structural seats") {
            A4PlayerWorldSetRuntime().build(
                formal.toKnowledgeSafeWorldInput(),
                outsideRecipient,
                EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                TroubleBrewingFixtures.fullRoleDefinitions(),
            )
        }
    }

    private fun knowledge(formal: FormalGameState) = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "safe-input-player-one",
        formalSnapshotId = formal.snapshotId,
        recipientSeat = 1,
        perceivedRole = RoleId("Chef"),
    )

    private fun expectIllegalArgument(label: String, block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException for $label")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed boundary.
        }
    }
}
