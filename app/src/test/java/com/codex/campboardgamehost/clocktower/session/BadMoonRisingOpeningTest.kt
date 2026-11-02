package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import org.junit.Assert.*
import org.junit.Test

class BadMoonRisingOpeningTest {
    @Test fun generatedAndEditedRosterMustRespectGodfatherAndActualShownIdentityBeforeSaving() {
        val names = (1..8).map { "P$it" }
        val generated = BadMoonRisingOpening.generate(names, 12L)
        assertEquals(generated, BadMoonRisingOpening.generate(names, 12L))
        assertTrue(BadMoonRisingOpening.errors(generated).isEmpty())
        val towns = listOf("Grandmother", "Sailor", "Chambermaid", "Exorcist", "Innkeeper", "Gambler")
        fun layout(outsiders: List<String>): GameState {
            val roles = towns.take(6 - outsiders.size) + outsiders + listOf("Godfather", "Po")
            return GameState(BadMoonRisingOpening.script, roles.mapIndexed { i, role ->
                val type = when(role) { "Godfather" -> CharacterType.MINION; "Po" -> CharacterType.DEMON
                    "Lunatic", "Tinker" -> CharacterType.OUTSIDER; else -> CharacterType.TOWNSFOLK }
                PlayerState(i + 1, names[i], RoleId(role), if(type in setOf(CharacterType.DEMON, CharacterType.MINION)) Alignment.EVIL else Alignment.GOOD,
                    type, RoleId(if(role == "Lunatic") "Po" else role))
            }, 12L)
        }
        val plus = layout(listOf("Lunatic", "Tinker"))
        val minus = layout(emptyList())
        BadMoonRisingOpening.requireValid(plus)
        BadMoonRisingOpening.requireValid(minus)
        assertThrows(IllegalArgumentException::class.java) { BadMoonRisingOpening.requireValid(layout(listOf("Tinker"))) }
        val lunatic = plus.players.single { it.actualRole.value == "Lunatic" }
        assertThrows(IllegalArgumentException::class.java) {
            BadMoonRisingOpening.requireValid(plus.copy(players = plus.players.map { if(it == lunatic) it.copy(shownRole = it.actualRole) else it }))
        }
        val session = ClocktowerGameSession.createProduction("bmr-roundtrip", plus.seed, plus, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val saved = ClocktowerSessionJson.encode(session.state)
        assertEquals(session.state, ClocktowerSessionJson.decode(saved))
        assertEquals(plus, session.state.gameState)
    }
}
