package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleHealthyHarnessTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `representative healthy seven-player space quotients before exact evaluation without losing provenance`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(5, "Investigator", CharacterType.TOWNSFOLK),
            player(6, "Scarlet Woman", CharacterType.MINION),
            player(7, "Imp", CharacterType.DEMON),
        )
        val context = context(game)

        val result = TroubleBrewingFirstNightHealthyBundleHarness.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            evaluationRecipientSeats = setOf(1),
        )

        assertTrue(result is FirstNightHealthyBundleHarnessEvaluation.Ready)
        val ready = result as FirstNightHealthyBundleHarnessEvaluation.Ready
        assertEquals(BigInteger.valueOf(110_000L), ready.rawCompleteBundleCount)
        assertEquals(BigInteger.valueOf(100L), ready.publicFactorCombinationCount)
        assertEquals(100, ready.distinctProjectedSignatureCount)
        assertEquals(100, ready.exactEvaluatedSignatureCount)
        assertFalse(ready.samplingApplied)
        assertEquals(100, ready.signatureGroups.size)
        assertEquals(
            BigInteger.valueOf(110_000L),
            ready.signatureGroups.fold(BigInteger.ZERO) { sum, group -> sum + group.multiplicity },
        )
        assertTrue(ready.signatureGroups.all { it.multiplicity == BigInteger.valueOf(1_100L) })
        assertTrue(ready.signatureGroups.all { it.publicObservations.size == 8 })
        assertTrue(ready.signatureGroups.all { it.provenanceFamilies.isNotEmpty() })
        assertTrue(ready.signatureGroups.all { it.recipientDiagnostics.single().after.value > BigInteger.ZERO })
        assertTrue(ready.signatureGroups.all { it.leaveOneOutDiagnostics.size == 8 })

        val first = ready.signatureGroups.first()
        val family = first.provenanceFamilies.single()
        assertEquals(BigInteger.valueOf(1_100L), family.multiplicity)
        assertEquals(
            setOf("setup.red-herring", "setup.demon-bluffs"),
            family.latentOptionIdsByFactor.keys,
        )
        assertEquals(1_100, family.completeBundleIds().count())
        assertEquals(1_100, family.completeBundleIds().toSet().size)
    }

    @Test
    fun `healthy harness defers staged uncertainty sources instead of silently flattening them`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(5, "Investigator", CharacterType.TOWNSFOLK),
            player(6, "Poisoner", CharacterType.MINION),
            player(7, "Imp", CharacterType.DEMON),
        )

        val result = TroubleBrewingFirstNightHealthyBundleHarness.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(game),
            evaluationRecipientSeats = setOf(1),
        )

        assertTrue(result is FirstNightHealthyBundleHarnessEvaluation.Deferred)
        assertEquals(
            setOf(FirstNightBundleDeferredComplexity.POISONER_TARGET),
            (result as FirstNightHealthyBundleHarnessEvaluation.Deferred).deferredComplexities,
        )
    }

    private fun context(game: GameState): ExactHistoricalHypotheticalContext {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "fn-bundle-2-healthy-harness",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-2-seven-player",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        return ExactHistoricalHypotheticalContext(
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
        )
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = TroubleBrewingFixtures.scriptId,
        players = players.toList(),
        seed = 20260916L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}
