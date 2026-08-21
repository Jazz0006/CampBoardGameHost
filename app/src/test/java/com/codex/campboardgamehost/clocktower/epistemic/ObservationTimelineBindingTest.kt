package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ObservationTimelineBindingTest {
    private val ruleset = RulesetRef(
        ScriptId("trouble-brewing"),
        "0123456789abcdef0123456789abcdef",
        "trouble-brewing-v1",
        "official-2026-08-11",
        RuleCoverage.PARTIAL,
    )
    private val formal = FormalGameState(
        snapshotId = "snapshot-r6-observation-timeline",
        gameId = "game-r6-observation-timeline",
        gameStateRevision = 3,
        rulesetRef = ruleset,
        phase = StorytellerPhase.NIGHT,
        round = 2,
        players = listOf(
            FormalPlayerState(1, RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        ),
    )

    @Test fun `global records canonicalize by global sequence across round boundaries`() {
        val globallyFirst = globalRecord(
            recordId = "night-two-first",
            point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0, globalSequence = 40),
        )
        val globallySecond = globalRecord(
            recordId = "first-night-later-global",
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 99, globalSequence = 41),
        )

        val log = EpistemicObservationLog()
            .append(globallySecond)
            .append(globallyFirst)

        assertEquals(listOf(globallyFirst, globallySecond), log.records)
    }

    @Test fun `duplicate global sequence in one global log fails closed`() {
        val first = globalRecord(
            recordId = "global-one",
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 1, 7),
        )
        val duplicate = globalRecord(
            recordId = "global-two",
            point = TimelinePoint(StorytellerPhase.DAY, 1, 0, 7),
        )

        expectIllegalArgument("duplicate global sequence") {
            EpistemicObservationLog().append(first).append(duplicate)
        }
    }

    @Test fun `legacy and global records cannot be mixed in one log`() {
        val legacy = legacyRecord("legacy-one")
        val global = globalRecord(
            recordId = "global-one",
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 1, 1),
        )

        expectIllegalArgument("mixed timeline modes") {
            EpistemicObservationLog().append(legacy).append(global)
        }
    }

    @Test fun `legacy schema v2 json without timeline binding restores explicitly as legacy local`() {
        val legacyJson = """
            {
              "phase":"FIRST_NIGHT",
              "proposition":{"alive":true,"kind":"alive-at","seat":1},
              "recipientSeats":[],
              "recordId":"legacy-json",
              "reliability":"NOT_ABILITY_INFORMATION",
              "round":1,
              "schemaVersion":2,
              "sequence":4,
              "sourceAbility":null,
              "sourceSeat":null,
              "visibility":"PUBLIC"
            }
        """.trimIndent()

        val restored = EpistemicSemanticJson.decodeRecordedEpistemicObservation(legacyJson)

        assertEquals(ObservationTimelineBinding.LegacyLocal, restored.timelineBinding)
        assertFalse(EpistemicSemanticJson.encode(restored).contains("timelineBinding"))
    }

    @Test fun `global recorded observation json round trip preserves exact timeline point`() {
        val point = TimelinePoint(StorytellerPhase.NIGHT, round = 3, sequence = 2, globalSequence = 87)
        val record = globalRecord("global-round-trip", point)

        val encoded = EpistemicSemanticJson.encode(record)
        val restored = EpistemicSemanticJson.decodeRecordedEpistemicObservation(encoded)

        assertEquals(record, restored)
        assertEquals(ObservationTimelineBinding.Global(point), restored.timelineBinding)
        assertTrue(encoded.contains("\"timelineBinding\""))
        assertTrue(encoded.contains("\"globalSequence\":87"))
    }

    @Test fun `malformed global timeline json fails closed`() {
        val encoded = JSONObject(EpistemicSemanticJson.encode(globalRecord(
            "malformed-global",
            TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 3, globalSequence = 12),
        )))
        encoded.getJSONObject("timelineBinding").getJSONObject("point").remove("globalSequence")

        expectRuntimeFailure("missing global sequence") {
            EpistemicSemanticJson.decodeRecordedEpistemicObservation(encoded.toString())
        }
    }

    @Test fun `global timeline point must match flat replay fields`() {
        val encoded = JSONObject(EpistemicSemanticJson.encode(globalRecord(
            "mismatched-global",
            TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 3, globalSequence = 12),
        )))
        encoded.put("sequence", 4)

        expectIllegalArgument("timeline mismatch") {
            EpistemicSemanticJson.decodeRecordedEpistemicObservation(encoded.toString())
        }
    }

    @Test fun `bindTo and observation json preserve global timeline identity`() {
        val point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 5, globalSequence = 33)
        val bound = globalRecord("bind-global", point).bindTo(formal)

        assertEquals(ObservationTimelineBinding.Global(point), bound.timelineBinding)
        assertEquals(bound, EpistemicSemanticJson.decodeEpistemicObservation(EpistemicSemanticJson.encode(bound)))
    }

    @Test fun `player world identity remains unchanged when only timeline migration binding changes`() {
        val point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 5, globalSequence = 33)
        val globalObservation = globalRecord("identity-observation", point).bindTo(formal)
        val legacyObservation = globalObservation.copy(timelineBinding = ObservationTimelineBinding.LegacyLocal)
        val globalKnowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-global",
            formalSnapshotId = formal.snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            publicObservations = listOf(globalObservation),
        )
        val legacyKnowledge = globalKnowledge.copy(
            knowledgeSnapshotId = "knowledge-legacy",
            publicObservations = listOf(legacyObservation),
        )

        val globalIdentity = PlayerWorldSetIdentity.create(
            ruleset,
            globalKnowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        val legacyIdentity = PlayerWorldSetIdentity.create(
            ruleset,
            legacyKnowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertEquals(globalIdentity.value, legacyIdentity.value)
    }

    private fun legacyRecord(recordId: String): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = recordId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 4,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, true),
    )

    private fun globalRecord(
        recordId: String,
        point: TimelinePoint,
    ): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = recordId,
        phase = point.phase,
        round = point.round,
        sequence = point.sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, true),
        timelineBinding = ObservationTimelineBinding.Global(point),
    )

    private fun expectIllegalArgument(label: String, block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException for $label")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed contract.
        }
    }

    private fun expectRuntimeFailure(label: String, block: () -> Unit) {
        try {
            block()
            fail("Expected decode failure for $label")
        } catch (_: RuntimeException) {
            // Expected malformed persisted input rejection.
        }
    }
}
