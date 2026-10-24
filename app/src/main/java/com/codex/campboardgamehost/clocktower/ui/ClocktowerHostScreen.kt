package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.session.*

/** Routes confirmed Session phase and setup interruptions. Each destination owns its UI drafts. */
@Composable
internal fun ClocktowerJudgeScreen(
    allowRuleDeterminedAutoUi: Boolean,
    cards: List<PlayerCard>,
    sessionState: ClocktowerSessionState,
    jointOpeningAvailable: Boolean = false,
    recommendedDemonBluffRoleNames: List<String>,
    onHostTools: () -> Unit,
    onCommitDemonBluffs: (List<String>) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onConfirmKlutzChoice: (String) -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onConfirmedNomination: (String, String, Boolean?, Int) -> Boolean,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord, Int) -> Unit,
    onConfirmDay: (Int) -> Unit,
    nightContent: @Composable () -> Unit,
) {
    if (sessionState.outcome != null) return
    val script = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionState.gameState.script }
    val newDemonSeat = sessionState.takeIf { it.phase == StorytellerPhase.NIGHT }?.pendingImpNotification()?.targetSeat
    key(sessionState.gameId, sessionState.phase, sessionState.round) {
        when {
            newDemonSeat != null -> ClocktowerNewDemonHostScreen(cards, sessionState, onHostTools, onConfirmNewDemon)
            sessionState.phase == StorytellerPhase.FIRST_NIGHT && sessionState.demonBluffRoleIds.isEmpty() &&
                sessionState.gameState.players.size >= 7 && sessionState.livingDemonSeat() != null ->
                ClocktowerDemonBluffsHostScreen(sessionState, recommendedDemonBluffRoleNames,
                    allowRuleDeterminedAutoUi && jointOpeningAvailable, onHostTools, onCommitDemonBluffs)
            sessionState.phase == StorytellerPhase.DAY ->
                ClocktowerDayHostScreen(sessionState, cards, script, allowRuleDeterminedAutoUi, onHostTools,
                    onConfirmDay, onConfirmKlutzChoice, onConfirmArtistQuestion, onSlayerShot,
                    onConfirmedNomination, onConfirmedVote)
            else -> nightContent()
        }
    }
}
