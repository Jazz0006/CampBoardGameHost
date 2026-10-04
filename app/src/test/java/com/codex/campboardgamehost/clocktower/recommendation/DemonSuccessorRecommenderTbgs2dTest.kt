package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemonSuccessorRecommenderTbgs2dTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/" + assetPath).readText(Charsets.UTF_8)
    }
    private val ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun forced_resolution_is_the_sole_legal_candidate_and_keeps_warning() {
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Scarlet Woman", CharacterType.MINION, poisoned = true),
                player(3, "Poisoner", CharacterType.MINION),
                player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
                player(5, "Mayor", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.Forced(targetSeat = 2),
        )

        val recommendations = DemonSuccessorRecommender.recommend(
            requestId = "night-2-successor",
            context = context,
        )

        assertEquals(1, recommendations.size)
        assertEquals(2, recommendations.single().targetSeat())
        assertEquals(RecommendationStyle.BALANCED, recommendations.single().style)
        assertTrue(recommendations.single().warnings.any { it.ruleId == "scarlet-woman-mandatory" })
    }

    @Test
    fun choice_ranks_exactly_supplied_legal_seats() {
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Poisoner", CharacterType.MINION),
                player(3, "Baron", CharacterType.MINION),
                player(4, "Spy", CharacterType.MINION),
                player(5, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.Choice(targetSeats = setOf(3)),
        )

        val recommendations = DemonSuccessorRecommender.recommend(
            requestId = "night-2-successor",
            context = context,
        )

        assertEquals(1, recommendations.size)
        assertEquals(3, recommendations.single().targetSeat())
    }

    @Test
    fun poisoned_scarlet_woman_follows_rules_choice_not_recommender_legality() {
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Scarlet Woman", CharacterType.MINION, poisoned = true),
                player(3, "Baron", CharacterType.MINION),
                player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
                player(5, "Mayor", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.Choice(targetSeats = setOf(2, 3)),
        )

        val recommendations = DemonSuccessorRecommender.recommend(
            requestId = "night-2-successor",
            context = context,
        )

        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.all { it.targetSeat() in setOf(2, 3) })
        assertTrue(recommendations.none { recommendation ->
            recommendation.warnings.any { warning -> warning.ruleId == "scarlet-woman-mandatory" }
        })
    }

    @Test
    fun none_resolution_yields_no_recommendations() {
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Baron", CharacterType.MINION),
                player(3, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.None,
        )

        assertTrue(
            DemonSuccessorRecommender.recommend(
                requestId = "night-2-successor",
                context = context,
            ).isEmpty(),
        )
    }

    @Test
    fun balanced_scoring_preserves_active_poisoner_when_legal_baron_exists() {
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Poisoner", CharacterType.MINION),
                player(3, "Baron", CharacterType.MINION),
                player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.Choice(targetSeats = setOf(2, 3)),
        )

        val recommendations = DemonSuccessorRecommender.recommend(
            requestId = "night-2-successor",
            context = context,
        )

        assertEquals(
            3,
            recommendations.first { it.style == RecommendationStyle.BALANCED }.targetSeat(),
        )
    }

    @Test
    fun snapshot_builder_preserves_poison_alive_role_and_balance_enrichment() {
        val pressure = mapOf(4 to PlayerInformationPressure(seat = 4, directSuspicion = 8))
        val context = context(
            players = listOf(
                player(1, "Imp", CharacterType.DEMON, alive = false),
                player(2, "Poisoner", CharacterType.MINION, poisoned = true),
                player(3, "Baron", CharacterType.MINION),
                player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
            ),
            resolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            pressure = pressure,
            spentAbilitySeats = setOf(4),
        )

        assertEquals(false, context.recommendationGameState.playerAt(1)?.alive)
        assertEquals(true, context.recommendationGameState.playerAt(2)?.poisoned)
        assertEquals(RoleId("Poisoner"), context.recommendationGameState.playerAt(2)?.actualRole)
        assertEquals(CharacterType.MINION, context.recommendationGameState.playerAt(2)?.actualType)
        assertEquals(Alignment.EVIL, context.recommendationGameState.playerAt(2)?.actualAlignment)
        assertEquals(
            GameBalanceEvaluator.evaluate(
                game = context.recommendationGameState,
                round = 2,
                spentAbilitySeats = setOf(4),
                playerInformationPressureBySeat = pressure,
            ).evilAdvantage,
            context.evilAdvantage,
        )
    }

    private fun context(
        players: List<PlayerState>,
        resolution: DemonSuccessionResolution,
        pressure: Map<Int, PlayerInformationPressure> = emptyMap(),
        spentAbilitySeats: Set<Int> = emptySet(),
    ): TroubleBrewingDemonSuccessorDecisionContext {
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 17L,
            players = players,
        )
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = GameSnapshot(
                gameId = "tbgs-2d-test",
                gameStateRevision = 12L,
                playerInputRevision = 21L,
                gameSeed = game.seed,
                rulesetRef = ruleset.toRulesetRef("tbgs-2d-test", "tbgs-2d-test"),
                gameState = game,
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            ),
            phase = StorytellerPhase.NIGHT,
            round = 2,
            characterRegistry = ruleset.characterRegistry,
        )
        return TroubleBrewingDemonSuccessorDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = ruleset.characterRegistry,
            successionResolution = resolution,
            playerInformationPressureBySeat = pressure,
            spentAbilitySeats = spentAbilitySeats,
        )
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        alignment: Alignment = Alignment.EVIL,
        poisoned: Boolean = false,
        alive: Boolean = true,
    ) = PlayerState(
        seat = seat,
        name = "P" + seat,
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        shownRole = RoleId(role),
        poisoned = poisoned,
        alive = alive,
    )

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.targetSeat() =
        (candidate.choice as DynamicStorytellerChoice.DemonSuccessor).targetSeat
}
