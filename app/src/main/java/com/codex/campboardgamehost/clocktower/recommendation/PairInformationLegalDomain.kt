package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.rules.PairInformationDisplaySemantics

/**
 * Complete selectable semantic domain for Washerwoman/Librarian/Investigator information.
 *
 * Display semantics decides which statements the ability may show. The natural-truth generator
 * decides which of those statements are true in the current game, including any exact Spy or
 * Recluse registration fact required to make the statement true. Reliability then decides whether
 * false-but-well-formed statements are selectable: functioning abilities are truth-only, while
 * Drunk/Poisoned abilities may use the complete legal display space.
 *
 * Recommendation score/style and legacy Automatic/Manual presentation state are deliberately
 * absent from this type. Recommendation and manual presentation are downstream consumers.
 */
internal object PairInformationLegalDomain {
    // Preserve the pre-UX-R2 stable candidate IDs so changing authority does not reshuffle legacy selection.
    private const val candidateSchemaVersion = "pair-information-ability-v1"

    fun generate(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): List<PairInformationLegalCandidate> {
        val truthfulByOutcome = NaturalPairInformationCandidateGenerator
            .generateHealthyInformationSpace(
                game = game,
                sourceSeat = sourceSeat,
                abilityRole = abilityRole,
                roleDefinitions = roleDefinitions,
            )
            .groupBy { candidate -> candidate.outcome.canonicalKey() }

        return PairInformationDisplaySemantics
            .legalOutcomes(
                game = game,
                roleDefinitions = roleDefinitions,
                sourceSeat = sourceSeat,
                abilityRole = abilityRole,
            )
            .mapNotNull { outcome ->
                val truthfulCandidates = truthfulByOutcome[outcome.canonicalKey()].orEmpty()
                val semanticTruthCandidate = truthfulCandidates
                    .firstOrNull { it.registrations.isEmpty() }
                    ?: truthfulCandidates.minByOrNull { it.candidateId }
                val truthful = semanticTruthCandidate != null
                if (reliability == ReliabilityState.RELIABLE && !truthful) {
                    return@mapNotNull null
                }
                PairInformationLegalCandidate(
                    candidateId = canonicalId(abilityRole, outcome),
                    outcome = outcome,
                    semanticTruth = if (truthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
                    registrations = semanticTruthCandidate?.registrations.orEmpty(),
                )
            }
    }

    private fun canonicalId(abilityRole: RoleId, outcome: PairInformationOutcome): String = listOf(
        candidateSchemaVersion,
        abilityRole.value,
        outcome.shownRole?.value ?: "none",
        outcome.candidateSeats.joinToString(","),
    ).joinToString("|")

    private fun PairInformationOutcome.canonicalKey(): PairInformationKey = PairInformationKey(
        shownRole = shownRole,
        candidateSeats = candidateSeats,
    )

    private data class PairInformationKey(
        val shownRole: RoleId?,
        val candidateSeats: List<Int>,
    )
}

internal data class PairInformationLegalCandidate(
    val candidateId: String,
    val outcome: PairInformationOutcome,
    val semanticTruth: SemanticTruth,
    val registrations: List<RegistrationFact>,
) {
    init {
        require(candidateId.isNotBlank())
        require(semanticTruth == SemanticTruth.TRUE || registrations.isEmpty()) {
            "False pair-information candidates cannot require a truth registration."
        }
    }
}
