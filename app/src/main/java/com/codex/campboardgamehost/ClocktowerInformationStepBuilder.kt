package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules

internal class ClocktowerInformationStepBuilder(
    private val cards: List<PlayerCard>,
    private val language: String,
    private val automaticStorytellerInfo: Boolean,
    private val text: (String, String) -> String,
    private val roleActor: (String) -> PlayerCard?,
    private val roleMissingReason: (String) -> String,
    private val abilityStateFor: (String, PlayerCard?) -> AbilityFunctioningState?,
    private val actorIsUnreliable: (String, PlayerCard?) -> Boolean,
    private val recentMisinformationStreak: (PlayerCard?) -> Int,
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
        displayProposition: InformationProposition? = null,
        hostInstruction: String? = null,
        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        reliableDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        previousShownNumber: Int? = null,
        spyRegistrationKey: String? = null,
        spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
        spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
        spyRegistrationHint: String? = null,
        recluseRegistrationKey: String? = null,
        recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    ): ClocktowerNightStepUi {
        val actor = roleActor(enName)
        val localizedRoleName = if (language == "en") enName else roleName
        val localizedDisplayTitle = displayTitle ?: text("$roleName 信息", "$enName information")
        val abilityState = abilityStateFor(enName, actor)
        val actorIsDrunkShownRole = abilityState == AbilityFunctioningState.DRUNK
        val informationReliability = when (abilityState) {
            AbilityFunctioningState.POISONED -> InformationReliability.POISONED
            AbilityFunctioningState.DRUNK -> InformationReliability.DRUNK
            else -> InformationReliability.RELIABLE
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
        val actorAbilityUnreliable = actor != null && actorIsUnreliable(enName, actor)
        val unreliableOptions = actor?.takeIf { actorAbilityUnreliable }?.let(displayOptions).orEmpty()
        val reliableRecommendations = actor?.takeUnless { actorAbilityUnreliable }?.let(reliableDisplayOptions).orEmpty()
        val automaticRecommendations = when {
            !automaticStorytellerInfo -> reliableRecommendations
            actorAbilityUnreliable -> unreliableOptions
            else -> reliableRecommendations
        }
        val resolvedDisplayKind = when (enName) {
            "Chef", "Empath", "Clockmaker", "Chambermaid" -> ClocktowerDisplayKind.Number
            "Fortune Teller" -> ClocktowerDisplayKind.YesNo
            "Ravenkeeper", "Undertaker" -> ClocktowerDisplayKind.RoleReveal
            "Washerwoman", "Librarian", "Investigator" -> ClocktowerDisplayKind.EitherOne
            else -> displayKind
        }
        val directLegacyCandidate = actor
            ?.takeIf { unreliableOptions.isEmpty() && !tellPlayer.isNullOrBlank() }
            ?.let {
                ClocktowerDisplayOption(
                    label = "direct-legacy",
                    displayKind = resolvedDisplayKind,
                    displayTitle = localizedDisplayTitle,
                    displayPrimary = displayPrimary ?: tellPlayer,
                    displaySecondary = displaySecondary,
                    displayFooter = displayFooter ?: explanation,
                    proposition = displayProposition,
                )
            }
        val completeLegacyCandidates = (unreliableOptions + reliableRecommendations + listOfNotNull(directLegacyCandidate))
            .distinctBy { option ->
                listOf(
                    option.displayKind.name,
                    option.proposition?.toString().orEmpty(),
                    option.displayPrimary.orEmpty(),
                    option.displaySecondary.orEmpty(),
                    option.displayFooter.orEmpty(),
                    option.spyRegistersGood?.toString().orEmpty(),
                    option.spyRegisteredRoleEnName.orEmpty(),
                    option.recluseRegistersEvil?.toString().orEmpty(),
                    option.recluseRegisteredRoleEnName.orEmpty(),
                    option.isTruthful.toString(),
                ).joinToString("|")
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
            tellPlayer = if (actor != null && unreliableOptions.isEmpty()) tellPlayer else null,
            explanation = listOfNotNull(explanation, hostUnreliableNote).joinToString("\n"),
            action = action,
            displayKind = if (actor != null && unreliableOptions.isEmpty() && !tellPlayer.isNullOrBlank()) resolvedDisplayKind else ClocktowerDisplayKind.None,
            displayTitle = localizedDisplayTitle,
            displayProposition = displayProposition,
            displayPrimary = if (actor != null && unreliableOptions.isEmpty()) displayPrimary ?: tellPlayer else null,
            displaySecondary = if (actor != null && unreliableOptions.isEmpty()) displaySecondary else null,
            displayFooter = if (actor != null && unreliableOptions.isEmpty()) displayFooter ?: explanation else null,
            displayOptions = if (automaticStorytellerInfo) emptyList() else unreliableOptions,
            recommendedDisplayOptions = automaticRecommendations,
            legacyInformationCandidates = completeLegacyCandidates,
            roleEnName = enName,
            informationReliability = informationReliability,
            recentMisinformationStreak = recentMisinformationStreak(actor),
            previousShownNumber = previousShownNumber,
            spyRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                spyRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            spyRegistrationTeams = spyRegistrationTeams,
            spyRegistrationDetail = spyRegistrationDetail,
            spyRegistrationHint = spyRegistrationHint,
            recluseRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                recluseRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            recluseRegistrationTeams = recluseRegistrationTeams,
        )
    }
}
