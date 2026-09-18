package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RulesetRef

/**
 * Exact strategic result for a pristine first-night hypothetical bundle.
 *
 * No mechanical-world cardinality is present by design. This contract answers exact topology
 * feasibility and structure without redefining the legacy exhaustive cardinality API.
 */
internal data class ExactStrategicTopologyBundleDiagnostics(
    val bundleId: String,
    val recipientSeat: Int,
    val beforeStructure: ExactWorldStructureDiagnostics,
    val afterStructure: ExactWorldStructureDiagnostics,
) {
    val beforeFeasible: Boolean get() = beforeStructure.strategicWorldKeys.isNotEmpty()
    val afterFeasible: Boolean get() = afterStructure.strategicWorldKeys.isNotEmpty()
}

internal sealed interface ExactStrategicTopologyBundleEvaluation {
    data class Ready(
        val diagnostics: List<ExactStrategicTopologyBundleDiagnostics>,
    ) : ExactStrategicTopologyBundleEvaluation

    data class Deferred(
        val unsupportedPropositions: List<InformationProposition>,
    ) : ExactStrategicTopologyBundleEvaluation {
        init {
            require(unsupportedPropositions.isNotEmpty())
        }
    }
}

/**
 * Topology-first exact bundle evaluator for pristine Trouble Brewing first-night planning.
 *
 * Every observation contributes alternative logical constraint branches. A topology survives only
 * when one composed branch has one shared setup/mechanical witness.
 */
internal object TroubleBrewingTopologyHypotheticalBundleEvaluator {
    fun evaluate(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        roleDefinitions: Collection<RoleDefinition>,
        queries: List<ExactHypotheticalObservationBundleQuery>,
        hypothesis: EpistemicHypothesis,
    ): ExactStrategicTopologyBundleEvaluation {
        require(rulesetRef.scriptId.value == "trouble_brewing") {
            "Topology-first bundle evaluation currently supports only Trouble Brewing."
        }
        require(queries.all { it.recipientSeat == knowledge.recipientSeat }) {
            "Every topology-first bundle query must target the supplied knowledge recipient."
        }

        val playerCount = requireNotNull(
            knowledge.setupKnowledge
                .filterIsInstance<InformationProposition.PlayerCount>()
                .map(InformationProposition.PlayerCount::value)
                .distinct()
                .singleOrNull(),
        ) {
            "Topology-first bundle evaluation requires exactly one player-count setup fact."
        }

        val baseCandidates = mutableListOf<ProfileTopology>()
        val unsupported = linkedSetOf<InformationProposition>()

        TroubleBrewingSetupProfiles.legalProfiles(playerCount).forEach { profile ->
            TroubleBrewingStrategicTopologyDomain.enumerate(playerCount, profile).forEach { topology ->
                when (
                    val base = TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
                        rulesetRef = rulesetRef,
                        knowledge = knowledge,
                        profile = profile,
                        topology = topology,
                        roleDefinitions = roleDefinitions,
                    )
                ) {
                    TroubleBrewingTopologySetupFeasibility.Feasible ->
                        baseCandidates += ProfileTopology(profile, topology)
                    TroubleBrewingTopologySetupFeasibility.Infeasible -> Unit
                    is TroubleBrewingTopologySetupFeasibility.Deferred ->
                        unsupported += base.unsupportedPropositions
                }
            }
        }

        if (unsupported.isNotEmpty()) {
            return ExactStrategicTopologyBundleEvaluation.Deferred(unsupported.toList())
        }

        val beforeKeys = baseCandidates
            .mapTo(linkedSetOf(), ProfileTopology::topology)
        val beforeStructure = structureFromKeys(playerCount, beforeKeys)
        val diagnostics = mutableListOf<ExactStrategicTopologyBundleDiagnostics>()

        queries.forEach { query ->
            val afterKeys = linkedSetOf<StrategicWorldKey>()

            baseCandidates.forEach candidateLoop@{ candidate ->
                var composed = listOf(BundleConstraintBranch())

                query.observations.forEach { observation ->
                    val selected = query.registrationWitnessBindingFor(observation)?.registrations
                    when (
                        val plan = TroubleBrewingTopologyObservationWitnessEvaluator.planConstraintBranches(
                            rulesetRef = rulesetRef,
                            knowledge = knowledge,
                            topology = candidate.topology,
                            roleDefinitions = roleDefinitions,
                            observation = observation,
                            selectedRegistrationWitness = selected,
                            hypothesis = hypothesis,
                        )
                    ) {
                        is TroubleBrewingTopologyObservationConstraintPlan.Deferred -> {
                            unsupported += plan.unsupportedPropositions
                            return@candidateLoop
                        }
                        is TroubleBrewingTopologyObservationConstraintPlan.Ready -> {
                            if (plan.branches.isEmpty()) {
                                return@candidateLoop
                            }
                            composed = composed.flatMap { prefix ->
                                plan.branches.mapNotNull { next ->
                                    combine(prefix, next)
                                }
                            }.distinct()
                            if (composed.isEmpty()) {
                                return@candidateLoop
                            }
                        }
                    }
                }

                val survives = composed.any { branch ->
                    val branchKnowledge = knowledge.copy(
                        setupKnowledge = knowledge.setupKnowledge + branch.setupFacts,
                    )
                    TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
                        rulesetRef = rulesetRef,
                        knowledge = branchKnowledge,
                        profile = candidate.profile,
                        topology = candidate.topology,
                        roleDefinitions = roleDefinitions,
                    ) == TroubleBrewingTopologySetupFeasibility.Feasible
                }
                if (survives) {
                    afterKeys += candidate.topology
                }
            }

            if (unsupported.isNotEmpty()) {
                return ExactStrategicTopologyBundleEvaluation.Deferred(unsupported.toList())
            }

            diagnostics += ExactStrategicTopologyBundleDiagnostics(
                bundleId = query.bundleId,
                recipientSeat = query.recipientSeat,
                beforeStructure = beforeStructure,
                afterStructure = structureFromKeys(playerCount, afterKeys),
            )
        }

        return ExactStrategicTopologyBundleEvaluation.Ready(diagnostics)
    }

    private fun combine(
        first: BundleConstraintBranch,
        second: TroubleBrewingTopologyObservationConstraintBranch,
    ): BundleConstraintBranch? {
        if (
            first.redHerringSeat != null &&
            second.redHerringSeat != null &&
            first.redHerringSeat != second.redHerringSeat
        ) {
            return null
        }
        return BundleConstraintBranch(
            setupFacts = first.setupFacts + second.setupFacts,
            redHerringSeat = first.redHerringSeat ?: second.redHerringSeat,
        )
    }

    private fun structureFromKeys(
        playerCount: Int,
        keys: Set<StrategicWorldKey>,
    ): ExactWorldStructureDiagnostics {
        if (keys.isEmpty()) return ExactWorldStructureDiagnostics.EMPTY

        val canonicalKeys = keys
            .sortedWith(
                compareBy<StrategicWorldKey>(
                    StrategicWorldKey::demonSeat,
                    { key -> key.minionSeats.joinToString(",") },
                ),
            )
            .toCollection(linkedSetOf())
        val evilConfigurations = canonicalKeys
            .map { key -> (setOf(key.demonSeat) + key.minionSeats).toSortedSet().toSet() }
            .distinct()
            .sortedWith(compareBy<Set<Int>>({ it.size }, { it.joinToString(",") }))
            .toCollection(linkedSetOf())
        val evilSets = canonicalKeys.map { key -> setOf(key.demonSeat) + key.minionSeats }
        val goodSets = evilSets.map { evil ->
            (1..playerCount).filterNot(evil::contains).toSet()
        }

        val forcedEvil = evilSets.drop(1).fold(evilSets.first().toMutableSet()) { acc, next ->
            acc.apply { retainAll(next) }
        }.toSortedSet()
        val forcedGood = goodSets.drop(1).fold(goodSets.first().toMutableSet()) { acc, next ->
            acc.apply { retainAll(next) }
        }.toSortedSet()

        return ExactWorldStructureDiagnostics(
            possibleDemonSeats = canonicalKeys.mapTo(sortedSetOf(), StrategicWorldKey::demonSeat),
            evilTeamSeatConfigurations = evilConfigurations,
            strategicWorldKeys = canonicalKeys,
            forcedGoodSeats = forcedGood,
            forcedEvilSeats = forcedEvil,
            evilCoverSeats = evilSets.flatten().toSortedSet(),
        )
    }

    private data class ProfileTopology(
        val profile: InformationProposition.SetupProfile,
        val topology: StrategicWorldKey,
    )

    private data class BundleConstraintBranch(
        val setupFacts: List<InformationProposition> = emptyList(),
        val redHerringSeat: Int? = null,
    )
}
