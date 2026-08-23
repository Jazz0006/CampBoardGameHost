package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EnumeratedHistoricalExactBaselineTest {
    private val script = ScriptId("trouble_brewing")
    private val ruleset = RulesetRef(
        script,
        "0123456789abcdef0123456789abcdef",
        "a3-historical-constructor-test",
        "official",
        RuleCoverage.VERIFIED,
    )
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
        assertTrue(
            result.worldSet.enumeratedWorlds().any { world ->
                world.abilityStatesBySeat.entries.any { it.value == com.codex.campboardgamehost.clocktower.domain.AbilityState.MALFUNCTIONING_POISONED }
            },
        )
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
