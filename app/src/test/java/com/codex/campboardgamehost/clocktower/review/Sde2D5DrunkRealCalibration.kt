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

internal object Sde2D5DrunkRealCalibrationBuilder {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()

    fun buildFalseNumericEvidence(): Sde2D5DrunkCalibrationEvidence {
        val snapshot = snapshot()
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
        require(evaluation is FirstNightDrunkNumericWholeBundleEvaluation.Ready) {
            "D5F Drunk review evidence requires real numeric whole-bundle evaluation."
        }
        val falseCandidate = evaluation.candidates.first { it.semanticTruth == SemanticTruth.FALSE }
        return Sde2D5DrunkCalibrationEvidenceProjector.project(
            playerCount = 6,
            profileKind = Sde2D5SetupProfileKind.BARON,
            healthyCore = evaluation.healthyCoreByRecipient.single(),
            candidate = falseCandidate,
        )
    }

    private fun snapshot(): GameSnapshot {
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
