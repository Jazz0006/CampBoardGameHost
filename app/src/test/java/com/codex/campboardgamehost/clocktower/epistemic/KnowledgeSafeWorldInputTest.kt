package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
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

    private fun expectIllegalArgument(label: String, block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException for $label")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed boundary.
        }
    }
}
