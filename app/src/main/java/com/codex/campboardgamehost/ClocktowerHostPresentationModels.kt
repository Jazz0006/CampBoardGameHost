package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

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
    /** Spatial presentation identity only; it does not make a player-visible claim reliable. */
    val presentationSubjectSeats: List<Int> = emptyList(),
    val displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    val recommendedDisplayOptions: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * The complete candidate pool emitted by the legacy helper. This remains a migration/parity
     * surface only; it must not define what a Storyteller is legally allowed to choose manually.
     */
    val legacyInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * Complete legal candidate domain available to the Storyteller in manual presentation.
     * Recommendation ranking may choose a subset for the default surface, but may not narrow this
     * list. Pair-information abilities source this through [ClocktowerPairManualAuthority].
     */
    val manualInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * Candidate domain used by automatic first-night information selection. Pair-information
     * abilities share the same legal domain as manual presentation so execution policy changes
     * ranking/selection only, never legality.
     */
    val automaticInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    /** Optional policy recommendation; consumers must rebind it to the current automatic domain. */
    val automaticPolicyRecommendation: ClocktowerDisplayOption? = null,
    val wakeText: String? = null,
    val roleEnName: String? = null,
    val informationReliability: InformationReliability = InformationReliability.RELIABLE,
    val recentMisinformationStreak: Int = 0,
    val previousShownNumber: Int? = null,
    /** Complete semantic bounds for a numeric information interaction, when applicable. */
    val numericMinimumValue: Int? = null,
    val numericMaximumValue: Int? = null,
    val selectedInformationTruthful: Boolean? = null,
    /** Confirmed Foundation authority; the draft is only publishable through this envelope. */
    val informationDecisionConfirmation: ConfirmedInformationDecision? = null,
    val informationDecisionExpectedSnapshot: InformationDecisionSnapshot? = null,
    val spyRegistrationKey: String? = null,
    val spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    val spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    val spyRegistrationHint: String? = null,
    val recluseRegistrationKey: String? = null,
    val recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    val interactionId: ClocktowerInteractionId? = null,
)
