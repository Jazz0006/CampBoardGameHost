package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightHealthyBundleHarnessEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightProjectedSignatureGroup
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightHealthyBundleHarness
import java.io.File
import java.math.BigInteger

enum class FirstNightBeginnerCorpusPartition { CALIBRATION, HOLDOUT }

enum class FirstNightBeginnerCorpusLabel {
    UNREVIEWED,
    BAD_TOO_STRONG,
    ACCEPTABLE,
    BAD_TOO_WEAK,
    UNCERTAIN,
}

enum class FirstNightBeginnerSelectionReason {
    LOWEST_AFTER_WORLD_COUNT,
    HIGHEST_AFTER_WORLD_COUNT,
    SMALLEST_DEMON_COVER,
    FEWEST_EVIL_CONFIGURATIONS,
    MOST_FORCED_GOOD,
    LARGEST_LEAVE_ONE_OUT_RECOVERY,
    LOWER_QUARTILE_AFTER,
    UPPER_QUARTILE_AFTER,
    BOUNDED_LOW_INFORMATION_SAMPLE,
}

data class FirstNightBeginnerDiagnostics(
    val recipientSeat: Int,
    val beforeWorldCount: BigInteger,
    val afterWorldCount: BigInteger,
    val demonCoverSize: Int,
    val distinctEvilTeamConfigurationCount: Int,
    val forcedGoodSeats: Set<Int>,
    val forcedEvilSeats: Set<Int>,
    val evilCoverSize: Int,
)

data class FirstNightBeginnerLeaveOneOutDiagnostics(
    val omittedObservation: String,
    val diagnostics: FirstNightBeginnerDiagnostics,
)

data class FirstNightBeginnerCorpusItem(
    val itemId: String,
    val scenarioId: String,
    val partition: FirstNightBeginnerCorpusPartition,
    val label: FirstNightBeginnerCorpusLabel,
    val signatureId: String,
    val multiplicity: BigInteger,
    val selectionReasons: Set<FirstNightBeginnerSelectionReason>,
    val publicObservations: List<String>,
    val anchorDiagnostics: FirstNightBeginnerDiagnostics,
    val anchorLeaveOneOut: List<FirstNightBeginnerLeaveOneOutDiagnostics>,
)

data class FirstNightBeginnerCorpusScenario(
    val scenarioId: String,
    val partition: FirstNightBeginnerCorpusPartition,
    val anchorRecipientSeat: Int,
    val seating: List<Pair<Int, RoleId>>,
    val rawCompleteBundleCount: BigInteger,
    val distinctProjectedSignatureCount: Int,
    val items: List<FirstNightBeginnerCorpusItem>,
)

data class FirstNightBeginnerCorpus(
    val scenarios: List<FirstNightBeginnerCorpusScenario>,
) {
    val items: List<FirstNightBeginnerCorpusItem> get() = scenarios.flatMap { it.items }
}

/**
 * FN-BUNDLE-3 calibration corpus infrastructure.
 *
 * Calibration and holdout are intentionally different execution phases. Calibration generation must
 * not evaluate the sealed holdout at all; a fresh holdout is evaluated only after candidate gates are
 * frozen. This prevents both information leakage and needless exact-evaluator cost.
 */
object FirstNightBundleBeginnerCorpusBuilder {
    const val SEALED_HOLDOUT_SCENARIO_COUNT: Int = 1

    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsByName = roleDefinitions.associateBy { it.id.value }

    private data class ScenarioDefinition(
        val id: String,
        val roleNamesBySeat: List<String>,
        val anchorRecipientSeat: Int,
    )

    private val exhaustiveCalibrationDefinitions = listOf(
        ScenarioDefinition(
            id = "cal-pair-rich-adjacent-evil",
            roleNamesBySeat = listOf(
                "Washerwoman",
                "Chef",
                "Empath",
                "Fortune Teller",
                "Investigator",
                "Scarlet Woman",
                "Imp",
            ),
            anchorRecipientSeat = 1,
        ),
    )

    /** Exhaustive calibration currently contains only the interaction-rich scenario. */
    fun buildCalibration(): FirstNightBeginnerCorpus = FirstNightBeginnerCorpus(
        scenarios = exhaustiveCalibrationDefinitions.map(::buildScenario),
    )

    fun renderMarkdown(
        corpus: FirstNightBeginnerCorpus,
        sealedHoldoutScenarioCount: Int = SEALED_HOLDOUT_SCENARIO_COUNT,
    ): String = buildString {
        require(corpus.scenarios.all { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION }) {
            "Calibration review export must not contain holdout scenarios."
        }
        require(sealedHoldoutScenarioCount >= 0)

        appendLine("FN_BUNDLE_3_CORPUS_START")
        appendLine("# FN-BUNDLE-3 BEGINNER calibration review corpus")
        appendLine()
        appendLine("All labels are UNREVIEWED. Diagnostics are evidence for human review, not thresholds.")
        appendLine(
            "Sealed holdout scenarios: $sealedHoldoutScenarioCount. " +
                "Holdout diagnostics are not evaluated during calibration.",
        )
        appendLine()
        corpus.scenarios.forEach { scenario ->
            appendLine("## ${scenario.scenarioId} — ${scenario.partition}")
            appendLine()
            appendLine("Anchor recipient: seat ${scenario.anchorRecipientSeat}")
            appendLine("Seating: " + scenario.seating.joinToString(" | ") { (seat, role) -> "$seat=${role.value}" })
            appendLine("Raw bundles: ${scenario.rawCompleteBundleCount}; signatures: ${scenario.distinctProjectedSignatureCount}")
            appendLine()
            scenario.items.forEach { item ->
                appendLine("### ${item.itemId}")
                appendLine()
                appendLine("Reasons: ${item.selectionReasons.joinToString()}; label: ${item.label}; multiplicity: ${item.multiplicity}")
                appendLine()
                appendLine("Public claims:")
                item.publicObservations.forEach { observation -> appendLine("- $observation") }
                appendLine()
                val d = item.anchorDiagnostics
                appendLine(
                    "Anchor diagnostics: BEFORE=${d.beforeWorldCount}, AFTER=${d.afterWorldCount}, " +
                        "demonCover=${d.demonCoverSize}, evilConfigs=${d.distinctEvilTeamConfigurationCount}, " +
                        "forcedGood=${d.forcedGoodSeats.sorted()}, forcedEvil=${d.forcedEvilSeats.sorted()}, " +
                        "evilCover=${d.evilCoverSize}",
                )
                if (item.anchorLeaveOneOut.isNotEmpty()) {
                    appendLine()
                    appendLine("Anchor leave-one-out evidence:")
                    item.anchorLeaveOneOut.forEach { loo ->
                        val ld = loo.diagnostics
                        appendLine(
                            "- omit `${loo.omittedObservation}` -> AFTER ${ld.afterWorldCount}, " +
                                "demonCover=${ld.demonCoverSize}, evilConfigs=${ld.distinctEvilTeamConfigurationCount}, " +
                                "forcedGood=${ld.forcedGoodSeats.sorted()}, forcedEvil=${ld.forcedEvilSeats.sorted()}, " +
                                "evilCover=${ld.evilCoverSize}",
                        )
                    }
                }
                appendLine()
            }
        }
        appendLine("FN_BUNDLE_3_CORPUS_END")
    }

    private fun buildScenario(definition: ScenarioDefinition): FirstNightBeginnerCorpusScenario {
        val game = game(definition)
        require(game.playerAt(definition.anchorRecipientSeat)?.actualAlignment?.name == "GOOD") {
            "Corpus anchor must be a good recipient in ${definition.id}."
        }
        val evaluation = TroubleBrewingFirstNightHealthyBundleHarness.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(definition.id, game),
            evaluationRecipientSeats = setOf(definition.anchorRecipientSeat),
        )
        require(evaluation is FirstNightHealthyBundleHarnessEvaluation.Ready) {
            "FN-BUNDLE-3 calibration scenario ${definition.id} must be supported by the healthy harness."
        }
        val selected = selectReviewGroups(evaluation.signatureGroups)

        return FirstNightBeginnerCorpusScenario(
            scenarioId = definition.id,
            partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
            anchorRecipientSeat = definition.anchorRecipientSeat,
            seating = game.players.map { it.seat to it.actualRole },
            rawCompleteBundleCount = evaluation.rawCompleteBundleCount,
            distinctProjectedSignatureCount = evaluation.distinctProjectedSignatureCount,
            items = selected.map { (group, reasons) ->
                val anchor = group.recipientDiagnostics.single()
                FirstNightBeginnerCorpusItem(
                    itemId = "${definition.id}:${group.signatureId}",
                    scenarioId = definition.id,
                    partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
                    label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
                    signatureId = group.signatureId,
                    multiplicity = group.multiplicity,
                    selectionReasons = reasons,
                    publicObservations = group.publicObservations.map(::reviewObservation),
                    anchorDiagnostics = FirstNightBeginnerDiagnostics(
                        recipientSeat = definition.anchorRecipientSeat,
                        beforeWorldCount = anchor.before.value,
                        afterWorldCount = anchor.after.value,
                        demonCoverSize = anchor.afterStructure.demonCoverSize,
                        distinctEvilTeamConfigurationCount = anchor.afterStructure.distinctEvilTeamConfigurationCount,
                        forcedGoodSeats = anchor.afterStructure.forcedGoodSeats,
                        forcedEvilSeats = anchor.afterStructure.forcedEvilSeats,
                        evilCoverSize = anchor.afterStructure.evilCoverSize,
                    ),
                    anchorLeaveOneOut = group.leaveOneOutDiagnostics.mapIndexed { index, loo ->
                        val diagnostics = loo.recipientDiagnostics.single()
                        FirstNightBeginnerLeaveOneOutDiagnostics(
                            omittedObservation = reviewObservation(group.publicObservations[index]),
                            diagnostics = FirstNightBeginnerDiagnostics(
                                recipientSeat = definition.anchorRecipientSeat,
                                beforeWorldCount = diagnostics.before.value,
                                afterWorldCount = diagnostics.after.value,
                                demonCoverSize = diagnostics.afterStructure.demonCoverSize,
                                distinctEvilTeamConfigurationCount = diagnostics.afterStructure.distinctEvilTeamConfigurationCount,
                                forcedGoodSeats = diagnostics.afterStructure.forcedGoodSeats,
                                forcedEvilSeats = diagnostics.afterStructure.forcedEvilSeats,
                                evilCoverSize = diagnostics.afterStructure.evilCoverSize,
                            ),
                        )
                    },
                )
            },
        )
    }

    private fun selectReviewGroups(
        groups: List<FirstNightProjectedSignatureGroup>,
    ): List<Pair<FirstNightProjectedSignatureGroup, Set<FirstNightBeginnerSelectionReason>>> {
        require(groups.isNotEmpty())
        val selected = linkedMapOf<String, Pair<FirstNightProjectedSignatureGroup, MutableSet<FirstNightBeginnerSelectionReason>>>()

        fun add(group: FirstNightProjectedSignatureGroup, reason: FirstNightBeginnerSelectionReason) {
            val entry = selected.getOrPut(group.signatureId) { group to linkedSetOf() }
            entry.second += reason
        }

        val byAfter = groups.sortedWith(
            compareBy<FirstNightProjectedSignatureGroup> { anchor(it).after.value }
                .thenBy { it.signatureId },
        )
        add(byAfter.first(), FirstNightBeginnerSelectionReason.LOWEST_AFTER_WORLD_COUNT)
        add(byAfter.last(), FirstNightBeginnerSelectionReason.HIGHEST_AFTER_WORLD_COUNT)
        add(
            groups.sortedWith(
                compareBy<FirstNightProjectedSignatureGroup> { anchor(it).afterStructure.demonCoverSize }
                    .thenBy { anchor(it).after.value }
                    .thenBy { it.signatureId },
            ).first(),
            FirstNightBeginnerSelectionReason.SMALLEST_DEMON_COVER,
        )
        add(
            groups.sortedWith(
                compareBy<FirstNightProjectedSignatureGroup> { anchor(it).afterStructure.distinctEvilTeamConfigurationCount }
                    .thenBy { anchor(it).after.value }
                    .thenBy { it.signatureId },
            ).first(),
            FirstNightBeginnerSelectionReason.FEWEST_EVIL_CONFIGURATIONS,
        )
        add(
            groups.sortedWith(
                compareByDescending<FirstNightProjectedSignatureGroup> { anchor(it).afterStructure.forcedGoodSeats.size }
                    .thenBy { anchor(it).after.value }
                    .thenBy { it.signatureId },
            ).first(),
            FirstNightBeginnerSelectionReason.MOST_FORCED_GOOD,
        )
        add(
            groups.sortedWith(
                compareByDescending<FirstNightProjectedSignatureGroup>(::largestLeaveOneOutRecovery)
                    .thenBy { it.signatureId },
            ).first(),
            FirstNightBeginnerSelectionReason.LARGEST_LEAVE_ONE_OUT_RECOVERY,
        )
        add(byAfter[((byAfter.size - 1) * 1) / 4], FirstNightBeginnerSelectionReason.LOWER_QUARTILE_AFTER)
        add(byAfter[((byAfter.size - 1) * 3) / 4], FirstNightBeginnerSelectionReason.UPPER_QUARTILE_AFTER)

        return selected.values
            .map { (group, reasons) -> group to reasons.toSet() }
            .sortedBy { (group, _) -> group.signatureId }
    }

    private fun largestLeaveOneOutRecovery(group: FirstNightProjectedSignatureGroup): BigInteger {
        val full = anchor(group).after.value
        return group.leaveOneOutDiagnostics
            .maxOfOrNull { loo -> loo.recipientDiagnostics.single().after.value.subtract(full) }
            ?: BigInteger.ZERO
    }

    private fun anchor(group: FirstNightProjectedSignatureGroup) = group.recipientDiagnostics.single()

    private fun reviewObservation(observation: EpistemicObservation): String {
        val source = observation.sourceSeat?.let { "seat-$it" } ?: "public"
        val kind = observation.sourceAbility?.value ?: "public-claim"
        return "$source/$kind: ${observation.proposition}"
    }

    private fun game(definition: ScenarioDefinition): GameState {
        val players = definition.roleNamesBySeat.mapIndexed { index, roleName ->
            val role = requireNotNull(definitionsByName[roleName]) { "Unknown Trouble Brewing role $roleName" }
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
            seed = 20260916L + definition.id.hashCode(),
        )
    }

    private fun context(scenarioId: String, game: GameState): ExactHistoricalHypotheticalContext {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "fn-bundle-3-pilot",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-3-$scenarioId",
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
}
