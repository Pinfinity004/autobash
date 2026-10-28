# AutoBash 7.1

## Changes
- Fair, non-linear scan order: each worker interleaves targets and rotates its starting point every full scan, including cancelled scans. Stable content-based worker assignment keeps caches reusable. Display order does not determine scan priority.
- Long-press a target image and drop it onto another target tile to move it to that position. Intervening targets shift; files are not renamed.
- Tap the ↕ control on a tile to enter a position anywhere in the collection, including another page.
- Preset order is saved atomically and retained through restarts, preset copies, and export/import. Existing presets keep their previous order until rearranged. Names, masks, enabled state, source resolution, and search areas stay with each image.
- Version 7.1 / build 21, same application ID and signing key as 7.0.

## What scan order does and does not change
Every full scan still checks every enabled target against one shared screenshot, with parallel workers and highest-score selection. An object that disappears while that screenshot is being processed remains visible in the captured image. Rotating order prevents fixed tail priority when work is interrupted; it does not guarantee detection of objects that appear and disappear entirely between captures, or reduce the total matching work. Existing fresh-frame confirmation remains required before an action.

A coprime interleaving stride visits every unique search exactly once per worker per scan. The starting index advances each time, so every target receives a first position over a complete rotation for a stable target set. Exact duplicates share recognition work while retaining separate action settings.

## Usage
Install over AutoBash 7.0 without uninstalling to preserve your presets. Open Actions, choose an action, then hold and drag an image onto its destination. Use ↕ for cross-page moves. Existing editing and deletion controls remain available. Dragging to an invalid area cancels the move.

## Verification
110 Android/Robolectric tests passed (zero failures/errors/skips). Native OpenCV regression suite passed; APK assembly succeeded; Android lint reported zero errors. Added tests cover moves in both directions, cross-page positioning of target 39, drag/drop and cancelled drag, metadata preservation, invalid positions, preset isolation, and copy/export/import order.

Native tests deliberately cancel each scan after its first search and verify that all 51 unique targets eventually receive that first search. Reversing display order produces the same content search sequence. Existing parallel parity, complete coverage, tiny targets, masks, strongest-match selection, fresh verification, and cancellation tests remain enabled.

Physical-device drag feel, accessibility navigation, and capture-to-action timing still require testing on your phone. No new phone speed guarantee is implied by this scheduling change.
