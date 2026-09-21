package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

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
    ): String = Sde2D5FExpertObservedConsequenceRenderer.render(
        caseId = "goldcand-ben-01",
        caseTitle = "A Stud In Scarlet",
        verificationStatus = calibration.verificationStatus,
        stages = listOf(
            Sde2D5FExpertObservedStageReport(
                title = "Chef",
                observedCandidateId = calibration.chefObservedCandidateId,
                evidence = calibration.chef,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Drunk shown Empath",
                observedCandidateId = calibration.drunkEmpathObservedCandidateId,
                evidence = calibration.drunkEmpath,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Fortune Teller after player-selected targets",
                observedCandidateId = calibration.fortuneTellerObservedCandidateId,
                evidence = calibration.fortuneTeller,
            ),
        ),
        caseNotes = listOf(
            "All currently reconstructed A Stud alternatives are topology-neutral on the evil-seat quotient; " +
                "the case remains valuable primarily for registration and impaired-information evidence.",
        ),
    )

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
        return Sde2D5FExpertObservedConsequenceProjector.projectInformationClaim(
            context = context,
            entryId = entryId,
            sequence = sequence,
            information = information,
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
        return Sde2D5FExpertObservedConsequenceProjector.projectFortuneTellerClaim(
            context = context,
            entryId = entryId,
            sequence = sequence,
            information = information,
            targetSeats = targets,
        )
    }
}
