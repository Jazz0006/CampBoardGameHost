package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannedDecisionRefTest {
    private val sourceRevision = InformationDecisionRevision(
        gameStateRevision = 7L,
        playerInputRevision = 11L,
    )
    private val sourceSnapshot = InformationDecisionSnapshot(
        semanticIdentity = "information-decision|7|11|empath-0,empath-1",
        revision = sourceRevision,
        legalCandidateIds = listOf("empath-0", "empath-1"),
        recommendedCandidateIds = setOf("empath-0"),
    )

    @Test
    fun `information snapshot creates a disposable planned reference without copying candidate state`() {
        val planned = PlannedDecisionRef.fromInformationSnapshot(
            decisionId = "first-night-empath-2",
            snapshot = sourceSnapshot,
        )

        assertEquals("first-night-empath-2", planned.decisionId)
        assertEquals(sourceRevision, planned.sourceRevision)
        assertEquals(sourceSnapshot.semanticIdentity, planned.sourceSemanticIdentity)
        assertTrue(planned.isCurrentFor(sourceSnapshot))
    }

    @Test
    fun `game state revision change invalidates planned decision`() {
        val planned = PlannedDecisionRef.fromInformationSnapshot(
            decisionId = "first-night-empath-2",
            snapshot = sourceSnapshot,
        )

        assertFalse(
            planned.isCurrentFor(
                sourceRevision.copy(gameStateRevision = sourceRevision.gameStateRevision + 1),
            ),
        )
    }

    @Test
    fun `player input revision change invalidates planned decision`() {
        val planned = PlannedDecisionRef.fromInformationSnapshot(
            decisionId = "first-night-empath-2",
            snapshot = sourceSnapshot,
        )

        assertFalse(
            planned.isCurrentFor(
                sourceRevision.copy(playerInputRevision = sourceRevision.playerInputRevision + 1),
            ),
        )
    }

    @Test
    fun `candidate space identity change invalidates snapshot-bound planned decision at same revision`() {
        val planned = PlannedDecisionRef.fromInformationSnapshot(
            decisionId = "first-night-empath-2",
            snapshot = sourceSnapshot,
        )
        val changedCandidateSpace = sourceSnapshot.copy(
            semanticIdentity = "information-decision|7|11|empath-0,empath-2",
            legalCandidateIds = listOf("empath-0", "empath-2"),
        )

        assertFalse(planned.isCurrentFor(changedCandidateSpace))
    }

    @Test
    fun `revision-only planned decision remains independent of information candidate space`() {
        val planned = PlannedDecisionRef(
            decisionId = "night-mayor-redirect",
            sourceRevision = sourceRevision,
        )
        val unrelatedInformationSnapshot = sourceSnapshot.copy(
            semanticIdentity = "unrelated-candidate-space",
        )

        assertTrue(planned.isCurrentFor(sourceRevision))
        assertTrue(planned.isCurrentFor(unrelatedInformationSnapshot))
    }
}
