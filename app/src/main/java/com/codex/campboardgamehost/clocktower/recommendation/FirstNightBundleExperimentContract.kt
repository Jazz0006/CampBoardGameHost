package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.SemanticStableId
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

/**
 * Ephemeral PUBLIC_GOOD_INFO projection for the current healthy-stage behavioral experiment.
 *
 * A Day-1 statement is not treated as an oracle fact. Under this profile a healthy good speaker is
 * assumed to share the information they received, while an evil speaker may make the same public
 * statement regardless of their actual role or mechanical information. Therefore each public
 * statement means:
 *
 *   speaker is evil
 *      OR
 *   (
 *       speaker has the claimed shown role
 *       AND (
 *           the source ability is malfunctioning
 *           OR (the source ability is functioning AND the claimed clue is mechanically true)
 *       )
 *   )
 *
 * Drunk/Poisoner state remains hidden world state. The projection exposes no actual identity or
 * poison target; exact-world legality decides whether a malfunction explanation is available.
 *
 * No durable observation, timeline, or bundle entry is mutated. Latent/non-shared choices remain
 * part of bundle identity but contribute no Day-1 public claim until a later experiment profile gives
 * them an epistemic consequence.
 */
internal object FirstNightPublicGoodInfoProjection {
    fun project(bundle: FirstNightInformationBundle): List<EpistemicObservation> = bundle.entries
        .filter { it.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO }
        .map { entry ->
            val original = requireNotNull(entry.observation)
            val seat = requireNotNull(original.sourceSeat) {
                "PUBLIC_GOOD_INFO first-night observations require a source seat for public-claim projection."
            }
            val role = requireNotNull(original.sourceAbility) {
                "PUBLIC_GOOD_INFO first-night observations require a source ability for public-claim projection."
            }
            val claimedGoodBranch = InformationProposition.AllOf(
                listOf(
                    InformationProposition.ShownRoleAt(seat, role),
                    InformationProposition.AnyOf(
                        listOf(
                            InformationProposition.AbilityStateAt(
                                seat = seat,
                                abilityRole = role,
                                abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                            ),
                            InformationProposition.AbilityStateAt(
                                seat = seat,
                                abilityRole = role,
                                abilityState = AbilityState.MALFUNCTIONING_POISONED,
                            ),
                            InformationProposition.AllOf(
                                listOf(
                                    InformationProposition.AbilityStateAt(
                                        seat = seat,
                                        abilityRole = role,
                                        abilityState = AbilityState.FUNCTIONING,
                                    ),
                                    original.proposition,
                                ),
                            ),
                        ),
                    ),
                ),
            )
            original.copy(
                observationId = SemanticStableId.create(
                    prefix = "fn-share-claim",
                    canonicalPayload = listOf(bundle.bundleId, entry.entryId, original.observationId)
                        .joinToString("|"),
                ),
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AnyOf(
                    listOf(
                        InformationProposition.AlignmentAt(seat, Alignment.EVIL),
                        claimedGoodBranch,
                    ),
                ),
            )
        }
}


