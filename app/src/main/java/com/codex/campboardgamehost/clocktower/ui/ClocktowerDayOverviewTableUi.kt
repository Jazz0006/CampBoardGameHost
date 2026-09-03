package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

/** Day Overview center controls hosted by the shared persistent Storyteller table. */
@Composable
internal fun ClocktowerDayOverviewScreen(
    round: Int,
    tableState: ClocktowerDayOverviewTableState,
    aliveCount: Int,
    executionThreshold: Int,
    highestVoteText: String,
    showSlayerAction: Boolean,
    slayerActionEnabled: Boolean,
    showArtistAction: Boolean,
    artistActionEnabled: Boolean,
    actionsEnabled: Boolean,
    diagnosticContent: (@Composable () -> Unit)? = null,
    onStartNomination: () -> Unit,
    onOpenSlayer: () -> Unit,
    onOpenArtist: () -> Unit,
    onEndDay: () -> Unit,
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
                centerContent = {
                    ClocktowerDayOverviewCenterContent(
                        round = round,
                        aliveCount = aliveCount,
                        executionThreshold = executionThreshold,
                        highestVoteText = highestVoteText,
                        showSlayerAction = showSlayerAction,
                        slayerActionEnabled = slayerActionEnabled,
                        showArtistAction = showArtistAction,
                        artistActionEnabled = artistActionEnabled,
                        actionsEnabled = actionsEnabled,
                        diagnosticContent = diagnosticContent,
                        text = ::text,
                        onStartNomination = onStartNomination,
                        onOpenSlayer = onOpenSlayer,
                        onOpenArtist = onOpenArtist,
                        onEndDay = onEndDay,
                    )
                },
            )
        }
    }
}

@Composable
private fun ClocktowerDayOverviewCenterContent(
    round: Int,
    aliveCount: Int,
    executionThreshold: Int,
    highestVoteText: String,
    showSlayerAction: Boolean,
    slayerActionEnabled: Boolean,
    showArtistAction: Boolean,
    artistActionEnabled: Boolean,
    actionsEnabled: Boolean,
    diagnosticContent: (@Composable () -> Unit)?,
    text: (String, String) -> String,
    onStartNomination: () -> Unit,
    onOpenSlayer: () -> Unit,
    onOpenArtist: () -> Unit,
    onEndDay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = text("第 $round 天", "Day $round"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Text(
            text = text("白天管理", "Day management"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = highestVoteText,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text("$aliveCount 人存活", "$aliveCount alive"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text("$executionThreshold 票可处决", "$executionThreshold to execute"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = text(
                "自由讨论 · 有人提名时进入提名流程",
                "Open discussion · start nominations when someone nominates",
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )

        if (showSlayerAction) {
            OutlinedButton(
                onClick = onOpenSlayer,
                enabled = actionsEnabled && slayerActionEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text("杀手行动", "Slayer action"))
            }
        }
        if (showArtistAction) {
            OutlinedButton(
                onClick = onOpenArtist,
                enabled = actionsEnabled && artistActionEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text("艺术家提问", "Artist question"))
            }
        }

        Button(
            onClick = onStartNomination,
            enabled = actionsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text("开始提名", "Start nomination"))
        }
        TextButton(
            onClick = onEndDay,
            enabled = actionsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 38.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(text("结束白天", "End day"), fontWeight = FontWeight.Bold)
        }

        diagnosticContent?.invoke()
    }
}
