package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.kind

internal object CandidateGenerator {
    private val fortuneTeller = RoleId("Fortune Teller")
    private val drunk = RoleId("Drunk")
    private val investigator = RoleId("Investigator")

    fun generatePlans(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
    ): Sequence<CandidatePlan> {
        val scriptRoles = roleDefinitions
            .filter { game.script in it.scriptIds }
            .distinctBy(RoleDefinition::id)
        val inPlayRoles = game.players.map { it.actualRole }.toSet()
        if (lockedDecisions.groupingBy { it.kind() }.eachCount().any { it.value > 1 }) return emptySequence()
        val lockedRedHerring = lockedDecisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()
        val lockedDrunkDecisions = lockedDecisions.filter {
            it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
        }
        val lockedDemonBluffs = lockedDecisions.filterIsInstance<StorytellerDecision.DemonBluffs>().singleOrNull()
        val redHerringOptions = redHerringOptions(game).filter { lockedRedHerring == null || it == lockedRedHerring }
        val drunkOptions = drunkDecisionOptions(game, scriptRoles, inPlayRoles)
            .filter { option -> lockedDrunkDecisions.all { it in option } }
        val demonBluffOptions = demonBluffOptions(game, scriptRoles, inPlayRoles)
            .filter { lockedDemonBluffs == null || it == lockedDemonBluffs }

        return sequence {
            for (redHerring in redHerringOptions) {
                for (drunkDecisions in drunkOptions) {
                    for (demonBluffs in demonBluffOptions) {
                        yield(
                            CandidatePlan(
                                decisions = buildList {
                                    redHerring?.let(::add)
                                    addAll(drunkDecisions)
                                    demonBluffs?.let(::add)
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun redHerringOptions(game: GameState): List<StorytellerDecision.RedHerring?> {
        val hasFortuneTeller = game.players.any { it.actualRole == fortuneTeller }
        if (!hasFortuneTeller) return listOf(null)
        return game.players
            .filter { it.actualAlignment == Alignment.GOOD }
            .sortedBy { it.seat }
            .map { StorytellerDecision.RedHerring(it.seat) }
    }

    private fun drunkDecisionOptions(
        game: GameState,
        scriptRoles: List<RoleDefinition>,
        inPlayRoles: Set<RoleId>,
    ): Sequence<List<StorytellerDecision>> {
        val hasDrunk = game.players.any { it.actualRole == drunk }
        if (!hasDrunk) return sequenceOf(emptyList())

        val shownRoleOptions = scriptRoles
            .filter { it.type == CharacterType.TOWNSFOLK }
            .filterNot { it.id in inPlayRoles }
        val minionRoles = scriptRoles.filter { it.type == CharacterType.MINION }.map { it.id }
        val candidatePairs = unorderedSeatPairs(game.players.map { it.seat }.sorted())

        return sequence {
            for (shownRole in shownRoleOptions) {
                val shownRoleDecision = StorytellerDecision.DrunkShownRole(shownRole.id)
                if (shownRole.id != investigator) {
                    yield(listOf(shownRoleDecision))
                    continue
                }
                for (minionRole in minionRoles) {
                    for (pair in candidatePairs) {
                        yield(
                            listOf(
                                shownRoleDecision,
                                StorytellerDecision.DrunkInvestigatorInfo(
                                    shownMinion = minionRole,
                                    candidateSeats = pair,
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun demonBluffOptions(
        game: GameState,
        scriptRoles: List<RoleDefinition>,
        inPlayRoles: Set<RoleId>,
    ): Sequence<StorytellerDecision.DemonBluffs?> {
        val needsDemonBluffs = game.players.size >= 7 && game.players.any {
            it.actualType == CharacterType.DEMON
        }
        if (!needsDemonBluffs) return sequenceOf(null)

        val legalRoles = scriptRoles
            .filter { it.alignment == Alignment.GOOD }
            .filterNot { it.id in inPlayRoles }
            .map { it.id }
            .distinct()
        return chooseThree(legalRoles).map { StorytellerDecision.DemonBluffs(it) }
    }

    private fun unorderedSeatPairs(seats: List<Int>): List<List<Int>> = buildList {
        for (firstIndex in 0 until seats.lastIndex) {
            for (secondIndex in firstIndex + 1 until seats.size) {
                add(listOf(seats[firstIndex], seats[secondIndex]))
            }
        }
    }

    private fun chooseThree(items: List<RoleId>): Sequence<List<RoleId>> = sequence {
        for (firstIndex in 0 until items.size - 2) {
            for (secondIndex in firstIndex + 1 until items.size - 1) {
                for (thirdIndex in secondIndex + 1 until items.size) {
                    yield(listOf(items[firstIndex], items[secondIndex], items[thirdIndex]))
                }
            }
        }
    }
}
