---
description: How to design and implement premium UI/UX
---

# UI Design Workflow

## 1. Requirements & Inspiration
- Define the target user, key screens, and core interactions
- Use `@concise-planning` to list UI requirements and acceptance criteria
- Gather visual inspiration (Dribbble, Awwwards, competitor analysis)

## 2. Design System
- Use `@ui-ux-pro-max` to establish design tokens: colors, typography, spacing, shadows
- Use `@frontend-design` for aesthetic guidelines
- Define a consistent component library (buttons, cards, inputs, modals)

## 3. Wireframe & Layout
- Sketch low-fidelity wireframes for key screens
- If designs exist in Google's Stitch platform, use Stitch MCP tools (`get_screen_image`, `get_screen_code`) to fetch design assets and code directly
- Apply `@mobile-design` principles for mobile-first responsive layouts
- Plan navigation flow and information hierarchy

## 4. Implement
- Build components using the design system tokens
- Apply `@react-patterns` or `@flutter-expert` depending on platform
- Use `@tailwind-patterns` for utility-first CSS (web)
- Use `@scroll-experience` for scroll-driven interactions
- Use `@3d-web-experience` for Three.js/R3F elements if needed
- Use `@canvas-design` for static visual assets

## 5. Responsive Testing
- Test on mobile (320px), tablet (768px), desktop (1280px+)
- Verify touch targets are minimum 44x44px on mobile
- Check landscape and portrait orientations
- Use `@mobile-design` checklist for mobile-specific issues

## 6. Accessibility Audit
- Semantic HTML structure with correct heading hierarchy
- ARIA labels for interactive elements without visible text
- Keyboard navigation for all interactive elements
- Color contrast ratio ≥ 4.5:1 (WCAG 2.1 AA)
- Screen reader testing for critical flows

## 7. Performance
- Lazy load images and heavy components
- Optimize fonts (subset, preload, `font-display: swap`)
- Use `@seo-audit` for web performance and discoverability
- Target Lighthouse score ≥ 90 for Performance and Accessibility
