---
description: How to perform a structured code review
---

# Code Review Workflow

Use `@code-review-checklist` as the foundation for every review.

## 1. Understand Context
- Read the PR description and linked issues
- Understand the "why" before reviewing the "what"

## 2. Correctness
- [ ] Does the code do what it claims to do?
- [ ] Are edge cases handled (null, empty, overflow, concurrency)?
- [ ] Are error messages helpful and user-facing strings correct?

## 3. Architecture
- [ ] Does the change follow `@architecture-patterns` principles?
- [ ] Is there unnecessary coupling between modules?
- [ ] Are abstractions at the right level?

## 4. Security (`@cc-skill-security-review`)
- [ ] No secrets, tokens, or credentials in code (`@secrets-management`)
- [ ] User inputs validated and sanitized (`@xss-html-injection`)
- [ ] Authentication/authorization checks present where needed (`@broken-authentication`)
- [ ] SQL injection / XSS / CSRF protections in place
- [ ] Application code and dependencies checked with `@security-scanning-security-sast` and `@security-scanning-security-dependencies`

## 5. Performance
- [ ] No N+1 queries or unnecessary re-renders
- [ ] Large lists virtualized, images optimized
- [ ] Async operations properly handled (loading, error states)

## 6. Testing
- [ ] Unit tests cover core logic
- [ ] Integration tests cover API endpoints
- [ ] Edge cases tested
- [ ] Tests are readable and maintainable

## 7. Accessibility (UI changes)
- [ ] Semantic HTML elements used
- [ ] ARIA labels where needed
- [ ] Keyboard navigation works
- [ ] Color contrast meets WCAG 2.1 AA

## 8. Maintainability
- [ ] Code is self-documenting (clear names, small functions)
- [ ] No dead code or commented-out blocks
- [ ] Dependencies justified and up-to-date
