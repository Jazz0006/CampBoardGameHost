package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Surface-valid numeric display range for a perceived first-night information ability. */
internal data class FirstNightNumericDisplayDomain(
    val abilityRole: RoleId,
    val minimumValue: Int,
    val maximumValue: Int,
) {
    init {
        require(minimumValue >= 0)
        require(maximumValue >= minimumValue)
    }

    val values: IntRange
        get() = minimumValue..maximumValue
}

/**
 * Rules-owned display domain for Trouble Brewing's first-night numeric information.
 *
 * This deliberately preserves the production Host range that existed before SDE-2D1:
 * - Empath may display 0..2.
 * - Chef may display 0 through the larger of the current healthy truth space or the current
 *   evil-identity count plus the existing Recluse allowance.
 *
 * Truth and reliability are separate concerns. [FirstNightNumericInformationSemantics] owns healthy
 * truth; recommendation decides whether a malfunctioning source may use false values in this domain.
 */
internal object FirstNightNumericInformationDisplayDomain {
    private val chef = RoleId("Chef")
    private val empath = RoleId("Empath")
    private val recluse = RoleId("Recluse")

    fun domain(
        game: GameState,
        sourceSeat: Int,
    ): FirstNightNumericDisplayDomain? {
        val source = game.playerAt(sourceSeat) ?: return null
        if (!source.alive) return null
        val perceivedRole = AbilityFunctioningSemantics.perceivedRole(
            AbilitySubject(
                actualRole = source.actualRole.value,
                shownRole = source.shownRole?.value,
                isPoisoned = source.poisoned,
                isAlive = source.alive,
            ),
        )?.let(::RoleId) ?: return null

        return when (perceivedRole) {
            empath -> FirstNightNumericDisplayDomain(
                abilityRole = empath,
                minimumValue = 0,
                maximumValue = 2,
            )

            chef -> {
                val healthyTruthMaximum = FirstNightNumericInformationSemantics
                    .healthyTruthValues(game, sourceSeat)
                    .maxOrNull()
                    ?: 0
                val hostCompatibleMaximum =
                    game.players.count { it.actualAlignment == Alignment.EVIL } +
                        if (game.players.any { it.actualRole == recluse }) 1 else 0
                FirstNightNumericDisplayDomain(
                    abilityRole = chef,
                    minimumValue = 0,
                    maximumValue = maxOf(healthyTruthMaximum, hostCompatibleMaximum),
                )
            }

            else -> null
        }
    }
}
