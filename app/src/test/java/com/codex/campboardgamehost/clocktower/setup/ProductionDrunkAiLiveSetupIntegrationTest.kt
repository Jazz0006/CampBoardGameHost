package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRecommendationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.session.DrunkAssignmentDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.ProductionDrunkAiGatewayV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProductionDrunkAiLiveSetupIntegrationTest {
    @Test
    fun `real prepared TB setup to live legal AI recommendation and existing Host commit`() {
        val registry = BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        val preset = TroubleBrewingSetupPreset(
            id = "prod1-real-setup",
            playerCount = 8,
            townsfolk = listOf("chef", "empath", "fortuneteller", "undertaker", "monk"),
            outsiders = listOf("drunk"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "standard",
            styleTags = listOf("balanced"),
            drunkAsOptions = listOf("washerwoman", "librarian", "investigator"),
        )
        val dataset = TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "prod1-fixture",
            status = "test",
            declaredPoolSizes = mapOf(8 to 1),
            runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
                exactRepeat = "reject",
                similarityScope = "test",
                roleOverlapFormula = "test",
                lastGameMaxOverlap = mapOf(8 to 1.0),
                historyWeights = listOf(1.0),
                extraSoftPenalties = emptyList(),
                fallback = "test",
            ),
            pools = mapOf(8 to listOf(preset)),
        )
        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = registry,
            orderedPlayerNames = (1..8).map { "Player $it" },
            gameSeed = 6002L,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )
        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = prepared,
            experienceMode = StorytellerExperienceMode.EXPERIENCED,
            recommendedCandidate = null,
        ) as TroubleBrewingDrunkSelectionRoute.ManualSelection
        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            gameId = "live-game",
            intermediateSetup = prepared.intermediateSetup,
        )
        val pending = DrunkAssignmentDecisionBoundary.create(
            snapshot, registry, StorytellerDecisionRevision(0, 0),
        )
        val request = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(
            pending, snapshot,
            StorytellerProviderGameContextBuilderV1.build(snapshot, StorytellerProviderRevisionV1(0, 0)),
        )
        assertEquals(8, JSONObject(ProductionDrunkAiGatewayV1.encode(request))
            .getJSONObject("state").getJSONArray("seats").length())
        assertEquals(
            route.request.candidates.map { it.seat to it.shownRoleId },
            pending.legalCandidates.map { it.payload.seat to it.payload.shownRoleId },
        )
        val first = pending.legalCandidates.first()
        val alternative = pending.legalCandidates[1]
        val response = StorytellerProviderResponseV1(
            decisionId = request.identity.decisionId,
            sourceRevision = request.sourceRevision,
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1(first.candidateId, listOf("Cross-role balance")),
                alternatives = listOf(
                    StorytellerProviderRecommendationV1(alternative.candidateId, listOf("Alternate clue story")),
                ),
            ),
        )
        assertTrue(ProductionDrunkAiGatewayV1.validateCurrent(request, pending, pending, response)
            is StorytellerProviderValidationV1.AcceptedRecommendation)
        val confirmation = pending.confirm(first.candidateId, StorytellerDecisionRevision(0, 0))
        assertTrue(confirmation is StorytellerDecisionConfirmation.Confirmed<*>)
        val confirmed = route.request.candidates.single {
            it.seat == first.payload.seat && it.shownRoleId == first.payload.shownRoleId
        }
        val committed = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = prepared.intermediateSetup,
            confirmedDrunkCandidate = confirmed,
            characterRegistry = registry,
        )
        assertEquals(confirmed, committed.confirmedDrunkCandidate)
        assertEquals(
            "Drunk",
            committed.gameState.players.single { it.seat == confirmed.seat }.actualRole.value,
        )
        assertEquals(8, committed.gameState.players.size)
    }
}
