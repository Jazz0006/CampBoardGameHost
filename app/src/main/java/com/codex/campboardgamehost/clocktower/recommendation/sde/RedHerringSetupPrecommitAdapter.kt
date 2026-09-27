package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorAudit
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorKind
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

internal data class RedHerringSetupPrecommitCandidateProjection(
    val targetSeat: Int,
    val sdeCandidate: SdeDecisionCandidate,
    val proposedCommitRef: CommittedDecisionInputRef,
    val credibilityDisruptions: Set<CredibilityDisruptionImpact>,
) {
    init {
        require(targetSeat > 0) {
            "Red-Herring setup projection target seat must be positive."
        }
        require(sdeCandidate.lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
            "Red-Herring setup projection must remain at SetupPrecommit lifecycle."
        }
        require(proposedCommitRef.kind == SdeCommittedDecisionInputKind.RED_HERRING) {
            "Red-Herring setup projection requires a RED_HERRING commit reference."
        }
    }
}

internal data class RedHerringSetupPrecommitProjection(
    val candidates: List<RedHerringSetupPrecommitCandidateProjection>,
    val ruleDeterminedHealthySourceClaims: Set<TruthDangerExactSourceClaim>,
    val unresolvedHealthySourceRefs: Set<ConfirmationChannelRef.Source>,
    val excludedPlayerControlledElementIds: Set<String>,
) {
    val ruleDeterminedHealthySourceRefs: Set<ConfirmationChannelRef.Source>
        get() = ruleDeterminedHealthySourceClaims.mapTo(linkedSetOf(), TruthDangerExactSourceClaim::source)

    init {
        require(
            candidates.map { it.sdeCandidate.candidateId }.distinct().size == candidates.size,
        ) {
            "Red-Herring setup projection candidates must have unique IDs."
        }
        require(
            ruleDeterminedHealthySourceRefs.intersect(unresolvedHealthySourceRefs).isEmpty(),
        ) {
            "One healthy source cannot be both rule-determined and unresolved."
        }
        require(excludedPlayerControlledElementIds.none(String::isBlank)) {
            "Excluded player-controlled element IDs cannot be blank."
        }
    }
}

/**
 * Read-only setup-precommit adapter for legal Trouble Brewing Red-Herring candidates.
 *
 * Legality remains owned by [SetupCandidateGenerator]. This adapter never chooses or commits a
 * target. A [CommittedDecisionInputRef] is emitted only as the typed identity that would represent
 * the external setup commitment if the candidate is later selected; it is deliberately not placed
 * in [SdeDecisionInputBindings] before commitment.
 *
 * Fortune Teller target choice remains absent. The Night-1 candidate-space auditor is the source of
 * truth for which player-controlled elements are excluded from this setup-time projection.
 */
internal object RedHerringSetupPrecommitAdapter {
    private val drunk = RoleId("Drunk")

    fun project(
        gameId: String,
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceRevision: InformationDecisionRevision,
    ): RedHerringSetupPrecommitProjection {
        require(gameId.isNotBlank()) {
            "Red-Herring setup projection requires a game ID."
        }

        val audit = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(
            game = game,
            roleDefinitions = roleDefinitions,
        )
        val healthySourceFactors = audit.factors
            .asSequence()
            .filter { it.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO }
            .mapNotNull { factor ->
                val sourceSeat = factor.sourceSeat ?: return@mapNotNull null
                val player = game.playerAt(sourceSeat)
                    ?: error("First-night factor references missing seat $sourceSeat.")
                if (
                    !player.alive ||
                    player.poisoned ||
                    player.actualRole == drunk
                ) {
                    return@mapNotNull null
                }
                factor to ConfirmationChannelRef.Source(
                    sourceSeat = sourceSeat,
                    sourceAbility = player.actualRole,
                )
            }
            .toList()

        val ruleDeterminedClaims = healthySourceFactors
            .filter { (factor, _) -> factor.control == FirstNightBundleEntryControl.RULE_DETERMINED }
            .mapTo(linkedSetOf()) { (factor, source) ->
                materializeRuleDeterminedClaim(
                    game = game,
                    roleDefinitions = roleDefinitions,
                    factor = factor,
                    source = source,
                )
            }
        val unresolvedSources = healthySourceFactors
            .filter { (factor, _) -> factor.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED }
            .mapTo(linkedSetOf()) { (_, source) -> source }
        val healthySources = ruleDeterminedClaims.map(TruthDangerExactSourceClaim::source) + unresolvedSources

        val decisionId = "setup:red-herring:$gameId"
        val projectedCandidates = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate ->
                val targetSeat = (candidate.outcome as SetupClueOutcome.RedHerring).seat
                val commitRef = CommittedDecisionInputRef(
                    inputId = candidate.candidateId,
                    ownerId = "setup:red-herring",
                    kind = SdeCommittedDecisionInputKind.RED_HERRING,
                )
                val affectedSource = healthySources.singleOrNull { source ->
                    source.sourceSeat == targetSeat
                }
                val disruptions = affectedSource?.let { source ->
                    setOf(
                        CredibilityDisruptionImpact(
                            committedInputRef = commitRef,
                            affectedSource = source,
                            mechanism = CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
                        ),
                    )
                } ?: emptySet()

                RedHerringSetupPrecommitCandidateProjection(
                    targetSeat = targetSeat,
                    sdeCandidate = SdeDecisionCandidate(
                        decisionId = decisionId,
                        candidateId = candidate.candidateId,
                        lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
                        sourceInteraction = SdeDecisionSourceInteraction(
                            interactionId = decisionId,
                        ),
                        sourceRevision = sourceRevision,
                        inputBindings = SdeDecisionInputBindings.Captured(),
                        historyPrefixRef = SdeHistoricalPrefixRef.Global(
                            gameId = gameId,
                            actionRefs = emptyList(),
                            observationRefs = emptyList(),
                        ),
                        legalOutcomeIdentity = candidate.candidateId,
                        hypotheticalRef = SdeDecisionHypotheticalRef(
                            effectRefs = listOf(candidate.candidateId),
                        ),
                        legalityProvenance = SdeDecisionLegalityProvenance(
                            ownerId = "SetupCandidateGenerator",
                            candidateSpaceIdentity = "red-herring",
                        ),
                    ),
                    proposedCommitRef = commitRef,
                    credibilityDisruptions = disruptions,
                )
            }

        return RedHerringSetupPrecommitProjection(
            candidates = projectedCandidates,
            ruleDeterminedHealthySourceClaims = ruleDeterminedClaims,
            unresolvedHealthySourceRefs = unresolvedSources,
            excludedPlayerControlledElementIds = audit.excludedPlayerControlledElements,
        )
    }

    private fun materializeRuleDeterminedClaim(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        factor: FirstNightBundleCandidateFactorAudit,
        source: ConfirmationChannelRef.Source,
    ): TruthDangerExactSourceClaim {
        require(factor.kind == FirstNightBundleCandidateFactorKind.FIXED_NUMERIC_INFORMATION) {
            "Unsupported rule-determined healthy factor kind ${factor.kind}; add canonical materialization before projecting truth danger."
        }
        val sourceSeat = requireNotNull(source.sourceSeat)
        val sourceAbility = requireNotNull(source.sourceAbility)
        val truthValues = FirstNightNumericInformationSemantics
            .healthyTruthValues(game, sourceSeat)
            .toSortedSet()
        require(truthValues.size == 1) {
            "Rule-determined healthy numeric source must have exactly one canonical truth value."
        }
        val value = truthValues.single()
        require(factor.optionIds == listOf("value-$value")) {
            "Rule-determined healthy factor options drifted from canonical numeric semantics."
        }
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = sourceAbility,
                value = InformationValue.Number(value),
            ),
            roleDefinitions = roleDefinitions,
        )
        return TruthDangerExactSourceClaim(
            source = source,
            proposition = proposition,
        )
    }
}