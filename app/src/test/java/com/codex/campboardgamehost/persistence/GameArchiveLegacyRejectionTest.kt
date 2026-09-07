package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertNull
import org.junit.Test

class GameArchiveLegacyRejectionTest {
    @Test
    fun `legacy snapshot archive is rejected`() {
        val role = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "洗衣妇",
            enName = "Washerwoman",
            zhDescription = "",
            enDescription = "",
        )
        val legacyEntry = JSONObject().apply {
            put("id", 111L)
            put("archivedAtMillis", 222L)
            put("snapshot", JSONObject().apply {
                put("currentGameKind", GameKind.Clocktower.name)
                put("round", 4)
                put("cards", JSONArray().put(JSONObject().apply {
                    put("name", "Alice")
                    put("role", Role.Civilian.name)
                    put("word", "shown text")
                    put("roleLabel", "洗衣妇")
                    put("actualRoleLabel", "洗衣妇")
                    put("clocktowerTeam", ClocktowerTeam.Townsfolk.name)
                    put("clocktowerRole", role.enName)
                    put("clocktowerShownRole", role.enName)
                }))
            })
        }

        assertNull(GameArchiveJsonCodec.decodeEntry(legacyEntry) { name ->
            role.takeIf { it.enName == name }
        })
    }

    @Test
    fun `current archive without explicit id is rejected`() {
        val role = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "洗衣妇",
            enName = "Washerwoman",
            zhDescription = "",
            enDescription = "",
        )
        val entry = GameArchiveJsonCodec.encodeEntry(
            record = GameArchiveRecord(
                gameKind = GameKind.Clocktower,
                round = 1,
                cards = listOf(
                    PlayerCard(
                        name = "Alice",
                        role = Role.Civilian,
                        word = "shown text",
                        roleLabel = "洗衣妇",
                        actualRoleLabel = "洗衣妇",
                        clocktowerTeam = ClocktowerTeam.Townsfolk,
                        clocktowerRole = role,
                        clocktowerShownRole = role,
                    ),
                ),
                records = emptyList(),
                events = emptyList(),
                outcome = null,
            ),
            id = 111L,
            archivedAtMillis = 222L,
        ).apply {
            remove("id")
        }

        assertNull(GameArchiveJsonCodec.decodeEntry(entry) { name ->
            role.takeIf { it.enName == name }
        })
    }
}
