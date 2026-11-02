package com.codex.campboardgamehost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.*

/** Reuses the existing nomination/vote/Day table, adding only BMR's actual result controls. */
@Composable
internal fun ClocktowerBmrDayControls(state: ClocktowerSessionState, cards: List<PlayerCard>, onHostTools: () -> Unit,
    onAction: (BmrHostAction) -> Unit, onNomination: (String, String, Boolean?, Int) -> Boolean,
    onVote: (String, String, ClocktowerConfirmedVoteRecord, Int) -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun t(zh: String, en: String) = if(language == "en") en else zh
    var execution by remember(state.gameStateRevision) { mutableStateOf(false) }
    var dies by remember(state.gameStateRevision) { mutableStateOf<Boolean?>(null) }
    var pacifist by remember(state.gameStateRevision) { mutableStateOf(false) }
    var gossip by remember(state.gameStateRevision) { mutableStateOf(false) }
    var claimant by remember(state.gameStateRevision) { mutableStateOf<Int?>(null) }
    var statement by remember(state.gameStateRevision) { mutableStateOf("") }
    var truth by remember(state.gameStateRevision) { mutableStateOf<Boolean?>(null) }
    var moonTarget by remember(state.gameStateRevision) { mutableStateOf<Int?>(null) }
    var error by remember(state.gameStateRevision) { mutableStateOf(false) }
    fun accept(action: BmrHostAction) { runCatching { onAction(action) }.onFailure { error = true } }
    val moon = state.pendingBmrMoonchild()
    val nightChanges = state.publicLifeChangesAtNight(state.round)
    val publicAlive = state.gameState.players.filter { it.publiclyAlive }.map { it.seat }
    val table = clocktowerDayOverviewTableState(state.gameState, { clocktowerRoleLabel(it, language) }, ClocktowerGhostVoteAuthority.from(state.actionTimeline))
    if(moon != null || state.dayResolved()) {
        ClocktowerDayTableScaffold("", t("主持工具", "Host tools"), if(moon != null) t("确认选择", "Confirm choice") else t("进入夜晚", "Start night"),
            {}, onHostTools, { if(moon != null) moonTarget?.let { accept(BmrHostAction.Moonchild(moon.fact.actionId, it)) } else accept(BmrHostAction.ContinueDay) },
            previousEnabled = false, nextEnabled = moon == null || moonTarget != null) {
            HostTableShell(table.seats, Modifier.fillMaxSize(),
                interaction = HostTableInteractionState(HostTableInteractionMode.Selection, if(moon != null) publicAlive.mapTo(mutableSetOf()) { ClocktowerSeatId(it) } else emptySet(), listOfNotNull(moonTarget?.let(::ClocktowerSeatId))),
                onSeatClick = { if(moon != null && it.number in publicAlive) moonTarget = it.number }) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if(moon != null) t("月之子：记录公开选择的存活玩家", "Moonchild: record the living player publicly chosen") else t("白天结算已确认", "Day result confirmed"), style = MaterialTheme.typography.titleMedium)
                    if(moonTarget != null) Text("${moonTarget} · ${state.gameState.playerAt(requireNotNull(moonTarget))?.name.orEmpty()}")
                    state.bmrMastermindExtraDay()?.let { Text(t("主谋加时：第 $it 天结束后核对终局。", "Mastermind extra day: review victory after day $it.")) }
                    ClocktowerBmrStatusControls(state, ::accept)
                    if(error) Text(t("请核对当前状态后重试。", "Check the current state and retry."), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    } else ClocktowerDayHostScreen(state, cards, ClocktowerScript.BadMoonRising, false, onHostTools,
        onConfirmDay = {
            if(state.voteStanding.nomineeSeat == null) accept(BmrHostAction.Execution(false, false)) else execution = true
        }, onConfirmKlutzChoice = {}, onConfirmArtistQuestion = { _, _, _ -> }, onSlayerShot = { _, _, _ -> },
        onConfirmedNomination = onNomination, onConfirmedVote = onVote,
        publicNightLifeChanges = nightChanges,
        extraContent = {
            if(nightChanges.any { it.revived }) Text(t("昨夜生死变化：", "Last night: ") + nightChanges.joinToString(" → ") {
                "${it.seat}" + if(it.revived) t("复活", " revived") else t("死亡", " died")
            })
            OutlinedButton(onClick = { gossip = true }) { Text(t("记录造谣发言", "Record Gossip statement")) }
            ClocktowerBmrStatusControls(state, ::accept)
            if(error) Text(t("请核对结算结果后重试。", "Review the result and retry."), color = MaterialTheme.colorScheme.error)
        })
    if(execution) {
        val seat = requireNotNull(state.voteStanding.nomineeSeat)
        val player = requireNotNull(state.gameState.playerAt(seat))
        val protected = !player.alive || state.bmrCertainlyProtected(seat, execution = true) || state.bmrFoolCanSave(seat)
        val actual = if(protected || pacifist) false else dies
        AlertDialog(onDismissRequest = { execution = false }, title = { Text(t("$seat 号被处决", "#$seat is executed")) }, text = {
            Column {
                if(protected) Text(t("此次处决不造成死亡。", "This execution causes no death."))
                else BmrYesNo(t("死亡结果生效？", "Does the death take effect?"), dies, { dies = it }, language)
                if(error) Text(t("请核对目标、状态和是否已记录。", "Check the target, status and whether this was already recorded."), color = MaterialTheme.colorScheme.error)
                if(player.actualRole.value == "Zombuul" && !player.impaired && !player.registersAsDead) Text(t("僵怖首次为登记死亡，实际仍存活。", "The Zombuul's first death is registration only."))
                if(player.actualAlignment == Alignment.GOOD && state.gameState.players.any { it.alive && !it.impaired && it.actualRole.value == "Pacifist" }) Row {
                    Checkbox(pacifist, onCheckedChange = { pacifist = it }); Text(t("和平主义者保命", "Pacifist saves them"))
                }
            }
        }, confirmButton = { TextButton(enabled = actual != null, onClick = { actual?.let { accept(BmrHostAction.Execution(it, pacifist)) } }) { Text(t("确认结果", "Confirm result")) } })
    }
    if(gossip) AlertDialog(onDismissRequest = { gossip = false }, title = { Text(t("公开造谣发言", "Public Gossip statement")) }, text = {
        Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
            BmrSeatPicker(t("发言玩家", "Claimant"), claimant, publicAlive, state, language) { claimant = it }
            OutlinedTextField(statement, onValueChange = { statement = it }, label = { Text(t("原话", "Statement")) })
            BmrYesNo(t("发言是否为真？", "Is the statement true?"), truth, { truth = it }, language)
            if(error) Text(t("该玩家本日可能已记录发言，请核对后重试。", "Check whether this player already gossiped today."), color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(enabled = claimant != null && statement.isNotBlank() && truth != null,
        onClick = { accept(BmrHostAction.Gossip(requireNotNull(claimant), statement, requireNotNull(truth))) }) { Text(t("确认发言", "Confirm statement")) } })
}
