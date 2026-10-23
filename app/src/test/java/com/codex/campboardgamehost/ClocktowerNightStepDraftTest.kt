package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerNightStepDraftTest {
    private val impStep = ClocktowerInteractionId("other_night:role:Imp")
    private fun draft(vararg slots: Int?, round: Int = 2) =
        ClocktowerNightStepDraft("draft-test", StorytellerPhase.NIGHT, round, impStep, slots.toList())

    @Test fun `two-player queries keep fixed slots and reject a third seat`() {
        val ft = ClocktowerNightAction.FortuneTeller
        assertEquals(listOf(2, null), draft().withTappedSeat(ft, 2)!!.seatSlots)
        assertEquals(listOf(2, 3), draft(2, null).withTappedSeat(ft, 3)!!.seatSlots)
        // Clearing the first pick leaves its slot open for the next tap.
        assertEquals(listOf(null, 3), draft(2, 3).withTappedSeat(ft, 2)!!.seatSlots)
        assertEquals(listOf(4, 3), draft(null, 3).withTappedSeat(ft, 4)!!.seatSlots)
        assertNull(draft(2, 3).withTappedSeat(ft, 4))
    }

    @Test fun `single-target actions toggle one seat`() {
        val imp = ClocktowerNightAction.DemonKill
        assertEquals(listOf(4), draft().withTappedSeat(imp, 4)!!.seatSlots)
        assertEquals(listOf(5), draft(4).withTappedSeat(imp, 5)!!.seatSlots)
        assertTrue(draft(4).withTappedSeat(imp, 4)!!.selectedSeats.isEmpty())
    }

    @Test fun `a late confirmation from the previous night writes nothing`() {
        val defs = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing).associateBy { it.id.value }
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(),
            listOf("Imp", "Baron", "Chef", "Soldier", "Saint").mapIndexed { i, role ->
                val d = requireNotNull(defs[role])
                PlayerState(i + 1, "P${i + 1}", d.id, d.alignment, d.type)
            }, 7)
        val session = ClocktowerGameSession.createProduction("draft-test", 7, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        fun advance(phase: StorytellerPhase, round: Int) = session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "advance-$phase-$round", session.state.phase, session.state.round, session.nextInputSequence, phase, round),
            session.state.gameStateRevision)
        advance(StorytellerPhase.DAY, 1)
        advance(StorytellerPhase.NIGHT, 2)
        val step = ClocktowerNightStepUi(title = "Imp", actor = PlayerCard("P1"), isRealAction = true, reason = "",
            storytellerAction = "", tellPlayer = null, explanation = "", action = ClocktowerNightAction.DemonKill,
            roleEnName = "Imp", interactionId = impStep)
        val nightTwoDraft = draft(4)
        advance(StorytellerPhase.DAY, 2)
        advance(StorytellerPhase.NIGHT, 3)
        val before = session.state
        val ruleset = BuiltInClocktowerRulesetCatalog { File("src/main/assets/$it").readText(Charsets.UTF_8) }
            .ruleset(ClocktowerScript.TroubleBrewing)

        assertFalse(session.confirmNightStep(ClocktowerNightStepConfirmation(step, nightTwoDraft), ruleset) {})
        assertSame(before, session.state)
    }
}
