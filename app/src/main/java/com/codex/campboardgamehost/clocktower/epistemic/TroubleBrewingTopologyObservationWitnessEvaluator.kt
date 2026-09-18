package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

internal sealed interface TroubleBrewingTopologyObservationFeasibility {
    data class Feasible(
        val registrationWitnesses: Set<Set<RegistrationFact>>,
    ) : TroubleBrewingTopologyObservationFeasibility {
        init {
            require(registrationWitnesses.isNotEmpty()) {
                "A feasible topology observation must expose at least one registration witness."
            }
        }
    }

    object Infeasible : TroubleBrewingTopologyObservationFeasibility

    data class Deferred(
        val unsupportedPropositions: List<InformationProposition>,
    ) : TroubleBrewingTopologyObservationFeasibility {
        init {
            require(unsupportedPropositions.isNotEmpty())
        }
    }
}

/**
 * D4C4a topology-first observation feasibility for identity observations.
 *
 * This evaluator deliberately separates actual setup facts from interaction-local registration.
 * It also preserves the existing observation reliability semantics: mechanically-credible
 * malfunctioning source branches may satisfy received ability information without asserting the
 * proposition itself.
 */
internal object TroubleBrewingTopologyObservationWitnessEvaluator {
    fun evaluate(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        roleDefinitions: Collection<RoleDefinition>,
        observation: EpistemicObservation,
        selectedRegistrationWitness: Set<RegistrationFact>? = null,
        hypothesis: EpistemicHypothesis,
    ): TroubleBrewingTopologyObservationFeasibility {
        val supported = when (val proposition = observation.proposition) {
            is InformationProposition.RoleAt,
            is InformationProposition.AlignmentAt,
            is InformationProposition.CharacterTypeAt -> true
            is InformationProposition.NumericResult ->
                proposition.metric == NumericMetric.ADJACENT_EVIL_PAIRS ||
                    proposition.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS
            is InformationProposition.BooleanResult ->
                proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT
            else -> false
        }
        if (!supported) {
            return TroubleBrewingTopologyObservationFeasibility.Deferred(
                listOf(observation.proposition),
            )
        }

        when (
            val base = TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
                rulesetRef = rulesetRef,
                knowledge = knowledge,
                profile = profile,
                topology = topology,
                roleDefinitions = roleDefinitions,
            )
        ) {
            TroubleBrewingTopologySetupFeasibility.Infeasible ->
                return TroubleBrewingTopologyObservationFeasibility.Infeasible
            is TroubleBrewingTopologySetupFeasibility.Deferred ->
                return TroubleBrewingTopologyObservationFeasibility.Deferred(
                    base.unsupportedPropositions,
                )
            TroubleBrewingTopologySetupFeasibility.Feasible -> Unit
        }

        val roles = roleDefinitions
            .filter { rulesetRef.scriptId in it.scriptIds }
            .associateBy(RoleDefinition::id)
        val witnesses = linkedSetOf<Set<RegistrationFact>>()

        fun retainIfSelected(witness: Set<RegistrationFact>): Boolean =
            selectedRegistrationWitness == null ||
                RegistrationWitnessSemanticMatcher.matches(
                    selected = selectedRegistrationWitness,
                    exactWitness = witness,
                    roles = roles,
                )

        fun addIfFeasible(
            additionalFacts: List<InformationProposition>,
            witness: Set<RegistrationFact>,
        ) {
            if (!retainIfSelected(witness)) return
            val branchKnowledge = knowledge.copy(
                setupKnowledge = knowledge.setupKnowledge + additionalFacts,
            )
            if (
                TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
                    rulesetRef = rulesetRef,
                    knowledge = branchKnowledge,
                    profile = profile,
                    topology = topology,
                    roleDefinitions = roleDefinitions,
                ) == TroubleBrewingTopologySetupFeasibility.Feasible
            ) {
                witnesses += witness
            }
        }

        val sourceSeat = observation.sourceSeat
        val sourceAbility = observation.sourceAbility ?: knowledge.perceivedRole
        val malfunctionAllowed = hypothesis != EpistemicHypothesis.FUNCTIONING_ONLY

        fun addMalfunctionBranches() {
            if (!malfunctionAllowed || sourceSeat == null) return
            addIfFeasible(
                listOf(
                    InformationProposition.AbilityStateAt(
                        seat = sourceSeat,
                        abilityRole = sourceAbility,
                        abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                    ),
                ),
                emptySet(),
            )
            addIfFeasible(
                listOf(
                    InformationProposition.AbilityStateAt(
                        seat = sourceSeat,
                        abilityRole = sourceAbility,
                        abilityState = AbilityState.MALFUNCTIONING_POISONED,
                    ),
                ),
                emptySet(),
            )
        }

        when (observation.reliability) {
            ObservationReliability.KNOWN_MALFUNCTIONING -> {
                addMalfunctionBranches()
                return witnesses.toResult()
            }
            ObservationReliability.RECEIVED_AS_FUNCTIONING -> addMalfunctionBranches()
            ObservationReliability.NOT_ABILITY_INFORMATION -> Unit
        }

        val functioningSourceFacts = if (
            observation.reliability == ObservationReliability.RECEIVED_AS_FUNCTIONING &&
            sourceSeat != null
        ) {
            listOf(
                InformationProposition.AbilityStateAt(
                    seat = sourceSeat,
                    abilityRole = sourceAbility,
                    abilityState = AbilityState.FUNCTIONING,
                ),
            )
        } else {
            emptyList()
        }

        when (observation.proposition) {
            is InformationProposition.RoleAt,
            is InformationProposition.AlignmentAt,
            is InformationProposition.CharacterTypeAt -> {
                addIfFeasible(
                    additionalFacts = functioningSourceFacts + observation.proposition,
                    witness = emptySet(),
                )

                specialRegistrationBranches(
                    roles = roles,
                    observation = observation,
                ).forEach { branch ->
                    addIfFeasible(
                        additionalFacts = functioningSourceFacts + branch.additionalFacts(),
                        witness = setOf(branch.registrationFact),
                    )
                }
            }

            is InformationProposition.NumericResult ->
                numericObservationBranches(
                    topology = topology,
                    playerCount = profile.total,
                    proposition = observation.proposition,
                    observation = observation,
                    roles = roles,
                ).forEach { branch ->
                    addIfFeasible(
                        additionalFacts = functioningSourceFacts + branch.additionalFacts,
                        witness = branch.registrationWitness,
                    )
                }

            is InformationProposition.BooleanResult ->
                fortuneTellerObservationBranches(
                    topology = topology,
                    playerCount = profile.total,
                    proposition = observation.proposition,
                    observation = observation,
                    roles = roles,
                ).forEach { branch ->
                    addIfFeasible(
                        additionalFacts = functioningSourceFacts + branch.additionalFacts,
                        witness = branch.registrationWitness,
                    )
                }

            else -> error("Unsupported proposition escaped D4C4 capability guard.")
        }

        return witnesses.toResult()
    }

    private fun specialRegistrationBranches(
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation,
    ): List<SpecialRegistrationBranch> {
        val proposition = observation.proposition
        val subjectSeat = when (proposition) {
            is InformationProposition.RoleAt -> proposition.seat
            is InformationProposition.AlignmentAt -> proposition.seat
            is InformationProposition.CharacterTypeAt -> proposition.seat
            else -> return emptyList()
        }

        val question = when (proposition) {
            is InformationProposition.RoleAt -> RegistrationQuestion.ROLE
            is InformationProposition.AlignmentAt -> RegistrationQuestion.ALIGNMENT
            is InformationProposition.CharacterTypeAt ->
                if (proposition.characterType == CharacterType.DEMON) {
                    RegistrationQuestion.DEMON
                } else {
                    RegistrationQuestion.CHARACTER_TYPE
                }
            else -> return emptyList()
        }

        val allowedRoles = when (proposition) {
            is InformationProposition.RoleAt ->
                listOfNotNull(roles[proposition.role])
            is InformationProposition.AlignmentAt ->
                roles.values.filter { it.alignment == proposition.alignment }
            is InformationProposition.CharacterTypeAt ->
                roles.values.filter { it.type == proposition.characterType }
            else -> emptyList()
        }
        if (allowedRoles.isEmpty()) return emptyList()

        return listOf("Spy", "Recluse").flatMap { specialName ->
            val actual = roles.values.singleOrNull { it.id.value == specialName }
                ?: return@flatMap emptyList()
            val subject = TroubleBrewingRegistrationSubject(
                seat = subjectSeat,
                actualRole = actual.id,
                actualAlignment = actual.alignment,
                actualType = actual.type,
                effectiveRole = actual.id,
                poisoned = false,
            )
            TroubleBrewingRegistrationDomain.resolve(
                subject = subject,
                allowedRoles = allowedRoles,
                question = question,
            ).special.mapNotNull { candidate ->
                val reason = candidate.specialReason ?: return@mapNotNull null
                val fact = RegistrationFact(
                    interactionId = observation.observationId,
                    subjectSeat = subjectSeat,
                    registeredRole = (proposition as? InformationProposition.RoleAt)?.role,
                    registeredType =
                        (proposition as? InformationProposition.CharacterTypeAt)?.characterType,
                    registeredAlignment = when (reason) {
                        com.codex.campboardgamehost.clocktower.domain.RegistrationReason.SPY_ABILITY ->
                            Alignment.GOOD
                        com.codex.campboardgamehost.clocktower.domain.RegistrationReason.RECLUSE_ABILITY ->
                            Alignment.EVIL
                        com.codex.campboardgamehost.clocktower.domain.RegistrationReason.OTHER ->
                            return@mapNotNull null
                    },
                    registrationQuestion = question,
                    reason = reason,
                )
                SpecialRegistrationBranch(
                    subjectSeat = subjectSeat,
                    actualRole = actual.id,
                    registrationFact = fact,
                )
            }
        }
    }

    private fun numericObservationBranches(
        topology: StrategicWorldKey,
        playerCount: Int,
        proposition: InformationProposition.NumericResult,
        observation: EpistemicObservation,
        roles: Map<RoleId, RoleDefinition>,
    ): List<ObservationConstraintBranch> {
        val seats = if (proposition.subjectSeats.isEmpty()) {
            (1..playerCount).toList()
        } else {
            proposition.subjectSeats
        }
        require(seats.isNotEmpty() && seats.all { it in 1..playerCount }) {
            "Topology numeric observation references seats outside the player range."
        }

        val spyCandidates = seats
            .filter { it in topology.minionSeats }
            .mapNotNull { seat ->
                specialRegistrationBranches(
                    roles = roles,
                    observation = observation.copy(
                        proposition = InformationProposition.AlignmentAt(seat, Alignment.GOOD),
                    ),
                ).singleOrNull { it.actualRole.value == "Spy" }
            }
        val recluseCandidates = seats
            .filter { it != topology.demonSeat && it !in topology.minionSeats }
            .mapNotNull { seat ->
                specialRegistrationBranches(
                    roles = roles,
                    observation = observation.copy(
                        proposition = InformationProposition.AlignmentAt(seat, Alignment.EVIL),
                    ),
                ).singleOrNull { it.actualRole.value == "Recluse" }
            }

        val spyChoices = listOf<SpecialRegistrationBranch?>(null) + spyCandidates
        val recluseChoices = listOf<SpecialRegistrationBranch?>(null) + recluseCandidates
        val result = linkedSetOf<ObservationConstraintBranch>()

        for (spy in spyChoices) {
            for (recluse in recluseChoices) {
                val evilBySeat = seats.associateWith { seat ->
                    when {
                        spy?.subjectSeat == seat -> false
                        recluse?.subjectSeat == seat -> true
                        seat == topology.demonSeat || seat in topology.minionSeats -> true
                        else -> false
                    }
                }
                val value = when (proposition.metric) {
                    NumericMetric.ADJACENT_EVIL_PAIRS ->
                        seats.indices.count { index ->
                            evilBySeat.getValue(seats[index]) &&
                                evilBySeat.getValue(seats[(index + 1) % seats.size])
                        }
                    NumericMetric.LIVING_EVIL_NEIGHBOURS ->
                        seats.count { evilBySeat.getValue(it) }
                    else -> continue
                }
                if (value != proposition.value) continue

                val specials = listOfNotNull(spy, recluse)
                result += ObservationConstraintBranch(
                    additionalFacts = specials.flatMap(SpecialRegistrationBranch::additionalFacts),
                    registrationWitness = specials.mapTo(linkedSetOf()) { it.registrationFact },
                )
            }
        }
        return result.toList()
    }

    private fun fortuneTellerObservationBranches(
        topology: StrategicWorldKey,
        playerCount: Int,
        proposition: InformationProposition.BooleanResult,
        observation: EpistemicObservation,
        roles: Map<RoleId, RoleDefinition>,
    ): List<ObservationConstraintBranch> {
        require(proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT)
        val seats = if (proposition.subjectSeats.isEmpty()) {
            (1..playerCount).toList()
        } else {
            proposition.subjectSeats
        }
        require(seats.isNotEmpty() && seats.all { it in 1..playerCount }) {
            "Topology Fortune Teller observation references seats outside the player range."
        }

        val fortuneTeller = roles.values.singleOrNull { it.id.value == "Fortune Teller" }
            ?: return emptyList()
        val demonSelected = topology.demonSeat in seats
        val selectedGoodSeats = seats.filter { seat ->
            seat != topology.demonSeat && seat !in topology.minionSeats
        }
        val goodSeatsOutsideSelection = (1..playerCount).filter { seat ->
            seat !in seats && seat != topology.demonSeat && seat !in topology.minionSeats
        }
        val result = linkedSetOf<ObservationConstraintBranch>()

        if (proposition.value) {
            if (demonSelected) {
                result += ObservationConstraintBranch(emptyList(), emptySet())
                return result.toList()
            }

            if (selectedGoodSeats.isNotEmpty()) {
                result += ObservationConstraintBranch(
                    additionalFacts = listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, true),
                    ),
                    registrationWitness = emptySet(),
                )
            }

            selectedGoodSeats.forEach { seat ->
                val recluse = specialRegistrationBranches(
                    roles = roles,
                    observation = observation.copy(
                        proposition = InformationProposition.CharacterTypeAt(
                            seat,
                            CharacterType.DEMON,
                        ),
                    ),
                ).singleOrNull { it.actualRole.value == "Recluse" } ?: return@forEach

                result += ObservationConstraintBranch(
                    additionalFacts = recluse.additionalFacts() + listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, false),
                    ),
                    registrationWitness = setOf(recluse.registrationFact),
                )
                if (goodSeatsOutsideSelection.isNotEmpty()) {
                    result += ObservationConstraintBranch(
                        additionalFacts = recluse.additionalFacts() + listOf(
                            InformationProposition.RoleInPlay(fortuneTeller.id, true),
                        ),
                        registrationWitness = setOf(recluse.registrationFact),
                    )
                }
            }
        } else if (!demonSelected) {
            result += ObservationConstraintBranch(
                additionalFacts = listOf(
                    InformationProposition.RoleInPlay(fortuneTeller.id, false),
                ),
                registrationWitness = emptySet(),
            )
            if (goodSeatsOutsideSelection.isNotEmpty()) {
                result += ObservationConstraintBranch(
                    additionalFacts = listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, true),
                    ),
                    registrationWitness = emptySet(),
                )
            }
        }

        return result.toList()
    }

    private fun Set<Set<RegistrationFact>>.toResult(): TroubleBrewingTopologyObservationFeasibility =
        if (isEmpty()) {
            TroubleBrewingTopologyObservationFeasibility.Infeasible
        } else {
            TroubleBrewingTopologyObservationFeasibility.Feasible(this)
        }

    private data class SpecialRegistrationBranch(
        val subjectSeat: Int,
        val actualRole: RoleId,
        val registrationFact: RegistrationFact,
    ) {
        fun additionalFacts(): List<InformationProposition> = listOf(
            InformationProposition.RoleAt(subjectSeat, actualRole),
            InformationProposition.AbilityStateAt(
                seat = subjectSeat,
                abilityRole = actualRole,
                abilityState = AbilityState.FUNCTIONING,
            ),
        )
    }

    private data class ObservationConstraintBranch(
        val additionalFacts: List<InformationProposition>,
        val registrationWitness: Set<RegistrationFact>,
    )

    private val InformationProposition.SetupProfile.total: Int
        get() = townsfolk + outsiders + minions + demons
}
