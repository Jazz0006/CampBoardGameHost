package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.math.BigInteger
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SDE-2D4 T3 scale evidence.
 *
 * This class deliberately separates source-derived total search-space volume from bounded measured
 * prefix throughput. Prefix timing is performance evidence only and is never used as exact SAT/UNSAT
 * evidence.
 */
class Sde2D4ScaleBenchmarkTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-scale-benchmark",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun `current minimally constrained Chef search-space matrix is reproducible`() {
        val expectedMechanicalWorlds = mapOf(
            5 to "16728",
            6 to "1614960",
            7 to "11048400",
            8 to "881274240",
            9 to "21546645120",
            10 to "238973898240",
            11 to "5844298521600",
            12 to "120286402790400",
            13 to "1448416055116800",
            14 to "22381606504857600",
            15 to "186298351906867200",
        ).mapValues { BigInteger(it.value) }

        for (playerCount in 5..15) {
            val mechanical = estimatedMechanicalWorldCount(playerCount)
            val minions = TroubleBrewingSetupProfiles.standard(playerCount).minions
            val topologyUpperBound =
                BigInteger.valueOf(playerCount.toLong()) * choose(playerCount - 1, minions)

            assertEquals(expectedMechanicalWorlds.getValue(playerCount), mechanical)
            assertTrue(mechanical >= topologyUpperBound)
            println(
                "SDE_2D4_SCALE_MATRIX players=$playerCount " +
                    "topologyUpperBound=$topologyUpperBound mechanicalWorlds=$mechanical",
            )
        }

        val fivePlayerKnowledge = minimallyConstrainedKnowledge(5)
        val actualFivePlayerWorlds = TroubleBrewingWorldEnumerator
            .stream(ruleset, fivePlayerKnowledge, roles)
            .worlds
            .count()
            .toBigInteger()
        assertEquals(expectedMechanicalWorlds.getValue(5), actualFivePlayerWorlds)
    }

    @Test
    fun `record bounded raw-stream prefix throughput across all four player regimes`() {
        for (playerCount in listOf(5, 8, 12, 15)) {
            val knowledge = minimallyConstrainedKnowledge(playerCount)
            var generated = 0
            val heapBefore = usedHeapBytes()
            val elapsedNanos = measureNanoTime {
                generated = TroubleBrewingWorldEnumerator
                    .stream(ruleset, knowledge, roles)
                    .worlds
                    .take(PREFIX_WORLD_LIMIT)
                    .count()
            }
            val elapsedMillis = elapsedNanos / 1_000_000
            val heapDelta = (usedHeapBytes() - heapBefore).coerceAtLeast(0)

            assertTrue(generated > 0)
            assertTrue(generated <= PREFIX_WORLD_LIMIT)
            println(
                "SDE_2D4_RAW_PREFIX players=$playerCount generated=$generated " +
                    "limit=$PREFIX_WORLD_LIMIT elapsedMs=$elapsedMillis " +
                    "coarseHeapDeltaBytes=$heapDelta",
            )
        }
    }

    private fun minimallyConstrainedKnowledge(playerCount: Int) = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "sde-2d4-scale-$playerCount",
        formalSnapshotId = "sde-2d4-scale-snapshot-$playerCount",
        recipientSeat = 1,
        perceivedRole = RoleId("Chef"),
        setupKnowledge = listOf(InformationProposition.PlayerCount(playerCount)),
    )

    /**
     * Mirrors the current TroubleBrewingWorldEnumerator dimensions without iterating seat
     * permutations. Recipient seat 1 is shown Chef and may actually be Chef or Drunk where legal.
     */
    private fun estimatedMechanicalWorldCount(playerCount: Int): BigInteger {
        val catalogByType = roles.groupBy(RoleDefinition::type)
        val townsfolk = catalogByType.getValue(CharacterType.TOWNSFOLK).map { it.id }
        val outsiders = catalogByType.getValue(CharacterType.OUTSIDER).map { it.id }
        val minions = catalogByType.getValue(CharacterType.MINION).map { it.id }
        val chef = RoleId("Chef")
        val drunk = RoleId("Drunk")
        val fortuneTeller = RoleId("Fortune Teller")
        val poisoner = RoleId("Poisoner")
        val baron = RoleId("Baron")
        val imp = RoleId("Imp")
        val seatPermutations = factorial(playerCount - 1)

        fun profileCount(
            profile: InformationProposition.SetupProfile,
            baronProfile: Boolean,
        ): BigInteger {
            val minionChoices = if (baronProfile) {
                combinations(minions.filterNot { it == baron }, profile.minions - 1)
                    .map { listOf(baron) + it }
            } else {
                combinations(minions.filterNot { it == baron }, profile.minions)
            }

            var total = BigInteger.ZERO

            if (profile.townsfolk >= 1) {
                combinations(townsfolk.filterNot { it == chef }, profile.townsfolk - 1).forEach { otherTownsfolk ->
                    val selectedTownsfolk = listOf(chef) + otherTownsfolk
                    combinations(outsiders, profile.outsiders).forEach { selectedOutsiders ->
                        minionChoices.forEach { selectedMinions ->
                            total += seatPermutations * variantMultiplier(
                                playerCount = playerCount,
                                minionCount = profile.minions,
                                actualTownsfolk = selectedTownsfolk,
                                actualOutsiders = selectedOutsiders,
                                selectedMinions = selectedMinions,
                                recipientIsDrunk = false,
                                townsfolkCatalogSize = townsfolk.size,
                                fortuneTeller = fortuneTeller,
                                poisoner = poisoner,
                                drunk = drunk,
                            )
                        }
                    }
                }
            }

            if (profile.outsiders >= 1) {
                combinations(townsfolk.filterNot { it == chef }, profile.townsfolk).forEach { selectedTownsfolk ->
                    combinations(outsiders.filterNot { it == drunk }, profile.outsiders - 1).forEach { otherOutsiders ->
                        val selectedOutsiders = listOf(drunk) + otherOutsiders
                        minionChoices.forEach { selectedMinions ->
                            total += seatPermutations * variantMultiplier(
                                playerCount = playerCount,
                                minionCount = profile.minions,
                                actualTownsfolk = selectedTownsfolk,
                                actualOutsiders = selectedOutsiders,
                                selectedMinions = selectedMinions,
                                recipientIsDrunk = true,
                                townsfolkCatalogSize = townsfolk.size,
                                fortuneTeller = fortuneTeller,
                                poisoner = poisoner,
                                drunk = drunk,
                            )
                        }
                    }
                }
            }

            // Imp is the only Trouble Brewing Demon, so its role choice adds no multiplicity.
            require(imp in roles.map(RoleDefinition::id))
            return total
        }

        val standard = TroubleBrewingSetupProfiles.standard(playerCount)
        val baronProfile = TroubleBrewingSetupProfiles.withBaron(playerCount)
        return profileCount(standard, false) + profileCount(baronProfile, true)
    }

    private fun variantMultiplier(
        playerCount: Int,
        minionCount: Int,
        actualTownsfolk: List<RoleId>,
        actualOutsiders: List<RoleId>,
        selectedMinions: List<RoleId>,
        recipientIsDrunk: Boolean,
        townsfolkCatalogSize: Int,
        fortuneTeller: RoleId,
        poisoner: RoleId,
        drunk: RoleId,
    ): BigInteger {
        val redHerringVariants =
            if (fortuneTeller in actualTownsfolk) playerCount - minionCount - 1 else 1
        val poisonVariants = if (poisoner in selectedMinions) playerCount else 1
        val hiddenDrunkShownRoleVariants =
            if (!recipientIsDrunk && drunk in actualOutsiders) {
                townsfolkCatalogSize - actualTownsfolk.size
            } else {
                1
            }
        return BigInteger.valueOf(
            redHerringVariants.toLong() *
                poisonVariants.toLong() *
                hiddenDrunkShownRoleVariants.toLong(),
        )
    }

    private fun factorial(value: Int): BigInteger =
        (2..value).fold(BigInteger.ONE) { product, factor ->
            product * BigInteger.valueOf(factor.toLong())
        }

    private fun choose(n: Int, k: Int): BigInteger {
        if (k < 0 || k > n) return BigInteger.ZERO
        val effective = minOf(k, n - k)
        return (1..effective).fold(BigInteger.ONE) { result, i ->
            result * BigInteger.valueOf((n - effective + i).toLong()) /
                BigInteger.valueOf(i.toLong())
        }
    }

    private fun <T> combinations(values: List<T>, count: Int): List<List<T>> {
        if (count == 0) return listOf(emptyList())
        if (count < 0 || count > values.size) return emptyList()
        val result = mutableListOf<List<T>>()
        fun walk(start: Int, selected: MutableList<T>) {
            if (selected.size == count) {
                result += selected.toList()
                return
            }
            val remaining = count - selected.size
            for (index in start..values.size - remaining) {
                selected += values[index]
                walk(index + 1, selected)
                selected.removeAt(selected.lastIndex)
            }
        }
        walk(0, mutableListOf())
        return result
    }

    private fun usedHeapBytes(): Long =
        Runtime.getRuntime().let { runtime -> runtime.totalMemory() - runtime.freeMemory() }

    private companion object {
        const val PREFIX_WORLD_LIMIT = 1_000
    }
}
