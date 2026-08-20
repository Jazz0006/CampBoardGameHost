package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.RegistrationDetail
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.RegistrationPolicy
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationPolicyTest {
    @Test
    fun `complete spy registration candidates bind every result to the detection question`() {
        val candidates = RegistrationPolicy.generateCandidates(
            request = request(),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(
                    role("Washerwoman", CharacterType.TOWNSFOLK),
                    role("Butler", CharacterType.OUTSIDER),
                ),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                registrationQuestion = RegistrationQuestion.ROLE,
            ),
            style = RecommendationStyle.BALANCED,
        )

        assertEquals(3, candidates.size)
        val actual = candidates.single { it.candidate.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE }
        assertTrue(actual.candidate.registrations.isEmpty())
        assertEquals("natural-truth", actual.candidate.candidateFamilyId)
        val special = candidates.filter { it.candidate.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE }
        assertEquals(2, special.size)
        assertTrue(special.all { it.candidate.candidateFamilyId == "registration-spy" })
        assertTrue(special.all { evaluation ->
            evaluation.candidate.registrations.single().registrationQuestion == RegistrationQuestion.ROLE
        })
    }

    @Test
    fun `recluse special registration uses its own family`() {
        val base = request()
        val recluse = base.state.game.players.first().copy(
            name = "Recluse",
            actualRole = RoleId("Recluse"),
            actualAlignment = Alignment.GOOD,
            actualType = CharacterType.OUTSIDER,
        )
        val candidates = RegistrationPolicy.generateCandidates(
            request = base.copy(
                state = base.state.copy(
                    game = base.state.game.copy(
                        players = base.state.game.players.map { if (it.seat == recluse.seat) recluse else it },
                    ),
                ),
            ),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Imp", CharacterType.DEMON)),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                registrationQuestion = RegistrationQuestion.DEMON,
            ),
            style = RecommendationStyle.BALANCED,
        )

        val special = candidates.single { it.candidate.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE }
        assertEquals("registration-recluse", special.candidate.candidateFamilyId)
        assertEquals(RegistrationQuestion.DEMON, special.candidate.registrations.single().registrationQuestion)
    }

    @Test
    fun `gentle uses actual identity while balanced can use useful special registration`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(
                    role("Washerwoman", CharacterType.TOWNSFOLK),
                    role("Virgin", CharacterType.TOWNSFOLK),
                ),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                outcomeDiscussionValue = 4,
                outcomeMisinformationPressure = 2,
            ),
        )

        assertFalse(recommendations.first { it.style == RecommendationStyle.GENTLE }.choice().usesSpecialAbility)
        assertTrue(recommendations.first { it.style == RecommendationStyle.BALANCED }.choice().usesSpecialAbility)
        assertTrue(recommendations.first { it.style == RecommendationStyle.AGGRESSIVE }.choice().usesSpecialAbility)
        assertEquals(3, recommendations.map { it.choice().registeredRole }.distinct().size)
    }

    @Test
    fun `poisoned special character only returns actual registration`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Washerwoman", CharacterType.TOWNSFOLK)),
                detail = RegistrationDetail.ROLE,
                canMisregister = false,
            ),
        )

        assertEquals(1, recommendations.size)
        assertFalse(recommendations.single().choice().usesSpecialAbility)
        assertEquals(RoleId("Spy"), recommendations.single().choice().registeredRole)
    }

    @Test
    fun `alignment-only interaction does not invent multiple role choices`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(
                    role("Washerwoman", CharacterType.TOWNSFOLK),
                    role("Butler", CharacterType.OUTSIDER),
                ),
                detail = RegistrationDetail.ALIGNMENT_ONLY,
                canMisregister = true,
            ),
        )

        assertEquals(RecommendationStyle.entries.toSet(), recommendations.map { it.style }.toSet())
        assertEquals(2, recommendations.map { it.choice().registeredAlignment }.toSet().size)
    }

    @Test
    fun `global balance discourages killing Recluse when evil is ahead`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(evilAdvantage = 70),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Imp", CharacterType.DEMON)),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                outcomeMisinformationPressure = 4,
                specialRegistrationBalanceImpact = 1,
            ),
        )

        assertFalse(recommendations.first { it.style == RecommendationStyle.BALANCED }.choice().usesSpecialAbility)
    }

    @Test
    fun `global balance can register Recluse as demon when good is well ahead`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(evilAdvantage = -70),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Imp", CharacterType.DEMON)),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                outcomeMisinformationPressure = 4,
                specialRegistrationBalanceImpact = 1,
            ),
        )

        assertTrue(recommendations.first { it.style == RecommendationStyle.BALANCED }.choice().usesSpecialAbility)
    }

    @Test
    fun `configured styles remain distinct in a neutral high impact ruling`() {
        val recommendations = RegistrationPolicy.recommendRegistration(
            request = request(),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Imp", CharacterType.DEMON)),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                outcomeMisinformationPressure = 4,
                specialRegistrationBalanceImpact = 1,
            ),
        )

        assertFalse(recommendations.first { it.style == RecommendationStyle.GENTLE }.choice().usesSpecialAbility)
        assertFalse(recommendations.first { it.style == RecommendationStyle.BALANCED }.choice().usesSpecialAbility)
        assertTrue(recommendations.first { it.style == RecommendationStyle.AGGRESSIVE }.choice().usesSpecialAbility)
    }

    @Test
    fun `stable variation is reproducible for the same decision`() {
        val context = SpecialRegistrationContext(
            subjectSeat = 1,
            allowedRoles = listOf(
                role("Washerwoman", CharacterType.TOWNSFOLK),
                role("Virgin", CharacterType.TOWNSFOLK),
            ),
            detail = RegistrationDetail.ROLE,
            canMisregister = true,
            outcomeMisinformationPressure = 2,
        )

        assertEquals(
            RegistrationPolicy.recommendRegistration(request(), context),
            RegistrationPolicy.recommendRegistration(request(), context),
        )
    }

    @Test
    fun `one shot final day registration receives unified consequence penalties`() {
        val special = RegistrationPolicy.generateCandidates(
            request = request(alivePlayers = 3),
            context = SpecialRegistrationContext(
                subjectSeat = 1,
                allowedRoles = listOf(role("Imp", CharacterType.DEMON)),
                detail = RegistrationDetail.ROLE,
                canMisregister = true,
                outcomeMisinformationPressure = 4,
                isOneShotAbility = true,
                playerSelectedTarget = true,
            ),
            style = RecommendationStyle.BALANCED,
        ).single { it.candidate.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE }

        assertEquals(QualityTier.EXPERT_ONLY, special.qualityTier)
        assertTrue("consequence.one-shot-ability-protection" in special.explanationCodes)
        assertTrue("consequence.final-day-impact-penalty" in special.explanationCodes)
    }

    private fun request(evilAdvantage: Int = 0, alivePlayers: Int = 4) = DynamicDecisionRequest(
        id = "night-1-empath-spy",
        type = StorytellerDecisionType.SPECIAL_REGISTRATION,
        sourceAbility = RoleId("Empath"),
        state = DynamicGameState(
            game = GameState(
                script = ScriptId("trouble-brewing"),
                players = listOf(
                    PlayerState(
                        seat = 1,
                        name = "Spy",
                        actualRole = RoleId("Spy"),
                        actualAlignment = Alignment.EVIL,
                        actualType = CharacterType.MINION,
                    ),
                    PlayerState(2, "Empath", RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK),
                    PlayerState(3, "Chef", RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
                    PlayerState(4, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
                ).mapIndexed { index, player -> player.copy(alive = index < alivePlayers) },
                seed = 11,
            ),
            phase = StorytellerPhase.NIGHT,
            round = 1,
            evilAdvantage = evilAdvantage,
        ),
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        type = type,
        scriptIds = setOf(ScriptId("trouble-brewing")),
    )

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.choice() =
        candidate.choice as DynamicStorytellerChoice.Registration
}
