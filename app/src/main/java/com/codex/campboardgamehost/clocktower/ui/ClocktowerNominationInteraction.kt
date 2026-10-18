package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics

/** A pending pair, not a confirmed nomination or vote. Host retains it across both screens. */
internal class ClocktowerNominationDraft {
    var nominatorName by mutableStateOf<String?>(null)
        private set
    var nomineeName by mutableStateOf<String?>(null)
        private set

    fun select(nominator: String, nominee: String) {
        nominatorName = nominator
        nomineeName = nominee
    }
    fun clear() {
        nominatorName = null
        nomineeName = null
    }
    fun confirmVote(vote: ClocktowerConfirmedVoteRecord, onConfirm: (String, String, ClocktowerConfirmedVoteRecord) -> Unit) {
        onConfirm(requireNotNull(nominatorName), requireNotNull(nomineeName), vote)
        clear()
    }
}

/** The UI requests confirmation; App remains the owner of the accepted facts. */
internal data class ClocktowerNominationConfirmation(
    val nominator: String,
    val nominee: String,
    val firstVirginNomination: Boolean,
    val virginExecutes: Boolean,
    val explicitSpyRegistration: Boolean?,
)

@Composable
internal fun ClocktowerNominationInteraction(
    draft: ClocktowerNominationDraft,
    cards: List<PlayerCard>,
    tableState: ClocktowerDayOverviewTableState,
    registrationState: ClocktowerRegistrationInteractionState,
    round: Int,
    executionThreshold: Int,
    poisonTarget: String?,
    virginUsed: Boolean,
    actionsEnabled: Boolean,
    onHostTools: () -> Unit,
    onConfirm: (ClocktowerNominationConfirmation) -> Unit,
    onCancel: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val nominatorName = draft.nominatorName
    val nomineeName = draft.nomineeName
    val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    fun registrationKey(ability: String, subject: String) = "${ClocktowerPhase.Day.name}:$round:$ability:$subject"
    val nominatorCard = cards.firstOrNull { it.name == nominatorName }
    val nomineeCard = cards.firstOrNull { it.name == nomineeName }
    val virginFirstNomination = nomineeCard?.let {
        AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Virgin")
    } == true && !virginUsed
    val virginAbilityWorks = nomineeCard?.let {
        AbilityFunctioningSemantics.functionsAs(it.abilitySubject(poisonTarget), "Virgin")
    } == true && virginFirstNomination
    val virginRegistrationKey = nominatorCard
        ?.takeIf { it.name == spyCard?.name && virginFirstNomination }
        ?.let { registrationKey("Virgin", it.name) }
    val virginSpyRegistersGood = registrationState.spyIsGood(virginRegistrationKey)
    val virginExecutes = virginAbilityWorks &&
        (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || virginSpyRegistersGood)
    val specialNotice = when {
        virginExecutes -> text(
            "${playerSeatLabel(cards, nomineeName)} 首次被登记为镇民的玩家提名：不进行投票，提名者将立即被处决。",
            "${playerSeatLabel(cards, nomineeName)} was first nominated by a player registering as Townsfolk: skip voting and execute the nominator.",
        )
        virginFirstNomination -> text(
            "这是圣女第一次被提名，但能力不会处决提名者；记录能力已用过后继续投票。",
            "This is the Virgin's first nomination, but the ability does not execute the nominator. Mark it spent and continue.",
        )
        else -> null
    }
    ClocktowerPendingNominationTableScreen(
        round = round,
        compact = false,
        cards = cards,
        tableState = tableState,
        executionThreshold = executionThreshold,
        nominatorName = nominatorName,
        nomineeName = nomineeName,
        specialNotice = specialNotice,
        specialNoticeIsDanger = virginExecutes,
        continueLabel = when {
            virginExecutes -> text("确认并处决提名者", "Confirm and execute nominator")
            virginFirstNomination -> text("记录能力，进入投票", "Record ability and continue")
            else -> text("开始投票", "Start voting")
        },
        actionsEnabled = actionsEnabled,
        onHostTools = onHostTools,
        onContinue = {
            val chosenNominator = nominatorName
            val chosenNominee = nomineeName
            if (chosenNominator != null && chosenNominee != null) {
                val explicitSpyRuling = registrationState.explicitChoice(
                    ClocktowerRegistrationSubject.SPY, virginRegistrationKey,
                )
                onConfirm(ClocktowerNominationConfirmation(
                    chosenNominator, chosenNominee, virginFirstNomination, virginExecutes,
                    explicitSpyRuling?.usesSpecialRegistration,
                ))
            }
        },
        onCancel = {
            draft.clear()
            onCancel()
        },
        specialContent = {
            if (virginRegistrationKey != null && spyCard != null) {
                ClocktowerSpyTownsfolkTypeDecisionControls(
                    registersAsTownsfolk = registrationState.spyIsGood(virginRegistrationKey),
                    hasExplicitChoice = registrationState.spyHasExplicitChoice(virginRegistrationKey),
                    enabled = actionsEnabled,
                    language = language,
                    onRegistersAsTownsfolkChange = { chosen ->
                        registrationState.chooseSpy(virginRegistrationKey, chosen)
                    },
                )
            }
        },
    )
}
