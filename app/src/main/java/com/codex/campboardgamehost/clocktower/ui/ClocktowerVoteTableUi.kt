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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** In-place Day voting workspace. Seat taps remain pending until the Storyteller confirms. */
@Composable
internal fun ClocktowerVoteTableScreen(
    round: Int,
    cards: List<PlayerCard>,
    tableState: ClocktowerDayOverviewTableState,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    highestVoteText: String,
    actionsEnabled: Boolean,
    onConfirm: (Set<ClocktowerSeatId>) -> Unit,
    onCancel: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val nominatorSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nominatorName }
    val nomineeSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nomineeName }

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            if (nomineeSeat == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text("投票目标无效，请返回提名。", "Invalid vote target; return to nomination."),
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onCancel) {
                        Text(text("返回提名", "Return to nomination"))
                    }
                }
                return@Surface
            }

            var voteState by remember(tableState.seats, nomineeSeat.seatId) {
                mutableStateOf(
                    clocktowerTableVoteState(
                        seats = tableState.seats,
                        nomineeSeatId = nomineeSeat.seatId,
                    ),
                )
            }
            val nominationLink = nominatorSeat
                ?.takeIf { seat -> seat.seatId != nomineeSeat.seatId }
                ?.let { seat -> HostTableDirectionalLink(seat.seatId, nomineeSeat.seatId) }
            val firstVoterSeat = voteState.orderedSeatIds.firstOrNull()
                ?.let { seatId -> voteState.seats.firstOrNull { seat -> seat.seatId == seatId } }

            HostTableShell(
                seats = voteState.seats,
                modifier = Modifier.fillMaxSize(),
                interaction = voteState.interaction,
                onSeatClick = { seatId -> voteState = voteState.togglePendingVoter(seatId) },
                directionalLink = nominationLink,
                seatBadge = { seat ->
                    if (seat.seatId == nomineeSeat.seatId) {
                        text("${voteState.voteCount}票", "${voteState.voteCount}v")
                    } else {
                        null
                    }
                },
                centerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(
                            text = text("第 $round 天 · 投票", "Day $round · Vote"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "正在对 ${playerSeatLabel(cards, nomineeName)} 投票",
                                "Voting on ${playerSeatLabel(cards, nomineeName)}",
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "${voteState.voteCount} 票 / $executionThreshold 票可处决",
                                "${voteState.voteCount} votes / $executionThreshold to execute",
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = firstVoterSeat?.let { firstSeat ->
                                text(
                                    "从 ${playerSeatLabel(cards, firstSeat.playerName)} 开始，顺时针；${playerSeatLabel(cards, nomineeName)} 最后一票。举手就点头像，再点一次取消。",
                                    "Start with ${playerSeatLabel(cards, firstSeat.playerName)}, move clockwise, and count ${playerSeatLabel(cards, nomineeName)} last. Tap raised hands; tap again to undo.",
                                )
                            } ?: text(
                                "顺时针计票，被提名人最后。",
                                "Count clockwise, with the nominee last.",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = highestVoteText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { onConfirm(voteState.selectedVoterSeatIds) },
                            enabled = actionsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text("确认投票", "Confirm vote"), fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("取消投票", "Cancel vote"))
                        }
                    }
                },
            )
        }
    }
}
