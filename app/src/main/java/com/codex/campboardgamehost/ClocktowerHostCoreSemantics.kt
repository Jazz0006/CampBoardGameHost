package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
