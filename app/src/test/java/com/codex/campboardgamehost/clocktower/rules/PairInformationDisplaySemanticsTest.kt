package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationDisplaySemanticsTest {
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val game = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        ),
        seed = 20260901L,
    )

    @Test
    fun `pair display outcomes use only the ability role type and exclude the source seat`() {
        val outcomes = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
        )

        assertTrue(outcomes.isNotEmpty())
        assertTrue(outcomes.all { outcome ->
            outcome.shownRole != null &&
                roleDefinitions.single { it.id == outcome.shownRole }.type == CharacterType.TOWNSFOLK &&
                outcome.candidateSeats.size == 2 &&
                outcome.candidateSeats.distinct().size == 2 &&
                1 !in outcome.candidateSeats
        })
    }

    @Test
    fun `only Librarian has a zero-character display outcome`() {
        val librarian = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
        )
        val investigator = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
        )
        val washerwoman = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
        )

        assertEquals(1, librarian.count { it.shownRole == null && it.candidateSeats.isEmpty() })
        assertEquals(0, investigator.count { it.shownRole == null || it.candidateSeats.isEmpty() })
        assertEquals(0, washerwoman.count { it.shownRole == null || it.candidateSeats.isEmpty() })
    }

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
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
