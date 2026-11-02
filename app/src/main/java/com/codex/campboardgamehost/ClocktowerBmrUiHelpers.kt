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
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.*

@Composable
internal fun BmrYesNo(label: String, value: Boolean?, onValue: (Boolean) -> Unit, language: String) {
    Text(label)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(true, false).forEach { choice -> OutlinedButton(onClick = { onValue(choice) }) {
            Text((if(value == choice) "✓ " else "") + if(language == "en") { if(choice) "Yes" else "No" } else { if(choice) "是" else "否" })
        } }
    }
}

@Composable
internal fun BmrRolePicker(label: String, selected: RoleId?, roles: List<ClocktowerRole>, language: String, onSelect: (RoleId) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(label + selected?.let { "：" + clocktowerRoleLabel(it, language) }.orEmpty()) }
        DropdownMenu(open, onDismissRequest = { open = false }) { roles.forEach { role ->
            DropdownMenuItem(text = { Text(role.nameFor(language)) }, onClick = { onSelect(RoleId(role.enName)); open = false })
        } }
    }
}

@Composable
internal fun BmrSeatPicker(label: String, selected: Int?, seats: List<Int>, state: ClocktowerSessionState, language: String, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(label + selected?.let { "：$it · ${state.gameState.playerAt(it)?.name.orEmpty()}" }.orEmpty()) }
        DropdownMenu(open, onDismissRequest = { open = false }) { seats.forEach { seat ->
            DropdownMenuItem(text = { Text("$seat · ${state.gameState.playerAt(seat)?.name.orEmpty()}") }, onClick = { onSelect(seat); open = false })
        } }
    }
}

/** The player renderer receives only this selected proposition text, with no host state/card metadata. */
internal fun bmrPlayerInformationText(p: InformationProposition, state: ClocktowerSessionState, language: String): String {
    fun t(zh: String, en: String) = if(language == "en") en else zh
    fun seat(n: Int) = "$n · ${state.gameState.playerAt(n)?.name.orEmpty()}"
    fun role(r: RoleId) = clocktowerRoleLabel(r, language)
    return when(p) {
        is InformationProposition.RoleAt -> "${seat(p.seat)}\n${role(p.role)}"
        is InformationProposition.CharacterTypeAt -> seat(p.seat) + " · " + when(p.characterType) {
            CharacterType.DEMON -> t("恶魔", "Demon")
            CharacterType.MINION -> t("爪牙", "Minion")
            CharacterType.TOWNSFOLK -> t("镇民", "Townsfolk")
            CharacterType.OUTSIDER -> t("外来者", "Outsider")
        }
        is InformationProposition.RoleInPlay -> role(p.role)
        is InformationProposition.CharactersInPlay -> p.roles.joinToString("\n") { role(it) }.ifEmpty { t("无外来者", "No Outsiders") }
        is InformationProposition.NumericResult -> p.value.toString()
        is InformationProposition.AlignmentAt -> t("你现在是", "You are now ") + if(p.alignment == Alignment.GOOD) t("善良", "good") else t("邪恶", "evil")
        is InformationProposition.PlayerTargets -> t("选择的玩家", "Chosen players") + "\n" + p.targetSeats.joinToString(" → ") { seat(it) }.ifEmpty { t("本夜未选人", "Nobody chosen") }
        is InformationProposition.AllOf -> p.propositions.joinToString("\n\n") { bmrPlayerInformationText(it, state, language) }
        else -> error("Unsupported BMR player display")
    }
}

@Composable
internal fun ClocktowerBmrStatusControls(state: ClocktowerSessionState, onAction: (BmrHostAction) -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun t(zh: String, en: String) = if(language == "en") en else zh
    var status by remember { mutableStateOf(false) }
    var ending by remember { mutableStateOf(false) }
    val review = state.bmrDrunkennessReview()
    TextButton(onClick = { status = true }) { Text(if(review.isNotEmpty()) t("核对醉酒来源（${review.size}）", "Review drunkenness sources (${review.size})") else t("状态与特殊结算", "Status and special effects")) }
    if(status) AlertDialog(onDismissRequest = { status = false }, title = { Text(t("说书人结算", "Storyteller resolution")) }, text = {
        Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.bmrMastermindExtraDay()?.let { Text(t("主谋加时：第 $it 天结束后确认胜负。", "Mastermind extra day: review victory after day $it.")) }
            if(state.gameState.players.none { it.alive && it.actualType == CharacterType.DEMON }) Text(t("没有实际存活的恶魔，请核对主谋与终局。", "No living Demon: review Mastermind and victory."))
            state.bmrDrunkennessEffects().forEach { effect ->
                if(effect in review) Text(if(effect.active) t("来源现已失效，请核对效果是否暂停。", "The source is impaired; review whether to suspend its effect.") else t("来源已经恢复，请核对是否恢复效果。", "The source is healthy again; review whether to resume its effect."))
                Text("${effect.sourceSeat} · ${clocktowerRoleLabel(effect.sourceRole, language)} → ${effect.targetSeats.joinToString(" ")}")
                Text(t("至第 ${effect.throughDayRound} 天黄昏；核对来源失效时的暂停与恢复。", "Until dusk of day ${effect.throughDayRound}; confirm suspension/resumption when the source malfunctions."))
                Row { Text(if(effect.active) t("生效", "Active") else t("暂停", "Suspended")); Switch(effect.active, onCheckedChange = { onAction(BmrHostAction.DrunkActivity(effect.effectId, it)) }) }
            }
            val tinker = state.gameState.players.singleOrNull { it.alive && !it.impaired && it.actualRole.value == "Tinker" }
            if(tinker != null) OutlinedButton(enabled = !state.bmrCertainlyProtected(tinker.seat), onClick = { onAction(BmrHostAction.Tinker); status = false }) { Text(t("修补匠死亡", "Tinker dies")) }
            OutlinedButton(onClick = { status = false; ending = true }) { Text(t("确认游戏结束", "Confirm game end")) }
        }
    }, confirmButton = { TextButton(onClick = { status = false }) { Text(t("返回", "Back")) } })
    if(ending) AlertDialog(onDismissRequest = { ending = false }, title = { Text(t("确认获胜阵营", "Confirm winning team")) },
        text = { Text(t("请核对主谋等特殊规则后确认。", "Confirm after checking Mastermind and other special rules.")) },
        confirmButton = { TextButton(onClick = { onAction(BmrHostAction.End(true)) }) { Text(t("善良获胜", "Good wins")) } },
        dismissButton = { TextButton(onClick = { onAction(BmrHostAction.End(false)) }) { Text(t("邪恶获胜", "Evil wins")) } })
}
