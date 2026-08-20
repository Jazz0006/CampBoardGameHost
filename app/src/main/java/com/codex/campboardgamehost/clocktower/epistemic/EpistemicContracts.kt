package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger

enum class EpistemicHypothesis { MECHANICALLY_CREDIBLE, FUNCTIONING_ONLY, MALFUNCTION_ALLOWED }

/**
 * Stable position on the committed game timeline.
 *
 * [globalSequence] is the ordering authority across phase and round boundaries. [sequence] is
 * retained as the local interaction position for display/replay diagnostics only.
 */
data class TimelinePoint(
    val phase: StorytellerPhase,
    val round: Int,
    val sequence: Int,
    val globalSequence: Long,
) : Comparable<TimelinePoint> {
    init { require(round > 0 && sequence >= 0 && globalSequence >= 0) }

    override fun compareTo(other: TimelinePoint): Int = compareValuesBy(
        this,
        other,
        TimelinePoint::globalSequence,
        TimelinePoint::round,
        { it.phase.ordinal },
        TimelinePoint::sequence,
    )
}

data class RegistrationQuery(
    val subjectSeat: Int,
    val interactionId: String,
    val timelinePoint: TimelinePoint,
    val detectingAbility: RoleId,
    val question: RegistrationQuestion,
    val queriedRole: RoleId? = null,
    val queriedType: CharacterType? = null,
    val queriedAlignment: Alignment? = null,
) {
    init {
        require(subjectSeat > 0)
        require(STABLE_ID.matches(interactionId)) { "interactionId must be a stable lowercase ID." }
        require(listOfNotNull(queriedRole, queriedType, queriedAlignment).isNotEmpty()) {
            "A registration query needs at least one queried value."
        }
        require(queriedRole == null || (queriedType != null && queriedAlignment != null)) {
            "A queried role must include its ruleset-resolved type and alignment."
        }
    }
    companion object { private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*") }
}

enum class RegistrationBasis { ACTUAL_STATE, SPY_ABILITY, RECLUSE_ABILITY }

data class RegistrationProfile(
    val role: RoleId?,
    val characterType: CharacterType?,
    val alignment: Alignment?,
    val basis: RegistrationBasis,
) {
    init { require(role != null || characterType != null || alignment != null) }
}

interface RegistrationSemantics {
    fun possibleRegistrations(state: FormalGameState, query: RegistrationQuery): Set<RegistrationProfile>

    fun isLegalSelection(state: FormalGameState, query: RegistrationQuery, selected: RegistrationFact): Boolean {
        if (selected.interactionId != query.interactionId || selected.subjectSeat != query.subjectSeat ||
            selected.registrationQuestion != query.question
        ) return false
        return possibleRegistrations(state, query).any { profile ->
            (selected.registeredRole == null || selected.registeredRole == profile.role) &&
                (selected.registeredType == null || selected.registeredType == profile.characterType) &&
                (selected.registeredAlignment == null || selected.registeredAlignment == profile.alignment) &&
                when (profile.basis) {
                    RegistrationBasis.ACTUAL_STATE -> selected.reason.name !in SPECIAL_REGISTRATION_REASONS
                    else -> selected.reason.name == profile.basis.name
                }
        }
    }

    companion object {
        private val SPECIAL_REGISTRATION_REASONS = setOf("SPY_ABILITY", "RECLUSE_ABILITY")
    }
}

/** Trouble Brewing registration rules; each result is local to the supplied interaction. */
object TroubleBrewingRegistrationSemantics : RegistrationSemantics {
    override fun possibleRegistrations(state: FormalGameState, query: RegistrationQuery): Set<RegistrationProfile> {
        val player = state.players.singleOrNull { it.seat == query.subjectSeat }
            ?: throw IllegalArgumentException("Unknown registration subject seat ${query.subjectSeat}.")
        val results = linkedSetOf(
            RegistrationProfile(player.actualRole, player.actualType, player.actualAlignment, RegistrationBasis.ACTUAL_STATE),
        )
        when (player.actualRole.value.lowercase()) {
            "spy" -> if ((query.queriedAlignment == null || query.queriedAlignment == Alignment.GOOD) &&
                (query.queriedType == null || query.queriedType == CharacterType.TOWNSFOLK || query.queriedType == CharacterType.OUTSIDER)
            ) {
                results += RegistrationProfile(
                    query.queriedRole,
                    query.queriedType,
                    query.queriedAlignment ?: Alignment.GOOD,
                    RegistrationBasis.SPY_ABILITY,
                )
            }
            "recluse" -> if ((query.queriedAlignment == null || query.queriedAlignment == Alignment.EVIL) &&
                (query.queriedType == null || query.queriedType == CharacterType.MINION || query.queriedType == CharacterType.DEMON)
            ) {
                results += RegistrationProfile(
                    query.queriedRole,
                    query.queriedType,
                    query.queriedAlignment ?: Alignment.EVIL,
                    RegistrationBasis.RECLUSE_ABILITY,
                )
            }
        }
        return results
    }
}

sealed interface WorldCardinality {
    val valueOrLowerBound: BigInteger

    data class Exact(val value: BigInteger) : WorldCardinality {
        init { require(value.signum() >= 0) }
        override val valueOrLowerBound: BigInteger get() = value
    }

    data class AtLeast(val lowerBound: BigInteger) : WorldCardinality {
        init { require(lowerBound.signum() >= 0) }
        override val valueOrLowerBound: BigInteger get() = lowerBound
    }
}

data class CandidateFamilyId(val value: String) { init { require(STABLE_ID.matches(value)) }; companion object { private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*") } }
data class WorldExplanationClusterId(val value: String) { init { require(STABLE_ID.matches(value)) }; companion object { private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*") } }

data class PlayerWorldSetIdentity(
    val value: String,
    val recipientSeat: Int,
    val hypothesis: EpistemicHypothesis,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(recipientSeat > 0)
        require(value.matches(Regex("world-set-[0-9a-f]{32}")))
    }

    companion object {
        fun create(
            rulesetRef: RulesetRef,
            knowledge: PlayerKnowledgeSnapshot,
            hypothesis: EpistemicHypothesis,
        ): PlayerWorldSetIdentity {
            val payload = EpistemicSemanticJson.encodeKnowledgeIdentityPayload(rulesetRef, knowledge, hypothesis)
            return PlayerWorldSetIdentity(
                value = SemanticStableId.create("world-set", payload),
                recipientSeat = knowledge.recipientSeat,
                hypothesis = hypothesis,
            )
        }
    }
}
