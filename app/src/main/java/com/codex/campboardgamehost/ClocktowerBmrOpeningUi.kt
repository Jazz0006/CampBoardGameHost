package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.BadMoonRisingOpening

@Composable
internal fun ClocktowerBmrOpeningScreen(draft: GameState, onChange: (GameState) -> Unit,
    onRegenerate: () -> Unit, onBack: () -> Unit, onConfirm: () -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun t(zh: String, en: String) = if (language == "en") en else zh
    BackHandler(onBack = onBack)
    val errors = BadMoonRisingOpening.errors(draft)
    ClocktowerDarkTheme {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(t("暗月初升 · 私密开局核对", "Bad Moon Rising · Private setup"), style = MaterialTheme.typography.headlineSmall)
                Text(t("开发预览：本批仅支持开局与方桌，不支持完整对局。", "Development preview: setup and table only; gameplay is not available."))
                if (draft.players.size < 7) Text(t("官方不推荐少于7人使用本剧本。", "The publisher does not recommend this edition below 7 players."))
                val counts = draft.players.groupingBy { it.actualType }.eachCount()
                Text(t("镇民 ${counts[CharacterType.TOWNSFOLK] ?: 0} · 外来者 ${counts[CharacterType.OUTSIDER] ?: 0} · 爪牙 ${counts[CharacterType.MINION] ?: 0} · 恶魔 ${counts[CharacterType.DEMON] ?: 0}",
                    "Townsfolk ${counts[CharacterType.TOWNSFOLK] ?: 0} · Outsiders ${counts[CharacterType.OUTSIDER] ?: 0} · Minions ${counts[CharacterType.MINION] ?: 0} · Demons ${counts[CharacterType.DEMON] ?: 0}"))
                OutlinedButton(onClick = onRegenerate) { Text(t("重新生成", "Regenerate")) }
            }
            draft.players.forEach { p -> item(key = p.seat) {
                var expanded by remember { mutableStateOf(false) }
                var shownExpanded by remember { mutableStateOf(false) }
                Column {
                    Text("${p.seat} · ${p.name}")
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(requireNotNull(clocktowerRoleByName(p.actualRole.value)).nameFor(language)) }
                        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            badMoonRisingRoles.forEach { role ->
                                val other = draft.players.firstOrNull { it.actualRole.value == role.enName && it.seat != p.seat }
                                DropdownMenuItem(text = { Text(role.nameFor(language) + (other?.let { t(" · 与${it.seat}号换位", " · swap with #${it.seat}") } ?: "")) },
                                    onClick = { onChange(BadMoonRisingOpening.assign(draft, p.seat, role.enName)); expanded = false })
                            }
                        }
                    }
                    if (p.actualRole.value == "Lunatic") Box {
                        TextButton(onClick = { shownExpanded = true }) { Text(t("展示身份：", "Shown identity: ") + requireNotNull(clocktowerRoleByName(p.shownRole?.value)).nameFor(language)) }
                        DropdownMenu(shownExpanded, onDismissRequest = { shownExpanded = false }) {
                            badMoonRisingRoles.filter { it.team == ClocktowerTeam.Demon }.forEach { role ->
                                DropdownMenuItem(text = { Text(role.nameFor(language)) }, onClick = {
                                    onChange(draft.copy(players = draft.players.map { if(it.seat == p.seat) it.copy(shownRole = RoleId(role.enName)) else it })); shownExpanded = false
                                })
                            }
                        }
                    }
                }
                HorizontalDivider()
            } }
            item {
                errors.distinct().forEach { error ->
                    val message = when (error) {
                        BadMoonRisingOpening.Error.PLAYERS -> t("需要5–15名姓名不重复的玩家。", "Use 5–15 players with unique names.")
                        BadMoonRisingOpening.Error.SEATS -> t("请检查座位分配。", "Check seat assignments.")
                        BadMoonRisingOpening.Error.ROLES -> t("请使用不重复的本剧本角色。", "Use unique characters from this script.")
                        BadMoonRisingOpening.Error.DISTRIBUTION -> t("角色构成不符合当前人数或教父的外来者增减规则。", "The composition does not meet player-count or Godfather adjustment rules.")
                        BadMoonRisingOpening.Error.SHOWN_IDENTITY -> t("请为疯子选择恶魔展示身份。", "Choose a Demon shown identity for the Lunatic.")
                    }
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onBack) { Text(t("返回", "Back")) }
                    Button(onClick = onConfirm, enabled = errors.isEmpty()) { Text(t("确认并发牌", "Confirm and deal")) }
                }
            }
        }
    }
}

/** Uses the existing private table, with no BMR mechanical callbacks until later batches. */
@Composable
internal fun ClocktowerBmrOpeningTable(cards: List<PlayerCard>, onHostTools: () -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    ClocktowerPhaseTheme(isNight = true) {
        ClocktowerHostFullScreenScaffold(previousEnabled = false, onPrevious = {}, onHostTools = onHostTools,
            nextEnabled = false, onNext = {}, previousLabel = "", hostToolsLabel = if(language == "en") "Host Tools" else "主持工具",
            nextLabel = if(language == "en") "Gameplay pending" else "主持流程待接入", onBack = {}) {
            HostTableShell(seats = cards.mapIndexed { index, card -> card.toStorytellerHostSeatPresentation(index + 1, language) }, modifier = Modifier.fillMaxSize()) {
                Text(if(language == "en") "BMR · First night\nSetup preview only" else "暗月初升 · 首夜\n当前仅支持开局预览")
            }
        }
    }
}
