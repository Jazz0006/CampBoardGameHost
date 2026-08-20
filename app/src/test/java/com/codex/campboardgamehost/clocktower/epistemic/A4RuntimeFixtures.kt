package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal object A4RuntimeFixtures {
    fun snapshot(): GameSnapshot {
        val roles = listOf("Chef", "Empath", "Washerwoman", "Poisoner", "Imp")
        return GameSnapshot(
            gameId = "runtime-game", gameStateRevision = 0, playerInputRevision = 0, gameSeed = 7L,
            rulesetRef = RulesetRef(TroubleBrewingFixtures.scriptId, "0123456789abcdef0123456789abcdef", "runtime", "official", RuleCoverage.VERIFIED),
            gameState = GameState(TroubleBrewingFixtures.scriptId, roles.mapIndexed { index, role ->
                PlayerState(index + 1, "P${index + 1}", RoleId(role),
                    if (role in setOf("Poisoner", "Imp")) Alignment.EVIL else Alignment.GOOD,
                    when (role) { "Poisoner" -> CharacterType.MINION; "Imp" -> CharacterType.DEMON; else -> CharacterType.TOWNSFOLK })
            }, 7L),
        )
    }
}
