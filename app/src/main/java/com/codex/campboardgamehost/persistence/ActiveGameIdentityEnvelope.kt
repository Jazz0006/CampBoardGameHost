package com.codex.campboardgamehost

import org.json.JSONObject

/**
 * Strict variant-identity envelope intended for ACTIVE_GAME_STATE_VERSION=2 snapshots.
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
        fun undercover(): PersistedActiveGameIdentityEnvelope =
            PersistedActiveGameIdentityEnvelope(gameKind = GameKind.Undercover)

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
    const val ROOT_KEY = "gameContentIdentity"

    fun encode(identity: PersistedActiveGameIdentityEnvelope): JSONObject = JSONObject().apply {
        put("gameKind", identity.gameKind.name)
        identity.clocktower?.let {
            put("clocktower", PersistedGameContentIdentityJsonCodec.encode(it))
        }
        identity.werewolf?.let {
            put("werewolf", PersistedWerewolfGameIdentityJsonCodec.encode(it))
        }
    }

    fun decode(json: JSONObject): PersistedActiveGameIdentityEnvelope {
        val gameKind = runCatching { GameKind.valueOf(json.getString("gameKind")) }
            .getOrElse { throw IllegalArgumentException("Unknown active-game identity gameKind.", it) }
        return when (gameKind) {
            GameKind.Undercover -> {
                require(!json.has("clocktower") && !json.has("werewolf")) {
                    "Undercover identity payload contains an unexpected variant identity."
                }
                PersistedActiveGameIdentityEnvelope.undercover()
            }
            GameKind.Clocktower -> {
                require(json.has("clocktower") && !json.has("werewolf")) {
                    "Clocktower identity payload must contain only Clocktower identity."
                }
                val identity = runCatching {
                    PersistedGameContentIdentityJsonCodec.decode(json.getJSONObject("clocktower"))
                }.getOrElse { throw IllegalArgumentException("Invalid Clocktower identity payload.", it) }
                PersistedActiveGameIdentityEnvelope.clocktower(identity)
            }
            GameKind.Werewolf -> {
                require(json.has("werewolf") && !json.has("clocktower")) {
                    "Werewolf identity payload must contain only Werewolf identity."
                }
                val identity = runCatching {
                    PersistedWerewolfGameIdentityJsonCodec.decode(json.getJSONObject("werewolf"))
                }.getOrElse { throw IllegalArgumentException("Invalid Werewolf identity payload.", it) }
                PersistedActiveGameIdentityEnvelope.werewolf(identity)
            }
        }
    }
}
