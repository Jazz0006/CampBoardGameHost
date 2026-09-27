package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** One bounded exact-enumeration smoke case per supported size regime; no machine timing gate. */
class A3EnumerationSmokeTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "a3-benchmark",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test fun `constrained 8 10 12 and 15 player snapshots remain nonempty and exact`() {
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
            val worlds = TroubleBrewingWorldEnumerator.enumerate(
                ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles,
            )
            assertFalse("$playerCount players", worlds.isEmpty())
            assertTrue("$playerCount players", worlds.cardinality() is WorldCardinality.Exact)
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

}
