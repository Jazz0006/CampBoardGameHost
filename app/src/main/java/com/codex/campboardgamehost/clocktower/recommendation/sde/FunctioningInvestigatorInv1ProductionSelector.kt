package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext

internal object FunctioningInvestigatorInv1ReasonCodes {
    val PRESERVES_DEMON_DENIABILITY_UNDER_EMPATH_PRESSURE =
        PolicyReasonCode("preserves-demon-deniability-under-empath-pressure")
}

internal data class FunctioningInvestigatorInv1ProductionSelection(
    val candidateId: String,
    val policyVersion: PolicyVersion = PolicyVersions.FUNCTIONING_INVESTIGATOR_INV1_V1,
    val reasonCodes: Set<PolicyReasonCode>,
) {
    init {
        require(candidateId.isNotBlank()) {
            "Functioning Investigator INV1 selection requires a candidate ID."
        }
        require(reasonCodes.isNotEmpty()) {
            "Functioning Investigator INV1 selection requires an evidence-backed reason."
        }
    }
}

/**
 * Evidence-bounded production selector for EL-LRE INV1-A.
 *
 * Scope is deliberately narrow:
 * - Trouble Brewing, first night, functioning Investigator;
 * - exactly one actual Minion;
 * - exactly one actual Empath;
 * - the Empath is seated between the Demon and an actual Townsfolk.
 *
 * Under that condition the verified evidence prefers a truthful Investigator pair containing the
 * Empath's Townsfolk neighbour and the real Minion, preserving Demon deniability without removing
 * the genuine Minion lead.
 *
 * Outside this exact predicate the selector fails closed. It does not fall back to V1, legacy
 * DynamicCandidateGenerator ranking, or any inferred ordering among other legal Investigator clues.
 */
internal object FunctioningInvestigatorInv1ProductionSelector {
    private val troubleBrewing = ScriptId("trouble_brewing")
    private val investigator = RoleId("Investigator")
    private val empath = RoleId("Empath")

    fun select(
        context: TroubleBrewingFirstNightPairDecisionContext,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        decisionId: String,
        selectionSeed: Long,
    ): FunctioningInvestigatorInv1ProductionSelection? {
        if (context.snapshot.script != troubleBrewing) return null
        if (abilityRole != investigator) return null
        if (reliability != ReliabilityState.RELIABLE) return null
        if (sourceSeat <= 0 || decisionId.isBlank()) return null

        val game = context.naturalPairGameState
        val players = game.players.sortedBy { it.seat }
        val empathPlayer = players.singleOrNull { it.actualRole == empath } ?: return null
        val empathIndex = players.indexOf(empathPlayer)
        if (empathIndex < 0 || players.size < 3) return null

        val left = players[(empathIndex - 1 + players.size) % players.size]
        val right = players[(empathIndex + 1) % players.size]
        val neighbours = listOf(left, right)
        if (neighbours.count { it.actualType == CharacterType.DEMON } != 1) return null
        val townsfolkNeighbour =
            neighbours.singleOrNull { it.actualType == CharacterType.TOWNSFOLK } ?: return null

        val realMinion = players.singleOrNull { it.actualType == CharacterType.MINION } ?: return null
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = context.roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
        if (legalCandidates.isEmpty()) return null
        if (legalCandidates.any { it.semanticTruth != SemanticTruth.TRUE }) return null

        val preferredIds = legalCandidates
            .filter { candidate ->
                candidate.registrations.isEmpty() &&
                    candidate.outcome.shownRole == realMinion.actualRole &&
                    candidate.outcome.candidateSeats.toSet() ==
                    setOf(townsfolkNeighbour.seat, realMinion.seat)
            }
            .map { it.candidateId }

        if (preferredIds.isEmpty()) return null
        val selectedId = PolicySeededHashSelector.select(
            candidateIds = preferredIds,
            decisionId = decisionId,
            selectionSeed = selectionSeed,
            hashPolicyVersion = PolicyVersions.FUNCTIONING_INVESTIGATOR_INV1_V1,
        )
        return FunctioningInvestigatorInv1ProductionSelection(
            candidateId = selectedId,
            reasonCodes = setOf(
                FunctioningInvestigatorInv1ReasonCodes
                    .PRESERVES_DEMON_DENIABILITY_UNDER_EMPATH_PRESSURE,
            ),
        )
    }
}
