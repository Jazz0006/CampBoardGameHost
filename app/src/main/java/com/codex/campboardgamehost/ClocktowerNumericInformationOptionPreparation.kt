package com.codex.campboardgamehost


internal fun previousClocktowerUnreliableNumber(
    events: List<ClocktowerEvent>,
    title: String,
    actorName: String,
): Int? = events
    .asReversed()
    .firstOrNull { event ->
        event.type == ClocktowerEventType.UnreliableInformation &&
            actorName in event.playerNames &&
            event.title.startsWith(title)
    }
    ?.detail
    ?.let { detail ->
        val payload = when {
            "：" in detail -> detail.substringAfter("：")
            ": " in detail -> detail.substringAfter(": ")
            else -> detail
        }
        Regex("\\d+").find(payload)?.value?.toIntOrNull()
    }
