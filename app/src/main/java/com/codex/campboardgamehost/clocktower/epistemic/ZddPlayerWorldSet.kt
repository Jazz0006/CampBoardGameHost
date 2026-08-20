package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import java.math.BigInteger

/**
 * A4 prototype backed by a canonical zero-suppressed decision diagram of exact mechanical worlds.
 * Filtering currently decodes matching paths and rebuilds the diagram; this keeps correctness simple
 * while A4 measures whether a native symbolic observation compiler is worth implementing.
 */
class ZddPlayerWorldSet private constructor(
    override val recipientSeat: Int,
    override val knowledgeSnapshotId: String,
    override val hypothesis: EpistemicHypothesis,
    override val identity: PlayerWorldSetIdentity,
    private val roles: Map<RoleId, RoleDefinition>,
    private val diagram: WorldZdd,
    val lastFilterStrategy: ZddFilterStrategy = ZddFilterStrategy.NONE,
    val lastDecodeRebuildMetrics: ZddDecodeRebuildMetrics? = null,
    private val overlayClusters: Set<WorldExplanationClusterId> = emptySet(),
) : PlayerWorldSet {
    override fun isEmpty(): Boolean = diagram.isEmpty()
    override fun cardinality(): WorldCardinality = WorldCardinality.Exact(diagram.cardinality())

    override fun require(observation: EpistemicObservation): ZddPlayerWorldSet = filtered(observation, true)
    override fun exclude(observation: EpistemicObservation): ZddPlayerWorldSet = filtered(observation, false)

    override fun possibleRoles(seat: Int): Set<RoleId> = worlds().mapNotNullTo(linkedSetOf()) { it.rolesBySeat[seat] }
    override fun possibleDemonSeats(): Set<Int> = possibleSeatsOfType(CharacterType.DEMON)
    override fun possibleMinionSeats(): Set<Int> = possibleSeatsOfType(CharacterType.MINION)

    override fun roleWorldCount(seat: Int, role: RoleId): WorldCardinality = exactCount { it.rolesBySeat[seat] == role }
    override fun demonWorldCount(seat: Int): WorldCardinality = exactCount {
        it.rolesBySeat[seat]?.let(roles::get)?.type == CharacterType.DEMON
    }

    override fun explanationClusters(): ExplanationClusterSummary {
        val counts = linkedMapOf<WorldExplanationClusterId, BigInteger>()
        worlds().forEach { world ->
            world.explanationClusters.forEach { cluster ->
                counts[cluster] = counts.getOrDefault(cluster, BigInteger.ZERO) + BigInteger.ONE
            }
        }
        return ExplanationClusterSummary(counts.mapValues { WorldCardinality.Exact(it.value) })
    }

    /** Prototype instrumentation used by A4 representation and device benchmarks. */
    fun nodeCount(): Int = diagram.nodeCount

    fun boundRegistrationFacts(observation: EpistemicObservation): Set<RegistrationFact> {
        require(observation.visibility == ObservationVisibility.PUBLIC || recipientSeat in observation.recipientSeats) {
            "Observation is not visible to recipient seat $recipientSeat."
        }
        return worlds().flatMap { world ->
            TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
                .takeIf(WorldObservationResult::matches)?.registrationFacts.orEmpty().asSequence()
        }.toCollection(linkedSetOf())
    }

    private fun filtered(observation: EpistemicObservation, retainMatches: Boolean): ZddPlayerWorldSet {
        require(observation.visibility == ObservationVisibility.PUBLIC || recipientSeat in observation.recipientSeats) {
            "Observation is not visible to recipient seat $recipientSeat."
        }
        nativeRestriction(observation, retainMatches)?.let { restricted ->
            return ZddPlayerWorldSet(
                recipientSeat, knowledgeSnapshotId, hypothesis, identity, roles, restricted,
                ZddFilterStrategy.NATIVE_RESTRICTION,
                null,
                overlayClusters + if (retainMatches) setOf(WorldExplanationClusterId("true-info")) else emptySet(),
            )
        }
        val evaluationStarted = System.nanoTime()
        val retained = worlds().mapNotNull { world ->
            val evaluation = TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
            if (evaluation.matches == retainMatches) world.withClusters(evaluation.clusters) else null
        }.toList()
        val evaluationMicros = (System.nanoTime() - evaluationStarted) / NANOS_PER_MICRO
        val rebuildStarted = System.nanoTime()
        val metrics = ZddDecodeRebuildMetrics(
            retainedWorldCount = retained.size,
            evaluationMicros = evaluationMicros,
            rebuildMicros = 0,
        )
        return copyWith(retained, ZddFilterStrategy.DECODE_REBUILD, metrics, rebuildStarted)
    }

    private fun nativeRestriction(observation: EpistemicObservation, retainMatches: Boolean): WorldZdd? {
        if (observation.reliability != ObservationReliability.NOT_ABILITY_INFORMATION) return null
        return when (val proposition = observation.proposition) {
            is InformationProposition.RoleAt -> directSeatRestriction(
                proposition.seat,
                setOf(proposition.role),
                retainMatches,
            )
            is InformationProposition.CharacterTypeAt -> directSeatRestriction(
                proposition.seat,
                roles.filterValues { it.type == proposition.characterType }.keys,
                retainMatches,
            )
            is InformationProposition.AlignmentAt -> directSeatRestriction(
                proposition.seat,
                roles.filterValues { it.alignment == proposition.alignment }.keys,
                retainMatches,
            )
            is InformationProposition.RoleInPlay -> diagram.restrictAny(
                diagram.roleVariables(proposition.role),
                present = if (retainMatches) proposition.inPlay else !proposition.inPlay,
            )
            is InformationProposition.AliveAt -> diagram.restrict(
                WorldVariable.dead(proposition.seat),
                present = if (retainMatches) !proposition.alive else proposition.alive,
            )
            is InformationProposition.AbilityStateAt -> when (proposition.abilityState) {
                AbilityState.FUNCTIONING -> if (retainMatches) {
                    diagram.restrict(WorldVariable.ability(proposition.seat, AbilityState.MALFUNCTIONING_DRUNK), false)
                        .restrict(WorldVariable.ability(proposition.seat, AbilityState.MALFUNCTIONING_POISONED), false)
                } else null
                else -> diagram.restrict(
                    WorldVariable.ability(proposition.seat, proposition.abilityState), retainMatches,
                )
            }
            else -> null
        }
    }

    /**
     * A role/type/alignment predicate is direct only when the current diagram has no local Spy or
     * Recluse role variable. Otherwise their interaction-scoped registration is part of the result.
     */
    private fun directSeatRestriction(seat: Int, acceptedRoles: Set<RoleId>, retainMatches: Boolean): WorldZdd? {
        if (diagram.hasRoleAt(seat, "Spy") || diagram.hasRoleAt(seat, "Recluse")) return null
        return diagram.restrictAny(
            acceptedRoles.mapTo(linkedSetOf()) { role -> WorldVariable.role(seat, role) },
            retainMatches,
        )
    }

    private fun worlds(): Sequence<EnumeratedWorld> = diagram.worlds().map { world ->
        if (overlayClusters.isEmpty()) world else world.withClusters(overlayClusters)
    }

    private fun possibleSeatsOfType(type: CharacterType): Set<Int> = worlds().flatMap { world ->
        world.rolesBySeat.filterValues { roles.getValue(it).type == type }.keys.asSequence()
    }.toCollection(linkedSetOf())

    private fun exactCount(predicate: (EnumeratedWorld) -> Boolean): WorldCardinality.Exact =
        WorldCardinality.Exact(worlds().count(predicate).toBigInteger())

    private fun copyWith(
        worlds: Collection<EnumeratedWorld>,
        strategy: ZddFilterStrategy,
        metrics: ZddDecodeRebuildMetrics? = null,
        rebuildStarted: Long? = null,
    ): ZddPlayerWorldSet = ZddPlayerWorldSet(
        recipientSeat,
        knowledgeSnapshotId,
        hypothesis,
        identity,
        roles,
        WorldZdd.create(worlds),
        strategy,
        metrics?.copy(rebuildMicros = (System.nanoTime() - requireNotNull(rebuildStarted)) / NANOS_PER_MICRO),
    )

    companion object {
        fun fromEnumerated(source: EnumeratedWorldSet): ZddPlayerWorldSet = ZddPlayerWorldSet(
            source.recipientSeat,
            source.knowledgeSnapshotId,
            source.hypothesis,
            source.identity,
            source.roleDefinitions().associateBy(RoleDefinition::id),
            WorldZdd.create(source.enumeratedWorlds()),
        )

        /** Builds the diagram directly from the lazy Trouble Brewing world stream. */
        fun enumerateDirect(
            rulesetRef: com.codex.campboardgamehost.clocktower.domain.RulesetRef,
            knowledge: PlayerKnowledgeSnapshot,
            hypothesis: EpistemicHypothesis,
            roleDefinitions: Collection<RoleDefinition>,
        ): ZddPlayerWorldSet = enumerateDirectMeasured(
            rulesetRef, knowledge, hypothesis, roleDefinitions,
        ).worldSet

        /** Same exact build as [enumerateDirect], with diagnostic phase timings. */
        fun enumerateDirectMeasured(
            rulesetRef: com.codex.campboardgamehost.clocktower.domain.RulesetRef,
            knowledge: PlayerKnowledgeSnapshot,
            hypothesis: EpistemicHypothesis,
            roleDefinitions: Collection<RoleDefinition>,
        ): A4MeasuredZddConstruction {
            val stream = TroubleBrewingWorldEnumerator.stream(rulesetRef, knowledge, roleDefinitions)
            val roles = stream.roleDefinitions.associateBy(RoleDefinition::id)
            val setupFiltered = stream.worlds.filter { world ->
                knowledge.setupKnowledge.all { proposition ->
                    TroubleBrewingWorldObservationEvaluator.evaluateKnownFact(world, roles, proposition)
                }
            }
            val built = WorldZdd.createStreaming(setupFiltered, stream.playerCount, roles.keys)
            var result = ZddPlayerWorldSet(
                knowledge.recipientSeat,
                knowledge.knowledgeSnapshotId,
                hypothesis,
                PlayerWorldSetIdentity.create(rulesetRef, knowledge, hypothesis),
                roles,
                built.diagram,
            )
            (knowledge.publicObservations + knowledge.privateObservations)
                .sortedWith(compareBy<EpistemicObservation>({ it.round }, { it.sequence }, { it.observationId }))
                .forEach { observation -> result = result.require(observation) }
            return A4MeasuredZddConstruction(result, built.metrics)
        }
    }
}

/** Diagnostic-only decomposition of the exact fallback path; no selection consumes these values. */
data class ZddDecodeRebuildMetrics(
    val retainedWorldCount: Int,
    val evaluationMicros: Long,
    val rebuildMicros: Long,
)

data class A4MeasuredZddConstruction(
    val worldSet: ZddPlayerWorldSet,
    val metrics: A4ZddConstructionMetrics,
)

data class A4ZddConstructionMetrics(
    val exactWorldCount: Long,
    val worldGenerationMicros: Long,
    val prefixInsertionMicros: Long,
    val canonicalizationMicros: Long,
)

enum class ZddFilterStrategy { NONE, NATIVE_RESTRICTION, DECODE_REBUILD }

private const val NANOS_PER_MICRO = 1_000L

private class WorldZdd private constructor(
    private val root: Int,
    private val nodes: List<Node>,
    private val variables: List<WorldVariable>,
) {
    data class Node(val variable: Int, val low: Int, val high: Int)
    val nodeCount: Int get() = nodes.size

    fun isEmpty(): Boolean = root == ZERO

    fun cardinality(): BigInteger {
        val memo = mutableMapOf(ZERO to BigInteger.ZERO, ONE to BigInteger.ONE)
        fun count(id: Int): BigInteger = memo.getOrPut(id) {
            val node = nodes[id - FIRST_NODE]
            count(node.low) + count(node.high)
        }
        return count(root)
    }

    fun worlds(): Sequence<EnumeratedWorld> = sequence {
        val atoms = linkedSetOf<Int>()
        suspend fun SequenceScope<EnumeratedWorld>.walk(id: Int) {
            when (id) {
                ZERO -> Unit
                ONE -> yield(WorldAtomCodec.decode(atoms, variables))
                else -> {
                    val node = nodes[id - FIRST_NODE]
                    walk(node.low)
                    atoms += node.variable
                    walk(node.high)
                    atoms -= node.variable
                }
            }
        }
        walk(root)
    }

    fun restrict(variable: WorldVariable, present: Boolean): WorldZdd {
        val target = variables.binarySearch(variable)
        if (target < 0) return if (present) WorldZdd(ZERO, nodes, variables) else this
        val mutableNodes = nodes.toMutableList()
        val unique = mutableNodes.mapIndexed { index, node -> node to index + FIRST_NODE }.toMap().toMutableMap()
        val memo = mutableMapOf<Int, Int>()
        fun makeNode(value: Int, low: Int, high: Int): Int {
            if (high == ZERO) return low
            val node = Node(value, low, high)
            return unique.getOrPut(node) {
                mutableNodes += node
                mutableNodes.lastIndex + FIRST_NODE
            }
        }
        fun walk(id: Int): Int {
            if (id == ZERO || id == ONE) return if (present) ZERO else id
            return memo.getOrPut(id) {
                val node = mutableNodes[id - FIRST_NODE]
                when {
                    node.variable > target -> if (present) ZERO else id
                    node.variable == target -> if (present) makeNode(target, ZERO, node.high) else node.low
                    else -> makeNode(node.variable, walk(node.low), walk(node.high))
                }
            }
        }
        return WorldZdd(walk(root), mutableNodes, variables)
    }

    /** Retains worlds where at least one [candidates] variable is present (or all are absent). */
    fun restrictAny(candidates: Set<WorldVariable>, present: Boolean): WorldZdd {
        if (candidates.isEmpty()) return if (present) WorldZdd(ZERO, nodes, variables) else this
        val targetIds = candidates.mapNotNull { variable ->
            variables.binarySearch(variable).takeIf { it >= 0 }
        }.toSet()
        if (targetIds.isEmpty()) return if (present) WorldZdd(ZERO, nodes, variables) else this
        val mutableNodes = nodes.toMutableList()
        val unique = mutableNodes.mapIndexed { index, node -> node to index + FIRST_NODE }.toMap().toMutableMap()
        val memo = mutableMapOf<Int, Int>()
        fun makeNode(value: Int, low: Int, high: Int): Int {
            if (high == ZERO) return low
            val node = Node(value, low, high)
            return unique.getOrPut(node) { mutableNodes += node; mutableNodes.lastIndex + FIRST_NODE }
        }
        fun walk(id: Int): Int {
            if (id == ZERO || id == ONE) return if (present) ZERO else id
            return memo.getOrPut(id) {
                val node = mutableNodes[id - FIRST_NODE]
                if (node.variable in targetIds) {
                    if (present) makeNode(node.variable, walk(node.low), node.high) else walk(node.low)
                } else makeNode(node.variable, walk(node.low), walk(node.high))
            }
        }
        return WorldZdd(walk(root), mutableNodes, variables)
    }

    fun roleVariables(role: RoleId): Set<WorldVariable> = variables.filterTo(linkedSetOf()) {
        it.kind == WorldVariable.ROLE && it.value == role.value
    }

    /** Whether [variable] occurs in a node reachable from the current root. */
    private fun containsReachable(variable: WorldVariable): Boolean {
        val target = variables.binarySearch(variable)
        if (target < 0) return false
        val visited = mutableSetOf<Int>()
        fun walk(id: Int): Boolean {
            if (id == ZERO || id == ONE || !visited.add(id)) return false
            val node = nodes[id - FIRST_NODE]
            return node.variable == target || walk(node.low) || walk(node.high)
        }
        return walk(root)
    }

    fun hasRoleAt(seat: Int, roleName: String): Boolean =
        containsReachable(WorldVariable.role(seat, RoleId(roleName)))

    companion object {
        private const val ZERO = 0
        private const val ONE = 1
        private const val FIRST_NODE = 2

        fun create(worlds: Collection<EnumeratedWorld>): WorldZdd {
            val encoded = worlds.map(WorldAtomCodec::encode).distinct()
            val variables = encoded.flatten().distinct().sorted()
            val variableIds = variables.withIndex().associate { it.value to it.index }
            val family = encoded.map { atoms -> atoms.mapTo(linkedSetOf()) { variableIds.getValue(it) } }
            val nodes = mutableListOf<Node>()
            val unique = mutableMapOf<Node, Int>()
            fun build(sets: List<Set<Int>>): Int {
                if (sets.isEmpty()) return ZERO
                val nonEmpty = sets.filter(Set<Int>::isNotEmpty)
                if (nonEmpty.isEmpty()) return ONE
                val variable = nonEmpty.minOf { it.min() }
                val low = build(sets.filter { variable !in it })
                val high = build(sets.filter { variable in it }.map { it - variable })
                if (high == ZERO) return low
                val node = Node(variable, low, high)
                return unique.getOrPut(node) {
                    nodes += node
                    nodes.lastIndex + FIRST_NODE
                }
            }
            return WorldZdd(build(family), nodes, variables)
        }

        /** Prefix-structured exact-family construction; the input sequence is never materialized. */
        fun createStreaming(
            worlds: Sequence<EnumeratedWorld>,
            playerCount: Int,
            roleIds: Collection<RoleId>,
        ): StreamingWorldZddBuild {
            val variables = buildList {
                for (seat in 1..playerCount) {
                    roleIds.forEach { role -> add(WorldVariable.role(seat, role)) }
                    add(WorldVariable.redHerring(seat))
                    roleIds.forEach { role -> add(WorldVariable.shown(seat, role)) }
                    add(WorldVariable.dead(seat))
                    AbilityState.entries.forEach { state -> add(WorldVariable.ability(seat, state)) }
                }
                add(WorldVariable.cluster(WorldExplanationClusterId("baron-setup")))
            }.distinct().sorted()
            val variableIds = variables.withIndex().associate { it.value to it.index }
            class PrefixNode {
                var terminal: Boolean = false
                var children: MutableMap<Int, PrefixNode>? = null

                fun child(variable: Int): PrefixNode {
                    val map = children ?: linkedMapOf<Int, PrefixNode>().also { children = it }
                    return map.getOrPut(variable, ::PrefixNode)
                }
            }
            val prefixRoot = PrefixNode()
            var worldGenerationNanos = 0L
            var prefixInsertionNanos = 0L
            var exactWorldCount = 0L
            val iterator = worlds.iterator()
            while (true) {
                var hasNext = false
                worldGenerationNanos += kotlin.system.measureNanoTime { hasNext = iterator.hasNext() }
                if (!hasNext) break
                lateinit var world: EnumeratedWorld
                worldGenerationNanos += kotlin.system.measureNanoTime { world = iterator.next() }
                prefixInsertionNanos += kotlin.system.measureNanoTime {
                    var cursor = prefixRoot
                    WorldAtomCodec.encode(world)
                        .map { atom -> variableIds[atom] ?: error("Missing structural ZDD variable $atom") }
                        .sorted()
                        .forEach { variable -> cursor = cursor.child(variable) }
                    cursor.terminal = true
                }
                exactWorldCount += 1
            }

            val nodes = mutableListOf<Node>()
            val unique = mutableMapOf<Node, Int>()
            fun makeNode(variable: Int, low: Int, high: Int): Int {
                if (high == ZERO) return low
                val node = Node(variable, low, high)
                return unique.getOrPut(node) { nodes += node; nodes.lastIndex + FIRST_NODE }
            }

            fun canonicalize(prefix: PrefixNode): Int {
                var low = if (prefix.terminal) ONE else ZERO
                prefix.children.orEmpty().entries.sortedByDescending(Map.Entry<Int, PrefixNode>::key)
                    .forEach { (variable, child) ->
                        low = makeNode(variable, low, canonicalize(child))
                    }
                return low
            }
            lateinit var diagram: WorldZdd
            val canonicalizationNanos = kotlin.system.measureNanoTime {
                diagram = WorldZdd(canonicalize(prefixRoot), nodes, variables)
            }
            return StreamingWorldZddBuild(
                diagram,
                A4ZddConstructionMetrics(
                    exactWorldCount = exactWorldCount,
                    worldGenerationMicros = worldGenerationNanos / 1_000,
                    prefixInsertionMicros = prefixInsertionNanos / 1_000,
                    canonicalizationMicros = canonicalizationNanos / 1_000,
                ),
            )
        }
    }
}

private data class StreamingWorldZddBuild(
    val diagram: WorldZdd,
    val metrics: A4ZddConstructionMetrics,
)

private object WorldAtomCodec {
    fun encode(world: EnumeratedWorld): Set<WorldVariable> = buildSet {
        world.rolesBySeat.forEach { (seat, role) -> add(WorldVariable.role(seat, role)) }
        world.redHerringSeat?.let { add(WorldVariable.redHerring(it)) }
        world.shownRolesBySeat.forEach { (seat, role) -> add(WorldVariable.shown(seat, role)) }
        world.rolesBySeat.keys.filterNot(world.aliveSeats::contains).forEach { add(WorldVariable.dead(it)) }
        world.abilityStatesBySeat.forEach { (seat, state) -> add(WorldVariable.ability(seat, state)) }
        world.explanationClusters.forEach { add(WorldVariable.cluster(it)) }
    }

    fun decode(atomIds: Set<Int>, variables: List<WorldVariable>): EnumeratedWorld {
        val roles = linkedMapOf<Int, RoleId>()
        var redHerring: Int? = null
        val shown = linkedMapOf<Int, RoleId>()
        val dead = linkedSetOf<Int>()
        val ability = linkedMapOf<Int, AbilityState>()
        val clusters = linkedSetOf<WorldExplanationClusterId>()
        atomIds.map(variables::get).forEach { atom ->
            when (atom.kind) {
                WorldVariable.ROLE -> roles[atom.seat] = RoleId(atom.value)
                WorldVariable.RED_HERRING -> redHerring = atom.seat
                WorldVariable.SHOWN -> shown[atom.seat] = RoleId(atom.value)
                WorldVariable.DEAD -> dead += atom.seat
                WorldVariable.ABILITY -> ability[atom.seat] = AbilityState.valueOf(atom.value)
                WorldVariable.CLUSTER -> clusters += WorldExplanationClusterId(atom.value)
            }
        }
        val canonicalRoles = roles.toSortedMap()
        return EnumeratedWorld(
            rolesBySeat = canonicalRoles,
            redHerringSeat = redHerring,
            shownRolesBySeat = shown.toSortedMap(),
            aliveSeats = canonicalRoles.keys - dead,
            abilityStatesBySeat = ability.toSortedMap(),
            explanationClusters = clusters,
        )
    }
}

private data class WorldVariable(val kind: Int, val seat: Int, val value: String) : Comparable<WorldVariable> {
    override fun compareTo(other: WorldVariable): Int = compareValuesBy(this, other,
        WorldVariable::kind, WorldVariable::seat, WorldVariable::value)

    companion object {
        const val ROLE = 0
        const val RED_HERRING = 1
        const val SHOWN = 2
        const val DEAD = 3
        const val ABILITY = 4
        const val CLUSTER = 5
        fun role(seat: Int, role: RoleId) = WorldVariable(ROLE, seat, role.value)
        fun redHerring(seat: Int) = WorldVariable(RED_HERRING, seat, "")
        fun shown(seat: Int, role: RoleId) = WorldVariable(SHOWN, seat, role.value)
        fun dead(seat: Int) = WorldVariable(DEAD, seat, "")
        fun ability(seat: Int, state: AbilityState) = WorldVariable(ABILITY, seat, state.name)
        fun cluster(cluster: WorldExplanationClusterId) = WorldVariable(CLUSTER, 0, cluster.value)
    }
}
