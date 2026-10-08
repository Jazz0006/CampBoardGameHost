package com.codex.campboardgamehost

import android.content.Context

internal fun clocktowerRoleByName(enName: String?): ClocktowerRole? {
    if (enName.isNullOrBlank()) return null
    return completeClocktowerRoles.firstOrNull { it.enName == enName }
}

private val troubleBrewingRoles = listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "洗衣妇", "Washerwoman", "得知某个镇民在两名玩家之一中。", "Learn that one of two players is a particular Townsfolk."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "图书管理员", "Librarian", "得知某个外来者在两名玩家之一中，或得知没有外来者。", "Learn that one of two players is a particular Outsider, or that there are no Outsiders."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "调查员", "Investigator", "得知某个爪牙在两名玩家之一中，或得知没有爪牙。", "Learn that one of two players is a particular Minion, or that there are no Minions."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "厨师", "Chef", "得知有多少对邪恶玩家相邻而坐。", "Learn how many pairs of evil players are sitting next to each other."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "共情者", "Empath", "每晚得知相邻存活玩家中有几名邪恶玩家。", "Each night, learn how many living neighbors are evil."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "占卜师", "Fortune Teller", "每晚选择两名玩家，得知其中是否有恶魔。", "Each night, choose two players and learn if either is the Demon."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "守鸦人", "Ravenkeeper", "若在夜晚死亡，选择一名玩家并得知其角色。", "If you die at night, choose a player and learn their character."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "士兵", "Soldier", "你不会因恶魔而死亡。", "You are safe from the Demon."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "市长", "Mayor", "若只剩三名存活玩家且白天无人被处决，你的阵营获胜。若你将在夜晚死亡，另一名玩家可能代替你死亡。", "If only three players live and no execution occurs, your team wins. If you would die at night, another player might die instead."),
    ClocktowerRole(ClocktowerTeam.Outsider, "管家", "Butler", "每天选择一名主人，白天只能在主人投票时投票。", "Each day, choose a master. You may only vote if your master votes."),
    ClocktowerRole(ClocktowerTeam.Outsider, "酒鬼", "Drunk", "你以为自己是镇民，但其实能力失效。", "You think you are a Townsfolk, but your ability is not working."),
    ClocktowerRole(ClocktowerTeam.Outsider, "隐士", "Recluse", "你可能被登记为邪恶、爪牙或恶魔，即使死亡后也是。", "You might register as evil and as a Minion or Demon, even if dead."),
    ClocktowerRole(ClocktowerTeam.Outsider, "圣徒", "Saint", "若你被处决，你的阵营失败。", "If you are executed, your team loses."),
    ClocktowerRole(ClocktowerTeam.Minion, "投毒者", "Poisoner", "每晚选择一名玩家，使其能力暂时失效。", "Each night, choose a player. Their ability temporarily stops working."),
    ClocktowerRole(ClocktowerTeam.Minion, "间谍", "Spy", "每晚查看说书人的魔典；你可能被登记为善良、镇民或外来者，即使死亡后也是。", "Each night, view the Storyteller grimoire. You might register as good and as a Townsfolk or Outsider, even if dead."),
    ClocktowerRole(ClocktowerTeam.Minion, "男爵", "Baron", "本局加入额外外来者。", "Extra Outsiders are in play."),
    ClocktowerRole(ClocktowerTeam.Minion, "猩红女巫", "Scarlet Woman", "若恶魔在五人以上时死亡，你可能变成恶魔。", "If the Demon dies with five or more players alive, you may become the Demon."),
    ClocktowerRole(ClocktowerTeam.Demon, "小恶魔", "Imp", "每晚选择一名玩家死亡；可选择自己并传递恶魔身份。", "Each night, choose a player to die. You may choose yourself to pass on the Demon role."),
)

internal val completeTroubleBrewingRoles = (troubleBrewingRoles + listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "送葬者", "Undertaker", "每个夜晚，得知今天被处决玩家的角色。", "Each night, learn which character died by execution today."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "僧侣", "Monk", "每个夜晚，选择除自己以外的一名玩家，使其免受恶魔伤害。", "Each night, choose a player other than yourself: they are safe from the Demon tonight."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "圣女", "Virgin", "首次被镇民提名时，提名者立即被处决。", "The first time you are nominated by a Townsfolk, the nominator is executed immediately."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "杀手", "Slayer", "每局一次，白天选择一名玩家；若其是恶魔，该玩家死亡。", "Once per game during the day, choose a player: if they are the Demon, that player dies."),
)).distinctBy { it.enName }

private val noGreaterJoyExtraRoles = listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "钟表匠", "Clockmaker", "第一夜得知恶魔到最近爪牙相隔几步。", "On the first night, learn how many steps from the Demon to their nearest Minion."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "侍女", "Chambermaid", "每晚选择两名存活玩家，得知其中有几人当晚因自己的能力醒来。", "Each night, choose two alive players and learn how many woke tonight due to their ability."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "艺术家", "Artist", "每局一次，白天私下向说书人询问一个是非问题。", "Once per game during the day, privately ask the Storyteller a yes/no question."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "贤者", "Sage", "如果被恶魔杀死，得知恶魔是两名玩家之一。", "If the Demon kills you, learn that it is one of two players."),
    ClocktowerRole(ClocktowerTeam.Outsider, "呆瓜", "Klutz", "当你得知自己死亡时，公开选择一名存活玩家；若对方邪恶，你的阵营失败。", "When you learn that you died, publicly choose one alive player: if they are evil, your team loses."),
)

private val completeClocktowerRoles = (completeTroubleBrewingRoles + noGreaterJoyExtraRoles).distinctBy { it.enName }

private val noGreaterJoyRoleNames = setOf(
    "Clockmaker",
    "Investigator",
    "Empath",
    "Chambermaid",
    "Artist",
    "Sage",
    "Drunk",
    "Klutz",
    "Baron",
    "Scarlet Woman",
    "Imp",
)

internal fun clocktowerRolesForScript(script: ClocktowerScript): List<ClocktowerRole> = when (script) {
    ClocktowerScript.TroubleBrewing -> completeTroubleBrewingRoles
    ClocktowerScript.NoGreaterJoy -> completeClocktowerRoles.filter { it.enName in noGreaterJoyRoleNames }
}

internal fun ClocktowerTeam.label(context: Context): String = when (this) {
    ClocktowerTeam.Townsfolk -> context.getString(R.string.clocktower_team_townsfolk)
    ClocktowerTeam.Outsider -> context.getString(R.string.clocktower_team_outsider)
    ClocktowerTeam.Minion -> context.getString(R.string.clocktower_team_minion)
    ClocktowerTeam.Demon -> context.getString(R.string.clocktower_team_demon)
}

internal fun ClocktowerRole.nameFor(language: String): String = if (language == "en") enName else zhName

internal fun ClocktowerRole.descriptionFor(language: String): String = if (language == "en") enDescription else zhDescription

internal fun ClocktowerScript.nameFor(language: String): String = when (this) {
    ClocktowerScript.TroubleBrewing -> if (language == "en") "Trouble Brewing" else "暗流涌动"
    ClocktowerScript.NoGreaterJoy -> "No Greater Joy"
}
