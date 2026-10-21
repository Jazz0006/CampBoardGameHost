package com.codex.campboardgamehost.clocktower

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_ID
import com.codex.campboardgamehost.clocktower.domain.TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_VERSION
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONObject

/**
 * Deterministic V1 interchange codec for the TB canonical snapshot.
 *
 * Encoding uses a fixed field order so golden fixtures can be shared verbatim with EvidenceLab.
 * Decoding validates the schema/script boundary and preserves UNCOMMITTED separately from UNKNOWN.
 */
internal object TroubleBrewingGameSnapshotJsonCodec {
    fun encode(snapshot: TroubleBrewingGameSnapshotV1): String {
        val seats = snapshot.grimoireSeats.joinToString(separator = ",") { seat -> encodeSeat(seat) }
        return buildString {
            append("{\"schemaId\":")
            append(JSONObject.quote(snapshot.schemaId))
            append(",\"schemaVersion\":")
            append(snapshot.schemaVersion)
            append(",\"gameId\":")
            append(JSONObject.quote(snapshot.gameId))
            append(",\"script\":")
            append(JSONObject.quote(snapshot.script.value))
            append(",\"gameSeed\":")
            append(snapshot.gameSeed)
            append(",\"position\":")
            append(encodePosition(snapshot.position))
            append(",\"grimoireSeats\":[")
            append(seats)
            append("],\"setupState\":")
            append(encodeSetupState(snapshot.setupState))
            append("}")
        }
    }

    fun decode(encoded: String): TroubleBrewingGameSnapshotV1 {
        val root = JSONObject(encoded)
        require(root.getString("schemaId") == TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_ID) {
            "Unsupported Trouble Brewing snapshot schema ID."
        }
        require(root.getInt("schemaVersion") == TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_VERSION) {
            "Unsupported Trouble Brewing snapshot schema version."
        }
        require(root.getString("script") == "trouble_brewing") {
            "Trouble Brewing snapshot codec only accepts the trouble_brewing script."
        }

        val positionJson = root.getJSONObject("position")
        val seatsJson = root.getJSONArray("grimoireSeats")
        val seats = (0 until seatsJson.length()).map { index ->
            decodeSeat(seatsJson.getJSONObject(index))
        }
        val setupJson = root.getJSONObject("setupState")

        return TroubleBrewingGameSnapshotV1(
            gameId = root.getString("gameId"),
            gameSeed = root.getLong("gameSeed"),
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.valueOf(positionJson.getString("stage")),
                phase = decodePhaseField(positionJson.getJSONObject("phase")),
                round = decodeIntField(positionJson.getJSONObject("round")),
                gameStateRevision = decodeLongField(
                    positionJson.getJSONObject("gameStateRevision"),
                ),
                playerInputRevision = decodeLongField(
                    positionJson.getJSONObject("playerInputRevision"),
                ),
            ),
            grimoireSeats = seats,
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = decodeBooleanField(setupJson.getJSONObject("hasDrunk")),
                drunkAssignmentSeat = decodeIntField(
                    setupJson.getJSONObject("drunkAssignmentSeat"),
                ),
            ),
        )
    }

    private fun encodePosition(position: TroubleBrewingSnapshotPosition): String =
        "{\"stage\":${JSONObject.quote(position.stage.name)}" +
            ",\"phase\":${encodePhaseField(position.phase)}" +
            ",\"round\":${encodeIntField(position.round)}" +
            ",\"gameStateRevision\":${encodeLongField(position.gameStateRevision)}" +
            ",\"playerInputRevision\":${encodeLongField(position.playerInputRevision)}}"

    private fun encodeSeat(seat: TroubleBrewingSnapshotSeat): String =
        "{\"seat\":${seat.seat}" +
            ",\"shownRoleId\":${encodeStringField(seat.shownRoleId)}" +
            ",\"actualRoleId\":${encodeStringField(seat.actualRoleId)}" +
            ",\"alive\":${encodeBooleanField(seat.alive)}" +
            ",\"poisoned\":${encodeBooleanField(seat.poisoned)}}"

    private fun encodeSetupState(setupState: TroubleBrewingSnapshotSetupState): String =
        "{\"hasDrunk\":${encodeBooleanField(setupState.hasDrunk)}" +
            ",\"drunkAssignmentSeat\":${encodeIntField(setupState.drunkAssignmentSeat)}}"

    private fun encodeStringField(field: SnapshotField<String>): String =
        encodeField(field) { value -> JSONObject.quote(value as String) }

    private fun encodeBooleanField(field: SnapshotField<Boolean>): String =
        encodeField(field) { value -> (value as Boolean).toString() }

    private fun encodeIntField(field: SnapshotField<Int>): String =
        encodeField(field) { value -> (value as Int).toString() }

    private fun encodeLongField(field: SnapshotField<Long>): String =
        encodeField(field) { value -> (value as Long).toString() }

    private fun encodePhaseField(field: SnapshotField<StorytellerPhase>): String =
        encodeField(field) { value -> JSONObject.quote((value as StorytellerPhase).name) }

    private fun encodeField(
        field: SnapshotField<*>,
        encodeKnown: (Any?) -> String,
    ): String = when (field) {
        is SnapshotField.Known<*> ->
            "{\"state\":\"KNOWN\",\"value\":${encodeKnown(field.value)}}"
        SnapshotField.Uncommitted -> "{\"state\":\"UNCOMMITTED\"}"
        SnapshotField.Unknown -> "{\"state\":\"UNKNOWN\"}"
        SnapshotField.NotApplicable -> "{\"state\":\"NOT_APPLICABLE\"}"
    }

    private fun decodeSeat(json: JSONObject): TroubleBrewingSnapshotSeat =
        TroubleBrewingSnapshotSeat(
            seat = json.getInt("seat"),
            shownRoleId = decodeStringField(json.getJSONObject("shownRoleId")),
            actualRoleId = decodeStringField(json.getJSONObject("actualRoleId")),
            alive = decodeBooleanField(json.getJSONObject("alive")),
            poisoned = decodeBooleanField(json.getJSONObject("poisoned")),
        )

    private fun decodeStringField(json: JSONObject): SnapshotField<String> =
        decodeField(json) { value -> value as String }

    private fun decodeBooleanField(json: JSONObject): SnapshotField<Boolean> =
        decodeField(json) { value -> value as Boolean }

    private fun decodeIntField(json: JSONObject): SnapshotField<Int> =
        decodeField(json) { value -> (value as Number).toInt() }

    private fun decodeLongField(json: JSONObject): SnapshotField<Long> =
        decodeField(json) { value -> (value as Number).toLong() }

    private fun decodePhaseField(json: JSONObject): SnapshotField<StorytellerPhase> =
        decodeField(json) { value -> StorytellerPhase.valueOf(value as String) }

    private fun <T> decodeField(
        json: JSONObject,
        decodeKnown: (Any) -> T,
    ): SnapshotField<T> = when (json.getString("state")) {
        "KNOWN" -> SnapshotField.Known(decodeKnown(json.get("value")))
        "UNCOMMITTED" -> SnapshotField.Uncommitted
        "UNKNOWN" -> SnapshotField.Unknown
        "NOT_APPLICABLE" -> SnapshotField.NotApplicable
        else -> error("Unsupported Trouble Brewing snapshot field state.")
    }
}