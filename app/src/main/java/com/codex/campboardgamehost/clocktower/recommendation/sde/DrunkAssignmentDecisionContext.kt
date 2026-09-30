package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef

/**
 * Policy-neutral required context for the late-bound Trouble Brewing Drunk decision.
 *
 * The canonical snapshot and rules-owned legal domain are read-only inputs. Optional enrichment and
 * candidate-specific consequence features remain outside this type until a policy version explicitly
 * authorizes them.
 */
internal data class DrunkAssignmentDecisionContext(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val sourceRevision: InformationDecisionRevision,
    val legalCandidates: List<TroubleBrewingDrunkCandidateRef>,
) {
    val decisionId: String = "setup:drunk-seat:${snapshot.gameId}"
    val selectionSeed: Long = snapshot.gameSeed
    val legalCandidateIds: List<String> = legalCandidates.map { candidate ->
        "setup:drunk-seat:seat-${candidate.seat}"
    }

    init {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT) {
            "Drunk-assignment decision context requires a setup-precommit TB snapshot."
        }
        require(legalCandidates.isNotEmpty()) {
            "Drunk-assignment decision context requires at least one rules-legal candidate."
        }
        require(legalCandidates.map { it.seat }.distinct().size == legalCandidates.size) {
            "Drunk-assignment decision context candidate seats must be unique."
        }
    }
}

internal object DrunkAssignmentDecisionContextBuilder {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
        sourceRevision: InformationDecisionRevision,
    ): DrunkAssignmentDecisionContext =
        DrunkAssignmentDecisionContext(
            snapshot = snapshot,
            sourceRevision = sourceRevision,
            legalCandidates = TroubleBrewingDrunkCandidateDomain.legalCandidateRefs(
                snapshot = snapshot,
                characterRegistry = characterRegistry,
            ),
        )
}
