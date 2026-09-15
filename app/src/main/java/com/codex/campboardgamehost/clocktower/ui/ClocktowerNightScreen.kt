package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ClocktowerNewDemonConfirmationScreen(
    newDemonLabel: String,
    hasNewDemon: Boolean,
    onHostTools: () -> Unit,
    onShowPlayerDisplay: () -> Unit,
    onConfirm: () -> Unit,
    compact: Boolean = false,
) {
    if (compact) {
        ClocktowerExperiencedNewDemonConfirmationScreen(
            newDemonLabel = newDemonLabel,
            hasNewDemon = hasNewDemon,
            onHostTools = onHostTools,
            onShowPlayerDisplay = onShowPlayerDisplay,
            onConfirm = onConfirm,
        )
        return
    }

    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text("小恶魔自杀", "Imp self-kill"),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text("恶魔传承 · 私密操作", "Demon succession · Private action"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text("告知新恶魔", "INFORM THE NEW DEMON"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            newDemonLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text("说书人操作", "STORYTELLER ACTION"),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text(
                                        "轻拍并唤醒这名玩家，只向他展示新的身份。确认看完后收回手机，并示意闭眼。",
                                        "Wake this player and show the new identity privately. Take back the phone and signal them to close their eyes.",
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text(
                                    "不要向其他玩家宣布恶魔已经更换。",
                                    "Do not announce the Demon change to other players.",
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onShowPlayerDisplay,
                        enabled = hasNewDemon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_show_to_player))
                    }
                    HostBottomActionBar(
                        previousLabel = text("上一步", "Previous"),
                        hostToolsLabel = text("主持工具", "Host Tools"),
                        nextLabel = text("已告知，进入天亮", "Informed, continue to dawn"),
                        onPrevious = {},
                        onHostTools = onHostTools,
                        onNext = onConfirm,
                        previousEnabled = false,
                        nextEnabled = hasNewDemon,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClocktowerExperiencedNewDemonConfirmationScreen(
    newDemonLabel: String,
    hasNewDemon: Boolean,
    onHostTools: () -> Unit,
    onShowPlayerDisplay: () -> Unit,
    onConfirm: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Text(
                    text = text("小恶魔 · 传承", "Imp · Succession"),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = newDemonLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = text("唤醒并展示新身份", "Wake and show the new identity"),
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = onShowPlayerDisplay,
                    enabled = hasNewDemon,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.clocktower_host_show_to_player))
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                HostBottomActionBar(
                    previousLabel = text("上一步", "Previous"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = text("完成 · 天亮", "Done · Dawn"),
                    onPrevious = {},
                    onHostTools = onHostTools,
                    onNext = onConfirm,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    previousEnabled = false,
                    nextEnabled = hasNewDemon,
                )
            }
        }
    }
}

/** Compact active-night shell: full-screen steps own the Activity-root workspace directly. */
@Composable
internal fun ClocktowerNightActiveScreen(
    title: String,
    subtitle: String,
    progress: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    contentOwnsFullScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    ClocktowerDarkTheme {
        CompositionLocalProvider(LocalClocktowerNightProgress provides progress) {
            if (contentOwnsFullScreen) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            } else {
                ClocktowerHostFullScreenScaffold(
                    previousLabel = if (language == "en") "← Previous" else "← 上一步",
                    hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
                    nextLabel = if (language == "en") "Next →" else "下一步 →",
                    previousEnabled = canGoPrevious,
                    nextEnabled = nextEnabled,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { content() }
                    }
                }
            }
        }
    }
}
@Composable
internal fun ClocktowerNightReadyCard() {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text("夜晚准备", "Night preparation"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text(
                    "所有人请闭眼，低头，保持安静。如果需要唤醒某位玩家，轻拍他。不要泄露信息。",
                    "Everyone close your eyes, look down, and stay quiet. Tap a player to wake them. Do not reveal information.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}