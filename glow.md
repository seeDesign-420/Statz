# GlowLineChart — Contrast and Tightening Pass Specification

## Purpose

The current `GlowLineChart` implementation is now structurally close to the target visual style:

- the curve shape is in the right family
- the layered glow stack is present
- the apex bloom has been introduced
- the card now reads as a premium chart rather than a generic line graph

However, it still does not fully match the intended reference because the rendering remains:

- too soft
- too hazy
- slightly too thick
- too low-contrast
- not yet disciplined enough around the filament

This refinement pass is focused on **contrast, precision, glow control, and visual tightening**.

The goal is not to add new major effects.
The goal is to **edit and rebalance the existing effects** so the chart looks cleaner, hotter, darker, and more premium.

---

# Objective

Refine the current `GlowLineChart` so that it visually reads as:

- a razor-hot filament line
- wrapped in a tight optical halo
- surrounded by a restrained cinematic bloom
- sitting on a near-black premium card
- with excellent separation between line, glow, grid, and background

The chart must feel more:

- precise
- luminous
- intentional
- premium
- contrast-rich

It must feel less:

- foggy
- blurred
- washed
- thick
- over-processed

---

# Current Problems

## 1. Glow Is Too Diffuse

The current glow field spreads too broadly across the card and softens the image too much.

This causes:

- lower perceived contrast
- reduced edge definition
- less premium optical feel
- too much orange presence away from the line

The glow should support the line, not flood the composition.

---

## 2. Filament Is Too Thick / Soft

The core filament line still feels slightly too broad and soft compared to the reference.

The reference has a line that feels:

- hot
- sharp
- disciplined
- concentrated

The current version is close, but still slightly too thick and gentle.

---

## 3. Background Is Not Dark Enough

The card background needs stronger darkness and better contrast separation from the line.

Right now the line glows nicely, but it does not yet feel like it is emerging from a sufficiently deep black field.

The line should feel brighter because the environment is darker.

---

## 4. Mid-Bloom Is Muddy

The middle glow layer is still contributing too much visual fog.

This creates:

- softness
- muddiness
- reduced precision
- flatter perceived image depth

The bloom must be reduced and tightened.

---

## 5. Apex Bloom Is Slightly Too Spread

The apex bloom concept is now correct, but it still needs refinement.

It should feel like a local intensification of light, not a broad orange cloud.

The bloom should become:

- smaller
- cleaner
- slightly more intense at center
- less spread laterally

---

## 6. Grid Is Still Slightly Too Visible

The grid is useful for subtle depth, but it must not compete with the line.

The grid should barely register.

It should feel more like texture than information.

---

## 7. Text Contrast Can Be Improved

The large primary value should feel crisp and dominant.

Secondary text should step back slightly more.

This helps reinforce the premium hierarchy of the card.

---

# Visual Target

The target chart must visually communicate:

- deep dark premium card
- subtle cinematic atmosphere
- bright hot orange line
- tight line-adjacent halo
- restrained ambient bloom
- smooth clean interpolation
- soft, minimal background structure
- polished contrast hierarchy

The viewer’s eye should immediately lock onto:

1. the main value
2. the glowing filament line
3. the curve’s movement

All supporting layers should remain subordinate.

---

# Rendering Philosophy

This pass should follow one principle:

## Reduce softness, increase discipline

That means:

- darker darks
- brighter filament
- tighter glow radii
- lower ambient alpha
- subtler secondary layers
- stronger line separation

Nothing should look “effect-heavy.”
Everything should feel controlled.

---

# Rendering Pipeline

Retain the same overall pipeline, but rebalance the strength of each stage.

Render order must remain:

```text
1. dark base background
2. radial vignette
3. faint grid
4. atmospheric glow strokes
5. bloom glow strokes
6. localized apex bloom
7. tight halo glow strokes
8. core filament line
9. subtle specular highlight
10. noise overlay

The structure is correct.
Only the tuning needs refinement.

Required Changes
1. Background Darkening
Goal

Increase contrast between the line and the card.

Required changes

Push the base card background closer to near-black.

Recommended range:

#050505 to #080808

Keep the background slightly lifted from true black so the composition still feels rich, but darker than the current implementation.

Vignette adjustments

The vignette should:

remain subtle

keep the center readable

darken the edges more confidently

avoid introducing visible gray haze

Intent

The line should feel brighter because the world around it is darker.

2. Atmospheric Glow Reduction
Goal

Reduce the broad orange fog effect.

Current issue

The atmospheric layer is too present and lowers contrast.

Required changes

Reduce all of the following slightly:

alpha

stroke width

blur radius

Recommended tuning direction

If current atmosphere values are approximately:

strokeWidth: 70px

alpha: 0.10

blurRadius: 60px

move toward:

strokeWidth: 52px to 60px

alpha: 0.05 to 0.08

blurRadius: 40px to 50px

Visual intent

This layer should be felt more than clearly seen.

It should create body around the line without visibly painting the card orange.

3. Bloom Pass Reduction
Goal

Make the intermediate glow layer cleaner and less muddy.

Current issue

The bloom pass spreads too much orange energy into the composition.

Required changes

Reduce:

stroke width

alpha

blur radius

Recommended tuning direction

If current bloom values are approximately:

strokeWidth: 35px

alpha: 0.18

blurRadius: 30px

move toward:

strokeWidth: 24px to 28px

alpha: 0.10 to 0.14

blurRadius: 18px to 24px

Visual intent

This layer should connect the atmosphere to the halo, not become a visible haze field of its own.

4. Halo Tightening
Goal

Concentrate the glow around the filament so the line feels more optical and less blurred.

Required changes

The halo should become:

slightly narrower

slightly cleaner

more clearly wrapped around the line

Recommended tuning
Outer halo

Move toward:

strokeWidth: 16px to 18px

alpha: 0.16 to 0.20

blurRadius: 16px to 20px

Tight halo

Move toward:

strokeWidth: 8px to 9px

alpha: 0.45 to 0.55

blurRadius: 7px to 9px

Visual intent

This is the most important glow zone.

It should tightly hug the filament and create the premium lit-edge effect.

5. Filament Core Refinement
Goal

Make the line feel hotter, thinner, and more premium.

Required changes

Reduce the filament width slightly.

If the current width is around:

2.4dp

move toward:

2.0dp to 2.2dp
Color tuning

The filament should stay in a tight warm range.

Avoid a decorative or overly obvious gradient.

Recommended warmth behavior:

bright warm amber near the hottest portions

strong orange body

slightly deeper orange toward ends if needed

no dramatic color travel

Suggested color range

Examples:

#FFB56A
#FF7A2C
#FF5E14
Visual intent

The line should feel incandescent, not painted.

6. Specular Highlight Reduction
Goal

Keep the highlight subtle enough that it reads as sheen, not as a second line.

Current issue

The highlight can easily become too visible and flatten the premium look.

Required changes

Keep the highlight extremely restrained.

Recommended direction:

width: 0.8dp to 1.0dp

alpha: 0.06 to 0.10

Visual intent

The highlight should only barely kiss the filament.

If you notice the highlight as a separate stroke, it is too strong.

7. Apex Bloom Tightening
Goal

Keep the apex glow, but make it more concentrated and less cloudy.

Current issue

The apex bloom is conceptually right, but still a touch too broad and soft.

Required changes

Refine the apex bloom so that it becomes:

smaller

tighter

slightly brighter at the center

less obviously oval-shaped

still fully diffused

Recommended tuning direction

If current apex bloom is approximately:

width: 14% of canvas width

height: 16% of canvas height

alpha: 0.18

blurRadius: 36px to 42px

move toward:

width: 9% to 12% of canvas width

height: 10% to 14% of canvas height

alpha: 0.14 to 0.18

blurRadius: 24px to 34px

Positioning

Keep the bloom centered at the apex, with only a very slight upward bias if visually helpful.

Suggested vertical offset:

-2px to -6px
Visual intent

The apex should feel hotter, not cloudier.

8. Grid Subtlety
Goal

Reduce the visibility of the grid.

Required changes

Lower grid alpha further.

If the current grid sits around:

0.03

move toward:

0.012 to 0.02
Additional guidance

keep lines thin

ensure they stay behind all glow passes

prevent the grid from reading as chart information

Visual intent

The grid should be barely perceptible in normal viewing.

9. Noise Reduction
Goal

Retain cinematic grain without dirtying the image.

Required changes

Reduce the effective strength of the noise layer if it is contributing to softness or grayness.

Recommended effective alpha range:

0.010 to 0.018
Visual intent

Noise should add texture to gradients, not visibly tint or veil the card.

10. Text Hierarchy Refinement
Goal

Improve overall premium contrast hierarchy in the card.

Main value

The primary value should remain:

crisp

dominant

bright white

Secondary label

The “Total Revenue” label should step back slightly.

Recommended visual behavior:

softer white/gray

lower alpha than the main value

still readable, but clearly secondary

Trend row

The trend percentage should remain strong.
The supporting “vs last month” text should remain subdued.

Visual intent

The eye should land first on:

main value

filament line

trend

Everything else should support those.

Geometry and Curve Discipline
Preserve smooth interpolation

The existing spline approach should remain smooth and premium.

The curve should avoid:

visible kinks

abrupt directional breaks

over-smoothed “rubber hose” behavior

Keep the line grounded

The curve should feel compositionally balanced inside the card.

Avoid excessive vertical compression or a line that floats awkwardly.

The chart should feel anchored and intentional.

Performance Constraints

This refinement must not introduce wasteful rendering behavior.

Requirements:

continue using drawWithCache

cache all paints

cache all paths

avoid per-frame allocations

avoid recalculating expensive objects during onDrawBehind

preserve smooth rendering performance

Target:

60fps minimum
Implementation Priorities

The agent should implement the changes in this order:

Priority 1

Reduce atmospheric haze and bloom spread.

Priority 2

Tighten halo and filament width.

Priority 3

Darken background and reduce grid visibility.

Priority 4

Refine apex bloom size and focus.

Priority 5

Fine-tune highlight and noise.

This order matters because most of the current mismatch comes from softness and over-spread glow, not missing features.

Acceptance Criteria

The refinement is correct when all of the following are true:

The chart feels darker overall.

The line feels thinner, hotter, and crisper.

The glow hugs the line more tightly.

The ambient orange haze is reduced.

The apex still glows, but more precisely.

The grid barely registers.

The card feels more premium and contrast-rich.

The chart no longer feels foggy or muddy.

The line clearly dominates the composition.

The result is visually closer to the target reference.

Summary

This pass is about editing down rather than building up.

The chart already contains the right effects.
Now those effects need stronger discipline.

To complete the refinement:

darken the card

reduce haze

tighten the halo

thin the filament

focus the apex bloom

reduce supporting layer visibility

The end result should feel like a hot filament glowing in darkness, not a blurred orange line on a dark card.