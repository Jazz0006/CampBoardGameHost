package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoGreaterJoyGenericArchitectureAcceptanceTest {
    @Test
    fun `NGJ generated setup crosses generic identity semantic and history seams without TB templates`() {
        val ruleset = builtInRuleset()
        val provider = ClocktowerSetupProvider(
            script = NGJ_SCRIPT,
            providerId = GENERATED_PROVIDER_ID,
            candidateSource = GeneratedSetupCandidateSource(
                providerId = GENERATED_PROVIDER_ID,
                ruleset = ruleset,
            ),
        )
        val investigator = requireNotNull(ruleset.characterRegistry.findByExternalId("investigator")).id
        val selectedSeedAndCandidate = (0L until 512L)
            .asSequence()
            .map { seed -> seed to provider.candidates(request(seed)).single() }
            .first { (_, candidate) -> investigator in candidate.actualRoles }
        val setupSeed = selectedSeedAndCandidate.first
        val candidate = selectedSeedAndCandidate.second

        assertEquals(SetupSourceKind.GENERATED, candidate.provenance.sourceKind)
        assertEquals(GENERATED_PROVIDER_ID, candidate.provenance.providerId)
        assertNull(candidate.provenance.candidateId)
        assertEquals(PLAYER_COUNT, candidate.playerCount)

        val identityPolicy = SetupShownIdentityPolicyResolver().resolve(candidate, ruleset)
        val identityCommitment = SetupShownIdentityCommitter().commit(
            candidate = candidate,
            policy = identityPolicy,
            setupSeed = setupSeed,
        )
        val committed = CommittedClocktowerSetup(
            script = NGJ_SCRIPT,
            setupSeed = setupSeed,
            assignments = candidate.actualRoles.mapIndexed { index, actualRole ->
                CommittedSetupSeat(
                    seat = index + 1,
                    actualRole = actualRole,
                    shownRole = identityCommitment.shownRoleFor(actualRole),
                )
            },
            provenance = candidate.provenance,
        )

        assertEquals(candidate.actualRoles, committed.assignments.map(CommittedSetupSeat::actualRole))
        assertEquals(SetupSourceKind.GENERATED, committed.provenance.sourceKind)

        val game = GameState(
            script = NGJ_SCRIPT,
            players = committed.assignments.map { assignment ->
                val definition = requireNotNull(ruleset.characterRegistry.findByRoleId(assignment.actualRole))
                PlayerState(
                    seat = assignment.seat,
                    name = "Player ${assignment.seat}",
                    actualRole = assignment.actualRole,
                    actualAlignment = definition.team.alignment(),
                    actualType = definition.team.characterType(),
                    shownRole = assignment.shownRole,
                )
            },
            seed = setupSeed,
        )
        val investigatorSeat = game.players.single { it.actualRole == investigator }.seat
        val legalInformation = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = investigatorSeat,
            abilityRole = investigator,
        )

        assertTrue(legalInformation.isNotEmpty())
        assertTrue(
            legalInformation.all { information ->
                val shownRole = requireNotNull(information.outcome.shownRole)
                ruleset.characterRegistry.findByRoleId(shownRole)?.team == ClocktowerCatalogTeam.MINION
            },
        )

        val candidatePool = (0L until 64L)
            .map { seed -> provider.candidates(request(seed)).single() }
            .distinctBy(SetupCandidate::actualRoles)
        assertTrue(candidatePool.size > 1)
        val history = SetupDiversityHistory(
            recentSetups = listOf(
                SetupDiversityRecord(
                    script = NGJ_SCRIPT,
                    actualRoles = candidatePool.first().actualRoles,
                ),
            ),
        )
        val next = SetupDiversitySelector().select(
            candidates = candidatePool,
            history = history,
            selectionSeed = 20260901L,
        )

        assertNotEquals(candidatePool.first().actualRoles, next.actualRoles)
    }

    private fun request(seed: Long): SetupCandidateRequest = SetupCandidateRequest(
        script = NGJ_SCRIPT,
        playerCount = PLAYER_COUNT,
        setupSeed = seed,
    )

    private fun builtInRuleset() = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.NoGreaterJoy)

    private fun ClocktowerCatalogTeam.alignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK, ClocktowerCatalogTeam.OUTSIDER -> Alignment.GOOD
        ClocktowerCatalogTeam.MINION, ClocktowerCatalogTeam.DEMON -> Alignment.EVIL
    }

    private fun ClocktowerCatalogTeam.characterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
    }

    private companion object {
        val NGJ_SCRIPT = ScriptId("no_greater_joy")
        const val GENERATED_PROVIDER_ID = "generated-seeded-v1"
        const val PLAYER_COUNT = 5
    }
}
