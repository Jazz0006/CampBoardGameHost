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
    fun numericOptions(
        title: String,
        trueValue: Int,
        maxValue: Int,
        footer: String,
        secondary: String? = null,
        propositionForValue: ((Int) -> InformationProposition)? = null,
    ): List<ClocktowerDisplayOption> {
        val maximumValue = maxOf(trueValue, maxValue)
        return (0..maximumValue).map { value ->
            displayOption(
                label = value.toString(),
                kind = ClocktowerDisplayKind.Number,
                title = title,
                primary = value.toString(),
                secondary = secondary,
                footer = footer,
                proposition = propositionForValue?.invoke(value),
                isTruthful = value == trueValue,
            )
        }
    }

    fun yesNoOptions(
        title: String,
        truthfulYes: Boolean,
        secondary: String?,
        footer: String,
        text: (String, String) -> String,
        propositionForValue: ((Boolean) -> InformationProposition)? = null,
    ): List<ClocktowerDisplayOption> = listOf(true, false).map { answer ->
        val value = if (answer) text("有", "Yes") else text("没有", "No")
        displayOption(
            label = value,
            kind = ClocktowerDisplayKind.YesNo,
            title = title,
            primary = value,
            secondary = secondary,
            footer = footer,
            proposition = propositionForValue?.invoke(answer),
            isTruthful = answer == truthfulYes,
        )
    }

    fun roleRevealOptions(
        title: String,
        truthfulRole: ClocktowerRole?,
        scriptRoles: List<ClocktowerRole>,
        roleLabel: (ClocktowerRole) -> String,
        footer: String,
        /** Player-selected or previously executed target, never an inferred future choice. */
        targetSeat: Int? = null,
    ): List<ClocktowerDisplayOption> {
        if (truthfulRole == null || (targetSeat != null && targetSeat <= 0)) return emptyList()
        return (scriptRoles + truthfulRole)
            .distinctBy(ClocktowerRole::enName)
            .sortedBy(ClocktowerRole::enName)
            .map { role ->
                displayOption(
                    label = roleLabel(role),
                    kind = ClocktowerDisplayKind.RoleReveal,
                    title = title,
                    primary = roleLabel(role),
                    footer = footer,
                    // Misinformation needs typed candidate metadata, NOT a
                    // fabricated epistemic RoleAt observation.
                    presentationSubjectSeats = targetSeat?.let { listOf(it) }.orEmpty(),
                    presentationRoleId = role.enName,
                    isTruthful = role.enName == truthfulRole.enName,
                )
            }
    }

    /** Manual presentation choices represent available seats and script roles, not legal answers. */
    fun pairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
        cards: List<PlayerCard>,
        scriptRoles: List<ClocktowerRole>,
        roleLabel: (ClocktowerRole) -> String,
        text: (String, String) -> String,
    ): List<ClocktowerDisplayOption> {
        if (actor !in cards) return emptyList()
        val roleTeam = when (ability) {
            ClocktowerPairInformationAbility.Washerwoman -> ClocktowerTeam.Townsfolk
            ClocktowerPairInformationAbility.Librarian -> ClocktowerTeam.Outsider
            ClocktowerPairInformationAbility.Investigator -> ClocktowerTeam.Minion
        }
        val roles = scriptRoles.filter { it.team == roleTeam }
        val pool = cards.filter { it.name != actor.name }
        val effects = buildList<PairInformationEffect> {
            roles.forEach { role ->
                for (firstIndex in 0 until pool.lastIndex) {
                    for (secondIndex in firstIndex + 1 until pool.size) {
                        add(
                            PairInformationEffect(
                                id = "unreliable:${ability.name}:${role.enName}:${cards.indexOf(pool[firstIndex]) + 1}:${cards.indexOf(pool[secondIndex]) + 1}",
                                shownRole = role,
                                target = pool[firstIndex],
                                decoy = pool[secondIndex],
                            ),
                        )
                    }
                }
            }
            // A manual result is an observed fact, including unusual or intentionally false cases.
            run {
                add(
                    PairInformationEffect(
                        id = "unreliable:${ability.name}:none",
                        shownRole = null,
                        target = null,
                        decoy = null,
                    ),
                )
            }
        }

        fun propositionFor(effect: PairInformationEffect): InformationProposition =
            if (effect.shownRole != null && effect.target != null && effect.decoy != null) {
                InformationProposition.AnyOf(
                    listOf(
                        InformationProposition.RoleAt(cards.indexOf(effect.target) + 1, RoleId(effect.shownRole.enName)),
                        InformationProposition.RoleAt(cards.indexOf(effect.decoy) + 1, RoleId(effect.shownRole.enName)),
                    ),
                )
            } else {
                InformationProposition.AllOf(
                    roles.map { InformationProposition.RoleInPlay(RoleId(it.enName), false) },
                )
            }

        return effects.map { effect ->
            val noRoleText = when (ability) {
                ClocktowerPairInformationAbility.Librarian -> text("没有外来者", "No Outsiders")
                ClocktowerPairInformationAbility.Investigator -> text("没有爪牙", "No Minions")
                ClocktowerPairInformationAbility.Washerwoman -> text("没有镇民", "No Townsfolk")
            }
            val roleText = effect.shownRole?.let(roleLabel) ?: noRoleText
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumber(cards, effect.target)}   ${seatNumber(cards, effect.decoy)}"
            } else {
                null
            }
            displayOption(
                label = "$roleText${seats?.let { " · $it" }.orEmpty()}",
                kind = ClocktowerDisplayKind.EitherOne,
                title = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                primary = roleText,
                secondary = seats,
                footer = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                proposition = propositionFor(effect),
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

    private fun seatNumber(cards: List<PlayerCard>, card: PlayerCard): String =
        ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()

    private data class PairInformationEffect(
        val id: String,
        val shownRole: ClocktowerRole?,
        val target: PlayerCard?,
        val decoy: PlayerCard?,
    )
}
