package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RedHerringSetupShadowIntegrationTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)
    private val game = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 17L,
        players = listOf(
            player(1, "Chef", "Chef"),
            player(2, "Fortune Teller", "Fortune Teller"),
            player(3, "Washerwoman", "Washerwoman"),
            player(4, "Poisoner", "Poisoner"),
            player(5, "Imp", "Imp"),
        ),
    )
    private val snapshot = GameSnapshot(
        gameId = "game-1",
        gameStateRevision = 3L,
        playerInputRevision = 5L,
        gameSeed = game.seed,
        rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "sde-c4-red-herring-shadow-test",
            sourceRevision = "official",
        ),
        gameState = game,
    )
    private val exactContext = ExactConsequenceContext(
        validatedRuleset = validatedRuleset,
        exactContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = game.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        ),
    )

    @Test
    fun `coordinator attaches typed red herring shadow beside unchanged visible setup result`() {
        val coordinator = ClocktowerRecommendationCoordinator()
        val request = SetupCoordinationRequest(game = game, roles = roles)
        val visibleResult = coordinator.recommendSetup(request)
        val visiblePlansBefore = visibleResult.plans.toList()

        val shadow = coordinator.evaluateSetupRedHerringShadow(
            request = request,
            visibleResult = visibleResult,
            exactContext = exactContext,
        )

        assertSame(visibleResult, shadow.visibleResult)
        assertEquals(visiblePlansBefore, shadow.visibleResult.plans)

        val legal = SetupCandidateGenerator.generateRedHerringCandidates(game)
        assertEquals(
            legal.map { it.candidateId },
            shadow.projection.candidates.map { it.sdeCandidate.candidateId },
        )
        assertTrue(shadow.legacyRedHerringCandidateIdByStyle.isNotEmpty())

        val featureEvaluation = shadow.featureEvaluation as DecisionFeatureEvaluation.Ready
        assertEquals(legal.map { it.candidateId }, featureEvaluation.candidateIds)

        val chefCandidateId = legal
            .single { (it.outcome as SetupClueOutcome.RedHerring).seat == 1 }
            .candidateId
        val chefFeatures = featureEvaluation.candidates
            .single { it.candidateId == chefCandidateId }
            .features
            .truthCredibility as FeatureProjection.Projected

        assertTrue(chefFeatures.value.truthDangerSources.any { impact ->
            impact.source == ConfirmationChannelRef.Source(1, RoleId("Chef")) &&
                impact.independentlyConstraining
        })
        assertEquals(
            setOf(ConfirmationChannelRef.Source(3, RoleId("Washerwoman"))),
            chefFeatures.value.unresolvedSourceRefs,
        )
        assertEquals(
            setOf(ConfirmationChannelRef.Source(1, RoleId("Chef"))),
            chefFeatures.value.credibilityDisruptions.mapTo(linkedSetOf()) {
                it.affectedSource
            },
        )
        assertEquals(
            setOf("fortune-teller-target"),
            shadow.projection.excludedPlayerControlledElementIds,
        )
    }

    @Test
    fun `locked red herring is a persistent setup input and cannot enter uncommitted shadow replanning`() {
        val locked = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .first()
            .outcome as SetupClueOutcome.RedHerring
        val request = SetupCoordinationRequest(
            game = game,
            roles = roles,
            lockedDecisions = listOf(StorytellerDecision.RedHerring(locked.seat)),
        )
        val coordinator = ClocktowerRecommendationCoordinator()
        val visibleResult = coordinator.recommendSetup(request)

        val error = assertThrows(IllegalArgumentException::class.java) {
            coordinator.evaluateSetupRedHerringShadow(
                request = request,
                visibleResult = visibleResult,
                exactContext = exactContext,
            )
        }

        assertTrue(error.message.orEmpty().contains("persistent setup input"))
    }

    private fun player(
        seat: Int,
        name: String,
        role: String,
    ): PlayerState {
        val definition = definitionsById.getValue(RoleId(role))
        return PlayerState(
            seat = seat,
            name = name,
            actualRole = definition.id,
            actualAlignment = definition.alignment,
            actualType = definition.type,
            shownRole = definition.id,
        )
    }
}