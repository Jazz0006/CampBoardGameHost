package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleCandidateSpaceAuditTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `healthy seven player census includes washerwoman and exposes full producer product`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(5, "Investigator", CharacterType.TOWNSFOLK),
            player(6, "Scarlet Woman", CharacterType.MINION),
            player(7, "Imp", CharacterType.DEMON),
        )

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)
        val factors = audit.factors.associateBy { it.factorId }

        assertEquals(20, factors.getValue("pair.washerwoman.seat-1").optionCount)
        assertEquals(5, factors.getValue("pair.investigator.seat-5").optionCount)
        assertEquals(1, factors.getValue("numeric.chef.seat-2").optionCount)
        assertEquals(FirstNightBundleEntryControl.RULE_DETERMINED, factors.getValue("numeric.chef.seat-2").control)
        assertEquals(1, factors.getValue("numeric.empath.seat-3").optionCount)
        assertEquals(5, factors.getValue("setup.red-herring").optionCount)
        assertEquals(220, factors.getValue("setup.demon-bluffs").optionCount)
        assertEquals(BigInteger.valueOf(110_000L), audit.rawCartesianCount)
        assertEquals(audit.rawCartesianCount, audit.legalCompleteBundleCount)
        assertEquals(BigInteger.ZERO, audit.evaluatedCount)
        assertFalse(audit.samplingApplied)
        assertEquals(setOf("fortune-teller-target"), audit.excludedPlayerControlledElements)
        assertTrue(audit.deferredComplexities.isEmpty())

        val setupPairRoles = SetupCandidateGenerator.generatePairInformationCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.PairInformation).abilityRole }
            .toSet()
        assertFalse(RoleId("Washerwoman") in setupPairRoles)
        assertTrue(RoleId("Investigator") in setupPairRoles)
    }

    @Test
    fun `librarian zero outsider result remains one canonical legal pair option`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Scarlet Woman", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)
        val librarian = audit.factors.single { it.factorId == "pair.librarian.seat-1" }

        assertEquals(1, librarian.optionCount)
        assertEquals(FirstNightBundleEntryControl.STORYTELLER_CONTROLLED, librarian.control)
        assertEquals(BigInteger.ONE, audit.rawCartesianCount)
    }

    @Test
    fun `staged uncertainty sources are visible without entering the healthy harness policy`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Washerwoman"),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Chef", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
            player(6, "Empath", CharacterType.TOWNSFOLK),
            player(7, "Imp", CharacterType.DEMON),
        )

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)

        assertEquals(
            setOf(
                FirstNightBundleDeferredComplexity.DRUNK,
                FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION,
                FirstNightBundleDeferredComplexity.POISONER_TARGET,
            ),
            audit.deferredComplexities,
        )
        assertTrue(audit.factors.none { it.factorId.contains("washerwoman") })
    }

    @Test
    fun `registration alternatives are counted at the existing numeric truth owner`() {
        val game = game(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Imp", CharacterType.DEMON),
            player(4, "Investigator", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
        )

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)
        val chef = audit.factors.single { it.factorId == "numeric.chef.seat-1" }

        assertEquals(listOf("value-0", "value-1"), chef.optionIds)
        assertEquals(FirstNightBundleEntryControl.STORYTELLER_CONTROLLED, chef.control)
        assertTrue(FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION in audit.deferredComplexities)
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
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
