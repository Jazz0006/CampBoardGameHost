package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
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
    fun drunkExportSeparatesObservedChoiceFromLegalUnchosenWithoutPolicyReplay() {
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
        val export = RecommendationDecisionExportV1.fromDrunkAssignment(
            context = context,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = snapshot.gameId,
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            featureEvaluation = drunkFeatures(candidateIds),
            actualChoice = RecommendationHistoricalChoiceV1.Committed(
                candidateId = candidateIds[1],
                source = InformationDecisionSource.MANUAL,
                manualOverride = true,
            ),
            evidenceReferences = listOf("res4:q04-drunk-empath-vs-monk"),
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
            listOf("res4:q04-drunk-empath-vs-monk"),
            export.evaluationMetadata.evidenceReferences,
        )
        assertEquals(
            export.evaluationMetadata.evidenceReferences,
            export.provenanceOnly.evidenceReferences,
        )
    }

    @Test
    fun pairExportPreservesCanonicalPrefixRevisionAndEvidenceWithoutPolicyIdentity() {
        val snapshot = runtimeSnapshot()
        val context = pairContext(snapshot)
        val request = pairRequest(context)
        val candidates = pairLegalCandidates()
        val candidateIds = candidates.map(PairInformationLegalCandidate::candidateId)
        val prefix = SdeHistoricalPrefixRef.Global(
            gameId = snapshot.gameId,
            actionRefs = listOf(SdeHistoricalActionRef("setup", 0L)),
            observationRefs = emptyList(),
        )
        val features = DecisionFeatureEvaluation.Deferred(
            candidateIds = candidateIds,
            missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
        )
        val choice = RecommendationHistoricalChoiceV1.Committed(
            candidateId = candidateIds[0],
            source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
            manualOverride = false,
        )

        val export = RecommendationDecisionExportV1.fromFirstNightPairInformation(
            request = request,
            legalCandidates = candidates,
            sourceRevision = REVISION,
            historyPrefixRef = prefix,
            featureEvaluation = features,
            actualChoice = choice,
            evidenceReferences = listOf("res4:g10-librarian-future-flexibility"),
        )

        assertEquals(RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION, export.decisionType)
        assertEquals(REVISION, export.inputEligible.sourceRevision)
        assertEquals(prefix, export.inputEligible.historyPrefixRef)
        assertEquals(candidateIds, export.inputEligible.legalCandidateIds)
        assertTrue(export.inputEligible.context is RecommendationDecisionContextV1.FirstNightPairInformation)
        val pairContext = export.inputEligible.context as RecommendationDecisionContextV1.FirstNightPairInformation
        assertEquals(listOf("pair:a", "pair:b"), pairContext.legalCandidates.map { it.candidateId })
        assertEquals("Saint", pairContext.legalCandidates[0].shownRoleId)
        assertEquals(listOf(2, 3), pairContext.legalCandidates[0].candidateSeats)
        assertEquals(SemanticTruth.TRUE, pairContext.legalCandidates[0].semanticTruth)
        assertEquals(
            listOf(
                RegistrationFact(
                    interactionId = "pair:test-game:librarian",
                    subjectSeat = 2,
                    registeredRole = RoleId("Saint"),
                    registrationQuestion = RegistrationQuestion.ROLE,
                    reason = RegistrationReason.SPY_ABILITY,
                ),
            ),
            pairContext.legalCandidates[0].registrations,
        )
        assertEquals(choice, export.targetOrLabel.actualChoice)
        assertEquals(
            listOf("res4:g10-librarian-future-flexibility"),
            export.provenanceOnly.evidenceReferences,
        )
    }

    @Test
    fun pendingActualChoiceDoesNotManufactureTrainingRelations() {
        val snapshot = runtimeSnapshot()
        val candidates = pairLegalCandidates()
        val export = RecommendationDecisionExportV1.fromFirstNightPairInformation(
            request = pairRequest(pairContext(snapshot)),
            legalCandidates = candidates,
            sourceRevision = REVISION,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = snapshot.gameId,
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidates.map(PairInformationLegalCandidate::candidateId),
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            actualChoice = RecommendationHistoricalChoiceV1.Pending,
        )

        assertTrue(export.targetOrLabel.candidateRelations.isEmpty())
    }

    @Test
    fun pairExportFailsClosedWhenSemanticPayloadOrderDoesNotMatchFeatureDomain() {
        val snapshot = runtimeSnapshot()
        val candidates = pairLegalCandidates()
        val canonicalIds = candidates.map(PairInformationLegalCandidate::candidateId)

        assertThrows(IllegalArgumentException::class.java) {
            RecommendationDecisionExportV1.fromFirstNightPairInformation(
                request = pairRequest(pairContext(snapshot)),
                legalCandidates = candidates.reversed(),
                sourceRevision = REVISION,
                historyPrefixRef = SdeHistoricalPrefixRef.Global(
                    gameId = snapshot.gameId,
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
                featureEvaluation = DecisionFeatureEvaluation.Deferred(
                    candidateIds = canonicalIds,
                    missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
                ),
                actualChoice = RecommendationHistoricalChoiceV1.Pending,
            )
        }
    }

    @Test
    fun pairExportFailsClosedWhenRevisionDoesNotMatchSnapshot() {
        val snapshot = runtimeSnapshot()
        val candidates = pairLegalCandidates()

        assertThrows(IllegalArgumentException::class.java) {
            RecommendationDecisionExportV1.fromFirstNightPairInformation(
                request = pairRequest(pairContext(snapshot)),
                legalCandidates = candidates,
                sourceRevision = InformationDecisionRevision(9, 9),
                historyPrefixRef = SdeHistoricalPrefixRef.Global(
                    gameId = snapshot.gameId,
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
                featureEvaluation = DecisionFeatureEvaluation.Deferred(
                    candidateIds = candidates.map(PairInformationLegalCandidate::candidateId),
                    missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
                ),
                actualChoice = RecommendationHistoricalChoiceV1.Pending,
            )
        }
    }

    private fun pairContext(snapshot: TroubleBrewingGameSnapshotV1) =
        TroubleBrewingFirstNightPairDecisionContext(
            snapshot = snapshot,
            naturalPairGameState = GameState(
                script = ScriptId("trouble_brewing"),
                players = emptyList(),
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = emptyList(),
        )

    private fun pairRequest(context: TroubleBrewingFirstNightPairDecisionContext) =
        RecommendationFirstNightPairExportRequestV1(
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

    private fun pairLegalCandidates(): List<PairInformationLegalCandidate> =
        listOf(
            PairInformationLegalCandidate(
                candidateId = "pair:a",
                outcome = PairInformationOutcome(
                    shownRole = RoleId("Saint"),
                    targetSeat = 2,
                    decoySeat = 3,
                ),
                semanticTruth = SemanticTruth.TRUE,
                registrations = listOf(
                    RegistrationFact(
                        interactionId = "pair:test-game:librarian",
                        subjectSeat = 2,
                        registeredRole = RoleId("Saint"),
                        registrationQuestion = RegistrationQuestion.ROLE,
                        reason = RegistrationReason.SPY_ABILITY,
                    ),
                ),
            ),
            PairInformationLegalCandidate(
                candidateId = "pair:b",
                outcome = PairInformationOutcome(
                    shownRole = null,
                    targetSeat = null,
                    decoySeat = null,
                ),
                semanticTruth = SemanticTruth.TRUE,
                registrations = emptyList(),
            ),
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
                TroubleBrewingSnapshotSeat(
                    seat = 2,
                    shownRoleId = SnapshotField.Known("Saint"),
                    actualRoleId = SnapshotField.Known("Saint"),
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                ),
                TroubleBrewingSnapshotSeat(
                    seat = 3,
                    shownRoleId = SnapshotField.Known("Empath"),
                    actualRoleId = SnapshotField.Known("Empath"),
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
