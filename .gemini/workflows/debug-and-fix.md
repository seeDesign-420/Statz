---
description: How to systematically debug and fix issues
---

# Debug & Fix Workflow

Use `@systematic-debugging` as the primary skill for this workflow.

## 1. Reproduce
- Get the exact steps to reproduce the bug
- Note the environment: browser, OS, device, Node version, etc.
- Capture error messages, stack traces, and logs verbatim

## 2. Isolate
- Narrow down: which file, function, or component is responsible?
- Use binary search: comment out half the code, check if bug persists
- Check recent git changes: `git log --oneline -20` and `git diff`
- Add targeted logging or breakpoints

## 3. Understand Root Cause
- Don't just fix the symptom — find the underlying cause
- Check for race conditions, stale state, or incorrect assumptions
- Review related code for similar patterns that may also be affected

## 4. Fix
- Write the minimal change that fixes the issue
- Follow the same coding standards as the rest of the codebase
- Use `@lint-and-validate` to ensure the fix is clean

## 5. Verify
- Confirm the fix resolves the original reproduction steps
- Test edge cases and related scenarios
- Run the full test suite to check for regressions

## 6. Prevent Recurrence
- Write a regression test that would have caught this bug
- Use `@test-fixing` if existing tests need updating
- Document the root cause in the commit message or PR description
- Consider if `@kaizen` improvements can prevent similar bugs
