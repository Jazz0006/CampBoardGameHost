from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
HELPER = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentation.kt")


def replace_exact(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{path}: expected exactly one anchor, found {count}")
    path.write_text(text.replace(old, new), encoding="utf-8")


HELPER.write_text(
    '''package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector

internal sealed interface DemonBluffPresentationResolution {
    data class Ready(val roles: List<ClocktowerRole>) : DemonBluffPresentationResolution

    data object Pending : DemonBluffPresentationResolution

    data class Invalid(
        val requestedRoleNames: List<String>,
        val unresolvedRoleNames: List<String>,
    ) : DemonBluffPresentationResolution
}

/**
 * Returns the exact Demon bluff recommendation that presentation is allowed to consume.
 *
 * AUTO uses the already-applied setup decision so presentation never outruns the state commit.
 * MANUAL has no setup-plan apply step, so it consumes the default BALANCED setup recommendation
 * directly once that recommendation is ready. Other setup decisions remain manual Storyteller
 * authority and are not applied by this projection.
 */
internal fun demonBluffRoleNamesForPresentation(
    automaticStorytellerInfo: Boolean,
    appliedRoleNames: List<String>,
    setupPlans: List<RecommendationPlan>,
    preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED,
): List<String>? {
    appliedRoleNames.takeIf { it.isNotEmpty() }?.let { return it }
    if (automaticStorytellerInfo) return null

    val selectedPlan = WeightedStableSelector.selectStyle(
        options = setupPlans,
        style = preferredManualStyle,
        styleOf = RecommendationPlan::style,
    ) ?: return null

    return selectedPlan.decisions
        .filterIsInstance<StorytellerDecision.DemonBluffs>()
        .singleOrNull()
        ?.roles
        ?.map { it.value }
}

/**
 * Resolves one exact recommended triple against current legal script roles.
 *
 * Missing recommendation is pending. Partial, duplicate, illegal or unresolvable identities are
 * invalid. Neither state is silently replaced with an arbitrary legal triple.
 */
internal fun resolveDemonBluffPresentation(
    recommendedRoleNames: List<String>?,
    legalRoles: List<ClocktowerRole>,
): DemonBluffPresentationResolution {
    val requested = recommendedRoleNames ?: return DemonBluffPresentationResolution.Pending
    if (requested.size != 3 || requested.distinct().size != 3) {
        return DemonBluffPresentationResolution.Invalid(
            requestedRoleNames = requested,
            unresolvedRoleNames = emptyList(),
        )
    }

    val legalByName = legalRoles.associateBy(ClocktowerRole::enName)
    val unresolved = requested.filterNot(legalByName::containsKey)
    if (unresolved.isNotEmpty()) {
        return DemonBluffPresentationResolution.Invalid(
            requestedRoleNames = requested,
            unresolvedRoleNames = unresolved,
        )
    }

    return DemonBluffPresentationResolution.Ready(requested.map(legalByName::getValue))
}
''',
    encoding="utf-8",
)

replace_exact(
    HOST,
    '''    val appliedDemonBluffs = recommendedDemonBluffRoleNames
        .mapNotNull { roleName -> legalDemonBluffs.firstOrNull { it.enName == roleName } }
        .distinctBy(ClocktowerRole::enName)
    val demonBluffs = if (appliedDemonBluffs.size == 3) appliedDemonBluffs else legalDemonBluffs.take(3)
''',
    '''    val setupPlansForDemonBluffs =
        (recommendationUiState as? RecommendationUiState.Ready)?.plans.orEmpty()
    val demonBluffRoleNames = demonBluffRoleNamesForPresentation(
        automaticStorytellerInfo = automaticStorytellerInfo,
        appliedRoleNames = recommendedDemonBluffRoleNames,
        setupPlans = setupPlansForDemonBluffs,
    )
    val demonBluffPresentation = resolveDemonBluffPresentation(
        recommendedRoleNames = demonBluffRoleNames,
        legalRoles = legalDemonBluffs,
    )
    val demonBluffs =
        (demonBluffPresentation as? DemonBluffPresentationResolution.Ready)?.roles.orEmpty()
''',
)

replace_exact(
    HOST,
    '''    val demonInfoText = buildList {
        add(
            if (minionCards.isEmpty()) {
                stringResource(R.string.clocktower_first_night_demon_no_minions)
            } else {
                stringResource(
                    R.string.clocktower_first_night_demon_minions_format,
                    minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                )
            },
        )
        add(
            stringResource(
                R.string.clocktower_first_night_demon_bluffs_format,
                demonBluffs.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) },
            ),
        )
    }.joinToString("\\n")
''',
    '''    val demonInfoText =
        (demonBluffPresentation as? DemonBluffPresentationResolution.Ready)?.let { ready ->
            buildList {
                add(
                    if (minionCards.isEmpty()) {
                        stringResource(R.string.clocktower_first_night_demon_no_minions)
                    } else {
                        stringResource(
                            R.string.clocktower_first_night_demon_minions_format,
                            minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                        )
                    },
                )
                add(
                    stringResource(
                        R.string.clocktower_first_night_demon_bluffs_format,
                        ready.roles.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) },
                    ),
                )
            }.joinToString("\\n")
        }
''',
)

replace_exact(
    HOST,
    '''    val firstNightDemonExplain =
        stringResource(R.string.clocktower_first_night_demon_explain)
''',
    '''    val firstNightDemonExplain = when (demonBluffPresentation) {
        is DemonBluffPresentationResolution.Ready ->
            stringResource(R.string.clocktower_first_night_demon_explain)
        DemonBluffPresentationResolution.Pending ->
            text("正在准备恶魔伪装身份，推荐完成后即可展示。", "Preparing Demon bluffs. Reveal is enabled when the recommendation is ready.")
        is DemonBluffPresentationResolution.Invalid ->
            text("恶魔伪装身份推荐无效，已阻止错误信息展示。", "Demon bluff recommendation is invalid; incorrect reveal has been blocked.")
    }
''',
)

replace_exact(
    HOST,
    '''    val firstNightDemonDisplaySecondary =
        if (demonCard != null && shouldGiveFirstNightEvilInfo) {
            "${stringResource(R.string.clocktower_evil_display_bluffs)}\\n${demonBluffs.joinToString(firstNightNameSeparator) { it.nameFor(language) }}"
        } else {
            null
        }
''',
    '''    val firstNightDemonDisplaySecondary =
        if (
            demonCard != null &&
            shouldGiveFirstNightEvilInfo &&
            demonBluffPresentation is DemonBluffPresentationResolution.Ready
        ) {
            "${stringResource(R.string.clocktower_evil_display_bluffs)}\\n${demonBluffs.joinToString(firstNightNameSeparator) { it.nameFor(language) }}"
        } else {
            null
        }
''',
)

replace_exact(
    HOST,
    '''                                tellPlayer = if (demonCard != null && shouldGiveFirstNightEvilInfo) demonInfoText else null,
''',
    '''                                tellPlayer = if (
                                    demonCard != null &&
                                    shouldGiveFirstNightEvilInfo &&
                                    demonBluffPresentation is DemonBluffPresentationResolution.Ready
                                ) demonInfoText else null,
''',
)
