package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Exact strategic quotient identity for the immutable setup evil topology.
 *
 * This deliberately ignores good-role assignments, shown roles, ability state, registrations,
 * reminders and historical current-role transitions. Those remain mechanical witness dimensions.
 */
internal data class StrategicWorldKey(
    val demonSeat: Int,
    val minionSeats: List<Int>,
) {
    init {
        require(demonSeat > 0) { "Strategic Demon seat must be positive." }
        require(minionSeats.all { it > 0 }) { "Strategic Minion seats must be positive." }
        require(minionSeats == minionSeats.sorted()) {
            "Strategic Minion seats must use canonical sorted order."
        }
        require(minionSeats.distinct().size == minionSeats.size) {
            "Strategic Minion seats must be distinct."
        }
        require(demonSeat !in minionSeats) {
            "Strategic Demon seat cannot also be a Minion seat."
        }
    }

    companion object {
        fun from(
            world: EnumeratedWorld,
            roles: Map<RoleId, RoleDefinition>,
        ): StrategicWorldKey {
            require(world.rolesBySeat.values.all(roles::containsKey)) {
                "Strategic world projection requires definitions for every setup role."
            }
            val demonSeats = world.rolesBySeat
                .filterValues { role -> roles.getValue(role).type == CharacterType.DEMON }
                .keys
            require(demonSeats.size == 1) {
                "Strategic world projection requires exactly one setup Demon."
            }
            val minionSeats = world.rolesBySeat
                .filterValues { role -> roles.getValue(role).type == CharacterType.MINION }
                .keys
                .sorted()
            return StrategicWorldKey(
                demonSeat = demonSeats.single(),
                minionSeats = minionSeats,
            )
        }
    }
}
