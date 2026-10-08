package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

/**
 * No Greater Joy Klutz: the real player chooses one alive target when learning
 * they died. This is mechanical player action, NOT Storyteller registration.
 *
 * The current, exact NGJ role roster has no Poisoner/ability poisoning source,
 * and a true Klutz is not the Drunk. Do NOT apply this resolver to future/custom
 * scripts without authoritative "when learned" functioning provenance.
 */
internal object NoGreaterJoyKlutzChoiceRuleV1 {
    private val script = ScriptId("no_greater_joy")

    data class Result(
        val klutzSeat: Int,
        val chosenSeat: Int,
        val chosenActualAlignment: Alignment,
    ) {
        val evilWins: Boolean get() = chosenActualAlignment == Alignment.EVIL
    }

    fun resolve(
        gameState: GameState,
        klutzSeat: Int,
        chosenSeat: Int,
        scriptRoles: List<RoleDefinition>,
    ): Result {
        require(gameState.script == script) {
            "No Greater Joy Klutz mechanics cannot be applied to other scripts."
        }
        require(scriptRoles.isNotEmpty() && scriptRoles.all { script in it.scriptIds })
        val byId = scriptRoles.associateBy(RoleDefinition::id)
        require(byId.size == scriptRoles.size)
        require(RoleId("Klutz") in byId && RoleId("Spy") !in byId &&
            RoleId("Poisoner") !in byId) {
            "Supported Klutz rule requires its exact script with no Spy or Poisoner."
        }
        require(gameState.players.all { p ->
            byId[p.actualRole]?.let { it.alignment == p.actualAlignment && it.type == p.actualType } == true
        }) {
            "Actual player roles must be part of the supported script roster."
        }
        require(klutzSeat != chosenSeat)
        val klutz = requireNotNull(gameState.playerAt(klutzSeat))
        val chosen = requireNotNull(gameState.playerAt(chosenSeat))
        require(klutz.actualRole == RoleId("Klutz") && !klutz.alive) {
            "Klutz must be the actual dead character whose death has been learned."
        }
        require(chosen.alive) {
            "The Klutz must publicly choose a living player."
        }
        return Result(klutzSeat, chosenSeat, chosen.actualAlignment)
    }
}
