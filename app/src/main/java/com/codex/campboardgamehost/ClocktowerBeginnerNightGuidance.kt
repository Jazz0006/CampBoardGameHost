package com.codex.campboardgamehost

internal data class ClocktowerBeginnerNightGuidance(
    val wakeLine: String,
    val actorLine: String,
    val instruction: String?,
) {
    fun asWakeInstruction(): String = listOfNotNull(
        wakeLine,
        actorLine,
        instruction?.takeIf { it.isNotBlank() },
    ).joinToString("\n")
}

/**
 * Projects the minimum actionable context needed by a Beginner Storyteller night step.
 *
 * This is presentation-only: role truth, shown-role identity, legal targets and recommendation
 * ownership stay upstream. Evil-team group wake steps use structured team membership rather than
 * parsing localized wake prose.
 */
internal fun clocktowerBeginnerNightGuidance(
    action: ClocktowerNightAction,
    actor: PlayerCard?,
    cards: List<PlayerCard>,
    language: String,
    groupTeam: ClocktowerTeam? = null,
): ClocktowerBeginnerNightGuidance? {
    val resolvedActor = actor ?: return null
    val seat = cards.indexOfFirst { card -> card.name == resolvedActor.name }
        .takeIf { index -> index >= 0 }
        ?.plus(1)
        ?: return null
    val actualRole = resolvedActor.clocktowerRole ?: return null
    val shownRole = resolvedActor.clocktowerShownRole
    fun roleLabel(role: ClocktowerRole): String = if (language == "en") role.enName else role.zhName
    fun seatAndName(card: PlayerCard): String? {
        val cardSeat = cards.indexOf(card).takeIf { it >= 0 }?.plus(1) ?: return null
        return if (language == "en") "P$cardSeat ${card.name}" else "${cardSeat}号 ${card.name}"
    }

    if (groupTeam == ClocktowerTeam.Minion || groupTeam == ClocktowerTeam.Demon) {
        val group = cards.filter { card -> card.clocktowerRole?.team == groupTeam }
        if (group.isEmpty()) return null
        val groupLabel = when (groupTeam) {
            ClocktowerTeam.Minion -> if (language == "en") "Minions" else "爪牙"
            ClocktowerTeam.Demon -> if (language == "en") "Demon" else "恶魔"
            else -> error("unreachable")
        }
        return ClocktowerBeginnerNightGuidance(
            wakeLine = if (language == "en") "Wake $groupLabel" else "唤醒 $groupLabel",
            actorLine = group.mapNotNull(::seatAndName).joinToString(" · "),
            instruction = null,
        )
    }

    val roleContext = if (
        actualRole.enName == "Drunk" &&
        shownRole != null &&
        shownRole.enName != actualRole.enName
    ) {
        "${roleLabel(actualRole)} → ${roleLabel(shownRole)}"
    } else {
        roleLabel(actualRole)
    }
    val wakeLine = if (language == "en") "Wake $roleContext" else "唤醒 $roleContext"
    val actorLine = if (language == "en") "P$seat ${resolvedActor.name}" else "${seat}号 ${resolvedActor.name}"
    val instruction = when (action) {
        ClocktowerNightAction.Poison -> if (language == "en") {
            "Have them choose one player to poison"
        } else {
            "让他选择一名玩家作为中毒目标"
        }
        ClocktowerNightAction.ButlerMaster -> if (language == "en") {
            "Have them choose one player as their master"
        } else {
            "让他选择一名玩家作为主人"
        }
        ClocktowerNightAction.MonkProtect -> if (language == "en") {
            "Have them choose one player to protect"
        } else {
            "让他选择一名玩家作为保护目标"
        }
        ClocktowerNightAction.DemonKill -> if (language == "en") {
            "Have them choose one player to kill"
        } else {
            "让他选择一名玩家作为击杀目标"
        }
        ClocktowerNightAction.FortuneTeller -> if (language == "en") {
            "Have them choose two players to check"
        } else {
            "让他选择两名玩家进行查验"
        }
        ClocktowerNightAction.Chambermaid -> if (language == "en") {
            "Have them choose two players to check"
        } else {
            "让她选择两名玩家进行查验"
        }
        ClocktowerNightAction.Ravenkeeper -> if (language == "en") {
            "Have them choose one player to learn their character"
        } else {
            "让他选择一名玩家查看其角色"
        }
        else -> null
    }
    return ClocktowerBeginnerNightGuidance(
        wakeLine = wakeLine,
        actorLine = actorLine,
        instruction = instruction,
    )
}
