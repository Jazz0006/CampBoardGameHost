package com.codex.campboardgamehost.clocktower.recommendation

/** Conservative, aggregate-only threshold for surfacing—not proving—possible withholding. */
data class SelectionDistributionReviewPolicy(
    val minimumEligibleObservations: Long = 20,
    val maximumRateGapFromLeadingFamily: Double = 0.25,
) {
    init {
        require(minimumEligibleObservations > 0)
        require(maximumRateGapFromLeadingFamily in 0.0..1.0)
    }
}

data class WithholdingReviewSignal(
    val key: SelectionAuditKey,
    val eligibleObservations: Long,
    val selectionRate: Double,
    val leadingFamilyRate: Double,
)

data class SelectionDistributionReview(
    val reviewedStrata: Int,
    val signals: List<WithholdingReviewSignal>,
) {
    val approvedForExpansion: Boolean
        get() = reviewedStrata > 0 && signals.isEmpty()
}

/**
 * Reviews only aggregate C8 exports. Families are compared within the same
 * player-count/phase/style cohort and only after enough AUTO-eligible
 * opportunities exist. A signal is intentionally not proof of a defect:
 * reviewer context is required before passing it to [SelectionRolloutGate].
 */
object SelectionDistributionReviewer {
    fun review(
        snapshot: Map<SelectionAuditKey, SelectionAuditTotals>,
        policy: SelectionDistributionReviewPolicy = SelectionDistributionReviewPolicy(),
    ): SelectionDistributionReview {
        val eligible = snapshot.entries.filter { it.value.familyEligibleCount >= policy.minimumEligibleObservations }
        val byCohort = eligible.groupBy { (key, _) ->
            ReviewCohort(key.playerCount, key.phase.name, key.style.name)
        }
        val signals = byCohort.values.flatMap { cohort ->
            val leadingRate = cohort.maxOf { (_, totals) -> totals.selectionRateGivenEligibility ?: 0.0 }
            cohort.mapNotNull { (key, totals) ->
                val rate = requireNotNull(totals.selectionRateGivenEligibility)
                if (leadingRate - rate > policy.maximumRateGapFromLeadingFamily) {
                    WithholdingReviewSignal(key, totals.familyEligibleCount, rate, leadingRate)
                } else {
                    null
                }
            }
        }.sortedWith(compareBy({ it.key.familyId }, { it.key.playerCount }, { it.key.phase.name }, { it.key.style.name }))
        return SelectionDistributionReview(eligible.size, signals)
    }

    private data class ReviewCohort(val playerCount: Int, val phase: String, val style: String)
}
