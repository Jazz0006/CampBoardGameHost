package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PassPhoneScreen(
    playerName: String,
    playerNames: List<String>,
    gameKind: GameKind,
    current: Int,
    total: Int,
    onReveal: () -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    if (gameKind == GameKind.Clocktower) {
        ClocktowerDealHandoffScreen(
            playerName = playerName,
            playerNames = playerNames,
            current = current,
            total = total,
            onReveal = onReveal,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
        )
        return
    }
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Text(stringResource(R.string.pass_phone_to), style = MaterialTheme.typography.titleLarge)
        Text(playerName, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.reveal_privacy_hint), color = Color(0xFF5C6A63), textAlign = TextAlign.Center)
        Button(
            onClick = onReveal,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.reveal_my_card))
        }
    }
}

@Composable
internal fun RevealCardScreen(
    card: PlayerCard,
    gameKind: GameKind,
    current: Int,
    total: Int,
    onHide: () -> Unit,
) {
    if (gameKind == GameKind.Clocktower) {
        ClocktowerPlayerRoleRevealScreen(
            card = card,
            onHide = onHide,
        )
        return
    }
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(card.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (gameKind == GameKind.Werewolf || gameKind == GameKind.Clocktower) {
                    Text(card.roleLabel ?: stringResource(card.role.labelResId()), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(card.word, color = Color(0xFF5C6A63), textAlign = TextAlign.Center)
                    Text(stringResource(R.string.remember_role_hint), color = Color(0xFF5C6A63))
                } else {
                    Text(card.word, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(stringResource(R.string.remember_word_hint), color = Color(0xFF5C6A63))
                }
            }
        }
        Button(
            onClick = onHide,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (current == total) stringResource(R.string.all_done_return_to_host) else stringResource(R.string.hide_and_next))
        }
    }
}
@Composable
private fun ClocktowerDealHandoffScreen(
    playerName: String,
    playerNames: List<String>,
    current: Int,
    total: Int,
    onReveal: () -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    require(total == playerNames.size) { "Identity controller total must match the physical seat count" }
    require(current in 1..total) { "Identity controller current seat must be in range" }
    require(playerNames[current - 1] == playerName) { "Identity controller target must match the current seat" }
    val seats = playerNames.mapIndexed { index, name ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(index + 1),
            playerName = name,
            isAlive = true,
        )
    }
    val currentSeatId = ClocktowerSeatId(current)

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
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text("身份展示", "IDENTITY REVEAL"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        "$current / $total",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            HostTableShell(
                seats = seats,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                interaction = HostTableInteractionState(
                    mode = HostTableInteractionMode.Sequential,
                    currentSeatId = currentSeatId,
                ),
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "$current / $total",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text(
                                "给 ${current} 号 $playerName 展示身份",
                                "Show the role to seat $current · $playerName",
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onReveal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(text("展示身份", "Show identity"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                HostBottomActionBar(
                    previousLabel = text("上一步", "Previous"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = text("下一步", "Next"),
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    previousEnabled = current > 1,
                )
            }
        }
    }
}
@Composable
private fun ClocktowerPlayerRoleRevealScreen(
    card: PlayerCard,
    onHide: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val shownRole = card.clocktowerShownRole
    val roleName = shownRole?.nameFor(language) ?: card.roleLabel ?: stringResource(card.role.labelResId())
    val team = shownRole?.team
    val teamName = team?.label(context)
    val description = shownRole?.descriptionFor(language) ?: card.word
    val accentColor = when (team) {
        ClocktowerTeam.Townsfolk -> Color(0xFF8FB6D6)
        ClocktowerTeam.Outsider -> Color(0xFF9AAEC0)
        ClocktowerTeam.Minion -> Color(0xFFD09A6A)
        ClocktowerTeam.Demon -> Color(0xFFD96B70)
        null -> Color(0xFFC5A56A)
    }

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
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text("仅供你查看", "FOR YOUR EYES ONLY"),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    )
                    Text(card.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.48f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        teamName?.let {
                            Surface(
                                color = accentColor.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                        Text(
                            roleName,
                            color = accentColor,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        HorizontalDivider(color = accentColor.copy(alpha = 0.28f))
                        Text(
                            text("你的能力", "YOUR ABILITY"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            description,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text(
                        "记住角色和能力。隐藏页面后把手机交回说书人。",
                        "Remember your character and ability. Hide this screen, then return the phone to the Storyteller.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onHide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = text("隐藏身份，交回说书人", "Hide role and return to Storyteller"),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
@Composable
private fun FullScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}
