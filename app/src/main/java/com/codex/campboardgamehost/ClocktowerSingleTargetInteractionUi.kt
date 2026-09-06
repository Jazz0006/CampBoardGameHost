package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Renders one already-prepared ability target task; no roster, rules service or Host dependency. */
@Composable
internal fun ClocktowerSingleTargetAbilitySection(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerSingleTargetAbilityPresentation,
    language: String,
    canGoPrevious: Boolean,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    val action = presentation.action
    val title = when (action) {
        ClocktowerNightAction.RedHerring -> stringResource(R.string.clocktower_host_choose_red_herring)
        ClocktowerNightAction.Poison -> stringResource(R.string.clocktower_host_choose_poison_target)
        ClocktowerNightAction.ButlerMaster -> if (language == "en") "Choose the Butler's master" else "选择管家的主人"
        ClocktowerNightAction.MonkProtect -> stringResource(R.string.clocktower_host_choose_monk_protect)
        ClocktowerNightAction.DemonKill -> stringResource(R.string.clocktower_host_choose_night_death)
        ClocktowerNightAction.Ravenkeeper -> stringResource(R.string.clocktower_host_ravenkeeper_target)
        else -> error("Unsupported single-target ability: $action")
    }
    val helper = when (action) {
        ClocktowerNightAction.RedHerring -> if (presentation.selection.selectableSeats.isEmpty()) {
            stringResource(R.string.clocktower_host_no_red_herring_candidates)
        } else stringResource(R.string.clocktower_host_choose_red_herring_hint)
        ClocktowerNightAction.MonkProtect -> stringResource(R.string.clocktower_host_choose_monk_protect_hint)
        ClocktowerNightAction.DemonKill -> stringResource(R.string.clocktower_host_choose_night_death_hint)
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
        canGoPrevious = canGoPrevious,
        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },
        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
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
    canGoPrevious: Boolean,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    val isMayor = presentation.action == ClocktowerNightAction.MayorRedirect
    ClocktowerSingleTargetSquareTableDialog(
        seats = seats,
        selectedSeat = presentation.selection.selectedSeat,
        selectableSeats = presentation.selection.selectableSeats,
        enabled = presentation.selection.enabled,
        title = if (isMayor) {
            if (language == "en") "The Demon attacked the Mayor" else "市长被恶魔击杀"
        } else {
            if (language == "en") "Choose the new Imp" else "选择新小恶魔"
        },
        helper = if (isMayor) {
            if (language == "en") {
                "Choosing a dead or protected player as the redirect target can result in no death tonight."
            } else {
                "选择死亡或受保护的玩家作为转移目标，可能导致今夜无人死亡。"
            }
        } else presentation.explanation,
        language = language,
        canGoPrevious = canGoPrevious,
        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },
        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
        secondaryActionLabel = if (isMayor) {
            if (language == "en") "Mayor dies" else "市长死亡"
        } else null,
        secondaryActionEnabled = isMayor && presentation.selection.enabled,
        onSecondaryAction = {
            presentation.mayorSeat?.let { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) }
        },
    )
}
