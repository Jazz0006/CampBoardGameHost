package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerDecisionFoundationTest {
    @Test
    fun `generic pending decision accepts only current legal candidate IDs`() {
        val revision = StorytellerDecisionRevision(gameStateRevision = 4, playerInputRevision = 9)
        val decision = PendingStorytellerDecision(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "game-res-1b",
                requestId = "decision-1",
            ),
            revision = revision,
            legalCandidates = listOf(
                StorytellerDecisionCandidate(candidateId = "candidate-a", payload = 1),
                StorytellerDecisionCandidate(candidateId = "candidate-b", payload = 2),
            ),
        )

        val accepted = decision.confirm("candidate-b", revision)
        assertTrue(accepted is StorytellerDecisionConfirmation.Confirmed)
        accepted as StorytellerDecisionConfirmation.Confirmed
        assertEquals(2, accepted.candidate.payload)

        assertEquals(
            StorytellerDecisionConfirmation.Blocked(StorytellerDecisionBlockReason.ILLEGAL_CANDIDATE),
            decision.confirm("candidate-x", revision),
        )
        assertEquals(
            StorytellerDecisionConfirmation.Blocked(StorytellerDecisionBlockReason.STALE_CONTEXT),
            decision.confirm("candidate-a", revision.copy(gameStateRevision = 5)),
        )
    }
}
