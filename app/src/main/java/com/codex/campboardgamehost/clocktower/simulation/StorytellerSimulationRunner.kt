package com.codex.campboardgamehost.clocktower.simulation

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.recommendation.CandidatePoolBuilder
import com.codex.campboardgamehost.clocktower.recommendation.FamilyProbabilityBudget
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector

internal data class SimulationScenario(
    val id: String,
    val evaluations: List<DecisionEvaluation<String>>,
    val familyBudget: FamilyProbabilityBudget,
    val scoreTolerance: Int,
    val pressureByCandidate: Map<String, Int> = emptyMap(),
    val baselineCandidateId: String,
) {
    init {
        require(id.isNotBlank())
        require(evaluations.any { it.candidate.candidateId == baselineCandidateId })
    }
}

internal object StorytellerSimulationRunner {
    fun runFixedSeeds(
        sampleSize: Int,
        scenarios: List<SimulationScenario>,
        baseSeed: Long,
    ): DistributionReport {
        require(sampleSize > 0)
        require(scenarios.isNotEmpty())
        val observations = ArrayList<SimulationObservation>(sampleSize)
        repeat(sampleSize) { index ->
            val scenario = scenarios[index % scenarios.size]
            val pool = CandidatePoolBuilder.build(scenario.evaluations, scenario.scoreTolerance)
            val seed = MurmurHash3.low64Utf8("simulation-v1|$baseSeed|${scenario.id}|$index")
            val selection = requireNotNull(
                WeightedStableSelector.select(pool, scenario.familyBudget, seed),
            )
            observations += SimulationObservation(
                stratum = scenario.id,
                candidateId = selection.selected.candidate.candidateId,
                baselineCandidateId = scenario.baselineCandidateId,
                playerPressure = scenario.pressureByCandidate[selection.selected.candidate.candidateId] ?: 0,
                targetProbabilityByCandidate = selection.finalProbabilityByCandidate.mapValues {
                    it.value.toDouble() / WeightedStableSelector.FIXED_POINT_SCALE
                },
            )
        }
        return DistributionReport.fromObservations(observations)
    }
}
