package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
