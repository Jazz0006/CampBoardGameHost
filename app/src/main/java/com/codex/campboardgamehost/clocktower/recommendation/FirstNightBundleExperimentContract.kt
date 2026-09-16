package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.SemanticStableId
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.util.Collections

/** Which owner determines one member of a complete first-night bundle. */
internal enum class FirstNightBundleEntryControl {
    RULE_DETERMINED,
    STORYTELLER_CONTROLLED,
}

/**
 * Exposure used by the first BEGINNER stress experiment.
 *
 * This is deliberately not an observation visibility. The real Night 1 observation remains private
 * in the durable game history; PUBLIC_GOOD_INFO means only that the experiment assumes the healthy
 * good recipient shares that information on Day 1.
 */
internal enum class FirstNightBundleProfileExposure {
    PUBLIC_GOOD_INFO,
    NOT_SHARED,
}

/**
 * One legal member of a complete first-night information bundle.
 *
 * [observation] is nullable on purpose. Some Storyteller-controlled setup choices (for example a
 * Red Herring assignment or demon bluffs) belong to the complete bundle but have no immediate
 * Day-1 public-share observation under the first experiment profile. Their legal generation remains
 * owned by the existing rules/recommendation seams; this contract only composes already-legal
 * choices.
 */
internal data class FirstNightInformationBundleEntry(
    val entryId: String,
    val control: FirstNightBundleEntryControl,
    val sourceChoiceId: String? = null,
    val observation: EpistemicObservation? = null,
    val profileExposure: FirstNightBundleProfileExposure = FirstNightBundleProfileExposure.NOT_SHARED,
) {
    init {
        require(STABLE_ID.matches(entryId)) { "First-night bundle entryId must be a stable lowercase ID." }
        require(sourceChoiceId == null || sourceChoiceId.isNotBlank()) {
            "First-night bundle sourceChoiceId cannot be blank."
        }
        if (profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO) {
            require(observation != null) {
                "PUBLIC_GOOD_INFO entries require an actual hypothetical observation."
            }
            require(observation.proposition !is InformationProposition.GrimoireState) {
                "Spy Grimoire information cannot be projected into PUBLIC_GOOD_INFO."
            }
        }
    }

    companion object {
        private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*")
    }
}

/**
 * Small experiment-only representation of one complete legal first-night choice combination.
 *
 * It deliberately carries composition/provenance only. Rules legality stays with the existing
 * generators, while exact consequence evaluation stays in the epistemic package.
 */
internal class FirstNightInformationBundle(
    val bundleId: String,
    entries: List<FirstNightInformationBundleEntry>,
) {
    val entries: List<FirstNightInformationBundleEntry> =
        Collections.unmodifiableList(entries.toList())

    init {
        require(STABLE_ID.matches(bundleId)) { "First-night bundleId must be a stable lowercase ID." }
        require(this.entries.isNotEmpty()) { "A first-night bundle must contain at least one entry." }
        require(this.entries.map(FirstNightInformationBundleEntry::entryId).distinct().size == this.entries.size) {
            "First-night bundle entry IDs must be unique."
        }
    }

    override fun equals(other: Any?): Boolean =
        other is FirstNightInformationBundle && bundleId == other.bundleId && entries == other.entries

    override fun hashCode(): Int = 31 * bundleId.hashCode() + entries.hashCode()

    override fun toString(): String = "FirstNightInformationBundle(bundleId=$bundleId, entries=$entries)"

    companion object {
        private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*")
    }
}

internal enum class FirstNightExperimentProfile {
    BEGINNER_PUBLIC_GOOD_INFO,
}

/**
 * Ephemeral PUBLIC_GOOD_INFO projection. No durable observation, timeline, or bundle entry is
 * mutated. Every sharing player contributes one deterministic shown-role identity claim plus their
 * clue observations. Repeated entries from the same player/role are deduplicated. Shown-role claims
 * are not ability information, so Drunk/poison malfunction semantics apply only to the clue itself.
 * Latent/non-shared choices remain part of bundle identity but contribute no Day-1 public observation
 * until a later experiment profile gives them an epistemic consequence.
 */
internal object FirstNightPublicGoodInfoProjection {
    fun project(bundle: FirstNightInformationBundle): List<EpistemicObservation> {
        val exposedEntries = bundle.entries.filter {
            it.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO
        }
        val shownRoleClaims = exposedEntries
            .map { entry ->
                val original = requireNotNull(entry.observation)
                val seat = requireNotNull(original.sourceSeat) {
                    "PUBLIC_GOOD_INFO first-night observations require a source seat for shown-role projection."
                }
                val role = requireNotNull(original.sourceAbility) {
                    "PUBLIC_GOOD_INFO first-night observations require a source ability for shown-role projection."
                }
                Triple(seat, role, original)
            }
            .distinctBy { (seat, role, _) -> seat to role }
            .sortedWith(compareBy({ it.first }, { it.second.value }))
            .map { (seat, role, original) ->
                original.copy(
                    observationId = SemanticStableId.create(
                        prefix = "fn-share-role",
                        canonicalPayload = listOf(bundle.bundleId, seat.toString(), role.value).joinToString("|"),
                    ),
                    sourceAbility = null,
                    visibility = ObservationVisibility.PUBLIC,
                    recipientSeats = emptySet(),
                    reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                    proposition = InformationProposition.ShownRoleAt(seat, role),
                )
            }
        val clueObservations = exposedEntries.map { entry ->
            val original = requireNotNull(entry.observation)
            original.copy(
                observationId = SemanticStableId.create(
                    prefix = "fn-share",
                    canonicalPayload = listOf(bundle.bundleId, entry.entryId, original.observationId)
                        .joinToString("|"),
                ),
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
            )
        }
        return shownRoleClaims + clueObservations
    }
}

internal data class FirstNightBundleRecipientExactDiagnostics(
    val recipientSeat: Int,
    val before: WorldCardinality.Exact,
    val after: WorldCardinality.Exact,
)

internal sealed interface FirstNightBundleExperimentEvaluation {
    val bundleId: String
    val profile: FirstNightExperimentProfile

    data class Ready(
        override val bundleId: String,
        override val profile: FirstNightExperimentProfile,
        val publicObservationCount: Int,
        val recipientDiagnostics: List<FirstNightBundleRecipientExactDiagnostics>,
    ) : FirstNightBundleExperimentEvaluation

    data class Deferred(
        override val bundleId: String,
        override val profile: FirstNightExperimentProfile,
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : FirstNightBundleExperimentEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred first-night bundle evaluation must identify missing capabilities."
            }
        }
    }
}

/**
 * Recommendation-owned adapter from the BEGINNER public-share experiment contract to the neutral
 * exact bundle evaluator. It adds no rules, ranking, Badness thresholds, or durable observations.
 */
internal object FirstNightBundleExperimentEvaluator {
    fun evaluateBeginnerPublicGoodInfo(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        bundle: FirstNightInformationBundle,
        evaluationRecipientSeats: Set<Int>,
    ): FirstNightBundleExperimentEvaluation {
        require(evaluationRecipientSeats.isNotEmpty()) {
            "BEGINNER PUBLIC_GOOD_INFO evaluation requires at least one recipient perspective."
        }
        require(evaluationRecipientSeats.all { it > 0 }) {
            "BEGINNER PUBLIC_GOOD_INFO recipient seats must be positive."
        }

        val projected = FirstNightPublicGoodInfoProjection.project(bundle)
        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            queries = evaluationRecipientSeats
                .toSortedSet()
                .map { recipientSeat ->
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = bundle.bundleId,
                        recipientSeat = recipientSeat,
                        observations = projected,
                    )
                },
        )

        return when (evaluation) {
            is ExactHypotheticalObservationBundleEvaluation.Ready ->
                FirstNightBundleExperimentEvaluation.Ready(
                    bundleId = bundle.bundleId,
                    profile = FirstNightExperimentProfile.BEGINNER_PUBLIC_GOOD_INFO,
                    publicObservationCount = projected.size,
                    recipientDiagnostics = evaluation.diagnostics.map { diagnostic ->
                        FirstNightBundleRecipientExactDiagnostics(
                            recipientSeat = diagnostic.recipientSeat,
                            before = diagnostic.before,
                            after = diagnostic.after,
                        )
                    },
                )

            is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                FirstNightBundleExperimentEvaluation.Deferred(
                    bundleId = bundle.bundleId,
                    profile = FirstNightExperimentProfile.BEGINNER_PUBLIC_GOOD_INFO,
                    missingCapabilities = evaluation.missingCapabilities,
                )
        }
    }
}
