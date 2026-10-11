package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRequest
import com.codex.campboardgamehost.clocktower.session.StorytellerGlobalStrategyV1

internal data class DrunkAiDisplay(
    val rationale: String,
    val globalStrategy: StorytellerGlobalStrategyV1,
    val alternatives: List<Pair<Int, String>>,
    val uncertainty: List<String>,
)

@Composable
internal fun ClocktowerDrunkSelectionScreen(
    request: TroubleBrewingDrunkSelectionRequest,
    operationMode: StorytellerOperationMode = StorytellerOperationMode.MANUAL,
    aiDisplay: DrunkAiDisplay? = null,
    aiBusy: Boolean = false,
    aiError: String? = null,
    onRequestAi: () -> Unit = {},
    language: String,
    roleNameForExternalId: (String) -> String,
    onBack: () -> Unit,
    onConfirm: (TroubleBrewingDrunkCandidate) -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var selectedCandidate by remember(request) {
        mutableStateOf(request.recommendedCandidate)
    }

    BackHandler(onBack = onBack)

    ClocktowerDarkTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = text("指定酒鬼", "CHOOSE THE DRUNK"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text = text("选择一名镇民作为本局酒鬼", "Choose one Townsfolk to be the Drunk"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (operationMode == StorytellerOperationMode.MANUAL) {
                            text("请人工选择规则允许的酒鬼。", "Choose a rules-legal Drunk manually.")
                        } else if (request.recommendedCandidate == null) {
                            text(
                                "当前自动推荐尚未启用。以下每一项都是规则允许的选择，请由说书人决定。",
                                "Automatic recommendation is not enabled yet. Every option below is rules-legal; choose as Storyteller.",
                            )
                        } else {
                            text(
                                "系统推荐已预选；你仍可改选任何规则允许的镇民。",
                                "The recommendation is preselected; you may choose any other rules-legal Townsfolk.",
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (operationMode != StorytellerOperationMode.MANUAL) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = text("整局局势分析", "Whole-game situation analysis"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (operationMode == StorytellerOperationMode.AI_AUTOMATIC) {
                            Text(
                                text("自动主持正在分析并确认合法选择；若失败，请人工接管。",
                                    "Automatic Host validates and commits; on failure, take over manually."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(onClick = onRequestAi, enabled = !aiBusy) {
                            Text(if (aiBusy) text("分析中", "Analysing...")
                                else text("重新分析整局", "Reanalyse whole game"))
                        }
                        aiError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        aiDisplay?.let { advice ->
                            Text(
                                text = advice.globalStrategy.situationSummary,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            advice.globalStrategy.issues.forEach { issue ->
                                Text(
                                    text = "• " + issue.diagnosis +
                                        " [座位 " + issue.seats.joinToString("/") + "] " +
                                        issue.futureEffect,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(text("有条件的后续计划", "Conditional strategic plan"), fontWeight = FontWeight.Bold)
                            advice.globalStrategy.intentions.forEach { intent ->
                                Text(
                                    intent.trigger + " → " + intent.approach + " (" + intent.tradeoff + ")",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = text("当前酒鬼推荐：", "Current Drunk recommendation: ") +
                                    advice.rationale,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            advice.alternatives.forEach { (seat, reason) ->
                                Text(
                                    text = text("备选 ", "Alternative ") + seat + ": " + reason,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (advice.uncertainty.isNotEmpty()) {
                                Text(
                                    text = text("不确定因素：", "Uncertainty: ") +
                                        advice.uncertainty.joinToString("; "),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = text("模型分析不是游戏事实。网络不可用时仍可手动选择。",
                                "AI analysis is not game truth; manual takeover remains possible."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(
                count = request.candidates.size,
                key = { index -> request.candidates[index].seat },
            ) { index ->
                val candidate = request.candidates[index]
                val selected = candidate == selectedCandidate
                val recommended = candidate == request.recommendedCandidate
                Card(
                    onClick = { selectedCandidate = candidate },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = candidate.seat.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = candidate.playerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = roleNameForExternalId(candidate.shownRoleId),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (recommended) {
                            Text(
                                text = text("推荐", "RECOMMENDED"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        } else if (selected) {
                            Text(
                                text = text("已选择", "SELECTED"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            item {
                HostBottomActionBar(
                    previousLabel = text("返回", "Back"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = text("确认酒鬼", "Confirm Drunk"),
                    onPrevious = onBack,
                    onHostTools = {},
                    onNext = {
                        selectedCandidate?.let(onConfirm)
                    },
                    hostToolsEnabled = false,
                    nextEnabled = selectedCandidate != null,
                )
            }
        }
    }
}
