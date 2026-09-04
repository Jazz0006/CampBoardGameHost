package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Slayer claimant/target selection hosted on the persistent Storyteller table. */
@Composable
internal fun ClocktowerSlayerTableScreen(
    round: Int,
    tableState: ClocktowerSlayerTableState,
    actionsEnabled: Boolean,
    onSeatClick: (ClocktowerSeatId) -> Unit,
    onResetClaimant: () -> Unit,
    onResolve: () -> Unit,
    onBack: () -> Unit,
    specialContent: @Composable () -> Unit = {},
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            HostTableShell(
                seats = tableState.seats,
                modifier = Modifier.fillMaxSize(),
                interaction = tableState.interaction,
                onSeatClick = { seatId ->
                    if (actionsEnabled) onSeatClick(seatId)
                },
                seatBadge = { seat ->
                    when (seat.seatId) {
                        tableState.claimantSeatId -> text("声称者", "Claimant")
                        tableState.targetSeatId -> text("目标", "Target")
                        else -> null
                    }
                },
                centerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = text("第 $round 天 · 杀手行动", "Day $round · Slayer action"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )

                        when {
                            tableState.choosingClaimant && !tableState.hasEligibleClaimant -> {
                                Text(
                                    text = text(
                                        "没有可用的杀手声称者。",
                                        "No eligible Slayer claimant remains.",
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            tableState.choosingClaimant -> {
                                Text(
                                    text = text(
                                        "点选公开声称发动杀手能力的玩家",
                                        "Tap the player publicly claiming the Slayer ability",
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            tableState.targetSeatId == null -> {
                                Text(
                                    text = text(
                                        "声称者：${tableState.claimantName} · 现在点选目标",
                                        "Claimant: ${tableState.claimantName} · now tap the target",
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            else -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(
                                        text = "${tableState.claimantName} → ${tableState.targetName}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        if (!tableState.choosingClaimant) {
                            TextButton(
                                onClick = onResetClaimant,
                                enabled = actionsEnabled,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text("重新选择声称者", "Choose a different claimant"))
                            }
                        }

                        if (tableState.targetSeatId != null) {
                            specialContent()
                        }

                        Button(
                            onClick = onResolve,
                            enabled = actionsEnabled &&
                                tableState.claimantSeatId != null &&
                                tableState.targetSeatId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text("结算杀手行动", "Resolve Slayer action"))
                        }

                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text("返回白天", "Return to day"))
                        }
                    }
                },
            )
        }
    }
}
