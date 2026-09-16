package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
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
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetValidator
import java.io.File
import java.math.BigInteger

data class FirstNightBeginnerRealPresetPoint(
    val pointId: String,
    val selectionLabel: String,
    val publicChoices: Map<String, String>,
    val publicClaims: List<String>,
    val diagnostics: FirstNightBeginnerDiagnostics,
)

data class FirstNightBeginnerRealPresetScenario(
    val presetId: String,
    val seatingProfile: String,
    val seating: List<Pair<Int, RoleId>>,
    val anchorRecipientSeat: Int,
    val legalCompleteBundleCount: BigInteger,
    val representedPublicProjectionUpperBound: BigInteger,
    val publicFactorCount: Int,
    val points: List<FirstNightBeginnerRealPresetPoint>,
)

data class FirstNightBeginnerRealPresetCalibration(
    val datasetId: String,
    val totalSevenPlayerPresetCount: Int,
    val eligibleHealthyPresetCount: Int,
    val excludedStagedPresetCount: Int,
    val scenarios: List<FirstNightBeginnerRealPresetScenario>,
)

/**
 * FN-BUNDLE-3 7A real-template calibration pilot.
 *
 * Production owns template parsing/validation and every legal information candidate. This builder
 * only chooses a deliberately small deterministic set of experiment points from those canonical
 * domains. Possible worlds are still evaluated exactly; only the number of real setup/public-bundle
 * scenarios sent to the exact evaluator is bounded.
 *
 * Stage 7A excludes Drunk, Spy, Recluse and Poisoner because the current healthy epistemic slice
 * deliberately stages their false-information/registration/impairment semantics. The first pilot
 * uses one explicit seating topology (evil adjacent) per eligible real 7-player preset. A second
 * topology is added only after this workload establishes runtime and diagnostic spread.
 */
object FirstNightBundleBeginnerRealPresetCalibrationBuilder {
    private const val PLAYER_COUNT = 7
    private const val SEATING_PROFILE = "EVIL_ADJACENT"
    private const val MAX_POINT_COUNT = 3

    private val stagedExternalRoleIds = setOf("drunk", "spy", "recluse", "poisoner")
    private val pairRoles = setOf(RoleId("Washerwoman"), RoleId("Librarian"), RoleId("Investigator"))
    private val numericRoles = setOf(RoleId("Chef"), RoleId("Empath"))
    private val preferredAnchorRoles = pairRoles + numericRoles

    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roleDefinitions.associateBy(RoleDefinition::id)

    fun build(): FirstNightBeginnerRealPresetCalibration {
        val dataset = TroubleBrewingSetupPresetJson.parse(
            File("src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json")
                .readText(Charsets.UTF_8),
        )
        TroubleBrewingSetupPresetValidator.validate(dataset, validatedRuleset.characterRegistry)

        val sevenPlayerPool = dataset.pools.getValue(PLAYER_COUNT)
        val eligible = sevenPlayerPool
            .filter(::isHealthyStageCompatible)
            .sortedBy(TroubleBrewingSetupPreset::id)

        return FirstNightBeginnerRealPresetCalibration(
            datasetId = dataset.datasetId,
            totalSevenPlayerPresetCount = sevenPlayerPool.size,
            eligibleHealthyPresetCount = eligible.size,
            excludedStagedPresetCount = sevenPlayerPool.size - eligible.size,
            scenarios = eligible.map(::buildScenario),
        )
    }

    fun renderMarkdown(calibration: FirstNightBeginnerRealPresetCalibration): String = buildString {
        appendLine("## Real 7-player preset calibration — Stage 7A / BOUNDED EXACT")
        appendLine()
        appendLine("Dataset: `${calibration.datasetId}`")
        appendLine(
            "7-player presets: ${calibration.totalSevenPlayerPresetCount}; " +
                "healthy-compatible: ${calibration.eligibleHealthyPresetCount}; " +
                "staged-out: ${calibration.excludedStagedPresetCount}",
        )
        appendLine("Staged-out roles: Drunk / Spy / Recluse / Poisoner.")
        appendLine(
            "Pilot topology: $SEATING_PROFILE. Up to $MAX_POINT_COUNT deterministic canonical public-bundle points " +
                "per preset; possible-world evaluation remains exact.",
        )
        appendLine("No Badness threshold or label is inferred from these diagnostics.")
        appendLine()

        calibration.scenarios.forEach { scenario ->
            appendLine("### ${scenario.presetId} / ${scenario.seatingProfile}")
            appendLine()
            appendLine("Anchor recipient: seat ${scenario.anchorRecipientSeat}")
            appendLine("Seating: " + scenario.seating.joinToString(" | ") { (seat, role) -> "$seat=${role.value}" })
            appendLine(
                "Legal complete bundles: ${scenario.legalCompleteBundleCount}; " +
                    "public projection upper bound: ${scenario.representedPublicProjectionUpperBound}; " +
                    "public factors: ${scenario.publicFactorCount}; evaluated points: ${scenario.points.size}",
            )
            appendLine()
            scenario.points.forEach { point ->
                appendLine("#### ${point.pointId} / ${point.selectionLabel}")
                appendLine()
                appendLine(
                    "Public choices: " + if (point.publicChoices.isEmpty()) {
                        "(none)"
                    } else {
                        point.publicChoices.entries.joinToString(" | ") { (factor, option) -> "$factor=$option" }
                    },
                )
                appendLine("Public claims:")
                if (point.publicClaims.isEmpty()) {
                    appendLine("- (none)")
                } else {
                    point.publicClaims.forEach { appendLine("- $it") }
                }
                val d = point.diagnostics
                appendLine(
                    "Diagnostics: BEFORE=${d.beforeWorldCount}, AFTER=${d.afterWorldCount}, " +
                        "demonCover=${d.demonCoverSize}, evilConfigs=${d.distinctEvilTeamConfigurationCount}, " +
                        "forcedGood=${d.forcedGoodSeats.sorted()}, forcedEvil=${d.forcedEvilSeats.sorted()}, " +
                        "evilCover=${d.evilCoverSize}",
                )
                appendLine()
            }
        }
    }

    private fun buildScenario(preset: TroubleBrewingSetupPreset): FirstNightBeginnerRealPresetScenario {
        val game = gameFor(preset)
        val anchorSeat = 1
        require(game.playerAt(anchorSeat)?.actualAlignment?.name == "GOOD") {
            "Real-preset calibration anchor must be good for ${preset.id}."
        }
        val context = context(preset.id, game)
        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(game, roleDefinitions)
        require(audit.deferredComplexities.isEmpty()) {
            "Stage 7A preset ${preset.id} unexpectedly contains deferred complexity ${audit.deferredComplexities}."
        }
        val legalCompleteBundleCount = requireNotNull(audit.legalCompleteBundleCount)
        val materialized = audit.factors.mapIndexed { index, factor ->
            materializeFactor(
                game = game,
                formal = formal,
                sequence = index + 1,
                factor = factor,
            )
        }
        val publicFactors = materialized.filter {
            it.audit.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO
        }
        val latentFactors = materialized.filter {
            it.audit.profileExposure == FirstNightBundleProfileExposure.NOT_SHARED
        }

        val pointBundles = selectionLabels(publicFactors).mapIndexed { pointIndex, label ->
            val selectedPublic = publicFactors.map { factor ->
                factor to factor.options[optionIndex(factor.options.size, label)]
            }
            val entries = buildList {
                selectedPublic.forEach { (_, option) -> add(option.entry) }
                latentFactors.forEach { factor -> add(factor.options.first().entry) }
            }
            val bundleId = "fn3-real-${stableIdPart(preset.id)}-${label.lowercase()}"
            val bundle = FirstNightInformationBundle(bundleId = bundleId, entries = entries)
            val projected = FirstNightPublicGoodInfoProjection.project(bundle)
            RealPresetPointDraft(
                pointId = "point-${pointIndex + 1}",
                selectionLabel = label,
                bundle = bundle,
                publicChoices = selectedPublic.associate { (factor, option) ->
                    factor.audit.factorId to option.optionId
                }.toSortedMap(),
                projected = projected,
            )
        }.distinctBy { draft ->
            draft.projected.map { observation -> observation.proposition.toString() }
        }

        val exactCounterworldRoles = roleDefinitions.filterNot { definition ->
            definition.id in STAGED_COUNTERWORLD_ROLES
        }
        val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context.copy(roleDefinitions = exactCounterworldRoles),
            queries = pointBundles.map { draft ->
                ExactHypotheticalObservationBundleQuery(
                    bundleId = draft.bundle.bundleId,
                    recipientSeat = anchorSeat,
                    observations = draft.projected,
                )
            },
        )
        require(exact is ExactHypotheticalObservationBundleEvaluation.Ready) {
            "Real-preset Stage 7A exact evaluation deferred for ${preset.id}."
        }
        val diagnosticsById = exact.diagnostics.associateBy { it.bundleId }

        return FirstNightBeginnerRealPresetScenario(
            presetId = preset.id,
            seatingProfile = SEATING_PROFILE,
            seating = game.players.map { it.seat to it.actualRole },
            anchorRecipientSeat = anchorSeat,
            legalCompleteBundleCount = legalCompleteBundleCount,
            representedPublicProjectionUpperBound = audit.representedPublicProjectionUpperBound,
            publicFactorCount = publicFactors.size,
            points = pointBundles.map { draft ->
                val diagnostic = diagnosticsById.getValue(draft.bundle.bundleId)
                FirstNightBeginnerRealPresetPoint(
                    pointId = draft.pointId,
                    selectionLabel = draft.selectionLabel,
                    publicChoices = draft.publicChoices,
                    publicClaims = draft.projected.map { it.proposition.toString() },
                    diagnostics = FirstNightBeginnerDiagnostics(
                        recipientSeat = diagnostic.recipientSeat,
                        beforeWorldCount = diagnostic.before.value,
                        afterWorldCount = diagnostic.after.value,
                        demonCoverSize = diagnostic.afterStructure.demonCoverSize,
                        distinctEvilTeamConfigurationCount = diagnostic.afterStructure.distinctEvilTeamConfigurationCount,
                        forcedGoodSeats = diagnostic.afterStructure.forcedGoodSeats,
                        forcedEvilSeats = diagnostic.afterStructure.forcedEvilSeats,
                        evilCoverSize = diagnostic.afterStructure.evilCoverSize,
                    ),
                )
            },
        )
    }

    private fun materializeFactor(
        game: GameState,
        formal: FormalGameState,
        sequence: Int,
        factor: FirstNightBundleCandidateFactorAudit,
    ): MaterializedFactor {
        val options = when (factor.kind) {
            FirstNightBundleCandidateFactorKind.PAIR_INFORMATION -> {
                val sourceSeat = requireNotNull(factor.sourceSeat)
                val sourceRole = requireNotNull(game.playerAt(sourceSeat)).actualRole
                NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
                    game = game,
                    sourceSeat = sourceSeat,
                    abilityRole = sourceRole,
                    roleDefinitions = roleDefinitions,
                ).map { candidate ->
                    val information = candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().single()
                    materializedInformationOption(
                        game = game,
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
                val sourceRole = requireNotNull(game.playerAt(sourceSeat)).actualRole
                FirstNightNumericInformationSemantics.healthyTruthValues(game, sourceSeat)
                    .toSortedSet()
                    .map { value ->
                        materializedInformationOption(
                            game = game,
                            formal = formal,
                            sequence = sequence,
                            factor = factor,
                            optionId = "value-$value",
                            information = EffectDraft.PlayerInformation(
                                recipientSeat = sourceSeat,
                                sourceAbility = sourceRole,
                                value = InformationValue.Number(value),
                            ),
                        )
                    }
            }
            FirstNightBundleCandidateFactorKind.RED_HERRING ->
                SetupCandidateGenerator.generateRedHerringCandidates(game).map { candidate ->
                    MaterializedOption(
                        optionId = candidate.candidateId,
                        entry = FirstNightInformationBundleEntry(
                            entryId = factor.factorId,
                            control = factor.control,
                            sourceChoiceId = candidate.candidateId,
                            profileExposure = factor.profileExposure,
                        ),
                    )
                }
            FirstNightBundleCandidateFactorKind.DEMON_BLUFFS ->
                SetupCandidateGenerator.generateDemonBluffCandidates(game, roleDefinitions).map { candidate ->
                    MaterializedOption(
                        optionId = candidate.candidateId,
                        entry = FirstNightInformationBundleEntry(
                            entryId = factor.factorId,
                            control = factor.control,
                            sourceChoiceId = candidate.candidateId,
                            profileExposure = factor.profileExposure,
                        ),
                    )
                }
        }.sortedBy(MaterializedOption::optionId)

        require(options.map(MaterializedOption::optionId) == factor.optionIds.sorted()) {
            "Real-preset materialization drifted from canonical factor ${factor.factorId}."
        }
        require(options.isNotEmpty())
        return MaterializedFactor(audit = factor, options = options)
    }

    private fun materializedInformationOption(
        game: GameState,
        formal: FormalGameState,
        sequence: Int,
        factor: FirstNightBundleCandidateFactorAudit,
        optionId: String,
        information: EffectDraft.PlayerInformation,
    ): MaterializedOption {
        val observation = EpistemicObservation(
            observationId = "fn3-real-private-${sequence}-${stableIdPart(optionId)}",
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
        return MaterializedOption(
            optionId = optionId,
            entry = FirstNightInformationBundleEntry(
                entryId = factor.factorId,
                control = factor.control,
                sourceChoiceId = optionId.takeIf {
                    factor.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
                },
                observation = observation,
                profileExposure = factor.profileExposure,
            ),
        )
    }

    private fun gameFor(preset: TroubleBrewingSetupPreset): GameState {
        val goodRoleIds = (preset.townsfolk + preset.outsiders)
            .map(::canonicalRoleId)
        val minionRoleIds = preset.minions.map(::canonicalRoleId)
        val demonRoleIds = preset.demons.map(::canonicalRoleId)
        require(goodRoleIds.size == 5 && minionRoleIds.size == 1 && demonRoleIds.size == 1) {
            "Validated 7-player Trouble Brewing preset ${preset.id} must resolve to 5 good / 1 minion / 1 demon."
        }

        val anchorRole = goodRoleIds
            .filter { it in preferredAnchorRoles }
            .minByOrNull(RoleId::value)
            ?: goodRoleIds.minBy(RoleId::value)
        val orderedGoodRoles = listOf(anchorRole) +
            goodRoleIds.filterNot { it == anchorRole }.sortedBy(RoleId::value)
        val rolesBySeat = orderedGoodRoles + minionRoleIds.single() + demonRoleIds.single()

        val players = rolesBySeat.mapIndexed { index, roleId ->
            val role = requireNotNull(definitionsById[roleId]) {
                "Fixture role definitions are missing canonical role ${roleId.value}."
            }
            PlayerState(
                seat = index + 1,
                name = "P${index + 1}",
                actualRole = role.id,
                actualAlignment = role.alignment,
                actualType = role.type,
                shownRole = role.id,
            )
        }
        return GameState(
            script = TroubleBrewingFixtures.scriptId,
            players = players,
            seed = 20260917L + preset.id.hashCode(),
        )
    }

    private fun context(
        presetId: String,
        game: GameState,
    ): ExactHistoricalHypotheticalContext {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "fn-bundle-3-real-preset-pilot",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-3-real-${stableIdPart(presetId)}",
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
            perceivedRolesBySeat = game.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roleDefinitions,
        )
    }

    private fun isHealthyStageCompatible(preset: TroubleBrewingSetupPreset): Boolean =
        presetActualExternalRoleIds(preset).none { it in stagedExternalRoleIds }

    private fun presetActualExternalRoleIds(preset: TroubleBrewingSetupPreset): List<String> =
        preset.townsfolk + preset.outsiders + preset.minions + preset.demons

    private fun canonicalRoleId(externalId: String): RoleId =
        requireNotNull(validatedRuleset.characterRegistry.findByExternalId(externalId)) {
            "Validated preset unexpectedly references unknown role '$externalId'."
        }.id

    private fun selectionLabels(publicFactors: List<MaterializedFactor>): List<String> =
        if (publicFactors.isEmpty()) listOf("FIRST") else listOf("FIRST", "MIDDLE", "LAST")

    private fun optionIndex(size: Int, selectionLabel: String): Int = when (selectionLabel) {
        "FIRST" -> 0
        "MIDDLE" -> (size - 1) / 2
        "LAST" -> size - 1
        else -> error("Unknown real-preset selection label $selectionLabel")
    }

    private fun stableIdPart(raw: String): String = raw
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "id" }

    private data class MaterializedFactor(
        val audit: FirstNightBundleCandidateFactorAudit,
        val options: List<MaterializedOption>,
    )

    private data class MaterializedOption(
        val optionId: String,
        val entry: FirstNightInformationBundleEntry,
    )

    private data class RealPresetPointDraft(
        val pointId: String,
        val selectionLabel: String,
        val bundle: FirstNightInformationBundle,
        val publicChoices: Map<String, String>,
        val projected: List<EpistemicObservation>,
    )

    private val STAGED_COUNTERWORLD_ROLES = setOf(
        RoleId("Drunk"),
        RoleId("Spy"),
        RoleId("Recluse"),
        RoleId("Poisoner"),
    )
}
