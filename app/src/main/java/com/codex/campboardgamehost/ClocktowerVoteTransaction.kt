package com.codex.campboardgamehost

/**
 * Atomic semantic result for one confirmed nomination vote.
 *
 * The transaction prepares the voter record (including ghost voters),
 * current high/tie standing, and execution candidate from the same immutable
 * pending table state. UI remains responsible only for collecting pending taps.
 */
internal data class ClocktowerVoteTransactionResult(
    val voteRecord: ClocktowerConfirmedVoteRecord,
    val highestVoteName: String?,
    val highestVoteCount: Int,
    val executionCandidateName: String?,
)

internal fun commitClocktowerVoteTransaction(
    voteState: ClocktowerTableVoteState,
    nomineeName: String,
    executionThreshold: Int,
    highestVoteName: String?,
    highestVoteCount: Int,
): ClocktowerVoteTransactionResult {
    require(nomineeName.isNotBlank()) { "Confirmed vote requires a nominee" }
    require(executionThreshold > 0) { "Execution threshold must be positive" }
    require(highestVoteCount >= 0) { "Highest vote count cannot be negative" }

    val voteRecord = voteState.confirmedVoteRecord()
    var nextHighestVoteName = highestVoteName
    var nextHighestVoteCount = highestVoteCount
    if (voteRecord.voteCount >= executionThreshold) {
        when {
            voteRecord.voteCount > highestVoteCount -> {
                nextHighestVoteName = nomineeName
                nextHighestVoteCount = voteRecord.voteCount
            }

            voteRecord.voteCount == highestVoteCount -> {
                nextHighestVoteName = null
            }
        }
    }

    val executionCandidateName = nextHighestVoteName
        ?.takeIf { nextHighestVoteCount >= executionThreshold }

    return ClocktowerVoteTransactionResult(
        voteRecord = voteRecord,
        highestVoteName = nextHighestVoteName,
        highestVoteCount = nextHighestVoteCount,
        executionCandidateName = executionCandidateName,
    )
}
