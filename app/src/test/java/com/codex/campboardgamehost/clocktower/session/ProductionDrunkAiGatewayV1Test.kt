package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRecommendationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionDrunkAiGatewayV1Test {
    private val revision = StorytellerDecisionRevision(0, 0)

    private fun decision() = PendingDrunkAssignmentDecision(
        PendingStorytellerDecision(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "live-precommit", requestId = "setup:drunk-seat:live-precommit",
            ),
            revision = revision,
            legalCandidates = listOf(
                StorytellerDecisionCandidate("setup:drunk-seat:seat-1", TroubleBrewingDrunkCandidateRef(1, "chef")),
                StorytellerDecisionCandidate("setup:drunk-seat:seat-2", TroubleBrewingDrunkCandidateRef(2, "empath")),
            ),
        ),
    )

    private fun snapshot() = TroubleBrewingGameSnapshotV1(
        gameId = "live-precommit",
        gameSeed = 42,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
            phase = SnapshotField.NotApplicable,
            round = SnapshotField.NotApplicable,
        ),
        grimoireSeats = listOf(
            seat(1, "chef", SnapshotField.Uncommitted),
            seat(2, "empath", SnapshotField.Uncommitted),
            seat(3, "investigator", SnapshotField.Uncommitted),
            seat(4, "poisoner", SnapshotField.Known("poisoner")),
            seat(5, "imp", SnapshotField.Known("imp")),
        ),
        setupState = TroubleBrewingSnapshotSetupState(
            hasDrunk = SnapshotField.Known(true),
            drunkAssignmentSeat = SnapshotField.Uncommitted,
        ),
    )

    private fun seat(
        number: Int, shown: String, actual: SnapshotField<String>,
    ) = TroubleBrewingSnapshotSeat(
        seat = number,
        shownRoleId = SnapshotField.Known(shown),
        actualRoleId = actual,
        alive = SnapshotField.Known(true),
        poisoned = SnapshotField.Known(false),
    )

    private fun request() = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(
        decision = decision(),
        snapshot = snapshot(),
        gameContext = StorytellerProviderGameContextBuilderV1.build(
            snapshot(), StorytellerProviderRevisionV1(0, 0),
        ),
    )

    private fun response(primary: String = "setup:drunk-seat:seat-1") = StorytellerProviderResponseV1(
        decisionId = "setup:drunk-seat:live-precommit",
        sourceRevision = StorytellerProviderRevisionV1(0, 0),
        outcome = StorytellerProviderOutcomeV1.Recommendation(
            primary = StorytellerProviderRecommendationV1(primary, listOf("Whole-table tension")),
            alternatives = listOf(
                StorytellerProviderRecommendationV1("setup:drunk-seat:seat-2", listOf("Different clue economy")),
            ),
        ),
    )

    @Test
    fun `encodes every displayed role but never invents an uncommitted Drunk seat`() {
        val json = JSONObject(ProductionDrunkAiGatewayV1.encode(request()))
        val seats = json.getJSONObject("state").getJSONArray("seats")
        assertEquals(5, seats.length())
        assertEquals("investigator", seats.getJSONObject(2).getString("shownRoleId"))
        assertEquals("UNCOMMITTED", seats.getJSONObject(2).getString("actualRoleId"))
        assertEquals("imp", seats.getJSONObject(4).getString("actualRoleId"))
        assertEquals("UNCOMMITTED", json.getJSONObject("state").getString("drunkAssignmentSeat"))
        assertEquals(5, json.getJSONArray("playerContext").length())
        assertEquals("NOT_AVAILABLE_AT_SETUP_PRECOMMIT", json.getString("historyCoverage"))
        assertEquals(2, json.getJSONArray("legalCandidates").length())
    }

    @Test
    fun `valid live response decodes but wrong candidate and stale instance fail closed`() {
        val original = decision()
        val request = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(original, snapshot())
        val decoded = ProductionDrunkAiGatewayV1.decode(JSONObject()
            .put("schemaId", StorytellerProviderResponseV1.SCHEMA_ID)
            .put("schemaVersion", 1)
            .put("decisionId", request.identity.decisionId)
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", 0).put("playerInputRevision", 0))
            .put("primaryCandidateId", "setup:drunk-seat:seat-1")
            .put("rationale", "Tension")
            .put("alternatives", org.json.JSONArray().put(JSONObject()
                .put("candidateId", "setup:drunk-seat:seat-2")
                .put("rationale", "Other choice")))
            .put("uncertainty", org.json.JSONArray().put("No historical claims"))
            .toString())
        assertTrue(ProductionDrunkAiGatewayV1.validateCurrent(request, original, original, decoded)
            is StorytellerProviderValidationV1.AcceptedRecommendation)
        assertNull(ProductionDrunkAiGatewayV1.validateCurrent(request, original, decision(), decoded))
        assertTrue(ProductionDrunkAiGatewayV1.validateCurrent(
            request, original, original, response("seat-does-not-exist"),
        ) is StorytellerProviderValidationV1.Rejected)
    }

    @Test
    fun `malformed provider payload is rejected before Host decision changes`() {
        val broken = """{"schemaId":"botc.storyteller-provider-response","schemaVersion":1,
          "decisionId":"x","sourceRevision":{"gameStateRevision":0,"playerInputRevision":0},
          "primaryCandidateId":"fake","rationale":"bad","alternatives":[],
          "uncertainty":[]}"""
        val result = ProductionDrunkAiGatewayV1.decode(broken)
        val original = decision()
        val validation = ProductionDrunkAiGatewayV1.validateCurrent(
            StorytellerProviderRequestFactoryV1.fromDrunkAssignment(original, snapshot()),
            original, original, result,
        )
        assertTrue(validation is StorytellerProviderValidationV1.Rejected)
        assertEquals(2, original.legalCandidates.size)
    }
    @Test
    fun `live provider envelope decodes global tensions and only legal seats`() {
        val req = request()
        val raw = JSONObject()
            .put("schemaId", StorytellerProviderResponseV1.SCHEMA_ID)
            .put("schemaVersion", 1)
            .put("decisionId", req.identity.decisionId)
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", 0).put("playerInputRevision", 0))
            .put("primaryCandidateId", req.legalCandidateIds.first())
            .put("rationale", "Issue-1: overlapping investigator and Chef information")
            .put("alternatives", org.json.JSONArray().put(JSONObject()
                .put("candidateId", req.legalCandidateIds.last())
                .put("rationale", "Alternative future information balance")))
            .put("uncertainty", org.json.JSONArray().put("Unknown social claims"))
            .put("strategy", JSONObject()
                .put("situationSummary", "Combined first-night clues create pressure.")
                .put("issues", org.json.JSONArray().put(JSONObject()
                    .put("issueId", "issue-1")
                    .put("priority", 1)
                    .put("seats", org.json.JSONArray().put(1).put(3).put(4))
                    .put("diagnosis", "Chef and Investigator interact")
                    .put("futureEffect", "Avoid an unreasonably forced opening world")))
                .put("relations", org.json.JSONArray().put(JSONObject()
                    .put("fromSeat", 1).put("toSeat", 3).put("issueId", "issue-1")
                    .put("label", "Hypothesis of information overlap")))
                .put("intentions", org.json.JSONArray().put(JSONObject()
                    .put("trigger", "If the first-night clues converge")
                    .put("approach", "Reevaluate future legal publication")
                    .put("tradeoff", "Never invent registration witnesses")))
                .put("planRevisionNote", "Initial provisional strategy"))
        val envelope = ProductionDrunkAiGatewayV1.decodeGlobal(raw.toString(), req)
        assertEquals(1, requireNotNull(envelope.globalStrategy).relations.size)
        assertTrue(requireNotNull(envelope.globalStrategy).issues.first().seats.contains(4))
        assertTrue(ProductionDrunkAiGatewayV1.validateCurrent(
            req, decision(), decision(), envelope.response,
        ) == null) // A new PendingDecision instance cannot inherit the previous decision's validity.
        raw.getJSONObject("strategy").getJSONArray("relations").getJSONObject(0)
            .put("toSeat", 99)
        assertTrue(runCatching {
            ProductionDrunkAiGatewayV1.decodeGlobal(raw.toString(), req)
        }.isFailure)
    }

}
