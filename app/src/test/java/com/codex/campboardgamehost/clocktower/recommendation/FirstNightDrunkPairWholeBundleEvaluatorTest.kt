package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightDrunkPairWholeBundleEvaluatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d1b-drunk-pair-test",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Drunk shown Investigator evaluates every legal pair candidate against the same HealthyCore`() {
        val snapshot = snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val healthyCore = listOf(healthyWasherwomanClaim(snapshot.gameState, formal.snapshotId))
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

        val evaluation = TroubleBrewingFirstNightDrunkPairWholeBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            request = FirstNightDrunkPairWholeBundleRequest(
                drunkSeat = 2,
                evaluationRecipientSeats = setOf(1),
                healthyCore = healthyCore,
            ),
        )

        assertTrue(evaluation is FirstNightDrunkPairWholeBundleEvaluation.Ready)
        val ready = evaluation as FirstNightDrunkPairWholeBundleEvaluation.Ready
        val expectedDomain = PairInformationLegalDomain.generate(
            game = snapshot.gameState,
            roleDefinitions = roles,
            sourceSeat = 2,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.DRUNK,
        ).sortedBy { it.candidateId }

        assertEquals(2, ready.drunkSeat)
        assertEquals(RoleId("Investigator"), ready.shownAbility)
        assertEquals(expectedDomain.map { it.candidateId }, ready.candidates.map { it.candidateId })
        assertTrue(ready.candidates.any { it.semanticTruth == SemanticTruth.FALSE })
        assertTrue(ready.candidates.any { it.semanticTruth == SemanticTruth.TRUE })

        val core = ready.healthyCoreByRecipient.single()
        ready.candidates.forEach { candidate ->
            val full = candidate.fullBundleByRecipient.single()
            val marginal = candidate.marginalByRecipient.single()
            assertEquals(1, full.recipientSeat)
            assertTrue(full.after.value <= core.after.value)
            assertEquals(core.after.value - full.after.value, marginal.rawWorldsRemoved)
            assertTrue(marginal.rawWorldsRemoved >= BigInteger.ZERO)
            assertEquals(ObservationVisibility.PUBLIC, candidate.publicObservation.visibility)
            assertEquals(null, candidate.publicObservation.sourceAbility)
        }

        val falseCandidate = ready.candidates.first { it.semanticTruth == SemanticTruth.FALSE }
        assertTrue(
            "A false canonical Drunk clue must retain mechanically credible worlds via the Drunk branch.",
            falseCandidate.fullBundleByRecipient.single().after.value > BigInteger.ZERO,
        )
    }

    private fun healthyWasherwomanClaim(
        game: GameState,
        formalSnapshotId: String,
    ): EpistemicObservation {
        val candidate = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.RELIABLE,
        ).first()
        val outcome = candidate.outcome
        val information = EffectDraft.PlayerInformation(
            recipientSeat = 1,
            sourceAbility = RoleId("Washerwoman"),
            value = InformationValue.PlayerPair(
                shownRole = requireNotNull(outcome.shownRole),
                seats = outcome.candidateSeats,
            ),
        )
        val privateObservation = EpistemicObservation(
            observationId = "healthy-washerwoman",
            snapshotId = formalSnapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Washerwoman"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = information,
                roleDefinitions = roles,
            ),
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "healthy-core",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "healthy-washerwoman",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = candidate.candidateId,
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    private fun snapshot(): GameSnapshot {
        val players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Soldier", CharacterType.TOWNSFOLK),
            player(5, "Scarlet Woman", CharacterType.MINION),
            player(6, "Imp", CharacterType.DEMON),
        )
        return GameSnapshot(
            gameId = "sde-2d1b-drunk-pair",
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
