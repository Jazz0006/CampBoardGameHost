package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal data class Sde2D5FHumanRemainsConsequenceCalibration(
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val washerwomanObservedCandidateId: String,
    val washerwoman: Sde2D5FExpertObservedStageConsequence,
    val fortuneTellerObservedCandidateId: String,
    val fortuneTeller: Sde2D5FExpertObservedStageConsequence,
) {
    val stages: List<Sde2D5FExpertObservedStageConsequence>
        get() = listOf(washerwoman, fortuneTeller)
}

internal object Sde2D5FHumanRemainsConsequenceCalibrationBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    fun build(): Sde2D5FHumanRemainsConsequenceCalibration {
        val reconstructed = Sde2D5FHumanRemainsCandidateBuilder.build()
        val context = Sde2D5FExpertObservedConsequenceProjector.context(
            caseId = reconstructed.caseId,
            game = reconstructed.game,
            roleDefinitions = roles,
        )

        val washerwomanClaims = reconstructed.washerwoman.alternatives
            .mapIndexed { index, alternative ->
                alternative.candidateId to
                    Sde2D5FExpertObservedConsequenceProjector.projectPairClaim(
                        context = context,
                        entryId = "washerwoman-${index + 1}",
                        sequence = 1,
                        decision = reconstructed.washerwoman,
                        alternative = alternative,
                    )
            }
            .toMap()
        val washerwomanObservedId = reconstructed.washerwoman.observedAlternative.candidateId
        val washerwomanStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-washerwoman",
            committedPrefix = emptyList(),
            candidates = washerwomanClaims,
        )

        val washerwomanObservedClaim = washerwomanClaims.getValue(washerwomanObservedId)
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
                sequence = 2,
                information = information,
                targetSeats = reconstructed.fortuneTeller.subjectSeats,
            )
        }
        val fortuneTellerObservedId =
            if (reconstructed.fortuneTeller.observedValue) "answer-yes" else "answer-no"
        val fortuneTellerStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-fortune-teller",
            committedPrefix = listOf(washerwomanObservedClaim),
            candidates = fortuneTellerClaims,
        )

        return Sde2D5FHumanRemainsConsequenceCalibration(
            verificationStatus = reconstructed.verificationStatus,
            washerwomanObservedCandidateId = washerwomanObservedId,
            washerwoman = washerwomanStage,
            fortuneTellerObservedCandidateId = fortuneTellerObservedId,
            fortuneTeller = fortuneTellerStage,
        )
    }

    fun renderMarkdown(
        calibration: Sde2D5FHumanRemainsConsequenceCalibration,
    ): String = Sde2D5FExpertObservedConsequenceRenderer.render(
        caseId = "goldcand-ben-02",
        caseTitle = "Human Remains Of The Day",
        verificationStatus = calibration.verificationStatus,
        stages = listOf(
            Sde2D5FExpertObservedStageReport(
                title = "Poisoned Washerwoman",
                observedCandidateId = calibration.washerwomanObservedCandidateId,
                evidence = calibration.washerwoman,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Fortune Teller forced result after observed Washerwoman prefix",
                observedCandidateId = calibration.fortuneTellerObservedCandidateId,
                evidence = calibration.fortuneTeller,
            ),
        ),
        caseNotes = listOf(
            "The Poisoner target is committed player-controlled hidden state before the Washerwoman decision; " +
                "it is represented in canonical game state but is not injected into good-player knowledge.",
            "The poisoned Washerwoman legal domain remains complete. The report summarizes large candidate " +
                "domains by strategic signature instead of truncating the legal alternatives.",
            "The later Fortune Teller result is forced for the reconstructed fixed targets/Red Herring and is " +
                "context validation rather than expert preference evidence.",
        ),
    )
}
