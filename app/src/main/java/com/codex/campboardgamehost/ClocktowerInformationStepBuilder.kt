package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState

internal class ClocktowerInformationStepBuilder(
    private val cards: List<PlayerCard>,
    private val language: String,
    private val text: (String, String) -> String,
    private val roleActor: (String) -> PlayerCard?,
    private val roleMissingReason: (String) -> String,
    private val abilityStateFor: (String, PlayerCard?) -> AbilityFunctioningState?,
) {
    fun build(
        roleName: String,
        enName: String,
        tellPlayer: String?,
        explanation: String,
        action: ClocktowerNightAction = ClocktowerNightAction.None,
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.Plain,
        displayPrimary: String? = tellPlayer,
        displaySecondary: String? = null,
        displayFooter: String? = explanation,
        displayTitle: String? = null,
        informationQuestion: ClocktowerInformationQuestion? = null,
        displayProposition: InformationProposition? = null,
        presentationSubjectSeats: List<Int> = emptyList(),
        hostInstruction: String? = null,
        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        previousShownNumber: Int? = null,
        numericMinimumValue: Int? = null,
        numericMaximumValue: Int? = null,
        actorOverride: PlayerCard? = null,
        abilityStateOverride: AbilityFunctioningState? = null,
    ): ClocktowerNightStepUi {
        val actor = actorOverride ?: roleActor(enName)
        val localizedRoleName = if (language == "en") enName else roleName
        val localizedDisplayTitle = displayTitle ?: text("$roleName 信息", "$enName information")
        val abilityState = abilityStateOverride ?: abilityStateFor(enName, actor)
        val actorIsDrunkShownRole = abilityState == AbilityFunctioningState.DRUNK
        val informationReliability = when (abilityState) {
            AbilityFunctioningState.POISONED -> ReliabilityState.POISONED
            AbilityFunctioningState.DRUNK -> ReliabilityState.DRUNK
            else -> ReliabilityState.RELIABLE
        }
        val hostUnreliableNote = if (actorIsDrunkShownRole) {
            text(
                "注意：这名玩家真实身份是酒鬼，显示为$roleName。请照常唤醒并给信息，但信息可以不可靠或完全错误。",
                "This player is actually the Drunk but appears as $enName. Wake them normally and give information that may be unreliable or entirely false.",
            )
        } else if (abilityState == AbilityFunctioningState.POISONED) {
            text(
                "注意：这名玩家今晚中毒，能力信息可以不可靠或错误。",
                "This player is poisoned tonight, so their ability information may be unreliable or false.",
            )
        } else {
            null
        }
        val options = actor?.let(displayOptions).orEmpty().distinctBy(::clocktowerInformationCandidateId)
        val resolvedDisplayKind = when (enName) {
            "Chef", "Empath", "Clockmaker", "Chambermaid" -> ClocktowerDisplayKind.Number
            "Fortune Teller" -> ClocktowerDisplayKind.YesNo
            "Ravenkeeper", "Undertaker" -> ClocktowerDisplayKind.RoleReveal
            "Washerwoman", "Librarian", "Investigator" -> ClocktowerDisplayKind.EitherOne
            else -> displayKind
        }
        return ClocktowerNightStepUi(
            title = localizedRoleName,
            actor = actor,
            isRealAction = actor != null,
            reason = if (actor != null) "" else roleMissingReason(enName),
            storytellerAction = if (actor != null) {
                listOfNotNull(
                    hostInstruction ?: text(
                        "轻拍 ${actor.seatLabel(cards)}，示意睁眼。把本步骤信息只给他看；看完后收回手机，示意闭眼。",
                        "Tap ${actor.seatLabel(cards)} to wake them. Show this information only to that player, then take the phone back and signal them to close their eyes.",
                    ),
                    hostUnreliableNote,
                ).joinToString("\n")
            } else {
                text(
                    "不要唤醒任何玩家。为了避免玩家通过流程判断角色是否在场，请停顿 2-3 秒，然后点击下一步。",
                    "Do not wake anyone. Pause for 2–3 seconds so players cannot infer whether this character is in play, then continue.",
                )
            },
            tellPlayer = if (actor != null) tellPlayer else null,
            explanation = listOfNotNull(explanation, hostUnreliableNote).joinToString("\n"),
            action = action,
            displayKind = if (actor != null && !tellPlayer.isNullOrBlank()) resolvedDisplayKind else ClocktowerDisplayKind.None,
            displayTitle = localizedDisplayTitle,
            informationQuestion = informationQuestion,
            displayProposition = displayProposition,
            presentationSubjectSeats = if (actor != null) presentationSubjectSeats else emptyList(),
            displayPrimary = if (actor != null) displayPrimary ?: tellPlayer else null,
            displaySecondary = if (actor != null) displaySecondary else null,
            displayFooter = if (actor != null) displayFooter ?: explanation else null,
            displayOptions = options,
            manualInformationCandidates = options,
            automaticInformationCandidates = options,
            roleEnName = enName,
            informationReliability = informationReliability,
            previousShownNumber = previousShownNumber,
            numericMinimumValue = numericMinimumValue,
            numericMaximumValue = numericMaximumValue,
        )
    }
}
