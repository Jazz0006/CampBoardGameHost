package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/**
 * One knowledge-safe mechanical successor of a functioning Imp self-kill.
 *
 * [successorSeat] is a rule-derived branch variable, never the Storyteller's persisted RoleChange
 * target. A null successor means no living current Minion exists after the Imp dies.
 */
internal data class EnumeratedWorldImpSelfKillSuccessionBranch(
    val successorSeat: Int?,
    val world: EnumeratedWorld,
)

/** Trouble Brewing Imp self-kill succession before historical-replay wiring. */
internal object EnumeratedWorldImpSelfKillSuccessionBranching {
    private val IMP = RoleId("Imp")
    private val SCARLET_WOMAN = RoleId("Scarlet Woman")
    private val MINION_ROLES = setOf(
        RoleId("Poisoner"),
        RoleId("Spy"),
        SCARLET_WOMAN,
        RoleId("Baron"),
    )

    fun branches(world: EnumeratedWorld): List<EnumeratedWorldImpSelfKillSuccessionBranch> {
        val impSeat = requireNotNull(
            world.currentRolesBySeat.entries.singleOrNull { (seat, role) ->
                seat in world.aliveSeats && role == IMP
            }?.key,
        ) { "Imp self-kill succession requires exactly one living current Imp." }
        require(AbilityFunctioningSemantics.functionsAs(subject(world, impSeat), "Imp")) {
            "Imp self-kill succession requires a functioning current Imp."
        }

        val deadImpWorld = world.copy(aliveSeats = world.aliveSeats - impSeat)
        val forcedScarletWomanSeat = if (world.aliveSeats.size >= 5) {
            world.currentRolesBySeat.entries.singleOrNull { (seat, role) ->
                seat in world.aliveSeats &&
                    role == SCARLET_WOMAN &&
                    AbilityFunctioningSemantics.functionsAs(subject(world, seat), "Scarlet Woman")
            }?.key
        } else {
            null
        }

        val successorSeats = if (forcedScarletWomanSeat != null) {
            listOf(forcedScarletWomanSeat)
        } else {
            world.currentRolesBySeat.entries
                .filter { (seat, role) ->
                    seat != impSeat && seat in world.aliveSeats && role in MINION_ROLES
                }
                .map { it.key }
        }

        if (successorSeats.isEmpty()) {
            return listOf(
                EnumeratedWorldImpSelfKillSuccessionBranch(
                    successorSeat = null,
                    world = deadImpWorld,
                ),
            )
        }

        return successorSeats.map { successorSeat ->
            EnumeratedWorldImpSelfKillSuccessionBranch(
                successorSeat = successorSeat,
                world = deadImpWorld.withCurrentRoles(
                    deadImpWorld.currentRolesBySeat + (successorSeat to IMP),
                ),
            )
        }
    }

    private fun subject(world: EnumeratedWorld, seat: Int) = AbilitySubject(
        actualRole = world.currentRolesBySeat.getValue(seat).value,
        shownRole = world.shownRolesBySeat[seat]?.value,
        isPoisoned = world.abilityStatesBySeat[seat] == AbilityState.MALFUNCTIONING_POISONED,
        isAlive = seat in world.aliveSeats,
    )
}
