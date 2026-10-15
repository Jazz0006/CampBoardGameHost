package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression coverage for the V2 request/response wire contract after V1 retirement. */
class PersonalDirectOpenAiV1Test {
    private fun host(id: String) = JSONObject().put("schemaId", id)
        .put("schemaVersion", 2).put("requestIdentity", JSONObject().put("gameId", "g"))

    private fun upstream(status: String, model: JSONObject): String =
        JSONObject().put("status", status).put("output", JSONArray()
            .put(JSONObject().put("type", "message")
                .put("content", JSONArray().put(JSONObject()
                    .put("type", "output_text").put("text", model.toString()))))).toString()

    @Test fun openingUsesStrictV2SchemaAndPreservesHostValidationResponsibility() {
        val h = host(InitialJointOpeningDirectV2.REQUEST_SCHEMA)
        val req = JSONObject(PersonalDirectOpenAiV1.buildRequest(h.toString(), "gpt-6-luna"))
        val format = req.getJSONObject("text").getJSONObject("format")
        assertEquals("botc_initial_joint_opening_v2", format.getString("name"))
        assertEquals("json_schema", format.getString("type"))
        assertTrue(format.getBoolean("strict"))
        assertEquals(6, format.getJSONObject("schema").getJSONArray("required").length())
        assertTrue(format.getJSONObject("schema").getJSONObject("properties")
            .getJSONObject("openingPlan").getJSONObject("properties").has("drunkSeat"))
        assertFalse(format.getJSONObject("schema").getJSONObject("properties").has("primaryCandidateId"))
        assertFalse(req.getBoolean("store"))
        assertEquals(h.toString(), req.getString("input"))
        val model = JSONObject().put("schemaVersion", 2).put("openingPlan", JSONObject())
        val result = JSONObject(PersonalDirectOpenAiV1.adaptResponse(
            h.toString(), upstream("completed", model)))
        assertTrue(result.has("openingPlan"))
        assertFalse(result.has("primaryCandidateId"))
    }

    @Test fun continuationUsesOnlyV2WholeGameSchema() {
        val req = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            host(StorytellerStrategicContinuationV2.REQUEST_SCHEMA).toString(), "gpt-6-luna"))
        val format = req.getJSONObject("text").getJSONObject("format")
        assertEquals("botc_whole_game_strategic_continuation_v3", format.getString("name"))
        assertTrue(format.getBoolean("strict"))
        assertFalse(req.getBoolean("store"))
    }

    @Test fun retainedOpeningAndChainedContinuationKeepHostContracts() {
        val opening = host(InitialJointOpeningDirectV2.REQUEST_SCHEMA).toString()
        val first = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            opening, "gpt-6-luna", retainForContinuation = true))
        assertTrue(first.getBoolean("store"))
        assertFalse(first.has("previous_response_id"))
        assertFalse(first.has("service_tier"))
        assertEquals(opening, first.getString("input"))
        val priorId = "resp_0123456789abcdef"
        val nextPayload = host(StorytellerStrategicContinuationV2.REQUEST_SCHEMA).toString()
        val next = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            nextPayload, "gpt-6-luna", priorId, retainForContinuation = true))
        assertTrue(next.getBoolean("store"))
        assertEquals(priorId, next.getString("previous_response_id"))
        assertEquals(StorytellerStrategicContinuationV2.INSTRUCTIONS,
            next.getString("instructions"))
        assertTrue(next.getJSONObject("text").getJSONObject("format").getBoolean("strict"))
        assertEquals(nextPayload, next.getString("input"))
    }

    @Test fun retainedReceiptRequiresCompletedModelAndProviderAnchor() {
        val h = host(InitialJointOpeningDirectV2.REQUEST_SCHEMA).toString()
        val model = JSONObject().put("schemaVersion", 2)
        val upstream = JSONObject(upstream("completed", model))
            .put("id", "resp_0123456789abcdef").put("store", true).toString()
        val parsed = PersonalDirectOpenAiV1.adaptRetainedResponse(h, upstream)
        assertEquals("resp_0123456789abcdef", parsed.responseId)
        assertEquals(2, JSONObject(parsed.text).getInt("schemaVersion"))
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptRetainedResponse(h, upstream("completed", model))
        }.isFailure)
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptRetainedResponse(h, JSONObject(upstream)
                .put("id", "untrusted-string").toString())
        }.isFailure)
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptRetainedResponse(h, JSONObject(upstream)
                .put("store", false).toString())
        }.isFailure)
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptRetainedResponse(h, JSONObject(upstream)
                .put("status", "incomplete").toString())
        }.isFailure)
    }

    @Test fun unretainedOrForgedContinuationFailsClosed() {
        val h = host(StorytellerStrategicContinuationV2.REQUEST_SCHEMA).toString()
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.buildRequest(h, "gpt-6-luna",
                "resp_0123456789abcdef", retainForContinuation = false)
        }.isFailure)
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.buildRequest(h, "gpt-6-luna",
                "resp_bad\\nmalicious", retainForContinuation = true)
        }.isFailure)
    }

    @Test fun retiredRequestsAndInvalidModelCannotBeSent() {
        for (id in listOf("botc.storyteller-provider-request",
                "botc.storyteller-global-analysis-request",
                "botc.first-night-one-shot-request")) {
            assertTrue(runCatching {
                PersonalDirectOpenAiV1.buildRequest(host(id).toString(), "gpt-6-luna")
            }.isFailure)
            assertTrue(runCatching {
                PersonalDirectOpenAiV1.adaptResponse(host(id).toString(),
                    upstream("completed", JSONObject()))
            }.isFailure)
        }
        val h = host(InitialJointOpeningDirectV2.REQUEST_SCHEMA).toString()
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.buildRequest(h, "https://invalid/model")
        }.isFailure)
        assertTrue(runCatching {
            PersonalDirectOpenAiV1.adaptResponse(h, upstream("incomplete", JSONObject()))
        }.isFailure)
    }
}
