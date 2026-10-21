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

/** Advice status may suppress only its own exact current Host decision. */
internal fun clocktowerCurrentAiRequestBlocked(
    currentKey: String?,
    adviceKey: String?,
    busy: Boolean,
    error: String?,
): Boolean = currentKey != null && adviceKey == currentKey &&
    (busy || error != null)

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
