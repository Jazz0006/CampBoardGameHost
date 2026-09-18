package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.commitPoisonTargetBoundary
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2PoisonReplanningTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2c-poison-replanning",
        sourceRevision = "official",
    )
    private val fixture = com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
        .snapshot()
        .copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Poisoner change stales all uncommitted plans and broadly recomputes exact consequences`() {
        val session = ClocktowerGameSession.create(
            gameId = fixture.gameId,
            gameSeed = fixture.gameSeed,
            rulesetRef = rulesetRef,
            initialState = fixture.gameState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val setup = committedSetup()

        // Existing committed history is not part of the disposable planning surface.
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "sde-2c-committed-seat-5-identity",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(5),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.ShownRoleAt(5, RoleId("Imp")),
            ),
        )
        val committedLog = session.state.epistemicObservationLog

        val beforeSnapshot = session.toGameSnapshot(rulesetRef)
        val beforeRevision = currentRevision(session)
        val empathBeforeModel = numericModel(
            revision = beforeRevision,
            abilityRole = "Empath",
            actorSeat = 2,
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            trueValue = 0,
            reliability = InformationReliability.RELIABLE,
        )
        val chefBeforeModel = numericModel(
            revision = beforeRevision,
            abilityRole = "Chef",
            actorSeat = 1,
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = listOf(1, 2, 3, 4, 5),
            trueValue = 1,
            reliability = InformationReliability.RELIABLE,
        )
        val stateBeforePlanning = session.state
        val empathBefore = shadow(empathBeforeModel.shadowDecisionContext, beforeSnapshot, setup)
        val chefBefore = shadow(chefBeforeModel.shadowDecisionContext, beforeSnapshot, setup)

        assertEquals(stateBeforePlanning, session.state)
        assertTrue(empathBefore.plannedDecisions.all { it.isCurrentFor(empathBeforeModel.contextSnapshot) })
        assertTrue(chefBefore.plannedDecisions.all { it.isCurrentFor(chefBeforeModel.contextSnapshot) })

        // Draft target edit invalidates every still-uncommitted plan through the existing input revision.
        session.recordPlayerInput()
        val draftRevision = currentRevision(session)
        assertTrue(empathBefore.plannedDecisions.all { !it.isCurrentFor(draftRevision) })
        assertTrue(chefBefore.plannedDecisions.all { !it.isCurrentFor(draftRevision) })

        // Production confirmation records durable mechanical history first, then owns one state revision.
        session.commitGlobalActionFact(
            ActionFactDraft.Poison(
                actionId = "sde-2c-poison-seat-2",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 2,
                targetSeat = 2,
            ),
        )
        session.commitPoisonTargetBoundary(targetSeat = 2)
        val poisonedSnapshot = session.toGameSnapshot(rulesetRef)
        val poisonedRevision = currentRevision(session)

        assertEquals(beforeRevision.gameStateRevision + 1, poisonedRevision.gameStateRevision)
        assertEquals(beforeRevision.playerInputRevision + 1, poisonedRevision.playerInputRevision)
        assertEquals(committedLog, session.state.epistemicObservationLog)
        assertEquals(2, poisonedSnapshot.actionTimeline.entries.single().let {
            (it.fact as com.codex.campboardgamehost.clocktower.domain.ActionFact.Poison).targetSeat
        })

        val empathAfterModel = numericModel(
            revision = poisonedRevision,
            abilityRole = "Empath",
            actorSeat = 2,
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            trueValue = 0,
            reliability = InformationReliability.POISONED,
        )
        val chefAfterModel = numericModel(
            revision = poisonedRevision,
            abilityRole = "Chef",
            actorSeat = 1,
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = listOf(1, 2, 3, 4, 5),
            trueValue = 1,
            reliability = InformationReliability.RELIABLE,
        )
        val poisonedStateBeforePlanning = session.state
        val empathAfter = shadow(empathAfterModel.shadowDecisionContext, poisonedSnapshot, setup)
        val chefAfter = shadow(chefAfterModel.shadowDecisionContext, poisonedSnapshot, setup)

        assertEquals(poisonedStateBeforePlanning, session.state)
        assertEquals(committedLog, session.state.epistemicObservationLog)
        assertTrue(empathAfter.plannedDecisions.all { it.isCurrentFor(empathAfterModel.contextSnapshot) })
        assertTrue(chefAfter.plannedDecisions.all { it.isCurrentFor(chefAfterModel.contextSnapshot) })
        assertTrue(empathBefore.plannedDecisions.all { !it.isCurrentFor(empathAfterModel.contextSnapshot) })
        assertTrue(chefBefore.plannedDecisions.all { !it.isCurrentFor(chefAfterModel.contextSnapshot) })

        // The poisoned ability is regenerated under a different legal candidate identity.
        assertNotEquals(
            empathBeforeModel.contextSnapshot.legalCandidateIds,
            empathAfterModel.contextSnapshot.legalCandidateIds,
        )

        // The unaffected Chef keeps the same legal IDs but still gets fresh revision-bound plans.
        assertEquals(
            chefBeforeModel.contextSnapshot.legalCandidateIds,
            chefAfterModel.contextSnapshot.legalCandidateIds,
        )
        assertNotEquals(
            chefBefore.plannedDecisions.map { it.sourceRevision }.toSet(),
            chefAfter.plannedDecisions.map { it.sourceRevision }.toSet(),
        )

        val empathBeforeReady = empathBefore.consequences as ExactConsequenceEvaluation.Ready
        val empathAfterReady = empathAfter.consequences as ExactConsequenceEvaluation.Ready
        val chefBeforeReady = chefBefore.consequences as ExactConsequenceEvaluation.Ready
        val chefAfterReady = chefAfter.consequences as ExactConsequenceEvaluation.Ready

        // Once seat 2 is durably poisoned, every shown Empath number is mechanically credible:
        // the hypothetical information no longer narrows the poisoned replay baseline.
        assertTrue(
            empathAfterReady.consequences.all { consequence ->
                consequence.diagnostics.after == consequence.diagnostics.before
            },
        )
        assertTrue(
            empathBeforeReady.consequences.any { consequence ->
                consequence.diagnostics.after != consequence.diagnostics.before
            },
        )

        // Poison history changes the exact baseline even for a still-healthy, unrelated Chef plan.
        assertNotEquals(
            chefBeforeReady.consequences.first().diagnostics.before,
            chefAfterReady.consequences.first().diagnostics.before,
        )
    }

    private fun numericModel(
        revision: InformationDecisionRevision,
        abilityRole: String,
        actorSeat: Int,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        trueValue: Int,
        reliability: InformationReliability,
    ) = prepareNumericInformationUiModel(
        coordinator = ClocktowerRecommendationCoordinator(),
        gameId = fixture.gameId,
        phase = ClocktowerPhase.FirstNight,
        round = 1,
        sequence = 20 + actorSeat,
        actorSeat = actorSeat,
        abilityRole = RoleId(abilityRole),
        metric = metric,
        subjectSeats = subjectSeats,
        trueValue = trueValue,
        minimumValue = 0,
        maximumValue = 2,
        reliability = reliability,
        recommendationStyle = RecommendationStyle.BALANCED,
        revision = revision,
        recommendedValue = trueValue.takeIf { reliability == InformationReliability.RELIABLE },
    )

    private fun shadow(
        decisionContext: com.codex.campboardgamehost.clocktower.session.InformationDecisionContext<
            com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome.Number
        >,
        snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot,
        setup: CommittedClocktowerSetup,
    ): StructuredInformationShadowEvaluation =
        StructuredInformationProductionShadow.evaluateFirstNight(
            decisionContext = decisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = setup,
            currentSnapshot = snapshot,
            roleDefinitions = roles,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

    private fun committedSetup(): CommittedClocktowerSetup = CommittedClocktowerSetup(
        script = fixture.gameState.script,
        setupSeed = fixture.gameSeed,
        assignments = fixture.gameState.players.map { player ->
            CommittedSetupSeat(
                seat = player.seat,
                actualRole = player.actualRole,
                shownRole = player.shownRole ?: player.actualRole,
            )
        },
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.GENERATED,
            providerId = "sde-2c-poison-replanning-test",
        ),
    )

    private fun currentRevision(session: ClocktowerGameSession): InformationDecisionRevision =
        InformationDecisionRevision(
            gameStateRevision = session.state.gameStateRevision,
            playerInputRevision = session.state.playerInputRevision,
        )
}
