package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.9E coarse App ownership guard; exactly-once semantics stay in typed planner/session tests. */
class ClocktowerDawnDurableMaterializationProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `night confirmation routes Dawn death durability through the idempotent materialization planner`() {
        val block = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")

        assertTrue(
            "Dawn death materialization must consume NightDawnDurableMaterializationPlanner.",
            block.contains("NightDawnDurableMaterializationPlanner.plan("),
        )
        assertTrue(
            "Dawn materialization must compare against current durable state rather than callback-local counters.",
            block.contains("DawnDurableMaterializationState("),
        )
        assertTrue(
            "Dawn death mutation must be independently suppressible on retry.",
            block.contains("stateMutationRequired"),
        )
        assertTrue(
            "Dawn death ActionFact must consume the planner-owned stable action ID.",
            block.contains("actionIdToCommit"),
        )
        assertTrue(
            "Dawn public AliveAt(false) history must consume the planner-owned stable observation ID.",
            block.contains("publicAliveObservationIdToCommit"),
        )
    }

    @Test
    fun `new Demon confirmation routes role change and Dawn phase durability through the same planner`() {
        val block = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice = {")

        assertTrue(
            "New-Demon Dawn completion must consume NightDawnDurableMaterializationPlanner.",
            block.contains("NightDawnDurableMaterializationPlanner.plan("),
        )
        assertTrue(
            "New-Demon role change and phase completion must compare against durable state.",
            block.contains("DawnDurableMaterializationState("),
        )
        assertTrue(
            "Role-change retry must distinguish state mutation from durable action repair.",
            block.contains("stateMutationRequired"),
        )
        assertTrue(
            "Role-change/phase ActionFacts must consume planner-owned stable IDs.",
            block.contains("actionIdToCommit"),
        )
        assertTrue(
            "Dawn phase transition must be planned independently from role mutation.",
            block.contains("phaseAdvance"),
        )
    }
}
