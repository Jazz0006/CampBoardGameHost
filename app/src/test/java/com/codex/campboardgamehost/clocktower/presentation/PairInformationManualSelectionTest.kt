package com.codex.campboardgamehost.clocktower.presentation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationManualSelectionTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `manual model preserves every supplied legal candidate exactly once`() {
        val candidates = listOf(
            candidate("washer-chef-23", "Chef", 2, 3),
            candidate("washer-chef-24", "Chef", 2, 4),
            candidate("washer-monk-34", "Monk", 3, 4),
        )

        val model = PairInformationManualSelection.fromLegalCandidates(candidates)

        assertEquals(listOf(RoleId("Chef"), RoleId("Monk")), model.roleChoices.map { it.shownRole })
        assertEquals(setOf(2 to 3, 2 to 4), model.roleChoices.first().seatPairs.toSet())
        assertEquals(candidates.toSet(), model.allCandidates.toSet())
        assertEquals(candidates.size, model.allCandidates.size)
    }

    @Test
    fun `manual resolve normalizes selected seat order`() {
        val candidate = candidate("investigator-poisoner-23", "Poisoner", 2, 3)
        val model = PairInformationManualSelection.fromLegalCandidates(listOf(candidate))

        assertSame(candidate, model.resolve(RoleId("Poisoner"), firstSeat = 3, secondSeat = 2))
        assertNull(model.resolve(RoleId("Poisoner"), firstSeat = 2, secondSeat = 4))
    }

    @Test
    fun `manual resolve returns exact legal candidate including registration facts`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val registered = requireNotNull(legalCandidates.firstOrNull { candidate ->
            3 in candidate.outcome.candidateSeats &&
                candidate.registrations.any { it.reason == RegistrationReason.RECLUSE_ABILITY }
        })
        val model = PairInformationManualSelection.fromLegalCandidates(legalCandidates)
        val seats = registered.outcome.candidateSeats

        val resolved = model.resolve(
            shownRole = requireNotNull(registered.outcome.shownRole),
            firstSeat = seats[1],
            secondSeat = seats[0],
        )

        assertSame(registered, resolved)
        assertEquals(registered.registrations, resolved?.registrations)
    }

    @Test
    fun `zero result is exposed only when supplied by upstream legal domain`() {
        val pair = candidate("librarian-saint-23", "Saint", 2, 3)
        val zero = PairInformationLegalCandidate(
            candidateId = "librarian-zero",
            outcome = PairInformationOutcome(shownRole = null, targetSeat = null, decoySeat = null),
            semanticTruth = SemanticTruth.FALSE,
            registrations = emptyList(),
        )

        val withZero = PairInformationManualSelection.fromLegalCandidates(listOf(pair, zero))
        val withoutZero = PairInformationManualSelection.fromLegalCandidates(listOf(pair))

        assertSame(zero, withZero.zeroResultCandidate)
        assertNull(withoutZero.zeroResultCandidate)
        assertTrue(withZero.allCandidates.contains(zero))
    }

    private fun candidate(
        id: String,
        shownRole: String,
        firstSeat: Int,
        secondSeat: Int,
    ) = PairInformationLegalCandidate(
        candidateId = id,
        outcome = PairInformationOutcome(
            shownRole = RoleId(shownRole),
            targetSeat = firstSeat,
            decoySeat = secondSeat,
        ),
        semanticTruth = SemanticTruth.FALSE,
        registrations = emptyList(),
    )

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
