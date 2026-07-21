# Contributing to EcoSync

EcoSync is a group course project for COMP3030J. Contributions should preserve team attribution, stay within the course-project scope, and make one reviewable improvement at a time.

## Before You Start

- Read the relevant backend, frontend, migration, and deployment code before proposing a change.
- Keep pull requests focused. Explain the affected role: administrator, store employee, consumer, or shared platform behavior.
- Do not add credentials, runtime databases, generated build output, or production deployment details.
- Discuss changes that alter seeded data, authentication, storage, API contracts, or Docker Compose behavior before implementation.

## Development Expectations

- Match the existing Spring Boot and Vue 3 patterns.
- Add or update focused tests when behavior changes.
- Include a Flyway migration for persistent schema changes; document its compatibility and rollback considerations in the pull request.
- Keep English documentation primary and update concise Chinese documentation when user-facing workflows, accounts, commands, or deployment boundaries change.

## Validation

Run the commands relevant to your change and report the exact result in the pull request. Backend integration tests require the configured MySQL test datasource.

```powershell
.\mvnw.cmd test -B
npm --prefix ecosync-frontend run build
npm --prefix ecosync-frontend exec -- vitest run --reporter=verbose
```

When a required local dependency is unavailable, state that clearly rather than changing the test configuration or treating a skipped check as a pass.

## Pull Requests

Use the repository pull-request template. Describe the user-visible behavior, affected role, migration impact, validation performed, and screenshots for UI changes. A maintainer will review changes that touch CI/CD, Docker Compose, authentication, object storage, or deployment workflows.
