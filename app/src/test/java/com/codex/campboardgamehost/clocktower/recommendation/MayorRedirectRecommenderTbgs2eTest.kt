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
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectDecisionDomain
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegalDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MayorRedirectRecommenderTbgs2eTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/" + assetPath).readText(Charsets.UTF_8)
    }
    private val ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun rules_domain_excludes_demon_and_includes_direct_mayor_death() {
        val game = game()
        val domain = MayorRedirectLegalDomain.resolve(game = game, mayorSeat = 1)

        assertEquals(setOf(1, 2, 3, 4), domain.legalTargetSeats)
        assertFalse(5 in domain.legalTargetSeats)
    }

    @Test
    fun recommender_ranks_only_rules_supplied_target_seats() {
        val context = context(
            decisionDomain = MayorRedirectDecisionDomain(
                mayorSeat = 1,
                legalTargetSeats = setOf(1, 4),
            ),
        )

        val recommendations = MayorRedirectRecommender.recommend(
            requestId = "night-2-mayor",
            context = context,
        )

        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.all { it.targetSeat() in setOf(1, 4) })
        assertTrue(recommendations.none { it.targetSeat() == 2 || it.targetSeat() == 3 || it.targetSeat() == 5 })
    }

    @Test
    fun protected_dead_and_soldier_targets_keep_no_death_outcomes() {
        val context = context(
            decisionDomain = MayorRedirectLegalDomain.resolve(game(), mayorSeat = 1),
            protectedSeats = setOf(3),
            deadSeats = setOf(4),
        )

        val soldier = MayorRedirectRecommender.resolveOutcome(context, targetSeat = 2)
        val protected = MayorRedirectRecommender.resolveOutcome(context, targetSeat = 3)
        val dead = MayorRedirectRecommender.resolveOutcome(context, targetSeat = 4)
        val direct = MayorRedirectRecommender.resolveOutcome(context, targetSeat = 1)

        assertNull(soldier.actualDeathSeat)
        assertNull(protected.actualDeathSeat)
        assertNull(dead.actualDeathSeat)
        assertEquals(1, direct.actualDeathSeat)
    }

    @Test
    fun snapshot_builder_preserves_runtime_truth_and_balance_enrichment() {
        val pressure = mapOf(4 to PlayerInformationPressure(seat = 4, directSuspicion = 7))
        val context = context(
            decisionDomain = MayorRedirectLegalDomain.resolve(
                game(poisonedSeats = setOf(3)),
                mayorSeat = 1,
            ),
            poisonedSeats = setOf(3),
            pressure = pressure,
            spentAbilitySeats = setOf(4),
        )

        assertEquals(RoleId("Mayor"), context.recommendationGameState.playerAt(1)?.actualRole)
        assertEquals(CharacterType.TOWNSFOLK, context.recommendationGameState.playerAt(1)?.actualType)
        assertEquals(Alignment.GOOD, context.recommendationGameState.playerAt(1)?.actualAlignment)
        assertEquals(true, context.recommendationGameState.playerAt(3)?.poisoned)
        assertEquals(
            GameBalanceEvaluator.evaluate(
                game = context.recommendationGameState,
                round = 2,
                spentAbilitySeats = setOf(4),
                playerInformationPressureBySeat = pressure,
            ).hint,
            context.publicBalanceHint,
        )
    }

    @Test
    fun context_rejects_non_night_snapshot() {
        val game = game()
        val snapshot = snapshot(game, phase = StorytellerPhase.DAY)

        assertTrue(
            runCatching {
                TroubleBrewingMayorRedirectDecisionContextBuilder.build(
                    snapshot = snapshot,
                    characterRegistry = ruleset.characterRegistry,
                    decisionDomain = MayorRedirectLegalDomain.resolve(game, mayorSeat = 1),
                    protectedSeats = emptySet(),
                    playerInformationPressureBySeat = emptyMap(),
                    spentAbilitySeats = emptySet(),
                )
            }.isFailure,
        )
    }

    private fun context(
        decisionDomain: MayorRedirectDecisionDomain,
        protectedSeats: Set<Int> = emptySet(),
        deadSeats: Set<Int> = emptySet(),
        poisonedSeats: Set<Int> = emptySet(),
        pressure: Map<Int, PlayerInformationPressure> = emptyMap(),
        spentAbilitySeats: Set<Int> = emptySet(),
    ): TroubleBrewingMayorRedirectDecisionContext {
        val game = game(deadSeats = deadSeats, poisonedSeats = poisonedSeats)
        return TroubleBrewingMayorRedirectDecisionContextBuilder.build(
            snapshot = snapshot(game),
            characterRegistry = ruleset.characterRegistry,
            decisionDomain = decisionDomain,
            protectedSeats = protectedSeats,
            playerInformationPressureBySeat = pressure,
            spentAbilitySeats = spentAbilitySeats,
        )
    }

    private fun snapshot(
        game: GameState,
        phase: StorytellerPhase = StorytellerPhase.NIGHT,
    ) = TroubleBrewingGameSnapshotProjector.fromRuntime(
        gameSnapshot = GameSnapshot(
            gameId = "tbgs-2e-test",
            gameStateRevision = 31L,
            playerInputRevision = 47L,
            gameSeed = game.seed,
            rulesetRef = ruleset.toRulesetRef("tbgs-2e-test", "tbgs-2e-test"),
            gameState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        ),
        phase = phase,
        round = 2,
        characterRegistry = ruleset.characterRegistry,
    )

    private fun game(
        deadSeats: Set<Int> = emptySet(),
        poisonedSeats: Set<Int> = emptySet(),
    ) = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 23L,
        players = listOf(
            player(1, "Mayor", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats, poisonedSeats),
            player(2, "Soldier", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats, poisonedSeats),
            player(3, "Virgin", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats, poisonedSeats),
            player(4, "Butler", Alignment.GOOD, CharacterType.OUTSIDER, deadSeats, poisonedSeats),
            player(5, "Imp", Alignment.EVIL, CharacterType.DEMON, deadSeats, poisonedSeats),
        ),
    )

    private fun player(
        seat: Int,
        role: String,
        alignment: Alignment,
        type: CharacterType,
        deadSeats: Set<Int>,
        poisonedSeats: Set<Int>,
    ) = PlayerState(
        seat = seat,
        name = "P" + seat,
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        shownRole = RoleId(role),
        alive = seat !in deadSeats,
        poisoned = seat in poisonedSeats,
    )

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.targetSeat(): Int =
        (candidate.choice as DynamicStorytellerChoice.MayorDeathResolution).targetSeat
}
