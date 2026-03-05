package com.statz.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Single source of truth for all animation specs across the Statz app.
 *
 * Naming convention:
 *  - "Micro"    → small state changes (toggles, chips, chevrons)       ~150 ms
 *  - "Standard" → normal transitions (nav slides, card reveals)        ~300 ms
 *  - "Slow"     → progress bars, large-area fills                      ~500 ms
 *  - "Spring"   → physics-based spring specs
 */
object StatzAnimation {

    // ── Springs ─────────────────────────────────────────────────
    /** TikTok-feel spring: snappy with slight bounce. Used by both pager
     *  fling and sub-screen nav transitions for a cohesive feel. */
    val NavSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 400f
    )

    /** Snappy spring for small interactive elements (toggles, tab indicators). */
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // ── Tweens ──────────────────────────────────────────────────
    /** Quick micro-transitions: chip selection, toggle thumb, chevron rotation. */
    fun <T> microTween() = tween<T>(durationMillis = 150, easing = FastOutSlowInEasing)

    /** Standard transitions: card reveals, nav slides. */
    fun <T> standardTween() = tween<T>(durationMillis = 300, easing = FastOutSlowInEasing)

    /** Slow transitions: progress bar fills. */
    fun <T> progressTween() = tween<T>(durationMillis = 500, easing = FastOutSlowInEasing)

    // ── Duration constants (for infinite/repeatable animations) ──
    const val MICRO_DURATION_MS = 150
    const val STANDARD_DURATION_MS = 300
    const val PROGRESS_DURATION_MS = 500
}
