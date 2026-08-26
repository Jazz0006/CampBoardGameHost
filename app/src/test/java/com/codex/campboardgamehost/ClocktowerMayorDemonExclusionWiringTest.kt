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
    fun `Mayor redirect excludes Demon at legality recommendation UI and restored fact boundaries`() {
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
        assertTrue(
            "A restored/confirmed Demon redirect target must fail closed before mechanical death resolution.",
            mayorStateBlock.contains("effectiveMayorRedirectTarget") &&
                mayorStateBlock.contains("resolvedNightDeathName") &&
                mayorStateBlock.contains("effectiveMayorRedirectTarget"),
        )

        val mayorUiBlock = nightUiSource
            .substringAfter("ClocktowerNightAction.DemonKill ->")
            .substringAfter("ClocktowerNightAction.MayorRedirect ->")
            .substringBefore("ClocktowerNightAction.DemonSuccessor ->")
        assertTrue(
            "Manual Mayor selection must render the legal target set, not recommendation target names.",
            mayorUiBlock.contains("cards = mayorRedirectTargetCards"),
        )
        assertFalse(
            "Mayor manual legality must not be reconstructed from assisted recommendations.",
            mayorUiBlock.contains("assistedDecisionOptions.map(ClocktowerDecisionOption::targetName)"),
        )
    }
}
