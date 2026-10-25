package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRequest
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup

/**
 * Storyteller-only, shown-role seating projection for the pre-Session Drunk choice.
 *
 * The actual Drunk seat/role is deliberately absent until confirmation. In Trouble Brewing the
 * visible good/evil camp is already determined: changing one shown Townsfolk to the good Drunk
 * never changes alignment. This is a disposable view of the ONE prepared opening, not GameState.
 */
internal fun troubleBrewingPrecommitHostSeats(
    preparedSetup: TroubleBrewingPreparedSetup,
    roleLabel: (String) -> String,
): List<HostSeatPresentation> {
    val intermediate = preparedSetup.intermediateSetup
    val goodShownRoles = (
        intermediate.visibleRoster.townsfolkRoleIds +
            intermediate.visibleRoster.outsiderRoleIds
        ).toSet()
    return intermediate.shownSeatAssignments.map { assignment ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(assignment.seat),
            playerName = assignment.playerName,
            shownRole = HostRolePresentation(
                roleId = assignment.shownRoleId,
                displayName = roleLabel(assignment.shownRoleId),
            ),
            actualAlignment = if (assignment.shownRoleId in goodShownRoles) {
                ClocktowerAlignment.GOOD
            } else {
                ClocktowerAlignment.EVIL
            },
            isAlive = true,
        )
    }
}

/** Selection is local-only until the existing opening commit receives one legal candidate. */
@Composable
internal fun ClocktowerDrunkSelectionScreen(
    preparedSetup: TroubleBrewingPreparedSetup,
    request: TroubleBrewingDrunkSelectionRequest,
    operationMode: StorytellerOperationMode = StorytellerOperationMode.MANUAL,
    language: String,
    roleNameForExternalId: (String) -> String,
    aiActions: ClocktowerAiRecommendationActions? = null,
    onBack: () -> Unit,
    onConfirm: (TroubleBrewingDrunkCandidate) -> Unit,
) {
    fun text(zh: String, en: String) = if (language == "en") en else zh

    val seats = remember(preparedSetup, roleNameForExternalId) {
        troubleBrewingPrecommitHostSeats(preparedSetup, roleNameForExternalId)
    }
    val eligibleSeats = remember(request) {
        request.candidates.mapTo(linkedSetOf()) { ClocktowerSeatId(it.seat) }
    }
    var selectedCandidate by remember(request) {
        mutableStateOf(request.recommendedCandidate)
    }
    val interaction = HostTableInteractionState(
        mode = HostTableInteractionMode.Selection,
        selectableSeatIds = eligibleSeats,
        selectedSeatIds = listOfNotNull(selectedCandidate?.seat?.let(::ClocktowerSeatId)),
    )

    ClocktowerAiRecommendationPage(aiActions) {
        ClocktowerHostFullScreenScaffold(
            previousLabel = text("← 返回", "← Back"),
            hostToolsLabel = text("主持工具", "Host Tools"),
            nextLabel = text("选定", "Select"),
            previousEnabled = true,
            hostToolsEnabled = false, // No real Session/host grimoire exists before selection.
            nextEnabled = selectedCandidate != null,
            onPrevious = onBack,
            onBack = onBack,
            onHostTools = {},
            onNext = {
                selectedCandidate
                    ?.takeIf { it in request.candidates }
                    ?.let(onConfirm)
            },
        ) {
            HostTableShell(
                seats = seats,
                modifier = Modifier.fillMaxSize(),
                interaction = interaction,
                onSeatClick = { seatId ->
                    request.candidates.firstOrNull { it.seat == seatId.number }
                        ?.let { selectedCandidate = it }
                },
                seatBadge = { seat ->
                    if (seat.seatId.number == request.recommendedCandidate?.seat &&
                        operationMode != StorytellerOperationMode.MANUAL
                    ) {
                        text("推荐", "AI")
                    } else {
                        null
                    }
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                ) {
                    Text(
                        text("指定酒鬼", "Choose the Drunk"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text("点击虚线边框的镇民", "Tap a dotted-outline Townsfolk"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = selectedCandidate?.let { candidate ->
                            text(
                                "已选：${candidate.seat}号 · ${roleNameForExternalId(candidate.shownRoleId)}",
                                "Selected: ${candidate.seat} · ${roleNameForExternalId(candidate.shownRoleId)}",
                            )
                        } ?: text("尚未选择", "Nothing selected"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
