package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/** Read-only answer for one current Undertaker/Ravenkeeper query, never another game authority. */
internal data class HostRoleResolution(
    val roles: List<RoleId>,
    val sourceSeat: Int,
    val subjectSeat: Int,
    val sourceAbility: AbilityFunctioningState,
) {
    fun accepts(result: InformationProposition.RoleAt): Boolean =
        result.seat == subjectSeat && result.role in roles
}

internal object RoleInformationResolver {
    /** Session has established the current interaction; Ravenkeeper may already be dead. */
    fun resolve(
        state: ClocktowerSessionState,
        ruleset: ValidatedClocktowerRuleset,
        role: String,
        sourceSeat: Int,
        subjectSeat: Int?,
    ): HostRoleResolution? {
        if (state.gameState.script != ruleset.script.id || role !in setOf("Undertaker", "Ravenkeeper")) return null
        val source = state.gameState.playerAt(sourceSeat) ?: return null
        val target = subjectSeat?.let(state.gameState::playerAt) ?: return null
        if (role == "Undertaker" && !source.alive) return null
        fun ability(actual: String, shown: String?, poisoned: Boolean, alive: Boolean, expected: String) =
            AbilityFunctioningSemantics.stateForEstablishedInteraction(AbilitySubject(actual, shown, poisoned, alive), expected)
        val functioning = ability(source.actualRole.value, source.shownRole?.value, source.poisoned, source.alive, role) ?: return null
        val targetFunctions = ability(target.actualRole.value, target.shownRole?.value, target.poisoned, target.alive,
            target.actualRole.value) == AbilityFunctioningState.FUNCTIONING
        val roles = when {
            functioning != AbilityFunctioningState.FUNCTIONING -> ruleset.characters.map { it.id }
            targetFunctions && target.actualRole.value == "Spy" -> listOf(target.actualRole) +
                ruleset.characters.filter { it.team in setOf(ClocktowerCatalogTeam.TOWNSFOLK, ClocktowerCatalogTeam.OUTSIDER) }.map { it.id }
            targetFunctions && target.actualRole.value == "Recluse" -> listOf(target.actualRole) +
                ruleset.characters.filter { it.team in setOf(ClocktowerCatalogTeam.MINION, ClocktowerCatalogTeam.DEMON) }.map { it.id }
            else -> listOf(target.actualRole)
        }
        return HostRoleResolution(roles.distinct(), sourceSeat, target.seat, functioning)
    }
}
