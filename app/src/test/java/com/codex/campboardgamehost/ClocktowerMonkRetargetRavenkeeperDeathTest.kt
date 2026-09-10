package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
import com.codex.campboardgamehost.clocktower.session.NightResolutionEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerMonkRetargetRavenkeeperDeathTest {
    @Test
    fun `reconfirmed Monk target replaces Ravenkeeper protection before Demon attack`() {
        var checkpoint = emptyCheckpoint()
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.EditMonkProtectionDraft("Ravenkeeper"),
        )
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.ConfirmMonkProtection,
        )
        checkpoint = NightCheckpointReducer.reduce(checkpoint, NightResolutionEvent.MovePrevious)
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.EditMonkProtectionDraft("Empath"),
        )
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.ConfirmMonkProtection,
        )
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.EditDemonAttackDraft("Ravenkeeper"),
        )
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint,
            NightResolutionEvent.ConfirmDemonAttack,
        )

        assertEquals("Empath", checkpoint.confirmedMonkTarget)
        assertEquals("Ravenkeeper", checkpoint.confirmedAttackTarget)

        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Monk", "Monk", ClocktowerTeam.Townsfolk),
                card("Ravenkeeper", "Ravenkeeper", ClocktowerTeam.Townsfolk),
                card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            ),
            targetName = checkpoint.confirmedAttackTarget,
            poisonedPlayerName = checkpoint.confirmedPoisonTarget,
            monkProtectedTargetName = checkpoint.confirmedMonkTarget,
        )

        assertEquals(DemonNightAttackOutcome.TARGET_DIES, facts.attackOutcome)
        assertEquals(3, facts.originalDeathSeat)
    }

    private fun emptyCheckpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 2,
        gameStateRevision = 0,
        playerInputRevision = 0,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = null,
        attackDraftTarget = null,
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        demonSuccessorDraftTarget = null,
    )

    private fun card(
        name: String,
        roleName: String,
        team: ClocktowerTeam,
    ) = PlayerCard(
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
