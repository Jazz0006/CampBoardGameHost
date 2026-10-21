package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Stable IDs for actual private night consequences, independent of a Dawn phase. */
internal object ConfirmedNightEffectIdentity {
    // Preserve existing on-disk IDs for any accepted facts from older game versions.
    fun death(gameId: String, round: Int, seat: Int): String =
        "dawn-${stableToken(gameId)}-$round-death-$seat"

    fun roleChange(gameId: String, round: Int, seat: Int, role: RoleId): String =
        "dawn-${stableToken(gameId)}-$round-role-$seat-${stableToken(role.value)}"

    private fun stableToken(value: String): String = buildString(value.length) {
        value.lowercase().forEach { character ->
            when {
                character.isLetterOrDigit() -> append(character)
                lastOrNull() != '-' -> append('-')
            }
        }
    }.trim('-').ifEmpty { "value" }
}
