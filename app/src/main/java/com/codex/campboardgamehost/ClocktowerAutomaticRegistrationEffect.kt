package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext

/**
 * Non-visual owner for Beginner automatic Spy/Recluse registration. Registration side effects used
 * to live inside legacy text panels; keeping them here lets presentation retire without changing
 * the deterministic temporary 90/10 ruling or selection-audit semantics.
 */
@Composable
internal fun ClocktowerAutomaticRegistrationEffect(
    automaticStorytellerInfo: Boolean,
    subjectName: String,
    legalSpecialRoleEnNames: List<String>,
    applyRegisteredRole: Boolean,
    enabled: Boolean,
    selectionAudit: SelectionAuditContext?,
    automaticDecisionKey: String?,
    fallbackFamily: String,
    onUsesSpecialRegistrationChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val automaticRuling = if (automaticStorytellerInfo && enabled) {
        val decisionKey = automaticDecisionKey
            ?: "$fallbackFamily:$subjectName:${legalSpecialRoleEnNames.sorted().joinToString(",")}"
        clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = legalSpecialRoleEnNames,
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }

    LaunchedEffect(
        automaticStorytellerInfo,
        enabled,
        automaticRuling,
        automaticDecisionKey,
        selectionAudit?.selectionId,
    ) {
        if (automaticStorytellerInfo && enabled && automaticRuling != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = clocktowerTemporaryRegistrationAuditCandidates(
                            legalSpecialRoleEnNames = legalSpecialRoleEnNames,
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
