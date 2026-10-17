package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Pending shot only. App/Session still validate and commit the shot and its consequences. */
internal class ClocktowerSlayerDraft {
    var claimantName by mutableStateOf<String?>(null)
        private set
    var targetName by mutableStateOf<String?>(null)
        private set
    var recluseRegistersDemon by mutableStateOf<Boolean?>(null)
        private set

    fun selectSeat(name: String?) {
        if (claimantName == null) {
            claimantName = name
            targetName = null
        } else {
            targetName = name.takeUnless { targetName == it }
        }
        recluseRegistersDemon = null
    }
    fun chooseRecluseRegistration(value: Boolean) { recluseRegistersDemon = value }
    fun clear() {
        claimantName = null
        targetName = null
        recluseRegistersDemon = null
    }
    fun confirm(targetIsHealthyRecluse: Boolean, onShot: (String, String, Boolean?) -> Unit): Boolean {
        val claimant = claimantName ?: return false
        val target = targetName ?: return false
        onShot(claimant, target, recluseRegistersDemon.takeIf { targetIsHealthyRecluse })
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
    registration: ClocktowerRegistrationProjection,
    round: Int,
    poisonTarget: String?,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun registrationKey(ability: String, subject: String) = "${ClocktowerPhase.Day.name}:$round:$ability:$subject"
    val slayerTargetCard = cards.firstOrNull { it.name == draft.targetName }
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
            val targetIsHealthyRecluse = slayerTargetCard?.clocktowerRole?.enName == "Recluse" &&
                poisonTarget != draft.targetName
            if (draft.confirm(targetIsHealthyRecluse, onSlayerShot)) onBack()
        },
        onBack = {
            draft.clear()
            onBack()
        },
        specialContent = {
            if (slayerTargetCard?.clocktowerRole?.enName == "Recluse") {
                ClocktowerRecluseRegistrationDecisionControls(
                    legalRoles = registration.legalRegistrationRoles(
                        registrationKey("SlayerRecluse", requireNotNull(slayerTargetCard).name),
                        "Slayer",
                        slayerTargetCard,
                        listOf(ClocktowerTeam.Demon),
                    )
                        .map { it.enName to it.nameFor(language) },
                    registersEvil = draft.recluseRegistersDemon == true,
                    registeredRoleEnName = if (draft.recluseRegistersDemon == true) "Imp" else null,
                    enabled = registration.recluseCanRegister("Slayer"),
                    hasExplicitChoice = draft.recluseRegistersDemon != null,
                    language = language,
                    onRegistersEvilChange = draft::chooseRecluseRegistration,
                    onRoleChange = {},
                )
            }
        },
    )
}
