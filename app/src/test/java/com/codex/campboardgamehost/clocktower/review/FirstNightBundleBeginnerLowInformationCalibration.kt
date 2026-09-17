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
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import java.io.File

data class FirstNightBeginnerLowInformationPoint(
    val pointId: String,
    val sourceChoiceId: String,
    val publicClaims: List<String>,
    val diagnostics: FirstNightBeginnerDiagnostics,
)

data class FirstNightBeginnerLowInformationCalibration(
    val scenarioId: String,
    val seating: List<Pair<Int, RoleId>>,
    val legalWasherwomanCandidateCount: Int,
    val selectedPointCount: Int,
    val points: List<FirstNightBeginnerLowInformationPoint>,
)

/**
 * Bounded exact weak-information calibration.
 *
 * The legal point candidates come from the canonical Washerwoman candidate generator and Chef truth
 * comes from the rules-owned numeric semantics. Only three deterministic public signatures are sent
 * to the exact evaluator. No possible world is sampled and no approximate inference is introduced;
 * the bounded dimension is the number of legal information bundles chosen for human calibration.
 */
object FirstNightBundleBeginnerLowInformationCalibrationBuilder {
    private val stagedCounterworldRoles = setOf(
        RoleId("Drunk"),
        RoleId("Spy"),
        RoleId("Recluse"),
        RoleId("Poisoner"),
    )
    private val washerwoman = RoleId("Washerwoman")
    private val chef = RoleId("Chef")

    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsByName = roleDefinitions.associateBy { it.id.value }

    fun build(): FirstNightBeginnerLowInformationCalibration {
        val game = game()
        val context = context(game)
        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )

        val legalWasherwomanCandidates = NaturalPairInformationCandidateGenerator
            .generateHealthyInformationSpace(
                game = game,
                sourceSeat = 1,
                abilityRole = washerwoman,
                roleDefinitions = roleDefinitions,
            )
            .sortedBy { it.candidateId }
        require(legalWasherwomanCandidates.isNotEmpty())

        val selectedIndices = listOf(
            0,
            (legalWasherwomanCandidates.size - 1) / 2,
            legalWasherwomanCandidates.lastIndex,
        ).distinct()
        val chefValues = FirstNightNumericInformationSemantics.healthyTruthValues(game, 2)
        require(chefValues.size == 1) { "Healthy Chef must have one deterministic truthful value." }
        val chefValue = chefValues.single()
        val chefInformation = EffectDraft.PlayerInformation(
            recipientSeat = 2,
            sourceAbility = chef,
            value = InformationValue.Number(chefValue),
        )
        val chefObservation = privateObservation(
            formal = formal,
            game = game,
            sequence = 2,
            information = chefInformation,
            observationId = "fn3-low-chef",
        )

        val bundles = selectedIndices.mapIndexed { pointIndex, candidateIndex ->
            val candidate = legalWasherwomanCandidates[candidateIndex]
            val washerInformation = candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().single()
            val washerObservation = privateObservation(
                formal = formal,
                game = game,
                sequence = 1,
                information = washerInformation,
                observationId = "fn3-low-washer-$pointIndex",
            )
            FirstNightInformationBundle(
                bundleId = "fn3-low-point-$pointIndex",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "washerwoman-info",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = candidate.candidateId,
                        observation = washerObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                    FirstNightInformationBundleEntry(
                        entryId = "chef-info",
                        control = FirstNightBundleEntryControl.RULE_DETERMINED,
                        observation = chefObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ) to candidate.candidateId
        }

        val projectedByBundle = bundles.associate { (bundle, _) ->
            bundle.bundleId to FirstNightPublicGoodInfoProjection.project(bundle)
        }
        val exactCounterworldRoles = roleDefinitions.filterNot { it.id in stagedCounterworldRoles }
        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context.copy(roleDefinitions = exactCounterworldRoles),
            queries = bundles.map { (bundle, _) ->
                ExactHypotheticalObservationBundleQuery(
                    bundleId = bundle.bundleId,
                    recipientSeat = 1,
                    observations = projectedByBundle.getValue(bundle.bundleId),
                )
            },
        )
        require(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready) {
            "Bounded low-information calibration requires exact healthy evaluation."
        }
        val diagnosticsById = evaluation.diagnostics.associateBy { it.bundleId }

        return FirstNightBeginnerLowInformationCalibration(
            scenarioId = "cal-bounded-low-information",
            seating = game.players.map { it.seat to it.actualRole },
            legalWasherwomanCandidateCount = legalWasherwomanCandidates.size,
            selectedPointCount = bundles.size,
            points = bundles.map { (bundle, sourceChoiceId) ->
                val diagnostic = diagnosticsById.getValue(bundle.bundleId)
                FirstNightBeginnerLowInformationPoint(
                    pointId = bundle.bundleId,
                    sourceChoiceId = sourceChoiceId,
                    publicClaims = projectedByBundle.getValue(bundle.bundleId).map { it.proposition.toString() },
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

    fun renderMarkdown(calibration: FirstNightBeginnerLowInformationCalibration): String = buildString {
        appendLine("## ${calibration.scenarioId} — CALIBRATION / BOUNDED EXACT")
        appendLine()
        appendLine("Seating: " + calibration.seating.joinToString(" | ") { (seat, role) -> "$seat=${role.value}" })
        appendLine(
            "Canonical legal Washerwoman candidates: ${calibration.legalWasherwomanCandidateCount}; " +
                "deterministic exact points evaluated: ${calibration.selectedPointCount}",
        )
        appendLine("Selection: first / middle / last candidate after canonical candidate-id ordering.")
        appendLine("No possible-world sampling. No leave-one-out evaluation for this weak-information probe.")
        appendLine()
        calibration.points.forEach { point ->
            appendLine("### ${point.pointId}")
            appendLine()
            appendLine("Washerwoman canonical choice: `${point.sourceChoiceId}`")
            appendLine("Public claims:")
            point.publicClaims.forEach { appendLine("- $it") }
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

    private fun privateObservation(
        formal: FormalGameState,
        game: GameState,
        sequence: Int,
        information: EffectDraft.PlayerInformation,
        observationId: String,
    ): EpistemicObservation = EpistemicObservation(
        observationId = observationId,
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

    private fun game(): GameState {
        val roleNames = listOf(
            "Washerwoman",
            "Chef",
            "Monk",
            "Soldier",
            "Virgin",
            "Scarlet Woman",
            "Imp",
        )
        val players = roleNames.mapIndexed { index, roleName ->
            val role = requireNotNull(definitionsByName[roleName])
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
            seed = 20260916L,
        )
    }

    private fun context(game: GameState): ExactHistoricalHypotheticalContext {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "fn-bundle-3-low-info",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-3-low-info",
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
