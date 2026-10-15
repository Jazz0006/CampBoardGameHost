package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the direct-result wire shape, NOT game legality or recommendation quality. */
class StorytellerJitDirectResultV2Test {
    private val source = StorytellerJitDirectResultV2.Source(
        "game-42", 8, 9, 15, StorytellerPhase.FIRST_NIGHT, 1, "washerwoman-seat-6", 0)

    private fun result(kind: String, number: Any? = null,
                       boolean: Any? = null, role: Any? = null,
                       seats: List<Int> = emptyList(), target: Any? = null) = JSONObject()
        .put("kind", kind)
        .put("number", number ?: JSONObject.NULL)
        .put("boolean", boolean ?: JSONObject.NULL)
        .put("roleId", role ?: JSONObject.NULL)
        .put("seats", JSONArray(seats))
        .put("targetSeat", target ?: JSONObject.NULL)

    private fun response(result: JSONObject) = JSONObject()
        .put("schemaVersion", 1)
        .put("source", StorytellerJitDirectResultV2.sourceJson(source))
        .put("result", result)
        .put("strategyMemoryUpdate", JSONObject.NULL)

    @Test fun pairInformationIsGeneratedDirectlyWithNoCandidateIdOrOptions() {
        val payload = response(result("PAIR", role = "Monk", seats = listOf(3, 7)))
        val acceptedShape = StorytellerJitDirectResultV2.decode(source, source, payload.toString())
        assertEquals(StorytellerJitDirectResultV2.Kind.PAIR, acceptedShape.result.kind)
        assertEquals("Monk", acceptedShape.result.roleId)
        assertEquals(listOf(3, 7), acceptedShape.result.seats)
        assertNull(acceptedShape.memoryUpdate)
        val outer = StorytellerJitDirectResultV2.responseSchema.getJSONObject("properties")
        assertFalse(outer.has("remainingChoices"))
        assertFalse(outer.has("candidateId"))
        assertFalse(outer.has("legalCandidates"))
    }

    @Test fun scalarBooleanRoleAndTargetAreStrictlyTyped() {
        val cases = listOf(
            result("NUMBER", number = 0),
            result("BOOLEAN", boolean = true),
            result("ROLE", role = "Undertaker"),
            result("PAIR_NONE"),
            result("SEAT", target = 4),
        )
        assertEquals(
            listOf("NUMBER", "BOOLEAN", "ROLE", "PAIR_NONE", "SEAT"),
            cases.map { StorytellerJitDirectResultV2.decode(
                source, source, response(it).toString()).result.kind.name })
    }

    @Test fun staleSourceOldWholePackageIllegalShapeAndExtraKeysAreRejected() {
        val pair = response(result("PAIR", role = "Monk", seats = listOf(3, 7)))
        assertTrue(runCatching {
            StorytellerJitDirectResultV2.decode(
                source, source.copy(playerInputRevision = 10), pair.toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitDirectResultV2.decode(
                source, source, JSONObject(pair.toString()).put(
                    "remainingChoices", JSONArray()).toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitDirectResultV2.decode(
                source, source, JSONObject(pair.toString()).put(
                    "candidateId", "legacy-1").toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitDirectResultV2.decode(
                source, source, response(result("PAIR", role = "Monk",
                    seats = listOf(3, 3))).toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitDirectResultV2.decode(
                source, source, response(result("NUMBER", number = 1, role = "Chef")).toString())
        }.isFailure)
    }

    @Test fun boundedStrategyRevisionCanBeReturnedAlongsideSingleResult() {
        val memory = JSONObject()
            .put("tensions", JSONArray(listOf("Two public claims now conflict.")))
            .put("intentions", JSONArray().put(JSONObject()
                .put("trigger", "Public voting shifted")
                .put("approach", "Keep both worlds discoverable")
                .put("risk", "Do not force an implausible result")))
            .put("uncertainties", JSONArray(listOf("Reason for vote unknown")))
        val payload = response(result("NUMBER", number = 1))
            .put("strategyMemoryUpdate", memory)
        val decoded = StorytellerJitDirectResultV2.decode(source, source, payload.toString())
        assertEquals(1, decoded.result.number)
        assertEquals(1, decoded.memoryUpdate?.intentions?.size)
    }
}
