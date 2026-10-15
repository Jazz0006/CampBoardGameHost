package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightNumericLegalDomain
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/**
 * Experimental facts ONLY from the authoritative Host session + confirmed action journal.
 * Never populate from a model response, a UI draft, or an unverified screenshot.
 *
 * This carries a first-night read-only projection: the real game is already setup-
 * committed. The call site must compare this tuple to a single atomic session
 * revision/observation-log snapshot before relying on any verdict.
 */
internal data class HostOpeningConfirmedFirstNightV2(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val phase: StorytellerPhase,
    val round: Int,
    val game: GameState,
    val drunkSeat: Int?,
    val redHerringSeat: Int?,
    val confirmedPoisonerTargetSeat: Int? = null,
    val confirmedFortuneTellerTargets: Map<Int, List<Int>> = emptyMap(),
    val published: List<HostOpeningPublishedInformationV2> = emptyList(),
) {
    init {
        require(gameId.isNotBlank() && gameStateRevision >= 0 && playerInputRevision >= 0)
        require(phase == StorytellerPhase.FIRST_NIGHT && round == 1)
        require(game.players.map { it.seat } == (1..game.players.size).toList())
        require(confirmedFortuneTellerTargets.keys.all { it in 1..game.players.size })
        require(published.map { it.decisionId }.distinct().size == published.size)
    }
}

/** Immutable existing observation identity; original public fact is never regenerated. */
internal data class HostOpeningPublishedInformationV2(
    val decisionId: String,
    val recordId: String,
    val semantic: HostOpeningSemanticValueV2,
) {
    init {
        require(decisionId.isNotBlank() && recordId.isNotBlank())
        require(semantic.observationKind != "NONE")
    }
}

internal enum class HostOpeningResidualDispositionV2 {
    PUBLISHED_LOCKED,
    STILL_LEGAL_UNPUBLISHED,
    HOST_RECOMPUTES,
    WAITING_EVENT,
    REPLAN_REQUIRED,
}

internal data class HostOpeningResidualDecisionV2(
    val decisionId: String,
    val disposition: HostOpeningResidualDispositionV2,
    val legalMatchingCandidateIds: Set<String> = emptySet(),
    val publishedRecordId: String? = null,
)

internal enum class HostOpeningResidualOutcomeV2 {
    WAIT_FOR_HOST_ACTION,
    REPLAN_WHOLE_UNPUBLISHED_RESIDUAL,
    REVIEW_READY_NO_PUBLICATION,
}

internal data class HostOpeningResidualReviewV2(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val outcome: HostOpeningResidualOutcomeV2,
    val decisions: List<HostOpeningResidualDecisionV2>,
) {
    val replanDecisionIds: Set<String> get() = decisions
        .filter { it.disposition == HostOpeningResidualDispositionV2.REPLAN_REQUIRED }
        .mapTo(linkedSetOf()) { it.decisionId }

    val preservedPublishedRecordIds: Set<String> get() = decisions
        .mapNotNullTo(linkedSetOf()) { it.publishedRecordId }
}

/**
 * First-night event-time residual gate. A positive review DOES NOT publish
 * an observation, adopt a new candidate or mutate session history.
 *
 * - Published observations remain immutable regardless of later poisoning.
 * - Pending actions cannot be anticipated by a model.
 * - Prior conditional intentions are tested against current rules-owned
 *   candidate domains, not accepted because their wording sounds plausible.
 * - If anything changed illegally, request ONE whole-residual replan.
 */
internal object HostOpeningResidualEventGateV2 {
    private val pairRoles = setOf("washerwoman", "librarian", "investigator")
    private val numericRoles = setOf("chef", "empath")
    private val infoRoles = pairRoles + numericRoles + "fortuneteller"

    fun review(
        original: HostOpeningSemanticAcceptanceV2,
        initialHostProjection: HostOpeningSemanticProjectionV2,
        facts: HostOpeningConfirmedFirstNightV2,
        registry: ClocktowerCharacterRegistry,
        roleDefinitions: List<RoleDefinition>,
    ): HostOpeningResidualReviewV2 {
        val scope = initialHostProjection.scope
        require(original.selection.validFor(scope))
        require(initialHostProjection.drunkSeat == original.selection.drunkSeat)
        require(facts.gameId == scope.snapshot.gameId &&
            facts.drunkSeat == original.selection.drunkSeat &&
            facts.gameStateRevision >= scope.gameStateRevision &&
            facts.playerInputRevision >= scope.playerInputRevision) {
            "Stale or mismatched confirmed Host event projection."
        }
        require(facts.game.script == scope.snapshot.script &&
            facts.game.seed == scope.snapshot.gameSeed &&
            facts.game.players.size == scope.snapshot.grimoireSeats.size)
        val scriptRoles = roleDefinitions.filter { facts.game.script in it.scriptIds }
        require(scriptRoles.isNotEmpty() &&
            registry.definitions.all { def -> scriptRoles.any { it.id == def.id } })
        val players = facts.game.players.sortedBy { it.seat }
        for (player in players) {
            val seat = scope.snapshot.grimoireSeats[player.seat - 1]
            val shownId = (seat.shownRoleId as SnapshotField.Known<String>).value
            val shown = requireNotNull(registry.findByExternalId(shownId))
            require(player.shownRole == shown.id)
            val actualId = (seat.actualRoleId as? SnapshotField.Known<String>)?.value
            val expected = if (player.seat == facts.drunkSeat) "drunk" else actualId ?: shownId
            require(registry.findByRoleId(player.actualRole)?.externalId == expected) {
                "First-night event projection cannot change the committed role assignment."
            }
            require(player.alive) { "First-night pre-dawn residual review expects live players." }
        }
        require(facts.game.players.count { it.actualRole == RoleId("Drunk") } ==
            if (facts.drunkSeat == null) 0 else 1)
        val ftSource = players.singleOrNull { it.actualRole == RoleId("Fortune Teller") }
        val red = original.selection.choices["setup.red-herring"]
        val initialRedSeat = initialHostProjection.redHerringCandidatesBySeat.entries
            .singleOrNull { it.value == red }?.key
        require(facts.redHerringSeat == initialRedSeat) {
            "Initial chosen Red Herring is immutable and must be confirmed by Host."
        }
        require((ftSource != null) == (facts.redHerringSeat != null))

        val poisoner = players.singleOrNull { it.actualRole == RoleId("Poisoner") }
        require(poisoner != null || facts.confirmedPoisonerTargetSeat == null)
        val poisoned = players.filter { it.poisoned }.map { it.seat }.toSet()
        if (facts.confirmedPoisonerTargetSeat != null) {
            require(poisoned == setOf(facts.confirmedPoisonerTargetSeat)) {
                "Confirmed Poisoner target must agree with authoritative effective status."
            }
        } else {
            require(poisoned.isEmpty()) {
                "Never infer a Poisoner confirmation from poisoned flags alone."
            }
        }
        require(facts.confirmedFortuneTellerTargets.all { (source, targets) ->
            FortuneTellerInformationSemantics.legalTargetPairs(facts.game, source)
                .any { listOf(it.first, it.second) == targets.sorted() }
        }) { "Unconfirmed or illegal Fortune Teller query." }
        val knownActions = initialHostProjection.deferredActions
        require(facts.confirmedFortuneTellerTargets.keys.all { seat ->
            knownActions["event:fortune_teller.targets:seat-" + seat] ==
                ("fortune_teller.targets:seat-" + seat to seat)
        })
        require(facts.confirmedPoisonerTargetSeat == null ||
            knownActions["event:poisoner.target"] ==
                ("poisoner.target" to requireNotNull(poisoner).seat))
        val decisions = mutableListOf<HostOpeningResidualDecisionV2>()
        val existing = initialHostProjection.branch
        val knownInfo = (
            initialHostProjection.candidatesByDecision.keys +
                initialHostProjection.determinedInformationIds +
                existing.deferred.map { it.decisionId }.filter { it.startsWith("first-night:seat-") }
            ).toSet()
        val published = facts.published.associateBy { it.decisionId }
        require(published.keys.all { it in knownInfo })
        require(facts.published.map { it.recordId }.distinct().size == facts.published.size)
        val proposed = original.conditional.associateBy { it.decisionId }
        val initialById = initialHostProjection.candidatesByDecision
        val all = (initialById.keys + initialHostProjection.determinedInformationIds +
            existing.deferred.map { it.decisionId }.filter { it.startsWith("first-night:seat-") })
            .toSortedSet()

        for (id in all) {
            val locked = published[id]
            if (locked != null) {
                require(locked.semantic.sourceSeat == id.substringAfterLast('-').toInt())
                decisions += HostOpeningResidualDecisionV2(
                    id, HostOpeningResidualDispositionV2.PUBLISHED_LOCKED,
                    publishedRecordId = locked.recordId)
                continue
            }
            val seat = id.substringAfterLast('-').toInt()
            val player = requireNotNull(facts.game.playerAt(seat))
            val external = requireNotNull(registry.findByRoleId(requireNotNull(player.shownRole))).externalId
            require(external in infoRoles)
            val queryMissing = external == "fortuneteller" &&
                seat !in facts.confirmedFortuneTellerTargets
            val poisonMissing = poisoner != null && facts.confirmedPoisonerTargetSeat == null
            if (queryMissing || poisonMissing) {
                decisions += HostOpeningResidualDecisionV2(
                    id, HostOpeningResidualDispositionV2.WAITING_EVENT)
                continue
            }

            val legal = currentPublicDomain(
                facts.game, player.seat, external, registry, scriptRoles,
                facts.redHerringSeat, facts.confirmedFortuneTellerTargets[seat])
            val intent = proposed[id]?.proposed ?: run {
                val oldChoice = original.selection.choices[id]
                initialById[id]?.get(oldChoice)
            }
            val disposition = when {
                intent == null && id in initialHostProjection.determinedInformationIds ->
                    // The value is recomputed by Host, never taken from model text.
                    if (legal.size == 1) HostOpeningResidualDispositionV2.HOST_RECOMPUTES
                    else HostOpeningResidualDispositionV2.REPLAN_REQUIRED
                intent == null -> HostOpeningResidualDispositionV2.REPLAN_REQUIRED
                legal.any { it.value.samePublicResultAs(intent) } ->
                    HostOpeningResidualDispositionV2.STILL_LEGAL_UNPUBLISHED
                else -> HostOpeningResidualDispositionV2.REPLAN_REQUIRED
            }
            val matching = legal.filterValues { value ->
                intent != null && value.samePublicResultAs(intent)
            }.keys
            decisions += HostOpeningResidualDecisionV2(id, disposition, matching)
        }
        // Once a prerequisite action is confirmed, its own journal result belongs
        // to Host. It is NOT a pending model decision and has no information to publish.
        val outstandingActions = (poisoner != null &&
            facts.confirmedPoisonerTargetSeat == null) ||
            knownActions.values.any { (event, seat) ->
                event.startsWith("fortune_teller.targets:seat-") &&
                    seat !in facts.confirmedFortuneTellerTargets
            }
        val outcome = when {
            decisions.any { it.disposition == HostOpeningResidualDispositionV2.REPLAN_REQUIRED } ->
                HostOpeningResidualOutcomeV2.REPLAN_WHOLE_UNPUBLISHED_RESIDUAL
            outstandingActions || decisions.any {
                it.disposition == HostOpeningResidualDispositionV2.WAITING_EVENT
            } -> HostOpeningResidualOutcomeV2.WAIT_FOR_HOST_ACTION
            else -> HostOpeningResidualOutcomeV2.REVIEW_READY_NO_PUBLICATION
        }
        return HostOpeningResidualReviewV2(
            facts.gameStateRevision, facts.playerInputRevision, outcome, decisions)
    }

    /** Equality of visible information, not equality of possible registration witnesses. */
    private fun HostOpeningSemanticValueV2.samePublicResultAs(
        proposed: HostOpeningSemanticValueV2,
    ): Boolean =
        sourceSeat == proposed.sourceSeat &&
            abilityRoleId == proposed.abilityRoleId &&
            effectiveAbilityRoleId == proposed.effectiveAbilityRoleId &&
            observationKind == proposed.observationKind &&
            number == proposed.number && roleId == proposed.roleId &&
            boolean == proposed.boolean &&
            (subjectMode == proposed.subjectMode ||
                proposed.subjectMode == "PLAYER_QUERY_PENDING" &&
                subjectMode == "PLAYER_SELECTED_TARGETS") &&
            (subjectSeats == proposed.subjectSeats ||
                proposed.subjectMode == "PLAYER_QUERY_PENDING" &&
                proposed.subjectSeats.isEmpty())

    private fun currentPublicDomain(
        game: GameState,
        seat: Int,
        shownId: String,
        registry: ClocktowerCharacterRegistry,
        roleDefinitions: List<RoleDefinition>,
        redHerringSeat: Int?,
        ftTargets: List<Int>?,
    ): Map<String, HostOpeningSemanticValueV2> {
        val player = requireNotNull(game.playerAt(seat))
        val role = requireNotNull(player.shownRole)
        val state = requireNotNull(AbilityFunctioningSemantics.stateFor(
            AbilitySubject(player.actualRole.value, role.value, player.poisoned, player.alive),
            role.value,
        ))
        val reliability = when (state) {
            AbilityFunctioningState.FUNCTIONING -> ReliabilityState.RELIABLE
            AbilityFunctioningState.DRUNK -> ReliabilityState.DRUNK
            AbilityFunctioningState.POISONED -> ReliabilityState.POISONED
        }
        val basis = when (reliability) {
            ReliabilityState.RELIABLE -> "FUNCTIONING"
            ReliabilityState.DRUNK -> "DRUNK_MISINFORMATION"
            ReliabilityState.POISONED -> "STATUS_DEPENDENT"
        }
        val actualAbility = if (player.actualRole == RoleId("Drunk")) null else shownId
        fun value(
            kind: String, mode: String = "NONE", seats: List<Int> = emptyList(),
            number: Int? = null, role: String? = null, answer: Boolean? = null,
        ) = HostOpeningSemanticValueV2(
            seat, shownId, actualAbility, basis,
            mode, seats, kind, number, role, answer)

        return when (shownId) {
            in pairRoles -> PairInformationLegalDomain.generate(
                game, roleDefinitions, seat, role, reliability,
            ).associate { candidate ->
                val result = candidate.outcome
                candidate.candidateId to if (result.shownRole == null) {
                    value("ZERO_OUTSIDERS")
                } else {
                    value("ROLE", "EVIDENCE_PAIR", result.candidateSeats,
                        role = requireNotNull(registry.findByRoleId(
                            requireNotNull(result.shownRole))).externalId)
                }
            }
            in numericRoles -> FirstNightNumericLegalDomain.generate(
                game, seat, role, reliability,
            ).associate { candidate ->
                candidate.candidateId to value("NUMBER",
                    mode = if (shownId == "empath") "FIXED_NEIGHBORS" else "NONE",
                    number = candidate.value)
            }
            "fortuneteller" -> {
                val targets = requireNotNull(ftTargets) { "FT target not yet confirmed." }.sorted()
                require(FortuneTellerInformationSemantics.legalTargetPairs(game, seat)
                    .any { listOf(it.first, it.second) == targets })
                val legalAnswers = when (reliability) {
                    ReliabilityState.POISONED, ReliabilityState.DRUNK -> setOf(false, true)
                    ReliabilityState.RELIABLE -> {
                        val red = requireNotNull(redHerringSeat)
                        val exact = FortuneTellerInformationSemantics.healthyResult(
                            game, targets, red)
                        if (exact) setOf(true) else {
                            // Recluse can register Demon when relevant, possibly with
                            // multiple legal witnesses. Do not force a witness.
                            val special = targets.any { targetSeat ->
                                val target = requireNotNull(game.playerAt(targetSeat))
                                TroubleBrewingRegistrationDomain.resolve(
                                    TroubleBrewingRegistrationSubject.from(target),
                                    roleDefinitions,
                                    RegistrationQuestion.DEMON,
                                ).special.any { registered ->
                                    registered.registeredType ==
                                        com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON
                                }
                            }
                            if (special) setOf(false, true) else setOf(false)
                        }
                    }
                }
                legalAnswers.associate { answer ->
                    ("answer-" + if (answer) "yes" else "no") to value(
                        "BOOLEAN", "PLAYER_SELECTED_TARGETS", targets, answer = answer)
                }
            }
            else -> error("Unrecognized TB first-night information role.")
        }
    }
}
