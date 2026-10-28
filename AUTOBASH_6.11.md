# AutoBash 6.11 — fresh-frame stall fix

## Cause

The previous pipeline required a screenshot to be no more than two seconds old when an action was dispatched. If searching the active collection took longer than two seconds, it discarded every candidate and began another full search. A stationary, valid target could therefore remain visible while every result expired. Prioritizing that target in the next search did not solve the problem because the entire collection still had to finish.

This is a reproducible code path consistent with the reported “new frame required” symptom. No physical device logs were available to prove it was the only cause on the user's phone.

## Fix

- A slow discovery scan now retains its detected candidates as proposals.
- The next capture rechecks those candidates in small neighborhoods (approximately 12 original-screen pixels), using the same matching, masking, region and accuracy rules.
- All candidates from that discovery frame are reverified together; the highest eligible score from the fresh verification is selected.
- Only a genuinely fresh screenshot can authorize an action. The original two-second limit remains. Timestamps are not replaced with processing-completion times.
- Identical pixels in a newly captured screenshot are valid; screen movement is not required. A repeated screenshot timestamp cannot confirm itself.
- A successful fresh check supplies the second observation when two-frame confirmation is enabled.
- Verification uses a short capture interval (350 ms requested minimum between requests, subject to Android throttling) rather than waiting for the full user pulse interval.
- Missing or moved-out-of-range candidates return to normal full scanning. No action is sent at an unverified old position.
- Pause, Stop, editor transitions, target refresh and changed recognition settings invalidate pending work. Existing preview, gesture and overlay gates remain.
- Verification has a bounded search budget; an over-budget or stale result returns to normal scanning with a diagnostic rather than dispatching partial results.

The fresh verification revisits discovered candidates; it is not a new exhaustive search for previously unseen objects. Normal full scans resume afterward. Processing limits or repeated capture failures can still prevent action, and the app continues to retry instead of treating old pixels as current.

## Validation

Android tests cover a simulated 4.5-second discovery, subsequent fresh dispatch, pause/stop cancellation, changed accuracy, stationary repeated captures and bypassing a ten-second pulse wait. Native OpenCV tests cover stationary and shifted targets, disappearance, exclusion zones, per-target regions, masks, 100% exact matching, cancellation and deadlines. Existing recognition and UI regression suites also run.

Build successful; 102 Android tests passed with zero failures or skips, native regression suite passed, and lint reported zero errors. Final results are included in tests/release-6.11-evidence. Native timing is desktop-only, not a phone performance guarantee. Actual Android screenshot delivery and gesture acceptance still require physical-device testing.

Android's screenshot timestamp contract was checked against:
https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.ScreenshotResult#getTimestamp()

## Install

Install AutoBash_6.11.apk over the existing app without uninstalling. Package and signing certificate remain unchanged. Version code 19; version name 6.11. Saved images, presets, timelines and settings remain compatible.
