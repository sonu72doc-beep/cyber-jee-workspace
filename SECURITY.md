# Security Policy

## Reporting a vulnerability

Please do not publish passwords, API keys, tokens, personal data, or exploit details in a public issue.

For a private repository, contact the repository owner privately with a concise description, impact, and safe reproduction steps. Rotate any credential that may have been exposed and remove it from the active configuration. Deleting a secret from a later commit alone does not invalidate it.

## Safe development defaults

- Keep real credentials in local environment variables or GitHub Actions Secrets; never commit them.
- Use least-privilege tokens and review workflow permissions before adding automation.
- Test only systems you own or are explicitly authorized to assess.
