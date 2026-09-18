package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightDrunkNumericWholeBundleEvaluatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d1c-drunk-numeric-test",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Drunk shown Empath evaluates complete numeric domain against one HealthyCore`() {
        val snapshot = snapshot(shownRole = "Empath")
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = snapshot.gameState.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )

        val evaluation = TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            request = FirstNightDrunkNumericWholeBundleRequest(
                drunkSeat = 2,
                evaluationRecipientSeats = setOf(1),
                healthyCore = emptyList(),
            ),
        )

        assertTrue(evaluation is FirstNightDrunkNumericWholeBundleEvaluation.Ready)
        val ready = evaluation as FirstNightDrunkNumericWholeBundleEvaluation.Ready
        assertEquals(RoleId("Empath"), ready.shownAbility)
        assertEquals(listOf(0, 1, 2), ready.candidates.map { it.value })
        assertEquals(
            listOf(SemanticTruth.TRUE, SemanticTruth.FALSE, SemanticTruth.FALSE),
            ready.candidates.map { it.semanticTruth },
        )

        val core = ready.healthyCoreByRecipient.single()
        ready.candidates.forEach { candidate ->
            val full = candidate.fullBundleByRecipient.single()
            val marginal = candidate.marginalByRecipient.single()
            assertEquals(ObservationVisibility.PUBLIC, candidate.publicObservation.visibility)
            assertEquals(null, candidate.publicObservation.sourceAbility)
            assertTrue(full.after.value <= core.after.value)
            assertEquals(core.after.value - full.after.value, marginal.rawWorldsRemoved)
            assertTrue(marginal.rawWorldsRemoved >= BigInteger.ZERO)
        }

        assertTrue(
            "A false canonical Drunk numeric clue must retain a mechanically credible Drunk world.",
            ready.candidates.first { it.value == 2 }.fullBundleByRecipient.single().after.value > BigInteger.ZERO,
        )
    }

    @Test
    fun `Drunk shown Chef uses the rules display domain in whole-bundle exact evaluation`() {
        val snapshot = snapshot(shownRole = "Chef")
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = snapshot.gameState.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )

        val evaluation = TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            request = FirstNightDrunkNumericWholeBundleRequest(
                drunkSeat = 2,
                evaluationRecipientSeats = setOf(1),
                healthyCore = emptyList(),
            ),
        )

        assertTrue(evaluation is FirstNightDrunkNumericWholeBundleEvaluation.Ready)
        val ready = evaluation as FirstNightDrunkNumericWholeBundleEvaluation.Ready
        assertEquals(RoleId("Chef"), ready.shownAbility)
        assertEquals(listOf(0, 1, 2), ready.candidates.map { it.value })
        assertTrue(ready.candidates.any { it.semanticTruth == SemanticTruth.TRUE })
        assertTrue(ready.candidates.any { it.semanticTruth == SemanticTruth.FALSE })
        assertTrue(
            ready.candidates.first { it.semanticTruth == SemanticTruth.FALSE }
                .fullBundleByRecipient.single().after.value > BigInteger.ZERO,
        )
    }

    private fun snapshot(shownRole: String): GameSnapshot {
        val players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = shownRole),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Soldier", CharacterType.TOWNSFOLK),
            player(5, "Scarlet Woman", CharacterType.MINION),
            player(6, "Imp", CharacterType.DEMON),
        )
        return GameSnapshot(
            gameId = "sde-2d1c-drunk-numeric",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = 20260918L,
            rulesetRef = rulesetRef,
            gameState = GameState(
                script = TroubleBrewingFixtures.scriptId,
                players = players,
                seed = 20260918L,
            ),
        )
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
