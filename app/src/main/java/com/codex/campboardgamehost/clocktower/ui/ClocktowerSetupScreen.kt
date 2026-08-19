package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Behavior-preserving R2 extraction for Clocktower pre-game setup UI. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerSettingsScreen(
    playerCount: Int,
    playerNames: List<String>,
    selectedScript: ClocktowerScript,
    onScriptChange: (ClocktowerScript) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val context = LocalContext.current
    var step by remember(playerCount) { mutableStateOf(0) }
    val distribution = clocktowerDistribution(playerCount)
    val showScriptChoice = playerCount in 5..6
    val effectiveScript = if (showScriptChoice) selectedScript else ClocktowerScript.TroubleBrewing
    val canStart = playerCount >= MIN_CLOCKTOWER_PLAYERS && canStartClocktowerScript(effectiveScript)
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val stepTitles = listOf(
        text("确认玩家", "Confirm players"),
        text("选择剧本", "Choose script"),
        text("开局确认", "Final review"),
    )

    ClocktowerDarkTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { if (step == 0) onBack() else step -= 1 }) {
                            Text(stringResource(R.string.back))
                        }
                        Text(
                            text = text("配置游戏", "GAME SETUP"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            text = "${step + 1} / 3",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (index <= step) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(50),
                                    ),
                            )
                        }
                    }
                    Text(
                        text = stepTitles[step],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = when (step) {
                            0 -> text("核对围桌顺序。座位号将用于整局主持。", "Check the seating order. Seat numbers stay with the game.")
                            1 -> text("剧本决定本局可出现的角色和夜间流程。", "The script defines the character pool and night order.")
                            else -> text("最后核对一次；开始后将进入逐人发牌。", "Review everything once more before dealing begins.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when (step) {
                0 -> item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = text("$playerCount 名玩家", "$playerCount players"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            playerNames.forEachIndexed { index, name ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                }
                                if (index < playerNames.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }
                    }
                }

                1 -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showScriptChoice) {
                            ClocktowerScript.entries.forEach { script ->
                                Card(
                                    onClick = { onScriptChange(script) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (script == effectiveScript) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (script == effectiveScript) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(script.nameFor(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = when (script) {
                                                ClocktowerScript.TroubleBrewing ->
                                                    text("经典入门剧本，角色互动完整，适合标准人数。", "The classic introductory script with the full core interaction set.")
                                                ClocktowerScript.NoGreaterJoy ->
                                                    text("为 5–6 人小局准备的精简角色组合。", "A focused character set designed for 5–6 players.")
                                            },
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (script == effectiveScript) {
                                            Text(
                                                text = text("已选择", "SELECTED"),
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(effectiveScript.nameFor(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = text(
                                            "7 人及以上固定使用暗流涌动，确保角色数量和夜间流程完整。",
                                            "Games with 7 or more players use Trouble Brewing for the complete distribution and night flow.",
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                else -> item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(text("准备就绪", "Ready to begin"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            ClocktowerSetupSummaryRow(text("玩家", "Players"), text("$playerCount 人", "$playerCount"))
                            ClocktowerSetupSummaryRow(text("剧本", "Script"), effectiveScript.nameFor(language))
                            ClocktowerSetupSummaryRow(
                                text("阵营", "Teams"),
                                ClocktowerTeam.entries.joinToString(" · ") { team ->
                                    "${team.label(context)} ${distribution[team] ?: 0}"
                                },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text = text(
                                    "点击开始后才会随机生成角色并保存本局。返回上一步不会丢失当前选择。",
                                    "Characters are randomized and saved only after you start. Going back keeps your choices.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (step < 2) step += 1 else onStart()
                    },
                    enabled = if (step == 2) canStart else true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (step < 2) {
                            text("下一步", "Continue")
                        } else if (canStart) {
                            stringResource(R.string.start_dealing)
                        } else {
                            stringResource(R.string.need_clocktower_min_players, MIN_CLOCKTOWER_PLAYERS)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (step == 0) {
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(text("返回首页修改玩家", "Edit players on home screen"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerSetupSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
