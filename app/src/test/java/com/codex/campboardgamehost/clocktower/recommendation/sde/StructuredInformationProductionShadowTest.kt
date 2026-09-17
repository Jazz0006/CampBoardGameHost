package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import org.junit.Assert.assertEquals
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
    fun `production shadow uses committed setup baseline and current session freshness without mutation`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val currentSnapshot = fixture.copy(
            gameStateRevision = fixture.gameStateRevision + 4,
            playerInputRevision = fixture.playerInputRevision + 2,
            actionTimeline = ActionFactTimeline(emptyList()),
            epistemicObservationLog = EpistemicObservationLog(),
            semanticHistoryMode = com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
            nextTimelineGlobalSequence = 0L,
        )
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
        val before = currentSnapshot

        val result = StructuredInformationProductionShadow.evaluateFirstNight(
            decisionContext = model.shadowDecisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = setup,
            currentSnapshot = currentSnapshot,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertEquals(model.contextSnapshot, result.informationSnapshot)
        assertEquals(model.contextSnapshot.legalCandidateIds, result.plannedDecisions.map { it.candidateId })
        assertTrue(result.plannedDecisions.all { it.sourceRevision == revision })
        assertEquals(before, currentSnapshot)
        assertEquals(0, currentSnapshot.actionTimeline.entries.size)
        assertEquals(0, currentSnapshot.epistemicObservationLog.records.size)
    }
}
