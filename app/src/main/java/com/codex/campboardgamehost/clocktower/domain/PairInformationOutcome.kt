package com.codex.campboardgamehost.clocktower.domain

data class PairInformationOutcome(
    val shownRole: RoleId?,
    val targetSeat: Int?,
    val decoySeat: Int?,
) {
    val candidateSeats: List<Int> = listOfNotNull(targetSeat, decoySeat).sorted()

    init {
        require((targetSeat == null) == (decoySeat == null)) {
            "Target and decoy must either both be present or both be absent."
        }
        require((shownRole == null) == (targetSeat == null)) {
            "A shown role requires a target pair; an empty result must not contain seats."
        }
        require(targetSeat == null || targetSeat > 0) { "targetSeat must be positive." }
        require(decoySeat == null || decoySeat > 0) { "decoySeat must be positive." }
        require(targetSeat == null || targetSeat != decoySeat) { "Target and decoy must be distinct." }
    }
}
