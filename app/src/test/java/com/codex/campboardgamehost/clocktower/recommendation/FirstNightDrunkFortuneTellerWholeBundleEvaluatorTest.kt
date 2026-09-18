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
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightDrunkFortuneTellerWholeBundleEvaluatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d1d-drunk-ft-test",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Drunk shown Fortune Teller evaluates only Yes No for one player-selected target pair`() {
        val snapshot = snapshot()
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

        val evaluation = TroubleBrewingFirstNightDrunkFortuneTellerWholeBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            request = FirstNightDrunkFortuneTellerWholeBundleRequest(
                drunkSeat = 2,
                selectedTargetSeats = listOf(3, 1),
                evaluationRecipientSeats = setOf(1),
                healthyCore = emptyList(),
            ),
        )

        assertTrue(evaluation is FirstNightDrunkFortuneTellerWholeBundleEvaluation.Ready)
        val ready = evaluation as FirstNightDrunkFortuneTellerWholeBundleEvaluation.Ready
        assertEquals(listOf(1, 3), ready.selectedTargetSeats)
        assertEquals(listOf(YesNoAnswer.NO, YesNoAnswer.YES), ready.candidates.map { it.answer })
        assertTrue(ready.candidates.all { it.semanticTruth == SemanticTruth.NOT_APPLICABLE })

        val core = ready.healthyCoreByRecipient.single()
        ready.candidates.forEach { candidate ->
            val proposition = claimedClue(candidate) as InformationProposition.BooleanResult
            assertEquals(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, proposition.metric)
            assertEquals(listOf(1, 3), proposition.subjectSeats)
            assertTrue(candidate.fullBundleByRecipient.single().after.value > BigInteger.ZERO)
            assertTrue(candidate.fullBundleByRecipient.single().after.value <= core.after.value)
        }
    }

    private fun claimedClue(
        candidate: FirstNightDrunkFortuneTellerCandidateEvaluation,
    ): InformationProposition {
        val publicAny = candidate.publicObservation.proposition as InformationProposition.AnyOf
        val claimedGood = publicAny.alternatives
            .filterIsInstance<InformationProposition.AllOf>()
            .single()
        val shownBranch = claimedGood.propositions
            .filterIsInstance<InformationProposition.AnyOf>()
            .single()
        val functioning = shownBranch.alternatives
            .filterIsInstance<InformationProposition.AllOf>()
            .single()
        return functioning.propositions.last()
    }

    private fun snapshot(): GameSnapshot {
        val players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Fortune Teller"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Soldier", CharacterType.TOWNSFOLK),
            player(5, "Scarlet Woman", CharacterType.MINION),
            player(6, "Imp", CharacterType.DEMON),
        )
        return GameSnapshot(
            gameId = "sde-2d1d-drunk-ft",
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
