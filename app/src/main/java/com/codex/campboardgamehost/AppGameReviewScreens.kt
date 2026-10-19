package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class HostToolTab {
    Roles,
    Settings,
}
@Composable
internal fun NewGameConfirmationDialog(
    onDismiss: () -> Unit,
    onManagePlayers: () -> Unit,
    onQuickRestart: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val gameName = stringResource(R.string.game_clocktower)
    ClocktowerDarkTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text("结束当前游戏？", "End this game?")) },
            text = {
                Text(
                    text(
                        "即将结束当前“$gameName”。建议先返回玩家管理，确认下一局参与者后再发牌。",
                        "End the current $gameName game. Review the player list before dealing the next game.",
                    ),
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onManagePlayers,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("保存并调整玩家", "Save & manage players"), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onQuickRestart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("原班人马快速再来一局", "Quick restart with same players"))
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("继续当前游戏", "Continue game"))
                    }
                }
            },
            dismissButton = {},
        )
    }
}

@Composable
internal fun HostGameToolsScreen(
    cards: List<PlayerCard>,
    initialTab: HostToolTab,
    settingsContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    BackHandler(onBack = onDismiss)

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text("私密 · 仅主持人", "PRIVATE · HOST ONLY"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text("主持工具", "Host tools"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text("关闭", "Close"))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HostToolTab.entries.forEach { tab ->
                        val label = when (tab) {
                            HostToolTab.Roles -> text("角色身份", "Roles")
                            HostToolTab.Settings -> text("设置", "Settings")
                        }
                        if (selectedTab == tab) {
                            Button(
                                onClick = {
                                    selectedTab = tab
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    selectedTab = tab
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                when (selectedTab) {
                    HostToolTab.Roles -> HostRolesList(
                        cards = cards,
                        modifier = Modifier.weight(1f),
                    )
                    HostToolTab.Settings -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            settingsContent()
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                ) {
                    TextButton(
                        onClick = onNewGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text("结束当前游戏并开始新一局", "End current game and start a new one"),
                            color = Color(0xFFC9574A),
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostRolesList(
    cards: List<PlayerCard>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text("请确认屏幕仅对主持人可见。", "Make sure only the host can see this screen."),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        items(cards) { card ->
            val role = card.hostRoleLabel(context)
            val shown = if (
                card.clocktowerShownAsDifferentRole() &&
                card.clocktowerShownRole != null
            ) {
                text(
                    " · 展示为 ${card.clocktowerShownRole.nameFor(language)}",
                    " · shown as ${card.clocktowerShownRole.nameFor(language)}",
                )
            } else {
                ""
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (cards.indexOf(card) + 1).toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(card.name, fontWeight = FontWeight.Bold)
                        Text(role + shown, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        card.eliminatedRound?.let { text("死亡", "Dead") } ?: text("存活", "Alive"),
                        color = if (card.eliminatedRound == null) Color(0xFF2F7A57) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
