package com.codex.campboardgamehost.clocktower.history

/**
 * Extracts referenced seat numbers from presentation-oriented information notes.
 * This parser is for display references only, not authoritative game history.
 */
internal object InformationReferenceExtractor {
    fun extractSeatNumbers(values: List<String?>, maximumSeat: Int): Set<Int> {
        if (maximumSeat <= 0) return emptySet()
        return values.filterNotNull()
            .flatMap { value -> Regex("\\d+").findAll(value).map { it.value.toIntOrNull() }.toList() }
            .filterNotNull()
            .filter { it in 1..maximumSeat }
            .toSet()
    }
}
