package com.codex.campboardgamehost

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
        put("cards", AppGameStateJsonCodec.encodeCards(record.cards))
        put("records", AppGameStateJsonCodec.encodeRecords(record.records))
        put("events", AppGameStateJsonCodec.encodeEvents(record.events))
        put("outcome", record.outcome?.let(AppGameStateJsonCodec::encodeOutcome) ?: JSONObject.NULL)
    }

    private fun decodeCurrentEntry(
        entry: JSONObject,
        payload: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): ArchivedGameReview? {
        if (payload.optInt("archiveFormatVersion", 0) != CURRENT_FORMAT_VERSION) return null
        val gameKind = enumByName<GameKind>(payload.optNullableString("gameKind")) ?: return null
        val cards = payload.optJSONArray("cards")
            ?.let { AppGameStateJsonCodec.decodeCards(it, roleByName) }
            .orEmpty()
        if (cards.isEmpty()) return null
        return ArchivedGameReview(
            id = entry.archiveId(),
            archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
            gameKind = gameKind,
            round = payload.optInt("round", 1).coerceAtLeast(1),
            cards = cards,
            records = payload.optJSONArray("records")
                ?.let(AppGameStateJsonCodec::decodeRecords)
                .orEmpty(),
            events = payload.optJSONArray("events")
                ?.let(AppGameStateJsonCodec::decodeEvents)
                .orEmpty(),
            outcome = AppGameStateJsonCodec.decodeOutcome(payload.optJSONObject("outcome")),
        )
    }

    private fun decodeLegacyEntry(
        entry: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): ArchivedGameReview? {
        val snapshot = entry.optJSONObject("snapshot") ?: return null
        val gameKind = enumByName<GameKind>(snapshot.optNullableString("currentGameKind")) ?: return null
        val cards = snapshot.optJSONArray("cards")
            ?.let { AppGameStateJsonCodec.decodeCards(it, roleByName) }
            .orEmpty()
        if (cards.isEmpty()) return null
        return ArchivedGameReview(
            id = entry.archiveId(),
            archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
            gameKind = gameKind,
            round = snapshot.optInt("round", 1).coerceAtLeast(1),
            cards = cards,
            records = snapshot.optJSONArray("records")
                ?.let(AppGameStateJsonCodec::decodeRecords)
                .orEmpty(),
            events = snapshot.optJSONArray("clocktowerEvents")
                ?.let(AppGameStateJsonCodec::decodeEvents)
                .orEmpty(),
            outcome = AppGameStateJsonCodec.decodeOutcome(snapshot.optJSONObject("gameOutcome")),
        )
    }

    private fun JSONObject.archiveId(): Long =
        optLong("id", optLong("archivedAtMillis", 0L))
}
