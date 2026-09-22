package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal data class Sde2D5FEvinFirstPlaythroughConsequenceCalibration(
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val washerwomanObservedCandidateId: String,
    val washerwoman: Sde2D5FExpertObservedStageConsequence,
    val chefObservedCandidateId: String,
    val chef: Sde2D5FExpertObservedStageConsequence,
    val fortuneTellerObservedCandidateId: String,
    val fortuneTeller: Sde2D5FExpertObservedStageConsequence,
) {
    val stages: List<Sde2D5FExpertObservedStageConsequence>
        get() = listOf(washerwoman, chef, fortuneTeller)
}

internal object Sde2D5FEvinFirstPlaythroughConsequenceCalibrationBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    fun build(): Sde2D5FEvinFirstPlaythroughConsequenceCalibration {
        val reconstructed = Sde2D5FEvinFirstPlaythroughCandidateBuilder.build()
        val context = Sde2D5FExpertObservedConsequenceProjector.context(
            caseId = reconstructed.caseId,
            game = reconstructed.game,
            roleDefinitions = roles,
        )

        val washerwomanClaims = reconstructed.washerwoman.alternatives.associate { alternative ->
            alternative.candidateId to Sde2D5FExpertObservedConsequenceProjector.projectPairClaim(
                context = context,
                entryId = alternative.candidateId,
                sequence = 1,
                decision = reconstructed.washerwoman,
                alternative = alternative,
            )
        }
        val washerwomanObservedId = reconstructed.washerwoman.observedAlternative.candidateId
        val washerwomanStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-washerwoman",
            committedPrefix = emptyList(),
            candidates = washerwomanClaims,
        )

        val washerwomanObservedClaim = washerwomanClaims.getValue(washerwomanObservedId)
        val chefClaims = reconstructed.chef.alternatives.associate { alternative ->
            val candidateId = "value-${alternative.value}"
            candidateId to Sde2D5FExpertObservedConsequenceProjector.projectInformationClaim(
                context = context,
                entryId = "chef-${alternative.value}",
                sequence = 2,
                information = EffectDraft.PlayerInformation(
                    recipientSeat = reconstructed.chef.sourceSeat,
                    sourceAbility = reconstructed.chef.abilityRole,
                    value = InformationValue.Number(alternative.value),
                ),
            )
        }
        val chefObservedId = "value-${reconstructed.chef.observedValue}"
        val chefStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-chef",
            committedPrefix = listOf(washerwomanObservedClaim),
            candidates = chefClaims,
        )

        val chefObservedClaim = chefClaims.getValue(chefObservedId)
        val fortuneTellerClaims = reconstructed.fortuneTeller.alternatives.associate { alternative ->
            val candidateId = if (alternative.value) "answer-yes" else "answer-no"
            val information = EffectDraft.PlayerInformation(
                recipientSeat = reconstructed.fortuneTeller.sourceSeat,
                sourceAbility = reconstructed.fortuneTeller.abilityRole,
                value = InformationValue.YesNo(
                    if (alternative.value) YesNoAnswer.YES else YesNoAnswer.NO,
                ),
            )
            candidateId to Sde2D5FExpertObservedConsequenceProjector.projectFortuneTellerClaim(
                context = context,
                entryId = candidateId,
                sequence = 3,
                information = information,
                targetSeats = reconstructed.fortuneTeller.subjectSeats,
            )
        }
        val fortuneTellerObservedId =
            if (reconstructed.fortuneTeller.observedValue) "answer-yes" else "answer-no"
        val fortuneTellerStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-fortune-teller",
            committedPrefix = listOf(washerwomanObservedClaim, chefObservedClaim),
            candidates = fortuneTellerClaims,
        )

        return Sde2D5FEvinFirstPlaythroughConsequenceCalibration(
            verificationStatus = reconstructed.verificationStatus,
            washerwomanObservedCandidateId = washerwomanObservedId,
            washerwoman = washerwomanStage,
            chefObservedCandidateId = chefObservedId,
            chef = chefStage,
            fortuneTellerObservedCandidateId = fortuneTellerObservedId,
            fortuneTeller = fortuneTellerStage,
        )
    }

    fun renderMarkdown(
        calibration: Sde2D5FEvinFirstPlaythroughConsequenceCalibration,
    ): String = Sde2D5FExpertObservedConsequenceRenderer.render(
        caseId = "goldcand-evin-01",
        caseTitle = "Evin 2019 First Full Playthrough",
        verificationStatus = calibration.verificationStatus,
        stages = listOf(
            Sde2D5FExpertObservedStageReport(
                title = "Washerwoman",
                observedCandidateId = calibration.washerwomanObservedCandidateId,
                evidence = calibration.washerwoman,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Chef",
                observedCandidateId = calibration.chefObservedCandidateId,
                evidence = calibration.chef,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Fortune Teller after player-selected targets",
                observedCandidateId = calibration.fortuneTellerObservedCandidateId,
                evidence = calibration.fortuneTeller,
            ),
        ),
        caseNotes = listOf(
            "Primary video did not expose the Demon bluff triplet. The current admitted decision slice " +
                "does not use Demon bluffs in its production legality domains.",
            "The later Fortune Teller YES is mechanically forced because the player-selected pair includes " +
                "the already-committed Red Herring; it is trajectory evidence, not a free Storyteller output.",
        ),
    )
}
