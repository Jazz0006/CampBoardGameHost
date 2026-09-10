from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
data = path.read_text(encoding="utf-8")

function_start = "    fun recommendedSageOptions(\n"
function_end = "    data class PairInformationEffect(\n"
if data.count(function_start) != 1:
    raise SystemExit(f"expected one Sage function start, found {data.count(function_start)}")
if data.count(function_end) != 1:
    raise SystemExit(f"expected one PairInformationEffect anchor, found {data.count(function_end)}")
start = data.index(function_start)
end = data.index(function_end, start)
data = data[:start] + data[end:]

sage_start = """        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Sage")),
"""
ravenkeeper_start = """        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper")),
"""
if data.count(sage_start) != 1:
    raise SystemExit(f"expected one Sage registry anchor, found {data.count(sage_start)}")
if data.count(ravenkeeper_start) != 1:
    raise SystemExit(f"expected one Ravenkeeper registry anchor, found {data.count(ravenkeeper_start)}")
start = data.index(sage_start)
end = data.index(ravenkeeper_start, start)
replacement = """        clocktowerSageStepMaterializer(
            builder = informationStepBuilder,
            cards = cards,
            triggerActor = sageNightDeath,
            demon = demonCard,
            directPair = sagePair,
            abilityState = sageDeathTriggerAbilityState,
            content = ClocktowerSageStepContent(
                explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                displayTitle = text("贤者信息", "Sage information"),
                displayPrimary = text("恶魔", "Demon"),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
                highPressureSuffix = text(" ⚠ 高压", " ⚠ high pressure"),
            ),
            recommendCategory = recommendationCoordinator::recommendCategory,
            isEvil = ::isClocktowerEvil,
            recommendationStyleLabel = ::recommendationStyleLabel,
        ),
"""
data = data[:start] + replacement + data[end:]

if "fun recommendedSageOptions(" in data:
    raise SystemExit("Sage recommendation function still owned by Host")
if data.count("clocktowerSageStepMaterializer(") != 1:
    raise SystemExit("Host must contain exactly one Sage materializer delegation")

path.write_text(data, encoding="utf-8", newline="\n")
