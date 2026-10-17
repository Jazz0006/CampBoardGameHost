package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Stable identities for repeated confirmation of the same resolved Dawn effect. */
internal object DawnCommitIdentity {
    fun death(gameId: String, round: Int, seat: Int): String =
        "dawn-${stableToken(gameId)}-$round-death-$seat"

    fun deathObservation(gameId: String, round: Int, seat: Int): String =
        "public-alive-dawn-${stableToken(gameId)}-$round-$seat"

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
