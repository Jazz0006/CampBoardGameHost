package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalSeedContractTest {
    private val script = ScriptId("trouble_brewing")
    private val legacyKnowledge by lazy {
        RulesetJsonLoader.parse(
            File("src/main/assets/rules/trouble_brewing.json").readText(Charsets.UTF_8),
        )
    }
    private val registry by lazy {
        LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = legacyKnowledge,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            coverage = RuleCoverage.PARTIAL,
        )
    }
    private val validatedRuleset by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/trouble_brewing.json").readText(Charsets.UTF_8),
            requestedScriptId = script,
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }
    private val ruleset by lazy {
        validatedRuleset.toRulesetRef(
            rulesetVersion = "a3-h1-historical-seed-contract",
            sourceRevision = "official",
        )
    }
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Poisoner", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )
    private val setupOnlyKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-a3-h1-seed",
        formalSnapshotId = "snapshot-a3-h1-seed",
        recipientSeat = 1,
        perceivedRole = RoleId("Empath"),
        setupKnowledge = listOf(
            InformationProposition.SetupProfile(3, 0, 1, 1),
            InformationProposition.RoleAt(4, RoleId("Poisoner")),
        ),
    )

    @Test
    fun `H1 historical seed excludes durable observations and GLOBAL replay consumes each visible observation once`() {
        val durableObservation = RecordedEpistemicObservation(
            recordId = "durable-after-execution",
            phase = StorytellerPhase.DAY,
            round = 1,
            sequence = 4,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(setupOnlyKnowledge.recipientSeat),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AllOf(
                listOf(
                    InformationProposition.AliveAt(2, false),
                    InformationProposition.RoleAt(3, RoleId("Fortune Teller")),
                ),
            ),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(
                    phase = StorytellerPhase.DAY,
                    round = 1,
                    sequence = 4,
                    globalSequence = 21L,
                ),
            ),
        )
        val fullHistoricalKnowledge = setupOnlyKnowledge.copy(
            privateObservations = listOf(durableObservation.bindTo(setupOnlyKnowledge.formalSnapshotId)),
        )
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.Poison("hidden-poison", 10L, 5),
                    StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 10L,
                ),
                action(
                    ActionFact.PhaseAdvance("day-1", 15L, StorytellerPhase.DAY, 1),
                    StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 15L,
                ),
                action(
                    ActionFact.Execution("execution-seat-2", 20L, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 3,
                    globalSequence = 20L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 30L, StorytellerPhase.NIGHT, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 5,
                    globalSequence = 30L,
                ),
            ),
        )
        val observationLog = EpistemicObservationLog(listOf(durableObservation))

        val projectedEvents = PlayerHistoricalTimeline.project(
            recipientSeat = setupOnlyKnowledge.recipientSeat,
            actionTimeline = actions,
            observationLog = observationLog,
        )
        assertEquals(
            1,
            projectedEvents.filterIsInstance<PlayerHistoricalEvent.Observation>()
                .count { it.record.recordId == durableObservation.recordId },
        )
        assertTrue(projectedEvents.none { it.point.globalSequence == 10L })

        val setupOnlyReference = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupOnlyKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = observationLog,
        )
        val result = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = fullHistoricalKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = observationLog,
        )

        assertFalse(result.worldSet.isEmpty())
        assertEquals(
            setupOnlyReference.worldSet.enumeratedWorlds().toSet(),
            result.worldSet.enumeratedWorlds().toSet(),
        )
        assertTrue(result.worldSet.enumeratedWorlds().all { world ->
            world.rolesBySeat[4] == RoleId("Poisoner")
        })
        assertTrue(result.worldSet.enumeratedWorlds().all { world ->
            world.rolesBySeat[3] == RoleId("Fortune Teller") && 2 !in world.aliveSeats
        })
        assertEquals(
            setOf(1, 2, 3, 4, 5),
            result.worldSet.enumeratedWorlds().flatMap { world ->
                world.abilityStatesBySeat.filterValues { it == AbilityState.MALFUNCTIONING_POISONED }.keys
            }.toSet(),
        )
        assertEquals(30L, result.lastGlobalSequence)
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

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
