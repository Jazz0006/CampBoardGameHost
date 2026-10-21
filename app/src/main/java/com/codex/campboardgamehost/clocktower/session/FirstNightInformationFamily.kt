package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Supported first-night information publication families, not recommendation policies. */
internal enum class FirstNightInformationFamily(val role: RoleId) {
    WASHERWOMAN(RoleId("Washerwoman")),
    LIBRARIAN(RoleId("Librarian")),
    INVESTIGATOR(RoleId("Investigator")),
    CHEF(RoleId("Chef")),
    EMPATH(RoleId("Empath")),
    FORTUNE_TELLER(RoleId("Fortune Teller")),
}

private val authoritativePairInformationFamilies = setOf(
    FirstNightInformationFamily.WASHERWOMAN,
    FirstNightInformationFamily.LIBRARIAN,
    FirstNightInformationFamily.INVESTIGATOR,
)

internal fun FirstNightInformationFamily.usesAuthoritativePairDomain(): Boolean =
    this in authoritativePairInformationFamilies
