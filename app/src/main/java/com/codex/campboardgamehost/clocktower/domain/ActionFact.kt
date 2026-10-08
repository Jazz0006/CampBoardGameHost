package com.codex.campboardgamehost.clocktower.domain

/**
 * A committed, replayable mechanical fact. These facts intentionally contain no
 * localized text or UI callbacks: the ordered list is the persisted timeline.
 */
/**
 * Captured by the game-session authority before the Klutz's death changes alive/poisoned.
 * An absent field on pre-migration Death/Execution means UNKNOWN, never "functioning".
 */
data class KlutzDeathTriggerEvidenceV1(
    val actualRole: RoleId,
    val wasAlive: Boolean,
    val wasPoisoned: Boolean,
    val sourceGameStateRevision: Long,
) {
    init {
        require(actualRole.value == "Klutz")
        require(wasAlive)
        require(sourceGameStateRevision >= 0L)
    }
    val functioningAtDeath: Boolean get() = !wasPoisoned
}

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
        val klutzDeathTrigger: KlutzDeathTriggerEvidenceV1? = null,
    ) : ActionFact

    data class Death(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
        val klutzDeathTrigger: KlutzDeathTriggerEvidenceV1? = null,
    ) : ActionFact

    /**
     * The public day was explicitly resolved with no execution. Absent actions in
     * older histories are UNKNOWN, never evidence that no execution occurred.
     */
    data class NoExecution(
        override val actionId: String,
        override val sequence: Long,
    ) : ActionFact

    /** Confirmed public player attempt, not a Storyteller decision or a claim that a hit occurred. */
    data class SlayerShot(
        override val actionId: String,
        override val sequence: Long,
        val claimantSeat: Int,
        val targetSeat: Int,
        val abilityConsumed: Boolean,
        val hit: Boolean,
    ) : ActionFact

    /** The confirmed public nomination; first-Virgin consumption is rules-owned mechanical history. */
    data class Nomination(
        override val actionId: String,
        override val sequence: Long,
        val nominatorSeat: Int,
        val nomineeSeat: Int,
        val firstVirginNomination: Boolean,
    ) : ActionFact

    /** Voters are explicit seats; ghost voters remain a subset, never reconstructed from text. */
    data class Vote(
        override val actionId: String,
        override val sequence: Long,
        val nominatorSeat: Int,
        val nomineeSeat: Int,
        val voterSeats: List<Int>,
        val ghostVoterSeats: List<Int>,
    ) : ActionFact

    /**
     * The actual dead Klutz has been informed of death. This is NOT the death
     * action itself, and never publishes the Klutz's hidden role or impairment.
     */
    data class KlutzLearnedDeath(
        override val actionId: String,
        override val sequence: Long,
        val klutzSeat: Int,
        val deathActionId: String,
        val functioningWhenLearned: Boolean,
    ) : ActionFact

    /** Public player choice; the Storyteller neither selects the seat nor registers a role. */
    data class KlutzChoice(
        override val actionId: String,
        override val sequence: Long,
        val klutzSeat: Int,
        val chosenSeat: Int,
        val learnedActionId: String,
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
                is ActionFact.NoExecution -> Unit
                is ActionFact.SlayerShot -> {
                    requireSeat(fact.claimantSeat)
                    requireSeat(fact.targetSeat)
                    require(!fact.hit || fact.abilityConsumed)
                }
                is ActionFact.Nomination -> {
                    requireSeat(fact.nominatorSeat)
                    requireSeat(fact.nomineeSeat)
                    require(fact.nominatorSeat != fact.nomineeSeat)
                }
                is ActionFact.Vote -> {
                    requireSeat(fact.nominatorSeat)
                    requireSeat(fact.nomineeSeat)
                    require(fact.voterSeats.distinct().size == fact.voterSeats.size)
                    require(fact.ghostVoterSeats.distinct().size == fact.ghostVoterSeats.size)
                    require(fact.ghostVoterSeats.all { it in fact.voterSeats })
                    fact.voterSeats.forEach(::requireSeat)
                }
                is ActionFact.KlutzLearnedDeath -> {
                    requireSeat(fact.klutzSeat)
                    require(fact.deathActionId.isNotBlank())
                }
                is ActionFact.KlutzChoice -> {
                    requireSeat(fact.klutzSeat)
                    requireSeat(fact.chosenSeat)
                    require(fact.klutzSeat != fact.chosenSeat)
                    require(fact.learnedActionId.isNotBlank())
                }
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
