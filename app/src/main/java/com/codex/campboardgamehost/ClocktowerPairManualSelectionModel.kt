package com.codex.campboardgamehost

/** Pure selection transitions over the supplied presentation; no proposition parsing or rules. */
internal data class ClocktowerPairManualSelectionModel private constructor(
    private val presentation: ClocktowerPairManualPresentation,
    val selectedRoleId: String? = null,
    val selectedFirstSeat: Int? = null,
    val selectedSecondSeat: Int? = null,
    val isZeroCaseSelected: Boolean = false,
) {
    private val pairCandidates get() = presentation.candidates
    private val zeroCaseOption get() = presentation.zeroCaseOption

    val roleIds: List<String>
        get() = pairCandidates.map { it.roleId }.distinct()

    val hasZeroCase: Boolean
        get() = zeroCaseOption != null

    val resolvedOption: ClocktowerDisplayOption?
        get() {
            if (isZeroCaseSelected) return zeroCaseOption
            val roleId = selectedRoleId ?: return null
            val first = selectedFirstSeat ?: return null
            val second = selectedSecondSeat ?: return null
            if (first == second) return null
            val selectedSeats = listOf(first, second).sorted()
            return pairCandidates.firstOrNull { candidate ->
                candidate.roleId == roleId && candidate.seats == selectedSeats
            }?.option
        }

    fun firstSeats(roleId: String): List<Int> = pairCandidates
        .asSequence()
        .filter { it.roleId == roleId }
        .flatMap { it.seats.asSequence() }
        .distinct()
        .sorted()
        .toList()

    fun secondSeats(roleId: String, firstSeat: Int): List<Int> = pairCandidates
        .asSequence()
        .filter { candidate -> candidate.roleId == roleId && firstSeat in candidate.seats }
        .mapNotNull { candidate -> candidate.seats.firstOrNull { it != firstSeat } }
        .distinct()
        .sorted()
        .toList()

    fun selectRole(roleId: String): ClocktowerPairManualSelectionModel {
        if (roleId !in roleIds) return this
        if (roleId == selectedRoleId && !isZeroCaseSelected) return this
        return copy(
            selectedRoleId = roleId,
            selectedFirstSeat = null,
            selectedSecondSeat = null,
            isZeroCaseSelected = false,
        )
    }

    fun selectZeroCase(): ClocktowerPairManualSelectionModel {
        if (!hasZeroCase) return this
        return copy(
            selectedRoleId = null,
            selectedFirstSeat = null,
            selectedSecondSeat = null,
            isZeroCaseSelected = true,
        )
    }

    fun clearChoice(): ClocktowerPairManualSelectionModel = copy(
        selectedRoleId = null,
        selectedFirstSeat = null,
        selectedSecondSeat = null,
        isZeroCaseSelected = false,
    )

    fun selectSeat(seatNumber: Int): ClocktowerPairManualSelectionModel {
        val roleId = selectedRoleId ?: return this
        val validFirstSeats = firstSeats(roleId)
        if (seatNumber !in validFirstSeats) return this

        val first = selectedFirstSeat
        val second = selectedSecondSeat
        if (first == null) {
            return copy(
                selectedFirstSeat = seatNumber,
                selectedSecondSeat = null,
                isZeroCaseSelected = false,
            )
        }

        if (second == null) {
            if (seatNumber == first) {
                return copy(selectedFirstSeat = null, selectedSecondSeat = null)
            }
            if (seatNumber in secondSeats(roleId, first)) {
                return copy(selectedSecondSeat = seatNumber)
            }
            return copy(selectedFirstSeat = seatNumber, selectedSecondSeat = null)
        }

        if (seatNumber == second) {
            return copy(selectedSecondSeat = null)
        }
        if (seatNumber == first) {
            return copy(selectedFirstSeat = second, selectedSecondSeat = null)
        }
        if (seatNumber in secondSeats(roleId, first)) {
            return copy(selectedSecondSeat = seatNumber)
        }

        val retainedSecond = second.takeIf { existingSecond ->
            existingSecond != seatNumber && existingSecond in secondSeats(roleId, seatNumber)
        }
        return copy(
            selectedFirstSeat = seatNumber,
            selectedSecondSeat = retainedSecond,
        )
    }

    companion object {
        internal fun from(presentation: ClocktowerPairManualPresentation): ClocktowerPairManualSelectionModel =
            ClocktowerPairManualSelectionModel(presentation)

        /**
         * Enters Manual editing from an already-authoritative displayed recommendation.
         *
         * The seed is accepted only when it is the exact option already present in the supplied
         * legal presentation. No localized label or proposition is reparsed here; stale or foreign
         * options therefore fail closed to an empty Manual selection.
         */
        internal fun from(
            presentation: ClocktowerPairManualPresentation,
            initialOption: ClocktowerDisplayOption?,
        ): ClocktowerPairManualSelectionModel {
            if (initialOption == null) return from(presentation)
            if (initialOption == presentation.zeroCaseOption) {
                return ClocktowerPairManualSelectionModel(
                    presentation = presentation,
                    isZeroCaseSelected = true,
                )
            }
            val candidate = presentation.candidates.firstOrNull { it.option == initialOption }
                ?: return from(presentation)
            return ClocktowerPairManualSelectionModel(
                presentation = presentation,
                selectedRoleId = candidate.roleId,
                selectedFirstSeat = candidate.seats.getOrNull(0),
                selectedSecondSeat = candidate.seats.getOrNull(1),
            )
        }
    }
}
