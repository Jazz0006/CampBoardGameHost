package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

/** Pending shot only. App/Session still validate and commit the shot and its consequences. */
internal class ClocktowerSlayerDraft {
    var claimantName by mutableStateOf<String?>(null)
        private set
    var targetName by mutableStateOf<String?>(null)
        private set

    fun selectSeat(name: String?) {
        if (claimantName == null) {
            claimantName = name
            targetName = null
        } else {
            targetName = name.takeUnless { targetName == it }
        }
    }
    fun clear() {
        claimantName = null
        targetName = null
    }
    fun confirm(targetDied: Boolean, onShot: (String, String, Boolean) -> Unit): Boolean {
        val claimant = claimantName ?: return false
        val target = targetName ?: return false
        onShot(claimant, target, targetDied)
        clear()
        return true
    }
}

@Composable
internal fun ClocktowerSlayerInteraction(
    draft: ClocktowerSlayerDraft,
    cards: List<PlayerCard>,
    seats: List<HostSeatPresentation>,
    claimantCandidateNames: Set<String>,
    round: Int,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onSlayerShot: (String, String, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    // The Storyteller confirms the visible result; target changes clear a stale selection.
    var targetDied by remember(draft.claimantName, draft.targetName) { mutableStateOf(false) }
    val slayerTableState = clocktowerSlayerTableState(
        seats = seats,
        claimantCandidateNames = claimantCandidateNames,
        alivePlayerNames = cards.filter { it.eliminatedRound == null }.mapTo(mutableSetOf()) { it.name },
        claimantName = draft.claimantName,
        targetName = draft.targetName,
    )
    ClocktowerSlayerTableScreen(
        round = round,
        tableState = slayerTableState,
        actionsEnabled = actionsEnabled,
        compact = false,
        onHostTools = onHostTools,
        onSeatClick = { seatId -> draft.selectSeat(slayerTableState.playerNameForSeat(seatId)) },
        onResetClaimant = draft::clear,
        onResolve = {
            if (draft.confirm(targetDied, onSlayerShot)) onBack()
        },
        onBack = {
            draft.clear()
            onBack()
        },
        specialContent = {
            if (draft.targetName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = targetDied,
                        onCheckedChange = { targetDied = it },
                        enabled = actionsEnabled,
                    )
                    Text(
                        if (language == "en") "Confirmed: target died from the shot"
                        else "说书人确认：目标因射击死亡",
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        },
    )
}
