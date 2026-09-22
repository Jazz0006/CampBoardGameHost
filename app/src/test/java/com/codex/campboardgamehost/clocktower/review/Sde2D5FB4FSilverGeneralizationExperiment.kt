package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * B4F keeps SILVER generalization narrower than GOLD discovery.
 *
 * This harness reuses the existing executable ClockTracker fixture rather than hand-authoring a
 * second rules model. It verifies that the structured SILVER case is still representable by the
 * current production-owned legality / topology surfaces, then records only the hypotheses that the
 * case can legitimately generalize.
 */
class Sde2D5FB4FSilverGeneralizationExperiment {
    @Test
    fun `ct01 executable SILVER case generalizes bounded hypotheses without inventing preference labels`() {
        val pilot = Sde2D5ExternalHumanPilotBuilder.buildB4FSilverSnapshot()

        assertTrue(pilot.actualBluffLegal)
        assertTrue(pilot.actualRedHerringLegal)
        assertTrue(pilot.actualDrunkCandidateLegal)
        assertEquals(SemanticTruth.FALSE, pilot.actualDrunkSemanticTruth)
        assertTrue(pilot.actualFullBundleFeasibleForEveryRecipient)

        val report = buildString {
            appendLine("# SDE-2D5F B4F bounded SILVER generalization")
            appendLine()
            appendLine("Executable source: ClockTracker ct-01 / ffb40a93-3d7b-42c4-bba8-bc9c363dcd30")
            appendLine("Evidence tier: SILVER")
            appendLine("Storyteller expertise: unverified")
            appendLine()
            appendLine("## Current-production expressiveness")
            appendLine()
            appendLine("- observed Demon bluff triplet remains production-legal: ${pilot.actualBluffLegal}")
            appendLine("- observed Red Herring remains production-legal: ${pilot.actualRedHerringLegal}")
            appendLine("- observed Drunk-Empath 0 remains in the complete legal domain: ${pilot.actualDrunkCandidateLegal}")
            appendLine("- observed Drunk-Empath 0 semantic truth: ${pilot.actualDrunkSemanticTruth}")
            appendLine("- observed whole Night-1 public bundle remains feasible for every recipient: ${pilot.actualFullBundleFeasibleForEveryRecipient}")
            appendLine()
            appendLine("## GOLD-derived hypotheses this case can test")
            appendLine()
            appendLine("| Dimension | SILVER result | Boundary |")
            appendLine("|---|---|---|")
            appendLine(
                "| Red Herring contextual utility | COMPATIBLE_WITH_DOWNSTREAM_BUNDLE_ROLE | " +
                    "The observed RH is a legal persistent setup commitment inside a feasible real bundle, " +
                    "but ct-01 has no recovered rationale for why that seat was selected; it cannot validate seat ordering. |",
            )
            appendLine(
                "| impaired-information believability | COMPATIBLE_NOT_GENERALIZED | " +
                    "The observed Drunk-Empath 0 is a legal false output, but no choice-specific rationale is recovered; " +
                    "do not infer that false or intermediate-pressure outputs were preferred for believability. |",
            )
            appendLine(
                "| Demon bluff joint output | AUTHENTIC_LEGAL_TRIPLET_ONLY | " +
                    "Chef / Investigator / Saint is one real legal triplet. Without verified expertise or rationale, " +
                    "it cannot establish a role ordering, diversity target, or shared-support threshold. |",
            )
            appendLine(
                "| whole-bundle expressiveness | GENERALIZES | " +
                    "The current production-owned legality/topology model can still represent the complete structured " +
                    "SILVER Night-1 ecology without inventing a fixture-local rules path. |",
            )
            appendLine()
            appendLine("## Explicit non-conclusions")
            appendLine()
            appendLine("- unchosen legal candidates are not negative labels;")
            appendLine("- ct-01 does not promote any SILVER choice to GOLD;")
            appendLine("- ct-01 does not establish a Red-Herring seat ranking;")
            appendLine("- ct-01 does not establish a Demon-bluff triplet preference;")
            appendLine("- ct-01 does not establish a false-at-all-costs impaired-information rule;")
            appendLine("- no numeric gate, band, weight, or score is derived here.")
            appendLine()
            appendLine(
                "Qualitative SILVER records ct-02 (truth danger / Evil-topology coupling) and ct-04 " +
                    "(Red-Herring trajectory) remain documentary generalization evidence; they are intentionally " +
                    "not fabricated into executable fixtures without complete reconstructable state.",
            )
        }

        assertTrue(report.contains("COMPATIBLE_NOT_GENERALIZED"))
        assertTrue(report.contains("GENERALIZES"))
        assertTrue(report.contains("no numeric gate, band, weight, or score"))

        val reportFile = File("build/reports/sde-2d5f-b4f-silver-generalization.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
