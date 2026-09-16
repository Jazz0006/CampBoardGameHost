package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
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
 * A complete set of hypothetical observations that must hold together in the same exact world.
 *
 * The evaluator deliberately accepts already-bound [EpistemicObservation] values. Draft-to-public
 * projection belongs to the bundle-composition/profile layer; exact epistemic evaluation must not
 * silently reinterpret observation visibility or timeline identity.
 */
internal data class ExactHypotheticalObservationBundleQuery(
    val bundleId: String,
    val recipientSeat: Int,
    val observations: List<EpistemicObservation>,
) {
    init {
        require(bundleId.isNotBlank()) { "Hypothetical observation bundle ID cannot be blank." }
        require(recipientSeat > 0) { "Hypothetical observation bundle recipient seat must be positive." }
    }
}

/**
 * Explainable strategic structure of one exact possible-world set.
 *
 * This is deliberately descriptive rather than evaluative: no Badness thresholds, probabilities,
 * or preference score belong here. Cover sizes are set-union sizes: the number of seats needed to
 * cover every surviving demon placement or every surviving evil-team placement respectively.
 */
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

/**
 * Unsupported exact semantics are represented only as [Deferred]. A supported bundle which leaves
 * zero worlds is still [Ready] with an exact zero AFTER cardinality; DEFERRED must never mean UNSAT.
 */
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
 * One historical BEFORE baseline is built per recipient and shared by every bundle for that
 * recipient. Every observation in a bundle is evaluated against the same candidate world, so bundle
 * semantics are logical conjunction rather than a sum or merge of independently scored clues.
 * Trouble Brewing observation truth/registration/malfunction semantics remain owned by
 * [TroubleBrewingWorldObservationEvaluator].
 *
 * A pristine Night-1 context has no historical replay to perform, so it may use the already-proven
 * direct ZDD representation internally. This is an experiment/evaluator representation choice, not
 * a change to the production A4 rollout policy. Historical contexts continue to use the enumerated
 * replay snapshot until historical symbolic replay itself is validated.
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
        val formal = FormalGameState.from(
            snapshot = snapshot,
            phase = context.initialPhase,
            round = context.initialRound,
        )
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = context.perceivedRolesBySeat.toMap(),
            observationLog = context.observationLog,
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val roles = context.roleDefinitions.toList()
        val rolesById = roles.associateBy(RoleDefinition::id)
        require(rolesById.isNotEmpty()) { "Exact hypothetical bundle evaluation requires role definitions." }

        val stableQueries = queries.map { query ->
            query.copy(observations = query.observations.toList())
        }
        stableQueries.forEach { query ->
            require(query.recipientSeat in knowledgeBySeat) {
                "Hypothetical observation bundle ${query.bundleId} references unknown recipient seat ${query.recipientSeat}."
            }
            query.observations.forEach { observation ->
                require(observation.snapshotId == formal.snapshotId) {
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

        val baselineWorldSetsBySeat = stableQueries
            .map(ExactHypotheticalObservationBundleQuery::recipientSeat)
            .distinct()
            .associateWith { recipientSeat ->
                buildExactBaseline(
                    validatedRuleset = validatedRuleset,
                    snapshot = snapshot,
                    knowledge = knowledgeBySeat.getValue(recipientSeat),
                    roles = roles,
                    context = context,
                )
            }
        val baselineStructureBySeat = baselineWorldSetsBySeat.mapValues { (_, worlds) ->
            summarizeWorldStructure(worlds.exactWorlds(), rolesById)
        }

        return ExactHypotheticalObservationBundleEvaluation.Ready(
            diagnostics = stableQueries.map { query ->
                val before = baselineWorldSetsBySeat.getValue(query.recipientSeat)
                val after = before.filter(
                    observations = exactFilterOrder(query.observations),
                    roles = rolesById,
                    hypothesis = context.hypothesis,
                )
                ExactHypotheticalObservationBundleDiagnostics(
                    bundleId = query.bundleId,
                    recipientSeat = query.recipientSeat,
                    before = before.cardinality(),
                    after = after.cardinality(),
                    beforeStructure = baselineStructureBySeat.getValue(query.recipientSeat),
                    afterStructure = summarizeWorldStructure(after.exactWorlds(), rolesById),
                )
            },
        )
    }

    private fun buildExactBaseline(
        validatedRuleset: ValidatedClocktowerRuleset,
        snapshot: GameSnapshot,
        knowledge: PlayerKnowledgeSnapshot,
        roles: List<RoleDefinition>,
        context: ExactHistoricalHypotheticalContext,
    ): ExactWorldFamily {
        val pristineInitialNight =
            context.initialPhase == StorytellerPhase.FIRST_NIGHT &&
                context.initialRound == 1 &&
                context.actionTimeline.entries.isEmpty() &&
                context.observationLog.records.isEmpty()
        if (pristineInitialNight) {
            return ZddExactWorldFamily(
                ZddPlayerWorldSet.enumerateDirect(
                    rulesetRef = snapshot.rulesetRef,
                    knowledge = knowledge,
                    hypothesis = context.hypothesis,
                    roleDefinitions = roles,
                ),
            )
        }
        return EnumeratedExactWorldFamily(
            EnumeratedHistoricalExactBaseline.build(
                validatedRuleset = validatedRuleset,
                rulesetRef = snapshot.rulesetRef,
                setupKnowledge = knowledge,
                hypothesis = context.hypothesis,
                roleDefinitions = roles,
                initialPhase = context.initialPhase,
                initialRound = context.initialRound,
                actionTimeline = context.actionTimeline,
                observationLog = context.observationLog,
            ).worldSet.enumeratedWorlds(),
        )
    }

    /**
     * Conjunction is order-independent. Apply cheap exact symbolic identity facts first so fallback
     * semantic filters decode only the already-narrowed family instead of the whole Night-1 space.
     */
    private fun exactFilterOrder(observations: List<EpistemicObservation>): List<EpistemicObservation> =
        observations.sortedWith(
            compareBy<EpistemicObservation>(
                { observation ->
                    when {
                        observation.reliability == ObservationReliability.NOT_ABILITY_INFORMATION &&
                            observation.proposition is InformationProposition.ShownRoleAt -> 0
                        observation.reliability == ObservationReliability.NOT_ABILITY_INFORMATION -> 1
                        else -> 2
                    }
                },
                EpistemicObservation::sequence,
                EpistemicObservation::observationId,
            ),
        )

    private fun summarizeWorldStructure(
        worlds: Sequence<EnumeratedWorld>,
        roles: Map<RoleId, RoleDefinition>,
    ): ExactWorldStructureDiagnostics {
        val possibleDemonSeats = sortedSetOf<Int>()
        val evilConfigurations = linkedSetOf<Set<Int>>()
        val evilCoverSeats = sortedSetOf<Int>()
        var forcedGoodSeats: MutableSet<Int>? = null
        var forcedEvilSeats: MutableSet<Int>? = null
        var seenWorld = false

        worlds.forEach { world ->
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

    private sealed interface ExactWorldFamily {
        fun cardinality(): WorldCardinality.Exact
        fun exactWorlds(): Sequence<EnumeratedWorld>
        fun filter(
            observations: List<EpistemicObservation>,
            roles: Map<RoleId, RoleDefinition>,
            hypothesis: EpistemicHypothesis,
        ): ExactWorldFamily
    }

    private data class ZddExactWorldFamily(
        val worldSet: ZddPlayerWorldSet,
    ) : ExactWorldFamily {
        override fun cardinality(): WorldCardinality.Exact =
            worldSet.cardinality() as WorldCardinality.Exact

        override fun exactWorlds(): Sequence<EnumeratedWorld> = worldSet.exactWorlds()

        override fun filter(
            observations: List<EpistemicObservation>,
            roles: Map<RoleId, RoleDefinition>,
            hypothesis: EpistemicHypothesis,
        ): ExactWorldFamily {
            val filtered = observations.fold(worldSet) { current, observation ->
                current.require(observation)
            }
            return ZddExactWorldFamily(filtered)
        }
    }

    private data class EnumeratedExactWorldFamily(
        val worlds: List<EnumeratedWorld>,
    ) : ExactWorldFamily {
        override fun cardinality(): WorldCardinality.Exact =
            WorldCardinality.Exact(BigInteger.valueOf(worlds.size.toLong()))

        override fun exactWorlds(): Sequence<EnumeratedWorld> = worlds.asSequence()

        override fun filter(
            observations: List<EpistemicObservation>,
            roles: Map<RoleId, RoleDefinition>,
            hypothesis: EpistemicHypothesis,
        ): ExactWorldFamily = EnumeratedExactWorldFamily(
            worlds.filter { world ->
                observations.all { observation ->
                    TroubleBrewingWorldObservationEvaluator.evaluate(
                        world = world,
                        roles = roles,
                        observation = observation,
                        hypothesis = hypothesis,
                    ).matches
                }
            },
        )
    }
}
