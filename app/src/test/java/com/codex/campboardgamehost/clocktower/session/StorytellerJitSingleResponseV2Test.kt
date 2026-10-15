package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerJitSingleResponseV2Test {
    private fun scope(): HostCurrentSingleDecisionJitV2.Scope {
        val players = listOf("Chef", "Empath", "Imp").mapIndexed { index, role ->
            PlayerState(index + 1, "Player ${index + 1}", RoleId(role),
                if (role == "Imp") Alignment.EVIL else Alignment.GOOD,
                if (role == "Imp") CharacterType.DEMON else CharacterType.TOWNSFOLK,
                shownRole = RoleId(role))
        }
        val game = ClocktowerGameSession.createProduction(
            "jit-wire", 21L, GameState(ScriptId("trouble_brewing"), players, 21L),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1)
        return HostCurrentSingleDecisionJitV2.Scope(
            game.state, StorytellerPhase.FIRST_NIGHT, 1,
            StorytellerStrategicContinuationV2.OpenDecision(
                "chef-information", 1, "NIGHT_INFORMATION",
                listOf(
                    StorytellerProviderCandidateV1("c0",
                        StorytellerProviderCandidatePayloadV1.ScalarResult("0")),
                    StorytellerProviderCandidateV1("c1",
                        StorytellerProviderCandidatePayloadV1.ScalarResult("1")),
                ),
                "Chef",
            ),
            3,
        )
    }

    private fun response(target: HostCurrentSingleDecisionJitV2.Scope): JSONObject =
        JSONObject()
            .put("schemaVersion", 2)
            .put("source", StorytellerJitSingleResponseV2.sourceJson(target.source))
            .put("candidateId", "c1")
            .put("updatedStrategyMemory", JSONObject()
                .put("tensions", JSONArray(listOf("Maintain plausible worlds")))
                .put("intentions", JSONArray().put(JSONObject()
                    .put("trigger", "The recorded Host action changes strategy")
                    .put("approach", "Use current full-game context")
                    .put("risk", "Avoid repeating exposed information")))
                .put("uncertainties", JSONArray()))

    @Test fun acceptsExactlyOneCurrentCandidateAndBoundedMemory() {
        val current = scope()
        val verified = StorytellerJitSingleResponseV2.decode(
            current, current, response(current).toString())
        assertEquals("c1", verified.candidateId)
        assertEquals("Maintain plausible worlds",
            verified.updatedStrategyMemory.tensions.single())
        val keys = StorytellerJitSingleResponseV2.responseSchema
            .getJSONObject("properties").keys().asSequence().toSet()
        assertEquals(setOf("schemaVersion", "source", "candidateId", "updatedStrategyMemory"), keys)
    }

    @Test fun rejectsLegacyWholePackageExtraFieldsAndNoncurrentCandidates() {
        val current = scope()
        val legacy = response(current).put("remainingChoices", JSONArray())
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, current, legacy.toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, current,
                response(current).put("candidateId", "other").toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, current,
                response(current).put("schemaVersion", 1).toString())
        }.isFailure)
    }

    @Test fun rejectsIncorrectSourceAndStaleHostBeforeAcceptingStrategicMemory() {
        val current = scope()
        val wrongGame = response(current)
        wrongGame.getJSONObject("source").put("gameId", "different-game")
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, current, wrongGame.toString())
        }.isFailure)
        val changed = current.copy(frozen = current.frozen.copy(playerInputRevision = 1))
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, changed, response(current).toString())
        }.isFailure)
        val oversized = response(current)
        oversized.getJSONObject("updatedStrategyMemory")
            .put("tensions", JSONArray(listOf("a", "b", "c", "d")))
        assertTrue(runCatching {
            StorytellerJitSingleResponseV2.decode(current, current, oversized.toString())
        }.isFailure)
    }
}
