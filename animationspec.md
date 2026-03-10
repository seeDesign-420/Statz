# GlowLineChart — Energy Flow Animation Specification

## Purpose

Introduce a **subtle energy flow animation** to the `GlowLineChart` that enhances the perception of the glowing filament line without altering the geometry of the chart.

The animation should create the impression that **light energy is traveling along the line**, similar to:

* electricity moving through a filament
* light traveling through fiber optics
* energy pulsing through a neon tube

The chart itself must remain **structurally static**.
Only the **lighting behavior of the line** should animate.

The goal is to make the chart feel **alive and premium**, not animated like a typical chart.

---

# Design Philosophy

The animation must feel:

* subtle
* premium
* physical
* smooth
* continuous

It must **never look like a typical chart animation**.

Avoid:

* bouncing motion
* line morphing
* point transitions
* geometry movement
* exaggerated glow pulses

The animation should behave like **light moving through the filament**, not like the chart is changing shape.

---

# Visual Effect

A soft highlight should move along the curve from left to right.

The highlight must:

* be narrow
* be soft
* fade in and out
* blend with the glow
* never overpower the core line

The viewer should perceive a **traveling energy streak**, not a moving white stripe.

---

# Rendering Pipeline (Updated)

The energy flow animation must be added near the top of the rendering stack.

```text
1  dark base background
2  vignette
3  faint grid
4  atmospheric glow
5  bloom glow
6  apex bloom
7  tight halo glow
8  core filament line
9  energy flow highlight (animated)
10 specular highlight
11 cinematic noise overlay
```

The highlight should sit **above the core filament**, but **below the specular highlight**.

---

# Energy Flow Concept

The animation is created using a **moving gradient highlight** that travels along the X axis.

The gradient should contain:

* transparent edges
* a warm bright center
* soft falloff

Example conceptual gradient:

```text
transparent → warm highlight → transparent
```

This gradient moves horizontally across the chart.

When it overlaps the line, the filament appears temporarily brighter.

---

# Animation Behavior

The highlight should:

* move from left to right
* loop continuously
* move slowly
* fade naturally at edges

Recommended duration:

```text
2.6s – 3.4s
```

Recommended easing:

```text
LinearEasing
```

This maintains constant energy flow speed.

---

# Implementation Strategy

Use a **Compose infinite transition** to animate the gradient position.

The gradient offset should travel slightly beyond the visible canvas so the highlight fades smoothly in and out.

---

# Animation Setup

Create an infinite transition:

```kotlin
val energyTransition = rememberInfiniteTransition(label = "energyFlow")
```

Animate the horizontal offset:

```kotlin
val energyShift by energyTransition.animateFloat(
    initialValue = -200f,
    targetValue = size.width + 200f,
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = 3000,
            easing = LinearEasing
        )
    ),
    label = "energyShift"
)
```

The offset range extends beyond the canvas edges so the highlight does not abruptly appear or disappear.

---

# Energy Gradient Brush

Create a soft gradient centered on the animated offset.

Example gradient:

```kotlin
val energyBrush = Brush.linearGradient(
    colors = listOf(
        Color.Transparent,
        Color(0xFFFFC27A),
        Color.Transparent
    ),
    start = Offset(energyShift - 200f, 0f),
    end = Offset(energyShift + 200f, 0f)
)
```

This creates a **moving energy band**.

---

# Rendering the Energy Highlight

Render the highlight using the same chart path.

The stroke width should be **slightly thinner than the filament** so it does not overpower the base line.

Example:

```kotlin
drawPath(
    path = chartPath,
    brush = energyBrush,
    style = Stroke(
        width = filamentWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
)
```

The gradient will only be visible where it intersects the line.

---

# Visual Tuning

The highlight should be **soft and elegant**, not intense.

Recommended parameters:

| Property           | Suggested Range |
| ------------------ | --------------- |
| highlight alpha    | 0.35 – 0.55     |
| gradient width     | 160px – 260px   |
| animation duration | 2600ms – 3400ms |

Adjust visually until the motion feels smooth and natural.

---

# Avoid These Problems

The animation must **never**:

* appear as a bright white streak
* dominate the filament line
* flicker
* move too quickly
* reveal hard gradient edges

If the highlight becomes too obvious:

* reduce alpha
* increase gradient width
* slightly slow animation

---

# Performance Requirements

The animation must remain GPU-friendly.

Requirements:

* reuse `drawWithCache`
* avoid allocating brushes each frame
* only update the gradient offset
* keep the path cached
* avoid path recomputation

Target performance:

```text
60fps minimum
```

---

# Visual Acceptance Criteria

The implementation is correct when:

1. A soft highlight travels smoothly along the line.
2. The glow remains dominant while the highlight subtly enhances it.
3. The motion is calm and continuous.
4. The animation loops seamlessly.
5. The line appears energized rather than animated.

The viewer should interpret the effect as **energy flowing through the filament**.

---

# Summary

The Energy Flow animation adds life to the chart without altering its structure.

It works by introducing:

* a traveling highlight gradient
* smooth continuous motion
* subtle luminance variation along the filament

When implemented correctly, the chart will feel:

* alive
* luminous
* modern
* premium

without sacrificing the minimal aesthetic of the design.
