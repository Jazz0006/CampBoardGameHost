package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EpistemicSemanticModelTest {
    private val snapshotId = "snapshot-0123456789abcdef0123456789abcdef"
    private val pair = InformationProposition.AnyOf(
        listOf(InformationProposition.RoleAt(2, RoleId("Spy")), InformationProposition.RoleAt(5, RoleId("Spy"))),
    )
    private val observation = EpistemicObservation(
        "observation-investigator-1", snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 3,
        1, RoleId("Investigator"), ObservationVisibility.PRIVATE, setOf(1),
        ObservationReliability.RECEIVED_AS_FUNCTIONING, pair,
    )
    @Test fun `schema v2 roots and A1_1 values round trip canonically`() {
        val durableRecord = RecordedEpistemicObservation(
            recordId = "first-night-investigator", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 3, sourceSeat = 1, sourceAbility = RoleId("Investigator"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING, proposition = pair,
        )

        assertEquals(pair, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(pair)))
        val playerCount = InformationProposition.PlayerCount(5)
        assertEquals(playerCount, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(playerCount)))
        val booleanResult = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 4), true,
        )
        assertEquals(booleanResult, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(booleanResult)))
        assertEquals(observation, EpistemicSemanticJson.decodeEpistemicObservation(EpistemicSemanticJson.encode(observation)))
        assertEquals(durableRecord, EpistemicSemanticJson.decodeRecordedEpistemicObservation(EpistemicSemanticJson.encode(durableRecord)))
        assertTrue(EpistemicSemanticJson.encode(durableRecord).contains("\"schemaVersion\":2"))
    }

    @Test fun `schema v1 is rejected with an explicit migration error`() {
        val v1 = EpistemicSemanticJson.encode(observation).replaceFirst("\"schemaVersion\":2", "\"schemaVersion\":1")
        try {
            EpistemicSemanticJson.decodeEpistemicObservation(v1)
            fail("schema v1 must not be silently decoded")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("explicitly migrated"))
        }
    }

    @Test fun `timeline identity and ordering use the global monotonic sequence`() {
        val earlier = TimelinePoint(
            phase = StorytellerPhase.DAY,
            round = 2,
            sequence = 99,
            globalSequence = 40,
        )
        val later = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 0,
            globalSequence = 41,
        )

        assertEquals(listOf(earlier, later), listOf(later, earlier).sorted())

    }

    @Test fun `spy grimoire is a private timed observation and has no bluff field`() {
        val grimoire = InformationProposition.GrimoireState(
            listOf(
                GrimoireSeatView(1, RoleId("Investigator"), true),
                GrimoireSeatView(2, RoleId("Spy"), true),
            ),
        )
        val spyView = EpistemicObservation(
            "observation-spy-grimoire-night-1", snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 12,
            2, RoleId("Spy"), ObservationVisibility.PRIVATE, setOf(2),
            ObservationReliability.NOT_ABILITY_INFORMATION, grimoire,
        )
        val json = EpistemicSemanticJson.encode(spyView)
        assertEquals(spyView, EpistemicSemanticJson.decodeEpistemicObservation(json))
        assertFalse(json.contains("bluff", ignoreCase = true))
    }

    @Test fun `canonical sets and stable ids remain deterministic`() {
        val first = observation.copy(recipientSeats = linkedSetOf(3, 1, 2))
        val second = observation.copy(recipientSeats = linkedSetOf(2, 3, 1))
        assertEquals(EpistemicSemanticJson.encode(first), EpistemicSemanticJson.encode(second))

        val id = SemanticStableId.create("observation", "seat=1|pair=2,5")
        assertEquals(id, SemanticStableId.create("observation", "seat=1|pair=2,5"))
        assertNotEquals(id, SemanticStableId.create("observation", "seat=1|pair=2,6"))
        assertTrue(id.matches(Regex("observation-[0-9a-f]{32}")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `private observation rejects missing recipient`() {
        observation.copy(recipientSeats = emptySet())
    }
}
