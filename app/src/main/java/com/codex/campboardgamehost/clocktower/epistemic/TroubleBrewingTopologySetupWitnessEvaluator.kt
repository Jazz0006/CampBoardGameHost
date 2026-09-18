package com.codex.campboardgamehost.clocktower.epistemic

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

        canonicalShown[knowledge.recipientSeat]?.let { shownRole ->
            if (shownRole != knowledge.perceivedRole) {
                return TroubleBrewingTopologySetupFeasibility.Infeasible
            }
        }

        val drunk = catalog.values.singleOrNull { it.id.value.equals("Drunk", ignoreCase = true) }
        val baron = catalog.values.singleOrNull { it.id.value.equals("Baron", ignoreCase = true) }

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
        if (allowedTypes.values.any(Set<CharacterType>::isEmpty)) return false

        branch.fixedRoles.forEach { (seat, roleId) ->
            val definition = catalog[roleId] ?: return false
            if (definition.type !in allowedTypes.getValue(seat)) return false
            if (roleId in branch.forbiddenInPlay) return false
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
                )
            }

            val role = remainingRequired[index]
            if (role.id in usedRoles) return placeRequired(index + 1)

            for (seat in 1..playerCount) {
                if (seat in assignment) continue
                if (role.type !in allowedTypes.getValue(seat)) continue
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

    private fun canFillRemaining(
        assignment: Map<Int, RoleId>,
        usedRoles: Set<RoleId>,
        allowedTypes: Map<Int, Set<CharacterType>>,
        profile: InformationProposition.SetupProfile,
        playerCount: Int,
        catalog: Map<RoleId, RoleDefinition>,
        forbiddenRoles: Set<RoleId>,
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

        for (type in listOf(CharacterType.MINION, CharacterType.DEMON)) {
            val eligibleSeats = unassignedSeats.count { type in allowedTypes.getValue(it) }
            if (eligibleSeats != remainingNeed.getValue(type)) return false
        }

        val goodSeats = unassignedSeats.filter { seat ->
            CharacterType.TOWNSFOLK in allowedTypes.getValue(seat) ||
                CharacterType.OUTSIDER in allowedTypes.getValue(seat)
        }
        val forcedTownsfolk = goodSeats.count {
            allowedTypes.getValue(it) == setOf(CharacterType.TOWNSFOLK)
        }
        val forcedOutsider = goodSeats.count {
            allowedTypes.getValue(it) == setOf(CharacterType.OUTSIDER)
        }
        val townsfolkNeed = remainingNeed.getValue(CharacterType.TOWNSFOLK)
        val outsiderNeed = remainingNeed.getValue(CharacterType.OUTSIDER)
        if (townsfolkNeed < forcedTownsfolk || outsiderNeed < forcedOutsider) return false
        if (townsfolkNeed + outsiderNeed != goodSeats.size) return false

        targetCounts.keys.forEach { type ->
            val availableRoles = catalog.values.count { definition ->
                definition.type == type &&
                    definition.id !in usedRoles &&
                    definition.id !in forbiddenRoles
            }
            if (availableRoles < remainingNeed.getValue(type)) return false
        }

        return true
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
        is InformationProposition.RoleInPlay -> true
        else -> false
    }

    private data class ConstraintBranch(
        val fixedRoles: Map<Int, RoleId> = emptyMap(),
        val requiredTypes: Map<Int, CharacterType> = emptyMap(),
        val requiredAlignments: Map<Int, Alignment> = emptyMap(),
        val requiredInPlay: Set<RoleId> = emptySet(),
        val forbiddenInPlay: Set<RoleId> = emptySet(),
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
