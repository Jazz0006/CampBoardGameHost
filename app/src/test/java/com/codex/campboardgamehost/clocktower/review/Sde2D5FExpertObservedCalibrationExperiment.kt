package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
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
    private val humanRemains by lazy {
        Sde2D5FHumanRemainsConsequenceCalibrationBuilder.build()
    }
    private val policyFeatures by lazy {
        Sde2D5FExpertObservedPolicyFeatureReportBuilder.build()
    }

    @Test
    fun `A Stud uses topology-first committed prefix consequence without GOLD promotion`() {
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
    @Test
    fun `Human Remains keeps complete poisoned pair domain and summarizes rather than truncates it`() {
        val calibration = humanRemains

        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            calibration.verificationStatus,
        )
        assertEquals(listOf(0, 1), calibration.stages.map { it.prefixObservationCount })
        assertTrue(calibration.washerwoman.alternatives.size > 100)
        assertTrue(
            calibration.washerwoman.alternatives.any {
                it.candidateId == calibration.washerwomanObservedCandidateId
            },
        )
        assertEquals("answer-no", calibration.fortuneTellerObservedCandidateId)
        assertEquals(1, calibration.fortuneTeller.alternatives.size)

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

        val report = Sde2D5FHumanRemainsConsequenceCalibrationBuilder.renderMarkdown(calibration)
        assertTrue(report.contains("Human Remains Of The Day"))
        assertTrue(report.contains("PRIMARY_VERIFICATION_PENDING"))
        assertTrue(report.contains("Legal candidates:"))
        assertTrue(report.contains("Distinct strategic-after signatures:"))
        assertTrue(report.contains("instead of truncating the legal alternatives"))
        assertTrue(report.contains("Observed candidate: `answer-no`"))
        assertFalse(report.contains("BAD_TOO_STRONG"))
        assertFalse(report.contains("BAD_TOO_WEAK"))

        val reportFile = File("build/reports/sde-2d5f-expert-observed-human-remains.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }

    @Test
    fun `expert cases expose descriptive non-topology policy features without scoring`() {
        val features = policyFeatures

        val aStudChef = features.aStudChef.associateBy { it.candidateId }
        assertTrue(aStudChef.getValue("value-1").registration.requiresSpecialRegistration)
        assertEquals(
            setOf(RegistrationReason.RECLUSE_ABILITY),
            aStudChef.getValue("value-1").registration.specialReasons,
        )
        assertEquals(setOf(5), aStudChef.getValue("value-1").registration.specialSubjectSeats)
        assertTrue(aStudChef.getValue("value-0").registration.hasNaturalWitness)

        val aStudFt = features.aStudFortuneTeller.associateBy { it.candidateId }
        assertTrue(aStudFt.getValue("answer-yes").registration.requiresSpecialRegistration)
        assertTrue(aStudFt.getValue("answer-no").registration.hasNaturalWitness)

        val liveLibrarian = features.liveLibrarian.single {
            it.candidateId == "pair-information-ability-v1|Librarian|Recluse|5,8"
        }
        assertEquals(SemanticTruth.TRUE, liveLibrarian.semanticTruth)
        assertFalse(liveLibrarian.shownRoleIsDemonBluff)
        assertEquals(setOf(8), liveLibrarian.shownRoleActualInPlaySeats)
        assertEquals(setOf(8), liveLibrarian.shownRoleCandidateMatchSeats)
        assertEquals(setOf(8), liveLibrarian.specialRegistrationRoleMatchSeats)
        assertEquals(setOf(8), liveLibrarian.candidateSpecialRegistrationSubjectSeats)
        assertTrue(liveLibrarian.candidateEvilSeats.isEmpty())

        val liveChef = features.liveChef.associateBy { it.candidateId }
        assertTrue(liveChef.getValue("value-1").registration.hasNaturalWitness)
        assertTrue(liveChef.getValue("value-2").registration.requiresSpecialRegistration)
        assertEquals(setOf(8), liveChef.getValue("value-2").registration.specialSubjectSeats)

        val liveFt = features.liveFortuneTeller.associateBy { it.candidateId }
        assertTrue(liveFt.getValue("answer-no").registration.hasNaturalWitness)
        assertTrue(liveFt.getValue("answer-yes").registration.requiresSpecialRegistration)
        assertEquals(setOf(8), liveFt.getValue("answer-yes").registration.specialSubjectSeats)

        val humanObserved = features.humanWasherwoman.single {
            it.candidateId == "pair-information-ability-v1|Washerwoman|Empath|2,5"
        }
        assertEquals(SemanticTruth.FALSE, humanObserved.semanticTruth)
        assertTrue(humanObserved.registration.hasNaturalWitness)
        assertTrue(humanObserved.shownRoleIsDemonBluff)
        assertTrue(humanObserved.shownRoleActualInPlaySeats.isEmpty())
        assertTrue(humanObserved.shownRoleCandidateMatchSeats.isEmpty())
        assertTrue(humanObserved.specialRegistrationRoleMatchSeats.isEmpty())
        assertEquals(setOf(2, 5), humanObserved.candidateEvilSeats)
        assertEquals(setOf(5), humanObserved.candidateDemonSeats)
        assertEquals(setOf(2), humanObserved.candidateMinionSeats)

        val humanSignatures =
            features.humanWasherwoman.map { it.descriptiveSignature() }.toSet()
        assertTrue(humanSignatures.size > 1)

        val report = Sde2D5FExpertObservedPolicyFeatureReportBuilder.render(features)
        assertTrue(report.contains("descriptive candidate facts only"))
        assertTrue(report.contains("Distinct descriptive feature signatures:"))
        assertTrue(report.contains("shown role is Demon bluff"))
        assertFalse(report.contains("BAD_TOO_STRONG"))
        assertFalse(report.contains("BAD_TOO_WEAK"))

        val reportFile = File("build/reports/sde-2d5f-expert-observed-policy-features.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }


}
