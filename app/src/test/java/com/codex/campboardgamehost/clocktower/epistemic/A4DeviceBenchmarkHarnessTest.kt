package com.codex.campboardgamehost.clocktower.epistemic

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class A4DeviceBenchmarkHarnessTest {
    @Test fun `report separates native and fallback filter paths in a pasteable line`() {
        val report = A4DeviceBenchmarkReport(
            deviceLabel = "test-device",
            sampleCount = 3,
            worldCardinality = WorldCardinality.Exact(BigInteger.valueOf(42)),
            nodeCount = 7,
            construction = A4LatencyPercentiles(10, 20),
            constructionPhases = A4ConstructionPhaseBenchmarks(
                exactWorldCount = 42,
                worldGeneration = A4LatencyPercentiles(1, 2),
                prefixInsertion = A4LatencyPercentiles(3, 4),
                canonicalization = A4LatencyPercentiles(5, 6),
            ),
            coarseMaxBuildHeapDeltaBytes = 100,
            filters = listOf(
                A4DeviceFilterBenchmark("alive", ZddFilterStrategy.NATIVE_RESTRICTION, A4LatencyPercentiles(7, 8)),
                A4DeviceFilterBenchmark("chef", ZddFilterStrategy.DECODE_REBUILD, A4LatencyPercentiles(9, 10),
                    A4DecodeRebuildBenchmark(21, A4LatencyPercentiles(11, 12), A4LatencyPercentiles(13, 14))),
            ),
        )
        assertEquals(
            "A4_DEVICE_BENCHMARK device=test-device samples=3 worlds=42 nodes=7" +
                " buildP50Us=10 buildP95Us=20 generationP50P95Us=1/2 prefixInsertP50P95Us=3/4" +
                " canonicalizeP50P95Us=5/6 coarseMaxBuildHeapDeltaBytes=100" +
                " filter[alive]=NATIVE_RESTRICTION:7/8us" +
                " filter[chef]=DECODE_REBUILD:9/10us retainedWorlds=21 evalP50P95Us=11/12 rebuildP50P95Us=13/14",
            report.toLogLine(),
        )
    }
}
