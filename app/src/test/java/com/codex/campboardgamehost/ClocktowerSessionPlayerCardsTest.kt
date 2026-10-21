package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionJson
import com.codex.campboardgamehost.clocktower.session.commitConfirmedActualRoleChange
import com.codex.campboardgamehost.clocktower.session.commitConfirmedPlayerDeath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerSessionPlayerCardsTest {
    private val initial = TroubleBrewingFixtures.eightPlayerExample()

    private fun session(id: String) = ClocktowerGameSession.createProduction(
        gameId = id,
        gameSeed = initial.seed,
        initialState = initial,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )

    private fun setupCards() = initial.players.map { player ->
        PlayerCard(
            name = player.name,
            clocktowerRole = clocktowerRoleByName(player.actualRole.value),
            clocktowerShownRole = clocktowerRoleByName(player.shownRole?.value),
            clocktowerTeam = clocktowerRoleByName(player.actualRole.value)?.team,
        )
    }

    @Test
    fun `death and its original round are projected immediately without updating backing cards`() {
        val source = setupCards()
        val session = session("a3-death-presentation")
        val cards = ClocktowerSessionPlayerCards(source, { session.state }, "en")
        assertNull(cards[0].eliminatedRound)

        session.commitConfirmedPlayerDeath(
            draft = ActionFactDraft.Execution("execution-seat-1", StorytellerPhase.DAY, 2, 1, 1),
            expectedGameStateRevision = 0L,
        )
        assertEquals(2, cards[0].eliminatedRound)
        assertNull(source[0].eliminatedRound)
        assertNull(cards[1].eliminatedRound)
    }

    @Test
    fun `accepted role change is visible to the same card list without mirror mutation`() {
        val source = setupCards()
        val session = session("a3-role-presentation")
        val cards = ClocktowerSessionPlayerCards(source, { session.state }, "en")
        val previousShownRole = cards[0].clocktowerShownRole

        session.commitConfirmedActualRoleChange(
            draft = ActionFactDraft.RoleChange(
                actionId = "role-change-seat-1",
                phase = StorytellerPhase.NIGHT,
                round = 2,
                sequence = 1,
                targetSeat = 1,
                role = RoleId("Imp"),
                alignment = Alignment.EVIL,
                type = CharacterType.DEMON,
            ),
            expectedGameStateRevision = 0L,
        )
        assertEquals("Imp", cards[0].clocktowerRole?.enName)
        assertEquals(ClocktowerTeam.Demon, cards[0].clocktowerTeam)
        assertEquals("Imp", cards[0].actualRoleLabel)
        assertEquals(previousShownRole, cards[0].clocktowerShownRole)
        assertEquals(initial.players[0].actualRole.value, source[0].clocktowerRole?.enName)
    }

    @Test
    fun `without Session setup presentation remains independently editable`() {
        val source = mutableListOf(PlayerCard("Setup player"))
        val cards = ClocktowerSessionPlayerCards(source, { null }, "en")
        assertSame(source[0], cards[0])
        source[0] = source[0].copy(eliminatedRound = 3)
        assertEquals(3, cards[0].eliminatedRound)
    }

    @Test fun `restored game presents its complete roster without any setup cards`() {
        val original = session("restored-only")
        val restored = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(original.state)))
        val cards = ClocktowerSessionPlayerCards(emptyList(), { restored.state }, "en")
        assertEquals(initial.players.size, cards.size)
        assertEquals(initial.players.map { it.name }, cards.map { it.name })
        assertEquals("Investigator", cards[5].clocktowerShownRole?.enName)
        assertEquals("Drunk", cards[5].clocktowerRole?.enName)
    }

    @Test fun `clearing or changing setup cannot remove or rename active Session seats`() {
        val source = mutableListOf(PlayerCard("Unrelated setup"))
        val session = session("session-roster")
        val cards = ClocktowerSessionPlayerCards(source, { session.state }, "en")
        assertEquals(initial.players.size, cards.size)
        assertEquals(initial.players[0].name, cards[0].name)
        source.clear()
        assertEquals(initial.players.size, cards.size)
        assertEquals(initial.players.last().name, cards.last().name)
    }

    @Test fun `same presentation follows the active game replacement with different players and size`() {
        var active = session("first-session")
        val cards = ClocktowerSessionPlayerCards(emptyList(), { active.state }, "en")
        assertEquals(8, cards.size)
        val next = initial.copy(players = initial.players.take(5).map { it.copy(name = "New ${it.seat}") })
        active = ClocktowerGameSession.createProduction("second-session", next.seed, next, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        assertEquals(5, cards.size)
        assertEquals("New 1", cards[0].name)
        assertEquals("New 5", cards.last().name)
    }

}
