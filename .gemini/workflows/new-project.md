---
description: How to scaffold a new project (web, mobile, or desktop)
---

# New Project Workflow

// turbo-all

## 0. Wire Up Global Agent Config
```bash
ln -sfn ~/.agent .agent
```
This symlinks the global skills, rules, and workflows into the new workspace.

## 1. Define Project Scope
- Determine platform: **web** (Next.js/Vite), **mobile** (React Native/Flutter), or **desktop** (Electron/Tauri)
- Use `@concise-planning` to outline features, tech stack, and milestones
- Use `@senior-architect` and `@senior-fullstack` for architecture decisions on complex projects

## 2. Initialize Project

### Web (Next.js)
Use `@nextjs-app-router-patterns` as a guide.
```bash
npx -y create-next-app@latest ./ --typescript --tailwind --eslint --app --src-dir --import-alias "@/*"
```

### Web (Vite + React)
```bash
npx -y create-vite@latest ./ -- --template react-ts
npm install
```

### Mobile (React Native / Expo)
```bash
npx -y create-expo-app@latest ./ --template blank-typescript
```

### Mobile (Flutter)
```bash
flutter create --org com.example --platforms android,ios .
```

### Desktop (Electron)
```bash
npx -y create-electron-app@latest ./ --template=webpack-typescript
```

### Desktop (Tauri + React)
```bash
npx -y create-tauri-app@latest ./ --template react-ts
```

## 3. Setup Project Structure
- Follow `@architecture-patterns` for clean folder structure
- Apply `@backend-dev-guidelines` and `@microservices-patterns` for API projects
- Apply `@react-patterns` for React-based projects
- Apply `@react-native-architecture` for React Native projects
- Apply `@flutter-expert` for Flutter projects

## 4. Configure Tooling
```bash
# Linting & formatting
npm install -D eslint prettier eslint-config-prettier
# Testing
npm install -D vitest @testing-library/react
# Git hooks
npm install -D husky lint-staged
npx husky init
```

## 5. Setup Version Control
```bash
git init
git add .
git commit -m "chore: initial project setup"
```

## 6. Document
- Create/update README.md with setup instructions, architecture overview, and contributing guide
- Use `@architecture-decision-records` for the first ADR (tech stack choice)
