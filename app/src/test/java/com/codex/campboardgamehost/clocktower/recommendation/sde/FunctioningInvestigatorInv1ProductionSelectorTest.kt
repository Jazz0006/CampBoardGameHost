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

class FunctioningInvestigatorInv1ProductionSelectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun `INV1-A prefers the Empath townsfolk neighbour plus the sole real Minion`() {
        val fixture = pressureFixture()
        val first = FunctioningInvestigatorInv1ProductionSelector.select(
            context = fixture.context,
            sourceSeat = 4,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
            decisionId = DECISION_ID,
            selectionSeed = fixture.game.seed,
        )
        val second = FunctioningInvestigatorInv1ProductionSelector.select(
            context = fixture.context,
            sourceSeat = 4,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
            decisionId = DECISION_ID,
            selectionSeed = fixture.game.seed,
        )

        assertNotNull(first)
        assertEquals(first, second)
        first!!
        assertEquals(PolicyVersions.FUNCTIONING_INVESTIGATOR_INV1_V1, first.policyVersion)
        assertEquals(
            setOf(FunctioningInvestigatorInv1ReasonCodes.PRESERVES_DEMON_DENIABILITY_UNDER_EMPATH_PRESSURE),
            first.reasonCodes,
        )

        val legal = PairInformationLegalDomain.generate(
            game = fixture.context.naturalPairGameState,
            roleDefinitions = fixture.context.roleDefinitions,
            sourceSeat = 4,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val selected = legal.single { candidate -> candidate.candidateId == first.candidateId }

        assertEquals(RoleId("Poisoner"), selected.outcome.shownRole)
        assertEquals(setOf(3, 5), selected.outcome.candidateSeats.toSet())
        assertTrue(selected.registrations.isEmpty())
    }

    @Test
    fun `selector fails closed when Empath is not adjacent to the Demon`() {
        val fixture = noPressureFixture()

        assertNull(
            FunctioningInvestigatorInv1ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Investigator"),
                reliability = ReliabilityState.RELIABLE,
                decisionId = DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
    }

    @Test
    fun `selector fails closed for unreliable Investigator`() {
        val fixture = pressureFixture()

        assertNull(
            FunctioningInvestigatorInv1ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Investigator"),
                reliability = ReliabilityState.POISONED,
                decisionId = DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
    }

    @Test
    fun `selector fails closed when the pressure Empath is poisoned`() {
        val fixture = pressureFixture(empathPoisoned = true)

        assertNull(
            FunctioningInvestigatorInv1ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Investigator"),
                reliability = ReliabilityState.RELIABLE,
                decisionId = DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
    }

    @Test
    fun `selector fails closed outside conservative one Minion scope`() {
        val fixture = multipleMinionsFixture()

        assertNull(
            FunctioningInvestigatorInv1ProductionSelector.select(
                context = fixture.context,
                sourceSeat = 4,
                abilityRole = RoleId("Investigator"),
                reliability = ReliabilityState.RELIABLE,
                decisionId = DECISION_ID,
                selectionSeed = fixture.game.seed,
            ),
        )
    }

    private fun pressureFixture(empathPoisoned: Boolean = false): Fixture = fixture(
        listOf(
            player(1, "Imp", CharacterType.DEMON),
            player(2, "Empath", CharacterType.TOWNSFOLK, poisoned = empathPoisoned),
            player(3, "Monk", CharacterType.TOWNSFOLK),
            player(4, "Investigator", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
            player(6, "Chef", CharacterType.TOWNSFOLK),
            player(7, "Soldier", CharacterType.TOWNSFOLK),
        ),
        gameId = "inv1-pressure",
    )

    private fun noPressureFixture(): Fixture = fixture(
        listOf(
            player(1, "Soldier", CharacterType.TOWNSFOLK),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Monk", CharacterType.TOWNSFOLK),
            player(4, "Investigator", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
            player(6, "Chef", CharacterType.TOWNSFOLK),
            player(7, "Imp", CharacterType.DEMON),
        ),
        gameId = "inv1-no-pressure",
    )

    private fun multipleMinionsFixture(): Fixture = fixture(
        listOf(
            player(1, "Imp", CharacterType.DEMON),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Monk", CharacterType.TOWNSFOLK),
            player(4, "Investigator", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
            player(6, "Spy", CharacterType.MINION),
            player(7, "Chef", CharacterType.TOWNSFOLK),
            player(8, "Soldier", CharacterType.TOWNSFOLK),
            player(9, "Mayor", CharacterType.TOWNSFOLK),
            player(10, "Virgin", CharacterType.TOWNSFOLK),
        ),
        gameId = "inv1-multiple-minions",
    )

    private fun fixture(players: List<PlayerState>, gameId: String): Fixture {
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 20_261_004L,
            players = players,
        )
        val snapshot = GameSnapshot(
            gameId = gameId,
            gameStateRevision = REVISION.gameStateRevision,
            playerInputRevision = REVISION.playerInputRevision,
            gameSeed = game.seed,
            rulesetRef = validatedRuleset.toRulesetRef("inv1-test", "inv1-test"),
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
        poisoned: Boolean = false,
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
        poisoned = poisoned,
    )

    private data class Fixture(
        val game: GameState,
        val context: TroubleBrewingFirstNightPairDecisionContext,
    )

    private companion object {
        val REVISION = InformationDecisionRevision(17L, 23L)
        const val DECISION_ID = "first-night:INVESTIGATOR:inv1:seat-4"
    }
}
