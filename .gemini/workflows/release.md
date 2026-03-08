---
description: How to prepare and ship a release
---

# Release Workflow

## 1. Pre-Release Checks
- Ensure all tests pass: `npm test` / `flutter test` / platform-specific command
- Run `@lint-and-validate` on the entire codebase
- Self-review with `@code-review-checklist`
- Verify no TODO/FIXME items remain for this release

## 2. Changelog
- Review commits since last release: `git log --oneline $(git describe --tags --abbrev=0)..HEAD`
- Group changes by type: Features, Bug Fixes, Breaking Changes, Docs
- Write user-facing changelog in `CHANGELOG.md`

## 3. Version Bump
- Follow [Semantic Versioning](https://semver.org/): MAJOR.MINOR.PATCH
  - **MAJOR**: breaking changes
  - **MINOR**: new features (backward-compatible)
  - **PATCH**: bug fixes
- Update version in `package.json` / `pubspec.yaml` / `Cargo.toml` / etc.

## 4. Build & Validate
```bash
# Web
npm run build

# Mobile (React Native)
npx expo export

# Mobile (Flutter)
flutter build apk --release
flutter build ios --release

# Desktop (Electron)
npm run make

# Desktop (Tauri)
npm run tauri build
```

## 5. Tag & Push
```bash
git add .
git commit -m "chore: release vX.Y.Z"
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin main --tags
```

## 6. Deploy
- **Web**: Deploy to Vercel/Netlify/AWS (`@seo-audit` post-deploy check)
- **Mobile**: Submit to App Store / Play Store (`@app-store-optimization` checklist)
- **Desktop**: Publish to GitHub Releases or platform-specific store

## 7. Post-Release
- Verify deployment is live and functional
- Monitor error tracking (Sentry, Crashlytics, etc.)
- Announce release in relevant channels
- Apply `@kaizen` to retrospect on the release process
