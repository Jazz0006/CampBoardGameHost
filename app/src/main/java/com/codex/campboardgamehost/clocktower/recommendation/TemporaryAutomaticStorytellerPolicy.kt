package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

internal data class TemporaryAutomaticChoice<T>(
    val candidateId: String,
    val payload: T,
) {
    init {
        require(candidateId.isNotBlank()) { "Temporary automatic candidate ID cannot be blank." }
    }
}

internal data class TemporaryDemonSuccessorChoice(
    val seat: Int,
    val role: RoleId,
) {
    init {
        require(seat > 0) { "Demon successor seat must be positive." }
    }
}

internal data class TemporaryAutomaticSelection<T>(
    val selected: TemporaryAutomaticChoice<T>,
    val finalProbabilityByCandidate: Map<String, Long>,
    val decisionSeed: Long,
)

/**
 * Temporary product policy used until EPI-MQ replaces the recommendation provider.
 *
 * Inputs to this owner must already be rule-legal. It only applies the deliberately simple
 * automatic preference requested by UX-MODE-1 and delegates deterministic weighted choice to
 * [WeightedStableSelector].
 */
internal object TemporaryAutomaticStorytellerPolicy {
    private const val ACTUAL_REGISTRATION_FAMILY = "actual-registration"
    private const val SPECIAL_REGISTRATION_FAMILY = "special-registration"
    private const val MAYOR_DIES_FAMILY = "mayor-dies"
    private const val MAYOR_REDIRECT_FAMILY = "mayor-townfolk-redirect"
    private const val DEMON_SUCCESSOR_FAMILY = "demon-successor"

    private const val TEN_PERCENT = 100_000L
    private const val NINETY_PERCENT = 900_000L

    fun <T> selectRegistration(
        actual: TemporaryAutomaticChoice<T>,
        special: List<TemporaryAutomaticChoice<T>>,
        decisionSeed: Long,
    ): TemporaryAutomaticSelection<T> = select(
        choices = buildList {
            add(WeightedTemporaryChoice(actual, ACTUAL_REGISTRATION_FAMILY, 1L))
            special.forEach { add(WeightedTemporaryChoice(it, SPECIAL_REGISTRATION_FAMILY, 1L)) }
        },
        familyBudget = FamilyProbabilityBudget(
            mapOf(
                ACTUAL_REGISTRATION_FAMILY to TEN_PERCENT,
                SPECIAL_REGISTRATION_FAMILY to NINETY_PERCENT,
            ),
        ),
        decisionSeed = decisionSeed,
    )

    fun selectMayorRedirect(
        mayorSeat: Int,
        livingTownsfolkSeats: List<Int>,
        decisionSeed: Long,
    ): TemporaryAutomaticSelection<Int> {
        require(mayorSeat > 0) { "Mayor seat must be positive." }
        require(livingTownsfolkSeats.all { it > 0 && it != mayorSeat }) {
            "Mayor redirect candidates must be positive non-Mayor seats."
        }
        require(livingTownsfolkSeats.distinct().size == livingTownsfolkSeats.size) {
            "Mayor redirect candidate seats must be unique."
        }

        return select(
            choices = buildList {
                add(
                    WeightedTemporaryChoice(
                        TemporaryAutomaticChoice("mayor-dies:$mayorSeat", mayorSeat),
                        MAYOR_DIES_FAMILY,
                        1L,
                    ),
                )
                livingTownsfolkSeats.forEach { seat ->
                    add(
                        WeightedTemporaryChoice(
                            TemporaryAutomaticChoice("townsfolk-redirect:$seat", seat),
                            MAYOR_REDIRECT_FAMILY,
                            1L,
                        ),
                    )
                }
            },
            familyBudget = FamilyProbabilityBudget(
                mapOf(
                    MAYOR_DIES_FAMILY to TEN_PERCENT,
                    MAYOR_REDIRECT_FAMILY to NINETY_PERCENT,
                ),
            ),
            decisionSeed = decisionSeed,
        )
    }

    fun selectDemonSuccessor(
        eligible: List<TemporaryDemonSuccessorChoice>,
        decisionSeed: Long,
    ): TemporaryAutomaticSelection<TemporaryDemonSuccessorChoice>? {
        if (eligible.isEmpty()) return null
        require(eligible.map { it.seat }.distinct().size == eligible.size) {
            "Demon successor seats must be unique."
        }

        return select(
            choices = eligible.map { successor ->
                WeightedTemporaryChoice(
                    choice = TemporaryAutomaticChoice(
                        candidateId = "successor:${successor.seat}:${successor.role.value}",
                        payload = successor,
                    ),
                    familyId = DEMON_SUCCESSOR_FAMILY,
                    withinFamilyWeight = demonSuccessorWeight(successor.role),
                )
            },
            familyBudget = FamilyProbabilityBudget(mapOf(DEMON_SUCCESSOR_FAMILY to 1L)),
            decisionSeed = decisionSeed,
        )
    }

    private fun demonSuccessorWeight(role: RoleId): Long = when (role.value) {
        "Baron" -> 4L
        "Scarlet Woman" -> 3L
        "Spy" -> 2L
        "Poisoner" -> 1L
        else -> 1L
    }

    private fun <T> select(
        choices: List<WeightedTemporaryChoice<T>>,
        familyBudget: FamilyProbabilityBudget,
        decisionSeed: Long,
    ): TemporaryAutomaticSelection<T> {
        require(choices.isNotEmpty()) { "Temporary automatic selection requires at least one candidate." }
        val evaluations = choices.map { weighted ->
            DecisionEvaluation(
                candidate = DecisionCandidate(
                    candidateId = weighted.choice.candidateId,
                    candidateFamilyId = weighted.familyId,
                    outcome = weighted.choice.payload,
                    abilityState = AbilityState.FUNCTIONING,
                    truthRelation = TruthRelation.NOT_APPLICABLE,
                    metadata = CandidateMetadata(
                        candidateSchemaVersion = "temporary-auto-v1",
                        decisionType = "temporary-storyteller-auto",
                    ),
                ),
                qualityTier = QualityTier.RECOMMENDED,
                totalScore = 0,
                withinFamilyWeightFixedPoint = weighted.withinFamilyWeight,
                finalProbabilityFixedPoint = 0L,
                pressureDelta = emptyMap(),
                warnings = emptyList(),
                explanationCodes = emptyList(),
            )
        }
        val weightedSelection = checkNotNull(
            WeightedStableSelector.select(
                pool = evaluations,
                familyBudget = familyBudget,
                decisionSeed = decisionSeed,
            ),
        )
        val selectedId = weightedSelection.selected.candidate.candidateId
        val selectedChoice = choices.single { it.choice.candidateId == selectedId }.choice
        return TemporaryAutomaticSelection(
            selected = selectedChoice,
            finalProbabilityByCandidate = weightedSelection.finalProbabilityByCandidate,
            decisionSeed = weightedSelection.decisionSeed,
        )
    }

    private data class WeightedTemporaryChoice<T>(
        val choice: TemporaryAutomaticChoice<T>,
        val familyId: String,
        val withinFamilyWeight: Long,
    )
}
