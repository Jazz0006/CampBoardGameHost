package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ClocktowerDawnSummaryScreen(
    round: Int,
    cards: List<PlayerCard>,
    events: List<ClocktowerEvent>,
    pendingNightDeath: String?,
    onHostTools: () -> Unit,
    onEnterDay: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val deathLabel = pendingNightDeath?.let { playerSeatLabel(cards, it) }
    val privateEvents = events
        .filter { event ->
            event.round == round &&
                event.phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
                event.type in setOf(
                    ClocktowerEventType.RoleAction,
                    ClocktowerEventType.Death,
                    ClocktowerEventType.RoleChange,
                )
        }
        .takeLast(8)

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text("夜晚已结算", "NIGHT RESOLVED"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text("第 $round 天 · 天亮", "Day $round · Dawn"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text("先私下复核结算，再向所有玩家播报。", "Review the private resolution before making the public announcement."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text("说书人私密复核", "HOST-ONLY REVIEW"),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text("不要展示给玩家", "PRIVATE"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (privateEvents.isEmpty()) {
                                Text(
                                    text("没有需要额外复核的夜间事件。", "No additional night events need review."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                privateEvents.forEachIndexed { index, event ->
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(event.title, fontWeight = FontWeight.Bold)
                                        if (event.detail.isNotBlank()) {
                                            Text(
                                                event.detail,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    if (index < privateEvents.lastIndex) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text("公开播报", "PUBLIC ANNOUNCEMENT"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = deathLabel?.let {
                                    text("天亮了。昨晚，$it 死亡。", "Dawn has arrived. $it died last night.")
                                } ?: text("天亮了。昨晚，没有人死亡。", "Dawn has arrived. Nobody died last night."),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text(
                                    "只播报死亡结果，不说明保护、中毒、转移或具体角色。",
                                    "Announce only the death result. Do not reveal protection, poison, redirects, or roles.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                HostBottomActionBar(
                    previousLabel = text("上一步", "Previous"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = text("已完成播报，进入白天", "Announcement complete — enter day"),
                    onPrevious = {},
                    onHostTools = onHostTools,
                    onNext = onEnterDay,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    previousEnabled = false,
                )
            }
        }
    }
}

@Composable
internal fun ClocktowerExecutionConfirmScreen(
    round: Int,
    cards: List<PlayerCard>,
    executionThreshold: Int,
    selectedExecution: String?,
    highestVoteCount: Int,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val targetLabel = selectedExecution?.let { playerSeatLabel(cards, it) }

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ClocktowerDayActionHeader(
                round = round,
                currentStep = 2,
                executionThreshold = executionThreshold,
                title = text("结束白天", "Resolve the day"),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = if (targetLabel != null) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        1.dp,
                        if (targetLabel != null) MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (targetLabel != null) text("即将记录处决", "EXECUTION TO RECORD")
                            else text("今日无人被处决", "NO EXECUTION TODAY"),
                            color = if (targetLabel != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = targetLabel ?: text("进入夜晚", "Continue to night"),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (targetLabel != null) {
                                text("最高票 $highestVoteCount；确认后将立即结算角色能力与胜负。", "Highest vote: $highestVoteCount. Confirming resolves abilities and victory.")
                            } else {
                                text("确认后将结束今天并进入夜晚。", "Confirm to close the day and continue to night.")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                HostBottomActionBar(
                    previousLabel = text("返回白天检查", "Return to day"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = if (targetLabel != null) {
                        text("确认处决 $targetLabel", "Confirm execution: $targetLabel")
                    } else {
                        text("确认无人被处决，进入夜晚", "Confirm no execution and continue")
                    },
                    onPrevious = onBack,
                    onHostTools = onHostTools,
                    onNext = onConfirm,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    nextEnabled = actionsEnabled,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerDayActionHeader(
    round: Int,
    currentStep: Int,
    executionThreshold: Int,
    title: String,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        text("第 $round 天 · $executionThreshold 票可处决", "Day $round · $executionThreshold votes to execute"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "${currentStep + 1} / 3",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= currentStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(50),
                            ),
                    )
                }
            }
        }
    }
}
