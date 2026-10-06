package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateAuditSummary
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerPolicyGameContextV1Test {
    @Test
    fun builderDefaultsEverySeatAndKeepsExplicitClaimsAndPressureSeparateFromDerivedHistory() {
        val context = StorytellerPolicyGameContextBuilderV1.build(
            snapshot = snapshot(),
            sourceRevision = REVISION,
            playerInputsBySeat = mapOf(
                2 to StorytellerPlayerContextInputV1(
                    experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                    claimedRoleIds = listOf(RoleId("Saint"), RoleId("Empath")),
                    pressureLevel = StorytellerDeclaredPressureLevelV1.HIGH,
                ),
            ),
        )

        assertEquals(listOf(1, 2), context.players.map { it.seat })
        assertEquals(PlayerExperienceLevelV1.NORMAL, context.players[0].experienceLevel)
        assertTrue(context.players[0].claimedRoleIds.isEmpty())
        assertEquals(null, context.players[0].pressureLevel)
        assertEquals(PlayerExperienceLevelV1.BEGINNER, context.players[1].experienceLevel)
        assertEquals(listOf(RoleId("Saint"), RoleId("Empath")), context.players[1].claimedRoleIds)
        assertEquals(StorytellerDeclaredPressureLevelV1.HIGH, context.players[1].pressureLevel)
    }

    @Test
    fun builderUsesOnlyEffectiveCommittedPrefixAndDropsFutureHistory() {
        val appliedPast = event("past", 2, 1, DecisionEventStatus.APPLIED)
        val appliedFuture = event("future", 9, 1, DecisionEventStatus.APPLIED)
        val proposed = event("proposed", 1, 1, DecisionEventStatus.PROPOSED)

        val context = StorytellerPolicyGameContextBuilderV1.build(
            snapshot = snapshot(),
            sourceRevision = REVISION,
            decisionHistory = DecisionHistoryArchive(
                events = listOf(appliedPast, appliedFuture, proposed),
            ),
        )

        assertEquals(listOf("past"), context.priorDecisions.map { it.eventId })
        assertEquals("candidate:past", context.priorDecisions.single().selectedCandidateId)
        assertEquals("test-decision", context.priorDecisions.single().selectedOutcome.decisionType)
    }

    @Test
    fun invocationRejectsMissingSeatContextOrFutureHistory() {
        val request = request()
        val validContext = StorytellerPolicyGameContextBuilderV1.build(
            snapshot = snapshot(),
            sourceRevision = REVISION,
        )

        assertThrows(IllegalArgumentException::class.java) {
            StorytellerPolicyInvocationV1(
                request = request,
                gameContext = validContext.copy(players = validContext.players.dropLast(1)),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            StorytellerPolicyInvocationV1(
                request = request,
                gameContext = validContext.copy(
                    priorDecisions = listOf(
                        StorytellerPriorDecisionContextV1.fromEvent(
                            event("future", 99, 99, DecisionEventStatus.APPLIED),
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun legacyGameContextAdaptsIntoTheNeutralStatelessProviderEnvelope() {
        val legacyRequest = request()
        val legacyContext = StorytellerPolicyGameContextBuilderV1.build(
            snapshot = snapshot(),
            sourceRevision = REVISION,
            playerInputsBySeat = mapOf(
                2 to StorytellerPlayerContextInputV1(
                    experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                    claimedRoleIds = listOf(RoleId("Saint")),
                    pressureLevel = StorytellerDeclaredPressureLevelV1.HIGH,
                ),
            ),
            decisionHistory = DecisionHistoryArchive(
                events = listOf(event("past", 2, 1, DecisionEventStatus.APPLIED)),
            ),
        )

        val neutral = StorytellerProviderContractAdapterV1.fromLegacy(
            request = legacyRequest,
            gameContext = legacyContext,
        )

        val context: StorytellerProviderGameContextV1 = neutral.gameContext
        assertEquals(listOf(1, 2), context.players.map { it.seat })
        assertEquals(PlayerExperienceLevelV1.BEGINNER, context.players[1].experienceLevel)
        assertEquals(listOf(RoleId("Saint")), context.players[1].claimedRoleIds)
        assertEquals(StorytellerDeclaredPressureLevelV1.HIGH, context.players[1].pressureLevel)
        assertEquals(listOf("past"), context.priorDecisions.map { it.eventId })
        assertEquals("candidate:past", context.priorDecisions.single().selectedCandidateId)
    }

    @Test
    fun episodeAllowsManualAfterDeferralAndProviderSelectionOnlyFromExplicitSuggestions() {
        val request = request()
        val invocation = StorytellerPolicyInvocationV1(
            request = request,
            gameContext = StorytellerPolicyGameContextBuilderV1.build(
                snapshot = snapshot(),
                sourceRevision = REVISION,
            ),
        )
        val deferred = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = REVISION,
            outcome = StorytellerPolicyOutcomeV1.Deferred(
                reasons = listOf("missing public narrative"),
            ),
        )

        StorytellerDecisionEpisodeV1(
            invocation = invocation,
            providerResponse = deferred,
            committedSelection = StorytellerDecisionEpisodeSelectionV1(
                candidateId = "a",
                source = StorytellerDecisionEpisodeSelectionSourceV1.STORYTELLER_MANUAL,
            ),
        )

        val recommendation = StorytellerPolicyResponseV1(
            decisionId = request.input.decisionId,
            sourceRevision = REVISION,
            outcome = StorytellerPolicyOutcomeV1.Recommendation(
                primary = StorytellerPolicyRecommendationV1("a"),
                alternatives = listOf(StorytellerPolicyRecommendationV1("b")),
            ),
        )
        StorytellerDecisionEpisodeV1(
            invocation = invocation,
            providerResponse = recommendation,
            committedSelection = StorytellerDecisionEpisodeSelectionV1(
                candidateId = "b",
                source = StorytellerDecisionEpisodeSelectionSourceV1.PROVIDER_SUGGESTION,
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            StorytellerDecisionEpisodeV1(
                invocation = invocation,
                providerResponse = deferred,
                committedSelection = StorytellerDecisionEpisodeSelectionV1(
                    candidateId = "a",
                    source = StorytellerDecisionEpisodeSelectionSourceV1.PROVIDER_SUGGESTION,
                ),
            )
        }
    }

    private fun request(): StorytellerPolicyRequestV1 {
        val input = RecommendationDecisionInputV1(
            snapshot = snapshot(),
            decisionId = "decision:gsp2b",
            lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
            sourceRevision = REVISION,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "gsp2b-test",
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = listOf("a", "b"),
            context = RecommendationDecisionContextV1.DrunkAssignment(
                legalCandidates = listOf(
                    RecommendationDrunkCandidateContextV1("a", 1, "Empath"),
                    RecommendationDrunkCandidateContextV1("b", 2, "Monk"),
                ),
            ),
            featureProjection = RecommendationFeatureProjectionV1.DrunkAssignment(
                evaluation = DrunkAssignmentFeatureEvaluation(
                    candidates = listOf("a", "b").map { id ->
                        CandidateDrunkAssignmentFeatures(
                            candidateId = id,
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
                                    DrunkAssignmentFirstNightInformationOpportunityFeatures(emptyList()),
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
                ),
            ),
        )
        return StorytellerPolicyRequestV1(
            decisionType = RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT,
            input = input,
        )
    }

    private fun snapshot(): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = "gsp2b-test",
            gameSeed = 7L,
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

    private fun event(
        id: String,
        gameRevision: Long,
        playerRevision: Long,
        status: DecisionEventStatus,
    ): StorytellerDecisionEvent {
        val candidateId = "candidate:$id"
        return StorytellerDecisionEvent(
            eventId = id,
            requestId = "request:$id",
            idempotencyKey = "idem:$id",
            gameStateRevision = gameRevision,
            playerInputRevision = playerRevision,
            rulesetRef = RulesetRef(
                scriptId = ScriptId("trouble_brewing"),
                scriptContentHash = "00000000000000000000000000000000",
                rulesetVersion = "test",
                sourceRevision = "test",
                coverage = RuleCoverage.VERIFIED,
            ),
            algorithmConfigVersion = "legacy-test",
            selectorVersion = "legacy-test",
            decisionSeed = 1L,
            stateDigest = "state:$id",
            historyDigest = "history:$id",
            selectedCandidateId = candidateId,
            selectedOutcomeSnapshot = DecisionOutcomeSnapshot(
                decisionType = "test-decision",
                canonicalFields = sortedMapOf("seat" to "1"),
            ),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            registrations = emptyList(),
            qualityTier = QualityTier.RECOMMENDED,
            totalScore = 999,
            finalProbabilityFixedPoint = 999L,
            pressureDelta = emptyMap(),
            candidatePoolFingerprint = "pool:$id",
            candidateAudit = listOf(
                CandidateAuditSummary(
                    candidateId = candidateId,
                    candidateFamilyId = "family",
                    qualityTier = QualityTier.RECOMMENDED,
                    totalScore = 999,
                    finalProbabilityFixedPoint = 999L,
                    explanationCodes = listOf("legacy-only"),
                ),
            ),
            explanationCodes = listOf("legacy-only"),
            status = status,
        )
    }

    companion object {
        private val REVISION = InformationDecisionRevision(4, 2)
    }
}
