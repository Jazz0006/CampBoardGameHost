package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerMayorDemonExclusionWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val nightUiSource = File(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    ).readText(Charsets.UTF_8)
    private val recommenderSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/recommendation/MayorRedirectRecommender.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Mayor redirect excludes Demon at legality recommendation and manual UI boundaries`() {
        assertTrue(
            "Mayor recommendation candidates must use the shared Demon-exclusion legality rule.",
            recommenderSource.contains("MayorRedirectLegality.canReceiveRedirect") &&
                recommenderSource.contains("CharacterType.DEMON"),
        )

        val mayorStateBlock = hostSource
            .substringAfter("val mayorTarget =")
            .substringBefore("val ravenkeeperTrigger =")
        assertTrue(
            "Host must derive Mayor manual legal targets independently of recommendations.",
            mayorStateBlock.contains("mayorRedirectTargetCards") &&
                mayorStateBlock.contains("ClocktowerTeam.Demon") &&
                mayorStateBlock.contains("MayorRedirectLegality.canReceiveRedirect"),
        )

        // Restored/confirmed Mayor redirect mechanical validation intentionally does not depend on
        // Host source shape here. SNE-7.9C moved that responsibility to the checkpoint-backed
        // canonical Dawn resolver and NightDawnResolutionPlanner. The typed Mayor planner contract
        // proves that an effective Demon redirect fails closed before Dawn materialization, while
        // ClocktowerNightTransactionArchitectureGuardTest protects the Host ownership wiring.

        assertTrue(
            "Mayor UI must expose a dedicated rules-owned legal target parameter.",
            Regex("""mayorRedirectTargetCards\s*:\s*List<PlayerCard>""")
                .containsMatchIn(nightUiSource),
        )
        assertTrue(
            "Manual Mayor selection must consume the rules-owned legal target set regardless of formatting.",
            Regex("""cards\s*=\s*mayorRedirectTargetCards\s*,""")
                .containsMatchIn(nightUiSource),
        )
        assertFalse(
            "Mayor manual legality must not be reconstructed from assisted recommendations.",
            nightUiSource.contains("assistedDecisionOptions.map(ClocktowerDecisionOption::targetName)"),
        )
    }
}
