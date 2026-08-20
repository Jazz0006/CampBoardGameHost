package com.codex.campboardgamehost.clocktower.epistemic

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class A4WorldEngineRuntimePolicyTest {
    @Test fun `default rollout retains enumerated baseline until device validation`() {
        assertEquals(
            A4WorldSetRepresentation.ENUMERATED,
            A4WorldEngineRuntimePolicy().preferredRepresentation(),
        )
        assertEquals(
            A4WorldSetRepresentation.ENUMERATED,
            A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW).preferredRepresentation(),
        )
        assertEquals(true, A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW).shadowsZdd())
        assertEquals(
            A4WorldSetRepresentation.ZDD,
            A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_DEVICE_VALIDATED).preferredRepresentation(),
        )
    }

    @Test fun `over-budget recommendations never imply an approximate world result`() {
        val policy = A4WorldEngineRuntimePolicy(targetOperationMillis = 15, provisionalMaximumOperationMillis = 50)
        assertEquals(A4DegradationRecommendation.NONE, policy.recommendation(15))
        assertEquals(A4DegradationRecommendation.REDUCE_EXPLANATION_DETAIL, policy.recommendation(16))
        assertEquals(A4DegradationRecommendation.ASSISTED, policy.recommendation(51))
        assertEquals(A4DegradationRecommendation.MANUAL_ONLY, policy.recommendation(51, repeatedOverBudget = true))
    }

    @Test fun `telemetry retains exact cardinality and native versus fallback classification`() {
        val recorder = A4WorldEngineTelemetryRecorder()
        recorder.record(
            A4WorldEngineTelemetry(
                A4WorldEngineOperation.REQUIRE,
                A4WorldSetRepresentation.ZDD,
                ZddFilterStrategy.NATIVE_RESTRICTION,
                elapsedMillis = 4,
                cardinality = WorldCardinality.Exact(BigInteger.TEN),
                recommendation = A4DegradationRecommendation.NONE,
            ),
        )
        recorder.record(
            A4WorldEngineTelemetry(
                A4WorldEngineOperation.REQUIRE,
                A4WorldSetRepresentation.ZDD,
                ZddFilterStrategy.DECODE_REBUILD,
                elapsedMillis = 54,
                cardinality = WorldCardinality.Exact(BigInteger.ONE),
                recommendation = A4DegradationRecommendation.ASSISTED,
            ),
        )

        assertEquals(2, recorder.snapshot().size)
        assertEquals(ZddFilterStrategy.DECODE_REBUILD, recorder.snapshot().last().filterStrategy)
        assertEquals(WorldCardinality.Exact(BigInteger.ONE), recorder.snapshot().last().cardinality)
    }
}
