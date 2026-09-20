package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealPlanner
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetValidator
import java.io.File

internal enum class Sde2D5FHealthyPairTruthBasis {
    ACTUAL_ROLE,
    SPY_REGISTRATION,
    RECLUSE_REGISTRATION,
}

internal enum class Sde2D5FFixedNumericContextBasis {
    RULE_DETERMINED,
    FIXED_REGISTRATION_BRANCH_NOT_OPTIMIZED,
}

internal data class Sde2D5FRepresentativeSeatMeaning(
    val seat: Int,
    val actualRole: RoleId,
    val shownRole: RoleId,
    val poisoned: Boolean,
)

internal data class Sde2D5FFixedNumericContext(
    val sourceSeat: Int,
    val sourceRole: RoleId,
    val legalHealthyValues: List<Int>,
    val fixedValue: Int,
    val basis: Sde2D5FFixedNumericContextBasis,
)

internal data class Sde2D5FHealthyPairCandidateMeaning(
    val candidateId: String,
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
    val anchorSeat: Int?,
    val decoySeat: Int?,
    val truthBasis: Sde2D5FHealthyPairTruthBasis,
    val tableMeaning: String,
)

internal data class Sde2D5FRepresentativeHealthyScenario(
    val scenarioId: String,
    val presetId: String,
    val playerCount: Int,
    val complexity: String,
    val source: String,
    val styleTags: List<String>,
    val gameSeed: Long,
    val seating: List<Sde2D5FRepresentativeSeatMeaning>,
    val poisonerTargetSeat: Int?,
    val activeSourceSeat: Int,
    val activeRole: RoleId,
    val fixedNumericContext: List<Sde2D5FFixedNumericContext>,
    val candidates: List<Sde2D5FHealthyPairCandidateMeaning>,
)

internal data class Sde2D5FRepresentativeHealthyInformationCorpus(
    val datasetId: String,
    val scenarios: List<Sde2D5FRepresentativeHealthyScenario>,
)

/**
 * First representative D5F healthy-information materialization surface.
 *
 * This owner deliberately stops before policy labels or gate derivation. It selects known
 * beginner/standard presets from the production dataset, uses the production deal planner for a
 * deterministic committed seating, freezes non-SDE Poisoner context where needed, and enumerates
 * healthy Washerwoman/Librarian/Investigator outputs through the production legality owner.
 *
 * The output is intentionally table-readable. Strategic diagnostics are attached in a later slice
 * to these same scenario/candidate identities rather than replacing their semantic meaning.
 */
internal object Sde2D5FRepresentativeHealthyInformationCorpusBuilder {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roleDefinitions.associateBy(RoleDefinition::id)
    private val chef = RoleId("Chef")
    private val empath = RoleId("Empath")
    private val numericRoles = setOf(chef, empath)

    private val scenarioSpecs = listOf(
        ScenarioSpec(
            presetId = "TB2_7_015",
            activeRoleExternalId = "washerwoman",
            gameSeed = 202609200715L,
        ),
        ScenarioSpec(
            presetId = "TB2_7_037",
            activeRoleExternalId = "librarian",
            gameSeed = 202609200737L,
        ),
        ScenarioSpec(
            presetId = "TB2_7_006",
            activeRoleExternalId = "investigator",
            gameSeed = 202609200706L,
        ),
        ScenarioSpec(
            presetId = "TB2_8_010",
            activeRoleExternalId = "washerwoman",
            gameSeed = 202609200810L,
            fixedPoisonTargetExternalId = "monk",
        ),
        ScenarioSpec(
            presetId = "TB2_8_012",
            activeRoleExternalId = "investigator",
            gameSeed = 202609200812L,
        ),
        ScenarioSpec(
            presetId = "TB2_9_008",
            activeRoleExternalId = "librarian",
            gameSeed = 202609200908L,
            fixedPoisonTargetExternalId = "monk",
        ),
        ScenarioSpec(
            presetId = "TB2_9_031",
            activeRoleExternalId = "washerwoman",
            gameSeed = 202609200931L,
            fixedPoisonTargetExternalId = "monk",
        ),
    )

    fun build(): Sde2D5FRepresentativeHealthyInformationCorpus {
        val dataset = TroubleBrewingSetupPresetJson.parse(
            File("src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json")
                .readText(Charsets.UTF_8),
        )
        TroubleBrewingSetupPresetValidator.validate(
            dataset,
            validatedRuleset.characterRegistry,
        )
        val presetsById = dataset.pools.values.flatten().associateBy(TroubleBrewingSetupPreset::id)

        return Sde2D5FRepresentativeHealthyInformationCorpus(
            datasetId = dataset.datasetId,
            scenarios = scenarioSpecs.map { spec ->
                buildScenario(
                    datasetId = dataset.datasetId,
                    schemaVersion = dataset.schemaVersion,
                    preset = requireNotNull(presetsById[spec.presetId]) {
                        "Representative healthy-information preset ${spec.presetId} is missing."
                    },
                    spec = spec,
                )
            },
        )
    }

    private fun buildScenario(
        datasetId: String,
        schemaVersion: Int,
        preset: TroubleBrewingSetupPreset,
        spec: ScenarioSpec,
    ): Sde2D5FRepresentativeHealthyScenario {
        require(preset.playerCount in 7..9)
        require(preset.complexity == "beginner" || preset.complexity == "standard") {
            "Representative healthy-information corpus must use beginner/standard presets."
        }
        require("drunk" !in preset.outsiders) {
            "First representative materialization slice deliberately keeps Drunk identity out of scope."
        }
        require(spec.activeRoleExternalId in preset.townsfolk) {
            "Preset ${preset.id} does not contain active role ${spec.activeRoleExternalId}."
        }
        spec.fixedPoisonTargetExternalId?.let { targetRole ->
            require("poisoner" in preset.minions) {
                "Fixed Poisoner target is valid only when Poisoner is in play."
            }
            require(targetRole in preset.townsfolk + preset.outsiders + preset.minions + preset.demons) {
                "Fixed Poisoner target role $targetRole is not in preset ${preset.id}."
            }
            require(targetRole != spec.activeRoleExternalId) {
                "Representative healthy source must not be the fixed Poisoner target."
            }
        }

        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = spec.gameSeed,
            preset = preset,
            selectedDrunkShownRole = null,
        )
        val deal = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
            orderedPlayerNames = (1..preset.playerCount).map { seat -> "P$seat" },
        )
        val poisonerTargetSeat = spec.fixedPoisonTargetExternalId?.let { targetRole ->
            deal.assignments.single { assignment -> assignment.actualRoleId == targetRole }.seat
        }
        val players = deal.assignments.map { assignment ->
            val actual = requireNotNull(
                validatedRuleset.characterRegistry.findByExternalId(assignment.actualRoleId),
            )
            val shown = requireNotNull(
                validatedRuleset.characterRegistry.findByExternalId(assignment.shownRoleId),
            )
            val definition = definitionsById.getValue(actual.id)
            PlayerState(
                seat = assignment.seat,
                name = assignment.playerName,
                actualRole = actual.id,
                actualAlignment = definition.alignment,
                actualType = definition.type,
                shownRole = shown.id,
                poisoned = assignment.seat == poisonerTargetSeat,
            )
        }
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            players = players,
            seed = spec.gameSeed,
        )
        val activeRole = requireNotNull(
            validatedRuleset.characterRegistry.findByExternalId(spec.activeRoleExternalId),
        ).id
        val source = game.players.single { player -> player.actualRole == activeRole }
        require(!source.poisoned) {
            "Representative healthy-information source must be functioning."
        }

        val candidates = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = source.seat,
            abilityRole = activeRole,
            roleDefinitions = roleDefinitions,
        )
        require(candidates.isNotEmpty()) {
            "Representative scenario ${preset.id} must expose at least one legal healthy clue."
        }

        val fixedNumeric = game.players
            .filter { player -> player.actualRole in numericRoles && !player.poisoned }
            .sortedBy(PlayerState::seat)
            .map { player ->
                val legalValues = FirstNightNumericInformationSemantics
                    .healthyTruthValues(game, player.seat)
                    .sorted()
                require(legalValues.isNotEmpty()) {
                    "Healthy numeric context at seat ${player.seat} has no legal truth value."
                }
                Sde2D5FFixedNumericContext(
                    sourceSeat = player.seat,
                    sourceRole = player.actualRole,
                    legalHealthyValues = legalValues,
                    fixedValue = legalValues.first(),
                    basis = if (legalValues.size == 1) {
                        Sde2D5FFixedNumericContextBasis.RULE_DETERMINED
                    } else {
                        Sde2D5FFixedNumericContextBasis.FIXED_REGISTRATION_BRANCH_NOT_OPTIMIZED
                    },
                )
            }

        return Sde2D5FRepresentativeHealthyScenario(
            scenarioId = "d5f-healthy-${preset.id.lowercase()}-${spec.activeRoleExternalId}",
            presetId = preset.id,
            playerCount = preset.playerCount,
            complexity = preset.complexity,
            source = preset.source,
            styleTags = preset.styleTags,
            gameSeed = spec.gameSeed,
            seating = game.players.sortedBy(PlayerState::seat).map { player ->
                Sde2D5FRepresentativeSeatMeaning(
                    seat = player.seat,
                    actualRole = player.actualRole,
                    shownRole = requireNotNull(player.shownRole),
                    poisoned = player.poisoned,
                )
            },
            poisonerTargetSeat = poisonerTargetSeat,
            activeSourceSeat = source.seat,
            activeRole = activeRole,
            fixedNumericContext = fixedNumeric,
            candidates = candidates
                .sortedBy { candidate -> candidate.candidateId }
                .map { candidate ->
                    val outcome = candidate.outcome
                    val truthBasis = when {
                        "spy-registration" in candidate.metadata.tags ->
                            Sde2D5FHealthyPairTruthBasis.SPY_REGISTRATION
                        "recluse-registration" in candidate.metadata.tags ->
                            Sde2D5FHealthyPairTruthBasis.RECLUSE_REGISTRATION
                        else -> Sde2D5FHealthyPairTruthBasis.ACTUAL_ROLE
                    }
                    Sde2D5FHealthyPairCandidateMeaning(
                        candidateId = candidate.candidateId,
                        shownRole = outcome.shownRole,
                        candidateSeats = outcome.candidateSeats,
                        anchorSeat = outcome.targetSeat,
                        decoySeat = outcome.decoySeat,
                        truthBasis = truthBasis,
                        tableMeaning = tableMeaning(
                            sourceSeat = source.seat,
                            sourceRole = activeRole,
                            shownRole = outcome.shownRole,
                            candidateSeats = outcome.candidateSeats,
                            anchorSeat = outcome.targetSeat,
                            decoySeat = outcome.decoySeat,
                            truthBasis = truthBasis,
                        ),
                    )
                },
        )
    }

    private fun tableMeaning(
        sourceSeat: Int,
        sourceRole: RoleId,
        shownRole: RoleId?,
        candidateSeats: List<Int>,
        anchorSeat: Int?,
        decoySeat: Int?,
        truthBasis: Sde2D5FHealthyPairTruthBasis,
    ): String {
        if (shownRole == null) {
            return "Seat $sourceSeat ${sourceRole.value}: show that no Outsiders are in play."
        }
        return "Seat $sourceSeat ${sourceRole.value}: show ${shownRole.value}; " +
            "tell them one of seats ${candidateSeats.joinToString("/")} is ${shownRole.value}; " +
            "truth/registration anchor seat=$anchorSeat, decoy seat=$decoySeat, " +
            "truth basis=${truthBasis.name}."
    }

    private data class ScenarioSpec(
        val presetId: String,
        val activeRoleExternalId: String,
        val gameSeed: Long,
        val fixedPoisonTargetExternalId: String? = null,
    )
}

internal object Sde2D5FRepresentativeHealthyInformationCorpusRenderer {
    fun renderMarkdown(
        corpus: Sde2D5FRepresentativeHealthyInformationCorpus,
    ): String = buildString {
        appendLine("# SDE-2D5F representative healthy-information materialization")
        appendLine()
        appendLine("Dataset: `${corpus.datasetId}`")
        appendLine("This report is calibration material only. No label, gate, or threshold is inferred.")
        appendLine()

        corpus.scenarios.forEach { scenario ->
            appendLine("## ${scenario.scenarioId}")
            appendLine()
            appendLine(
                "Preset=`${scenario.presetId}`; players=${scenario.playerCount}; " +
                    "complexity=${scenario.complexity}; source=${scenario.source}",
            )
            appendLine("Style tags: ${scenario.styleTags.joinToString(", ")}")
            appendLine(
                "Active Storyteller control: seat ${scenario.activeSourceSeat} " +
                    scenario.activeRole.value,
            )
            scenario.poisonerTargetSeat?.let { targetSeat ->
                appendLine(
                    "Fixed external context: Poisoner targets seat $targetSeat " +
                        "(Evil-player-controlled; not optimized by this corpus).",
                )
            }
            appendLine()
            appendLine("### Table seating")
            appendLine()
            appendLine("| Seat | Actual role | Shown role | State |")
            appendLine("|---:|---|---|---|")
            scenario.seating.forEach { seat ->
                appendLine(
                    "| ${seat.seat} | ${seat.actualRole.value} | ${seat.shownRole.value} | " +
                        if (seat.poisoned) "POISONED |" else "healthy |",
                )
            }
            appendLine()
            appendLine("### Fixed Chef / Empath context")
            appendLine()
            if (scenario.fixedNumericContext.isEmpty()) {
                appendLine("- none")
            } else {
                scenario.fixedNumericContext.forEach { context ->
                    appendLine(
                        "- Seat ${context.sourceSeat} ${context.sourceRole.value}: fixed value " +
                            "${context.fixedValue}; legal healthy values=${context.legalHealthyValues}; " +
                            "basis=${context.basis}.",
                    )
                }
            }
            appendLine()
            appendLine("### Legal Storyteller options")
            appendLine()
            appendLine("| # | Shown role | Pair | Truth basis | Table meaning | Candidate ID |")
            appendLine("|---:|---|---|---|---|---|")
            scenario.candidates.forEachIndexed { index, candidate ->
                appendLine(
                    "| ${index + 1} | ${candidate.shownRole?.value ?: "NO_OUTSIDER"} | " +
                        "${candidate.candidateSeats.joinToString("/").ifBlank { "-" }} | " +
                        "${candidate.truthBasis} | ${candidate.tableMeaning} | " +
                        "`${candidate.candidateId}` |",
                )
            }
            appendLine()
        }
    }
}
