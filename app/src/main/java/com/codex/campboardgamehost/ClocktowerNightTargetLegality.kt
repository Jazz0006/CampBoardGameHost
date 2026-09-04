package com.codex.campboardgamehost

internal fun clocktowerMonkTargetCards(
    cards: List<PlayerCard>,
    actorName: String?,
): List<PlayerCard> = cards.filter { card -> card.name != actorName }

internal fun clocktowerRavenkeeperTargetCards(
    cards: List<PlayerCard>,
): List<PlayerCard> = cards
