package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

internal data class DecisionSeedMaterial(
    val persistedGameSeed: Long,
    val gameId: String,
    val idempotencyKey: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val historyDigest: String,
    val rulesetVersion: String,
    val algorithmConfigVersion: String,
    val selectorVersion: String,
) {
    init {
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(idempotencyKey.isNotBlank()) { "idempotencyKey cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
        require(historyDigest.isNotBlank()) { "historyDigest cannot be blank." }
        require(rulesetVersion.isNotBlank()) { "rulesetVersion cannot be blank." }
        require(algorithmConfigVersion.isNotBlank()) { "algorithmConfigVersion cannot be blank." }
        require(selectorVersion.isNotBlank()) { "selectorVersion cannot be blank." }
    }
}

internal object DecisionSeedFactory {
    private const val SCHEMA_VERSION = "decision-seed-v1"

    fun create(material: DecisionSeedMaterial): Long = MurmurHash3.low64Utf8(
        listOf(
            SCHEMA_VERSION,
            material.persistedGameSeed.toString(),
            material.gameId,
            material.idempotencyKey,
            material.gameStateRevision.toString(),
            material.playerInputRevision.toString(),
            material.historyDigest,
            material.rulesetVersion,
            material.algorithmConfigVersion,
            material.selectorVersion,
        ).joinToString(separator = "") { value -> "${value.toByteArray(Charsets.UTF_8).size}:$value" },
    )
}
