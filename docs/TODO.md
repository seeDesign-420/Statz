# Statz — Project To-Do Plan

> **Last updated:** 2026-03-06
> Organized by category, ordered by priority within each section.

---

## 🐛 Bugs

### Critical

- [ ] **Step-down broken after manual input (Daily Entry)**
  Entering a number manually into an input on `DailyEntryScreen` prevents the step-down button from working.

- [ ] **Step-down broken after manual input (Edit Targets)**
  Same issue as above but on `EditTargetsScreen` — step-down fails on manually entered values.

- [ ] **To-do notification not showing**
  Notifications for to-do items are not firing. Investigate notification scheduling, permissions, and channel configuration.

### Visual

- [ ] **Dialog boxes using wrong background tint colour**
  Dialog backgrounds are not matching the expected dark glass theme — likely a wrong color token or missing tint override.

- [ ] **Delete button artifact in dialog**
  The delete button inside dialog boxes has visual rendering artifacts. Inspect composable for clipping, padding, or layering issues.

---

## 🎨 UI / Design

- [ ] **Redesign to-do glass card to match query glass card**
  The `TodoListScreen` card style should be brought closer to the `QueriesListScreen` glass card — consistent shape, tint behaviour, and layout.

- [ ] **No visual feedback when saving a to-do item**
  Add a confirmation signal (snackbar, animation, haptic) after a to-do is saved so the user knows the action succeeded.

---

## ✨ Features

- [ ] **Wire up due date & reminders to to-do items**
  Due date and reminder fields exist but are not functional. Connect them to the data layer and schedule local notifications.

- [ ] **Allow editing a query's ticket number**
  There is currently no way to edit the ticket number on an existing query. Add an edit path in the query detail or edit screen.

---
