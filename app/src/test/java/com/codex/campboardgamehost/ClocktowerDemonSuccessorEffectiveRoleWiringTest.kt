package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RED production-wiring contracts for projecting a confirmed same-night Demon successor.
 *
 * The confirmed successor is a mechanical fact. The draft selection must never become current-role
 * authority until the DemonSuccessor interaction has been confirmed, and public PlayerCard roles
 * remain the persisted/dawn representation rather than same-night mechanical state.
 */
class ClocktowerDemonSuccessorEffectiveRoleWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App passes confirmed successor separately from the draft`() {
        assertTrue(
            "ClocktowerJudgeScreen must receive the persisted confirmed successor fact separately " +
                "from the editable demonSuccessorTarget draft.",
            appSource.contains(
                "confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget",
            ),
        )

        val hostSignature = hostSource
            .substringAfter("internal fun ClocktowerJudgeScreen(")
            .substringBefore(") {")

        assertTrue(
            "Host must accept a confirmed successor input without reusing the draft as mechanics.",
            hostSignature.contains("confirmedDemonSuccessorTarget: String?"),
        )
    }

    @Test
    fun `public roles seed the effective role projection`() {
        val projectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds")
            .substringBefore("val chambermaidInteractionId")

        assertTrue(
            "Effective state needs a base RoleId for every publicly assigned seat before chronological " +
                "RoleChanged events can be projected.",
            projectionBlock.contains("val baseRoleIdsBySeat") &&
                projectionBlock.contains("card.clocktowerRole?.enName") &&
                projectionBlock.contains("RoleId("),
        )
        assertTrue(
            "The production projector call must consume the base role map rather than relying on the " +
                "projector's empty-map default.",
            projectionBlock.contains("baseRoleIdsBySeat = baseRoleIdsBySeat"),
        )
    }

    @Test
    fun `confirmed legal successor becomes a RoleChanged event at DemonSuccessor AFTER`() {
        val eventProjectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds")
            .substringBefore("fun effectiveNightStateAt")

        assertTrue(
            "Only the confirmed successor fact may be resolved to a successor seat.",
            eventProjectionBlock.contains("confirmedDemonSuccessorTarget"),
        )
        assertTrue(
            "A restored or otherwise stale confirmed successor must fail closed unless its seat remains " +
                "inside the rules-owned current successor target set.",
            eventProjectionBlock.contains("takeIf { it in demonSuccessorTargetSeats }"),
        )
        assertTrue(
            "The confirmed successor must enter the same typed mechanical event stream as night death.",
            eventProjectionBlock.contains("ResolvedNightMechanicalEvent.RoleChanged("),
        )
        assertTrue(
            "Role change becomes effective at the stable canonical DemonSuccessor AFTER boundary.",
            eventProjectionBlock.contains("ClocktowerProductionNightStepIdentity.demonSuccessor()") &&
                eventProjectionBlock.contains("ClocktowerNightFlowPhase.OTHER_NIGHT") &&
                eventProjectionBlock.contains("ClocktowerInteractionBoundary.AFTER"),
        )
    }

    @Test
    fun `successor role identity comes from the current Demon rather than a hardcoded script role`() {
        val eventProjectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds")
            .substringBefore("fun effectiveNightStateAt")

        assertTrue(
            "Same-night succession must project the canonical current Demon RoleId so dynamic scripts " +
                "do not acquire a Trouble-Brewing-specific role branch.",
            eventProjectionBlock.contains("demonCard?.clocktowerRole?.enName") &&
                eventProjectionBlock.contains("RoleId"),
        )
        assertFalse(
            "Production RoleChanged wiring must not hardcode the Trouble Brewing Imp identity.",
            eventProjectionBlock.contains("roleId = RoleId(\"Imp\")"),
        )
    }

    @Test
    fun `draft successor selection is not mechanical RoleChanged authority`() {
        val eventProjectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds")
            .substringBefore("fun effectiveNightStateAt")

        assertFalse(
            "Editing or navigating back to a Demon successor draft must not change effective current " +
                "role until that draft is confirmed again.",
            eventProjectionBlock.contains("demonSuccessorTarget?.let"),
        )
        assertTrue(
            "The mechanical event path must consume confirmedDemonSuccessorTarget instead.",
            eventProjectionBlock.contains("confirmedDemonSuccessorTarget"),
        )
    }
}
