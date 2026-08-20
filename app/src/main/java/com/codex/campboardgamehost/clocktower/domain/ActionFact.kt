package com.codex.campboardgamehost.clocktower.domain

/**
 * A committed, replayable mechanical fact. These facts intentionally contain no
 * localized text or UI callbacks: the ordered list is the persisted timeline.
 */
sealed interface ActionFact {
    val actionId: String
    val sequence: Long

    data class Poison(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int?,
    ) : ActionFact

    data class Protect(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
    ) : ActionFact

    data class Attack(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
    ) : ActionFact

    data class Execution(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
    ) : ActionFact

    data class Death(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
    ) : ActionFact

    data class RoleChange(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
        val role: RoleId,
        val alignment: Alignment,
        val type: CharacterType,
    ) : ActionFact

    data class PhaseAdvance(
        override val actionId: String,
        override val sequence: Long,
        val phase: StorytellerPhase,
        val round: Int,
    ) : ActionFact
}

data class ReducedDynamicGameState(
    val snapshot: GameSnapshot,
    val phase: StorytellerPhase,
    val round: Int,
    val protectedSeats: Set<Int>,
    val pendingAttackSeat: Int?,
    val actionFacts: List<ActionFact>,
)

/** Pure ordered reducer used both online and after restore. */
object DynamicActionReducer {
    fun reduce(
        initialSnapshot: GameSnapshot,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        facts: List<ActionFact>,
    ): ReducedDynamicGameState {
        require(initialRound > 0)
        require(facts.map { it.actionId }.distinct().size == facts.size) { "Action IDs must be unique." }
        require(facts.map { it.sequence }.distinct().size == facts.size) { "Action sequences must be unique." }
        val ordered = facts.sortedWith(compareBy<ActionFact>({ it.sequence }, { it.actionId }))
        var game = initialSnapshot.gameState
        var phase = initialPhase
        var round = initialRound
        var protected = emptySet<Int>()
        var attack: Int? = null

        fun requireSeat(seat: Int) {
            require(game.playerAt(seat) != null) { "Action references unknown seat $seat." }
        }
        fun updatePlayer(seat: Int, transform: (PlayerState) -> PlayerState) {
            requireSeat(seat)
            game = game.copy(players = game.players.map { if (it.seat == seat) transform(it) else it })
        }

        ordered.forEach { fact ->
            require(fact.actionId.isNotBlank() && fact.sequence >= 0) { "Action ID and sequence must be valid." }
            when (fact) {
                is ActionFact.Poison -> {
                    fact.targetSeat?.let(::requireSeat)
                    game = game.copy(players = game.players.map { it.copy(poisoned = it.seat == fact.targetSeat) })
                }
                is ActionFact.Protect -> {
                    requireSeat(fact.targetSeat)
                    protected = protected + fact.targetSeat
                }
                is ActionFact.Attack -> {
                    requireSeat(fact.targetSeat)
                    attack = fact.targetSeat
                }
                is ActionFact.Execution -> updatePlayer(fact.targetSeat) { it.copy(alive = false, poisoned = false) }
                is ActionFact.Death -> updatePlayer(fact.targetSeat) { it.copy(alive = false, poisoned = false) }
                is ActionFact.RoleChange -> updatePlayer(fact.targetSeat) {
                    it.copy(actualRole = fact.role, actualAlignment = fact.alignment, actualType = fact.type)
                }
                is ActionFact.PhaseAdvance -> {
                    require(fact.round > 0)
                    phase = fact.phase
                    round = fact.round
                    protected = emptySet()
                    attack = null
                }
            }
        }
        return ReducedDynamicGameState(
            snapshot = initialSnapshot.copy(
                gameStateRevision = initialSnapshot.gameStateRevision + ordered.size,
                gameState = game,
            ),
            phase = phase,
            round = round,
            protectedSeats = protected,
            pendingAttackSeat = attack,
            actionFacts = ordered,
        )
    }
}
