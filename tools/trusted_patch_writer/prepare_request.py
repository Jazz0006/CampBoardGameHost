#!/usr/bin/env python3
"""Validate and materialize a single-file trusted patch request from a PR comment."""

from __future__ import annotations

import base64
import binascii
import json
import os
import re
import sys
import urllib.request
from pathlib import PurePosixPath

MARKER = "/trusted-patch-writer"
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
MAX_PATCH_BYTES = 64 * 1024
ALLOWED_KEYS = {
    "expected_head_sha",
    "target_path",
    "expected_blob_sha",
    "test_profile",
    "commit_message",
    "patch_base64",
}


def fail(message: str) -> "NoReturn":
    raise SystemExit(f"trusted-patch-writer: {message}")


def parse_comment(body: str) -> dict[str, str]:
    lines = body.replace("\r\n", "\n").splitlines()
    if not lines or lines[0].strip() != MARKER:
        fail(f"first line must be exactly {MARKER}")

    values: dict[str, str] = {}
    for raw_line in lines[1:]:
        line = raw_line.strip()
        if not line:
            continue
        if "=" not in line:
            fail(f"invalid request line: {raw_line!r}")
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip()
        if key not in ALLOWED_KEYS:
            fail(f"unsupported key: {key}")
        if key in values:
            fail(f"duplicate key: {key}")
        values[key] = value

    missing = sorted(ALLOWED_KEYS - values.keys())
    if missing:
        fail(f"missing required keys: {', '.join(missing)}")
    return values


def validate_path(raw: str) -> str:
    path = PurePosixPath(raw)
    if path.is_absolute() or not raw or raw.startswith("/"):
        fail("target_path must be repository-relative")
    if any(part in {"", ".", ".."} for part in path.parts):
        fail("target_path contains an unsafe path component")
    normalized = path.as_posix()
    protected = (
        normalized == ".gitattributes"
        or normalized.startswith(".github/")
        or normalized.startswith(".git/")
        or normalized.startswith("tools/trusted_patch_writer/")
    )
    if protected:
        fail("target_path points at protected writer/repository infrastructure")
    return normalized


def fetch_pr(url: str, token: str) -> dict:
    request = urllib.request.Request(
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
        },
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def write_output(key: str, value: str) -> None:
    output_path = os.environ.get("GITHUB_OUTPUT")
    if not output_path:
        fail("GITHUB_OUTPUT is not available")
    if "\n" in value or "\r" in value:
        fail(f"output {key} contains a newline")
    with open(output_path, "a", encoding="utf-8", newline="\n") as handle:
        handle.write(f"{key}={value}\n")


def main() -> int:
    event_path = os.environ.get("GITHUB_EVENT_PATH")
    token = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    runner_temp = os.environ.get("RUNNER_TEMP")
    if not event_path or not token or not runner_temp:
        fail("required GitHub Actions environment is missing")

    with open(event_path, encoding="utf-8") as handle:
        event = json.load(handle)

    repository = event.get("repository") or {}
    owner = (repository.get("owner") or {}).get("login")
    actor = (event.get("sender") or {}).get("login")
    issue = event.get("issue") or {}
    pr_link = issue.get("pull_request") or {}
    comment = event.get("comment") or {}
    if not owner or actor != owner:
        fail("only the repository owner may request a trusted patch")
    if not pr_link.get("url"):
        fail("request must be made on a pull request conversation")

    values = parse_comment(comment.get("body") or "")
    expected_head = values["expected_head_sha"].lower()
    expected_blob = values["expected_blob_sha"].lower()
    if not SHA_RE.fullmatch(expected_head) or not SHA_RE.fullmatch(expected_blob):
        fail("expected_head_sha and expected_blob_sha must be full 40-character lowercase SHAs")

    target_path = validate_path(values["target_path"])
    test_profile = values["test_profile"]
    if test_profile not in {"android", "none"}:
        fail("test_profile must be android or none")
    if target_path.startswith("app/src/main/") and test_profile != "android":
        fail("production Android source requires test_profile=android")

    commit_message = values["commit_message"]
    if not (1 <= len(commit_message) <= 120) or "\n" in commit_message or "\r" in commit_message:
        fail("commit_message must be a single line of 1..120 characters")

    try:
        patch_bytes = base64.b64decode(values["patch_base64"], validate=True)
    except (binascii.Error, ValueError):
        fail("patch_base64 is not valid base64")
    if not patch_bytes or len(patch_bytes) > MAX_PATCH_BYTES:
        fail(f"decoded patch must be 1..{MAX_PATCH_BYTES} bytes")
    try:
        patch_text = patch_bytes.decode("utf-8")
    except UnicodeDecodeError:
        fail("patch must be UTF-8 text")

    header = f"diff --git a/{target_path} b/{target_path}"
    if patch_text.count("diff --git ") != 1 or header not in patch_text:
        fail("patch must contain exactly one diff and it must target target_path")
    if "GIT binary patch" in patch_text or "--- /dev/null" in patch_text or "+++ /dev/null" in patch_text:
        fail("binary/create/delete patches are not supported")

    pr = fetch_pr(pr_link["url"], token)
    head = pr.get("head") or {}
    head_repo = head.get("repo") or {}
    repo_full_name = repository.get("full_name")
    if head_repo.get("full_name") != repo_full_name:
        fail("fork pull requests are not eligible for trusted writes")
    if head.get("sha") != expected_head:
        fail(f"PR head drifted: expected {expected_head}, live {head.get('sha')}")

    target_branch = head.get("ref") or ""
    default_branch = repository.get("default_branch") or "main"
    if not target_branch or target_branch == default_branch:
        fail("trusted writer will not write the default branch")

    patch_path = os.path.join(runner_temp, "trusted-patch.diff")
    with open(patch_path, "wb") as handle:
        handle.write(patch_bytes)

    outputs = {
        "expected_head_sha": expected_head,
        "expected_blob_sha": expected_blob,
        "target_path": target_path,
        "target_branch": target_branch,
        "test_profile": test_profile,
        "commit_message": commit_message,
        "patch_file": patch_path,
    }
    for key, value in outputs.items():
        write_output(key, value)
    return 0


if __name__ == "__main__":
    sys.exit(main())
