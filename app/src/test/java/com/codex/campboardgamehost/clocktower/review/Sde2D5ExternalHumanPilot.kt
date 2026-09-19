package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingSetupProfiles
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingTopologyHypotheticalBundleEvaluator
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundle
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightInformationBundleEntry
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightNumericLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightPublicGoodInfoProjection
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.rankingPriority
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import java.io.File

internal data class Sde2D5ExternalHumanPilotResult(
    val actualBluffLegal: Boolean,
    val actualRedHerringLegal: Boolean,
    val actualDrunkCandidateLegal: Boolean,
    val actualDrunkSemanticTruth: SemanticTruth?,
    val actualFullBundleFeasibleForEveryRecipient: Boolean,
    val report: String,
)

internal object Sde2D5ExternalHumanPilotBuilder {
    private const val playerCount = 14
    private const val actualDemonSeat = 2
    private const val actualRedHerringSeat = 10
    private const val drunkSeat = 8
    private val actualBluffs = setOf(RoleId("Chef"), RoleId("Investigator"), RoleId("Saint"))

    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2d5-external-human-pilot",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val game = reconstructedGame()
    private val snapshot = GameSnapshot(
        gameId = "clocktracker-ffb40a93-3d7b-42c4-bba8-bc9c363dcd30",
        gameStateRevision = 0,
        playerInputRevision = 0,
        gameSeed = game.seed,
        rulesetRef = rulesetRef,
        gameState = game,
    )
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
    private val knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot> =
        A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = game.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observations = emptyList<EpistemicObservation>(),
            setupKnowledge = listOf(TroubleBrewingSetupProfiles.standard(playerCount)),
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)

    fun build(): Sde2D5ExternalHumanPilotResult {
        val legalBluffCandidates = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
        val legalBluffs = legalBluffCandidates.map { candidate ->
            val outcome = candidate.outcome as SetupClueOutcome.DemonBluffs
            LegalBluff(candidate.candidateId, outcome.roles)
        }
        val actualBluff = legalBluffs.singleOrNull { it.roles.toSet() == actualBluffs }

        val legalRedHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.RedHerring).seat }
            .toSet()
        val actualRedHerringLegal = actualRedHerringSeat in legalRedHerrings

        val drunkDomain = FirstNightNumericLegalDomain.generate(
            game = game,
            sourceSeat = drunkSeat,
            abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.DRUNK,
        )
        val actualDrunk = drunkDomain.singleOrNull { it.value == 0 }

        val actualPublic = actualPublicObservations(drunkValue = 0)
        val fullBundleDiagnostics = (1..playerCount).map { recipientSeat ->
            evaluate(
                recipientSeat = recipientSeat,
                queries = listOf(
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = "clocktracker-full-r$recipientSeat",
                        recipientSeat = recipientSeat,
                        observations = actualPublic,
                    ),
                ),
            ).single()
        }
        val fullBundleFeasible = fullBundleDiagnostics.all(ExactStrategicTopologyBundleDiagnostics::afterFeasible)

        val report = buildString {
            appendLine("# SDE-2D5 external human evidence pilot")
            appendLine()
            appendLine("Source: ClockTracker game ffb40a93-3d7b-42c4-bba8-bc9c363dcd30")
            appendLine("Script: Trouble Brewing")
            appendLine("Players: 14")
            appendLine("Evidence status: calibration-only external human observation; not a D5F label or holdout item.")
            appendLine()
            appendLine("## Expressiveness")
            appendLine()
            appendLine("- legal Demon bluff triplets: ${legalBluffs.size}")
            appendLine("- observed Demon bluffs Chef / Investigator / Saint legal: ${actualBluff != null}")
            appendLine("- observed Red Herring seat 10 legal: $actualRedHerringLegal")
            appendLine("- Drunk shown Empath legal numeric domain: ${drunkDomain.map { it.value }}")
            appendLine("- observed Drunk value 0 present: ${actualDrunk != null}")
            appendLine("- observed Drunk value 0 semantic truth: ${actualDrunk?.semanticTruth}")
            appendLine("- observed complete public Night-1 bundle feasible for every recipient: $fullBundleFeasible")
            appendLine()
            appendLine("## Reconstructed setup")
            appendLine()
            appendLine("| Seat | Actual role | Shown role |")
            appendLine("|---:|---|---|")
            game.players.sortedBy(PlayerState::seat).forEach { player ->
                appendLine("| ${player.seat} | ${player.actualRole.value} | ${(player.shownRole ?: player.actualRole).value} |")
            }
            appendLine()
            appendLine("Observed Night-1 choices used by this pilot:")
            appendLine()
            appendLine("- Demon bluffs: Chef / Investigator / Saint")
            appendLine("- Red Herring: seat 10")
            appendLine("- Washerwoman: seats 12 / 2 as Chef")
            appendLine("- poisoned Librarian: seats 1 / 9 as Saint")
            appendLine("- Drunk shown Empath: 0")
            appendLine("- Fortune Teller: seats 7 / 6 -> NO")
            appendLine()
            appendLine("## Current production legacy setup heuristic")
            appendLine()
            appendLine("| Profile | Human plan retained | Human tier | Human score | Higher quality/score plans | Same quality/score plans | Top RH | Top bluffs |")
            appendLine("|---|---:|---|---:|---:|---:|---:|---|")
            legacyComparisons().forEach { row ->
                appendLine(
                    "| ${row.profile} | ${row.humanRetained} | ${row.humanTier ?: "-"} | " +
                        "${row.humanScore?.toString() ?: "-"} | ${row.higherCount} | ${row.equalCount} | " +
                        "${row.topRedHerring?.toString() ?: "-"} | ${row.topBluffs.ifEmpty { "-" }} |",
                )
            }
            appendLine()
            appendLine("The comparison above uses the legacy quality-tier/score ordering, not its final seeded weighted selection.")
            appendLine()
            appendLine("## Actual whole-bundle topology diagnostics")
            appendLine()
            appendLine("| Recipient | Before keys | After keys | Demon cover retention | Evil topology retention | Evil cover retention | Forced-good fraction |")
            appendLine("|---:|---:|---:|---|---|---|---|")
            fullBundleDiagnostics.forEach { diagnostic ->
                val normalized = NormalizedStrategicDiagnosticsProjector.project(diagnostic, playerCount)
                appendLine(
                    "| ${diagnostic.recipientSeat} | ${diagnostic.beforeStructure.distinctStrategicWorldCount} | " +
                        "${diagnostic.afterStructure.distinctStrategicWorldCount} | " +
                        "${format(normalized.demonCoverRetention)} | ${format(normalized.evilTopologyRetention)} | " +
                        "${format(normalized.evilCoverRetention)} | ${format(normalized.forcedGoodFraction)} |",
                )
            }
            appendLine()
            appendLine("## Drunk shown-Empath choice: observed 0 versus complete legal numeric domain")
            appendLine()
            appendLine("| Recipient | Healthy-core keys | value=0 keys / marginal topology | value=1 keys / marginal topology | value=2 keys / marginal topology |")
            appendLine("|---:|---:|---|---|---|")
            (1..playerCount).forEach { recipientSeat ->
                val healthyCore = healthyCoreObservations()
                val queries = buildList {
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = "clocktracker-drunk-core-r$recipientSeat",
                            recipientSeat = recipientSeat,
                            observations = healthyCore,
                        ),
                    )
                    drunkDomain.sortedBy { it.value }.forEach { candidate ->
                        add(
                            ExactHypotheticalObservationBundleQuery(
                                bundleId = "clocktracker-drunk-${candidate.value}-r$recipientSeat",
                                recipientSeat = recipientSeat,
                                observations = healthyCore + drunkPublicObservation(candidate.value),
                            ),
                        )
                    }
                }
                val diagnostics = evaluate(recipientSeat, queries)
                val core = diagnostics.first()
                val byValue = drunkDomain.sortedBy { it.value }.mapIndexed { index, candidate ->
                    candidate.value to diagnostics[index + 1]
                }.toMap()
                fun cell(value: Int): String {
                    val candidate = byValue.getValue(value)
                    val marginal = NormalizedStrategicDiagnosticsProjector.project(
                        before = core.afterStructure,
                        after = candidate.afterStructure,
                        playerCount = playerCount,
                    )
                    return "${candidate.afterStructure.distinctStrategicWorldCount} / ${format(marginal.evilTopologyRetention)}"
                }
                appendLine(
                    "| $recipientSeat | ${core.afterStructure.distinctStrategicWorldCount} | " +
                        "${cell(0)} | ${cell(1)} | ${cell(2)} |",
                )
            }
            appendLine()
            appendLine("## Demon bluff topology support in the observed complete Night-1 ecology")
            appendLine()
            appendLine("| Recipient | Supported human bluff roles | Chef keys | Investigator keys | Saint keys | Union | Shared | Shared/union | Legal triplets lower / equal / higher | Distinct patterns |")
            appendLine("|---:|---:|---:|---:|---:|---:|---:|---|---|---:|")
            if (actualBluff == null) {
                appendLine("| - | - | - | - | - | - | - | - | observed triplet was not in legal domain | - |")
            } else {
                (1..playerCount).forEach { recipientSeat ->
                    val distinctRoles = legalBluffs.flatMap(LegalBluff::roles)
                        .distinct()
                        .sortedBy(RoleId::value)
                    val queries = distinctRoles.map { role ->
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = "clocktracker-bluff-${slug(role)}-r$recipientSeat",
                            recipientSeat = recipientSeat,
                            observations = actualPublic + strictShownRoleProbe(role),
                        )
                    }
                    val byRole = evaluate(recipientSeat, queries)
                        .associateBy { diagnostic ->
                            val roleToken = diagnostic.bundleId
                                .substringAfter("clocktracker-bluff-")
                                .substringBefore("-r$recipientSeat")
                            distinctRoles.single { slug(it) == roleToken }
                        }
                    val points = legalBluffs.map { bluff -> bluffPoint(bluff, byRole) }
                    val actualPoint = points.single { it.candidateId == actualBluff.candidateId }
                    val lower = points.count { compareRatio(it, actualPoint) < 0 }
                    val equal = points.count { compareRatio(it, actualPoint) == 0 }
                    val higher = points.count { compareRatio(it, actualPoint) > 0 }
                    val roleCounts = actualBluff.roles.associateWith { role ->
                        byRole.getValue(role).afterStructure.distinctStrategicWorldCount
                    }
                    appendLine(
                        "| $recipientSeat | ${actualPoint.supportedRoleCount} | " +
                            "${roleCounts.getValue(RoleId("Chef"))} | " +
                            "${roleCounts.getValue(RoleId("Investigator"))} | " +
                            "${roleCounts.getValue(RoleId("Saint"))} | " +
                            "${actualPoint.unionCount} | ${actualPoint.sharedCount} | ${actualPoint.ratioText()} | " +
                            "$lower / $equal / $higher | ${actualPoint.distinctPatternCount} |",
                    )
                }
            }
            appendLine()
            appendLine("Interpretation guard: a human-selected candidate is positive observational evidence only. " +
                "Unselected legal candidates are not treated as negative labels.")
        }

        return Sde2D5ExternalHumanPilotResult(
            actualBluffLegal = actualBluff != null,
            actualRedHerringLegal = actualRedHerringLegal,
            actualDrunkCandidateLegal = actualDrunk != null,
            actualDrunkSemanticTruth = actualDrunk?.semanticTruth,
            actualFullBundleFeasibleForEveryRecipient = fullBundleFeasible,
            report = report,
        )
    }

    private fun legacyComparisons(): List<LegacyComparison> {
        val profiles = listOf(
            "GENTLE" to RecommendationProfiles.gentle,
            "BALANCED" to RecommendationProfiles.balanced,
            "AGGRESSIVE" to RecommendationProfiles.aggressive,
        )
        return profiles.map { (label, profile) ->
            val ranked = SetupRecommendationService.rankedPlans(
                game = game,
                roleDefinitions = roles,
                profile = profile,
                maxResults = 1000,
            )
            val human = ranked.singleOrNull(::matchesObservedSetupPlan)
            val top = ranked.firstOrNull()
            val higher = if (human == null) ranked.size else ranked.count { qualityScoreCompare(it, human) > 0 }
            val equal = if (human == null) 0 else ranked.count { qualityScoreCompare(it, human) == 0 }
            LegacyComparison(
                profile = label,
                humanRetained = human != null,
                humanTier = human?.qualityTier?.name,
                humanScore = human?.totalScore,
                higherCount = higher,
                equalCount = equal,
                topRedHerring = top?.decisions
                    ?.filterIsInstance<StorytellerDecision.RedHerring>()
                    ?.singleOrNull()
                    ?.seat,
                topBluffs = top?.decisions
                    ?.filterIsInstance<StorytellerDecision.DemonBluffs>()
                    ?.singleOrNull()
                    ?.roles
                    ?.map(RoleId::value)
                    ?.sorted()
                    ?.joinToString(" / ")
                    .orEmpty(),
            )
        }
    }

    private fun matchesObservedSetupPlan(
        plan: com.codex.campboardgamehost.clocktower.domain.RecommendationPlan,
    ): Boolean {
        val redHerring = plan.decisions
            .filterIsInstance<StorytellerDecision.RedHerring>()
            .singleOrNull()
            ?.seat
        val bluffs = plan.decisions
            .filterIsInstance<StorytellerDecision.DemonBluffs>()
            .singleOrNull()
            ?.roles
            ?.toSet()
        return redHerring == actualRedHerringSeat && bluffs == actualBluffs
    }

    private fun qualityScoreCompare(
        left: com.codex.campboardgamehost.clocktower.domain.RecommendationPlan,
        right: com.codex.campboardgamehost.clocktower.domain.RecommendationPlan,
    ): Int {
        val tier = left.qualityTier.rankingPriority().compareTo(right.qualityTier.rankingPriority())
        return if (tier != 0) tier else left.totalScore.compareTo(right.totalScore)
    }

    private fun evaluate(
        recipientSeat: Int,
        queries: List<ExactHypotheticalObservationBundleQuery>,
    ): List<ExactStrategicTopologyBundleDiagnostics> {
        val evaluation = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
            rulesetRef = rulesetRef,
            knowledge = knowledgeBySeat.getValue(recipientSeat),
            roleDefinitions = roles,
            queries = queries,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        require(evaluation is ExactStrategicTopologyBundleEvaluation.Ready) {
            "External-human pilot requires topology-first support for all observed propositions."
        }
        return evaluation.diagnostics
    }

    private fun actualPublicObservations(drunkValue: Int): List<EpistemicObservation> = listOf(
        washerwomanPublicObservation(),
        poisonedLibrarianPublicObservation(),
        drunkPublicObservation(drunkValue),
        fortuneTellerPublicObservation(),
    )

    private fun healthyCoreObservations(): List<EpistemicObservation> = listOf(
        washerwomanPublicObservation(),
        poisonedLibrarianPublicObservation(),
        fortuneTellerPublicObservation(),
    )

    private fun washerwomanPublicObservation(): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = 13,
            sourceAbility = RoleId("Washerwoman"),
            value = InformationValue.PlayerPair(RoleId("Chef"), listOf(12, 2)),
        )
        return projectPublicClaim(
            entryId = "washerwoman-clue",
            sequence = 1,
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            information = information,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = information,
                roleDefinitions = roles,
            ),
        )
    }

    private fun poisonedLibrarianPublicObservation(): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = 11,
            sourceAbility = RoleId("Librarian"),
            value = InformationValue.PlayerPair(RoleId("Saint"), listOf(1, 9)),
        )
        return projectPublicClaim(
            entryId = "librarian-clue",
            sequence = 2,
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            information = information,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = information,
                roleDefinitions = roles,
            ),
        )
    }

    private fun drunkPublicObservation(value: Int): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = drunkSeat,
            sourceAbility = RoleId("Empath"),
            value = InformationValue.Number(value),
        )
        return projectPublicClaim(
            entryId = "drunk-empath-clue",
            sequence = 3,
            control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
            information = information,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = information,
                roleDefinitions = roles,
            ),
        )
    }

    private fun fortuneTellerPublicObservation(): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = 5,
            sourceAbility = RoleId("Fortune Teller"),
            value = InformationValue.YesNo(YesNoAnswer.NO),
        )
        return projectPublicClaim(
            entryId = "fortune-teller-clue",
            sequence = 4,
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            information = information,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materializeFortuneTeller(
                information = information,
                targetSeats = listOf(7, 6),
            ),
        )
    }

    private fun projectPublicClaim(
        entryId: String,
        sequence: Int,
        control: FirstNightBundleEntryControl,
        information: EffectDraft.PlayerInformation,
        proposition: InformationProposition,
    ): EpistemicObservation {
        val privateObservation = EpistemicObservation(
            observationId = "clocktracker-private-$entryId",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = sequence,
            sourceSeat = information.recipientSeat,
            sourceAbility = information.sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(information.recipientSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "clocktracker-$entryId",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = entryId,
                        control = control,
                        sourceChoiceId = "observed",
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    private fun strictShownRoleProbe(role: RoleId): EpistemicObservation = EpistemicObservation(
        observationId = "clocktracker-bluff-probe-${slug(role)}",
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 5,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.ShownRoleAt(actualDemonSeat, role),
    )

    private fun bluffPoint(
        bluff: LegalBluff,
        byRole: Map<RoleId, ExactStrategicTopologyBundleDiagnostics>,
    ): BluffPoint {
        val keySets = bluff.roles.map { role -> byRole.getValue(role).afterStructure.strategicWorldKeys }
        val union = keySets.flatten().toSet()
        val shared = keySets.drop(1).fold(keySets.first().toSet()) { acc, next -> acc.intersect(next) }
        return BluffPoint(
            candidateId = bluff.candidateId,
            supportedRoleCount = keySets.count { it.isNotEmpty() },
            unionCount = union.size,
            sharedCount = shared.size,
            distinctPatternCount = keySets.distinct().size,
        )
    }

    private fun compareRatio(left: BluffPoint, right: BluffPoint): Int = when {
        left.unionCount == 0 && right.unionCount == 0 -> 0
        left.unionCount == 0 -> -1
        right.unionCount == 0 -> 1
        else -> (left.sharedCount.toLong() * right.unionCount.toLong())
            .compareTo(right.sharedCount.toLong() * left.unionCount.toLong())
    }

    private fun format(ratio: StrategicRatio): String = when (ratio) {
        is StrategicRatio.Defined -> "${ratio.numerator}/${ratio.denominator}"
        StrategicRatio.Undefined -> "undefined"
    }

    private fun reconstructedGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20250910L,
        players = listOf(
            player(1, "Scarlet Woman", CharacterType.MINION),
            player(2, "Imp", CharacterType.DEMON),
            player(3, "Soldier", CharacterType.TOWNSFOLK),
            player(4, "Ravenkeeper", CharacterType.TOWNSFOLK),
            player(5, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(6, "Undertaker", CharacterType.TOWNSFOLK),
            player(7, "Slayer", CharacterType.TOWNSFOLK),
            player(8, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(9, "Poisoner", CharacterType.MINION),
            player(10, "Monk", CharacterType.TOWNSFOLK),
            player(11, "Librarian", CharacterType.TOWNSFOLK),
            player(12, "Spy", CharacterType.MINION),
            player(13, "Washerwoman", CharacterType.TOWNSFOLK),
            player(14, "Virgin", CharacterType.TOWNSFOLK),
        ),
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )

    private fun slug(role: RoleId): String = role.value.lowercase().replace(' ', '-')

    private data class LegalBluff(
        val candidateId: String,
        val roles: List<RoleId>,
    )

    private data class BluffPoint(
        val candidateId: String,
        val supportedRoleCount: Int,
        val unionCount: Int,
        val sharedCount: Int,
        val distinctPatternCount: Int,
    ) {
        fun ratioText(): String = if (unionCount == 0) "undefined" else "$sharedCount/$unionCount"
    }

    private data class LegacyComparison(
        val profile: String,
        val humanRetained: Boolean,
        val humanTier: String?,
        val humanScore: Int?,
        val higherCount: Int,
        val equalCount: Int,
        val topRedHerring: Int?,
        val topBluffs: String,
    )
}