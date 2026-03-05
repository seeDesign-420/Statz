# Statz UI Redesign — Findings & Comparison (Revised)

> Reference: Delivery tracking app (dark mode, status-tinted cards, bold KPIs, floating nav/headers, 4-icon grid)
> Current: Statz with OLED dark theme, Liquid Glass components, Neon Orange accent

---

## 1. Design DNA Extracted from Reference

| Trait | Reference App | Notes |
|---|---|---|
| **Background** | True OLED black (#000000) | Zero-compromise dark |
| **Typography** | Oversized bold KPI values (eg `$504.45`), small uppercase labels | Creates dramatic hierarchy |
| **Card Backgrounds** | **Tinted based on status** | Cards take on a deep, subtle tint of their status color. "Complete" is dark green, "Returned" is dark maroon. Neutral statuses stay dark grey. |
| **Floating Top Bar** | Disconnected top header (e.g., Location + Profile/Notifications) | Sits below the very top edge, looking like a floating pill/card rather than a full-width rigid bar. |
| **Quick Action Grid** | Horizontal row of 4 circular icon buttons with labels | Highly accessible shortcut area on the main dashboard. |
| **Filter tabs** | Pill-shaped, filled accent when selected, ghost when not | Prominent row at top of list views |
| **Search bar** | Inline dark pill with search icon, always visible on list screens | Not hidden behind icon toggle |
| **List items** | Product name (bold white) → metadata (grey) → **status badge** (right-aligned, colored pill) → chevron `>` | Clear scan pattern: left=info, right=status |
| **Status badges** | Pill-shaped, color-coded (outlined or filled) | Distinct badge matching the card's background tint |
| **Action buttons** | Accent-filled pill (`New delivery`) + ghost-outlined pill (`Track package`) side-by-side | Two tiers of prominence |
| **Floating Bottom Nav** | Suspended floating pill, not attached to the bottom edge | Modern "island" aesthetic |

---

## 2. Current Statz UI vs Reference

### 2.1 Sales Dashboard (`SalesDashboardScreen`)

| Element | Current | Needed Update |
|---|---|---|
| **Top Bar** | Rigid full-width `TopAppBar` | Create a Floating Top Bar holding Month/Year and navigation arrows. |
| **KPIs** | Boxy Material Cards | Replace with inline, oversized hero text (e.g. Sales total). |
| **Quick Actions** | Blank/Missing | Introduce the 4-icon grid (Daily Entry, Targets, New Query, New Task). |
| **Action Buttons** | Floating Action Button (FAB) | Replace FAB with side-by-side prominent action pill buttons (e.g. "Add Entry", "Edit Targets"). |

### 2.2 Queries List (`QueriesListScreen`)

| Element | Current | Needed Update |
|---|---|---|
| **Cards** | Standard grey glass cards | **Tint entire card background** based on Urgency/Status (e.g. deep maroon for Critical/Escalated). |
| **Filters** | Material `FilterChip` | Custom solid/ghost pill tabs matching the reference's `All|In Progress|...` |
| **Search** | Icon toggle | Always visible dark pill search bar below filters. |
| **Layout** | Status chip bottom left | Move status to a right-aligned pill badge. Add trailing chevron `>`. |

### 2.3 To-Do List (`TodoListScreen`)

| Element | Current | Needed Update |
|---|---|---|
| **Cards** | Standard grey glass cards | **Tint entire card background** based on Priority (e.g. dark red for High). Move status badge to right, add chevron. |

### 2.4 Bottom Navigation

| Element | Current | Needed Update |
|---|---|---|
| **Nav Bar** | GlassNavBar (pill shaped) | Ensure it floats above the bottom edge (margins) to match the reference "island" nav. |

---

## 3. Priority Implementation Steps

1. **Implement Status-Tinted Cards**
   - Update `StatzGlassCard` to accept a tint color.
   - For Queries: Tint based on Status (Open=Blue, Escalated=Red, Closed=Grey).
   - For Tasks: Tint based on Priority/Overdue (Overdue=Red, High=Orange, Normal=Grey).

2. **Sales Dashboard Reconstruction**
   - Add the **Floating Top Bar** (Month/Navigation).
   - Add **Hero Typography** for Total Units and Revenue.
   - Add side-by-side **Main Action Buttons** (e.g., "Daily Entry" + "Edit Targets").
   - Add the **4-Icon Quick Action Grid** below the main buttons.

3. **List Screen Enhancements**
   - Replace Material filter chips with **Pill Filter Tabs**.
   - Add the **Persistent Search Pill**.
   - Redesign card content layout (Title left, Right-aligned Status Badge, Chevron).

4. **Floating Bottom Nav**
   - Apply bottom and side margins to `GlassNavBar` so it floats as an island.
