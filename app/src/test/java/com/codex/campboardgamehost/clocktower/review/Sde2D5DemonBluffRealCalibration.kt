package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffJointOutputEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.sde.SetupDemonBluffJointOutputAdapter
import com.codex.campboardgamehost.clocktower.recommendation.sde.TroubleBrewingDemonBluffJointOutputEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import java.io.File

internal data class Sde2D5DemonBluffRealCalibration(
    val evidence: List<Sde2D5DemonBluffCalibrationEvidence>,
    val selected: List<Sde2D5DemonBluffCalibrationSelection>,
    val roleDomainCompleteness: Sde2D5CalibrationRoleDomainCompleteness,
)

/**
 * Full-script real D2D2 -> D5D calibration fixture.
 *
 * D5 policy calibration must preserve every legal Trouble Brewing counterworld family. In
 * particular, STANDARD actual setup does not let review evidence discard BARON-profile worlds,
 * Drunk shown-role worlds, or other script roles that remain possible from player knowledge.
 * Bounded role domains remain valid for local exact-evaluator correctness tests only.
 */
internal object Sde2D5DemonBluffRealCalibrationBuilder {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d5-demon-bluff-calibration",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val game = GameState(
        script = TroubleBrewingFixtures.scriptId,
        players = listOf(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(4, "Undertaker", CharacterType.TOWNSFOLK),
            player(5, "Virgin", CharacterType.TOWNSFOLK),
            player(6, "Scarlet Woman", CharacterType.MINION),
            player(7, "Imp", CharacterType.DEMON),
        ),
        seed = 20260919L,
    )
    private val snapshot = GameSnapshot(
        gameId = "sde-2d5-demon-bluff-calibration",
        gameStateRevision = 0,
        playerInputRevision = 0,
        gameSeed = game.seed,
        rulesetRef = rulesetRef,
        gameState = game,
    )
    private val context = ExactHistoricalHypotheticalContext(
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

    fun build(): Sde2D5DemonBluffRealCalibration {
        val roleDomainCompleteness =
            Sde2D5CalibrationRoleDomainContract.requireFullTroubleBrewing(roles)
        val legal = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
        val candidates = SetupDemonBluffJointOutputAdapter.fromLegalCandidates(legal)
        require(candidates.size >= 2) {
            "D5D full-domain fixture requires at least two legal bluff triplets."
        }

        val evaluation = TroubleBrewingDemonBluffJointOutputEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            actualDemonSeat = 7,
            evaluationRecipientSeats = setOf(1),
            publicWholeBundleObservations = listOf(publicChefClaim()),
            candidates = candidates,
        )
        require(evaluation is DemonBluffJointOutputEvaluation.Ready) {
            "D5D full-domain real bluff calibration requires exact joint-output support."
        }

        val evidence = evaluation.candidates.map { diagnostic ->
            Sde2D5DemonBluffCalibrationEvidenceProjector.project(
                playerCount = 7,
                profileKind = Sde2D5SetupProfileKind.STANDARD,
                recipientSeat = 1,
                diagnostic = diagnostic,
            )
        }

        return Sde2D5DemonBluffRealCalibration(
            evidence = evidence,
            selected = Sde2D5DemonBluffCalibrationEvidenceSelector.selectReviewContrasts(evidence),
            roleDomainCompleteness = roleDomainCompleteness,
        )
    }

    private fun publicChefClaim(): EpistemicObservation {
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val privateObservation = EpistemicObservation(
            observationId = "d5d-chef-1",
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
                bundleId = "d5d-healthy-core",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "d5d-chef",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = "chef-1",
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}
