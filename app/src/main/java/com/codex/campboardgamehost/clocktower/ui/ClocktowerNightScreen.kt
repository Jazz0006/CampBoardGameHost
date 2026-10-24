package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.pendingImpNotification
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Compact active-night shell: full-screen steps own the Activity-root workspace directly. */
@Composable
internal fun ClocktowerNightActiveScreen(
    progress: String,
    nextEnabled: Boolean,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    contentOwnsFullScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    ClocktowerDarkTheme {
        CompositionLocalProvider(
            LocalClocktowerNightProgress provides progress,
            LocalClocktowerNightNextEnabled provides nextEnabled,
        ) {
            if (contentOwnsFullScreen) {
                // A full-screen role surface bypasses the ordinary scaffold.
                // Own its opaque canvas and foreground explicitly so text never
                // inherits an unrelated screen's light-theme content color.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    content()
                }
            } else {
                ClocktowerHostFullScreenScaffold(
                    previousLabel = if (language == "en") "← Previous" else "← 上一步",
                    hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
                    nextLabel = if (language == "en") "Next →" else "下一步 →",
                    previousEnabled = false,
                    previousVisible = false,
                    nextEnabled = nextEnabled,

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

/** The succession handoff has its own preview lifetime, separate from role information. */
@Composable
internal fun ClocktowerNewDemonHostScreen(
    cards: List<PlayerCard>,
    sessionState: ClocktowerSessionState,
    onHostTools: () -> Unit,
    onConfirmNewDemon: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String) = if (language == "en") en else zh
    val change = sessionState.pendingImpNotification() ?: return
    val newDemonSeat = change.targetSeat
    val previewKey = listOf(sessionState.gameId, change.actionId, sessionState.gameStateRevision, sessionState.playerInputRevision)
    var showing by remember(previewKey) { mutableStateOf(false) }
    var hasPreviewed by remember(previewKey) { mutableStateOf(false) }
    val seats = sessionState.gameState.toHostSeatPresentations { roleId ->
        clocktowerRoleByName(roleId.value)?.nameFor(language) ?: roleId.value
    }
    val newDemon = cards.getOrNull(newDemonSeat - 1)
    val newDemonStep = ClocktowerNightStepUi(
        interactionId = ClocktowerProductionNightStepIdentity.newDemonIdentity().interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
        action = ClocktowerNightAction.NewDemonIdentity,
        title = text("新恶魔", "New Demon"),
        actor = newDemon,
        isRealAction = newDemon != null,
        reason = "",
        storytellerAction = text("唤醒新的小恶魔。", "Wake the new Imp."),
        tellPlayer = text("你现在是小恶魔", "You are now the Imp"),
        explanation = "",
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayTitle = text("新身份", "New role"),
        displayPrimary = text("你现在是小恶魔", "You are now the Imp"),
        displayFooter = "",
    )
    if (showing) {
        ClocktowerPlayerDisplayCardLocalized(newDemonStep, cards, onDismiss = { showing = false })
        return
    }
    ClocktowerDarkTheme {
        ClocktowerHostSquareTableScaffold(
            seats = seats, language = language, previousEnabled = false,
            onHostTools = onHostTools,
            onNext = { if (hasPreviewed && newDemon != null) onConfirmNewDemon() },
            nextEnabled = hasPreviewed && newDemon != null,
            nextLabel = text("已告知，继续游戏", "Informed, continue"),
            interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
            seatUiModel = { seat ->
                val content = hostSeatContentPresentation(seat, language)
                ClocktowerSquareTableSeatUiModel(
                    seatId = seat.seatId.renderKey(), seatNumber = seat.seatId.number,
                    label = content.primaryLabel, detailLabels = content.detailLabels,
                    actualAlignment = seat.actualAlignment, isAlive = seat.isAlive,
                    hasUnspentGhostVote = seat.hasUnspentGhostVote,
                    isCurrentActor = seat.seatId.number == newDemonSeat,
                )
            },
        ) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)) {
                Text(text("唤醒 $newDemonSeat 号", "Wake seat $newDemonSeat"),
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                OutlinedButton(
                    onClick = { hasPreviewed = true; showing = true },
                    enabled = newDemon != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.clocktower_host_show_to_player)) }
            }
        }
    }
}
