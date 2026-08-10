package com.codex.campboardgamehost.clocktower.simulation

import com.codex.campboardgamehost.clocktower.config.DecisionRiskClass
import com.codex.campboardgamehost.clocktower.config.DecisionScoreTolerances
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.FamilyProbabilityBudget

internal data class ToleranceCalibrationReport(
    val calibrationVersion: String,
    val sampleSize: Int,
    val tolerances: Map<DecisionRiskClass, Int>,
    val distribution: DistributionReport,
    val outsideToleranceSelections: Int,
)

internal object ToleranceCalibration {
    const val FORMAL_SAMPLE_SIZE = 50_000
    private const val CALIBRATION_SEED = 10_240_000L

    fun run(sampleSize: Int = FORMAL_SAMPLE_SIZE): ToleranceCalibrationReport {
        val scenarios = scenarios()
        val report = StorytellerSimulationRunner.runFixedSeeds(sampleSize, scenarios, CALIBRATION_SEED)
        val outsideIds = scenarios.map { "${it.id}:outside" }.toSet()
        return ToleranceCalibrationReport(
            calibrationVersion = DecisionScoreTolerances.CALIBRATION_VERSION,
            sampleSize = sampleSize,
            tolerances = DecisionRiskClass.entries.associateWith(DecisionScoreTolerances::forRiskClass),
            distribution = report,
            outsideToleranceSelections = report.buckets
                .filter { it.candidateId in outsideIds }
                .sumOf { it.count },
        )
    }

    private fun scenarios(): List<SimulationScenario> = buildList {
        val playerCounts = listOf(5, 8, 12)
        val keyRoles = listOf("Drunk", "Investigator", "Librarian")
        DecisionRiskClass.entries.forEach { riskClass ->
            RecommendationStyle.entries.forEach { style ->
                playerCounts.forEach { playerCount ->
                    keyRoles.forEach { keyRole ->
                        val id = "${riskClass.name}:${style.name}:$playerCount:$keyRole"
                        val tolerance = DecisionScoreTolerances.forRiskClass(riskClass)
                        val boundaryScore = 100 - tolerance
                        val candidates = listOf(
                            evaluation("$id:best", 100, withinFamilyWeight = 10, pressure = 1),
                            evaluation("$id:boundary", boundaryScore, withinFamilyWeight = 6, pressure = 3),
                            evaluation("$id:outside", boundaryScore - 1, withinFamilyWeight = 100, pressure = 8),
                        )
                        add(
                            SimulationScenario(
                                id = id,
                                evaluations = candidates,
                                familyBudget = FamilyProbabilityBudget(mapOf("calibration" to 1_000_000L)),
                                scoreTolerance = tolerance,
                                pressureByCandidate = candidates.associate {
                                    it.candidate.candidateId to it.pressureDelta.values.sum()
                                },
                                baselineCandidateId = "$id:best",
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun evaluation(
        candidateId: String,
        score: Int,
        withinFamilyWeight: Long,
        pressure: Int,
    ): DecisionEvaluation<String> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = candidateId,
            candidateFamilyId = "calibration",
            outcome = candidateId,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            metadata = CandidateMetadata("calibration-v1", "score-tolerance"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = score,
        withinFamilyWeightFixedPoint = withinFamilyWeight,
        finalProbabilityFixedPoint = 0,
        pressureDelta = mapOf(1 to pressure),
        warnings = emptyList(),
        explanationCodes = emptyList(),
    )
}
