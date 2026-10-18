package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * R0 regression: AI can still preselect first-night pair clues after the B2 Host
 * manual presentation change; Host records actual clues independently of AI choices.
 */
class ClocktowerAiPairRecommendationRecoveryTest {
    @Test fun `AI pair candidate matches new manual presentation and manual override remains recordable`() {
        val scriptRoles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
        val cards = listOf("Investigator", "Chef", "Recluse", "Poisoner", "Imp")
            .mapIndexed { i, name ->
                PlayerCard("P${i + 1}", clocktowerShownRole = scriptRoles.single { it.enName == name })
            }
        val players = listOf(
            state(1, "Investigator", CharacterType.TOWNSFOLK),
            state(2, "Chef", CharacterType.TOWNSFOLK),
            state(3, "Recluse", CharacterType.OUTSIDER),
            state(4, "Poisoner", CharacterType.MINION),
            state(5, "Imp", CharacterType.DEMON),
        )
        val decision = PendingPairInformationDecision(
            requestIdentity = StorytellerDecisionRequestIdentity(
                "ai-recovery", "first-night:investigator:1"),
            revision = StorytellerDecisionRevision(1, 0),
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val options = ClocktowerNeutralInformationPreparation.pairInformationOptions(
            ability = ClocktowerPairInformationAbility.Investigator,
            actor = cards.first(),
            cards = cards,
            scriptRoles = scriptRoles,
            roleLabel = { it.enName },
            text = { _, en -> en },
        )
        val candidate = SemanticResult("PAIR_RESULT", pairRoleId = "Baron", pairSeats = listOf(1, 2))
        val selected = ClocktowerResidualDisplayMatcherV2.matchPair(
            decision, candidate, emptyList(), cards, "en")
        assertNotNull("the restored AI candidate should preselect the structural Host UI", selected)
        assertEquals(candidate.pairSeats.sorted(),
            selected!!.proposition.let {
                (it as com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.AnyOf)
                    .alternatives.map { item ->
                        (item as com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.RoleAt).seat
                    }.sorted()
            })

        val manuallySelected = options.first { it.displayPrimary == "Baron" }
        val step = ClocktowerNightStepUi(
            title = "Investigator", actor = cards.first(), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = null, explanation = "",
            roleEnName = "Investigator", displayKind = ClocktowerDisplayKind.EitherOne,
            manualInformationCandidates = options,
        )
        val displayed = resolveClocktowerPlayerDisplay(step, manuallySelected)
        val observed = requireNotNull(
            clocktowerFirstNightInformationRequest(displayed, ClocktowerPhase.FirstNight, 1, cards))
            .candidates.single().observation
        assertEquals(RoleId("Baron"), observed.shownRole)
        assertEquals(SemanticTruth.NOT_APPLICABLE, observed.semanticTruth)
        assertTrue(observed.registrations.isEmpty())
    }

    private fun state(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        shownRole = RoleId(role),
        actualType = type,
        actualAlignment = if (type == CharacterType.MINION || type == CharacterType.DEMON) {
            Alignment.EVIL
        } else Alignment.GOOD,
    )
}
