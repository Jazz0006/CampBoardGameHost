package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** SNE-7.9C2B: Dawn must consume one canonical set of direct Demon attack/safety facts. */
class ClocktowerDemonAttackDawnFactsTest {
    @Test
    fun `ordinary target produces canonical original death seat without Mayor branch`() {
        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            ),
            targetName = "Empath",
            poisonedPlayerName = null,
            monkProtectedTargetName = null,
        )

        assertEquals(DemonNightAttackOutcome.TARGET_DIES, facts.attackOutcome)
        assertEquals(2, facts.originalDeathSeat)
        assertNull(facts.mayorSeat)
    }

    @Test
    fun `Mayor branch and redirected Demon safe seats come from the same canonical semantics`() {
        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
                card("Soldier", "Soldier", ClocktowerTeam.Townsfolk),
                card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            ),
            targetName = "Mayor",
            poisonedPlayerName = null,
            monkProtectedTargetName = null,
        )

        assertEquals(DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED, facts.attackOutcome)
        assertEquals(2, facts.originalDeathSeat)
        assertEquals(2, facts.mayorSeat)
        assertEquals(setOf(3), facts.demonSafeSeats)
    }

    @Test
    fun `poisoned Soldier is removed from Demon safe seats`() {
        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
                card("Soldier", "Soldier", ClocktowerTeam.Townsfolk),
            ),
            targetName = "Mayor",
            poisonedPlayerName = "Soldier",
            monkProtectedTargetName = null,
        )

        assertEquals(DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED, facts.attackOutcome)
        assertEquals(emptySet<Int>(), facts.demonSafeSeats)
    }

    @Test
    fun `functioning Monk protection blocks the original attack and marks its target Demon safe`() {
        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Monk", "Monk", ClocktowerTeam.Townsfolk),
                card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
            ),
            targetName = "Mayor",
            poisonedPlayerName = null,
            monkProtectedTargetName = "Mayor",
        )

        assertEquals(DemonNightAttackOutcome.NO_DEATH, facts.attackOutcome)
        assertEquals(3, facts.originalDeathSeat)
        assertNull(facts.mayorSeat)
        assertEquals(setOf(3), facts.demonSafeSeats)
    }

    @Test
    fun `poisoned Monk cannot create a Demon safe redirect target`() {
        val facts = resolveTroubleBrewingDawnDeathFacts(
            cards = listOf(
                card("Imp", "Imp", ClocktowerTeam.Demon),
                card("Monk", "Monk", ClocktowerTeam.Townsfolk),
                card("Mayor", "Mayor", ClocktowerTeam.Townsfolk),
            ),
            targetName = "Mayor",
            poisonedPlayerName = "Monk",
            monkProtectedTargetName = "Mayor",
        )

        assertEquals(DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED, facts.attackOutcome)
        assertEquals(emptySet<Int>(), facts.demonSafeSeats)
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
