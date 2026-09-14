package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution

/**
 * Non-visual owner for Beginner automatic Spy/Recluse registration. Registration side effects used
 * to live inside legacy text panels; keeping them here lets presentation retire without changing
 * the deterministic temporary 90/10 ruling or selection-audit semantics.
 */
@Composable
internal fun ClocktowerAutomaticRegistrationEffect(
    automaticStorytellerInfo: Boolean,
    subjectName: String,
    registration: TroubleBrewingRegistrationResolution,
    applyRegisteredRole: Boolean,
    selectionAudit: SelectionAuditContext?,
    automaticDecisionKey: String?,
    fallbackFamily: String,
    onUsesSpecialRegistrationChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val legalSpecialRoleEnNames = registration.special.map { it.registeredRole.value }
    val automaticRuling = if (automaticStorytellerInfo) {
        val decisionKey = automaticDecisionKey
            ?: "$fallbackFamily:$subjectName:${legalSpecialRoleEnNames.sorted().joinToString(",")}"
        clocktowerTemporaryRegistrationSelection(
            registration = registration,
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }

    LaunchedEffect(
        automaticStorytellerInfo,
        automaticRuling,
        automaticDecisionKey,
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
