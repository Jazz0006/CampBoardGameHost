package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupRecommendationModulePerceivedAbilityTest {
    @Test
    fun `first night natural pair precompute includes Drunk perceived Investigator registration truth`() {
        val investigator = RoleId("Investigator")
        val poisoner = RoleId("Poisoner")
        val game = GameState(
            script = ScriptId("trouble_brewing"),
            players = listOf(
                player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Recluse", CharacterType.OUTSIDER),
                player(4, "Poisoner", CharacterType.MINION),
                player(5, "Imp", CharacterType.DEMON),
            ),
            seed = 20260901L,
        )

        val candidates = SetupRecommendationModule().naturalPairCandidates(game)
        val perceivedInvestigatorCandidates = candidates.filter { candidate ->
            candidate.effects
                .filterIsInstance<EffectDraft.PlayerInformation>()
                .singleOrNull()
                ?.let { information ->
                    information.recipientSeat == 1 && information.sourceAbility == investigator
                } == true
        }

        assertTrue(
            "The production first-night precompute must expose Investigator semantics for a Drunk shown Investigator",
            perceivedInvestigatorCandidates.isNotEmpty(),
        )
        assertNotNull(
            "The perceived Investigator truth space must retain Recluse registration truth before DRUNK impairment is applied",
            perceivedInvestigatorCandidates.firstOrNull { candidate ->
                candidate.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE &&
                    candidate.registrations.any { registration ->
                        registration.subjectSeat == 3 &&
                            registration.registeredRole == poisoner &&
                            registration.reason == RegistrationReason.RECLUSE_ABILITY
                    }
            },
        )
    }

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
