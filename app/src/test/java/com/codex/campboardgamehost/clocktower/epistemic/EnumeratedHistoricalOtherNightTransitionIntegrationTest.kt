package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.BooleanMetric
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalOtherNightTransitionIntegrationTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val ruleset = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-h7-8-other-night-transition-test",
        sourceRevision = "official",
    )
    private val script = ruleset.scriptId

    @Test
    fun `post Imp Fortune Teller observation is evaluated after rule derived other-night mechanics`() {
        val rolesBySeat = linkedMapOf(
            1 to RoleId("Fortune Teller"),
            2 to RoleId("Chef"),
            3 to RoleId("Empath"),
            4 to RoleId("Poisoner"),
            5 to RoleId("Imp"),
        )
        val result = build(
            rolesBySeat = rolesBySeat,
            observation = observation(
                sourceSeat = 1,
                sourceAbility = RoleId("Fortune Teller"),
                proposition = InformationProposition.BooleanResult(
                    metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                    sourceSeat = 1,
                    subjectSeats = listOf(4, 5),
                    value = true,
                ),
            ),
        )

        assertFalse(result.worldSet.isEmpty())
        assertTrue(
            "A Fortune Teller who receives their normal post-Imp observation must still be alive after the hidden Demon step.",
            result.worldSet.enumeratedWorlds().all { world -> 1 in world.aliveSeats },
        )
        assertTrue(
            "The replay must already contain rule-derived Demon outcomes at the Fortune Teller slot; it cannot defer all hidden mechanics to dawn.",
            result.worldSet.enumeratedWorlds().any { world -> world.aliveSeats.size < rolesBySeat.size },
        )
        assertTrue(
            "Rule-derived hidden transitions must not invent a durable GLOBAL timeline identity.",
            result.lastGlobalSequence == 10L,
        )
    }

    @Test
    fun `Ravenkeeper observation survives only worlds where the Imp step killed the Ravenkeeper that night`() {
        val rolesBySeat = linkedMapOf(
            1 to RoleId("Ravenkeeper"),
            2 to RoleId("Chef"),
            3 to RoleId("Fortune Teller"),
            4 to RoleId("Poisoner"),
            5 to RoleId("Imp"),
        )
        val result = build(
            rolesBySeat = rolesBySeat,
            observation = observation(
                sourceSeat = 1,
                sourceAbility = RoleId("Ravenkeeper"),
                proposition = InformationProposition.RoleAt(2, RoleId("Chef")),
            ),
        )

        assertFalse(result.worldSet.isEmpty())
        assertTrue(
            "Ravenkeeper is a death-triggered exception, but incremental replay must still prove the source actually died at this night's Demon step.",
            result.worldSet.enumeratedWorlds().all { world -> 1 !in world.aliveSeats },
        )
    }

    private fun build(
        rolesBySeat: LinkedHashMap<Int, RoleId>,
        observation: RecordedEpistemicObservation,
    ): EnumeratedHistoricalReplayResult {
        val setupKnowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-h7-8-${rolesBySeat.getValue(1).value.lowercase()}",
            formalSnapshotId = "snapshot-h7-8-${rolesBySeat.getValue(1).value.lowercase()}",
            recipientSeat = 1,
            perceivedRole = rolesBySeat.getValue(1),
            setupKnowledge = listOf(
                InformationProposition.SetupProfile(3, 0, 1, 1),
            ) + rolesBySeat.map { (seat, role) -> InformationProposition.RoleAt(seat, role) },
        )
        val actions = ActionFactTimeline(
            listOf(
                action(
                    fact = ActionFact.PhaseAdvance("day-1", 1L, StorytellerPhase.DAY, 1),
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 1L,
                ),
                action(
                    fact = ActionFact.PhaseAdvance("night-2", 2L, StorytellerPhase.NIGHT, 2),
                    phase = StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 2L,
                ),
            ),
        )

        return EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = rolesBySeat.values.map { roleId -> role(roleId) },
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(listOf(observation)),
        )
    }

    private fun observation(
        sourceSeat: Int,
        sourceAbility: RoleId,
        proposition: InformationProposition,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 10,
            globalSequence = 10L,
        )
        return RecordedEpistemicObservation(
            recordId = "h7-8-${sourceAbility.value.lowercase()}",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 10,
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }

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
