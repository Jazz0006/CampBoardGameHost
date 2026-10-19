package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * Stateless, read-only preparation of player-visible information candidates.
 *
 * This owner prepares structural presentation options only. It does not rank candidates, retain
 * recommendation memory, publish information, mutate registration choices, or commit game state.
 */
internal object ClocktowerNeutralInformationPreparation {
    fun roleRevealOptions(
        title: String,
        scriptRoles: List<ClocktowerRole>,
        roleLabel: (ClocktowerRole) -> String,
        footer: String,
        /** Player-selected or previously executed target, never an inferred future choice. */
        targetSeat: Int? = null,
    ): List<ClocktowerDisplayOption> {
        if (targetSeat == null || targetSeat <= 0) return emptyList()
        return scriptRoles
            .distinctBy(ClocktowerRole::enName)
            .sortedBy(ClocktowerRole::enName)
            .map { role ->
                displayOption(
                    label = roleLabel(role),
                    kind = ClocktowerDisplayKind.RoleReveal,
                    title = title,
                    primary = roleLabel(role),
                    footer = footer,
                    proposition = InformationProposition.RoleAt(targetSeat, RoleId(role.enName)),
                    presentationSubjectSeats = targetSeat?.let { listOf(it) }.orEmpty(),
                    presentationRoleId = role.enName,
                    isTruthful = false,
                )
            }
    }

    private fun displayOption(
        label: String,
        kind: ClocktowerDisplayKind,
        title: String,
        primary: String?,
        secondary: String? = null,
        footer: String? = null,
        proposition: InformationProposition? = null,
        presentationSubjectSeats: List<Int> = emptyList(),
        presentationRoleId: String? = null,
        isTruthful: Boolean = true,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = kind,
        displayTitle = title,
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = footer,
        proposition = proposition,
        presentationSubjectSeats = presentationSubjectSeats,
        presentationRoleId = presentationRoleId,
        isTruthful = isTruthful,
        misinformationPressure = 0,
        isDefaultRecommendation = false,
    )

}
