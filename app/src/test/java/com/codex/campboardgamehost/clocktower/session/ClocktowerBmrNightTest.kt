package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.badMoonRisingRoles
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.*
import org.junit.Test

class ClocktowerBmrNightTest {
    private fun session(demon: String = "Shabaloth", courtier: Boolean = false, minion: String = if(courtier) "Assassin" else "Mastermind", outsider: String = "Goon", fifthTown: String = "Gossip"): ClocktowerGameSession {
        val roles = listOf(if (courtier) "Courtier" else "Tea Lady", "Fool", "Pacifist", "Minstrel", fifthTown, outsider, minion, demon)
        val game = GameState(BadMoonRisingOpening.script, roles.mapIndexed { i, name ->
            val role = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name),
                if (role.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(role.team.name.uppercase()))
        }, 50L)
        return ClocktowerGameSession.createProduction("bmr-night", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun ClocktowerGameSession.night(round: Int) {
        commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-$round", state.phase, state.round,
            nextInputSequence, StorytellerPhase.DAY, round - 1), state.gameStateRevision)
        commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night-$round", state.phase, state.round,
            nextInputSequence, StorytellerPhase.NIGHT, round), state.gameStateRevision)
    }
    private fun ruleset() = BuiltInClocktowerRulesetCatalog { path ->
        java.io.File("src/main/assets", path).readText()
    }.ruleset(com.codex.campboardgamehost.ClocktowerScript.BadMoonRising)

    @Test fun orderedShabalothKillsStopAtGoonAndResumeSoberNextNight() {
        val s = session(courtier = true, minion = "Mastermind")
        val rules = ruleset()
        s.night(2)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, chosenRole = RoleId("Gossip")), s.state.gameStateRevision, rules)
        assertTrue(s.state.gameState.playerAt(5)!!.drunk)
        val first = s.state.nextBmrNightInteraction(rules)!!.id.value
        val choice = BmrNightChoice(first, listOf(1), deaths = listOf(true))
        val revision = s.state.gameStateRevision
        s.confirmBmrNightChoice(choice, revision, rules)
        s.confirmBmrNightChoice(choice, revision, rules)
        val goonAttack = s.state.nextBmrNightInteraction(rules)!!
        // The table must lock the Goon's death to "no", matching the confirmation below.
        assertTrue(6 in s.state.bmrNightSelection(goonAttack, listOf(6), true, false, null).forcedNoDeathSeats)
        s.confirmBmrNightChoice(BmrNightChoice(goonAttack.id.value, listOf(6), deaths = listOf(false)), s.state.gameStateRevision, rules)
        assertFalse(s.state.gameState.playerAt(1)!!.alive)
        assertFalse(s.state.gameState.playerAt(5)!!.drunk) // Source death ends its ongoing intoxication.
        assertTrue(s.state.gameState.playerAt(6)!!.alive)
        assertEquals(Alignment.EVIL, s.state.gameState.playerAt(6)!!.actualAlignment)
        assertTrue(s.state.gameState.playerAt(8)!!.drunk)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
        s.night(3)
        assertFalse(s.state.gameState.playerAt(8)!!.drunk)
        val regurgitate = s.state.nextBmrNightInteraction(rules)!!.id.value
        s.confirmBmrRegurgitation(regurgitate, 1, s.state.gameStateRevision, rules)
        assertTrue(s.state.gameState.playerAt(1)!!.alive)
        assertEquals("Shabaloth", s.state.nextBmrNightInteraction(rules)!!.roleId!!.value) // Courtier's slot already passed.
        val next = s.state.nextBmrNightInteraction(rules)!!.id.value
        // This time Goon is first: neither target dies; the prior night's same step ID must not block a new choice.
        s.confirmBmrNightChoice(BmrNightChoice(next, listOf(6), deaths = listOf(false)), s.state.gameStateRevision, rules)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(3), deaths = listOf(false)), s.state.gameStateRevision, rules)
        assertTrue(s.state.gameState.playerAt(3)!!.alive)
        assertTrue(s.state.gameState.playerAt(8)!!.drunk)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Death })
        s.night(4)
        val regained = s.state.nextBmrNightInteraction(rules)!!
        assertEquals("Courtier", regained.roleId!!.value)
        s.confirmBmrNightChoice(BmrNightChoice(regained.id.value, chosenRole = RoleId("Mastermind")), s.state.gameStateRevision, rules)
        assertTrue(s.state.gameState.playerAt(7)!!.drunk) // A consumed once-per-game ability is available in the new life.
    }

    @Test fun assassinChoosingTheGoonIsDrunkAndCannotKill() {
        val s = session(courtier = true)
        val rules = ruleset()
        s.night(2)
        var spareTarget = 3
        while (s.state.nextBmrNightInteraction(rules)!!.roleId!!.value != "Assassin") {
            val earlier = s.state.nextBmrNightInteraction(rules)!!
            val input = if (earlier.roleId!!.value == "Shabaloth") BmrNightChoice(earlier.id.value, listOf(spareTarget++), deaths = listOf(false))
                else BmrNightChoice(earlier.id.value, useAbility = false)
            s.confirmBmrNightChoice(input, s.state.gameStateRevision, rules)
        }
        val step = s.state.nextBmrNightInteraction(rules)!!
        assertTrue(6 in s.state.bmrNightSelection(step, listOf(6), true, false, null).forcedNoDeathSeats)
        val revision = s.state.gameStateRevision
        assertThrows(IllegalArgumentException::class.java) {
            s.confirmBmrNightChoice(BmrNightChoice(step.id.value, listOf(6), deaths = listOf(true)), revision, rules)
        }
        s.confirmBmrNightChoice(BmrNightChoice(step.id.value, listOf(6), deaths = listOf(false)), revision, rules)
        assertTrue(s.state.gameState.playerAt(6)!!.alive)
        assertTrue(s.state.gameState.playerAt(7)!!.drunk)
        assertEquals(1, s.state.bmrUses(7, "Assassin").count { it.used }) // The once-per-game ability is spent.
    }

    @Test fun courtierDrunkennessSuspendsPukkaPoisonAcrossThreeNightsAndReload() {
        var s = session("Pukka", courtier = true)
        s.confirmPukkaChoice(2, s.state.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        s.night(2)
        val rules = ruleset()
        val courtier = s.state.nextBmrNightInteraction(rules)!!
        s.confirmBmrNightChoice(BmrNightChoice(courtier.id.value, chosenRole = RoleId("Pukka")), s.state.gameStateRevision, rules)
        assertTrue(s.state.gameState.playerAt(8)!!.drunk)
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        s.confirmPukkaChoice(5, s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(5)!!.poisoned)
        assertTrue(s.state.pukkaPoisonsDue().isEmpty())
        s.night(3)
        s = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
        assertTrue(s.state.gameState.playerAt(8)!!.drunk)
        val resumedEarly = ClocktowerGameSession.restore(s.state)
        resumedEarly.confirmPukkaChoice(5, resumedEarly.state.gameStateRevision)
        val assassin = resumedEarly.state.nextBmrNightInteraction(rules)!!
        resumedEarly.confirmBmrNightChoice(BmrNightChoice(assassin.id.value, listOf(1), deaths = listOf(true)), resumedEarly.state.gameStateRevision, rules)
        assertFalse(resumedEarly.state.gameState.playerAt(8)!!.drunk)
        assertTrue(resumedEarly.state.gameState.playerAt(2)!!.poisoned)
        assertTrue(resumedEarly.state.nextBmrNightInteraction(rules)!!.id.value.startsWith("bmr:pukka-settlement:"))
        resumedEarly.confirmPukkaPoisonResolution(resumedEarly.state.pukkaPoisonsDue().single().fact.actionId, true, resumedEarly.state.gameStateRevision)
        assertFalse(resumedEarly.state.gameState.playerAt(2)!!.alive)
        assertEquals(s.state.round, resumedEarly.state.round) // Same night, without a second Pukka wake/choice.
        s.night(4)
        assertTrue(s.state.gameState.playerAt(8)!!.drunk)
        s.night(5)
        assertFalse(s.state.gameState.playerAt(8)!!.drunk)
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        assertEquals(1, s.state.pukkaPoisonsDue().size)
        assertFalse(s.state.gameState.playerAt(5)!!.poisoned)
    }

    @Test fun firstNightPublishesOnlyLastGrandmotherPreviewAndForcedChambermaidCount() {
        val names = listOf("Grandmother", "Chambermaid", "Fool", "Assassin", "Po")
        val players = names.mapIndexed { i, name ->
            val r = badMoonRisingRoles.single { it.enName == name }
            PlayerState(i + 1, "P${i + 1}", RoleId(name), if(r.team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)) Alignment.EVIL else Alignment.GOOD,
                CharacterType.valueOf(r.team.name.uppercase()))
        }
        val s = ClocktowerGameSession.createProduction("bmr-info", 51L, GameState(BadMoonRisingOpening.script, players, 51L), ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val rules = ruleset()
        s.confirmBmrGrandchild(s.state.nextBmrNightInteraction(rules)!!.id.value, 3, s.state.gameStateRevision, rules)
        val step = s.state.nextBmrNightInteraction(rules)!!
        val revision = StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision)
        val before = s.state
        s.state.bmrInformationDraft(step, com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.RoleAt(2, RoleId("Chambermaid")))
        val last = s.state.bmrInformationDraft(step, com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.RoleAt(3, RoleId("Fool")))
        assertSame(before, s.state) // Previewing is not an action, wake or disclosure fact.
        s.confirmBmrInformation(step.id.value, last, revision, rules)
        s.confirmBmrInformation(step.id.value, last, revision, rules)
        assertEquals(listOf(last.proposition), s.state.epistemicObservationLog.records.map { it.proposition })
        assertEquals(setOf(1), s.state.epistemicObservationLog.records.single().recipientSeats)
        val chambermaid = s.state.nextBmrNightInteraction(rules)!!
        assertEquals(setOf(1), s.state.bmrChambermaidValues(2, listOf(1, 3)))
        val current = StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision)
        val wrong = s.state.bmrInformationDraft(chambermaid, com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.NumericResult(
            com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 2, listOf(1, 3), 0))
        assertThrows(IllegalArgumentException::class.java) { s.confirmBmrInformation(chambermaid.id.value, wrong, current, rules) }
        assertEquals(1, s.state.epistemicObservationLog.records.size)
        val right = wrong.copy(proposition = com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.NumericResult(
            com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 2, listOf(1, 3), 1))
        s.confirmBmrInformation(chambermaid.id.value, right, current, rules)
        assertEquals(setOf(2), s.state.epistemicObservationLog.records.last().recipientSeats)
        assertNull(s.state.nextBmrNightInteraction(rules))
        s.completeBmrNight(s.state.gameStateRevision, rules)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }


    private fun ClocktowerGameSession.startDay(rules: com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset) {
        commitDemonBluffs(listOf(RoleId("Grandmother"), RoleId("Sailor"), RoleId("Courtier")), state.phase, state.round, nextInputSequence)
        repeat(2) { confirmEvilInformation(rules, state.nextBmrNightInteraction(rules)!!.id.value) }
        assertNull(state.nextBmrNightInteraction(rules))
        completeBmrNight(state.gameStateRevision, rules)
    }
    private fun ClocktowerGameSession.execute(seat: Int, dies: Boolean) {
        confirmNomination(1, seat, null)
        confirmVote(1, seat, listOf(1, 2, 4, 5))
        confirmBmrEndDay(dies, state.gameStateRevision)
    }
    private fun ClocktowerGameSession.finishPassiveNight(rules: com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset) {
        while(true) {
            val step = state.nextBmrNightInteraction(rules) ?: break
            when {
                step.id.value.startsWith("bmr:goon-notice:") -> {
                    val actor = state.gameState.playerAt(step.actorSeat!!)!!
                    confirmBmrInformation(step.id.value, state.bmrInformationDraft(step,
                        com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.AlignmentAt(actor.seat, actor.actualAlignment)),
                        StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision), rules)
                }
                step.roleId?.value == "Assassin" -> confirmBmrNightChoice(BmrNightChoice(step.id.value, useAbility = false), state.gameStateRevision, rules)
                else -> error("Unexpected mandatory night operation: ${step.id}")
            }
        }
        completeBmrNight(state.gameStateRevision, rules)
    }

    @Test fun eightPlayerPoGameChargesThenStopsTripleAttackAtGoonAndEndsAfterGhostVote() {
        val s = session("Po", minion = "Assassin", fifthTown = "Exorcist")
        val rules = ruleset()
        s.startDay(rules)
        s.execute(3, true)
        s.continueBmrAfterDay(s.state.gameStateRevision)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(7)), s.state.gameStateRevision, rules)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value), s.state.gameStateRevision, rules)
        s.finishPassiveNight(rules)
        s.confirmBmrEndDay(false, s.state.gameStateRevision)
        s.continueBmrAfterDay(s.state.gameStateRevision)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(8)), s.state.gameStateRevision, rules)
        val notice = s.state.nextBmrNightInteraction(rules)!!
        s.confirmBmrInformation(notice.id.value, s.state.bmrInformationPreview(notice,
            s.state.bmrNightSelection(notice, emptyList(), true, false, null), emptyList(), emptyList(), null, null, null)!!.let { s.state.bmrInformationDraft(notice, it) },
            StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision), rules)
        s.confirmBmrExorcisedSkip(s.state.nextBmrNightInteraction(rules)!!.id.value, s.state.gameStateRevision, rules)
        assertTrue(s.state.bmrAttackChoices(8).isEmpty()) // A skipped wake is not a new no-target choice.
        s.finishPassiveNight(rules)
        s.confirmBmrEndDay(false, s.state.gameStateRevision)
        s.continueBmrAfterDay(s.state.gameStateRevision)
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(2)), s.state.gameStateRevision, rules)
        listOf(5 to true, 6 to false, 4 to false).forEach { (seat, dies) ->
            s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(seat), deaths = listOf(dies)), s.state.gameStateRevision, rules)
        }
        assertTrue(s.state.gameState.playerAt(4)!!.alive)
        assertEquals(Alignment.EVIL, s.state.gameState.playerAt(6)!!.actualAlignment)
        s.finishPassiveNight(rules)
        s.execute(8, true) // 5 is dead and casts the ghost vote in this ballot.
        assertEquals(setOf(5), s.state.actionTimeline.spentGhostVoterSeats())
        s.confirmBmrGameEnd(true, s.state.gameStateRevision)
        assertEquals(GameEndReason.BMR_GOOD, s.state.outcome!!.reason)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }

    @Test fun eightPlayerZombuulGameRegistersFirstDeathThenMastermindUsesNonLethalGoodExecution() {
        val s = session("Zombuul")
        val rules = ruleset()
        s.startDay(rules)
        s.execute(8, true)
        assertTrue(s.state.gameState.playerAt(8)!!.alive)
        assertFalse(s.state.gameState.playerAt(8)!!.publiclyAlive)
        assertNull(s.state.bmrMastermindExtraDay())
        s.continueBmrAfterDay(s.state.gameStateRevision)
        assertNull(s.state.nextBmrNightInteraction(rules)) // Public first death today prevents the Zombuul wake.
        s.completeBmrNight(s.state.gameStateRevision, rules)
        s.execute(8, true) // A publicly dead player may be nominated again.
        assertFalse(s.state.gameState.playerAt(8)!!.alive)
        assertEquals(3, s.state.bmrMastermindExtraDay())
        assertNull(s.state.outcome)
        s.continueBmrAfterDay(s.state.gameStateRevision)
        s.completeBmrNight(s.state.gameStateRevision, rules)
        s.execute(2, false) // Fool survives, but was executed: their GOOD team loses in the extra day.
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
        s.confirmBmrGameEnd(false, s.state.gameStateRevision)
        assertEquals(GameEndReason.BMR_EVIL, s.state.outcome!!.reason)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }


    @Test fun devilsAdvocateStopsExecutionButNotTinkersLaterDaytimeDeath() {
        val s = session("Po", minion = "Devil's Advocate", outsider = "Tinker")
        val rules = ruleset()
        s.commitDemonBluffs(listOf(RoleId("Grandmother"), RoleId("Sailor"), RoleId("Courtier")), s.state.phase, s.state.round, s.nextInputSequence)
        repeat(2) { s.confirmEvilInformation(rules, s.state.nextBmrNightInteraction(rules)!!.id.value) }
        s.confirmBmrNightChoice(BmrNightChoice(s.state.nextBmrNightInteraction(rules)!!.id.value, listOf(6)), s.state.gameStateRevision, rules)
        s.completeBmrNight(s.state.gameStateRevision, rules)
        s.execute(6, false)
        assertTrue(s.state.gameState.playerAt(6)!!.alive)
        s.confirmBmrTinkerDeath(s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(6)!!.alive)
        assertFalse(s.state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.Execution }.single().causedDeath)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
    }

}
