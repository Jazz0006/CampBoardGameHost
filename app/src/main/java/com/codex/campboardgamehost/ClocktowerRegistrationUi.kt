package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SpyRegistrationPanel(
    cards: List<PlayerCard>,
    spy: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersGood: Boolean,
    registeredRoleEnName: String?,
    detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    hint: String? = null,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    automaticStorytellerStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    selectionAudit: SelectionAuditContext? = null,
    automaticDecisionKey: String? = null,
    enabled: Boolean,
    onRegistersGoodChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
    val registrationPool = unifiedRegistrationPool(recommendations)
    val assistedRecommendations = registrationPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: recommendations
    val automaticRuling = if (automaticStorytellerInfo && enabled) {
        val decisionKey = automaticDecisionKey
            ?: "spy-registration-fallback:${spy.name}:${roles.map { it.enName }.sorted().joinToString(",")}"
        clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = roles.map { it.enName },
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, automaticDecisionKey, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRuling != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = clocktowerTemporaryRegistrationAuditCandidates(
                            legalSpecialRoleEnNames = roles.map { it.enName },
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
            onRegistersGoodChange(automaticRuling.usesSpecialRegistration)
            if (automaticRuling.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                automaticRuling.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Private Storyteller ruling" else "说书人私密裁定", fontWeight = FontWeight.Black)
        Text(
            "${spy.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        hint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (!enabled) {
            Text(
                if (language == "en") "The Spy is poisoned; registration cannot change." else "间谍已中毒，本次不能改变登记身份。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRuling != null) {
                Text(
                    if (language == "en") "System ruling applied" else "系统已自动裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                val registeredRoleLabel = automaticRuling.registeredRoleEnName?.let { selectedRole ->
                    roles.firstOrNull { it.enName == selectedRole }?.nameFor(language) ?: selectedRole
                }
                Text(
                    when {
                        !automaticRuling.usesSpecialRegistration -> if (language == "en") "Use actual identity" else "按真实身份登记"
                        detail == ClocktowerRegistrationDetail.Role && registeredRoleLabel != null -> {
                            if (language == "en") "Register as $registeredRoleLabel" else "登记为 $registeredRoleLabel"
                        }
                        else -> if (language == "en") "Register as good" else "登记为善良"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (assistedRecommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                assistedRecommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersGoodChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                    RecommendationReasonSummary(recommendation.reasonCodes, recommendation.warningCodes, language)
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (registersGood) {
                    OutlinedButton(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    Button(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                } else {
                    Button(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    OutlinedButton(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                }
            }
            if (registersGood && detail == ClocktowerRegistrationDetail.Role && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecluseRegistrationPanel(
    cards: List<PlayerCard>,
    recluse: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersEvil: Boolean,
    registeredRoleEnName: String?,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    automaticStorytellerStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    selectionAudit: SelectionAuditContext? = null,
    automaticDecisionKey: String? = null,
    enabled: Boolean,
    onRegistersEvilChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams }
    val registrationPool = unifiedRegistrationPool(recommendations)
    val assistedRecommendations = registrationPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: recommendations
    val automaticRuling = if (automaticStorytellerInfo && enabled) {
        val decisionKey = automaticDecisionKey
            ?: "recluse-registration-fallback:${recluse.name}:${roles.map { it.enName }.sorted().joinToString(",")}"
        clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = roles.map { it.enName },
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, automaticDecisionKey, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRuling != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = clocktowerTemporaryRegistrationAuditCandidates(
                            legalSpecialRoleEnNames = roles.map { it.enName },
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
            onRegistersEvilChange(automaticRuling.usesSpecialRegistration)
            if (automaticRuling.usesSpecialRegistration) {
                automaticRuling.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Recluse registration" else "隐士登记裁定", fontWeight = FontWeight.Black)
        Text(
            "${recluse.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!enabled) {
            Text(
                if (language == "en") "The Recluse is poisoned and must register normally." else "隐士已中毒，本次只能按真实身份登记。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRuling != null) {
                Text(
                    if (language == "en") "System ruling applied" else "系统已自动裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                val registeredRoleLabel = automaticRuling.registeredRoleEnName?.let { selectedRole ->
                    roles.firstOrNull { it.enName == selectedRole }?.nameFor(language) ?: selectedRole
                }
                Text(
                    if (!automaticRuling.usesSpecialRegistration) {
                        if (language == "en") "Use actual identity" else "按真实身份登记"
                    } else if (registeredRoleLabel != null) {
                        if (language == "en") "Register as $registeredRoleLabel" else "登记为 $registeredRoleLabel"
                    } else {
                        if (language == "en") "Register as evil" else "登记为邪恶"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (assistedRecommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                assistedRecommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersEvilChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                    RecommendationReasonSummary(recommendation.reasonCodes, recommendation.warningCodes, language)
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!registersEvil) {
                    Button(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                }
                if (registersEvil) {
                    Button(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                }
            }
            if (registersEvil && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}