#!/usr/bin/env python3
"""Summarize one completed Gradle JUnit XML result directory (standard library only).

Does not run Gradle or infer wall-clock/compile time from testcase durations.
Save --json output outside build/ before another invocation overwrites the results.
"""

import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET


def summarize(directory):
    files = sorted(directory.glob("TEST-*.xml"))
    if not files:
        raise ValueError(f"No TEST-*.xml results in {directory}; run the suite first")
    suites = []
    for path in files:
        root = ET.parse(path).getroot()
        cases = [
            {
                "class": case.get("classname", root.get("name")),
                "name": case.attrib["name"],
                "seconds": float(case.get("time", "0")),
                "status": next(
                    (tag for tag in ("failure", "error", "skipped") if case.find(tag) is not None),
                    "passed",
                ),
            }
            for case in root.findall("testcase")
        ]
        suites.append({
            "class": root.attrib["name"],
            "timestamp": root.get("timestamp"),
            "seconds": float(root.get("time", "0")),
            "cases": cases,
        })
    cases = [case for suite in suites for case in suite["cases"]]
    return {
        "directory": str(directory),
        "suite_count": len(suites),
        "test_count": len(cases),
        "failures": sum(case["status"] in ("failure", "error") for case in cases),
        "skipped": sum(case["status"] == "skipped" for case in cases),
        "suite_seconds": round(sum(suite["seconds"] for suite in suites), 3),
        "testcase_seconds": round(sum(case["seconds"] for case in cases), 3),
        "suites": sorted(suites, key=lambda suite: (-suite["seconds"], suite["class"])),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--json", type=Path, help="Save timing and exact test identity inventory")
    parser.add_argument("--top", type=int, default=20)
    args = parser.parse_args()
    try:
        report = summarize(args.directory)
    except (ValueError, OSError, ET.ParseError, KeyError) as error:
        parser.exit(2, f"{error}\n")
    if args.json:
        args.json.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(f"{report['suite_count']} suites / {report['test_count']} tests / "
          f"{report['failures']} failures / {report['skipped']} skipped")
    print(f"Suite sum: {report['suite_seconds']:.3f}s; "
          f"testcase sum: {report['testcase_seconds']:.3f}s (neither is build wall time)")
    for suite in report["suites"][:args.top]:
        print(f"{suite['seconds']:9.3f}s  {len(suite['cases']):4} tests  {suite['class']}")
    return 1 if report["failures"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
