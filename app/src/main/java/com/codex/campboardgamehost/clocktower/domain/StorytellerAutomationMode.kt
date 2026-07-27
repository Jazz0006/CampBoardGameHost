package com.codex.campboardgamehost.clocktower.domain

enum class StorytellerAutomationMode(
    val prefsValue: String,
    val style: RecommendationStyle?,
) {
    MANUAL("manual", null),
    AUTO_BALANCED("auto_balanced", RecommendationStyle.BALANCED),
    AUTO_AGGRESSIVE("auto_aggressive", RecommendationStyle.AGGRESSIVE),
    AUTO_GENTLE("auto_gentle", RecommendationStyle.GENTLE),
    ;

    val isAutomatic: Boolean
        get() = style != null
}
