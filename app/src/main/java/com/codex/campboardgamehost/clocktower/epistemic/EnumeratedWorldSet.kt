package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import java.math.BigInteger

/** One exact mechanical world. Registration is evaluated per observation and is never persisted as identity. */
data class EnumeratedWorld(
    val rolesBySeat: Map<Int, RoleId>,
    val redHerringSeat: Int? = null,
    val shownRolesBySeat: Map<Int, RoleId> = emptyMap(),
    val aliveSeats: Set<Int> = rolesBySeat.keys,
    val abilityStatesBySeat: Map<Int, AbilityState> = emptyMap(),
    val explanationClusters: Set<WorldExplanationClusterId> = emptySet(),
) {
    init {
        require(rolesBySeat.isNotEmpty())
        require(rolesBySeat.keys.all { it > 0 })
        require(rolesBySeat.keys.toList() == rolesBySeat.keys.sorted()) { "World seats must use canonical order." }
        require(rolesBySeat.values.distinct().size == rolesBySeat.size) { "Characters are unique in a legal setup." }
        require(redHerringSeat == null || redHerringSeat in rolesBySeat)
        require(shownRolesBySeat.keys.all { it in rolesBySeat })
        require(aliveSeats.all { it in rolesBySeat })
        require(abilityStatesBySeat.keys.all { it in rolesBySeat })
    }

    fun withClusters(clusters: Set<WorldExplanationClusterId>): EnumeratedWorld =
        copy(explanationClusters = explanationClusters + clusters)
}

/**
 * Identity of the current exact mechanical state. Explanation/provenance is deliberately excluded:
 * multiple hidden paths which reach this same state are one possible world, not several.
 */
private data class EnumeratedWorldMechanicalIdentity(
    val rolesBySeat: Map<Int, RoleId>,
    val redHerringSeat: Int?,
    val shownRolesBySeat: Map<Int, RoleId>,
    val aliveSeats: Set<Int>,
    val abilityStatesBySeat: Map<Int, AbilityState>,
)

internal object EnumeratedWorldMechanicalConvergence {
    fun converge(worlds: Collection<EnumeratedWorld>): List<EnumeratedWorld> {
        val converged = linkedMapOf<EnumeratedWorldMechanicalIdentity, EnumeratedWorld>()
        worlds.forEach { world ->
            val identity = world.mechanicalIdentity()
            val existing = converged[identity]
            converged[identity] = if (existing == null) {
                world
            } else {
                existing.copy(
                    explanationClusters = existing.explanationClusters + world.explanationClusters,
                )
            }
        }
        return converged.values.toList()
    }

    private fun EnumeratedWorld.mechanicalIdentity(): EnumeratedWorldMechanicalIdentity =
        EnumeratedWorldMechanicalIdentity(
            rolesBySeat = rolesBySeat,
            redHerringSeat = redHerringSeat,
            shownRolesBySeat = shownRolesBySeat,
            aliveSeats = aliveSeats,
            abilityStatesBySeat = abilityStatesBySeat,
        )
}

/** Transparent A3 correctness baseline. It deliberately materializes every surviving world. */
class EnumeratedWorldSet private constructor(
    override val recipientSeat: Int,
    override val knowledgeSnapshotId: String,
    override val hypothesis: EpistemicHypothesis,
    override val identity: PlayerWorldSetIdentity,
    private val roles: Map<RoleId, RoleDefinition>,
    private val worlds: List<EnumeratedWorld>,
) : PlayerWorldSet {

    override fun isEmpty(): Boolean = worlds.isEmpty()

    override fun cardinality(): WorldCardinality = WorldCardinality.Exact(worlds.size.toBigInteger())

    override fun require(observation: EpistemicObservation): EnumeratedWorldSet = filtered(observation, retainMatches = true)

    override fun exclude(observation: EpistemicObservation): EnumeratedWorldSet = filtered(observation, retainMatches = false)

    override fun possibleRoles(seat: Int): Set<RoleId> = worlds.mapNotNullTo(linkedSetOf()) { it.rolesBySeat[seat] }

    override fun possibleDemonSeats(): Set<Int> = possibleSeatsOfType(CharacterType.DEMON)

    override fun possibleMinionSeats(): Set<Int> = possibleSeatsOfType(CharacterType.MINION)

    override fun roleWorldCount(seat: Int, role: RoleId): WorldCardinality = exactCount {
        it.rolesBySeat[seat] == role
    }

    override fun demonWorldCount(seat: Int): WorldCardinality = exactCount {
        it.rolesBySeat[seat]?.let(roles::get)?.type == CharacterType.DEMON
    }

    override fun explanationClusters(): ExplanationClusterSummary {
        val counts = linkedMapOf<WorldExplanationClusterId, BigInteger>()
        worlds.forEach { world ->
            world.explanationClusters.forEach { cluster -> counts[cluster] = counts.getOrDefault(cluster, BigInteger.ZERO) + BigInteger.ONE }
        }
        return ExplanationClusterSummary(counts.mapValues { WorldCardinality.Exact(it.value) })
    }

    /** Debug/test view only; production integrations must use [PlayerWorldSet]. */
    internal fun enumeratedWorlds(): List<EnumeratedWorld> = worlds.toList()
    internal fun roleDefinitions(): Collection<RoleDefinition> = roles.values

    /**
     * The interaction-local registration selections that preserve at least one world for [observation].
     * This is deliberately a baseline inspection API; a future candidate model will bind one fact to one
     * selected candidate rather than exposing the union of all surviving alternatives.
     */
    fun boundRegistrationFacts(observation: EpistemicObservation): Set<RegistrationFact> {
        require(observation.visibility == ObservationVisibility.PUBLIC || recipientSeat in observation.recipientSeats) {
            "Observation is not visible to recipient seat $recipientSeat."
        }
        return worlds.flatMapTo(linkedSetOf()) { world ->
            TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
                .takeIf(WorldObservationResult::matches)
                ?.registrationFacts
                .orEmpty()
        }
    }

    private fun filtered(observation: EpistemicObservation, retainMatches: Boolean): EnumeratedWorldSet {
        require(observation.visibility == ObservationVisibility.PUBLIC || recipientSeat in observation.recipientSeats) {
            "Observation is not visible to recipient seat $recipientSeat."
        }
        val filtered = worlds.mapNotNull { world ->
            val result = TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
            if (result.matches == retainMatches) world.withClusters(result.clusters) else null
        }
        return EnumeratedWorldSet(recipientSeat, knowledgeSnapshotId, hypothesis, identity, roles, filtered)
    }

    private fun possibleSeatsOfType(type: CharacterType): Set<Int> = worlds.flatMapTo(linkedSetOf()) { world ->
        world.rolesBySeat.filterValues { roles.getValue(it).type == type }.keys
    }

    private fun exactCount(predicate: (EnumeratedWorld) -> Boolean): WorldCardinality.Exact =
        WorldCardinality.Exact(worlds.count(predicate).toBigInteger())

    companion object {
        fun fromWorlds(
            rulesetRef: RulesetRef,
            knowledge: PlayerKnowledgeSnapshot,
            hypothesis: EpistemicHypothesis,
            roleDefinitions: Collection<RoleDefinition>,
            worlds: Collection<EnumeratedWorld>,
        ): EnumeratedWorldSet {
            val roles = roleDefinitions.associateBy(RoleDefinition::id)
            require(roles.isNotEmpty())
            val perceived = roles[knowledge.perceivedRole]
                ?: throw IllegalArgumentException("Perceived role ${knowledge.perceivedRole.value} is not in the ruleset.")
            require(worlds.all { world -> world.rolesBySeat.values.all(roles::containsKey) })
            require(worlds.all { world ->
                world.redHerringSeat == null ||
                    roles.getValue(world.rolesBySeat.getValue(world.redHerringSeat)).alignment == Alignment.GOOD
            }) { "A Fortune Teller red herring must be an actual good player." }
            val recipientCompatibleWorlds = worlds.filter { world ->
                val actual = world.rolesBySeat[knowledge.recipientSeat]
                actual == knowledge.perceivedRole ||
                    (actual?.value.equals("Drunk", true) &&
                        perceived.type == CharacterType.TOWNSFOLK &&
                        knowledge.perceivedRole !in world.rolesBySeat.values)
            }
            val identity = PlayerWorldSetIdentity.create(rulesetRef, knowledge, hypothesis)
            var result = EnumeratedWorldSet(
                knowledge.recipientSeat,
                knowledge.knowledgeSnapshotId,
                hypothesis,
                identity,
                roles,
                EnumeratedWorldMechanicalConvergence.converge(recipientCompatibleWorlds),
            )
            knowledge.setupKnowledge.forEach { proposition ->
                result = result.filterProposition(proposition)
            }
            knowledge.worldReplayObservationsInTimelineOrder()
                .forEach { observation -> result = result.require(observation) }
            return result
        }
    }

    private fun filterProposition(proposition: InformationProposition): EnumeratedWorldSet {
        val retained = worlds.filter { world ->
            TroubleBrewingWorldObservationEvaluator.evaluateKnownFact(world, roles, proposition)
        }
        return EnumeratedWorldSet(recipientSeat, knowledgeSnapshotId, hypothesis, identity, roles, retained)
    }
}

internal data class WorldObservationResult(
    val matches: Boolean,
    val clusters: Set<WorldExplanationClusterId> = emptySet(),
    /** Registration selections which make this exact world satisfy this interaction. */
    val registrationFacts: Set<RegistrationFact> = emptySet(),
)

internal object TroubleBrewingWorldObservationEvaluator {
    private val TRUE_INFO = WorldExplanationClusterId("true-info")
    private val DRUNK_EXPLANATION = WorldExplanationClusterId("drunk-explanation")
    private val POISONED_EXPLANATION = WorldExplanationClusterId("poisoned-explanation")
    private val SPY_REGISTRATION = WorldExplanationClusterId("spy-registration")
    private val RECLUSE_REGISTRATION = WorldExplanationClusterId("recluse-registration")

    fun evaluate(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation,
        hypothesis: EpistemicHypothesis,
    ): WorldObservationResult {
        val abilityState = observation.sourceSeat?.let { world.abilityStatesBySeat[it] } ?: AbilityState.FUNCTIONING
        val isAbilityInformation = observation.reliability != ObservationReliability.NOT_ABILITY_INFORMATION
        if (observation.reliability == ObservationReliability.KNOWN_MALFUNCTIONING) {
            val malfunction = malfunctionClusters(world, observation)
            return WorldObservationResult(
                hypothesis != EpistemicHypothesis.FUNCTIONING_ONLY && malfunction.isNotEmpty(),
                malfunction,
            )
        }
        if (isAbilityInformation && abilityState != AbilityState.FUNCTIONING) {
            val malfunction = malfunctionClusters(world, observation)
            return WorldObservationResult(
                hypothesis != EpistemicHypothesis.FUNCTIONING_ONLY,
                malfunction,
            )
        }
        val mechanical = evaluateWithRegistration(world, roles, observation)
        if (mechanical.matches) return mechanical.copy(clusters = mechanical.clusters + TRUE_INFO)
        return WorldObservationResult(false)
    }

    fun evaluateKnownFact(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        proposition: InformationProposition,
    ): Boolean = evaluateActual(world, roles, proposition)

    private fun evaluateWithRegistration(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation,
    ): WorldObservationResult = when (val proposition = observation.proposition) {
        is InformationProposition.RoleAt -> registrationMatch(world, roles, observation, proposition.seat, proposition.role, null, null)
        is InformationProposition.AlignmentAt -> registrationMatch(world, roles, observation, proposition.seat, null, null, proposition.alignment)
        is InformationProposition.CharacterTypeAt -> registrationMatch(world, roles, observation, proposition.seat, null, proposition.characterType, null)
        is InformationProposition.AnyOf -> combineAny(proposition.alternatives.map {
            evaluateWithRegistration(world, roles, observation.copy(proposition = it))
        })
        is InformationProposition.AllOf -> combineAll(proposition.propositions.map {
            evaluateWithRegistration(world, roles, observation.copy(proposition = it))
        })
        is InformationProposition.Not -> evaluateWithRegistration(world, roles, observation.copy(proposition = proposition.proposition)).let {
            WorldObservationResult(!it.matches, if (it.matches) emptySet() else it.clusters)
        }
        is InformationProposition.NumericResult -> evaluateNumeric(world, roles, observation, proposition)
        is InformationProposition.BooleanResult -> evaluateBoolean(world, roles, observation, proposition)
        else -> WorldObservationResult(evaluateActual(world, roles, proposition))
    }

    private fun evaluateActual(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        proposition: InformationProposition,
    ): Boolean = when (proposition) {
        is InformationProposition.RoleAt -> world.rolesBySeat[proposition.seat] == proposition.role
        is InformationProposition.AlignmentAt -> world.rolesBySeat[proposition.seat]?.let(roles::get)?.alignment == proposition.alignment
        is InformationProposition.CharacterTypeAt -> world.rolesBySeat[proposition.seat]?.let(roles::get)?.type == proposition.characterType
        is InformationProposition.AliveAt -> (proposition.seat in world.aliveSeats) == proposition.alive
        is InformationProposition.AbilityStateAt ->
            (world.abilityStatesBySeat[proposition.seat] ?: AbilityState.FUNCTIONING) == proposition.abilityState
        is InformationProposition.RoleInPlay -> (proposition.role in world.rolesBySeat.values) == proposition.inPlay
        is InformationProposition.PlayerCount -> world.rolesBySeat.size == proposition.value
        is InformationProposition.SetupProfile -> profile(world, roles) == proposition
        is InformationProposition.AnyOf -> proposition.alternatives.any { evaluateActual(world, roles, it) }
        is InformationProposition.AllOf -> proposition.propositions.all { evaluateActual(world, roles, it) }
        is InformationProposition.Not -> !evaluateActual(world, roles, proposition.proposition)
        is InformationProposition.NumericResult -> evaluateNumeric(world, roles, null, proposition).matches
        is InformationProposition.BooleanResult -> evaluateBoolean(world, roles, null, proposition).matches
        is InformationProposition.GrimoireState -> when (proposition.truthBinding) {
            GrimoireTruthBinding.LEGACY_DISPLAY_ONLY -> legacyGrimoireMatches(world, proposition)
            GrimoireTruthBinding.VERIFIED_EXACT -> grimoireMatches(world, roles, proposition)
        }
    }

    private fun legacyGrimoireMatches(
        world: EnumeratedWorld,
        grimoire: InformationProposition.GrimoireState,
    ): Boolean = grimoire.seats.all { view ->
        (world.shownRolesBySeat[view.seat] ?: world.rolesBySeat[view.seat]) == view.displayedRole &&
            (view.seat in world.aliveSeats) == view.alive
    }

    private fun grimoireMatches(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        grimoire: InformationProposition.GrimoireState,
    ): Boolean {
        val observedSeats = grimoire.seats.mapTo(linkedSetOf()) { it.seat }
        if (observedSeats != world.rolesBySeat.keys) return false

        val expectedConstraints = expectedMechanicalGrimoireConstraints(world)
        return grimoire.seats.all { view ->
            val observedConstraints = view.reminderTokens.mapNotNullTo(linkedSetOf()) {
                TroubleBrewingGrimoireReminderSemantics.worldConstraint(it)
            }
            grimoireDisplayedRoleMatches(world, roles, view, observedConstraints) &&
                (view.seat in world.aliveSeats) == view.alive &&
                observedConstraints == expectedConstraints[view.seat].orEmpty()
        }
    }

    private fun grimoireDisplayedRoleMatches(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        view: GrimoireSeatView,
        observedConstraints: Set<GrimoireReminderWorldConstraint>,
    ): Boolean {
        val actualRole = world.rolesBySeat[view.seat] ?: return false
        if (!actualRole.value.equals("Drunk", ignoreCase = true)) {
            return actualRole == view.displayedRole
        }
        if (GrimoireReminderWorldConstraint.DRUNK_IDENTITY !in observedConstraints) return false
        val displayed = roles[view.displayedRole] ?: return false
        return displayed.type == CharacterType.TOWNSFOLK && view.displayedRole !in world.rolesBySeat.values
    }

    private fun expectedMechanicalGrimoireConstraints(
        world: EnumeratedWorld,
    ): Map<Int, Set<GrimoireReminderWorldConstraint>> {
        val result = linkedMapOf<Int, MutableSet<GrimoireReminderWorldConstraint>>()
        fun add(seat: Int, constraint: GrimoireReminderWorldConstraint) {
            result.getOrPut(seat, ::linkedSetOf) += constraint
        }

        world.redHerringSeat?.let { add(it, GrimoireReminderWorldConstraint.RED_HERRING) }
        world.rolesBySeat.entries.singleOrNull { it.value.value.equals("Drunk", ignoreCase = true) }?.key?.let {
            add(it, GrimoireReminderWorldConstraint.DRUNK_IDENTITY)
        }
        currentPoisonerTargetSeat(world)?.let {
            add(it, GrimoireReminderWorldConstraint.POISONER_TARGET)
        }
        return result.mapValues { (_, constraints) -> constraints.toSet() }
    }

    /**
     * Trouble Brewing has one mandatory Poisoner target each night while the Poisoner is alive. The
     * current setup world model stores ordinary targets as MALFUNCTIONING_POISONED. If the target is
     * already the Drunk, its single AbilityState remains MALFUNCTIONING_DRUNK, so the absence of
     * another poisoned seat plus an alive, in-play Poisoner and an in-play Drunk identifies that
     * collapsed target without treating the printed token label as rule authority.
     */
    private fun currentPoisonerTargetSeat(world: EnumeratedWorld): Int? {
        val poisonerSeat = world.rolesBySeat.entries.singleOrNull {
            it.value.value.equals("Poisoner", ignoreCase = true)
        }?.key ?: return null
        if (poisonerSeat !in world.aliveSeats) return null
        val poisonedSeats = world.abilityStatesBySeat.filterValues {
            it == AbilityState.MALFUNCTIONING_POISONED
        }.keys
        if (poisonedSeats.size == 1) return poisonedSeats.single()
        if (poisonedSeats.isNotEmpty()) return null
        return world.rolesBySeat.entries.singleOrNull { it.value.value.equals("Drunk", ignoreCase = true) }?.key
    }

    private fun profile(world: EnumeratedWorld, roles: Map<RoleId, RoleDefinition>): InformationProposition.SetupProfile {
        val counts = world.rolesBySeat.values.groupingBy { roles.getValue(it).type }.eachCount()
        return InformationProposition.SetupProfile(
            counts[CharacterType.TOWNSFOLK] ?: 0,
            counts[CharacterType.OUTSIDER] ?: 0,
            counts[CharacterType.MINION] ?: 0,
            counts[CharacterType.DEMON] ?: 0,
        )
    }

    private fun registrationMatch(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation,
        seat: Int,
        queriedRole: RoleId?,
        queriedType: CharacterType?,
        queriedAlignment: Alignment?,
    ): WorldObservationResult {
        val actualRole = world.rolesBySeat[seat] ?: return WorldObservationResult(false)
        val actual = roles.getValue(actualRole)
        if ((queriedRole == null || actualRole == queriedRole) &&
            (queriedType == null || actual.type == queriedType) &&
            (queriedAlignment == null || actual.alignment == queriedAlignment)
        ) return WorldObservationResult(true)

        // Spy/Recluse registration is an ability of the queried character, not of the
        // information source. Poisoning that subject removes the optional registration branch.
        if ((world.abilityStatesBySeat[seat] ?: AbilityState.FUNCTIONING) != AbilityState.FUNCTIONING) {
            return WorldObservationResult(false)
        }

        return when (actualRole.value.lowercase()) {
            "spy" -> WorldObservationResult(
                (queriedAlignment == null || queriedAlignment == Alignment.GOOD) &&
                    (queriedType == null || queriedType == CharacterType.TOWNSFOLK || queriedType == CharacterType.OUTSIDER) &&
                    (queriedRole == null || roles[queriedRole]?.alignment == Alignment.GOOD),
                setOf(SPY_REGISTRATION),
                registrationFacts = setOf(registrationFact(
                    observation, seat, queriedRole, queriedType, queriedAlignment ?: Alignment.GOOD,
                    RegistrationReason.SPY_ABILITY,
                )),
            )
            "recluse" -> WorldObservationResult(
                (queriedAlignment == null || queriedAlignment == Alignment.EVIL) &&
                    (queriedType == null || queriedType == CharacterType.MINION || queriedType == CharacterType.DEMON) &&
                    (queriedRole == null || roles[queriedRole]?.alignment == Alignment.EVIL),
                setOf(RECLUSE_REGISTRATION),
                registrationFacts = setOf(registrationFact(
                    observation, seat, queriedRole, queriedType, queriedAlignment ?: Alignment.EVIL,
                    RegistrationReason.RECLUSE_ABILITY,
                )),
            )
            else -> WorldObservationResult(false)
        }
    }

    private fun registrationFact(
        observation: EpistemicObservation,
        subjectSeat: Int,
        role: RoleId?,
        type: CharacterType?,
        alignment: Alignment,
        reason: RegistrationReason,
    ): RegistrationFact = RegistrationFact(
        interactionId = observation.observationId,
        subjectSeat = subjectSeat,
        registeredRole = role,
        registeredType = type,
        registeredAlignment = alignment,
        registrationQuestion = when {
            role != null -> RegistrationQuestion.ROLE
            type == CharacterType.DEMON -> RegistrationQuestion.DEMON
            type != null -> RegistrationQuestion.CHARACTER_TYPE
            else -> RegistrationQuestion.ALIGNMENT
        },
        reason = reason,
    )

    private fun evaluateNumeric(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation?,
        value: InformationProposition.NumericResult,
    ): WorldObservationResult {
        val seats = if (value.subjectSeats.isEmpty()) world.rolesBySeat.keys.toList() else value.subjectSeats
        return when (value.metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> {
                val matches = alignmentAssignments(seats.map { alignmentOptions(world, roles, observation, it) })
                    .filter { assignment ->
                        assignment.indices.count { index ->
                            assignment[index].alignment == Alignment.EVIL &&
                                assignment[(index + 1) % assignment.size].alignment == Alignment.EVIL
                        } == value.value
                    }.toList()
                numericResult(matches)
            }
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> {
                val matches = alignmentAssignments(seats.map { alignmentOptions(world, roles, observation, it) })
                    .filter { assignment -> assignment.count { it.alignment == Alignment.EVIL } == value.value }
                    .toList()
                numericResult(matches)
            }
            else -> WorldObservationResult(false)
        }
    }

    private fun evaluateBoolean(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation?,
        value: InformationProposition.BooleanResult,
    ): WorldObservationResult = when (value.metric) {
        BooleanMetric.DEMON_OR_RED_HERRING_PRESENT -> {
            val mandatoryDetection = value.subjectSeats.any { seat ->
                world.rolesBySeat[seat]?.let(roles::get)?.type == CharacterType.DEMON || world.redHerringSeat == seat
            }
            val optionalRegistrations = value.subjectSeats.map { seat ->
                observation?.let { registrationMatch(world, roles, it, seat, null, CharacterType.DEMON, Alignment.EVIL) }
            }.filterNotNull().filter { it.matches && it.registrationFacts.isNotEmpty() }
            val matches = if (value.value) mandatoryDetection || optionalRegistrations.isNotEmpty() else !mandatoryDetection
            val registrationRequired = value.value && !mandatoryDetection
            WorldObservationResult(
                matches,
                if (registrationRequired) optionalRegistrations.flatMapTo(linkedSetOf()) { it.clusters } else emptySet(),
                if (registrationRequired) optionalRegistrations.flatMapTo(linkedSetOf()) { it.registrationFacts } else emptySet(),
            )
        }
    }

    private data class AlignmentOption(
        val alignment: Alignment,
        val cluster: WorldExplanationClusterId? = null,
        val registrationFact: RegistrationFact? = null,
    )

    private fun alignmentOptions(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation?,
        seat: Int,
    ): List<AlignmentOption> {
        val role = world.rolesBySeat.getValue(seat)
        val actual = roles.getValue(role).alignment
        val specialRegistrationAvailable =
            (world.abilityStatesBySeat[seat] ?: AbilityState.FUNCTIONING) == AbilityState.FUNCTIONING
        if (!specialRegistrationAvailable) return listOf(AlignmentOption(actual))

        return when (role.value.lowercase()) {
            "spy" -> listOf(
                AlignmentOption(actual),
                AlignmentOption(Alignment.GOOD, SPY_REGISTRATION, observation?.let {
                    registrationFact(it, seat, null, null, Alignment.GOOD, RegistrationReason.SPY_ABILITY)
                }),
            )
            "recluse" -> listOf(
                AlignmentOption(actual),
                AlignmentOption(Alignment.EVIL, RECLUSE_REGISTRATION, observation?.let {
                    registrationFact(it, seat, null, null, Alignment.EVIL, RegistrationReason.RECLUSE_ABILITY)
                }),
            )
            else -> listOf(AlignmentOption(actual))
        }
    }

    private fun alignmentAssignments(options: List<List<AlignmentOption>>): Sequence<List<AlignmentOption>> = sequence {
        val current = ArrayList<AlignmentOption>(options.size)
        suspend fun SequenceScope<List<AlignmentOption>>.walk(index: Int) {
            if (index == options.size) yield(current.toList())
            else for (alignment in options[index]) {
                current += alignment
                walk(index + 1)
                current.removeAt(current.lastIndex)
            }
        }
        walk(0)
    }

    private fun numericResult(matches: List<List<AlignmentOption>>): WorldObservationResult = WorldObservationResult(
        matches = matches.isNotEmpty(),
        clusters = matches.flatten().mapNotNullTo(linkedSetOf(), AlignmentOption::cluster),
        registrationFacts = matches.flatten().mapNotNullTo(linkedSetOf(), AlignmentOption::registrationFact),
    )

    private fun malfunctionClusters(
        world: EnumeratedWorld,
        observation: EpistemicObservation,
    ): Set<WorldExplanationClusterId> {
        val sourceSeat = observation.sourceSeat ?: return emptySet()
        return when (world.abilityStatesBySeat[sourceSeat] ?: AbilityState.FUNCTIONING) {
            AbilityState.FUNCTIONING -> emptySet()
            AbilityState.MALFUNCTIONING_DRUNK -> setOf(DRUNK_EXPLANATION)
            AbilityState.MALFUNCTIONING_POISONED -> setOf(POISONED_EXPLANATION)
        }
    }

    private fun combineAny(results: List<WorldObservationResult>): WorldObservationResult {
        val matches = results.filter(WorldObservationResult::matches)
        return WorldObservationResult(
            matches.isNotEmpty(),
            matches.flatMapTo(linkedSetOf()) { it.clusters },
            matches.flatMapTo(linkedSetOf()) { it.registrationFacts },
        )
    }

    private fun combineAll(results: List<WorldObservationResult>): WorldObservationResult =
        WorldObservationResult(
            results.all(WorldObservationResult::matches),
            results.flatMapTo(linkedSetOf()) { it.clusters },
            results.flatMapTo(linkedSetOf()) { it.registrationFacts },
        )
}
