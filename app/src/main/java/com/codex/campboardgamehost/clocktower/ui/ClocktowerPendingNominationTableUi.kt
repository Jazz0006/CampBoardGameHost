package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

/** Persistent-table confirmation surface for a nomination already chosen by directional gesture. */
@Composable
internal fun ClocktowerPendingNominationTableScreen(
    round: Int,
    cards: List<PlayerCard>,
    tableState: ClocktowerDayOverviewTableState,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    specialNotice: String?,
    specialNoticeIsDanger: Boolean,
    continueLabel: String,
    actionsEnabled: Boolean,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    specialContent: @Composable ColumnScope.() -> Unit = {},
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val nominatorSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nominatorName }
    val nomineeSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nomineeName }
    val pendingLink = if (
        nominatorSeat != null && nomineeSeat != null && nominatorSeat.seatId != nomineeSeat.seatId
    ) {
        HostTableDirectionalLink(nominatorSeat.seatId, nomineeSeat.seatId)
    } else {
        null
    }

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
                directionalLink = pendingLink,
                centerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = text("第 $round 天 · 提名", "Day $round · Nomination"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (pendingLink != null) {
                                "${playerSeatLabel(cards, nominatorName)}  →  ${playerSeatLabel(cards, nomineeName)}"
                            } else {
                                text("提名信息无效，请取消后重试", "Invalid nomination; cancel and try again")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "处决门槛：$executionThreshold 票",
                                "Execution threshold: $executionThreshold",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (specialNotice != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (specialNoticeIsDanger) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text("角色能力检查", "ABILITY CHECK"),
                                        color = if (specialNoticeIsDanger) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                    )
                                    Text(
                                        specialNotice,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    specialContent()
                                }
                            }
                        }
                        Button(
                            onClick = onContinue,
                            enabled = actionsEnabled && pendingLink != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(continueLabel, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("取消提名", "Cancel nomination"))
                        }
                    }
                },
            )
        }
    }
}
