package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Healthy first-night truth space for fixed numeric information abilities.
 *
 * Reliability belongs above this layer: a healthy, poisoned, or Drunk source that
 * perceives the same ability must start from the same legal role semantics. Spy and
 * Recluse registration are therefore represented as alternate healthy truths rather
 * than being collapsed into raw actual identity when the information ability malfunctions.
 */
internal object FirstNightNumericInformationSemantics {
    private val chef = RoleId("Chef")
    private val empath = RoleId("Empath")
    private val spy = RoleId("Spy")
    private val recluse = RoleId("Recluse")

    fun healthyTruthValues(game: GameState, sourceSeat: Int): Set<Int> {
        val source = game.playerAt(sourceSeat) ?: return emptySet()
        if (!source.alive) return emptySet()
        val perceivedRole = AbilityFunctioningSemantics.perceivedRole(source.abilitySubject())
            ?.let(::RoleId)
            ?: return emptySet()

        val registrationSubjects = when (perceivedRole) {
            chef -> game.players
            empath -> FixedInformationEvaluator.livingNeighbors(game.players, sourceSeat)
            else -> return emptySet()
        }
        val registrations = evilRegistrationAssignments(registrationSubjects)

        return registrations.mapTo(sortedSetOf()) { registersAsEvilBySeat ->
            when (perceivedRole) {
                chef -> FixedInformationEvaluator.chefEvilPairs(game.players) { player ->
                    registersAsEvilBySeat[player.seat] ?: player.actualAlignment == Alignment.EVIL
                }
                empath -> FixedInformationEvaluator.empathEvilNeighborCount(game.players, sourceSeat) { player ->
                    registersAsEvilBySeat[player.seat] ?: player.actualAlignment == Alignment.EVIL
                }
                else -> error("Unsupported first-night numeric information role: ${perceivedRole.value}")
            }
        }
    }

    private fun PlayerState.abilitySubject() = AbilitySubject(
        actualRole = actualRole.value,
        shownRole = shownRole?.value,
        isPoisoned = poisoned,
        isAlive = alive,
    )

    private fun evilRegistrationAssignments(players: List<PlayerState>): List<Map<Int, Boolean>> =
        players.sortedBy(PlayerState::seat).fold(listOf(emptyMap())) { assignments, player ->
            assignments.flatMap { assignment ->
                evilRegistrationOptions(player).map { registersAsEvil ->
                    assignment + (player.seat to registersAsEvil)
                }
            }
        }

    private fun evilRegistrationOptions(player: PlayerState): Set<Boolean> {
        val actualEvil = player.actualAlignment == Alignment.EVIL
        if (player.poisoned) return setOf(actualEvil)
        return when (player.actualRole) {
            spy -> setOf(true, false)
            recluse -> setOf(false, true)
            else -> setOf(actualEvil)
        }
    }
}
