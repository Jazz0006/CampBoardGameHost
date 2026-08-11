package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpistemicSemanticModelTest {
    private val ruleset = RulesetRef(
        scriptId = ScriptId("trouble-brewing"),
        scriptContentHash = "0123456789abcdef0123456789abcdef",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-2026-08-11",
        coverage = RuleCoverage.PARTIAL,
    )
    private val snapshotId = "snapshot-0123456789abcdef0123456789abcdef"
    private val pairProposition = InformationProposition.AnyOf(
        listOf(
            InformationProposition.RoleAt(2, RoleId("Spy")),
            InformationProposition.RoleAt(5, RoleId("Spy")),
        ),
    )
    private val privateObservation = EpistemicObservation(
        observationId = "observation-investigator-1",
        snapshotId = snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 3,
        sourceSeat = 1,
        sourceAbility = RoleId("Investigator"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = pairProposition,
    )

    @Test
    fun `all A1 semantic roots have stable round trip serialization`() {
        val formalState = FormalGameState(
            snapshotId = snapshotId,
            gameId = "game-1",
            gameStateRevision = 4,
            rulesetRef = ruleset,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            players = listOf(
                FormalPlayerState(1, RoleId("Investigator"), Alignment.GOOD, CharacterType.TOWNSFOLK),
                FormalPlayerState(2, RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
                FormalPlayerState(3, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
                FormalPlayerState(4, RoleId("Drunk"), Alignment.GOOD, CharacterType.OUTSIDER, RoleId("Chef")),
                FormalPlayerState(5, RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK),
            ),
            publicPropositions = listOf(InformationProposition.AliveAt(1, true)),
            storytellerOnlyPropositions = listOf(
                InformationProposition.AbilityStateAt(
                    seat = 4,
                    abilityRole = RoleId("Chef"),
                    abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                ),
            ),
        )
        val decisionPoint = StorytellerDecisionPoint(
            decisionPointId = "decision-investigator-1",
            snapshotId = snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 3,
            sourceSeat = 1,
            sourceAbility = RoleId("Investigator"),
            decisionTypeId = "pair-information",
            recipientSeats = setOf(1),
        )
        val choiceSet = LegalChoiceSet(
            choiceSetId = "choice-set-investigator-1",
            decisionPointId = decisionPoint.decisionPointId,
            rulesetRef = ruleset,
            choices = listOf(
                LegalEpistemicChoice(
                    choiceId = "choice-natural-spy",
                    observation = privateObservation,
                    registrations = listOf(
                        RegistrationFact(
                            interactionId = "investigator-spy-registration",
                            subjectSeat = 2,
                            registeredRole = RoleId("Spy"),
                            registrationQuestion = RegistrationQuestion.SPECIFIC_MINION,
                            reason = RegistrationReason.SPY_ABILITY,
                        ),
                    ),
                ),
            ),
        )
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-seat-1",
            formalSnapshotId = snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Investigator"),
            privateObservations = listOf(privateObservation),
            setupKnowledge = listOf(
                InformationProposition.SetupProfile(3, 0, 1, 1),
            ),
        )

        assertEquals(formalState, EpistemicSemanticJson.decodeFormalGameState(EpistemicSemanticJson.encode(formalState)))
        assertEquals(pairProposition, EpistemicSemanticJson.decodeInformationProposition(EpistemicSemanticJson.encode(pairProposition)))
        assertEquals(privateObservation, EpistemicSemanticJson.decodeEpistemicObservation(EpistemicSemanticJson.encode(privateObservation)))
        assertEquals(decisionPoint, EpistemicSemanticJson.decodeStorytellerDecisionPoint(EpistemicSemanticJson.encode(decisionPoint)))
        assertEquals(choiceSet, EpistemicSemanticJson.decodeLegalChoiceSet(EpistemicSemanticJson.encode(choiceSet)))
        assertEquals(knowledge, EpistemicSemanticJson.decodePlayerKnowledgeSnapshot(EpistemicSemanticJson.encode(knowledge)))
    }

    @Test
    fun `canonical JSON is independent of set insertion order and contains no UI text`() {
        val first = privateObservation.copy(recipientSeats = linkedSetOf(3, 1, 2))
        val second = privateObservation.copy(recipientSeats = linkedSetOf(2, 3, 1))

        assertEquals(EpistemicSemanticJson.encode(first), EpistemicSemanticJson.encode(second))
        assertEquals(
            "{\"kind\":\"role-at\",\"role\":\"Imp\",\"seat\":7}",
            EpistemicSemanticJson.encode(InformationProposition.RoleAt(7, RoleId("Imp"))),
        )
    }

    @Test
    fun `stable IDs reproduce and change with semantic payload`() {
        val first = SemanticStableId.create("observation", "seat=1|role=Investigator|pair=2,5")
        val replay = SemanticStableId.create("observation", "seat=1|role=Investigator|pair=2,5")
        val changed = SemanticStableId.create("observation", "seat=1|role=Investigator|pair=2,6")

        assertEquals(first, replay)
        assertNotEquals(first, changed)
        assertTrue(first.matches(Regex("observation-[0-9a-f]{32}")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `knowledge snapshot rejects private facts addressed to another player`() {
        PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-seat-2",
            formalSnapshotId = snapshotId,
            recipientSeat = 2,
            perceivedRole = RoleId("Empath"),
            privateObservations = listOf(privateObservation),
        )
    }
}
