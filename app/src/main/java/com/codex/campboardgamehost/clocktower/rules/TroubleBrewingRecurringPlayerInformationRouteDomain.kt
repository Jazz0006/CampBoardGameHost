package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Factual capability of one perceived Trouble Brewing role to produce player-facing information
 * again after the first night.
 *
 * This is rules metadata only. It does not imply that preserving the route is good, bad, stronger,
 * weaker, or preferred in any Storyteller decision.
 */
internal enum class PlayerInformationRouteCapability {
    RECURRING_OTHER_NIGHT_INFORMATION,
}

/**
 * Trouble Brewing rules-owned classifier for recurring player-information routes.
 *
 * The set is deliberately bounded to roles whose shown ability can provide new information on
 * multiple later nights. First-night-only information roles and one-shot/conditional information
 * roles are intentionally excluded. Recommendation policy must consume the typed capability rather
 * than branch on role names.
 */
internal object TroubleBrewingRecurringPlayerInformationRouteDomain {
    private val recurringInformationRoles = setOf(
        RoleId("Empath"),
        RoleId("Fortune Teller"),
        RoleId("Undertaker"),
    )

    fun capabilityFor(perceivedRole: RoleId?): PlayerInformationRouteCapability? =
        perceivedRole
            ?.takeIf(recurringInformationRoles::contains)
            ?.let { PlayerInformationRouteCapability.RECURRING_OTHER_NIGHT_INFORMATION }
}
