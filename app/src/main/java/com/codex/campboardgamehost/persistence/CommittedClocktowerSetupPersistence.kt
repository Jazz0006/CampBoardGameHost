package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistence boundary for an already committed Clocktower setup.
 *
 * The encoded assignments are the restore authority. Provenance is persisted only as origin/audit
 * metadata; decoding never consults a template dataset, selector, recommendation service, or random
 * source to recreate identities.
 */
internal object CommittedClocktowerSetupPersistence {
    const val ROOT_KEY = "committedClocktowerSetup"
    const val SCHEMA_VERSION = 1

    fun encode(setup: CommittedClocktowerSetup): JSONObject = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("scriptId", setup.script.value)
        put("setupSeed", setup.setupSeed)
        put("assignments", JSONArray().apply {
            setup.assignments.forEach { assignment ->
                put(JSONObject().apply {
                    put("seat", assignment.seat)
                    put("actualRole", assignment.actualRole.value)
                    put("shownRole", assignment.shownRole.value)
                })
            }
        })
        put("provenance", JSONObject().apply {
            put("sourceKind", setup.provenance.sourceKind.name)
            put("providerId", setup.provenance.providerId)
            put("candidateId", setup.provenance.candidateId ?: JSONObject.NULL)
        })
    }

    fun decodeOrNull(root: JSONObject): CommittedClocktowerSetup? {
        if (!root.has(ROOT_KEY)) return null
        require(!root.isNull(ROOT_KEY)) { "$ROOT_KEY cannot be null." }
        val json = root.optJSONObject(ROOT_KEY)
            ?: throw IllegalArgumentException("$ROOT_KEY must be an object.")

        val schemaVersion = json.requiredCommittedSetupInt("schemaVersion")
        require(schemaVersion == SCHEMA_VERSION) {
            "Unsupported committed Clocktower setup schema '$schemaVersion'."
        }

        val assignmentsJson = json.requiredCommittedSetupArray("assignments")
        val assignments = buildList {
            for (index in 0 until assignmentsJson.length()) {
                val assignment = assignmentsJson.optJSONObject(index)
                    ?: throw IllegalArgumentException("Committed Clocktower setup assignment $index must be an object.")
                add(
                    CommittedSetupSeat(
                        seat = assignment.requiredCommittedSetupInt("seat"),
                        actualRole = RoleId(assignment.requiredCommittedSetupString("actualRole")),
                        shownRole = RoleId(assignment.requiredCommittedSetupString("shownRole")),
                    ),
                )
            }
        }

        val provenanceJson = json.requiredCommittedSetupObject("provenance")
        val sourceKindName = provenanceJson.requiredCommittedSetupString("sourceKind")
        val sourceKind = enumValues<SetupSourceKind>().firstOrNull { it.name == sourceKindName }
            ?: throw IllegalArgumentException("Invalid committed Clocktower setup source kind '$sourceKindName'.")

        return CommittedClocktowerSetup(
            script = ScriptId(json.requiredCommittedSetupString("scriptId")),
            setupSeed = json.requiredCommittedSetupLong("setupSeed"),
            assignments = assignments,
            provenance = SetupProvenance(
                sourceKind = sourceKind,
                providerId = provenanceJson.requiredCommittedSetupString("providerId"),
                candidateId = provenanceJson.requiredCommittedSetupNullableString("candidateId"),
            ),
        )
    }
}

private fun JSONObject.requiredCommittedSetupString(key: String): String {
    require(has(key) && !isNull(key)) { "Missing required committed Clocktower setup string '$key'." }
    val value = opt(key) as? String
        ?: throw IllegalArgumentException("Committed Clocktower setup '$key' must be a string.")
    require(value.isNotBlank()) { "Committed Clocktower setup '$key' cannot be blank." }
    return value
}

private fun JSONObject.requiredCommittedSetupInt(key: String): Int {
    val value = requiredCommittedSetupIntegralNumber(key).toLong()
    require(value in Int.MIN_VALUE..Int.MAX_VALUE) {
        "Committed Clocktower setup '$key' is outside Int range."
    }
    return value.toInt()
}

private fun JSONObject.requiredCommittedSetupLong(key: String): Long =
    requiredCommittedSetupIntegralNumber(key).toLong()

private fun JSONObject.requiredCommittedSetupIntegralNumber(key: String): Number {
    require(has(key) && !isNull(key)) { "Missing required committed Clocktower setup integer '$key'." }
    val raw = opt(key)
    require(raw is Byte || raw is Short || raw is Int || raw is Long) {
        "Committed Clocktower setup '$key' must be an integer."
    }
    return raw as Number
}

private fun JSONObject.requiredCommittedSetupArray(key: String): JSONArray {
    require(has(key) && !isNull(key)) { "Missing required committed Clocktower setup array '$key'." }
    return optJSONArray(key)
        ?: throw IllegalArgumentException("Committed Clocktower setup '$key' must be an array.")
}

private fun JSONObject.requiredCommittedSetupObject(key: String): JSONObject {
    require(has(key) && !isNull(key)) { "Missing required committed Clocktower setup object '$key'." }
    return optJSONObject(key)
        ?: throw IllegalArgumentException("Committed Clocktower setup '$key' must be an object.")
}

private fun JSONObject.requiredCommittedSetupNullableString(key: String): String? {
    require(has(key)) { "Missing committed Clocktower setup nullable string '$key'." }
    if (isNull(key)) return null
    return requiredCommittedSetupString(key)
}
