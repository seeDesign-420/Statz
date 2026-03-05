package com.statz.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Seed Colors (DESIGN.md §2) ──────────────────────────────────
val Primary = Color(0xFFFF5A00) // Neon Orange (single accent)
val Secondary = Color(0xFFFF5A00) // Neon Orange
val Tertiary = Color(0xFFD9F522) // Neon Lime
val Error = Color(0xFFEF4444)
val Success = Color(0xFF22C55E)

// ── Dark Theme Surfaces ─────────────────────────────────────────
val DarkBackground = Color(0xFF050505) // OLED Black
val DarkSurface = Color(0x0DFFFFFF) // 5% White (Frosted Glass base)
val DarkSurfaceVariant = Color(0xFF171717) // Solid dark elevated
val DarkSurfaceContainer = Color(0xFF121212)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFA3A3A3)
val DarkOutline = Color(0xFF262626)
val DarkOutlineVariant = Color(0xFF1A1A1A)

// ── Light Theme Surfaces ────────────────────────────────────────
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F3F9)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFCBD5E1)

// ── Status / Semantic Colors ────────────────────────────────────
val StatusOpen = Color(0xFF3B82F6)
val StatusFollowUp = Color(0xFFF59E0B)
val StatusEscalated = Color(0xFFEF4444)
val StatusClosed = Color(0xFF6B7280)

val UrgencyLow = Color(0xFFB0BEC5)
val UrgencyMedium = Color(0xFFFFC107)
val UrgencyHigh = Color(0xFFFF9100)
val UrgencyCritical = Color(0xFFFF1744)
val UrgencyCustom = Color(0xFFA855F7)

// ── Urgency Glow Variants (neon-bright for Shadow halo) ─────────
val UrgencyLowGlow = Color(0xFFCFD8DC)
val UrgencyMediumGlow = Color(0xFFFFD740)
val UrgencyHighGlow = Color(0xFFFFAB00)
val UrgencyCriticalGlow = Color(0xFFFF1744)
val UrgencyCustomGlow = Color(0xFFC084FC)
