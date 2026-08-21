package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.util.Collections

/** One seat exactly as it is physically represented in the Grimoire at the Spy wake. */
internal data class GrimoireSeatTruth(
    val seat: Int,
    val displayedRole: RoleId,
    val alive: Boolean,
) {
    init {
        require(seat > 0) { "Grimoire truth seat must be positive." }
    }
}

/**
 * A physical reminder-token placement without a caller-supplied label.
 *
 * The token is resolved through the active [ValidatedClocktowerRuleset] only when projected, so a
 * runtime caller cannot upgrade arbitrary UI text or a stale/off-script token into Spy knowledge.
 */
internal data class GrimoireReminderPlacement(
    val targetSeat: Int,
    val sourceRole: RoleId,
    val scope: GrimoireReminderTokenScope,
    val occurrence: Int,
) {
    init {
        require(targetSeat > 0) { "Grimoire reminder target seat must be positive." }
        require(occurrence > 0) { "Grimoire reminder occurrence must be positive." }
    }
}

/**
 * Explicit snapshot of the physical Grimoire truth at one Spy wake interaction.
 *
 * This deliberately does not accept [com.codex.campboardgamehost.clocktower.domain.GameState] as a
 * source of truth. Current runtime mechanical state is insufficient to reconstruct every physical
 * Grimoire detail (for example setup-only reminder placements and current displayed character
 * tokens), so callers must supply those facts explicitly until a durable production authority owns
 * them.
 */
internal class GrimoireTruthSnapshotInput(
    seats: List<GrimoireSeatTruth>,
    reminderPlacements: List<GrimoireReminderPlacement> = emptyList(),
) {
    val seats: List<GrimoireSeatTruth> = Collections.unmodifiableList(seats.toList())
    val reminderPlacements: List<GrimoireReminderPlacement> =
        Collections.unmodifiableList(reminderPlacements.toList())

    init {
        require(this.seats.isNotEmpty()) { "Grimoire truth snapshot cannot be empty." }
        require(this.seats.map(GrimoireSeatTruth::seat).distinct().size == this.seats.size) {
            "Grimoire truth seats must be unique."
        }
        require(this.seats.map(GrimoireSeatTruth::seat) == this.seats.map(GrimoireSeatTruth::seat).sorted()) {
            "Grimoire truth seats must use canonical seat order."
        }
        val seatIds = this.seats.mapTo(linkedSetOf(), GrimoireSeatTruth::seat)
        require(this.reminderPlacements.all { it.targetSeat in seatIds }) {
            "Every Grimoire reminder placement must target a seat in the snapshot."
        }
    }

    override fun equals(other: Any?): Boolean = other is GrimoireTruthSnapshotInput &&
        seats == other.seats && reminderPlacements == other.reminderPlacements

    override fun hashCode(): Int = 31 * seats.hashCode() + reminderPlacements.hashCode()

    override fun toString(): String =
        "GrimoireTruthSnapshotInput(seats=$seats, reminderPlacements=$reminderPlacements)"
}

/** Converts explicit physical Grimoire truth into the private proposition the Spy can observe. */
internal object SpyGrimoireTruthProjector {
    fun project(
        ruleset: ValidatedClocktowerRuleset,
        input: GrimoireTruthSnapshotInput,
    ): InformationProposition.GrimoireState {
        input.seats.forEach { seat ->
            require(ruleset.characterRegistry.findByRoleId(seat.displayedRole) != null) {
                "Displayed Grimoire role '${seat.displayedRole.value}' is not present in active ruleset '${ruleset.script.id.value}'."
            }
        }

        val tokensBySeat = input.reminderPlacements.groupBy(GrimoireReminderPlacement::targetSeat)
            .mapValues { (_, placements) ->
                placements.map { placement ->
                    ruleset.grimoireReminderToken(
                        sourceRole = placement.sourceRole,
                        scope = placement.scope,
                        occurrence = placement.occurrence,
                    )
                }.sorted()
            }

        return InformationProposition.GrimoireState(
            input.seats.map { seat ->
                GrimoireSeatView(
                    seat = seat.seat,
                    displayedRole = seat.displayedRole,
                    alive = seat.alive,
                    reminderTokens = tokensBySeat[seat.seat].orEmpty(),
                )
            },
        )
    }
}
