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

internal data class ClocktowerPlainInformationSquareTablePresentation(
    val actorSeat: Int?,
    val wakeInstruction: String?,
    val displayStep: ClocktowerNightStepUi,
)

/**
 * Square-table fallback for direct night information that has no role-specific interactive surface.
 * It deliberately exposes no candidate selection; roles with choices must own a dedicated surface.
 */
internal fun clocktowerPlainInformationSquareTablePresentation(
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    wakeInstruction: String?,
): ClocktowerPlainInformationSquareTablePresentation? {
    if (!step.isRealAction || step.displayKind == ClocktowerDisplayKind.None) return null
    return ClocktowerPlainInformationSquareTablePresentation(actorSeat, wakeInstruction, step)
}

@Composable
internal fun ClocktowerPlainInformationSquareTableDialog(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerPlainInformationSquareTablePresentation,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShowPlayerDisplay: () -> Unit,
) {
    val step = presentation.displayStep
    if (clocktowerUsesBeginnerCompactNightGuidance(presentation.wakeInstruction)) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = presentation.actorSeat,
            wakeInstruction = presentation.wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = onShowPlayerDisplay,
        )
        return
    }
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            ClocktowerNightActionSeatPresentation(
                targetState = ClocktowerSquareTableSeatState.Neutral,
                isCurrentActor = seatNumber == presentation.actorSeat,
            )
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(presentation.wakeInstruction)
                Text(
                    text = step.displayTitle.ifBlank { step.title },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                (step.displayPrimary ?: step.tellPlayer)?.takeIf { it.isNotBlank() }?.let { primary ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
                step.displaySecondary?.takeIf { it.isNotBlank() }?.let { secondary ->
                    Spacer(Modifier.height(8.dp))
                    Text(text = secondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                step.displayFooter?.takeIf { it.isNotBlank() }?.let { footer ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onShowPlayerDisplay, modifier = Modifier.fillMaxWidth()) {
                    Text(if (language == "en") "Show to player" else "展示给玩家")
                }
            }
        }
    }
}
