package com.codex.campboardgamehost.clocktower.domain

enum class StorytellerExperienceMode(
    val prefsValue: String,
) {
    BEGINNER("beginner"),
    EXPERIENCED("experienced"),
    ;

    companion object {
        fun fromPrefsValue(value: String?): StorytellerExperienceMode =
            entries.firstOrNull { it.prefsValue == value } ?: BEGINNER
    }
}
