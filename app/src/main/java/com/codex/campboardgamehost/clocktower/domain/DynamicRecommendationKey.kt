package com.codex.campboardgamehost.clocktower.domain

data class StorytellerPolicySnapshot(val version: String, val style: RecommendationStyle, val algorithmConfigVersion: String) {
    init { require(version.isNotBlank() && algorithmConfigVersion.isNotBlank()) }
}

data class DynamicDecisionSnapshot(
    val gameSnapshot: GameSnapshot,
    val phase: StorytellerPhase,
    val round: Int,
    val decisionPointId: String,
    val nightOrderPosition: Int = 0,
    val poisonTargetSeat: Int? = null,
    val protectedSeats: Set<Int> = emptySet(),
    val pendingAttackSeat: Int? = null,
    val spentAbilitySeats: Set<Int> = emptySet(),
    val lockedDecisionTokens: List<String> = emptyList(),
    val policy: StorytellerPolicySnapshot,
) {
    init {
        val seats = gameSnapshot.gameState.players.map { it.seat }.toSet()
        require(round > 0 && nightOrderPosition >= 0 && decisionPointId.isNotBlank())
        require(listOfNotNull(poisonTargetSeat, pendingAttackSeat).all { it in seats })
        require(protectedSeats.all { it in seats } && spentAbilitySeats.all { it in seats })
        require(lockedDecisionTokens.all { it.isNotBlank() })
    }

    val stateDigest = digest(listOf(
        gameSnapshot.gameState.script.value, gameSnapshot.gameSeed.toString(), phase.name, round.toString(),
        nightOrderPosition.toString(), poisonTargetSeat?.toString().orEmpty(), protectedSeats.sorted().joinToString(","),
        pendingAttackSeat?.toString().orEmpty(), spentAbilitySeats.sorted().joinToString(","),
        gameSnapshot.gameState.players.sortedBy { it.seat }.joinToString(";") { p ->
            "${p.seat}:${p.actualRole.value}:${p.actualAlignment}:${p.actualType}:${p.shownRole?.value}:${p.alive}:${p.poisoned}"
        },
    ).joinToString("|"))
    val decisionHistoryDigest = digest(
        gameSnapshot.decisionHistory.events.joinToString("|") { e ->
            "${e.eventId}:${e.idempotencyKey}:${e.status}:${e.stateDigest}:${e.historyDigest}:${e.selectedCandidateId}"
        } + "#" + gameSnapshot.decisionHistory.corrections.joinToString("|") { c ->
            "${c.eventId}:${c.replacedEventId}:${c.replacementEventId}:${c.reasonCode}"
        },
    )
    val observationLogDigest = digest(gameSnapshot.epistemicObservationLog.records.joinToString("|") { r ->
        "${r.recordId}:${r.phase}:${r.round}:${r.sequence}:${r.sourceSeat}:${r.sourceAbility?.value}:" +
            "${r.visibility}:${r.recipientSeats.sorted().joinToString(",")}:${r.reliability}:${r.proposition}"
    })
    val lockedDecisionDigest = digest(lockedDecisionTokens.sorted().joinToString("|"))

    fun key() = DynamicRecommendationKey(
        gameSnapshot.gameId, gameSnapshot.gameStateRevision, gameSnapshot.playerInputRevision, phase, round,
        decisionPointId, stateDigest, decisionHistoryDigest, observationLogDigest, lockedDecisionDigest,
        policy.version, policy.style, policy.algorithmConfigVersion,
    )

    private fun digest(value: String) = "dynamic-${MurmurHash3.low64Utf8(value).toULong().toString(16).padStart(16, '0')}"
}

data class DynamicRecommendationKey(
    val gameId: String, val gameStateRevision: Long, val playerInputRevision: Long,
    val phase: StorytellerPhase, val round: Int, val decisionPointId: String,
    val stateDigest: String, val decisionHistoryDigest: String, val observationLogDigest: String,
    val lockedDecisionDigest: String, val policyVersion: String, val style: RecommendationStyle,
    val algorithmConfigVersion: String,
) {
    init {
        require(gameId.isNotBlank() && decisionPointId.isNotBlank() && round > 0)
        require(gameStateRevision >= 0 && playerInputRevision >= 0)
        require(listOf(stateDigest, decisionHistoryDigest, observationLogDigest, lockedDecisionDigest,
            policyVersion, algorithmConfigVersion).all { it.isNotBlank() })
    }
}

class DynamicRecommendationGeneration internal constructor(val id: Long, val key: DynamicRecommendationKey)
enum class DynamicGenerationTerminal { READY, SUPERSEDED, FAILED, RESOURCE_EXHAUSTED, CANCELLED }
sealed interface DynamicPublishResult<out T> {
    data class Published<T>(val value: T) : DynamicPublishResult<T>
    data class Rejected(val terminal: DynamicGenerationTerminal) : DynamicPublishResult<Nothing>
}

class DynamicRecommendationGenerationStore<T> {
    private val lock = Any()
    private var nextId = 0L
    private val currentByGame = mutableMapOf<String, DynamicRecommendationGeneration>()
    private val terminalById = mutableMapOf<Long, DynamicGenerationTerminal>()

    fun begin(key: DynamicRecommendationKey): DynamicRecommendationGeneration = synchronized(lock) {
        currentByGame[key.gameId]?.let { terminalById[it.id] = DynamicGenerationTerminal.SUPERSEDED }
        DynamicRecommendationGeneration(++nextId, key).also { currentByGame[key.gameId] = it }
    }
    fun current(gameId: String) = synchronized(lock) { currentByGame[gameId] }
    fun publishIfCurrent(generation: DynamicRecommendationGeneration, value: T): DynamicPublishResult<T> = synchronized(lock) {
        terminalById[generation.id]?.let { return@synchronized DynamicPublishResult.Rejected(it) }
        if (currentByGame[generation.key.gameId] != generation) {
            terminalById[generation.id] = DynamicGenerationTerminal.SUPERSEDED
            return@synchronized DynamicPublishResult.Rejected(DynamicGenerationTerminal.SUPERSEDED)
        }
        terminalById[generation.id] = DynamicGenerationTerminal.READY
        DynamicPublishResult.Published(value)
    }
    fun fail(generation: DynamicRecommendationGeneration, error: Throwable): DynamicGenerationTerminal = synchronized(lock) {
        val terminal = if (error is OutOfMemoryError) DynamicGenerationTerminal.RESOURCE_EXHAUSTED else DynamicGenerationTerminal.FAILED
        terminalById[generation.id] = terminal
        if (currentByGame[generation.key.gameId] == generation) currentByGame.remove(generation.key.gameId)
        terminal
    }
    fun cancelGame(gameId: String) = synchronized(lock) {
        currentByGame.remove(gameId)?.let { terminalById[it.id] = DynamicGenerationTerminal.CANCELLED }
    }
    fun terminal(generation: DynamicRecommendationGeneration) = synchronized(lock) { terminalById[generation.id] }
}
