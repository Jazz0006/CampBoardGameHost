package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingFirstNightPairDecisionContextTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun `snapshot context preserves pair semantics and historical poison-neutral precompute basis`() {
        val canonicalGame = game(poisonedSeat = 1)
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = snapshot(canonicalGame, gameStateRevision = 4L, playerInputRevision = 3L),
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = ruleset.characterRegistry,
        )

        val context = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = ruleset.characterRegistry,
        )

        assertEquals(SnapshotField.Known(true), snapshot.grimoireSeats.single { it.seat == 1 }.poisoned)
        assertFalse(context.naturalPairGameState.playerAt(1)!!.poisoned)
        assertEquals(canonicalGame.script, context.naturalPairGameState.script)
        assertEquals(canonicalGame.seed, context.naturalPairGameState.seed)
        assertEquals(
            canonicalGame.players.map { it.copy(name = "Seat ${it.seat}", poisoned = false) },
            context.naturalPairGameState.players,
        )
        assertEquals(
            clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing).sortedBy { it.id.value },
            context.roleDefinitions.sortedBy { it.id.value },
        )
    }

    @Test
    fun `snapshot backed context produces exactly the legacy natural pair candidate space`() {
        val canonicalGame = game(poisonedSeat = 1)
        val legacyNaturalBasis = canonicalGame.copy(
            players = canonicalGame.players.map { player -> player.copy(poisoned = false) },
        )
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = snapshot(canonicalGame, gameStateRevision = 4L, playerInputRevision = 3L),
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = ruleset.characterRegistry,
        )
        val context = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = ruleset.characterRegistry,
        )
        val coordinator = ClocktowerRecommendationCoordinator()

        val legacy = coordinator.naturalPairCandidates(legacyNaturalBasis)
        val migrated = coordinator.naturalPairCandidates(context)

        assertTrue(legacy.isNotEmpty())
        assertEquals(legacy, migrated)
    }

    @Test
    fun `precompute equality ignores revisions and poison when natural candidate basis is unchanged`() {
        val poisoned = game(poisonedSeat = 1)
        val unpoisoned = game(poisonedSeat = null)
        val first = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                gameSnapshot = snapshot(poisoned, gameStateRevision = 4L, playerInputRevision = 3L),
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                characterRegistry = ruleset.characterRegistry,
            ),
            characterRegistry = ruleset.characterRegistry,
        )
        val second = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                gameSnapshot = snapshot(unpoisoned, gameStateRevision = 5L, playerInputRevision = 9L),
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                characterRegistry = ruleset.characterRegistry,
            ),
            characterRegistry = ruleset.characterRegistry,
        )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    private fun snapshot(
        game: GameState,
        gameStateRevision: Long,
        playerInputRevision: Long,
    ) = GameSnapshot(
        gameId = "tbgs-2a",
        gameStateRevision = gameStateRevision,
        playerInputRevision = playerInputRevision,
        gameSeed = game.seed,
        rulesetRef = RulesetRef(
            scriptId = ScriptId("trouble_brewing"),
            scriptContentHash = "0123456789abcdef0123456789abcdef",
            rulesetVersion = "tbgs-2a-test",
            sourceRevision = "tbgs-2a-test",
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
            player(8, "Librarian", CharacterType.TOWNSFOLK, poisoned = poisonedSeat == 8),
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
