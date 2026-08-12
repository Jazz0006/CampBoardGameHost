package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef

/** Exact, intentionally simple Trouble Brewing setup enumerator used as the A3 correctness oracle. */
object TroubleBrewingWorldEnumerator {
    private val BARON_SETUP = WorldExplanationClusterId("baron-setup")

    fun enumerate(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
    ): EnumeratedWorldSet {
        val catalog = roleDefinitions.filter { rulesetRef.scriptId in it.scriptIds }.associateBy(RoleDefinition::id)
        require(catalog.isNotEmpty()) { "No roles belong to ruleset ${rulesetRef.scriptId.value}." }
        val playerCount = inferPlayerCount(knowledge)
        val knownProfiles = knowledge.setupKnowledge.filterIsInstance<InformationProposition.SetupProfile>().distinct()
        val profiles = if (knownProfiles.isEmpty()) TroubleBrewingSetupProfiles.legalProfiles(playerCount) else knownProfiles
        require(profiles.all { it in TroubleBrewingSetupProfiles.legalProfiles(playerCount) }) {
            "Setup knowledge contains an illegal Trouble Brewing profile."
        }
        val worlds = buildList {
            profiles.forEach { profile ->
                enumerateProfile(profile, knowledge, catalog).forEach(::add)
            }
        }
        return EnumeratedWorldSet.fromWorlds(rulesetRef, knowledge, hypothesis, catalog.values, worlds)
    }

    private fun inferPlayerCount(knowledge: PlayerKnowledgeSnapshot): Int {
        val explicitCounts = knowledge.setupKnowledge.filterIsInstance<InformationProposition.PlayerCount>().map { it.value }
        val profiles = knowledge.setupKnowledge.filterIsInstance<InformationProposition.SetupProfile>()
        val totals = (explicitCounts + profiles.map { it.townsfolk + it.outsiders + it.minions + it.demons }).distinct()
        require(totals.isNotEmpty()) { "A3 enumeration requires known player-count knowledge." }
        require(totals.size == 1) { "Setup knowledge contains inconsistent player counts." }
        return totals.single()
    }

    private fun enumerateProfile(
        profile: InformationProposition.SetupProfile,
        knowledge: PlayerKnowledgeSnapshot,
        catalog: Map<RoleId, RoleDefinition>,
    ): Sequence<EnumeratedWorld> = sequence {
        val seats = 1..(profile.townsfolk + profile.outsiders + profile.minions + profile.demons)
        val targetCounts = mapOf(
            CharacterType.TOWNSFOLK to profile.townsfolk,
            CharacterType.OUTSIDER to profile.outsiders,
            CharacterType.MINION to profile.minions,
            CharacterType.DEMON to profile.demons,
        )
        val rolesByType = catalog.values.groupBy(RoleDefinition::type).mapValues { (_, values) -> values.sortedBy { it.id.value } }
        val pinnedRoles = knowledge.setupKnowledge.filterIsInstance<InformationProposition.RoleAt>()
            .associate { it.seat to it.role }
        require(pinnedRoles.keys.all { it in seats }) { "Pinned role knowledge contains an unknown seat." }
        require(pinnedRoles.values.all(catalog::containsKey)) { "Pinned role knowledge contains an unknown role." }
        require(pinnedRoles.values.distinct().size == pinnedRoles.size) { "Pinned roles must remain unique." }
        val assignment = linkedMapOf<Int, RoleId>()
        val used = linkedSetOf<RoleId>()
        val remaining = targetCounts.toMutableMap()

        suspend fun SequenceScope<EnumeratedWorld>.walk(seat: Int) {
            if (seat > seats.last) {
                if (assignment[knowledge.recipientSeat]?.value.equals("Drunk", true) &&
                    knowledge.perceivedRole in assignment.values
                ) return
                val hasBaron = assignment.values.any { it.value.equals("Baron", true) }
                val isBaronProfile = TroubleBrewingSetupProfiles.isBaronProfile(seats.last, profile)
                if (hasBaron != isBaronProfile) return
                val canonical = assignment.toSortedMap()
                val baseClusters = if (hasBaron) setOf(BARON_SETUP) else emptySet()
                val fortuneTellerInPlay = canonical.values.any { it.value.equals("Fortune Teller", true) }
                if (fortuneTellerInPlay) {
                    for (redHerring in canonical.filterValues { catalog.getValue(it).alignment == Alignment.GOOD }.keys) {
                        for (world in mechanicalVariants(canonical, redHerring, knowledge, baseClusters)) yield(world)
                    }
                } else for (world in mechanicalVariants(canonical, null, knowledge, baseClusters)) yield(world)
                return
            }

            val candidates = pinnedRoles[seat]?.let { listOf(catalog.getValue(it)) }
                ?: if (seat == knowledge.recipientSeat) {
                    recipientActualRoleCandidates(knowledge.perceivedRole, catalog)
                } else catalog.values.sortedBy { it.id.value }
            for (role in candidates) {
                if (role.id in used || remaining.getValue(role.type) == 0) continue
                assignment[seat] = role.id
                used += role.id
                remaining[role.type] = remaining.getValue(role.type) - 1
                walk(seat + 1)
                remaining[role.type] = remaining.getValue(role.type) + 1
                used -= role.id
                assignment.remove(seat)
            }
        }
        walk(1)
    }

    private fun recipientActualRoleCandidates(
        perceivedRole: RoleId,
        catalog: Map<RoleId, RoleDefinition>,
    ): List<RoleDefinition> {
        val perceived = catalog[perceivedRole]
            ?: throw IllegalArgumentException("Perceived role ${perceivedRole.value} is not in the ruleset.")
        return buildList {
            add(perceived)
            if (perceived.type == CharacterType.TOWNSFOLK) {
                catalog.values.singleOrNull { it.id.value.equals("Drunk", true) }?.let(::add)
            }
        }
    }

    private fun recipientShownRoles(
        rolesBySeat: Map<Int, RoleId>,
        knowledge: PlayerKnowledgeSnapshot,
    ): Map<Int, RoleId> = if (rolesBySeat[knowledge.recipientSeat]?.value.equals("Drunk", true)) {
        mapOf(knowledge.recipientSeat to knowledge.perceivedRole)
    } else emptyMap()

    private fun mechanicalVariants(
        rolesBySeat: Map<Int, RoleId>,
        redHerringSeat: Int?,
        knowledge: PlayerKnowledgeSnapshot,
        clusters: Set<WorldExplanationClusterId>,
    ): Sequence<EnumeratedWorld> = sequence {
        val drunkSeat = rolesBySeat.entries.singleOrNull { it.value.value.equals("Drunk", true) }?.key
        val poisonerInPlay = rolesBySeat.values.any { it.value.equals("Poisoner", true) }
        val poisonTargets: List<Int?> = if (poisonerInPlay) rolesBySeat.keys.map { it } else listOf(null)
        for (poisonTarget in poisonTargets) {
            val abilityStates = buildMap {
                if (poisonTarget != null) put(poisonTarget, AbilityState.MALFUNCTIONING_POISONED)
                if (drunkSeat != null) put(drunkSeat, AbilityState.MALFUNCTIONING_DRUNK)
            }
            yield(EnumeratedWorld(
                rolesBySeat = rolesBySeat,
                redHerringSeat = redHerringSeat,
                shownRolesBySeat = recipientShownRoles(rolesBySeat, knowledge),
                abilityStatesBySeat = abilityStates,
                explanationClusters = clusters,
            ))
        }
    }
}

object TroubleBrewingSetupProfiles {
    private val STANDARD = mapOf(
        5 to InformationProposition.SetupProfile(3, 0, 1, 1),
        6 to InformationProposition.SetupProfile(3, 1, 1, 1),
        7 to InformationProposition.SetupProfile(5, 0, 1, 1),
        8 to InformationProposition.SetupProfile(5, 1, 1, 1),
        9 to InformationProposition.SetupProfile(5, 2, 1, 1),
        10 to InformationProposition.SetupProfile(7, 0, 2, 1),
        11 to InformationProposition.SetupProfile(7, 1, 2, 1),
        12 to InformationProposition.SetupProfile(7, 2, 2, 1),
        13 to InformationProposition.SetupProfile(9, 0, 3, 1),
        14 to InformationProposition.SetupProfile(9, 1, 3, 1),
        15 to InformationProposition.SetupProfile(9, 2, 3, 1),
    )

    fun standard(playerCount: Int): InformationProposition.SetupProfile =
        STANDARD[playerCount] ?: throw IllegalArgumentException("Trouble Brewing supports 5 to 15 non-Traveller players.")

    fun withBaron(playerCount: Int): InformationProposition.SetupProfile {
        val standard = standard(playerCount)
        require(standard.townsfolk >= 2)
        return standard.copy(townsfolk = standard.townsfolk - 2, outsiders = standard.outsiders + 2)
    }

    fun legalProfiles(playerCount: Int): List<InformationProposition.SetupProfile> =
        listOf(standard(playerCount), withBaron(playerCount)).distinct()

    fun isBaronProfile(playerCount: Int, profile: InformationProposition.SetupProfile): Boolean =
        profile == withBaron(playerCount)
}
