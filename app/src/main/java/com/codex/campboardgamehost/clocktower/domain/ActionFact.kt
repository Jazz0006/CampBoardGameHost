package com.codex.campboardgamehost.clocktower.domain

enum class GameEndReason { NO_DEMON, TWO_ALIVE, SAINT, MAYOR, KLUTZ }

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
        /** Private explicit answer only; null means no registration was confirmed. */
        val targetRegistersAsDemon: Boolean? = null,
    ) : ActionFact

    /** The confirmed public nomination; first-Virgin consumption is rules-owned mechanical history. */
    data class Nomination(
        override val actionId: String,
        override val sequence: Long,
        val nominatorSeat: Int,
        val nomineeSeat: Int,
        val firstVirginNomination: Boolean,
        /** Private explicit answer only; never infer role or alignment from this type. */
        val nominatorRegistersAsTownsfolk: Boolean? = null,
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

    data class KlutzChoice(
        override val actionId: String,
        override val sequence: Long,
        val klutzSeat: Int,
        val chosenSeat: Int,
        val deathActionId: String,
    ) : ActionFact

    data class RoleChange(
        override val actionId: String,
        override val sequence: Long,
        val targetSeat: Int,
        val role: RoleId,
        val alignment: Alignment,
        val type: CharacterType,
    ) : ActionFact

    /** The Butler's actually confirmed nightly master, not a UI draft. */
    data class ButlerMasterChoice(
        override val actionId: String,
        override val sequence: Long,
        val butlerSeat: Int,
        val masterSeat: Int,
    ) : ActionFact

    /** Accepted private Fortune Teller setup/override, including explicit clearing. */
    data class RedHerringChoice(
        override val actionId: String,
        override val sequence: Long,
        val selectedSeat: Int?,
    ) : ActionFact

    /** Host-confirmed Mayor bounce; both seats are private mechanical facts, not player observations. */
    data class MayorRedirectChoice(
        override val actionId: String,
        override val sequence: Long,
        val mayorSeat: Int,
        val targetSeat: Int,
    ) : ActionFact

    /** Accepted private Demon bluff role set/override, distinct from UI drafts. */
    data class DemonBluffChoice(
        override val actionId: String,
        override val sequence: Long,
        val roles: List<RoleId>,
    ) : ActionFact

    /** Actual Spy wake/table handoff, without a duplicate Grimoire snapshot. */
    data class NightWake(
        override val actionId: String,
        override val sequence: Long,
        val actorSeat: Int,
        val role: RoleId,
        val stepId: String,
    ) : ActionFact

    data class ArtistAnswer(
        override val actionId: String,
        override val sequence: Long,
        val claimantSeat: Int,
        val shownAnswer: Boolean,
        val abilityConsumed: Boolean,
    ) : ActionFact

    /** One terminal fact; UI only localizes its reason. */
    data class GameEnded(
        override val actionId: String,
        override val sequence: Long,
        val reason: GameEndReason,
        val subjectSeat: Int? = null,
    ) : ActionFact

    data class PhaseAdvance(
        override val actionId: String,
        override val sequence: Long,
        val phase: StorytellerPhase,
        val round: Int,
    ) : ActionFact
}
