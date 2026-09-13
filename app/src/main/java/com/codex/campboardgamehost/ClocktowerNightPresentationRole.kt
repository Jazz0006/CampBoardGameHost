package com.codex.campboardgamehost

/**
 * Resolves the role whose night information surface the Storyteller should present.
 *
 * A Drunk remains Drunk for rules/state ownership, but their night presentation follows the
 * Townsfolk character they believe they are. Other roles keep the step's authoritative role key.
 */
internal fun clocktowerNightPresentationRoleEnName(
    stepRoleEnName: String?,
    actor: PlayerCard?,
): String? {
    val actualRoleEnName = actor?.clocktowerRole?.enName
    val shownRoleEnName = actor?.clocktowerShownRole?.enName
    return if (actualRoleEnName == "Drunk" && !shownRoleEnName.isNullOrBlank()) {
        shownRoleEnName
    } else {
        stepRoleEnName
    }
}
