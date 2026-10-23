package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** The current Host step is derived from the Session, not UI-supplied step metadata. */
class ClocktowerHostNightStepTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { File("src/main/assets/$it").readText(Charsets.UTF_8) }
        .ruleset(ClocktowerScript.TroubleBrewing)

    private fun session(vararg roles: String, drunkShownAs: String? = null, night: Int = 1): ClocktowerGameSession {
        val defs = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing).associateBy { it.id.value }
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), roles.mapIndexed { i, role ->
            val d = requireNotNull(defs[role])
            PlayerState(i + 1, "P" + (i + 1), d.id, d.alignment, d.type,
                shownRole = drunkShownAs?.takeIf { role == "Drunk" }?.let(::RoleId))
        }, 5)
        return ClocktowerGameSession.createProduction("host-step", 5, game, ClocktowerSemanticHistoryMode.GLOBAL_V1).also { s ->
            (2..night).forEach { round ->
                listOf(StorytellerPhase.DAY to round - 1, StorytellerPhase.NIGHT to round).forEach { (phase, r) ->
                    s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("advance-$phase-$r", s.state.phase,
                        s.state.round, s.nextInputSequence, phase, r), s.state.gameStateRevision)
                }
            }
        }
    }

    private fun draft(s: ClocktowerGameSession, vararg seats: Int) = ClocktowerNightStepDraft(
        s.state.gameId, s.state.phase, s.state.round,
        requireNotNull(s.state.nextNightInteraction(ruleset)).id, seats.toList())

    @Test fun chefNumberAndStepIdentityComeFromSession() {
        val s = session("Chef", "Soldier", "Baron", "Imp", "Saint")
        val host = hostNightStep(s.state, ruleset, null)
        assertEquals("Chef", host.facts.roleEnName)
        assertEquals(ClocktowerNightAction.None, host.facts.action)
        assertEquals(1, host.facts.actorSeat)
        assertEquals(requireNotNull(s.state.nextNightInteraction(ruleset)).id, host.facts.interactionId)
        assertTrue(host.facts.isRealAction)
        assertEquals(NumericMetric.ADJACENT_EVIL_PAIRS, host.numericMetric)
        assertEquals(listOf(1), host.numericResolution?.values)
        assertTrue(host.hostDeterminedNumeric)
        assertTrue(host.isPrivateInformation)
    }

    @Test fun recluseAndDrunkPreserveDiscretionFromSessionFacts() {
        val recluse = hostNightStep(session("Chef", "Recluse", "Imp", "Soldier", "Baron").state, ruleset, null)
        assertTrue(recluse.numericResolution!!.values.size > 1)
        assertFalse(recluse.hostDeterminedNumeric)
        val drunk = hostNightStep(session("Drunk", "Soldier", "Baron", "Imp", "Saint",
            drunkShownAs = "Chef").state, ruleset, null)
        assertEquals("Chef", drunk.facts.roleEnName)
        assertEquals(1, drunk.facts.actorSeat)
        assertFalse(drunk.hostDeterminedNumeric)
    }

    @Test fun currentPoisonerAndButlerTargetsUseSessionSeatConstraints() {
        val s = session("Poisoner", "Imp", "Baron", "Soldier", "Saint")
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death("death-5", s.state.phase, s.state.round,
            s.nextInputSequence, 5), s.state.gameStateRevision)
        assertEquals(ClocktowerNightAction.Poison, hostNightStep(s.state, ruleset, draft(s)).facts.action)
        assertFalse(hostNightStep(s.state, ruleset, draft(s, 5)).targetReady)
        assertTrue(hostNightStep(s.state, ruleset, draft(s, 4)).targetReady)

        val butler = session("Butler", "Imp", "Baron", "Soldier", "Saint")
        val ownSeat = hostNightStep(butler.state, ruleset, draft(butler, 1))
        assertEquals(ClocktowerNightAction.ButlerMaster, ownSeat.facts.action)
        assertFalse(ownSeat.targetReady)
    }

    @Test fun redHerringAndFortuneTellerAreDistinctSessionInteractions() {
        val s = session("Fortune Teller", "Imp", "Baron", "Soldier", "Saint")
        assertEquals(ClocktowerNightAction.RedHerring, hostNightStep(s.state, ruleset, draft(s)).facts.action)
        assertFalse(hostNightStep(s.state, ruleset, draft(s)).targetReady)
        assertEquals(4, hostNightStep(s.state, ruleset, draft(s, 4)).selectedSeat)
        s.commitRedHerringSeat(4, StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence)
        val fortune = hostNightStep(s.state, ruleset, draft(s, 2, 4))
        assertEquals(ClocktowerNightAction.FortuneTeller, fortune.facts.action)
        assertEquals(listOf(2, 4),
            (fortune.facts.question as ClocktowerInformationQuestion.Boolean).subjectSeats)
    }

    @Test fun impSelfKillSuccessorUsesCurrentSessionInteraction() {
        val s = session("Imp", "Poisoner", "Baron", "Chef", "Soldier", night = 2)
        s.confirmNightPoison(5, StorytellerPhase.NIGHT, 2, "other_night:role:Poisoner")
        val host = hostNightStep(s.state, ruleset, draft(s, 1))
        assertEquals(ClocktowerNightAction.DemonKill, host.facts.action)
        assertEquals(listOf(2, 3), host.successorSeats.sorted())
        assertFalse(host.successorReady(null))
        assertNull(host.successorSeat(5))
        assertEquals(3, host.successorSeat(3))
    }

    @Test fun currentMayorRulingIsNotUISupplied() {
        val s = session("Imp", "Mayor", "Baron", "Soldier", "Saint", night = 2)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, "other_night:role:Imp")
        val pending = hostNightStep(s.state, ruleset, draft(s))
        assertEquals(ClocktowerNightAction.MayorRedirect, pending.facts.action)
        assertTrue(pending.facts.isRealAction)
        assertFalse(pending.targetReady)
        assertTrue(hostNightStep(s.state, ruleset, draft(s, 3)).targetReady)
    }

    @Test fun anOldInteractionDraftIsRejectedAfterStepAdvances() {
        val s = session("Poisoner", "Imp", "Baron", "Soldier", "Saint")
        val old = draft(s, 4)
        assertEquals(ClocktowerNightAction.Poison, hostNightStep(s.state, ruleset, old).facts.action)
        s.confirmNightPoison(4, StorytellerPhase.FIRST_NIGHT, 1, "first_night:role:Poisoner")
        assertThrows(IllegalArgumentException::class.java) {
            hostNightStep(s.state, ruleset, old)
        }
    }

    @Test fun manualOverlayIsEmptyAndOutOfRangeOpeningNumbersAreRejected() {
        val s = session("Chef", "Soldier", "Baron", "Imp", "Saint")
        val host = hostNightStep(s.state, ruleset, null)
        val cards = s.state.gameState.players.map { PlayerCard(it.name, clocktowerRole = clocktowerRoleByName(it.actualRole.value)) }
        val step = ClocktowerNightStepUi(title = "Chef", actor = cards[0], isRealAction = true, reason = "",
            storytellerAction = "", tellPlayer = null, explanation = "", displayKind = ClocktowerDisplayKind.Number,
            informationQuestion = host.facts.question, roleEnName = "Chef")
        fun overlay(assisted: Boolean, plannedNumber: Int) = nightAiOverlay(host, NightAiInputs(
            assisted = assisted, automatic = false, adviceKey = null, adviceBusy = false, adviceError = null,
            residualChoices = emptyMap(), mayorPending = null, fortuneTellerSelectedNames = emptyList(),
            jointOpeningSemantics = mapOf(1 to HostOpeningSemanticValueV2(1, "Chef", null, "FUNCTIONING", "NONE",
                emptyList(), "NUMBER", number = plannedNumber)),
        ), s.state, ruleset, step, 0, ClocktowerPhase.FirstNight, cards, "en",
            clocktowerRolesForScript(ClocktowerScript.TroubleBrewing))
        assertEquals(NightAiOverlay(), overlay(assisted = false, plannedNumber = 1))
        assertNotNull(overlay(assisted = true, plannedNumber = 1).suggestedOption)
        assertNull(overlay(assisted = true, plannedNumber = 0).suggestedOption)
    }
}
