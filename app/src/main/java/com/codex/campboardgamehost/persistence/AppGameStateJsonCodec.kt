package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject

/** Shared JSON primitives for persisted player/game facts. */
internal object AppGameStateJsonCodec {
    fun encodeCards(cards: List<PlayerCard>): JSONArray = JSONArray().apply {
        cards.forEach { card -> put(encodeCard(card)) }
    }

    fun decodeCards(
        json: JSONArray,
        roleByName: (String) -> ClocktowerRole?,
    ): List<PlayerCard> = buildList {
        for (index in 0 until json.length()) {
            json.optJSONObject(index)?.let { cardJson ->
                decodeCard(cardJson, roleByName)?.let(::add)
            }
        }
    }

    fun encodeRecords(records: List<EliminationRecord>): JSONArray = JSONArray().apply {
        records.forEach { record ->
            put(JSONObject().apply {
                put("round", record.round)
                put("playerName", record.playerName)
                putNullableString("note", record.note)
            })
        }
    }

    fun decodeRecords(json: JSONArray): List<EliminationRecord> = buildList {
        for (index in 0 until json.length()) {
            val recordJson = json.optJSONObject(index) ?: continue
            val playerName = recordJson.optString("playerName").takeIf { it.isNotBlank() } ?: continue
            add(
                EliminationRecord(
                    round = recordJson.optInt("round", 1),
                    playerName = playerName,
                    note = recordJson.optNullableString("note"),
                ),
            )
        }
    }

    fun encodeEvents(events: List<ClocktowerEvent>): JSONArray = JSONArray().apply {
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

    fun decodeEvents(json: JSONArray): List<ClocktowerEvent> = buildList {
        for (index in 0 until json.length()) {
            val eventJson = json.optJSONObject(index) ?: continue
            val title = eventJson.optString("title").takeIf { it.isNotBlank() } ?: continue
            add(
                ClocktowerEvent(
                    sequence = eventJson.optInt("sequence", 0),
                    type = enumByName<ClocktowerEventType>(eventJson.optNullableString("type"))
                        ?: ClocktowerEventType.System,
                    title = title,
                    detail = eventJson.optString("detail"),
                    playerNames = eventJson.optJSONArray("playerNames")?.toStringList().orEmpty(),
                    phase = enumByName<ClocktowerPhase>(eventJson.optNullableString("phase"))
                        ?: ClocktowerPhase.FirstNight,
                    round = eventJson.optInt("round", 1).coerceAtLeast(1),
                ),
            )
        }
    }

    fun encodeOutcome(outcome: GameOutcome): JSONObject = JSONObject().apply {
        put("title", outcome.title)
        put("summary", outcome.summary)
        put("reason", outcome.reason)
    }

    fun decodeOutcome(json: JSONObject?): GameOutcome? {
        if (json == null) return null
        val title = json.optString("title").takeIf { it.isNotBlank() } ?: return null
        return GameOutcome(
            title = title,
            summary = json.optString("summary"),
            reason = json.optString("reason"),
        )
    }

    private fun encodeCard(card: PlayerCard): JSONObject = JSONObject().apply {
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

    private fun decodeCard(
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
}
