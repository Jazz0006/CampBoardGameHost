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

internal data class TroubleBrewingTopologyObservationConstraintBranch(
    val setupFacts: List<InformationProposition>,
    val registrationWitness: Set<RegistrationFact>,
    /**
     * Exact Red Herring identity when this logical branch requires one concrete seat.
     * Null means this branch does not constrain the Red Herring.
     */
    val redHerringSeat: Int? = null,
)

internal sealed interface TroubleBrewingTopologyObservationConstraintPlan {
    data class Ready(
        val branches: List<TroubleBrewingTopologyObservationConstraintBranch>,
    ) : TroubleBrewingTopologyObservationConstraintPlan

    data class Deferred(
        val unsupportedPropositions: List<InformationProposition>,
    ) : TroubleBrewingTopologyObservationConstraintPlan {
        init {
            require(unsupportedPropositions.isNotEmpty())
        }
    }
}

/**
 * Topology-first first-night observation feasibility.
 *
 * The planner preserves logical alternatives as composable constraint branches. The convenience
 * evaluate() projection proves whether at least one branch has a setup/mechanical witness. Whole
 * bundle evaluation can reuse the same branches so finite resources and latent variables remain
 * shared across every observation in the bundle.
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

        val plan = planConstraintBranches(
            rulesetRef = rulesetRef,
            knowledge = knowledge,
            topology = topology,
            roleDefinitions = roleDefinitions,
            observation = observation,
            selectedRegistrationWitness = selectedRegistrationWitness,
            hypothesis = hypothesis,
        )
        if (plan is TroubleBrewingTopologyObservationConstraintPlan.Deferred) {
            return TroubleBrewingTopologyObservationFeasibility.Deferred(
                plan.unsupportedPropositions,
            )
        }

        val witnesses = linkedSetOf<Set<RegistrationFact>>()
        (plan as TroubleBrewingTopologyObservationConstraintPlan.Ready).branches.forEach { branch ->
            val branchKnowledge = knowledge.copy(
                setupKnowledge = knowledge.setupKnowledge + branch.setupFacts,
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
                witnesses += branch.registrationWitness
            }
        }
        return witnesses.toResult()
    }

    fun planConstraintBranches(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        topology: StrategicWorldKey,
        roleDefinitions: Collection<RoleDefinition>,
        observation: EpistemicObservation,
        selectedRegistrationWitness: Set<RegistrationFact>? = null,
        hypothesis: EpistemicHypothesis,
    ): TroubleBrewingTopologyObservationConstraintPlan {
        val roles = roleDefinitions
            .filter { rulesetRef.scriptId in it.scriptIds }
            .associateBy(RoleDefinition::id)

        val sourceSeat = observation.sourceSeat
        val sourceAbility = observation.sourceAbility ?: knowledge.perceivedRole
        val malfunctionAllowed = hypothesis != EpistemicHypothesis.FUNCTIONING_ONLY

        fun malfunctionBranches(): List<TroubleBrewingTopologyObservationConstraintBranch> {
            if (!malfunctionAllowed || sourceSeat == null) return emptyList()
            return listOf(
                AbilityState.MALFUNCTIONING_DRUNK,
                AbilityState.MALFUNCTIONING_POISONED,
            ).map { state ->
                TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = listOf(
                        InformationProposition.AbilityStateAt(
                            seat = sourceSeat,
                            abilityRole = sourceAbility,
                            abilityState = state,
                        ),
                    ),
                    registrationWitness = emptySet(),
                )
            }
        }

        if (observation.reliability == ObservationReliability.KNOWN_MALFUNCTIONING) {
            val branches = malfunctionBranches()
                .filterBySelectedWitness(selectedRegistrationWitness, roles)
            return TroubleBrewingTopologyObservationConstraintPlan.Ready(branches)
        }

        val propositionPlan = propositionBranches(
            topology = topology,
            playerCount = knowledge.playerCountOrNull() ?: return TroubleBrewingTopologyObservationConstraintPlan.Deferred(
                listOf(observation.proposition),
            ),
            proposition = observation.proposition,
            observation = observation,
            roles = roles,
        )
        if (propositionPlan is TroubleBrewingTopologyObservationConstraintPlan.Deferred) {
            return propositionPlan
        }

        var functioningBranches =
            (propositionPlan as TroubleBrewingTopologyObservationConstraintPlan.Ready).branches

        if (
            observation.reliability == ObservationReliability.RECEIVED_AS_FUNCTIONING &&
            sourceSeat != null
        ) {
            val sourceFunctioning = InformationProposition.AbilityStateAt(
                seat = sourceSeat,
                abilityRole = sourceAbility,
                abilityState = AbilityState.FUNCTIONING,
            )
            functioningBranches = functioningBranches.map { branch ->
                branch.copy(setupFacts = listOf(sourceFunctioning) + branch.setupFacts)
            }
        }

        val allBranches = when (observation.reliability) {
            ObservationReliability.RECEIVED_AS_FUNCTIONING ->
                malfunctionBranches() + functioningBranches
            ObservationReliability.NOT_ABILITY_INFORMATION ->
                functioningBranches
            ObservationReliability.KNOWN_MALFUNCTIONING ->
                error("Known-malfunctioning branch returned earlier.")
        }.distinct().filterBySelectedWitness(selectedRegistrationWitness, roles)

        return TroubleBrewingTopologyObservationConstraintPlan.Ready(allBranches)
    }

    private fun propositionBranches(
        topology: StrategicWorldKey,
        playerCount: Int,
        proposition: InformationProposition,
        observation: EpistemicObservation,
        roles: Map<RoleId, RoleDefinition>,
    ): TroubleBrewingTopologyObservationConstraintPlan = when (proposition) {
        is InformationProposition.RoleAt,
        is InformationProposition.AlignmentAt,
        is InformationProposition.CharacterTypeAt -> {
            val branches = mutableListOf(
                TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = listOf(proposition),
                    registrationWitness = emptySet(),
                ),
            )
            specialRegistrationBranches(
                roles = roles,
                observation = observation.copy(proposition = proposition),
            ).forEach { special ->
                branches += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = special.additionalFacts(),
                    registrationWitness = setOf(special.registrationFact),
                )
            }
            TroubleBrewingTopologyObservationConstraintPlan.Ready(branches.distinct())
        }

        is InformationProposition.ShownRoleAt,
        is InformationProposition.AbilityStateAt,
        is InformationProposition.RoleInPlay,
        is InformationProposition.PlayerCount,
        is InformationProposition.SetupProfile ->
            TroubleBrewingTopologyObservationConstraintPlan.Ready(
                listOf(
                    TroubleBrewingTopologyObservationConstraintBranch(
                        setupFacts = listOf(proposition),
                        registrationWitness = emptySet(),
                    ),
                ),
            )

        is InformationProposition.AnyOf -> {
            val alternatives = mutableListOf<TroubleBrewingTopologyObservationConstraintBranch>()
            proposition.alternatives.forEach { alternative ->
                when (
                    val plan = propositionBranches(
                        topology = topology,
                        playerCount = playerCount,
                        proposition = alternative,
                        observation = observation.copy(proposition = alternative),
                        roles = roles,
                    )
                ) {
                    is TroubleBrewingTopologyObservationConstraintPlan.Deferred -> return plan
                    is TroubleBrewingTopologyObservationConstraintPlan.Ready ->
                        alternatives += plan.branches
                }
            }
            TroubleBrewingTopologyObservationConstraintPlan.Ready(alternatives.distinct())
        }

        is InformationProposition.AllOf -> {
            var combined = listOf(
                TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = emptyList(),
                    registrationWitness = emptySet(),
                ),
            )
            proposition.propositions.forEach { child ->
                val plan = propositionBranches(
                    topology = topology,
                    playerCount = playerCount,
                    proposition = child,
                    observation = observation.copy(proposition = child),
                    roles = roles,
                )
                if (plan is TroubleBrewingTopologyObservationConstraintPlan.Deferred) return plan
                plan as TroubleBrewingTopologyObservationConstraintPlan.Ready
                combined = combined.flatMap { prefix ->
                    plan.branches.mapNotNull { next -> combineBranches(prefix, next) }
                }.distinct()
            }
            TroubleBrewingTopologyObservationConstraintPlan.Ready(combined)
        }

        is InformationProposition.Not -> {
            val roleInPlay = proposition.proposition as? InformationProposition.RoleInPlay
                ?: return TroubleBrewingTopologyObservationConstraintPlan.Deferred(listOf(proposition))
            TroubleBrewingTopologyObservationConstraintPlan.Ready(
                listOf(
                    TroubleBrewingTopologyObservationConstraintBranch(
                        setupFacts = listOf(
                            InformationProposition.RoleInPlay(
                                role = roleInPlay.role,
                                inPlay = !roleInPlay.inPlay,
                            ),
                        ),
                        registrationWitness = emptySet(),
                    ),
                ),
            )
        }

        is InformationProposition.NumericResult -> {
            if (
                proposition.metric != NumericMetric.ADJACENT_EVIL_PAIRS &&
                proposition.metric != NumericMetric.LIVING_EVIL_NEIGHBOURS
            ) {
                return TroubleBrewingTopologyObservationConstraintPlan.Deferred(listOf(proposition))
            }
            TroubleBrewingTopologyObservationConstraintPlan.Ready(
                numericObservationBranches(
                    topology = topology,
                    playerCount = playerCount,
                    proposition = proposition,
                    observation = observation,
                    roles = roles,
                ),
            )
        }

        is InformationProposition.BooleanResult -> {
            if (proposition.metric != BooleanMetric.DEMON_OR_RED_HERRING_PRESENT) {
                return TroubleBrewingTopologyObservationConstraintPlan.Deferred(listOf(proposition))
            }
            TroubleBrewingTopologyObservationConstraintPlan.Ready(
                fortuneTellerObservationBranches(
                    topology = topology,
                    playerCount = playerCount,
                    proposition = proposition,
                    observation = observation,
                    roles = roles,
                ),
            )
        }

        else -> TroubleBrewingTopologyObservationConstraintPlan.Deferred(listOf(proposition))
    }

    private fun combineBranches(
        first: TroubleBrewingTopologyObservationConstraintBranch,
        second: TroubleBrewingTopologyObservationConstraintBranch,
    ): TroubleBrewingTopologyObservationConstraintBranch? {
        val firstRedHerring = first.redHerringSeat
        val secondRedHerring = second.redHerringSeat
        if (
            firstRedHerring != null &&
            secondRedHerring != null &&
            firstRedHerring != secondRedHerring
        ) {
            return null
        }
        return TroubleBrewingTopologyObservationConstraintBranch(
            setupFacts = first.setupFacts + second.setupFacts,
            registrationWitness = first.registrationWitness + second.registrationWitness,
            redHerringSeat = firstRedHerring ?: secondRedHerring,
        )
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
    ): List<TroubleBrewingTopologyObservationConstraintBranch> {
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
        val result = linkedSetOf<TroubleBrewingTopologyObservationConstraintBranch>()

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
                result += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = specials.flatMap(SpecialRegistrationBranch::additionalFacts),
                    registrationWitness =
                        specials.mapTo(linkedSetOf()) { it.registrationFact },
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
    ): List<TroubleBrewingTopologyObservationConstraintBranch> {
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
        val result = linkedSetOf<TroubleBrewingTopologyObservationConstraintBranch>()

        if (proposition.value) {
            if (demonSelected) {
                result += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = emptyList(),
                    registrationWitness = emptySet(),
                )
                return result.toList()
            }

            selectedGoodSeats.forEach { redHerringSeat ->
                result += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, true),
                    ),
                    registrationWitness = emptySet(),
                    redHerringSeat = redHerringSeat,
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

                result += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = recluse.additionalFacts() + listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, false),
                    ),
                    registrationWitness = setOf(recluse.registrationFact),
                )
                goodSeatsOutsideSelection.forEach { redHerringSeat ->
                    result += TroubleBrewingTopologyObservationConstraintBranch(
                        setupFacts = recluse.additionalFacts() + listOf(
                            InformationProposition.RoleInPlay(fortuneTeller.id, true),
                        ),
                        registrationWitness = setOf(recluse.registrationFact),
                        redHerringSeat = redHerringSeat,
                    )
                }
            }
        } else if (!demonSelected) {
            result += TroubleBrewingTopologyObservationConstraintBranch(
                setupFacts = listOf(
                    InformationProposition.RoleInPlay(fortuneTeller.id, false),
                ),
                registrationWitness = emptySet(),
            )
            goodSeatsOutsideSelection.forEach { redHerringSeat ->
                result += TroubleBrewingTopologyObservationConstraintBranch(
                    setupFacts = listOf(
                        InformationProposition.RoleInPlay(fortuneTeller.id, true),
                    ),
                    registrationWitness = emptySet(),
                    redHerringSeat = redHerringSeat,
                )
            }
        }

        return result.toList()
    }

    private fun List<TroubleBrewingTopologyObservationConstraintBranch>.filterBySelectedWitness(
        selected: Set<RegistrationFact>?,
        roles: Map<RoleId, RoleDefinition>,
    ): List<TroubleBrewingTopologyObservationConstraintBranch> {
        if (selected == null) return this
        return filter { branch ->
            RegistrationWitnessSemanticMatcher.matches(
                selected = selected,
                exactWitness = branch.registrationWitness,
                roles = roles,
            )
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

    private fun PlayerKnowledgeSnapshot.playerCountOrNull(): Int? =
        setupKnowledge.filterIsInstance<InformationProposition.PlayerCount>()
            .map(InformationProposition.PlayerCount::value)
            .distinct()
            .singleOrNull()
}
