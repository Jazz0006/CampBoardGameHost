package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkNumericWholeBundleEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkNumericWholeBundleRequest
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit SDE-2D5 T3 calibration evidence workload.
 *
 * Cross-regime topology evaluation and real whole-bundle calibration cases are intentionally
 * outside ordinary FAST/FULL regression. They generate deterministic review evidence and carry no
 * production policy thresholds.
 */
class Sde2D5CalibrationExperiment {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `baseline evidence spans all four player count regimes and both setup profile families`() {
        val evidence = Sde2D5CrossRegimeCalibrationEvidenceBuilder.buildBaseline()

        assertEquals(
            listOf(6, 6, 9, 9, 12, 12, 15, 15),
            evidence.points.map { it.playerCount },
        )
        assertEquals(
            Sde2D5PlayerCountRegime.entries.toSet(),
            evidence.points.mapTo(linkedSetOf()) { it.regime },
        )
        assertEquals(
            Sde2D5SetupProfileKind.entries.toSet(),
            evidence.points.mapTo(linkedSetOf()) { it.profileKind },
        )
        Sde2D5PlayerCountRegime.entries.forEach { regime ->
            assertEquals(
                Sde2D5SetupProfileKind.entries.toSet(),
                evidence.points
                    .filter { it.regime == regime }
                    .mapTo(linkedSetOf()) { it.profileKind },
            )
        }
        assertTrue(evidence.points.all { it.beforeStrategicWorldCount > 0 })
        assertTrue(evidence.points.all { it.afterStrategicWorldCount > 0 })
        assertTrue(evidence.points.all {
            it.afterStrategicWorldCount <= it.beforeStrategicWorldCount
        })
        assertTrue(evidence.points.all {
            it.normalized.demonCoverRetention.valueOrNull() != null &&
                it.normalized.evilTopologyRetention.valueOrNull() != null &&
                it.normalized.evilCoverRetention.valueOrNull() != null
        })
        evidence.points.forEach { point ->
            assertNull(point.rawMechanicalBefore)
            assertNull(point.rawMechanicalAfter)
        }
    }

    @Test
    fun `real Drunk numeric candidate projects HealthyCore FullBundle and marginal evidence`() {
        val snapshot = drunkSnapshot()
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = snapshot.gameState.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roleDefinitions,
        )
        val evaluation = TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            request = FirstNightDrunkNumericWholeBundleRequest(
                drunkSeat = 2,
                evaluationRecipientSeats = setOf(1),
                healthyCore = emptyList(),
            ),
        )
        assertTrue(evaluation is FirstNightDrunkNumericWholeBundleEvaluation.Ready)
        val ready = evaluation as FirstNightDrunkNumericWholeBundleEvaluation.Ready
        val falseCandidate = ready.candidates.first { it.semanticTruth == SemanticTruth.FALSE }

        val evidence = Sde2D5DrunkCalibrationEvidenceProjector.project(
            playerCount = 6,
            profileKind = Sde2D5SetupProfileKind.BARON,
            healthyCore = ready.healthyCoreByRecipient.single(),
            candidate = falseCandidate,
        )

        assertEquals(falseCandidate.candidateId, evidence.candidateId)
        assertEquals(SemanticTruth.FALSE, evidence.semanticTruth)
        assertEquals(Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE, evidence.healthyCore.evidenceKind)
        assertEquals(Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE, evidence.fullBundle.evidenceKind)
        assertTrue(evidence.fullBundle.rawMechanicalAfter!!.signum() > 0)
        assertTrue(
            evidence.fullBundle.afterStrategicWorldCount <=
                evidence.healthyCore.afterStrategicWorldCount,
        )
    }

    @Test
    fun `real Demon bluff joint output yields low and high shared support review contrasts`() {
        val calibration = Sde2D5DemonBluffRealCalibrationBuilder.build()

        assertTrue(calibration.evidence.size >= 2)
        assertEquals(
            setOf(
                Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION,
                Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION,
            ),
            calibration.selected.flatMapTo(linkedSetOf()) { it.selectionReasons },
        )
        assertTrue(
            "D5D bounded real fixture must expose more than one shared-support level.",
            calibration.evidence
                .map { evidence -> evidence.sharedToUnionRetention }
                .distinct()
                .size > 1,
        )
    }

    @Test
    fun `real healthy bundle marginals expose topology neutral and near raw strategic contrasts`() {
        val calibration = Sde2D5RoleInformationRealCalibrationBuilder.build()

        assertTrue(calibration.topologyNeutral.hasMechanicalInformationGain)
        assertTrue(calibration.topologyNeutral.topologyNeutral)
        assertTrue(calibration.topologyNeutral.rawWorldsRemoved.signum() > 0)

        val nearRaw = calibration.closestRawDifferentTopology
        assertTrue(nearRaw.first.hasMechanicalInformationGain)
        assertTrue(nearRaw.second.hasMechanicalInformationGain)
        assertTrue(
            nearRaw.first.point.normalized.evilTopologyRetention !=
                nearRaw.second.point.normalized.evilTopologyRetention,
        )
        assertTrue(nearRaw.rawWorldRemovalDifference.signum() >= 0)
    }

    private fun drunkSnapshot(): GameSnapshot {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "sde-2d5-drunk-calibration",
            sourceRevision = "official",
        )
        val players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Soldier", CharacterType.TOWNSFOLK),
            player(5, "Scarlet Woman", CharacterType.MINION),
            player(6, "Imp", CharacterType.DEMON),
        )
        return GameSnapshot(
            gameId = "sde-2d5-drunk-calibration",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = 20260919L,
            rulesetRef = rulesetRef,
            gameState = GameState(
                script = TroubleBrewingFixtures.scriptId,
                players = players,
                seed = 20260919L,
            ),
        )
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
