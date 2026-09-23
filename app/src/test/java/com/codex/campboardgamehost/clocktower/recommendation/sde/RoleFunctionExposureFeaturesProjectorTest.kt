package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleFunctionExposureFeaturesProjectorTest {
    private val target = RoleFunctionExposureTargetRef(
        seat = 4,
        role = RoleId("Hidden Role"),
        recipientSeat = 2,
        capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
    )

    @Test
    fun `exposure shared by every legal candidate is forced`() {
        val projected = RoleFunctionExposureFeaturesProjector.project(
            listOf(
                evidence("candidate-a", direct = setOf(target)),
                evidence("candidate-b", direct = setOf(target)),
            ),
        )

        projected.values.forEach { features ->
            assertEquals(setOf(target), features.directlyExposedTargets)
            assertEquals(setOf(target), features.newlyExposedTargets)
            assertEquals(setOf(target), features.forcedExposureTargets)
            assertTrue(features.avoidableExposureTargets.isEmpty())
        }
    }

    @Test
    fun `exposure with a legal preserving alternative is avoidable`() {
        val projected = RoleFunctionExposureFeaturesProjector.project(
            listOf(
                evidence("exposes", direct = setOf(target)),
                evidence("preserves"),
            ),
        )

        val exposed = projected.getValue("exposes")
        assertEquals(setOf(target), exposed.avoidableExposureTargets)
        assertTrue(exposed.forcedExposureTargets.isEmpty())
        assertTrue(projected.getValue("preserves").directlyExposedTargets.isEmpty())
    }

    @Test
    fun `already exposed target is not reported as newly exposed`() {
        val features = RoleFunctionExposureFeaturesProjector.project(
            listOf(
                evidence(
                    candidateId = "candidate",
                    direct = setOf(target),
                    already = setOf(target),
                ),
            ),
        ).getValue("candidate")

        assertEquals(setOf(target), features.alreadyExposedTargets)
        assertTrue(features.newlyExposedTargets.isEmpty())
    }

    @Test
    fun `confirmation amplification remains separate from exposure novelty`() {
        val features = RoleFunctionExposureFeaturesProjector.project(
            listOf(
                evidence(
                    candidateId = "candidate",
                    direct = setOf(target),
                    already = setOf(target),
                    amplified = setOf(target),
                ),
            ),
        ).getValue("candidate")

        assertEquals(setOf(target), features.alreadyExposedTargets)
        assertEquals(setOf(target), features.confirmationAmplifiedTargets)
        assertTrue(features.newlyExposedTargets.isEmpty())
    }

    private fun evidence(
        candidateId: String,
        direct: Set<RoleFunctionExposureTargetRef> = emptySet(),
        already: Set<RoleFunctionExposureTargetRef> = emptySet(),
        amplified: Set<RoleFunctionExposureTargetRef> = emptySet(),
    ) = RoleFunctionExposureCandidateEvidence(
        candidateId = candidateId,
        directlyExposedTargets = direct,
        alreadyExposedTargets = already,
        confirmationAmplifiedTargets = amplified,
    )
}
