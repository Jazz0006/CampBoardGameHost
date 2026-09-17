package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorAudit
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorKind
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetValidator
import java.io.File
import java.math.BigInteger

data class FirstNightFortuneTellerRobustnessCase(
    val targetSeats: List<Int>,
    val actualResult: Boolean,
    val diagnostics: FirstNightBeginnerDiagnostics,
)

data class FirstNightFortuneTellerStorytellerCandidate(
    val candidateId: String,
    val redHerringSeat: Int,
    val publicChoices: Map<String, String>,
    val basePublicClaims: List<String>,
    val representedCompleteBundleMultiplicity: BigInteger,
    val robustnessCases: List<FirstNightFortuneTellerRobustnessCase>,
) {
    val yesCaseCount: Int get() = robustnessCases.count { it.actualResult }
    val noCaseCount: Int get() = robustnessCases.size - yesCaseCount
}

data class FirstNightFortuneTellerPilot(
    val presetId: String,
    val seatingProfile: String,
    val seating: List<Pair<Int, RoleId>>,
    val anchorRecipientSeat: Int,
    val fortuneTellerSeat: Int,
    val legalCompleteBundleCount: BigInteger,
    val publicInformationCombinationCount: Int,
    val redHerringCandidateCount: Int,
    val latentMultiplicityPerDiagnosticCandidate: BigInteger,
    val targetPairCount: Int,
    val diagnosticStorytellerCandidateCount: Int,
    val candidates: List<FirstNightFortuneTellerStorytellerCandidate>,
)

/**
 * Reworked FN-BUNDLE-3 Stage 7A pilot.
 *
 * The complete Storyteller decision surface is represented. Storyteller choices that cannot change
 * the current PUBLIC_GOOD_INFO consequence (currently demon bluffs) are retained as exact
 * multiplicity instead of being re-evaluated as duplicate epistemic queries. Red Herring is never
 * quotiented because it changes the mechanically correct Fortune Teller result.
 *
 * Fortune Teller target choice is not a Storyteller candidate. Every legal two-player target pair is
 * instead enumerated as a player-controlled robustness case for every diagnostic Storyteller
 * candidate. Possible worlds are exact; only redundant complete-bundle representations are
 * quotient-compressed.
 */
object FirstNightBundleBeginnerFortuneTellerPilotBuilder {
    private const val PLAYER_COUNT = 7
    private const val PILOT_PRESET_ID = "TB2_7_003"
    private const val SEATING_PROFILE = "EVIL_ADJACENT"

    private val fortuneTeller = RoleId("Fortune Teller")
    private val stagedCounterworldRoles = setOf(
        RoleId("Drunk"),
        RoleId("Spy"),
        RoleId("Recluse"),
        RoleId("Poisoner"),
    )

    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roleDefinitions.associateBy(RoleDefinition::id)

    fun build(): FirstNightFortuneTellerPilot {
        val dataset = TroubleBrewingSetupPresetJson.parse(
            File("src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json")
                .readText(Charsets.UTF_8),
        )
        TroubleBrewingSetupPresetValidator.validate(dataset, validatedRuleset.characterRegistry)
        val preset = dataset.pools.getValue(PLAYER_COUNT).single { it.id == PILOT_PRESET_ID }
        require(presetActualExternalRoleIds(preset).none { it in STAGED_EXTERNAL_ROLE_IDS }) {
            "Stage 7A Fortune Teller pilot must remain in the healthy-compatible role slice."
        }

        val game = gameFor(preset)
        val anchorSeat = 1
        val fortuneTellerSeat = game.players.single { it.actualRole == fortuneTeller }.seat
        val context = context(game)
        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roleDefinitions)
        require(audit.deferredComplexities.isEmpty()) {
            "Stage 7A Fortune Teller pilot unexpectedly contains deferred complexity ${audit.deferredComplexities}."
        }
        val legalCompleteBundleCount = requireNotNull(audit.legalCompleteBundleCount)

        val publicFactors = audit.factors
            .filter { it.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO }
            .mapIndexed { index, factor ->
                materializePublicFactor(game, formal, index + 1, factor)
            }
        val publicCombinations = cartesianPublicChoices(publicFactors)
        val redHerringFactor = audit.factors.single { it.kind == FirstNightBundleCandidateFactorKind.RED_HERRING }
        val redHerringCandidates = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .sortedBy { it.candidateId }
        require(redHerringCandidates.map { it.candidateId } == redHerringFactor.optionIds.sorted()) {
            "Fortune Teller pilot Red Herring materialization drifted from canonical candidate audit."
        }
        val redHerringSeatsByCandidate = redHerringCandidates.associate { candidate ->
            candidate.candidateId to (candidate.outcome as SetupClueOutcome.RedHerring).seat
        }
        val latentFactors = audit.factors.filter { factor ->
            factor.profileExposure == FirstNightBundleProfileExposure.NOT_SHARED &&
                factor.kind != FirstNightBundleCandidateFactorKind.RED_HERRING
        }
        val latentMultiplicity = latentFactors.fold(BigInteger.ONE) { product, factor ->
            product.multiply(BigInteger.valueOf(factor.optionCount.toLong()))
        }
        val targetPairs = FortuneTellerInformationSemantics.legalTargetPairs(game)
        require(targetPairs.size == 21) {
            "Seven-player Fortune Teller pilot must enumerate all 21 distinct target pairs."
        }

        val expectedCompleteCount = BigInteger.valueOf(publicCombinations.size.toLong())
            .multiply(BigInteger.valueOf(redHerringCandidates.size.toLong()))
            .multiply(latentMultiplicity)
        require(expectedCompleteCount == legalCompleteBundleCount) {
            "Diagnostic quotient lost complete-bundle multiplicity: expected $legalCompleteBundleCount, reconstructed $expectedCompleteCount."
        }

        val representativeLatentEntries = latentFactors.map { factor ->
            FirstNightInformationBundleEntry(
                entryId = factor.factorId,
                control = factor.control,
                sourceChoiceId = factor.optionIds.first(),
                profileExposure = factor.profileExposure,
            )
        }
        val exactContext = context.copy(
            roleDefinitions = roleDefinitions.filterNot { it.id in stagedCounterworldRoles },
        )

        val storytellerCandidates = publicCombinations.flatMapIndexed { publicIndex, publicChoices ->
            redHerringCandidates.mapIndexed { redIndex, redCandidate ->
                val redHerringSeat = redHerringSeatsByCandidate.getValue(redCandidate.candidateId)
                val candidateId = "ft7a-${publicIndex + 1}-${redIndex + 1}"
                val baseEntries = buildList {
                    addAll(publicChoices.map { it.option.entry })
                    add(
                        FirstNightInformationBundleEntry(
                            entryId = redHerringFactor.factorId,
                            control = redHerringFactor.control,
                            sourceChoiceId = redCandidate.candidateId,
                            profileExposure = redHerringFactor.profileExposure,
                        ),
                    )
                    addAll(representativeLatentEntries)
                }
                val baseBundle = FirstNightInformationBundle(
                    bundleId = "$candidateId-base",
                    entries = baseEntries,
                )
                val baseProjected = FirstNightPublicGoodInfoProjection.project(baseBundle)
                val queryDrafts = targetPairs.mapIndexed { pairIndex, pair ->
                    val targets = listOf(pair.first, pair.second)
                    val result = FortuneTellerInformationSemantics.healthyResult(
                        game = game,
                        targetSeats = targets,
                        redHerringSeat = redHerringSeat,
                    )
                    val fortuneObservation = EpistemicObservation(
                        observationId = "$candidateId-ft-${pair.first}-${pair.second}",
                        snapshotId = formal.snapshotId,
                        phase = StorytellerPhase.FIRST_NIGHT,
                        round = 1,
                        sequence = publicFactors.size + 1 + pairIndex,
                        sourceSeat = fortuneTellerSeat,
                        sourceAbility = fortuneTeller,
                        visibility = ObservationVisibility.PRIVATE,
                        recipientSeats = setOf(fortuneTellerSeat),
                        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                        proposition = InformationProposition.BooleanResult(
                            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                            sourceSeat = fortuneTellerSeat,
                            subjectSeats = targets,
                            value = result,
                        ),
                    )
                    val queryBundle = FirstNightInformationBundle(
                        bundleId = "$candidateId-query-${pair.first}-${pair.second}",
                        entries = baseEntries + FirstNightInformationBundleEntry(
                            entryId = "environment.fortune-teller-query",
                            control = FirstNightBundleEntryControl.RULE_DETERMINED,
                            observation = fortuneObservation,
                            profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                        ),
                    )
                    QueryDraft(
                        targets = targets,
                        result = result,
                        bundle = queryBundle,
                        projected = FirstNightPublicGoodInfoProjection.project(queryBundle),
                    )
                }
                val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                    validatedRuleset = validatedRuleset,
                    context = exactContext,
                    queries = queryDrafts.map { draft ->
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = draft.bundle.bundleId,
                            recipientSeat = anchorSeat,
                            observations = draft.projected,
                        )
                    },
                )
                require(exact is ExactHypotheticalObservationBundleEvaluation.Ready) {
                    "Stage 7A Fortune Teller pilot exact evaluation deferred for candidate $candidateId."
                }
                val diagnosticsById = exact.diagnostics.associateBy { it.bundleId }
                FirstNightFortuneTellerStorytellerCandidate(
                    candidateId = candidateId,
                    redHerringSeat = redHerringSeat,
                    publicChoices = publicChoices.associate { choice ->
                        choice.factor.audit.factorId to choice.option.optionId
                    }.toSortedMap(),
                    basePublicClaims = baseProjected.map { it.proposition.toString() },
                    representedCompleteBundleMultiplicity = latentMultiplicity,
                    robustnessCases = queryDrafts.map { draft ->
                        val diagnostic = diagnosticsById.getValue(draft.bundle.bundleId)
                        FirstNightFortuneTellerRobustnessCase(
                            targetSeats = draft.targets,
                            actualResult = draft.result,
                            diagnostics = FirstNightBeginnerDiagnostics(
                                recipientSeat = diagnostic.recipientSeat,
                                beforeWorldCount = diagnostic.before.value,
                                afterWorldCount = diagnostic.after.value,
                                demonCoverSize = diagnostic.afterStructure.demonCoverSize,
                                distinctEvilTeamConfigurationCount =
                                    diagnostic.afterStructure.distinctEvilTeamConfigurationCount,
                                forcedGoodSeats = diagnostic.afterStructure.forcedGoodSeats,
                                forcedEvilSeats = diagnostic.afterStructure.forcedEvilSeats,
                                evilCoverSize = diagnostic.afterStructure.evilCoverSize,
                            ),
                        )
                    },
                )
            }
        }

        return FirstNightFortuneTellerPilot(
            presetId = preset.id,
            seatingProfile = SEATING_PROFILE,
            seating = game.players.map { it.seat to it.actualRole },
            anchorRecipientSeat = anchorSeat,
            fortuneTellerSeat = fortuneTellerSeat,
            legalCompleteBundleCount = legalCompleteBundleCount,
            publicInformationCombinationCount = publicCombinations.size,
            redHerringCandidateCount = redHerringCandidates.size,
            latentMultiplicityPerDiagnosticCandidate = latentMultiplicity,
            targetPairCount = targetPairs.size,
            diagnosticStorytellerCandidateCount = storytellerCandidates.size,
            candidates = storytellerCandidates,
        )
    }

    fun renderMarkdown(pilot: FirstNightFortuneTellerPilot): String = buildString {
        appendLine("## Real 7-player Fortune Teller calibration — Stage 7A / COMPLETE CANDIDATE PILOT")
        appendLine()
        appendLine("Preset: `${pilot.presetId}` / `${pilot.seatingProfile}`")
        appendLine("Seating: " + pilot.seating.joinToString(" | ") { (seat, role) -> "$seat=${role.value}" })
        appendLine("Anchor recipient: seat ${pilot.anchorRecipientSeat}; Fortune Teller: seat ${pilot.fortuneTellerSeat}")
        appendLine(
            "Legal complete Storyteller bundles: ${pilot.legalCompleteBundleCount}; " +
                "public combinations: ${pilot.publicInformationCombinationCount}; " +
                "Red Herring candidates: ${pilot.redHerringCandidateCount}; " +
                "latent multiplicity per diagnostic candidate: ${pilot.latentMultiplicityPerDiagnosticCandidate}",
        )
        appendLine(
            "Diagnostic Storyteller candidates: ${pilot.diagnosticStorytellerCandidateCount}; " +
                "Fortune Teller target pairs per candidate: ${pilot.targetPairCount}.",
        )
        appendLine("Every target-pair consequence uses exact possible-world evaluation; no Badness gate or scalar score is applied.")
        appendLine()

        pilot.candidates.forEach { candidate ->
            appendLine("### ${candidate.candidateId} — Red Herring seat ${candidate.redHerringSeat}")
            appendLine()
            appendLine("Represents ${candidate.representedCompleteBundleMultiplicity} complete Storyteller bundles after latent quotienting.")
            appendLine(
                "Public choices: " + if (candidate.publicChoices.isEmpty()) "(none)" else
                    candidate.publicChoices.entries.joinToString(" | ") { (factor, option) -> "$factor=$option" },
            )
            appendLine("Base public claims:")
            if (candidate.basePublicClaims.isEmpty()) appendLine("- (none)")
            else candidate.basePublicClaims.forEach { appendLine("- $it") }
            appendLine("Actual Fortune Teller results across player choices: YES=${candidate.yesCaseCount}, NO=${candidate.noCaseCount}")
            appendLine()
            appendLine("| FT targets | result | AFTER | demonCover | evilConfigs | forcedGood | forcedEvil | evilCover |")
            appendLine("| --- | --- | ---: | ---: | ---: | --- | --- | ---: |")
            candidate.robustnessCases.forEach { case ->
                val d = case.diagnostics
                appendLine(
                    "| ${case.targetSeats.joinToString("+")} | ${if (case.actualResult) "YES" else "NO"} | " +
                        "${d.afterWorldCount} | ${d.demonCoverSize} | ${d.distinctEvilTeamConfigurationCount} | " +
                        "${d.forcedGoodSeats.sorted()} | ${d.forcedEvilSeats.sorted()} | ${d.evilCoverSize} |",
                )
            }
            appendLine()
        }
    }

    private fun materializePublicFactor(
        game: GameState,
        formal: FormalGameState,
        sequence: Int,
        factor: FirstNightBundleCandidateFactorAudit,
    ): MaterializedPublicFactor {
        require(factor.kind == FirstNightBundleCandidateFactorKind.PAIR_INFORMATION) {
            "Pilot $PILOT_PRESET_ID expects only pair-information PUBLIC_GOOD_INFO factors; found ${factor.kind}."
        }
        val sourceSeat = requireNotNull(factor.sourceSeat)
        val sourceRole = requireNotNull(game.playerAt(sourceSeat)).actualRole
        val options = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = sourceRole,
            roleDefinitions = roleDefinitions,
        ).map { candidate ->
            val information = candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().single()
            val observation = EpistemicObservation(
                observationId = "ft7a-private-$sequence-${candidate.candidateId}",
                snapshotId = formal.snapshotId,
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = sequence,
                sourceSeat = information.recipientSeat,
                sourceAbility = information.sourceAbility,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(information.recipientSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                    game = game,
                    information = information,
                    roleDefinitions = roleDefinitions,
                ),
            )
            MaterializedPublicOption(
                optionId = candidate.candidateId,
                entry = FirstNightInformationBundleEntry(
                    entryId = factor.factorId,
                    control = factor.control,
                    sourceChoiceId = candidate.candidateId.takeIf {
                        factor.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
                    },
                    observation = observation,
                    profileExposure = factor.profileExposure,
                ),
            )
        }.sortedBy { it.optionId }
        require(options.map { it.optionId } == factor.optionIds.sorted()) {
            "Pilot public-factor materialization drifted from canonical factor ${factor.factorId}."
        }
        return MaterializedPublicFactor(factor, options)
    }

    private fun cartesianPublicChoices(
        factors: List<MaterializedPublicFactor>,
    ): List<List<PublicChoice>> {
        if (factors.isEmpty()) return listOf(emptyList())
        var combinations: List<List<PublicChoice>> = listOf(emptyList())
        factors.forEach { factor ->
            combinations = combinations.flatMap { prefix ->
                factor.options.map { option -> prefix + PublicChoice(factor, option) }
            }
        }
        return combinations
    }

    private fun gameFor(preset: TroubleBrewingSetupPreset): GameState {
        val goodRoles = (preset.townsfolk + preset.outsiders).map(::canonicalRoleId)
        val minion = preset.minions.map(::canonicalRoleId).single()
        val demon = preset.demons.map(::canonicalRoleId).single()
        require(goodRoles.size == 5)
        require(fortuneTeller in goodRoles) {
            "Pilot preset $PILOT_PRESET_ID must contain Fortune Teller."
        }
        val librarian = RoleId("Librarian")
        require(librarian in goodRoles) {
            "Pilot preset $PILOT_PRESET_ID is expected to anchor Librarian at seat 1."
        }
        val orderedGood = listOf(librarian, fortuneTeller) +
            goodRoles.filterNot { it == librarian || it == fortuneTeller }.sortedBy(RoleId::value)
        val rolesBySeat = buildMap {
            orderedGood.forEachIndexed { index, role -> put(index + 1, role) }
            put(6, minion)
            put(7, demon)
        }
        val players = (1..PLAYER_COUNT).map { seat ->
            val role = definitionsById.getValue(rolesBySeat.getValue(seat))
            PlayerState(
                seat = seat,
                name = "P$seat",
                actualRole = role.id,
                actualAlignment = role.alignment,
                actualType = role.type,
                shownRole = role.id,
            )
        }
        return GameState(
            script = TroubleBrewingFixtures.scriptId,
            players = players,
            seed = 20260917L,
        )
    }

    private fun context(game: GameState): ExactHistoricalHypotheticalContext {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "fn-bundle-3-ft-stage-7a",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-3-ft-stage-7a",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        return ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = game.players.associate { it.seat to (it.shownRole ?: it.actualRole) },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roleDefinitions,
        )
    }

    private fun canonicalRoleId(externalId: String): RoleId =
        requireNotNull(validatedRuleset.characterRegistry.findByExternalId(externalId)) {
            "Validated preset unexpectedly references unknown role '$externalId'."
        }.id

    private fun presetActualExternalRoleIds(preset: TroubleBrewingSetupPreset): List<String> =
        preset.townsfolk + preset.outsiders + preset.minions + preset.demons

    private data class MaterializedPublicFactor(
        val audit: FirstNightBundleCandidateFactorAudit,
        val options: List<MaterializedPublicOption>,
    )

    private data class MaterializedPublicOption(
        val optionId: String,
        val entry: FirstNightInformationBundleEntry,
    )

    private data class PublicChoice(
        val factor: MaterializedPublicFactor,
        val option: MaterializedPublicOption,
    )

    private data class QueryDraft(
        val targets: List<Int>,
        val result: Boolean,
        val bundle: FirstNightInformationBundle,
        val projected: List<EpistemicObservation>,
    )

    private val STAGED_EXTERNAL_ROLE_IDS = setOf("drunk", "spy", "recluse", "poisoner")
}
