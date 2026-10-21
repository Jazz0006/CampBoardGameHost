package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/** Virgin looks at a Townsfolk CHARACTER TYPE, never an arbitrary first good role. */
@Composable
internal fun ClocktowerSpyTownsfolkTypeDecisionControls(
    registersAsTownsfolk: Boolean,
    hasExplicitChoice: Boolean,
    enabled: Boolean,
    language: String,
    onRegistersAsTownsfolkChange: (Boolean) -> Unit,
) {
    if (!hasExplicitChoice) {
        Text(
            if (language == "en") "No explicit ruling selected; showing the actual identity by default."
            else "尚未明确裁定；默认按真实身份结算。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    ClocktowerRegistrationDecisionControls(
        usesSpecialRegistration = registersAsTownsfolk,
        enabled = enabled,
        language = language,
        specialLabelEn = "Register as Townsfolk",
        specialLabelZh = "登记为镇民",
        disabledTextEn = "The Spy cannot use special registration in this interaction.",
        disabledTextZh = "间谍当前不能在本次交互中特殊登记。",
        onUsesSpecialRegistrationChange = onRegistersAsTownsfolkChange,
    )
}

@Composable
private fun ClocktowerRegistrationDecisionControls(
    usesSpecialRegistration: Boolean,
    enabled: Boolean,
    language: String,
    specialLabelEn: String,
    specialLabelZh: String,
    disabledTextEn: String,
    disabledTextZh: String,
    onUsesSpecialRegistrationChange: (Boolean) -> Unit,
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

    }
}
