package com.codex.campboardgamehost
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.*
import org.junit.Test

class ClocktowerRecurringInformationStepMaterializersTest {
    private val cards = listOf(PlayerCard("Alice"), PlayerCard("Bob"), PlayerCard("Carol"))
    private val builder = ClocktowerInformationStepBuilder(cards, "en", { _, en -> en }, { cards[0] },
        { "missing" }, { _, _ -> AbilityFunctioningState.POISONED })
    @Test fun `recurring questions keep actual targets without Host answers or candidate menus`() {
        val empath = clocktowerEmpathStepMaterializer(builder, listOf(2, 3), { _, en -> en }).build()
        assertEquals(ClocktowerInformationQuestion.Number(NumericMetric.LIVING_EVIL_NEIGHBOURS, listOf(2, 3)), empath.informationQuestion)
        assertNull(empath.displayProposition)
        assertTrue(empath.manualInformationCandidates.isEmpty())
        val ft = clocktowerFortuneTellerStepMaterializer(builder, listOf(2, 3), { _, en -> en }).build()
        assertEquals(ClocktowerInformationQuestion.Boolean(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, listOf(2, 3)), ft.informationQuestion)
        assertNull(ft.tellPlayer)
        assertTrue(ft.displayOptions.isEmpty())
        assertNull(clocktowerFortuneTellerStepMaterializer(builder, listOf(2), { _, en -> en }).build().informationQuestion)
    }
}
