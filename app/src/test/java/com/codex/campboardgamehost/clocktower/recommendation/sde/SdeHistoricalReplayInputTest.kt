package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SdeHistoricalReplayInputTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { path ->
        File("src/main/assets/$path").readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = ruleset.toRulesetRef("c1-replay-input-test", "official")

    @Test
    fun `fresh authorities export a versioned durable input that restores equivalently`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val setup = setup(fixture.gameState.players.map { it.actualRole to (it.shownRole ?: it.actualRole) })
        val action = TimelineBoundActionFact(
            ActionFact.Poison("poison-2", 0, 2),
            TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 0, 0),
        )
        val observation = EpistemicObservationDraft(
            recordId = "empath-1",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS, 1, listOf(2, fixture.gameState.players.size), 1,
            ),
        ).bindGlobal(TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 1, 1))
        val snapshot = fixture.copy(
            gameStateRevision = 4,
            playerInputRevision = 7,
            actionTimeline = ActionFactTimeline(listOf(action)),
            epistemicObservationLog = EpistemicObservationLog(listOf(observation)),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            nextTimelineGlobalSequence = 2,
        )

        val fresh = SdeHistoricalReplayInputFactory.captureFresh(setup, snapshot)
        val raw = SdeHistoricalReplayInputJsonCodec.encode(fresh.input)
        val restored = SdeHistoricalReplayInputJsonCodec.decodeStrict(raw)

        assertEquals(SdeHistoricalReplayInputOrigin.FRESH_AUTHORITIES, fresh.origin)
        assertEquals(SdeHistoricalReplayInputOrigin.DURABLE_EXPORT, restored.origin)
        assertEquals(fresh.input, restored.input)
        assertEquals(raw, SdeHistoricalReplayInputJsonCodec.encode(restored.input))
        assertEquals(setup, restored.input.toCommittedSetup())
        val rebuilt = restored.input.toGameSnapshot(TroubleBrewingFixtures.fullRoleDefinitions())
        assertEquals(snapshot.gameId, rebuilt.gameId)
        assertEquals(snapshot.rulesetRef, rebuilt.rulesetRef)
        assertEquals(snapshot.gameSeed, rebuilt.gameSeed)
        assertEquals(snapshot.gameStateRevision, rebuilt.gameStateRevision)
        assertEquals(snapshot.playerInputRevision, rebuilt.playerInputRevision)
        assertEquals(snapshot.actionTimeline, rebuilt.actionTimeline)
        assertEquals(snapshot.epistemicObservationLog, rebuilt.epistemicObservationLog)
        assertEquals(snapshot.nextTimelineGlobalSequence, rebuilt.nextTimelineGlobalSequence)
        assertEquals(snapshot.gameState.players.map { it.name }, rebuilt.gameState.players.map { it.name })

        val callerOwnedNames = restored.input.playerNamesBySeat.toMutableList()
        val immutableInput = SdeHistoricalReplayInput(
            gameId = restored.input.gameId,
            gameStateRevision = restored.input.gameStateRevision,
            playerInputRevision = restored.input.playerInputRevision,
            rulesetRef = restored.input.rulesetRef,
            committedSetup = restored.input.committedSetup,
            playerNamesBySeat = callerOwnedNames,
            actionTimeline = restored.input.actionTimeline,
            observationLog = restored.input.observationLog,
            nextTimelineGlobalSequence = restored.input.nextTimelineGlobalSequence,
        )
        callerOwnedNames[0] = "mutated outside"
        assertEquals(restored.input.playerNamesBySeat, immutableInput.playerNamesBySeat)
    }

    @Test
    fun `capture rejects missing or inconsistent canonical replay authorities`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val setup = setup(fixture.gameState.players.map { it.actualRole to (it.shownRole ?: it.actualRole) })

        assertThrows(IllegalArgumentException::class.java) {
            SdeHistoricalReplayInputFactory.captureFresh(setup, fixture)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SdeHistoricalReplayInputFactory.captureFresh(
                setup(setup.assignments.map { it.actualRole to it.shownRole }, seed = fixture.gameSeed + 1),
                fixture.copy(semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1),
            )
        }
    }

    @Test
    fun `strict restore rejects malformed nested action observation and proposition material`() {
        val raw = nestedReplayRaw()

        val invalidPayloads = listOf(
            JSONObject(raw.toString()).apply {
                getJSONArray("actionTimeline").getJSONObject(0).getJSONObject("fact")
                    .put("unexpected", true)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("actionTimeline").getJSONObject(0).getJSONObject("point")
                    .put("round", 1.5)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("actionTimeline").getJSONObject(0).getJSONObject("fact")
                    .remove("actionId")
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("observations").getJSONObject(0).put("unexpected", true)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("observations").getJSONObject(0).put("sourceAbility", 7)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("observations").getJSONObject(0).getJSONObject("proposition")
                    .put("unexpected", true)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("observations").getJSONObject(0).getJSONObject("proposition")
                    .put("value", 0.5)
            },
            JSONObject(raw.toString()).apply {
                getJSONArray("observations").getJSONObject(0).getJSONObject("proposition")
                    .put("metric", true)
            },
        )

        invalidPayloads.forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) {
                SdeHistoricalReplayInputJsonCodec.decodeStrict(invalid.toString())
            }
        }
    }

    @Test
    fun `strict restore rejects missing unsupported and unknown payload shape`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(
            rulesetRef = rulesetRef,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val setup = setup(fixture.gameState.players.map { it.actualRole to (it.shownRole ?: it.actualRole) })
        val raw = JSONObject(SdeHistoricalReplayInputJsonCodec.encode(
            SdeHistoricalReplayInputFactory.captureFresh(setup, fixture).input,
        ))

        listOf(
            JSONObject(raw.toString()).apply { put("schemaVersion", 999) },
            JSONObject(raw.toString()).apply { remove("committedSetup") },
            JSONObject(raw.toString()).apply { put("unexpected", true) },
        ).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) {
                SdeHistoricalReplayInputJsonCodec.decodeStrict(invalid.toString())
            }
        }
    }

    private fun nestedReplayRaw(): JSONObject {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val setup = setup(fixture.gameState.players.map { it.actualRole to (it.shownRole ?: it.actualRole) })
        val action = TimelineBoundActionFact(
            ActionFact.Poison("strict-poison", 0, 2),
            TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 0, 0),
        )
        val observation = EpistemicObservationDraft(
            recordId = "strict-empath",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS,
                1,
                listOf(2, fixture.gameState.players.size),
                1,
            ),
        ).bindGlobal(TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 1, 1))
        val snapshot = fixture.copy(
            actionTimeline = ActionFactTimeline(listOf(action)),
            epistemicObservationLog = EpistemicObservationLog(listOf(observation)),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            nextTimelineGlobalSequence = 2,
        )
        return JSONObject(
            SdeHistoricalReplayInputJsonCodec.encode(
                SdeHistoricalReplayInputFactory.captureFresh(setup, snapshot).input,
            ),
        )
    }

    private fun setup(
        roles: List<Pair<RoleId, RoleId>>,
        seed: Long = A4RuntimeFixtures.snapshot().gameSeed,
    ) = CommittedClocktowerSetup(
        script = rulesetRef.scriptId,
        setupSeed = seed,
        assignments = roles.mapIndexed { index, (actual, shown) ->
            CommittedSetupSeat(index + 1, actual, shown)
        },
        provenance = SetupProvenance(SetupSourceKind.GENERATED, "c1-test"),
    )
}
