package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Semantic identity matcher for one selected interaction-local registration witness.
 *
 * Generator-local interaction IDs and registration-question labels are provenance. The observation
 * itself supplies interaction scope, so exact matching is based on subject, special reason and the
 * registered semantic dimensions.
 */
internal object RegistrationWitnessSemanticMatcher {
    fun matches(
        selected: Set<RegistrationFact>,
        exactWitness: Set<RegistrationFact>,
        roles: Map<RoleId, RoleDefinition>,
    ): Boolean {
        if (selected.size != exactWitness.size) return false
        if (selected.isEmpty()) return true

        val unmatched = exactWitness.toMutableList()
        selected.forEach { selectedFact ->
            val matchIndex = unmatched.indexOfFirst { exactFact ->
                sameSelection(selectedFact, exactFact, roles)
            }
            if (matchIndex < 0) return false
            unmatched.removeAt(matchIndex)
        }
        return unmatched.isEmpty()
    }

    private fun sameSelection(
        selected: RegistrationFact,
        exact: RegistrationFact,
        roles: Map<RoleId, RoleDefinition>,
    ): Boolean {
        if (selected.subjectSeat != exact.subjectSeat || selected.reason != exact.reason) return false

        val selectedRole = selected.registeredRole
        val exactRole = exact.registeredRole
        if (exactRole != null && selectedRole != exactRole) return false

        val selectedType = selected.registeredType ?: selectedRole?.let(roles::get)?.type
        val exactType = exact.registeredType ?: exactRole?.let(roles::get)?.type
        if (exactType != null && selectedType != exactType) return false

        val selectedAlignment =
            selected.registeredAlignment ?: selectedRole?.let(roles::get)?.alignment
        val exactAlignment =
            exact.registeredAlignment ?: exactRole?.let(roles::get)?.alignment
        if (exactAlignment != null && selectedAlignment != exactAlignment) return false

        return true
    }
}
