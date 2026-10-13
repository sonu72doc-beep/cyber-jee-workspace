# Family Secure — Family Spending Controls (UPI)

## Goal
Help consenting family members manage spending across Google Pay, PhonePe, Paytm, and other UPI apps without pretending Family Secure can secretly control third-party payment apps.

## Important platform boundary
Family Secure cannot directly impose a hard spending cap on arbitrary third-party UPI apps unless the relevant UPI app, bank, or an approved payment integration exposes a supported feature/API. Android accessibility tricks, screen scraping, or reading UPI PINs are not acceptable ways to enforce a limit. Never collect, store, or transmit UPI PINs, OTPs, card details, or banking passwords.

## Planned features
1. **Family budget dashboard** — owner and each member can set a daily/weekly/monthly budget and see the remaining planned budget.
2. **Manual spending log first** — members can record spending and choose the app used; clearly label it as self-reported, not bank-verified.
3. **Consent-based reminders** — alerts when a member reaches configurable thresholds (for example 50%, 80%, 100%) based on data they voluntarily enter or a future authorized integration.
4. **UPI app shortcuts** — open the installed payment app; Family Secure does not intercept or modify its payment flow.
5. **Bank/app-native controls guidance** — show instructions and links for official bank/UPI controls where available.
6. **UPI Circle research track** — investigate whether an approved UPI Circle integration is available to this app. NPCI describes UPI Circle as a primary user delegating payment authority to a secondary user with defined limits; onboarding, partner eligibility, regulatory requirements, and supported limits must be verified before promising integration.
7. **Emergency pause and revoke** — family members can disconnect sharing or revoke permissions at any time.

## Consent and security requirements
- Every adult member explicitly opts in on their own device.
- A parent/guardian may configure age-appropriate controls only where legally appropriate; do not secretly monitor adults.
- Pairing by QR code or invitation link must require confirmation on the other device. Invite links should be single-use, short-lived, and revocable.
- Request only the minimum permissions needed. Explain each permission in plain language.
- Never bypass OS permissions, UPI PIN/OTP protections, device locks, or third-party app safeguards.
- Encrypt data in transit and at rest; apply role-based access and audit sensitive actions.
- Clearly distinguish verified transaction data from manually entered data.
- Provide data export and account/device deletion.

## MVP scope
Start with a budget planner, per-member spending limits as reminders (not hard payment blocks), manual expense entry, alerts, consent-based family pairing, and a settings page linking to official payment-app/bank guidance. Do not claim that this MVP can stop a payment inside Google Pay, PhonePe, Paytm, or another UPI app.

## Acceptance criteria
- A member can set and edit their own budget and see remaining budget.
- A family owner can view only data the member has explicitly agreed to share.
- Alerts are calculated from the available data source and labelled as manual or verified.
- A member can revoke access and delete shared data.
- No UPI PIN, OTP, bank password, or card secret is requested or stored.
- Any UPI Circle/bank integration is feature-flagged until partner approval, compliance, and end-to-end testing are complete.

## References to verify during implementation
- NPCI UPI Circle: https://www.npci.org.in/product/upi-circle
- NPCI UPI FAQ: https://www.npci.org.in/what-we-do/upi/faqs
- Google Pay UPI limits help: https://support.google.com/pay/india/answer/7359498
