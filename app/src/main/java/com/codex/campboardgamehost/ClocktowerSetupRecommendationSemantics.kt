package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.kind

internal fun recommendationLocksWithCommittedSetupDecisions(
    mutableLocks: List<StorytellerDecision>,
    committedDecisions: List<StorytellerDecision>,
): List<StorytellerDecision> {
    val committedKinds = committedDecisions.map(StorytellerDecision::kind)
    require(committedKinds.distinct().size == committedKinds.size) {
        "Committed setup decision kinds must be unique."
    }
    val committedKindSet = committedKinds.toSet()
    return mutableLocks.filterNot { it.kind() in committedKindSet } + committedDecisions
}
