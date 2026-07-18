package com.codex.campboardgamehost

/** Rule-level helpers shared by the current host flow and the recommendation engine. */
internal fun ClocktowerTeam.isLegalRedHerringTeam(): Boolean =
    this == ClocktowerTeam.Townsfolk || this == ClocktowerTeam.Outsider

/**
 * Returns every legal Demon bluff candidate for the active script.
 *
 * Ordering follows the script role list so that callers remain deterministic. Quality ranking and
 * seeded tie-breaking belong to the recommendation engine, not to this legality helper.
 */
internal fun legalDemonBluffRoles(
    scriptRoles: List<ClocktowerRole>,
    inPlayRoleNames: Set<String>,
): List<ClocktowerRole> = scriptRoles
    .asSequence()
    .filter { it.team == ClocktowerTeam.Townsfolk || it.team == ClocktowerTeam.Outsider }
    .filterNot { it.enName in inPlayRoleNames }
    .distinctBy { it.enName }
    .toList()
