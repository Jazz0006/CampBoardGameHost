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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Klutz public choice hosted on the persistent Storyteller table. */
@Composable
internal fun ClocktowerKlutzTableScreen(
    round: Int,
    tableState: ClocktowerKlutzTableState,
    actionsEnabled: Boolean,
    onSeatClick: (ClocktowerSeatId) -> Unit,
    onConfirm: () -> Unit,
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
                        tableState.klutzSeatId -> text("呆瓜", "Klutz")
                        tableState.choiceSeatId -> text("选择", "Choice")
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
                            text = text("第 $round 天 · 呆瓜选择", "Day $round · Klutz choice"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )

                        val klutzName = tableState.klutzName
                        if (klutzName == null) {
                            Text(
                                text = text(
                                    "没有待处理的呆瓜玩家。",
                                    "No pending Klutz player is available.",
                                ),
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Text(
                                text = text(
                                    "$klutzName 是呆瓜，得知自己死亡后必须公开选择一名存活玩家。",
                                    "$klutzName is the Klutz and must publicly choose a living player after learning of their death.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }

                        if (tableState.choiceName == null) {
                            Text(
                                text = text(
                                    "点选呆瓜公开指定的存活玩家",
                                    "Tap the living player publicly named by the Klutz",
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    text = "${tableState.klutzName} → ${tableState.choiceName}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        if (tableState.choiceSeatId != null) {
                            specialContent()
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = actionsEnabled && tableState.choiceSeatId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text("确认呆瓜选择", "Confirm Klutz choice"))
                        }
                    }
                },
            )
        }
    }
}
