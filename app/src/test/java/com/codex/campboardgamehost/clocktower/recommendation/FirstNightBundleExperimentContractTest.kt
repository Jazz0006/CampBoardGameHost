package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleExperimentContractTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "fn-bundle-experiment-contract-test",
        sourceRevision = "official",
    )
    private val snapshot = runtimeSnapshot()
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `public good projection is ephemeral and excludes latent choices`() {
        val original = privateObservation(
            id = "private-clue",
            proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        )
        val entries = mutableListOf(
            FirstNightInformationBundleEntry(
                entryId = "fixed-clue",
                control = FirstNightBundleEntryControl.RULE_DETERMINED,
                observation = original,
                profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
            ),
            FirstNightInformationBundleEntry(
                entryId = "red-herring-choice",
                control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                sourceChoiceId = "red-herring-seat-3",
            ),
        )
        val bundle = FirstNightInformationBundle("bundle-a", entries)
        entries.clear()

        val projected = FirstNightPublicGoodInfoProjection.project(bundle)

        assertEquals(2, bundle.entries.size)
        assertEquals(1, projected.size)
        assertEquals(ObservationVisibility.PRIVATE, original.visibility)
        assertEquals(setOf(1), original.recipientSeats)
        assertEquals(ObservationVisibility.PUBLIC, projected.single().visibility)
        assertTrue(projected.single().recipientSeats.isEmpty())
        assertEquals(original.proposition, projected.single().proposition)
        assertEquals(original.timelineBinding, projected.single().timelineBinding)
        assertNotEquals(original.observationId, projected.single().observationId)
    }

    @Test
    fun `beginner public good evaluation uses every requested recipient perspective without mutating bundle`() {
        val original = privateObservation(
            id = "shared-poisoner-clue",
            proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        )
        val bundle = FirstNightInformationBundle(
            bundleId = "bundle-shared",
            entries = listOf(
                FirstNightInformationBundleEntry(
                    entryId = "shared-clue",
                    control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                    sourceChoiceId = "candidate-a",
                    observation = original,
                    profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                ),
                FirstNightInformationBundleEntry(
                    entryId = "latent-bluffs",
                    control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                    sourceChoiceId = "bluffs-a",
                ),
            ),
        )
        val entriesBefore = bundle.entries.toList()

        val evaluation = FirstNightBundleExperimentEvaluator.evaluateBeginnerPublicGoodInfo(
            validatedRuleset = validatedRuleset,
            context = context(),
            bundle = bundle,
            evaluationRecipientSeats = setOf(2, 1),
        )

        assertTrue(evaluation is FirstNightBundleExperimentEvaluation.Ready)
        val ready = evaluation as FirstNightBundleExperimentEvaluation.Ready
        assertEquals(FirstNightExperimentProfile.BEGINNER_PUBLIC_GOOD_INFO, ready.profile)
        assertEquals(1, ready.publicObservationCount)
        assertEquals(listOf(1, 2), ready.recipientDiagnostics.map { it.recipientSeat })
        assertTrue(ready.recipientDiagnostics.all { it.after.value <= it.before.value })
        assertEquals(entriesBefore, bundle.entries)
        assertEquals(ObservationVisibility.PRIVATE, original.visibility)
        assertEquals(setOf(1), original.recipientSeats)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `public good exposure cannot be assigned to a latent entry`() {
        FirstNightInformationBundleEntry(
            entryId = "invalid-latent-share",
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            sourceChoiceId = "candidate-a",
            observation = null,
            profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
        )
    }

    private fun context(): ExactHistoricalHypotheticalContext = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = ActionFactTimeline(emptyList()),
        perceivedRolesBySeat = perceived,
        observationLog = EpistemicObservationLog(),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun privateObservation(
        id: String,
        proposition: InformationProposition,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = 1,
        sourceAbility = null,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )

    private fun runtimeSnapshot(): GameSnapshot {
        val rolesInPlay = listOf("Chef", "Empath", "Washerwoman", "Poisoner", "Imp")
        return GameSnapshot(
            gameId = "fn-bundle-contract-game",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = 7L,
            rulesetRef = rulesetRef,
            gameState = GameState(
                script = TroubleBrewingFixtures.scriptId,
                players = rolesInPlay.mapIndexed { index, role ->
                    PlayerState(
                        seat = index + 1,
                        name = "P${index + 1}",
                        actualRole = RoleId(role),
                        actualAlignment = if (role in setOf("Poisoner", "Imp")) Alignment.EVIL else Alignment.GOOD,
                        actualType = when (role) {
                            "Poisoner" -> CharacterType.MINION
                            "Imp" -> CharacterType.DEMON
                            else -> CharacterType.TOWNSFOLK
                        },
                    )
                },
                seed = 7L,
            ),
        )
    }
}
