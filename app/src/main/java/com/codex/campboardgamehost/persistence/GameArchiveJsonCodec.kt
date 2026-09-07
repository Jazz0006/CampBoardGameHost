package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject

internal data class GameArchiveRecord(
    val gameKind: GameKind,
    val round: Int,
    val cards: List<PlayerCard>,
    val records: List<EliminationRecord>,
    val events: List<ClocktowerEvent>,
    val outcome: GameOutcome?,
)

internal object GameArchiveJsonCodec {
    internal const val PAYLOAD_KEY = "archive"
    internal const val CURRENT_FORMAT_VERSION = 1

    fun encodeEntry(
        record: GameArchiveRecord,
        id: Long,
        archivedAtMillis: Long,
    ): JSONObject = JSONObject().apply {
        put("id", id)
        put("archivedAtMillis", archivedAtMillis)
        put(PAYLOAD_KEY, encodePayload(record))
    }

    fun decodeEntry(
        entry: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): ArchivedGameReview? {
        val payload = entry.optJSONObject(PAYLOAD_KEY)
        return if (payload != null) {
            decodeCurrentEntry(entry, payload, roleByName)
        } else {
            decodeLegacyEntry(entry, roleByName)
        }
    }

    private fun encodePayload(record: GameArchiveRecord): JSONObject = JSONObject().apply {
        put("archiveFormatVersion", CURRENT_FORMAT_VERSION)
        put("gameKind", record.gameKind.name)
        put("round", record.round.coerceAtLeast(1))
        put("cards", cardsToJson(record.cards))
        put("records", recordsToJson(record.records))
        put("events", eventsToJson(record.events))
        put("outcome", record.outcome?.let(::outcomeToJson) ?: JSONObject.NULL)
    }

    private fun decodeCurrentEntry(
        entry: JSONObject,
        payload: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): ArchivedGameReview? {
        if (payload.optInt("archiveFormatVersion", 0) != CURRENT_FORMAT_VERSION) return null
        val gameKind = enumByName<GameKind>(payload.optNullableString("gameKind")) ?: return null
        val cards = payload.optJSONArray("cards")?.toCards(roleByName).orEmpty()
        if (cards.isEmpty()) return null
        return ArchivedGameReview(
            id = entry.archiveId(),
            archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
            gameKind = gameKind,
            round = payload.optInt("round", 1).coerceAtLeast(1),
            cards = cards,
            records = payload.optJSONArray("records")?.toRecords().orEmpty(),
            events = payload.optJSONArray("events")?.toEvents().orEmpty(),
            outcome = outcomeFromJson(payload.optJSONObject("outcome")),
        )
    }

    private fun decodeLegacyEntry(
        entry: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): ArchivedGameReview? {
        val snapshot = entry.optJSONObject("snapshot") ?: return null
        val gameKind = enumByName<GameKind>(snapshot.optNullableString("currentGameKind")) ?: return null
        val cards = snapshot.optJSONArray("cards")?.toCards(roleByName).orEmpty()
        if (cards.isEmpty()) return null
        return ArchivedGameReview(
            id = entry.archiveId(),
            archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
            gameKind = gameKind,
            round = snapshot.optInt("round", 1).coerceAtLeast(1),
            cards = cards,
            records = snapshot.optJSONArray("records")?.toRecords().orEmpty(),
            events = snapshot.optJSONArray("clocktowerEvents")?.toEvents().orEmpty(),
            outcome = outcomeFromJson(snapshot.optJSONObject("gameOutcome")),
        )
    }

    private fun JSONObject.archiveId(): Long =
        optLong("id", optLong("archivedAtMillis", 0L))

    private fun cardsToJson(cards: List<PlayerCard>): JSONArray = JSONArray().apply {
        cards.forEach { card -> put(cardToJson(card)) }
    }

    private fun cardToJson(card: PlayerCard): JSONObject = JSONObject().apply {
        put("name", card.name)
        put("role", card.role.name)
        put("word", card.word)
        putNullableString("roleLabel", card.roleLabel)
        putNullableString("actualRoleLabel", card.actualRoleLabel)
        putNullableString("clocktowerTeam", card.clocktowerTeam?.name)
        putNullableString("clocktowerRole", card.clocktowerRole?.enName)
        putNullableString("clocktowerShownRole", card.clocktowerShownRole?.enName)
        putNullableInt("eliminatedRound", card.eliminatedRound)
    }

    private fun JSONArray.toCards(roleByName: (String) -> ClocktowerRole?): List<PlayerCard> = buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let { json ->
                cardFromJson(json, roleByName)?.let(::add)
            }
        }
    }

    private fun cardFromJson(
        json: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): PlayerCard? {
        val name = json.optString("name").takeIf { it.isNotBlank() } ?: return null
        val role = enumByName<Role>(json.optNullableString("role")) ?: return null
        val clocktowerRole = json.optNullableString("clocktowerRole")?.let(roleByName)
        val clocktowerShownRole = json.optNullableString("clocktowerShownRole")?.let(roleByName)
        val clocktowerTeam = enumByName<ClocktowerTeam>(json.optNullableString("clocktowerTeam"))
            ?: clocktowerRole?.team
        return PlayerCard(
            name = name,
            role = role,
            word = json.optString("word"),
            roleLabel = json.optNullableString("roleLabel"),
            actualRoleLabel = json.optNullableString("actualRoleLabel"),
            clocktowerTeam = clocktowerTeam,
            clocktowerRole = clocktowerRole,
            clocktowerShownRole = clocktowerShownRole,
            eliminatedRound = json.optNullableInt("eliminatedRound"),
        )
    }

    private fun recordsToJson(records: List<EliminationRecord>): JSONArray = JSONArray().apply {
        records.forEach { record ->
            put(JSONObject().apply {
                put("round", record.round)
                put("playerName", record.playerName)
                putNullableString("note", record.note)
            })
        }
    }

    private fun JSONArray.toRecords(): List<EliminationRecord> = buildList {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            val playerName = json.optString("playerName").takeIf { it.isNotBlank() } ?: continue
            add(
                EliminationRecord(
                    round = json.optInt("round", 1),
                    playerName = playerName,
                    note = json.optNullableString("note"),
                ),
            )
        }
    }

    private fun outcomeToJson(outcome: GameOutcome): JSONObject = JSONObject().apply {
        put("title", outcome.title)
        put("summary", outcome.summary)
        put("reason", outcome.reason)
    }

    private fun outcomeFromJson(json: JSONObject?): GameOutcome? {
        if (json == null) return null
        val title = json.optString("title").takeIf { it.isNotBlank() } ?: return null
        return GameOutcome(
            title = title,
            summary = json.optString("summary"),
            reason = json.optString("reason"),
        )
    }

    private fun eventsToJson(events: List<ClocktowerEvent>): JSONArray = JSONArray().apply {
        events.forEach { event ->
            put(JSONObject().apply {
                put("sequence", event.sequence)
                put("type", event.type.name)
                put("title", event.title)
                put("detail", event.detail)
                put("playerNames", stringsToJsonArray(event.playerNames))
                put("phase", event.phase.name)
                put("round", event.round)
            })
        }
    }

    private fun JSONArray.toEvents(): List<ClocktowerEvent> = buildList {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            val title = json.optString("title").takeIf { it.isNotBlank() } ?: continue
            add(
                ClocktowerEvent(
                    sequence = json.optInt("sequence", 0),
                    type = enumByName<ClocktowerEventType>(json.optNullableString("type"))
                        ?: ClocktowerEventType.System,
                    title = title,
                    detail = json.optString("detail"),
                    playerNames = json.optJSONArray("playerNames")?.toStringList().orEmpty(),
                    phase = enumByName<ClocktowerPhase>(json.optNullableString("phase"))
                        ?: ClocktowerPhase.FirstNight,
                    round = json.optInt("round", 1).coerceAtLeast(1),
                ),
            )
        }
    }
}
