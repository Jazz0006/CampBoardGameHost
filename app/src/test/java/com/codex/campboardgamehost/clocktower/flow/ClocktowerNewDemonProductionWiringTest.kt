package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerNewDemonProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `next-night new Demon identity is explicit persisted app state`() {
        assertTrue(appSource.contains(
            "var clocktowerPendingNightNewDemonIdentityName by remember { mutableStateOf<String?>(null) }",
        ))
        assertTrue(appSource.contains(
            "\"clocktowerPendingNightNewDemonIdentityName\" to json.optNullableString(\"clocktowerPendingNightNewDemonIdentityName\")",
        ))
        assertTrue(appSource.contains(
            "pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,",
        ))
        assertTrue(appSource.contains(
            "clocktowerPendingNightNewDemonIdentityName = restoredNightCheckpoint.pendingNightNewDemonIdentityName",
        ))
    }

    @Test
    fun `only daytime Demon deaths queue the next-night identity interaction`() {
        val slayerPromotion = appSource
            .substringAfter("val promotedName = if (targetCard.clocktowerTeam == ClocktowerTeam.Demon) {")
            .substringBefore("shotOutcome =")
        assertTrue(slayerPromotion.contains("clocktowerPendingNightNewDemonIdentityName = promotedName"))

        val executionPromotion = appSource
            .substringAfter("} else if (executedCard.clocktowerTeam == ClocktowerTeam.Demon) {")
            .substringBefore("} else {", missingDelimiterValue = "")
        assertTrue(executionPromotion.contains("clocktowerPendingNightNewDemonIdentityName = promotedName"))

        val nightSuccession = appSource
            .substringAfter("if (demonDied) {")
            .substringBefore("if (nightDeathCard.clocktowerRole?.enName == \"Klutz\")")
        assertFalse(
            "Night self-kill succession must keep using the existing same-night confirmation path.",
            nightSuccession.contains("clocktowerPendingNightNewDemonIdentityName"),
        )
    }

    @Test
    fun `host projects queued daytime promotion through resolved fact and lazy new-Demon materializer`() {
        assertTrue(appSource.contains("NewDemonIdentity,"))
        assertTrue(appSource.contains(
            "pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,",
        ))
        assertTrue(hostSource.contains("pendingNightNewDemonIdentityName: String?,"))
        assertTrue(hostSource.contains("if (pendingNightNewDemonIdentityName != null)"))
        assertTrue(hostSource.contains("action = ClocktowerNightAction.NewDemonIdentity"))
        assertTrue(hostSource.contains(
            "if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)",
        ))
        assertTrue(hostSource.contains(
            "ClocktowerProductionOtherNightFlow.interactions(",
        ))
        assertTrue(hostSource.contains(
            "phase = ClocktowerNightFlowPhase.OTHER_NIGHT",
        ))
        assertTrue(hostSource.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
        assertTrue(hostSource.contains(
            "otherNightMaterializers.materialize(otherNightInteractions)",
        ))
    }

    @Test
    fun `night self-kill confirmation remains separate from next-night lazy identity interaction`() {
        assertTrue(hostSource.contains("pendingNewDemonName?.let { newDemonName ->"))
        assertTrue(hostSource.contains("ClocktowerNewDemonConfirmationScreen("))
        assertTrue(hostSource.contains(
            "ClocktowerProductionOtherNightFlow.interactions(",
        ))
        assertTrue(hostSource.contains(
            "phase = ClocktowerNightFlowPhase.OTHER_NIGHT",
        ))
        assertTrue(hostSource.contains(
            "ClocktowerProductionNightStepIdentity.newDemonIdentity()",
        ))
        assertTrue(hostSource.contains(
            "otherNightMaterializers.materialize(otherNightInteractions)",
        ))
    }

    @Test
    fun `queued identity survives the whole night and is cleared only at the night completion boundary`() {
        val confirmNight = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")
        assertTrue(confirmNight.contains("clocktowerPendingNightNewDemonIdentityName = null"))

        val hostAdvance = hostSource
            .substringAfter("val advanceNightStep = {")
            .substringBefore("LaunchedEffect(")
        assertFalse(
            "Confirming the identity step must not remove it mid-night and shift the step index.",
            hostAdvance.contains("pendingNightNewDemonIdentityName"),
        )
    }
}
