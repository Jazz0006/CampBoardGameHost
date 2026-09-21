package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal data class Sde2D5FLiveAndImpPersonConsequenceCalibration(
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val librarianObservedCandidateId: String,
    val librarian: Sde2D5FExpertObservedStageConsequence,
    val chefObservedCandidateId: String,
    val chef: Sde2D5FExpertObservedStageConsequence,
    val fortuneTellerObservedCandidateId: String,
    val fortuneTeller: Sde2D5FExpertObservedStageConsequence,
) {
    val stages: List<Sde2D5FExpertObservedStageConsequence>
        get() = listOf(librarian, chef, fortuneTeller)
}

internal object Sde2D5FLiveAndImpPersonConsequenceCalibrationBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    fun build(): Sde2D5FLiveAndImpPersonConsequenceCalibration {
        val reconstructed = Sde2D5FLiveAndImpPersonCandidateBuilder.build()
        val context = Sde2D5FExpertObservedConsequenceProjector.context(
            caseId = reconstructed.caseId,
            game = reconstructed.game,
            roleDefinitions = roles,
        )

        val librarianClaims = reconstructed.librarian.alternatives
            .mapIndexed { index, alternative ->
                val value = alternative.shownRole?.let { shownRole ->
                    InformationValue.PlayerPair(
                        shownRole = shownRole,
                        seats = alternative.candidateSeats,
                    )
                } ?: InformationValue.NoCharacters(CharacterType.OUTSIDER)
                alternative.candidateId to
                    Sde2D5FExpertObservedConsequenceProjector.projectInformationClaim(
                        context = context,
                        entryId = "librarian-${index + 1}",
                        sequence = 1,
                        information = EffectDraft.PlayerInformation(
                            recipientSeat = reconstructed.librarian.sourceSeat,
                            sourceAbility = reconstructed.librarian.abilityRole,
                            value = value,
                        ),
                    )
            }
            .toMap()
        val librarianObservedId = reconstructed.librarian.observedAlternative.candidateId
        val librarianStage = Sde2D5FExpertObservedConsequenceProjector.evaluateCommittedPrefix(
            context = context,
            stageId = "${reconstructed.caseId}-librarian",
            committedPrefix = emptyList(),
            candidates = librarianClaims,
        )

        val librarianObservedClaim = librarianClaims.getValue(librarianObservedId)
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
            committedPrefix = listOf(librarianObservedClaim),
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
            committedPrefix = listOf(librarianObservedClaim, chefObservedClaim),
            candidates = fortuneTellerClaims,
        )

        return Sde2D5FLiveAndImpPersonConsequenceCalibration(
            verificationStatus = reconstructed.verificationStatus,
            librarianObservedCandidateId = librarianObservedId,
            librarian = librarianStage,
            chefObservedCandidateId = chefObservedId,
            chef = chefStage,
            fortuneTellerObservedCandidateId = fortuneTellerObservedId,
            fortuneTeller = fortuneTellerStage,
        )
    }

    fun renderMarkdown(
        calibration: Sde2D5FLiveAndImpPersonConsequenceCalibration,
    ): String = Sde2D5FExpertObservedConsequenceRenderer.render(
        caseId = "goldcand-ben-03",
        caseTitle = "Live and Imp-Person",
        verificationStatus = calibration.verificationStatus,
        stages = listOf(
            Sde2D5FExpertObservedStageReport(
                title = "Librarian",
                observedCandidateId = calibration.librarianObservedCandidateId,
                evidence = calibration.librarian,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Chef after observed Librarian clue",
                observedCandidateId = calibration.chefObservedCandidateId,
                evidence = calibration.chef,
            ),
            Sde2D5FExpertObservedStageReport(
                title = "Fortune Teller after observed Librarian/Chef prefix and player-selected targets",
                observedCandidateId = calibration.fortuneTellerObservedCandidateId,
                evidence = calibration.fortuneTeller,
            ),
        ),
        caseNotes = listOf(
            "The reconstructed Poisoner target is already committed player-controlled state, but remains hidden " +
                "from good-player knowledge during consequence evaluation.",
            "The observed Chef value uses a natural registration witness, while the later observed Fortune Teller " +
                "YES uses Recluse-as-Demon. This is interaction-scoped registration evidence, not a global state.",
        ),
    )
}
