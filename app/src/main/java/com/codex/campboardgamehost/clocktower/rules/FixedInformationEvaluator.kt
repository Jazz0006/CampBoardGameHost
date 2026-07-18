package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.PlayerState

internal object FixedInformationEvaluator {
    fun chefEvilPairs(
        players: List<PlayerState>,
        registersAsEvil: (PlayerState) -> Boolean = { it.actualAlignment == Alignment.EVIL },
    ): Int {
        val seatedPlayers = players.sortedBy(PlayerState::seat)
        if (seatedPlayers.size < 2) return 0
        return seatedPlayers.indices.count { index ->
            val next = seatedPlayers[(index + 1) % seatedPlayers.size]
            registersAsEvil(seatedPlayers[index]) && registersAsEvil(next)
        }
    }

    fun livingNeighbors(players: List<PlayerState>, sourceSeat: Int): List<PlayerState> {
        val alivePlayers = players.filter(PlayerState::alive).sortedBy(PlayerState::seat)
        if (alivePlayers.size <= 1) return emptyList()
        val sourceIndex = alivePlayers.indexOfFirst { it.seat == sourceSeat }
        if (sourceIndex < 0) return emptyList()
        val left = alivePlayers[(sourceIndex - 1 + alivePlayers.size) % alivePlayers.size]
        val right = alivePlayers[(sourceIndex + 1) % alivePlayers.size]
        return listOf(left, right).distinctBy(PlayerState::seat)
    }

    fun empathEvilNeighborCount(
        players: List<PlayerState>,
        sourceSeat: Int,
        registersAsEvil: (PlayerState) -> Boolean = { it.actualAlignment == Alignment.EVIL },
    ): Int = livingNeighbors(players, sourceSeat).count(registersAsEvil)
}
