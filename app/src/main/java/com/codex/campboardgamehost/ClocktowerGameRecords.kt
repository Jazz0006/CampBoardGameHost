package com.codex.campboardgamehost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

@Composable
internal fun ClocktowerGameRecords(sessionState: ClocktowerSessionState?, modifier: Modifier = Modifier) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun t(zh: String, en: String) = if (language == "en") en else zh
    if (sessionState == null) {
        Box(modifier.padding(16.dp)) { Text(t("暂无当前对局记录", "No current game records")) }
        return
    }
    key(sessionState.gameId) {
        val rows = remember(sessionState, language) { gameRecordRows(sessionState, language) }
        var selectedSeat by remember { mutableStateOf<Int?>(null) }
        var category by remember { mutableStateOf<GameRecordCategory?>(null) }
        var phase by remember { mutableStateOf<Pair<StorytellerPhase, Int>?>(null) }
        val visible = rows.filter { (selectedSeat == null || selectedSeat in it.seats) &&
            (category == null || category == it.category) && (phase == null || phase == (it.phase to it.round)) }
        val groups = visible.groupBy { if (it.category == GameRecordCategory.SETUP) null else it.phase to it.round }
        fun phaseLabel(p: Pair<StorytellerPhase, Int>) = when(p.first) {
            StorytellerPhase.FIRST_NIGHT -> t("首夜", "First night")
            StorytellerPhase.DAY -> t("第 ${p.second} 天", "Day ${p.second}")
            StorytellerPhase.NIGHT -> t("第 ${p.second} 夜", "Night ${p.second}")
        }
        fun categoryLabel(c: GameRecordCategory) = when(c) {
            GameRecordCategory.INFORMATION -> t("信息", "Information")
            GameRecordCategory.ACTION -> t("行动", "Actions")
            GameRecordCategory.VOTE -> t("提名与投票", "Nominations & votes")
            GameRecordCategory.CHANGE -> t("局面变化", "Changes")
            GameRecordCategory.SETUP -> t("配置", "Setup")
        }
        val listState = rememberLazyListState()
        // Land on the latest confirmed entry; changing a filter starts at its first result.
        LaunchedEffect(selectedSeat, category, phase) {
            listState.scrollToItem(0)
        }
        Column(modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RecordFilter(
                    label = selectedSeat?.let { t("${it}号", "#$it") } ?: t("全部玩家", "All players"),
                    options = listOf<Int?>(null) + sessionState.gameState.players.map { it.seat },
                    optionLabel = { n -> n?.let { t("${it}号", "#$it") + " " + sessionState.gameState.playerAt(it)?.name.orEmpty() } ?: t("全部玩家", "All players") },
                    onSelect = { selectedSeat = it }, modifier = Modifier.weight(1f))
                RecordFilter(label = category?.let(::categoryLabel) ?: t("全部类型", "All types"),
                    options = listOf<GameRecordCategory?>(null) + GameRecordCategory.entries,
                    optionLabel = { it?.let(::categoryLabel) ?: t("全部类型", "All types") },
                    onSelect = { category = it }, modifier = Modifier.weight(1f))
                RecordFilter(label = phase?.let(::phaseLabel) ?: t("全部阶段", "All phases"),
                    options = listOf<Pair<StorytellerPhase, Int>?>(null) + rows.map { it.phase to it.round }.distinct(),
                    optionLabel = { it?.let(::phaseLabel) ?: t("全部阶段", "All phases") },
                    onSelect = { phase = it }, modifier = Modifier.weight(1f))
            }
            if (visible.isEmpty()) Text(t("暂无已确认记录", "No confirmed records"), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                groups.forEach { (p, entries) ->
                    item(key = "phase:$p") {
                        Text(p?.let(::phaseLabel) ?: t("开局配置", "Opening setup"), Modifier.padding(top = 12.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    entries.forEach { record ->
                        item(key = record.id) {
                            var expanded by remember { mutableStateOf(false) }
                            Column(Modifier.fillMaxWidth().then(if(record.details.isNotEmpty()) Modifier.clickable { expanded = !expanded } else Modifier).padding(vertical = 10.dp)) {
                                Text(record.summary + (if (record.details.isEmpty()) "" else if (expanded) " ▴" else " ▾"), style = MaterialTheme.typography.bodyMedium)
                                if (expanded) Text(record.details, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> RecordFilter(label: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 2.dp)) { Text("$label ▾", maxLines = 1) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(optionLabel(option)) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}
