package com.codex.campboardgamehost.clocktower.domain

data class PlayerInformationPressure(
    val seat: Int,
    val directSuspicion: Int = 0,
    val indirectSuspicion: Int = 0,
    val confirmation: Int = 0,
    val recentTargetCount: Int = 0,
    val highImpactTargetCount: Int = 0,
) {
    init {
        require(seat > 0) { "seat must be positive." }
        require(directSuspicion >= 0 && indirectSuspicion >= 0 && confirmation >= 0)
        require(recentTargetCount >= 0 && highImpactTargetCount >= 0)
    }
}

data class MisinformationLedger(
    val totalOpportunities: Int = 0,
    val falseInformationCount: Int = 0,
    val highImpactFalseCount: Int = 0,
    val consecutiveFalseCount: Int = 0,
    val truthfulWhileImpairedCount: Int = 0,
) {
    init {
        require(listOf(totalOpportunities, falseInformationCount, highImpactFalseCount, consecutiveFalseCount, truthfulWhileImpairedCount).all { it >= 0 })
        require(falseInformationCount <= totalOpportunities)
        require(highImpactFalseCount <= falseInformationCount)
        require(consecutiveFalseCount <= falseInformationCount)
        require(truthfulWhileImpairedCount <= totalOpportunities)
    }
}

data class RegistrationLedger(
    val evilRegistrationCount: Int = 0,
    val goodRegistrationCount: Int = 0,
    val minionRegistrationCount: Int = 0,
    val demonRegistrationCount: Int = 0,
    val highImpactRegistrationCount: Int = 0,
    val consecutiveSameRegistrationCount: Int = 0,
) {
    init {
        require(listOf(evilRegistrationCount, goodRegistrationCount, minionRegistrationCount, demonRegistrationCount, highImpactRegistrationCount, consecutiveSameRegistrationCount).all { it >= 0 })
    }

    val totalRegistrations: Int get() = evilRegistrationCount + goodRegistrationCount
}

data class DecisionCorrectionEvent(
    val eventId: String,
    val replacedEventId: String,
    val replacementEventId: String,
    val reasonCode: String,
) {
    init {
        require(eventId.isNotBlank() && replacedEventId.isNotBlank() && replacementEventId.isNotBlank())
        require(replacedEventId != replacementEventId)
        require(reasonCode.isNotBlank())
    }
}

data class DecisionHistoryArchive(
    val events: List<StorytellerDecisionEvent> = emptyList(),
    val corrections: List<DecisionCorrectionEvent> = emptyList(),
)

data class DecisionHistoryProjection(
    val effectiveEvents: List<StorytellerDecisionEvent>,
    val pressureBySeat: Map<Int, PlayerInformationPressure>,
    val misinformationLedger: MisinformationLedger,
    val registrationLedgerBySeat: Map<Int, RegistrationLedger>,
)
