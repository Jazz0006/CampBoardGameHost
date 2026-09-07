package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameArchiveJsonCodecTest {
    private val imp = ClocktowerRole(
        team = ClocktowerTeam.Demon,
        zhName = "小恶魔",
        enName = "Imp",
        zhDescription = "",
        enDescription = "",
    )
    private val drunk = ClocktowerRole(
        team = ClocktowerTeam.Outsider,
        zhName = "酒鬼",
        enName = "Drunk",
        zhDescription = "",
        enDescription = "",
    )
    private val washerwoman = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "洗衣妇",
        enName = "Washerwoman",
        zhDescription = "",
        enDescription = "",
    )
    private val rolesByName = listOf(imp, drunk, washerwoman).associateBy { it.enName }

    @Test
    fun `new archive entry preserves review data without recovery payload`() {
        val record = GameArchiveRecord(
            gameKind = GameKind.Clocktower,
            round = 3,
            cards = listOf(
                PlayerCard(
                    name = "Alice",
                    role = Role.Civilian,
                    word = "shown text",
                    roleLabel = "洗衣妇",
                    actualRoleLabel = "酒鬼",
                    clocktowerTeam = ClocktowerTeam.Outsider,
                    clocktowerRole = drunk,
                    clocktowerShownRole = washerwoman,
                    eliminatedRound = 2,
                ),
                PlayerCard(
                    name = "Bob",
                    role = Role.Civilian,
                    word = "demon text",
                    roleLabel = "小恶魔",
                    actualRoleLabel = "小恶魔",
                    clocktowerTeam = ClocktowerTeam.Demon,
                    clocktowerRole = imp,
                    clocktowerShownRole = imp,
                ),
            ),
            records = listOf(EliminationRecord(round = 2, playerName = "Alice", note = "executed")),
            events = listOf(
                ClocktowerEvent(
                    sequence = 4,
                    type = ClocktowerEventType.Execution,
                    title = "Execution",
                    detail = "#1 Alice",
                    playerNames = listOf("Alice"),
                    phase = ClocktowerPhase.Day,
                    round = 2,
                ),
            ),
            outcome = GameOutcome(
                title = "Good wins",
                summary = "Demon dead",
                reason = "Execution",
            ),
        )

        val entry = GameArchiveJsonCodec.encodeEntry(
            record = record,
            id = 1234L,
            archivedAtMillis = 5678L,
        )
        val archivePayload = entry.getJSONObject(GameArchiveJsonCodec.PAYLOAD_KEY)

        assertEquals(1234L, entry.getLong("id"))
        assertEquals(5678L, entry.getLong("archivedAtMillis"))
        assertEquals(GameArchiveJsonCodec.CURRENT_FORMAT_VERSION, archivePayload.getInt("archiveFormatVersion"))
        assertEquals("Clocktower", archivePayload.getString("gameKind"))
        assertFalse(entry.has("snapshot"))
        listOf(
            "version",
            "savedAtMillis",
            "screen",
            "currentGameKind",
            "activeGameIdentity",
            "clocktowerNightStarted",
            "clocktowerNightStepIndex",
            "clocktowerEpistemicObservations",
        ).forEach { forbiddenKey ->
            assertFalse("archive payload must not contain recovery key $forbiddenKey", archivePayload.has(forbiddenKey))
        }

        val review = GameArchiveJsonCodec.decodeEntry(entry, rolesByName::get)
        assertNotNull(review)
        requireNotNull(review)
        assertEquals(1234L, review.id)
        assertEquals(5678L, review.archivedAtMillis)
        assertEquals(record.gameKind, review.gameKind)
        assertEquals(record.round, review.round)
        assertEquals(record.cards, review.cards)
        assertEquals(record.records, review.records)
        assertEquals(record.events, review.events)
        assertEquals(record.outcome, review.outcome)
    }

    @Test
    fun `legacy snapshot archive remains readable without active recovery compatibility`() {
        val legacySnapshot = JSONObject().apply {
            put("version", 999_999)
            put("screen", "UnknownFutureScreen")
            put("currentGameKind", GameKind.Clocktower.name)
            put("round", 4)
            put("cards", JSONArray().apply {
                put(JSONObject().apply {
                    put("name", "Alice")
                    put("role", Role.Civilian.name)
                    put("word", "shown text")
                    put("roleLabel", "洗衣妇")
                    put("actualRoleLabel", "酒鬼")
                    put("clocktowerTeam", ClocktowerTeam.Outsider.name)
                    put("clocktowerRole", drunk.enName)
                    put("clocktowerShownRole", washerwoman.enName)
                    put("eliminatedRound", 3)
                })
            })
            put("records", JSONArray().apply {
                put(JSONObject().apply {
                    put("round", 3)
                    put("playerName", "Alice")
                    put("note", "executed")
                })
            })
            put("clocktowerEvents", JSONArray().apply {
                put(JSONObject().apply {
                    put("sequence", 5)
                    put("type", ClocktowerEventType.Execution.name)
                    put("title", "Execution")
                    put("detail", "#1 Alice")
                    put("playerNames", JSONArray().put("Alice"))
                    put("phase", ClocktowerPhase.Day.name)
                    put("round", 3)
                })
            })
            put("gameOutcome", JSONObject().apply {
                put("title", "Good wins")
                put("summary", "Demon dead")
                put("reason", "Execution")
            })
        }
        val legacyEntry = JSONObject().apply {
            put("id", 111L)
            put("archivedAtMillis", 222L)
            put("snapshot", legacySnapshot)
        }

        val review = GameArchiveJsonCodec.decodeEntry(legacyEntry, rolesByName::get)

        assertNotNull(review)
        requireNotNull(review)
        assertEquals(111L, review.id)
        assertEquals(222L, review.archivedAtMillis)
        assertEquals(GameKind.Clocktower, review.gameKind)
        assertEquals(4, review.round)
        assertEquals(drunk, review.cards.single().clocktowerRole)
        assertEquals(washerwoman, review.cards.single().clocktowerShownRole)
        assertEquals("executed", review.records.single().note)
        assertEquals(ClocktowerEventType.Execution, review.events.single().type)
        assertEquals("Good wins", review.outcome?.title)
    }

    @Test
    fun `archive decoder rejects missing review identity or cards`() {
        val missingGameKind = JSONObject().apply {
            put("id", 1L)
            put("archivedAtMillis", 2L)
            put(GameArchiveJsonCodec.PAYLOAD_KEY, JSONObject().apply {
                put("archiveFormatVersion", GameArchiveJsonCodec.CURRENT_FORMAT_VERSION)
                put("round", 1)
                put("cards", JSONArray().put(JSONObject()))
            })
        }
        val emptyCards = JSONObject().apply {
            put("id", 1L)
            put("archivedAtMillis", 2L)
            put(GameArchiveJsonCodec.PAYLOAD_KEY, JSONObject().apply {
                put("archiveFormatVersion", GameArchiveJsonCodec.CURRENT_FORMAT_VERSION)
                put("gameKind", GameKind.Werewolf.name)
                put("round", 1)
                put("cards", JSONArray())
            })
        }

        assertNull(GameArchiveJsonCodec.decodeEntry(missingGameKind, rolesByName::get))
        assertNull(GameArchiveJsonCodec.decodeEntry(emptyCards, rolesByName::get))
        assertTrue(emptyCards.has(GameArchiveJsonCodec.PAYLOAD_KEY))
    }
}
