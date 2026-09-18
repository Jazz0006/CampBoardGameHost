package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef

/**
 * Exact setup-identity witness result for one strategic topology.
 *
 * Deferred is explicit: D4C2 handles setup identity/type/shown-role existence only. Mechanical
 * variants such as Poisoner state and Red Herring remain later feasibility slices and must never be
 * flattened into an incorrect UNSAT result.
 */
internal sealed interface TroubleBrewingTopologySetupFeasibility {
    object Feasible : TroubleBrewingTopologySetupFeasibility
    object Infeasible : TroubleBrewingTopologySetupFeasibility

    data class Deferred(
        val unsupportedPropositions: List<InformationProposition>,
    ) : TroubleBrewingTopologySetupFeasibility {
        init {
            require(unsupportedPropositions.isNotEmpty())
        }
    }
}

/**
 * Topology-first exact setup witness checker.
 *
 * It never enumerates complete role permutations. Referenced/required role identities are assigned
 * only where necessary; all remaining seats are discharged by exact type-capacity and unused-role
 * capacity checks.
 */
internal object TroubleBrewingTopologySetupWitnessEvaluator {
    fun evaluate(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        roleDefinitions: Collection<RoleDefinition>,
    ): TroubleBrewingTopologySetupFeasibility {
        val playerCount = profile.total
        require(profile in TroubleBrewingSetupProfiles.legalProfiles(playerCount)) {
            "Topology setup witness evaluation requires a legal Trouble Brewing profile."
        }

        if (!topologyHasLegalShape(topology, playerCount, profile.minions)) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        val catalog = roleDefinitions
            .filter { rulesetRef.scriptId in it.scriptIds }
            .associateBy(RoleDefinition::id)
        require(catalog.isNotEmpty()) {
            "Topology setup witness evaluation requires role definitions for the current script."
        }
        val perceived = requireNotNull(catalog[knowledge.perceivedRole]) {
            "Perceived role ${knowledge.perceivedRole.value} is not in the current script."
        }
        require(knowledge.recipientSeat in 1..playerCount) {
            "Knowledge recipient must belong to the evaluated player range."
        }

        val unsupported = knowledge.setupKnowledge.filterNot(::isSupportedSetupProposition)
        if (unsupported.isNotEmpty()) {
            return TroubleBrewingTopologySetupFeasibility.Deferred(unsupported)
        }

        if (knowledge.setupKnowledge.filterIsInstance<InformationProposition.PlayerCount>()
                .any { it.value != playerCount }
        ) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }
        if (knowledge.setupKnowledge.filterIsInstance<InformationProposition.SetupProfile>()
                .any { it != profile }
        ) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        var base = ConstraintBranch()
        for (fact in knowledge.setupKnowledge) {
            base = when (fact) {
                is InformationProposition.RoleAt ->
                    base.withFixedRole(fact.seat, fact.role)
                is InformationProposition.CharacterTypeAt ->
                    base.withRequiredType(fact.seat, fact.characterType)
                is InformationProposition.AlignmentAt ->
                    base.withRequiredAlignment(fact.seat, fact.alignment)
                is InformationProposition.RoleInPlay ->
                    if (fact.inPlay) base.withRequiredInPlay(fact.role)
                    else base.withForbiddenInPlay(fact.role)
                else -> base
            } ?: return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        val shownFacts = knowledge.setupKnowledge
            .filterIsInstance<InformationProposition.ShownRoleAt>()
            .groupBy(InformationProposition.ShownRoleAt::seat)
        if (shownFacts.any { (_, values) -> values.map { it.role }.distinct().size != 1 }) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }
        val canonicalShown = shownFacts.mapValues { (_, values) -> values.first().role }

        val abilityFacts = knowledge.setupKnowledge
            .filterIsInstance<InformationProposition.AbilityStateAt>()
            .groupBy(InformationProposition.AbilityStateAt::seat)
        if (abilityFacts.any { (_, values) -> values.map { it.abilityState }.distinct().size != 1 }) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }
        val requiredAbilityStates = abilityFacts.mapValues { (_, values) -> values.first().abilityState }
        if (requiredAbilityStates.filterValues { it == AbilityState.MALFUNCTIONING_POISONED }.size > 1) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        canonicalShown[knowledge.recipientSeat]?.let { shownRole ->
            if (shownRole != knowledge.perceivedRole) {
                return TroubleBrewingTopologySetupFeasibility.Infeasible
            }
        }

        val drunk = catalog.values.singleOrNull { it.id.value.equals("Drunk", ignoreCase = true) }
        val poisoner = catalog.values.singleOrNull { it.id.value.equals("Poisoner", ignoreCase = true) }
        val baron = catalog.values.singleOrNull { it.id.value.equals("Baron", ignoreCase = true) }

        requiredAbilityStates.forEach { (seat, state) ->
            base = when (state) {
                AbilityState.FUNCTIONING -> {
                    if (drunk == null) base else base.withForbiddenRoleAt(seat, drunk.id)
                }
                AbilityState.MALFUNCTIONING_DRUNK -> {
                    val role = drunk ?: return TroubleBrewingTopologySetupFeasibility.Infeasible
                    base.withFixedRole(seat, role.id)
                }
                AbilityState.MALFUNCTIONING_POISONED -> {
                    val poisonRole = poisoner ?: return TroubleBrewingTopologySetupFeasibility.Infeasible
                    val withoutDrunk = if (drunk == null) base else base.withForbiddenRoleAt(seat, drunk.id)
                    withoutDrunk?.withRequiredInPlay(poisonRole.id)
                }
            } ?: return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        base = if (TroubleBrewingSetupProfiles.isBaronProfile(playerCount, profile)) {
            val role = baron ?: return TroubleBrewingTopologySetupFeasibility.Infeasible
            base.withRequiredInPlay(role.id)
        } else {
            baron?.let { base.withForbiddenInPlay(it.id) } ?: base
        } ?: return TroubleBrewingTopologySetupFeasibility.Infeasible

        var branches = recipientBranches(
            base = base,
            knowledge = knowledge,
            perceived = perceived,
            drunk = drunk,
        )

        canonicalShown
            .filterKeys { it != knowledge.recipientSeat }
            .toSortedMap()
            .forEach { (seat, shownRole) ->
                branches = branches.flatMap { branch ->
                    shownRoleBranches(
                        branch = branch,
                        seat = seat,
                        shownRole = shownRole,
                        catalog = catalog,
                        drunk = drunk,
                    )
                }
                if (branches.isEmpty()) {
                    return TroubleBrewingTopologySetupFeasibility.Infeasible
                }
            }

        branches = poisonerPresenceBranches(
            branches = branches,
            requiredAbilityStates = requiredAbilityStates,
            poisoner = poisoner,
            playerCount = playerCount,
        )
        if (branches.isEmpty()) {
            return TroubleBrewingTopologySetupFeasibility.Infeasible
        }

        val feasible = branches.any { branch ->
            branchHasRoleAssignment(
                branch = branch,
                topology = topology,
                profile = profile,
                playerCount = playerCount,
                catalog = catalog,
            )
        }
        return if (feasible) {
            TroubleBrewingTopologySetupFeasibility.Feasible
        } else {
            TroubleBrewingTopologySetupFeasibility.Infeasible
        }
    }

    private fun recipientBranches(
        base: ConstraintBranch,
        knowledge: PlayerKnowledgeSnapshot,
        perceived: RoleDefinition,
        drunk: RoleDefinition?,
    ): List<ConstraintBranch> = buildList {
        base.withFixedRole(knowledge.recipientSeat, perceived.id)?.let(::add)
        if (perceived.type == CharacterType.TOWNSFOLK && drunk != null) {
            base.withFixedRole(knowledge.recipientSeat, drunk.id)
                ?.withForbiddenInPlay(perceived.id)
                ?.let(::add)
        }
    }.distinct()

    private fun shownRoleBranches(
        branch: ConstraintBranch,
        seat: Int,
        shownRole: RoleId,
        catalog: Map<RoleId, RoleDefinition>,
        drunk: RoleDefinition?,
    ): List<ConstraintBranch> {
        if (seat <= 0) return emptyList()
        val shown = catalog[shownRole] ?: return emptyList()
        return buildList {
            if (!shown.id.value.equals("Drunk", ignoreCase = true)) {
                branch.withFixedRole(seat, shown.id)?.let(::add)
            }
            if (shown.type == CharacterType.TOWNSFOLK && drunk != null) {
                branch.withFixedRole(seat, drunk.id)
                    ?.withForbiddenInPlay(shown.id)
                    ?.let(::add)
            }
        }.distinct()
    }

    private fun poisonerPresenceBranches(
        branches: List<ConstraintBranch>,
        requiredAbilityStates: Map<Int, AbilityState>,
        poisoner: RoleDefinition?,
        playerCount: Int,
    ): List<ConstraintBranch> {
        val poisonedSeats = requiredAbilityStates
            .filterValues { it == AbilityState.MALFUNCTIONING_POISONED }
            .keys
        if (poisonedSeats.isNotEmpty()) {
            val role = poisoner ?: return emptyList()
            return branches.mapNotNull { it.withRequiredInPlay(role.id) }.distinct()
        }

        if (poisoner == null) return branches

        val poisonTargetCanExist = (1..playerCount).any { seat ->
            requiredAbilityStates[seat] != AbilityState.FUNCTIONING
        }
        return branches.flatMap { branch ->
            buildList {
                branch.withForbiddenInPlay(poisoner.id)?.let(::add)
                if (poisonTargetCanExist) {
                    branch.withRequiredInPlay(poisoner.id)?.let(::add)
                }
            }
        }.distinct()
    }

    private fun branchHasRoleAssignment(
        branch: ConstraintBranch,
        topology: StrategicWorldKey,
        profile: InformationProposition.SetupProfile,
        playerCount: Int,
        catalog: Map<RoleId, RoleDefinition>,
    ): Boolean {
        if ((branch.requiredInPlay intersect branch.forbiddenInPlay).isNotEmpty()) return false
        if (branch.fixedRoles.keys.any { it !in 1..playerCount }) return false
        if (branch.requiredTypes.keys.any { it !in 1..playerCount }) return false
        if (branch.requiredAlignments.keys.any { it !in 1..playerCount }) return false
        if (branch.forbiddenRolesBySeat.keys.any { it !in 1..playerCount }) return false
        if (branch.fixedRoles.values.distinct().size != branch.fixedRoles.size) return false

        val allowedTypes = (1..playerCount).associateWith { seat ->
            val topologyTypes = when (seat) {
                topology.demonSeat -> setOf(CharacterType.DEMON)
                in topology.minionSeats -> setOf(CharacterType.MINION)
                else -> setOf(CharacterType.TOWNSFOLK, CharacterType.OUTSIDER)
            }
            val typeConstrained = branch.requiredTypes[seat]?.let { topologyTypes intersect setOf(it) }
                ?: topologyTypes
            branch.requiredAlignments[seat]?.let { alignment ->
                typeConstrained.filterTo(linkedSetOf()) { it.alignment() == alignment }
            } ?: typeConstrained
        }
        if (allowedTypes.values.any { it.isEmpty() }) return false

        branch.fixedRoles.forEach { (seat, roleId) ->
            val definition = catalog[roleId] ?: return false
            if (definition.type !in allowedTypes.getValue(seat)) return false
            if (roleId in branch.forbiddenInPlay) return false
            if (roleId in branch.forbiddenRolesBySeat[seat].orEmpty()) return false
        }

        val requiredDefinitions = branch.requiredInPlay.map { roleId ->
            catalog[roleId] ?: return false
        }
        if (requiredDefinitions.any { it.id in branch.forbiddenInPlay }) return false

        val assignment = branch.fixedRoles.toMutableMap()
        val usedRoles = assignment.values.toMutableSet()
        val remainingRequired = requiredDefinitions
            .filterNot { it.id in usedRoles }
            .sortedBy { definition ->
                (1..playerCount).count { seat ->
                    seat !in assignment && definition.type in allowedTypes.getValue(seat)
                }
            }

        fun placeRequired(index: Int): Boolean {
            if (index == remainingRequired.size) {
                return canFillRemaining(
                    assignment = assignment,
                    usedRoles = usedRoles,
                    allowedTypes = allowedTypes,
                    profile = profile,
                    playerCount = playerCount,
                    catalog = catalog,
                    forbiddenRoles = branch.forbiddenInPlay,
                    forbiddenRolesBySeat = branch.forbiddenRolesBySeat,
                )
            }

            val role = remainingRequired[index]
            if (role.id in usedRoles) return placeRequired(index + 1)

            for (seat in 1..playerCount) {
                if (seat in assignment) continue
                if (role.type !in allowedTypes.getValue(seat)) continue
                if (role.id in branch.forbiddenRolesBySeat[seat].orEmpty()) continue
                assignment[seat] = role.id
                usedRoles += role.id
                if (placeRequired(index + 1)) return true
                usedRoles -= role.id
                assignment.remove(seat)
            }
            return false
        }

        return placeRequired(0)
    }

    /**
     * Proves the remaining exact role assignment as a single quota-constrained flow problem.
     *
     * Source -> CharacterType edges encode the exact remaining setup counts. Type -> role edges
     * preserve role identity/uniqueness. Role -> seat edges apply topology/type eligibility and
     * seat-local forbidden-role constraints. Saturating every seat therefore proves one complete
     * assignment without enumerating Townsfolk/Outsider seat partitions or role permutations.
     */
    private fun canFillRemaining(
        assignment: Map<Int, RoleId>,
        usedRoles: Set<RoleId>,
        allowedTypes: Map<Int, Set<CharacterType>>,
        profile: InformationProposition.SetupProfile,
        playerCount: Int,
        catalog: Map<RoleId, RoleDefinition>,
        forbiddenRoles: Set<RoleId>,
        forbiddenRolesBySeat: Map<Int, Set<RoleId>>,
    ): Boolean {
        val targetCounts = mapOf(
            CharacterType.TOWNSFOLK to profile.townsfolk,
            CharacterType.OUTSIDER to profile.outsiders,
            CharacterType.MINION to profile.minions,
            CharacterType.DEMON to profile.demons,
        )
        val assignedCounts = assignment.values
            .map { catalog.getValue(it).type }
            .groupingBy { it }
            .eachCount()
        val remainingNeed = targetCounts.mapValues { (type, target) ->
            target - (assignedCounts[type] ?: 0)
        }
        if (remainingNeed.values.any { it < 0 }) return false

        val unassignedSeats = (1..playerCount).filterNot(assignment::containsKey)
        if (remainingNeed.values.sum() != unassignedSeats.size) return false
        if (unassignedSeats.isEmpty()) return true

        val availableRoles = catalog.values
            .filter { definition ->
                definition.id !in usedRoles && definition.id !in forbiddenRoles
            }
            .sortedBy { it.id.value }
        if (targetCounts.keys.any { type ->
                availableRoles.count { it.type == type } < remainingNeed.getValue(type)
            }
        ) {
            return false
        }

        val types = listOf(
            CharacterType.TOWNSFOLK,
            CharacterType.OUTSIDER,
            CharacterType.MINION,
            CharacterType.DEMON,
        )
        val source = 0
        val typeStart = 1
        val roleStart = typeStart + types.size
        val seatStart = roleStart + availableRoles.size
        val sink = seatStart + unassignedSeats.size
        val graph = Array(sink + 1) { mutableListOf<FlowEdge>() }

        types.forEachIndexed { index, type ->
            addFlowEdge(
                graph = graph,
                from = source,
                to = typeStart + index,
                capacity = remainingNeed.getValue(type),
            )
        }

        availableRoles.forEachIndexed { roleIndex, definition ->
            val typeIndex = types.indexOf(definition.type)
            if (typeIndex < 0) return false
            val roleNode = roleStart + roleIndex
            addFlowEdge(graph, typeStart + typeIndex, roleNode, 1)

            unassignedSeats.forEachIndexed { seatIndex, seat ->
                if (
                    definition.type in allowedTypes.getValue(seat) &&
                    definition.id !in forbiddenRolesBySeat[seat].orEmpty()
                ) {
                    addFlowEdge(graph, roleNode, seatStart + seatIndex, 1)
                }
            }
        }

        unassignedSeats.indices.forEach { seatIndex ->
            addFlowEdge(graph, seatStart + seatIndex, sink, 1)
        }

        return maxFlow(graph, source, sink) == unassignedSeats.size
    }

    private data class FlowEdge(
        val to: Int,
        var capacity: Int,
        val reverseIndex: Int,
    )

    private fun addFlowEdge(
        graph: Array<MutableList<FlowEdge>>,
        from: Int,
        to: Int,
        capacity: Int,
    ) {
        if (capacity <= 0) return
        val forward = FlowEdge(to = to, capacity = capacity, reverseIndex = graph[to].size)
        val reverse = FlowEdge(to = from, capacity = 0, reverseIndex = graph[from].size)
        graph[from] += forward
        graph[to] += reverse
    }

    private fun maxFlow(
        graph: Array<MutableList<FlowEdge>>,
        source: Int,
        sink: Int,
    ): Int {
        var total = 0
        while (true) {
            val parentNode = IntArray(graph.size) { -1 }
            val parentEdge = IntArray(graph.size) { -1 }
            val queue = ArrayDeque<Int>()
            parentNode[source] = source
            queue.addLast(source)

            while (queue.isNotEmpty() && parentNode[sink] < 0) {
                val node = queue.removeFirst()
                graph[node].forEachIndexed { edgeIndex, edge ->
                    if (edge.capacity > 0 && parentNode[edge.to] < 0) {
                        parentNode[edge.to] = node
                        parentEdge[edge.to] = edgeIndex
                        queue.addLast(edge.to)
                    }
                }
            }
            if (parentNode[sink] < 0) return total

            var augment = Int.MAX_VALUE
            var node = sink
            while (node != source) {
                val from = parentNode[node]
                val edge = graph[from][parentEdge[node]]
                augment = minOf(augment, edge.capacity)
                node = from
            }

            node = sink
            while (node != source) {
                val from = parentNode[node]
                val edge = graph[from][parentEdge[node]]
                edge.capacity -= augment
                graph[node][edge.reverseIndex].capacity += augment
                node = from
            }
            total += augment
        }
    }

    private fun topologyHasLegalShape(
        topology: StrategicWorldKey,
        playerCount: Int,
        minionCount: Int,
    ): Boolean =
        topology.demonSeat in 1..playerCount &&
            topology.minionSeats.size == minionCount &&
            topology.minionSeats.all { it in 1..playerCount } &&
            topology.demonSeat !in topology.minionSeats

    private fun isSupportedSetupProposition(
        proposition: InformationProposition,
    ): Boolean = when (proposition) {
        is InformationProposition.PlayerCount,
        is InformationProposition.SetupProfile,
        is InformationProposition.RoleAt,
        is InformationProposition.ShownRoleAt,
        is InformationProposition.AlignmentAt,
        is InformationProposition.CharacterTypeAt,
        is InformationProposition.AbilityStateAt,
        is InformationProposition.RoleInPlay -> true
        else -> false
    }

    private data class ConstraintBranch(
        val fixedRoles: Map<Int, RoleId> = emptyMap(),
        val requiredTypes: Map<Int, CharacterType> = emptyMap(),
        val requiredAlignments: Map<Int, Alignment> = emptyMap(),
        val requiredInPlay: Set<RoleId> = emptySet(),
        val forbiddenInPlay: Set<RoleId> = emptySet(),
        val forbiddenRolesBySeat: Map<Int, Set<RoleId>> = emptyMap(),
    ) {
        fun withFixedRole(seat: Int, role: RoleId): ConstraintBranch? {
            val existing = fixedRoles[seat]
            if (existing != null && existing != role) return null
            return copy(fixedRoles = fixedRoles + (seat to role))
        }

        fun withRequiredType(seat: Int, type: CharacterType): ConstraintBranch? {
            val existing = requiredTypes[seat]
            if (existing != null && existing != type) return null
            return copy(requiredTypes = requiredTypes + (seat to type))
        }

        fun withRequiredAlignment(seat: Int, alignment: Alignment): ConstraintBranch? {
            val existing = requiredAlignments[seat]
            if (existing != null && existing != alignment) return null
            return copy(requiredAlignments = requiredAlignments + (seat to alignment))
        }

        fun withRequiredInPlay(role: RoleId): ConstraintBranch? {
            if (role in forbiddenInPlay) return null
            return copy(requiredInPlay = requiredInPlay + role)
        }

        fun withForbiddenInPlay(role: RoleId): ConstraintBranch? {
            if (role in requiredInPlay) return null
            return copy(forbiddenInPlay = forbiddenInPlay + role)
        }

        fun withForbiddenRoleAt(seat: Int, role: RoleId): ConstraintBranch? {
            if (fixedRoles[seat] == role) return null
            return copy(
                forbiddenRolesBySeat = forbiddenRolesBySeat +
                    (seat to (forbiddenRolesBySeat[seat].orEmpty() + role)),
            )
        }
    }

    private val InformationProposition.SetupProfile.total: Int
        get() = townsfolk + outsiders + minions + demons

    private fun CharacterType.alignment(): Alignment = when (this) {
        CharacterType.TOWNSFOLK,
        CharacterType.OUTSIDER -> Alignment.GOOD
        CharacterType.MINION,
        CharacterType.DEMON -> Alignment.EVIL
    }
}
