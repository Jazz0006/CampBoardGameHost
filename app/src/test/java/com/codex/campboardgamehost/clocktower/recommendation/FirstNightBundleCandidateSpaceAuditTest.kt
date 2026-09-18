package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleCandidateSpaceAuditTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

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
        assertEquals(FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO, librarian.profileExposure)
        assertEquals(BigInteger.ONE, audit.rawCartesianCount)
        assertEquals(BigInteger.ONE, audit.representedPublicProjectionUpperBound)
        assertEquals(BigInteger.ONE, audit.legalCompleteBundleCount)
    }

    @Test
    fun `staged uncertainty sources make complete bundle count explicitly unknown`() {
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
                FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION,
                FirstNightBundleDeferredComplexity.POISONER_TARGET,
            ),
            audit.deferredComplexities,
        )
        assertNull(audit.legalCompleteBundleCount)
        assertEquals(BigInteger.ZERO, audit.evaluatedCount)
        val drunkWasherwoman = audit.factors.single { it.factorId == "pair.washerwoman.seat-2" }
        val expectedDrunkDomain = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 2,
            abilityRole = RoleId("Washerwoman"),
            reliability = com.codex.campboardgamehost.clocktower.domain.ReliabilityState.DRUNK,
        )
        assertEquals(expectedDrunkDomain.map { it.candidateId }.sorted(), drunkWasherwoman.optionIds)
        assertTrue(expectedDrunkDomain.any { it.semanticTruth == com.codex.campboardgamehost.clocktower.domain.SemanticTruth.FALSE })
    }

    @Test
    fun `Drunk shown pair role is a complete legal factor rather than deferred complexity`() {
        val game = game(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Scarlet Woman", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
            player(6, "Soldier", CharacterType.TOWNSFOLK),
        )

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)
        val drunkInvestigator = audit.factors.single { it.factorId == "pair.investigator.seat-2" }
        val legal = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 2,
            abilityRole = RoleId("Investigator"),
            reliability = com.codex.campboardgamehost.clocktower.domain.ReliabilityState.DRUNK,
        )

        assertTrue(FirstNightBundleDeferredComplexity.DRUNK !in audit.deferredComplexities)
        assertEquals(legal.map { it.candidateId }.sorted(), drunkInvestigator.optionIds)
        assertTrue(legal.any { it.semanticTruth == com.codex.campboardgamehost.clocktower.domain.SemanticTruth.FALSE })
        assertTrue(legal.any { it.semanticTruth == com.codex.campboardgamehost.clocktower.domain.SemanticTruth.TRUE })
        assertEquals(audit.rawCartesianCount, audit.legalCompleteBundleCount)
    }

    @Test
    fun `registration alternatives are counted only as known producer options until that stage is enabled`() {
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
        assertNull(audit.legalCompleteBundleCount)
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
