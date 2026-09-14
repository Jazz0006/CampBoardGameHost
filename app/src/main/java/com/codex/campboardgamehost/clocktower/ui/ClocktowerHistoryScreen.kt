package com.codex.campboardgamehost

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

internal fun clocktowerEventPhaseLabel(event: ClocktowerEvent, language: String): String = when (event.phase) {
    ClocktowerPhase.FirstNight -> if (language == "en") "Night 1" else "第 1 夜"
    ClocktowerPhase.Dawn -> if (language == "en") "Day ${event.round}" else "第 ${event.round} 天"
    ClocktowerPhase.Day -> if (language == "en") "Day ${event.round}" else "第 ${event.round} 天"
    ClocktowerPhase.Night -> if (language == "en") "Night ${event.round}" else "第 ${event.round} 夜"
}

@Composable
internal fun ClocktowerResultsDialog(
    cards: List<PlayerCard>,
    outcome: GameOutcome?,
    onDismiss: () -> Unit,
    onReview: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var rolesRevealed by remember(cards) { mutableStateOf(false) }
    val resultTitle = outcome?.title ?: text("游戏结束", "Game over")
    val goodWon = resultTitle.contains("好人") || resultTitle.contains("Good", ignoreCase = true)
    val evilWon = resultTitle.contains("邪恶") || resultTitle.contains("Evil", ignoreCase = true)
    val accentColor = when {
        goodWon -> Color(0xFF8FB6D6)
        evilWon -> Color(0xFFD96B70)
        else -> Color(0xFFC5A56A)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        ClocktowerDarkTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = if (rolesRevealed) text("角色揭晓", "ROLE REVEAL") else text("游戏结束", "GAME OVER"),
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    text = if (rolesRevealed) text("完整魔典", "Final grimoire") else text("胜负结算", "Game result"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(onClick = onDismiss) {
                                Text(text("返回主持界面", "Back to host"))
                            }
                        }
                    }

                    if (!rolesRevealed) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.13f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(26.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = resultTitle,
                                        color = accentColor,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                    )
                                    outcome?.let {
                                        Text(
                                            text = it.summary,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            text = it.reason,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Text(
                                        text("角色仍然隐藏", "ROLES ARE STILL HIDDEN"),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                    )
                                    Text(
                                        text(
                                            "确认所有玩家都准备好后，再揭晓真实角色和伪装角色。",
                                            "Reveal only when every player is ready to see actual and shown characters.",
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Button(
                                    onClick = { rolesRevealed = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("确认并揭晓全部角色", "Confirm and reveal all roles"), fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("暂不揭晓", "Not yet"))
                                }
                                TextButton(onClick = onReview, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("复盘操作记录", "Review game log"))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item {
                                Surface(
                                    color = accentColor.copy(alpha = 0.12f),
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(18.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Text(resultTitle, color = accentColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                        outcome?.let {
                                            Text(it.summary, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                            Text(it.reason, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                            item {
                                Text(
                                    text("全部玩家与真实角色", "All players and actual roles"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            items(cards) { card ->
                                ClocktowerResultPlayerRow(
                                    card = card,
                                    cards = cards,
                                    context = context,
                                    language = language,
                                )
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                OutlinedButton(
                                    onClick = onReview,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("复盘本局记录", "Review this game"))
                                }
                                OutlinedButton(
                                    onClick = onNewGame,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("结束收尾，准备下一局", "Finish and prepare next game"))
                                }
                                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("返回主持界面", "Back to host"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerResultPlayerRow(
    card: PlayerCard,
    cards: List<PlayerCard>,
    context: Context,
    language: String,
) {
    val team = card.clocktowerTeam
    val teamColor = when (team) {
        ClocktowerTeam.Townsfolk -> Color(0xFF8FB6D6)
        ClocktowerTeam.Outsider -> Color(0xFF9AAEC0)
        ClocktowerTeam.Minion -> Color(0xFFD09A6A)
        ClocktowerTeam.Demon -> Color(0xFFD96B70)
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val actualRole = card.hostRoleLabel(context, GameKind.Clocktower)
    val shownRole = card.clocktowerShownRole
        ?.takeIf { card.clocktowerShownAsDifferentRole() }
        ?.nameFor(language)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(teamColor.copy(alpha = 0.17f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (cards.indexOfFirst { it.name == card.name } + 1).toString(),
                    color = teamColor,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(card.name, fontWeight = FontWeight.Bold)
                Text(
                    text = listOfNotNull(team?.label(context), actualRole).joinToString(" · "),
                    color = teamColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                shownRole?.let {
                    Text(
                        text = if (language == "en") "Shown to player: $it" else "对玩家展示为：$it",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = card.eliminatedRound?.let {
                    if (language == "en") "Dead · day $it" else "死亡 · 第 $it 天"
                } ?: if (language == "en") "Alive" else "存活",
                color = if (card.eliminatedRound == null) Color(0xFFA6D8BA) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
