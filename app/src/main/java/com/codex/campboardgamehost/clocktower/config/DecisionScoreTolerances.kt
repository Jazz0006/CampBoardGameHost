package com.codex.campboardgamehost.clocktower.config

internal enum class DecisionRiskClass {
    SETUP_PLAN,
    SETUP_ITEM,
    DYNAMIC_INFORMATION,
    HIGH_IMPACT_DYNAMIC,
    RULE_ONLY,
}

internal object DecisionScoreTolerances {
    const val CALIBRATION_VERSION = "v4-pr10-fixed-50000-v1"
    const val SETUP_PLAN = 32
    const val SETUP_ITEM = 6
    const val DYNAMIC_INFORMATION = 4
    const val HIGH_IMPACT_DYNAMIC = 2
    const val RULE_ONLY = 0

    fun forRiskClass(riskClass: DecisionRiskClass): Int = when (riskClass) {
        // Composite plans accumulate independent score items; PR5's 1000-game calibration established 32.
        DecisionRiskClass.SETUP_PLAN -> SETUP_PLAN
        DecisionRiskClass.SETUP_ITEM -> SETUP_ITEM
        DecisionRiskClass.DYNAMIC_INFORMATION -> DYNAMIC_INFORMATION
        DecisionRiskClass.HIGH_IMPACT_DYNAMIC -> HIGH_IMPACT_DYNAMIC
        DecisionRiskClass.RULE_ONLY -> RULE_ONLY
    }
}
