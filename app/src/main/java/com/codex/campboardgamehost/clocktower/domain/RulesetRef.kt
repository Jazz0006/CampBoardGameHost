package com.codex.campboardgamehost.clocktower.domain

enum class RuleCoverage {
    VERIFIED,
    PARTIAL,
    UNVERIFIED,
}

data class RulesetRef(
    val scriptId: ScriptId,
    val scriptContentHash: String,
    val rulesetVersion: String,
    val sourceRevision: String,
    val coverage: RuleCoverage,
) {
    init {
        require(SCRIPT_HASH_PATTERN.matches(scriptContentHash)) {
            "scriptContentHash must be a 128-bit lowercase hexadecimal SHA-256 prefix."
        }
        require(rulesetVersion.isNotBlank()) { "rulesetVersion cannot be blank." }
        require(sourceRevision.isNotBlank()) { "sourceRevision cannot be blank." }
    }

    companion object {
        private val SCRIPT_HASH_PATTERN = Regex("[0-9a-f]{32}")
    }
}
