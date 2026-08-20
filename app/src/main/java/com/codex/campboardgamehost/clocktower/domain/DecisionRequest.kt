package com.codex.campboardgamehost.clocktower.domain

data class StorytellerDecisionRequest(
    val requestId: String,
    val idempotencyKey: String,
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val round: Int,
    val phase: StorytellerPhase,
    val sourceSeat: Int?,
    val actorActualRole: RoleId?,
    val abilityRole: RoleId,
    val abilityInstanceId: String,
    val abilityType: AbilityType,
    val detectionSemantics: DetectionSemantics,
    val decisionSequence: Int,
    val rulesetRef: RulesetRef,
    val algorithmConfigVersion: String,
    val gameState: DynamicGameState,
) {
    init {
        require(requestId.isNotBlank()) { "requestId cannot be blank." }
        require(idempotencyKey.isNotBlank()) { "idempotencyKey cannot be blank." }
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
        require(round > 0) { "round must be positive." }
        require(sourceSeat == null || gameState.game.playerAt(sourceSeat) != null) {
            "sourceSeat must identify a player in the state."
        }
        require(abilityInstanceId.isNotBlank()) { "abilityInstanceId cannot be blank." }
        require(decisionSequence >= 0) { "decisionSequence cannot be negative." }
        require(algorithmConfigVersion.isNotBlank()) { "algorithmConfigVersion cannot be blank." }
        require(round == gameState.round) { "Request and game state rounds must match." }
        require(phase == gameState.phase) { "Request and game state phases must match." }
        require(rulesetRef.scriptId == gameState.game.script) {
            "Request ruleset and game state must identify the same script."
        }
    }
}
