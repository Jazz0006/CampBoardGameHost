package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/**
 * Rules-backed pair-information projection for registration-ambiguity exposure.
 *
 * Candidate legality, natural truth and registration witnesses remain owned upstream. This projector
 * only identifies when an already-legal visible pair directly names the actual role of a currently
 * functioning registration-capable subject. Registered-truth alternatives therefore preserve that
 * subject's ambiguity instead of being reinterpreted as direct exposure.
 *
 * No role-name policy, score, weight, threshold, history persistence or recommendation selection is
 * owned here.
 */
internal object PairInformationRegistrationAmbiguityExposureProjector {
    fun project(
        game: GameState,
        naturalCandidates: List<DecisionCandidate<PairInformationOutcome>>,
        legalCandidates: List<PairInformationLegalCandidate>,
        exactCandidates: List<ExactConsequenceCandidate>,
    ): List<RoleFunctionExposureCandidateEvidence> {
        require(legalCandidates.isNotEmpty()) {
            "Pair registration-ambiguity projection requires legal candidates."
        }
        val candidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Pair registration-ambiguity legal candidate IDs must be unique."
        }
        require(legalCandidates.all { it.semanticTruth == SemanticTruth.TRUE }) {
            "Pair registration-ambiguity projection currently requires truthful legal candidates."
        }
        require(exactCandidates.map(ExactConsequenceCandidate::candidateId) == candidateIds) {
            "Pair registration-ambiguity exact candidates must preserve legal candidate order."
        }

        val actualTruthByVisiblePair = naturalCandidates
            .asSequence()
            .filter { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE }
            .groupBy { it.outcome.visiblePairKey() }

        val registrationSubjectsBySeat = game.players
            .mapNotNull { player ->
                TroubleBrewingRegistrationDomain.specialReason(
                    TroubleBrewingRegistrationSubject.from(player),
                ) ?: return@mapNotNull null
                player.seat to RegistrationAmbiguitySubject(
                    seat = player.seat,
                    actualRole = player.actualRole,
                )
            }
            .toMap()

        return legalCandidates.zip(exactCandidates).map { (legal, exact) ->
            val selectedRegistrations = exact.selectedRegistrationFacts()
            require(selectedRegistrations == legal.registrations.toSet()) {
                "Pair registration-ambiguity projection must preserve the legal registration witness."
            }

            val directlyExposedTargets = actualTruthByVisiblePair[legal.outcome.visiblePairKey()]
                .orEmpty()
                .mapNotNull { natural ->
                    val targetSeat = natural.outcome.targetSeat ?: return@mapNotNull null
                    val subject = registrationSubjectsBySeat[targetSeat] ?: return@mapNotNull null
                    if (natural.outcome.shownRole != subject.actualRole) return@mapNotNull null
                    if (selectedRegistrations.any { it.subjectSeat == subject.seat }) {
                        return@mapNotNull null
                    }
                    RoleFunctionExposureTargetRef(
                        seat = subject.seat,
                        role = subject.actualRole,
                        recipientSeat = exact.recipientSeat,
                        capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
                    )
                }
                .toSet()

            RoleFunctionExposureCandidateEvidence(
                candidateId = legal.candidateId,
                directlyExposedTargets = directlyExposedTargets,
            )
        }
    }

    private fun ExactConsequenceCandidate.selectedRegistrationFacts(): Set<RegistrationFact> {
        require(registrationWitnessBindings.size == 1) {
            "Pair registration-ambiguity projection requires the pair exact-adapter witness binding."
        }
        return registrationWitnessBindings.single().registrations
    }

    private fun PairInformationOutcome.visiblePairKey(): VisiblePairKey = VisiblePairKey(
        shownRole = shownRole,
        candidateSeats = candidateSeats,
    )

    private data class VisiblePairKey(
        val shownRole: RoleId?,
        val candidateSeats: List<Int>,
    )

    private data class RegistrationAmbiguitySubject(
        val seat: Int,
        val actualRole: RoleId,
    )
}
