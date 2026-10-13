# Cyber Jee Workspace

Starter workspace for Cyber Jee application testing and authorized cybersecurity projects.

## Automated checks

GitHub Actions runs the CI workflow when code is pushed to `main`, when a pull request targets `main`, or when manually started from the Actions tab. It currently runs:

- Python unit tests in `tests/`
- Python syntax compilation checks
- A dependency vulnerability audit when `requirements.txt` exists

Dependabot checks weekly for GitHub Actions and Python dependency updates.

## Getting started in GitHub Codespaces

1. Open this repository on GitHub.
2. Select **Code → Codespaces → Create codespace on main**.
3. In the terminal, run `python -m unittest discover -s tests -p "test_*.py" -v`.
4. Add your application code and tests as the project takes shape.

## Add dependencies

List Python packages in `requirements.txt`. Prefer reviewed, maintained packages and pin versions for reproducible builds. The CI workflow will install the file and run a dependency audit when it exists.

## Secrets and security

- Never commit `.env` files, passwords, API keys, tokens, or private certificates.
- Use GitHub Actions Secrets only when a workflow genuinely needs a credential.
- Keep this repository private unless you intentionally decide to publish it.
- Only test systems you own or have permission to assess.

## FamGuard Android build and testing

FamGuard is currently an **installable Android prototype**, not a production-ready family-control service.

- Latest app version in source: **0.4.0**.
- Every push to `main` starts a GitHub Actions APK build.
- Open [APK build history](https://github.com/sonu72doc-beep/cyber-jee-workspace/actions/workflows/android-apk.yml), open the newest successful run, and download the `famguard-android-apk` artifact. Extract the ZIP and install the APK on an Android phone.
- The debug APK is for testing; it is not a Play Store release or a signed production build.

### What currently works

- Save a profile locally on one phone.
- Generate and scan a demo QR invitation.
- Store up to 10 scanned demo-device records locally and clear that list with confirmation.
- Reject QR tokens that are not UUID-formatted.

### What is not live yet

QR pairing is **local-only demo behavior**: scanning on one phone does not link or notify the other phone. Firebase libraries are present, and a starter `firestore.rules` file has now been committed, but Firebase project configuration, app-side sign-in, cloud invitation handling, and synchronization are not implemented yet. The app does not remotely control devices or UPI apps, and it does not collect SMS, call history, camera/gallery content, or live location. Those capabilities must not be represented as active until implemented with explicit consent, appropriate Android permissions/policies, and real-device testing.

Do not store UPI PINs, bank passwords, or payment credentials in FamGuard. Spending-limit features remain postponed until the final phase.


## FamGuard cloud pairing (free-tier setup)

The Android app now includes Firebase Authentication, Cloud Firestore, and ZXing QR libraries as the foundation for secure family-device pairing. These libraries are dependencies only; cloud sign-in, database rules, QR scanning, and device linking are not active until the Firebase project is configured and the app code is completed.

### Required one-time account setup

1. Open the [Firebase Console](https://console.firebase.google.com/) and create a project on the no-cost Spark plan.
2. Add an Android app with package name `com.cyberjee.familycontrol`.
3. Download `google-services.json` and place it in `app/google-services.json` locally. Do not commit credentials or private keys.
4. Enable Authentication and Cloud Firestore in the Firebase Console.
5. Review and deploy the repository's `firestore.rules` using Firebase CLI (`firebase deploy --only firestore:rules`) from a trusted computer after installing the Firebase CLI and signing in. Do not use permissive test-mode rules.
6. The Android app still needs the cloud pairing code and project configuration before those rules can be used; do not treat QR pairing as live until a new APK has been tested on two phones.
7. Keep the project on Spark if you want to avoid linking a billing account. Free quotas apply; when a quota is exceeded, some services can stop until the quota resets.

Official plan details: https://firebase.google.com/docs/projects/billing/firebase-pricing-plans

Do not store payment credentials or UPI PINs in FamGuard. Spending-limit features remain postponed until the final phase.
