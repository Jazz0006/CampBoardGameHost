package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContextBuilder
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ClocktowerFirstNightInformationRequestTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)
    private val cards = listOf("Investigator", "Chef", "Empath", "Poisoner", "Imp").mapIndexed { index, name ->
        val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).single { it.enName == name }
        PlayerCard("P${index + 1}", Role.Civilian, "", clocktowerRole = role, clocktowerTeam = role.team)
    }
    private fun step(role: String = "Chef") = ClocktowerNightStepUi(
        title = "information", actor = cards[0], isRealAction = true, reason = "",
        storytellerAction = "", tellPlayer = "2", explanation = "", roleEnName = role,
        displayKind = ClocktowerDisplayKind.Number,
    )
    private fun request(
        step: ClocktowerNightStepUi,
        phase: ClocktowerPhase = ClocktowerPhase.FirstNight,
        round: Int = 3,
        pairContext: TroubleBrewingFirstNightPairDecisionContext? = null,
    ) = clocktowerFirstNightInformationRequest(
        step,
        phase,
        round,
        cards,
        ClocktowerScript.TroubleBrewing,
        42L,
        null,
        "en",
        pairContext,
    )

    @Test fun `request gating rejects non-first-night missing actor and unsupported family`() {
        assertNull(request(step(), ClocktowerPhase.Night))
        assertNull(request(step().copy(actor = null)))
        assertNull(request(step().copy(actor = cards[0].copy(name = "absent"))))
        assertNull(request(step("Monk")))
    }

    @Test fun `numeric request preserves identity fallback reliability and selected truth`() {
        val result = requireNotNull(request(step().copy(
            informationReliability = InformationReliability.POISONED, selectedInformationTruthful = false,
        )))
        assertEquals("first-night:FirstNight:3:CHEF:1", result.decisionId)
        val candidate = result.migratedCandidates.single()
        assertEquals(result.selectedCandidateId, candidate.id)
        assertEquals(result.legacyCandidates, result.migratedCandidates)
        assertEquals(2, candidate.observation.shownNumber)
        assertEquals(ReliabilityState.POISONED, candidate.observation.reliability)
        assertEquals(SemanticTruth.FALSE, candidate.observation.semanticTruth)
    }

    @Test fun `legacy duplicate preserves neutral candidate identity without ranking metadata`() {
        val option = ClocktowerDisplayOption("template", ClocktowerDisplayKind.Number, "information",
            "2", null, null, isDefaultRecommendation = true, reasonCodes = listOf("reason"))
        val result = requireNotNull(request(step().copy(legacyInformationCandidates = listOf(option, option))))
        assertEquals(1, result.legacyCandidates.size)
        assertTrue(result.legacyCandidates.single().reasonCodes.isEmpty())
    }

    @Test fun `pair request resolves structured legal observation independently of display seats`() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(4, RoleId("Poisoner")),
            InformationProposition.RoleAt(2, RoleId("Poisoner")),
        ))
        val result = requireNotNull(request(
            step("Investigator").copy(
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayPrimary = "Poisoner",
                displaySecondary = "1 / 3",
                displayProposition = proposition,
            ),
            round = 1,
            pairContext = pairContext(),
        ))
        assertEquals(listOf(2, 4), result.migratedCandidates.single().observation.candidateSeats)
        assertEquals(RoleId("Poisoner"), result.migratedCandidates.single().observation.shownRole)
        assertEquals(SemanticTruth.TRUE, result.migratedCandidates.single().observation.semanticTruth)
        assertEquals(
            "pair-information-ability-v1|Investigator|Poisoner|2,4",
            result.migratedCandidates.single().id,
        )
        assertEquals(result.migratedCandidates.single().id, result.selectedCandidateId)
        assertEquals(listOf(1, 3), result.legacyCandidates.single().observation.candidateSeats)
    }

    @Test fun `trouble brewing pair request fails closed without snapshot context`() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(4, RoleId("Poisoner")),
            InformationProposition.RoleAt(2, RoleId("Poisoner")),
        ))

        val failure = assertThrows(IllegalArgumentException::class.java) {
            request(
                step("Investigator").copy(
                    displayKind = ClocktowerDisplayKind.EitherOne,
                    displayPrimary = "Poisoner",
                    displayProposition = proposition,
                ),
                round = 1,
            )
        }

        assertTrue(failure.message.orEmpty().contains("snapshot-backed first-night context"))
    }

    @Test fun `no greater joy investigator preserves legacy pair publication compatibility`() {
        val ngjCards = listOf("Investigator", "Clockmaker", "Baron", "Imp", "Empath").mapIndexed { index, name ->
            val role = clocktowerRolesForScript(ClocktowerScript.NoGreaterJoy).single { it.enName == name }
            PlayerCard("N${index + 1}", Role.Civilian, "", clocktowerRole = role, clocktowerTeam = role.team)
        }
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(3, RoleId("Baron")),
            InformationProposition.RoleAt(2, RoleId("Baron")),
        ))
        val result = requireNotNull(clocktowerFirstNightInformationRequest(
            displayStep = ClocktowerNightStepUi(
                title = "information",
                actor = ngjCards[0],
                isRealAction = true,
                reason = "",
                storytellerAction = "",
                tellPlayer = "Baron",
                explanation = "",
                roleEnName = "Investigator",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayPrimary = "Baron",
                displayProposition = proposition,
            ),
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            cards = ngjCards,
            script = ClocktowerScript.NoGreaterJoy,
            gameSeed = 99L,
            poisonTarget = null,
            language = "en",
        ))

        assertEquals(listOf(2, 3), result.migratedCandidates.single().observation.candidateSeats)
        assertEquals(RoleId("Baron"), result.migratedCandidates.single().observation.shownRole)
        assertEquals(SemanticTruth.TRUE, result.migratedCandidates.single().observation.semanticTruth)
    }

    private fun pairContext(): TroubleBrewingFirstNightPairDecisionContext {
        val game = cards.toClocktowerGameState(
            script = ClocktowerScript.TroubleBrewing,
            seed = 42L,
            poisonedPlayerName = null,
        )
        return TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                gameSnapshot = GameSnapshot(
                    gameId = "tbgs-2b-request",
                    gameStateRevision = 3L,
                    playerInputRevision = 2L,
                    gameSeed = game.seed,
                    rulesetRef = RulesetRef(
                        scriptId = ScriptId("trouble_brewing"),
                        scriptContentHash = "0123456789abcdef0123456789abcdef",
                        rulesetVersion = "tbgs-2b-test",
                        sourceRevision = "tbgs-2b-test",
                        coverage = RuleCoverage.PARTIAL,
                    ),
                    gameState = game,
                ),
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                characterRegistry = ruleset.characterRegistry,
            ),
            characterRegistry = ruleset.characterRegistry,
        )
    }
}
