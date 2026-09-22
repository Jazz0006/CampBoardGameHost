package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

internal data class Sde2D5FRegistrationWitnessFeatures(
    val witnessCount: Int,
    val hasNoSpecialRegistrationWitness: Boolean,
    val specialReasons: Set<RegistrationReason>,
    val specialSubjectSeats: Set<Int>,
) {
    init {
        require(witnessCount > 0)
    }

    val hasSpecialRegistrationAlternative: Boolean
        get() = specialReasons.isNotEmpty()

    val requiresSpecialRegistration: Boolean
        get() = !hasNoSpecialRegistrationWitness && hasSpecialRegistrationAlternative
}

internal object Sde2D5FRegistrationWitnessFeatureProjector {
    fun project(
        witnesses: Set<Set<RegistrationFact>>,
    ): Sde2D5FRegistrationWitnessFeatures {
        require(witnesses.isNotEmpty()) {
            "Expert-observed feature projection requires at least one exact registration witness."
        }
        val facts = witnesses.flatten()
        return Sde2D5FRegistrationWitnessFeatures(
            witnessCount = witnesses.size,
            hasNoSpecialRegistrationWitness = witnesses.any { it.isEmpty() },
            specialReasons = facts.mapTo(linkedSetOf(), RegistrationFact::reason),
            specialSubjectSeats = facts.mapTo(linkedSetOf(), RegistrationFact::subjectSeat),
        )
    }
}

internal data class Sde2D5FPairCandidatePolicyFeatures(
    val candidateId: String,
    val semanticTruth: SemanticTruth,
    val registration: Sde2D5FRegistrationWitnessFeatures,
    val shownRole: RoleId?,
    val shownRoleIsDemonBluff: Boolean?,
    val shownRoleActualInPlaySeats: Set<Int>,
    val shownRoleCandidateMatchSeats: Set<Int>,
    val specialRegistrationRoleMatchSeats: Set<Int>,
    val candidateSpecialRegistrationSubjectSeats: Set<Int>,
    val candidateSeatCount: Int,
    val candidateEvilSeats: Set<Int>,
    val candidateDemonSeats: Set<Int>,
    val candidateMinionSeats: Set<Int>,
    val candidateOutsiderSeats: Set<Int>,
    val candidateTownsfolkSeats: Set<Int>,
) {
    init {
        require(candidateId.isNotBlank())
    }

    fun descriptiveSignature(): String = listOf(
        "truth=$semanticTruth",
        "natural=${registration.hasNoSpecialRegistrationWitness}",
        "specialReasons=${registration.specialReasons.sortedBy { it.name }.joinToString(",")}",
        "shownBluff=$shownRoleIsDemonBluff",
        "shownInPlay=${shownRoleActualInPlaySeats.isNotEmpty()}",
        "shownMatches=${shownRoleCandidateMatchSeats.size}",
        "specialRoleMatches=${specialRegistrationRoleMatchSeats.size}",
        "specialSubjects=${candidateSpecialRegistrationSubjectSeats.size}",
        "candidateSeats=$candidateSeatCount",
        "evil=${candidateEvilSeats.size}",
        "demon=${candidateDemonSeats.size}",
        "minion=${candidateMinionSeats.size}",
        "outsider=${candidateOutsiderSeats.size}",
        "townsfolk=${candidateTownsfolkSeats.size}",
    ).joinToString("|")
}

internal data class Sde2D5FPairDomainPolicyFeatureSummary(
    val candidateCount: Int,
    val semanticTrueCount: Int,
    val semanticFalseCount: Int,
    val noSpecialRegistrationWitnessCount: Int,
    val hasSpecialRegistrationAlternativeCount: Int,
    val requiresSpecialRegistrationCount: Int,
    val shownRoleDemonBluffCount: Int,
    val shownRoleDemonBluffUnknownCount: Int,
    val shownRoleActualInPlayCount: Int,
    val shownRoleMatchesCandidateCount: Int,
    val specialRegistrationRoleMatchesCandidateCount: Int,
    val containsActualEvilSeatCount: Int,
    val containsActualDemonSeatCount: Int,
    val containsActualMinionSeatCount: Int,
    val allCandidateSeatsActualEvilCount: Int,
) {
    init {
        require(candidateCount > 0)
        listOf(
            semanticTrueCount,
            semanticFalseCount,
            noSpecialRegistrationWitnessCount,
            hasSpecialRegistrationAlternativeCount,
            requiresSpecialRegistrationCount,
            shownRoleDemonBluffCount,
            shownRoleDemonBluffUnknownCount,
            shownRoleActualInPlayCount,
            shownRoleMatchesCandidateCount,
            specialRegistrationRoleMatchesCandidateCount,
            containsActualEvilSeatCount,
            containsActualDemonSeatCount,
            containsActualMinionSeatCount,
            allCandidateSeatsActualEvilCount,
        ).forEach { count ->
            require(count in 0..candidateCount)
        }
        require(semanticTrueCount + semanticFalseCount == candidateCount)
    }
}

internal data class Sde2D5FNumericCandidatePolicyFeatures(
    val candidateId: String,
    val value: Int,
    val semanticTruth: SemanticTruth,
    val registration: Sde2D5FRegistrationWitnessFeatures,
)

internal data class Sde2D5FBooleanCandidatePolicyFeatures(
    val candidateId: String,
    val value: Boolean,
    val registration: Sde2D5FRegistrationWitnessFeatures,
)

internal object Sde2D5FExpertObservedPolicyFeatureProjector {
    fun projectPair(
        game: GameState,
        decision: Sde2D5FExpertObservedPairDecisionEvidence,
        demonBluffs: Set<RoleId>?,
    ): List<Sde2D5FPairCandidatePolicyFeatures> =
        decision.alternatives.map { alternative ->
            val players = alternative.candidateSeats.map { seat ->
                requireNotNull(game.playerAt(seat)) {
                    "Pair evidence references missing seat $seat."
                }
            }
            val shownRole = alternative.shownRole
            val actualRoleSeats = if (shownRole == null) {
                emptySet()
            } else {
                game.players
                    .filter { it.actualRole == shownRole }
                    .mapTo(linkedSetOf()) { it.seat }
            }
            val candidateMatchSeats = players
                .filter { shownRole != null && it.actualRole == shownRole }
                .mapTo(linkedSetOf()) { it.seat }
            val specialRegistrationSubjects = players
                .filter { player ->
                    TroubleBrewingRegistrationDomain.specialReason(
                        TroubleBrewingRegistrationSubject.from(player),
                    ) != null
                }
                .mapTo(linkedSetOf()) { it.seat }
            val specialRoleMatches = candidateMatchSeats
                .filterTo(linkedSetOf()) { it in specialRegistrationSubjects }

            Sde2D5FPairCandidatePolicyFeatures(
                candidateId = alternative.candidateId,
                semanticTruth = alternative.semanticTruth,
                registration = Sde2D5FRegistrationWitnessFeatureProjector.project(
                    alternative.exactRegistrationWitnesses,
                ),
                shownRole = shownRole,
                shownRoleIsDemonBluff = when {
                    shownRole == null -> false
                    demonBluffs == null -> null
                    else -> shownRole in demonBluffs
                },
                shownRoleActualInPlaySeats = actualRoleSeats,
                shownRoleCandidateMatchSeats = candidateMatchSeats,
                specialRegistrationRoleMatchSeats = specialRoleMatches,
                candidateSpecialRegistrationSubjectSeats = specialRegistrationSubjects,
                candidateSeatCount = players.size,
                candidateEvilSeats = players
                    .filter { it.actualAlignment == Alignment.EVIL }
                    .mapTo(linkedSetOf()) { it.seat },
                candidateDemonSeats = players
                    .filter { it.actualType == CharacterType.DEMON }
                    .mapTo(linkedSetOf()) { it.seat },
                candidateMinionSeats = players
                    .filter { it.actualType == CharacterType.MINION }
                    .mapTo(linkedSetOf()) { it.seat },
                candidateOutsiderSeats = players
                    .filter { it.actualType == CharacterType.OUTSIDER }
                    .mapTo(linkedSetOf()) { it.seat },
                candidateTownsfolkSeats = players
                    .filter { it.actualType == CharacterType.TOWNSFOLK }
                    .mapTo(linkedSetOf()) { it.seat },
            )
        }

    fun summarizePair(
        candidates: List<Sde2D5FPairCandidatePolicyFeatures>,
    ): Sde2D5FPairDomainPolicyFeatureSummary {
        require(candidates.isNotEmpty()) {
            "Pair-domain feature summary requires at least one legal candidate."
        }
        return Sde2D5FPairDomainPolicyFeatureSummary(
            candidateCount = candidates.size,
            semanticTrueCount = candidates.count { it.semanticTruth == SemanticTruth.TRUE },
            semanticFalseCount = candidates.count { it.semanticTruth == SemanticTruth.FALSE },
            noSpecialRegistrationWitnessCount =
                candidates.count { it.registration.hasNoSpecialRegistrationWitness },
            hasSpecialRegistrationAlternativeCount =
                candidates.count { it.registration.hasSpecialRegistrationAlternative },
            requiresSpecialRegistrationCount =
                candidates.count { it.registration.requiresSpecialRegistration },
            shownRoleDemonBluffCount = candidates.count { it.shownRoleIsDemonBluff == true },
            shownRoleDemonBluffUnknownCount =
                candidates.count { it.shownRoleIsDemonBluff == null },
            shownRoleActualInPlayCount =
                candidates.count { it.shownRoleActualInPlaySeats.isNotEmpty() },
            shownRoleMatchesCandidateCount =
                candidates.count { it.shownRoleCandidateMatchSeats.isNotEmpty() },
            specialRegistrationRoleMatchesCandidateCount =
                candidates.count { it.specialRegistrationRoleMatchSeats.isNotEmpty() },
            containsActualEvilSeatCount = candidates.count { it.candidateEvilSeats.isNotEmpty() },
            containsActualDemonSeatCount = candidates.count { it.candidateDemonSeats.isNotEmpty() },
            containsActualMinionSeatCount = candidates.count { it.candidateMinionSeats.isNotEmpty() },
            allCandidateSeatsActualEvilCount = candidates.count {
                it.candidateSeatCount > 0 &&
                    it.candidateEvilSeats.size == it.candidateSeatCount
            },
        )
    }

    fun projectNumeric(
        decision: Sde2D5FExpertObservedNumericDecisionEvidence,
    ): List<Sde2D5FNumericCandidatePolicyFeatures> =
        decision.alternatives.map { alternative ->
            Sde2D5FNumericCandidatePolicyFeatures(
                candidateId = alternative.candidateId,
                value = alternative.value,
                semanticTruth = alternative.semanticTruth,
                registration = Sde2D5FRegistrationWitnessFeatureProjector.project(
                    alternative.registrationWitnesses,
                ),
            )
        }

    fun projectBoolean(
        decision: Sde2D5FExpertObservedBooleanDecisionEvidence,
    ): List<Sde2D5FBooleanCandidatePolicyFeatures> =
        decision.alternatives.map { alternative ->
            Sde2D5FBooleanCandidatePolicyFeatures(
                candidateId = if (alternative.value) "answer-yes" else "answer-no",
                value = alternative.value,
                registration = Sde2D5FRegistrationWitnessFeatureProjector.project(
                    alternative.registrationWitnesses,
                ),
            )
        }
}
