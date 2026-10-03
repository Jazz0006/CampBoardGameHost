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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationShadowReplayBridgeTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `G10 Librarian full legal domain reaches V1 replay without candidate or witness loss`() {
        val fixture = fixture()
        val result = PairInformationShadowReplayBridge.evaluate(
            request = request(fixture),
            exactContext = exactContext(fixture),
        )

        val legalIds = result.legalCandidates.map { it.candidateId }
        assertEquals(40, legalIds.size)
        assertEquals(legalIds, result.exactCandidates.map { it.candidateId })
        assertEquals(legalIds, result.sdeCandidates.map { it.candidateId })
        assertEquals(legalIds, result.featureEvaluation.candidateIds)
        assertEquals(legalIds, result.decisionTrace.legalCandidateIds)
        assertEquals(legalIds, result.replayInput.legalCandidateIds)

        val observed = result.legalCandidates.single { candidate ->
            candidate.outcome.shownRole == RoleId("Drunk") &&
                candidate.outcome.candidateSeats == listOf(1, 3)
        }
        val observedFeatures = (result.featureEvaluation as DecisionFeatureEvaluation.Ready)
            .candidates
            .single { it.candidateId == observed.candidateId }
            .features
        val future = observedFeatures.futureFlexibility as FeatureProjection.Projected
        assertTrue(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                future.value.reasonCodes,
        )

        val lowConsequence = result.legalCandidates.single { candidate ->
            candidate.outcome.shownRole == RoleId("Drunk") &&
                candidate.outcome.candidateSeats == listOf(1, 7)
        }
        val lowFuture = (result.featureEvaluation as DecisionFeatureEvaluation.Ready)
            .candidates
            .single { it.candidateId == lowConsequence.candidateId }
            .features
            .futureFlexibility as FeatureProjection.Projected
        assertFalse(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                lowFuture.value.reasonCodes,
        )

        val registered = result.legalCandidates.first { it.registrations.isNotEmpty() }
        val registeredExact = result.exactCandidates.single { it.candidateId == registered.candidateId }
        assertEquals(1, registeredExact.registrationWitnessBindings.size)
        assertEquals(
            registered.registrations.toSet(),
            registeredExact.registrationWitnessBindings.single().registrations,
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
        assertEquals(result.featureEvaluation, replayed.featureEvaluation)
        assertEquals(sourceTrace.actualChoice, replayed.actualChoice)
        assertEquals(sourceTrace.historyPrefixRef, replayed.historyPrefixRef)

        val archive = DecisionTraceArchive(listOf(replayed))
        assertEquals(
            archive,
            DecisionTraceArchiveJsonCodec.decode(DecisionTraceArchiveJsonCodec.encode(archive)),
        )
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
    fun `C5-B requires exact source revision parity with the snapshot-backed pair context`() {
        val fixture = fixture()

        assertThrows(IllegalArgumentException::class.java) {
            PairInformationShadowReplayBridge.evaluate(
                request = request(fixture),
                exactContext = exactContext(
                    fixture,
                    revision = InformationDecisionRevision(
                        gameStateRevision = REVISION.gameStateRevision + 1,
                        playerInputRevision = REVISION.playerInputRevision,
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
        sourceSeat = 4,
        abilityRole = RoleId("Librarian"),
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
                player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
                player(2, "Imp", CharacterType.DEMON),
                player(3, "Undertaker", CharacterType.TOWNSFOLK),
                player(4, "Librarian", CharacterType.TOWNSFOLK),
                player(5, "Spy", CharacterType.MINION),
                player(6, "Monk", CharacterType.TOWNSFOLK),
                player(7, "Mayor", CharacterType.TOWNSFOLK),
                player(8, "Virgin", CharacterType.TOWNSFOLK),
                player(9, "Butler", CharacterType.OUTSIDER),
            ),
        )
        val gameSnapshot = GameSnapshot(
            gameId = "g10-game-2",
            gameStateRevision = REVISION.gameStateRevision,
            playerInputRevision = REVISION.playerInputRevision,
            gameSeed = game.seed,
            rulesetRef = validatedRuleset.toRulesetRef(
                rulesetVersion = "c5b-test",
                sourceRevision = "c5b-test",
            ),
            gameState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = gameSnapshot,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = validatedRuleset.characterRegistry,
        )
        val context = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = validatedRuleset.characterRegistry,
        )
        return Fixture(game, gameSnapshot, context)
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String? = role,
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
        shownRole = shownRole?.let(::RoleId),
    )

    private data class Fixture(
        val game: GameState,
        val gameSnapshot: GameSnapshot,
        val context: com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext,
    )

    private companion object {
        val REVISION = InformationDecisionRevision(
            gameStateRevision = 7L,
            playerInputRevision = 11L,
        )
        const val DECISION_ID = "first-night:LIBRARIAN:g10-game-2:seat-4"
        val LIFECYCLE = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 10,
        )
    }
}
