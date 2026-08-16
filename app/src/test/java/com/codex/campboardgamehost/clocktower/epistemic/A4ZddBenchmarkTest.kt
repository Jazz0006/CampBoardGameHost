package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import kotlin.system.measureNanoTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Development comparison only; target-device acceptance remains a separate A4 gate. */
class A4ZddBenchmarkTest {
    @Test fun `record ZDD construction and native filtering against materialized baseline`() {
        val ruleset = RulesetRef(
            TroubleBrewingFixtures.scriptId,
            "0123456789abcdef0123456789abcdef",
            "a4-benchmark",
            "official",
            RuleCoverage.VERIFIED,
        )
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-benchmark-knowledge",
            formalSnapshotId = "a4-benchmark-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val numericObservation = EpistemicObservation(
            observationId = "a4-benchmark-chef-one",
            snapshotId = "a4-benchmark-snapshot",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 1,
            ),
        )
        val spyAbsentObservation = publicObservation(
            "a4-benchmark-spy-absent",
            InformationProposition.RoleInPlay(RoleId("Spy"), false),
        )
        val aliveObservation = publicObservation(
            "a4-benchmark-seat-two-alive",
            InformationProposition.AliveAt(2, true),
        )
        val cases = listOf(
            FilterCase("numericFallback", numericObservation, ZddFilterStrategy.DECODE_REBUILD),
            FilterCase("spyAbsentNative", spyAbsentObservation, ZddFilterStrategy.NATIVE_RESTRICTION),
            FilterCase("aliveNative", aliveObservation, ZddFilterStrategy.NATIVE_RESTRICTION),
        )

        val buildTimes = mutableListOf<Long>()
        val directBuildTimes = mutableListOf<Long>()
        val filterTimes = cases.associateWith { FilterTimes() }
        var nodeCount = 0
        repeat(SAMPLES) {
            lateinit var zdd: ZddPlayerWorldSet
            buildTimes += measureNanoTime { zdd = ZddPlayerWorldSet.fromEnumerated(enumerated) } / 1_000
            lateinit var direct: ZddPlayerWorldSet
            directBuildTimes += measureNanoTime {
                direct = ZddPlayerWorldSet.enumerateDirect(
                    ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                    TroubleBrewingFixtures.fullRoleDefinitions(),
                )
            } / 1_000
            assertEquals(zdd.cardinality(), direct.cardinality())
            assertEquals(zdd.nodeCount(), direct.nodeCount())
            nodeCount = zdd.nodeCount()
            cases.forEach { case ->
                lateinit var filteredEnumerated: EnumeratedWorldSet
                lateinit var filteredZdd: ZddPlayerWorldSet
                filterTimes.getValue(case).enumeratedMicros += measureNanoTime {
                    filteredEnumerated = enumerated.require(case.observation)
                } / 1_000
                filterTimes.getValue(case).zddMicros += measureNanoTime {
                    filteredZdd = zdd.require(case.observation)
                } / 1_000
                assertEquals(filteredEnumerated.cardinality(), filteredZdd.cardinality())
                assertEquals(case.expectedStrategy, filteredZdd.lastFilterStrategy)
            }
        }
        val retainedZddHeapEstimate = estimateRetainedZddBytes(enumerated)

        assertTrue(nodeCount > 0)
        assertTrue(nodeCount < enumerated.cardinality().valueOrLowerBound.toInt())
        println("A4_ZDD_BENCHMARK worlds=${enumerated.cardinality().valueOrLowerBound} nodes=$nodeCount " +
            "buildColdUs=${buildTimes.first()} buildWarmP50Us=${p50(warm(buildTimes))} " +
            "buildWarmP95Us=${p95(warm(buildTimes))} " +
            "directBuildColdUs=${directBuildTimes.first()} directBuildWarmP50Us=${p50(warm(directBuildTimes))} " +
            "directBuildWarmP95Us=${p95(warm(directBuildTimes))} " +
            "retainedZddHeapEstimateBytes=$retainedZddHeapEstimate")
        cases.forEach { case ->
            val times = filterTimes.getValue(case)
            println("A4_ZDD_FILTER case=${case.label} strategy=${case.expectedStrategy} " +
                "enumeratedP50Us=${p50(warm(times.enumeratedMicros))} " +
                "enumeratedP95Us=${p95(warm(times.enumeratedMicros))} " +
                "zddP50Us=${p50(warm(times.zddMicros))} zddP95Us=${p95(warm(times.zddMicros))}")
        }
    }

    private fun publicObservation(id: String, proposition: InformationProposition) = EpistemicObservation(
        observationId = id,
        snapshotId = "a4-benchmark-snapshot",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )

    private fun estimateRetainedZddBytes(enumerated: EnumeratedWorldSet): Long {
        forceGc()
        val before = usedHeapBytes()
        val retained = ZddPlayerWorldSet.fromEnumerated(enumerated)
        forceGc()
        val after = usedHeapBytes()
        assertTrue(retained.nodeCount() > 0)
        return (after - before).coerceAtLeast(0)
    }

    private fun forceGc() = repeat(2) { Runtime.getRuntime().gc() }
    private fun warm(values: List<Long>): List<Long> = values.drop(1)
    private fun p50(values: List<Long>): Long = values.sorted()[values.size / 2]
    private fun p95(values: List<Long>): Long = values.sorted()[(values.size * 95 + 99) / 100 - 1]
    private fun usedHeapBytes(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }

    private data class FilterCase(
        val label: String,
        val observation: EpistemicObservation,
        val expectedStrategy: ZddFilterStrategy,
    )

    private class FilterTimes(
        val enumeratedMicros: MutableList<Long> = mutableListOf(),
        val zddMicros: MutableList<Long> = mutableListOf(),
    )

    private companion object { const val SAMPLES = 11 }
}
