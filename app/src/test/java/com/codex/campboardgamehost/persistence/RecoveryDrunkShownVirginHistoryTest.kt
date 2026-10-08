package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Actual legal 8-seat Trouble Brewing: 5 Townsfolk + Drunk + Spy + Imp.
 * The *Drunk shown Virgin* is nominated when alive, then dies later; Recovery
 * must not confuse a first perceived-Virgin interaction with a functioning Virgin.
 */
class RecoveryDrunkShownVirginHistoryTest {
    private val tb = ClocktowerScript.TroubleBrewing
    private val roles = clocktowerRolesForScript(tb).associateBy(ClocktowerRole::enName)
    private val names = listOf("Drunk", "Chef", "Empath", "Washerwoman", "Monk", "Investigator", "Spy", "Imp")

    private fun cards(shownDrunkRole: String = "Virgin"): List<PlayerCard> =
        names.mapIndexed { i, name ->
            val role = requireNotNull(roles[name])
            val shown = requireNotNull(roles[if (i == 0) shownDrunkRole else name])
            PlayerCard(
                name = "P${i + 1}",
                role = Role.Civilian,
                word = "",
                roleLabel = shown.enName,
                actualRoleLabel = role.enName,
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shown,
                eliminatedRound = if (i == 0) 2 else null,
            )
        }

    private fun snapshot(shownDrunkRole: String = "Virgin"): RecoverySnapshot =
        RecoverySnapshot(
            compatibilityToken = "drunk-virgin-regression",
            savedAtMillis = 999_000L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = cards(shownDrunkRole),
                records = listOf(EliminationRecord(2, "P1")),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = tb, gameId = "drunk-perceived-virgin", gameSeed = 33L,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = ClocktowerPhase.Day, nightStarted = false, nightStepIndex = 0,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = null,
                    confirmedPoisonTarget = null,
                    confirmedMonkProtectedTarget = null,
                    confirmedMayorRedirectTarget = null,
                    pendingNewDemonName = null,
                    pendingNightNewDemonIdentityName = null,
                    confirmedDemonSuccessorTarget = null,
                    redHerring = null,
                    demonBluffRoleNames = emptyList(),
                    butlerMaster = null,
                    virginUsed = true,
                    slayerUsed = false,
                    slayerClaimedNames = emptyList(),
                    artistUsed = false,
                    artistClaimedNames = emptyList(),
                    lastExecutedName = "P1",
                    pendingKlutzName = null,
                    klutzReturnToDawn = false,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = null,
                    highestVoteCount = 0,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 2L,
                    playerInputRevision = 0L,
                    actionTimeline = ActionFactTimeline(listOf(
                        TimelineBoundActionFact(
                            ActionFact.Nomination("first-perceived-virgin", 0L, 2, 1, true),
                            TimelinePoint(StorytellerPhase.DAY, 1, 1, 0L),
                        ),
                        TimelineBoundActionFact(
                            ActionFact.Execution("later-execution", 1L, 1),
                            TimelinePoint(StorytellerPhase.DAY, 2, 1, 1L),
                        ),
                    )),
                    nextTimelineGlobalSequence = 2L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )

    private fun restore(saved: RecoverySnapshot): RecoveryPlanPreparation =
        RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(saved),
            expectedCompatibilityToken = "drunk-virgin-regression",
            nowMillis = 1_000_000L,
            roleByName = roles::get,
            clocktowerRulesetResolver = { script, _ ->
                RulesetRef(
                    scriptId = ScriptId("trouble_brewing"),
                    scriptContentHash = "0123456789abcdef0123456789abcdef",
                    rulesetVersion = "drunk-virgin-test",
                    sourceRevision = "fixture",
                    coverage = RuleCoverage.PARTIAL,
                ).takeIf { script == tb }
            },
        )

    @Test fun `first Drunk shown Virgin nomination remains recoverable after later death`() {
        val interaction = AbilitySubject(
            actualRole = "Drunk", shownRole = "Virgin",
            isPoisoned = false, isAlive = true,
        )
        assertTrue(AbilityFunctioningSemantics.interactsAs(interaction, "Virgin"))
        assertTrue(!AbilityFunctioningSemantics.functionsAs(interaction, "Virgin"))
        val accepted = restore(snapshot())
        assertTrue(accepted is RecoveryPlanPreparation.Ready)
        val game = (accepted as RecoveryPlanPreparation.Ready).plan.snapshot.game as ClocktowerRecovery
        assertEquals("Drunk", game.cards[0].clocktowerRole?.enName)
        assertEquals("Virgin", game.cards[0].clocktowerShownRole?.enName)
        assertEquals(2, game.cards[0].eliminatedRound)
        assertEquals(true, (game.history.actionTimeline.entries.first().fact as ActionFact.Nomination).firstVirginNomination)
    }

    @Test fun `first Virgin marker cannot be forged by changing Drunk shown role`() {
        val rejected = restore(snapshot(shownDrunkRole = "Chef"))
        assertTrue(rejected is RecoveryPlanPreparation.Rejected)
    }
}
