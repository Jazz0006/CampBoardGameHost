package com.codex.campboardgamehost.clocktower.session

/** Accepted standing for the current day; a null nominee with positive high count is a tie. */
internal data class ClocktowerVoteStanding(
    val nomineeSeat: Int? = null,
    val count: Int = 0,
) {
    fun confirm(nomineeSeat: Int, count: Int, threshold: Int): ClocktowerVoteStanding {
        require(nomineeSeat > 0 && count >= 0 && threshold > 0)
        return when {
            count < threshold || count < this.count -> this
            count == this.count -> copy(nomineeSeat = null)
            else -> ClocktowerVoteStanding(nomineeSeat, count)
        }
    }
}
