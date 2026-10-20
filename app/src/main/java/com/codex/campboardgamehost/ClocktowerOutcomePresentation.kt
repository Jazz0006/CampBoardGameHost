package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

internal fun ActionFact.GameEnded.present(context: Context, state: ClocktowerSessionState): GameOutcome {
    val good = reason in setOf(GameEndReason.NO_DEMON, GameEndReason.MAYOR)
    val alive = state.gameState.players.filter { it.alive }
    val title = context.getString(if (good) R.string.outcome_clocktower_good_title else R.string.outcome_clocktower_evil_title)
    val summary = context.getString(when(reason) {
        GameEndReason.SAINT -> R.string.clocktower_outcome_saint_summary
        GameEndReason.MAYOR -> R.string.clocktower_outcome_mayor_summary
        GameEndReason.NO_DEMON -> R.string.outcome_clocktower_good_summary
        else -> R.string.outcome_clocktower_evil_summary
    })
    val detail = when(reason) {
        GameEndReason.SAINT -> context.getString(R.string.clocktower_outcome_saint_reason, state.gameState.playerAt(subjectSeat!!)!!.name)
        GameEndReason.MAYOR -> context.getString(R.string.clocktower_outcome_mayor_reason)
        GameEndReason.NO_DEMON -> context.getString(R.string.outcome_clocktower_good_reason)
        GameEndReason.TWO_ALIVE -> context.getString(R.string.outcome_clocktower_evil_reason, alive.size, alive.count { it.actualType == CharacterType.DEMON })
        GameEndReason.KLUTZ -> if(context.resources.configuration.locales[0].language == "en") "The Klutz chose an evil player." else "呆瓜选择了邪恶玩家，好人失败。"
    }
    return GameOutcome(title, summary, detail)
}
