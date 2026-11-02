package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.session.*

/** Disposable UI intents; these are never a second game object or persisted navigation state. */
internal sealed interface BmrHostAction {
    data class Choice(val input: BmrNightChoice) : BmrHostAction
    data class Information(val stepId: String, val observation: EpistemicObservationDraft) : BmrHostAction
    data class EvilInformation(val stepId: String) : BmrHostAction
    data class Grandchild(val stepId: String, val target: Int) : BmrHostAction
    data class PukkaChoice(val target: Int) : BmrHostAction
    data class PukkaSettlement(val choiceId: String, val dies: Boolean, val grandmotherDies: Boolean?) : BmrHostAction
    data class Regurgitate(val stepId: String, val target: Int?) : BmrHostAction
    data class Passive(val stepId: String, val target: Int?, val dies: Boolean) : BmrHostAction
    data class Exorcised(val stepId: String) : BmrHostAction
    data class Execution(val dies: Boolean, val pacifistSaves: Boolean) : BmrHostAction
    data class Moonchild(val deathId: String, val target: Int) : BmrHostAction
    data class Gossip(val claimant: Int, val statement: String, val truthful: Boolean) : BmrHostAction
    data class DrunkActivity(val effectId: String, val active: Boolean) : BmrHostAction
    data class End(val goodWins: Boolean) : BmrHostAction
    data object Dawn : BmrHostAction
    data object ContinueDay : BmrHostAction
    data object Tinker : BmrHostAction
}

internal fun ClocktowerGameSession.confirmBmrHostAction(action: BmrHostAction, revision: StorytellerDecisionRevision,
    ruleset: ValidatedClocktowerRuleset) {
    when(action) {
        is BmrHostAction.Choice -> confirmBmrNightChoice(action.input, revision.gameStateRevision, ruleset)
        is BmrHostAction.Information -> confirmBmrInformation(action.stepId, action.observation, revision, ruleset)
        is BmrHostAction.EvilInformation -> confirmEvilInformation(ruleset, action.stepId, revision)
        is BmrHostAction.Grandchild -> confirmBmrGrandchild(action.stepId, action.target, revision.gameStateRevision, ruleset)
        is BmrHostAction.PukkaChoice -> confirmPukkaChoice(action.target, revision.gameStateRevision)
        is BmrHostAction.PukkaSettlement -> confirmPukkaPoisonResolution(action.choiceId, action.dies, revision.gameStateRevision, action.grandmotherDies)
        is BmrHostAction.Regurgitate -> confirmBmrRegurgitation(action.stepId, action.target, revision.gameStateRevision, ruleset)
        is BmrHostAction.Passive -> confirmBmrNightEvent(action.stepId, action.target, action.dies, revision.gameStateRevision, ruleset)
        is BmrHostAction.Exorcised -> confirmBmrExorcisedSkip(action.stepId, revision.gameStateRevision, ruleset)
        is BmrHostAction.Execution -> confirmBmrEndDay(action.dies, revision.gameStateRevision, action.pacifistSaves)
        is BmrHostAction.Moonchild -> confirmBmrMoonchildChoice(action.deathId, action.target, revision.gameStateRevision)
        is BmrHostAction.Gossip -> confirmBmrGossip(action.claimant, action.statement, action.truthful, revision.gameStateRevision)
        is BmrHostAction.DrunkActivity -> confirmBmrDrunkActivity(action.effectId, action.active, revision.gameStateRevision)
        is BmrHostAction.End -> confirmBmrGameEnd(action.goodWins, revision.gameStateRevision)
        BmrHostAction.Dawn -> completeBmrNight(revision.gameStateRevision, ruleset)
        BmrHostAction.ContinueDay -> continueBmrAfterDay(revision.gameStateRevision)
        BmrHostAction.Tinker -> confirmBmrTinkerDeath(revision.gameStateRevision)
    }
}
