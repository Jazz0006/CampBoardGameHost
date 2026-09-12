package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Artist claimant selection hosted on the persistent Storyteller table. */
@Composable
internal fun ClocktowerArtistTableScreen(
    round: Int,
    tableState: ClocktowerArtistTableState,
    actionsEnabled: Boolean,
    primaryEnabled: Boolean,
    onHostTools: () -> Unit,
    onSeatClick: (ClocktowerSeatId) -> Unit,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    detailContent: @Composable () -> Unit = {},
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDayTableScaffold(
        previousLabel = text("返回白天", "Return to day"),
        hostToolsLabel = text("主持工具", "Host Tools"),
        nextLabel = text("记录艺术家提问", "Record Artist question"),
        onPrevious = onBack,
        onHostTools = onHostTools,
        onNext = onPrimary,
        nextEnabled = actionsEnabled && primaryEnabled,
    ) {
        HostTableShell(
            seats = tableState.seats,
            modifier = Modifier.fillMaxSize(),
            interaction = tableState.interaction,
            onSeatClick = { seatId -> if (actionsEnabled) onSeatClick(seatId) },
            seatBadge = { seat ->
                if (seat.seatId == tableState.claimantSeatId) text("提问者", "Claimant") else null
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
                        text = text("第 $round 天 · 艺术家提问", "Day $round · Artist question"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                    if (!tableState.hasEligibleClaimant) {
                        Text(
                            text = text("没有可用的艺术家声称者。", "No eligible Artist claimant remains."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    } else if (tableState.claimantSeatId == null) {
                        Text(
                            text = text("点选公开声称自己是艺术家的玩家", "Tap the player publicly claiming to be the Artist"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            text = text("提问者：${tableState.claimantName}", "Claimant: ${tableState.claimantName}"),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    detailContent()
                }
            },
        )
    }
}
