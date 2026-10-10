package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalDirectOpenAiV1Test {
    private fun case(profile: String? = null) = JSONObject()
        .put("schemaId", "botc.storyteller-provider-request")
        .put("schemaVersion", 1)
        .put("identity", JSONObject().put("decisionId", "test-decision"))
        .put("sourceRevision", JSONObject()
            .put("gameStateRevision", 2).put("playerInputRevision", 1))
        .put("legalCandidates", JSONArray()
            .put(JSONObject().put("candidateId", "seat-1"))
            .put(JSONObject().put("candidateId", "seat-2")))
        .also { if (profile != null) it.put("responseProfile", profile) }

    private fun strategy() = JSONObject()
        .put("situationSummary", "Whole board information overlap")
        .put("issues", JSONArray().put(JSONObject().put("issueId", "A")
            .put("priority", 1).put("seats", JSONArray().put(1).put(2))
            .put("diagnosis", "Two claims may converge")
            .put("futureEffect", "Keep another plausible world")))
        .put("relations", JSONArray())
        .put("intentions", JSONArray().put(JSONObject()
            .put("trigger", "Information appears")
            .put("approach", "Reevaluate pressure")
            .put("tradeoff", "Preserve deductibility")))
        .put("planRevisionNote", "Independent assessment")

    private fun upstream(result: JSONObject) = JSONObject()
        .put("status", "completed")
        .put("output", JSONArray().put(JSONObject()
            .put("type", "message")
            .put("content", JSONArray().put(JSONObject()
                .put("type", "output_text").put("text", result.toString()))))
        .toString()

    @Test
    fun `request uses strict responses schema store false and no credentials`() {
        val raw = PersonalDirectOpenAiV1.buildRequest(
            case().toString(), "gpt-5.6-luna")
        val json = JSONObject(raw)
        assertEquals("gpt-5.6-luna", json.getString("model"))
        assertFalse(json.getBoolean("store"))
        assertTrue(json.getString("instructions").contains("whole-game"))
        assertEquals("json_schema", json.getJSONObject("text")
            .getJSONObject("format").getString("type"))
        assertTrue(json.getJSONObject("text").getJSONObject("format")
            .getBoolean("strict"))
        assertFalse(raw.contains("Bearer"))
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.buildRequest(case().toString(), "https://evil/?key=x")
        }.isFailure)
    }

    @Test
    fun `full global response is wrapped for existing Host validator`() {
        val answer = JSONObject().put("strategy", strategy())
            .put("primaryCandidateId", "seat-1")
            .put("rationale", "Board-level issue with Chef and Investigator")
            .put("alternatives", JSONArray().put(JSONObject()
                .put("candidateId", "seat-2").put("rationale", "Different clue path")))
            .put("uncertainty", JSONArray())
        val result = PersonalDirectOpenAiV1.adaptResponse(
            case().toString(), upstream(answer))
        val decoded = ProductionDrunkAiGatewayV1.decode(result)
        assertEquals("test-decision", decoded.decisionId)
        assertEquals(2, decoded.sourceRevision.gameStateRevision)
        assertTrue(JSONObject(result).has("strategy"))
        assertTrue(runCatching {
            val wrong = JSONObject(answer.toString()).put("primaryCandidateId", "illegal")
            PersonalDirectOpenAiV1.adaptResponse(case().toString(), upstream(wrong))
        }.isFailure)
    }

    @Test
    fun `compact live result preserves one legal ID and bounded memo`() {
        val host = case("COMPACT_MEMO_V1").toString()
        val candidate = JSONObject().put("candidateId", "seat-2")
            .put("planMemo", "Revisit earlier information across seats.")
        val result = JSONObject(PersonalDirectOpenAiV1.adaptResponse(host, upstream(candidate)))
        assertEquals("COMPACT_MEMO_V1", result.getString("responseProfile"))
        assertEquals("seat-2", result.getString("primaryCandidateId"))
        assertEquals(0, result.getJSONArray("alternatives").length())
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptResponse(host, upstream(
                JSONObject(candidate.toString()).put("candidateId", "not-legal")))
        }.isFailure)
    }

    @Test
    fun `committed analysis is analysis-only with exact identity`() {
        val host = JSONObject()
            .put("schemaId", "botc.storyteller-global-analysis-request")
            .put("analysisIdentity", JSONObject().put("analysisId", "committed-setup:g"))
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", 0).put("playerInputRevision", 0))
            .toString()
        val req = JSONObject(PersonalDirectOpenAiV1.buildRequest(host, "gpt-5.6-luna"))
        assertTrue(req.getString("instructions").contains("ANALYSIS ONLY"))
        val result = JSONObject(PersonalDirectOpenAiV1.adaptResponse(
            host, upstream(JSONObject().put("strategy", strategy()))))
        assertEquals("committed-setup:g", result.getString("analysisId"))
        assertFalse(result.has("primaryCandidateId"))
        assertEquals("botc.storyteller-global-analysis-response", result.getString("schemaId"))
    }
}
