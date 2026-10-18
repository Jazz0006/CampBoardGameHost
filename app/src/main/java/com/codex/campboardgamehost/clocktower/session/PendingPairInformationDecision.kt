package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** The current recipient, ability and revision; no enumerated answers. */
internal data class PendingPairInformationDecision(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
)
