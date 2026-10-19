package com.codex.campboardgamehost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/** A transient confirmation input; committed events remain the only game history. */
internal data class ClocktowerConfirmedNightResult(
    val gameId: String,
    val deathSeats: List<Int>,
    val newImpSeat: Int?,
    val keepPoison: Boolean,
    val sourceRevision: StorytellerDecisionRevision,
) {
    init {
        require(gameId.isNotBlank())
        require(deathSeats.all { it > 0 } && deathSeats.distinct().size == deathSeats.size)
        require(newImpSeat == null || newImpSeat > 0)
    }
}

@Composable
internal fun ClocktowerNightResultConfirmationScreen(
    gameId: String,
    cards: List<PlayerCard>,
    sourceRevision: StorytellerDecisionRevision,
    currentRevision: StorytellerDecisionRevision,
    confirmedNightDeathSeats: List<Int>,
    poisonedPlayerName: String?,
    language: String,
    onCancel: () -> Unit,
    onConfirm: (ClocktowerConfirmedNightResult) -> Unit,
) {
    fun text(zh: String, en: String) = if (language == "en") en else zh
    // A mechanical Death accepted at the Imp boundary is not a night-end draft.
    val deathsLocked = confirmedNightDeathSeats.isNotEmpty()
    var deaths by remember(sourceRevision, confirmedNightDeathSeats) {
        mutableStateOf(confirmedNightDeathSeats.toSet())
    }
    var successor by remember(sourceRevision) { mutableStateOf<Int?>(null) }
    var keepPoison by remember(sourceRevision) { mutableStateOf(poisonedPlayerName != null) }
    var submitting by remember(sourceRevision) { mutableStateOf(false) }
    val current = sourceRevision == currentRevision && !submitting
    ClocktowerDarkTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp)) {
                Text(text("确认本夜实际结果", "Confirm actual night results"), style = MaterialTheme.typography.headlineSmall)
                Text(text("确认后记录并公开播报死亡；新身份单独私密展示。", "Confirm and announce the deaths. Reveal the new identity privately."))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(if (deathsLocked) text(
                        "恶魔行动已确认死亡；不可在夜末撤销或更改。",
                        "The confirmed Imp death cannot be changed at night end.",
                    ) else text(
                        "尚未确认的死亡结果（可多选；不选表示无人死亡）",
                        "Unresolved night deaths (select any; none means no deaths)",
                    ))
                    cards.forEachIndexed { index, card ->
                        val seat = index + 1
                        Row {
                            Checkbox(seat in deaths, { checked -> deaths = if (checked) deaths + seat else deaths - seat }, enabled = current && !deathsLocked)
                            Text(card.seatLabel(cards), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    HorizontalDivider()
                    Text(text("实际成为小恶魔的玩家", "Player actually becoming the Imp"))
                    Row {
                        RadioButton(successor == null, { successor = null }, enabled = current)
                        Text(text("无角色变化", "No role change"), modifier = Modifier.padding(top = 12.dp))
                    }
                    cards.forEachIndexed { index, card ->
                        Row {
                            RadioButton(successor == index + 1, { successor = index + 1 }, enabled = current)
                            Text(card.seatLabel(cards), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    if (poisonedPlayerName != null) {
                        Row {
                            Checkbox(keepPoison, { keepPoison = it }, enabled = current)
                            Text(text("中毒延续至白天：", "Poison continues into the day: ") + playerSeatLabel(cards, poisonedPlayerName), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    if (!current) Text(text("局面已变化，请返回后重新复核。", "The game has changed. Go back and review again."))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel) { Text(text("返回复核", "Back")) }
                    Button(onClick = {
                        submitting = true
                        onConfirm(ClocktowerConfirmedNightResult(gameId, deaths.sorted(), successor, keepPoison, sourceRevision))
                    }, enabled = current) { Text(text("确认并公布死亡", "Confirm and announce deaths")) }
                }
            }
        }
    }
}
