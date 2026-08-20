package com.codex.campboardgamehost

import org.json.JSONObject

internal enum class PersistedVariantKind {
    CLOCKTOWER_SCRIPT,
    WEREWOLF_BOARD,
}

internal enum class PersistedGameCompatibility {
    COMPATIBLE,
    KIND_MISMATCH,
    VARIANT_MISMATCH,
    CONTENT_MISMATCH,
    SEMANTIC_MISMATCH,
    HOUSE_RULE_MISMATCH,
}

internal data class PersistedGameContentIdentity(
    val kind: PersistedVariantKind,
    val variantId: String,
    val contentHash: String,
    val semanticVersion: String,
    val sourceRevision: String? = null,
) {
    init {
        require(variantId.isNotBlank()) { "Persisted variantId cannot be blank." }
        require(CONTENT_HASH_PATTERN.matches(contentHash)) {
            "Persisted contentHash must be a 128-bit lowercase hexadecimal SHA-256 prefix."
        }
        require(semanticVersion.isNotBlank()) { "Persisted semanticVersion cannot be blank." }
        require(sourceRevision == null || sourceRevision.isNotBlank()) {
            "Persisted sourceRevision cannot be blank."
        }
    }

    fun compatibilityWith(current: PersistedGameContentIdentity): PersistedGameCompatibility = when {
        kind != current.kind -> PersistedGameCompatibility.KIND_MISMATCH
        variantId != current.variantId -> PersistedGameCompatibility.VARIANT_MISMATCH
        contentHash != current.contentHash -> PersistedGameCompatibility.CONTENT_MISMATCH
        semanticVersion != current.semanticVersion -> PersistedGameCompatibility.SEMANTIC_MISMATCH
        else -> PersistedGameCompatibility.COMPATIBLE
    }

    private companion object {
        val CONTENT_HASH_PATTERN = Regex("[0-9a-f]{32}")
    }
}

internal data class PersistedWerewolfGameIdentity(
    val board: PersistedGameContentIdentity,
    val ruleOptions: WerewolfRuleOptions,
) {
    init {
        require(board.kind == PersistedVariantKind.WEREWOLF_BOARD) {
            "Werewolf persisted identity must reference a Werewolf board."
        }
    }

    fun compatibilityWith(current: PersistedWerewolfGameIdentity): PersistedGameCompatibility {
        val boardCompatibility = board.compatibilityWith(current.board)
        if (boardCompatibility != PersistedGameCompatibility.COMPATIBLE) return boardCompatibility
        return if (ruleOptions == current.ruleOptions) {
            PersistedGameCompatibility.COMPATIBLE
        } else {
            PersistedGameCompatibility.HOUSE_RULE_MISMATCH
        }
    }
}

internal object PersistedGameContentIdentityJsonCodec {
    fun encode(identity: PersistedGameContentIdentity): JSONObject = JSONObject().apply {
        put("kind", identity.kind.name)
        put("variantId", identity.variantId)
        put("contentHash", identity.contentHash)
        put("semanticVersion", identity.semanticVersion)
        put("sourceRevision", identity.sourceRevision ?: JSONObject.NULL)
    }

    fun decode(json: JSONObject): PersistedGameContentIdentity = PersistedGameContentIdentity(
        kind = PersistedVariantKind.valueOf(json.getString("kind")),
        variantId = json.getString("variantId"),
        contentHash = json.getString("contentHash"),
        semanticVersion = json.getString("semanticVersion"),
        sourceRevision = if (json.isNull("sourceRevision")) null else json.getString("sourceRevision"),
    )
}

internal object PersistedWerewolfGameIdentityJsonCodec {
    fun encode(identity: PersistedWerewolfGameIdentity): JSONObject = JSONObject().apply {
        put("board", PersistedGameContentIdentityJsonCodec.encode(identity.board))
        put("ruleOptions", JSONObject().apply {
            put("lastWordsMode", identity.ruleOptions.lastWordsMode.name)
        })
    }

    fun decode(json: JSONObject): PersistedWerewolfGameIdentity {
        val options = json.getJSONObject("ruleOptions")
        return PersistedWerewolfGameIdentity(
            board = PersistedGameContentIdentityJsonCodec.decode(json.getJSONObject("board")),
            ruleOptions = WerewolfRuleOptions(
                lastWordsMode = LastWordsMode.valueOf(options.getString("lastWordsMode")),
            ),
        )
    }
}
