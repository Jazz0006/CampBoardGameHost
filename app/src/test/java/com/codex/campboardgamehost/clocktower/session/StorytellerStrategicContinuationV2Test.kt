package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
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

    private val open = listOf(StorytellerStrategicContinuationV2.OpenDecision(
        decisionId = "current:seat-2",
        sourceSeat = 2,
        family = "NIGHT_INFORMATION",
        legalCandidates = listOf(
            StorytellerProviderCandidateV1("empath-zero", StorytellerProviderCandidatePayloadV1.ScalarResult("0")),
            StorytellerProviderCandidateV1("empath-one", StorytellerProviderCandidatePayloadV1.ScalarResult("1")),
        ),
        abilityRoleId = "Empath",
        currentDecisionContext = StorytellerProviderDecisionContextV1.ScalarInformation(
            2, RoleId("Empath"), ReliabilityState.RELIABLE,
            StorytellerProviderScalarKindV1.NUMBER, "LIVING_EVIL_NEIGHBOURS", listOf(1, 3)),
    ))

    private fun semantic(
        kind: String = "SCALAR_RESULT",
        scalar: String? = "1",
        role: String? = null,
        pair: List<Int> = emptyList(),
        seat: Int? = null,
        status: String? = null,
        registrationRole: String? = null,
    ) = JSONObject()
        .put("kind", kind)
        .put("scalarValue", scalar ?: JSONObject.NULL)
        .put("pairRoleId", role ?: JSONObject.NULL)
        .put("pairSeats", JSONArray(pair))
        .put("seat", seat ?: JSONObject.NULL)
        .put("registrationStatus", status ?: JSONObject.NULL)
        .put("registrationRoleId", registrationRole ?: JSONObject.NULL)

    private fun response(host: JSONObject, value: JSONObject = semantic()) =
        JSONObject()
            .put("schemaVersion", 3)
            .put("source", JSONObject(host.getJSONObject("source").toString()))
            .put("revisions", JSONArray().also {
                it.put(JSONObject().put("priorIndex", 0).put("verdict", "REVISE")
                    .put("reason", "Updated from confirmed Host facts"))
                it.put(JSONObject().put("priorIndex", 1).put("verdict", "KEEP")
                    .put("reason", "Earlier conditional strategy remains available"))
            })
            .put("affectedOpenDecisionIds", JSONArray(listOf(
                host.getJSONObject("currentDecision").getString("decisionId"))))
            .put("currentResult", JSONObject()
                .put("decisionId", host.getJSONObject("currentDecision").getString("decisionId"))
                .put("semantic", value))
            .put("updatedStrategyMemory", host.getJSONObject("priorStrategyMemory"))
            .put("strategicDirection", "Reconsider the current decision in whole-game context.")

    @Test fun carriesActualRosterHistoryAndPlayerClaimsWithoutLegalMenu() {
        val game = session()
        game.updateStorytellerPlayerContext(2,
            StorytellerPlayerContextInputV1(claimedRoleIds = listOf(RoleId("Mayor"))))
        game.commitGlobalActionFact(ActionFactDraft.Poison(
            "confirmed-poison", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        game.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-info-received",
            phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 1,
            sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 1),
        ))
        val host = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open))
        val history = host.getJSONArray("gameLogDelta")
        assertEquals(2, history.length())
        assertEquals("poison", history.getJSONObject(0).getString("type"))
        assertEquals(2, history.getJSONObject(0).getInt("target"))
        assertEquals("N1", history.getJSONObject(0).getString("period"))
        assertEquals("information", history.getJSONObject(1).getString("type"))
        assertEquals(1, history.getJSONObject(1).getInt("value"))
        assertEquals("Chef", history.getJSONObject(1).getString("role"))
        assertFalse(host.has("causalHistory"))
        assertFalse(host.toString().contains("typedFact"))
        assertFalse(host.toString().contains("globalSequence\"") && host.toString().contains("entryId"))
        assertEquals("Mayor", host.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(1).getJSONArray("claimedRoleIds").getString(0))
        assertEquals("Empath", host.getJSONObject("currentDecision").getString("abilityRoleId"))
        assertEquals("LIVING_EVIL_NEIGHBOURS",
            host.getJSONObject("currentDecision").getJSONObject("decisionContext")
                .getString("metric"))
        assertFalse(host.has("openDecisions"))
        assertFalse(host.toString().contains("legalCandidates"))
        assertFalse(host.toString().contains("candidateId"))
        assertFalse(host.toString().contains("remainingChoices"))
        assertTrue(host.getBoolean("noUnobservedFutureFactMayBeAssumed"))
        val wire = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            host.toString(), PersonalDirectOpenAiV1.DEFAULT_MODEL))
        assertEquals(host.toString(), wire.getString("input"))
        assertFalse(wire.getBoolean("store"))
        assertTrue(wire.getString("instructions").contains("currentResult"))
        val accepted = StorytellerStrategicContinuationV2.decode(
            host.toString(), response(host).toString(), open)
        assertEquals(mapOf("current:seat-2" to "empath-one"), accepted.remainingChoices)
        assertEquals(memory, accepted.updatedMemory)
        assertEquals(game.state.nextTimelineGlobalSequence, accepted.exclusiveGlobalSequence)
    }

    @Test fun sendsOnlyDeltaAndOmitsTechnicalCarryPhaseAndDuplicateDeath() {
        val game = session()
        game.commitGlobalActionFact(ActionFactDraft.Poison(
            "confirmed-night1", StorytellerPhase.NIGHT, 1, 0, 5))
        val priorCutoff = game.state.nextTimelineGlobalSequence
        val cursor = StorytellerStrategicContinuationV2.MemoryCursor(
            game.state.gameId, game.state.gameStateRevision,
            game.state.playerInputRevision, priorCutoff, 1, memory)

        // These facts are valid Host mechanics but have no new AI decision content.
        game.commitGlobalActionFact(ActionFactDraft.Poison(
            "dawn-strategic-continuation-game-1-poison-seat-5-to-none",
            StorytellerPhase.DAWN, 1, 1, null))
        game.commitGlobalActionFact(ActionFactDraft.PhaseAdvance(
            "phase-day1", StorytellerPhase.DAWN, 1, 2, StorytellerPhase.DAY, 1))
        game.commitGlobalActionFact(ActionFactDraft.Nomination(
            "nominate", StorytellerPhase.DAY, 1, 3, 3, 7, false))
        game.commitGlobalActionFact(ActionFactDraft.Execution(
            "execute", StorytellerPhase.DAY, 1, 4, 7))
        game.commitGlobalActionFact(ActionFactDraft.Death(
            "death-from-execution", StorytellerPhase.DAY, 1, 5, 7))
        game.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "public-death", phase = StorytellerPhase.DAY, round = 1, sequence = 6,
            sourceSeat = null, sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC, recipientSeats = (1..8).toSet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(7, false)))

        val request = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open, cursor))
        val delta = request.getJSONArray("gameLogDelta")
        assertEquals(2, delta.length())
        assertEquals("nomination", delta.getJSONObject(0).getString("type"))
        assertEquals("execution", delta.getJSONObject(1).getString("type"))
        assertEquals("D1", delta.getJSONObject(1).getString("period"))
        assertFalse(request.toString().contains("confirmed-night1"))
        assertFalse(request.toString().contains("dawn-strategic"))
        assertFalse(request.toString().contains("public-death"))
        assertFalse(request.toString().contains("death-from-execution"))
        assertFalse(request.toString().contains("entryId"))
        assertFalse(request.toString().contains("localSequence"))
    }

    @Test fun latestAcceptedMemoryContinuesAcrossMultipleActualRevisions() {
        val game = session()
        game.advanceGameStateRevision()
        val initial = StorytellerStrategicContinuationV2.MemoryCursor.initial(prior())
        val host1 = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open, initial))
        val firstModel = response(host1)
        firstModel.getJSONObject("updatedStrategyMemory").getJSONArray("intentions")
            .getJSONObject(0).put("approach", "Revision informed by Host facts")
        val first = StorytellerStrategicContinuationV2.decode(
            host1.toString(), firstModel.toString(), open)
        val next = initial.advance(first)
        assertEquals(1, next.version)
        game.recordPlayerInput()
        val host2 = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open, next))
        assertEquals(1, host2.getJSONObject("source").getInt("memoryVersion"))
        val second = StorytellerStrategicContinuationV2.decode(
            host2.toString(), response(host2).toString(), open)
        assertEquals(2, next.advance(second).version)
        assertTrue(runCatching { next.advance(first) }.isFailure)
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.decode(
                host2.toString(), firstModel.toString(), open)
        }.isFailure)
    }

    @Test fun directSemanticResultMustMatchFreshHostLegalMeaning() {
        val host = JSONObject(StorytellerStrategicContinuationV2.encode(
            session().state, prior(), open))
        assertEquals(mapOf("current:seat-2" to "empath-zero"),
            StorytellerStrategicContinuationV2.decode(
                host.toString(), response(host, semantic(scalar = "0")).toString(), open)
                .remainingChoices)
        fun rejects(edit: (JSONObject) -> Unit) {
            val raw = JSONObject(response(host).toString())
            edit(raw)
            assertTrue(runCatching {
                StorytellerStrategicContinuationV2.decode(host.toString(), raw.toString(), open)
            }.isFailure)
        }
        rejects { it.getJSONObject("currentResult").getJSONObject("semantic")
            .put("scalarValue", "99") }
        rejects { it.getJSONObject("currentResult").put("decisionId", "invented") }
        rejects { it.getJSONObject("currentResult").getJSONObject("semantic")
            .put("scalarValue", "0").put("candidateId", "private-id") }
        rejects { it.getJSONObject("source").put("gameId", "another-game") }
        rejects { it.getJSONObject("source").put("exclusiveGlobalSequence", 999) }
        rejects { it.getJSONArray("affectedOpenDecisionIds").put("published-seat") }
        rejects { it.getJSONArray("revisions").remove(1) }
        rejects { it.put("remainingChoices", JSONArray()) }
        rejects { it.getJSONObject("updatedStrategyMemory").getJSONArray("intentions")
            .getJSONObject(0).put("risk", "") }
        assertTrue(runCatching { StorytellerStrategicContinuationV2.encode(
            session().state, prior(), open + open.first())
        }.isFailure)
    }

    @Test fun sameVisiblePairWithMultipleInternalWitnessIdsDoesNotLeakCandidateIds() {
        val pairs = listOf(
            StorytellerProviderCandidateV1("witness-b",
                StorytellerProviderCandidatePayloadV1.PairInformation(
                    "Baron", listOf(7, 6), com.codex.campboardgamehost.clocktower.domain.SemanticTruth.TRUE,
                    emptyList())),
            StorytellerProviderCandidateV1("witness-a",
                StorytellerProviderCandidatePayloadV1.PairInformation(
                    "Baron", listOf(6, 7), com.codex.campboardgamehost.clocktower.domain.SemanticTruth.FALSE,
                    emptyList())),
        )
        val current = listOf(StorytellerStrategicContinuationV2.OpenDecision(
            "current:pair", 5, "NIGHT_INFORMATION", pairs, "Investigator"))
        val host = JSONObject(StorytellerStrategicContinuationV2.encode(
            session().state, prior(), current))
        val proposal = response(host, semantic(kind = "PAIR_RESULT", scalar = null,
            role = "Baron", pair = listOf(7, 6)))
        val accepted = StorytellerStrategicContinuationV2.decode(
            host.toString(), proposal.toString(), current)
        assertEquals(mapOf("current:pair" to "witness-a"), accepted.remainingChoices)
        assertFalse(host.toString().contains("witness-a"))
        assertFalse(host.toString().contains("witness-b"))
    }

    @Test fun staleGameStateAndMismatchedHostDomainNeverAcceptASemanticResponse() {
        val game = session()
        val first = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open))
        game.updateGameState(game.state.gameState.copy(
            players = game.state.gameState.players.map {
                if (it.seat == 3) it.copy(alive = false) else it
            }))
        val changed = JSONObject(StorytellerStrategicContinuationV2.encode(
            game.state, prior(), open))
        assertEquals(false, changed.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(2).getBoolean("alive"))
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.decode(
                changed.toString(), response(first).toString(), open)
        }.isFailure)
        val mutatedDomain = open.map { it.copy(
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "changed-legal", StorytellerProviderCandidatePayloadV1.ScalarResult("2")))
        ) }
        assertTrue(runCatching {
            StorytellerStrategicContinuationV2.decode(
                changed.toString(), response(changed).toString(), mutatedDomain)
        }.isFailure)
    }

}
