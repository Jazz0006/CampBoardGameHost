package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.rules.*
import com.codex.campboardgamehost.clocktower.session.*

/** Read-only seat/ability presentation. Scheduling and confirmation belong to Session. */
internal class ClocktowerNightHostProjection(
    private val cards: List<PlayerCard>,
    private val state: ClocktowerSessionState,
) {
    private val game get() = state.gameState
    private val deaths: List<TimelineBoundActionFact> get() = state.actionTimeline.entries.filter {
        it.point.phase == state.phase && it.point.round == state.round && it.fact is ActionFact.Death
    }
    private fun deadActor(role: String): PlayerCard? = deaths.firstNotNullOfOrNull { entry ->
        val player = game.playerAt((entry.fact as ActionFact.Death).targetSeat)
        player?.takeIf { it.wakingRole() == RoleId(role) }?.let { cards.getOrNull(it.seat - 1) }
    }
    val mayorTarget get() = state.mayorRulingSeat()?.let { cards.getOrNull(it - 1) }
    val mayorRedirectTargetCards get() = cards.filter { it.name != mayorTarget?.name }
    val ravenkeeperTrigger get() = deadActor("Ravenkeeper")
    val sageNightDeath get() = deadActor("Sage")
    val demonCard get() = game.players.singleOrNull { it.alive && it.actualRole.value == "Imp" }?.let { cards.getOrNull(it.seat - 1) }
    val demonPoisonedForActionExplanation get() = game.players.any { it.alive && it.actualRole.value == "Imp" && it.poisoned }
    val chambermaidTargetCards get() = cards.filterIndexed { index, _ -> game.playerAt(index + 1)?.alive == true }
    val ravenkeeperDeathTriggerAbilityState get() = triggerState("Ravenkeeper", ravenkeeperTrigger)
    val sageDeathTriggerAbilityState get() = triggerState("Sage", sageNightDeath)

    private fun triggerState(role: String, actor: PlayerCard?): AbilityFunctioningState? =
        effectiveAbilitySubjectForRole(role, actor)?.let { AbilityFunctioningSemantics.stateForEstablishedInteraction(it, role) }

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? {
        val player = actor?.name?.let { name -> game.players.firstOrNull { it.name == name } } ?: return null
        return AbilitySubject(player.actualRole.value, player.shownRole?.value,
            effectivePoisonForRole(enName) == player.name, player.alive)
    }

    fun effectivePoisonForRole(enName: String): String? {
        val player = game.players.firstOrNull { it.wakingRole() == RoleId(enName) } ?: return null
        if (enName in setOf("Ravenkeeper", "Sage") && !player.alive) {
            val death = deaths.firstOrNull { (it.fact as ActionFact.Death).targetSeat == player.seat } ?: return null
            val poison = state.actionTimeline.entries.lastOrNull {
                it.point.globalSequence < death.point.globalSequence && it.fact is ActionFact.Poison
            }?.fact as? ActionFact.Poison
            return player.name.takeIf { poison?.targetSeat == player.seat }
        }
        return player.name.takeIf { player.poisoned }
    }
}
