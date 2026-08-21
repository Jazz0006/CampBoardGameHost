package com.codex.campboardgamehost

import org.json.JSONObject

/**
 * Strict variant-identity envelope for current active-game schema v3 snapshots.
 *
 * It is deliberately independent from the rest of the active-game payload so save/restore wiring
 * can validate variant identity before interpreting game-specific mutable state.
 */
internal data class PersistedActiveGameIdentityEnvelope(
    val gameKind: GameKind,
    val clocktower: PersistedGameContentIdentity? = null,
    val werewolf: PersistedWerewolfGameIdentity? = null,
) {
    init {
        when (gameKind) {
            GameKind.Undercover -> require(clocktower == null && werewolf == null) {
                "Undercover active games cannot carry Clocktower or Werewolf content identity."
            }
            GameKind.Clocktower -> {
                require(clocktower != null && werewolf == null) {
                    "Clocktower active games require exactly one Clocktower script identity."
                }
                require(clocktower.kind == PersistedVariantKind.CLOCKTOWER_SCRIPT) {
                    "Clocktower active-game identity must reference a Clocktower script."
                }
            }
            GameKind.Werewolf -> require(werewolf != null && clocktower == null) {
                "Werewolf active games require exactly one Werewolf board identity."
            }
        }
    }

    companion object {
        fun undercover(): PersistedActiveGameIdentityEnvelope = PersistedActiveGameIdentityEnvelope(
            gameKind = GameKind.Undercover,
        )

        fun clocktower(identity: PersistedGameContentIdentity): PersistedActiveGameIdentityEnvelope =
            PersistedActiveGameIdentityEnvelope(
                gameKind = GameKind.Clocktower,
                clocktower = identity,
            )

        fun werewolf(identity: PersistedWerewolfGameIdentity): PersistedActiveGameIdentityEnvelope =
            PersistedActiveGameIdentityEnvelope(
                gameKind = GameKind.Werewolf,
                werewolf = identity,
            )
    }
}

internal object PersistedActiveGameIdentityJsonCodec {
    const val ROOT_KEY = "contentIdentity"

    fun encode(envelope: PersistedActiveGameIdentityEnvelope): JSONObject = JSONObject().apply {
        put("gameKind", envelope.gameKind.name)
        envelope.clocktower?.let { identity ->
            put("clocktower", JSONObject().apply {
                put("kind", identity.kind.name)
                put("variantId", identity.variantId)
                put("contentHash", identity.contentHash)
                put("semanticVersion", identity.semanticVersion)
                put("sourceRevision", identity.sourceRevision)
            })
        }
        envelope.werewolf?.let { identity ->
            put("werewolf", JSONObject().apply {
                put("board", JSONObject().apply {
                    put("kind", identity.board.kind.name)
                    put("variantId", identity.board.variantId)
                    put("contentHash", identity.board.contentHash)
                    put("semanticVersion", identity.board.semanticVersion)
                    put("sourceRevision", identity.board.sourceRevision)
                })
                put("ruleOptionsHash", identity.ruleOptionsHash)
            })
        }
    }

    fun decode(json: JSONObject): PersistedActiveGameIdentityEnvelope {
        val gameKind = json.requiredEnum<GameKind>("gameKind")
        val clocktower = json.optJSONObject("clocktower")?.let(::decodeContentIdentity)
        val werewolf = json.optJSONObject("werewolf")?.let { value ->
            PersistedWerewolfGameIdentity(
                board = decodeContentIdentity(value.getJSONObject("board")),
                ruleOptionsHash = value.requiredString("ruleOptionsHash"),
            )
        }
        return PersistedActiveGameIdentityEnvelope(
            gameKind = gameKind,
            clocktower = clocktower,
            werewolf = werewolf,
        )
    }

    private fun decodeContentIdentity(json: JSONObject): PersistedGameContentIdentity =
        PersistedGameContentIdentity(
            kind = json.requiredEnum("kind"),
            variantId = json.requiredString("variantId"),
            contentHash = json.requiredString("contentHash"),
            semanticVersion = json.requiredString("semanticVersion"),
            sourceRevision = json.requiredString("sourceRevision"),
        )
}

private fun JSONObject.requiredString(key: String): String {
    require(has(key) && !isNull(key)) { "Missing required persisted string '$key'." }
    return getString(key).takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Persisted string '$key' cannot be blank.")
}

private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(key: String): T {
    val raw = requiredString(key)
    return enumValues<T>().firstOrNull { it.name == raw }
        ?: throw IllegalArgumentException("Invalid persisted enum '$key'.")
}
