package com.codex.campboardgamehost.clocktower.flow

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production guards for the non-callable queued new-Demon identity lifecycle.
 *
 * Stable interaction identity, ordering and checkpoint round-trip semantics are proved by
 * ClocktowerNewDemonIdentityContractTest. These source checks retain only the App/Host lifecycle
 * boundaries that are not directly callable from JVM tests.
 */
class ClocktowerNewDemonProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `restored queued identity reaches canonical other-night promotion fact`() {
        assertTrue(
            appSource.contains(
                "clocktowerPendingNightNewDemonIdentityName = restoredNightCheckpoint.pendingNightNewDemonIdentityName",
            ),
        )
        assertTrue(
            appSource.contains(
                "pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,",
            ),
        )
        assertTrue(
            hostSource.contains(
                "if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)",
            ),
        )
        assertTrue(hostSource.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
    }

    @Test
    fun `queued identity clears at night completion rather than step advance`() {
        val confirmNight = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")
        val hostAdvance = hostSource
            .substringAfter("val advanceNightStep = {")
            .substringBefore("LaunchedEffect(")

        assertTrue(confirmNight.contains("clocktowerPendingNightNewDemonIdentityName = null"))
        assertFalse(hostAdvance.contains("pendingNightNewDemonIdentityName"))
    }
}
