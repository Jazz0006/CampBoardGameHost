package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpecialRegistrationRecommenderTest {
    @Test
    fun `gentle uses actual identity while balanced can use useful special registration`() {
        val recommendations = SpecialRegistrationRecommender.recommend(
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
        val recommendations = SpecialRegistrationRecommender.recommend(
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
        val recommendations = SpecialRegistrationRecommender.recommend(
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
        val recommendations = SpecialRegistrationRecommender.recommend(
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
        val recommendations = SpecialRegistrationRecommender.recommend(
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
        val recommendations = SpecialRegistrationRecommender.recommend(
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
            SpecialRegistrationRecommender.recommend(request(), context),
            SpecialRegistrationRecommender.recommend(request(), context),
        )
    }

    private fun request(evilAdvantage: Int = 0) = DynamicDecisionRequest(
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
                ),
                seed = 11,
            ),
            phase = StorytellerPhase.NIGHT,
            round = 1,
            evilAdvantage = evilAdvantage,
        ),
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = Alignment.GOOD,
        type = type,
        scriptIds = setOf(ScriptId("trouble-brewing")),
    )

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.choice() =
        candidate.choice as DynamicStorytellerChoice.Registration
}
