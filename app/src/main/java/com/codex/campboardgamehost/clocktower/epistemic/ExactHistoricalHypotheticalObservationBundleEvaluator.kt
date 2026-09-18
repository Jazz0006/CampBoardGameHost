package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger

/**
 * Knowledge-safe historical input for exact hypothetical bundle evaluation.
 *
 * [observationLog] contains only the currently durable recipient-visible history. Hypothetical
 * observations are supplied separately, so the BEFORE baseline never contains a candidate that is
 * then applied again during evaluation.
 */
internal data class ExactHistoricalHypotheticalContext(
    val initialSnapshot: GameSnapshot,
    val initialPhase: StorytellerPhase,
    val initialRound: Int,
    val actionTimeline: ActionFactTimeline,
    val perceivedRolesBySeat: Map<Int, RoleId>,
    val observationLog: EpistemicObservationLog,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
) {
    init {
        require(initialRound > 0) { "Historical hypothetical evaluation requires a positive initial round." }
        actionTimeline.requireCompatibleWith(observationLog)
    }
}

/**
 * Optional exact binding for one Storyteller-selected registration witness.
 *
 * The binding is scoped by [observationId], not by the generator-local RegistrationFact
 * interaction ID. An empty [registrations] set explicitly selects a natural/no-special witness.
 * Omitting a binding from the query preserves the legacy existential "any legal witness" semantics.
 */
internal data class ExactRegistrationWitnessBinding(
    val observationId: String,
    val registrations: Set<RegistrationFact>,
) {
    init {
        require(observationId.isNotBlank()) { "Exact registration witness observation ID cannot be blank." }
    }
}

internal data class ExactHypotheticalObservationBundleQuery(
    val bundleId: String,
    val recipientSeat: Int,
    val observations: List<EpistemicObservation>,
    val registrationWitnessBindings: List<ExactRegistrationWitnessBinding> = emptyList(),
) {
    init {
        require(bundleId.isNotBlank()) { "Hypothetical observation bundle ID cannot be blank." }
        require(recipientSeat > 0) { "Hypothetical observation bundle recipient seat must be positive." }
        require(
            registrationWitnessBindings.map(ExactRegistrationWitnessBinding::observationId).distinct().size ==
                registrationWitnessBindings.size,
        ) {
            "A hypothetical observation may have at most one selected registration witness binding."
        }
        val observationIds = observations.map(EpistemicObservation::observationId).toSet()
        require(registrationWitnessBindings.all { it.observationId in observationIds }) {
            "Every selected registration witness must bind an observation in the same hypothetical bundle."
        }
    }

    fun registrationWitnessBindingFor(
        observation: EpistemicObservation,
    ): ExactRegistrationWitnessBinding? =
        registrationWitnessBindings.singleOrNull { it.observationId == observation.observationId }
}

/** Descriptive strategic structure only. Badness policy does not belong in the exact evaluator. */
internal data class ExactWorldStructureDiagnostics(
    val possibleDemonSeats: Set<Int>,
    val evilTeamSeatConfigurations: Set<Set<Int>>,
    val forcedGoodSeats: Set<Int>,
    val forcedEvilSeats: Set<Int>,
    val evilCoverSeats: Set<Int>,
) {
    val demonCoverSize: Int get() = possibleDemonSeats.size
    val evilCoverSize: Int get() = evilCoverSeats.size
    val distinctEvilTeamConfigurationCount: Int get() = evilTeamSeatConfigurations.size

    companion object {
        val EMPTY = ExactWorldStructureDiagnostics(
            possibleDemonSeats = emptySet(),
            evilTeamSeatConfigurations = emptySet(),
            forcedGoodSeats = emptySet(),
            forcedEvilSeats = emptySet(),
            evilCoverSeats = emptySet(),
        )
    }
}

internal data class ExactHypotheticalObservationBundleDiagnostics(
    val bundleId: String,
    val recipientSeat: Int,
    val before: WorldCardinality.Exact,
    val after: WorldCardinality.Exact,
    val beforeStructure: ExactWorldStructureDiagnostics,
    val afterStructure: ExactWorldStructureDiagnostics,
)

internal sealed interface ExactHypotheticalObservationBundleEvaluation {
    data class Ready(
        val diagnostics: List<ExactHypotheticalObservationBundleDiagnostics>,
    ) : ExactHypotheticalObservationBundleEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : ExactHypotheticalObservationBundleEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred hypothetical bundle evaluation must identify missing epistemic capabilities."
            }
        }
    }
}

/**
 * Exact, mutation-free historical evaluator for composed hypothetical observations.
 *
 * A pristine first-night experiment uses constant-memory source enumeration. BEFORE is scanned once
 * per recipient. Queries are grouped by cheap necessary identity envelopes and evaluated in
 * streaming passes without retaining world lists. The envelope supports both strict shown-role facts
 * and the current healthy public-claim form: an evil speaker may lie, while a truthful-good branch
 * requires the claimed shown role. The complete observations are still evaluated afterwards, so the
 * prefilter cannot decide semantics.
 *
 * Historical replay keeps the existing enumerated historical baseline. No production A4/ZDD rollout
 * decision is changed by this experiment evaluator.
 */
internal object ExactHistoricalHypotheticalObservationBundleEvaluator {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        queries: List<ExactHypotheticalObservationBundleQuery>,
    ): ExactHypotheticalObservationBundleEvaluation {
        when (
            val availability = EpistemicEvaluationCapabilityBoundary.assess(
                validatedRuleset = validatedRuleset,
                requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            )
        ) {
            EpistemicEvaluationAvailability.Ready -> Unit
            is EpistemicEvaluationAvailability.Deferred ->
                return ExactHypotheticalObservationBundleEvaluation.Deferred(
                    missingCapabilities = availability.missingCapabilities,
                )
        }

        val snapshot = context.initialSnapshot
        val formal = FormalGameState.from(snapshot, context.initialPhase, context.initialRound)
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = context.perceivedRolesBySeat.toMap(),
            observationLog = context.observationLog,
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val roles = context.roleDefinitions.toList()
        val rolesById = roles.associateBy(RoleDefinition::id)
        require(rolesById.isNotEmpty()) { "Exact hypothetical bundle evaluation requires role definitions." }

        val stableQueries = queries.map { query -> query.copy(observations = query.observations.toList()) }
        validateQueries(stableQueries, knowledgeBySeat, formal.snapshotId)

        val pristineInitialNight =
            context.initialPhase == StorytellerPhase.FIRST_NIGHT &&
                context.initialRound == 1 &&
                context.actionTimeline.entries.isEmpty() &&
                context.observationLog.records.isEmpty()

        return if (pristineInitialNight) {
            evaluatePristineInitialNight(
                snapshot = snapshot,
                knowledgeBySeat = knowledgeBySeat,
                roles = roles,
                rolesById = rolesById,
                hypothesis = context.hypothesis,
                queries = stableQueries,
            )
        } else {
            evaluateHistorical(
                validatedRuleset = validatedRuleset,
                snapshot = snapshot,
                knowledgeBySeat = knowledgeBySeat,
                roles = roles,
                rolesById = rolesById,
                context = context,
                queries = stableQueries,
            )
        }
    }

    private fun evaluatePristineInitialNight(
        snapshot: GameSnapshot,
        knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot>,
        roles: List<RoleDefinition>,
        rolesById: Map<RoleId, RoleDefinition>,
        hypothesis: EpistemicHypothesis,
        queries: List<ExactHypotheticalObservationBundleQuery>,
    ): ExactHypotheticalObservationBundleEvaluation.Ready {
        val diagnostics = mutableListOf<ExactHypotheticalObservationBundleDiagnostics>()

        queries.groupBy(ExactHypotheticalObservationBundleQuery::recipientSeat)
            .toSortedMap()
            .forEach { (recipientSeat, recipientQueries) ->
                val knowledge = knowledgeBySeat.getValue(recipientSeat)
                val worldSequence: () -> Sequence<EnumeratedWorld> = {
                    exactInitialNightWorldSequence(
                        snapshot = snapshot,
                        knowledge = knowledge,
                        roles = roles,
                        rolesById = rolesById,
                    )
                }
                val beforeScan = scanWorlds(worldSequence(), rolesById)

                recipientQueries
                    .groupBy(::pristinePrefilterKey)
                    .toSortedMap(compareBy(PristinePrefilterKey::stableId))
                    .forEach { (prefilterKey, groupedQueries) ->
                        val representative = groupedQueries.first()
                        require(pristinePrefilterKey(representative) == prefilterKey) {
                            "Pristine first-night prefilter grouping drifted from query semantics."
                        }

                        val strictShownObservations = representative.observations
                            .filter(::isStrictShownRoleClaim)
                            .distinctBy { observation -> observation.proposition }
                        val remainingByQuery = groupedQueries.map { query ->
                            query.observations.filterNot { observation ->
                                isStrictShownRoleClaim(observation) &&
                                    query.registrationWitnessBindingFor(observation) == null
                            }
                        }
                        val afterCounts = LongArray(groupedQueries.size)
                        val afterStructureAccumulators = List(groupedQueries.size) {
                            WorldStructureAccumulator(rolesById)
                        }

                        worldSequence().forEach { world ->
                            val passesIdentityEnvelope =
                                strictShownObservations.all { observation ->
                                    TroubleBrewingWorldObservationEvaluator.evaluate(
                                        world = world,
                                        roles = rolesById,
                                        observation = observation,
                                        hypothesis = hypothesis,
                                    ).matches
                                } &&
                                    prefilterKey.publicClaimShownRoles.all { claim ->
                                        publicClaimIdentityEnvelopeMatches(world, rolesById, claim)
                                    }
                            if (passesIdentityEnvelope) {
                                groupedQueries.indices.forEach { queryIndex ->
                                    val query = groupedQueries[queryIndex]
                                    val matchesQuery = remainingByQuery[queryIndex].all { observation ->
                                        matchesObservation(
                                            query = query,
                                            world = world,
                                            roles = rolesById,
                                            observation = observation,
                                            hypothesis = hypothesis,
                                        )
                                    }
                                    if (matchesQuery) {
                                        afterCounts[queryIndex] += 1L
                                        afterStructureAccumulators[queryIndex].accept(world)
                                    }
                                }
                            }
                        }

                        groupedQueries.forEachIndexed { queryIndex, query ->
                            diagnostics += ExactHypotheticalObservationBundleDiagnostics(
                                bundleId = query.bundleId,
                                recipientSeat = query.recipientSeat,
                                before = beforeScan.cardinality,
                                after = exactCardinality(afterCounts[queryIndex]),
                                beforeStructure = beforeScan.structure,
                                afterStructure = afterStructureAccumulators[queryIndex].finish(),
                            )
                        }
                    }
            }

        val byId = diagnostics.associateBy { it.bundleId to it.recipientSeat }
        return ExactHypotheticalObservationBundleEvaluation.Ready(
            diagnostics = queries.map { query -> byId.getValue(query.bundleId to query.recipientSeat) },
        )
    }

    private fun evaluateHistorical(
        validatedRuleset: ValidatedClocktowerRuleset,
        snapshot: GameSnapshot,
        knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot>,
        roles: List<RoleDefinition>,
        rolesById: Map<RoleId, RoleDefinition>,
        context: ExactHistoricalHypotheticalContext,
        queries: List<ExactHypotheticalObservationBundleQuery>,
    ): ExactHypotheticalObservationBundleEvaluation.Ready {
        val baselineBySeat = queries.map { it.recipientSeat }.distinct().associateWith { recipientSeat ->
            EnumeratedHistoricalExactBaseline.build(
                validatedRuleset = validatedRuleset,
                rulesetRef = snapshot.rulesetRef,
                setupKnowledge = knowledgeBySeat.getValue(recipientSeat),
                hypothesis = context.hypothesis,
                roleDefinitions = roles,
                initialPhase = context.initialPhase,
                initialRound = context.initialRound,
                actionTimeline = context.actionTimeline,
                observationLog = context.observationLog,
            ).worldSet.enumeratedWorlds()
        }
        val beforeBySeat = baselineBySeat.mapValues { (_, worlds) -> scanWorlds(worlds.asSequence(), rolesById) }

        return ExactHypotheticalObservationBundleEvaluation.Ready(
            diagnostics = queries.map { query ->
                val beforeWorlds = baselineBySeat.getValue(query.recipientSeat)
                val afterWorlds = beforeWorlds.filter { world ->
                    query.observations.all { observation ->
                        matchesObservation(
                            query = query,
                            world = world,
                            roles = rolesById,
                            observation = observation,
                            hypothesis = context.hypothesis,
                        )
                    }
                }
                val before = beforeBySeat.getValue(query.recipientSeat)
                ExactHypotheticalObservationBundleDiagnostics(
                    bundleId = query.bundleId,
                    recipientSeat = query.recipientSeat,
                    before = before.cardinality,
                    after = exactCardinality(afterWorlds.size.toLong()),
                    beforeStructure = before.structure,
                    afterStructure = summarizeWorldStructure(afterWorlds.asSequence(), rolesById),
                )
            },
        )
    }

    private fun exactInitialNightWorldSequence(
        snapshot: GameSnapshot,
        knowledge: PlayerKnowledgeSnapshot,
        roles: List<RoleDefinition>,
        rolesById: Map<RoleId, RoleDefinition>,
    ): Sequence<EnumeratedWorld> {
        val stream = TroubleBrewingWorldEnumerator.stream(snapshot.rulesetRef, knowledge, roles)
        return stream.worlds.filter { world ->
            knowledge.setupKnowledge.all { proposition ->
                TroubleBrewingWorldObservationEvaluator.evaluateKnownFact(world, rolesById, proposition)
            }
        }
    }

    private fun matchesObservation(
        query: ExactHypotheticalObservationBundleQuery,
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        observation: EpistemicObservation,
        hypothesis: EpistemicHypothesis,
    ): Boolean {
        val result = TroubleBrewingWorldObservationEvaluator.evaluate(
            world = world,
            roles = roles,
            observation = observation,
            hypothesis = hypothesis,
        )
        if (!result.matches) return false
        val selected = query.registrationWitnessBindingFor(observation) ?: return true
        return result.registrationWitnesses.any { witness ->
            registrationWitnessMatches(
                selected = selected.registrations,
                exactWitness = witness,
                roles = roles,
            )
        }
    }

    private fun registrationWitnessMatches(
        selected: Set<RegistrationFact>,
        exactWitness: Set<RegistrationFact>,
        roles: Map<RoleId, RoleDefinition>,
    ): Boolean {
        if (selected.size != exactWitness.size) return false
        if (selected.isEmpty()) return true

        val unmatched = exactWitness.toMutableList()
        selected.forEach { selectedFact ->
            val matchIndex = unmatched.indexOfFirst { exactFact ->
                sameRegistrationSelection(
                    selected = selectedFact,
                    exact = exactFact,
                    roles = roles,
                )
            }
            if (matchIndex < 0) return false
            unmatched.removeAt(matchIndex)
        }
        return unmatched.isEmpty()
    }

    /**
     * Generator-local interaction IDs and query labels are provenance, not selected registration
     * identity. The exact observation supplies interaction scope. Compare the selected semantic
     * registration against the dimensions the exact observation actually queried.
     */
    private fun sameRegistrationSelection(
        selected: RegistrationFact,
        exact: RegistrationFact,
        roles: Map<RoleId, RoleDefinition>,
    ): Boolean {
        if (selected.subjectSeat != exact.subjectSeat || selected.reason != exact.reason) return false

        val selectedRole = selected.registeredRole
        val exactRole = exact.registeredRole
        if (exactRole != null && selectedRole != exactRole) return false

        val selectedType = selected.registeredType ?: selectedRole?.let(roles::get)?.type
        val exactType = exact.registeredType ?: exactRole?.let(roles::get)?.type
        if (exactType != null && selectedType != exactType) return false

        val selectedAlignment =
            selected.registeredAlignment ?: selectedRole?.let(roles::get)?.alignment
        val exactAlignment =
            exact.registeredAlignment ?: exactRole?.let(roles::get)?.alignment
        if (exactAlignment != null && selectedAlignment != exactAlignment) return false

        return true
    }

    private fun validateQueries(
        queries: List<ExactHypotheticalObservationBundleQuery>,
        knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot>,
        snapshotId: String,
    ) {
        queries.forEach { query ->
            require(query.recipientSeat in knowledgeBySeat) {
                "Hypothetical observation bundle ${query.bundleId} references unknown recipient seat ${query.recipientSeat}."
            }
            query.observations.forEach { observation ->
                require(observation.snapshotId == snapshotId) {
                    "Hypothetical observation ${observation.observationId} in bundle ${query.bundleId} is bound to a different formal snapshot."
                }
                require(
                    observation.visibility == ObservationVisibility.PUBLIC ||
                        query.recipientSeat in observation.recipientSeats,
                ) {
                    "Hypothetical observation ${observation.observationId} in bundle ${query.bundleId} is not visible to recipient seat ${query.recipientSeat}."
                }
            }
        }
    }

    private fun isStrictShownRoleClaim(observation: EpistemicObservation): Boolean =
        observation.reliability == ObservationReliability.NOT_ABILITY_INFORMATION &&
            observation.proposition is InformationProposition.ShownRoleAt

    private data class PristinePrefilterKey(
        val strictShownRoles: List<InformationProposition.ShownRoleAt>,
        val publicClaimShownRoles: List<InformationProposition.ShownRoleAt>,
    ) {
        val stableId: String = buildString {
            append("strict=")
            append(strictShownRoles.joinToString(";") { "${it.seat}:${it.role.value}" })
            append("|claims=")
            append(publicClaimShownRoles.joinToString(";") { "${it.seat}:${it.role.value}" })
        }
    }

    private fun pristinePrefilterKey(
        query: ExactHypotheticalObservationBundleQuery,
    ): PristinePrefilterKey = PristinePrefilterKey(
        strictShownRoles = query.observations
            .filter(::isStrictShownRoleClaim)
            .map { it.proposition as InformationProposition.ShownRoleAt }
            .distinct()
            .sortedWith(compareBy({ it.seat }, { it.role.value })),
        publicClaimShownRoles = query.observations
            .mapNotNull(::publicClaimShownRoleEnvelope)
            .distinct()
            .sortedWith(compareBy({ it.seat }, { it.role.value })),
    )

    /**
     * Recognizes the public-claim identity envelope without depending on the internal clue branch.
     *
     * The claim must contain one evil-speaker branch and every non-evil branch must require the same
     * shown role. Only that necessary identity constraint is returned for cheap prefiltering; the
     * complete proposition, including functioning/malfunctioning alternatives, is evaluated exactly
     * afterwards.
     */
    private fun publicClaimShownRoleEnvelope(
        observation: EpistemicObservation,
    ): InformationProposition.ShownRoleAt? {
        if (observation.reliability != ObservationReliability.NOT_ABILITY_INFORMATION) return null
        val any = observation.proposition as? InformationProposition.AnyOf ?: return null
        val evilBranches = any.alternatives
            .filterIsInstance<InformationProposition.AlignmentAt>()
            .filter { it.alignment == Alignment.EVIL }
        val evilBranch = evilBranches.singleOrNull() ?: return null
        val claimedGoodBranches = any.alternatives.filterNot { it == evilBranch }
        if (claimedGoodBranches.isEmpty()) return null

        val shownRoles = claimedGoodBranches.map { branch ->
            val all = branch as? InformationProposition.AllOf ?: return null
            all.propositions.filterIsInstance<InformationProposition.ShownRoleAt>().singleOrNull()
                ?: return null
        }.distinct()
        val shownRole = shownRoles.singleOrNull() ?: return null
        if (evilBranch.seat != shownRole.seat) return null
        if (observation.sourceSeat != null && observation.sourceSeat != shownRole.seat) return null
        return shownRole
    }

    /**
     * Necessary-only prefilter for a public claim. A world survives when the speaker can satisfy the
     * lie branch (actual evil; Recluse is retained conservatively because it can register evil) or
     * when its shown role matches the claimed-good envelope. Exact claim semantics are applied later.
     */
    private fun publicClaimIdentityEnvelopeMatches(
        world: EnumeratedWorld,
        roles: Map<RoleId, RoleDefinition>,
        claim: InformationProposition.ShownRoleAt,
    ): Boolean {
        val actualRole = world.rolesBySeat[claim.seat] ?: return false
        val speakerMaySatisfyEvilBranch =
            roles.getValue(actualRole).alignment == Alignment.EVIL ||
                actualRole.value.equals("Recluse", ignoreCase = true)
        return speakerMaySatisfyEvilBranch || world.shownRolesBySeat[claim.seat] == claim.role
    }

    private data class ExactWorldScan(
        val cardinality: WorldCardinality.Exact,
        val structure: ExactWorldStructureDiagnostics,
    )

    private fun scanWorlds(
        worlds: Sequence<EnumeratedWorld>,
        roles: Map<RoleId, RoleDefinition>,
    ): ExactWorldScan {
        var count = 0L
        val accumulator = WorldStructureAccumulator(roles)
        worlds.forEach { world ->
            count += 1
            accumulator.accept(world)
        }
        return ExactWorldScan(
            cardinality = exactCardinality(count),
            structure = accumulator.finish(),
        )
    }

    private fun summarizeWorldStructure(
        worlds: Sequence<EnumeratedWorld>,
        roles: Map<RoleId, RoleDefinition>,
    ): ExactWorldStructureDiagnostics {
        val accumulator = WorldStructureAccumulator(roles)
        worlds.forEach(accumulator::accept)
        return accumulator.finish()
    }

    private class WorldStructureAccumulator(
        private val roles: Map<RoleId, RoleDefinition>,
    ) {
        private val possibleDemonSeats = sortedSetOf<Int>()
        private val evilConfigurations = linkedSetOf<Set<Int>>()
        private val evilCoverSeats = sortedSetOf<Int>()
        private var forcedGoodSeats: MutableSet<Int>? = null
        private var forcedEvilSeats: MutableSet<Int>? = null
        private var seenWorld = false

        fun accept(world: EnumeratedWorld) {
            seenWorld = true
            val current = world.currentRolesBySeat
            current.forEach { (seat, role) ->
                if (seat in world.aliveSeats && roles.getValue(role).type == CharacterType.DEMON) {
                    possibleDemonSeats += seat
                }
            }
            val evilSeats = current
                .filterValues { role -> roles.getValue(role).alignment == Alignment.EVIL }
                .keys
                .toSortedSet()
            val goodSeats = current.keys.filterTo(sortedSetOf()) { seat ->
                roles.getValue(current.getValue(seat)).alignment == Alignment.GOOD
            }
            evilConfigurations += evilSeats.toSet()
            evilCoverSeats += evilSeats
            if (forcedGoodSeats == null) {
                forcedGoodSeats = goodSeats
                forcedEvilSeats = evilSeats
            } else {
                forcedGoodSeats!!.retainAll(goodSeats)
                forcedEvilSeats!!.retainAll(evilSeats)
            }
        }

        fun finish(): ExactWorldStructureDiagnostics {
            if (!seenWorld) return ExactWorldStructureDiagnostics.EMPTY
            val canonicalConfigurations = evilConfigurations
                .sortedWith(compareBy<Set<Int>>({ it.size }, { it.joinToString(",") }))
                .toCollection(linkedSetOf())
            return ExactWorldStructureDiagnostics(
                possibleDemonSeats = possibleDemonSeats,
                evilTeamSeatConfigurations = canonicalConfigurations,
                forcedGoodSeats = forcedGoodSeats.orEmpty().toSortedSet(),
                forcedEvilSeats = forcedEvilSeats.orEmpty().toSortedSet(),
                evilCoverSeats = evilCoverSeats,
            )
        }
    }

    private fun exactCardinality(count: Long): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(count))
}
