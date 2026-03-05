# Statz — Design System

![Statz Logo](file:///home/thomas/Statz/assets/logo.png)

> **Telecom Sales Companion** · Android (Kotlin + Jetpack Compose) · Offline-only
> Material Design 3 · Dynamic Color · Dark Mode First

---

## 1. Brand Identity

| Attribute | Value |
|-----------|-------|
| **App Name** | Statz |
| **Tagline** | Your sales, tracked. |
| **Personality** | Efficient, trustworthy, data-driven |
| **Industry** | Telecom / Field Sales |
| **Platform** | Android — Kotlin + Jetpack Compose |
| **Logo** | [logo.png](file:///home/thomas/Statz/assets/logo.png) (Minimal Growth Glow) |

---

## 2. Color System (Material 3 Dynamic Color Compatible)

### Seed Colors

| Role | Hex | Token | Usage |
|------|-----|-------|-------|
| **Primary** | `#3B82F6` | `md_theme_primary` | FAB, active nav, key actions |
| **Secondary** | `#60A5FA` | `md_theme_secondary` | Chips, toggle states, sub-headers |
| **Tertiary** | `#F97316` | `md_theme_tertiary` | CTA highlights, progress fill, badges |
| **Error** | `#EF4444` | `md_theme_error` | Validation, declined indicators |
| **Success** | `#22C55E` | _(custom)_ | Target met, closed queries |

### Surface System (Dark Theme — Default)

| Token | Hex | Usage |
|-------|-----|-------|
| `background` | `#0F1117` | App canvas |
| `surface` | `#1A1D27` | Cards, sheets |
| `surfaceVariant` | `#232736` | Input fields, chips |
| `surfaceContainer` | `#1E2130` | Bottom nav, top bar |
| `onSurface` | `#E8EAED` | Primary text |
| `onSurfaceVariant` | `#9AA0B0` | Secondary text, labels |
| `outline` | `#3D4358` | Dividers, borders |
| `outlineVariant` | `#2A2E3D` | Subtle dividers |

### Light Theme Override

| Token | Hex | Usage |
|-------|-----|-------|
| `background` | `#F8FAFC` | App canvas |
| `surface` | `#FFFFFF` | Cards |
| `surfaceVariant` | `#F1F3F9` | Input fields |
| `onSurface` | `#1E293B` | Primary text |
| `onSurfaceVariant` | `#64748B` | Secondary text |
| `outline` | `#CBD5E1` | Dividers |

### Status / Semantic Colors

| Status | Color | Hex | Usage |
|--------|-------|-----|-------|
| Open | Blue | `#3B82F6` | Default query/task state |
| Follow-up | Amber | `#F59E0B` | Pending action |
| Escalated | Red | `#EF4444` | Urgent attention |
| Closed | Grey | `#6B7280` | Resolved items |
| Low priority | Slate | `#94A3B8` | Low urgency badge |
| Medium priority | Amber | `#F59E0B` | Medium urgency badge |
| High priority | Orange | `#F97316` | High urgency badge |
| Critical priority | Red | `#DC2626` | Critical urgency badge |

---

## 3. Typography (Material 3 Type Scale)

| Role | Font | Size | Weight | Line Height |
|------|------|------|--------|-------------|
| Display Large | Fira Sans | 57sp | Regular | 64sp |
| Headline Large | Fira Sans | 32sp | Regular | 40sp |
| Headline Medium | Fira Sans | 28sp | Regular | 36sp |
| Title Large | Fira Sans | 22sp | Medium | 28sp |
| Title Medium | Fira Sans | 16sp | Medium | 24sp |
| Body Large | Fira Sans | 16sp | Regular | 24sp |
| Body Medium | Fira Sans | 14sp | Regular | 20sp |
| Body Small | Fira Sans | 12sp | Regular | 16sp |
| Label Large | Fira Sans | 14sp | Medium | 20sp |
| Label Small | Fira Sans | 11sp | Medium | 16sp |
| Monospace (values) | Fira Code | 16sp | Medium | 24sp |

> **Why Fira Sans + Fira Code?** — Clean, readable at small sizes, excellent for numeric-heavy data entry UIs. Fira Code gives monospaced currency/count values perfect alignment.

---

## 4. Spacing & Layout (8dp Grid)

| Token | Value | Usage |
|-------|-------|-------|
| `space-xs` | 4dp | Icon padding, inline gaps |
| `space-sm` | 8dp | Chip gaps, compact elements |
| `space-md` | 16dp | Card padding, list margins |
| `space-lg` | 24dp | Section spacing |
| `space-xl` | 32dp | Screen margins, major separation |

### Screen Structure

```
┌─────────────────────────────────────┐
│  Top App Bar (Small)            64dp │
├─────────────────────────────────────┤
│                                     │
│  Content Area                       │
│  (scrollable, 16dp horizontal pad)  │
│                                     │
│                              ┌────┐ │
│                              │ ➕ │ │  FAB (56dp)
│                              └────┘ │
├─────────────────────────────────────┤
│  Bottom Navigation              80dp │
│  [Sales] [Queries] [To-Do] [Settings] │
└─────────────────────────────────────┘
```

---

## 5. Component Patterns

### Bottom Navigation

- 4 destinations: Sales Stats, Queries, To-Do, Settings
- Material Symbols Rounded icons (24dp), filled when active
- Active: Indicator pill + primary color
- Labels always visible
- 80dp height

### Cards

- Elevated cards for dashboard KPI summary
- Filled cards for list items (categories, queries, tasks)
- Corner radius: 16dp (slightly warmer than M3 default 12dp)
- Internal padding: 16dp
- Elevation level 1 on dark surfaces

### KPI / Progress Row

```
┌────────────────────────────────────────┐
│  Category Name                  12/15  │
│  ████████████░░░░░░░░░  80%    [+1]   │
└────────────────────────────────────────┘
```

- Progress bar: 8dp height, rounded ends
- Fill: Tertiary orange (`#F97316`)
- Track: `surfaceVariant`
- Quick-add FAB to the right

### Status Chips

- Filled tonal chips for status
- Rounded shape (full radius)
- Color matches status palette
- 32dp height

### Text Fields

- Outlined variant (better contrast in dark mode)
- 56dp height
- Floating label animation
- Supporting text for errors
- Currency fields: `R` prefix, Fira Code input

### Dialogs

- Standard Material dialogs for confirmations
- Bottom sheet for multi-option menus (snooze, quick-add)
- 28dp top corner radius on sheets

---

## 6. Icons

- **Library:** Material Symbols Rounded
- **Size:** 24dp standard, 20dp dense
- **State:** Outlined (inactive), Filled (active)
- **No emojis** — SVG/vector only

### Key Icons by Tab

| Tab | Icons Needed |
|-----|-------------|
| Sales | `bar_chart`, `add_circle`, `edit`, `calendar_month` |
| Queries | `support_agent`, `priority_high`, `snooze`, `check_circle` |
| To-Do | `task_alt`, `schedule`, `flag`, `delete` |
| Settings | `settings`, `backup`, `restore`, `schedule` |

---

## 7. Motion & Animation

| Interaction | Duration | Curve |
|-------------|----------|-------|
| Page transition | 300ms | `EmphasizedDecelerate` |
| Bottom sheet expand | 250ms | `EmphasizedDecelerate` |
| Chip toggle | 150ms | `Standard` |
| Progress bar fill | 400ms | `EmphasizedDecelerate` |
| FAB press | 100ms | `StandardDecelerate` |

- Respect `prefers-reduced-motion` / Android animator scale
- Ripple effect on ALL touchable elements
- Shared element transitions between list → detail

---

## 8. Touch & Accessibility

| Rule | Minimum |
|------|---------|
| Touch target | 48dp × 48dp |
| Touch spacing | 8dp between targets |
| Color contrast | 4.5:1 (WCAG AA) |
| Font scaling | Support up to 200% |
| Content descriptions | On all interactive elements |
| TalkBack | Full screen reader support |

### Thumb Zone Placement

- **Bottom (easy):** Tab bar, FAB, primary CTA
- **Middle (ok):** Content lists, cards, category rows
- **Top (stretch):** Navigation, settings, back

---

## 9. Data Display Conventions

| Type | Format | Example |
|------|--------|---------|
| Currency | `R X,XXX.XX` | `R 1,250.00` |
| Units | Integer | `15` |
| Percentage | `XX%` | `80%` |
| Date | `dd MMM yyyy` | `03 Mar 2026` |
| Time | `HH:mm` | `09:00` |
| Month | `MMMM yyyy` | `March 2026` |
| Empty target | `—` | _(dash, not 0%)_ |

---

## 10. Responsive Behavior

| Window Class | Width | Layout |
|--------------|-------|--------|
| Compact | < 600dp | Single column, bottom nav |
| Medium | 600–840dp | Two-column list-detail, nav rail |
| Expanded | > 840dp | Full list-detail, permanent nav |

---

## 11. Stitch Prompt Design Tokens

When generating screens in Stitch, always inject these tokens:

```
DESIGN SYSTEM (REQUIRED):
- Platform: Android, Mobile-first
- Theme: Dark, Material Design 3, data-dense dashboard
- Background: Deep Navy (#0F1117)
- Surface: Dark Card (#1A1D27) with 16dp rounded corners
- Primary Accent: Blue (#3B82F6) for FAB, active states, links
- Secondary: Light Blue (#60A5FA) for toggles, chips
- Tertiary/CTA: Orange (#F97316) for progress bars, badges, highlights
- Text Primary: Light Gray (#E8EAED)
- Text Secondary: Muted Blue-Gray (#9AA0B0)
- Typography: Fira Sans (headings/body), Fira Code (numeric values)
- Icons: Material Symbols Rounded, 24dp
- Touch targets: 48dp minimum
- Spacing: 8dp grid, 16dp card padding, 24dp section gaps
```
