package com.codex.campboardgamehost

import android.os.SystemClock
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

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


/**
 * No provisional PRECOMMIT plan is displayed as confirmed truth while post-commit
 * reassessment is in flight. A missing model never forces a fabricated recommendation.
 */
@Composable
internal fun StorytellerGlobalOverviewStatusScreen(
    busy: Boolean,
    error: String?,
    language: String,
    startedAtElapsedMs: Long = 0L,
    diagnosticStage: String = "IDLE",
    onRetry: () -> Unit,
    onExportDebugBundle: () -> Unit = {},
    onTakeOverManually: () -> Unit,
) {
    fun label(zh: String, en: String) = if (language == "en") en else zh
    var elapsedSeconds by remember(startedAtElapsedMs) { mutableStateOf(0L) }
    LaunchedEffect(startedAtElapsedMs, busy) {
        while (busy && startedAtElapsedMs > 0L) {
            elapsedSeconds = ((SystemClock.elapsedRealtime() - startedAtElapsedMs) / 1000L)
                .coerceAtLeast(0L)
            delay(1000)
        }
    }
    val stageText = when (diagnosticStage) {
        "BUILDING_HOST_CONTEXT" -> label("构建已确认阵容上下文", "Building confirmed roster")
        "PREPARING" -> label("准备 OpenAI 请求", "Preparing OpenAI request")
        "CONNECTING" -> label("连接 OpenAI HTTPS 服务", "Connecting via HTTPS")
        "AWAITING_MODEL" -> label("已发送，等待模型响应", "Sent; waiting for model")
        "HTTP_RESPONSE" -> label("已收到 HTTP 响应", "Received HTTP response")
        "PARSING_RESPONSE" -> label("解析结构化返回", "Parsing structured response")
        "VALIDATING_HOST_RESPONSE" -> label("验证 Host 策略约束", "Checking Host strategy constraints")
        "SUCCESS" -> label("已完成", "Completed")
        "FAILED" -> label("失败", "Failed")
        "STALE" -> label("游戏状态已发生变化", "Game state changed")
        else -> label("等待请求", "Waiting to start")
    }
    ClocktowerDarkTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                label("确认阵容后全局分析", "POST-COMMIT WHOLE-GAME ASSESSMENT"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                if (busy) label(
                    "正在根据最终角色和酒鬼裁定重新分析整局，不会把发牌前的战略假设当成已确认事实。",
                    "Reassessing all confirmed roles and the actual Drunk decision; provisional predeal analysis is not confirmed fact.",
                ) else label(
                    "没有可验证的当前全局策略。可以重新获取，或接管为全手动模式。",
                    "No verified current-game strategy. Retry, or take over in Manual mode.",
                ),
            )
            if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                label("请求阶段：", "Request stage: ") + stageText +
                    if (startedAtElapsedMs > 0L) " · ${elapsedSeconds}s" else "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                label(
                    "约 20–90 秒内可能完成，超过正常等待可切换全手动；重新分析会产生新的 API 调用费用。",
                    "Analysis can take tens of seconds. Manual takeover is always available; retry makes another billable API call.",
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedButton(
                onClick = onExportDebugBundle,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(label("导出调试包（发送前请检查内容）", "Export debug bundle (review before sharing)")) }
            Button(
                onClick = onRetry,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(label("重新分析整局", "Retry whole-game analysis")) }
            OutlinedButton(
                onClick = onTakeOverManually,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(label("切换全手动并继续", "Continue in Manual mode")) }
        }
    }
}
