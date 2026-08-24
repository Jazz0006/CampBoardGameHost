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

class EnumeratedHistoricalNoPublicDeathDawnReconciliationTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val ruleset = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-h7-10-no-public-death-dawn-test",
        sourceRevision = "official",
    )
    private val script = ruleset.scriptId
    private val rolesBySeat = linkedMapOf(
        1 to RoleId("Fortune Teller"),
        2 to RoleId("Chef"),
        3 to RoleId("Soldier"),
        4 to RoleId("Scarlet Woman"),
        5 to RoleId("Imp"),
    )
    private val setupKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-h7-10-no-public-death",
        formalSnapshotId = "snapshot-h7-10-no-public-death",
        recipientSeat = 1,
        perceivedRole = RoleId("Fortune Teller"),
        setupKnowledge = listOf(
            InformationProposition.SetupProfile(3, 0, 1, 1),
        ) + rolesBySeat.map { (seat, role) -> InformationProposition.RoleAt(seat, role) },
    )

    @Test
    fun `night to day with no public death retains only no death hidden outcomes`() {
        val result = build(
            dawnGlobalSequence = 3L,
            observations = EpistemicObservationLog(),
        )

        assertFalse(result.worldSet.isEmpty())
        assertEquals(StorytellerPhase.DAY, result.phase)
        assertEquals(2, result.round)
        assertEquals(3L, result.lastGlobalSequence)
        assertTrue(
            "A completed night with no durable PublicDeath is the public no-death result; death branches must not survive dawn.",
            result.worldSet.enumeratedWorlds().all { world ->
                world.aliveSeats == rolesBySeat.keys
            },
        )
    }

    @Test
    fun `dawn no public death reconciles worlds already materialized by post Imp observation`() {
        val result = build(
            dawnGlobalSequence = 4L,
            observations = EpistemicObservationLog(listOf(fortuneTellerObservation(3L))),
        )

        assertFalse(result.worldSet.isEmpty())
        assertEquals(StorytellerPhase.DAY, result.phase)
        assertEquals(2, result.round)
        assertEquals(4L, result.lastGlobalSequence)
        assertTrue(
            "Post-Imp observations may materialize the Demon step early, but absence of PublicDeath by dawn must still select only no-death successor states.",
            result.worldSet.enumeratedWorlds().all { world ->
                world.aliveSeats == rolesBySeat.keys
            },
        )
    }

    private fun build(
        dawnGlobalSequence: Long,
        observations: EpistemicObservationLog,
    ): EnumeratedHistoricalReplayResult {
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
                phaseAdvance(
                    actionId = "day-2",
                    pointPhase = StorytellerPhase.NIGHT,
                    pointRound = 2,
                    nextPhase = StorytellerPhase.DAY,
                    nextRound = 2,
                    globalSequence = dawnGlobalSequence,
                ),
            ),
        )
        return EnumeratedHistoricalExactBaseline.build(
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
    }

    private fun fortuneTellerObservation(globalSequence: Long): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = "h7-10-fortune-teller-$globalSequence",
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
    ): TimelineBoundActionFact = TimelineBoundActionFact(
        fact = ActionFact.PhaseAdvance(actionId, globalSequence, nextPhase, nextRound),
        point = TimelinePoint(
            phase = pointPhase,
            round = pointRound,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        ),
    )

    private fun role(roleId: RoleId): RoleDefinition {
        val type = when (roleId.value) {
            "Scarlet Woman" -> CharacterType.MINION
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
