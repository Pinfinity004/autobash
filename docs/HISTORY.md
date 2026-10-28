# AutoBash 7.4

Adjustable simple and advanced floating-control sizes. See [7.4 notes](AUTOBASH_7.4.md).

# AutoBash 7.3

Dedicated Auto Tap with 1–20 movable targets, Fastest timing, Play/Pause/Stop, three saved layouts and an on-device received-tap test. See [release notes](AUTOBASH_7.3.md).

# AutoBash 7.2

Session recovery without resetting the action count, timed-plan progress, or floating controls. See [AUTOBASH_7.2.md](AUTOBASH_7.2.md).

# AutoBash 7.1

Fair rotating/interleaved scan scheduling and persistent drag-to-reorder target grids. See [AUTOBASH_7.1.md](AUTOBASH_7.1.md) for changes and verification.

# AutoBash 7.0

Parallel recognition with Balanced/Fast modes, exact-file search consolidation, persistent worker caches and recent-location checks. See AUTOBASH_7.0.md for setup, benchmark methodology and limits.

# AutoBash 6.11

Fix for full scans repeatedly expiring: retain proposals, capture a fresh screen, and reverify locally before dispatch. See AUTOBASH_6.11.md.

# AutoBash 6.10

Interface refinement: bundled typography, grouped settings, navigation drawer, consistent spacing and scalable action tiles. See AUTOBASH_6.10.md for design rationale and validation.

# Current release: AutoBash 6.9

See [AUTOBASH_6.9.md](AUTOBASH_6.9.md) for Quick save destinations, General markers, Timeline sequences and validation limits.

# Current release: AutoBash 6.8

See [AUTOBASH_6.8.md](AUTOBASH_6.8.md) for action collections, strongest-match selection, sharing compatibility and validation limitations.

# Current release: AutoBash 6.7

See AUTOBASH_6.7.md for the six new features, preset sharing, installation, and validation. Earlier release documentation below is retained for history.

# Current release: AutoBash 6.6

See `AUTOBASH_6.6.md` for the hybrid recognition pipeline, benchmark results, installation, and testing notes. Earlier release documentation below is retained for history.

# AutoBash 6.5

See AUTOBASH_6.5.md for this release’s identity, controls, installation, previews and validation. The following material documents the earlier Skiptap releases whose recognition functionality is retained.

# Skiptap 6.4.1

Install Skiptap_6.4.1.apk over 6.4 without uninstalling. Package and signing identity are unchanged. Android 11 or newer, ARM64 / x86_64. Version code 12. Existing images become Preset 1, with their saved image options intact.

## Tap delivery patch

See TAP_FIX_6.4.1.md for the independent confirmation fix, 100 ms default touch duration, persistent tap-status messages and the 33-test validation results. The following sections describe the retained 6.4 features and their original validation.

## What changed

### Pause / Resume

The floating control now has Quick save, Pause/Resume and Stop. Pause invalidates pending recognition results and stops scheduling scans. Resume starts scanning again. Manual pause survives service reconnection; Stop ends the session and clears it.

Quick save automatically pauses recognition while it captures a screenshot and opens the editor. Saving, cancelling or backing out of that editor releases the automatic pause. If you were already manually paused, you remain paused. Going Home while leaving an unfinished editor open keeps that editor's automatic pause; return and save/cancel it. An already dispatched touch cannot be recalled, and native computations cancel cooperatively at checkpoints.

### Stable floating control

The expanded control has a reserved three-line status area, fixed-height buttons and a single-line progress label. Switching between scanning, match details and pause no longer changes its height. Long text is truncated instead of growing the panel. Deliberately collapsing the control still changes its size.

### Every active image sees each captured frame

The rotating 1–4-target batches are removed. Each captured screenshot is retained while every active image is checked against it. Matches are reported as they are found, rather than waiting for the entire collection. The scanner keeps at most one active recognition job and does not build an unbounded screenshot queue.

The default checks the expected size, calculated from the source screenshot width. This is an intentional change from 6.3's automatic multi-size search. For your supplied crops from 709-pixel-wide screenshots, use source width 709. Quick save records the phone screenshot width automatically. Do not substitute the cropped icon width.

If the target can appear at other sizes, enable **Settings → Search alternate sizes**. This adds the existing multi-size search for unmatched targets after the expected-size pass. It improves size coverage but increases the interval until the next capture. Existing match accuracy, masks, per-image zones and global exclusions still apply; this update does not lower thresholds or add semantic training.

The progress label now says **Expected size · X/N**, or **Scale search · X/N** when enabled. These are checks against the same frame, not images postponed to future screenshots. **All N checked · … ms** reports elapsed time from screenshot capture. A previous candidate may be checked first on the next frame, but no other image is removed from that frame's collection.

### Freshness and practical limits

Every image being checked does not mean every comparison finishes instantly. A desktop regression checked 51 opaque synthetic references against one 720×1560 frame in approximately 1.3–1.4 seconds, finding the reference at index 51. That is a narrow desktop test, not a phone benchmark or a guarantee for masks, complex backgrounds, 500 images or alternate-size searches.

A match from a screenshot older than two seconds cannot trigger a tap. It is prioritized for checking on a fresh screenshot. Only one accepted gesture is sent per captured frame. If a control disappears before recognition finishes or appears entirely between captures, this implementation can still miss the opportunity to click. Slowing the scan interval, enabling two-frame confirmation, or enabling alternate-size search can increase latency. Keep active sets focused and use search areas to reduce work. Retaining a past screenshot cannot make a vanished control clickable.

### Three named presets

Open **Images → Presets**.

- **Open:** switch to that slot. Images and edits in each collection are retained automatically. Open an empty slot to start a new collection without deleting the old one.
- **Rename:** name the selected slot.
- **Save here:** copy the current collection into the selected slot and give it a name. The dialog explains that the destination is replaced. The current collection stays intact. Saving into the already active slot simply updates its name because its images are already saved.

Each slot supports up to 500 saved images, with its own enabled states, names, source widths and per-image search areas. Changes to one copy do not change another slot's copy. Global scan settings and global accuracy/exclusion regions remain shared, as stated in the preset dialog. Merge undo belongs to the individual collection.

Preset copies write into a new directory, then commit the slot pointer after the copy succeeds. A failed copy leaves the destination pointer unchanged. Images are not all decoded into memory during copying. Presets are on-device collections, not an external backup or export feature.

## Validation

- Android assembleDebug, lintDebug and testDebugUnitTest completed successfully.
- 24 Robolectric Android API 34 tests passed: existing UI/crop/storage checks plus independent manual/Quick save pause state, editor closure preserving manual pause, popup height stability, preset copy/options isolation, invalid save preserving the destination, and three-slot rendering.
- Eight pure Java regression programs and two native OpenCV test programs passed, including full-frame order coverage for 0/1/51/500 targets, fresh-candidate priority without omissions, retained-frame recognition at index 51, and cancellation in both matcher passes.
- Floating control and preset-dialog native renders visually inspected.
- Lint: zero errors, 56 warnings, including existing legacy View, localization and accessibility/drawing conventions.
- APK version, package and signing certificate checked. Certificate SHA-256: a864d8961f62522dcc7dd8db9ac1f62cd6ab7d8955988f4428b9aea0f8f67a72.

No physical phone or emulator was available. Actual Accessibility screenshot capture, gesture delivery, battery behavior, rapid transient targets, and extended operation still require phone testing. Desktop matcher tests use OpenCV 4.9.0; the APK uses Android OpenCV 4.12.0.

## Suggested first phone check

Keep Tap on detection off. Pause and verify the progress stops; Resume and verify it restarts. Use Quick save, cancel it, and verify scanning resumes; repeat after manually pausing and verify it stays paused. Open an empty preset, add an image, then switch back and check that the original images remain. Finally test the target that previously appeared near the end of the collection and note the displayed total scan time. Enable taps only after confirming the candidate locations.
