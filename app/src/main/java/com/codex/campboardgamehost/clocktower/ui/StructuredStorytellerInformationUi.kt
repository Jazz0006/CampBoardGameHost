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
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.InformationDecisionHardBlockReason
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionValidationResult
import com.codex.campboardgamehost.clocktower.session.InformationResolutionRequest
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

/**
 * First production structured-manual slice: Empath numeric information.
 *
 * Legal values come from the existing numeric rules/recommendation candidate generator. This
 * adapter only supplies interaction identity and converts each validated value into the exact
 * player-visible observation draft that the existing session authority will commit.
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
    recommendationStyle: com.codex.campboardgamehost.clocktower.domain.RecommendationStyle,
    revision: InformationDecisionRevision,
    recommendedValue: Int?,
): StructuredNumberInformationUiModel {
    require(actorSeat > 0) { "Empath actor seat must be positive." }
    require(subjectSeats.all { it > 0 } && subjectSeats.distinct().size == subjectSeats.size) {
        "Empath subject seats must be positive and unique."
    }
    val evaluations = coordinator.resolveInformation(
        InformationResolutionRequest.Number(
            context = UnreliableNumberContext(
                trueValue = trueValue,
                minimumValue = 0,
                maximumValue = 2,
            ),
            generation = DynamicGenerationContext(
                abilityRole = RoleId("Empath"),
                recipientSeat = actorSeat,
                reliability = reliability,
                style = recommendationStyle,
                targetSeats = subjectSeats.toSet(),
            ),
        ),
    )
    val recommendedIds = recommendedValue?.let { value ->
        evaluations
            .filter { (it.candidate.outcome as DynamicInformationOutcome.Number).value == value }
            .mapTo(linkedSetOf()) { it.candidate.candidateId }
    }.orEmpty()
    val storytellerPhase = when (phase) {
        ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
        ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
        ClocktowerPhase.Day -> StorytellerPhase.DAY
        ClocktowerPhase.Night -> StorytellerPhase.NIGHT
    }
    val context = coordinator.informationDecisionContext(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedIds,
        revision = revision,
        semanticIdentity = "empath|$gameId|${phase.name}|$round|$sequence|$actorSeat",
        draftOf = { evaluation ->
            val value = (evaluation.candidate.outcome as DynamicInformationOutcome.Number).value
            val proposition = InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = actorSeat,
                subjectSeats = subjectSeats,
                value = value,
            )
            EpistemicObservationDraft(
                recordId = clocktowerPrivateObservationRecordId(
                    gameId = gameId,
                    phase = phase,
                    round = round,
                    roleEnName = "Empath",
                    actorSeat = actorSeat,
                    proposition = proposition,
                ),
                phase = storytellerPhase,
                round = round,
                sequence = sequence,
                sourceSeat = actorSeat,
                sourceAbility = RoleId("Empath"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(actorSeat),
                // Player knowledge records what was received; impairment truth stays in the
                // candidate/decision evidence and is not leaked into player-facing history.
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = proposition,
            )
        },
    )
    return StructuredNumberInformationUiModel.from(context)
}

@Composable
internal fun StructuredNumberInformationDecisionPanel(
    model: StructuredNumberInformationUiModel,
    currentRevision: InformationDecisionRevision,
    automaticStorytellerInfo: Boolean,
    language: String,
    onConfirmed: (ConfirmedInformationDecision, Int) -> Unit,
) {
    var blockedReason by remember(model, currentRevision) { mutableStateOf<InformationDecisionHardBlockReason?>(null) }
    var pendingWarning by remember(model, currentRevision) {
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (language == "en") "Choose the Empath number" else "选择展示给共情者的数字",
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            if (language == "en") {
                "Only rule-valid values are available. The recommended value uses the same confirmation path as a manual choice."
            } else {
                "这里只显示规则允许的结果；采用推荐与手动选择会经过同一确认路径。"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )

        model.choices
            .filter { !automaticStorytellerInfo || it.recommended }
            .forEach { choice ->
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
