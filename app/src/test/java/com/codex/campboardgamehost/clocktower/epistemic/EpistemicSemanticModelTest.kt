package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EpistemicSemanticModelTest {
    private val ruleset = RulesetRef(
        ScriptId("trouble-brewing"), "0123456789abcdef0123456789abcdef",
        "trouble-brewing-v1", "official-2026-08-11", RuleCoverage.PARTIAL,
    )
    private val snapshotId = "snapshot-0123456789abcdef0123456789abcdef"
    private val state = FormalGameState(
        snapshotId, "game-1", 4, ruleset, StorytellerPhase.FIRST_NIGHT, 1,
        listOf(
            FormalPlayerState(1, RoleId("Investigator"), Alignment.GOOD, CharacterType.TOWNSFOLK),
            FormalPlayerState(2, RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
            FormalPlayerState(3, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
            FormalPlayerState(4, RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER),
            FormalPlayerState(5, RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        ),
    )
    private val pair = InformationProposition.AnyOf(
        listOf(InformationProposition.RoleAt(2, RoleId("Spy")), InformationProposition.RoleAt(5, RoleId("Spy"))),
    )
    private val observation = EpistemicObservation(
        "observation-investigator-1", snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 3,
        1, RoleId("Investigator"), ObservationVisibility.PRIVATE, setOf(1),
        ObservationReliability.RECEIVED_AS_FUNCTIONING, pair,
    )
    private val knowledge = PlayerKnowledgeSnapshot(
        "knowledge-seat-1", snapshotId, 1, RoleId("Investigator"),
        privateObservations = listOf(observation),
        setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
    )

    @Test fun `schema v2 roots and A1_1 values round trip canonically`() {
        val query = RegistrationQuery(
            4, "ft-night-1-seat-4", TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 8, 8), RoleId("Fortune Teller"),
            RegistrationQuestion.DEMON, RoleId("Imp"), CharacterType.DEMON, Alignment.EVIL,
        )
        val profile = RegistrationProfile(RoleId("Imp"), CharacterType.DEMON, Alignment.EVIL, RegistrationBasis.RECLUSE_ABILITY)
        val cardinalities = listOf(
            WorldCardinality.Exact(BigInteger("9223372036854775808123")),
            WorldCardinality.AtLeast(BigInteger("9999999999999999999999")),
        )
        val identity = PlayerWorldSetIdentity.create(ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE)
        val decision = StorytellerDecisionPoint(
            "decision-investigator-1", snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 3, 1,
            RoleId("Investigator"), "pair-information", setOf(1), candidateFamilyId = CandidateFamilyId("first-night-pair-info"),
        )
        val choiceSet = LegalChoiceSet(
            "choice-set-investigator-1", decision.decisionPointId, ruleset,
            listOf(LegalEpistemicChoice("choice-natural-spy", "investigator-night-1", observation)),
        )
        val durableRecord = RecordedEpistemicObservation(
            recordId = "first-night-investigator", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 3, sourceSeat = 1, sourceAbility = RoleId("Investigator"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING, proposition = pair,
        )

        assertEquals(state, EpistemicSemanticJson.decodeFormalGameState(EpistemicSemanticJson.encode(state)))
        assertEquals(pair, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(pair)))
        val playerCount = InformationProposition.PlayerCount(5)
        assertEquals(playerCount, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(playerCount)))
        val booleanResult = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 4), true,
        )
        assertEquals(booleanResult, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(booleanResult)))
        assertEquals(observation, EpistemicSemanticJson.decodeEpistemicObservation(EpistemicSemanticJson.encode(observation)))
        assertEquals(durableRecord, EpistemicSemanticJson.decodeRecordedEpistemicObservation(EpistemicSemanticJson.encode(durableRecord)))
        assertEquals(knowledge, EpistemicSemanticJson.decodePlayerKnowledgeSnapshot(EpistemicSemanticJson.encode(knowledge)))
        assertEquals(decision, EpistemicSemanticJson.decodeStorytellerDecisionPoint(EpistemicSemanticJson.encode(decision)))
        assertEquals(choiceSet, EpistemicSemanticJson.decodeLegalChoiceSet(EpistemicSemanticJson.encode(choiceSet)))
        assertEquals(query, EpistemicSemanticJson.decodeRegistrationQuery(EpistemicSemanticJson.encode(query)))
        assertEquals(profile, EpistemicSemanticJson.decodeRegistrationProfile(EpistemicSemanticJson.encode(profile)))
        cardinalities.forEach { assertEquals(it, EpistemicSemanticJson.decodeWorldCardinality(EpistemicSemanticJson.encode(it))) }
        assertEquals(identity, EpistemicSemanticJson.decodePlayerWorldSetIdentity(EpistemicSemanticJson.encode(identity)))
        assertTrue(EpistemicSemanticJson.encode(state).contains("\"schemaVersion\":2"))
    }

    @Test fun `schema v1 is rejected with an explicit migration error`() {
        val v1 = EpistemicSemanticJson.encode(knowledge).replaceFirst("\"schemaVersion\":2", "\"schemaVersion\":1")
        try {
            EpistemicSemanticJson.decodePlayerKnowledgeSnapshot(v1)
            fail("schema v1 must not be silently decoded")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("explicitly migrated"))
        }
    }

    @Test fun `registration capability is interaction scoped and selected fact stays bound to choice`() {
        val query = RegistrationQuery(
            4, "ft-night-1-seat-4", TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 8, 8), RoleId("Fortune Teller"),
            RegistrationQuestion.DEMON, RoleId("Imp"), CharacterType.DEMON, Alignment.EVIL,
        )
        val profiles = TroubleBrewingRegistrationSemantics.possibleRegistrations(state, query)
        assertTrue(profiles.any { it.basis == RegistrationBasis.ACTUAL_STATE && it.role == RoleId("Recluse") })
        assertTrue(profiles.any { it.basis == RegistrationBasis.RECLUSE_ABILITY && it.role == RoleId("Imp") })

        val selected = RegistrationFact(
            query.interactionId, 4, RoleId("Imp"), CharacterType.DEMON, Alignment.EVIL,
            RegistrationQuestion.DEMON, RegistrationReason.RECLUSE_ABILITY,
        )
        assertTrue(TroubleBrewingRegistrationSemantics.isLegalSelection(state, query, selected))
        assertFalse(TroubleBrewingRegistrationSemantics.isLegalSelection(state, query.copy(interactionId = "other-interaction"), selected))
        val choiceSet = LegalChoiceSet(
            "choice-set-ft-1", "decision-ft-1", ruleset,
            listOf(LegalEpistemicChoice("choice-ft-yes", query.interactionId, observation, listOf(selected))),
        )
        val decoded = EpistemicSemanticJson.decodeLegalChoiceSet(EpistemicSemanticJson.encode(choiceSet))
        assertEquals(query.interactionId, decoded.choices.single().registrations.single().interactionId)

        val otherInteraction = query.copy(
            interactionId = "ft-night-2-seat-4",
            timelinePoint = TimelinePoint(StorytellerPhase.NIGHT, 2, 4, 104),
        )
        assertNotEquals(EpistemicSemanticJson.encode(query), EpistemicSemanticJson.encode(otherInteraction))
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

        val query = RegistrationQuery(
            4,
            "ft-night-2-seat-4",
            later,
            RoleId("Fortune Teller"),
            RegistrationQuestion.DEMON,
            RoleId("Imp"),
            CharacterType.DEMON,
            Alignment.EVIL,
        )
        val moved = query.copy(timelinePoint = later.copy(globalSequence = 42))

        assertEquals(query, EpistemicSemanticJson.decodeRegistrationQuery(EpistemicSemanticJson.encode(query)))
        assertNotEquals(EpistemicSemanticJson.encode(query), EpistemicSemanticJson.encode(moved))
    }

    @Test fun `red herring eligibility uses actual alignment not registration`() {
        assertFalse(2 in state.eligibleRedHerringSeats())
        assertTrue(4 in state.eligibleRedHerringSeats())
    }

    @Test fun `player world identity excludes formal secret ids but includes visible knowledge and hypothesis`() {
        val first = PlayerWorldSetIdentity.create(ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE)
        val secretOnlyChange = knowledge.copy(
            knowledgeSnapshotId = "caller-id-changed",
            formalSnapshotId = "snapshot-secret-changed",
            privateObservations = listOf(observation.copy(
                snapshotId = "snapshot-secret-changed",
                observationId = "caller-observation-id-changed",
                sequence = 99,
                recipientSeats = setOf(1, 5),
            )),
        )
        val same = PlayerWorldSetIdentity.create(ruleset, secretOnlyChange, EpistemicHypothesis.MECHANICALLY_CREDIBLE)
        val otherMode = PlayerWorldSetIdentity.create(ruleset, knowledge, EpistemicHypothesis.FUNCTIONING_ONLY)
        val visibleChange = knowledge.copy(setupKnowledge = listOf(InformationProposition.SetupProfile(2, 1, 1, 1)))
        val otherKnowledge = PlayerWorldSetIdentity.create(ruleset, visibleChange, EpistemicHypothesis.MECHANICALLY_CREDIBLE)

        assertEquals(first.value, same.value)
        assertNotEquals(first.value, otherMode.value)
        assertNotEquals(first.value, otherKnowledge.value)
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

    @Test fun `candidate and explanation taxonomies cannot be interchanged`() {
        val family = CandidateFamilyId("registration-dependent-info")
        val cluster = WorldExplanationClusterId("recluse-registration")
        assertNotEquals(family.value, cluster.value)
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
    fun `knowledge rejects private observation addressed to another player`() {
        knowledge.copy(recipientSeat = 2)
    }
}
