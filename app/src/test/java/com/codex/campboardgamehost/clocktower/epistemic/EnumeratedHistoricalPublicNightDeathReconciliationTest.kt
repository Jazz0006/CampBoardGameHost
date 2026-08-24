package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalPublicNightDeathReconciliationTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val ruleset = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-h7-9-night-death-reconciliation-test",
        sourceRevision = "official",
    )
    private val script = ruleset.scriptId
    private val rolesBySeat = linkedMapOf(
        1 to RoleId("Fortune Teller"),
        2 to RoleId("Chef"),
        3 to RoleId("Empath"),
        4 to RoleId("Poisoner"),
        5 to RoleId("Imp"),
    )
    private val setupKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-h7-9-night-death",
        formalSnapshotId = "snapshot-h7-9-night-death",
        recipientSeat = 1,
        perceivedRole = RoleId("Fortune Teller"),
        setupKnowledge = listOf(
            InformationProposition.SetupProfile(3, 0, 1, 1),
        ) + rolesBySeat.map { (seat, role) -> InformationProposition.RoleAt(seat, role) },
    )

    @Test
    fun `public night death after post Imp observation filters materialized worlds instead of killing twice`() {
        val actions = ActionFactTimeline(
            listOf(
                phaseAdvance(
                    actionId = "day-1",
                    pointPhase = StorytellerPhase.FIRST_NIGHT,
                    pointRound = 1,
                    nextPhase = StorytellerPhase.DAY,
                    nextRound = 1,
                    globalSequence = 1L,
                ),
                phaseAdvance(
                    actionId = "night-2",
                    pointPhase = StorytellerPhase.DAY,
                    pointRound = 1,
                    nextPhase = StorytellerPhase.NIGHT,
                    nextRound = 2,
                    globalSequence = 2L,
                ),
                action(
                    fact = ActionFact.Death("night-two-chef-death", 11L, 2),
                    phase = StorytellerPhase.NIGHT,
                    round = 2,
                    localSequence = 11,
                    globalSequence = 11L,
                ),
            ),
        )
        val result = build(
            actions = actions,
            observations = EpistemicObservationLog(listOf(fortuneTellerObservation(10L))),
        )

        assertFalse(result.worldSet.isEmpty())
        assertEquals(StorytellerPhase.NIGHT, result.phase)
        assertEquals(2, result.round)
        assertEquals(11L, result.lastGlobalSequence)
        assertTrue(
            "PublicDeath must confirm the already-materialized hidden night outcome; it must not add a second death.",
            result.worldSet.enumeratedWorlds().all { world ->
                world.aliveSeats == setOf(1, 3, 4, 5)
            },
        )
    }

    @Test
    fun `public night death materializes hidden mechanics before reconciliation when it is first post Imp evidence`() {
        val actions = ActionFactTimeline(
            listOf(
                phaseAdvance(
                    actionId = "day-1",
                    pointPhase = StorytellerPhase.FIRST_NIGHT,
                    pointRound = 1,
                    nextPhase = StorytellerPhase.DAY,
                    nextRound = 1,
                    globalSequence = 1L,
                ),
                phaseAdvance(
                    actionId = "night-2",
                    pointPhase = StorytellerPhase.DAY,
                    pointRound = 1,
                    nextPhase = StorytellerPhase.NIGHT,
                    nextRound = 2,
                    globalSequence = 2L,
                ),
                action(
                    fact = ActionFact.Death("night-two-chef-death", 10L, 2),
                    phase = StorytellerPhase.NIGHT,
                    round = 2,
                    localSequence = 10,
                    globalSequence = 10L,
                ),
                phaseAdvance(
                    actionId = "day-2",
                    pointPhase = StorytellerPhase.NIGHT,
                    pointRound = 2,
                    nextPhase = StorytellerPhase.DAY,
                    nextRound = 2,
                    globalSequence = 11L,
                ),
            ),
        )
        val result = build(actions = actions)

        assertFalse(result.worldSet.isEmpty())
        assertEquals(StorytellerPhase.DAY, result.phase)
        assertEquals(2, result.round)
        assertEquals(11L, result.lastGlobalSequence)
        assertTrue(
            "A PublicDeath that is the first post-Imp evidence must trigger the hidden transition before filtering it.",
            result.worldSet.enumeratedWorlds().all { world ->
                world.aliveSeats == setOf(1, 3, 4, 5)
            },
        )
    }

    private fun build(
        actions: ActionFactTimeline,
        observations: EpistemicObservationLog = EpistemicObservationLog(),
    ): EnumeratedHistoricalReplayResult = EnumeratedHistoricalExactBaseline.build(
        validatedRuleset = validatedRuleset,
        rulesetRef = ruleset,
        setupKnowledge = setupKnowledge,
        hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
        roleDefinitions = rolesBySeat.values.map(::role),
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = actions,
        observationLog = observations,
    )

    private fun fortuneTellerObservation(globalSequence: Long): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = "h7-9-fortune-teller-$globalSequence",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            sourceSeat = 1,
            sourceAbility = RoleId("Fortune Teller"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 1,
                subjectSeats = listOf(4, 5),
                value = true,
            ),
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }

    private fun phaseAdvance(
        actionId: String,
        pointPhase: StorytellerPhase,
        pointRound: Int,
        nextPhase: StorytellerPhase,
        nextRound: Int,
        globalSequence: Long,
    ): TimelineBoundActionFact = action(
        fact = ActionFact.PhaseAdvance(actionId, globalSequence, nextPhase, nextRound),
        phase = pointPhase,
        round = pointRound,
        localSequence = globalSequence.toInt(),
        globalSequence = globalSequence,
    )

    private fun action(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
        globalSequence: Long,
    ) = TimelineBoundActionFact(
        fact = fact,
        point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = localSequence,
            globalSequence = globalSequence,
        ),
    )

    private fun role(roleId: RoleId): RoleDefinition {
        val type = when (roleId.value) {
            "Poisoner" -> CharacterType.MINION
            "Imp" -> CharacterType.DEMON
            else -> CharacterType.TOWNSFOLK
        }
        return RoleDefinition(
            id = roleId,
            alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
            type = type,
            scriptIds = setOf(script),
        )
    }
}
