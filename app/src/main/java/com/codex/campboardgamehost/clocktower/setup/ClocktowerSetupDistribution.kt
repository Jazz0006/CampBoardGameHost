package com.codex.campboardgamehost.clocktower.setup

/** Canonical base role distribution before script-specific setup modifiers are applied. */
internal data class ClocktowerSetupDistribution(
    val townsfolk: Int,
    val outsiders: Int,
    val minions: Int,
    val demons: Int,
)

internal fun clocktowerSetupDistribution(playerCount: Int): ClocktowerSetupDistribution = when (playerCount) {
    5 -> ClocktowerSetupDistribution(3, 0, 1, 1)
    6 -> ClocktowerSetupDistribution(3, 1, 1, 1)
    7 -> ClocktowerSetupDistribution(5, 0, 1, 1)
    8 -> ClocktowerSetupDistribution(5, 1, 1, 1)
    9 -> ClocktowerSetupDistribution(5, 2, 1, 1)
    10 -> ClocktowerSetupDistribution(7, 0, 2, 1)
    11 -> ClocktowerSetupDistribution(7, 1, 2, 1)
    12 -> ClocktowerSetupDistribution(7, 2, 2, 1)
    13 -> ClocktowerSetupDistribution(9, 0, 3, 1)
    14 -> ClocktowerSetupDistribution(9, 1, 3, 1)
    15 -> ClocktowerSetupDistribution(9, 2, 3, 1)
    else -> error("Unsupported Clocktower player count: $playerCount")
}
