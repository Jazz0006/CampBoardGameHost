package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import java.math.BigInteger

/** Canonical producer represented by one factor in the complete Night 1 candidate space. */
internal enum class FirstNightBundleCandidateFactorKind {
    PAIR_INFORMATION,
    FIXED_NUMERIC_INFORMATION,
    RED_HERRING,
    DEMON_BLUFFS,
}

/** Complexity deliberately observed during FN-BUNDLE-0 but staged out of the first healthy harness. */
internal enum class FirstNightBundleDeferredComplexity {
    DRUNK,
    SPY_RECLUSE_REGISTRATION,
    POISONER_TARGET,
}

/**
 * Audit-only view of one independently selectable Night 1 factor.
 *
 * [optionIds] are identities emitted by the existing legality owner (or canonical numeric truth
 * values for fixed Chef/Empath information). This type never regenerates candidate legality.
 * [profileExposure] records whether changing this factor can change the first experiment's
 * PUBLIC_GOOD_INFO projection; it is not durable observation visibility.
 */
internal data class FirstNightBundleCandidateFactorAudit(
    val factorId: String,
    val kind: FirstNightBundleCandidateFactorKind,
    val control: FirstNightBundleEntryControl,
    val profileExposure: FirstNightBundleProfileExposure,
    val sourceSeat: Int? = null,
    val optionIds: List<String>,
) {
    init {
        require(STABLE_ID.matches(factorId)) { "First-night candidate factorId must be a stable lowercase ID." }
        require(sourceSeat == null || sourceSeat > 0) { "First-night candidate factor source seat must be positive." }
        require(optionIds.distinct().size == optionIds.size) { "First-night candidate factor option IDs must be unique." }
        require(optionIds.all { it.isNotBlank() }) { "First-night candidate factor option IDs cannot be blank." }
    }

    val optionCount: Int
        get() = optionIds.size

    companion object {
        private val STABLE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*")
    }
}

/**
 * FN-BUNDLE-0 census of the candidate-product shape before epistemic materialization.
 *
 * [rawCartesianCount] is always the product of the currently represented canonical producer
 * factors. [legalCompleteBundleCount] is deliberately nullable: it is exact only when no staged
 * complexity is present. Unsupported Drunk information roles, Spy/Recluse registration sources, or
 * Poisoner mean the represented producer set is incomplete, so this audit must not pretend its partial
 * product is the count of complete legal Night 1 bundles.
 *
 * [representedPublicProjectionUpperBound] is the Cartesian product after removing factors that are
 * known to have no immediate BEGINNER_PUBLIC_GOOD_INFO exposure. It is an upper bound rather than a
 * claim about distinct epistemic observations: FN-BUNDLE-1/2 must materialize observations and
 * deduplicate by canonical projected-observation signature before exact evaluation.
 *
 * This is deliberately not an evaluator result. [evaluatedCount] remains zero and sampling remains
 * disabled until a later harness actually materializes complete bundles as hypothetical observations.
 */
internal data class FirstNightBundleCandidateSpaceAudit(
    val factors: List<FirstNightBundleCandidateFactorAudit>,
    val rawCartesianCount: BigInteger,
    val representedPublicProjectionUpperBound: BigInteger,
    val legalCompleteBundleCount: BigInteger?,
    val evaluatedCount: BigInteger = BigInteger.ZERO,
    val samplingApplied: Boolean = false,
    val excludedPlayerControlledElements: Set<String> = emptySet(),
    val deferredComplexities: Set<FirstNightBundleDeferredComplexity> = emptySet(),
) {
    init {
        require(factors.map { it.factorId }.distinct().size == factors.size) {
            "First-night candidate factor IDs must be unique."
        }
        require(rawCartesianCount.signum() >= 0) { "First-night raw Cartesian count cannot be negative." }
        require(representedPublicProjectionUpperBound.signum() >= 0) {
            "First-night public projection upper bound cannot be negative."
        }
        require(representedPublicProjectionUpperBound <= rawCartesianCount) {
            "First-night public projection upper bound cannot exceed the represented raw Cartesian count."
        }
        require(legalCompleteBundleCount == null || legalCompleteBundleCount.signum() >= 0) {
            "First-night legal complete-bundle count cannot be negative."
        }
        require(evaluatedCount.signum() >= 0) { "Evaluated bundle count cannot be negative." }
        require(legalCompleteBundleCount != null || evaluatedCount == BigInteger.ZERO) {
            "An incomplete candidate-space audit cannot report evaluated complete bundles."
        }
        require(legalCompleteBundleCount == null || evaluatedCount <= legalCompleteBundleCount) {
            "Evaluated bundle count cannot exceed the legal complete-bundle count."
        }
        require((legalCompleteBundleCount == null) == deferredComplexities.isNotEmpty()) {
            "Complete bundle count is known exactly if and only if no staged complexity is deferred."
        }
        require(excludedPlayerControlledElements.all { it.isNotBlank() }) {
            "Excluded player-controlled element IDs cannot be blank."
        }
    }
}

/**
 * Recommendation-owned live-producer audit for Trouble Brewing Night 1.
 *
 * The object only composes producer identities/counts:
 * - Washerwoman/Librarian/Investigator legality stays in [PairInformationLegalDomain];
 * - Chef/Empath truth values stay in [FirstNightNumericInformationSemantics];
 * - Fortune Teller Red Herring and demon-bluff legality stay in [SetupCandidateGenerator].
 *
 * Fortune Teller target selection is intentionally absent because it is player-controlled.
 */
internal object TroubleBrewingFirstNightBundleCandidateSpaceAuditor {
    private val washerwoman = RoleId("Washerwoman")
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")
    private val chef = RoleId("Chef")
    private val empath = RoleId("Empath")
    private val fortuneTeller = RoleId("Fortune Teller")
    private val drunk = RoleId("Drunk")
    private val spy = RoleId("Spy")
    private val recluse = RoleId("Recluse")
    private val poisoner = RoleId("Poisoner")
    private val pairRoles = setOf(washerwoman, librarian, investigator)
    private val numericRoles = setOf(chef, empath)

    fun inspect(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): FirstNightBundleCandidateSpaceAudit {
        val factors = buildList {
            addAll(pairInformationFactors(game, roleDefinitions))
            addAll(numericInformationFactors(game))
            redHerringFactor(game)?.let(::add)
            demonBluffsFactor(game, roleDefinitions)?.let(::add)
        }.sortedBy(FirstNightBundleCandidateFactorAudit::factorId)

        val rawCartesianCount = productOfOptionCounts(factors)
        val representedPublicProjectionUpperBound = productOfOptionCounts(
            factors.filter { factor ->
                factor.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO
            },
        )
        val deferredComplexities = deferredComplexities(game)

        return FirstNightBundleCandidateSpaceAudit(
            factors = factors,
            rawCartesianCount = rawCartesianCount,
            representedPublicProjectionUpperBound = representedPublicProjectionUpperBound,
            legalCompleteBundleCount = rawCartesianCount.takeIf { deferredComplexities.isEmpty() },
            excludedPlayerControlledElements = buildSet {
                if (game.players.any { it.actualRole == fortuneTeller }) add(FORTUNE_TELLER_TARGET)
            },
            deferredComplexities = deferredComplexities,
        )
    }

    private fun pairInformationFactors(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<FirstNightBundleCandidateFactorAudit> = game.players
        .asSequence()
        .mapNotNull { source ->
            if (!source.alive) return@mapNotNull null
            val abilityRole = when {
                source.actualRole == drunk && source.shownRole in pairRoles -> requireNotNull(source.shownRole)
                !source.poisoned && source.actualRole in pairRoles -> source.actualRole
                else -> return@mapNotNull null
            }
            val reliability = if (source.actualRole == drunk) {
                ReliabilityState.DRUNK
            } else {
                ReliabilityState.RELIABLE
            }
            source to (abilityRole to reliability)
        }
        .sortedBy { (source, _) -> source.seat }
        .map { (source, ability) ->
            val (abilityRole, reliability) = ability
            val roleKey = pairRoleKey(abilityRole)
            val candidates = PairInformationLegalDomain.generate(
                game = game,
                roleDefinitions = roleDefinitions,
                sourceSeat = source.seat,
                abilityRole = abilityRole,
                reliability = reliability,
            )
            FirstNightBundleCandidateFactorAudit(
                factorId = "pair.$roleKey.seat-${source.seat}",
                kind = FirstNightBundleCandidateFactorKind.PAIR_INFORMATION,
                control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                sourceSeat = source.seat,
                optionIds = candidates.map { it.candidateId }.sorted(),
            )
        }
        .toList()

    private fun numericInformationFactors(game: GameState): List<FirstNightBundleCandidateFactorAudit> = game.players
        .asSequence()
        .filter { source ->
            source.alive && !source.poisoned && source.actualRole in numericRoles
        }
        .sortedBy { it.seat }
        .map { source ->
            val roleKey = numericRoleKey(source.actualRole)
            val truthValues = FirstNightNumericInformationSemantics
                .healthyTruthValues(game, source.seat)
                .toSortedSet()
            FirstNightBundleCandidateFactorAudit(
                factorId = "numeric.$roleKey.seat-${source.seat}",
                kind = FirstNightBundleCandidateFactorKind.FIXED_NUMERIC_INFORMATION,
                control = if (truthValues.size <= 1) {
                    FirstNightBundleEntryControl.RULE_DETERMINED
                } else {
                    // Multiple healthy truths are produced only by legal registration alternatives.
                    FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
                },
                profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                sourceSeat = source.seat,
                optionIds = truthValues.map { value -> "value-$value" },
            )
        }
        .toList()

    private fun redHerringFactor(game: GameState): FirstNightBundleCandidateFactorAudit? {
        if (game.players.none { it.actualRole == fortuneTeller }) return null
        val candidates = SetupCandidateGenerator.generateRedHerringCandidates(game)
        return FirstNightBundleCandidateFactorAudit(
            factorId = "setup.red-herring",
            kind = FirstNightBundleCandidateFactorKind.RED_HERRING,
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            profileExposure = FirstNightBundleProfileExposure.NOT_SHARED,
            optionIds = candidates.map { it.candidateId }.sorted(),
        )
    }

    private fun demonBluffsFactor(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): FirstNightBundleCandidateFactorAudit? {
        val requiresBluffs = game.players.size >= MIN_DEMON_BLUFF_PLAYER_COUNT &&
            game.players.any { it.actualType == CharacterType.DEMON }
        if (!requiresBluffs) return null
        val candidates = SetupCandidateGenerator.generateDemonBluffCandidates(game, roleDefinitions)
        return FirstNightBundleCandidateFactorAudit(
            factorId = "setup.demon-bluffs",
            kind = FirstNightBundleCandidateFactorKind.DEMON_BLUFFS,
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            profileExposure = FirstNightBundleProfileExposure.NOT_SHARED,
            optionIds = candidates.map { it.candidateId }.sorted(),
        )
    }

    private fun deferredComplexities(game: GameState): Set<FirstNightBundleDeferredComplexity> = buildSet {
        val unsupportedDrunkInformation = game.players.any { source ->
            source.actualRole == drunk &&
                (source.shownRole in numericRoles || source.shownRole == fortuneTeller)
        }
        if (unsupportedDrunkInformation) add(FirstNightBundleDeferredComplexity.DRUNK)
        if (game.players.any { it.actualRole == spy || it.actualRole == recluse }) {
            add(FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION)
        }
        if (game.players.any { it.actualRole == poisoner }) {
            add(FirstNightBundleDeferredComplexity.POISONER_TARGET)
        }
    }

    private fun productOfOptionCounts(factors: List<FirstNightBundleCandidateFactorAudit>): BigInteger =
        factors.fold(BigInteger.ONE) { product, factor ->
            product.multiply(BigInteger.valueOf(factor.optionCount.toLong()))
        }

    private fun pairRoleKey(role: RoleId): String = when (role) {
        washerwoman -> "washerwoman"
        librarian -> "librarian"
        investigator -> "investigator"
        else -> error("Unsupported pair-information role ${role.value}.")
    }

    private fun numericRoleKey(role: RoleId): String = when (role) {
        chef -> "chef"
        empath -> "empath"
        else -> error("Unsupported fixed numeric role ${role.value}.")
    }

    private const val MIN_DEMON_BLUFF_PLAYER_COUNT = 7
    private const val FORTUNE_TELLER_TARGET = "fortune-teller-target"
}
