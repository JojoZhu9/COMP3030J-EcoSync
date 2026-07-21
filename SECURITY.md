# Security Policy

## Supported Context

EcoSync is a COMP3030J group course project and demonstration environment. It is not presented as a production service. Security reports are still welcome because they help the team and future contributors learn from real risks.

## Reporting a Vulnerability

Please report vulnerabilities privately to [jiuzhou.zhu@ucdconnect.ie](mailto:jiuzhou.zhu@ucdconnect.ie). Include a concise description, affected component, reproduction steps, impact, and any safe proof of concept. Do not open a public issue for an unpatched vulnerability.

Do not include passwords, JWTs, cookies, API responses containing personal data, object-storage credentials, host details, deployment configuration, or runtime database dumps in a report.

## Areas of Interest

- **Test accounts:** Seeded README accounts are demo-only. Do not reuse them outside local testing, elevate their roles, or treat them as deployment credentials.
- **JWT and authentication:** Report token validation, authorization, role-boundary, password-handling, session, or account-status issues.
- **Object storage:** Report unintended upload/download access, unsafe object naming, missing authorization, or exposed MinIO configuration without retrieving private data.
- **Deployment secrets:** Treat GitHub Actions secrets, registry credentials, SSH material, database credentials, and environment files as confidential. Never commit or paste them into issues, pull requests, logs, or documentation.

## Response

The team will acknowledge a private report, assess reproducibility and impact, and coordinate a fix appropriate to the course-project context. Please allow time for review before discussing the issue publicly.
