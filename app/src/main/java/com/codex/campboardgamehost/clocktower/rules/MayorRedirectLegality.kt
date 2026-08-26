package com.codex.campboardgamehost.clocktower.rules

/**
 * Current product restriction for Trouble Brewing automatic hosting.
 *
 * Official Mayor rules can redirect a night death to another player, including a Demon. The app
 * intentionally excludes Demon targets so the current Trouble Brewing host does not create a
 * non-self Demon-death succession path before generic cross-script Demon succession is supported.
 */
internal object MayorRedirectLegality {
    fun canReceiveRedirect(targetIsDemon: Boolean): Boolean = !targetIsDemon
}
