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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Full G10 C5-B acceptance regression.
 *
 * Deliberately excluded from testFast because all 40 legal outcomes pass through exact consequence
 * evaluation. It remains part of testFull/T4.
 */
class PairInformationShadowReplayBridgeG10FullDomainTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val exactRoles = roles.filter { role ->
        role.id in setOf(
            RoleId("Drunk"),
            RoleId("Butler"),
            RoleId("Recluse"),
            RoleId("Saint"),
            RoleId("Empath"),
            RoleId("Imp"),
            RoleId("Undertaker"),
            RoleId("Librarian"),
            RoleId("Spy"),
            RoleId("Monk"),
            RoleId("Mayor"),
            RoleId("Virgin"),
        )
    }

    @Test
    fun `G10 Librarian full legal domain reaches V1 replay without candidate or witness loss`() {
        val fixture = fixture()
        val result = PairInformationShadowReplayBridge.evaluate(
            request = PairInformationShadowReplayRequest(
                decisionId = DECISION_ID,
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Librarian"),
                reliability = ReliabilityState.RELIABLE,
                lifecycleStage = LIFECYCLE,
            ),
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
        val observedFuture = (
            (result.featureEvaluation as DecisionFeatureEvaluation.Ready)
                .candidates
                .single { it.candidateId == observed.candidateId }
                .features
                .futureFlexibility as FeatureProjection.Projected
            ).value
        assertTrue(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                observedFuture.reasonCodes,
        )

        val lowConsequence = result.legalCandidates.single { candidate ->
            candidate.outcome.shownRole == RoleId("Drunk") &&
                candidate.outcome.candidateSeats == listOf(1, 7)
        }
        val lowFuture = (
            (result.featureEvaluation as DecisionFeatureEvaluation.Ready)
                .candidates
                .single { it.candidateId == lowConsequence.candidateId }
                .features
                .futureFlexibility as FeatureProjection.Projected
            ).value
        assertFalse(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                lowFuture.reasonCodes,
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

    private fun exactContext(fixture: Fixture) = ExactConsequenceContext(
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
            // The pair legal domain above still comes from the full Trouble Brewing registry.
            // Exact replay only needs a bounded role universe that contains every actual/shown
            // G10 role plus every Outsider role a functioning Librarian may truthfully show.
            // This preserves the C5-B bridge contract without turning the acceptance harness into
            // a full-script world-enumeration benchmark.
            roleDefinitions = exactRoles,
        ),
        sourceRevision = REVISION,
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
        val gameSnapshot = gameSnapshot(game, "g10-game-2")
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

    private fun gameSnapshot(game: GameState, gameId: String) = GameSnapshot(
        gameId = gameId,
        gameStateRevision = REVISION.gameStateRevision,
        playerInputRevision = REVISION.playerInputRevision,
        gameSeed = game.seed,
        rulesetRef = validatedRuleset.toRulesetRef("c5b-test", "c5b-test"),
        gameState = game,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String? = role,
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
        shownRole = shownRole?.let(::RoleId),
    )

    private data class Fixture(
        val game: GameState,
        val gameSnapshot: GameSnapshot,
        val context: com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext,
    )

    private companion object {
        val REVISION = InformationDecisionRevision(7L, 11L)
        const val DECISION_ID = "first-night:LIBRARIAN:g10-game-2:seat-4"
        val LIFECYCLE = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 10,
        )
    }
}
