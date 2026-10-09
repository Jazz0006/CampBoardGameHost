package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerCommittedAnalysisV1Test {
    private fun snapshot(drunkSeat: Int?): TroubleBrewingGameSnapshotV1 {
        val shown = listOf("chef", "empath", "investigator", "poisoner", "imp")
        val actual = shown.mapIndexed { index, role ->
            if (drunkSeat == index + 1) "drunk" else role
        }
        return TroubleBrewingGameSnapshotV1(
            gameId = "committed-game",
            gameSeed = 13,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.SETUP_COMMITTED,
                phase = SnapshotField.NotApplicable,
                round = SnapshotField.NotApplicable,
            ),
            grimoireSeats = shown.mapIndexed { index, role ->
                TroubleBrewingSnapshotSeat(
                    seat = index + 1,
                    shownRoleId = SnapshotField.Known(role),
                    actualRoleId = SnapshotField.Known(actual[index]),
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(drunkSeat != null),
                drunkAssignmentSeat = drunkSeat?.let { SnapshotField.Known(it) }
                    ?: SnapshotField.NotApplicable,
            ),
        )
    }

    private fun strategy(): StorytellerGlobalStrategyV1 = StorytellerGlobalStrategyV1(
        situationSummary = "Chef and Investigator interaction pressure",
        issues = listOf(StorytellerGlobalIssueV1(
            issueId = "core", priority = 1, seats = listOf(1, 3),
            diagnosis = "Overlap", futureEffect = "More worlds",
        )),
        relations = listOf(StorytellerGlobalRelationV1(1, 3, "core", "Hypothesis")),
        intentions = listOf(StorytellerGlobalIntentionV1("First night", "Balance", "Avoid lies")),
        planRevisionNote = "Precommit note",
    )

    @Test
    fun `committed analysis includes real Drunk and full roster or no Drunk`() {
        for (seat in listOf<Int?>(1, null)) {
            val current = snapshot(seat)
            val body = JSONObject(StorytellerCommittedAnalysisV1.encode(current, strategy()))
            val state = body.getJSONObject("state")
            assertEquals("SETUP_COMMITTED", state.getString("stage"))
            assertEquals(5, state.getJSONArray("seats").length())
            assertEquals(seat ?: "NOT_APPLICABLE", state.get("drunkAssignmentSeat"))
            assertEquals(seat ?: false, state.getBoolean("hasDrunk"))
            assertTrue(!body.has("legalCandidates"))
            assertTrue(!body.has("decisionId"))
            assertEquals("committed-game",
                body.getJSONObject("priorStrategy").getString("sourceGameId"))
        }
    }

    @Test
    fun `strategy callback rejects unrelated game and stale source revision`() {
        val current = snapshot(null)
        val good = JSONObject()
            .put("schemaId", "botc.storyteller-global-analysis-response")
            .put("schemaVersion", 1)
            .put("analysisId", "committed-setup:committed-game")
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", 0)
                .put("playerInputRevision", 0))
            .put("strategy", strategy().toJson())
        assertEquals(1, StorytellerCommittedAnalysisV1.decode(
            good.toString(), current,
        ).relations.size)
        good.put("analysisId", "committed-setup:another-game")
        assertTrue(runCatching {
            StorytellerCommittedAnalysisV1.decode(good.toString(), current)
        }.isFailure)
        good.put("analysisId", "committed-setup:committed-game")
        good.getJSONObject("sourceRevision").put("gameStateRevision", 99)
        assertTrue(runCatching {
            StorytellerCommittedAnalysisV1.decode(good.toString(), current)
        }.isFailure)
    }
}
