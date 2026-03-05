# Telecom Sales Companion (Offline) — spec.md
**Platform:** Android (Kotlin + Jetpack Compose)  
**Mode:** Offline-only (no network, no accounts)  
**Navigation:** Bottom Nav (4 tabs): Sales Stats • Queries • To-Do • Settings  
**Timezone:** Africa/Johannesburg

---

## 0) Goals & Non-Goals

### Goals
- Replace paper/notes/friction with a fast, offline workflow for:
  1) Monthly sales targets + daily entries + progress
  2) Query ticket tracking with urgency-based follow-up reminders
  3) Work to-do list with reminders
- Data safety: local persistence, simple backup/export/import

### Non-Goals (MVP)
- No cloud sync, no multi-device, no CRM integration
- No analytics backend; all calculations are local
- No complex charts required (basic progress + totals is enough)

---

## 1) UX Structure

### Bottom Navigation Tabs
1. **Sales Stats**
2. **Queries**
3. **To-Do**
4. **Settings**

### Common UX Requirements
- Fast input (defaults to 0 where applicable)
- Search + filters on list screens
- All edits persist immediately (no “wizard” flows)
- Money displayed as ZAR currency (R), stored as integer cents internally
- Dark mode compatible (use Material 3 defaults)

---

## 2) Tab 1 — Sales Stats

### 2.1 Categories (Seeded)
**Unit categories (counts)**
- New
- Upgrade
- SME New
- SME Up
- Employee Connect New
- Employee Connect Upgd
- Fiber
- Home WiFi Contract
- Home WiFi MTM
- Insurance

**Money categories (ZAR)**
- Accessories
- Cash Sales

> NOTE: If you later want “Home WiFi” as a single rollup, add it in v2 as a computed grouping. For MVP keep as above.

### 2.2 Definitions
- **T (Target):** Monthly target per category (editable at any time)
- **A (Actual):** Monthly actual per category = **sum of Daily Sales** values in that month
- **Open Orders:** Track counts for New + Upgrade per day (not part of A)
- **Declined:** Track counts for New + Upgrade per day (not part of A)

### 2.3 Screens

#### A) Sales Dashboard (Month View)
**Purpose:** show monthly progress and allow quick adjustments
- Month selector (default current month)
- Summary:
  - Total Units (sum of all unit categories’ A)
  - Total Revenue (Accessories A + Cash Sales A)
- Category rows (sorted in a stable order):
  - Name
  - Target (T)
  - Actual (A)
  - Progress (A/T as %, guard divide-by-zero)
  - Optional quick action:
    - Unit: +1 button
    - Money: +R button (opens small input dialog)

**Acceptance**
- Changing month updates all displayed values
- Progress display handles T=0 gracefully (show “—” or 0%)

#### B) Daily Entry (by Date)
**Purpose:** enter/update daily numbers quickly
- Date selector (default today)
- Inputs:
  - Daily sales fields for all seeded categories (units + money)
  - Open Orders: New, Upgrade
  - Declined: New, Upgrade
- Save updates/creates a record for that date

**Acceptance**
- Re-opening a date shows previously saved values
- Values default to 0 if none exist
- Save is idempotent (same date overwrites record)

#### C) Edit Targets (Month)
**Purpose:** edit monthly targets on the fly
- Month selector (same monthKey system as dashboard)
- Inline editable target per category
- Save immediately

**Acceptance**
- Targets persist per month
- Dashboard reflects changes instantly

### 2.4 Calculations
- `monthKey = YYYY-MM` (based on local timezone)
- `A(category, month) = SUM(dailySales[category]) for dates in month`
- Revenue displayed: `Accessories + CashSales`
- Store money in cents (long); render as `R` with 2 decimals

---

## 3) Tab 2 — Queries Tracker

### 3.1 Data Fields (per Query)
- Query Ticket Number (string; allow alphanumeric)
- Customer ID Number (string; preserve leading zeros)
- Customer Name (string)
- Status: `OPEN | FOLLOW_UP | ESCALATED | CLOSED`
- Urgency: `LOW | MEDIUM | HIGH | CRITICAL`
- Next Follow-Up DateTime (auto-managed, editable via snooze)
- Log entries (timestamp + note)

### 3.2 Screens

#### A) Queries List
- Search (ticket/customer ID/name)
- Filters (chips): Open, Follow-up, Escalated, Closed
- Sort: Urgency desc, then NextFollowUp asc, then UpdatedAt desc
- Row shows:
  - Ticket #
  - Customer name
  - Status chip
  - Urgency badge
  - “Next: <date/time>” if not closed

**Acceptance**
- Search and filters work together
- Closed items show Closed status; no follow-up due display

#### B) Query Detail
- Editable fields
- Status actions:
  - Mark Follow-up
  - Escalate
  - Close
- Snooze action:
  - +1h, +4h, Tomorrow 09:00 (or configurable options)
- Log timeline:
  - Add note (required to add a log entry)
  - Auto log status changes (at minimum store statusAfter + timestamp)

**Acceptance**
- Closing a query stops follow-up notifications
- Status changes update list immediately

#### C) New Query
- Minimal creation form
- Must set urgency + initial status (default OPEN)
- Set initial nextFollowUpAt based on urgency rules

### 3.3 Urgency Follow-Up Rules (MVP defaults)
Work hours default (editable in Settings):
- Mon–Fri: 08:00–17:30
- Weekends: notifications optional; default OFF (configurable)

Intervals:
- LOW: 3 days
- MEDIUM: 1 day
- HIGH: 4 hours (within work hours)
- CRITICAL: 1 hour (within work hours)

Rule:
- If query is not CLOSED and `nextFollowUpAt <= now` -> notify + advance `nextFollowUpAt` by interval (respect work hours)

---

## 4) Tab 3 — To-Do

### 4.1 Fields
- Title (required)
- Notes (optional)
- Due date/time (optional)
- Priority: `LOW | MEDIUM | HIGH`
- Done (boolean)

### 4.2 Screens

#### A) Today View
Sections:
- Overdue
- Today
- Upcoming
- Quick add at top

**Acceptance**
- Mark done removes from active sections (or moves to Done filter)

#### B) Task Detail
- Edit fields
- Toggle done
- Reminder toggle (only if dueAt is set)

**Acceptance**
- Due reminders notify at dueAt (or next worker tick if using periodic checks)

---

## 5) Tab 4 — Settings

### 5.1 Settings Items (MVP)
- Work hours:
  - Start time, end time
  - Enable weekends (bool)
- Notifications:
  - Queries reminders (on/off)
  - To-Do reminders (on/off)
- Sales:
  - Shortcut to “Edit Targets”
- Backup:
  - Export to local file (JSON)
  - Import from JSON (restore)

**Acceptance**
- Export creates a single JSON bundle containing all tables
- Import replaces local DB (with a confirmation dialog)

---

## 6) Data Model (Room)

### 6.1 Entities

#### SalesCategory
- id: String (stable slug, e.g. "new", "upgrade", "accessories")
- name: String
- type: Enum { UNIT, MONEY }
- sortOrder: Int
- isActive: Boolean (default true)

#### MonthlyTarget
- id: String (e.g. "${monthKey}_${categoryId}")
- monthKey: String (YYYY-MM)
- categoryId: String (FK)
- targetValue: Long (units or cents)
- updatedAt: Long (epoch millis)

#### DailySalesRecord
- dateKey: String (YYYY-MM-DD)
- monthKey: String (derived but stored for query efficiency)
- updatedAt: Long
- openOrdersNew: Int
- openOrdersUpgrade: Int
- declinedNew: Int
- declinedUpgrade: Int

#### DailySalesValue
- id: String (e.g. "${dateKey}_${categoryId}")
- dateKey: String (FK to DailySalesRecord)
- categoryId: String (FK)
- value: Long (units or cents)

> Reason: keep daily category values normalized rather than JSON-in-DB for easier queries.

#### QueryItem
- id: String (UUID)
- ticketNumber: String
- customerId: String
- customerName: String
- status: Enum
- urgency: Enum
- nextFollowUpAt: Long
- createdAt: Long
- updatedAt: Long
- closedAt: Long? (nullable)

#### QueryLogEntry
- id: String (UUID)
- queryId: String (FK)
- timestamp: Long
- note: String
- statusAfter: Enum? (nullable)

#### TaskItem
- id: String (UUID)
- title: String
- notes: String?
- dueAt: Long?
- priority: Enum
- isDone: Boolean
- createdAt: Long
- updatedAt: Long

### 6.2 Money Handling
- Store money as **cents** (Long)
- UI input accepts:
  - whole rands or rands.cents
  - internally convert to cents

---

## 7) Notifications (Android)

### 7.1 Mechanism (MVP)
- Use **WorkManager PeriodicWorkRequest** (every 15 minutes) to:
  - find due QueryItem where status != CLOSED and nextFollowUpAt <= now
  - find due TaskItem where dueAt <= now and not done (and reminder enabled)
- Post notifications with deep links to Query Detail / Task Detail.

### 7.2 Requirements
- Respect Settings toggles
- Respect work hours for query reminders
- When a query reminder fires:
  - auto-advance nextFollowUpAt by urgency interval
  - add a log entry like “Auto reminder sent” (optional but useful)

---

## 8) Compose UI Implementation Notes (Non-visual)
- Use Material3 + Navigation Compose
- Keep state via ViewModel + Flow
- Database via Room + Repository pattern
- No network permissions required
- Use DataStore for settings (work hours, toggles)

---

## 9) MVP Delivery Checklist (Acceptance Criteria)

### Sales
- [ ] Can edit monthly targets per category and month
- [ ] Can enter daily values and they roll up into monthly actuals
- [ ] Dashboard shows T/A/progress for all categories
- [ ] Open orders and declined tracked per day

### Queries
- [ ] Create/edit queries with status + urgency
- [ ] List search + filter + sort works
- [ ] Notifications fire for due follow-ups (respect settings/work hours)
- [ ] Snooze pushes nextFollowUpAt

### To-Do
- [ ] Create/edit tasks, mark done
- [ ] Due reminders notify (respect toggle)

### Settings + Backup
- [ ] Work hours editable
- [ ] Export JSON works
- [ ] Import JSON restores data (with confirmation)

---

## 10) Phase 2 Ideas (Explicitly Out of Scope)
- Charts and trends (weekly/monthly graphs)
- CSV export for sales
- Recurring tasks
- Category customization UI
- Attachments (screenshots/files) per query
- iOS client via KMP shared data layer

---