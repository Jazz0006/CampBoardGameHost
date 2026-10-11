package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightOneShotPlanV1Test {
    private fun scope(): FirstNightOneShotScopeV1 = FirstNightOneShotScopeV1(
        gameId = "opening-8",
        gameStateRevision = 0,
        playerInputRevision = 0,
        legalDemonBluffRoleIds = listOf("Saint", "Monk", "Washerwoman", "Soldier"),
        availableDecisions = listOf(
            FirstNightOneShotDecisionScopeV1(
                "investigator-seat3", 3, "PAIR_INFORMATION", listOf("pair-A", "pair-B"),
            ),
            FirstNightOneShotDecisionScopeV1(
                "chef-seat4", 4, "SCALAR_INFORMATION", listOf("value-0", "value-1"),
            ),
        ),
        deferredDecisionIds = listOf("poisoner-target", "fortune-teller-query"),
    )

    private fun valid(): JSONObject = JSONObject()
        .put("gameId", "opening-8")
        .put("gameStateRevision", 0)
        .put("playerInputRevision", 0)
        .put("demonBluffRoleIds", JSONArray(listOf("Monk", "Saint", "Washerwoman")))
        .put("choices", JSONArray().put(JSONObject()
            .put("decisionId", "investigator-seat3").put("candidateId", "pair-A"))
            .put(JSONObject().put("decisionId", "chef-seat4").put("candidateId", "value-1")))
        .put("deferredDecisionIds", JSONArray(listOf("poisoner-target", "fortune-teller-query")))

    @Test
    fun `one concise output covers full available domain with a legal bluff triple`() {
        val plan = FirstNightOneShotPlanV1.decode(valid(), scope())
        assertTrue(plan.validFor(scope()))
        assertEquals("pair-A", plan.candidateByDecisionId["investigator-seat3"])
        assertEquals(2, scope().providerLegalScope().getJSONArray("availableDecisions").length())
        assertEquals(2, plan.deferredDecisionIds.size)
    }

    @Test
    fun `missing or illegal choices reject the entire one-tap package`() {
        val wrong = valid()
        wrong.put("choices", JSONArray().put(JSONObject()
            .put("decisionId", "investigator-seat3").put("candidateId", "pair-B")))
        assertTrue(runCatching { FirstNightOneShotPlanV1.decode(wrong, scope()) }.isFailure)

        val illegal = valid()
        illegal.put("demonBluffRoleIds", JSONArray(listOf("Monk", "Monk", "Saint")))
        assertTrue(runCatching { FirstNightOneShotPlanV1.decode(illegal, scope()) }.isFailure)

        val noLegalCandidate = valid()
        noLegalCandidate.put("choices", JSONArray().put(JSONObject()
            .put("decisionId", "investigator-seat3").put("candidateId", "not-legal"))
            .put(JSONObject().put("decisionId", "chef-seat4").put("candidateId", "value-1")))
        assertTrue(runCatching { FirstNightOneShotPlanV1.decode(noLegalCandidate, scope()) }.isFailure)
    }

    @Test
    fun `unknown Poisoner and Fortune Teller actions cannot disappear`() {
        val hidden = valid()
        hidden.put("deferredDecisionIds", JSONArray())
        assertTrue(runCatching { FirstNightOneShotPlanV1.decode(hidden, scope()) }.isFailure)
    }

    @Test
    fun `cross-game and changed revision are not one-click adoptable`() {
        val plan = FirstNightOneShotPlanV1.decode(valid(), scope())
        assertFalse(plan.validFor(scope().copy(gameId = "other")))
        assertFalse(plan.validFor(scope().copy(gameStateRevision = 1)))
        assertFalse(plan.validFor(scope().copy(playerInputRevision = 1)))
        assertFalse(plan.validFor(scope().copy(
            availableDecisions = scope().availableDecisions.mapIndexed { index, entry ->
                if (index == 0) entry.copy(legalCandidateIds = listOf("pair-B"))
                else entry
            },
        )))
    }

    @Test
    fun `not applicable Demon bluffs remain absent`() {
        val reduced = scope().copy(legalDemonBluffRoleIds = null)
        val noBluffs = valid().put("demonBluffRoleIds", JSONObject.NULL)
        assertTrue(FirstNightOneShotPlanV1.decode(noBluffs, reduced).validFor(reduced))
        assertTrue(runCatching { FirstNightOneShotPlanV1.decode(valid(), reduced) }.isFailure)
    }
}
