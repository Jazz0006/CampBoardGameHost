package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

/** Disposable setup draft on the shared private table; confirmed bluffs remain Session facts. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerDemonBluffsHostScreen(
    sessionState: ClocktowerSessionState,
    recommendedDemonBluffRoleNames: List<String>,
    acceptOpeningRecommendation: Boolean,
    onHostTools: () -> Unit,
    onCommitDemonBluffs: (List<String>) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String) = if (language == "en") en else zh
    val candidates = clocktowerDemonBluffCandidates(sessionState.gameState)
    val candidateNames = candidates.mapTo(linkedSetOf()) { it.enName }
    var selected by remember(sessionState.gameId, candidateNames, recommendedDemonBluffRoleNames) {
        mutableStateOf(recommendedDemonBluffRoleNames.filter { it in candidateNames }.distinct().take(3))
    }
    val ready = manualDemonBluffSelectionReady(selected, candidates)
    // Only a complete valid recommendation may auto-confirm; a repaired manual draft needs a tap.
    LaunchedEffect(sessionState.gameId, acceptOpeningRecommendation, candidateNames, recommendedDemonBluffRoleNames) {
        if (acceptOpeningRecommendation && manualDemonBluffSelectionReady(recommendedDemonBluffRoleNames, candidates)) {
            onCommitDemonBluffs(recommendedDemonBluffRoleNames)
        }
    }
    val seats = sessionState.gameState.toHostSeatPresentations { roleId ->
        clocktowerRoleByName(roleId.value)?.nameFor(language) ?: roleId.value
    }
    ClocktowerDarkTheme {
        ClocktowerHostSquareTableScaffold(
            seats = seats, language = language, previousEnabled = false,
            onHostTools = onHostTools,
            onNext = { if (ready) onCommitDemonBluffs(selected) },
            nextEnabled = ready,
            nextLabel = text("确认掩饰 →", "Confirm Demon bluffs"),
            interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
            seatUiModel = { seat ->
                val content = hostSeatContentPresentation(seat, language)
                ClocktowerSquareTableSeatUiModel(
                    seatId = seat.seatId.renderKey(), seatNumber = seat.seatId.number,
                    label = content.primaryLabel, detailLabels = content.detailLabels,
                    actualAlignment = seat.actualAlignment, isAlive = seat.isAlive,
                    hasUnspentGhostVote = seat.hasUnspentGhostVote,
                )
            },
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text("恶魔掩饰", "Demon bluffs") + " · ${selected.size}/3",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text("选择 3 个不在场好人角色", "Choose 3 out-of-play good roles"),
                    style = MaterialTheme.typography.bodySmall)
                Text(text("仅说书人", "Storyteller only"),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Selected identities stay visible while the candidate area scrolls.
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    selected.forEach { name ->
                        FilterChip(
                            selected = true, onClick = { selected = selected - name },
                            label = { Text("${candidates.single { it.enName == name }.nameFor(language)} ×") },
                        )
                    }
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider).forEach { team ->
                        val roles = candidates.filter { it.team == team }
                        if (roles.isNotEmpty()) {
                            Text(team.label(LocalContext.current), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                roles.forEach { role ->
                                    FilterChip(
                                        selected = role.enName in selected,
                                        enabled = selected.size < 3 || role.enName in selected,
                                        onClick = { selected = toggleManualDemonBluffSelection(selected, role.enName, candidateNames) },
                                        label = { Text(role.nameFor(language)) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
