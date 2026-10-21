package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.session.*

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
    fun confirm(registration: Boolean?, onShot: (String, String, Boolean?) -> Unit): Boolean {
        val claimant = claimantName ?: return false
        val target = targetName ?: return false
        onShot(claimant, target, registration)
        clear()
        return true
    }
}

@Composable
internal fun ClocktowerSlayerInteraction(
    draft: ClocktowerSlayerDraft,
    sessionState: ClocktowerSessionState,
    seats: List<HostSeatPresentation>,
    round: Int,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    // The Storyteller confirms the visible result; target changes clear a stale selection.
    var registration by remember(draft.claimantName, draft.targetName) { mutableStateOf<Boolean?>(null) }
    val claimant = sessionState.gameState.players.firstOrNull { it.name == draft.claimantName }?.seat
    val target = sessionState.gameState.players.firstOrNull { it.name == draft.targetName }?.seat
    val needsChoice = claimant != null && target != null && sessionState.slayerNeedsRegistration(claimant, target)
    val slayerTableState = clocktowerSlayerTableState(
        seats = seats,
        claimantName = draft.claimantName,
        targetName = draft.targetName,
    )
    ClocktowerSlayerTableScreen(
        round = round,
        tableState = slayerTableState,
        actionsEnabled = actionsEnabled && (!needsChoice || registration != null),
        compact = false,
        onHostTools = onHostTools,
        onSeatClick = { seatId -> draft.selectSeat(slayerTableState.playerNameForSeat(seatId)) },
        onResetClaimant = draft::clear,
        onResolve = {
            if (draft.confirm(registration, onSlayerShot)) onBack()
        },
        onBack = {
            draft.clear()
            onBack()
        },
        specialContent = {
            if (needsChoice) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.OutlinedButton(onClick = { registration = true }, enabled = actionsEnabled) {
                        Text(if (language == "en") "Recluse registers as Demon" else "陌客登记为恶魔")
                    }
                    androidx.compose.material3.OutlinedButton(onClick = { registration = false }, enabled = actionsEnabled) {
                        Text(if (language == "en") "No registration" else "不登记为恶魔")
                    }
                    Text(registration?.toString().orEmpty())
                }
            }
        },
    )
}
