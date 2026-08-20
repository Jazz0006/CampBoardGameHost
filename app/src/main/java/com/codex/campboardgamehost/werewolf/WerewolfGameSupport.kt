package com.codex.campboardgamehost

import android.content.Context

/**
 * Behavior-preserving R2 extraction from MainActivity.
 * State ownership and UI behavior remain in CampBoardGameHostApp during this pass.
 */
internal enum class WerewolfJudgeStep {
    Wolves,
    Seer,
    Witch,
    Hunter,
    Dawn,
    DayVote,
}

internal enum class LastWordsMode {
    None,
    FirstDay,
    FirstTwoDays,
    Always,
}

internal data class WerewolfTemplate(
    val playerCount: Int,
    val werewolfCount: Int,
    val includeSeer: Boolean,
    val includeWitch: Boolean,
    val includeHunter: Boolean,
)

internal fun Role.werewolfDescription(context: Context): String = when (this) {
    Role.Villager -> context.getString(R.string.role_villager_desc)
    Role.Werewolf -> context.getString(R.string.role_werewolf_desc)
    Role.Seer -> context.getString(R.string.role_seer_desc)
    Role.Witch -> context.getString(R.string.role_witch_desc)
    Role.Hunter -> context.getString(R.string.role_hunter_desc)
    else -> ""
}

internal fun werewolfRolesFor(
    playerCount: Int,
    werewolfCount: Int,
    includeSeer: Boolean,
    includeWitch: Boolean,
    includeHunter: Boolean,
): List<Role> {
    val specialRoles = buildList {
        if (includeSeer) add(Role.Seer)
        if (includeWitch) add(Role.Witch)
        if (includeHunter) add(Role.Hunter)
    }
    val villagerCount = (playerCount - werewolfCount - specialRoles.size).coerceAtLeast(0)
    return buildList {
        repeat(werewolfCount) { add(Role.Werewolf) }
        addAll(specialRoles)
        repeat(villagerCount) { add(Role.Villager) }
    }.shuffled()
}

internal val werewolfTemplates = listOf(
    WerewolfTemplate(playerCount = 4, werewolfCount = 1, includeSeer = true, includeWitch = false, includeHunter = false),
    WerewolfTemplate(playerCount = 5, werewolfCount = 1, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 6, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 7, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 8, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 9, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 10, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 11, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 12, werewolfCount = 4, includeSeer = true, includeWitch = true, includeHunter = true),
)

internal fun LastWordsMode.labelResId(): Int = when (this) {
    LastWordsMode.None -> R.string.last_words_none
    LastWordsMode.FirstDay -> R.string.last_words_first_day
    LastWordsMode.FirstTwoDays -> R.string.last_words_first_two_days
    LastWordsMode.Always -> R.string.last_words_always
}

internal fun WerewolfJudgeStep.titleResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_step_wolves_title
    WerewolfJudgeStep.Seer -> R.string.werewolf_step_seer_title
    WerewolfJudgeStep.Witch -> R.string.werewolf_step_witch_title
    WerewolfJudgeStep.Hunter -> R.string.werewolf_step_hunter_title
    WerewolfJudgeStep.Dawn -> R.string.werewolf_step_dawn_title
    WerewolfJudgeStep.DayVote -> R.string.werewolf_step_day_vote_title
}

internal fun WerewolfJudgeStep.instructionResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_step_wolves_instruction
    WerewolfJudgeStep.Seer -> R.string.werewolf_step_seer_instruction
    WerewolfJudgeStep.Witch -> R.string.werewolf_step_witch_instruction
    WerewolfJudgeStep.Hunter -> R.string.werewolf_step_hunter_instruction
    WerewolfJudgeStep.Dawn -> R.string.werewolf_step_dawn_instruction
    WerewolfJudgeStep.DayVote -> R.string.werewolf_step_day_vote_instruction
}

internal fun WerewolfJudgeStep.scriptResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_script_wolves
    WerewolfJudgeStep.Seer -> R.string.werewolf_script_seer
    WerewolfJudgeStep.Witch -> R.string.werewolf_script_witch
    WerewolfJudgeStep.Hunter -> R.string.werewolf_script_hunter
    WerewolfJudgeStep.Dawn -> R.string.werewolf_script_dawn
    WerewolfJudgeStep.DayVote -> R.string.werewolf_script_day_vote
}

internal fun WerewolfJudgeStep.actionResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_action_wolves
    WerewolfJudgeStep.Seer -> R.string.werewolf_action_seer
    WerewolfJudgeStep.Witch -> R.string.werewolf_action_witch
    WerewolfJudgeStep.Hunter -> R.string.werewolf_action_hunter
    WerewolfJudgeStep.Dawn -> R.string.werewolf_action_dawn
    WerewolfJudgeStep.DayVote -> R.string.werewolf_action_day_vote
}
