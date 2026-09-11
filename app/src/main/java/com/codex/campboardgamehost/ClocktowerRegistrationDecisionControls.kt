package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Embedded square-table controls for Experienced-mode Spy registration decisions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerSpyRegistrationDecisionControls(
    recommendations: List<ClocktowerRegistrationRecommendationOption>,
    legalRoles: List<Pair<String, String>>,
    registersGood: Boolean,
    registeredRoleEnName: String?,
    enabled: Boolean,
    language: String,
    onRegistersGoodChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    ClocktowerRegistrationDecisionControls(
        recommendations = recommendations,
        legalRoles = legalRoles,
        usesSpecialRegistration = registersGood,
        registeredRoleEnName = registeredRoleEnName,
        enabled = enabled,
        language = language,
        specialLabelEn = "Register good",
        specialLabelZh = "登记为善良",
        disabledTextEn = "The Spy is poisoned; registration cannot change.",
        disabledTextZh = "间谍已中毒，本次不能改变登记身份。",
        onUsesSpecialRegistrationChange = onRegistersGoodChange,
        onRoleChange = onRoleChange,
    )
}

/** Embedded square-table controls for Experienced-mode Recluse registration decisions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerRecluseRegistrationDecisionControls(
    recommendations: List<ClocktowerRegistrationRecommendationOption>,
    legalRoles: List<Pair<String, String>>,
    registersEvil: Boolean,
    registeredRoleEnName: String?,
    enabled: Boolean,
    language: String,
    onRegistersEvilChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    ClocktowerRegistrationDecisionControls(
        recommendations = recommendations,
        legalRoles = legalRoles,
        usesSpecialRegistration = registersEvil,
        registeredRoleEnName = registeredRoleEnName,
        enabled = enabled,
        language = language,
        specialLabelEn = "Register evil",
        specialLabelZh = "登记为邪恶",
        disabledTextEn = "The Recluse is poisoned and must register normally.",
        disabledTextZh = "隐士已中毒，本次只能按真实身份登记。",
        onUsesSpecialRegistrationChange = onRegistersEvilChange,
        onRoleChange = onRoleChange,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClocktowerRegistrationDecisionControls(
    recommendations: List<ClocktowerRegistrationRecommendationOption>,
    legalRoles: List<Pair<String, String>>,
    usesSpecialRegistration: Boolean,
    registeredRoleEnName: String?,
    enabled: Boolean,
    language: String,
    specialLabelEn: String,
    specialLabelZh: String,
    disabledTextEn: String,
    disabledTextZh: String,
    onUsesSpecialRegistrationChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            if (language == "en") "Registration ruling" else "登记裁定",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        if (!enabled) {
            Text(
                if (language == "en") disabledTextEn else disabledTextZh,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            return@Column
        }

        recommendations.sortedBy { if (it.isDefaultRecommendation) 0 else 1 }.forEach { recommendation ->
            val apply = {
                onUsesSpecialRegistrationChange(recommendation.usesSpecialRegistration)
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
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (usesSpecialRegistration) {
                OutlinedButton(
                    onClick = { onUsesSpecialRegistrationChange(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (language == "en") "Actual" else "真实身份")
                }
                Button(
                    onClick = { onUsesSpecialRegistrationChange(true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (language == "en") specialLabelEn else specialLabelZh)
                }
            } else {
                Button(
                    onClick = { onUsesSpecialRegistrationChange(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (language == "en") "Actual" else "真实身份")
                }
                OutlinedButton(
                    onClick = { onUsesSpecialRegistrationChange(true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (language == "en") specialLabelEn else specialLabelZh)
                }
            }
        }

        if (usesSpecialRegistration && legalRoles.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                legalRoles.forEach { (roleEnName, roleLabel) ->
                    if (registeredRoleEnName == roleEnName) {
                        Button(onClick = { onRoleChange(roleEnName) }) { Text(roleLabel) }
                    } else {
                        OutlinedButton(onClick = { onRoleChange(roleEnName) }) { Text(roleLabel) }
                    }
                }
            }
        }
    }
}
