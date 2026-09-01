package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Rules authority for the player-visible shape of pair-information abilities.
 *
 * This answers only what statements the ability is allowed to display. Whether a statement is
 * truthful in the current game, whether special registration makes it truthful, and how strongly
 * it should be recommended are separate concerns layered on top of this domain.
 */
internal object PairInformationDisplaySemantics {
    private val washerwoman = RoleId("Washerwoman")
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")

    fun allowsZeroCharacterOutcome(abilityRole: RoleId): Boolean = abilityRole == librarian

    fun legalOutcomes(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
    ): List<PairInformationOutcome> {
        if (game.playerAt(sourceSeat) == null) return emptyList()
        val targetType = when (abilityRole) {
            washerwoman -> CharacterType.TOWNSFOLK
            librarian -> CharacterType.OUTSIDER
            investigator -> CharacterType.MINION
            else -> return emptyList()
        }
        val displayRoles = roleDefinitions
            .asSequence()
            .filter { game.script in it.scriptIds && it.type == targetType }
            .map { it.id }
            .distinct()
            .sortedBy { it.value }
            .toList()
        val seats = game.players
            .asSequence()
            .map { it.seat }
            .filter { it != sourceSeat }
            .sorted()
            .toList()
        val rolePairOutcomes = displayRoles.flatMap { shownRole ->
            unorderedPairs(seats).map { (first, second) ->
                PairInformationOutcome(
                    shownRole = shownRole,
                    targetSeat = first,
                    decoySeat = second,
                )
            }
        }
        return if (allowsZeroCharacterOutcome(abilityRole)) {
            rolePairOutcomes + PairInformationOutcome(
                shownRole = null,
                targetSeat = null,
                decoySeat = null,
            )
        } else {
            rolePairOutcomes
        }
    }

    private fun unorderedPairs(seats: List<Int>): List<Pair<Int, Int>> = buildList {
        for (firstIndex in 0 until seats.lastIndex) {
            for (secondIndex in firstIndex + 1 until seats.size) {
                add(seats[firstIndex] to seats[secondIndex])
            }
        }
    }
}
