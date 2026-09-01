package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.PairInformationDisplaySemantics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationLegalDomainTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `reliable pair domain keeps only truthful legal outcomes while drunk domain keeps the complete legal display space`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val ability = RoleId("Investigator")

        val reliable = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = ability,
            reliability = ReliabilityState.RELIABLE,
        )
        val drunk = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = ability,
            reliability = ReliabilityState.DRUNK,
        )
        val fullDisplaySpace = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = ability,
        )

        assertTrue(reliable.isNotEmpty())
        assertTrue(reliable.all { it.semanticTruth == SemanticTruth.TRUE })
        assertEquals(fullDisplaySpace.toSet(), drunk.map { it.outcome }.toSet())
        assertTrue(drunk.any { it.semanticTruth == SemanticTruth.FALSE })
        assertTrue(drunk.size > reliable.size)
    }

    @Test
    fun `registered truth keeps the exact registration fact in the legal domain`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val registered = candidates.firstOrNull { candidate ->
            3 in candidate.outcome.candidateSeats &&
                candidate.registrations.any { it.reason == RegistrationReason.RECLUSE_ABILITY }
        }

        requireNotNull(registered)
        assertEquals(SemanticTruth.TRUE, registered.semanticTruth)
        assertTrue(registered.registrations.any { registration ->
            registration.subjectSeat == 3 &&
                registration.registeredRole == registered.outcome.shownRole &&
                registration.registeredType == CharacterType.MINION &&
                registration.registeredAlignment == Alignment.EVIL &&
                registration.reason == RegistrationReason.RECLUSE_ABILITY
        })
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260901L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}
