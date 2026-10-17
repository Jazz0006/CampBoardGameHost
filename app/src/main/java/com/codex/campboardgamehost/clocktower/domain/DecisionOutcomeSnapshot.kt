package com.codex.campboardgamehost.clocktower.domain

/** Canonical typed outcome retained for actual Host causal journal and Recovery. */
data class DecisionOutcomeSnapshot(
    val decisionType: String,
    val canonicalFields: Map<String, String>,
) {
    init {
        require(decisionType.isNotBlank()) { "decisionType cannot be blank." }
        require(canonicalFields.keys.all { it.isNotBlank() }) { "Outcome field names cannot be blank." }
        require(canonicalFields.keys.toList() == canonicalFields.keys.sorted()) {
            "Outcome fields must use canonical key order."
        }
    }
}
