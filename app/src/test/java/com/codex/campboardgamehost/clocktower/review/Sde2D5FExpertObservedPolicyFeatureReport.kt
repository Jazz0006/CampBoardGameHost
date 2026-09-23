package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RegistrationReason

internal data class Sde2D5FExpertObservedPolicyFeatureReport(
    val aStudChef: List<Sde2D5FNumericCandidatePolicyFeatures>,
    val aStudDrunkEmpath: List<Sde2D5FNumericCandidatePolicyFeatures>,
    val aStudFortuneTeller: List<Sde2D5FBooleanCandidatePolicyFeatures>,
    val liveLibrarian: List<Sde2D5FPairCandidatePolicyFeatures>,
    val liveChef: List<Sde2D5FNumericCandidatePolicyFeatures>,
    val liveFortuneTeller: List<Sde2D5FBooleanCandidatePolicyFeatures>,
    val humanWasherwoman: List<Sde2D5FPairCandidatePolicyFeatures>,
    val evinWasherwoman: List<Sde2D5FPairCandidatePolicyFeatures>,
    val evinChef: List<Sde2D5FNumericCandidatePolicyFeatures>,
    val evinFortuneTeller: List<Sde2D5FBooleanCandidatePolicyFeatures>,
)

internal object Sde2D5FExpertObservedPolicyFeatureReportBuilder {
    fun build(): Sde2D5FExpertObservedPolicyFeatureReport {
        val aStud = Sde2D5FAStudInScarletCandidateBuilder.build()
        val live = Sde2D5FLiveAndImpPersonCandidateBuilder.build()
        val human = Sde2D5FHumanRemainsCandidateBuilder.build()
        val evin = Sde2D5FEvinFirstPlaythroughCandidateBuilder.build()

        return Sde2D5FExpertObservedPolicyFeatureReport(
            aStudChef = Sde2D5FExpertObservedPolicyFeatureProjector.projectNumeric(aStud.chef),
            aStudDrunkEmpath =
                Sde2D5FExpertObservedPolicyFeatureProjector.projectNumeric(aStud.drunkEmpath),
            aStudFortuneTeller =
                Sde2D5FExpertObservedPolicyFeatureProjector.projectBoolean(aStud.fortuneTeller),
            liveLibrarian = Sde2D5FExpertObservedPolicyFeatureProjector.projectPair(
                game = live.game,
                decision = live.librarian,
                demonBluffs = live.observedDemonBluffs,
            ),
            liveChef = Sde2D5FExpertObservedPolicyFeatureProjector.projectNumeric(live.chef),
            liveFortuneTeller =
                Sde2D5FExpertObservedPolicyFeatureProjector.projectBoolean(live.fortuneTeller),
            humanWasherwoman = Sde2D5FExpertObservedPolicyFeatureProjector.projectPair(
                game = human.game,
                decision = human.washerwoman,
                demonBluffs = human.observedDemonBluffs,
            ),
            evinWasherwoman = Sde2D5FExpertObservedPolicyFeatureProjector.projectPair(
                game = evin.game,
                decision = evin.washerwoman,
                demonBluffs = evin.observedDemonBluffs,
            ),
            evinChef = Sde2D5FExpertObservedPolicyFeatureProjector.projectNumeric(evin.chef),
            evinFortuneTeller =
                Sde2D5FExpertObservedPolicyFeatureProjector.projectBoolean(evin.fortuneTeller),
        )
    }

    fun render(
        report: Sde2D5FExpertObservedPolicyFeatureReport,
    ): String = buildString {
        appendLine("# SDE-2D5F expert-observed descriptive policy features")
        appendLine()
        appendLine(
            "These are descriptive candidate facts only. They do not score, rank, label, or promote " +
                "any observed expert choice. A Stud and Evin are PRIMARY_VERIFIED GOLD decision slices; " +
                "Live and Human Remains remain PRIMARY_VERIFICATION_PENDING.",
        )
        appendLine()
        appendLine(
            "Strategic topology is reported separately by each committed-prefix consequence report. " +
                "This table keeps non-topology candidate facts and provenance dimensions explicit.",
        )
        appendLine(
            "Legal-domain prevalence counts below are descriptive only; they are not rarity scores, " +
                "preference weights, or evidence that an observed candidate was better than an unchosen one.",
        )
        appendLine()

        appendLine("## A Stud In Scarlet")
        appendLine()
        numericSection(
            title = "Chef",
            observedCandidateId = "value-1",
            candidates = report.aStudChef,
        )
        numericSection(
            title = "Drunk shown Empath",
            observedCandidateId = "value-0",
            candidates = report.aStudDrunkEmpath,
        )
        booleanSection(
            title = "Fortune Teller",
            observedCandidateId = "answer-yes",
            candidates = report.aStudFortuneTeller,
        )

        appendLine("## Live and Imp-Person")
        appendLine()
        pairSection(
            title = "Librarian",
            observedCandidateId = "pair-information-ability-v1|Librarian|Recluse|5,8",
            candidates = report.liveLibrarian,
        )
        numericSection(
            title = "Chef",
            observedCandidateId = "value-1",
            candidates = report.liveChef,
        )
        booleanSection(
            title = "Fortune Teller",
            observedCandidateId = "answer-yes",
            candidates = report.liveFortuneTeller,
        )

        appendLine("## Human Remains Of The Day")
        appendLine()
        pairSection(
            title = "Poisoned Washerwoman",
            observedCandidateId = "pair-information-ability-v1|Washerwoman|Empath|2,5",
            candidates = report.humanWasherwoman,
        )

        appendLine("## Evin 2019 First Full Playthrough")
        appendLine()
        pairSection(
            title = "Washerwoman",
            observedCandidateId = "pair-information-ability-v1|Washerwoman|Undertaker|5,7",
            candidates = report.evinWasherwoman,
        )
        numericSection(
            title = "Chef",
            observedCandidateId = "value-1",
            candidates = report.evinChef,
        )
        booleanSection(
            title = "Fortune Teller",
            observedCandidateId = "answer-yes",
            candidates = report.evinFortuneTeller,
        )
    }

    private fun StringBuilder.numericSection(
        title: String,
        observedCandidateId: String,
        candidates: List<Sde2D5FNumericCandidatePolicyFeatures>,
    ) {
        appendLine("### $title")
        appendLine()
        appendLine("| Candidate | Observed | Value | Semantic truth | No-special-registration witness | Special reasons | Special seats |")
        appendLine("|---|---|---:|---|---|---|---|")
        candidates.forEach { candidate ->
            appendLine(
                "| ${cell(candidate.candidateId)} | ${candidate.candidateId == observedCandidateId} | " +
                    "${candidate.value} | ${candidate.semanticTruth} | " +
                    "${candidate.registration.hasNoSpecialRegistrationWitness} | " +
                    "${reasons(candidate.registration.specialReasons)} | " +
                    "${seats(candidate.registration.specialSubjectSeats)} |",
            )
        }
        appendLine()
    }

    private fun StringBuilder.booleanSection(
        title: String,
        observedCandidateId: String,
        candidates: List<Sde2D5FBooleanCandidatePolicyFeatures>,
    ) {
        appendLine("### $title")
        appendLine()
        appendLine("| Candidate | Observed | Value | No-special-registration witness | Special reasons | Special seats |")
        appendLine("|---|---|---|---|---|---|")
        candidates.forEach { candidate ->
            appendLine(
                "| ${cell(candidate.candidateId)} | ${candidate.candidateId == observedCandidateId} | " +
                    "${candidate.value} | ${candidate.registration.hasNoSpecialRegistrationWitness} | " +
                    "${reasons(candidate.registration.specialReasons)} | " +
                    "${seats(candidate.registration.specialSubjectSeats)} |",
            )
        }
        appendLine()
    }

    private fun StringBuilder.pairSection(
        title: String,
        observedCandidateId: String,
        candidates: List<Sde2D5FPairCandidatePolicyFeatures>,
    ) {
        val signatures = candidates.groupBy(Sde2D5FPairCandidatePolicyFeatures::descriptiveSignature)
        val observed = candidates.single { it.candidateId == observedCandidateId }
        val observedSignatureCount = signatures.getValue(observed.descriptiveSignature()).size
        val summary = Sde2D5FExpertObservedPolicyFeatureProjector.summarizePair(candidates)

        appendLine("### $title")
        appendLine()
        appendLine("Legal candidates: ${candidates.size}")
        appendLine("Distinct descriptive feature signatures: ${signatures.size}")
        appendLine("Candidates sharing observed descriptive signature: $observedSignatureCount")
        appendLine()
        appendLine("#### Legal-domain feature prevalence")
        appendLine()
        appendLine("| Domain feature | Candidate count |")
        appendLine("|---|---:|")
        appendLine("| semantic truth = TRUE | ${summary.semanticTrueCount} |")
        appendLine("| semantic truth = FALSE | ${summary.semanticFalseCount} |")
        appendLine("| has no-special-registration witness | ${summary.noSpecialRegistrationWitnessCount} |")
        appendLine("| has special-registration alternative | ${summary.hasSpecialRegistrationAlternativeCount} |")
        appendLine("| requires special registration | ${summary.requiresSpecialRegistrationCount} |")
        appendLine("| shown role is Demon bluff | ${summary.shownRoleDemonBluffCount} |")
        appendLine("| shown role actually in play | ${summary.shownRoleActualInPlayCount} |")
        appendLine("| shown role matches a candidate seat | ${summary.shownRoleMatchesCandidateCount} |")
        appendLine("| special-registration role matches a candidate seat | ${summary.specialRegistrationRoleMatchesCandidateCount} |")
        appendLine("| candidate contains actual Evil seat | ${summary.containsActualEvilSeatCount} |")
        appendLine("| candidate contains actual Demon seat | ${summary.containsActualDemonSeatCount} |")
        appendLine("| candidate contains actual Minion seat | ${summary.containsActualMinionSeatCount} |")
        appendLine("| all candidate seats are actual Evil | ${summary.allCandidateSeatsActualEvilCount} |")
        appendLine()
        appendLine("| Observed candidate | Feature | Value |")
        appendLine("|---|---|---|")
        appendLine("| ${cell(observed.candidateId)} | semantic truth | ${observed.semanticTruth} |")
        appendLine("| ${cell(observed.candidateId)} | no-special-registration witness | ${observed.registration.hasNoSpecialRegistrationWitness} |")
        appendLine("| ${cell(observed.candidateId)} | special registration reasons | ${reasons(observed.registration.specialReasons)} |")
        appendLine("| ${cell(observed.candidateId)} | shown role is Demon bluff | ${observed.shownRoleIsDemonBluff} |")
        appendLine("| ${cell(observed.candidateId)} | shown role actual in-play seats | ${seats(observed.shownRoleActualInPlaySeats)} |")
        appendLine("| ${cell(observed.candidateId)} | shown role matches candidate seats | ${seats(observed.shownRoleCandidateMatchSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | special-registration role matches | ${seats(observed.specialRegistrationRoleMatchSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate special-registration subjects | ${seats(observed.candidateSpecialRegistrationSubjectSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate evil seats | ${seats(observed.candidateEvilSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate Demon seats | ${seats(observed.candidateDemonSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate Minion seats | ${seats(observed.candidateMinionSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate Outsider seats | ${seats(observed.candidateOutsiderSeats)} |")
        appendLine("| ${cell(observed.candidateId)} | candidate Townsfolk seats | ${seats(observed.candidateTownsfolkSeats)} |")
        appendLine()
    }

    private fun cell(value: String): String = value.replace("|", "\\|")

    private fun reasons(reasons: Set<RegistrationReason>): String =
        reasons.sortedBy { it.name }.joinToString(",").ifEmpty { "-" }

    private fun seats(seats: Set<Int>): String =
        seats.sorted().joinToString(",").ifEmpty { "-" }
}
