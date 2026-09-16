package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
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
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
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
}

data class FirstNightBeginnerPerspectiveDiagnostics(
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
    val afterWorldCount: BigInteger,
    val demonCoverSize: Int,
    val distinctEvilTeamConfigurationCount: Int,
    val forcedGoodSeats: Set<Int>,
    val forcedEvilSeats: Set<Int>,
    val evilCoverSize: Int,
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
    val perspectives: List<FirstNightBeginnerPerspectiveDiagnostics>,
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
 * FN-BUNDLE-3 pilot corpus builder.
 *
 * This is review infrastructure, not a Badness classifier. It deliberately leaves every item
 * UNREVIEWED and preserves raw interpretable diagnostics. Calibration/holdout separation is by
 * complete setup+seating scenario, so near-duplicate signatures from one scenario cannot leak across
 * the validation boundary.
 */
object FirstNightBundleBeginnerCorpusBuilder {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsByName = roleDefinitions.associateBy { it.id.value }

    private data class ScenarioDefinition(
        val id: String,
        val partition: FirstNightBeginnerCorpusPartition,
        val roleNamesBySeat: List<String>,
        val anchorRecipientSeat: Int,
    )

    private val scenarioDefinitions = listOf(
        ScenarioDefinition(
            id = "cal-pair-rich-adjacent-evil",
            partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
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
        ScenarioDefinition(
            id = "cal-baron-outsider-chain",
            partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
            roleNamesBySeat = listOf(
                "Librarian",
                "Empath",
                "Investigator",
                "Butler",
                "Saint",
                "Baron",
                "Imp",
            ),
            anchorRecipientSeat = 1,
        ),
        ScenarioDefinition(
            id = "holdout-zero-outsider-librarian",
            partition = FirstNightBeginnerCorpusPartition.HOLDOUT,
            roleNamesBySeat = listOf(
                "Librarian",
                "Chef",
                "Empath",
                "Monk",
                "Investigator",
                "Scarlet Woman",
                "Imp",
            ),
            anchorRecipientSeat = 1,
        ),
    )

    fun build(): FirstNightBeginnerCorpus = FirstNightBeginnerCorpus(
        scenarios = scenarioDefinitions.map(::buildScenario),
    )

    fun renderMarkdown(corpus: FirstNightBeginnerCorpus): String = buildString {
        appendLine("FN_BUNDLE_3_CORPUS_START")
        appendLine("# FN-BUNDLE-3 BEGINNER pilot review corpus")
        appendLine()
        appendLine("All labels are UNREVIEWED. Diagnostics are evidence for human review, not thresholds.")
        appendLine()
        corpus.scenarios.forEach { scenario ->
            appendLine("## ${scenario.scenarioId} — ${scenario.partition}")
            appendLine()
            appendLine("Seating: " + scenario.seating.joinToString(" | ") { (seat, role) -> "$seat=${role.value}" })
            appendLine("Raw bundles: ${scenario.rawCompleteBundleCount}; signatures: ${scenario.distinctProjectedSignatureCount}")
            appendLine()
            scenario.items.forEach { item ->
                appendLine("### ${item.itemId}")
                appendLine()
                appendLine("Reasons: ${item.selectionReasons.joinToString()}; label: ${item.label}; multiplicity: ${item.multiplicity}")
                appendLine()
                appendLine("Public observations:")
                item.publicObservations.forEach { observation -> appendLine("- $observation") }
                appendLine()
                appendLine("| Recipient | BEFORE | AFTER | Demon cover | Evil configs | Forced good | Forced evil | Evil cover |")
                appendLine("|---:|---:|---:|---:|---:|---|---|---:|")
                item.perspectives.forEach { perspective ->
                    appendLine(
                        "| ${perspective.recipientSeat} | ${perspective.beforeWorldCount} | ${perspective.afterWorldCount} | " +
                            "${perspective.demonCoverSize} | ${perspective.distinctEvilTeamConfigurationCount} | " +
                            "${perspective.forcedGoodSeats.sorted()} | ${perspective.forcedEvilSeats.sorted()} | ${perspective.evilCoverSize} |",
                    )
                }
                appendLine()
                appendLine("Anchor leave-one-out evidence:")
                item.anchorLeaveOneOut.forEach { loo ->
                    appendLine(
                        "- omit `${loo.omittedObservation}` -> AFTER ${loo.afterWorldCount}, " +
                            "demonCover=${loo.demonCoverSize}, evilConfigs=${loo.distinctEvilTeamConfigurationCount}, " +
                            "forcedGood=${loo.forcedGoodSeats.sorted()}, forcedEvil=${loo.forcedEvilSeats.sorted()}, " +
                            "evilCover=${loo.evilCoverSize}",
                    )
                }
                appendLine()
            }
        }
        appendLine("FN_BUNDLE_3_CORPUS_END")
    }

    private fun buildScenario(definition: ScenarioDefinition): FirstNightBeginnerCorpusScenario {
        val game = game(definition)
        val context = context(definition.id, game)
        val anchorEvaluation = TroubleBrewingFirstNightHealthyBundleHarness.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            evaluationRecipientSeats = setOf(definition.anchorRecipientSeat),
        )
        require(anchorEvaluation is FirstNightHealthyBundleHarnessEvaluation.Ready) {
            "FN-BUNDLE-3 pilot scenario ${definition.id} must be supported by the healthy harness."
        }
        val ready = anchorEvaluation
        val selected = selectReviewGroups(ready.signatureGroups)
        val goodSeats = game.players
            .filter { it.actualAlignment == Alignment.GOOD }
            .map(PlayerState::seat)
            .sorted()
        require(definition.anchorRecipientSeat in goodSeats) {
            "Corpus anchor must be a good recipient in ${definition.id}."
        }

        val additionalSeats = goodSeats - definition.anchorRecipientSeat
        val healthyRoleDefinitions = roleDefinitions.filterNot { it.id in ready.excludedCounterworldRoles }
        val additionalQueries = selected.flatMap { (group, _) ->
            additionalSeats.map { recipientSeat ->
                ExactHypotheticalObservationBundleQuery(
                    bundleId = perspectiveQueryId(definition.id, group.signatureId, recipientSeat),
                    recipientSeat = recipientSeat,
                    observations = group.publicObservations,
                )
            }
        }
        val additionalById = if (additionalQueries.isEmpty()) {
            emptyMap()
        } else {
            val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context.copy(roleDefinitions = healthyRoleDefinitions),
                queries = additionalQueries,
            )
            require(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready) {
                "FN-BUNDLE-3 perspective expansion unexpectedly deferred for ${definition.id}."
            }
            evaluation.diagnostics.associateBy { it.bundleId to it.recipientSeat }
        }

        return FirstNightBeginnerCorpusScenario(
            scenarioId = definition.id,
            partition = definition.partition,
            anchorRecipientSeat = definition.anchorRecipientSeat,
            seating = game.players.map { it.seat to it.actualRole },
            rawCompleteBundleCount = ready.rawCompleteBundleCount,
            distinctProjectedSignatureCount = ready.distinctProjectedSignatureCount,
            items = selected.map { (group, reasons) ->
                val anchor = group.recipientDiagnostics.single()
                val perspectives = goodSeats.map { recipientSeat ->
                    if (recipientSeat == definition.anchorRecipientSeat) {
                        FirstNightBeginnerPerspectiveDiagnostics(
                            recipientSeat = recipientSeat,
                            beforeWorldCount = anchor.before.value,
                            afterWorldCount = anchor.after.value,
                            demonCoverSize = anchor.afterStructure.demonCoverSize,
                            distinctEvilTeamConfigurationCount = anchor.afterStructure.distinctEvilTeamConfigurationCount,
                            forcedGoodSeats = anchor.afterStructure.forcedGoodSeats,
                            forcedEvilSeats = anchor.afterStructure.forcedEvilSeats,
                            evilCoverSize = anchor.afterStructure.evilCoverSize,
                        )
                    } else {
                        additionalById.getValue(
                            perspectiveQueryId(definition.id, group.signatureId, recipientSeat) to recipientSeat,
                        ).toPerspective()
                    }
                }
                FirstNightBeginnerCorpusItem(
                    itemId = "${definition.id}:${group.signatureId}",
                    scenarioId = definition.id,
                    partition = definition.partition,
                    label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
                    signatureId = group.signatureId,
                    multiplicity = group.multiplicity,
                    selectionReasons = reasons,
                    publicObservations = group.publicObservations.map(::reviewObservation),
                    perspectives = perspectives,
                    anchorLeaveOneOut = group.leaveOneOutDiagnostics.mapIndexed { index, loo ->
                        val diagnostics = loo.recipientDiagnostics.single()
                        FirstNightBeginnerLeaveOneOutDiagnostics(
                            omittedObservation = reviewObservation(group.publicObservations[index]),
                            afterWorldCount = diagnostics.after.value,
                            demonCoverSize = diagnostics.afterStructure.demonCoverSize,
                            distinctEvilTeamConfigurationCount = diagnostics.afterStructure.distinctEvilTeamConfigurationCount,
                            forcedGoodSeats = diagnostics.afterStructure.forcedGoodSeats,
                            forcedEvilSeats = diagnostics.afterStructure.forcedEvilSeats,
                            evilCoverSize = diagnostics.afterStructure.evilCoverSize,
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
        add(
            byAfter[((byAfter.size - 1) * 1) / 4],
            FirstNightBeginnerSelectionReason.LOWER_QUARTILE_AFTER,
        )
        add(
            byAfter[((byAfter.size - 1) * 3) / 4],
            FirstNightBeginnerSelectionReason.UPPER_QUARTILE_AFTER,
        )

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

    private fun ExactHypotheticalObservationBundleDiagnostics.toPerspective() =
        FirstNightBeginnerPerspectiveDiagnostics(
            recipientSeat = recipientSeat,
            beforeWorldCount = before.value,
            afterWorldCount = after.value,
            demonCoverSize = afterStructure.demonCoverSize,
            distinctEvilTeamConfigurationCount = afterStructure.distinctEvilTeamConfigurationCount,
            forcedGoodSeats = afterStructure.forcedGoodSeats,
            forcedEvilSeats = afterStructure.forcedEvilSeats,
            evilCoverSize = afterStructure.evilCoverSize,
        )

    private fun reviewObservation(observation: EpistemicObservation): String {
        val source = observation.sourceSeat?.let { "seat-$it" } ?: "public"
        val ability = observation.sourceAbility?.value ?: "identity"
        return "$source/$ability: ${observation.proposition}"
    }

    private fun perspectiveQueryId(scenarioId: String, signatureId: String, recipientSeat: Int) =
        "fn3:$scenarioId:$signatureId:r$recipientSeat"

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
