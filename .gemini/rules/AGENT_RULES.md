---
trigger: always_on
description: Global rules for quality, consistency, and cross-platform development
---

# Agent Rules

## Planning
- Always start non-trivial tasks with a plan. Use `@concise-planning` for structured planning.
- Break work into small, reviewable increments.
- Use `@architecture-decision-records` for significant technical decisions.

## Code Quality
- Run `@lint-and-validate` before every commit.
- Follow language-specific best practices: `@typescript-expert`, `@javascript-pro`, `@react-patterns`.
- Keep functions small, focused, and well-named. Prefer composition over inheritance.
- No hardcoded values — use environment variables and config files.

## Testing
- Write tests alongside implementation (not after). Use `@test-driven-development` when appropriate.
- Minimum coverage: unit tests for logic, integration tests for APIs, E2E for critical flows.
- Use `@test-fixing` to systematically resolve failing tests.

## Git & Version Control
- Use `@git-pushing` conventions: conventional commits (`feat:`, `fix:`, `chore:`, `docs:`).
- Keep commits atomic — one logical change per commit.
- Write descriptive PR titles and descriptions.

## Security
- Never commit secrets, tokens, or credentials. Use `.env` files (gitignored).
- Validate and sanitize all user inputs.
- Use `@api-security-best-practices` when building APIs.

## Documentation
- Keep README files current with setup instructions and architecture overview.
- Use inline comments only for non-obvious logic (code should be self-documenting).
- Document API contracts and data models.

## Cross-Platform Awareness
- Consider desktop, web, and mobile constraints in every design decision.
- Use responsive design principles for all UI work. Reference `@mobile-design` and `@frontend-design`.
- Test across platforms when building shared logic.

## Design & UI
- Follow `@ui-ux-pro-max` for premium design systems.
- Use modern, accessible design patterns. Ensure WCAG 2.1 AA compliance.
- Prioritize performance — lazy load, code split, optimize assets.

## Continuous Improvement
- Apply `@kaizen` mindset: always leave the codebase better than you found it.
- Use `@systematic-debugging` for methodical problem-solving.
- Review and refactor regularly using `@code-review-checklist`.