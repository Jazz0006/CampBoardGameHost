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
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StableCandidateIdFactory
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationCandidate
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

internal object NaturalPairInformationCandidateGenerator {
    private const val candidateSchemaVersion = "1"
    private const val naturalTruthFamily = "natural-truth"
    private val washerwoman = RoleId("Washerwoman")
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")
    private val spy = RoleId("Spy")
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
     * Role definitions are optional for compatibility callers; the production recommender
     * supplies them so registration truth can cover legal script roles that are not in play.
     */
    fun generateHealthyInformationSpace(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        roleDefinitions: List<RoleDefinition> = emptyList(),
    ): List<DecisionCandidate<PairInformationOutcome>> {
        if (game.playerAt(sourceSeat) == null) return emptyList()
        val targetType = when (abilityRole) {
            washerwoman -> CharacterType.TOWNSFOLK
            librarian -> CharacterType.OUTSIDER
            investigator -> CharacterType.MINION
            else -> return emptyList()
        }
        val targets = game.players
            .filter { it.seat != sourceSeat && it.actualType == targetType }
            .sortedBy { it.seat }
        val zeroCandidates = if (abilityRole == librarian && targets.isEmpty()) {
            listOf(noOutsiderCandidate(sourceSeat))
        } else {
            emptyList()
        }

        val naturalCandidates = targets.flatMap { target ->
            game.players
                .filter { it.seat != sourceSeat && it.seat != target.seat }
                .sortedBy { it.seat }
                .map { decoy -> naturalCandidate(sourceSeat, abilityRole, target.actualRole, target.seat, decoy.seat) }
        }
        if (abilityRole != investigator) {
            val allowedGoodRoles = roleDefinitions
                .asSequence()
                .filter {
                    game.script in it.scriptIds &&
                        it.type == targetType &&
                        it.alignment == Alignment.GOOD
                }
                .distinctBy { it.id }
                .sortedBy { it.id.value }
                .toList()
                .ifEmpty {
                    targets.distinctBy { it.actualRole }.map { target ->
                        RoleDefinition(target.actualRole, target.actualAlignment, target.actualType, setOf(game.script))
                    }
                }
            val naturalOutcomes = naturalCandidates.map { it.outcome }.toSet()
            val spyCandidates = game.players
                .filter { it.seat != sourceSeat && it.actualRole == spy }
                .sortedBy { it.seat }
                .flatMap { target ->
                    val legalRegistrations = TroubleBrewingRegistrationDomain.resolve(
                        subject = TroubleBrewingRegistrationSubject.from(target),
                        allowedRoles = allowedGoodRoles,
                        question = RegistrationQuestion.ROLE,
                    ).special
                    legalRegistrations.flatMap { registration ->
                        game.players
                            .filter { it.seat != sourceSeat && it.seat != target.seat }
                            .sortedBy { it.seat }
                            .map { decoy ->
                                spyRegistrationCandidate(
                                    sourceSeat = sourceSeat,
                                    abilityRole = abilityRole,
                                    registration = registration,
                                    targetSeat = target.seat,
                                    decoySeat = decoy.seat,
                                )
                            }
                    }
                }
                .filterNot { it.outcome in naturalOutcomes }

            return (zeroCandidates + naturalCandidates + spyCandidates).distinctBy { it.candidateId }
        }

        // Recluse may register as any Minion on the current script for the Investigator
        // interaction, including a Minion that is not actually in play. Keep actual Minion
        // candidates as TRUE_TO_ACTUAL_STATE and represent the Recluse path explicitly as
        // TRUE_TO_REGISTERED_STATE rather than changing the underlying player identity.
        val allowedMinionRoles = roleDefinitions
            .asSequence()
            .filter { game.script in it.scriptIds && it.type == CharacterType.MINION }
            .distinctBy { it.id }
            .sortedBy { it.id.value }
            .toList()
            .ifEmpty {
                targets.distinctBy { it.actualRole }.map { target ->
                    RoleDefinition(target.actualRole, target.actualAlignment, target.actualType, setOf(game.script))
                }
            }
        val recluseCandidates = game.players
            .filter { it.seat != sourceSeat && it.actualRole == recluse }
            .sortedBy { it.seat }
            .flatMap { target ->
                val legalRegistrations = TroubleBrewingRegistrationDomain.resolve(
                    subject = TroubleBrewingRegistrationSubject.from(target),
                    allowedRoles = allowedMinionRoles,
                    question = RegistrationQuestion.SPECIFIC_MINION,
                ).special
                legalRegistrations.flatMap { registration ->
                    game.players
                        .filter { it.seat != sourceSeat && it.seat != target.seat }
                        .sortedBy { it.seat }
                        .map { decoy ->
                            recluseRegistrationCandidate(
                                sourceSeat = sourceSeat,
                                registration = registration,
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

    private fun spyRegistrationCandidate(
        sourceSeat: Int,
        abilityRole: RoleId,
        registration: TroubleBrewingRegistrationCandidate,
        targetSeat: Int,
        decoySeat: Int,
    ): DecisionCandidate<PairInformationOutcome> {
        val shownRole = registration.registeredRole
        val registrationFact = requireNotNull(registration.registrationFact(
            interactionId = listOf(
                "pair-information-registration-v1",
                sourceSeat,
                targetSeat,
                shownRole.value,
                RegistrationQuestion.ROLE.name,
            ).joinToString(":"),
            question = RegistrationQuestion.ROLE,
        ))
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
                abilityRole = abilityRole,
                shownRole = shownRole,
                candidateSeats = outcome.candidateSeats,
                registrations = listOf(registrationFact),
            ),
            candidateFamilyId = naturalTruthFamily,
            outcome = outcome,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = listOf(registrationFact),
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = sourceSeat,
                    sourceAbility = abilityRole,
                    value = InformationValue.PlayerPair(shownRole, outcome.candidateSeats),
                ),
            ),
            metadata = metadata(abilityRole, extraTags = setOf("registered-truth", "spy-registration")),
        )
    }

    private fun recluseRegistrationCandidate(
        sourceSeat: Int,
        registration: TroubleBrewingRegistrationCandidate,
        targetSeat: Int,
        decoySeat: Int,
    ): DecisionCandidate<PairInformationOutcome> {
        val shownRole = registration.registeredRole
        val registrationFact = requireNotNull(registration.registrationFact(
            interactionId = listOf(
                "pair-information-registration-v1",
                sourceSeat,
                targetSeat,
                shownRole.value,
                RegistrationQuestion.SPECIFIC_MINION.name,
            ).joinToString(":"),
            question = RegistrationQuestion.SPECIFIC_MINION,
        ))
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
                registrations = listOf(registrationFact),
            ),
            candidateFamilyId = naturalTruthFamily,
            outcome = outcome,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = listOf(registrationFact),
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
