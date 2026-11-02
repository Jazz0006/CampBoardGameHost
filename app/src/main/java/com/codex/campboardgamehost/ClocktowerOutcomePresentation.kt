package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

internal fun ActionFact.GameEnded.present(context: Context, state: ClocktowerSessionState): GameOutcome {
    val good = reason in setOf(GameEndReason.NO_DEMON, GameEndReason.MAYOR, GameEndReason.BMR_GOOD)
    val alive = state.gameState.players.filter { it.alive }
    val title = context.getString(if (good) R.string.outcome_clocktower_good_title else R.string.outcome_clocktower_evil_title)
    val summary = context.getString(when(reason) {
        GameEndReason.SAINT -> R.string.clocktower_outcome_saint_summary
        GameEndReason.MAYOR -> R.string.clocktower_outcome_mayor_summary
        GameEndReason.NO_DEMON, GameEndReason.BMR_GOOD -> R.string.outcome_clocktower_good_summary
        else -> R.string.outcome_clocktower_evil_summary
    })
    val detail = when(reason) {
        GameEndReason.SAINT -> context.getString(R.string.clocktower_outcome_saint_reason, state.gameState.playerAt(subjectSeat!!)!!.name)
        GameEndReason.MAYOR -> context.getString(R.string.clocktower_outcome_mayor_reason)
        GameEndReason.NO_DEMON -> context.getString(R.string.outcome_clocktower_good_reason)
        GameEndReason.TWO_ALIVE -> context.getString(R.string.outcome_clocktower_evil_reason, alive.size, alive.count { it.actualType == CharacterType.DEMON })
        GameEndReason.BMR_GOOD -> if(context.resources.configuration.locales[0].language == "en") "The Storyteller confirmed a good victory." else "说书人确认善良阵营获胜。"
        GameEndReason.BMR_EVIL -> if(context.resources.configuration.locales[0].language == "en") "The Storyteller confirmed an evil victory." else "说书人确认邪恶阵营获胜。"
        GameEndReason.KLUTZ -> if(context.resources.configuration.locales[0].language == "en") "The Klutz chose an evil player." else "呆瓜选择了邪恶玩家，好人失败。"
    }
    return GameOutcome(title, summary, detail)
}
