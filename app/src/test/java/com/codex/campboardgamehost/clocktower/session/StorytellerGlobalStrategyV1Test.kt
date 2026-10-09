package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerGlobalStrategyV1Test {
    private fun global(): JSONObject = JSONObject()
        .put("situationSummary", "Chef and Investigator can combine pressure on the evil pair.")
        .put("issues", JSONArray().put(JSONObject()
            .put("issueId", "pressure-1")
            .put("priority", 1)
            .put("seats", JSONArray().put(1).put(3).put(4))
            .put("diagnosis", "Multiple investigators reinforce the same world")
            .put("futureEffect", "Maintain several plausible worlds without falsifying known facts")))
        .put("relations", JSONArray().put(JSONObject()
            .put("fromSeat", 1)
            .put("toSeat", 3)
            .put("issueId", "pressure-1")
            .put("label", "Hypothesis of information interaction")))
        .put("intentions", JSONArray().put(JSONObject()
            .put("trigger", "First-night information overlaps")
            .put("approach", "Reevaluate future legal clues against pressure")
            .put("tradeoff", "Do not guarantee a future result")))
        .put("planRevisionNote", "First provisional plan")

    @Test
    fun `decodes global first issue relations and future intentions`() {
        val parsed = StorytellerGlobalStrategyV1.decode(global(), (1..5).toSet())
        assertEquals(3, parsed.issues.first().seats.size)
        assertEquals(1, parsed.relations.size)
        assertEquals(1, parsed.intentions.size)
        assertTrue(parsed.situationSummary.contains("Investigator"))
    }

    @Test
    fun `refuses model invented seats issue pointers or empty global diagnosis`() {
        val badSeats = global().put("relations", JSONArray().put(JSONObject()
            .put("fromSeat", 1).put("toSeat", 9)
            .put("issueId", "pressure-1").put("label", "Fake relation")))
        assertInvalid(badSeats)
        val badPointer = global()
        badPointer.getJSONArray("relations").getJSONObject(0).put("issueId", "not-real")
        assertInvalid(badPointer)
        val noIssues = global().put("issues", JSONArray())
        assertInvalid(noIssues)
        val noPlans = global().put("intentions", JSONArray())
        assertInvalid(noPlans)
    }

    private fun assertInvalid(raw: JSONObject) {
        val rejected = runCatching { StorytellerGlobalStrategyV1.decode(raw, (1..5).toSet()) }
        assertTrue(rejected.isFailure)
    }
}
