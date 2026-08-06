package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StableCandidateIdFactory
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

internal object NaturalPairInformationCandidateGenerator {
    private const val candidateSchemaVersion = "1"
    private const val naturalTruthFamily = "natural-truth"
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")

    fun generate(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
    ): List<DecisionCandidate<PairInformationOutcome>> {
        val source = game.playerAt(sourceSeat) ?: return emptyList()
        if (source.poisoned || source.actualRole != abilityRole) return emptyList()
        val targetType = when (abilityRole) {
            librarian -> CharacterType.OUTSIDER
            investigator -> CharacterType.MINION
            else -> return emptyList()
        }
        val targets = game.players
            .filter { it.seat != sourceSeat && it.actualType == targetType }
            .sortedBy { it.seat }
        if (targets.isEmpty()) {
            return if (abilityRole == librarian) listOf(noOutsiderCandidate(sourceSeat)) else emptyList()
        }

        return targets.flatMap { target ->
            game.players
                .filter { it.seat != sourceSeat && it.seat != target.seat }
                .sortedBy { it.seat }
                .map { decoy -> naturalCandidate(sourceSeat, abilityRole, target.actualRole, target.seat, decoy.seat) }
        }.distinctBy { it.candidateId }
    }

    private fun naturalCandidate(
        sourceSeat: Int,
        abilityRole: RoleId,
        shownRole: RoleId,
        targetSeat: Int,
        decoySeat: Int,
    ): DecisionCandidate<PairInformationOutcome> {
        val outcome = PairInformationOutcome(
            shownRole = shownRole,
            targetSeat = targetSeat,
            decoySeat = decoySeat,
        )
        return DecisionCandidate(
            candidateId = StableCandidateIdFactory.create(
                candidateSchemaVersion = candidateSchemaVersion,
                abilityState = AbilityState.FUNCTIONING,
                truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
                abilityRole = abilityRole,
                shownRole = shownRole,
                candidateSeats = outcome.candidateSeats,
            ),
            candidateFamilyId = naturalTruthFamily,
            outcome = outcome,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = sourceSeat,
                    sourceAbility = abilityRole,
                    value = InformationValue.PlayerPair(shownRole, outcome.candidateSeats),
                ),
            ),
            metadata = metadata(abilityRole),
        )
    }

    private fun noOutsiderCandidate(sourceSeat: Int): DecisionCandidate<PairInformationOutcome> = DecisionCandidate(
        candidateId = StableCandidateIdFactory.create(
            candidateSchemaVersion = candidateSchemaVersion,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            abilityRole = librarian,
        ),
        candidateFamilyId = naturalTruthFamily,
        outcome = PairInformationOutcome(shownRole = null, targetSeat = null, decoySeat = null),
        abilityState = AbilityState.FUNCTIONING,
        truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
        effects = listOf(
            EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = librarian,
                value = InformationValue.NoCharacters(CharacterType.OUTSIDER),
            ),
        ),
        metadata = metadata(librarian),
    )

    private fun metadata(abilityRole: RoleId) = CandidateMetadata(
        candidateSchemaVersion = candidateSchemaVersion,
        decisionType = "${abilityRole.value.lowercase().replace(' ', '-')}-pair-information",
        tags = setOf("natural-truth"),
    )
}
