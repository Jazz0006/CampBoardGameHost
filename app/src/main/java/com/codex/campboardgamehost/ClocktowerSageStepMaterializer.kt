package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalRecommendation
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState

internal data class ClocktowerSageStepContent(
    val explanation: String,
    val displayTitle: String,
    val displayPrimary: String,
    val displayFooter: String,
    val hostInstruction: String,
    val highPressureSuffix: String,
)

internal fun clocktowerSageDisplayOptions(
    cards: List<PlayerCard>,
    actor: PlayerCard,
    demon: PlayerCard,
    truthfulOnly: Boolean,
    content: ClocktowerSageStepContent,
    recommendCategory: (List<UnreliableCategoricalCandidate>) -> List<UnreliableCategoricalRecommendation>,
    isEvil: (PlayerCard) -> Boolean,
    recommendationStyleLabel: (RecommendationStyle) -> String,
): List<ClocktowerDisplayOption> {
    val pool = cards.filter { it.name != actor.name }
    val pairs = buildList {
        for (firstIndex in 0 until pool.lastIndex) {
            for (secondIndex in firstIndex + 1 until pool.size) {
                add(pool[firstIndex] to pool[secondIndex])
            }
        }
    }
    if (pairs.isEmpty()) return emptyList()

    fun pairId(pair: Pair<PlayerCard, PlayerCard>): String =
        listOf(cards.indexOf(pair.first), cards.indexOf(pair.second))
            .sorted()
            .joinToString(":")

    val byId = pairs.associateBy(::pairId)
    val candidates = pairs.map { pair ->
        val isTruthful = pair.first.name == demon.name || pair.second.name == demon.name
        val evilCount = listOf(pair.first, pair.second).count(isEvil)
        UnreliableCategoricalCandidate(
            id = pairId(pair),
            isTruthful = isTruthful,
            misinformationPressure = if (isTruthful) {
                0
            } else {
                when (evilCount) {
                    1 -> 2
                    2 -> 3
                    else -> 4
                }
            },
        )
    }.filter { !truthfulOnly || it.isTruthful }

    return recommendCategory(candidates).mapNotNull { recommendation ->
        val candidate = candidates.firstOrNull { it.id == recommendation.candidateId }
            ?: return@mapNotNull null
        val pair = byId[recommendation.candidateId] ?: return@mapNotNull null
        val subjectSeats = clocktowerSageSubjectSeats(cards, pair.first, pair.second)
        if (subjectSeats.size != 2) return@mapNotNull null
        val seats = subjectSeats.joinToString("   ")
        val warning = if (recommendation.warningIds.isNotEmpty()) content.highPressureSuffix else ""
        ClocktowerDisplayOption(
            label = "${recommendationStyleLabel(recommendation.style)}：$seats$warning",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = content.displayTitle,
            displayPrimary = content.displayPrimary,
            displaySecondary = seats,
            displayFooter = content.displayFooter,
            proposition = null,
            presentationSubjectSeats = subjectSeats,
            recommendationStyle = recommendation.style,
            isTruthful = candidate.isTruthful,
            misinformationPressure = candidate.misinformationPressure,
            isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            reasonCodes = listOf("dynamic.pair-score"),
            warningCodes = recommendation.warningIds,
        )
    }
}

internal fun clocktowerSageStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    cards: List<PlayerCard>,
    triggerActor: PlayerCard?,
    demon: PlayerCard?,
    directPair: Pair<PlayerCard, PlayerCard>?,
    abilityState: AbilityFunctioningState?,
    content: ClocktowerSageStepContent,
    recommendCategory: (List<UnreliableCategoricalCandidate>) -> List<UnreliableCategoricalRecommendation>,
    isEvil: (PlayerCard) -> Boolean,
    recommendationStyleLabel: (RecommendationStyle) -> String,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Sage")),
    build = {
        val resolvedDemon = requireNotNull(demon)
        val resolvedDirectPair = requireNotNull(directPair)
        val resolvedTrigger = requireNotNull(triggerActor)
        val directSubjectSeats = clocktowerSageSubjectSeats(
            cards = cards,
            first = resolvedDemon,
            second = resolvedDirectPair.second,
        )
        builder.build(
            roleName = "贤者",
            enName = "Sage",
            actorOverride = resolvedTrigger,
            abilityStateOverride = abilityState,
            tellPlayer = "${resolvedDemon.seatLabel(cards)} / ${resolvedDirectPair.second.seatLabel(cards)}",
            explanation = content.explanation,
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = content.displayTitle,
            displayPrimary = content.displayPrimary,
            displaySecondary = directSubjectSeats.takeIf { it.size == 2 }?.joinToString("   "),
            displayFooter = content.displayFooter,
            presentationSubjectSeats = directSubjectSeats,
            hostInstruction = content.hostInstruction,
            displayOptions = { actor ->
                clocktowerSageDisplayOptions(
                    cards = cards,
                    actor = actor,
                    demon = resolvedDemon,
                    truthfulOnly = false,
                    content = content,
                    recommendCategory = recommendCategory,
                    isEvil = isEvil,
                    recommendationStyleLabel = recommendationStyleLabel,
                )
            },
            reliableDisplayOptions = { actor ->
                clocktowerSageDisplayOptions(
                    cards = cards,
                    actor = actor,
                    demon = resolvedDemon,
                    truthfulOnly = true,
                    content = content,
                    recommendCategory = recommendCategory,
                    isEvil = isEvil,
                    recommendationStyleLabel = recommendationStyleLabel,
                )
            },
        )
    },
)

private fun clocktowerSageSubjectSeats(
    cards: List<PlayerCard>,
    first: PlayerCard,
    second: PlayerCard,
): List<Int> {
    val seats = listOf(cards.indexOf(first) + 1, cards.indexOf(second) + 1)
    return seats.takeIf { values ->
        values.size == 2 &&
            values.distinct().size == 2 &&
            values.all { it in 1..cards.size }
    }.orEmpty()
}
