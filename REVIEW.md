# Skiptap 6.3 — code and effectiveness review

Reviewed the saved 6.2 source, reproduced failures, then developed 6.3 from that source. This is a critical review of the code, not independent third-party certification. No physical Android phone or emulator was available.

## Verdict

Useful for a focused set of clearly defined visual targets. It is not a reliable universal ad-skipper: a matching symbol does not establish what its button does. The most valuable improvement is constraining where each image may match, coupled with correct source resolution and better scheduling. Training and Smart mode remain removed.

## Findings and changes

| Finding | Evidence / impact | Change in 6.3 |
|---|---|---|
| High: priority target can starve the rest | With a budget allowing only the first item in each batch, 6.2 examined 1 of 20 targets after 200 pulses. Existing tests missed this interaction. | Bounded priority and separate handling of unattempted versus timed-out items. New regression reaches 20/20. |
| High: wrong action despite good visual score | Your Open Store screenshot matched the optional skip-arrow template at 93.705%, at (60,112). | Per-image full/top-left/top-right/custom search areas. Restricting that arrow to top-right rejects this screenshot. This reduces ambiguity; it does not understand button intent. |
| High: imported screenshot scale mismatch | A known reference rendered at 2× size was missed by 6.2's 0.65–1.5× search. | Source screenshot width calibrates the scale range. Native synthetic 2× test passes. Quick save remembers capture width by default. |
| High: capture ownership and stale coordinates | Worker-busy checks did not cover screenshot callbacks in flight; age started after capture rather than at the screenshot timestamp. | Capture ownership tickets, generation invalidation, and a 2-second freshness check based on Android screenshot uptime timestamp, checked before analysis and dispatch. |
| Medium: 500 stored images are not 500 checks per pulse | A pulse processes only 1–4 images, subject to a processing budget. | Enable/disable images without deleting them; active counts and scan-cycle coverage feedback. |
| Medium: repeated full-resolution allocations | Tiny targets could each create another native-resolution frame in the same batch. | Reuse one full-resolution frame per batch. Phone memory/throughput impact remains unmeasured. |
| Medium: crop work lost during configuration recreation | Selection and mask edits were attached to the old activity. | Retain bitmap, selection and mask edits across activity recreation; regression verifies erased pixels survive. Process-death recovery is not included. |
| Medium: failed image writes could leave partial targets | Direct writes to final PNG names. | Write temporary file, flush/sync, rename; failed-write test verifies no partial target remains. |
| Medium: corrupted target settings could broaden search | Falling back to enabled/full-screen would lose restrictions. | Malformed options disable that target until repaired. |

Two-frame confirmation now defaults on when there is no saved preference; an explicit existing preference is preserved. Preview mode remains available before enabling taps. Package identity and signing certificate remain compatible with 6.2.

## What was tested

- Android assembleDebug, lintDebug and testDebugUnitTest.
- 18 Robolectric API 34 tests with native graphics: existing UI/crop/storage/handoff checks plus per-image settings, activation, source-width validation, corrupt options, failed image writes, settings dialog interaction and crop activity recreation.
- Seven pure-Java test programs: geometry, scheduling, timing, global regions, duplicate similarity, recoverable merge, capture ownership/freshness and the newly reproduced starvation scenario.
- Desktop native OpenCV matcher regression suite: exact pixels, brightness, scale, distractors, transparency, changing backgrounds, unchanged screens, tracking, exclusions, regional thresholds, 8×8 synthetic target, calibrated 2× size and per-image ROI constraints.
- Real screenshot replay: unrestricted Open Store arrow 93.705%; top-right restriction rejects it. A real close-button reference matches at (635,79), score 100%. The positive uses its source screenshot, so it verifies integration and coordinates—not unseen-ad accuracy.
- Native UI renders inspected for the crop controls and existing gallery/settings layout. Signature, package, version and permissions checked on the APK.

Desktop OpenCV is 4.9.0; APK uses Android OpenCV 4.12.0. These tests do not establish phone performance or long-run service reliability. Lint has zero errors and 53 warnings; remaining warnings include legacy View/accessibility conventions, localization and drawing allocations.

## Practical use

1. Install 6.3 over 6.2 without uninstalling. Existing images and global settings remain.
2. Tap an image in Images to open Image options. Disable unused images, optionally name them, and choose a search area.
3. For the supplied batch cropped from 709-pixel-wide screenshots, set source screenshot width to **709**, not the tiny crop width. Leave zero only when the original screenshot width is unknown or native pixel size is intentional.
4. Keep optional skip arrows limited to the appropriate corner. The same glyph may be an Open Store button. A location constraint cannot protect against a deceptive matching glyph inside that corner.
5. Use transparent masks to remove scenery; test with Tap on detection off, then enable taps after checking candidate locations.
6. Keep the active subset small. At 500 active images and four checks per 0.5-second pulse, even an ideal sweep takes about 62.5 seconds; at one check per pulse it takes 250 seconds. Processing limits and confirmation can add time. The 0.5-second setting is not a full-library scan guarantee.

## Remaining limits and phone acceptance checks

- No semantic understanding, no measured general false-positive rate, no universal minimum recognizable size. An 8×8 synthetic pass is not a promise for real compressed icons.
- Masks ignore selected background pixels but anti-aliased edge colors still depend on the background; low contrast, blur, rotation, changed styling and occlusion remain difficult.
- Exact 100% means retained pixel equality, not certainty about a button's action. Lower scores are similarity measures, not calibrated probabilities.
- Global accuracy regions use the candidate center; exclusions and per-image allowed zones constrain the complete bounding box.
- Android can deny secure screenshots, lock the display, suspend or disconnect the accessibility service. Retry logic cannot override these conditions. A dispatched/completed gesture does not prove the destination acted on it.
- Capture ownership and scheduling are unit-tested; actual screenshot/gesture callbacks, rapid Stop/restart, Quick save, orientation changes, overlays, OEM battery policy and sustained operation need phone testing.
- Before relying on automation, run representative ads in preview mode, record missed/wrong candidates, compare source-resolution settings, then test with taps enabled. Check 30–60 minutes of static and animated screens, Stop during capture, lock/unlock and service reconnection. Do not interpret this checklist as already completed.

API reference used for capture-age correction: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.ScreenshotResult#getTimestamp()
