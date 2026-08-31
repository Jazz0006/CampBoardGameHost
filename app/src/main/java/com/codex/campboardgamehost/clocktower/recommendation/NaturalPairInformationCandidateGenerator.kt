package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StableCandidateIdFactory
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

internal object NaturalPairInformationCandidateGenerator {
    private const val candidateSchemaVersion = "1"
    private const val naturalTruthFamily = "natural-truth"
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")
    private val recluse = RoleId("Recluse")

    fun generate(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
    ): List<DecisionCandidate<PairInformationOutcome>> {
        val source = game.playerAt(sourceSeat) ?: return emptyList()
        if (source.poisoned || source.actualRole != abilityRole) return emptyList()
        return generateHealthyInformationSpace(game, sourceSeat, abilityRole)
    }

    /**
     * Enumerates the truthful information space defined by the perceived ability itself.
     *
     * Recipient functioning state and identity ownership are intentionally outside this seam:
     * a healthy actual-role caller validates those before delegating here, while an impaired
     * caller may reuse the same ability semantics and apply reliability policy afterwards.
     */
    fun generateHealthyInformationSpace(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
    ): List<DecisionCandidate<PairInformationOutcome>> {
        if (game.playerAt(sourceSeat) == null) return emptyList()
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

        val naturalCandidates = targets.flatMap { target ->
            game.players
                .filter { it.seat != sourceSeat && it.seat != target.seat }
                .sortedBy { it.seat }
                .map { decoy -> naturalCandidate(sourceSeat, abilityRole, target.actualRole, target.seat, decoy.seat) }
        }
        if (abilityRole != investigator) return naturalCandidates.distinctBy { it.candidateId }

        // Recluse may register as a Minion for the Investigator interaction. Keep the actual
        // Minion candidates above as TRUE_TO_ACTUAL_STATE; add explicit registered-state
        // candidates rather than pretending Recluse's underlying character type changed.
        val shownMinionRoles = targets.map { it.actualRole }.distinct().sortedBy { it.value }
        val recluseCandidates = game.players
            .filter { it.seat != sourceSeat && it.actualRole == recluse }
            .sortedBy { it.seat }
            .flatMap { target ->
                shownMinionRoles.flatMap { shownRole ->
                    game.players
                        .filter { it.seat != sourceSeat && it.seat != target.seat }
                        .sortedBy { it.seat }
                        .map { decoy ->
                            recluseRegistrationCandidate(
                                sourceSeat = sourceSeat,
                                shownRole = shownRole,
                                targetSeat = target.seat,
                                decoySeat = decoy.seat,
                            )
                        }
                }
            }

        return (naturalCandidates + recluseCandidates).distinctBy { it.candidateId }
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

    private fun recluseRegistrationCandidate(
        sourceSeat: Int,
        shownRole: RoleId,
        targetSeat: Int,
        decoySeat: Int,
    ): DecisionCandidate<PairInformationOutcome> {
        val registration = RegistrationFact(
            interactionId = listOf(
                "pair-information-registration-v1",
                sourceSeat,
                targetSeat,
                shownRole.value,
                RegistrationQuestion.SPECIFIC_MINION.name,
            ).joinToString(":"),
            subjectSeat = targetSeat,
            registeredRole = shownRole,
            registeredType = CharacterType.MINION,
            registeredAlignment = Alignment.EVIL,
            registrationQuestion = RegistrationQuestion.SPECIFIC_MINION,
            reason = RegistrationReason.RECLUSE_ABILITY,
        )
        val outcome = PairInformationOutcome(
            shownRole = shownRole,
            targetSeat = targetSeat,
            decoySeat = decoySeat,
        )
        return DecisionCandidate(
            candidateId = StableCandidateIdFactory.create(
                candidateSchemaVersion = candidateSchemaVersion,
                abilityState = AbilityState.FUNCTIONING,
                truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
                abilityRole = investigator,
                shownRole = shownRole,
                candidateSeats = outcome.candidateSeats,
                registrations = listOf(registration),
            ),
            candidateFamilyId = naturalTruthFamily,
            outcome = outcome,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = listOf(registration),
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = sourceSeat,
                    sourceAbility = investigator,
                    value = InformationValue.PlayerPair(shownRole, outcome.candidateSeats),
                ),
            ),
            metadata = metadata(investigator, extraTags = setOf("registered-truth", "recluse-registration")),
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

    private fun metadata(
        abilityRole: RoleId,
        extraTags: Set<String> = emptySet(),
    ) = CandidateMetadata(
        candidateSchemaVersion = candidateSchemaVersion,
        decisionType = "${abilityRole.value.lowercase().replace(' ', '-')}-pair-information",
        tags = setOf("natural-truth") + extraTags,
    )
}
