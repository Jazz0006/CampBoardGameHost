package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import java.security.MessageDigest

/**
 * Stable non-Compose semantics extracted from the legacy Clocktower host monolith.
 *
 * Keep signatures and record identity byte-for-byte compatible while the host screen
 * is decomposed. This file intentionally remains in the existing package during the
 * behavior-preserving extraction phase.
 */
internal fun isClocktowerEvil(card: PlayerCard): Boolean =
    card.clocktowerTeam == ClocktowerTeam.Minion || card.clocktowerTeam == ClocktowerTeam.Demon

internal fun PlayerCard.clocktowerShownAsDifferentRole(): Boolean =
    clocktowerRole?.enName != null && clocktowerShownRole?.enName != null && clocktowerRole?.enName != clocktowerShownRole?.enName

internal fun clocktowerDisplayedInformationIsUnreliable(
    step: ClocktowerNightStepUi,
    actorIsUnreliable: (String, PlayerCard?) -> Boolean,
): Boolean =
    step.actor?.clocktowerRole?.enName == "Drunk" ||
        step.roleEnName?.let { roleEnName -> actorIsUnreliable(roleEnName, step.actor) } == true

/**
 * Recover only a value that is explicitly present on the player-facing display.
 * Never substitute the real game answer for a Drunk/poisoned player's shown result.
 * Role/pair/grimoire claims require the selected option's typed proposition.
 */
internal fun clocktowerShownUnreliableProposition(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    allSeats: List<Int>,
    empathNeighborSeats: List<Int>,
    fortuneTellerSubjectSeats: List<Int>,
): InformationProposition? {
    val authoritative = step.informationDecisionConfirmation?.draft?.proposition
        ?: step.displayProposition
    // Numeric information is compared against the literal number on the player display.
    // A stale truthful proposition cannot be substituted for a different fabricated number.
    if (authoritative != null) {
        if (step.displayKind == ClocktowerDisplayKind.Number &&
            authoritative is InformationProposition.NumericResult
        ) {
            val shownNumber = (step.displayPrimary ?: step.tellPlayer)?.trim()?.toIntOrNull()
            if (shownNumber != authoritative.value) return null
        }
        if (step.displayKind == ClocktowerDisplayKind.YesNo &&
            authoritative is InformationProposition.BooleanResult
        ) {
            val shownBoolean = when ((step.displayPrimary ?: step.tellPlayer)?.trim()?.lowercase()) {
                "有", "是", "yes", "demon" -> true
                "没有", "否", "no", "no demon" -> false
                else -> return null
            }
            if (shownBoolean != authoritative.value) return null
        }
        return authoritative
    }
    if (step.displayKind == ClocktowerDisplayKind.YesNo && step.roleEnName == "Fortune Teller") {
        if (fortuneTellerSubjectSeats.size != 2 ||
            fortuneTellerSubjectSeats.distinct().size != 2 ||
            fortuneTellerSubjectSeats.any { it !in allSeats }
        ) return null
        val shownBoolean = when ((step.displayPrimary ?: step.tellPlayer)?.trim()?.lowercase()) {
            "有", "是", "yes", "demon" -> true
            "没有", "否", "no", "no demon" -> false
            else -> return null
        }
        return InformationProposition.BooleanResult(
            com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            actorSeat, fortuneTellerSubjectSeats, shownBoolean,
        )
    }
    if (step.displayKind != ClocktowerDisplayKind.Number) return null
    val shown = (step.displayPrimary ?: step.tellPlayer)?.trim()?.toIntOrNull()
        ?.takeIf { it >= 0 } ?: return null
    val metricAndSubjects = when (step.roleEnName) {
        "Chef" -> com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.ADJACENT_EVIL_PAIRS to allSeats
        "Empath" -> com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.LIVING_EVIL_NEIGHBOURS to empathNeighborSeats
        "Clockmaker" -> com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.STEPS_TO_NEAREST_MINION to emptyList()
        "Chambermaid" -> {
            if (step.presentationSubjectSeats.size != 2) return null
            com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.PLAYERS_WAKING_FOR_ABILITY to
                step.presentationSubjectSeats
        }
        else -> return null
    }
    if (metricAndSubjects.second.any { it !in allSeats }) return null
    return InformationProposition.NumericResult(
        metricAndSubjects.first, actorSeat, metricAndSubjects.second, shown,
    )
}

internal fun clocktowerPrivateObservationRecordId(
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    roleEnName: String,
    actorSeat: Int,
    proposition: InformationProposition,
): String {
    val statementKey = MessageDigest
        .getInstance("SHA-256")
        .digest(EpistemicSemanticJson.encode(proposition).toByteArray(Charsets.UTF_8))
        .joinToString("") { byte ->
            (byte.toInt() and 0xff).toString(16).padStart(2, '0')
        }
    return "private-$gameId-${phase.name}-$round-$roleEnName-$actorSeat-$statementKey"
}

internal fun clocktowerRedHerringCandidates(aliveCards: List<PlayerCard>): List<PlayerCard> =
    aliveCards.filter { card -> card.clocktowerTeam?.isLegalRedHerringTeam() == true }

internal fun actualClocktowerRoleCards(cards: List<PlayerCard>, enName: String): List<PlayerCard> =
    cards.filter { it.clocktowerRole?.enName == enName }

internal fun chefEvilPairs(cards: List<PlayerCard>, isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil): Int {
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.chefEvilPairs(cards.toClocktowerPlayerStates()) { it.seat in evilSeats }
}

internal fun livingNeighbors(cards: List<PlayerCard>, playerName: String): List<PlayerCard> {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return emptyList()
    val neighborSeats = FixedInformationEvaluator
        .livingNeighbors(cards.toClocktowerPlayerStates(), sourceSeat)
        .map { it.seat }
    return neighborSeats.mapNotNull { seat -> cards.getOrNull(seat - 1) }
}

internal fun empathEvilNeighborCount(
    cards: List<PlayerCard>,
    playerName: String,
    isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil,
): Int {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return 0
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.empathEvilNeighborCount(cards.toClocktowerPlayerStates(), sourceSeat) {
        it.seat in evilSeats
    }
}

internal fun <T> clocktowerFortuneTellerRoleAuthority(
    phase: ClocktowerPhase,
    baseRole: T?,
    otherNightRole: () -> T?,
): T? = if (phase == ClocktowerPhase.Night) otherNightRole() else baseRole

internal fun clocktowerOtherNightWakingRoleIds(
    cards: List<PlayerCard>,
    pendingSuccessionDemonRoleId: RoleId?,
): Set<RoleId> = buildSet {
    cards.filter { it.eliminatedRound == null }.forEach { card ->
        card.clocktowerRole?.enName?.let { add(RoleId(it)) }
        if (card.clocktowerRole?.enName == "Drunk") {
            card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }
        }
    }
    pendingSuccessionDemonRoleId?.let(::add)
}

internal fun storytellerPairHint(
    target: PlayerCard,
    cards: List<PlayerCard>,
    fallbackPool: List<PlayerCard> = cards,
    excludeNames: Set<String> = emptySet(),
): Pair<PlayerCard, PlayerCard>? {
    val decoy = fallbackPool.firstOrNull { it.name != target.name && it.name !in excludeNames }
        ?: fallbackPool.firstOrNull { it.name != target.name }
        ?: return null
    return target to decoy
}
