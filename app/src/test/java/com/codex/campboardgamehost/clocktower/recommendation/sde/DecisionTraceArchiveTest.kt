package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DecisionTraceArchiveTest {
    @Test
    fun `archive is immutable idempotent and keyed by canonical prefix decision revision and policy`() {
        val trace = deferredTrace()
        val archive = DecisionTraceArchive().append(trace)

        assertEquals(trace, archive.find(trace.archiveKey))
        assertEquals(archive, archive.append(trace))

        val conflictingSameKey = trace.copy(
            evidenceCheckpoint = EvidenceCheckpointId("different-evidence-checkpoint"),
        )
        assertThrows(IllegalArgumentException::class.java) {
            archive.append(conflictingSameKey)
        }
    }

    @Test
    fun `archive rejects a trace without a canonical global replay prefix`() {
        val trace = deferredTrace().copy(
            historyPrefixRef = SdeHistoricalPrefixRef.NotCaptured,
        )

        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchive(listOf(trace))
        }
    }

    private fun deferredTrace(): DecisionTrace {
        val candidateIds = listOf("candidate-a", "candidate-b")
        val revision = InformationDecisionRevision(
            gameStateRevision = 3L,
            playerInputRevision = 5L,
        )
        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            decisionId = "numeric|Empath|game-1|FirstNight|1|0|2|LIVING_EVIL_NEIGHBOURS",
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
            ),
            sourceRevision = revision,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "game-1",
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(
                    EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY,
                ),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-deferred")),
            ),
            policySelection = null,
        )
    }
}
