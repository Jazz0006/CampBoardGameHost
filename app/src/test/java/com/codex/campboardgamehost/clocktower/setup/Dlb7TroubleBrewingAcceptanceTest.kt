package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Dlb7TroubleBrewingAcceptanceTest {
    @Test
    fun `every built in Drunk template option exposes every and only dealt Townsfolk and commits one Drunk`() {
        val dataset = builtInDataset()
        val registry = canonicalRegistry()
        val drunkPresets = dataset.pools.values
            .flatten()
            .filter { preset -> "drunk" in preset.outsiders }

        assertTrue("Expected built-in Trouble Brewing data to contain Drunk presets.", drunkPresets.isNotEmpty())

        drunkPresets.forEach { preset ->
            assertTrue(
                "Drunk preset ${preset.id} must expose at least one visible Townsfolk option.",
                preset.drunkAsOptions.isNotEmpty(),
            )
            preset.drunkAsOptions.forEach { addedVisibleTownsfolk ->
                val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
                    preset = preset,
                    addedVisibleTownsfolkRoleId = addedVisibleTownsfolk,
                )
                val intermediate = intermediateSetup(preset, visibleRoster)
                val candidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)

                assertTrue("Drunk preset ${preset.id} must remain marked as Drunk-bearing.", visibleRoster.hasDrunk)
                assertFalse(
                    "Drunk preset ${preset.id} must not deal the Drunk identity visibly.",
                    "drunk" in visibleRoster.visibleRoleIds,
                )
                assertEquals(
                    "Drunk preset ${preset.id} option $addedVisibleTownsfolk must preserve player count.",
                    preset.playerCount,
                    visibleRoster.visibleRoleIds.size,
                )
                assertEquals(
                    "Drunk preset ${preset.id} option $addedVisibleTownsfolk must expose every and only dealt Townsfolk as legal candidates.",
                    visibleRoster.townsfolkRoleIds.toSet(),
                    candidates.map { candidate -> candidate.shownRoleId }.toSet(),
                )

                candidates.forEach { candidate ->
                    val projected = TroubleBrewingDrunkHypotheticalProjector.project(
                        intermediateSetup = intermediate,
                        candidate = candidate,
                        characterRegistry = registry,
                    )
                    val projectedDrunk = projected.gameState.players.single {
                        player -> player.actualRole == RoleId("Drunk")
                    }
                    assertEquals(candidate.seat, projectedDrunk.seat)
                    assertEquals(
                        requireNotNull(registry.findByExternalId(candidate.shownRoleId)).id,
                        projectedDrunk.shownRole,
                    )

                    val committed = TroubleBrewingSetupCommitter.commit(
                        intermediateSetup = intermediate,
                        confirmedDrunkCandidate = candidate,
                        characterRegistry = registry,
                    )
                    val committedDrunks = committed.gameState.players.filter {
                        player -> player.actualRole == RoleId("Drunk")
                    }
                    assertEquals(
                        "Drunk preset ${preset.id} candidate ${candidate.shownRoleId} must commit exactly one Drunk.",
                        1,
                        committedDrunks.size,
                    )
                    assertEquals(candidate.seat, committedDrunks.single().seat)
                    assertEquals(
                        requireNotNull(registry.findByExternalId(candidate.shownRoleId)).id,
                        committedDrunks.single().shownRole,
                    )
                }
            }
        }
    }

    @Test
    fun `every built in non Drunk template remains candidate free and commits shown identities unchanged`() {
        val dataset = builtInDataset()
        val registry = canonicalRegistry()
        val nonDrunkPresets = dataset.pools.values
            .flatten()
            .filterNot { preset -> "drunk" in preset.outsiders }

        assertTrue(
            "Expected built-in Trouble Brewing data to contain non-Drunk presets.",
            nonDrunkPresets.isNotEmpty(),
        )

        nonDrunkPresets.forEach { preset ->
            val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
                preset = preset,
                addedVisibleTownsfolkRoleId = null,
            )
            val intermediate = intermediateSetup(preset, visibleRoster)

            assertFalse("Non-Drunk preset ${preset.id} must remain non-Drunk.", visibleRoster.hasDrunk)
            assertEquals(
                "Non-Drunk preset ${preset.id} must preserve its exact visible role set.",
                (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).toSet(),
                visibleRoster.visibleRoleIds.toSet(),
            )
            assertTrue(
                "Non-Drunk preset ${preset.id} must expose no Drunk candidates.",
                TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate).isEmpty(),
            )

            val committed = TroubleBrewingSetupCommitter.commit(
                intermediateSetup = intermediate,
                confirmedDrunkCandidate = null,
                characterRegistry = registry,
            )
            assertTrue(
                "Non-Drunk preset ${preset.id} must commit no Drunk.",
                committed.gameState.players.none { player -> player.actualRole == RoleId("Drunk") },
            )
            committed.gameState.players.forEach { player ->
                assertEquals(
                    "Non-Drunk preset ${preset.id} must keep actual and shown identity equal.",
                    player.actualRole,
                    player.shownRole,
                )
            }
        }
    }

    private fun intermediateSetup(
        preset: TroubleBrewingSetupPreset,
        visibleRoster: TroubleBrewingVisibleRoster,
    ): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = DATASET_ID,
            schemaVersion = DATASET_SCHEMA_VERSION,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = ACCEPTANCE_SEED,
            visibleRoster = visibleRoster,
            shownSeatAssignments = visibleRoster.visibleRoleIds.mapIndexed { index, roleId ->
                TroubleBrewingShownSeatAssignment(
                    seat = index + 1,
                    playerName = "Player ${index + 1}",
                    shownRoleId = roleId,
                )
            },
        )

    private fun builtInDataset(): TroubleBrewingSetupPresetDataset {
        val dataset = TroubleBrewingSetupPresetJson.parse(
            File(
                "src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json",
            ).readText(Charsets.UTF_8),
        )
        TroubleBrewingSetupPresetValidator.validate(dataset, canonicalRegistry())
        return dataset
    }

    private fun canonicalRegistry(): ClocktowerCharacterRegistry =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry

    private companion object {
        const val DATASET_ID = "trouble_brewing_setup_presets_v2_final"
        const val DATASET_SCHEMA_VERSION = 2
        const val ACCEPTANCE_SEED = 20_261_003L
    }
}
