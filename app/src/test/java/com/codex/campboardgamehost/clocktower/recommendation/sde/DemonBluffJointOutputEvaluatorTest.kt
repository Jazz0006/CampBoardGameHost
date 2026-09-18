package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DemonBluffJointOutputEvaluatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d2-demon-bluff-test",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.roleDefinitions().filterNot { definition ->
        definition.id in setOf(RoleId("Poisoner"), RoleId("Spy"), RoleId("Baron"))
    }
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val snapshot = GameSnapshot(
        gameId = "sde-2d2-demon-bluff",
        gameStateRevision = 0,
        playerInputRevision = 0,
        gameSeed = game.seed,
        rulesetRef = rulesetRef,
        gameState = game,
    )
    private val exactContext = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = ActionFactTimeline(emptyList()),
        perceivedRolesBySeat = game.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        },
        observationLog = EpistemicObservationLog(),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    @Test
    fun `setup legality projects losslessly into SDE bluff candidates`() {
        val legalCandidates = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
        val setupCandidates = SetupDemonBluffJointOutputAdapter.fromLegalCandidates(legalCandidates)
        val distinctTriples = setupCandidates.map { it.roles.toSet() }.toSet()

        assertTrue(setupCandidates.isNotEmpty())
        assertEquals(setupCandidates.size, distinctTriples.size)
        assertTrue(setupCandidates.all { it.roles.size == 3 && it.roles.distinct().size == 3 })
        assertTrue(setupCandidates.all { candidate ->
            candidate.roles.none { role -> role in game.players.map { it.actualRole }.toSet() }
        })
    }

    @Test
    fun `role support delegates to exact shown-role counterworld and is shared across triplets`() {
        val legalCandidates = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
        val candidates = SetupDemonBluffJointOutputAdapter.fromLegalCandidates(legalCandidates)
        val recipientSeats = setOf(1)
        val timelineBefore = exactContext.actionTimeline.reducerFacts()
        val logBefore = exactContext.observationLog.records.toList()
        val publicWholeBundle = listOf(publicChefClaim())

        val coordinator = ClocktowerRecommendationCoordinator()
        val setupRequest = SetupCoordinationRequest(game = game, roles = roles)
        val visibleResult = coordinator.recommendSetup(setupRequest)
        val visiblePlansBefore = visibleResult.plans.toList()
        val shadow = coordinator.evaluateSetupDemonBluffShadow(
            request = setupRequest,
            visibleResult = visibleResult,
            exactContext = ExactConsequenceContext(
                validatedRuleset = validatedRuleset,
                exactContext = exactContext,
            ),
            evaluationRecipientSeats = recipientSeats,
            publicWholeBundleObservations = publicWholeBundle,
        )

        assertSame(visibleResult, shadow.visibleResult)
        assertEquals(visiblePlansBefore, shadow.visibleResult.plans)
        assertTrue(shadow.jointOutput is DemonBluffJointOutputEvaluation.Ready)
        val ready = shadow.jointOutput as DemonBluffJointOutputEvaluation.Ready
        val distinctRoles = candidates.flatMap { it.roles }.distinct().sortedBy(RoleId::value)

        assertEquals(distinctRoles, ready.roleSupports.map(DemonBluffRoleSupport::role))
        assertEquals(candidates.map { it.candidateId }, ready.candidates.map(DemonBluffJointOutputDiagnostics::candidateId))
        ready.candidates.forEach { candidate ->
            assertEquals(
                candidate.roles,
                candidate.roleSupports.map(DemonBluffRoleSupport::role),
            )
            candidate.roleSupports.forEach { support ->
                assertTrue(ready.roleSupports.single { it.role == support.role } === support)
            }
        }

        val firstCandidate = ready.candidates.first()
        val topology = firstCandidate.byRecipient.single()
        val topologySets = firstCandidate.roleSupports.map { support ->
            support.byRecipient.single().afterStructure.evilTeamSeatConfigurations
        }
        val expectedUnion = topologySets.flatMap { it }.toSet()
        val expectedShared = topologySets.reduce { acc, next -> acc.intersect(next) }
        val expectedSupportedRoles = firstCandidate.roleSupports
            .filter { support -> support.byRecipient.single().after.value.signum() > 0 }
            .mapTo(linkedSetOf(), DemonBluffRoleSupport::role)

        assertEquals(expectedSupportedRoles, topology.supportedRoles)
        assertEquals(expectedUnion, topology.unionEvilTeamSeatConfigurations)
        assertEquals(expectedShared, topology.sharedEvilTeamSeatConfigurations)
        assertEquals(topologySets.distinct().size, topology.distinctRoleTopologyPatternCount)

        val firstSupport = ready.roleSupports.first()
        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = exactContext,
            queries = recipientSeats.sorted().map { recipientSeat ->
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "direct-$recipientSeat",
                    recipientSeat = recipientSeat,
                    observations = publicWholeBundle + strictShownRoleProbe(firstSupport.role),
                )
            },
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Ready)
        val directDiagnostics =
            (direct as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics

        firstSupport.byRecipient.zip(directDiagnostics).forEach { (actual, expected) ->
            assertEquals(expected.recipientSeat, actual.recipientSeat)
            assertEquals(expected.before, actual.before)
            assertEquals(expected.after, actual.after)
            assertEquals(expected.beforeStructure, actual.beforeStructure)
            assertEquals(expected.afterStructure, actual.afterStructure)
        }
        assertEquals(timelineBefore, exactContext.actionTimeline.reducerFacts())
        assertEquals(logBefore, exactContext.observationLog.records)

        assertSame(ready, shadow.jointOutput)
        assertEquals(
            visibleResult.plans.associate { plan ->
                val bluff = plan.decisions.filterIsInstance<StorytellerDecision.DemonBluffs>().single()
                plan.style to legalCandidates.single { candidate ->
                    val roles = (candidate.outcome as SetupClueOutcome.DemonBluffs).roles
                    roles == bluff.roles
                }.candidateId
            },
            shadow.legacyBluffCandidateIdByStyle,
        )
        assertTrue(RecommendationStyle.BALANCED in shadow.legacyBluffCandidateIdByStyle)
    }


    @Test
    fun `locked Demon bluffs are persistent inputs and cannot enter shadow replanning`() {
        val legalCandidate = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles).first()
        val lockedBluffs = (legalCandidate.outcome as SetupClueOutcome.DemonBluffs).roles
        val request = SetupCoordinationRequest(
            game = game,
            roles = roles,
            lockedDecisions = listOf(StorytellerDecision.DemonBluffs(lockedBluffs)),
        )
        val coordinator = ClocktowerRecommendationCoordinator()
        val visibleResult = coordinator.recommendSetup(request)

        val error = assertThrows(IllegalArgumentException::class.java) {
            coordinator.evaluateSetupDemonBluffShadow(
                request = request,
                visibleResult = visibleResult,
                exactContext = ExactConsequenceContext(
                    validatedRuleset = validatedRuleset,
                    exactContext = exactContext,
                ),
                evaluationRecipientSeats = setOf(1),
                publicWholeBundleObservations = emptyList(),
            )
        }

        assertTrue(error.message.orEmpty().contains("persistent setup inputs"))
    }

    private fun publicChefClaim(): EpistemicObservation {
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val privateObservation = EpistemicObservation(
            observationId = "healthy-chef-1",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = EffectDraft.PlayerInformation(
                    recipientSeat = 1,
                    sourceAbility = RoleId("Chef"),
                    value = InformationValue.Number(1),
                ),
                roleDefinitions = roles,
            ),
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "healthy-core",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "healthy-chef",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = "chef-1",
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    private fun strictShownRoleProbe(role: RoleId): EpistemicObservation {
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        return EpistemicObservation(
            observationId = "direct-bluff-probe-${role.value.lowercase().replace(' ', '-')}",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.ShownRoleAt(
                seat = 8,
                role = role,
            ),
        )
    }
}
