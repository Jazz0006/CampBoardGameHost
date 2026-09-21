package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit SDE-2D5F-B4 expert-observed evidence workload.
 *
 * This stays separate from the historical broad SDE-2D5 calibration experiment so B4 iteration
 * validates the current evidence path without rerunning multi-hour historical calibration corpora.
 */
class Sde2D5FExpertObservedCalibrationExperiment {
    private val aStud by lazy {
        Sde2D5FAStudInScarletConsequenceCalibrationBuilder.build()
    }
    private val liveAndImpPerson by lazy {
        Sde2D5FLiveAndImpPersonConsequenceCalibrationBuilder.build()
    }

    @Test
    fun `A Stud uses committed prefix topology with bounded exact parity and no GOLD promotion`() {
        val calibration = aStud

        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            calibration.verificationStatus,
        )
        assertEquals(listOf(0, 1, 2), calibration.stages.map { it.prefixObservationCount })
        assertEquals("value-1", calibration.chefObservedCandidateId)
        assertEquals("value-0", calibration.drunkEmpathObservedCandidateId)
        assertEquals("answer-yes", calibration.fortuneTellerObservedCandidateId)

        calibration.stages.forEach { stage ->
            val exactSamples = stage.alternatives.flatMap { alternative ->
                alternative.byRecipient.filter { recipient -> recipient.strategicParity != null }
            }
            assertTrue(exactSamples.isEmpty())
            stage.alternatives.forEach { alternative ->
                alternative.byRecipient.forEach { recipient ->
                    assertTrue(
                        recipient.candidateTopologyStructure.distinctStrategicWorldCount <=
                            recipient.prefixTopologyStructure.distinctStrategicWorldCount,
                    )
                }
            }
        }

        val report = Sde2D5FAStudInScarletConsequenceCalibrationBuilder.renderMarkdown(calibration)
        assertTrue(report.contains("PRIMARY_VERIFICATION_PENDING"))
        assertTrue(report.contains("Observed candidate: `value-1`"))
        assertTrue(report.contains("Observed candidate: `value-0`"))
        assertTrue(report.contains("Observed candidate: `answer-yes`"))
        assertTrue(report.contains("topology-first strategic diagnostics"))
        assertTrue(report.contains("no unchosen legal candidate"))
        assertFalse(report.contains("BAD_TOO_STRONG"))
        assertFalse(report.contains("BAD_TOO_WEAK"))

        val reportFile = File("build/reports/sde-2d5f-expert-observed-a-stud.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
    @Test
    fun `Live and Imp-Person preserves full pair domain and interaction-scoped registration sequence`() {
        val calibration = liveAndImpPerson

        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            calibration.verificationStatus,
        )
        assertEquals(listOf(0, 1, 2), calibration.stages.map { it.prefixObservationCount })
        assertTrue(calibration.librarian.alternatives.size > 1)
        assertTrue(
            calibration.librarian.alternatives.any {
                it.candidateId == calibration.librarianObservedCandidateId
            },
        )
        assertEquals("value-1", calibration.chefObservedCandidateId)
        assertEquals("answer-yes", calibration.fortuneTellerObservedCandidateId)

        calibration.stages.forEach { stage ->
            assertTrue(
                stage.alternatives.flatMap { it.byRecipient }
                    .all { recipient -> recipient.strategicParity == null },
            )
            stage.alternatives.forEach { alternative ->
                alternative.byRecipient.forEach { recipient ->
                    assertTrue(
                        recipient.candidateTopologyStructure.distinctStrategicWorldCount <=
                            recipient.prefixTopologyStructure.distinctStrategicWorldCount,
                    )
                }
            }
        }

        val report =
            Sde2D5FLiveAndImpPersonConsequenceCalibrationBuilder.renderMarkdown(calibration)
        assertTrue(report.contains("Live and Imp-Person"))
        assertTrue(report.contains("PRIMARY_VERIFICATION_PENDING"))
        assertTrue(report.contains("interaction-scoped registration evidence"))
        assertTrue(report.contains("Observed candidate: `value-1`"))
        assertTrue(report.contains("Observed candidate: `answer-yes`"))
        assertFalse(report.contains("BAD_TOO_STRONG"))
        assertFalse(report.contains("BAD_TOO_WEAK"))

        val reportFile = File("build/reports/sde-2d5f-expert-observed-live-and-imp-person.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }

}
