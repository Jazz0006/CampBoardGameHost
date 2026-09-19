package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import kotlin.system.measureNanoTime
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D4E T3 evidence for topology-first whole-bundle evaluation.
 *
 * This records measurements rather than imposing a guessed CI timing threshold. Production/mobile
 * cutover thresholds are frozen only after CI plus representative device evidence exists.
 */
class Sde2D4TopologyBundlePerformanceTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-performance",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun record_topology_first_bundle_cost_for_every_supported_player_count() {
        for (playerCount in 5..15) {
            val profile = TroubleBrewingSetupProfiles.standard(playerCount)
            val knowledge = PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = "d4e-$playerCount",
                formalSnapshotId = "d4e-snapshot-$playerCount",
                recipientSeat = 1,
                perceivedRole = RoleId("Chef"),
                setupKnowledge = listOf(
                    InformationProposition.PlayerCount(playerCount),
                    profile,
                ),
            )
            val observation = EpistemicObservation(
                observationId = "d4e-shown-$playerCount",
                snapshotId = "d4e-snapshot-$playerCount",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.ShownRoleAt(1, RoleId("Chef")),
            )
            val query = ExactHypotheticalObservationBundleQuery(
                bundleId = "d4e-$playerCount",
                recipientSeat = 1,
                observations = listOf(observation),
                registrationWitnessBindings = listOf(
                    ExactRegistrationWitnessBinding(
                        observationId = observation.observationId,
                        registrations = emptySet(),
                    ),
                ),
            )

            val heapBefore = usedHeapBytes()
            lateinit var result: ExactStrategicTopologyBundleEvaluation
            val elapsedNanos = measureNanoTime {
                result = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
                    rulesetRef = ruleset,
                    knowledge = knowledge,
                    roleDefinitions = roles,
                    queries = listOf(query),
                    hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                )
            }
            val elapsedMs = elapsedNanos / 1_000_000
            val heapDelta = (usedHeapBytes() - heapBefore).coerceAtLeast(0)

            assertTrue(result is ExactStrategicTopologyBundleEvaluation.Ready)
            val diagnostic =
                (result as ExactStrategicTopologyBundleEvaluation.Ready).diagnostics.single()
            val topologyUpperBound =
                playerCount * choose(playerCount - 1, profile.minions)
            assertTrue(diagnostic.beforeStructure.distinctStrategicWorldCount <= topologyUpperBound)
            assertTrue(diagnostic.afterStructure.distinctStrategicWorldCount <= topologyUpperBound)
            assertTrue(diagnostic.afterFeasible)

            println(
                "SDE_2D4_TOPOLOGY_BUNDLE players=$playerCount " +
                    "topologyUpperBound=$topologyUpperBound " +
                    "beforeKeys=${diagnostic.beforeStructure.distinctStrategicWorldCount} " +
                    "afterKeys=${diagnostic.afterStructure.distinctStrategicWorldCount} " +
                    "elapsedMs=$elapsedMs coarseHeapDeltaBytes=$heapDelta",
            )
        }
    }

    private fun choose(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        val effective = minOf(k, n - k)
        var result = 1L
        for (i in 1..effective) {
            result = result * (n - effective + i) / i
        }
        return result.toInt()
    }

    private fun usedHeapBytes(): Long =
        Runtime.getRuntime().let { runtime -> runtime.totalMemory() - runtime.freeMemory() }
}
