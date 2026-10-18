package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightTargets
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerMonkRetargetRavenkeeperDeathTest {
    @Test
    fun `reconfirmed Monk target replaces Ravenkeeper protection before Demon attack`() {
        val targets = ClocktowerNightTargets().withProtection(3).withProtection(4).withAttack(3)
        val names = listOf("Imp", "Monk", "Ravenkeeper", "Empath")
        val checkpoint = emptyCheckpoint().copy(
            confirmedMonkTarget = targets.monkSeat?.let { names[it - 1] },
            confirmedAttackTarget = targets.attackSeat?.let { names[it - 1] },
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
    )

    private fun card(
        name: String,
        roleName: String,
        team: ClocktowerTeam,
    ) = PlayerCard(
        name = name,
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
