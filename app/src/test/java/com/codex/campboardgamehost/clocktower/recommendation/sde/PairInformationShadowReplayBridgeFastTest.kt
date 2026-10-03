package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContextBuilder
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationShadowReplayBridgeFastTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `small functioning pair domain reaches V1 replay with stable order and future flexibility`() {
        val fixture = fixture()
        val result = PairInformationShadowReplayBridge.evaluate(
            request = request(fixture),
            exactContext = exactContext(fixture),
        )

        val legalIds = result.legalCandidates.map { it.candidateId }
        assertEquals(6, legalIds.size)
        assertEquals(legalIds, result.exactCandidates.map { it.candidateId })
        assertEquals(legalIds, result.sdeCandidates.map { it.candidateId })
        assertEquals(legalIds, result.featureEvaluation.candidateIds)
        assertEquals(legalIds, result.decisionTrace.legalCandidateIds)
        assertEquals(legalIds, result.replayInput.legalCandidateIds)
        assertEquals(
            DecisionPolicyReplayScope.PairInformation(
                script = TroubleBrewingFixtures.scriptId,
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                targetType = CharacterType.TOWNSFOLK,
                reliability = ReliabilityState.RELIABLE,
                truthfulLegalOutcomes = true,
            ),
            result.replayInput.policyScope,
        )

        val observed = result.legalCandidates.single { candidate ->
            candidate.outcome.shownRole == RoleId("Empath") &&
                candidate.outcome.candidateSeats == listOf(2, 3)
        }
        val future = (
            (result.featureEvaluation as DecisionFeatureEvaluation.Ready)
                .candidates
                .single { it.candidateId == observed.candidateId }
                .features
                .futureFlexibility as FeatureProjection.Projected
            ).value
        assertTrue(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                future.reasonCodes,
        )

        val sourceTrace = result.decisionTrace.copy(
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = observed.candidateId,
                source = InformationDecisionSource.MANUAL,
                manualOverride = false,
            ),
        )
        val replayed = MultiPolicyReplayEngine.replay(
            sourceTrace = sourceTrace,
            recomputedInput = result.replayInput,
            policyVersions = listOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
        ).single()

        assertEquals(legalIds, replayed.legalCandidateIds)
        assertEquals(sourceTrace.actualChoice, replayed.actualChoice)
        assertEquals(sourceTrace.historyPrefixRef, replayed.historyPrefixRef)
    }

    @Test
    fun `C5-B fails closed instead of widening into impaired pair information`() {
        val fixture = fixture()
        assertThrows(IllegalArgumentException::class.java) {
            PairInformationShadowReplayBridge.evaluate(
                request = request(fixture, reliability = ReliabilityState.DRUNK),
                exactContext = exactContext(fixture),
            )
        }
    }

    @Test
    fun `C5-B requires exact source revision parity with snapshot-backed pair context`() {
        val fixture = fixture()
        assertThrows(IllegalArgumentException::class.java) {
            PairInformationShadowReplayBridge.evaluate(
                request = request(fixture),
                exactContext = exactContext(
                    fixture,
                    revision = InformationDecisionRevision(
                        REVISION.gameStateRevision + 1,
                        REVISION.playerInputRevision,
                    ),
                ),
            )
        }
    }

    private fun request(
        fixture: Fixture,
        reliability: ReliabilityState = ReliabilityState.RELIABLE,
    ) = PairInformationShadowReplayRequest(
        decisionId = DECISION_ID,
        context = fixture.context,
        sourceSeat = 1,
        abilityRole = RoleId("Washerwoman"),
        reliability = reliability,
        lifecycleStage = LIFECYCLE,
    )

    private fun exactContext(
        fixture: Fixture,
        revision: InformationDecisionRevision = REVISION,
    ) = ExactConsequenceContext(
        validatedRuleset = validatedRuleset,
        exactContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = fixture.gameSnapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(),
            perceivedRolesBySeat = fixture.game.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        ),
        sourceRevision = revision,
    )

    private fun fixture(): Fixture {
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 20_261_003L,
            players = listOf(
                player(1, "Washerwoman", CharacterType.TOWNSFOLK),
                player(2, "Empath", CharacterType.TOWNSFOLK),
                player(3, "Undertaker", CharacterType.TOWNSFOLK),
                player(4, "Poisoner", CharacterType.MINION),
                player(5, "Imp", CharacterType.DEMON),
            ),
        )
        val gameSnapshot = GameSnapshot(
            gameId = "c5b-fast-game",
            gameStateRevision = REVISION.gameStateRevision,
            playerInputRevision = REVISION.playerInputRevision,
            gameSeed = game.seed,
            rulesetRef = validatedRuleset.toRulesetRef("c5b-fast", "c5b-fast"),
            gameState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = gameSnapshot,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = validatedRuleset.characterRegistry,
        )
        return Fixture(
            game = game,
            gameSnapshot = gameSnapshot,
            context = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
                snapshot = snapshot,
                characterRegistry = validatedRuleset.characterRegistry,
            ),
        )
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = if (type == CharacterType.TOWNSFOLK || type == CharacterType.OUTSIDER) {
            Alignment.GOOD
        } else {
            Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )

    private data class Fixture(
        val game: GameState,
        val gameSnapshot: GameSnapshot,
        val context: com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext,
    )

    private companion object {
        val REVISION = InformationDecisionRevision(3L, 5L)
        const val DECISION_ID = "first-night:WASHERWOMAN:c5b-fast-game:seat-1"
        val LIFECYCLE = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 10,
        )
    }
}
