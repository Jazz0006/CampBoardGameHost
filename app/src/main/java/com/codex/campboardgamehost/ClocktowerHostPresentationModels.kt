package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision

internal fun recommendationReasonLabel(code: String, language: String): String {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    return when (code) {
        "truth-distance" -> text("结果与真实值的距离符合当前风格", "Distance from truth fits the selected style")
        "extreme-pressure" -> text("限制极端数字造成的压力", "Limits pressure from extreme numbers")
        "history-continuity" -> text("与此前信息保持可解释的连续性", "Maintains explainable continuity with earlier information")
        "dynamic.categorical-score", "dynamic.pair-score" -> text("完整信息结果通过场景评分", "The complete information result passed scenario scoring")
        "special-registration" -> text("本结果使用一次合法的特殊登记", "This result uses a legal special registration")
        "registration-discussion-value" -> text("登记结果保留有意义的讨论空间", "The registration preserves useful discussion")
        "registration-pressure" -> text("登记造成的信息压力符合当前风格", "Registration pressure fits the selected style")
        "registration-history" -> text("已考虑该玩家此前的登记次数", "Prior registrations for this player were considered")
        "global-balance", "consequence.alignment-advantage-adjustment" -> text("根据当前阵营优势修正", "Adjusted for the current alignment advantage")
        "pressure.repeated-target-penalty" -> text("避免持续针对同一名玩家", "Avoids repeatedly targeting the same player")
        "consequence.one-shot-ability-protection" -> text("保护一次性能力的核心体验", "Protects the core experience of a one-shot ability")
        "consequence.high-impact-misinformation-penalty" -> text("近期高冲击误导较多，已降低权重", "Recent high-impact misinformation lowers this option's weight")
        "consequence.final-day-impact-penalty" -> text("终局结果可能直接决定胜负", "This final-day result could decide the game")
        "successor-role-suitability" -> text("继任角色与当前局面相符", "The successor role fits the current state")
        "successor-public-pressure" -> text("已考虑该玩家的公开压力", "Public pressure on this player was considered")
        "scarlet-woman-mandatory" -> text("当前人数下必须由红唇女郎继任", "The Scarlet Woman must succeed at this player count")
        "mayor-survival" -> text("市长存活价值已纳入评估", "The value of keeping the Mayor alive was considered")
        else -> code
    }
}

internal enum class ClocktowerRegistrationDetail {
    AlignmentOnly,
    Role,
}

internal enum class ClocktowerPairInformationAbility {
    Washerwoman,
    Librarian,
    Investigator,
}

internal data class ClocktowerNightStepUi(
    val title: String,
    val actor: PlayerCard?,
    val isRealAction: Boolean,
    val reason: String,
    val storytellerAction: String,
    val tellPlayer: String?,
    val explanation: String,
    val action: ClocktowerNightAction = ClocktowerNightAction.None,
    val displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.None,
    val displayTitle: String = title,
    val displayPrimary: String? = null,
    val displaySecondary: String? = null,
    val displayFooter: String? = null,
    val displayProposition: InformationProposition? = null,
    val displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    val recommendedDisplayOptions: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * The complete candidate pool emitted by the legacy helper.  This is kept
     * separate from the UI lists because automatic mode deliberately renders
     * only its selected recommendation.  Batch 4 compares this full pool at
     * the display boundary before allowing the migrated lifecycle to commit.
     */
    val legacyInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    val decisionOptions: List<ClocktowerDecisionOption> = emptyList(),
    val wakeText: String? = null,
    val roleEnName: String? = null,
    val informationReliability: InformationReliability = InformationReliability.RELIABLE,
    val recentMisinformationStreak: Int = 0,
    val previousShownNumber: Int? = null,
    val selectedInformationTruthful: Boolean? = null,
    /** Confirmed Foundation authority; the draft is only publishable through this envelope. */
    val informationDecisionConfirmation: ConfirmedInformationDecision? = null,
    val spyRegistrationKey: String? = null,
    val spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    val spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    val spyRegistrationHint: String? = null,
    val recluseRegistrationKey: String? = null,
    val recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
)
