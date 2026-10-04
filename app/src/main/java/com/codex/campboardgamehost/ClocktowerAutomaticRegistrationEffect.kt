package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.StorytellerDecisionAuthority
import com.codex.campboardgamehost.clocktower.recommendation.storytellerDecisionAuthority
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution

internal fun clocktowerRuleDeterministicRegistrationRuling(
    registration: TroubleBrewingRegistrationResolution,
): ClocktowerAutomaticRegistrationRuling? =
    when (storytellerDecisionAuthority(registration.candidates.size)) {
        StorytellerDecisionAuthority.RuleDeterministic ->
            ClocktowerAutomaticRegistrationRuling(
                usesSpecialRegistration = false,
                registeredRoleEnName = null,
            )
        is StorytellerDecisionAuthority.PolicyReady,
        is StorytellerDecisionAuthority.ManualRequired,
        -> null
    }

/**
 * Non-visual owner for rule-deterministic Spy/Recluse registration.
 *
 * LRE-1 fails closed when several legal registrations exist: without an accepted versioned policy
 * this effect performs no automatic ruling and the complete legal domain remains a manual choice.
 */
@Composable
internal fun ClocktowerAutomaticRegistrationEffect(
    automaticStorytellerInfo: Boolean,
    registration: TroubleBrewingRegistrationResolution,
    applyRegisteredRole: Boolean,
    selectionAudit: SelectionAuditContext?,
    onUsesSpecialRegistrationChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val automaticRuling = if (automaticStorytellerInfo) {
        clocktowerRuleDeterministicRegistrationRuling(registration)
    } else {
        null
    }

    LaunchedEffect(
        automaticStorytellerInfo,
        automaticRuling,
        selectionAudit?.selectionId,
    ) {
        if (automaticStorytellerInfo && automaticRuling != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = clocktowerTemporaryRegistrationAuditCandidates(
                            registration = registration,
                        ),
                    ),
                )
                audit.recorder.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        selectedFamilyId = clocktowerTemporaryRegistrationAuditFamilyId(automaticRuling),
                    ),
                )
            }
            onUsesSpecialRegistrationChange(automaticRuling.usesSpecialRegistration)
            if (automaticRuling.usesSpecialRegistration && applyRegisteredRole) {
                automaticRuling.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
}
