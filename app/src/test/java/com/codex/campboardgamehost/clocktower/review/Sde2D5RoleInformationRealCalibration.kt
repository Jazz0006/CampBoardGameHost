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
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightHealthyBundleHarnessEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightHealthyRecipientExactDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightHealthyBundleHarness
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.io.File
import java.math.BigInteger

internal fun compareStrategicRatioValue(
    left: StrategicRatio,
    right: StrategicRatio,
): Int = when {
    left is StrategicRatio.Undefined && right is StrategicRatio.Undefined -> 0
    left is StrategicRatio.Undefined -> 1
    right is StrategicRatio.Undefined -> -1
    left is StrategicRatio.Defined && right is StrategicRatio.Defined ->
        (left.numerator.toLong() * right.denominator.toLong())
            .compareTo(right.numerator.toLong() * left.denominator.toLong())
    else -> error("Unknown strategic ratio implementation.")
}

internal fun sameStrategicRatioValue(
    left: StrategicRatio,
    right: StrategicRatio,
): Boolean = compareStrategicRatioValue(left, right) == 0

internal data class Sde2D5RoleInformationNearRawContrast(
    val first: Sde2D5RoleInformationCalibrationEvidence,
    val second: Sde2D5RoleInformationCalibrationEvidence,
    val rawWorldRemovalDifference: BigInteger,
) {
    init {
        require(rawWorldRemovalDifference.signum() >= 0)
        require(first.point.playerCount == second.point.playerCount)
        require(first.point.profileKind == second.point.profileKind)
        require(
            !sameStrategicRatioValue(
                first.point.normalized.evilTopologyRetention,
                second.point.normalized.evilTopologyRetention,
            ),
        )
    }
}

internal data class Sde2D5RoleInformationRealCalibration(
    val allEvidence: List<Sde2D5RoleInformationCalibrationEvidence>,
    val topologyNeutral: Sde2D5RoleInformationCalibrationEvidence,
    val closestRawDifferentTopology: Sde2D5RoleInformationNearRawContrast,
    val strongestStrategicCollapse: Sde2D5RoleInformationCalibrationEvidence,
    val weakestMechanicalInformation: Sde2D5RoleInformationCalibrationEvidence,
    val confirmationChainEvidence: List<Sde2D5BundleConfirmationChainEvidence>,
    val confirmationChainSelections: List<Sde2D5BundleConfirmationSelection>,
)

/**
 * Reuses the existing FN-BUNDLE healthy exact harness to discover real D5E review contrasts.
 *
 * Selection is deterministic and descriptive over leave-one-out marginals:
 * - every point means "omitted bundle -> complete bundle" for one public observation;
 * - choose the topology-neutral marginal with the largest exact mechanical reduction;
 * - among marginals with different strategic topology retention, choose the pair whose exact
 *   raw-world removals are closest.
 *
 * No similarity threshold or recommendation gate is introduced.
 */
internal object Sde2D5RoleInformationRealCalibrationBuilder {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsByName = roleDefinitions.associateBy { it.id.value }

    fun build(): Sde2D5RoleInformationRealCalibration {
        val game = game()
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "sde-2d5-role-information-calibration",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "sde-2d5-role-information-calibration",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        val context = ExactHistoricalHypotheticalContext(
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
        val evaluation = TroubleBrewingFirstNightHealthyBundleHarness.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            evaluationRecipientSeats = setOf(1),
        )
        require(evaluation is FirstNightHealthyBundleHarnessEvaluation.Ready) {
            "D5E real role-information calibration requires the healthy exact bundle harness."
        }

        val evidence = evaluation.signatureGroups.flatMap { group ->
            val full = group.recipientDiagnostics.single()
            group.leaveOneOutDiagnostics.mapIndexed { omittedIndex, leaveOneOut ->
                val omitted = leaveOneOut.recipientDiagnostics.single()
                val omittedObservation = group.publicObservations[omittedIndex]
                val sourceSeat = requireNotNull(omittedObservation.sourceSeat) {
                    "Healthy public review clue must retain its source seat."
                }
                val sourceRole = requireNotNull(game.playerAt(sourceSeat)).actualRole
                Sde2D5RoleInformationCalibrationEvidenceProjector.project(
                    playerCount = 7,
                    profileKind = Sde2D5SetupProfileKind.STANDARD,
                    contrastId = "d5e-real-role-information",
                    sourceSeat = sourceSeat,
                    sourceRole = sourceRole,
                    control = roleInformationControl(sourceRole),
                    diagnostic = marginalDiagnostic(
                        bundleId = "${group.signatureId}:marginal-$omittedIndex",
                        omitted = omitted,
                        full = full,
                    ),
                )
            }
        }.filter { point ->
            point.hasMechanicalInformationGain &&
                requireNotNull(point.point.rawMechanicalAfter).signum() > 0
        }
        require(evidence.isNotEmpty()) {
            "D5E real role-information calibration requires mechanically informative marginal clues."
        }
        val evidenceSummary = evidence
            .sortedBy { it.point.pointId }
            .joinToString(separator = " | ") { point ->
                val topology = point.point.normalized.evilTopologyRetention
                "${point.point.pointId}:rawRemoved=${point.rawWorldsRemoved}," +
                    "topology=$topology,neutral=${point.topologyNeutral}"
            }

        val topologyNeutral = evidence
            .filter(Sde2D5RoleInformationCalibrationEvidence::topologyNeutral)
            .sortedWith(
                compareByDescending<Sde2D5RoleInformationCalibrationEvidence> {
                    it.rawWorldsRemoved
                }.thenBy { it.point.pointId },
            )
            .firstOrNull()
            ?: error(
                "D5E real fixture did not expose a mechanically informative topology-neutral " +
                    "marginal clue. Evidence: $evidenceSummary",
            )

        val pairCandidates = buildList {
            for (firstIndex in evidence.indices) {
                for (secondIndex in firstIndex + 1 until evidence.size) {
                    val first = evidence[firstIndex]
                    val second = evidence[secondIndex]
                    if (
                        sameStrategicRatioValue(
                            first.point.normalized.evilTopologyRetention,
                            second.point.normalized.evilTopologyRetention,
                        )
                    ) {
                        continue
                    }
                    add(
                        Sde2D5RoleInformationNearRawContrast(
                            first = first,
                            second = second,
                            rawWorldRemovalDifference =
                                first.rawWorldsRemoved.subtract(second.rawWorldsRemoved).abs(),
                        ),
                    )
                }
            }
        }
        require(pairCandidates.isNotEmpty()) {
            "D5E real fixture did not expose different strategic topology retention levels. " +
                "Evidence: $evidenceSummary"
        }
        val closest = pairCandidates.minWith(
            compareBy<Sde2D5RoleInformationNearRawContrast> { it.rawWorldRemovalDifference }
                .thenBy { it.first.point.pointId }
                .thenBy { it.second.point.pointId },
        )

        val strongestStrategicCollapse = evidence.minWith(
            Comparator { left, right ->
                val ratioOrder = compareStrategicRatioValue(
                    left.point.normalized.evilTopologyRetention,
                    right.point.normalized.evilTopologyRetention,
                )
                if (ratioOrder != 0) {
                    ratioOrder
                } else {
                    left.point.pointId.compareTo(right.point.pointId)
                }
            },
        )
        val weakestMechanicalInformation = evidence.minWith(
            compareBy<Sde2D5RoleInformationCalibrationEvidence> { it.rawWorldsRemoved }
                .thenBy { it.point.pointId },
        )

        val confirmationChainEvidence = evaluation.signatureGroups.map { group ->
            Sde2D5BundleConfirmationChainEvidenceProjector.project(
                playerCount = 7,
                profileKind = Sde2D5SetupProfileKind.STANDARD,
                group = group,
            )
        }.sortedBy { it.signatureId }

        return Sde2D5RoleInformationRealCalibration(
            allEvidence = evidence.sortedBy { it.point.pointId },
            topologyNeutral = topologyNeutral,
            closestRawDifferentTopology = closest,
            strongestStrategicCollapse = strongestStrategicCollapse,
            weakestMechanicalInformation = weakestMechanicalInformation,
            confirmationChainEvidence = confirmationChainEvidence,
            confirmationChainSelections =
                Sde2D5BundleConfirmationChainEvidenceSelector.selectReviewContrasts(
                    confirmationChainEvidence,
                ),
        )
    }

    private fun roleInformationControl(
        role: RoleId,
    ): FirstNightBundleEntryControl = when (role.value) {
        "Washerwoman", "Librarian", "Investigator" ->
            FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
        "Chef", "Empath" ->
            FirstNightBundleEntryControl.RULE_DETERMINED
        else -> error("Unexpected healthy-bundle role-information source ${role.value}.")
    }

    private fun marginalDiagnostic(
        bundleId: String,
        omitted: FirstNightHealthyRecipientExactDiagnostics,
        full: FirstNightHealthyRecipientExactDiagnostics,
    ): ExactHypotheticalObservationBundleDiagnostics {
        require(omitted.recipientSeat == full.recipientSeat)
        require(full.after.value <= omitted.after.value) {
            "A complete bundle cannot create worlds relative to its leave-one-out marginal baseline."
        }
        return ExactHypotheticalObservationBundleDiagnostics(
            bundleId = bundleId,
            recipientSeat = full.recipientSeat,
            before = omitted.after,
            after = full.after,
            beforeStructure = omitted.afterStructure,
            afterStructure = full.afterStructure,
        )
    }

    private fun game(): GameState {
        val roleNames = listOf(
            "Washerwoman",
            "Chef",
            "Empath",
            "Fortune Teller",
            "Investigator",
            "Scarlet Woman",
            "Imp",
        )
        val players = roleNames.mapIndexed { index, roleName ->
            val role = requireNotNull(definitionsByName[roleName]) {
                "Unknown Trouble Brewing role $roleName"
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
            seed = 20260919L,
        )
    }
}
