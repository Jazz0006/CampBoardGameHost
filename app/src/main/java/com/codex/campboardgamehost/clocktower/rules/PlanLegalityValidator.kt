package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ConstraintAuthority
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision

internal sealed interface LegalityFailure {
    val code: String
    val constraintAuthority: ConstraintAuthority

    data class MissingRequiredDecision(val decisionType: String) : LegalityFailure {
        override val code: String = "missing-required-decision"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class UnexpectedDecision(val decisionType: String) : LegalityFailure {
        override val code: String = "unexpected-decision"
        override val constraintAuthority = ConstraintAuthority.PRODUCT_POLICY_REQUIRED
    }

    data class MultipleDecisions(val decisionType: String) : LegalityFailure {
        override val code: String = "multiple-decisions"
        override val constraintAuthority = ConstraintAuthority.PRODUCT_POLICY_REQUIRED
    }

    data class MissingSeat(val seat: Int) : LegalityFailure {
        override val code: String = "missing-seat"
        override val constraintAuthority = ConstraintAuthority.PRODUCT_POLICY_REQUIRED
    }

    data class EvilRedHerring(val seat: Int) : LegalityFailure {
        override val code: String = "evil-red-herring"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class RoleOutsideScript(val role: RoleId) : LegalityFailure {
        override val code: String = "role-outside-script"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class InvalidRoleType(
        val role: RoleId,
        val expectedType: CharacterType,
    ) : LegalityFailure {
        override val code: String = "invalid-role-type"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class DrunkShownRoleIsInPlay(val role: RoleId) : LegalityFailure {
        override val code: String = "drunk-shown-role-is-in-play"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class InvalidCandidateCount(val actualCount: Int) : LegalityFailure {
        override val code: String = "invalid-candidate-count"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class DuplicateCandidateSeats(val seats: List<Int>) : LegalityFailure {
        override val code: String = "duplicate-candidate-seats"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class InvalidBluffCount(val actualCount: Int) : LegalityFailure {
        override val code: String = "invalid-bluff-count"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class DuplicateBluffs(val roles: List<RoleId>) : LegalityFailure {
        override val code: String = "duplicate-bluffs"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class BluffIsInPlay(val role: RoleId) : LegalityFailure {
        override val code: String = "bluff-is-in-play"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }

    data class EvilBluff(val role: RoleId) : LegalityFailure {
        override val code: String = "evil-bluff"
        override val constraintAuthority = ConstraintAuthority.OFFICIAL_RULE_REQUIRED
    }
}

internal object PlanLegalityValidator {
    private val fortuneTeller = RoleId("Fortune Teller")
    private val drunk = RoleId("Drunk")
    private val investigator = RoleId("Investigator")

    fun validate(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        plan: CandidatePlan,
    ): List<LegalityFailure> = buildList {
        val scriptRoles = roleDefinitions
            .filter { game.script in it.scriptIds }
            .distinctBy(RoleDefinition::id)
            .associateBy(RoleDefinition::id)
        val inPlayRoles = game.players.map { it.actualRole }.toSet()

        val redHerrings = plan.decisions.filterIsInstance<StorytellerDecision.RedHerring>()
        val drunkShownRoles = plan.decisions.filterIsInstance<StorytellerDecision.DrunkShownRole>()
        val drunkInvestigatorInfos = plan.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>()
        val demonBluffDecisions = plan.decisions.filterIsInstance<StorytellerDecision.DemonBluffs>()

        val hasFortuneTeller = fortuneTeller in inPlayRoles
        validateDecisionCount("red-herring", redHerrings.size, required = hasFortuneTeller)
        redHerrings.singleOrNull()?.let { decision ->
            val player = game.playerAt(decision.seat)
            when {
                player == null -> add(LegalityFailure.MissingSeat(decision.seat))
                player.actualAlignment != Alignment.GOOD -> add(LegalityFailure.EvilRedHerring(decision.seat))
            }
        }

        // Shown identity is already a committed setup fact. Recommendation may consume
        // PlayerState.shownRole but must never select, replace, or lock it as a decision.
        validateDecisionCount("drunk-shown-role", drunkShownRoles.size, required = false)

        // DrunkInvestigatorInfo is a legacy compatibility payload, not a required active
        // recommendation decision. A single historical/locked value remains legal only when
        // the committed shown identity is Investigator; new active plans omit it entirely.
        val drunkPlayer = game.players.singleOrNull { it.actualRole == drunk }
        when {
            drunkInvestigatorInfos.size > 1 ->
                add(LegalityFailure.MultipleDecisions("drunk-investigator-info"))
            drunkInvestigatorInfos.size == 1 && drunkPlayer?.shownRole != investigator ->
                add(LegalityFailure.UnexpectedDecision("drunk-investigator-info"))
        }
        drunkInvestigatorInfos.singleOrNull()?.let { decision ->
            validateRoleType(decision.shownMinion, CharacterType.MINION, scriptRoles)
            if (decision.candidateSeats.size != 2) {
                add(LegalityFailure.InvalidCandidateCount(decision.candidateSeats.size))
            }
            if (decision.candidateSeats.distinct().size != decision.candidateSeats.size) {
                add(LegalityFailure.DuplicateCandidateSeats(decision.candidateSeats))
            }
            decision.candidateSeats
                .filter { game.playerAt(it) == null }
                .forEach { add(LegalityFailure.MissingSeat(it)) }
        }

        val needsDemonBluffs = game.players.size >= 7 && game.players.any {
            it.actualType == CharacterType.DEMON
        }
        validateDecisionCount("demon-bluffs", demonBluffDecisions.size, required = needsDemonBluffs)
        demonBluffDecisions.singleOrNull()?.let { decision ->
            if (decision.roles.size != 3) add(LegalityFailure.InvalidBluffCount(decision.roles.size))
            if (decision.roles.distinct().size != decision.roles.size) {
                add(LegalityFailure.DuplicateBluffs(decision.roles))
            }
            decision.roles.forEach { role ->
                val definition = scriptRoles[role]
                when {
                    definition == null -> add(LegalityFailure.RoleOutsideScript(role))
                    definition.alignment != Alignment.GOOD -> add(LegalityFailure.EvilBluff(role))
                }
                if (role in inPlayRoles) add(LegalityFailure.BluffIsInPlay(role))
            }
        }
    }

    private fun MutableList<LegalityFailure>.validateDecisionCount(
        decisionType: String,
        count: Int,
        required: Boolean,
    ) {
        when {
            count > 1 -> add(LegalityFailure.MultipleDecisions(decisionType))
            required && count == 0 -> add(LegalityFailure.MissingRequiredDecision(decisionType))
            !required && count == 1 -> add(LegalityFailure.UnexpectedDecision(decisionType))
        }
    }

    private fun MutableList<LegalityFailure>.validateRoleType(
        role: RoleId,
        expectedType: CharacterType,
        scriptRoles: Map<RoleId, RoleDefinition>,
    ) {
        val definition = scriptRoles[role]
        when {
            definition == null -> add(LegalityFailure.RoleOutsideScript(role))
            definition.type != expectedType -> add(LegalityFailure.InvalidRoleType(role, expectedType))
        }
    }
}
