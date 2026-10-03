package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationDecisionExportV1Test {
    @Test
    fun drunkExportSeparatesObservedChoiceFromLegalUnchosenWithoutInventingRejection() {
        val snapshot = setupSnapshot()
        val context = DrunkAssignmentDecisionContext(
            snapshot = snapshot,
            sourceRevision = REVISION,
            legalCandidates = listOf(
                TroubleBrewingDrunkCandidateRef(seat = 1, shownRoleId = "Empath"),
                TroubleBrewingDrunkCandidateRef(seat = 2, shownRoleId = "Monk"),
            ),
        )
        val candidateIds = context.legalCandidateIds
        val features = drunkFeatures(candidateIds)
        val prefix = SdeHistoricalPrefixRef.Global(
            gameId = snapshot.gameId,
            actionRefs = emptyList(),
            observationRefs = emptyList(),
        )
        val record = DrunkAssignmentShadowReplayRecord(
            evidenceCheckpoint = EvidenceCheckpointId("q04-test"),
            decisionId = context.decisionId,
            lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
            sourceRevision = REVISION,
            historyPrefixRef = prefix,
            legalCandidateIds = candidateIds,
            featureEvaluation = features,
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.DRUNK_ASSIGNMENT_Q04_V1,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-only")),
            ),
            policySelection = null,
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = candidateIds[1],
                source = InformationDecisionSource.MANUAL,
                manualOverride = true,
            ),
        )

        val export = RecommendationDecisionExportV1.fromDrunkAssignment(
            context = context,
            replayRecords = listOf(record),
        )

        assertEquals(RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT, export.decisionType)
        assertEquals(candidateIds, export.inputEligible.legalCandidateIds)
        assertTrue(export.inputEligible.context is RecommendationDecisionContextV1.DrunkAssignment)
        assertTrue(export.inputEligible.featureProjection is RecommendationFeatureProjectionV1.DrunkAssignment)
        assertEquals(
            listOf(
                RecommendationHistoricalDomainRelationV1(
                    candidateId = candidateIds[0],
                    kind = RecommendationHistoricalDomainRelationKindV1.LEGAL_UNCHOSEN,
                ),
                RecommendationHistoricalDomainRelationV1(
                    candidateId = candidateIds[1],
                    kind = RecommendationHistoricalDomainRelationKindV1.OBSERVED_CHOICE,
                ),
            ),
            export.targetOrLabel.candidateRelations,
        )
        assertEquals(
            listOf(PolicyVersions.DRUNK_ASSIGNMENT_Q04_V1),
            export.evaluationMetadata.policyTraces.map { it.policyVersion },
        )
        assertEquals(listOf(EvidenceCheckpointId("q04-test")), export.provenanceOnly.policyEvidenceCheckpoints)
    }

    @Test
    fun pairExportPreservesCanonicalPrefixRevisionAndPolicyReplayOutsideInputEligiblePayload() {
        val snapshot = runtimeSnapshot()
        val context = TroubleBrewingFirstNightPairDecisionContext(
            snapshot = snapshot,
            naturalPairGameState = GameState(
                script = ScriptId("trouble_brewing"),
                players = emptyList(),
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = emptyList(),
        )
        val request = PairInformationShadowReplayRequest(
            decisionId = "pair:test-game:librarian",
            context = context,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 3,
            ),
        )
        val candidateIds = listOf("pair:a", "pair:b")
        val trace = DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("c5d-test"),
            decisionId = request.decisionId,
            lifecycleStage = request.lifecycleStage,
            sourceRevision = REVISION,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = snapshot.gameId,
                actionRefs = listOf(SdeHistoricalActionRef("setup", 0L)),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-only")),
            ),
            policySelection = null,
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = candidateIds[0],
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                manualOverride = false,
            ),
        )

        val export = RecommendationDecisionExportV1.fromFirstNightPairInformation(
            request = request,
            replayTraces = listOf(trace),
        )

        assertEquals(RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION, export.decisionType)
        assertEquals(REVISION, export.inputEligible.sourceRevision)
        assertEquals(trace.historyPrefixRef, export.inputEligible.historyPrefixRef)
        assertEquals(candidateIds, export.inputEligible.legalCandidateIds)
        assertTrue(export.inputEligible.context is RecommendationDecisionContextV1.FirstNightPairInformation)
        assertTrue(export.inputEligible.featureProjection is RecommendationFeatureProjectionV1.StandardDecision)
        assertEquals(trace.actualChoice, export.targetOrLabel.actualChoice)
        assertEquals(
            listOf(PolicyVersions.BEGINNER_CONSERVATIVE_V2),
            export.evaluationMetadata.policyTraces.map { it.policyVersion },
        )
        assertEquals(snapshot.gameId, export.provenanceOnly.gameId)
        assertEquals("trouble_brewing", export.provenanceOnly.scriptId)
    }

    @Test
    fun pendingActualChoiceDoesNotManufactureTrainingRelations() {
        val snapshot = runtimeSnapshot()
        val context = TroubleBrewingFirstNightPairDecisionContext(
            snapshot = snapshot,
            naturalPairGameState = GameState(
                script = ScriptId("trouble_brewing"),
                players = emptyList(),
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = emptyList(),
        )
        val request = PairInformationShadowReplayRequest(
            decisionId = "pair:test-game:librarian",
            context = context,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 3,
            ),
        )
        val candidateIds = listOf("pair:a", "pair:b")
        val trace = deferredPairTrace(request, candidateIds, DecisionTraceActualChoice.Pending)

        val export = RecommendationDecisionExportV1.fromFirstNightPairInformation(
            request = request,
            replayTraces = listOf(trace),
        )

        assertTrue(export.targetOrLabel.candidateRelations.isEmpty())
    }

    @Test
    fun pairExportFailsClosedWhenReplayRevisionDoesNotMatchSnapshot() {
        val snapshot = runtimeSnapshot()
        val context = TroubleBrewingFirstNightPairDecisionContext(
            snapshot = snapshot,
            naturalPairGameState = GameState(
                script = ScriptId("trouble_brewing"),
                players = emptyList(),
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = emptyList(),
        )
        val request = PairInformationShadowReplayRequest(
            decisionId = "pair:test-game:librarian",
            context = context,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 3,
            ),
        )
        val candidateIds = listOf("pair:a", "pair:b")
        val trace = deferredPairTrace(
            request = request,
            candidateIds = candidateIds,
            actualChoice = DecisionTraceActualChoice.Pending,
            revision = InformationDecisionRevision(9, 9),
        )

        assertThrows(IllegalArgumentException::class.java) {
            RecommendationDecisionExportV1.fromFirstNightPairInformation(
                request = request,
                replayTraces = listOf(trace),
            )
        }
    }

    private fun deferredPairTrace(
        request: PairInformationShadowReplayRequest,
        candidateIds: List<String>,
        actualChoice: DecisionTraceActualChoice,
        revision: InformationDecisionRevision = REVISION,
    ): DecisionTrace =
        DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("c5d-test"),
            decisionId = request.decisionId,
            lifecycleStage = request.lifecycleStage,
            sourceRevision = revision,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = request.context.snapshot.gameId,
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-only")),
            ),
            policySelection = null,
            actualChoice = actualChoice,
        )

    private fun drunkFeatures(candidateIds: List<String>): DrunkAssignmentFeatureEvaluation =
        DrunkAssignmentFeatureEvaluation(
            candidates = candidateIds.map { candidateId ->
                CandidateDrunkAssignmentFeatures(
                    candidateId = candidateId,
                    features = DrunkAssignmentFeatures(
                        topology = FeatureProjection.Projected(
                            DrunkAssignmentTopologyFeatures(
                                previousSeat = 1,
                                nextSeat = 2,
                                adjacentEvilSeats = emptySet(),
                                adjacentDemonSeats = emptySet(),
                                adjacentMinionSeats = emptySet(),
                            ),
                        ),
                        firstNightInformationOpportunity = FeatureProjection.Projected(
                            DrunkAssignmentFirstNightInformationOpportunityFeatures(
                                factors = emptyList(),
                            ),
                        ),
                        longitudinalNarrativeOpportunity = FeatureProjection.Unavailable(
                            FeatureUnavailableReason.MISSING_CAPABILITY,
                        ),
                        limitations = DrunkAssignmentFeatureLimitations(
                            excludedPlayerControlledElements = emptySet(),
                            deferredComplexities = emptySet(),
                        ),
                    ),
                )
            },
        )

    private fun setupSnapshot(): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = "test-game",
            gameSeed = 17L,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                phase = SnapshotField.NotApplicable,
                round = SnapshotField.NotApplicable,
            ),
            grimoireSeats = listOf(
                TroubleBrewingSnapshotSeat(
                    seat = 1,
                    shownRoleId = SnapshotField.Known("Empath"),
                    actualRoleId = SnapshotField.Uncommitted,
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.NotApplicable,
                ),
                TroubleBrewingSnapshotSeat(
                    seat = 2,
                    shownRoleId = SnapshotField.Known("Monk"),
                    actualRoleId = SnapshotField.Uncommitted,
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.NotApplicable,
                ),
            ),
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(true),
                drunkAssignmentSeat = SnapshotField.Uncommitted,
            ),
        )

    private fun runtimeSnapshot(): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = "test-game",
            gameSeed = 17L,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(StorytellerPhase.FIRST_NIGHT),
                round = SnapshotField.Known(1),
                gameStateRevision = SnapshotField.Known(REVISION.gameStateRevision),
                playerInputRevision = SnapshotField.Known(REVISION.playerInputRevision),
            ),
            grimoireSeats = listOf(
                TroubleBrewingSnapshotSeat(
                    seat = 1,
                    shownRoleId = SnapshotField.Known("Librarian"),
                    actualRoleId = SnapshotField.Known("Librarian"),
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                ),
            ),
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(false),
                drunkAssignmentSeat = SnapshotField.NotApplicable,
            ),
        )

    companion object {
        private val REVISION = InformationDecisionRevision(
            gameStateRevision = 4,
            playerInputRevision = 2,
        )
    }
}
