package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome

/**
 * Knowledge-safe materialization boundary for one possible world's rule-derived Other Night
 * protection and Imp-attack alternatives.
 *
 * Resolved direct outcomes, Imp self-kill succession, and Mayor night-death alternatives are
 * converted into mechanical worlds and converged without counting hidden choice provenance as
 * distinct worlds.
 *
 * This helper consumes no Storyteller-selected Protect, Attack, Mayor resolution, or RoleChange
 * target.
 */
internal data class EnumeratedWorldOtherNightMechanicsMaterializationResult(
    val resolvedWorlds: List<EnumeratedWorld>,
    val unresolvedBranches: List<EnumeratedWorldOtherNightAttackBranch>,
)

internal object EnumeratedWorldOtherNightMechanicsMaterializer {
    fun materialize(world: EnumeratedWorld): EnumeratedWorldOtherNightMechanicsMaterializationResult {
        val resolvedWorlds = mutableListOf<EnumeratedWorld>()
        val unresolvedBranches = mutableListOf<EnumeratedWorldOtherNightAttackBranch>()

        EnumeratedWorldOtherNightProtectionBranching.branches(world)
            .flatMap(EnumeratedWorldOtherNightAttackBranching::branches)
            .forEach { branch ->
                when (branch.outcome) {
                    DemonNightAttackOutcome.NO_DEATH -> {
                        resolvedWorlds += branch.protectionBranch.world
                    }
                    DemonNightAttackOutcome.TARGET_DIES -> {
                        val targetSeat = requireNotNull(branch.possibleAttackTargetSeat) {
                            "A direct Demon death branch must identify its target seat."
                        }
                        resolvedWorlds += materializeDeath(
                            world = branch.protectionBranch.world,
                            targetSeat = targetSeat,
                        )
                    }
                    DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED -> {
                        resolvedWorlds += EnumeratedWorldImpSelfKillSuccessionBranching
                            .branches(branch.protectionBranch.world)
                            .map { it.world }
                    }
                    DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED -> {
                        resolvedWorlds += EnumeratedWorldMayorNightDeathBranching
                            .branches(branch)
                            .map { it.world }
                    }
                }
            }

        return EnumeratedWorldOtherNightMechanicsMaterializationResult(
            resolvedWorlds = EnumeratedWorldMechanicalConvergence.converge(resolvedWorlds),
            unresolvedBranches = unresolvedBranches,
        )
    }

    /** Keep direct hidden-death state evolution aligned with the historical public-death reducer. */
    private fun materializeDeath(
        world: EnumeratedWorld,
        targetSeat: Int,
    ): EnumeratedWorld {
        require(targetSeat in world.rolesBySeat) {
            "Other-night attack materialization references unknown seat $targetSeat."
        }
        val abilityStates = if (
            world.currentRolesBySeat.getValue(targetSeat).value.equals("Poisoner", ignoreCase = true)
        ) {
            world.abilityStatesBySeat.filterValues { it != AbilityState.MALFUNCTIONING_POISONED }
        } else {
            world.abilityStatesBySeat
        }
        return world.copy(
            aliveSeats = world.aliveSeats - targetSeat,
            abilityStatesBySeat = abilityStates,
        )
    }
}
