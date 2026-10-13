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

## Deployment status

Continuous integration is configured as a starter. Production deployment is **not configured yet** because the application framework and hosting target have not been selected. Once the app and target (for example, GitHub Pages for a static site or a chosen app host) are known, add a deployment workflow with narrowly scoped credentials if needed.
