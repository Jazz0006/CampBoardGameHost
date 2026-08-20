package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition

/**
 * Persistence identity for Clocktower deliberately uses normalized script content identity, while
 * the existing RulesetRef remains untouched for the A3/A4 correctness boundary.
 */
internal object ClocktowerPersistenceIdentityFactory {
    const val SEMANTIC_VERSION = "clocktower-r5_5-v1"

    fun fromScript(
        script: ClocktowerScriptDefinition,
        sourceRevision: String? = null,
    ): PersistedGameContentIdentity = PersistedGameContentIdentity(
        kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
        variantId = script.id.value,
        contentHash = script.contentHash,
        semanticVersion = SEMANTIC_VERSION,
        sourceRevision = sourceRevision,
    )
}
