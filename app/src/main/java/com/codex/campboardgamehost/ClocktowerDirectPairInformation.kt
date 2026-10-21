package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * Direct human pair input. No precomputed role x seat x seat domain and no
 * answer-validity checks against hidden game truth.
 */
internal data class ClocktowerDirectPairSelection(
    val roleId: String? = null,
    val firstSeat: Int? = null,
    val secondSeat: Int? = null,
    val noneSelected: Boolean = false,
) {
    fun chooseRole(role: String): ClocktowerDirectPairSelection =
        ClocktowerDirectPairSelection(roleId = role)

    fun chooseNone(): ClocktowerDirectPairSelection =
        ClocktowerDirectPairSelection(noneSelected = true)

    fun chooseSeat(seat: Int, seatCount: Int): ClocktowerDirectPairSelection {
        if (roleId == null || seat !in 1..seatCount) return this
        return when (seat) {
            firstSeat -> copy(firstSeat = secondSeat, secondSeat = null)
            secondSeat -> copy(secondSeat = null)
            else -> if (firstSeat == null) copy(firstSeat = seat)
                else if (secondSeat == null) copy(secondSeat = seat)
                else copy(firstSeat = seat, secondSeat = null)
        }
    }

    fun toDisplayOption(
        abilityRole: String,
        seatCount: Int,
        scriptRoles: List<ClocktowerRole>,
        roleLabel: (String) -> String,
        title: String,
        language: String,
    ): ClocktowerDisplayOption? {
        val roleTeam = pairAbsenceTeam(abilityRole) ?: return null
        val absentRoles = scriptRoles.filter { it.team == roleTeam }.map { it.enName }.distinct()
        val proposition: InformationProposition
        val primary: String
        val subjects: List<Int>
        if (noneSelected) {
            proposition = InformationProposition.AllOf(
                absentRoles.map { InformationProposition.RoleInPlay(RoleId(it), false) },
            )
            primary = if (language == "en") when (abilityRole) {
                "Washerwoman" -> "No Townsfolk"
                "Librarian" -> "No Outsiders"
                else -> "No Minions"
            } else when (abilityRole) {
                "Washerwoman" -> "没有镇民"
                "Librarian" -> "没有外来者"
                else -> "没有爪牙"
            }
            subjects = emptyList()
        } else {
            val role = roleId?.takeIf { chosen -> scriptRoles.any { it.enName == chosen } }
                ?: return null
            val first = firstSeat?.takeIf { it in 1..seatCount } ?: return null
            val second = secondSeat?.takeIf { it in 1..seatCount && it != first } ?: return null
            subjects = listOf(first, second).sorted()
            proposition = InformationProposition.AnyOf(subjects.map {
                InformationProposition.RoleAt(it, RoleId(role))
            })
            primary = roleLabel(role)
        }
        return ClocktowerDisplayOption(
            label = primary,
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = title,
            displayPrimary = primary,
            displaySecondary = subjects.takeIf { it.isNotEmpty() }?.joinToString("   "),
            displayFooter = if (subjects.isNotEmpty()) {
                if (language == "en") "One of these two players" else "在下面两位玩家之中"
            } else "",
            proposition = proposition,
            presentationSubjectSeats = subjects,
            isTruthful = false,
        )
    }

    companion object {
        fun fromRecommendation(
            option: ClocktowerDisplayOption?,
            abilityRole: String,
            seatCount: Int,
            scriptRoles: List<ClocktowerRole>,
        ): ClocktowerDirectPairSelection? = fromProposition(
            option?.proposition, abilityRole, seatCount, scriptRoles,
        )

        /** Restore UI highlighting from the latest fullscreen preview, not an AI default. */
        fun fromProposition(
            proposition: InformationProposition?,
            abilityRole: String,
            seatCount: Int,
            scriptRoles: List<ClocktowerRole>,
        ): ClocktowerDirectPairSelection? {
            val confirmedPreview = proposition ?: return null
            val roles = scriptRoles.map { it.enName }.toSet()
            return when (confirmedPreview) {
                is InformationProposition.AnyOf -> {
                    val alternatives = confirmedPreview.alternatives
                        .map { it as? InformationProposition.RoleAt ?: return null }
                    if (alternatives.size != 2 ||
                        alternatives.map { it.role.value }.distinct().size != 1) return null
                    val seatIds = alternatives.map { it.seat }.distinct().sorted()
                    if (seatIds.size != 2 || seatIds.any { it !in 1..seatCount }) return null
                    val role = alternatives.first().role.value.takeIf { it in roles } ?: return null
                    ClocktowerDirectPairSelection(role, seatIds[0], seatIds[1])
                }
                is InformationProposition.AllOf -> {
                    val absentRoles = scriptRoles.filter { it.team == pairAbsenceTeam(abilityRole) }
                        .map { RoleId(it.enName) }.toSet()
                    val facts = confirmedPreview.propositions
                        .map { it as? InformationProposition.RoleInPlay ?: return null }
                    if (absentRoles.isEmpty() || facts.any { it.inPlay } ||
                        facts.map { it.role }.toSet() != absentRoles) return null
                    ClocktowerDirectPairSelection(noneSelected = true)
                }
                else -> null
            }
        }
    }
}

private fun pairAbsenceTeam(abilityRole: String): ClocktowerTeam? = when (abilityRole) {
    "Washerwoman" -> ClocktowerTeam.Townsfolk
    "Librarian" -> ClocktowerTeam.Outsider
    "Investigator" -> ClocktowerTeam.Minion
    else -> null
}
