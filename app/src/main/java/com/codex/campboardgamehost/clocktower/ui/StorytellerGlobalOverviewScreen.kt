package com.codex.campboardgamehost

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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

/** Post-commit private strategy: readable outline first, seat graph only on demand. */
@Composable
internal fun StorytellerGlobalOverviewScreen(
    cards: List<PlayerCard>,
    strategy: StorytellerGlobalStrategyV1,
    language: String,
    onContinue: () -> Unit,
) {
    fun label(zh: String, en: String) = if (language == "en") en else zh
    val issues = remember(strategy) { strategy.issues.sortedBy { it.priority } }
    var graphVisible by remember(strategy) { mutableStateOf(false) }
    var selectedIssue by remember(strategy) { mutableStateOf(issues.first().issueId) }
    val selected = issues.firstOrNull { it.issueId == selectedIssue } ?: issues.first()
    val relatedSeats = selected.seats.toSet()
    val relations = strategy.relations.filter { it.issueId == selected.issueId }.take(4)
    val seatDescriptions = remember(cards, language) {
        cards.mapIndexed { index, card ->
            "${index + 1}号 ${card.clocktowerRole?.nameFor(language).orEmpty()}"
        }
    }
    fun seatsLabel(seats: List<Int>): String =
        seats.joinToString(" · ") { seat ->
            seatDescriptions.getOrNull(seat - 1) ?: "${seat}号"
        }

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                label("说书人 · 全局战略", "STORYTELLER · GLOBAL STRATEGY"),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label("已确认阵容 · AI 提出的是推测，不是魔典事实",
                    "Confirmed roster · AI hypotheses are not grimoire facts"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { graphVisible = false },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (!graphVisible) label("✓ 战略摘要", "✓ Summary")
                        else label("战略摘要", "Summary"))
                }
                OutlinedButton(
                    onClick = { graphVisible = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (graphVisible) label("✓ 方桌关系", "✓ Seat graph")
                        else label("方桌关系", "Seat graph"))
                }
            }
            if (!graphVisible) {
                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                label("局势总览", "WHOLE-GAME SUMMARY"),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                strategy.situationSummary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Text(
                        label("关键问题（${issues.size}）", "KEY ISSUES (${issues.size})"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    issues.forEachIndexed { index, issue ->
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    label("问题 ${index + 1}", "ISSUE ${index + 1}"),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    seatsLabel(issue.seats),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    issue.diagnosis,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                androidx.compose.material3.HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                                Text(
                                    label("可能影响", "POSSIBLE IMPACT"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(issue.futureEffect, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Text(
                        label("后续条件策略", "CONDITIONAL NEXT STEPS"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    strategy.intentions.forEachIndexed { index, intention ->
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    label("条件 ${index + 1}", "CONDITION ${index + 1}"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(intention.trigger, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    intention.approach,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    label("取舍：", "Tradeoff: ") + intention.tradeoff,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (strategy.planRevisionNote.isNotBlank()) {
                        Text(
                            label("计划修订：", "Plan revision: ") + strategy.planRevisionNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    issues.forEachIndexed { index, issue ->
                        OutlinedButton(onClick = { selectedIssue = issue.issueId }) {
                            Text(
                                if (issue.issueId == selectedIssue) "${index + 1} ✓"
                                else "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
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
                                label = "${seat} · ${card.name}",
                                detailLabels = listOfNotNull(card.clocktowerRole?.nameFor(language)),
                                state = if (seat in relatedSeats)
                                    ClocktowerSquareTableSeatState.HighlightedInformation
                                else ClocktowerSquareTableSeatState.Neutral,
                                isAlive = card.eliminatedRound == null,
                            )
                        },
                        strategicRelations = relations.map { it.fromSeat to it.toSeat },
                        interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
                    ) {
                        Text(
                            label("关系假设", "HYPOTHESES"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(max = 164.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        seatsLabel(selected.seats),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(selected.diagnosis, style = MaterialTheme.typography.bodyMedium)
                    relations.forEach { relation ->
                        Text(
                            "${relation.fromSeat} ↔ ${relation.toSeat} · ${relation.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        label("虚线是推测，不代表真实登记", "Dashed lines are hypotheses, not registrations"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
