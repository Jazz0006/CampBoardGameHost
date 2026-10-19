package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal enum class TwoPlayerSelectionAction {
    ToggleFirst,
    ToggleSecond,
    RejectLimit,
}

/**
 * A real player target action is not confirmable until the live Host-legal
 * target is explicitly selected. The same gate protects manual Next and the
 * automatic mode's fresh selection continuation; absence is never a no-op.
 * Inactive/illustrative night steps can still be skipped normally.
 */
internal fun clocktowerLivePlayerTargetReady(
    action: ClocktowerNightAction,
    isRealAction: Boolean,
    selectedName: String?,
    legalNames: Set<String>,
): Boolean = !isRealAction || action !in setOf(
    ClocktowerNightAction.Poison,
    ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect,
    ClocktowerNightAction.DemonKill,
) || (selectedName != null && selectedName in legalNames)

/** A retained draft, or a previously visited step, must never trigger automatic confirmation. */
internal fun clocktowerShouldAutoConfirmFreshPlayerTarget(
    automaticMode: Boolean,
    action: ClocktowerNightAction,
    isRealAction: Boolean,
    freshTarget: String?,
    selectedName: String?,
    legalNames: Set<String>,
    playerDisplayOpen: Boolean,
): Boolean = automaticMode && isRealAction && !playerDisplayOpen &&
    action in setOf(
        ClocktowerNightAction.Poison,
        ClocktowerNightAction.ButlerMaster,
        ClocktowerNightAction.MonkProtect,
        ClocktowerNightAction.DemonKill,
    ) &&
    freshTarget != null && selectedName == freshTarget &&
    clocktowerLivePlayerTargetReady(action, isRealAction, selectedName, legalNames)

internal fun clocktowerToggledSingleTargetSelection(
    currentSelection: String?,
    tappedSelection: String,
): String? = tappedSelection.takeUnless { it == currentSelection }

internal fun twoPlayerSelectionAction(
    first: String?,
    second: String?,
    selectedName: String,
): TwoPlayerSelectionAction = when {
    selectedName == first -> TwoPlayerSelectionAction.ToggleFirst
    selectedName == second -> TwoPlayerSelectionAction.ToggleSecond
    first == null -> TwoPlayerSelectionAction.ToggleFirst
    second == null -> TwoPlayerSelectionAction.ToggleSecond
    else -> TwoPlayerSelectionAction.RejectLimit
}

internal data class RevalidatedTwoPlayerSelection(
    val first: String?,
    val second: String?,
) {
    val isComplete: Boolean
        get() = first != null && second != null && first != second
}

internal fun revalidateTwoPlayerSelection(
    first: String?,
    second: String?,
    eligibleNames: Set<String>,
): RevalidatedTwoPlayerSelection {
    val revalidatedFirst = first?.takeIf { it in eligibleNames }
    val revalidatedSecond = second?.takeIf { it in eligibleNames && it != revalidatedFirst }
    return RevalidatedTwoPlayerSelection(
        first = revalidatedFirst,
        second = revalidatedSecond,
    )
}

/**
 * Only a genuinely current, Host-resolved information result may enter the
 * normal reveal flow without a human information-choice click. For player-query
 * steps, the player-selected subjects MUST exist first; choosing those subjects
 * is never an AI decision. Already published results never auto-reveal again.
 * No parallel execution state: the Session history is authority.
 */
internal fun clocktowerShouldAutoPresentCurrentInformation(
    automaticMode: Boolean,
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    actorSeat: Int?,
    hasCurrentLegalResult: Boolean,
    playerQueryResolved: Boolean = false,
    confirmedObservations: List<RecordedEpistemicObservation>,
): Boolean = automaticMode &&
    step.isRealAction &&
    (step.action == ClocktowerNightAction.None ||
        (step.action in setOf(
            ClocktowerNightAction.FortuneTeller,
            ClocktowerNightAction.Chambermaid,
            ClocktowerNightAction.Ravenkeeper,
        ) && playerQueryResolved)) &&
    step.actor != null &&
    actorSeat != null &&
    step.roleEnName != null &&
    hasCurrentLegalResult &&
    confirmedObservations.none { received ->
        received.phase == phase.toStorytellerPhase() &&
            received.round == round &&
            received.visibility == ObservationVisibility.PRIVATE &&
            received.sourceSeat == actorSeat &&
            received.sourceAbility?.value == step.roleEnName
    }

/**
 * Player-chosen targets are drafts until their information is actually shown.
 * Once one result has been published in the Session, it may only be reopened
 * with the identical typed result. A new target/result must not silently add a
 * second incompatible observation for the same actor, ability, phase and night.
 */
internal enum class ClocktowerLiveQueryRevealDisposition {
    NEW,
    REOPEN,
    CONFLICT,
}

internal fun clocktowerLiveQueryRevealDisposition(
    action: ClocktowerNightAction,
    phase: ClocktowerPhase,
    round: Int,
    actorSeat: Int?,
    roleEnName: String?,
    shown: InformationProposition?,
    confirmedObservations: List<RecordedEpistemicObservation>,
): ClocktowerLiveQueryRevealDisposition {
    if (action !in setOf(
            ClocktowerNightAction.FortuneTeller,
            ClocktowerNightAction.Chambermaid,
            ClocktowerNightAction.Ravenkeeper,
        )
    ) return ClocktowerLiveQueryRevealDisposition.NEW
    if (shown == null || actorSeat == null || roleEnName == null) {
        return ClocktowerLiveQueryRevealDisposition.CONFLICT
    }
    val previous = confirmedObservations.filter { it.phase == phase.toStorytellerPhase() &&
        it.round == round && it.visibility == ObservationVisibility.PRIVATE &&
        it.sourceSeat == actorSeat && it.sourceAbility?.value == roleEnName
    }
    if (previous.isEmpty()) return ClocktowerLiveQueryRevealDisposition.NEW
    return if (previous.all { it.proposition == shown }) {
        ClocktowerLiveQueryRevealDisposition.REOPEN
    } else ClocktowerLiveQueryRevealDisposition.CONFLICT
}

/** A previously published observation never becomes a fresh auto-advance permission
 * just because its player display was manually reopened. */
internal fun clocktowerShouldAutoAdvanceAfterFreshReveal(
    automaticMode: Boolean,
    phase: ClocktowerPhase,
    currentStep: ClocktowerNightStepUi,
    revealedStep: ClocktowerNightStepUi,
    actorSeat: Int?,
    alreadyPublished: Boolean,
    handoff: ClocktowerPlayerRevealHandoff,
): Boolean = automaticMode &&
    phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
    currentStep.isRealAction && actorSeat != null &&
    currentStep.actor?.name != null &&
    currentStep.actor?.name == revealedStep.actor?.name &&
    currentStep.roleEnName != null && currentStep.roleEnName == revealedStep.roleEnName &&
    !alreadyPublished && handoff.openReveal && handoff.recordPublication

/** Advice status may suppress only its own exact current Host decision. */
internal fun clocktowerCurrentAiRequestBlocked(
    currentKey: String?,
    adviceKey: String?,
    busy: Boolean,
    error: String?,
): Boolean = currentKey != null && adviceKey == currentKey &&
    (busy || error != null)

internal fun shouldAutoAdvanceRedHerring(
    allowRuleDeterminedAutoUi: Boolean,
    isRedHerringStep: Boolean,
    isRealAction: Boolean,
    hasSelectedRedHerring: Boolean,
    acceptedJointOpening: Boolean = false,
): Boolean = allowRuleDeterminedAutoUi &&
    isRedHerringStep &&
    // A real choice is skippable only when the Host already accepted and applied
    // the single joint opening; an arbitrary manual/legacy selection is not enough.
    (!isRealAction || (acceptedJointOpening && hasSelectedRedHerring))

internal data class ClocktowerDisplayOption(
    val label: String,
    val displayKind: ClocktowerDisplayKind,
    val displayTitle: String,
    val displayPrimary: String?,
    val displaySecondary: String?,
    val displayFooter: String?,
    /** Exact player-visible statement; never reconstruct it from localized display strings. */
    val proposition: InformationProposition? = null,
    /**
     * Typed seat identity used only to render spatial presentation. This is deliberately separate
     * from [proposition]: misleading information may need seat highlights without becoming an
     * epistemic observation.
     */
    val presentationSubjectSeats: List<Int> = emptyList(),
    /** Role-result UI meaning only; never asserts an observed or game-truth proposition. */
    val presentationRoleId: String? = null,
    val spyRegistersGood: Boolean? = null,
    val spyRegisteredRoleEnName: String? = null,
    val recluseRegistersEvil: Boolean? = null,
    val recluseRegisteredRoleEnName: String? = null,
    /** Legal explanations, not committed facts. Result-first choices never set a witness implicitly. */
    val isTruthful: Boolean = true,
    val misinformationPressure: Int = 0,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

/** Canonical semantic ID shared by legacy, unified-pool and first-night shadow paths. */
internal fun clocktowerInformationCandidateId(option: ClocktowerDisplayOption): String = listOf(
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
).joinToString("|").let { baseline ->
    // Leave every unrelated established candidate ID unchanged.
    option.presentationRoleId?.let { "$baseline|shownRole:$it" } ?: baseline
}
