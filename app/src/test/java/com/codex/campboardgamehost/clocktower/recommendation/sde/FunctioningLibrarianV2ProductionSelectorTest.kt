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
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContextBuilder
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctioningLibrarianV2ProductionSelectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun `G10 functioning Librarian selects deterministically from the V2 preferred band`() {
        val fixture = g10Fixture()
        val first = FunctioningLibrarianV2ProductionSelector.select(
            context = fixture.context,
            sourceSeat = 4,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            decisionId = G10_DECISION_ID,
            selectionSeed = fixture.game.seed,
        )
        val second = FunctioningLibrarianV2ProductionSelector.select(
            context = fixture.context,
            sourceSeat = 4,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            decisionId = G10_DECISION_ID,
            selectionSeed = fixture.game.seed,
        )

        assertNotNull(first)
        assertEquals(first, second)
        first!!
        assertEquals(BeginnerConservativeV2SelectionMode.PREFERRED_BAND, first.selectionMode)
        assertEquals(
            setOf(
                BeginnerConservativeV2PolicyReasons
                    .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
            ),
            first.reasonCodes,
        )

        val legal = PairInformationLegalDomain.generate(
            game = fixture.context.naturalPairGameState,
            roleDefinitions = fixture.context.roleDefinitions,
            sourceSeat = 4,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
        )
        assertEquals(40, legal.size)
        assertTrue(legal.all { it.semanticTruth.name == "TRUE" })
        val selected = legal.single { it.candidateId == first.candidateId }
        val future = PairInformationFutureFlexibilityProjector
            .project(fixture.context.naturalPairGameState, legal)
            .getValue(selected.candidateId) as FeatureProjection.Projected
        assertTrue(
            PairInformationFutureFlexibilityReasonCodes
                .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in future.value.reasonCodes,
        )
    }

    @Test
    fun `no strict preferred subset falls back to frozen V1 seeded selection identity`() {
        val fixture = fallbackFixture()
        val legal = PairInformationLegalDomain.generate(
            game = fixture.context.naturalPairGameState,
            roleDefinitions = fixture.context.roleDefinitions,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
        )
        val production = requireNotNull(
            FunctioningLibrarianV2ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 1,
                abilityRole = RoleId("Librarian"),
                reliability = ReliabilityState.RELIABLE,
                decisionId = FALLBACK_DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )

        assertEquals(BeginnerConservativeV2SelectionMode.V1_FALLBACK, production.selectionMode)
        assertTrue(production.reasonCodes.isEmpty())

        val synthetic = DecisionFeatureEvaluation.Ready(
            legal.map { candidate ->
                CandidateDecisionFeatures(
                    candidateId = candidate.candidateId,
                    features = DecisionFeatures(
                        strategic = FeatureProjection.Projected(nonContradictoryStrategic()),
                    ),
                )
            },
        )
        val v1 = BeginnerConservativeV1Policy.evaluate(synthetic)
        val expected = requireNotNull(
            BeginnerConservativeV1Selector.select(
                evaluation = v1,
                decisionId = FALLBACK_DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
        assertEquals(expected.candidateId, production.candidateId)
    }

    @Test
    fun `selector fails closed outside exact C5-E scope`() {
        val fixture = g10Fixture()

        assertNull(
            FunctioningLibrarianV2ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Investigator"),
                reliability = ReliabilityState.RELIABLE,
                decisionId = G10_DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
        assertNull(
            FunctioningLibrarianV2ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Librarian"),
                reliability = ReliabilityState.POISONED,
                decisionId = G10_DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
    }

    private fun nonContradictoryStrategic() = StrategicDecisionFeatures(
        demonCoverRetention = StrategicRatio.Defined(1, 1),
        evilTopologyRetention = StrategicRatio.Defined(1, 1),
        evilCoverRetention = StrategicRatio.Defined(1, 1),
        forcedGoodFraction = StrategicRatio.Defined(0, 1),
        forcedEvilFraction = StrategicRatio.Defined(0, 1),
        forcedGoodSeats = emptySet(),
        forcedEvilSeats = emptySet(),
    )

    private fun g10Fixture(): Fixture {
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
        return fixture(game, "g10-c5e")
    }

    private fun fallbackFixture(): Fixture {
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 20_261_004L,
            players = listOf(
                player(1, "Librarian", CharacterType.TOWNSFOLK),
                player(2, "Butler", CharacterType.OUTSIDER),
                player(3, "Mayor", CharacterType.TOWNSFOLK),
                player(4, "Poisoner", CharacterType.MINION),
                player(5, "Imp", CharacterType.DEMON),
            ),
        )
        return fixture(game, "fallback-c5e")
    }

    private fun fixture(game: GameState, gameId: String): Fixture {
        val snapshot = GameSnapshot(
            gameId = gameId,
            gameStateRevision = REVISION.gameStateRevision,
            playerInputRevision = REVISION.playerInputRevision,
            gameSeed = game.seed,
            rulesetRef = validatedRuleset.toRulesetRef("c5e-test", "c5e-test"),
            gameState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val tbSnapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = snapshot,
            phase = com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.FIRST_NIGHT,
            round = 1,
            characterRegistry = validatedRuleset.characterRegistry,
        )
        return Fixture(
            game = game,
            context = TroubleBrewingFirstNightPairDecisionContextBuilder.build(
                snapshot = tbSnapshot,
                characterRegistry = validatedRuleset.characterRegistry,
            ),
        )
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
        val context: TroubleBrewingFirstNightPairDecisionContext,
    )

    private companion object {
        val REVISION = InformationDecisionRevision(9L, 13L)
        const val G10_DECISION_ID = "first-night:LIBRARIAN:g10-c5e:seat-4"
        const val FALLBACK_DECISION_ID = "first-night:LIBRARIAN:fallback-c5e:seat-1"
    }
}
