package com.codex.campboardgamehost.clocktower.domain

enum class StorytellerPhase {
    FIRST_NIGHT,
    DAWN,
    DAY,
    NIGHT,
}

enum class StorytellerDecisionType {
    SPECIAL_REGISTRATION,
    UNRELIABLE_INFORMATION,
    MAYOR_DEATH_RESOLUTION,
    DEMON_SUCCESSION,
}

enum class PublicBalanceHint {
    UNKNOWN,
    GOOD_AHEAD,
    BALANCED,
    EVIL_AHEAD,
}

/**
 * Immutable state shared by recommendations made after setup.
 *
 * Free-form UI text is intentionally excluded. Recommendation inputs must be
 * stable, replayable facts instead of localized event descriptions.
 */
data class DynamicGameState(
    val game: GameState,
    val phase: StorytellerPhase,
    val round: Int,
    val protectedSeats: Set<Int> = emptySet(),
    val spentAbilitySeats: Set<Int> = emptySet(),
    val playerInformationPressureBySeat: Map<Int, PlayerInformationPressure> = emptyMap(),
    val misinformationLedger: MisinformationLedger = MisinformationLedger(),
    val registrationLedgerBySeat: Map<Int, RegistrationLedger> = emptyMap(),
    val publicBalanceHint: PublicBalanceHint = PublicBalanceHint.UNKNOWN,
    /** Positive means evil is ahead; negative means good is ahead. */
    val evilAdvantage: Int = 0,
) {
    init {
        require(round > 0)
        require(protectedSeats.all { game.playerAt(it) != null })
        require(spentAbilitySeats.all { game.playerAt(it) != null })
        require(playerInformationPressureBySeat.keys.all { game.playerAt(it) != null })
        require(playerInformationPressureBySeat.all { (seat, pressure) -> pressure.seat == seat })
        require(registrationLedgerBySeat.keys.all { game.playerAt(it) != null })
        require(evilAdvantage in -100..100)
    }
}

sealed interface DynamicStorytellerChoice {
    data class Registration(
        val subjectSeat: Int,
        val registeredAlignment: Alignment,
        val registeredType: CharacterType,
        val registeredRole: RoleId,
        val usesSpecialAbility: Boolean,
    ) : DynamicStorytellerChoice

    data class MayorDeathResolution(
        /** The Mayor's seat means the Mayor dies; any other seat is the redirect target. */
        val targetSeat: Int,
    ) : DynamicStorytellerChoice

    data class DemonSuccessor(
        val targetSeat: Int,
    ) : DynamicStorytellerChoice
}

sealed interface PredictedDecisionOutcome {
    data class Registration(
        val affectedAbility: RoleId,
        val subjectSeat: Int,
        val usesSpecialAbility: Boolean,
    ) : PredictedDecisionOutcome

    data class NightDeath(
        val attackedSeat: Int,
        val actualDeathSeat: Int?,
        val mayorSurvives: Boolean,
    ) : PredictedDecisionOutcome

    data class CharacterChange(
        val subjectSeat: Int,
        val fromRole: RoleId,
        val toRole: RoleId,
    ) : PredictedDecisionOutcome
}

data class DynamicDecisionCandidate(
    val choice: DynamicStorytellerChoice,
    val outcome: PredictedDecisionOutcome,
)

data class DynamicDecisionRequest(
    val id: String,
    val type: StorytellerDecisionType,
    val sourceAbility: RoleId,
    val state: DynamicGameState,
) {
    init {
        require(id.isNotBlank())
    }
}

data class DynamicDecisionRecommendation(
    val requestId: String,
    val candidate: DynamicDecisionCandidate,
    val style: RecommendationStyle,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val scoreItems: List<ScoreItem>,
    val warnings: List<PlanWarning>,
)
