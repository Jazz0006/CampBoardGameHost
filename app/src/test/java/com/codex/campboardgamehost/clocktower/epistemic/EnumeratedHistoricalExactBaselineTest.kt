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
import org.junit.Assert.fail
import org.junit.Test

class EnumeratedHistoricalExactBaselineTest {
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
            rulesetVersion = "a3-historical-constructor-test",
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
    private val setupKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-a3-historical-constructor",
        formalSnapshotId = "snapshot-a3-historical-constructor",
        recipientSeat = 1,
        perceivedRole = RoleId("Empath"),
        setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
    )

    @Test
    fun `exact baseline constructs setup worlds then replays knowledge safe global history`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.Poison("hidden-poison", 10L, 4),
                    StorytellerPhase.NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 10L,
                ),
                action(
                    ActionFact.PhaseAdvance("day-1", 15L, StorytellerPhase.DAY, 1),
                    StorytellerPhase.NIGHT,
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
                    localSequence = 4,
                    globalSequence = 30L,
                ),
            ),
        )

        val result = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(),
        )

        assertFalse(result.worldSet.isEmpty())
        assertTrue(result.worldSet.enumeratedWorlds().all { 2 !in it.aliveSeats })
        assertEquals(StorytellerPhase.NIGHT, result.phase)
        assertEquals(2, result.round)
        assertEquals(30L, result.lastGlobalSequence)
        assertEquals(
            setOf(1, 2, 3, 4, 5),
            result.worldSet.enumeratedWorlds().flatMap { world ->
                world.abilityStatesBySeat.filterValues { it == AbilityState.MALFUNCTIONING_POISONED }.keys
            }.toSet(),
        )
    }

    @Test
    fun `exact baseline uses validated night order to reject reversed ability chronology`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.PhaseAdvance("day-1", 1L, StorytellerPhase.DAY, 1),
                    StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 1L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 2L, StorytellerPhase.NIGHT, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 2L,
                ),
            ),
        )
        val fortuneTellerFirst = nightAbilityObservation(
            recordId = "fortune-teller-first",
            sourceSeat = 2,
            sourceAbility = RoleId("Fortune Teller"),
            globalSequence = 10L,
        )
        val empathSecond = nightAbilityObservation(
            recordId = "empath-second",
            sourceSeat = 1,
            sourceAbility = RoleId("Empath"),
            globalSequence = 11L,
        )

        val result = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(listOf(fortuneTellerFirst, empathSecond)),
        )

        assertTrue(result.worldSet.isEmpty())
    }

    @Test
    fun `exact baseline refuses hidden attack and protect until successor branching is modeled`() {
        listOf(
            ActionFact.Attack("hidden-attack", 10L, 2) to "Attack",
            ActionFact.Protect("hidden-protect", 10L, 2) to "Protect",
        ).forEach { (fact, expectedName) ->
            val actions = ActionFactTimeline(
                listOf(
                    action(
                        fact,
                        StorytellerPhase.NIGHT,
                        round = 1,
                        localSequence = 1,
                        globalSequence = 10L,
                    ),
                ),
            )

            try {
                EnumeratedHistoricalExactBaseline.build(
                    validatedRuleset = validatedRuleset,
                    rulesetRef = ruleset,
                    setupKnowledge = setupKnowledge,
                    hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
                    roleDefinitions = roles,
                    initialPhase = StorytellerPhase.FIRST_NIGHT,
                    initialRound = 1,
                    actionTimeline = actions,
                    observationLog = EpistemicObservationLog(),
                )
                fail("Expected incomplete $expectedName semantics to block an exact result.")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().contains(expectedName))
                assertTrue(expected.message.orEmpty().contains("exact", ignoreCase = true))
            }
        }
    }

    @Test
    fun `exact baseline refuses hidden role changes until successor branching is modeled`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.RoleChange(
                        actionId = "starpass",
                        sequence = 10L,
                        targetSeat = 5,
                        role = RoleId("Imp"),
                        alignment = Alignment.EVIL,
                        type = CharacterType.DEMON,
                    ),
                    StorytellerPhase.NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 10L,
                ),
            ),
        )

        try {
            EnumeratedHistoricalExactBaseline.build(
                validatedRuleset = validatedRuleset,
                rulesetRef = ruleset,
                setupKnowledge = setupKnowledge,
                hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
                roleDefinitions = roles,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = actions,
                observationLog = EpistemicObservationLog(),
            )
            fail("Expected incomplete RoleChange semantics to block an exact result.")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("RoleChange"))
            assertTrue(expected.message.orEmpty().contains("exact", ignoreCase = true))
        }
    }

    private fun nightAbilityObservation(
        recordId: String,
        sourceSeat: Int,
        sourceAbility: RoleId,
        globalSequence: Long,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = recordId,
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(setupKnowledge.recipientSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.AliveAt(3, true),
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

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
