package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.InformationDecisionHardBlockReason
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionValidationResult
import com.codex.campboardgamehost.clocktower.session.SmallDomainPresentation
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

/**
 * Empath compatibility wrapper over the role-neutral structured numeric adapter.
 *
 * The complete legal domain and exact typed commit path are owned by
 * [prepareNumericInformationUiModel]; this wrapper supplies Empath semantics only.
 */
internal fun prepareEmpathNumberInformationUiModel(
    coordinator: ClocktowerRecommendationCoordinator,
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    actorSeat: Int,
    subjectSeats: List<Int>,
    trueValue: Int,
    reliability: InformationReliability,
    recommendationStyle: RecommendationStyle,
    revision: InformationDecisionRevision,
    recommendedValue: Int?,
    previousShownValue: Int? = null,
    pressureCostPerPoint: Int = 1,
): StructuredNumberInformationUiModel = prepareNumericInformationUiModel(
    coordinator = coordinator,
    gameId = gameId,
    phase = phase,
    round = round,
    sequence = sequence,
    actorSeat = actorSeat,
    abilityRole = RoleId("Empath"),
    metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
    subjectSeats = subjectSeats,
    trueValue = trueValue,
    minimumValue = 0,
    maximumValue = 2,
    reliability = reliability,
    recommendationStyle = recommendationStyle,
    revision = revision,
    recommendedValue = recommendedValue,
    previousShownValue = previousShownValue,
    pressureCostPerPoint = pressureCostPerPoint,
)

@Composable
internal fun StructuredNumberInformationDecisionPanel(
    model: StructuredNumberInformationUiModel,
    currentRevision: InformationDecisionRevision,
    automaticStorytellerInfo: Boolean,
    language: String,
    roleLabel: String? = null,
    onConfirmed: (ConfirmedInformationDecision, Int) -> Unit,
) {
    var blockedReason by remember(model.semanticStateKey, currentRevision) {
        mutableStateOf<InformationDecisionHardBlockReason?>(null)
    }
    var pendingWarning by remember(model.semanticStateKey, currentRevision) {
        mutableStateOf<Pair<InformationDecisionConfirmation, Int>?>(null)
    }

    fun choose(choice: StructuredNumberInformationUiModel.Choice) {
        blockedReason = null
        pendingWarning = null
        val result = if (choice.recommended) {
            model.acceptRecommendation(choice.candidateId, currentRevision)
        } else {
            model.chooseManually(choice.candidateId, currentRevision)
        }
        when (val validation = result.validation) {
            is InformationDecisionValidationResult.Blocked -> blockedReason = validation.reason
            is InformationDecisionValidationResult.Allowed -> {
                val confirmed = requireNotNull(result.confirmed)
                if (validation.warnings.isEmpty()) {
                    onConfirmed(confirmed, choice.value)
                } else {
                    pendingWarning = result to choice.value
                }
            }
        }
    }

    val presentation = SmallDomainPresentation.from(
        legalCandidates = model.choices,
        recommendedCandidateIds = model.choices.filter { it.recommended }.map { it.candidateId },
        candidateId = StructuredNumberInformationUiModel.Choice::candidateId,
    )
    val visibleChoices = if (automaticStorytellerInfo) {
        listOfNotNull(presentation.primary)
    } else {
        listOfNotNull(presentation.primary) + presentation.remaining
    }
    val localizedRoleLabel = roleLabel ?: if (language == "en") "Empath" else "共情者"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (language == "en") "Choose the $localizedRoleLabel number" else "选择展示给${localizedRoleLabel}的数字",
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            if (language == "en") {
                "Only rule-valid values are available. The recommended value is primary; every remaining legal value stays directly selectable."
            } else {
                "这里只显示规则允许的结果；推荐值优先展示，其余所有合法值仍可直接选择。"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )

        visibleChoices.forEach { choice ->
            val label = when {
                choice.recommended && language == "en" -> "Recommended · ${choice.value}"
                choice.recommended -> "推荐 · ${choice.value}"
                language == "en" -> "Manual · ${choice.value}"
                else -> "手动 · ${choice.value}"
            }
            if (choice.recommended) {
                Button(onClick = { choose(choice) }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { choose(choice) }, modifier = Modifier.fillMaxWidth()) {
                    Text(label)
                }
            }
        }

        blockedReason?.let { reason ->
            Text(
                text = when (reason) {
                    InformationDecisionHardBlockReason.STALE_CONTEXT ->
                        if (language == "en") "Game input changed. Re-open this decision before confirming." else "对局输入已经变化，请重新打开本次裁定后再确认。"
                    InformationDecisionHardBlockReason.ILLEGAL_CANDIDATE ->
                        if (language == "en") "That result is no longer legal for this interaction." else "该结果已不属于本次交互的合法选项。"
                    InformationDecisionHardBlockReason.NOT_RECOMMENDED ->
                        if (language == "en") "That result is not the recommendation being accepted." else "该结果不是当前可接受的推荐结果。"
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        pendingWarning?.let { (result, value) ->
            val confirmed = requireNotNull(result.confirmed)
            val warnings = (result.validation as InformationDecisionValidationResult.Allowed).warnings
            Text(
                text = if (language == "en") {
                    "Review before confirming: ${warnings.joinToString { it.code }}"
                } else {
                    "确认前请复核：${warnings.joinToString { it.code }}"
                },
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = {
                    pendingWarning = null
                    onConfirmed(confirmed, value)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Confirm despite warning" else "确认并继续")
            }
        }
    }
}
