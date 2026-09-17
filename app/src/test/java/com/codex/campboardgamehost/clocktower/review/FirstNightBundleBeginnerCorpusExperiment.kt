package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit FN-BUNDLE-3 calibration workload.
 *
 * Production-relevant Stage 7A now runs the complete Fortune Teller-aware pilot for one real
 * seven-player preset. Every Storyteller diagnostic candidate includes its Red Herring choice and
 * is evaluated against every legal player-selected Fortune Teller target pair. Latent choices that
 * cannot change the current PUBLIC_GOOD_INFO consequence retain exact multiplicity instead of
 * generating duplicate epistemic queries. Older rich/weak fixtures remain synthetic stress probes.
 * Holdout is never evaluated here.
 */
class FirstNightBundleBeginnerCorpusExperiment {
    @Test
    fun `generate beginner calibration review corpus`() {
        val fortuneTellerPilot = FirstNightBundleBeginnerFortuneTellerPilotBuilder.build()
        val richCorpus = FirstNightBundleBeginnerCorpusBuilder.buildCalibration()
        val lowInformation = FirstNightBundleBeginnerLowInformationCalibrationBuilder.build()

        assertEquals("TB2_7_003", fortuneTellerPilot.presetId)
        assertEquals(7, fortuneTellerPilot.seating.size)
        assertTrue(fortuneTellerPilot.legalCompleteBundleCount.signum() > 0)
        assertTrue(fortuneTellerPilot.publicInformationCombinationCount > 0)
        assertTrue(fortuneTellerPilot.redHerringCandidateCount > 0)
        assertEquals(21, fortuneTellerPilot.targetPairCount)
        assertEquals(
            fortuneTellerPilot.publicInformationCombinationCount * fortuneTellerPilot.redHerringCandidateCount,
            fortuneTellerPilot.diagnosticStorytellerCandidateCount,
        )
        assertEquals(
            fortuneTellerPilot.legalCompleteBundleCount,
            java.math.BigInteger.valueOf(fortuneTellerPilot.diagnosticStorytellerCandidateCount.toLong())
                .multiply(fortuneTellerPilot.latentMultiplicityPerDiagnosticCandidate),
        )
        assertTrue(fortuneTellerPilot.candidates.all { candidate ->
            candidate.robustnessCases.size == fortuneTellerPilot.targetPairCount &&
                candidate.robustnessCases.all { case ->
                    case.diagnostics.afterWorldCount.signum() > 0 &&
                        case.diagnostics.afterWorldCount <= case.diagnostics.beforeWorldCount
                }
        })

        assertEquals(1, richCorpus.scenarios.size)
        assertTrue(richCorpus.scenarios.all { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION })
        assertTrue(richCorpus.items.isNotEmpty())
        assertTrue(richCorpus.items.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(richCorpus.items.all { it.selectionReasons.isNotEmpty() })
        assertTrue(richCorpus.items.all { it.anchorDiagnostics.afterWorldCount.signum() > 0 })
        assertTrue(richCorpus.items.all {
            it.anchorDiagnostics.afterWorldCount <= it.anchorDiagnostics.beforeWorldCount
        })
        assertTrue(lowInformation.legalWasherwomanCandidateCount >= lowInformation.selectedPointCount)
        assertTrue(lowInformation.selectedPointCount in 1..3)
        assertTrue(lowInformation.points.all { point ->
            point.diagnostics.afterWorldCount.signum() > 0 &&
                point.diagnostics.afterWorldCount <= point.diagnostics.beforeWorldCount
        })

        val report = buildString {
            append(FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(richCorpus).removeSuffix("FN_BUNDLE_3_CORPUS_END\n"))
            appendLine()
            append(FirstNightBundleBeginnerFortuneTellerPilotBuilder.renderMarkdown(fortuneTellerPilot))
            appendLine()
            appendLine("## Synthetic weak-information stress probe")
            appendLine()
            append(FirstNightBundleBeginnerLowInformationCalibrationBuilder.renderMarkdown(lowInformation))
            appendLine("FN_BUNDLE_3_CORPUS_END")
        }
        val reportFile = File("build/reports/fn-bundle-3-beginner-corpus.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
