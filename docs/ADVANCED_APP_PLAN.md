# Cyber Jee Family Control — Advanced Version Plan

**Status:** Planning / MVP definition  
**Target:** 5–10 owner- or company-managed Android devices  
**Principle:** Transparent management, least privilege, explicit consent, and auditable access. No stealth collection or bypassing Android permissions.

## 1. Product scope

### MVP (build first)
- Admin sign-in and role-based access (owner/admin/device user).
- Pair a device using a short-lived QR/invite token; require confirmation on the device.
- Device inventory: friendly name, Android app version, last check-in, online/stale status, and battery level when available.
- Live dashboard updates for device-reported status and activity summaries.
- Consent-based location sharing with visible sharing state, timestamp, accuracy, and pause/stop controls.
- Camera/media requests: send a request to the device; show clear on-device approval; capture/select/upload only after approval.
- Family/company budgets and manual spending entries, notifications, and reports.
- Audit log for sign-ins, pairing, permission changes, exports, media requests, and admin actions.
- Secure cloud sync, data deletion/export, and device unlink/revoke.

### Later phases (only if platform policy allows)
- Call-log integration on eligible managed devices after documented permission and policy review.
- SMS features only when the app's role and intended use meet current Android/Google Play requirements. Prefer user-initiated exports or permitted aggregate reports if direct SMS access is not eligible.
- Enterprise controls through Android Enterprise on properly enrolled and authorized managed devices.
- Push notifications for requests, stale devices, low battery, and budget thresholds.

## 2. Explicit platform limits
- An ordinary Android app cannot silently take over another app's camera, gallery, calls, SMS, or UPI payment flow.
- Camera requests must be visibly approved on the target phone. Android privacy indicators and OS permissions must remain enabled.
- Gallery access should use Android Photo Picker or a similar user-selected media flow. Do not scrape all media by default.
- Background location needs an appropriate foreground-service/permission design, clear disclosure, and platform-policy review; show sharing status and let the device user stop it.
- Call/SMS permissions are restricted by Android and Google Play policy. Do not assume approval; review current requirements before implementing or publishing.
- UPI apps such as Google Pay, PhonePe, and Paytm generally do not expose a third-party API that lets this app block arbitrary payments. Start with a budget tracker, user-entered transactions, or approved integrations. Never collect UPI PINs or OTPs.

## 3. Recommended architecture

### Android companion app
- Kotlin + Jetpack Compose.
- Android Keystore for local secrets; runtime permissions requested only at point of use.
- WorkManager for deferrable sync; foreground service only for a clearly disclosed, user-enabled active location session where allowed.
- CameraX for an approved capture flow; Android Photo Picker for media selection.
- Firebase Cloud Messaging for notifications/requests; never put sensitive content in push payloads.

### Admin dashboard
- Responsive web app (React/TypeScript or a similarly maintained framework).
- Device list, status timeline, location-sharing state, pending approvals, budget/report views, audit log, and revoke controls.
- MFA for administrators and short-lived sessions.

### Backend and storage
- Firebase Authentication (MFA where supported), Firestore for device metadata and audit events, Cloud Functions for authorization/business logic, Cloud Storage for approved media.
- Enforce per-organization and per-device access rules; clients must not authorize themselves.
- Use short-lived, single-use pairing tokens; rotate/revoke credentials on unlink.
- Minimize collected data, define retention periods, and encrypt data in transit and at rest. Avoid storing raw SMS or call details unless approved, necessary, and explicitly enabled.

## 4. Core data model (draft)
- organizations: organization ID, owner ID, policy settings.
- users: auth UID, role, organization ID, MFA state.
- devices: device ID, organization ID, friendly name, app version, last check-in, status, consent settings.
- consents: device ID, feature, granted/revoked timestamp, disclosure version.
- location_updates: device ID, timestamp, coarse/precise mode, coordinates, accuracy; access restricted to authorized roles.
- approval_requests: request ID, device ID, action type, requester, created/expiry time, status, device-user decision.
- media_assets: device ID, storage path, selected asset metadata, uploader, approval reference, deletion deadline.
- reports: device ID, time window, aggregated metrics.
- audit_events: actor, action, target, timestamp, outcome, minimal metadata.

Do not store passwords, payment PINs, OTPs, or unnecessary message contents.

## 5. Security and privacy requirements
- Secure-by-default: no location, camera, gallery, call-log, or SMS collection until the relevant feature is enabled and approved.
- Clear on-device disclosure, feature-level consent, revocation, and a visible active-sharing indicator.
- Role-based access control and least privilege; require re-authentication for exports and sensitive actions.
- Audit all admin access to location, media, and communication metadata.
- Rate-limit pairing, requests, and login attempts; expire all approval requests.
- Signed app releases, dependency scanning, backups, incident response, and documented deletion.
- No stealth mode, hidden recording, permission bypass, or remote access to personal content without on-device approval.

## 6. Delivery milestones
1. M0 — UX prototype: device dashboard, pairing flow mock, request/approval mock, report cards.
2. M1 — Secure foundation: repository structure, Android app shell, web dashboard shell, auth, organization/device model, CI.
3. M2 — Pairing and telemetry: expiring QR pairing, device check-ins, online/stale status, battery/app version.
4. M3 — Consent workflows: visible location sharing and approved camera/photo requests, audit events, revoke controls.
5. M4 — Reports and budgets: usage summaries, manual spending tracker, threshold notifications.
6. M5 — Policy review: evaluate call/SMS eligibility, privacy notice, retention, Google Play / Android Enterprise requirements.
7. M6 — Pilot: test on 2–3 owned/managed devices, security review, then expand to 5–10.

## 7. Acceptance criteria for the first working MVP
- Pairing works only with an unexpired, single-use invite and on-device confirmation.
- Admin can see when a device last checked in; stale devices are not labelled live.
- Location is off by default and shows timestamp/accuracy when enabled.
- Camera/gallery requests expire and cannot execute without on-device approval.
- A device user can revoke sharing and unlink the device.
- All sensitive admin actions create audit events.
- Security rules prevent one organization from reading another organization's data.
- Automated tests cover pairing expiry, role authorization, revocation, and request approval.

## 8. Immediate next tasks
- Keep this repository private.
- Add project documentation and an architecture decision record.
- Scaffold the web dashboard and Android app in separate folders after choosing deployment/account setup.
- Never commit credentials; configure cloud secrets through the hosting provider's secret manager.
