package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalPairInformationCandidateGeneratorTest {
    @Test
    fun `healthy librarian always includes the only recluse as natural truth`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian"))

        assertEquals(3, candidates.size)
        assertTrue(candidates.all { it.outcome.shownRole == RoleId("Recluse") })
        assertTrue(candidates.all { 3 in it.outcome.candidateSeats })
        assertTrue(candidates.all { it.candidateFamilyId == "natural-truth" })
        assertTrue(candidates.all { it.abilityState == AbilityState.FUNCTIONING })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE })
        assertTrue(candidates.all { it.registrations.isEmpty() })
    }

    @Test
    fun `healthy investigator with the only spy never invents another minion`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Spy", CharacterType.MINION),
            player(4, "Saint", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Investigator"))

        assertEquals(3, candidates.size)
        assertEquals(setOf(RoleId("Spy")), candidates.mapNotNull { it.outcome.shownRole }.toSet())
        assertTrue(candidates.all { 3 in it.outcome.candidateSeats })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE })
    }

    @Test
    fun `librarian with no outsiders receives one typed zero result`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidate = NaturalPairInformationCandidateGenerator
            .generate(game, 1, RoleId("Librarian"))
            .single()

        assertTrue(candidate.outcome.candidateSeats.isEmpty())
        val information = candidate.effects.single() as EffectDraft.PlayerInformation
        assertEquals(InformationValue.NoCharacters(CharacterType.OUTSIDER), information.value)
    }

    @Test
    fun `poisoned ability does not enter the functioning natural truth generator`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK, poisoned = true),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        assertTrue(
            NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian")).isEmpty(),
        )
    }

    @Test
    fun `drunk simulating librarian does not enter the functioning natural truth generator`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Librarian"),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        assertTrue(
            NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian")).isEmpty(),
        )
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260806L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        poisoned: Boolean = false,
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
        poisoned = poisoned,
    )
}
