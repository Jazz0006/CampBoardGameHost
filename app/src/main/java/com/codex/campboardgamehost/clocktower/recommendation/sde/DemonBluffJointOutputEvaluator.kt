package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator

/**
 * One already-legal Demon bluff triplet projected into the SDE planning vocabulary.
 *
 * This model validates shape only. Script legality, in-play exclusion and player-count rules remain
 * owned by [SetupCandidateGenerator].
 */
internal data class DemonBluffJointOutputCandidate(
    val candidateId: String,
    val roles: List<RoleId>,
) {
    init {
        require(candidateId.isNotBlank()) { "Demon bluff joint-output candidate ID cannot be blank." }
        require(roles.size == 3) { "A Demon bluff joint-output candidate must contain exactly three roles." }
        require(roles.distinct().size == roles.size) {
            "A Demon bluff joint-output candidate cannot repeat a role."
        }
    }
}

/**
 * Exact counterworld support for one bluff role at the actual Demon seat.
 *
 * [byRecipient] remains descriptive exact evidence. Selection policy deliberately stays out of this
 * seam so SDE-2D3 can later define the durable strategic quotient without replacing this authority.
 */
internal data class DemonBluffRoleSupport(
    val role: RoleId,
    val byRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
) {
    init {
        require(byRecipient.isNotEmpty()) { "Demon bluff role support requires at least one recipient." }
        require(byRecipient.map(ExactHypotheticalObservationBundleDiagnostics::recipientSeat).distinct().size ==
            byRecipient.size
        ) {
            "Demon bluff role support may contain at most one diagnostic per recipient."
        }
    }

    val supportedRecipientSeats: Set<Int>
        get() = byRecipient
            .filter { diagnostic -> diagnostic.after.value.signum() > 0 }
            .mapTo(linkedSetOf(), ExactHypotheticalObservationBundleDiagnostics::recipientSeat)
}

/**
 * One legal triplet composed from shared per-role exact support.
 *
 * The support objects are intentionally shared with [DemonBluffJointOutputEvaluation.Ready.roleSupports]:
 * triplets do not own or recompute exact world scans.
 */
internal data class DemonBluffJointOutputDiagnostics(
    val candidateId: String,
    val roles: List<RoleId>,
    val roleSupports: List<DemonBluffRoleSupport>,
) {
    init {
        require(roles.size == 3 && roles.distinct().size == roles.size)
        require(roleSupports.map(DemonBluffRoleSupport::role) == roles) {
            "Demon bluff triplet support must preserve candidate role order."
        }
    }
}

internal sealed interface DemonBluffJointOutputEvaluation {
    data class Ready(
        val roleSupports: List<DemonBluffRoleSupport>,
        val candidates: List<DemonBluffJointOutputDiagnostics>,
    ) : DemonBluffJointOutputEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : DemonBluffJointOutputEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred Demon bluff joint-output evaluation must identify missing exact capabilities."
            }
        }
    }
}

/**
 * Lossless adapter from the existing setup legality owner into SDE joint-output candidates.
 *
 * No bluff legality is copied here.
 */
internal object SetupDemonBluffJointOutputAdapter {
    fun legalCandidates(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<DemonBluffJointOutputCandidate> =
        SetupCandidateGenerator.generateDemonBluffCandidates(game, roleDefinitions).map { candidate ->
            val outcome = candidate.outcome as? SetupClueOutcome.DemonBluffs
                ?: error("Setup Demon bluff producer returned a non-bluff outcome.")
            DemonBluffJointOutputCandidate(
                candidateId = candidate.candidateId,
                roles = outcome.roles.toList(),
            )
        }
}

/**
 * SDE-2D2 exact support overlay for uncommitted Trouble Brewing Demon bluffs.
 *
 * A bluff is not a current public fact. The diagnostic probe asks whether the already-visible
 * whole-bundle facts still permit a counterworld where the actual Demon seat is *shown as* a
 * candidate bluff role. Each distinct role is queried once per recipient. Legal triplets are then
 * composed from those shared role-support records, avoiding an exact scan per three-role combination.
 *
 * This object is mutation-free and performs no selection, commitment, or heuristic fallback.
 */
internal object TroubleBrewingDemonBluffJointOutputEvaluator {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        actualDemonSeat: Int,
        evaluationRecipientSeats: Set<Int>,
        publicWholeBundleObservations: List<EpistemicObservation>,
        candidates: List<DemonBluffJointOutputCandidate>,
    ): DemonBluffJointOutputEvaluation {
        require(actualDemonSeat > 0) { "Actual Demon seat must be positive." }
        require(evaluationRecipientSeats.isNotEmpty()) {
            "Demon bluff joint-output evaluation requires at least one recipient."
        }
        require(evaluationRecipientSeats.all { it > 0 }) {
            "Demon bluff joint-output recipient seats must be positive."
        }
        require(publicWholeBundleObservations.all { it.visibility == ObservationVisibility.PUBLIC }) {
            "Demon bluff joint-output whole-bundle observations must be public projections."
        }
        require(candidates.isNotEmpty()) {
            "Demon bluff joint-output evaluation requires at least one legal triplet."
        }
        require(candidates.map(DemonBluffJointOutputCandidate::candidateId).distinct().size == candidates.size) {
            "Demon bluff joint-output candidate IDs must be unique."
        }

        val actualDemon = context.initialSnapshot.gameState.players
            .singleOrNull { player -> player.actualType == CharacterType.DEMON }
        require(actualDemon?.seat == actualDemonSeat) {
            "Trouble Brewing bluff evaluation must target the unique actual Demon seat."
        }

        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val recipientSeats = evaluationRecipientSeats.toSortedSet()
        val distinctRoles = candidates
            .flatMap(DemonBluffJointOutputCandidate::roles)
            .distinct()
            .sortedBy(RoleId::value)
        val probesByRole = distinctRoles.associateWith { role ->
            strictShownRoleProbe(
                formalSnapshotId = formal.snapshotId,
                context = context,
                actualDemonSeat = actualDemonSeat,
                role = role,
            )
        }

        val queries = buildList {
            distinctRoles.forEachIndexed { roleIndex, role ->
                recipientSeats.forEach { recipientSeat ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = queryId(roleIndex, recipientSeat, role),
                            recipientSeat = recipientSeat,
                            observations = publicWholeBundleObservations + probesByRole.getValue(role),
                        ),
                    )
                }
            }
        }

        return when (
            val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context,
                queries = queries,
            )
        ) {
            is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                DemonBluffJointOutputEvaluation.Deferred(exact.missingCapabilities)

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                val expectedCount = distinctRoles.size * recipientSeats.size
                require(exact.diagnostics.size == expectedCount) {
                    "Exact evaluator returned ${exact.diagnostics.size} diagnostics for $expectedCount " +
                        "Demon bluff role-support probes."
                }
                val diagnosticsById = exact.diagnostics.associateBy(
                    ExactHypotheticalObservationBundleDiagnostics::bundleId,
                )
                val supportByRole = linkedMapOf<RoleId, DemonBluffRoleSupport>()
                distinctRoles.forEachIndexed { roleIndex, role ->
                    supportByRole[role] = DemonBluffRoleSupport(
                        role = role,
                        byRecipient = recipientSeats.map { recipientSeat ->
                            diagnosticsById.getValue(queryId(roleIndex, recipientSeat, role))
                        },
                    )
                }

                val sharedSupports = supportByRole.values.toList()
                DemonBluffJointOutputEvaluation.Ready(
                    roleSupports = sharedSupports,
                    candidates = candidates.map { candidate ->
                        DemonBluffJointOutputDiagnostics(
                            candidateId = candidate.candidateId,
                            roles = candidate.roles,
                            roleSupports = candidate.roles.map(supportByRole::getValue),
                        )
                    },
                )
            }
        }
    }

    private fun strictShownRoleProbe(
        formalSnapshotId: String,
        context: ExactHistoricalHypotheticalContext,
        actualDemonSeat: Int,
        role: RoleId,
    ): EpistemicObservation = EpistemicObservation(
        observationId = "sde-2d2-bluff-probe-${canonicalRole(role)}",
        snapshotId = formalSnapshotId,
        phase = context.initialPhase,
        round = context.initialRound,
        sequence = 0,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.ShownRoleAt(
            seat = actualDemonSeat,
            role = role,
        ),
    )

    private fun queryId(
        roleIndex: Int,
        recipientSeat: Int,
        role: RoleId,
    ): String = "sde-2d2:bluff-role:$roleIndex:${canonicalRole(role)}:recipient-$recipientSeat"

    private fun canonicalRole(role: RoleId): String =
        role.value.lowercase().replace(' ', '-')
}
