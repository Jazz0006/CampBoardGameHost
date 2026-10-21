package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Renders one already-prepared ability target task; no roster, rules service or Host dependency. */
@Composable
internal fun ClocktowerSingleTargetAbilitySection(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerSingleTargetAbilityPresentation,
    language: String,
    onHostTools: () -> Unit,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    val action = presentation.action
    val usesCompactActionSurface = action in setOf(
        ClocktowerNightAction.Poison,
        ClocktowerNightAction.ButlerMaster,
        ClocktowerNightAction.MonkProtect,
        ClocktowerNightAction.DemonKill,
    )
    if (clocktowerUsesBeginnerCompactNightGuidance(presentation.wakeInstruction) || usesCompactActionSurface) {
        ClocktowerBeginnerSingleTargetAbilityDialog(
            seats = seats,
            presentation = presentation,
            language = language,

            onHostTools = onHostTools,
            onEvent = onEvent,
        )
        return
    }
    val title = when (action) {
        ClocktowerNightAction.RedHerring -> presentation.selection.selectedSeat?.let { seat ->
            if (language == "en") "Red herring · P$seat preselected"
            else "红鲱鱼 · 已预选 P$seat"
        } ?: stringResource(R.string.clocktower_host_choose_red_herring)
        ClocktowerNightAction.Poison -> stringResource(R.string.clocktower_host_choose_poison_target)
        ClocktowerNightAction.ButlerMaster -> if (language == "en") "Choose the Butler's master" else "选择管家的主人"
        ClocktowerNightAction.MonkProtect -> stringResource(R.string.clocktower_host_choose_monk_protect)
        ClocktowerNightAction.DemonKill -> if (language == "en") "Imp · Kill" else "小恶魔 · 击杀"
        ClocktowerNightAction.Ravenkeeper -> stringResource(R.string.clocktower_host_ravenkeeper_target)
        else -> error("Unsupported single-target ability: $action")
    }
    val helper = when (action) {
        ClocktowerNightAction.RedHerring -> if (presentation.selection.selectableSeats.isEmpty()) {
            stringResource(R.string.clocktower_host_no_red_herring_candidates)
        } else if (presentation.selection.selectedSeat != null) {
            if (language == "en") "Highlighted seat is already selected. Continue or tap another eligible seat to override."
            else "已高亮预选座位，无需重新选择；可直接继续，或点击其他合法座位修改。"
        } else stringResource(R.string.clocktower_host_choose_red_herring_hint)
        ClocktowerNightAction.MonkProtect -> stringResource(R.string.clocktower_host_choose_monk_protect_hint)
        ClocktowerNightAction.Ravenkeeper -> stringResource(R.string.clocktower_host_ravenkeeper_target_hint)
        else -> null
    }
    ClocktowerSingleTargetSquareTableDialog(
        seats = seats,
        selectedSeat = presentation.selection.selectedSeat,
        selectableSeats = presentation.selection.selectableSeats,
        enabled = presentation.selection.enabled,
        actorSeat = presentation.actorSeat,
        wakeInstruction = presentation.wakeInstruction,
        title = title,
        helper = helper,
        language = language,

        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },

        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
        nextEnabled = presentation.confirmationEnabled,
        showSelectionSummary = action == ClocktowerNightAction.RedHerring,
        secondaryActionLabel = if (action == ClocktowerNightAction.Ravenkeeper) {
            stringResource(R.string.clocktower_host_show_to_player)
        } else null,
        secondaryActionEnabled = presentation.canShowResult,
        onSecondaryAction = { onEvent(ClocktowerSingleTargetEvent.ShowResult) },
    )
}

/** Ruling tasks have their own content and optional Mayor action, without an actor cue. */
@Composable
internal fun ClocktowerNightRulingSection(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerNightRulingPresentation,
    language: String,
    onHostTools: () -> Unit,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    ClocktowerSingleTargetSquareTableDialog(
        seats = seats,
        selectedSeat = presentation.selection.selectedSeat,
        selectableSeats = presentation.selection.selectableSeats,
        enabled = presentation.selection.enabled,
        title = if (language == "en") "Mayor · Redirect death" else "市长 · 转移死亡",
        helper = null,
        language = language,

        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },

        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
        nextEnabled = presentation.confirmationEnabled,
        showSelectionSummary = false,
        secondaryActionLabel = if (language == "en") "Mayor dies" else "市长死亡",
        secondaryActionEnabled = presentation.selection.enabled,
        onSecondaryAction = {
            presentation.mayorSeat?.let { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) }
        },
    )
}
