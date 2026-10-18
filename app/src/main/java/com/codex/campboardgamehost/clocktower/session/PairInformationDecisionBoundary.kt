package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain

/**
 * Compatibility input for the existing AI pair recommendation provider.
 * This object is not a Host publication/confirmation authority.
 * Any actual information, including a value outside these AI choices, is confirmed
 * by the ordinary Host observation writer, which does not call this candidate producer.
 */
internal data class PendingPairInformationDecision(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val legalCandidates: List<PairInformationLegalCandidate>,
) {
    init {
        require(sourceSeat > 0) { "Pair-information actor seat must be positive." }
        require(legalCandidates.map { it.candidateId }.distinct().size == legalCandidates.size)
    }
}

/**
 * AI-only candidate adaptation, pending replacement by independent read-only Rules.
 * The resulting options are sent to the model and matched to UI preselection;
 * none of them can reject a Host-confirmed manual observation.
 */
internal object PairInformationDecisionBoundary {
    fun create(
        requestIdentity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): PendingPairInformationDecision = PendingPairInformationDecision(
        requestIdentity = requestIdentity,
        revision = revision,
        sourceSeat = sourceSeat,
        abilityRole = abilityRole,
        reliability = reliability,
        legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        ),
    )
}

