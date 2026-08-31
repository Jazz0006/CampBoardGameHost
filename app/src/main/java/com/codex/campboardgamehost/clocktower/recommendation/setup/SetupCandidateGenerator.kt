package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator

internal object SetupCandidateGenerator {
    private const val candidateSchemaVersion = "setup-v1"
    private val fortuneTeller = RoleId("Fortune Teller")
    private val drunk = RoleId("Drunk")
    private val investigator = RoleId("Investigator")

    fun generateClueCandidates(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<DecisionCandidate<SetupClueOutcome>> = buildList {
        addAll(generateDrunkCandidates(game, roleDefinitions))
        addAll(generatePairInformationCandidates(game))
        addAll(generateRedHerringCandidates(game))
        addAll(generateDemonBluffCandidates(game, roleDefinitions))
    }.sortedBy { it.candidateId }

    fun generateDrunkCandidates(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<DecisionCandidate<SetupClueOutcome>> {
        val drunkPlayer = game.players.firstOrNull { it.actualRole == drunk } ?: return emptyList()
        if (drunkPlayer.shownRole != investigator) return emptyList()
        val scriptRoles = scriptRoles(game, roleDefinitions)
        return drunkInformationOptions(game, scriptRoles).map { decisions ->
            val investigatorInfo = decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().single()
            val canonical = listOf(
                investigatorInfo.shownMinion.value,
                investigatorInfo.candidateSeats.sorted().joinToString(","),
            ).joinToString("|")
            DecisionCandidate<SetupClueOutcome>(
                candidateId = stableId("drunk-investigator-info", canonical),
                candidateFamilyId = "drunk-investigator-info",
                outcome = SetupClueOutcome.DrunkShownRole(drunkPlayer.shownRole, investigatorInfo),
                abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                truthRelation = TruthRelation.NOT_APPLICABLE,
                effects = listOf(
                    EffectDraft.Reminder(drunkPlayer.seat, "drunk-investigator-info"),
                ),
                metadata = metadata("drunk-investigator-info", setOf("setup", "drunk", "information")),
            )
        }.toList()
    }

    fun generatePairInformationCandidates(game: GameState): List<DecisionCandidate<SetupClueOutcome>> = game.players
        .asSequence()
        .filter { it.actualRole == RoleId("Librarian") || it.actualRole == investigator }
        .flatMap { source ->
            NaturalPairInformationCandidateGenerator.generate(game, source.seat, source.actualRole).asSequence()
        }
        .map { candidate ->
            DecisionCandidate<SetupClueOutcome>(
                candidateId = candidate.candidateId,
                candidateFamilyId = candidate.candidateFamilyId,
                outcome = SetupClueOutcome.PairInformation(candidate.sourceAbility(), candidate.outcome),
                abilityState = candidate.abilityState,
                truthRelation = candidate.truthRelation,
                registrations = candidate.registrations,
                effects = candidate.effects,
                metadata = candidate.metadata,
            )
        }
        .sortedBy { it.candidateId }
        .toList()

    fun generateRedHerringCandidates(game: GameState): List<DecisionCandidate<SetupClueOutcome>> {
        if (game.players.none { it.actualRole == fortuneTeller }) return emptyList()
        return game.players
            .filter { it.actualAlignment == Alignment.GOOD }
            .sortedBy { it.seat }
            .map { target ->
                DecisionCandidate<SetupClueOutcome>(
                    candidateId = stableId("red-herring", target.seat.toString()),
                    candidateFamilyId = "red-herring",
                    outcome = SetupClueOutcome.RedHerring(target.seat),
                    abilityState = AbilityState.FUNCTIONING,
                    truthRelation = TruthRelation.NOT_APPLICABLE,
                    effects = listOf(EffectDraft.Reminder(target.seat, "fortune-teller-red-herring")),
                    metadata = metadata("red-herring", setOf("setup", "fortune-teller")),
                )
            }
    }

    fun generateDemonBluffCandidates(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<DecisionCandidate<SetupClueOutcome>> {
        val demonPlayer = game.players.firstOrNull { it.actualType == CharacterType.DEMON } ?: return emptyList()
        if (game.players.size < 7) return emptyList()
        val inPlayRoles = game.players.map { it.actualRole }.toSet()
        return demonBluffOptions(game, scriptRoles(game, roleDefinitions), inPlayRoles)
            .filterNotNull()
            .map { decision ->
                val sortedRoles = decision.roles.sortedBy { it.value }
                DecisionCandidate<SetupClueOutcome>(
                    candidateId = stableId("demon-bluffs", sortedRoles.joinToString(",") { it.value }),
                    candidateFamilyId = "demon-bluffs",
                    outcome = SetupClueOutcome.DemonBluffs(sortedRoles),
                    abilityState = AbilityState.FUNCTIONING,
                    truthRelation = TruthRelation.NOT_APPLICABLE,
                    effects = listOf(
                        EffectDraft.PlayerInformation(
                            recipientSeat = demonPlayer.seat,
                            sourceAbility = demonPlayer.actualRole,
                            value = InformationValue.RoleSet(sortedRoles),
                        ),
                    ),
                    metadata = metadata("demon-bluffs", setOf("setup", "demon")),
                )
            }
            .toList()
    }

    fun generatePlans(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
    ): Sequence<CandidatePlan> {
        val scriptRoles = scriptRoles(game, roleDefinitions)
        val inPlayRoles = game.players.map { it.actualRole }.toSet()
        if (lockedDecisions.groupingBy { it.kind() }.eachCount().any { it.value > 1 }) return emptySequence()
        if (lockedDecisions.any { it is StorytellerDecision.DrunkShownRole }) return emptySequence()
        val lockedRedHerring = lockedDecisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()
        val lockedDrunkDecisions = lockedDecisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>()
        val lockedDemonBluffs = lockedDecisions.filterIsInstance<StorytellerDecision.DemonBluffs>().singleOrNull()
        val redHerringOptions = redHerringOptions(game).filter { lockedRedHerring == null || it == lockedRedHerring }
        val drunkOptions = drunkInformationOptions(game, scriptRoles)
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

    fun generatePlanCandidates(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
    ): Sequence<DecisionCandidate<SetupClueOutcome.FullPlan>> =
        generatePlans(game, roleDefinitions, lockedDecisions).map(::planCandidate)

    fun planCandidate(plan: CandidatePlan): DecisionCandidate<SetupClueOutcome.FullPlan> {
        val familyId = drunkInformationFamily(plan.decisions) ?: "setup-plan"
        return DecisionCandidate(
            candidateId = stableId("setup-plan", canonicalPlan(plan.decisions)),
            candidateFamilyId = familyId,
            outcome = SetupClueOutcome.FullPlan(plan.decisions),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            metadata = metadata("setup-plan", setOf("setup", "complete-plan")),
        )
    }

    internal fun drunkInformationFamily(decisions: List<StorytellerDecision>): String? =
        decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>()
            .singleOrNull()
            ?.let { "drunk-investigator-info" }

    private fun scriptRoles(game: GameState, roleDefinitions: List<RoleDefinition>): List<RoleDefinition> =
        roleDefinitions.filter { game.script in it.scriptIds }.distinctBy(RoleDefinition::id)

    private fun stableId(decisionType: String, canonical: String): String = java.lang.Long
        .toUnsignedString(MurmurHash3.low64Utf8("$candidateSchemaVersion|$decisionType|$canonical"), 16)
        .padStart(16, '0')

    internal fun canonicalPlan(decisions: List<StorytellerDecision>): String = decisions.joinToString("|") { decision ->
        when (decision) {
            is StorytellerDecision.RedHerring -> "red-herring:${decision.seat}"
            is StorytellerDecision.DrunkShownRole -> "drunk-role:${decision.role.value}"
            is StorytellerDecision.DrunkInvestigatorInfo -> listOf(
                "drunk-investigator",
                decision.shownMinion.value,
                decision.candidateSeats.sorted().joinToString(","),
            ).joinToString(":")
            is StorytellerDecision.DemonBluffs ->
                "demon-bluffs:${decision.roles.map { it.value }.sorted().joinToString(",")}"
        }
    }

    private fun metadata(decisionType: String, tags: Set<String>) = CandidateMetadata(
        candidateSchemaVersion = candidateSchemaVersion,
        decisionType = decisionType,
        tags = tags,
    )

    private fun DecisionCandidate<*>.sourceAbility(): RoleId =
        (effects.single() as EffectDraft.PlayerInformation).sourceAbility

    private fun redHerringOptions(game: GameState): List<StorytellerDecision.RedHerring?> {
        val hasFortuneTeller = game.players.any { it.actualRole == fortuneTeller }
        if (!hasFortuneTeller) return listOf(null)
        return game.players
            .filter { it.actualAlignment == Alignment.GOOD }
            .sortedBy { it.seat }
            .map { StorytellerDecision.RedHerring(it.seat) }
    }

    private fun drunkInformationOptions(
        game: GameState,
        scriptRoles: List<RoleDefinition>,
    ): Sequence<List<StorytellerDecision>> {
        val drunkPlayer = game.players.firstOrNull { it.actualRole == drunk }
            ?: return sequenceOf(emptyList())
        if (drunkPlayer.shownRole != investigator) return sequenceOf(emptyList())

        val minionRoles = scriptRoles.filter { it.type == CharacterType.MINION }.map { it.id }
        val candidatePairs = unorderedSeatPairs(game.players.map { it.seat }.sorted())

        return sequence {
            for (minionRole in minionRoles) {
                for (pair in candidatePairs) {
                    yield(
                        listOf(
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
