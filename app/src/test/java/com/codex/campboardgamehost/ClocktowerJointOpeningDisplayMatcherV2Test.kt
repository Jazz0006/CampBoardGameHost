package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.commitAcceptedTestAction
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerJointOpeningDisplayMatcherV2Test {
    private fun numericOption(value: Int) = ClocktowerDisplayOption(
        label = "numeric", displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chef", displayPrimary = value.toString(),
        displaySecondary = null, displayFooter = null,
        proposition = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), value),
    )
    private val chefOne = HostOpeningSemanticValueV2(
        sourceSeat = 1, abilityRoleId = "chef",
        effectiveAbilityRoleId = "chef", basis = "FUNCTIONING",
        subjectMode = "NONE", subjectSeats = emptyList(),
        observationKind = "NUMBER", number = 1,
    )

    @Test fun currentTypedNumericHostOptionMustMatchPlanExactly() {
        val chosen = numericOption(1)
        assertEquals(chosen, ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(numericOption(0), chosen)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(numericOption(0))))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 2, listOf(chosen)))
        // A duplicated visible interpretation is not a license to pick a witness.
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen, chosen)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen.copy(spyRegistersGood = true))))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen.copy(proposition = null))))
    }

    private fun currentSession(): ClocktowerGameSession {
        val players = listOf("Chef", "Butler", "Imp").mapIndexed { index, role ->
            val type = when (role) {
                "Butler" -> CharacterType.OUTSIDER
                "Imp" -> CharacterType.DEMON
                else -> CharacterType.TOWNSFOLK
            }
            PlayerState(
                seat = index + 1, name = "P${index + 1}",
                actualRole = RoleId(role), shownRole = RoleId(role),
                actualAlignment = if (role == "Imp") Alignment.EVIL else Alignment.GOOD,
                actualType = type,
            )
        }
        return ClocktowerGameSession.createProduction(
            "opening-advice-check", 19,
            GameState(ScriptId("trouble_brewing"), players, 19),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    @Test fun openingAdviceSurvivesMatchingSetupAndReceivedInformationButNotDivergentFacts() {
        val session = currentSession()
        val bluffs = listOf("Monk", "Ravenkeeper", "Empath")
        fun valid() = ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
            session.state, null, mapOf(1 to chefOne), 1, bluffs)
        assertTrue(valid())
        session.commitDemonBluffs(bluffs.map(::RoleId), StorytellerPhase.FIRST_NIGHT, 1, 0)
        session.commitRedHerringSeat(1, StorytellerPhase.FIRST_NIGHT, 1, 1)
        assertTrue(valid())
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-shown-one", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 2, sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = numericOption(1).proposition!!,
        ))
        assertTrue(valid())
        session.commitAcceptedTestAction(ActionFactDraft.Poison(
            "poison-changes-opening-context", StorytellerPhase.FIRST_NIGHT, 1, 3, 2))
        assertFalse(valid())
    }

    @Test fun openingAdviceRejectsConfirmedHumanOverrides() {
        val session = currentSession()
        assertFalse(ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
            session.state, 2, mapOf(1 to chefOne), null, emptyList()))
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-manual-zero", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 0, sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = numericOption(0).proposition!!,
        ))
        assertFalse(ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
            session.state, null, mapOf(1 to chefOne), null, emptyList()))
    }

    @Test fun pairUsesTypedSeatRoleDisjunctionAndNotLocalizedText() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(6, RoleId("Baron")),
            InformationProposition.RoleAt(7, RoleId("Baron"))))
        val option = ClocktowerDisplayOption(
            label = "translated label", displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "任意", displayPrimary = "男爵",
            displaySecondary = "任意文本", displayFooter = null,
            proposition = proposition, presentationSubjectSeats = listOf(6, 7),
        )
        val proposed = HostOpeningSemanticValueV2(
            sourceSeat = 5, abilityRoleId = "investigator",
            effectiveAbilityRoleId = "investigator", basis = "FUNCTIONING",
            subjectMode = "EVIDENCE_PAIR", subjectSeats = listOf(6, 7),
            observationKind = "ROLE", roleId = "baron",
        )
        assertEquals(option, ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed, 5, listOf(option)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed.copy(subjectSeats = listOf(5, 7)), 5, listOf(option)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed.copy(roleId = "spy"), 5, listOf(option)))
    }
}
