package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary SNE-7.4F production ownership guard.
 *
 * NightDawnResolutionPlannerContractTest owns the typed poison-carry semantics. This source guard
 * only protects the App adapter boundary until onConfirmNewDemon is directly callable from a JVM
 * integration seam.
 */
class ClocktowerNewDemonPoisonAuthorityProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val confirmNewDemonBlock = appSource
        .substringAfter("onConfirmNewDemon = {")
        .substringBefore("onSelectKlutzChoice = {")

    @Test
    fun `planner new Demon Dawn consumes poison carry intent`() {
        assertTrue(
            "The planner-backed successor path must materialize poison from DawnCommitIntent.",
            confirmNewDemonBlock.contains("dawnCommitIntent.poisonCarry"),
        )
        assertTrue(
            "Durable poison history must remain App-owned when the planner changes the carried target.",
            confirmNewDemonBlock.contains("kind = \"poison-after-night\""),
        )
    }

    @Test
    fun `common new Demon Dawn commit does not recompute poison after planner intent`() {
        val commonDawnCommitIndex = confirmNewDemonBlock.lastIndexOf("if (canEnterDawn) {")
        val legacyAfterNightIndex = confirmNewDemonBlock.lastIndexOf("PoisonEffectLifecycle.afterNight(")

        assertTrue("Common Dawn commit boundary must be present.", commonDawnCommitIndex >= 0)
        assertTrue(
            "The legacy poison lifecycle must remain available before the common Dawn commit, not run after planner intent has already been consumed.",
            legacyAfterNightIndex in 0 until commonDawnCommitIndex,
        )

        val commonDawnCommit = confirmNewDemonBlock.substring(commonDawnCommitIndex)
        assertFalse(
            "The common Dawn commit must not become a second poison-resolution authority.",
            commonDawnCommit.contains("PoisonEffectLifecycle.afterNight("),
        )
    }
}
