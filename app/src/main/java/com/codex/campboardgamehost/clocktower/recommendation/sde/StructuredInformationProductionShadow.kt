package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Production-facing, read-only bridge for structured SDE shadow evaluation.
 *
 * Durable setup identity comes from [CommittedClocktowerSetup]. Current history and freshness come
 * from the canonical [GameSnapshot] supplied by ClocktowerGameSession. This bridge owns neither of
 * those facts and never writes back to either source.
 *
 * [evaluateFirstNight] retains the original round-one compatibility boundary. [evaluateHistorical]
 * replays the same immutable setup baseline through the canonical committed action/observation
 * prefix, allowing later interactions without introducing a second historical state owner.
 */
internal object StructuredInformationProductionShadow {
    fun <T : DynamicInformationOutcome> evaluateFirstNight(
        decisionContext: InformationDecisionContext<T>,
        validatedRuleset: ValidatedClocktowerRuleset,
        committedSetup: CommittedClocktowerSetup,
        currentSnapshot: GameSnapshot,
        roleDefinitions: Collection<RoleDefinition>,
        inputBindings: SdeDecisionInputBindings = SdeDecisionInputBindings.NotCaptured,
        hypothesis: EpistemicHypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    ): StructuredInformationShadowEvaluation {
        require(decisionContext.legalCandidates.all { candidate ->
            candidate.draft.phase == StorytellerPhase.FIRST_NIGHT && candidate.draft.round == 1
        }) {
            "First-night structured shadow only accepts round-one FIRST_NIGHT candidates."
        }
        return evaluateHistorical(
            decisionContext = decisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = committedSetup,
            currentSnapshot = currentSnapshot,
            roleDefinitions = roleDefinitions,
            inputBindings = inputBindings,
            hypothesis = hypothesis,
        )
    }

    /**
     * Lifecycle-safe read-only shadow over the canonical committed prefix.
     *
     * The immutable setup supplies the replay baseline; the current snapshot supplies the complete
     * committed action/observation prefix. The candidate itself may belong to any lifecycle point at
     * or after that baseline. This method still owns no production recommendation or commit action.
     */
    fun <T : DynamicInformationOutcome> evaluateHistorical(
        decisionContext: InformationDecisionContext<T>,
        validatedRuleset: ValidatedClocktowerRuleset,
        committedSetup: CommittedClocktowerSetup,
        currentSnapshot: GameSnapshot,
        roleDefinitions: Collection<RoleDefinition>,
        inputBindings: SdeDecisionInputBindings = SdeDecisionInputBindings.NotCaptured,
        hypothesis: EpistemicHypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    ): StructuredInformationShadowEvaluation {
        require(currentSnapshot.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "Structured production shadow requires GLOBAL_V1 semantic history."
        }
        require(decisionContext.requestIdentity.gameId == currentSnapshot.gameId) {
            "Structured production shadow requires the information request to belong to the current game."
        }
        require(committedSetup.script == currentSnapshot.gameState.script) {
            "Committed setup and current snapshot must use the same script."
        }
        require(committedSetup.setupSeed == currentSnapshot.gameSeed) {
            "Committed setup and current snapshot must use the same game seed."
        }
        val currentRevision = InformationDecisionRevision(
            gameStateRevision = currentSnapshot.gameStateRevision,
            playerInputRevision = currentSnapshot.playerInputRevision,
        )
        require(decisionContext.snapshot.revision == currentRevision) {
            "Structured production shadow requires the current session revision."
        }

        val rolesById = roleDefinitions.associateBy(RoleDefinition::id)
        require(rolesById.isNotEmpty()) {
            "Structured production shadow requires role definitions."
        }
        val setupBySeat = committedSetup.assignments.associateBy { assignment -> assignment.seat }
        val currentSeats = currentSnapshot.gameState.players.map { player -> player.seat }.toSet()
        require(setupBySeat.keys == currentSeats) {
            "Committed setup seats must match the current session seats."
        }

        val baselinePlayers = currentSnapshot.gameState.players.map { currentPlayer ->
            val assignment = setupBySeat.getValue(currentPlayer.seat)
            val actualDefinition = requireNotNull(rolesById[assignment.actualRole]) {
                "Missing role definition for committed role ${assignment.actualRole.value}."
            }
            currentPlayer.copy(
                actualRole = assignment.actualRole,
                actualAlignment = actualDefinition.alignment,
                actualType = actualDefinition.type,
                shownRole = assignment.shownRole,
                alive = true,
                poisoned = false,
            )
        }
        val initialSnapshot = currentSnapshot.copy(
            gameState = currentSnapshot.gameState.copy(players = baselinePlayers),
            actionTimeline = ActionFactTimeline(),
            epistemicObservationLog = EpistemicObservationLog(),
            nextTimelineGlobalSequence = 0L,
        )
        val perceivedRolesBySeat = committedSetup.assignments.associate { assignment ->
            assignment.seat to assignment.shownRole
        }

        return StructuredInformationShadowAdapter.evaluate(
            decisionContext = decisionContext,
            exactContext = ExactConsequenceContext(
                validatedRuleset = validatedRuleset,
                exactContext = ExactHistoricalHypotheticalContext(
                    initialSnapshot = initialSnapshot,
                    initialPhase = StorytellerPhase.FIRST_NIGHT,
                    initialRound = 1,
                    actionTimeline = currentSnapshot.actionTimeline,
                    perceivedRolesBySeat = perceivedRolesBySeat,
                    observationLog = currentSnapshot.epistemicObservationLog,
                    hypothesis = hypothesis,
                    roleDefinitions = roleDefinitions,
                ),
                sourceRevision = currentRevision,
            ),
            inputBindings = inputBindings,
        )
    }
}
