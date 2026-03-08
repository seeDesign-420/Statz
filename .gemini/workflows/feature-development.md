---
description: How to develop a feature end-to-end (plan, implement, test, review)
---

# Feature Development Workflow

## 1. Plan
- Use `@concise-planning` to define scope, acceptance criteria, and edge cases
- For architectural changes, use `@senior-architect` and create an ADR with `@architecture-decision-records`
- Break the feature into small, independently testable tasks

## 2. Design
- For UI features: check if designs are available in Google Stitch. If so, use Stitch MCP tools (`get_screen_image`, `get_screen_code`) to import them.
- Use `@frontend-design` and `@ui-ux-pro-max` for design tokens and patterns
- Use `@form-cro` for converting forms and funnels
- For API features: use `@api-patterns` to choose REST/GraphQL/tRPC
- For data models: use `@database-design` for schema decisions

## 3. Implement
- Create a feature branch: `git checkout -b feat/feature-name`
- Write code following platform-specific skills:
  - Web: `@react-patterns`, `@react-best-practices`, `@nextjs-best-practices`, `@nextjs-app-router-patterns`, `@tailwind-patterns`, `@frontend-developer`, `@frontend-security-coder`, `@top-web-vulnerabilities`
  - Mobile: `@react-native-architecture`, `@flutter-expert`, `@ios-developer`, `@mobile-developer`, `@mobile-security-coder`
  - Backend: `@backend-dev-guidelines`, `@nodejs-best-practices`, `@backend-security-coder`, `@api-security-best-practices`
- For integrating payments or checkout experiences, use `@stripe-integration`
- Follow `@typescript-expert` or `@javascript-pro` for language patterns

## 4. Test
- Write unit tests alongside code using `@test-driven-development`
- Add integration tests for API endpoints
- Use `@e2e-testing-patterns` and `@browser-automation` for critical user flows
- Run `@lint-and-validate` to catch issues

## 5. Review
- Self-review using `@code-review-checklist`
- Ensure no secrets are committed
- Verify responsive design on mobile and desktop viewports

## 6. Merge
- Use `@git-pushing` conventions for the commit message
- Squash and merge or rebase — keep history clean
- Delete the feature branch after merge
