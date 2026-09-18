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
        val supported = observation.proposition is InformationProposition.RoleAt ||
            observation.proposition is InformationProposition.AlignmentAt ||
            observation.proposition is InformationProposition.CharacterTypeAt
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

        addIfFeasible(
            additionalFacts = functioningSourceFacts + observation.proposition,
            witness = emptySet(),
        )

        specialRegistrationBranches(
            roles = roles,
            observation = observation,
        ).forEach { branch ->
            addIfFeasible(
                additionalFacts = functioningSourceFacts + listOf(
                    InformationProposition.RoleAt(branch.subjectSeat, branch.actualRole),
                    InformationProposition.AbilityStateAt(
                        seat = branch.subjectSeat,
                        abilityRole = branch.actualRole,
                        abilityState = AbilityState.FUNCTIONING,
                    ),
                ),
                witness = setOf(branch.registrationFact),
            )
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
                candidate.registrationFact(
                    interactionId = observation.observationId,
                    question = question,
                )?.let { fact ->
                    SpecialRegistrationBranch(
                        subjectSeat = subjectSeat,
                        actualRole = actual.id,
                        registrationFact = fact,
                    )
                }
            }
        }
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
    )
}
