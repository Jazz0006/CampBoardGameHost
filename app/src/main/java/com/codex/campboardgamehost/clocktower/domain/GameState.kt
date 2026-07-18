package com.codex.campboardgamehost.clocktower.domain

@JvmInline
value class RoleId(val value: String) {
    init {
        require(value.isNotBlank()) { "RoleId cannot be blank." }
    }
}

@JvmInline
value class ScriptId(val value: String) {
    init {
        require(value.isNotBlank()) { "ScriptId cannot be blank." }
    }
}

enum class Alignment {
    GOOD,
    EVIL,
}

enum class CharacterType {
    TOWNSFOLK,
    OUTSIDER,
    MINION,
    DEMON,
}

data class PlayerState(
    val seat: Int,
    val name: String,
    val actualRole: RoleId,
    val actualAlignment: Alignment,
    val actualType: CharacterType,
    val shownRole: RoleId? = null,
    val alive: Boolean = true,
    val poisoned: Boolean = false,
) {
    init {
        require(seat > 0) { "Seat numbers start at 1." }
    }
}

data class RoleDefinition(
    val id: RoleId,
    val alignment: Alignment,
    val type: CharacterType,
    val scriptIds: Set<ScriptId>,
)

data class GameState(
    val script: ScriptId,
    val players: List<PlayerState>,
    val seed: Long,
) {
    init {
        require(players.map(PlayerState::seat).distinct().size == players.size) {
            "Each player must have a unique seat."
        }
    }

    fun playerAt(seat: Int): PlayerState? = players.firstOrNull { it.seat == seat }
}
