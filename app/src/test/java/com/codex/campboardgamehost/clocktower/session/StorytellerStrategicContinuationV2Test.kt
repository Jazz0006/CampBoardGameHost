package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mechanical contract tests; not a model strategy-quality benchmark. */
class StorytellerStrategicContinuationV2Test {
    private val memory = HostOpeningStrategyMemoryV2(
        tensions = listOf("Good information links three seats."),
        intentions = listOf(
            HostOpeningStrategicIntentionV2(
                "A new player action changes information ecology",
                "Maintain a plausible multi-night story across seats",
                "Never lock in an unobserved future clue",
            ),
            HostOpeningStrategicIntentionV2(
                "Public claims or a manual Host choice change world plausibility",
                "Reassess connected still-open decisions",
                "Do not treat claims as proven truth",
            ),
        ),
        uncertainties = listOf("Future nominations and player choices unknown."),
    )

    private fun session(): ClocktowerGameSession {
        val roles = listOf("Chef", "Empath", "Ravenkeeper", "Butler",
            "Investigator", "Recluse", "Poisoner", "Imp")
        val players = roles.mapIndexed { i, role ->
            val type = when (role) {
                "Recluse", "Butler" -> CharacterType.OUTSIDER
                "Poisoner" -> CharacterType.MINION
                "Imp" -> CharacterType.DEMON
                else -> CharacterType.TOWNSFOLK
            }
            val identity = RoleId(role)
            PlayerState(
                seat = i + 1, name = "Seat " + (i + 1),
                actualRole = identity, shownRole = identity,
                actualAlignment = if (type == CharacterType.DEMON ||
                    type == CharacterType.MINION) Alignment.EVIL else Alignment.GOOD,
                actualType = type)
        }
        return ClocktowerGameSession.createProduction(
            "strategic-continuation-game", 19,
            GameState(ScriptId("trouble_brewing"), players, 19),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    private fun prior() = HostOpeningSemanticAcceptanceV2(
        selection = InitialGlobalOpeningSelectionV1(
            gameId = "strategic-continuation-game",
            gameStateRevision = 0, playerInputRevision = 0,
            rosterFingerprint = "shown-roster-fixed",
            scopeFingerprint = "rules-fixed",
            drunkSeat = null, choices = emptyMap(),
            demonBluffs = null, deferredDecisionIds = emptyList(),
        ),
        conditional = emptyList(),
        strategyMemory = memory,
    )

    private val open = listOf(
        StorytellerStrategicContinuationV2.OpenDecision(
            "current:seat-2", 2, "NIGHT_INFORMATION"),
        StorytellerStrategicContinuationV2.OpenDecision(
            "current:seat-5", 5, "STORYTELLER_DISCRETION"),
    )

    private fun result(host: JSONObject): JSONObject {
        val source = host.getJSONObject("source")
        return JSONObject().put("schemaVersion", 2)
            .put("source", JSONObject(source.toString()))
            .put("revisions", JSONArray().also {
                it.put(JSONObject().put("priorIndex", 0)
                    .put("verdict", "REVISE").put("reason", "New confirmed situation"))
                it.put(JSONObject().put("priorIndex", 1)
                    .put("verdict", "KEEP").put("reason", "Still a valid conditional approach"))
            })
            .put("affectedOpenDecisionIds", JSONArray(listOf("current:seat-2")))
            .put("updatedStrategyMemory", host.getJSONObject("priorStrategyMemory"))
            .put("strategicDirection", "Update entire remaining plan without rewriting history.")
    }

    @Test fun carriesPriorMemoryActualRoleStateCausalActionsAndClaimsSeparately() {
        val session = session()
        session.updateStorytellerPlayerContext(2,
            StorytellerPlayerContextInputV1(claimedRoleIds = listOf(RoleId("Mayor"))))
        session.commitGlobalActionFact(ActionFactDraft.Poison(
            "confirmed-poison", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-info-received",
            phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 1,
            sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 1),
        ))
        val request = JSONObject(StorytellerStrategicContinuationV2.encode(
            session.state, prior(), open))
        val entries = request.getJSONObject("causalHistory").getJSONArray("entries")
        assertEquals(2, entries.length())
        assertEquals("action", entries.getJSONObject(0).getString("kind"))
        assertEquals("Poison", entries.getJSONObject(0).getString("factClass"))
        assertTrue(entries.getJSONObject(0).getString("typedFact").contains("targetSeat=2"))
        assertEquals("observation", entries.getJSONObject(1).getString("kind"))
        assertEquals("PRIVATE", entries.getJSONObject(1).getString("visibility"))
        assertEquals(1, request.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(1).getJSONArray("claimedRoleIds").length())
        assertEquals("Mayor", request.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(1).getJSONArray("claimedRoleIds").getString(0))
        assertEquals("UNKNOWN", request.getJSONObject("causalHistory")
            .getJSONObject("coverage").getJSONObject("PRIOR_DECISIONS").getString("state"))
        assertTrue(request.getBoolean("noUnobservedFutureFactMayBeAssumed"))
        assertEquals(2, request.getJSONObject("priorStrategyMemory")
            .getJSONArray("intentions").length())
        assertEquals(listOf("current:seat-2", "current:seat-5"),
            (0 until request.getJSONArray("openDecisions").length())
                .map { request.getJSONArray("openDecisions")
                    .getJSONObject(it).getString("decisionId") })

        val payload = request.toString()
        val wire = JSONObject(PersonalDirectOpenAiV1.buildRequest(payload, "gpt-5.6-luna"))
        assertFalse(wire.getBoolean("store"))
        assertEquals("botc_whole_game_strategic_continuation_v2",
            wire.getJSONObject("text").getJSONObject("format").getString("name"))
        assertTrue(wire.getString("instructions").contains("KEEP / REVISE / RETIRE"))
        val accepted = StorytellerStrategicContinuationV2.decode(
            payload, result(request).toString())
        assertEquals(setOf("current:seat-2"), accepted.affectedOpenDecisionIds)
        assertEquals(listOf("REVISE", "KEEP"), accepted.revisions.map { it.verdict })
        assertEquals(memory, accepted.updatedMemory)
        assertEquals(session.state.nextTimelineGlobalSequence,
            accepted.exclusiveGlobalSequence)
    }

    @Test fun acceptedPolicyCursorFeedsTheLatestMemoryAcrossMultipleNights() {
        val game = session()
        game.advanceGameStateRevision()
        val initialPolicy = StorytellerStrategicContinuationV2.MemoryCursor.initial(prior())
        val firstInput = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open, initialPolicy))
        val firstModel = result(firstInput)
        firstModel.getJSONObject("updatedStrategyMemory").getJSONArray("intentions")
            .getJSONObject(0).put("approach", "Changed after a real mechanical event")
        val first = StorytellerStrategicContinuationV2.decode(
            firstInput.toString(), firstModel.toString())
        val nextPolicy = initialPolicy.advance(first)
        assertEquals(1, nextPolicy.version)
        assertTrue(nextPolicy.memory.intentions.first().approach.startsWith("Changed"))

        // A new Host state revision changes the causal prefix: the next request
        // must use the newly accepted policy, NOT replay the original opening memory.
        game.recordPlayerInput()
        val nextInput = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open, nextPolicy))
        assertEquals(1, nextInput.getJSONObject("source").getInt("memoryVersion"))
        assertEquals("Changed after a real mechanical event",
            nextInput.getJSONObject("priorStrategyMemory")
                .getJSONArray("intentions").getJSONObject(0).getString("approach"))
        val nextAccepted = StorytellerStrategicContinuationV2.decode(
            nextInput.toString(), result(nextInput).toString())
        val finalPolicy = nextPolicy.advance(nextAccepted)
        assertEquals(2, finalPolicy.version)
        assertTrue(runCatching { nextPolicy.advance(first) }.isFailure)
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.encode(
                game.state, prior(), open, finalPolicy)
        }.isSuccess) // same snapshot can be read, but no next model call is automatic
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.decode(
                nextInput.toString(), result(firstInput).toString())
        }.isFailure)
    }

    @Test fun forbidsInventedChangedOrPublishedDecisionAndStaleResponse() {
        val host = JSONObject(StorytellerStrategicContinuationV2.encode(
            session().state, prior(), open))
        val normal = result(host)
        fun reject(edit: (JSONObject) -> Unit) {
            val forged = JSONObject(normal.toString())
            edit(forged)
            assertTrue(runCatching {
                StorytellerStrategicContinuationV2.decode(host.toString(), forged.toString())
            }.isFailure)
        }
        reject { it.getJSONObject("source").put("gameId", "another") }
        reject { it.getJSONObject("source").put("exclusiveGlobalSequence", 1000) }
        reject { it.getJSONArray("affectedOpenDecisionIds").put("already-published-seat-1") }
        reject { it.getJSONArray("revisions").remove(1) }
        reject { it.getJSONArray("revisions").getJSONObject(1).put("priorIndex", 0) }
        reject { it.getJSONArray("revisions").getJSONObject(0).put("verdict", "INVENTED") }
        reject { it.getJSONObject("updatedStrategyMemory")
            .getJSONArray("intentions").put(JSONObject().put("trigger", "")
                .put("approach", "x").put("risk", "y")) }
        reject { it.put("newInformation", "uncommitted false clue") }
    }

    @Test fun everyRoleAndChangedStateUsesSameGenericStrategyContract() {
        val live = session()
        val original = JSONObject(StorytellerStrategicContinuationV2.encode(
            live.state, prior(), open))
        live.updateGameState(live.state.gameState.copy(
            players = live.state.gameState.players.map {
                if (it.seat == 3) it.copy(alive = false) else it
            }))
        val changed = JSONObject(StorytellerStrategicContinuationV2.encode(
            live.state, prior(), open))
        assertEquals(original.getString("schemaId"), changed.getString("schemaId"))
        assertEquals(original.getJSONObject("priorStrategyMemory").toString(),
            changed.getJSONObject("priorStrategyMemory").toString())
        assertEquals(false, changed.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(2).getBoolean("alive"))
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.decode(changed.toString(),
                result(original).toString())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.encode(
                live.state, prior(), open + open.first())
        }.isFailure)
    }
}
