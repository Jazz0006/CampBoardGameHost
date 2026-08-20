package com.codex.campboardgamehost.clocktower.domain

sealed interface DynamicInformationOutcome {
    data class Number(val value: Int) : DynamicInformationOutcome
    data class Category(val id: String) : DynamicInformationOutcome {
        init {
            require(id.isNotBlank()) { "Category outcome ID cannot be blank." }
        }
    }
}
