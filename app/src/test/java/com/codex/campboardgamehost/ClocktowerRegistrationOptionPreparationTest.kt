package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.*
import org.junit.Test

class ClocktowerRegistrationOptionPreparationTest {
    private fun card(name: String, role: String) = PlayerCard(
        name = name, role = Role.Civilian, word = "",
        clocktowerRole = completeTroubleBrewingRoles.single { it.enName == role },
    )
    private val spy = card("S", "Spy")
    private val recluse = card("R", "Recluse")
    private val actor = card("A", "Chef")
    private val cards = listOf(spy, recluse, actor)
    private val choices = ClocktowerRegistrationInteractionState()
    private fun preparation(poison: String? = null, effectiveSpyRole: String = "Spy") =
        ClocktowerRegistrationOptionPreparation(
            cards,
            ClocktowerRegistrationProjection(
                cards, ClocktowerScript.TroubleBrewing, choices, { poison },
                { _, card -> RoleId(if (card == spy) effectiveSpyRole else card.clocktowerRole!!.enName) },
            ),
            text = { _, en -> en }, roleLabel = { it.enName },
        )

    @Test fun `numeric candidates require functioning registration without changing the choice`() {
        choices.chooseSpy("chef", true, "Chef")
        fun options(preparation: ClocktowerRegistrationOptionPreparation) = preparation.numericOptions(
            "Chef", actor, "Chef", NumericMetric.ADJACENT_EVIL_PAIRS,
            listOf(1, 2, 3), "pairs", "chef", null,
        ) { if (it.spyRegistersGood == true) 0 else 1 }
        assertEquals(listOf("0", "1"), options(preparation()).map { it.displayPrimary })
        assertTrue(options(preparation(poison = "S")).isEmpty())
        assertTrue(options(preparation(effectiveSpyRole = "Imp")).isEmpty())
        assertTrue(choices.spyIsGood("chef"))
    }

    @Test fun `fortune teller rejects missing repeated or impaired queries before evaluating results`() {
        for ((key, seats) in listOf(null to listOf(1, 2), "ft" to listOf(2), "ft" to listOf(2, 2))) {
            assertTrue(preparation().fortuneTellerOptions(actor, key, seats, null) {
                error("Invalid query must not be evaluated")
            }.isEmpty())
        }
        assertTrue(preparation(poison = "R").fortuneTellerOptions(actor, "ft", listOf(1, 2), null) {
            error("Impaired registration must not be evaluated")
        }.isEmpty())
        val results = preparation().fortuneTellerOptions(actor, "ft", listOf(1, 2), "1   2") { it }
        assertEquals(listOf("No", "Yes"), results.map { it.displayPrimary })
        assertFalse(choices.recluseIsEvil("ft"))
    }

    @Test fun `role reveal preserves selected ordering and excludes unavailable special registration`() {
        choices.chooseSpy("ut", true, "Chef")
        fun options(preparation: ClocktowerRegistrationOptionPreparation, target: PlayerCard?) =
            preparation.roleRevealOptions(
                "Undertaker", "Undertaker", target, "executed", "ut",
                listOf(ClocktowerTeam.Townsfolk), null, listOf(ClocktowerTeam.Demon),
            )
        assertEquals(listOf("Chef", "Spy"), options(preparation(), spy).take(2).map { it.displayPrimary })
        assertTrue(options(preparation(poison = "S"), spy).isEmpty())
        assertTrue(options(preparation(), actor).isEmpty())
        assertTrue(options(preparation(), null).isEmpty())
        assertTrue(choices.spyIsGood("ut"))
    }
}
