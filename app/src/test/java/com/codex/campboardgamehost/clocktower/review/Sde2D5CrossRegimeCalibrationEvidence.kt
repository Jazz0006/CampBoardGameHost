package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactRegistrationWitnessBinding
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingSetupProfiles
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologyHypotheticalBundleEvaluator
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import java.math.BigInteger

internal enum class Sde2D5PlayerCountRegime {
    FIVE_TO_SIX,
    SEVEN_TO_NINE,
    TEN_TO_TWELVE,
    THIRTEEN_TO_FIFTEEN;

    companion object {
        fun from(playerCount: Int): Sde2D5PlayerCountRegime = when (playerCount) {
            in 5..6 -> FIVE_TO_SIX
            in 7..9 -> SEVEN_TO_NINE
            in 10..12 -> TEN_TO_TWELVE
            in 13..15 -> THIRTEEN_TO_FIFTEEN
            else -> throw IllegalArgumentException(
                "SDE-2D5 calibration supports Trouble Brewing player counts 5..15, got $playerCount.",
            )
        }
    }
}

internal enum class Sde2D5SetupProfileKind {
    STANDARD,
    BARON,
}

internal data class Sde2D5CalibrationEvidencePoint(
    val pointId: String,
    val playerCount: Int,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val beforeStrategicWorldCount: Int,
    val afterStrategicWorldCount: Int,
    val normalized: NormalizedStrategicDiagnostics,
    val rawMechanicalBefore: BigInteger? = null,
    val rawMechanicalAfter: BigInteger? = null,
) {
    init {
        require(pointId.isNotBlank())
        require(playerCount in 5..15)
        require(regime == Sde2D5PlayerCountRegime.from(playerCount))
        require(beforeStrategicWorldCount >= 0)
        require(afterStrategicWorldCount in 0..beforeStrategicWorldCount)
        require((rawMechanicalBefore == null) == (rawMechanicalAfter == null)) {
            "Raw mechanical calibration evidence must provide BEFORE and AFTER together."
        }
        if (rawMechanicalBefore != null && rawMechanicalAfter != null) {
            require(rawMechanicalBefore.signum() >= 0)
            require(rawMechanicalAfter.signum() >= 0)
            require(rawMechanicalAfter <= rawMechanicalBefore)
        }
    }
}

internal data class Sde2D5CrossRegimeCalibrationEvidence(
    val points: List<Sde2D5CalibrationEvidencePoint>,
) {
    init {
        require(points.isNotEmpty())
        require(points.map(Sde2D5CalibrationEvidencePoint::pointId).distinct().size == points.size)
    }
}

/**
 * Small D5B contract probe proving that calibration can consume the scalable D2D4 topology path
 * across every supported player-count regime.
 *
 * This is intentionally a neutral baseline rather than policy calibration. The shown-role probe is
 * deterministic and already supported by the topology evaluator. No mechanical-world enumeration,
 * ranking threshold, sampling or holdout data is introduced here.
 */
internal object Sde2D5CrossRegimeCalibrationEvidenceBuilder {
    private val representativePlayerCounts = listOf(6, 9, 12, 15)
    private val chef = RoleId("Chef")
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d5-cross-regime-calibration",
        "official",
        RuleCoverage.VERIFIED,
    )

    fun buildBaseline(): Sde2D5CrossRegimeCalibrationEvidence =
        Sde2D5CrossRegimeCalibrationEvidence(
            points = representativePlayerCounts.map(::buildPoint),
        )

    private fun buildPoint(playerCount: Int): Sde2D5CalibrationEvidencePoint {
        val profile = TroubleBrewingSetupProfiles.standard(playerCount)
        val snapshotId = "d5b-snapshot-$playerCount"
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d5b-knowledge-$playerCount",
            formalSnapshotId = snapshotId,
            recipientSeat = 1,
            perceivedRole = chef,
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ),
        )
        val observation = EpistemicObservation(
            observationId = "d5b-shown-chef-$playerCount",
            snapshotId = snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.ShownRoleAt(1, chef),
        )
        val query = ExactHypotheticalObservationBundleQuery(
            bundleId = "d5b-baseline-$playerCount",
            recipientSeat = 1,
            observations = listOf(observation),
            registrationWitnessBindings = listOf(
                ExactRegistrationWitnessBinding(
                    observationId = observation.observationId,
                    registrations = emptySet(),
                ),
            ),
        )
        val evaluation = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
            rulesetRef = ruleset,
            knowledge = knowledge,
            roleDefinitions = roles,
            queries = listOf(query),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        require(evaluation is ExactStrategicTopologyBundleEvaluation.Ready) {
            "D5B cross-regime baseline must be supported by topology-first evaluation."
        }
        val diagnostic = evaluation.diagnostics.single()
        require(diagnostic.afterFeasible) {
            "D5B baseline must retain at least one strategic topology at $playerCount players."
        }

        return Sde2D5CalibrationEvidencePoint(
            pointId = query.bundleId,
            playerCount = playerCount,
            regime = Sde2D5PlayerCountRegime.from(playerCount),
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            beforeStrategicWorldCount = diagnostic.beforeStructure.distinctStrategicWorldCount,
            afterStrategicWorldCount = diagnostic.afterStructure.distinctStrategicWorldCount,
            normalized = NormalizedStrategicDiagnosticsProjector.project(
                diagnostic = diagnostic,
                playerCount = playerCount,
            ),
        )
    }
}
