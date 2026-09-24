package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredInformationProductionShadowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-1e-production-shadow-test",
        sourceRevision = "official",
    )

    @Test
    fun `production shadow uses committed setup and canonical session without stealing commit authority`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val session = ClocktowerGameSession.create(
            gameId = fixture.gameId,
            gameSeed = fixture.gameSeed,
            rulesetRef = rulesetRef,
            initialState = fixture.gameState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        repeat(4) { session.advanceGameStateRevision() }
        repeat(2) { session.recordPlayerInput() }
        val currentSnapshot = session.toGameSnapshot(rulesetRef)
        val setup = CommittedClocktowerSetup(
            script = currentSnapshot.gameState.script,
            setupSeed = currentSnapshot.gameSeed,
            assignments = currentSnapshot.gameState.players.map { player ->
                CommittedSetupSeat(
                    seat = player.seat,
                    actualRole = player.actualRole,
                    shownRole = player.shownRole ?: player.actualRole,
                )
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "sde-1e-production-shadow-test",
            ),
        )
        val revision = InformationDecisionRevision(
            gameStateRevision = currentSnapshot.gameStateRevision,
            playerInputRevision = currentSnapshot.playerInputRevision,
        )
        val model = prepareNumericInformationUiModel(
            coordinator = ClocktowerRecommendationCoordinator(),
            gameId = currentSnapshot.gameId,
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            sequence = 20,
            actorSeat = 1,
            abilityRole = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(2, currentSnapshot.gameState.players.size),
            trueValue = 0,
            minimumValue = 0,
            maximumValue = 2,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = 0,
        )
        val visibleChoicesBefore = model.choices.toList()
        val sessionBeforeShadow = session.state

        val result = StructuredInformationProductionShadow.evaluateFirstNight(
            decisionContext = model.shadowDecisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = setup,
            currentSnapshot = currentSnapshot,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertEquals(model.contextSnapshot, result.informationSnapshot)
        assertEquals(model.contextSnapshot.legalCandidateIds, result.sdeCandidates.map { it.candidateId })
        assertEquals(model.contextSnapshot.legalCandidateIds, result.plannedDecisions.map { it.candidateId })
        assertTrue(result.sdeCandidates.all { it.sourceRevision == revision })
        assertTrue(result.sdeCandidates.all {
            it.sourceInteraction.abilityState == AbilityState.FUNCTIONING
        })
        assertTrue(result.sdeCandidates.all {
            it.legalityProvenance.candidateSpaceIdentity == model.contextSnapshot.semanticIdentity
        })
        assertTrue(result.sdeCandidates.all { it.inputBindings === SdeDecisionInputBindings.NotCaptured })
        assertTrue(result.sdeCandidates.all {
            val prefix = it.historyPrefixRef as SdeHistoricalPrefixRef.Global
            prefix.gameId == currentSnapshot.gameId &&
                prefix.actionRefs.isEmpty() &&
                prefix.observationRefs.isEmpty()
        })
        assertTrue(result.plannedDecisions.all { it.sourceRevision == revision })
        assertTrue(result.consequences is ExactConsequenceEvaluation.Ready)
        assertTrue(result.featureEvaluation is DecisionFeatureEvaluation.Ready)
        assertEquals(PolicyVersions.BEGINNER_CONSERVATIVE_V1, result.policyEvaluation.policyVersion)
        assertEquals(model.contextSnapshot.legalCandidateIds, result.policyEvaluation.candidateIds)
        assertEquals(
            BeginnerConservativeV1Selector.select(
                evaluation = result.policyEvaluation,
                decisionId = model.contextSnapshot.semanticIdentity,
                selectionSeed = currentSnapshot.gameSeed,
            ),
            result.policySelection,
        )
        assertNotNull(result.policySelection)
        val featureEvaluation = result.featureEvaluation as DecisionFeatureEvaluation.Ready
        assertEquals(
            model.contextSnapshot.legalCandidateIds,
            featureEvaluation.candidates.map(CandidateDecisionFeatures::candidateId),
        )
        assertTrue(featureEvaluation.candidates.all {
            it.features.strategic is FeatureProjection.Projected<*>
        })
        assertTrue(featureEvaluation.candidates.all {
            (it.features.semanticTruth as FeatureProjection.Projected<*>).value == SemanticTruth.TRUE
        })

        val chefModel = prepareNumericInformationUiModel(
            coordinator = ClocktowerRecommendationCoordinator(),
            gameId = currentSnapshot.gameId,
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            sequence = 21,
            actorSeat = 1,
            abilityRole = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = currentSnapshot.gameState.players.map { it.seat },
            trueValue = 1,
            minimumValue = 0,
            maximumValue = 2,
            reliability = InformationReliability.POISONED,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = 1,
        )
        val chefBindings = SdeDecisionInputBindings.Captured(
            committedInputRefs = setOf(
                CommittedDecisionInputRef(
                    inputId = "shown-identity:setup",
                    ownerId = "committed-clocktower-setup",
                    kind = SdeCommittedDecisionInputKind.SETUP_SHOWN_IDENTITY,
                ),
            ),
        )
        val chefResult = StructuredInformationProductionShadow.evaluateFirstNight(
            decisionContext = chefModel.shadowDecisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = setup,
            currentSnapshot = currentSnapshot,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            inputBindings = chefBindings,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        assertTrue(chefModel.contextSnapshot.legalCandidateIds.size > 1)
        assertTrue(chefResult.featureEvaluation is DecisionFeatureEvaluation.Ready)
        val chefFeatureEvaluation = chefResult.featureEvaluation as DecisionFeatureEvaluation.Ready
        assertTrue(chefResult.sdeCandidates.all {
            it.sourceInteraction.abilityState == AbilityState.MALFUNCTIONING_POISONED
        })
        assertTrue(chefResult.sdeCandidates.all { it.inputBindings == chefBindings })
        assertEquals(PolicyVersions.BEGINNER_CONSERVATIVE_V1, chefResult.policyEvaluation.policyVersion)
        assertEquals(chefModel.contextSnapshot.legalCandidateIds, chefResult.policyEvaluation.candidateIds)
        assertEquals(
            chefModel.contextSnapshot.legalCandidateIds,
            chefFeatureEvaluation.candidates.map(CandidateDecisionFeatures::candidateId),
        )
        val chefTruths = chefFeatureEvaluation.candidates.map {
            (it.features.semanticTruth as FeatureProjection.Projected<SemanticTruth>).value
        }.toSet()
        assertEquals(setOf(SemanticTruth.TRUE, SemanticTruth.FALSE), chefTruths)
        assertEquals(visibleChoicesBefore, model.choices)
        assertEquals(sessionBeforeShadow, session.state)

        val recommended = model.choices.single { it.recommended }
        val confirmation = model.acceptRecommendation(recommended.candidateId, revision)
        val confirmed = confirmation.confirmed
        assertNotNull(confirmed)
        assertEquals(sessionBeforeShadow, session.state)

        session.commitGlobalEpistemicObservation(requireNotNull(confirmed).draft)

        assertEquals(sessionBeforeShadow.gameStateRevision, session.state.gameStateRevision)
        assertEquals(sessionBeforeShadow.playerInputRevision + 1, session.state.playerInputRevision)
        assertEquals(
            sessionBeforeShadow.epistemicObservationLog.records.size + 1,
            session.state.epistemicObservationLog.records.size,
        )
    }
    @Test
    fun `historical shadow evaluates a night two interaction against the canonical committed prefix`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val session = ClocktowerGameSession.create(
            gameId = fixture.gameId,
            gameSeed = fixture.gameSeed,
            rulesetRef = rulesetRef,
            initialState = fixture.gameState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val empathPlayer = fixture.gameState.players.single { player -> player.actualRole.value == "Empath" }
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "history:shown-role:seat-1",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(empathPlayer.seat),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.ShownRoleAt(
                    seat = empathPlayer.seat,
                    role = empathPlayer.shownRole ?: empathPlayer.actualRole,
                ),
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.PhaseAdvance(
                actionId = "phase:night-2",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 2,
                nextPhase = StorytellerPhase.NIGHT,
                nextRound = 2,
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Poison(
                actionId = "poison:night-2:seat-1",
                phase = StorytellerPhase.NIGHT,
                round = 2,
                sequence = 0,
                targetSeat = empathPlayer.seat,
            ),
        )
        val currentSnapshot = session.toGameSnapshot(rulesetRef)
        val setup = CommittedClocktowerSetup(
            script = currentSnapshot.gameState.script,
            setupSeed = currentSnapshot.gameSeed,
            assignments = currentSnapshot.gameState.players.map { player ->
                CommittedSetupSeat(
                    seat = player.seat,
                    actualRole = player.actualRole,
                    shownRole = player.shownRole ?: player.actualRole,
                )
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "sde-3b-historical-shadow-test",
            ),
        )
        val revision = InformationDecisionRevision(
            gameStateRevision = currentSnapshot.gameStateRevision,
            playerInputRevision = currentSnapshot.playerInputRevision,
        )
        val playerCount = currentSnapshot.gameState.players.size
        val neighbourSeats = listOf(
            if (empathPlayer.seat == 1) playerCount else empathPlayer.seat - 1,
            if (empathPlayer.seat == playerCount) 1 else empathPlayer.seat + 1,
        )
        val empathTrueValue = neighbourSeats.count { seat ->
            currentSnapshot.gameState.playerAt(seat)?.actualAlignment == Alignment.EVIL
        }
        val model = prepareNumericInformationUiModel(
            coordinator = ClocktowerRecommendationCoordinator(),
            gameId = currentSnapshot.gameId,
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 1,
            actorSeat = empathPlayer.seat,
            abilityRole = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = neighbourSeats,
            trueValue = empathTrueValue,
            minimumValue = 0,
            maximumValue = 2,
            reliability = InformationReliability.POISONED,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = 0,
        )
        val choicesBefore = model.choices.toList()
        val sessionBeforeShadow = session.state

        val historicalBindings = SdeDecisionInputBindings.Captured(
            playerControlledInputRefs = setOf(
                PlayerControlledDecisionInputRef(
                    inputId = "poison:night-2:seat-${empathPlayer.seat}",
                    ownerId = "action-fact-timeline",
                    kind = SdePlayerControlledDecisionInputKind.TARGET_SELECTION,
                ),
            ),
        )
        val result = StructuredInformationProductionShadow.evaluateHistorical(
            decisionContext = model.shadowDecisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = setup,
            currentSnapshot = currentSnapshot,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            inputBindings = historicalBindings,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertTrue(result.sdeCandidates.all {
            it.lifecycleStage == SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.NIGHT,
                round = 2,
                sequence = 1,
            )
        })
        assertTrue(result.sdeCandidates.all { it.inputBindings == historicalBindings })
        assertTrue(result.sdeCandidates.all { candidate ->
            val prefix = candidate.historyPrefixRef as SdeHistoricalPrefixRef.Global
            prefix.observationRefs.map(SdeHistoricalObservationRef::recordId) ==
                listOf("history:shown-role:seat-1") &&
                prefix.actionRefs.map(SdeHistoricalActionRef::actionId) ==
                listOf("phase:night-2", "poison:night-2:seat-1")
        })
        assertEquals(model.contextSnapshot.legalCandidateIds, result.policyEvaluation.candidateIds)
        assertEquals(choicesBefore, model.choices)
        assertEquals(sessionBeforeShadow, session.state)

        session.commitGlobalActionFact(
            ActionFactDraft.Protect(
                actionId = "future:night-2:protect",
                phase = StorytellerPhase.NIGHT,
                round = 2,
                sequence = 2,
                targetSeat = empathPlayer.seat,
            ),
        )
        val snapshotWithFutureFact = session.toGameSnapshot(rulesetRef)
        try {
            StructuredInformationProductionShadow.evaluateHistorical(
                decisionContext = model.shadowDecisionContext,
                validatedRuleset = validatedRuleset,
                committedSetup = setup,
                currentSnapshot = snapshotWithFutureFact,
                roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
                inputBindings = historicalBindings,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
            throw AssertionError("Historical shadow must reject facts committed after the decision point.")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("committed prefix"))
        }
    }

}
