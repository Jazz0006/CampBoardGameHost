# Android Field-Test APK Distribution

## Goal

Provide a low-friction real-device test path when Android Studio and USB debugging are unavailable:

```text
GitHub source ref
-> GitHub Actions
-> FAST Android tests
-> signed debug APK
-> fixed GitHub prerelease: field-test-latest
-> fixed APK asset name
-> phone downloads and installs over the previous field-test build
```

The field-test app is deliberately isolated from any future production install:

- production application ID: `com.codex.campboardgamehost`
- field-test debug application ID: `com.codex.campboardgamehost.fieldtest`

The first APK produced by this pipeline therefore installs as a separate app from older debug APKs that used `com.codex.campboardgamehost`. After that one-time transition, later field-test APKs update the same installed field-test app in place.

## Stable download locations

Release page:

`https://github.com/Jazz0006/CampBoardGameHost/releases/tag/field-test-latest`

Direct APK:

`https://github.com/Jazz0006/CampBoardGameHost/releases/download/field-test-latest/CampBoardGameHost-field-test.apk`

The workflow always uploads the same asset name with `gh release upload --clobber`, so a successful new publication replaces the previous APK at the same download URL.

## One-time signing setup

Android only permits an APK to update an installed package when the application ID and signing certificate remain compatible. GitHub-hosted runners are disposable, so the ordinary generated debug keystore cannot be relied on across runs.

This repository therefore expects one fixed repository secret:

`FIELD_TEST_DEBUG_KEYSTORE_BASE64`

Do not commit the keystore or its base64 form to Git.

### Setup without Android Studio

A JDK is sufficient. From a repository checkout:

```bash
bash tools/setup_field_test_keystore.sh
```

The helper creates a long-lived debug-only signing key under the user's config directory, not inside the repository.

If GitHub CLI is installed and authenticated, the helper can create/update the repository secret directly:

```bash
gh auth login
bash tools/setup_field_test_keystore.sh --upload
```

Otherwise, run the helper without `--upload`, then use GitHub:

`Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`

Set the name to:

`FIELD_TEST_DEBUG_KEYSTORE_BASE64`

and paste the complete contents of the generated `debug.keystore.base64.txt` file.

Keep the original generated keystore. Replacing the signing key later means Android will reject an attempted in-place update of an already installed field-test app; in that case the old field-test app must first be uninstalled.

## Publishing a build

### Main branch

After this workflow is on `main`, every push to `main` builds and republishes `field-test-latest` automatically.

### Any feature branch or exact commit

Open:

`GitHub -> Actions -> Field Test APK -> Run workflow`

Enter a branch, tag, or commit SHA in `source_ref`.

The workflow checks out that requested source, runs `:app:testFast`, builds `:app:assembleDebug`, verifies the application ID/version metadata and APK signature, and only then replaces the fixed Release asset.

This makes it possible to test a feature branch on a phone before the branch is merged.

## Versioning contract

The repository's normal version remains the source-controlled baseline. The field-test workflow overrides it only for the APK it builds.

Field-test `versionCode` is calculated as:

```text
100000 + GitHub workflow run number * 10 + run attempt
```

This keeps field-test versions above the current source-controlled version and monotonically increases across ordinary workflow runs and reruns. Android can therefore install each newly published field-test build as an update over the previous one while preserving app data.

The field-test version name includes the workflow run and source commit for diagnosis.

## Phone workflow

For normal real-device testing:

1. Bookmark the `field-test-latest` Release page or the direct APK URL on the phone.
2. Publish the desired branch/commit through the Field Test APK workflow.
3. Open the bookmark and download `CampBoardGameHost-field-test.apk`.
4. Open the downloaded APK and choose the Android update/install action.
5. Existing field-test app data is retained when the application ID and signing key are unchanged.

Android may require the browser or file manager to have permission to install unknown apps.

## Security boundary

The field-test key is intentionally separate from any future production/Play signing key. It is only for sideloaded development builds.

Never reuse this field-test key for a production Play/App Store release, and never commit the keystore or its base64 representation to this public repository.
