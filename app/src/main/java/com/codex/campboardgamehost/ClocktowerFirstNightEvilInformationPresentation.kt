package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

internal data class ClocktowerFirstNightEvilInformationSteps(
    val minion: ClocktowerNightStepUi,
    val demon: ClocktowerNightStepUi,
)

/**
 * Fully localized copy prepared by the presentation adapter.
 *
 * This contains no canonical state and no recommendation/legality authority. The pure builder below
 * only gates already-prepared content onto the two existing first-night UI steps.
 */
internal data class ClocktowerFirstNightEvilInformationPreparedText(
    val minionTitle: String,
    val demonTitle: String,
    val smallGameNoEvilInfoReason: String,
    val noMinionsReason: String,
    val noDemonReason: String,
    val placeholderAction: String,
    val minionActionText: String,
    val minionTellPlayer: String?,
    val minionExplain: String,
    val smallGameNoEvilInfoExplain: String,
    val minionDisplayPrimary: String?,
    val minionWakeText: String,
    val demonActionText: String,
    val demonTellPlayer: String?,
    val demonExplain: String,
    val demonDisplayPrimary: String,
    val demonDisplaySecondary: String?,
)

/**
 * Presentation-only first-night evil-information seam.
 *
 * Actor membership, small-game eligibility, bluff commitment/legality, and interaction ordering are
 * all supplied by upstream owners. This function never inspects role/team truth or mutates state.
 */
internal fun buildClocktowerFirstNightEvilInformationSteps(
    minionActor: PlayerCard?,
    demonActor: PlayerCard?,
    shouldGiveInformation: Boolean,
    text: ClocktowerFirstNightEvilInformationPreparedText,
): ClocktowerFirstNightEvilInformationSteps {
    val minionActive = minionActor != null && shouldGiveInformation
    val demonActive = demonActor != null && shouldGiveInformation

    return ClocktowerFirstNightEvilInformationSteps(
        minion = ClocktowerNightStepUi(
            title = text.minionTitle,
            actor = minionActor.takeIf { shouldGiveInformation },
            isRealAction = minionActive,
            reason = when {
                !shouldGiveInformation -> text.smallGameNoEvilInfoReason
                minionActor == null -> text.noMinionsReason
                else -> ""
            },
            storytellerAction = if (minionActive) text.minionActionText else text.placeholderAction,
            tellPlayer = text.minionTellPlayer.takeIf { minionActive },
            explanation = if (shouldGiveInformation) text.minionExplain else text.smallGameNoEvilInfoExplain,
            displayKind = if (minionActive && text.minionTellPlayer != null) {
                ClocktowerDisplayKind.EvilInfo
            } else {
                ClocktowerDisplayKind.None
            },
            displayTitle = text.minionTitle,
            displayPrimary = text.minionDisplayPrimary.takeIf { minionActive },
            displayFooter = null,
            wakeText = text.minionWakeText.takeIf { minionActive },
        ),
        demon = ClocktowerNightStepUi(
            title = text.demonTitle,
            actor = demonActor.takeIf { shouldGiveInformation },
            isRealAction = demonActive,
            reason = when {
                !shouldGiveInformation -> text.smallGameNoEvilInfoReason
                demonActor == null -> text.noDemonReason
                else -> ""
            },
            storytellerAction = if (demonActive) text.demonActionText else text.placeholderAction,
            tellPlayer = text.demonTellPlayer.takeIf { demonActive },
            explanation = if (shouldGiveInformation) text.demonExplain else text.smallGameNoEvilInfoExplain,
            displayKind = if (demonActive) ClocktowerDisplayKind.EvilInfo else ClocktowerDisplayKind.None,
            displayTitle = text.demonTitle,
            displayPrimary = text.demonDisplayPrimary.takeIf { demonActive },
            displaySecondary = text.demonDisplaySecondary.takeIf { demonActive },
            displayFooter = null,
        ),
    )
}

@Composable
internal fun clocktowerFirstNightEvilInformationSteps(
    minionActor: PlayerCard?,
    demonActor: PlayerCard?,
    minionSeatLabels: List<String>,
    demonSeatLabel: String?,
    shouldGiveInformation: Boolean,
    demonBluffPresentation: DemonBluffPresentationResolution,
    language: String,
): ClocktowerFirstNightEvilInformationSteps {
    val separator = stringResource(R.string.name_separator)
    val minionSeats = minionSeatLabels.joinToString(separator)
    val readyBluffs = demonBluffPresentation as? DemonBluffPresentationResolution.Ready
    val bluffNames = readyBluffs
        ?.roles
        ?.joinToString(separator) { it.nameFor(language) }

    val minionTellPlayer = demonSeatLabel?.let {
        stringResource(R.string.clocktower_first_night_minion_info_format, it)
    }
    val demonMinionsLine = if (minionSeatLabels.isEmpty()) {
        stringResource(R.string.clocktower_first_night_demon_no_minions)
    } else {
        stringResource(R.string.clocktower_first_night_demon_minions_format, minionSeats)
    }
    val demonBluffsLine = bluffNames?.let {
        stringResource(R.string.clocktower_first_night_demon_bluffs_format, it)
    }
    val demonTellPlayer = if (readyBluffs != null) {
        listOf(demonMinionsLine, requireNotNull(demonBluffsLine)).joinToString("\n")
    } else {
        null
    }
    val demonExplain = when (demonBluffPresentation) {
        is DemonBluffPresentationResolution.Ready ->
            stringResource(R.string.clocktower_first_night_demon_explain)
        DemonBluffPresentationResolution.Pending ->
            if (language == "en") {
                "Preparing Demon bluffs. Reveal is enabled when the recommendation is ready."
            } else {
                "正在准备恶魔伪装身份，推荐完成后即可展示。"
            }
        is DemonBluffPresentationResolution.Invalid ->
            if (language == "en") {
                "Demon bluff recommendation is invalid; incorrect reveal has been blocked."
            } else {
                "恶魔伪装身份推荐无效，已阻止错误信息展示。"
            }
    }

    return buildClocktowerFirstNightEvilInformationSteps(
        minionActor = minionActor,
        demonActor = demonActor,
        shouldGiveInformation = shouldGiveInformation,
        text = ClocktowerFirstNightEvilInformationPreparedText(
            minionTitle = stringResource(R.string.clocktower_first_night_minion_title),
            demonTitle = stringResource(R.string.clocktower_first_night_demon_title),
            smallGameNoEvilInfoReason =
                stringResource(R.string.clocktower_first_night_small_game_no_evil_info_reason),
            noMinionsReason = stringResource(R.string.clocktower_first_night_no_minions_reason),
            noDemonReason = stringResource(R.string.clocktower_first_night_no_demon_reason),
            placeholderAction = stringResource(R.string.clocktower_first_night_placeholder_action),
            minionActionText = stringResource(
                R.string.clocktower_first_night_minion_action_format,
                minionSeats,
            ),
            minionTellPlayer = minionTellPlayer,
            minionExplain = stringResource(R.string.clocktower_first_night_minion_explain),
            smallGameNoEvilInfoExplain =
                stringResource(R.string.clocktower_first_night_small_game_no_evil_info_explain),
            minionDisplayPrimary =
                "${stringResource(R.string.clocktower_evil_display_demon)}\n${demonSeatLabel.orEmpty()}",
            minionWakeText = stringResource(
                R.string.clocktower_first_night_minion_wake_format,
                minionSeats,
            ),
            demonActionText = stringResource(
                R.string.clocktower_first_night_demon_action_format,
                demonSeatLabel.orEmpty(),
            ),
            demonTellPlayer = demonTellPlayer,
            demonExplain = demonExplain,
            demonDisplayPrimary =
                "${stringResource(R.string.clocktower_evil_display_minions)}\n${
                    if (minionSeatLabels.isEmpty()) {
                        stringResource(R.string.clocktower_first_night_demon_no_minions)
                    } else {
                        minionSeats
                    }
                }",
            demonDisplaySecondary = bluffNames?.let {
                "${stringResource(R.string.clocktower_evil_display_bluffs)}\n$it"
            },
        ),
    )
}
