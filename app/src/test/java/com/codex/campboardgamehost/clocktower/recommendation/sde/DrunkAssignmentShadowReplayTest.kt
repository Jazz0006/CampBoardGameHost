package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingShownSeatAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingVisibleRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DrunkAssignmentShadowReplayTest {
    @Test
    fun `ordinary production replay registry remains frozen while Drunk experiment registry is separate`() {
        assertEquals(
            setOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
            DecisionPolicyReplayRegistry.production().supportedVersions,
        )

        val registry = DrunkAssignmentPolicyExperimentRegistry.experimental()

        assertEquals(
            setOf(PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1),
            registry.supportedVersions,
        )
        assertEquals(
            DrunkAssignmentExperimentPolicyDefinitions.SHADOW_V1,
            registry.requireRunner(PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1).definition,
        )
    }

    @Test
    fun `shadow replay input preserves dedicated features and legal candidate order`() {
        val shadow = g10Shadow()

        val input = DrunkAssignmentShadowReplayInput.fromShadow(shadow)

        assertEquals(shadow.decisionTrace.decisionId, input.decisionId)
        assertEquals(shadow.decisionTrace.lifecycleStage, input.lifecycleStage)
        assertEquals(shadow.decisionTrace.sourceRevision, input.sourceRevision)
        assertEquals(shadow.decisionTrace.historyPrefixRef, input.historyPrefixRef)
        assertEquals(shadow.decisionTrace.legalCandidateIds, input.legalCandidateIds)
        assertEquals(
            shadow.drunkAssignmentFeatureEvaluation,
            input.featureEvaluation,
        )
        assertEquals(shadow.replayInput.selectionSeed, input.selectionSeed)
    }

    @Test
    fun `Drunk shadow V1 defers on missing longitudinal capability and unauthorized ordering evidence`() {
        val shadow = g10Shadow()

        val run = DrunkAssignmentShadowV1ReplayRunner.run(
            featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
            decisionId = shadow.decisionTrace.decisionId,
            selectionSeed = shadow.replayInput.selectionSeed,
        )

        val deferred = run.policySnapshot as DecisionTracePolicySnapshot.Deferred
        assertEquals(
            PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1,
            deferred.policyVersion,
        )
        assertEquals(
            shadow.decisionTrace.legalCandidateIds,
            deferred.candidateIds,
        )
        assertEquals(
            setOf(
                PolicyDeferralCode(
                    "drunk-assignment-shadow-v1.longitudinal-narrative-missing-capability",
                ),
                PolicyDeferralCode(
                    "drunk-assignment-shadow-v1.ordering-evidence-not-authorized",
                ),
            ),
            deferred.reasons,
        )
        assertNull(run.policySelection)
    }

    @Test
    fun `G10 historical actual choice survives dedicated replay without becoming a policy label`() {
        val shadow = g10Shadow()
        val observedCandidateId = "setup:drunk-seat:seat-1"
        val sourceTrace = shadow.decisionTrace.copy(
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = observedCandidateId,
                source = InformationDecisionSource.MANUAL,
                manualOverride = false,
            ),
        )

        val record = DrunkAssignmentShadowReplayEngine.replay(
            sourceTrace = sourceTrace,
            recomputedInput = DrunkAssignmentShadowReplayInput.fromShadow(shadow),
            policyVersions = listOf(PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1),
        ).single()

        assertEquals(
            DrunkAssignmentExperimentPolicyDefinitions.SHADOW_V1.evidenceCheckpoint,
            record.evidenceCheckpoint,
        )
        assertEquals(sourceTrace.actualChoice, record.actualChoice)
        assertEquals(
            shadow.drunkAssignmentFeatureEvaluation,
            record.featureEvaluation,
        )
        assertTrue(record.policySnapshot is DecisionTracePolicySnapshot.Deferred)
        assertNull(record.policySelection)
        assertEquals(
            observedCandidateId,
            (record.actualChoice as DecisionTraceActualChoice.Committed).candidateId,
        )
    }

    @Test
    fun `multiple explicit experimental versions replay same dedicated input in requested order`() {
        val shadow = g10Shadow()
        val sourceTrace = shadow.decisionTrace
        val input = DrunkAssignmentShadowReplayInput.fromShadow(shadow)
        val versionA = PolicyVersion("DRUNK_TEST_A")
        val versionB = PolicyVersion("DRUNK_TEST_B")
        val registry = DrunkAssignmentPolicyExperimentRegistry(
            listOf(
                TestExperimentRunner(
                    definition = testDefinition(versionA, "drunk-test-a"),
                    selectedCandidateId = input.legalCandidateIds.first(),
                ),
                TestExperimentRunner(
                    definition = testDefinition(versionB, "drunk-test-b"),
                    selectedCandidateId = input.legalCandidateIds.last(),
                ),
            ),
        )

        val records = DrunkAssignmentShadowReplayEngine.replay(
            sourceTrace = sourceTrace,
            recomputedInput = input,
            policyVersions = listOf(versionB, versionA),
            registry = registry,
        )

        assertEquals(
            listOf(versionB, versionA),
            records.map { it.policySnapshot.policyVersion },
        )
        assertEquals(
            listOf(EvidenceCheckpointId("drunk-test-b"), EvidenceCheckpointId("drunk-test-a")),
            records.map(DrunkAssignmentShadowReplayRecord::evidenceCheckpoint),
        )
        assertEquals(
            listOf(input.legalCandidateIds.last(), input.legalCandidateIds.first()),
            records.map { it.policySelection?.candidateId },
        )
        assertTrue(records.all { it.featureEvaluation == input.featureEvaluation })
        assertTrue(records.all { it.actualChoice == sourceTrace.actualChoice })
    }

    @Test
    fun `Drunk experiment registry and replay fail closed for duplicate unknown and identity mismatch`() {
        val shadow = g10Shadow()
        val source = shadow.decisionTrace
        val input = DrunkAssignmentShadowReplayInput.fromShadow(shadow)
        val version = PolicyVersion("DRUNK_TEST_A")
        val unknown = PolicyVersion("DRUNK_TEST_UNKNOWN")
        val registry = DrunkAssignmentPolicyExperimentRegistry(
            listOf(
                TestExperimentRunner(
                    definition = testDefinition(version),
                    selectedCandidateId = input.legalCandidateIds.first(),
                ),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            DrunkAssignmentPolicyExperimentRegistry(
                listOf(
                    TestExperimentRunner(
                        definition = testDefinition(version),
                        selectedCandidateId = input.legalCandidateIds.first(),
                    ),
                    TestExperimentRunner(
                        definition = testDefinition(version),
                        selectedCandidateId = input.legalCandidateIds.last(),
                    ),
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            DrunkAssignmentShadowReplayEngine.replay(
                sourceTrace = source,
                recomputedInput = input,
                policyVersions = listOf(unknown),
                registry = registry,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DrunkAssignmentShadowReplayEngine.replay(
                sourceTrace = source,
                recomputedInput = input,
                policyVersions = listOf(version, version),
                registry = registry,
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            input.copy(
                lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    sequence = 7,
                ),
            )
        }

        val reducedFeatures = DrunkAssignmentFeatureEvaluation(
            candidates = input.featureEvaluation.candidates.dropLast(1),
        )
        val mismatches = listOf(
            input.copy(decisionId = "other-decision"),
            input.copy(sourceRevision = InformationDecisionRevision(99L, 7L)),
            input.copy(
                historyPrefixRef = SdeHistoricalPrefixRef.Global(
                    gameId = "other-game",
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
            ),
            input.copy(
                legalCandidateIds = input.legalCandidateIds.dropLast(1),
                featureEvaluation = reducedFeatures,
            ),
        )

        mismatches.forEach { mismatched ->
            assertThrows(IllegalArgumentException::class.java) {
                DrunkAssignmentShadowReplayEngine.replay(
                    sourceTrace = source,
                    recomputedInput = mismatched,
                    policyVersions = listOf(version),
                    registry = registry,
                )
            }
        }
    }

    @Test
    fun `dedicated replay is pure and leaves source trace and input unchanged`() {
        val shadow = g10Shadow()
        val source = shadow.decisionTrace
        val input = DrunkAssignmentShadowReplayInput.fromShadow(shadow)
        val sourceBefore = source.copy()
        val inputBefore = input.copy()

        DrunkAssignmentShadowReplayEngine.replay(
            sourceTrace = source,
            recomputedInput = input,
            policyVersions = listOf(PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1),
        )

        assertEquals(sourceBefore, source)
        assertEquals(inputBefore, input)
    }

    private class TestExperimentRunner(
        override val definition: StorytellerPolicyDefinition,
        private val selectedCandidateId: String,
    ) : DrunkAssignmentPolicyExperimentRunner {
        override fun run(
            featureEvaluation: DrunkAssignmentFeatureEvaluation,
            decisionId: String,
            selectionSeed: Long,
        ): DecisionPolicyReplayRun {
            require(selectedCandidateId in featureEvaluation.candidateIds)
            val candidateIds = featureEvaluation.candidateIds
            val equivalence = if (candidateIds.size == 1) {
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
                            equivalenceState = equivalence,
                        )
                    },
                ),
                policySelection = PolicySelection(
                    policyVersion = policyVersion,
                    candidateId = selectedCandidateId,
                    method = definition.selectionMethod,
                ),
            )
        }
    }

    private fun testDefinition(
        policyVersion: PolicyVersion,
        evidenceCheckpoint: String = "drunk-test",
    ): StorytellerPolicyDefinition =
        StorytellerPolicyDefinition(
            policyVersion = policyVersion,
            evidenceCheckpoint = EvidenceCheckpointId(evidenceCheckpoint),
            selectionMethod = PolicySelectionMethod.SEEDED_HASH_V1,
        )

    private fun g10Shadow(): DrunkSetupShadowEvaluation {
        val ruleset = canonicalRuleset()
        return DrunkSetupShadowAdapter.evaluate(
            gameId = "evidence:c1d:g10-game2",
            intermediateSetup = g10Game2IntermediateSetup(),
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )
    }

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb3b3-g10",
            schemaVersion = 1,
            presetId = "historical-replay-only:g10-game2",
            playerCount = 9,
            gameSeed = 20_260_929L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf(
                    "empath",
                    "undertaker",
                    "librarian",
                    "monk",
                    "mayor",
                    "virgin",
                ),
                outsiderRoleIds = listOf("butler"),
                minionRoleIds = listOf("spy"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "imp"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "undertaker"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "librarian"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "spy"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "monk"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", "mayor"),
                TroubleBrewingShownSeatAssignment(8, "Seat 8", "virgin"),
                TroubleBrewingShownSeatAssignment(9, "Seat 9", "butler"),
            ),
        )

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
