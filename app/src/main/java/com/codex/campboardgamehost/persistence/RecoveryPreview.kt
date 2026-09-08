package com.codex.campboardgamehost

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

internal data class RecoveryPreviewMetadata(
    val gameKind: GameKind,
    val round: Int,
    val playerCount: Int,
    val savedAtMillis: Long,
    val safeReentry: RecoverySafeReentry,
    val presentResults: Boolean,
)

internal object RecoveryPreviewLoader {
    fun load(
        raw: JSONObject?,
        prepare: (JSONObject) -> RecoveryPlanPreparation,
        clearRejected: () -> Unit,
    ): RecoveryPreviewMetadata? {
        val recovery = raw ?: return null
        return when (val preparation = prepare(recovery)) {
            is RecoveryPlanPreparation.Ready -> preparation.plan.toPreviewMetadata()
            is RecoveryPlanPreparation.Rejected -> {
                clearRejected()
                null
            }
        }
    }

    private fun ValidatedRecoveryPlan.toPreviewMetadata(): RecoveryPreviewMetadata = RecoveryPreviewMetadata(
        gameKind = snapshot.game.gameKind,
        round = snapshot.game.round,
        playerCount = snapshot.game.cards.size,
        savedAtMillis = snapshot.savedAtMillis,
        safeReentry = safeReentry,
        presentResults = presentResults,
    )
}

internal fun RecoveryPreviewMetadata.toSavedGamePreview(context: Context): SavedGamePreview {
    val gameName = when (gameKind) {
        GameKind.Undercover -> context.getString(R.string.game_who_is_undercover)
        GameKind.Werewolf -> context.getString(R.string.game_werewolf)
        GameKind.Clocktower -> context.getString(R.string.game_clocktower)
    }
    val stage = when {
        presentResults -> context.getString(R.string.saved_game_stage_results)
        safeReentry is RecoverySafeReentry.PassPhone || safeReentry is RecoverySafeReentry.RevealCard ->
            context.getString(R.string.saved_game_stage_dealing)
        gameKind == GameKind.Clocktower -> {
            val phase = (safeReentry as RecoverySafeReentry.ClocktowerJudge).phase
            when (phase) {
                ClocktowerPhase.FirstNight -> context.getString(R.string.clocktower_phase_first_night)
                ClocktowerPhase.Dawn -> context.getString(R.string.saved_game_stage_dawn)
                ClocktowerPhase.Day -> context.getString(R.string.clocktower_phase_day, round)
                ClocktowerPhase.Night -> context.getString(R.string.clocktower_phase_night, round)
            }
        }
        else -> context.getString(R.string.round_format, round)
    }
    val savedAtLabel = savedAtMillis
        .takeIf { it > 0L }
        ?.let { timestamp ->
            val locale = context.resources.configuration.locales[0]
            val pattern = if (locale.language == "en") "MMM d, HH:mm" else "M月d日 HH:mm"
            SimpleDateFormat(pattern, locale).format(Date(timestamp))
        }

    return SavedGamePreview(
        title = context.getString(R.string.resume_saved_game),
        subtitle = context.getString(R.string.saved_game_summary_format, gameName, stage, playerCount),
        savedAtLabel = savedAtLabel,
    )
}
