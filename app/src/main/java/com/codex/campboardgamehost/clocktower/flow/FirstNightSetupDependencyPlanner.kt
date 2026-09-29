package com.codex.campboardgamehost.clocktower.flow

internal enum class DeferredFirstNightSetupFact {
    DEMON_BLUFFS,
    RED_HERRING,
}

internal enum class FirstNightSetupFactDependencyKind {
    PRESENTATION_REQUIRED,
    OBSERVATION_REQUIRED,
    RESULT_SEMANTICS_REQUIRED,
}

internal data class FirstNightSetupFactDependency(
    val interactionId: ClocktowerInteractionId,
    val fact: DeferredFirstNightSetupFact,
    val kind: FirstNightSetupFactDependencyKind,
)

internal data class FirstNightSetupCommitmentBarrier(
    val fact: DeferredFirstNightSetupFact,
    val beforeInteractionId: ClocktowerInteractionId,
    val dependencyKinds: Set<FirstNightSetupFactDependencyKind>,
) {
    init {
        require(dependencyKinds.isNotEmpty()) {
            "First-night setup commitment barrier requires at least one dependency kind."
        }
    }
}

/**
 * Pure DLB-5 planner for latest-safe first-night setup commitment.
 *
 * The planner deliberately knows nothing about character names, recommendation policy, Compose,
 * legality generation or session mutation. Callers project the current effective runtime into typed
 * dependencies; the planner only finds the earliest interaction that can observe or semantically
 * depend on each still-uncommitted setup fact.
 */
internal object FirstNightSetupDependencyPlanner {
    fun plan(
        orderedInteractionIds: List<ClocktowerInteractionId>,
        dependencies: List<FirstNightSetupFactDependency>,
        committedFacts: Set<DeferredFirstNightSetupFact> = emptySet(),
    ): List<FirstNightSetupCommitmentBarrier> {
        require(orderedInteractionIds.distinct().size == orderedInteractionIds.size) {
            "First-night dependency planner requires unique ordered interaction identities."
        }

        val rankByInteractionId = orderedInteractionIds
            .withIndex()
            .associate { (index, id) -> id to index }

        dependencies.forEach { dependency ->
            require(dependency.interactionId in rankByInteractionId) {
                "First-night setup dependency references an interaction outside the ordered flow: " +
                    dependency.interactionId.value
            }
        }

        return DeferredFirstNightSetupFact.entries
            .asSequence()
            .filterNot { fact -> fact in committedFacts }
            .mapNotNull { fact ->
                val factDependencies = dependencies.filter { dependency ->
                    dependency.fact == fact
                }
                if (factDependencies.isEmpty()) return@mapNotNull null

                val earliestRank = factDependencies.minOf { dependency ->
                    requireNotNull(rankByInteractionId[dependency.interactionId])
                }
                val earliestInteractionId = orderedInteractionIds[earliestRank]
                val earliestKinds = factDependencies
                    .asSequence()
                    .filter { dependency ->
                        rankByInteractionId.getValue(dependency.interactionId) == earliestRank
                    }
                    .mapTo(linkedSetOf(), FirstNightSetupFactDependency::kind)

                FirstNightSetupCommitmentBarrier(
                    fact = fact,
                    beforeInteractionId = earliestInteractionId,
                    dependencyKinds = earliestKinds,
                )
            }
            .sortedWith(
                compareBy<FirstNightSetupCommitmentBarrier> {
                    rankByInteractionId.getValue(it.beforeInteractionId)
                }.thenBy { it.fact.ordinal },
            )
            .toList()
    }
}
