package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import java.math.BigInteger

/**
 * A compact, lossless provenance family for complete healthy first-night bundles.
 *
 * Public factor selections are fixed. Every Cartesian choice in [latentOptionIdsByFactor] is a
 * complete legal bundle with the same PUBLIC_GOOD_INFO projection. This preserves complete-bundle
 * identity/multiplicity without materializing all 110k representative raw bundles in memory.
 */
internal data class FirstNightBundleProvenanceFamily(
    val selectedPublicOptionIds: Map<String, String>,
    val latentOptionIdsByFactor: Map<String, List<String>>,
    val multiplicity: BigInteger,
) {
    init {
        require(selectedPublicOptionIds.keys.all(String::isNotBlank))
        require(selectedPublicOptionIds.values.all(String::isNotBlank))
        require(latentOptionIdsByFactor.keys.all(String::isNotBlank))
        require(latentOptionIdsByFactor.values.all { options -> options.isNotEmpty() && options.all(String::isNotBlank) })
        require(multiplicity == productOfOptionDomains(latentOptionIdsByFactor.values)) {
            "First-night provenance multiplicity must equal the latent Cartesian product."
        }
    }

    fun completeBundleIds(): Sequence<String> = sequence {
        val latentFactors = latentOptionIdsByFactor.entries.sortedBy { it.key }
        suspend fun SequenceScope<String>.expand(
            index: Int,
            selections: Map<String, String>,
        ) {
            if (index == latentFactors.size) {
                yield(stableCompleteBundleId(selections))
                return
            }
            val factor = latentFactors[index]
            factor.value.sorted().forEach { optionId ->
                expand(index + 1, selections + (factor.key to optionId))
            }
        }
        expand(0, selectedPublicOptionIds.toSortedMap())
    }
}

internal data class FirstNightHealthyRecipientExactDiagnostics(
    val recipientSeat: Int,
    val before: WorldCardinality.Exact,
    val after: WorldCardinality.Exact,
    val beforeStructure: ExactWorldStructureDiagnostics,
    val afterStructure: ExactWorldStructureDiagnostics,
)

internal data class FirstNightLeaveOneOutDiagnostics(
    val omittedObservationKey: String,
    val recipientDiagnostics: List<FirstNightHealthyRecipientExactDiagnostics>,
)

internal data class FirstNightProjectedSignatureGroup(
    val signatureId: String,
    val canonicalObservationKeys: List<String>,
    val publicObservations: List<EpistemicObservation>,
    val provenanceFamilies: List<FirstNightBundleProvenanceFamily>,
    val multiplicity: BigInteger,
    val recipientDiagnostics: List<FirstNightHealthyRecipientExactDiagnostics>,
    val leaveOneOutDiagnostics: List<FirstNightLeaveOneOutDiagnostics>,
)

internal sealed interface FirstNightHealthyBundleHarnessEvaluation {
    data class Ready(
        val rawCompleteBundleCount: BigInteger,
        val publicFactorCombinationCount: BigInteger,
        val distinctProjectedSignatureCount: Int,
        val exactEvaluatedSignatureCount: Int,
        val excludedCounterworldRoles: Set<RoleId>,
        val signatureGroups: List<FirstNightProjectedSignatureGroup>,
        val samplingApplied: Boolean,
    ) : FirstNightHealthyBundleHarnessEvaluation

    data class Deferred(
        val deferredComplexities: Set<FirstNightBundleDeferredComplexity> = emptySet(),
        val missingCapabilities: Set<EpistemicEvaluationCapability> = emptySet(),
    ) : FirstNightHealthyBundleHarnessEvaluation {
        init {
            require(deferredComplexities.isNotEmpty() || missingCapabilities.isNotEmpty()) {
                "Deferred healthy bundle evaluation must identify staged complexity or a missing exact capability."
            }
        }
    }
}

/**
 * FN-BUNDLE-2 experiment harness for complete healthy 7-player Trouble Brewing first nights.
 *
 * Legality remains in the canonical producers audited by
 * [TroubleBrewingFirstNightBundleCandidateSpaceAuditor]. This harness only materializes those
 * producer choices, quotients complete bundles by their PUBLIC_GOOD_INFO semantics, and asks the
 * epistemic exact evaluator for descriptive consequences. It deliberately contains no Badness gate.
 *
 * The first experiment's counterworld domain deliberately excludes the uncertainty sources reserved
 * for later staged validation: Drunk, Spy/Recluse registration, and Poisoner impairment. Candidate
 * legality still uses the complete official role catalog. Later experiments widen only this explicit
 * diagnostic domain; they do not replace or weaken the rules engine.
 */
internal object TroubleBrewingFirstNightHealthyBundleHarness {
    private val pairRoles = setOf(RoleId("Washerwoman"), RoleId("Librarian"), RoleId("Investigator"))
    private val numericRoles = setOf(RoleId("Chef"), RoleId("Empath"))
    private val stagedCounterworldRoles = setOf(
        RoleId("Drunk"),
        RoleId("Spy"),
        RoleId("Recluse"),
        RoleId("Poisoner"),
    )

    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        evaluationRecipientSeats: Set<Int>,
    ): FirstNightHealthyBundleHarnessEvaluation {
        require(context.initialPhase == StorytellerPhase.FIRST_NIGHT && context.initialRound == 1) {
            "FN-BUNDLE-2 healthy harness is defined only for Night 1."
        }
        require(evaluationRecipientSeats.isNotEmpty()) {
            "FN-BUNDLE-2 healthy harness requires at least one evaluation recipient."
        }
        val game = context.initialSnapshot.gameState
        require(game.players.size == 7) { "FN-BUNDLE-2 healthy harness currently requires exactly 7 players." }
        require(game.players.none { it.poisoned }) { "FN-BUNDLE-2 healthy harness cannot flatten poisoned state." }
        require(game.players.all { (it.shownRole ?: it.actualRole) == it.actualRole }) {
            "FN-BUNDLE-2 healthy harness cannot flatten a non-actual shown role; stage Drunk separately."
        }

        val roles = context.roleDefinitions.toList()
        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roles)
        if (audit.deferredComplexities.isNotEmpty()) {
            return FirstNightHealthyBundleHarnessEvaluation.Deferred(
                deferredComplexities = audit.deferredComplexities,
            )
        }
        requireNotNull(audit.legalCompleteBundleCount) {
            "Healthy candidate-space audit must know the complete legal bundle count."
        }

        val exactCounterworldRoles = roles.filterNot { it.id in stagedCounterworldRoles }
        val exactRoleIds = exactCounterworldRoles.mapTo(linkedSetOf(), RoleDefinition::id)
        require(game.players.all { it.actualRole in exactRoleIds }) {
            "Healthy FN-BUNDLE-2 must defer a setup containing a staged counterworld role."
        }

        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val factors = audit.factors.mapIndexed { index, factor ->
            materializeFactor(
                game = game,
                roleDefinitions = roles,
                formal = formal,
                sequence = index + 1,
                factor = factor,
            )
        }
        val publicFactors = factors.filter { it.audit.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO }
        val latentFactors = factors.filter { it.audit.profileExposure == FirstNightBundleProfileExposure.NOT_SHARED }
        val latentOptionDomains = latentFactors.associate { factor ->
            factor.audit.factorId to factor.options.map(ConcreteFactorOption::optionId).sorted()
        }.toSortedMap()
        val latentMultiplicity = productOfOptionDomains(latentOptionDomains.values)

        val mutableGroups = linkedMapOf<String, MutableSignatureGroup>()
        cartesianPublicSelections(publicFactors).forEach { publicSelection ->
            val selectedPublicOptionIds = publicSelection
                .associate { (factor, option) -> factor.audit.factorId to option.optionId }
                .toSortedMap()
            val representativeSelections = buildMap {
                putAll(selectedPublicOptionIds)
                latentFactors.forEach { factor ->
                    put(factor.audit.factorId, factor.options.first().optionId)
                }
            }.toSortedMap()
            val representativeEntries = buildList {
                publicSelection.forEach { (_, option) -> add(option.entry) }
                latentFactors.forEach { factor -> add(factor.options.first().entry) }
            }
            val representative = FirstNightInformationBundle(
                bundleId = stableCompleteBundleId(representativeSelections),
                entries = representativeEntries,
            )
            val projected = canonicalizeProjectedObservations(
                FirstNightPublicGoodInfoProjection.project(representative),
            )
            val observationKeys = projected.map(::canonicalObservationKey)
            val signatureCanonical = observationKeys.joinToString("\n")
            val signatureId = stableSignatureId(signatureCanonical)
            val family = FirstNightBundleProvenanceFamily(
                selectedPublicOptionIds = selectedPublicOptionIds,
                latentOptionIdsByFactor = latentOptionDomains,
                multiplicity = latentMultiplicity,
            )
            val group = mutableGroups.getOrPut(signatureCanonical) {
                MutableSignatureGroup(
                    signatureId = signatureId,
                    observationKeys = observationKeys,
                    observations = projected,
                )
            }
            group.provenanceFamilies += family
        }

        val signatureGroups = mutableGroups.values.sortedBy(MutableSignatureGroup::signatureId)
        val queries = buildList {
            signatureGroups.forEach { group ->
                evaluationRecipientSeats.sorted().forEach { recipientSeat ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = fullQueryId(group.signatureId, recipientSeat),
                            recipientSeat = recipientSeat,
                            observations = group.observations,
                        ),
                    )
                    group.observations.indices.forEach { omittedIndex ->
                        add(
                            ExactHypotheticalObservationBundleQuery(
                                bundleId = leaveOneOutQueryId(group.signatureId, omittedIndex, recipientSeat),
                                recipientSeat = recipientSeat,
                                observations = group.observations.filterIndexed { index, _ -> index != omittedIndex },
                            ),
                        )
                    }
                }
            }
        }

        val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context.copy(roleDefinitions = exactCounterworldRoles),
            queries = queries,
        )
        if (exact is ExactHypotheticalObservationBundleEvaluation.Deferred) {
            return FirstNightHealthyBundleHarnessEvaluation.Deferred(
                missingCapabilities = exact.missingCapabilities,
            )
        }
        val exactById = (exact as ExactHypotheticalObservationBundleEvaluation.Ready)
            .diagnostics.associateBy(ExactHypotheticalObservationBundleDiagnostics::bundleId)

        val completedGroups = signatureGroups.map { group ->
            val fullDiagnostics = evaluationRecipientSeats.sorted().map { recipientSeat ->
                exactById.getValue(fullQueryId(group.signatureId, recipientSeat)).toHealthyDiagnostics()
            }
            val leaveOneOut = group.observations.indices.map { omittedIndex ->
                FirstNightLeaveOneOutDiagnostics(
                    omittedObservationKey = group.observationKeys[omittedIndex],
                    recipientDiagnostics = evaluationRecipientSeats.sorted().map { recipientSeat ->
                        exactById.getValue(
                            leaveOneOutQueryId(group.signatureId, omittedIndex, recipientSeat),
                        ).toHealthyDiagnostics()
                    },
                )
            }
            FirstNightProjectedSignatureGroup(
                signatureId = group.signatureId,
                canonicalObservationKeys = group.observationKeys,
                publicObservations = group.observations,
                provenanceFamilies = group.provenanceFamilies.toList(),
                multiplicity = group.provenanceFamilies.fold(BigInteger.ZERO) { sum, family ->
                    sum + family.multiplicity
                },
                recipientDiagnostics = fullDiagnostics,
                leaveOneOutDiagnostics = leaveOneOut,
            )
        }

        val totalMultiplicity = completedGroups.fold(BigInteger.ZERO) { sum, group -> sum + group.multiplicity }
        require(totalMultiplicity == audit.legalCompleteBundleCount) {
            "Projected signature quotient lost complete-bundle multiplicity: $totalMultiplicity != ${audit.legalCompleteBundleCount}."
        }

        return FirstNightHealthyBundleHarnessEvaluation.Ready(
            rawCompleteBundleCount = audit.legalCompleteBundleCount,
            publicFactorCombinationCount = audit.representedPublicProjectionUpperBound,
            distinctProjectedSignatureCount = completedGroups.size,
            exactEvaluatedSignatureCount = completedGroups.size,
            excludedCounterworldRoles = stagedCounterworldRoles,
            signatureGroups = completedGroups,
            samplingApplied = false,
        )
    }

    private fun materializeFactor(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        formal: FormalGameState,
        sequence: Int,
        factor: FirstNightBundleCandidateFactorAudit,
    ): ConcreteFactor {
        val options = when (factor.kind) {
            FirstNightBundleCandidateFactorKind.PAIR_INFORMATION -> {
                val sourceSeat = requireNotNull(factor.sourceSeat)
                val source = requireNotNull(game.playerAt(sourceSeat))
                require(source.actualRole in pairRoles)
                NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
                    game = game,
                    sourceSeat = sourceSeat,
                    abilityRole = source.actualRole,
                    roleDefinitions = roleDefinitions,
                ).map { candidate ->
                    val information = candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().single()
                    concreteInformationOption(
                        game = game,
                        roleDefinitions = roleDefinitions,
                        formal = formal,
                        sequence = sequence,
                        factor = factor,
                        optionId = candidate.candidateId,
                        information = information,
                    )
                }
            }
            FirstNightBundleCandidateFactorKind.FIXED_NUMERIC_INFORMATION -> {
                val sourceSeat = requireNotNull(factor.sourceSeat)
                val source = requireNotNull(game.playerAt(sourceSeat))
                require(source.actualRole in numericRoles)
                FirstNightNumericInformationSemantics.healthyTruthValues(game, sourceSeat)
                    .toSortedSet()
                    .map { value ->
                        concreteInformationOption(
                            game = game,
                            roleDefinitions = roleDefinitions,
                            formal = formal,
                            sequence = sequence,
                            factor = factor,
                            optionId = "value-$value",
                            information = EffectDraft.PlayerInformation(
                                recipientSeat = sourceSeat,
                                sourceAbility = source.actualRole,
                                value = InformationValue.Number(value),
                            ),
                        )
                    }
            }
            FirstNightBundleCandidateFactorKind.RED_HERRING ->
                SetupCandidateGenerator.generateRedHerringCandidates(game).map { candidate ->
                    concreteLatentOption(factor, candidate.candidateId)
                }
            FirstNightBundleCandidateFactorKind.DEMON_BLUFFS ->
                SetupCandidateGenerator.generateDemonBluffCandidates(game, roleDefinitions).map { candidate ->
                    concreteLatentOption(factor, candidate.candidateId)
                }
        }.sortedBy(ConcreteFactorOption::optionId)

        require(options.map(ConcreteFactorOption::optionId) == factor.optionIds.sorted()) {
            "Concrete factor ${factor.factorId} drifted from the canonical candidate-space audit."
        }
        require(options.isNotEmpty()) { "Healthy factor ${factor.factorId} has no legal option." }
        return ConcreteFactor(factor, options)
    }

    private fun concreteInformationOption(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        formal: FormalGameState,
        sequence: Int,
        factor: FirstNightBundleCandidateFactorAudit,
        optionId: String,
        information: EffectDraft.PlayerInformation,
    ): ConcreteFactorOption {
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = information,
            roleDefinitions = roleDefinitions,
        )
        val observation = EpistemicObservation(
            observationId = "fn2-private-${stableToken("${factor.factorId}|$optionId")}",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = sequence,
            sourceSeat = information.recipientSeat,
            sourceAbility = information.sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(information.recipientSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
        return ConcreteFactorOption(
            optionId = optionId,
            entry = FirstNightInformationBundleEntry(
                entryId = factor.factorId,
                control = factor.control,
                sourceChoiceId = optionId.takeIf { factor.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED },
                observation = observation,
                profileExposure = factor.profileExposure,
            ),
        )
    }

    private fun concreteLatentOption(
        factor: FirstNightBundleCandidateFactorAudit,
        optionId: String,
    ) = ConcreteFactorOption(
        optionId = optionId,
        entry = FirstNightInformationBundleEntry(
            entryId = factor.factorId,
            control = factor.control,
            sourceChoiceId = optionId,
            observation = null,
            profileExposure = factor.profileExposure,
        ),
    )

    private fun cartesianPublicSelections(
        factors: List<ConcreteFactor>,
    ): List<List<Pair<ConcreteFactor, ConcreteFactorOption>>> {
        if (factors.isEmpty()) return listOf(emptyList())
        return factors.fold(listOf(emptyList())) { partial, factor ->
            partial.flatMap { selections ->
                factor.options.map { option -> selections + (factor to option) }
            }
        }
    }

    private fun canonicalizeProjectedObservations(
        observations: List<EpistemicObservation>,
    ): List<EpistemicObservation> = observations
        .groupBy(::canonicalObservationKey)
        .toSortedMap()
        .values
        .map { equivalent -> equivalent.first() }

    private fun canonicalObservationKey(observation: EpistemicObservation): String =
        EpistemicSemanticJson.encode(
            observation.copy(
                observationId = "fn2-signature",
                sequence = 0,
            ),
        )

    private fun ExactHypotheticalObservationBundleDiagnostics.toHealthyDiagnostics() =
        FirstNightHealthyRecipientExactDiagnostics(
            recipientSeat = recipientSeat,
            before = before,
            after = after,
            beforeStructure = beforeStructure,
            afterStructure = afterStructure,
        )

    private fun fullQueryId(signatureId: String, recipientSeat: Int) =
        "fn2:$signatureId:full:r$recipientSeat"

    private fun leaveOneOutQueryId(signatureId: String, omittedIndex: Int, recipientSeat: Int) =
        "fn2:$signatureId:loo-$omittedIndex:r$recipientSeat"

    private data class ConcreteFactor(
        val audit: FirstNightBundleCandidateFactorAudit,
        val options: List<ConcreteFactorOption>,
    )

    private data class ConcreteFactorOption(
        val optionId: String,
        val entry: FirstNightInformationBundleEntry,
    )

    private data class MutableSignatureGroup(
        val signatureId: String,
        val observationKeys: List<String>,
        val observations: List<EpistemicObservation>,
        val provenanceFamilies: MutableList<FirstNightBundleProvenanceFamily> = mutableListOf(),
    )
}

private fun productOfOptionDomains(domains: Collection<List<String>>): BigInteger =
    domains.fold(BigInteger.ONE) { product, options ->
        product.multiply(BigInteger.valueOf(options.size.toLong()))
    }

private fun stableCompleteBundleId(selections: Map<String, String>): String {
    val canonical = selections.toSortedMap().entries.joinToString("|") { (factor, option) -> "$factor=$option" }
    return "fn2-${stableToken(canonical)}"
}

private fun stableSignatureId(canonical: String): String = "sig-${stableToken(canonical)}"

private fun stableToken(canonical: String): String = java.lang.Long
    .toUnsignedString(MurmurHash3.low64Utf8(canonical), 16)
    .padStart(16, '0')
