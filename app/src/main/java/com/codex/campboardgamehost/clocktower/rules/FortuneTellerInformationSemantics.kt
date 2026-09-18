package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Rules authority for Fortune Teller target legality and the mechanically correct healthy result.
 *
 * Target legality follows the source's perceived role so a Drunk shown Fortune Teller can make the
 * same player-controlled selection. [healthyResult] remains the functioning-ability truth owner;
 * malfunctioning result policy stays outside this rules helper.
 */
internal object FortuneTellerInformationSemantics {
    private val fortuneTeller = RoleId("Fortune Teller")

    fun legalTargetPairs(game: GameState): List<Pair<Int, Int>> {
        val source = game.players.firstOrNull { it.alive && it.actualRole == fortuneTeller }
            ?: return emptyList()
        return legalTargetPairs(game, source.seat)
    }

    fun legalTargetPairs(
        game: GameState,
        sourceSeat: Int,
    ): List<Pair<Int, Int>> {
        val source = game.playerAt(sourceSeat) ?: return emptyList()
        if (!source.alive) return emptyList()
        val perceivedRole = AbilityFunctioningSemantics.perceivedRole(
            AbilitySubject(
                actualRole = source.actualRole.value,
                shownRole = source.shownRole?.value,
                isPoisoned = source.poisoned,
                isAlive = source.alive,
            ),
        )?.let(::RoleId)
        if (perceivedRole != fortuneTeller) return emptyList()

        val seats = game.players.map { it.seat }.sorted()
        return buildList {
            for (firstIndex in 0 until seats.lastIndex) {
                for (secondIndex in firstIndex + 1 until seats.size) {
                    add(seats[firstIndex] to seats[secondIndex])
                }
            }
        }
    }

    fun healthyResult(
        game: GameState,
        targetSeats: Collection<Int>,
        redHerringSeat: Int,
    ): Boolean {
        require(game.players.any { it.alive && it.actualRole == fortuneTeller }) {
            "Healthy Fortune Teller result requires a living Fortune Teller in play."
        }
        val canonicalTargets = targetSeats.toSortedSet()
        require(canonicalTargets.size == 2) {
            "Fortune Teller must inspect exactly two distinct players."
        }
        require(canonicalTargets.all { game.playerAt(it) != null }) {
            "Fortune Teller targets must reference players in the current game state."
        }
        val redHerring = requireNotNull(game.playerAt(redHerringSeat)) {
            "Fortune Teller red herring must reference a player in the current game state."
        }
        require(redHerring.actualAlignment == Alignment.GOOD) {
            "Healthy Fortune Teller red herring must be an actual-good player."
        }

        return canonicalTargets.any { seat ->
            seat == redHerringSeat || game.playerAt(seat)?.actualType == CharacterType.DEMON
        }
    }
}
