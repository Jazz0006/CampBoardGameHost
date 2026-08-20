package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import kotlin.system.measureNanoTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Repeatable development benchmark; it records boundaries but does not impose a correctness cap. */
class A3EnumerationBenchmarkTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "a3-benchmark",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test fun `constrained 8 10 12 and 15 player snapshots remain exact and measurable`() {
        for (playerCount in listOf(8, 10, 12, 15)) {
            val assignment = standardAssignment(playerCount)
            val unpinnedRoles = if (playerCount == 10) {
                setOf("Fortune Teller", "Poisoner", "Spy", "Imp")
            } else setOf("Fortune Teller", "Drunk", "Poisoner", "Imp")
            val setupKnowledge = buildList<InformationProposition> {
                add(TroubleBrewingSetupProfiles.standard(playerCount))
                assignment.filterValues { it.value !in unpinnedRoles }.forEach { (seat, role) ->
                    add(InformationProposition.RoleAt(seat, role))
                }
            }
            val knowledge = PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = "a3-benchmark-$playerCount",
                formalSnapshotId = "a3-benchmark-snapshot-$playerCount",
                recipientSeat = 1,
                perceivedRole = assignment.getValue(1),
                setupKnowledge = setupKnowledge,
            )
            val elapsed = mutableListOf<Long>()
            var cardinality: WorldCardinality? = null
            var maxHeapDelta = 0L
            repeat(5) {
                var result: EnumeratedWorldSet? = null
                val usedBefore = usedHeapBytes()
                val elapsedNanos = measureNanoTime {
                    result = TroubleBrewingWorldEnumerator.enumerate(
                        ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles,
                    )
                }
                val worlds = requireNotNull(result)
                assertFalse(worlds.isEmpty())
                assertTrue(worlds.cardinality() is WorldCardinality.Exact)
                cardinality?.let { assertTrue(it == worlds.cardinality()) }
                cardinality = worlds.cardinality()
                elapsed += elapsedNanos / 1_000_000
                maxHeapDelta = maxOf(maxHeapDelta, (usedHeapBytes() - usedBefore).coerceAtLeast(0))
            }
            assertTrue("Constrained A3 benchmark exceeded 10 seconds at $playerCount players", elapsed.all { it < 10_000 })
            val warm = elapsed.drop(1).sorted()
            println("A3_BENCHMARK players=$playerCount worlds=${requireNotNull(cardinality).valueOrLowerBound} " +
                "coldMs=${elapsed.first()} warmP50Ms=${warm[warm.size / 2]} warmP95Ms=${warm.last()} " +
                "maxObservedHeapDeltaBytes=$maxHeapDelta")
        }
    }

    private fun standardAssignment(playerCount: Int): Map<Int, RoleId> {
        val profile = TroubleBrewingSetupProfiles.standard(playerCount)
        val byType = roles.groupBy(RoleDefinition::type)
        val selected = buildList {
            addAll(preferred(byType.getValue(com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK),
                listOf("Chef", "Fortune Teller"), profile.townsfolk))
            addAll(preferred(byType.getValue(com.codex.campboardgamehost.clocktower.domain.CharacterType.OUTSIDER),
                listOf("Drunk", "Recluse"), profile.outsiders))
            addAll(preferred(byType.getValue(com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION),
                listOf("Poisoner", "Spy", "Scarlet Woman"), profile.minions))
            addAll(preferred(byType.getValue(com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON),
                listOf("Imp"), profile.demons))
        }
        return selected.mapIndexed { index, role -> index + 1 to role.id }.toMap(linkedMapOf())
    }

    private fun preferred(available: List<RoleDefinition>, preferred: List<String>, count: Int): List<RoleDefinition> {
        val order = preferred + available.map { it.id.value }.sorted()
        return order.distinct().mapNotNull { name -> available.singleOrNull { it.id.value == name } }.take(count)
            .also { require(it.size == count) }
    }

    private fun usedHeapBytes(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
}
