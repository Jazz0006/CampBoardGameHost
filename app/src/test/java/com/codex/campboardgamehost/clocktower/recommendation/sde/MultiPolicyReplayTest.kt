package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiPolicyReplayTest {
    @Test
    fun productionReplayRegistryExposesOnlyTheRealV1Policy() {
        val registry = DecisionPolicyReplayRegistry.production()

        assertEquals(
            setOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
            registry.supportedVersions,
        )
        assertEquals(
            StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1,
            registry.requireRunner(PolicyVersions.BEGINNER_CONSERVATIVE_V1).definition,
        )
    }

    @Test
    fun v1ReplayRunnerPreservesExistingPolicyAndSelectorSemantics() {
        val features = recomputedFeatures()
        val expectedEvaluation = BeginnerConservativeV1Policy.evaluate(features)
        val expectedSelection = BeginnerConservativeV1Selector.select(
            evaluation = expectedEvaluation,
            decisionId = DECISION_ID,
            selectionSeed = 17L,
        )

        val replay = BeginnerConservativeV1ReplayRunner.run(
            featureEvaluation = features,
            decisionId = DECISION_ID,
            selectionSeed = 17L,
        )

        assertEquals(expectedEvaluation.toDecisionTracePolicySnapshot(), replay.policySnapshot)
        assertEquals(expectedSelection, replay.policySelection)
    }

    @Test
    fun productionV1ReplayUsesFrozenPolicyDefinitionEvidenceCheckpoint() {
        val trace = MultiPolicyReplayEngine.replay(
            sourceTrace = sourceTrace(),
            recomputedInput = replayInput(),
            policyVersions = listOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
        ).single()

        assertEquals(
            StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1.evidenceCheckpoint,
            trace.evidenceCheckpoint,
        )
    }

    @Test
    fun `replay persists freshly recomputed typed truth credibility instead of trusting historical trace features`() {
        val source = ConfirmationChannelRef.Source(
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
        )
        val base = replayInput()
        val ready = base.featureEvaluation as DecisionFeatureEvaluation.Ready
        val enriched = DecisionFeatureEvaluation.Ready(
            ready.candidates.map { candidate ->
                candidate.copy(
                    features = candidate.features.copy(
                        truthCredibility = FeatureProjection.Projected(
                            TruthCredibilityFeatures(
                                truthDangerSources = setOf(
                                    TruthDangerSourceImpact(
                                        source = source,
                                        exactWorldReduction = BigInteger.ONE,
                                        strategicWorldKeysRemoved = emptySet(),
                                        demonSeatsRemoved = emptySet(),
                                    ),
                                ),
                            ),
                        ),
                    ),
                )
            },
        )
        val recomputed = base.copy(featureEvaluation = enriched)

        val trace = MultiPolicyReplayEngine.replay(
            sourceTrace = sourceTrace(),
            recomputedInput = recomputed,
            policyVersions = listOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
        ).single()

        assertEquals(enriched, trace.featureEvaluation)
        assertTrue(sourceTrace().featureEvaluation is DecisionFeatureEvaluation.Deferred)
        val projected =
            ((trace.featureEvaluation as DecisionFeatureEvaluation.Ready)
                .candidates
                .first()
                .features
                .truthCredibility as FeatureProjection.Projected)
                .value
        assertTrue(projected.hasTypedMaterial)
        assertEquals(setOf(source), projected.truthDangerSources.mapTo(linkedSetOf()) { it.source })
    }

    @Test
    fun multipleExplicitPolicyVersionsReplayTheSameRecomputedCanonicalInputInRequestedOrder() {
        val source = sourceTrace()
        val recomputed = replayInput()
        val versionA = PolicyVersion("TEST_REPLAY_A")
        val versionB = PolicyVersion("TEST_REPLAY_B")
        val registry = DecisionPolicyReplayRegistry(
            listOf(
                TestReplayRunner(
                    definition = testDefinition(versionA, "test-evidence-a"),
                    selectedCandidateId = "candidate-a",
                ),
                TestReplayRunner(
                    definition = testDefinition(versionB, "test-evidence-b"),
                    selectedCandidateId = "candidate-b",
                ),
            ),
        )

        val traces = MultiPolicyReplayEngine.replay(
            sourceTrace = source,
            recomputedInput = recomputed,
            policyVersions = listOf(versionB, versionA),
            registry = registry,
        )

        assertEquals(listOf(versionB, versionA), traces.map { it.policySnapshot.policyVersion })
        assertEquals(
            listOf(EvidenceCheckpointId("test-evidence-b"), EvidenceCheckpointId("test-evidence-a")),
            traces.map(DecisionTrace::evidenceCheckpoint),
        )
        assertEquals(listOf("candidate-b", "candidate-a"), traces.map { it.policySelection?.candidateId })
        assertTrue(traces.all { it.decisionId == source.decisionId })
        assertTrue(traces.all { it.lifecycleStage == source.lifecycleStage })
        assertTrue(traces.all { it.sourceRevision == source.sourceRevision })
        assertTrue(traces.all { it.historyPrefixRef == source.historyPrefixRef })
        assertTrue(traces.all { it.legalCandidateIds == source.legalCandidateIds })
        assertTrue(traces.all { it.featureEvaluation == recomputed.featureEvaluation })
        assertTrue(traces.all { it.actualChoice == source.actualChoice })

        assertNotEquals(source.featureEvaluation, recomputed.featureEvaluation)
        assertTrue(source.featureEvaluation is DecisionFeatureEvaluation.Deferred)

        val archive = traces.fold(DecisionTraceArchive()) { current, trace -> current.append(trace) }
        assertEquals(2, archive.traces.size)
        assertEquals(2, archive.traces.map { it.archiveKey }.distinct().size)
    }

    @Test
    fun replayRegistryRejectsDuplicateRunnerVersions() {
        val version = PolicyVersion("TEST_REPLAY_A")
        assertThrows(IllegalArgumentException::class.java) {
            DecisionPolicyReplayRegistry(
                listOf(
                    TestReplayRunner(testDefinition(version), "candidate-a"),
                    TestReplayRunner(testDefinition(version), "candidate-b"),
                ),
            )
        }
    }

    @Test
    fun replayRejectsUnknownAndDuplicatePolicyRequests() {
        val source = sourceTrace()
        val recomputed = replayInput()
        val versionA = PolicyVersion("TEST_REPLAY_A")
        val versionB = PolicyVersion("TEST_REPLAY_B")
        val registry = DecisionPolicyReplayRegistry(
            listOf(TestReplayRunner(testDefinition(versionA), "candidate-a")),
        )

        assertThrows(IllegalArgumentException::class.java) {
            MultiPolicyReplayEngine.replay(
                sourceTrace = source,
                recomputedInput = recomputed,
                policyVersions = listOf(versionB),
                registry = registry,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MultiPolicyReplayEngine.replay(
                sourceTrace = source,
                recomputedInput = recomputed,
                policyVersions = listOf(versionA, versionA),
                registry = registry,
            )
        }
    }

    @Test
    fun replayFailsClosedWhenSourceTraceDoesNotMatchRecomputedCanonicalIdentity() {
        val source = sourceTrace()
        val base = replayInput()
        val version = PolicyVersion("TEST_REPLAY_A")
        val registry = DecisionPolicyReplayRegistry(
            listOf(TestReplayRunner(testDefinition(version), "candidate-a")),
        )

        val mismatches = listOf(
            base.copy(decisionId = "other-decision"),
            base.copy(sourceRevision = InformationDecisionRevision(99, 7)),
            base.copy(
                lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    sequence = 8,
                ),
            ),
            base.copy(
                historyPrefixRef = SdeHistoricalPrefixRef.Global(
                    gameId = "other-game",
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
            ),
            base.copy(
                legalCandidateIds = listOf("candidate-a"),
                featureEvaluation = readyFeatures(listOf("candidate-a")),
            ),
        )

        mismatches.forEach { mismatched ->
            assertThrows(IllegalArgumentException::class.java) {
                MultiPolicyReplayEngine.replay(
                    sourceTrace = source,
                    recomputedInput = mismatched,
                    policyVersions = listOf(version),
                    registry = registry,
                )
            }
        }
    }

    @Test
    fun replayIsPureAndLeavesSourceTraceAndInputUnchanged() {
        val source = sourceTrace()
        val recomputed = replayInput()
        val sourceBefore = source.copy()
        val inputBefore = recomputed.copy()
        val version = PolicyVersion("TEST_REPLAY_A")

        MultiPolicyReplayEngine.replay(
            sourceTrace = source,
            recomputedInput = recomputed,
            policyVersions = listOf(version),
            registry = DecisionPolicyReplayRegistry(
                listOf(TestReplayRunner(testDefinition(version), "candidate-a")),
            ),
        )

        assertEquals(sourceBefore, source)
        assertEquals(inputBefore, recomputed)
    }

    private class TestReplayRunner(
        override val definition: StorytellerPolicyDefinition,
        private val selectedCandidateId: String,
    ) : DecisionPolicyReplayRunner {
        override fun run(
            featureEvaluation: DecisionFeatureEvaluation,
            decisionId: String,
            selectionSeed: Long,
        ): DecisionPolicyReplayRun {
            val candidateIds = featureEvaluation.candidateIds
            require(selectedCandidateId in candidateIds)
            val tie = if (candidateIds.size == 1) {
                PolicyEquivalenceState.Unique
            } else {
                PolicyEquivalenceState.Tied(candidateIds.toSet())
            }
            return DecisionPolicyReplayRun(
                policySnapshot = DecisionTracePolicySnapshot.Ready(
                    policyVersion = policyVersion,
                    evaluations = candidateIds.map { candidateId ->
                        PolicyEvaluation(
                            candidateId = candidateId,
                            policyVersion = policyVersion,
                            disposition = PolicyDisposition.SURVIVOR,
                            equivalenceState = tie,
                        )
                    },
                ),
                policySelection = PolicySelection(
                    policyVersion = policyVersion,
                    candidateId = selectedCandidateId,
                    method = PolicySelectionMethod.SEEDED_HASH_V1,
                ),
            )
        }
    }

    private fun testDefinition(
        policyVersion: PolicyVersion,
        evidenceCheckpoint: String = "test-evidence",
    ): StorytellerPolicyDefinition = StorytellerPolicyDefinition(
        policyVersion = policyVersion,
        evidenceCheckpoint = EvidenceCheckpointId(evidenceCheckpoint),
        selectionMethod = PolicySelectionMethod.SEEDED_HASH_V1,
    )

    private fun sourceTrace(): DecisionTrace {
        val candidateIds = CANDIDATE_IDS
        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("historical-capture"),
            decisionId = DECISION_ID,
            lifecycleStage = LIFECYCLE,
            sourceRevision = REVISION,
            historyPrefixRef = PREFIX,
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("historical-deferred")),
            ),
            policySelection = null,
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = "candidate-b",
                source = InformationDecisionSource.MANUAL,
                manualOverride = true,
                overrideReason = DecisionTraceOverrideReason(code = "historical-manual"),
            ),
        )
    }

    private fun replayInput(): MultiPolicyReplayInput = MultiPolicyReplayInput(
        decisionId = DECISION_ID,
        lifecycleStage = LIFECYCLE,
        sourceRevision = REVISION,
        historyPrefixRef = PREFIX,
        legalCandidateIds = CANDIDATE_IDS,
        featureEvaluation = recomputedFeatures(),
        selectionSeed = 17L,
    )

    private fun recomputedFeatures(): DecisionFeatureEvaluation =
        readyFeatures(CANDIDATE_IDS)

    private fun readyFeatures(candidateIds: List<String>): DecisionFeatureEvaluation.Ready =
        DecisionFeatureEvaluation.Ready(
            candidates = candidateIds.mapIndexed { index, candidateId ->
                CandidateDecisionFeatures(
                    candidateId = candidateId,
                    features = DecisionFeatures(
                        strategic = FeatureProjection.Projected(
                            StrategicDecisionFeatures(
                                demonCoverRetention = StrategicRatio.Defined(1, 1),
                                evilTopologyRetention = StrategicRatio.Defined(
                                    numerator = if (index == 0 && candidateIds.size > 1) 0 else 1,
                                    denominator = 1,
                                ),
                                evilCoverRetention = StrategicRatio.Defined(1, 1),
                                forcedGoodFraction = StrategicRatio.Defined(0, 8),
                                forcedEvilFraction = StrategicRatio.Defined(0, 8),
                                forcedGoodSeats = emptySet(),
                                forcedEvilSeats = emptySet(),
                            ),
                        ),
                    ),
                )
            },
        )

    private companion object {
        val CANDIDATE_IDS = listOf("candidate-a", "candidate-b")
        const val DECISION_ID = "numeric|Empath|game-1|FirstNight|1|7|2|LIVING_EVIL_NEIGHBOURS"
        val REVISION = InformationDecisionRevision(3, 7)
        val LIFECYCLE = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 7,
        )
        val PREFIX = SdeHistoricalPrefixRef.Global(
            gameId = "game-1",
            actionRefs = emptyList(),
            observationRefs = emptyList(),
        )
    }
}