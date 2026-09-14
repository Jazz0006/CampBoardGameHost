package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerNightHostProjectionTest {
    @Test
    fun `confirmed Imp attack projects one Ravenkeeper death and effective night chronology`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Poisoner", "Poisoner", ClocktowerTeam.Minion),
            card("Ravenkeeper", "Ravenkeeper", ClocktowerTeam.Townsfolk),
            card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            card("Chef", "Chef", ClocktowerTeam.Townsfolk),
        )
        val projection = ClocktowerNightHostProjectionFactory.project(
            cards = cards,
            script = ClocktowerScript.TroubleBrewing,
            ruleset = builtInRuleset(),
            gameSeed = 17L,
            phase = ClocktowerPhase.Night,
            poisonTarget = null,
            checkpoint = checkpoint(confirmedAttackTarget = "Ravenkeeper"),
            pendingNightNewDemonIdentityName = null,
            lastExecutedName = null,
        )
        val impInteractionId = ClocktowerProductionNightStepIdentity
            .role(RoleId("Imp"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)

        assertEquals("Ravenkeeper", projection.resolvedNightDeathName)
        assertEquals("Ravenkeeper", projection.ravenkeeperTrigger?.name)
        assertEquals(null, projection.sageNightDeath)
        assertTrue(projection.otherNightInteractions.any { it.id == impInteractionId })
        assertEquals(
            listOf(3),
            projection.resolvedMechanicalEvents
                .filterIsInstance<ResolvedNightMechanicalEvent.MechanicalDeath>()
                .map { it.targetSeat },
        )
        assertTrue(
            projection.effectiveNightStateAt(
                impInteractionId,
                ClocktowerInteractionBoundary.BEFORE,
            ).isMechanicallyAlive(3),
        )
        assertFalse(
            projection.effectiveNightStateAt(
                impInteractionId,
                ClocktowerInteractionBoundary.AFTER,
            ).isMechanicallyAlive(3),
        )
    }

    private fun builtInRuleset() = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)

    private fun checkpoint(confirmedAttackTarget: String) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 2,
        gameStateRevision = 1L,
        playerInputRevision = 1L,
        nightStarted = true,
        nightStepIndex = 0,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = confirmedAttackTarget,
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        demonSuccessorDraftTarget = null,
    )

    private fun card(name: String, roleName: String, team: ClocktowerTeam) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = team,
        clocktowerRole = ClocktowerRole(
            team = team,
            zhName = roleName,
            enName = roleName,
            zhDescription = "",
            enDescription = "",
        ),
    )
}
