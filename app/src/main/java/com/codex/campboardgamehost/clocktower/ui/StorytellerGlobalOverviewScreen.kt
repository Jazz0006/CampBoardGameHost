package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.session.StorytellerGlobalStrategyV1

/** Private storyteller overlay: only hypotheses; the grimoire is factual authority. */
@Composable
internal fun StorytellerGlobalOverviewScreen(
    cards: List<PlayerCard>,
    strategy: StorytellerGlobalStrategyV1,
    language: String,
    onContinue: () -> Unit,
) {
    fun label(zh: String, en: String) = if (language == "en") en else zh
    var selectedIssue by remember(strategy) { mutableStateOf(strategy.issues.first().issueId) }
    val selected = strategy.issues.firstOrNull { it.issueId == selectedIssue }
        ?: strategy.issues.first()
    val relatedSeats = selected.seats.toSet()
    val relations = strategy.relations
        .filter { it.issueId == selected.issueId }
        .take(4)
        .map { it.fromSeat to it.toSeat }

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                label("说书人专用 · 全局局势分析", "STORYTELLER ONLY · WHOLE-GAME ANALYSIS"),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label(
                    "虚线仅表示 AI 战略假设，不代表已确认的登记或事实。此分析基于发牌前阵容；已确认裁量以魔典为准。",
                    "Dashed links are hypotheses, not confirmed registrations. This analysis began before dealing; grimoire wins.",
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                strategy.issues.take(4).forEachIndexed { index, issue ->
                    OutlinedButton(
                        onClick = { selectedIssue = issue.issueId },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text((index + 1).toString() + if (issue.issueId == selectedIssue) " ✓" else "", fontSize = 12.sp)
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                ClocktowerSquareTableSeatSurface(
                    seats = cards.mapIndexed { index, card ->
                        val seat = index + 1
                        ClocktowerSquareTableSeatUiModel(
                            seatId = seat.toString(),
                            seatNumber = seat,
                            label = seat.toString() + " · " + card.name,
                            detailLabels = listOfNotNull(card.clocktowerRole?.nameFor(language)),
                            state = if (seat in relatedSeats)
                                ClocktowerSquareTableSeatState.HighlightedInformation
                            else ClocktowerSquareTableSeatState.Neutral,
                            isAlive = card.eliminatedRound == null,
                        )
                    },
                    strategicRelations = relations,
                    interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(selected.diagnosis, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(selected.futureEffect, fontSize = 11.sp)
                        strategy.relations.filter { it.issueId == selected.issueId }
                            .forEach {
                                Text(
                                    it.fromSeat.toString() + " ↔ " + it.toSeat + ": " + it.label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(strategy.situationSummary, style = MaterialTheme.typography.bodyMedium)
                Text(label("后续条件策略", "CONDITIONAL FUTURE PLAN"), fontWeight = FontWeight.Bold)
                strategy.intentions.take(3).forEach {
                    Text("• " + it.trigger + " → " + it.approach + "（" + it.tradeoff + "）", fontSize = 12.sp)
                }
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(label("继续主持", "Continue hosting"))
            }
        }
    }
}
