#!/usr/bin/env bash
set -euo pipefail

repo='Jazz0006/CampBoardGameHost'
secret_name='FIELD_TEST_DEBUG_KEYSTORE_BASE64'
state_dir="${XDG_CONFIG_HOME:-$HOME/.config}/campboardgamehost-field-test"
keystore="$state_dir/debug.keystore"
encoded="$state_dir/debug.keystore.base64.txt"

mkdir -p "$state_dir"
chmod 700 "$state_dir"

if ! command -v keytool >/dev/null 2>&1; then
  echo 'keytool is required. Install a JDK (Java 17 is sufficient); Android Studio is not required.' >&2
  exit 1
fi

if [[ ! -f "$keystore" ]]; then
  keytool -genkeypair \
    -keystore "$keystore" \
    -storetype JKS \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname 'CN=CampBoardGameHost Field Test,O=CampBoardGameHost,C=AU' \
    -noprompt
  chmod 600 "$keystore"
  echo "Created field-test keystore: $keystore"
else
  echo "Reusing existing field-test keystore: $keystore"
fi

if base64 --help >/dev/null 2>&1; then
  base64 -w 0 "$keystore" > "$encoded"
else
  base64 < "$keystore" | tr -d '\n' > "$encoded"
fi
chmod 600 "$encoded"

echo "Prepared GitHub-secret value: $encoded"

if [[ "${1:-}" == '--upload' ]]; then
  if ! command -v gh >/dev/null 2>&1; then
    echo 'gh CLI is required for --upload. Install/authenticate gh or paste the base64 file into the GitHub secret manually.' >&2
    exit 1
  fi
  gh secret set "$secret_name" --repo "$repo" < "$encoded"
  echo "Uploaded repository secret: $secret_name"
else
  cat <<EOF

One-time setup is ready.

Option A (recommended):
  gh auth login
  bash tools/setup_field_test_keystore.sh --upload

Option B (GitHub web UI):
  Repository Settings -> Secrets and variables -> Actions -> New repository secret
  Name: $secret_name
  Value: paste the complete contents of:
    $encoded

Keep $keystore. Replacing this signing key later will prevent Android from installing a new field-test APK over the existing field-test app.
EOF
fi
