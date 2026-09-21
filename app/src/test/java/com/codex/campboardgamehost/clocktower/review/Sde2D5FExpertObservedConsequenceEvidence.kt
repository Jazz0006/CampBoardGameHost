package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologyHypotheticalBundleEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import java.io.File
import java.math.BigInteger

internal data class Sde2D5FExpertObservedConsequenceContext(
    val caseId: String,
    val game: GameState,
    val roleDefinitions: List<RoleDefinition>,
    val validatedRuleset: ValidatedClocktowerRuleset,
    val rulesetRef: RulesetRef,
    val formalSnapshotId: String,
    val knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot>,
    val exactContext: ExactHistoricalHypotheticalContext,
    val evaluationRecipientSeats: List<Int>,
)

internal data class Sde2D5FExpertObservedRecipientConsequence(
    val recipientSeat: Int,
    val prefixExactWorldCount: BigInteger,
    val candidateExactWorldCount: BigInteger,
    val prefixExactStructure: ExactWorldStructureDiagnostics,
    val candidateExactStructure: ExactWorldStructureDiagnostics,
    val prefixTopologyStructure: ExactWorldStructureDiagnostics,
    val candidateTopologyStructure: ExactWorldStructureDiagnostics,
) {
    val strategicParity: Boolean
        get() =
            prefixExactStructure.strategicWorldKeys == prefixTopologyStructure.strategicWorldKeys &&
                candidateExactStructure.strategicWorldKeys == candidateTopologyStructure.strategicWorldKeys
}

internal data class Sde2D5FExpertObservedCandidateConsequence(
    val candidateId: String,
    val byRecipient: List<Sde2D5FExpertObservedRecipientConsequence>,
) {
    init {
        require(candidateId.isNotBlank())
        require(byRecipient.isNotEmpty())
    }
}

internal data class Sde2D5FExpertObservedStageConsequence(
    val stageId: String,
    val prefixObservationCount: Int,
    val alternatives: List<Sde2D5FExpertObservedCandidateConsequence>,
) {
    init {
        require(stageId.isNotBlank())
        require(prefixObservationCount >= 0)
        require(alternatives.isNotEmpty())
        require(alternatives.map { it.candidateId }.distinct().size == alternatives.size)
    }
}

/**
 * Evidence-only consequence bridge for expert-observed cases.
 *
 * Legality is deliberately upstream. This owner only projects player-visible claims and compares an
 * already-committed prefix with legal candidate continuations through BOTH existing exact mechanical
 * enumeration and topology-first feasibility. Hidden reconstructed truth is not injected into player
 * knowledge: no actual role map, Baron profile, Red Herring seat, poison target, or registration
 * witness is added as setup knowledge.
 *
 * Registration witness identity remains evidence metadata for policy analysis, but the public
 * consequence is evaluated existentially because players observe the clue, not the Storyteller's
 * hidden interaction-local registration ruling.
 */
internal object Sde2D5FExpertObservedConsequenceProjector {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    fun context(
        caseId: String,
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): Sde2D5FExpertObservedConsequenceContext {
        val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "sde-2d5f-expert-observed",
            sourceRevision = "official",
        )
        val snapshot = GameSnapshot(
            gameId = "sde-2d5f-$caseId",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceivedRolesBySeat = game.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceivedRolesBySeat,
            observations = emptyList(),
            setupKnowledge = emptyList(),
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val exactContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = perceivedRolesBySeat,
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roleDefinitions,
        )
        val goodRecipients = game.players
            .filter { player -> player.actualAlignment == Alignment.GOOD }
            .map { player -> player.seat }
            .sorted()
        require(goodRecipients.isNotEmpty())

        return Sde2D5FExpertObservedConsequenceContext(
            caseId = caseId,
            game = game,
            roleDefinitions = roleDefinitions,
            validatedRuleset = validatedRuleset,
            rulesetRef = rulesetRef,
            formalSnapshotId = formal.snapshotId,
            knowledgeBySeat = knowledgeBySeat,
            exactContext = exactContext,
            evaluationRecipientSeats = goodRecipients,
        )
    }

    fun projectPublicClaim(
        context: Sde2D5FExpertObservedConsequenceContext,
        entryId: String,
        sequence: Int,
        sourceSeat: Int,
        sourceAbility: RoleId,
        proposition: com.codex.campboardgamehost.clocktower.epistemic.InformationProposition,
    ): EpistemicObservation {
        val privateObservation = EpistemicObservation(
            observationId = "expert-private-${context.caseId}-$entryId",
            snapshotId = context.formalSnapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = sequence,
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "expert-${context.caseId}-$entryId",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = entryId,
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = entryId,
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    fun evaluateCommittedPrefix(
        context: Sde2D5FExpertObservedConsequenceContext,
        stageId: String,
        committedPrefix: List<EpistemicObservation>,
        candidates: Map<String, EpistemicObservation>,
    ): Sde2D5FExpertObservedStageConsequence {
        require(candidates.isNotEmpty())
        require(candidates.keys.all { it.isNotBlank() })
        require(candidates.values.all { it.snapshotId == context.formalSnapshotId })
        require(committedPrefix.all { it.snapshotId == context.formalSnapshotId })

        val exactQueries = buildList {
            context.evaluationRecipientSeats.forEach { recipientSeat ->
                add(
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = bundleId(stageId, "prefix", recipientSeat),
                        recipientSeat = recipientSeat,
                        observations = committedPrefix,
                    ),
                )
                candidates.forEach { (candidateId, observation) ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = bundleId(stageId, candidateId, recipientSeat),
                            recipientSeat = recipientSeat,
                            observations = committedPrefix + observation,
                        ),
                    )
                }
            }
        }
        val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = context.validatedRuleset,
            context = context.exactContext,
            queries = exactQueries,
        )
        require(exact is ExactHypotheticalObservationBundleEvaluation.Ready) {
            "Expert-observed exact consequence evaluation deferred at stage $stageId."
        }
        val exactById = exact.diagnostics.associateBy { it.bundleId }

        val topologyById = linkedMapOf<String, com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleDiagnostics>()
        context.evaluationRecipientSeats.forEach { recipientSeat ->
            val queries = buildList {
                add(
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = bundleId(stageId, "prefix", recipientSeat),
                        recipientSeat = recipientSeat,
                        observations = committedPrefix,
                    ),
                )
                candidates.forEach { (candidateId, observation) ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = bundleId(stageId, candidateId, recipientSeat),
                            recipientSeat = recipientSeat,
                            observations = committedPrefix + observation,
                        ),
                    )
                }
            }
            val topology = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
                rulesetRef = context.rulesetRef,
                knowledge = context.knowledgeBySeat.getValue(recipientSeat),
                roleDefinitions = context.roleDefinitions,
                queries = queries,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
            require(topology is ExactStrategicTopologyBundleEvaluation.Ready) {
                "Expert-observed topology consequence evaluation deferred at stage $stageId."
            }
            topology.diagnostics.forEach { diagnostic ->
                topologyById[diagnostic.bundleId] = diagnostic
            }
        }

        val alternatives = candidates.keys.map { candidateId ->
            Sde2D5FExpertObservedCandidateConsequence(
                candidateId = candidateId,
                byRecipient = context.evaluationRecipientSeats.map { recipientSeat ->
                    val prefixId = bundleId(stageId, "prefix", recipientSeat)
                    val candidateBundleId = bundleId(stageId, candidateId, recipientSeat)
                    val prefixExact = exactById.getValue(prefixId)
                    val candidateExact = exactById.getValue(candidateBundleId)
                    val prefixTopology = topologyById.getValue(prefixId)
                    val candidateTopology = topologyById.getValue(candidateBundleId)
                    Sde2D5FExpertObservedRecipientConsequence(
                        recipientSeat = recipientSeat,
                        prefixExactWorldCount = prefixExact.after.value,
                        candidateExactWorldCount = candidateExact.after.value,
                        prefixExactStructure = prefixExact.afterStructure,
                        candidateExactStructure = candidateExact.afterStructure,
                        prefixTopologyStructure = prefixTopology.afterStructure,
                        candidateTopologyStructure = candidateTopology.afterStructure,
                    )
                },
            )
        }

        return Sde2D5FExpertObservedStageConsequence(
            stageId = stageId,
            prefixObservationCount = committedPrefix.size,
            alternatives = alternatives,
        )
    }

    private fun bundleId(
        stageId: String,
        candidateId: String,
        recipientSeat: Int,
    ): String = "expert:$stageId:$candidateId:r$recipientSeat"
}
