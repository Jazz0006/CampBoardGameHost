package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer

internal data class Sde2D5FAStudInScarletConsequenceCalibration(
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val chefObservedCandidateId: String,
    val chef: Sde2D5FExpertObservedStageConsequence,
    val drunkEmpathObservedCandidateId: String,
    val drunkEmpath: Sde2D5FExpertObservedStageConsequence,
    val fortuneTellerObservedCandidateId: String,
    val fortuneTeller: Sde2D5FExpertObservedStageConsequence,
) {
    val stages: List<Sde2D5FExpertObservedStageConsequence>
        get() = listOf(chef, drunkEmpath, fortuneTeller)
}

internal object Sde2D5FAStudInScarletConsequenceCalibrationBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    fun build(): Sde2D5FAStudInScarletConsequenceCalibration {
        val reconstructed = Sde2D5FAStudInScarletCandidateBuilder.build()
        val context = Sde2D5FExpertObservedConsequenceProjector.context(
            caseId = reconstructed.caseId,
            game = reconstructed.game,
            roleDefinitions = roles,
        )

        val chefClaims = reconstructed.chef.alternatives.associate { alternative ->
            val candidateId = "value-${alternative.value}"
            candidateId to numericClaim(
                context = context,
                entryId = "chef-${alternative.value}",
                sequence = 1,
                sourceSeat = reconstructed.chef.sourceSeat,
                abilityRole = reconstructed.chef.abilityRole,
                value = alternative.value,
            )
        }
        val chefObservedId = "value-${reconstructed.chef.observedValue}"
        val chefStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-chef",
            committedPrefix = emptyList(),
            candidates = chefClaims,
            exactParityRecipientSeats = setOf(context.evaluationRecipientSeats.first()),
        )

        val chefObservedClaim = chefClaims.getValue(chefObservedId)
        val drunkClaims = reconstructed.drunkEmpathLegalValues.associate { value ->
            "value-$value" to numericClaim(
                context = context,
                entryId = "drunk-empath-$value",
                sequence = 2,
                sourceSeat = 9,
                abilityRole = RoleId("Empath"),
                value = value,
            )
        }
        val drunkObservedId = "value-${reconstructed.drunkEmpathObservedValue}"
        val drunkStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-drunk-empath",
            committedPrefix = listOf(chefObservedClaim),
            candidates = drunkClaims,
        )

        val drunkObservedClaim = drunkClaims.getValue(drunkObservedId)
        val ftClaims = reconstructed.fortuneTeller.alternatives.associate { alternative ->
            val candidateId = if (alternative.value) "answer-yes" else "answer-no"
            candidateId to fortuneTellerClaim(
                context = context,
                entryId = candidateId,
                sequence = 3,
                sourceSeat = reconstructed.fortuneTeller.sourceSeat,
                targets = reconstructed.fortuneTeller.subjectSeats,
                value = alternative.value,
            )
        }
        val ftObservedId = if (reconstructed.fortuneTeller.observedValue) "answer-yes" else "answer-no"
        val ftStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-fortune-teller",
            committedPrefix = listOf(chefObservedClaim, drunkObservedClaim),
            candidates = ftClaims,
        )

        return Sde2D5FAStudInScarletConsequenceCalibration(
            verificationStatus = reconstructed.verificationStatus,
            chefObservedCandidateId = chefObservedId,
            chef = chefStage,
            drunkEmpathObservedCandidateId = drunkObservedId,
            drunkEmpath = drunkStage,
            fortuneTellerObservedCandidateId = ftObservedId,
            fortuneTeller = ftStage,
        )
    }

    fun renderMarkdown(
        calibration: Sde2D5FAStudInScarletConsequenceCalibration,
    ): String = buildString {
        appendLine("# SDE-2D5F expert-observed candidate consequence calibration")
        appendLine()
        appendLine("Case: `goldcand-ben-01` / A Stud In Scarlet")
        appendLine("Verification: `${calibration.verificationStatus}`")
        appendLine(
            "Guard: mechanical reconstruction and counterfactual diagnostics do not promote this case to GOLD " +
                "until the material Night-1 state is verified against the primary recording.",
        )
        appendLine()
        stage(
            title = "Chef",
            observedCandidateId = calibration.chefObservedCandidateId,
            evidence = calibration.chef,
        )
        stage(
            title = "Drunk shown Empath",
            observedCandidateId = calibration.drunkEmpathObservedCandidateId,
            evidence = calibration.drunkEmpath,
        )
        stage(
            title = "Fortune Teller after player-selected targets",
            observedCandidateId = calibration.fortuneTellerObservedCandidateId,
            evidence = calibration.fortuneTeller,
        )
        appendLine(
            "Interpretation guard: observed is distinguished from counterfactual; no unchosen legal candidate " +
                "is assigned BAD/ACCEPTABLE or any gate label by this report.",
        )
    }

    private fun StringBuilder.stage(
        title: String,
        observedCandidateId: String,
        evidence: Sde2D5FExpertObservedStageConsequence,
    ) {
        appendLine("## $title")
        appendLine()
        appendLine("Committed-prefix observations: ${evidence.prefixObservationCount}")
        appendLine("Observed candidate: `$observedCandidateId`")
        appendLine()
        appendLine("| Candidate | Observed | Recipient | Exact prefix | Exact after | Strategic prefix | Strategic after | Exact/topology parity |")
        appendLine("|---|---|---:|---:|---:|---:|---:|---|")
        evidence.alternatives.forEach { alternative ->
            alternative.byRecipient.forEach { recipient ->
                appendLine(
                    "| ${alternative.candidateId} | ${alternative.candidateId == observedCandidateId} | " +
                        "${recipient.recipientSeat} | ${recipient.prefixExactWorldCount?.toString() ?: "-"} | " +
                        "${recipient.candidateExactWorldCount?.toString() ?: "-"} | " +
                        "${recipient.prefixTopologyStructure.distinctStrategicWorldCount} | " +
                        "${recipient.candidateTopologyStructure.distinctStrategicWorldCount} | " +
                        "${recipient.strategicParity?.toString() ?: "-"} |",
                )
            }
        }
        appendLine()
    }

    private fun numericClaim(
        context: Sde2D5FExpertObservedConsequenceContext,
        entryId: String,
        sequence: Int,
        sourceSeat: Int,
        abilityRole: RoleId,
        value: Int,
    ): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = sourceSeat,
            sourceAbility = abilityRole,
            value = InformationValue.Number(value),
        )
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = context.game,
            information = information,
            roleDefinitions = roles,
        )
        return Sde2D5FExpertObservedConsequenceProjector.projectPublicClaim(
            context = context,
            entryId = entryId,
            sequence = sequence,
            sourceSeat = sourceSeat,
            sourceAbility = abilityRole,
            proposition = proposition,
        )
    }

    private fun fortuneTellerClaim(
        context: Sde2D5FExpertObservedConsequenceContext,
        entryId: String,
        sequence: Int,
        sourceSeat: Int,
        targets: List<Int>,
        value: Boolean,
    ): EpistemicObservation {
        val ability = RoleId("Fortune Teller")
        val information = EffectDraft.PlayerInformation(
            recipientSeat = sourceSeat,
            sourceAbility = ability,
            value = InformationValue.YesNo(if (value) YesNoAnswer.YES else YesNoAnswer.NO),
        )
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materializeFortuneTeller(
            information = information,
            targetSeats = targets,
        )
        return Sde2D5FExpertObservedConsequenceProjector.projectPublicClaim(
            context = context,
            entryId = entryId,
            sequence = sequence,
            sourceSeat = sourceSeat,
            sourceAbility = ability,
            proposition = proposition,
        )
    }
}
