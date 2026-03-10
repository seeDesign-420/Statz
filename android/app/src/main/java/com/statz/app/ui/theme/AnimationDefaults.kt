package com.statz.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Single source of truth for all animation specs across the Statz app.
 *
 * **Spring-first design** — physics-based springs are preferred over
 * duration-based tweens because they are:
 *  - Frame-rate independent (fluid on 60/90/120 Hz)
 *  - Interruptible (re-targeting mid-flight feels natural)
 *  - Physically realistic (momentum, deceleration)
 *
 * Tuned for **premium feel** on 120 Hz displays:
 *  - Lower stiffness → smoother, more deliberate motion
 *  - Higher damping → refined settle with no jitter
 *  - Slight under-damping on transitions → subtle "live" quality
 */
object StatzAnimation {

    // ── Springs ─────────────────────────────────────────────────

    /** Quick micro-interactions: chip selection, toggle thumb, chevron rotation.
     *  Critically damped (no bounce), moderate stiffness for snappy but smooth. */
    fun <T> microSpring() = spring<T>(
        dampingRatio = 1.0f,
        stiffness = 800f
    )

    /** Screen transitions: nav slides, card reveals, content changes.
     *  Slightly underdamped for the tiniest "settle" — gives life to motion. */
    fun <T> standardSpring() = spring<T>(
        dampingRatio = 0.95f,
        stiffness = 350f
    )

    /** Smooth fills: progress bars, large-area animations.
     *  Critically damped with low stiffness for a gentle, satisfying sweep. */
    fun <T> easeSpring() = spring<T>(
        dampingRatio = 1.0f,
        stiffness = 200f
    )

    /** Playful entrances: toast pop-in, overlay scale, FAB reveal.
     *  Underdamped for a bouncy, delightful arrival. */
    fun <T> overshootSpring() = spring<T>(
        dampingRatio = 0.82f,
        stiffness = 400f
    )

    /** Nav-level spring for pager tab-swiping.
     *  Smooth convergence with a hint of natural settle. */
    val NavSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 400f
    )

    // ── Legacy tween helpers (for infinite/repeatable ONLY) ────

    /** Quick micro-tween — only for infiniteRepeatable animations. */
    fun <T> microTween() = tween<T>(durationMillis = 150, easing = FastOutSlowInEasing)

    /** Standard tween — only for infiniteRepeatable animations. */
    fun <T> standardTween() = tween<T>(durationMillis = 300, easing = FastOutSlowInEasing)

    /** Slow tween — only for infiniteRepeatable animations. */
    fun <T> progressTween() = tween<T>(durationMillis = 500, easing = FastOutSlowInEasing)

    // ── Duration constants (for infinite/repeatable animations) ──
    const val MICRO_DURATION_MS = 150
    const val STANDARD_DURATION_MS = 300
    const val PROGRESS_DURATION_MS = 500
}
