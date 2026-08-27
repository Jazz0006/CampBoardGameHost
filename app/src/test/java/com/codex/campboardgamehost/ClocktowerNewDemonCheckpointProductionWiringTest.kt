package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary production ownership guard for SNE-7.4F-2.
 *
 * Typed planner tests own Dawn behavior. This source guard only protects the remaining App
 * adapter boundary until onConfirmNewDemon is directly callable from a JVM integration seam.
 */
class ClocktowerNewDemonCheckpointProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val confirmNewDemonBlock = appSource
        .substringAfter("onConfirmNewDemon = {")
        .substringBefore("onSelectKlutzChoice = {")

    @Test
    fun `new Demon identity confirmation reuses canonical night checkpoint snapshot`() {
        assertTrue(
            "onConfirmNewDemon must reuse currentClocktowerNightCheckpoint() instead of rebuilding a second snapshot owner.",
            confirmNewDemonBlock.contains("currentClocktowerNightCheckpoint()"),
        )
    }

    @Test
    fun `new Demon identity confirmation does not reconstruct ClocktowerNightCheckpoint by hand`() {
        assertFalse(
            "Night checkpoint fields already have one canonical App projection; onConfirmNewDemon must not duplicate that field list.",
            confirmNewDemonBlock.contains("val checkpoint = ClocktowerNightCheckpoint("),
        )
    }
}
