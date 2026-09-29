package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

internal enum class FirstNightObservationCapability {
    GRIMOIRE_STATE,
}

internal data class FirstNightSetupFactDependencySpec(
    val fact: DeferredFirstNightSetupFact,
    val kind: FirstNightSetupFactDependencyKind,
)

internal data class FirstNightSetupInteractionMetadata(
    val activatesFacts: Set<DeferredFirstNightSetupFact> = emptySet(),
    val commitsFact: DeferredFirstNightSetupFact? = null,
    val fixedDependencies: Set<FirstNightSetupFactDependencySpec> = emptySet(),
) {
    init {
        require(commitsFact == null || commitsFact in activatesFacts) {
            "A first-night setup commit interaction must activate the fact it commits."
        }
    }
}

/**
 * Typed registration boundary for role/system capabilities that participate in DLB-5.
 *
 * Character names may appear here as catalog registration keys, but the dependency planner and
 * ordering algorithm consume only semantic facts/capabilities. Adding another Grimoire viewer does
 * not require changing either algorithm.
 */
internal class FirstNightSetupDependencyRegistry(
    private val roleObservationCapabilities: Map<RoleId, Set<FirstNightObservationCapability>> =
        BUILT_IN_ROLE_OBSERVATION_CAPABILITIES,
    private val roleDependencies: Map<RoleId, Set<FirstNightSetupFactDependencySpec>> =
        BUILT_IN_ROLE_DEPENDENCIES,
    private val interactionMetadata: Map<ClocktowerInteractionId, FirstNightSetupInteractionMetadata> =
        BUILT_IN_INTERACTION_METADATA,
    private val observationDependencies:
        Map<FirstNightObservationCapability, Set<FirstNightSetupFactDependencySpec>> =
        BUILT_IN_OBSERVATION_DEPENDENCIES,
) {
    fun observationCapabilities(roleId: RoleId): Set<FirstNightObservationCapability> =
        roleObservationCapabilities[roleId].orEmpty()

    fun roleDependencies(roleId: RoleId): Set<FirstNightSetupFactDependencySpec> =
        roleDependencies[roleId].orEmpty()

    fun metadata(interactionId: ClocktowerInteractionId): FirstNightSetupInteractionMetadata =
        interactionMetadata[interactionId] ?: FirstNightSetupInteractionMetadata()

    fun observationDependencies(
        capability: FirstNightObservationCapability,
    ): Set<FirstNightSetupFactDependencySpec> = observationDependencies[capability].orEmpty()

    companion object {
        fun builtIn(): FirstNightSetupDependencyRegistry = FirstNightSetupDependencyRegistry()

        private val BUILT_IN_ROLE_OBSERVATION_CAPABILITIES = mapOf(
            RoleId("Spy") to setOf(FirstNightObservationCapability.GRIMOIRE_STATE),
        )

        private val BUILT_IN_ROLE_DEPENDENCIES = mapOf(
            RoleId("Fortune Teller") to setOf(
                FirstNightSetupFactDependencySpec(
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
            ),
        )

        private val BUILT_IN_INTERACTION_METADATA = mapOf(
            ClocktowerInteractionId("first_night:fortune_teller:red_herring") to
                FirstNightSetupInteractionMetadata(
                    activatesFacts = setOf(DeferredFirstNightSetupFact.RED_HERRING),
                    commitsFact = DeferredFirstNightSetupFact.RED_HERRING,
                ),
            ClocktowerInteractionId("first_night:system:demon_info") to
                FirstNightSetupInteractionMetadata(
                    activatesFacts = setOf(DeferredFirstNightSetupFact.DEMON_BLUFFS),
                    fixedDependencies = setOf(
                        FirstNightSetupFactDependencySpec(
                            fact = DeferredFirstNightSetupFact.DEMON_BLUFFS,
                            kind = FirstNightSetupFactDependencyKind.PRESENTATION_REQUIRED,
                        ),
                    ),
                ),
        )

        private val BUILT_IN_OBSERVATION_DEPENDENCIES = mapOf(
            FirstNightObservationCapability.GRIMOIRE_STATE to setOf(
                FirstNightSetupFactDependencySpec(
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED,
                ),
            ),
        )
    }
}

internal data class FirstNightSetupDependencyProjection(
    val activeFacts: Set<DeferredFirstNightSetupFact>,
    val dependencies: List<FirstNightSetupFactDependency>,
    val commitInteractionIdsByFact: Map<DeferredFirstNightSetupFact, ClocktowerInteractionId>,
)

/**
 * Adapts the current effective first-night flow into the pure DLB-5 dependency planner contract.
 *
 * [functioningRoleIds] is supplied by the runtime/rules adapter. A role that normally observes a
 * fact but is currently malfunctioning therefore contributes no observation dependency.
 */
internal object FirstNightSetupDependencyProjector {
    fun project(
        interactions: List<ClocktowerHostInteraction>,
        functioningRoleIds: Set<RoleId>,
        registry: FirstNightSetupDependencyRegistry = FirstNightSetupDependencyRegistry.builtIn(),
    ): FirstNightSetupDependencyProjection {
        require(interactions.all { it.phase == ClocktowerNightFlowPhase.FIRST_NIGHT }) {
            "First-night setup dependency projection cannot consume another-night interactions."
        }
        require(interactions.map(ClocktowerHostInteraction::id).distinct().size == interactions.size) {
            "First-night setup dependency projection requires unique interaction identities."
        }

        val metadataByInteraction = interactions.associate { interaction ->
            interaction.id to registry.metadata(interaction.id)
        }
        val activeFacts = metadataByInteraction.values
            .flatMapTo(linkedSetOf(), FirstNightSetupInteractionMetadata::activatesFacts)

        val commitPairs = interactions.mapNotNull { interaction ->
            registry.metadata(interaction.id).commitsFact?.let { fact -> fact to interaction.id }
        }
        require(commitPairs.map { it.first }.distinct().size == commitPairs.size) {
            "Each deferred first-night setup fact may have at most one commit interaction."
        }
        val commitInteractionIdsByFact = commitPairs.toMap()

        val dependencies = buildList {
            interactions.forEach { interaction ->
                val metadata = metadataByInteraction.getValue(interaction.id)
                metadata.fixedDependencies
                    .filter { spec -> spec.fact in activeFacts }
                    .forEach { spec ->
                        add(
                            FirstNightSetupFactDependency(
                                interactionId = interaction.id,
                                fact = spec.fact,
                                kind = spec.kind,
                            ),
                        )
                    }

                if (interaction.kind != ClocktowerHostInteractionKind.ROLE_PHASE_ACTION) {
                    return@forEach
                }
                val roleId = interaction.roleId ?: return@forEach

                registry.roleDependencies(roleId)
                    .filter { spec -> spec.fact in activeFacts }
                    .forEach { spec ->
                        add(
                            FirstNightSetupFactDependency(
                                interactionId = interaction.id,
                                fact = spec.fact,
                                kind = spec.kind,
                            ),
                        )
                    }

                if (roleId !in functioningRoleIds) return@forEach
                registry.observationCapabilities(roleId)
                    .flatMap(registry::observationDependencies)
                    .filter { spec -> spec.fact in activeFacts }
                    .forEach { spec ->
                        add(
                            FirstNightSetupFactDependency(
                                interactionId = interaction.id,
                                fact = spec.fact,
                                kind = spec.kind,
                            ),
                        )
                    }
            }
        }
            .distinct()
            .sortedWith(
                compareBy<FirstNightSetupFactDependency> { dependency ->
                    interactions.indexOfFirst { it.id == dependency.interactionId }
                }.thenBy { it.fact.ordinal }
                    .thenBy { it.kind.ordinal },
            )

        return FirstNightSetupDependencyProjection(
            activeFacts = activeFacts,
            dependencies = dependencies,
            commitInteractionIdsByFact = commitInteractionIdsByFact,
        )
    }
}

/**
 * Repositions existing setup-commit interactions immediately before the earliest semantic barrier.
 *
 * This owns ordering only. It does not choose a value or mark a fact committed.
 */
internal object FirstNightSetupCommitInteractionOrderer {
    fun order(
        interactions: List<ClocktowerHostInteraction>,
        functioningRoleIds: Set<RoleId>,
        committedFacts: Set<DeferredFirstNightSetupFact> = emptySet(),
        registry: FirstNightSetupDependencyRegistry = FirstNightSetupDependencyRegistry.builtIn(),
    ): List<ClocktowerHostInteraction> {
        val projection = FirstNightSetupDependencyProjector.project(
            interactions = interactions,
            functioningRoleIds = functioningRoleIds,
            registry = registry,
        )
        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = interactions.map(ClocktowerHostInteraction::id),
            dependencies = projection.dependencies,
            committedFacts = committedFacts,
        )
        if (projection.commitInteractionIdsByFact.isEmpty()) return interactions

        val barrierByFact = barriers.associateBy(FirstNightSetupCommitmentBarrier::fact)
        val originalRank = interactions
            .map(ClocktowerHostInteraction::id)
            .withIndex()
            .associate { (index, id) -> id to index }

        val movable = projection.commitInteractionIdsByFact
            .filterKeys { fact -> fact !in committedFacts }
            .map { (fact, commitId) ->
                val barrier = requireNotNull(barrierByFact[fact]) {
                    "Uncommitted first-night setup fact $fact has a commit interaction but no semantic barrier."
                }
                require(commitId != barrier.beforeInteractionId) {
                    "A first-night setup commit interaction cannot also be its own dependency barrier."
                }
                Triple(fact, commitId, barrier.beforeInteractionId)
            }
            .sortedBy { (_, _, barrierId) -> originalRank.getValue(barrierId) }

        if (movable.isEmpty()) return interactions

        val movingIds = movable.mapTo(linkedSetOf()) { it.second }
        val result = interactions.filterNot { it.id in movingIds }.toMutableList()

        movable.forEach { (_, commitId, barrierId) ->
            val commitInteraction = interactions.single { it.id == commitId }
            val barrierIndex = result.indexOfFirst { it.id == barrierId }
            require(barrierIndex >= 0) {
                "First-night setup dependency barrier disappeared while ordering interactions."
            }
            result.add(barrierIndex, commitInteraction)
        }

        require(result.map(ClocktowerHostInteraction::id).toSet() ==
            interactions.map(ClocktowerHostInteraction::id).toSet()) {
            "First-night setup commitment ordering must preserve the exact interaction set."
        }
        return result
    }
}
