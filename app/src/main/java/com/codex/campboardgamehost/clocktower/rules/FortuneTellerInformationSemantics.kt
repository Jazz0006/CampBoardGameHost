package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Rules authority for healthy Fortune Teller target legality and the mechanically correct result.
 *
 * This stage intentionally models the ordinary healthy case only. Recluse registration and
 * malfunctioning information remain separate staged concerns and must not be silently folded into
 * this helper.
 */
internal object FortuneTellerInformationSemantics {
    private val fortuneTeller = RoleId("Fortune Teller")

    fun legalTargetPairs(game: GameState): List<Pair<Int, Int>> {
        if (game.players.none { it.alive && it.actualRole == fortuneTeller }) return emptyList()
        val seats = game.players.filter { it.alive }.map { it.seat }.sorted()
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
        require(canonicalTargets.all { game.playerAt(it)?.alive == true }) {
            "Fortune Teller targets must be living players in the current game state."
        }
        val redHerring = requireNotNull(game.playerAt(redHerringSeat)) {
            "Fortune Teller red herring must reference a player in the current game state."
        }
        require(redHerring.alive && redHerring.actualAlignment == Alignment.GOOD) {
            "Healthy Fortune Teller red herring must be a living actual-good player."
        }

        return canonicalTargets.any { seat ->
            seat == redHerringSeat || game.playerAt(seat)?.actualType == CharacterType.DEMON
        }
    }
}
