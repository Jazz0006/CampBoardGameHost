# PERSONAL-DIRECT-1 — Android owner-only OpenAI Responses experiment

Status: **isolated experimental branch** based on PROD-GLOBAL-1 PR #292.
This is not a public mobile release, a verified device-to-OpenAI smoke test or a substitute for the private gateway.

## User-facing workflow

1. Install a build of this experimental branch on **your own trusted Android device**.
2. Configure a new TB game, select **AI assisted** (or bounded AI automatic), and set **OpenAI direct** under AI connection.
3. Paste your **own newly generated, scoped OpenAI project API Key** into the masked field. It is encrypted with a distinct Android Keystore AES-GCM key and held in `noBackupFilesDir`, not compiled into the APK or Git.
4. Leave model ID at `gpt-5.6-luna` or explicitly enter another available Responses API model ID such as `gpt-5.6-sol`. This is an API model ID, not a ChatGPT UI setting.
5. Begin TB; the existing precommit whole-board Drunk assessment, post-deal strategy and compact live recommendation paths use the exact same Host serializer/validator/Host commit control as the existing gateway transport.
6. To stop using the personal credential, tap **Erase local API Key**. To resume the old server design, switch back to **Gateway**; the two credentials are stored under different Keystore aliases, AAD, and no-backup ciphertext files. Disabling direct mode does not silently send the personal Key to the gateway.

## Security boundaries

- **Known exception to OpenAI guidance:** OpenAI advises against API keys in client apps, including keys entered by users. This exception is strictly an owner-only test on a trusted physical device; do not distribute the APK with retained credentials or repurpose for public release.
- A dedicated project, restricted/expiring API Key, monthly project budget monitoring and the owner's reported $20 budget reduce but do not eliminate financial risk. Confirm whether the specific account's configured dollar budget is an enforced hard cap rather than an alert; do not treat it as guaranteed.
- No arbitrary URL for direct mode: fixed `https://api.openai.com/v1/responses`, HTTPS-only, no redirects. Never put Authorization headers in logs, share a debug bundle containing a secret, or let game archives export the key.
- Android Keystore protects at-rest storage, **not** against an unlocked or compromised device while the app makes a request. Erase the key when no longer needed.
- No model output can directly commit game state. Existing pending-decision ID, source revision, rules-legal candidates, and state-freshness validators remain the authority. Offline manual mode requires no network.
- The direct adapter is explicitly compatible with the three already implemented flows: full global Drunk selection, confirmed post-deal analysis-only, and compact next legal decision. No alternate per-role recommender, no separate prompts in application screens.

## Acceptance

Before marking ready: Android Kotlin/JVM tests (full/compact/analysis conversion and malicious/illegal candidate), old gateway tests, build and independent CI/R2; one real Android -> Responses call with valid account Key in **device input only**, lawful Host recommendation, and manual/offline regression. Neither CI nor source control may contain an API Key or paid live call.

References:
- https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety
- https://developers.openai.com/api/docs/guides/structured-outputs
