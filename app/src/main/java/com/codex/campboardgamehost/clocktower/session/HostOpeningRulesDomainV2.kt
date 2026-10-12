package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightNumericLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain

/**
 * 3B3D-2: real TB Host-domain projection without provider candidate enumeration.
 * Hypothetical Drunk branches are purely local, never setup commits. All concrete
 * legal outputs come from existing rules-owned Pair/Numeric/Setup generators.
 *
 * Unconfirmed Poisoner or Fortune Teller actions are timing barriers. An AI
 * conditional intent is not certified or published before those actions resolve.
 */
internal class HostOpeningRulesDomainV2 private constructor(
    val scope: InitialGlobalOpeningScopeV1,
    val projections: Map<Int?, HostOpeningSemanticProjectionV2>,
) {
    fun forDrunkSeat(seat: Int?): HostOpeningSemanticProjectionV2 =
        requireNotNull(projections[seat]) { "Unknown Host Drunk hypothesis." }

    private data class Branch(
        val hypothesis: InitialOpeningHypothesisV1,
        val candidates: Map<String, Map<String, HostOpeningSemanticValueV2>>,
        val redHerringBySeat: Map<Int, String>,
        val determinedInfo: Set<String>,
        val actions: Map<String, Pair<String, Int?>>,
    )

    companion object {
        private val pairRoles = setOf("washerwoman", "librarian", "investigator")
        private val numberRoles = setOf("chef", "empath")
        private val informationRoles = pairRoles + numberRoles + "fortuneteller"

        fun fromHostSnapshot(
            snapshot: TroubleBrewingGameSnapshotV1,
            registry: ClocktowerCharacterRegistry,
            roleDefinitions: List<RoleDefinition>,
            playerExperience: Map<Int, OpeningPlayerExperienceV1> = emptyMap(),
            gameStateRevision: Long = 0,
            playerInputRevision: Long = 0,
        ): HostOpeningRulesDomainV2 {
            val scriptRoles = roleDefinitions.filter { snapshot.script in it.scriptIds }
            require(scriptRoles.isNotEmpty() &&
                registry.definitions.all { character -> scriptRoles.any { it.id == character.id } }) {
                "Complete TB script role definitions required."
            }
            require(snapshot.grimoireSeats.all { seat ->
                (seat.alive as? SnapshotField.Known<Boolean>)?.value == true &&
                    (seat.poisoned as? SnapshotField.Known<Boolean>)?.value == false
            }) { "Opening projection requires pristine precommit state." }
            val hasDrunk = (snapshot.setupState.hasDrunk as? SnapshotField.Known<Boolean>)?.value
                ?: error("Host hasDrunk must be known.")
            val drunkSeats: List<Int?> = if (hasDrunk) {
                TroubleBrewingDrunkCandidateDomain.legalCandidateRefs(snapshot, registry).map { it.seat }
            } else listOf(null)
            require(drunkSeats.isNotEmpty())
            val shown = snapshot.grimoireSeats.map {
                (it.shownRoleId as SnapshotField.Known<String>).value
            }.toSet()
            val hasDemon = shown.any {
                registry.findByExternalId(it)?.team == ClocktowerCatalogTeam.DEMON
            }
            val bluffs = if (hasDemon && shown.size >= 7) {
                registry.definitions.filter {
                    it.team in setOf(ClocktowerCatalogTeam.TOWNSFOLK, ClocktowerCatalogTeam.OUTSIDER) &&
                        it.externalId !in shown
                }.map { it.externalId }.distinct().sorted()
            } else null
            val branches = drunkSeats.associateWith {
                branch(snapshot, registry, scriptRoles, it)
            }
            val scope = InitialGlobalOpeningScopeV1.fromHost(
                snapshot, registry, branches.values.map { it.hypothesis },
                bluffs, playerExperience, gameStateRevision, playerInputRevision,
            )
            val projections = branches.mapValues { (seat, candidate) ->
                HostOpeningSemanticProjectionV2(
                    scope = scope, drunkSeat = seat,
                    knownRoleIds = registry.definitions.map { it.externalId }.toSet(),
                    candidatesByDecision = candidate.candidates,
                    redHerringDecisionId = "setup.red-herring".takeIf {
                        candidate.redHerringBySeat.isNotEmpty()
                    },
                    redHerringCandidatesBySeat = candidate.redHerringBySeat,
                    determinedInformationIds = candidate.determinedInfo,
                    deferredActions = candidate.actions,
                )
            }
            return HostOpeningRulesDomainV2(scope, projections)
        }

        private fun branch(
            snapshot: TroubleBrewingGameSnapshotV1,
            registry: ClocktowerCharacterRegistry,
            roleDefinitions: List<RoleDefinition>,
            drunkSeat: Int?,
        ): Branch {
            val game = hypothetical(snapshot, registry, drunkSeat)
            val decisions = mutableListOf<InitialOpeningDecisionV1>()
            val determined = mutableListOf<InitialOpeningDeterminedV1>()
            val deferred = mutableListOf<InitialOpeningDeferredV1>()
            val maps = linkedMapOf<String, Map<String, HostOpeningSemanticValueV2>>()
            val determinedInfo = linkedSetOf<String>()
            val actions = linkedMapOf<String, Pair<String, Int?>>()

            val poisonerSeat = game.players.singleOrNull {
                it.actualRole == RoleId("Poisoner")
            }?.seat
            if (poisonerSeat != null) {
                val id = "event:poisoner.target"
                deferred += InitialOpeningDeferredV1(id, listOf("poisoner.target"))
                actions[id] = "poisoner.target" to poisonerSeat
            }
            for (source in game.players.sortedBy { it.seat }) {
                val role = requireNotNull(source.shownRole)
                val shown = requireNotNull(registry.findByRoleId(role)).externalId
                if (shown !in informationRoles) continue
                val id = "first-night:seat-" + source.seat
                val dependencies = buildList {
                    if (poisonerSeat != null) add("poisoner.target")
                    if (shown == "fortuneteller") {
                        val event = "fortune_teller.targets:seat-" + source.seat
                        add(event)
                        val actionId = "event:" + event
                        deferred += InitialOpeningDeferredV1(actionId, listOf(event))
                        actions[actionId] = event to source.seat
                    }
                }
                if (dependencies.isNotEmpty()) {
                    deferred += InitialOpeningDeferredV1(id, dependencies)
                    continue
                }
                val state = requireNotNull(AbilityFunctioningSemantics.stateFor(
                    AbilitySubject(source.actualRole.value, source.shownRole?.value,
                        source.poisoned, source.alive), role.value,
                )) { "No first-night ability." }
                val reliability = when (state) {
                    AbilityFunctioningState.FUNCTIONING -> ReliabilityState.RELIABLE
                    AbilityFunctioningState.DRUNK -> ReliabilityState.DRUNK
                    AbilityFunctioningState.POISONED -> ReliabilityState.POISONED
                }
                val basis = if (reliability == ReliabilityState.DRUNK)
                    "DRUNK_MISINFORMATION" else "FUNCTIONING"
                val domain = when (shown) {
                    in pairRoles -> PairInformationLegalDomain.generate(
                        game, roleDefinitions, source.seat, role, reliability,
                    ).associate { candidate ->
                        val result = candidate.outcome
                        candidate.candidateId to HostOpeningSemanticValueV2(
                            sourceSeat = source.seat,
                            abilityRoleId = shown,
                            effectiveAbilityRoleId = if (reliability == ReliabilityState.DRUNK)
                                null else shown,
                            basis = basis,
                            subjectMode = if (result.shownRole == null) "NONE" else "EVIDENCE_PAIR",
                            subjectSeats = result.candidateSeats,
                            observationKind = if (result.shownRole == null) "ZERO_OUTSIDERS" else "ROLE",
                            roleId = result.shownRole?.let {
                                requireNotNull(registry.findByRoleId(it)).externalId
                            },
                        )
                    }
                    in numberRoles -> FirstNightNumericLegalDomain.generate(
                        game, source.seat, role, reliability,
                    ).associate { candidate ->
                        candidate.candidateId to HostOpeningSemanticValueV2(
                            sourceSeat = source.seat,
                            abilityRoleId = shown,
                            effectiveAbilityRoleId = if (reliability == ReliabilityState.DRUNK)
                                null else shown,
                            basis = basis,
                            subjectMode = if (shown == "empath") "FIXED_NEIGHBORS" else "NONE",
                            subjectSeats = emptyList(),
                            observationKind = "NUMBER", number = candidate.value,
                        )
                    }
                    else -> error("Unexpected immediate first-night role.")
                }
                require(domain.isNotEmpty()) { "Host rules returned an empty legal domain." }
                if (reliability == ReliabilityState.RELIABLE && domain.size == 1) {
                    determined += InitialOpeningDeterminedV1(
                        id, domain.keys.single(), "Host rule-determined first-night information",
                    )
                    determinedInfo += id
                } else {
                    decisions += InitialOpeningDecisionV1(
                        id, shown, source.seat,
                        domain.keys.associateWith { "Host rules-owned information" },
                    )
                    maps[id] = domain
                }
            }

            val red = SetupCandidateGenerator.generateRedHerringCandidates(game).associate {
                (it.outcome as SetupClueOutcome.RedHerring).seat to it.candidateId
            }
            if (red.isNotEmpty()) {
                decisions += InitialOpeningDecisionV1(
                    "setup.red-herring", "RED_HERRING", null,
                    red.values.associateWith { "Host rules-owned Good seat" },
                )
            }
            return Branch(
                InitialOpeningHypothesisV1(drunkSeat, decisions, determined, deferred),
                maps, red, determinedInfo, actions,
            )
        }

        private fun hypothetical(
            snapshot: TroubleBrewingGameSnapshotV1,
            registry: ClocktowerCharacterRegistry,
            drunkSeat: Int?,
        ): GameState {
            val hasDrunk = (snapshot.setupState.hasDrunk as SnapshotField.Known<Boolean>).value
            require(hasDrunk == (drunkSeat != null))
            val players = snapshot.grimoireSeats.map { seat ->
                val shownId = (seat.shownRoleId as SnapshotField.Known<String>).value
                val shown = requireNotNull(registry.findByExternalId(shownId))
                val actual = if (seat.seat == drunkSeat) {
                    require(shown.team == ClocktowerCatalogTeam.TOWNSFOLK &&
                        seat.actualRoleId === SnapshotField.Uncommitted)
                    requireNotNull(registry.findByExternalId("drunk"))
                } else {
                    when (val known = seat.actualRoleId) {
                        is SnapshotField.Known -> require(known.value == shownId)
                        SnapshotField.Uncommitted -> require(hasDrunk &&
                            shown.team == ClocktowerCatalogTeam.TOWNSFOLK)
                        else -> error("Actual role is not established in Host setup.")
                    }
                    shown
                }
                val type = when (actual.team) {
                    ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
                    ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
                    ClocktowerCatalogTeam.MINION -> CharacterType.MINION
                    ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
                    else -> error("Unsupported TB role category.")
                }
                val alignment = if (type in setOf(CharacterType.TOWNSFOLK, CharacterType.OUTSIDER))
                    Alignment.GOOD else Alignment.EVIL
                PlayerState(
                    seat = seat.seat, name = "Seat " + seat.seat,
                    actualRole = actual.id, actualType = type, actualAlignment = alignment,
                    shownRole = shown.id, alive = true, poisoned = false,
                )
            }
            return GameState(snapshot.script, players, snapshot.gameSeed)
        }
    }
}
