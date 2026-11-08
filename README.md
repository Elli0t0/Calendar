# تقویم (Calendar)

Persian Jalali calendar app — Kotlin, Jetpack Compose, Material 3. Month view with
Shamsi + Gregorian dates, day markers, and (coming) transactions, installments, and stats.

- Package: `io.github.elli0t0.calendar`
- minSdk 26, release build with minify off
- Every push to `main` runs the unit tests, builds a signed release APK
  (`versionCode` = GitHub run number), and publishes it to GitHub Releases as
  `Calendar-<version>.apk` (tag `v<version>-<run-number>`).

## Signing — one key forever

The same release key must sign every build so updates install over the old version.

1. Run the manual **Generate Keystore** workflow (Actions → Generate Keystore →
   Run workflow). It creates a keystore with `keytool` and uploads it as the
   `calendar-signing-keystore` artifact (keystore + password file).
2. Download that artifact and save the keystore + password somewhere safe offline.
3. In the repo: Settings → Secrets and variables → Actions, add these 4 secrets:

   | Secret | Value |
   | --- | --- |
   | `KEYSTORE_BASE64` | base64 of the `.jks` file |
   | `KEYSTORE_PASSWORD` | keystore password |
   | `KEY_ALIAS` | key alias (`calendar`) |
   | `KEY_PASSWORD` | key password |

4. After the next build goes green with these secrets, delete the artifact.
   Never commit the keystore or passwords to the repo.

If the secrets are missing, the build workflow generates a one-time key and
uploads it as the same `calendar-signing-keystore` artifact instead — save it as
the 4 secrets above before the next release.

## Versioning

`versionName` lives in `gradle.properties` (`VERSION_MAJOR.MINOR.PATCH`, numeric
order: 0.0.1, 0.0.2, …). `versionCode` is the GitHub run number, set by CI.
