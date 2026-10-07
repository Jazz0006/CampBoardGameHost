package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRecommendationDecisionContextTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun `runtime context preserves poison and rules owned recommendation base`() {
        val canonicalGame = game(poisonedSeat = 3)
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = snapshot(canonicalGame),
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = ruleset.characterRegistry,
        )

        val context = TroubleBrewingSetupRecommendationDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = ruleset.characterRegistry,
        )

        assertEquals(canonicalGame.script, context.recommendationGameState.script)
        assertEquals(canonicalGame.seed, context.recommendationGameState.seed)
        assertEquals(
            canonicalGame.players.map { player -> player.copy(name = "Seat ${player.seat}") },
            context.recommendationGameState.players,
        )
        assertTrue(context.recommendationGameState.playerAt(3)!!.poisoned)
        assertEquals(
            clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            context.roleDefinitions,
        )
    }

    @Test
    fun `committed setup context is accepted before runtime and starts unpoisoned`() {
        val canonicalGame = game(poisonedSeat = null)
        val committed = CommittedClocktowerSetup(
            script = canonicalGame.script,
            setupSeed = canonicalGame.seed,
            assignments = canonicalGame.players.map { player ->
                CommittedSetupSeat(
                    seat = player.seat,
                    actualRole = player.actualRole,
                    shownRole = requireNotNull(player.shownRole),
                )
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "tbgs-2c-test",
            ),
        )
        val snapshot = TroubleBrewingGameSnapshotProjector.fromCommitted(
            gameId = "tbgs-2c-committed",
            committedSetup = committed,
            characterRegistry = ruleset.characterRegistry,
        )

        val context = TroubleBrewingSetupRecommendationDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = ruleset.characterRegistry,
        )

        assertEquals(TroubleBrewingSnapshotStage.SETUP_COMMITTED, snapshot.position.stage)
        assertTrue(context.recommendationGameState.players.none(PlayerState::poisoned))
        assertEquals(canonicalGame.seed, context.recommendationGameState.seed)
    }

    @Test
    fun `context rejects precommit later round and non first night runtime positions`() {
        val canonicalGame = game(poisonedSeat = null)
        val runtime = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = snapshot(canonicalGame),
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = ruleset.characterRegistry,
        )

        val precommit = runtime.withPosition(
            runtime.position.copy(
                stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                phase = SnapshotField.NotApplicable,
                round = SnapshotField.NotApplicable,
            ),
        )
        assertThrows(IllegalStateException::class.java) {
            TroubleBrewingSetupRecommendationDecisionContextBuilder.build(
                precommit,
                ruleset.characterRegistry,
            )
        }

        val laterRound = runtime.withPosition(
            runtime.position.copy(round = SnapshotField.Known(2)),
        )
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupRecommendationDecisionContextBuilder.build(
                laterRound,
                ruleset.characterRegistry,
            )
        }

        val day = runtime.withPosition(
            runtime.position.copy(phase = SnapshotField.Known(StorytellerPhase.DAY)),
        )
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupRecommendationDecisionContextBuilder.build(
                day,
                ruleset.characterRegistry,
            )
        }
    }

    private fun TroubleBrewingGameSnapshotV1.withPosition(
        nextPosition: TroubleBrewingSnapshotPosition,
    ) = TroubleBrewingGameSnapshotV1(
        gameId = gameId,
        gameSeed = gameSeed,
        position = nextPosition,
        grimoireSeats = grimoireSeats,
        setupState = setupState,
    )

    private fun snapshot(game: GameState) = GameSnapshot(
        gameId = "tbgs-2c-runtime",
        gameStateRevision = 7L,
        playerInputRevision = 5L,
        gameSeed = game.seed,
        rulesetRef = RulesetRef(
            scriptId = ScriptId("trouble_brewing"),
            scriptContentHash = "0123456789abcdef0123456789abcdef",
            rulesetVersion = "tbgs-2c-test",
            sourceRevision = "tbgs-2c-test",
            coverage = RuleCoverage.PARTIAL,
        ),
        gameState = game,
    )

    private fun game(poisonedSeat: Int?): GameState = GameState(
        script = ScriptId("trouble_brewing"),
        seed = 20_261_003L,
        players = listOf(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Washerwoman", poisoned = poisonedSeat == 1),
            player(2, "Chef", CharacterType.TOWNSFOLK, poisoned = poisonedSeat == 2),
            player(3, "Empath", CharacterType.TOWNSFOLK, poisoned = poisonedSeat == 3),
            player(4, "Recluse", CharacterType.OUTSIDER, poisoned = poisonedSeat == 4),
            player(5, "Spy", CharacterType.MINION, poisoned = poisonedSeat == 5),
            player(6, "Imp", CharacterType.DEMON, poisoned = poisonedSeat == 6),
            player(7, "Investigator", CharacterType.TOWNSFOLK, poisoned = poisonedSeat == 7),
            player(8, "Fortune Teller", CharacterType.TOWNSFOLK, poisoned = poisonedSeat == 8),
        ),
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
        poisoned: Boolean = false,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK,
            CharacterType.OUTSIDER,
            -> Alignment.GOOD

            CharacterType.MINION,
            CharacterType.DEMON,
            -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
        poisoned = poisoned,
    )
}
