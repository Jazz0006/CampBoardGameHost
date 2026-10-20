package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.codex.campboardgamehost.clocktower.session.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Temporary answers; confirmed game facts remain owned by Session. */
internal class ClocktowerArtistDraft {
    var claimantName by mutableStateOf<String?>(null)
        private set
    var truthfulAnswer by mutableStateOf<Boolean?>(null)
        private set
    var shownAnswer by mutableStateOf<Boolean?>(null)
        private set

    fun selectClaimant(next: String?) {
        claimantName = next
        truthfulAnswer = null
        shownAnswer = null
    }
    fun selectTruthfulAnswer(next: Boolean?) {
        truthfulAnswer = next
        shownAnswer = null
    }
    fun selectShownAnswer(next: Boolean?) {
        shownAnswer = next
    }
    fun confirm(onConfirm: (String, Boolean, Boolean) -> Unit) {
        val claimant = requireNotNull(claimantName) { "Artist confirmation requires a claimant." }
        requireNotNull(truthfulAnswer) { "Artist confirmation requires a truthful answer." }
        requireNotNull(shownAnswer) { "Artist confirmation requires a shown answer." }
        onConfirm(claimant, requireNotNull(truthfulAnswer), requireNotNull(shownAnswer))
        selectClaimant(null)
    }
}

@Composable
internal fun ClocktowerArtistInteraction(
    draft: ClocktowerArtistDraft,
    cards: List<PlayerCard>,
    seats: List<HostSeatPresentation>,
    claimantCandidateNames: Set<String>,
    round: Int,
    sessionState: ClocktowerSessionState,
    allowRuleDeterminedAutoUi: Boolean,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onConfirm: (String, Boolean, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val artistClaimant = cards.firstOrNull { it.name == draft.claimantName }
    val currentArtistTruthfulAnswer = draft.truthfulAnswer
    val artistReliable = sessionState.gameState.players.firstOrNull { it.name == draft.claimantName }
        ?.let { sessionState.artistHasFunctioningAbility(it.seat) } == true
    val artistAnswerOptions = if (artistClaimant != null && currentArtistTruthfulAnswer != null) {
        if (artistReliable) listOf(currentArtistTruthfulAnswer) else listOf(true, false)
    } else {
        emptyList()
    }
    val artistManualRequired = !artistReliable && artistAnswerOptions.size > 1
    val automaticArtistAnswer = currentArtistTruthfulAnswer
        ?.takeIf { allowRuleDeterminedAutoUi && artistReliable }
    LaunchedEffect(allowRuleDeterminedAutoUi, draft.claimantName, currentArtistTruthfulAnswer, automaticArtistAnswer) {
        if (allowRuleDeterminedAutoUi && automaticArtistAnswer != null && draft.shownAnswer != automaticArtistAnswer) {
            draft.selectShownAnswer(automaticArtistAnswer)
        }
    }
    val artistTableState = clocktowerArtistTableState(
        seats = seats,
        claimantCandidateNames = claimantCandidateNames,
        claimantName = draft.claimantName,
    )
    ClocktowerArtistTableScreen(
        round = round,
        tableState = artistTableState,
        actionsEnabled = actionsEnabled,
        primaryEnabled = draft.claimantName != null &&
            currentArtistTruthfulAnswer != null &&
            draft.shownAnswer != null &&
            actionsEnabled,
        onHostTools = onHostTools,
        onSeatClick = { seatId ->
            val claimant = artistTableState.playerNameForSeat(seatId)
            draft.selectClaimant(if (draft.claimantName == claimant) null else claimant)
        },
        onPrimary = { draft.confirm(onConfirm) },
        onBack = {
            draft.selectClaimant(null)
            onBack()
        },
    ) {
        if (artistClaimant != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            HostActionSection(title = text("问题的真实答案", "Truthful answer")) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true, false).forEach { answer ->
                        val label = if (answer) text("是", "Yes") else text("否", "No")
                        if (currentArtistTruthfulAnswer == answer) {
                            Button(
                                onClick = { draft.selectTruthfulAnswer(answer) },
                                modifier = Modifier.weight(1f),
                            ) { Text(label) }
                        } else {
                            OutlinedButton(
                                onClick = { draft.selectTruthfulAnswer(answer) },
                                modifier = Modifier.weight(1f),
                            ) { Text(label) }
                        }
                    }
                }
            }
        }
        if (artistClaimant != null && currentArtistTruthfulAnswer != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            HostActionSection(title = text("告诉玩家的答案", "Answer to show")) {
                artistAnswerOptions.forEach { answer ->
                    val label = if (answer) text("是", "Yes") else text("否", "No")
                    if (allowRuleDeterminedAutoUi && !artistManualRequired) {
                        Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    } else if (draft.shownAnswer == answer) {
                        Button(
                            onClick = { draft.selectShownAnswer(answer) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(label) }
                    } else {
                        OutlinedButton(
                            onClick = { draft.selectShownAnswer(answer) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(label) }
                    }
                }
            }
        }
    }
}
