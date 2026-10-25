package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/**
 * UI-only seat selection for exactly one night interaction of one night. Interaction IDs carry
 * no round, so the identity includes phase and round. Never persisted or recorded as an event;
 * only that step's Next confirms it through the Session.
 */
internal data class ClocktowerNightStepDraft(
    val gameId: String,
    val phase: StorytellerPhase,
    val round: Int,
    val interactionId: ClocktowerInteractionId,
    /** Two-player queries keep fixed first/second slots; a cleared slot is refilled by the next tap. */
    val seatSlots: List<Int?> = emptyList(),
) {
    val selectedSeats: List<Int> get() = seatSlots.filterNotNull()
}

private val twoSeatNightActions = setOf(ClocktowerNightAction.FortuneTeller, ClocktowerNightAction.Chambermaid)

/** Night actions whose current step records live seat taps in the draft. */
internal val clocktowerNightSeatDraftActions = twoSeatNightActions + setOf(
    ClocktowerNightAction.RedHerring, ClocktowerNightAction.Poison, ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect, ClocktowerNightAction.DemonKill, ClocktowerNightAction.MayorRedirect,
    ClocktowerNightAction.Ravenkeeper,
)

/** Host-page selection policy: actual good players, with the owner's Recluse exclusion. */
internal fun clocktowerRedHerringSelectableSeats(
    game: com.codex.campboardgamehost.clocktower.domain.GameState,
): Set<Int> = game.players.filter {
    it.actualAlignment == com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD &&
        it.actualRole.value != "Recluse"
}.mapTo(linkedSetOf()) { it.seat }

/** One seat tap: two fixed slots for two-player queries, otherwise a single toggled target; null rejects a third seat. */
internal fun ClocktowerNightStepDraft.withTappedSeat(action: ClocktowerNightAction, seat: Int): ClocktowerNightStepDraft? {
    if (action !in twoSeatNightActions) {
        return copy(seatSlots = if (selectedSeats.firstOrNull() == seat) emptyList() else listOf(seat))
    }
    val first = seatSlots.getOrNull(0)
    val second = seatSlots.getOrNull(1)
    return copy(seatSlots = when {
        seat == first -> listOf(null, second)
        seat == second -> listOf(first, null)
        first == null -> listOf(seat, second)
        second == null -> listOf(first, seat)
        else -> return null
    })
}

/** The current step's Next request; [ClocktowerGameSession.confirmNightStep] is its only Session dispatcher. */
internal data class ClocktowerNightStepConfirmation(
    val step: ClocktowerNightStepUi,
    val draft: ClocktowerNightStepDraft,
    val successorSeat: Int? = null,
    val previewRevision: StorytellerDecisionRevision? = null,
    val observation: com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft? = null,
)

/**
 * A real player target action is not confirmable until the live Host-legal
 * target is explicitly selected. The same gate protects manual Next and the
 * automatic mode's fresh selection continuation; absence is never a no-op.
 * Inactive/illustrative night steps can still be skipped normally.
 */
internal fun clocktowerLivePlayerTargetReady(
    action: ClocktowerNightAction,
    isRealAction: Boolean,
    selectedSeat: Int?,
    legalSeats: Set<Int>,
): Boolean = !isRealAction || action !in setOf(
    ClocktowerNightAction.Poison,
    ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect,
    ClocktowerNightAction.DemonKill,
) || (selectedSeat != null && selectedSeat in legalSeats)

internal data class RevalidatedTwoPlayerSelection<T>(
    val first: T?,
    val second: T?,
) {
    val isComplete: Boolean
        get() = first != null && second != null && first != second
}

internal fun <T> revalidateTwoPlayerSelection(
    first: T?,
    second: T?,
    eligible: Set<T>,
): RevalidatedTwoPlayerSelection<T> {
    val revalidatedFirst = first?.takeIf { it in eligible }
    val revalidatedSecond = second?.takeIf { it in eligible && it != revalidatedFirst }
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
