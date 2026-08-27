package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

/** SNE-7.9C2A: production PlayerCard state must enter the canonical Demon attack resolver once. */
class ClocktowerDemonAttackProductionAdapterTest {
    @Test
    fun `poisoned Imp cannot produce a night death`() {
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = listOf(
                    card("Imp", "Imp", ClocktowerTeam.Demon),
                    card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
                ),
                targetName = "Mayor",
                poisonedPlayerName = "Imp",
                monkProtectedTargetName = null,
            ),
        )
    }

    @Test
    fun `functioning Monk protection reaches canonical attack semantics`() {
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = listOf(
                    card("Imp", "Imp", ClocktowerTeam.Demon),
                    card("Monk", "Monk", ClocktowerTeam.Townsfolk),
                    card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
                ),
                targetName = "Mayor",
                poisonedPlayerName = null,
                monkProtectedTargetName = "Mayor",
            ),
        )
    }

    @Test
    fun `Soldier safety disappears when the Soldier is poisoned`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Soldier", "Soldier", ClocktowerTeam.Townsfolk),
        )

        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = cards,
                targetName = "Soldier",
                poisonedPlayerName = null,
                monkProtectedTargetName = null,
            ),
        )
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = cards,
                targetName = "Soldier",
                poisonedPlayerName = "Soldier",
                monkProtectedTargetName = null,
            ),
        )
    }

    @Test
    fun `unprotected Mayor preserves the redirect branch`() {
        assertEquals(
            DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = listOf(
                    card("Imp", "Imp", ClocktowerTeam.Demon),
                    card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
                ),
                targetName = "Mayor",
                poisonedPlayerName = null,
                monkProtectedTargetName = null,
            ),
        )
    }

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
