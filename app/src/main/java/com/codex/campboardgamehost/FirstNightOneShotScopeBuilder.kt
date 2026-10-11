package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.FirstNightOneShotDecisionScopeV1
import com.codex.campboardgamehost.clocktower.session.FirstNightOneShotScopeV1
import java.security.MessageDigest

/**
 * Host UI projection of all current TB first-night decisions, not a selector.
 * Builds on the Host's existing manual information candidate domain.
 *
 * PRE-POISONER: any information that could change after the player's target
 * choice is explicitly deferred. A one-shot result must not harden the
 * unobserved action into a fixed canonical first-night observation.
 */
/**
 * Compact opaque ID for model transfer. Resolve against the exact same Host
 * manual candidate set at the time of confirmation; never trust the hash alone.
 */
internal fun clocktowerOneShotCandidateId(option: ClocktowerDisplayOption): String {
    val key = clocktowerInformationCandidateId(option)
    val bytes = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
    return "fn-" + bytes.take(16).joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

internal fun clocktowerFirstNightOneShotScope(
    gameId: String,
    gameStateRevision: Long,
    playerInputRevision: Long,
    script: ClocktowerScript,
    cards: List<PlayerCard>,
    steps: List<ClocktowerNightStepUi>,
): FirstNightOneShotScopeV1 {
    require(script == ClocktowerScript.TroubleBrewing)
    require(gameId.isNotBlank())
    val shownNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet()
    val demonPresent = cards.any { it.clocktowerTeam == ClocktowerTeam.Demon }
    val needsBluffs = demonPresent && cards.size >= 7
    val bluffs = if (needsBluffs) legalDemonBluffRoles(
        scriptRoles = clocktowerRolesForScript(script),
        inPlayRoleNames = shownNames,
    ).map { it.enName }.distinct() else null

    val hasUnresolvedPoisoner = cards.any {
        it.clocktowerRole?.enName == "Poisoner"
    }
    val families = setOf(
        "Washerwoman", "Librarian", "Investigator", "Chef", "Empath", "Fortune Teller",
    )
    val available = mutableListOf<FirstNightOneShotDecisionScopeV1>()
    val deferred = linkedSetOf<String>()
    val seenSeats = mutableSetOf<Int>()
    steps.filter { it.isRealAction && it.roleEnName in families }.forEach { step ->
        val seat = cards.indexOfFirst { it.name == step.actor?.name }
            .takeIf { it >= 0 }?.plus(1) ?: return@forEach
        // Same perceived role can only require one first-night observation.
        if (!seenSeats.add(seat)) return@forEach
        val role = requireNotNull(step.roleEnName)
        val id = "first-night:$role:seat-$seat"
        if (role == "Fortune Teller") {
            deferred.add(id)
            return@forEach
        }
        // Only Host-published Manual legality is authoritative: no fallback
        // to curated/legacy recommender lists which may be subsets.
        val legal = step.manualInformationCandidates
            .distinctBy(::clocktowerOneShotCandidateId)
        if (legal.isEmpty()) {
            deferred.add(id)
            return@forEach
        }
        val descriptions = legal.associate { option ->
            clocktowerOneShotCandidateId(option) to buildString {
                append("value=").append(option.displayPrimary.orEmpty())
                option.displaySecondary?.let { append("; seats=").append(it) }
                option.displayFooter?.let { append("; note=").append(it) }
                append("; truthful=").append(option.isTruthful)
            }.take(600)
        }
        available.add(FirstNightOneShotDecisionScopeV1(
            decisionId = id,
            sourceSeat = seat,
            family = role,
            legalCandidateIds = descriptions.keys.toList(),
            candidateDescriptions = descriptions,
        ))
    }
    // Red Herring is the Storyteller's choice, and is legal only for an
    // actual Good player. Never treat an unchosen Fortune Teller query as fact.
    if (cards.any { it.clocktowerRole?.enName == "Fortune Teller" }) {
        val good = cards.mapIndexedNotNull { index, card ->
            if (card.clocktowerTeam in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider)) {
                index + 1
            } else null
        }
        if (good.isNotEmpty()) {
            available.add(FirstNightOneShotDecisionScopeV1(
                decisionId = "setup.red-herring",
                sourceSeat = null,
                family = "RED_HERRING",
                legalCandidateIds = good.map { "seat-$it" },
                candidateDescriptions = good.associate { seat ->
                    "seat-$seat" to "Actually Good seat $seat"
                },
            ))
        } else {
            deferred.add("setup.red-herring")
        }
    }
    // Pre-poison information is a CONDITIONAL provisional plan. The Poisoner
    // controls their own target; adoption must never commit a later observation
    // before the Host revalidates that selected option under the actual target.
    if (hasUnresolvedPoisoner) deferred.add("dependency.poisoner-target-unconfirmed")
    return FirstNightOneShotScopeV1(
        gameId = gameId,
        gameStateRevision = gameStateRevision,
        playerInputRevision = playerInputRevision,
        legalDemonBluffRoleIds = bluffs,
        availableDecisions = available.sortedBy { it.decisionId },
        deferredDecisionIds = deferred.toList().sorted(),
    )
}
