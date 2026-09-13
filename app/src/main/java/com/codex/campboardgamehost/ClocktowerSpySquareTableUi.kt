package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal data class ClocktowerSpySquareTablePresentation(
    val displayKind: ClocktowerDisplayKind,
    val showLegacyRevealAction: Boolean,
    val helper: String,
)

/**
 * Spy migration intentionally owns only the Storyteller interaction shell. The existing Grimoire
 * reveal remains the player-facing owner and is invoked through the same legacy handoff.
 */
internal fun clocktowerSpySquareTablePresentation(
    step: ClocktowerNightStepUi,
): ClocktowerSpySquareTablePresentation? {
    if (step.roleEnName != "Spy" || step.actor == null || !step.isRealAction) return null
    if (step.displayKind != ClocktowerDisplayKind.Grimoire) return null
    return ClocktowerSpySquareTablePresentation(
        displayKind = step.displayKind,
        showLegacyRevealAction = step.tellPlayer?.isNotBlank() == true,
        helper = step.explanation,
    )
}

internal fun clocktowerSpySeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerSpySquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    presentation: ClocktowerSpySquareTablePresentation,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShowLegacyReveal: () -> Unit,
) {
    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction) && presentation.showLegacyRevealAction) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = onShowLegacyReveal,
            buttonLabel = if (language == "en") "Show grimoire" else "展示魔典",
        )
        return
    }
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSpySeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
            )
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                Text(
                    text = if (language == "en") "Spy" else "间谍",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                presentation.helper.takeIf { it.isNotBlank() }?.let { helper ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
                if (presentation.showLegacyRevealAction) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onShowLegacyReveal,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (language == "en") "Show grimoire" else "展示魔典")
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (language == "en") {
                            "Do not show the true Grimoire this step."
                        } else {
                            "本步骤不要展示真实魔典。"
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
