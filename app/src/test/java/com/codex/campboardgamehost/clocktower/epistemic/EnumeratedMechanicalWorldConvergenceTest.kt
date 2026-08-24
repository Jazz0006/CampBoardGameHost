package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedMechanicalWorldConvergenceTest {
    private val script = ScriptId("trouble_brewing")
    private val ruleset = RulesetRef(
        script,
        "0123456789abcdef0123456789abcdef",
        "a3-h3-convergence-test",
        "official",
        RuleCoverage.VERIFIED,
    )
    private val formalSnapshotId = "snapshot-a3-h3-convergence"
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Poisoner", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )
    private val rolesBySeat = linkedMapOf(
        1 to RoleId("Empath"),
        2 to RoleId("Chef"),
        3 to RoleId("Poisoner"),
        4 to RoleId("Imp"),
        5 to RoleId("Fortune Teller"),
    )

    @Test
    fun `H3 mechanically identical setup worlds merge provenance instead of increasing cardinality`() {
        val pathA = WorldExplanationClusterId("h3-setup-path-a")
        val pathB = WorldExplanationClusterId("h3-setup-path-b")
        val first = EnumeratedWorld(
            rolesBySeat = rolesBySeat,
            explanationClusters = setOf(pathA),
        )
        val second = EnumeratedWorld(
            rolesBySeat = rolesBySeat,
            explanationClusters = setOf(pathB),
        )

        val result = worldSet(listOf(first, second))
        val worlds = result.enumeratedWorlds()

        assertEquals(1, worlds.size)
        assertEquals(setOf(pathA, pathB), worlds.single().explanationClusters)
    }

    @Test
    fun `H3 historical hidden paths converge by mechanical state and retain merged provenance`() {
        val poisonSeat2 = WorldExplanationClusterId("h3-poison-seat-2")
        val poisonSeat5 = WorldExplanationClusterId("h3-poison-seat-5")
        val first = EnumeratedWorld(
            rolesBySeat = rolesBySeat,
            abilityStatesBySeat = mapOf(2 to AbilityState.MALFUNCTIONING_POISONED),
            explanationClusters = setOf(poisonSeat2),
        )
        val second = EnumeratedWorld(
            rolesBySeat = rolesBySeat,
            abilityStatesBySeat = mapOf(5 to AbilityState.MALFUNCTIONING_POISONED),
            explanationClusters = setOf(poisonSeat5),
        )
        val initial = worldSet(listOf(first, second))

        assertEquals(2, initial.enumeratedWorlds().size)

        val result = EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = initial,
            formalSnapshotId = formalSnapshotId,
            initialPhase = StorytellerPhase.DAY,
            initialRound = 1,
            events = listOf(
                PlayerHistoricalEvent.PublicDeath(
                    actionId = "h3-poisoner-death",
                    targetSeat = 3,
                    point = TimelinePoint(StorytellerPhase.DAY, 1, 8, 20L),
                ),
            ),
        )
        val worlds = result.worldSet.enumeratedWorlds()

        assertEquals(1, worlds.size)
        assertEquals(emptyMap<Int, AbilityState>(), worlds.single().abilityStatesBySeat)
        assertEquals(setOf(1, 2, 4, 5), worlds.single().aliveSeats)
        assertEquals(setOf(poisonSeat2, poisonSeat5), worlds.single().explanationClusters)
    }

    private fun worldSet(worlds: List<EnumeratedWorld>): EnumeratedWorldSet = EnumeratedWorldSet.fromWorlds(
        rulesetRef = ruleset,
        knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-a3-h3-convergence",
            formalSnapshotId = formalSnapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Empath"),
            setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
        ),
        hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
        roleDefinitions = roles,
        worlds = worlds,
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
