package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Test

class GameSnapshotTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = TroubleBrewingFixtures.scriptId,
        scriptContentHash = "0123456789abcdef0123456789abcdef",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.PARTIAL,
    )

    @Test(expected = IllegalArgumentException::class)
    fun `snapshot rejects recommendation state with a different seed`() {
        GameSnapshot(
            gameId = "game-2026-08-06-001",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed + 1,
            rulesetRef = rulesetRef,
            gameState = game,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `snapshot rejects a ruleset for another script`() {
        GameSnapshot(
            gameId = "game-2026-08-06-001",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef.copy(scriptId = ScriptId("other-script")),
            gameState = game,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ruleset hash must use the documented lowercase 128 bit format`() {
        rulesetRef.copy(scriptContentHash = "ABC123")
    }
}
